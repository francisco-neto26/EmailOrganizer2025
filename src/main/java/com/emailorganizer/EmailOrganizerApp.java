// Classe principal do aplicativo
package com.emailorganizer;

import com.emailorganizer.model.ContaEmail;
import com.emailorganizer.model.RegrasClassificacao;
import com.emailorganizer.service.GmailService;
import com.emailorganizer.view.TelaPrincipal;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.*;

/**
 * Classe principal que inicia o aplicativo
 */
public class EmailOrganizerApp {
    public static void main(String[] args) {
        // Define o look and feel para parecer com o sistema operacional
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Inicializa o aplicativo
        SwingUtilities.invokeLater(() -> {
            // Mostra tela de login
            TelaLogin login = new TelaLogin();
            login.setVisible(true);
        });
    }

    private static class TelaLogin extends JFrame {
        private JTextField txtEmail;
        // Remova o campo de senha pois usaremos OAuth
        private JTextField txtClientId;
        private JTextField txtClientSecret;

        public TelaLogin() {
            setTitle("Login - Organizador de Emails");
            setSize(450, 300);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);

            JPanel painel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // Componentes
            JLabel lblEmail = new JLabel("Email:");
            txtEmail = new JTextField(20);

            JLabel lblClientId = new JLabel("Client ID:");
            txtClientId = new JTextField(30);

            JLabel lblClientSecret = new JLabel("Client Secret:");
            txtClientSecret = new JTextField(30);

            JButton btnEntrar = new JButton("Autorizar com Google");
            JButton btnSair = new JButton("Sair");

            // Adiciona componentes ao painel
            gbc.gridx = 0; gbc.gridy = 0;
            painel.add(lblEmail, gbc);

            gbc.gridx = 1; gbc.gridy = 0;
            painel.add(txtEmail, gbc);

            gbc.gridx = 0; gbc.gridy = 1;
            painel.add(lblClientId, gbc);

            gbc.gridx = 1; gbc.gridy = 1;
            painel.add(txtClientId, gbc);

            gbc.gridx = 0; gbc.gridy = 2;
            painel.add(lblClientSecret, gbc);

            gbc.gridx = 1; gbc.gridy = 2;
            painel.add(txtClientSecret, gbc);

            JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            painelBotoes.add(btnEntrar);
            painelBotoes.add(btnSair);

            gbc.gridx = 0; gbc.gridy = 3;
            gbc.gridwidth = 2;
            painel.add(painelBotoes, gbc);

            add(painel);

            // Eventos
            btnEntrar.addActionListener(e -> loginOAuth());
            btnSair.addActionListener(e -> System.exit(0));
        }

        private void loginOAuth() {
            try {
                String email = txtEmail.getText();
                String clientId = txtClientId.getText();
                String clientSecret = txtClientSecret.getText();

                if (email.isEmpty() || clientId.isEmpty() || clientSecret.isEmpty()) {
                    throw new Exception("Preencha todos os campos obrigatórios.");
                }

                // Cria a conta de email (sem senha, pois usaremos OAuth)
                ContaEmail conta = new ContaEmail(email, "", "imap.gmail.com", 993, true);

                // Cria as regras de classificação
                RegrasClassificacao regras = new RegrasClassificacao();

                // Inicializa o serviço Gmail com OAuth
                GmailService gmailService = new GmailService(conta, regras, clientId, clientSecret);

                try {
                    // Isso iniciará o fluxo de autorização do navegador
                    gmailService.inicializar();

                    // Se chegou aqui, a autorização foi bem-sucedida
                    // Inicia a tela principal
                    dispose();
                    TelaPrincipal telaPrincipal = new TelaPrincipal(conta, regras, gmailService);
                    telaPrincipal.setVisible(true);
                } catch (Exception ex) {
                    throw new Exception("Falha na autorização: " + ex.getMessage());
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Erro ao fazer login: " + e.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
