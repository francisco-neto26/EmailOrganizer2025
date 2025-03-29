
// Pacote de serviço (Service)
package com.emailorganizer.service;

import com.emailorganizer.model.ContaEmail;
import com.emailorganizer.model.Email;
import com.emailorganizer.model.RegrasClassificacao;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.search.FlagTerm;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Serviço para conexão e manipulação de emails via IMAP
 */
public class EmailService {
    private ContaEmail contaEmail;
    private RegrasClassificacao regras;

    public EmailService(ContaEmail contaEmail, RegrasClassificacao regras) {
        this.contaEmail = contaEmail;
        this.regras = regras;
    }

    /**
     * Conecta ao servidor de email

     public Store conectar() throws MessagingException {
     Properties props = new Properties();
     props.put("mail.store.protocol", "imaps");
     props.put("mail.imaps.host", contaEmail.getServidorImap());
     props.put("mail.imaps.port", contaEmail.getPorta());
     props.put("mail.imaps.ssl.enable", contaEmail.isUsarSsl());

     Session session = Session.getInstance(props);
     Store store = session.getStore("imaps");
     store.connect(contaEmail.getServidorImap(), contaEmail.getEmail(), contaEmail.getSenha());

     return store;
     }

     */

    public Store conectar() throws MessagingException {
        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.host", contaEmail.getServidorImap());
        props.put("mail.imaps.port", contaEmail.getPorta());
        props.put("mail.imaps.ssl.enable", contaEmail.isUsarSsl());

        // Adicione estas propriedades para resolver o erro de protocolo
        props.put("mail.imaps.ssl.protocols", "TLSv1.2");
        props.put("mail.imaps.ssl.trust", "*");
        props.put("mail.imaps.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        // Opcionalmente para debug
        // props.put("mail.debug", "true");

        Session session = Session.getInstance(props);
        Store store = session.getStore("imaps");
        store.connect(contaEmail.getServidorImap(), contaEmail.getEmail(), contaEmail.getSenha());

        return store;
    }

    /**
     * Busca emails não lidos
     */
    public List<Email> buscarEmailsNaoLidos() throws MessagingException {
        Store store = conectar();
        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_WRITE);

        // Busca por emails não lidos
        FlagTerm ft = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
        Message[] messages = inbox.search(ft);

        List<Email> emails = converterMensagens(messages);

        inbox.close(false);
        store.close();

        return emails;
    }

    /**
     * Busca emails por período e/ou remetente
     */
    public List<Email> buscarEmails(Date dataInicio, Date dataFim, String remetente) throws MessagingException {
        Store store = conectar();
        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_WRITE);

        // Busca todos os emails
        Message[] messages = inbox.getMessages();

        List<Email> emails = new ArrayList<>();
        for (Message message : messages) {
            // Filtra por data
            Date messageDate = message.getReceivedDate();
            boolean incluirPorData = true;

            if (dataInicio != null && messageDate.before(dataInicio)) {
                incluirPorData = false;
            }

            if (dataFim != null && messageDate.after(dataFim)) {
                incluirPorData = false;
            }

            // Filtra por remetente
            Address[] from = message.getFrom();
            String fromAddress = "";
            if (from != null && from.length > 0) {
                fromAddress = ((InternetAddress) from[0]).getAddress();
            }

            boolean incluirPorRemetente = true;
            if (remetente != null && !remetente.isEmpty() && !fromAddress.contains(remetente)) {
                incluirPorRemetente = false;
            }

            if (incluirPorData && incluirPorRemetente) {
                // Converte a mensagem em um objeto Email
                String id = message.getMessageNumber() + "";
                String assunto = message.getSubject();
                String conteudo = obterConteudo(message);
                boolean lido = message.isSet(Flags.Flag.SEEN);

                Email email = new Email(id, fromAddress, contaEmail.getEmail(),
                        assunto, conteudo, messageDate, lido);

                // Classifica se é marketing
                email.setMarketing(regras.isEmailMarketing(email));

                emails.add(email);
            }
        }

        inbox.close(false);
        store.close();

        return emails;
    }

    /**
     * Marca emails como lidos
     */
    public void marcarComoLidos(List<Email> emails) throws MessagingException {
        if (emails == null || emails.isEmpty()) {
            return;
        }

        Store store = conectar();
        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_WRITE);

        for (Email email : emails) {
            // Busca a mensagem pelo ID
            Message[] messages = inbox.getMessages();
            for (Message message : messages) {
                if (message.getMessageNumber() == Integer.parseInt(email.getId())) {
                    // Marca como lido
                    message.setFlag(Flags.Flag.SEEN, true);
                    email.setLido(true);
                    break;
                }
            }
        }

        inbox.close(true);
        store.close();
    }

    /**
     * Converte mensagens do JavaMail para nosso modelo
     */
    private List<Email> converterMensagens(Message[] messages) throws MessagingException {
        List<Email> emails = new ArrayList<>();

        for (Message message : messages) {
            String id = message.getMessageNumber() + "";

            // Obtém o remetente
            Address[] from = message.getFrom();
            String fromAddress = "";
            if (from != null && from.length > 0) {
                fromAddress = ((InternetAddress) from[0]).getAddress();
            }

            String assunto = message.getSubject();
            Date data = message.getReceivedDate();
            String conteudo = obterConteudo(message);
            boolean lido = message.isSet(Flags.Flag.SEEN);

            Email email = new Email(id, fromAddress, contaEmail.getEmail(),
                    assunto, conteudo, data, lido);

            // Classifica se é marketing
            email.setMarketing(regras.isEmailMarketing(email));

            emails.add(email);
        }

        return emails;
    }

    /**
     * Extrai o conteúdo do email
     */
    private String obterConteudo(Message message) {
        try {
            Object content = message.getContent();
            if (content instanceof String) {
                return (String) content;
            } else if (content instanceof Multipart) {
                return extrairTextoMultipart((Multipart) content);
            }
        } catch (Exception e) {
            return "Não foi possível extrair o conteúdo do email.";
        }

        return "";
    }

    /**
     * Extrai texto de um multipart
     */
    private String extrairTextoMultipart(Multipart multipart) throws Exception {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);

            if (bodyPart.getContentType().toLowerCase().contains("text/plain")) {
                result.append(bodyPart.getContent().toString());
            } else if (bodyPart.getContent() instanceof Multipart) {
                result.append(extrairTextoMultipart((Multipart) bodyPart.getContent()));
            }
        }

        return result.toString();
    }
}