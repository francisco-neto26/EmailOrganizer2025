package com.emailorganizer.service;

import com.emailorganizer.utils.ConfiguracaoUtils;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.*;
import com.google.api.client.util.Base64;

import javax.mail.Message.RecipientType;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.*;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.Thread;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import com.emailorganizer.view.TelaPrincipal;
import com.emailorganizer.view.ProgressoDialog;

public class EmailAutomatorService {
    private final GmailService gmailService;

    public EmailAutomatorService(GmailService gmailService) {
        this.gmailService = gmailService;
    }

    public int marcarComoLidosEContarRemetentes() throws Exception {
        // Criar e mostrar a janela de progresso na thread da UI
        final ProgressoDialog progressoDialog = new ProgressoDialog(TelaPrincipal.getInstance(), "Processando E-mails");

        SwingUtilities.invokeLater(() -> {
            progressoDialog.setVisible(true);
        });

        try {
            Map<String, Integer> contadores = new HashMap<>();
            int totalProcessados = 0;

            ZonedDateTime dataInicio = ZonedDateTime.of(2005, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
            ZonedDateTime dataFinalAtual = ZonedDateTime.now(ZoneOffset.UTC);

            progressoDialog.atualizarStatus("Iniciando processamento de e-mails não lidos...");

            while (dataInicio.isBefore(dataFinalAtual)) {
                String query = String.format("is:unread after:%d before:%d",
                        dataInicio.toEpochSecond(),
                        dataFinalAtual.toEpochSecond());

                String pageToken = null;
                boolean encontrouEmails = false;

                do {
                    int limite = ConfiguracaoUtils.lerLimiteEmails();

                    progressoDialog.atualizarStatus("Buscando e-mails não lidos...");

                    ListMessagesResponse response = gmailService.getGmail().users().messages().list("me")
                            .setQ(query)
                            .setMaxResults((long) limite)
                            .setPageToken(pageToken)
                            .execute();

                    List<Message> messages = response.getMessages();
                    if (messages == null || messages.isEmpty()) break;

                    encontrouEmails = true;
                    long ultimaData = dataInicio.toEpochSecond();

                    final String mensagemEncontrados = "Encontrados " + messages.size() + " e-mails para processar";
                    progressoDialog.atualizarStatus(mensagemEncontrados);

                    // Pequena pausa para garantir que a UI seja atualizada
                    Thread.sleep(100);

                    for (int i = 0; i < messages.size(); i++) {
                        Message m = messages.get(i);

                        // Atualizar o status com o progresso atual
                        final int idx = i + 1;
                        final String mensagemProcessando = "Processando e-mail " + idx + " de " + messages.size();
                        progressoDialog.atualizarStatus(mensagemProcessando);

                        // Forçar uma pequena pausa para garantir que a UI seja atualizada
                        if (i % 10 == 0) {
                            Thread.sleep(50);
                        }

                        Message email = gmailService.getGmail().users().messages().get("me", m.getId())
                                .setFormat("metadata")
                                .setMetadataHeaders(List.of("From"))
                                .execute();

                        String remetente = extrairRemetente(email);
                        contadores.put(remetente, contadores.getOrDefault(remetente, 0) + 1);

                        LocalDate dataLimite = LocalDate.of(2022, 7, 1);
                        Instant instant = Instant.ofEpochMilli(email.getInternalDate());
                        ZonedDateTime dataEmail = instant.atZone(ZoneOffset.UTC);
                        LocalDate localDataEmail = dataEmail.toLocalDate();

                        //System.out.println("date: " + dataEmail + " data: " + dataLimite + " teste " + localDataEmail + " teste 2 " + localDataEmail.isBefore(dataLimite));
                        if (localDataEmail.isBefore(dataLimite)) {
                            final String msg = "Excluindo e-mail de " + remetente + " anterior a 01/07/2022";
                            progressoDialog.atualizarStatus(msg);
                            gmailService.excluirEmail(email.getId());
                        } else {
                            final String msg = "Marcando como Lido o e-mail de " + remetente;
                            progressoDialog.atualizarStatus(msg);
                            gmailService.marcarComoLido(email.getId());
                        }

                        long internalDate = email.getInternalDate();
                        if (internalDate > ultimaData) {
                            ultimaData = internalDate;
                        }

                        totalProcessados++;
                        if (totalProcessados % 50 == 0) {
                            final String msgTotal = "Total processado: " + totalProcessados + " e-mails";
                            progressoDialog.atualizarStatus(msgTotal);

                            if (totalProcessados % 5000 == 0) {
                                enviarNotificacao("Processados " + totalProcessados + " e-mails até agora.");
                            }
                        }
                    }

                    pageToken = response.getNextPageToken();
                    dataFinalAtual = ZonedDateTime.ofInstant(Instant.ofEpochSecond(ultimaData), ZoneOffset.UTC).minusDays(1);

                } while (pageToken != null);

                if (!encontrouEmails) break; // não há mais e-mails não lidos para processar
            }

            atualizarContadorRemetentes(contadores);
            enviarNotificacao("Processamento concluído. Total: " + totalProcessados + " e-mails.");
            progressoDialog.atualizarStatus("Processamento concluído. Total: " + totalProcessados + " e-mails");

            // Pequena pausa para permitir que a mensagem final seja vista
            Thread.sleep(2000);

            return totalProcessados;
        } finally {
            // Garantir que a janela de progresso seja fechada ao final
            SwingUtilities.invokeLater(() -> {
                progressoDialog.dispose();
            });
        }
    }

    private String extrairRemetente(Message email) {
        return email.getPayload().getHeaders().stream()
                .filter(h -> h.getName().equalsIgnoreCase("From"))
                .map(MessagePartHeader::getValue)
                .findFirst()
                .orElse("Desconhecido");
    }

    private void atualizarContadorRemetentes(Map<String, Integer> novosContadores) throws IOException {
        String caminhoCredenciais = ConfiguracaoUtils.lerCaminhoCredenciais();
        if (caminhoCredenciais == null || caminhoCredenciais.isBlank()) {
            throw new IOException("Caminho das credenciais não está configurado.");
        }

        File arquivoCredenciais = new File(caminhoCredenciais);
        File diretorio = arquivoCredenciais.getParentFile();
        File arquivoContador = new File(diretorio, "contadores_por_remetente.txt");

        Map<String, Integer> contadoresExistentes = new HashMap<>();
        if (arquivoContador.exists()) {
            List<String> linhas = Files.readAllLines(arquivoContador.toPath(), StandardCharsets.UTF_8);
            for (String linha : linhas) {
                String[] partes = linha.split(":", 2);
                if (partes.length == 2) {
                    String remetente = partes[0].trim();
                    int count = Integer.parseInt(partes[1].trim());
                    contadoresExistentes.put(remetente, count);
                }
            }
        }

        for (Map.Entry<String, Integer> entry : novosContadores.entrySet()) {
            contadoresExistentes.merge(entry.getKey(), entry.getValue(), Integer::sum);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(arquivoContador.toPath(), StandardCharsets.UTF_8)) {
            for (Map.Entry<String, Integer> entry : contadoresExistentes.entrySet()) {
                writer.write(entry.getKey() + ": " + entry.getValue());
                writer.newLine();
            }
        }
    }

    private void enviarNotificacao(String mensagem) {
        try {
            MimeMessage email = criarMensagem("me", "neto.ludwig@hotmail.com", "EmailOrganizer - Notificação", mensagem);
            gmailService.getGmail().users().messages().send("me", criarMensagemGmail(email)).execute();
        } catch (Exception e) {
            System.err.println("Erro ao enviar notificação: " + e.getMessage());
        }
    }

    private MimeMessage criarMensagem(String de, String para, String assunto, String corpo) throws Exception {
        Properties props = new Properties();
        Session session = Session.getInstance(props);
        MimeMessage email = new MimeMessage(session);

        email.setFrom(new InternetAddress(de));
        email.addRecipient(RecipientType.TO, new InternetAddress(para));
        email.setSubject(assunto);
        email.setText(corpo);
        return email;
    }

    private Message criarMensagemGmail(MimeMessage email) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        email.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(bytes);

        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }
}
