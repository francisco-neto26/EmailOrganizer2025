package com.emailorganizer.view;

import com.emailorganizer.model.ContaEmail;
import com.emailorganizer.model.RegrasClassificacao;
import com.emailorganizer.utils.ConfiguracaoUtils;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.prefs.Preferences;

public class TelaConfiguracao extends JDialog {
    private ContaEmail contaEmail;
    private RegrasClassificacao regras;

    private JTextField txtEmail;
    private JPasswordField txtSenha;
    private JTextField txtServidor;
    private JTextField txtPorta;
    private JCheckBox chkSsl;

    private JTextArea txtDominios;
    private JTextArea txtPadroes;
    private JTabbedPane abasRegras;
    private JTextField txtLimiteEmails;
    private JTextField txtDiretorioJson;
    private final Preferences prefs = Preferences.userRoot().node("com/emailorganizer");

    public TelaConfiguracao(ContaEmail contaEmail, RegrasClassificacao regras) {
        this.contaEmail = contaEmail;
        this.regras = regras;

        configurarJanela();
        inicializarComponentes();
        carregarDados();
    }

    private void configurarJanela() {
        setTitle("Configurações");
        setSize(650, 550);
        setModal(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
    }

    private void inicializarComponentes() {
        JTabbedPane abas = new JTabbedPane();

        // Painel da conta
        JPanel painelConta = criarPainelConta();
        abas.addTab("Conta", painelConta);

        // Painel de regras
        JPanel painelRegras = criarPainelRegras();
        abas.addTab("Regras", painelRegras);

        // Painel de credenciais
        JPanel painelJson = criarPainelCredenciais();
        abas.addTab("Credenciais", painelJson);

        // Painel de botões
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSalvar = new JButton("Salvar");
        JButton btnCancelar = new JButton("Cancelar");
        painelBotoes.add(btnSalvar);
        painelBotoes.add(btnCancelar);

        add(abas, BorderLayout.CENTER);
        add(painelBotoes, BorderLayout.SOUTH);

        btnSalvar.addActionListener(e -> salvarConfiguracoes());
        btnCancelar.addActionListener(e -> dispose());
    }

    private JPanel criarPainelConta() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(BorderFactory.createTitledBorder("Conta de Email"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtEmail = new JTextField(20);
        txtSenha = new JPasswordField(20);
        txtServidor = new JTextField(20);
        txtPorta = new JTextField(5);
        chkSsl = new JCheckBox();

        gbc.gridx = 0; gbc.gridy = 0; painel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; painel.add(txtEmail, gbc);
        gbc.gridx = 0; gbc.gridy = 1; painel.add(new JLabel("Senha:"), gbc);
        gbc.gridx = 1; painel.add(txtSenha, gbc);
        gbc.gridx = 0; gbc.gridy = 2; painel.add(new JLabel("Servidor IMAP:"), gbc);
        gbc.gridx = 1; painel.add(txtServidor, gbc);
        gbc.gridx = 0; gbc.gridy = 3; painel.add(new JLabel("Porta:"), gbc);
        gbc.gridx = 1; painel.add(txtPorta, gbc);
        gbc.gridx = 0; gbc.gridy = 4; painel.add(new JLabel("Usar SSL:"), gbc);
        gbc.gridx = 1; painel.add(chkSsl, gbc);

        return painel;
    }

    private JPanel criarPainelRegras() {
        // Painel principal que irá conter o JTabbedPane interno
        JPanel painelPrincipal = new JPanel(new BorderLayout());

        // Criando o JTabbedPane interno
        JTabbedPane subAbas = new JTabbedPane();

        // Sub-aba 1: Regras de Classificação
        JPanel abaClassificacao = new JPanel(new GridBagLayout());
        abaClassificacao.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtDominios = new JTextArea(5, 20);
        txtPadroes = new JTextArea(5, 20);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        abaClassificacao.add(new JLabel("Domínios de Marketing (um por linha):"), gbc);
        gbc.gridy = 1; abaClassificacao.add(new JScrollPane(txtDominios), gbc);

        gbc.gridy = 2;
        abaClassificacao.add(new JLabel("Padrões de Assunto (regex, um por linha):"), gbc);
        gbc.gridy = 3; abaClassificacao.add(new JScrollPane(txtPadroes), gbc);

        // Sub-aba 2: Limite de E-mails
        JPanel abaLimite = criarSubAbaLimiteEmails(); // metodo auxiliar que criamos antes

        // Adicionando as abas ao tabbedPane interno
        subAbas.addTab("Regras de Classificação", abaClassificacao);
        subAbas.addTab("Limite de E-mails", abaLimite);

        // Adicionando o tabbedPane ao painel principal
        painelPrincipal.add(subAbas, BorderLayout.CENTER);
        return painelPrincipal;
    }

    private JPanel criarPainelCredenciais() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(BorderFactory.createTitledBorder("Arquivo de Credenciais (credentials.json)"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtDiretorioJson = new JTextField(30);
        txtDiretorioJson.setEditable(false);
        String caminho = "";
        try {
            caminho = ConfiguracaoUtils.lerCaminhoCredenciais();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao ler o caminho do JSON: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
        txtDiretorioJson.setText(caminho);

        JButton btnLimpar = new JButton("Limpar caminho salvo");
        btnLimpar.addActionListener(e -> {
            try {
                ConfiguracaoUtils.removerCaminhoCredenciais();
                txtDiretorioJson.setText("");
                JOptionPane.showMessageDialog(this, "Caminho removido com sucesso do Registro.", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao remover caminho do JSON: " + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
            txtDiretorioJson.setText("");
            JOptionPane.showMessageDialog(this, "Caminho removido com sucesso do Registro.", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        });

        gbc.gridx = 0; gbc.gridy = 0;
        painel.add(new JLabel("Caminho atual:"), gbc);

        gbc.gridx = 1;
        painel.add(txtDiretorioJson, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        painel.add(btnLimpar, gbc);

        return painel;
    }

    private void carregarDados() {
        txtEmail.setText(contaEmail.getEmail());
        txtSenha.setText(contaEmail.getSenha());
        txtServidor.setText(contaEmail.getServidorImap());
        txtPorta.setText(String.valueOf(contaEmail.getPorta()));
        chkSsl.setSelected(contaEmail.isUsarSsl());

        txtDominios.setText("newsletter\nmarketing\npromo\noffer");
        txtPadroes.setText("(?i).*promoção.*\n(?i).*desconto.*\n(?i).*oferta.*\n(?i).*newsletter.*");
    }

    private void salvarConfiguracoes() {
        try {
            contaEmail.setEmail(txtEmail.getText());
            contaEmail.setSenha(new String(txtSenha.getPassword()));
            contaEmail.setServidorImap(txtServidor.getText());
            contaEmail.setPorta(Integer.parseInt(txtPorta.getText()));
            contaEmail.setUsarSsl(chkSsl.isSelected());

            // --- NOVO BLOCO: salvar limite de e-mails ---
            int novoLimite = Integer.parseInt(txtLimiteEmails.getText());
            if (novoLimite < 1 || novoLimite > 500) {
                throw new NumberFormatException("O limite deve estar entre 1 e 500.");
            }

            int limiteAtual = ConfiguracaoUtils.lerLimiteEmails();
            if (limiteAtual != novoLimite) {
                try {
                    ConfiguracaoUtils.salvarLimiteEmails(novoLimite);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, "Erro ao salvar o limite de e-mails: " + e.getMessage(),
                            "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            JOptionPane.showMessageDialog(this,
                    "Configurações salvas com sucesso!",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Valor inválido para o limite de e-mails (1 a 500).",
                    "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao salvar configurações: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel criarSubAbaLimiteEmails() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtLimiteEmails = new JTextField(5);
        int limiteSalvo = 100; // valor padrão

        try {
            limiteSalvo = ConfiguracaoUtils.lerLimiteEmails();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar limite salvo. Usando padrão (100).", "Aviso", JOptionPane.WARNING_MESSAGE);
        }
        txtLimiteEmails.setText(String.valueOf(limiteSalvo));

        gbc.gridx = 0; gbc.gridy = 0;
        painel.add(new JLabel("Máximo de e-mails a carregar (1 a 500):"), gbc);

        gbc.gridx = 1;
        painel.add(txtLimiteEmails, gbc);

        return painel;
    }
}
