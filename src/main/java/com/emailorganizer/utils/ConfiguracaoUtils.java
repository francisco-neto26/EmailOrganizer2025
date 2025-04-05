package com.emailorganizer.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConfiguracaoUtils {

    private static final String REGISTRO_BASE = "HKEY_CURRENT_USER\\Software\\EmailOrganizer";
    private static final String CHAVE_CREDENCIAIS = "credentialsPath";
    private static final String CHAVE_LIMITE_EMAILS = "limiteEmails";
    private static final String CHAVE_TEMA_ESCURO = "temaEscuro";

    private static final int LIMITE_EMAILS_PADRAO = 100;
    private static final int LIMITE_EMAILS_MAXIMO = 500;

    // ========================
    // MÉTODOS GENÉRICOS
    // ========================

    public static void salvarValorRegistro(String chave, String valor) throws IOException {
        String comando = String.format("reg add \"%s\" /v %s /t REG_SZ /d \"%s\" /f", REGISTRO_BASE, chave, valor);
        executarComando(comando);
    }

    public static String lerValorRegistro(String chave) throws IOException {
        String comando = String.format("reg query \"%s\" /v %s", REGISTRO_BASE, chave);
        Process processo = new ProcessBuilder("cmd.exe", "/c", comando)
                .redirectErrorStream(true)
                .start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(processo.getInputStream()))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                if (linha.contains(chave)) {
                    return linha.split("\\s{2,}")[2].trim();
                }
            }
        }

        return null;
    }

    public static void removerValorRegistro(String chave) throws IOException {
        String comando = String.format("reg delete \"%s\" /v %s /f", REGISTRO_BASE, chave);
        executarComando(comando);
    }

    private static void executarComando(String comando) throws IOException {
        new ProcessBuilder("cmd.exe", "/c", comando)
                .redirectErrorStream(true)
                .start();
    }

    // ========================
    // CONFIGURAÇÃO: Caminho das credenciais
    // ========================

    public static void salvarCaminhoCredenciais(String caminho) throws IOException {
        salvarValorRegistro(CHAVE_CREDENCIAIS, caminho);
    }

    public static String lerCaminhoCredenciais() throws IOException {
        return lerValorRegistro(CHAVE_CREDENCIAIS);
    }

    public static void removerCaminhoCredenciais() throws IOException {
        removerValorRegistro(CHAVE_CREDENCIAIS);
    }

    // ========================
    // CONFIGURAÇÃO: Limite de e-mails
    // ========================

    public static void salvarLimiteEmails(int limite) throws IOException {
        if (limite < 1 || limite > LIMITE_EMAILS_MAXIMO) {
            throw new IllegalArgumentException("O limite deve estar entre 1 e " + LIMITE_EMAILS_MAXIMO);
        }
        salvarValorRegistro(CHAVE_LIMITE_EMAILS, String.valueOf(limite));
    }

    public static int lerLimiteEmails() throws IOException {
        String valor = lerValorRegistro(CHAVE_LIMITE_EMAILS);
        try {
            int limite = Integer.parseInt(valor);
            if (limite < 1 || limite > LIMITE_EMAILS_MAXIMO) {
                return LIMITE_EMAILS_PADRAO;
            }
            return limite;
        } catch (Exception e) {
            return LIMITE_EMAILS_PADRAO;
        }
    }

    // ========================
    // CONFIGURAÇÃO: Tema escuro
    // ========================

    public static void salvarTemaEscuro(boolean ativado) throws IOException {
        salvarValorRegistro(CHAVE_TEMA_ESCURO, ativado ? "true" : "false");
    }

    public static boolean estaUsandoTemaEscuro() throws IOException {
        String valor = lerValorRegistro(CHAVE_TEMA_ESCURO);
        return valor != null && valor.equalsIgnoreCase("true");
    }

    public static void removerTemaEscuro() throws IOException {
        removerValorRegistro(CHAVE_TEMA_ESCURO);
    }
}
