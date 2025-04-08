package com.emailorganizer.view;

import com.emailorganizer.service.LoginService;
import com.emailorganizer.utils.ConfiguracaoUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

public class TelaLogin extends JFrame {
    private JTextField txtEmail;
    private JTextField txtCaminhoJson;
    private final Preferences prefs = Preferences.userRoot().node("com/emailorganizer");

    public TelaLogin() {
        setTitle("Login - Organizador de Emails");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        montarInterface();
    }

    private void montarInterface() {
        JPanel painel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtEmail = new JTextField("ludwig.lindo26@gmail.com", 20);
        txtCaminhoJson = new JTextField(30);

        // Carrega caminho salvo anteriormente (se houver)

        String caminhoSalvo = "";
        try {
            caminhoSalvo = ConfiguracaoUtils.lerCaminhoCredenciais();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao ler o caminho do JSON: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }

        txtCaminhoJson.setText(caminhoSalvo);

        JButton btnSelecionarArquivo = new JButton("Selecionar...");
        btnSelecionarArquivo.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setDialogTitle("Selecione o arquivo credentials.json");
            chooser.setAcceptAllFileFilterUsed(true);
            chooser.addChoosableFileFilter(new FileNameExtensionFilter("JSON Files", "json"));

            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File arquivoSelecionado = chooser.getSelectedFile();
                txtCaminhoJson.setText(arquivoSelecionado.getAbsolutePath());
                //prefs.put("caminho_json", arquivoSelecionado.getAbsolutePath());
                try {
                    ConfiguracaoUtils.salvarCaminhoCredenciais(arquivoSelecionado.getAbsolutePath());// Salva no registro
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Erro ao ler o caminho do JSON: " + ex.getMessage(),
                            "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        gbc.gridx = 0; gbc.gridy = 0;
        painel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        painel.add(txtEmail, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        painel.add(new JLabel("Arquivo credentials.json:"), gbc);
        gbc.gridx = 1;
        painel.add(txtCaminhoJson, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        painel.add(btnSelecionarArquivo, gbc);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnEntrar = new JButton("Autorizar com Google");
        JButton btnSair = new JButton("Sair");
        botoes.add(btnEntrar);
        botoes.add(btnSair);

        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        painel.add(botoes, gbc);

        add(painel);

        btnEntrar.addActionListener(e -> tentarLogin());
        btnSair.addActionListener(e -> System.exit(0));
    }

    private void tentarLogin() {
        String email = txtEmail.getText().trim();
        String caminhoArquivo = txtCaminhoJson.getText().trim();

        if (email.isEmpty() || caminhoArquivo.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Informe o email e o caminho do arquivo credentials.json.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        File credentialsFile = new File(caminhoArquivo);
        if (!credentialsFile.exists() || !credentialsFile.getName().equals("credentials.json")) {
            JOptionPane.showMessageDialog(this,
                    "O arquivo selecionado não é válido ou não é o credentials.json.",
                    "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            LoginService loginService = new LoginService();
            loginService.autenticarComJson(email, credentialsFile);
            dispose(); // Fecha a janela de login
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Erro ao autenticar: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
