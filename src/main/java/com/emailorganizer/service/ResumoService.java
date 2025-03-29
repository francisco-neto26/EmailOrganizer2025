// Serviço para resumo de emails com IA
package com.emailorganizer.service;

import com.emailorganizer.model.Email;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Serviço para resumir emails usando IA
 * Nota: Este é um exemplo simplificado que usa uma API fictícia de IA
 */
public class ResumoService {
    private final String apiKey;
    private final HttpClient httpClient;
    private final ExecutorService executorService;

    public ResumoService(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.executorService = Executors.newFixedThreadPool(5);
    }

    /**
     * Gera resumo de um único email
     */
    public String gerarResumo(Email email) {
        try {
            // Prepara o conteúdo para enviar à API de IA
            String conteudo = "Assunto: " + email.getAssunto() + "\n\n" + email.getConteudo();

            // Exemplo: integração com uma API de resumo (fictícia)
            String jsonBody = "{\"text\": \"" + conteudo.replace("\"", "\\\"") + "\", \"max_length\": 100}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.resumoai.exemplo/summarize"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Aqui normalmente analisaríamos o JSON da resposta
                // Por simplicidade, estamos retornando um resumo genérico
                return "Resumo do email: " + email.getAssunto() + " (Enviado em: " + email.getData() + ")";
            } else {
                return "Não foi possível gerar um resumo para este email.";
            }
        } catch (Exception e) {
            return "Erro ao gerar resumo: " + e.getMessage();
        }
    }

    /**
     * Gera resumos para múltiplos emails em paralelo
     */
    public Future<String> gerarResumoEmLote(List<Email> emails) {
        return executorService.submit(() -> {
            StringBuilder resultado = new StringBuilder();
            resultado.append("Resumo de ").append(emails.size()).append(" emails:\n\n");

            for (Email email : emails) {
                if (!email.isMarketing()) {  // Ignora emails de marketing
                    resultado.append("- ").append(gerarResumo(email)).append("\n");
                }
            }

            return resultado.toString();
        });
    }

    /**
     * Fecha os recursos do serviço
     */
    public void fechar() {
        executorService.shutdown();
    }
}