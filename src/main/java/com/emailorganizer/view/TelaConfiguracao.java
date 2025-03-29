// Tela de configuração
package com.emailorganizer.view;

import com.emailorganizer.model.ContaEmail;
import com.emailorganizer.model.RegrasClassificacao;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Tela para configuração da conta de email e regras de classificação
 */
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

    public TelaConfiguracao(ContaEmail contaEmail, RegrasClassificacao regras) {
        this.contaEmail = contaEmail;
        this.regras = regras;

        configurarJanela();
        inicializarComponentes();
        carregarDados();
    }

    private void configurarJanela() {
        setTitle("Configurações");
        setSize(600, 500);
        setModal(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
    }

    private void inicializarComponentes() {
        // Painel de configuração da conta
        JPanel painelConta = new JPanel(new GridBagLayout());
        painelConta.setBorder(BorderFactory.createTitledBorder("Configurações da Conta"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Componentes da conta
        JLabel lblEmail = new JLabel("Email:");
        txtEmail = new JTextField(20);

        JLabel lblSenha = new JLabel("Senha:");
        txtSenha = new JPasswordField(20);

        JLabel lblServidor = new JLabel("Servidor IMAP:");
        txtServidor = new JTextField(20);

        JLabel lblPorta = new JLabel("Porta:");
        txtPorta = new JTextField(5);

        JLabel lblSsl = new JLabel("Usar SSL:");
        chkSsl = new JCheckBox();

        // Adiciona componentes ao painel da conta
        gbc.gridx = 0; gbc.gridy = 0;
        painelConta.add(lblEmail, gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        painelConta.add(txtEmail, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        painelConta.add(lblSenha, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        painelConta.add(txtSenha, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        painelConta.add(lblServidor, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        painelConta.add(txtServidor, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        painelConta.add(lblPorta, gbc);

        gbc.gridx = 1; gbc.gridy = 3;
        painelConta.add(txtPorta, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        painelConta.add(lblSsl, gbc);

        gbc.gridx = 1; gbc.gridy = 4;
        painelConta.add(chkSsl, gbc);

        // Painel de regras de classificação
        JPanel painelRegras = new JPanel(new GridBagLayout());
        painelRegras.setBorder(BorderFactory.createTitledBorder("Regras de Classificação"));

        JLabel lblDominios = new JLabel("Domínios de Marketing (um por linha):");
        txtDominios = new JTextArea(5, 20);
        JScrollPane scrollDominios = new JScrollPane(txtDominios);

        JLabel lblPadroes = new JLabel("Padrões de Assunto (regex, um por linha):");
        txtPadroes = new JTextArea(5, 20);
        JScrollPane scrollPadroes = new JScrollPane(txtPadroes);

        // Adiciona componentes ao painel de regras
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        painelRegras.add(lblDominios, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        painelRegras.add(scrollDominios, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        painelRegras.add(lblPadroes, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        painelRegras.add(scrollPadroes, gbc);

        // Painel de botões
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSalvar = new JButton("Salvar");
        JButton btnCancelar = new JButton("Cancelar");

        painelBotoes.add(btnSalvar);
        painelBotoes.add(btnCancelar);

        // Adiciona os painéis à janela
        JTabbedPane abas = new JTabbedPane();
        abas.addTab("Conta", painelConta);
        abas.addTab("Regras", painelRegras);

        add(abas, BorderLayout.CENTER);
        add(painelBotoes, BorderLayout.SOUTH);

        // Configura eventos dos botões
        btnSalvar.addActionListener(e -> salvarConfiguracoes());
        btnCancelar.addActionListener(e -> dispose());
    }

    private void carregarDados() {
        // Carrega dados da conta
        txtEmail.setText(contaEmail.getEmail());
        txtSenha.setText(contaEmail.getSenha());
        txtServidor.setText(contaEmail.getServidorImap());
        txtPorta.setText(String.valueOf(contaEmail.getPorta()));
        chkSsl.setSelected(contaEmail.isUsarSsl());

        // Carrega regras de classificação
        // Para simplificar, estamos apenas mostrando regras predefinidas
        txtDominios.setText("newsletter\nmarketing\npromo\noffer");
        txtPadroes.setText("(?i).*promoção.*\n(?i).*desconto.*\n(?i).*oferta.*\n(?i).*newsletter.*");
    }

    private void salvarConfiguracoes() {
        try {
            // Atualiza dados da conta
            contaEmail.setEmail(txtEmail.getText());
            contaEmail.setSenha(new String(txtSenha.getPassword()));
            contaEmail.setServidorImap(txtServidor.getText());
            contaEmail.setPorta(Integer.parseInt(txtPorta.getText()));
            contaEmail.setUsarSsl(chkSsl.isSelected());

            // Atualiza regras de classificação
            // Em um aplicativo real, implementaríamos a atualização das regras

            JOptionPane.showMessageDialog(this,
                    "Configurações salvas com sucesso!",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);

            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao salvar configurações: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
