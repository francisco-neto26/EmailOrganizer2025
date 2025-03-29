// Classe para regras de classificação
package com.emailorganizer.model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Classe que define regras para classificar emails como marketing
 */
public class RegrasClassificacao {
    private List<String> dominiosMarketing;
    private List<Pattern> padroesAssunto;

    public RegrasClassificacao() {
        dominiosMarketing = new ArrayList<>();
        padroesAssunto = new ArrayList<>();

        // Adiciona alguns domínios comuns de marketing por padrão
        dominiosMarketing.add("newsletter");
        dominiosMarketing.add("marketing");
        dominiosMarketing.add("promo");
        dominiosMarketing.add("offer");

        // Adiciona alguns padrões de assunto comuns em emails de marketing
        padroesAssunto.add(Pattern.compile("(?i).*promoção.*"));
        padroesAssunto.add(Pattern.compile("(?i).*desconto.*"));
        padroesAssunto.add(Pattern.compile("(?i).*oferta.*"));
        padroesAssunto.add(Pattern.compile("(?i).*newsletter.*"));
    }

    public void adicionarDominioMarketing(String dominio) {
        dominiosMarketing.add(dominio);
    }

    public void adicionarPadraoAssunto(String regex) {
        padroesAssunto.add(Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
    }

    public boolean isEmailMarketing(Email email) {
        // Verifica se o remetente contém algum dos domínios de marketing
        for (String dominio : dominiosMarketing) {
            if (email.getRemetente().toLowerCase().contains(dominio.toLowerCase())) {
                return true;
            }
        }

        // Verifica se o assunto corresponde a algum dos padrões de marketing
        for (Pattern padrao : padroesAssunto) {
            if (padrao.matcher(email.getAssunto()).matches()) {
                return true;
            }
        }

        return false;
    }
}