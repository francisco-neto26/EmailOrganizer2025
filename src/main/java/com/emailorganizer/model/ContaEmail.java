// Classe para configurações da conta de email
package com.emailorganizer.model;

/**
 * Classe para armazenar configurações da conta de email
 */
public class ContaEmail {
    private String email;
    private String senha;
    private String servidorImap;
    private int porta;
    private boolean usarSsl;

    public ContaEmail(String email, String senha, String servidorImap, int porta, boolean usarSsl) {
        this.email = email;
        this.senha = senha;
        this.servidorImap = servidorImap;
        this.porta = porta;
        this.usarSsl = usarSsl;
    }
    public ContaEmail() {
        // Construtor vazio para casos em que os dados serão definidos depois
    }

    // Getters e Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getServidorImap() { return servidorImap; }
    public void setServidorImap(String servidorImap) { this.servidorImap = servidorImap; }

    public int getPorta() { return porta; }
    public void setPorta(int porta) { this.porta = porta; }

    public boolean isUsarSsl() { return usarSsl; }
    public void setUsarSsl(boolean usarSsl) { this.usarSsl = usarSsl; }
}