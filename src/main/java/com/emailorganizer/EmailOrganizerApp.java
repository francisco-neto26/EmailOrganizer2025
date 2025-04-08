package com.emailorganizer;

import com.emailorganizer.view.TelaLogin;

import javax.swing.*;
import java.nio.charset.Charset;

public class EmailOrganizerApp {
    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> {
            TelaLogin login = new TelaLogin();
            login.setVisible(true);
        });
    }
}