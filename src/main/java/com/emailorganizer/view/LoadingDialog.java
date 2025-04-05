package com.emailorganizer.view;

import javax.swing.*;
import java.awt.*;

public class LoadingDialog extends JDialog {
    public LoadingDialog(Frame parent, String mensagem) {
        super(parent, true);
        setUndecorated(true);
        setLayout(new BorderLayout());
        add(new JLabel(mensagem, SwingConstants.CENTER), BorderLayout.CENTER);
        setSize(300, 100);
        setLocationRelativeTo(parent);
    }
}
