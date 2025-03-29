package com.emailorganizer.service;

import com.emailorganizer.model.ContaEmail;
import com.emailorganizer.model.Email;
import com.emailorganizer.model.RegrasClassificacao;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;


import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

public class GmailService {
    private final ContaEmail contaEmail;
    private final RegrasClassificacao regras;
    private final GmailOAuthHelper oAuthHelper;
    private Gmail service;

    public GmailService(ContaEmail contaEmail, RegrasClassificacao regras,
                        String clientId, String clientSecret) {
        this.contaEmail = contaEmail;
        this.regras = regras;
        this.oAuthHelper = new GmailOAuthHelper(clientId, clientSecret, "Email Organizer");
    }

    /**
     * Inicializa o serviço Gmail
     */
    public void inicializar() throws Exception {
        this.service = oAuthHelper.getGmailService();
    }

    /**
     * Busca emails não lidos
     */
    public List<Email> buscarEmailsNaoLidos() throws Exception {
        if (service == null) {
            inicializar();
        }

        // Busca mensagens não lidas
        ListMessagesResponse response = service.users().messages().list("me")
                .setQ("is:unread")
                .execute();

        List<Email> emails = new ArrayList<>();
        if (response.getMessages() != null) {
            for (Message message : response.getMessages()) {
                // Obtém detalhes completos da mensagem
                Message fullMessage = service.users().messages().get("me", message.getId()).execute();
                Email email = converterParaEmail(fullMessage);
                emails.add(email);
            }
        }

        return emails;
    }

    /**
     * Converte uma mensagem da API do Gmail para o modelo Email
     */
    private Email converterParaEmail(Message message) throws Exception {
        // Extrai informações básicas
        String id = message.getId();
        String remetente = "";
        String assunto = "";
        Date data = new Date(message.getInternalDate());
        boolean lido = !message.getLabelIds().contains("UNREAD");

        // Extrai cabeçalhos
        List<MessagePartHeader> headers = message.getPayload().getHeaders();
        for (MessagePartHeader header : headers) {
            if (header.getName().equalsIgnoreCase("From")) {
                remetente = header.getValue();
            } else if (header.getName().equalsIgnoreCase("Subject")) {
                assunto = header.getValue();
            }
        }

        // Extrai conteúdo
        String conteudo = extrairConteudo(message.getPayload());

        // Cria o objeto Email
        Email email = new Email(id, remetente, contaEmail.getEmail(), assunto, conteudo, data, lido);
        email.setMarketing(regras.isEmailMarketing(email));

        return email;
    }

    /**
     * Extrai conteúdo de texto da mensagem
     */
    private String extrairConteudo(MessagePart part) {
        if (part.getMimeType().equals("text/plain") && part.getBody().getData() != null) {
            byte[] bytes = Base64.getUrlDecoder().decode(part.getBody().getData());
            return new String(bytes);
        }

        if (part.getParts() != null) {
            for (MessagePart subPart : part.getParts()) {
                String conteudo = extrairConteudo(subPart);
                if (!conteudo.isEmpty()) {
                    return conteudo;
                }
            }
        }

        return "";
    }

    /**
     * Marca um email como lido
     */
    public void marcarComoLido(Email email) throws Exception {
        if (service == null) {
            inicializar();
        }

        service.users().messages().modify("me", email.getId(),
                        new com.google.api.services.gmail.model.ModifyMessageRequest()
                                .setRemoveLabelIds(Collections.singletonList("UNREAD")))
                .execute();

        email.setLido(true);
    }
}