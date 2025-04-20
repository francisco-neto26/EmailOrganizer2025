package com.emailorganizer.view;

import com.emailorganizer.model.ContaEmail;
import com.emailorganizer.model.RegrasClassificacao;
import com.emailorganizer.service.GmailService;
import com.emailorganizer.utils.ConfiguracaoUtils;
import com.emailorganizer.service.EmailAutomatorService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.*;

import com.toedter.calendar.JDateChooser;

import java.util.List;
import java.util.Map;

public class TelaPrincipal extends JFrame {

    private ContaEmail conta;
    private RegrasClassificacao regras;
    private GmailService gmailService;
    private EmailAutomatorService emailAutomatorService;

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
    private JButton btnSelecionarTodos;
    private JButton btnDesmarcarTodos;
    private JButton btnConfiguracoes;
    private JButton btnExcluir;
    private JButton btnAutoMarcarLido;
    private JLabel lblStatus;
    private static TelaPrincipal instancia;
    private JProgressBar progressBar;

    public TelaPrincipal(ContaEmail conta, RegrasClassificacao regras,GmailService gmailService, EmailAutomatorService emailAutomatorService,boolean carregarEmails) {
        this.conta = conta;
        this.regras = regras;
        this.gmailService = gmailService;
        this.emailAutomatorService = emailAutomatorService;
        instancia = this;
        lblStatus = new JLabel("Pronto");
        configurarJanela();
        inicializarComponentes();
        configurarEventos();
        setVisible(true);

        if (carregarEmails) {
            carregarEmails();
        }
    }
    public static TelaPrincipal getInstance() {
        return instancia;
    }

    public void atualizarStatus(String texto) {
        SwingUtilities.invokeLater(() -> {
            if (lblStatus != null) {
                lblStatus.setText(texto);
                lblStatus.paintImmediately(lblStatus.getBounds());
            }
        });
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
        modeloTabela = criarModeloTabela();
        tabelaEmails = new JTable(modeloTabela);

        tabelaEmails.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tabelaEmails.rowAtPoint(e.getPoint());
                int col = tabelaEmails.columnAtPoint(e.getPoint());

                if (col == 0) return; // Deixe o usuário interagir livremente com checkboxes

                // Clique na linha: desmarcar todos e marcar só a clicada
                for (int i = 0; i < modeloTabela.getRowCount(); i++) {
                    modeloTabela.setValueAt(false, i, 0);
                }
                modeloTabela.setValueAt(true, row, 0);
            }
        });

        //tabelaEmails.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tabelaEmails.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaEmails.getColumnModel().getColumn(0).setPreferredWidth(50);
        tabelaEmails.getColumnModel().getColumn(3).setPreferredWidth(120);
        tabelaEmails.getColumnModel().getColumn(4).setPreferredWidth(100);
        tabelaEmails.getColumnModel().getColumn(5).setPreferredWidth(80);

        JScrollPane scrollTabela = new JScrollPane(tabelaEmails);

        // Painel inferior com botões de ação
        JPanel painelAcoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnMarcarLido = new JButton("Marcar como Lido");
        btnArquivar = new JButton("Arquivar");
        btnExcluir = new JButton("Excluir");
        btnSelecionarTodos = new JButton("Selecionar Todos");
        btnDesmarcarTodos = new JButton("Desmarcar Todos");
        btnConfiguracoes = new JButton("Configurações");
        btnAutoMarcarLido = new JButton("Auto Marcar Lido");

        painelAcoes.add(btnMarcarLido);
        painelAcoes.add(btnArquivar);
        painelAcoes.add(btnExcluir);
        painelAcoes.add(btnSelecionarTodos);
        painelAcoes.add(btnDesmarcarTodos);
        painelAcoes.add(btnConfiguracoes);
        painelAcoes.add(btnAutoMarcarLido);

        // Adicionar todos os painéis ao frame
        add(painelFiltros, BorderLayout.NORTH);
        add(scrollTabela, BorderLayout.CENTER);

        JPanel painelInferior = new JPanel();
        painelInferior.setLayout(new BorderLayout());
        painelInferior.add(painelAcoes, BorderLayout.NORTH);
        add(painelInferior, BorderLayout.SOUTH);
    }

    public void iniciarProcessamentoLongo() {
        SwingUtilities.invokeLater(() -> {
            progressBar.setVisible(true);
            btnAutoMarcarLido.setEnabled(false);  // Desabilitar o botão enquanto processa
        });
    }

    public void finalizarProcessamentoLongo() {
        SwingUtilities.invokeLater(() -> {
            progressBar.setVisible(false);
            btnAutoMarcarLido.setEnabled(true);  // Reabilitar o botão
        });
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

        btnExcluir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                excluirEmail();
            }
        });

        btnSelecionarTodos.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < modeloTabela.getRowCount(); i++) {
                    modeloTabela.setValueAt(true, i, 0); // Coluna 0 = checkbox de seleção
                }
            }
        });

        btnDesmarcarTodos.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < modeloTabela.getRowCount(); i++) {
                    modeloTabela.setValueAt(false, i, 0); // Desmarca
                }
            }
        });

        btnConfiguracoes.addActionListener(e -> {
            ContaEmail conta = new ContaEmail(); // ou carregue de onde você mantém
            RegrasClassificacao regras = new RegrasClassificacao(); // idem
            TelaConfiguracao config = new TelaConfiguracao(conta, regras);
            config.setVisible(true);
        });

        btnAutoMarcarLido.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnAutoMarcarLido.setEnabled(false); // Desabilitar o botão durante processamento

                new Thread(() -> {
                    try {
                        int total = emailAutomatorService.marcarComoLidosEContarRemetentes();

                        // Mostrar resultado na thread da UI
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(
                                    TelaPrincipal.this,
                                    "Processamento concluído com sucesso!\n" +
                                            "Total de e-mails processados: " + total,
                                    "Sucesso",
                                    JOptionPane.INFORMATION_MESSAGE
                            );
                            btnAutoMarcarLido.setEnabled(true); // Reabilitar o botão
                            // Opcionalmente recarregar os e-mails
                            filtrarEmails();
                        });

                    } catch (Exception ex) {
                        ex.printStackTrace();

                        // Mostrar erro na thread da UI
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(
                                    TelaPrincipal.this,
                                    "Erro durante o processamento:\n" + ex.getMessage(),
                                    "Erro",
                                    JOptionPane.ERROR_MESSAGE
                            );
                            btnAutoMarcarLido.setEnabled(true); // Reabilitar o botão
                        });
                    }
                }).start();
            }
        });

    }

    private void carregarEmails() {
        LoadingDialog loading = new LoadingDialog(this, "Carregando emails...");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            List<Map<String, Object>> emails;

            @Override
            protected Void doInBackground() {
                try {
                    int limite = ConfiguracaoUtils.lerLimiteEmails();
                    emails = gmailService.buscarEmails(null, null, null, null, null, limite);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(TelaPrincipal.this,
                            "Erro ao carregar emails: " + e.getMessage(),
                            "Erro", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void done() {
                loading.dispose();

                if (emails != null) {
                    preencherTabelaEmails(emails);

                    JOptionPane.showMessageDialog(TelaPrincipal.this,
                            emails.size() + " emails foram carregados.",
                            "Emails Carregados", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        };

        worker.execute();
        loading.setVisible(true);
    }

    private void filtrarEmails() {
        String assunto = txtAssunto.getText();
        String remetente = txtRemetente.getText();
        String tipo = (String) comboTipoEmail.getSelectedItem();
        String statusSelecionado = (String) comboStatus.getSelectedItem();
        Boolean lido = null;

        if ("Lido".equals(statusSelecionado)) {
            lido = true;
        } else if ("Não lido".equals(statusSelecionado)) {
            lido = false;
        }

        Date dataDe = dateInicio.getDate();
        Date dataAte = dateFim.getDate();
        Date[] datas = (dataDe != null && dataAte != null) ? new Date[]{dataDe, dataAte} : null;

        final Boolean finalLido = lido;
        final String finalAssunto = assunto;
        final String finalRemetente = remetente;
        final String finalTipo = tipo;
        final Date[] finalDatas = datas;

        LoadingDialog loading = new LoadingDialog(this, "Filtrando emails...");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            List<Map<String, Object>> emails;

            @Override
            protected Void doInBackground() {
                try {
                    int limite = ConfiguracaoUtils.lerLimiteEmails();
                    emails = gmailService.buscarEmails(finalAssunto, finalRemetente, finalTipo, finalLido, finalDatas, limite);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(TelaPrincipal.this,
                            "Erro ao filtrar emails: " + e.getMessage(),
                            "Erro", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void done() {
                loading.dispose();

                if (emails != null) {
                    preencherTabelaEmails(emails);

                    JOptionPane.showMessageDialog(TelaPrincipal.this,
                            emails.size() + " emails encontrados.",
                            "Resultado do Filtro", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        };

        worker.execute();
        loading.setVisible(true);
    }


    private void marcarComoLido() {
        boolean encontrouMarcado = false;

        try {
            for (int i = 0; i < modeloTabela.getRowCount(); i++) {
                Boolean marcado = (Boolean) modeloTabela.getValueAt(i, 0);
                if (Boolean.TRUE.equals(marcado)) {
                    encontrouMarcado = true;

                    String emailId = modeloTabela.getValueAt(i, 1).toString(); // ID está na coluna 1
                    gmailService.marcarComoLido(emailId);

                    modeloTabela.setValueAt("Lido", i, 6); // Status está na coluna 6
                }
            }

            // Se nenhum checkbox foi marcado, marca a linha selecionada
            if (!encontrouMarcado) {
                int linhaSelecionada = tabelaEmails.getSelectedRow();
                if (linhaSelecionada != -1) {
                    String emailId = modeloTabela.getValueAt(linhaSelecionada, 1).toString();
                    gmailService.marcarComoLido(emailId);
                    modeloTabela.setValueAt("Lido", linhaSelecionada, 6);
                    encontrouMarcado = true;
                }
            }

            if (encontrouMarcado) {
                JOptionPane.showMessageDialog(this,
                        "Email(s) marcado(s) como lido com sucesso.",
                        "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Nenhum email selecionado.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao marcar emails como lidos: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void arquivarEmail() {
        boolean encontrouMarcado = false;

        try {
            for (int i = 0; i < modeloTabela.getRowCount(); i++) {
                Boolean marcado = (Boolean) modeloTabela.getValueAt(i, 0);
                if (Boolean.TRUE.equals(marcado)) {
                    encontrouMarcado = true;

                    String emailId = modeloTabela.getValueAt(i, 1).toString(); // ID está na coluna 1
                    gmailService.arquivarEmail(emailId);

                    modeloTabela.setValueAt("Arquivado", i, 6); // Status está na coluna 6
                }
            }

            // Se nenhum checkbox foi marcado, tenta arquivar linha selecionada
            if (!encontrouMarcado) {
                int linhaSelecionada = tabelaEmails.getSelectedRow();
                if (linhaSelecionada != -1) {
                    String emailId = modeloTabela.getValueAt(linhaSelecionada, 1).toString();
                    gmailService.arquivarEmail(emailId);
                    modeloTabela.setValueAt("Arquivado", linhaSelecionada, 6);
                    encontrouMarcado = true;
                }
            }

            if (encontrouMarcado) {
                JOptionPane.showMessageDialog(this,
                        "Email(s) arquivado(s) com sucesso.",
                        "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Nenhum email selecionado para arquivar.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao arquivar emails: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void excluirEmail() {
        boolean encontrouMarcado = false;
        List<String> idsParaExcluir = new ArrayList<>();

        // Coleta os IDs dos emails marcados
        for (int i = 0; i < modeloTabela.getRowCount(); i++) {
            Boolean marcado = (Boolean) modeloTabela.getValueAt(i, 0);
            if (Boolean.TRUE.equals(marcado)) {
                encontrouMarcado = true;
                String emailId = modeloTabela.getValueAt(i, 1).toString(); // ID na coluna 1
                idsParaExcluir.add(emailId);
            }
        }

        // Se nada foi marcado, tenta excluir o email da linha selecionada
        if (!encontrouMarcado) {
            int linhaSelecionada = tabelaEmails.getSelectedRow();
            if (linhaSelecionada != -1) {
                String emailId = modeloTabela.getValueAt(linhaSelecionada, 1).toString();
                idsParaExcluir.add(emailId);
                encontrouMarcado = true;
            }
        }

        if (!encontrouMarcado) {
            JOptionPane.showMessageDialog(this, "Nenhum email selecionado para exclusão.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Confirma a exclusão
        int opcao = JOptionPane.showConfirmDialog(this, "Tem certeza que deseja excluir o(s) email(s) selecionado(s)?", "Confirmar Exclusão", JOptionPane.YES_NO_OPTION);
        if (opcao != JOptionPane.YES_OPTION) return;

        // Exclui os emails
        for (String emailId : idsParaExcluir) {
            try {
                gmailService.excluirEmail(emailId); // Implementar no seu serviço
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao excluir o e-mail ID: " + emailId + "\n" + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }

        // Atualiza a tela usando o fitro ja definido
        filtrarEmails();
    }

    private DefaultTableModel criarModeloTabela() {
        return new DefaultTableModel(null, new String[]{"Selecionado", "ID", "Remetente", "Assunto", "Data", "Tipo", "Status"}) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Boolean.class; // Checkbox
                return String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0; // Apenas checkbox é editável
            }
        };
    }

    private void preencherTabelaEmails(List<Map<String, Object>> emails) {
        modeloTabela.setRowCount(0);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        for (Map<String, Object> email : emails) {
            modeloTabela.addRow(new Object[]{
                    false,
                    email.get("id"),
                    email.get("from"),
                    email.get("subject"),
                    dateFormat.format((Date) email.get("date")),
                    email.get("type"),
                    (Boolean) email.get("read") ? "Lido" : "Não lido"
            });
        }
    }

}
