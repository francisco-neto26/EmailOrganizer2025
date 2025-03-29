package com.emailorganizer.view;

import com.emailorganizer.model.ContaEmail;
import com.emailorganizer.model.RegrasClassificacao;
import com.emailorganizer.service.GmailService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import com.toedter.calendar.JDateChooser;

public class TelaPrincipal extends JFrame {

    private ContaEmail conta;
    private RegrasClassificacao regras;
    private GmailService gmailService;

    // Componentes da UI
    private JTable tabelaEmails;
    private DefaultTableModel modeloTabela;
    private JComboBox<String> comboTipoEmail;
    private JTextField txtAssunto;
    private JTextField txtRemetente;
    private JComboBox<String> comboStatus;
    private JDateChooser dateInicio;
    private JDateChooser dateFim;
    private JButton btnFiltrar;
    private JButton btnMarcarLido;
    private JButton btnArquivar;

    public TelaPrincipal(ContaEmail conta, RegrasClassificacao regras, GmailService gmailService) {
        this.conta = conta;
        this.regras = regras;
        this.gmailService = gmailService;

        configurarJanela();
        inicializarComponentes();
        configurarEventos();
        carregarEmails();
    }

    private void configurarJanela() {
        setTitle("Organizador de Emails - " + conta.getEmail());
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
    }

    private void inicializarComponentes() {
        // Painel superior para filtros
        JPanel painelFiltros = new JPanel(new GridBagLayout());
        painelFiltros.setBorder(BorderFactory.createTitledBorder("Filtros"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Tipo de email
        JLabel lblTipo = new JLabel("Tipo:");
        comboTipoEmail = new JComboBox<>(new String[]{"Todos", "Pessoal", "Trabalho", "Financeiro", "Redes Sociais", "Promocional"});

        // Status
        JLabel lblStatus = new JLabel("Status:");
        comboStatus = new JComboBox<>(new String[]{"Todos", "Lido", "Não lido"});

        // Assunto
        JLabel lblAssunto = new JLabel("Assunto:");
        txtAssunto = new JTextField(15);

        // Remetente
        JLabel lblRemetente = new JLabel("Remetente:");
        txtRemetente = new JTextField(15);

        // Datas
        JLabel lblDataInicio = new JLabel("De:");
        dateInicio = new JDateChooser();
        dateInicio.setDate(getDataInicial());

        JLabel lblDataFim = new JLabel("Até:");
        dateFim = new JDateChooser();
        dateFim.setDate(new Date());

        // Botão de filtrar
        btnFiltrar = new JButton("Filtrar");

        // Adicionar componentes ao painel de filtros
        gbc.gridx = 0; gbc.gridy = 0;
        painelFiltros.add(lblTipo, gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        painelFiltros.add(comboTipoEmail, gbc);

        gbc.gridx = 2; gbc.gridy = 0;
        painelFiltros.add(lblStatus, gbc);

        gbc.gridx = 3; gbc.gridy = 0;
        painelFiltros.add(comboStatus, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        painelFiltros.add(lblAssunto, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        painelFiltros.add(txtAssunto, gbc);

        gbc.gridx = 2; gbc.gridy = 1;
        painelFiltros.add(lblRemetente, gbc);

        gbc.gridx = 3; gbc.gridy = 1;
        painelFiltros.add(txtRemetente, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        painelFiltros.add(lblDataInicio, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        painelFiltros.add(dateInicio, gbc);

        gbc.gridx = 2; gbc.gridy = 2;
        painelFiltros.add(lblDataFim, gbc);

        gbc.gridx = 3; gbc.gridy = 2;
        painelFiltros.add(dateFim, gbc);

        gbc.gridx = 4; gbc.gridy = 2;
        painelFiltros.add(btnFiltrar, gbc);

        // Painel central com tabela de emails
        String[] colunas = {"ID", "Remetente", "Assunto", "Data", "Tipo", "Status"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelaEmails = new JTable(modeloTabela);
        tabelaEmails.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaEmails.getColumnModel().getColumn(0).setMaxWidth(50);
        tabelaEmails.getColumnModel().getColumn(3).setPreferredWidth(120);
        tabelaEmails.getColumnModel().getColumn(4).setPreferredWidth(100);
        tabelaEmails.getColumnModel().getColumn(5).setPreferredWidth(80);

        JScrollPane scrollTabela = new JScrollPane(tabelaEmails);

        // Painel inferior com botões de ação
        JPanel painelAcoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnMarcarLido = new JButton("Marcar como Lido");
        btnArquivar = new JButton("Arquivar");

        painelAcoes.add(btnMarcarLido);
        painelAcoes.add(btnArquivar);

        // Adicionar todos os painéis ao frame
        add(painelFiltros, BorderLayout.NORTH);
        add(scrollTabela, BorderLayout.CENTER);
        add(painelAcoes, BorderLayout.SOUTH);
    }

    private Date getDataInicial() {
        // Retorna a data de 30 dias atrás
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -30);
        return cal.getTime();
    }

    private void configurarEventos() {
        btnFiltrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filtrarEmails();
            }
        });

        btnMarcarLido.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                marcarComoLido();
            }
        });

        btnArquivar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                arquivarEmail();
            }
        });
    }

    private void carregarEmails() {
        try {
            // Limpar tabela atual
            modeloTabela.setRowCount(0);

            // Usar o GmailService para carregar emails
            List<Map<String, Object>> emails = gmailService.buscarEmails(null, null, null, null, null);

            // Preencher tabela com emails
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            for (Map<String, Object> email : emails) {
                modeloTabela.addRow(new Object[]{
                        email.get("id"),
                        email.get("from"),
                        email.get("subject"),
                        dateFormat.format((Date)email.get("date")),
                        email.get("type"),
                        (Boolean)email.get("read") ? "Lido" : "Não lido"
                });
            }

            JOptionPane.showMessageDialog(this,
                    emails.size() + " emails foram carregados.",
                    "Emails Carregados", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao carregar emails: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void filtrarEmails() {
        try {
            // Obter valores dos filtros
            String tipo = comboTipoEmail.getSelectedItem().equals("Todos") ?
                    null : (String) comboTipoEmail.getSelectedItem();

            String status = comboStatus.getSelectedItem().toString();
            Boolean lido = null;
            if (status.equals("Lido")) lido = true;
            else if (status.equals("Não lido")) lido = false;

            String assunto = txtAssunto.getText().isEmpty() ? null : txtAssunto.getText();
            String remetente = txtRemetente.getText().isEmpty() ? null : txtRemetente.getText();
            Date dataInicio = dateInicio.getDate();
            Date dataFim = dateFim.getDate();

            // Buscar emails filtrados
            List<Map<String, Object>> emails = gmailService.buscarEmails(
                    assunto, remetente, tipo, lido,
                    new Date[]{dataInicio, dataFim}
            );

            // Atualizar tabela
            modeloTabela.setRowCount(0);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            for (Map<String, Object> email : emails) {
                modeloTabela.addRow(new Object[]{
                        email.get("id"),
                        email.get("from"),
                        email.get("subject"),
                        dateFormat.format((Date)email.get("date")),
                        email.get("type"),
                        (Boolean)email.get("read") ? "Lido" : "Não lido"
                });
            }

            JOptionPane.showMessageDialog(this,
                    emails.size() + " emails encontrados.",
                    "Filtro Aplicado", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao filtrar emails: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void marcarComoLido() {
        int linhaSelecionada = tabelaEmails.getSelectedRow();
        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this,
                    "Selecione um email para marcar como lido.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String emailId = tabelaEmails.getValueAt(linhaSelecionada, 0).toString();
            gmailService.marcarComoLido(emailId);
            modeloTabela.setValueAt("Lido", linhaSelecionada, 5);

            JOptionPane.showMessageDialog(this,
                    "Email marcado como lido com sucesso.",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao marcar email como lido: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void arquivarEmail() {
        int linhaSelecionada = tabelaEmails.getSelectedRow();
        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this,
                    "Selecione um email para arquivar.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String emailId = tabelaEmails.getValueAt(linhaSelecionada, 0).toString();
            gmailService.arquivarEmail(emailId);
            modeloTabela.removeRow(linhaSelecionada);

            JOptionPane.showMessageDialog(this,
                    "Email arquivado com sucesso.",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao arquivar email: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
