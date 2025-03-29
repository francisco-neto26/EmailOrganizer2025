//Estrutura principal do projeto

//Pacote de modelo (Model)
package com.emailorganizer.model;

import java.util.Date;

/**
 * Classe que representa um email
 */
public class Email {
    private String id;
    private String remetente;
    private String destinatario;
    private String assunto;
    private String conteudo;
    private Date data;
    private boolean lido;
    private boolean marketing;

    public Email(String id, String remetente, String destinatario, String assunto,
                 String conteudo, Date data, boolean lido) {
        this.id = id;
        this.remetente = remetente;
        this.destinatario = destinatario;
        this.assunto = assunto;
        this.conteudo = conteudo;
        this.data = data;
        this.lido = lido;
        this.marketing = false; // Por padrão, não é marketing
    }

    // Getters e Setters
    public String getId() { return id; }

    public String getRemetente() { return remetente; }

    public String getDestinatario() { return destinatario; }

    public String getAssunto() { return assunto; }

    public String getConteudo() { return conteudo; }

    public Date getData() { return data; }

    public boolean isLido() { return lido; }
    public void setLido(boolean lido) { this.lido = lido; }

    public boolean isMarketing() { return marketing; }
    public void setMarketing(boolean marketing) { this.marketing = marketing; }

    @Override
    public String toString() {
        return "De: " + remetente + "\nAssunto: " + assunto + "\nData: " + data;
    }
}