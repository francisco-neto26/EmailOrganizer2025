package com.emailorganizer.view;

import javax.swing.*;
import java.awt.*;

public class TelaSelecaoInicial extends JDialog {
    private boolean carregarEmails = false;

    public TelaSelecaoInicial(Frame owner) {
        super(owner, "Carregar E-mails", true);
        configurarJanela();
    }

    private void configurarJanela() {
        setSize(400, 150);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel label = new JLabel("Deseja carregar os e-mails agora?");
        JCheckBox chkCarregar = new JCheckBox("Sim, carregar agora");
        chkCarregar.setSelected(true);

        JButton btnOk = new JButton("OK");
        btnOk.addActionListener(e -> {
            carregarEmails = chkCarregar.isSelected();
            dispose();
        });

        JPanel painelCentro = new JPanel(new GridLayout(2, 1));
        painelCentro.add(label);
        painelCentro.add(chkCarregar);

        JPanel painelInferior = new JPanel();
        painelInferior.add(btnOk);

        add(painelCentro, BorderLayout.CENTER);
        add(painelInferior, BorderLayout.SOUTH);
    }

    public boolean isCarregarEmails() {
        return carregarEmails;
    }
}
