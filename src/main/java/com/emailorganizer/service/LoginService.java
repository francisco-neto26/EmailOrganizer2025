package com.emailorganizer.service;

import com.emailorganizer.model.ContaEmail;
import com.emailorganizer.model.RegrasClassificacao;
import com.emailorganizer.view.TelaPrincipal;
import com.emailorganizer.view.TelaSelecaoInicial;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.GmailScopes;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

public class LoginService {
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    public void autenticarComJson(String email, File credentialsFile) throws Exception {
        if (!credentialsFile.exists()) {
            throw new IOException("Arquivo credentials.json não encontrado: " + credentialsFile.getAbsolutePath());
        }

        // Lê as credenciais do arquivo
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new FileReader(credentialsFile));

        // Define o diretório de armazenamento de tokens
        File tokenDirectory = new File(credentialsFile.getParentFile(), "tokens_" + email);
        if (!tokenDirectory.exists()) {
            tokenDirectory.mkdirs();
        }

        // Cria o fluxo de autenticação
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                clientSecrets,
                Collections.singletonList(GmailScopes.GMAIL_MODIFY)
        )
                .setDataStoreFactory(new FileDataStoreFactory(tokenDirectory))
                .setAccessType("offline")
                .build();

        // Cria a conta de e-mail (sem senha, usa OAuth)
        ContaEmail conta = new ContaEmail(email, "", "imap.gmail.com", 993, true);

        // Regras de classificação (pode ser carregado de arquivo futuramente)
        RegrasClassificacao regras = new RegrasClassificacao();

        // Cria serviço Gmail e inicializa
        GmailService gmailService = new GmailService(conta, regras, flow);
        gmailService.inicializar();

// Tela de seleção inicial
        TelaSelecaoInicial selecao = new TelaSelecaoInicial(null);
        selecao.setVisible(true);

        boolean carregarEmails = selecao.isCarregarEmails();

// Serviço de automação de e-mails
        EmailAutomatorService emailAutomatorService = new EmailAutomatorService(gmailService);

// Criação da tela principal com os 5 argumentos
        TelaPrincipal principal = new TelaPrincipal(conta, regras, gmailService, emailAutomatorService, carregarEmails);
        principal.setVisible(true);

    }
}