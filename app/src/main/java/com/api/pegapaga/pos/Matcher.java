package com.api.pegapaga.pos;


import android.util.Pair;
import java.util.List;

/**
 * O cérebro do sistema de autenticação biométrica.
 * Responsável por realizar o matching 1:N, comparando um descriptor de prova
 * com todos os templates armazenados na base de dados local.
 */
public class Matcher {
    private final TemplateStore store;
    private double threshold;

    /**
     * Construtor para o Matcher.
     * @param store Uma instância do TemplateStore para aceder aos templates.
     */
    public Matcher(TemplateStore store) {
        this.store = store;
        // O threshold padrão é um bom ponto de partida, mas deve ser calibrado.
        this.threshold = 0.85;
    }

    /**
     * Define o limiar de similaridade para a identificação.
     * Um valor mais alto aumenta a segurança (menos falsos positivos), mas pode
     * aumentar a taxa de rejeição (mais falsos negativos).
     * @param t O valor do threshold, entre 0.0 e 1.0.
     */
    public void setThreshold(double t) {
        if (t >= 0.0 && t <= 1.0) {
            this.threshold = t;
        }
    }

    /**
     * Identifica um utilizador a partir de um descriptor biométrico (probe).
     * Percorre todos os templates armazenados, calcula a similaridade e retorna
     * o utilizador com o maior score que ultrapasse o threshold.
     *
     * @param probe O vetor de características (float[]) capturado no momento do toque.
     * @return Um Pair<String, Double> contendo o ID do utilizador e o score de similaridade,
     *         ou null se nenhuma correspondência for encontrada.
     */
    public Pair<String, Double> identify(float[] probe) {
        if (probe == null) {
            return null;
        }

        try {
            List<String> userIds = store.listUserIds();
            String bestMatchId = null;
            double bestScore = -1.0;

            for (String userId : userIds) {
                float[] storedDescriptor = store.loadDescriptor(userId);
                if (storedDescriptor == null) {
                    continue; // Pula para o próximo se não conseguir carregar o template
                }

                double score = cosineSimilarity(probe, storedDescriptor);

                if (score > bestScore) {
                    bestScore = score;
                    bestMatchId = userId;
                }
            }

            // Verifica se o melhor score encontrado é suficiente para ser considerado um match
            if (bestScore >= threshold) {
                return new Pair<>(bestMatchId, bestScore);
            }

        } catch (Exception e) {
            // Em uma aplicação real, aqui deveria haver um log mais detalhado
            e.printStackTrace();
        }

        return null; // Nenhum match encontrado
    }

    /**
     * Calcula a similaridade de cosseno entre dois vetores.
     * Este é um método eficaz para comparar a semelhança entre dois descritores biométricos.
     *
     * @param a O primeiro vetor de características.
     * @param b O segundo vetor de características.
     * @return Um valor entre -1.0 e 1.0 representando a similaridade.
     *         Para descritores normalizados, o resultado estará entre 0.0 e 1.0.
     */
    private double cosineSimilarity(float[] a, float[] b) {
        if (a == null || b == null || a.length != b.length) {
            return -1.0; // Indica erro ou vetores incompatíveis
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += Math.pow(a[i], 2);
            normB += Math.pow(b[i], 2);
        }

        if (normA == 0.0 || normB == 0.0) {
            return -1.0; // Evita divisão por zero
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}