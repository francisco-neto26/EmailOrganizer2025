package com.emailorganizer.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;

import java.util.Properties;
import java.io.File;
import java.util.Collections;
import java.util.List;

public class GmailOAuthHelper {
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList("https://www.googleapis.com/auth/gmail.modify");

    private final GoogleAuthorizationCodeFlow flow;
    private final String applicationName;

    public GmailOAuthHelper(GoogleAuthorizationCodeFlow flow, String applicationName) {
        this.flow = flow;
        this.applicationName = applicationName;
    }

    public Gmail getGmailService() throws Exception {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        // Usa fluxo já criado (LoginService é quem cria)
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

        return new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(applicationName)
                .build();
    }

    public Properties getOAuthProperties(String email) {
        Properties props = new Properties();
        props.put("mail.imap.ssl.enable", "true");
        props.put("mail.imap.auth.mechanisms", "XOAUTH2");
        props.put("mail.imap.host", "imap.gmail.com");
        props.put("mail.imap.port", "993");
        props.put("mail.imap.auth.xoauth2.disable", "false");
        return props;
    }
}