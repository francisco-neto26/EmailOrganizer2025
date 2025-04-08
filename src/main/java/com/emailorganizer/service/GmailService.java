package com.emailorganizer.service;

import com.emailorganizer.model.ContaEmail;
import com.emailorganizer.model.Email;
import com.emailorganizer.model.RegrasClassificacao;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.*;

import java.text.SimpleDateFormat;
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
                        //.setAddLabelIds(Collections.singletonList("ARCHIVE"))
                        .setRemoveLabelIds(Collections.singletonList("INBOX"))).execute();
    }

    public void excluirEmail(String emailId) throws Exception {
        if (service == null) inicializar();
        service.users().messages().trash("me", emailId).execute();
    }

    public List<Map<String, Object>> buscarEmails(String assunto, String remetente,
                                                  String tipo, Boolean lido, Date[] dataRange,
                                                  int limite) throws Exception {
        if (service == null) inicializar();

        System.out.println("\n[DEBUG] === Início da busca de e-mails ===");
        System.out.println("[DEBUG] Assunto: " + assunto);
        System.out.println("[DEBUG] Remetente: " + remetente);
        System.out.println("[DEBUG] Tipo: " + tipo);
        System.out.println("[DEBUG] Status (lido): " + lido);
        System.out.println("[DEBUG] Data De: " + (dataRange != null ? dataRange[0] : "null"));
        System.out.println("[DEBUG] Data Até: " + (dataRange != null ? dataRange[1] : "null"));
        System.out.println("[DEBUG] Limite: " + limite);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        StringBuilder query = new StringBuilder();

        if (assunto != null && !assunto.isEmpty()) query.append("subject:").append(assunto).append(" ");
        if (remetente != null && !remetente.isEmpty()) query.append("from:").append(remetente).append(" ");
        if (lido != null) query.append(lido ? "is:read " : "is:unread ");

        if (dataRange != null && dataRange.length == 2) {
            if (dataRange[0] != null) query.append("after:").append(sdf.format(dataRange[0])).append(" ");
            if (dataRange[1] != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(dataRange[1]);
                cal.add(Calendar.DAY_OF_MONTH, 1); // inclui o último dia
                query.append("before:").append(sdf.format(cal.getTime())).append(" ");
            }
        }

        System.out.println("[DEBUG] Query final da API: \"" + query.toString().trim() + "\"");

        ListMessagesResponse response = service.users().messages().list("me")
                .setQ(query.toString().trim())
                .setMaxResults((long) limite)
                .execute();

        List<Map<String, Object>> resultados = new ArrayList<>();
        int totalRetornados = 0;
        int puladosPorData = 0;
        int puladosPorTipo = 0;

        if (response.getMessages() != null) {
            for (Message msg : response.getMessages()) {
                if (resultados.size() >= limite) break;

                Message full = service.users().messages().get("me", msg.getId()).execute();
                totalRetornados++;

                Map<String, Object> mapa = new HashMap<>();
                mapa.put("id", full.getId());

                String from = "", subject = "";
                for (MessagePartHeader header : full.getPayload().getHeaders()) {
                    if ("From".equalsIgnoreCase(header.getName())) from = header.getValue();
                    if ("Subject".equalsIgnoreCase(header.getName())) subject = header.getValue();
                }

                Date data = new Date(full.getInternalDate());
                boolean isRead = !full.getLabelIds().contains("UNREAD");

                mapa.put("from", from);
                mapa.put("subject", subject);
                mapa.put("date", data);
                mapa.put("read", isRead);

                String tipoEmail = classificarEmail(from, subject);
                mapa.put("type", tipoEmail);

                //System.out.println("[EMAIL] Data: " + data + " | Assunto: " + subject + " | Tipo: " + tipoEmail);

                if (tipo != null && !"Todos".equalsIgnoreCase(tipo) && !tipo.equals(tipoEmail)) {
                    puladosPorTipo++;
                    continue;
                }

                resultados.add(mapa);
            }
        }

        System.out.println("[DEBUG] Total retornados da API: " + totalRetornados);
        System.out.println("[DEBUG] Incluídos no resultado final: " + resultados.size());
        System.out.println("[DEBUG] Pulados por tipo: " + puladosPorTipo);
        System.out.println("[DEBUG] === Fim da busca ===\n");

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
