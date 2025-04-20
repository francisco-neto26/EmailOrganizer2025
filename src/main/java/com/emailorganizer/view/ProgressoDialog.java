package com.emailorganizer.view;

import javax.swing.*;
import java.awt.*;

public class ProgressoDialog extends JDialog {
    private JLabel lblStatus;
    private JProgressBar progressBar;

    public ProgressoDialog(Frame owner, String title) {
        super(owner, title, false); // não-modal para permitir interação com a janela principal

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        lblStatus = new JLabel("Iniciando...");
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);

        panel.add(lblStatus, BorderLayout.NORTH);
        panel.add(progressBar, BorderLayout.CENTER);

        add(panel);
        pack();
        setSize(400, 150);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); // impedir fechamento durante processo
    }

    public void atualizarStatus(String texto) {
        SwingUtilities.invokeLater(() -> {
            lblStatus.setText(texto);
            // Forçar repinte
            lblStatus.paintImmediately(lblStatus.getBounds());
        });
    }
}