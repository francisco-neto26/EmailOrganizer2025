package com.emailorganizer.service;

import com.emailorganizer.model.ContaEmail;
import com.emailorganizer.model.Email;
import com.emailorganizer.model.RegrasClassificacao;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.*;

import java.util.*;
import java.util.Base64;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GmailService {
    private final ContaEmail contaEmail;
    private final RegrasClassificacao regras;
    private final GoogleAuthorizationCodeFlow flow;
    private Gmail service;

    public GmailService(ContaEmail contaEmail, RegrasClassificacao regras, GoogleAuthorizationCodeFlow flow) {
        this.contaEmail = contaEmail;
        this.regras = regras;
        this.flow = flow;
    }

    public void inicializar() throws Exception {
        GmailOAuthHelper oAuthHelper = new GmailOAuthHelper(flow, "EmailOrganizer");
        this.service = oAuthHelper.getGmailService();
    }

    public List<Email> buscarEmailsNaoLidos() throws Exception {
        if (service == null) inicializar();

        ListMessagesResponse response = service.users().messages().list("me")
                .setQ("is:unread")
                .execute();

        List<Email> emails = new ArrayList<>();
        if (response.getMessages() != null) {
            for (Message msg : response.getMessages()) {
                Message fullMessage = service.users().messages().get("me", msg.getId()).execute();
                emails.add(converterParaEmail(fullMessage));
            }
        }
        return emails;
    }

    private Email converterParaEmail(Message message) throws Exception {
        String id = message.getId();
        String remetente = "", assunto = "";
        Date data = new Date(message.getInternalDate());
        boolean lido = !message.getLabelIds().contains("UNREAD");

        for (MessagePartHeader header : message.getPayload().getHeaders()) {
            if ("From".equalsIgnoreCase(header.getName())) remetente = header.getValue();
            if ("Subject".equalsIgnoreCase(header.getName())) assunto = header.getValue();
        }

        String conteudo = extrairConteudo(message.getPayload());
        Email email = new Email(id, remetente, contaEmail.getEmail(), assunto, conteudo, data, lido);
        email.setMarketing(regras.isEmailMarketing(email));
        return email;
    }

    private String extrairConteudo(MessagePart part) {
        if ("text/plain".equalsIgnoreCase(part.getMimeType()) && part.getBody().getData() != null) {
            byte[] bytes = Base64.getUrlDecoder().decode(part.getBody().getData());
            return new String(bytes);
        }

        if (part.getParts() != null) {
            for (MessagePart sub : part.getParts()) {
                String content = extrairConteudo(sub);
                if (!content.isEmpty()) return content;
            }
        }
        return "";
    }

    public void marcarComoLido(String emailId) throws Exception {
        if (service == null) inicializar();
        service.users().messages().modify("me", emailId,
                new ModifyMessageRequest().setRemoveLabelIds(Collections.singletonList("UNREAD"))).execute();
    }

    public void arquivarEmail(String emailId) throws Exception {
        if (service == null) inicializar();
        service.users().messages().modify("me", emailId,
                new ModifyMessageRequest()
                        .setAddLabelIds(Collections.singletonList("ARCHIVE"))
                        .setRemoveLabelIds(Collections.singletonList("INBOX"))).execute();
    }

    public List<Map<String, Object>> buscarEmails(String assunto, String remetente,
                                                  String tipo, Boolean lido, Date[] dataRange,
                                                  int limite) throws Exception {
        if (service == null) inicializar();

        StringBuilder query = new StringBuilder();
        if (assunto != null && !assunto.isEmpty()) query.append("subject:").append(assunto).append(" ");
        if (remetente != null && !remetente.isEmpty()) query.append("from:").append(remetente).append(" ");
        if (lido != null) query.append(lido ? "is:read " : "is:unread ");

        ListMessagesResponse response = service.users().messages().list("me")
                .setQ(query.toString())
                .execute();

        List<Map<String, Object>> resultados = new ArrayList<>();
        if (response.getMessages() != null) {
            for (Message msg : response.getMessages()) {
                if (resultados.size() >= limite) break;

                Message full = service.users().messages().get("me", msg.getId()).execute();
                Map<String, Object> mapa = new HashMap<>();
                mapa.put("id", full.getId());

                for (MessagePartHeader header : full.getPayload().getHeaders()) {
                    if ("From".equalsIgnoreCase(header.getName())) mapa.put("from", header.getValue());
                    if ("Subject".equalsIgnoreCase(header.getName())) mapa.put("subject", header.getValue());
                }

                Date data = new Date(full.getInternalDate());
                mapa.put("date", data);

                if (dataRange != null && dataRange.length == 2) {
                    if (dataRange[0] != null && data.before(dataRange[0])) continue;
                    if (dataRange[1] != null && data.after(dataRange[1])) continue;
                }

                boolean isRead = !full.getLabelIds().contains("UNREAD");
                mapa.put("read", isRead);

                String tipoEmail = classificarEmail((String) mapa.get("from"), (String) mapa.get("subject"));
                mapa.put("type", tipoEmail);

                if (tipo != null && !tipo.equals(tipoEmail)) continue;

                resultados.add(mapa);
            }
        }

        return resultados;
    }

    private String classificarEmail(String remetente, String assunto) {
        if (regras.isEmailTrabalho(remetente, assunto)) return "Trabalho";
        if (regras.isEmailFinanceiro(remetente, assunto)) return "Financeiro";
        if (regras.isEmailRedesSociais(remetente, assunto)) return "Redes Sociais";
        if (regras.isEmailPromocional(remetente, assunto)) return "Promocional";
        return "Pessoal";
    }
}
