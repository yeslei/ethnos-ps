package com.projeto.ethnos.model;

import java.util.List;

/**
 * Tabuleiro contém de 1 a 6 regiões (cardinalidade do diagrama).
 *
 * Padrão GRASP: High Cohesion.
 *  - Tabuleiro tem uma única responsabilidade: gerenciar o conjunto de
 *    regiões e localizar uma região pelo nome.
 *  - A pontuação fica em Regiao (Information Expert), o controle de turno
 *    em Partida (Controller). Cada classe faz uma coisa só.
 */
public class Tabuleiro {

    private final String guiaPontuacao;
    private final List<Regiao> regioes;

    public Tabuleiro(List<Regiao> regioes) {
        this(regioes, "1º=8 / 2º=5 / 3º=2");
    }

    public Tabuleiro(List<Regiao> regioes, String guiaPontuacao) {
        if (regioes == null || regioes.isEmpty() || regioes.size() > 6) {
            throw new IllegalArgumentException("Tabuleiro deve ter de 1 a 6 regiões");
        }
        this.regioes = List.copyOf(regioes);
        this.guiaPontuacao = guiaPontuacao;
    }

    /** Operação do diagrama: getRegiao(nome). */
    public Regiao getRegiao(String nome) {
        if (nome == null) {
            return null;
        }
        for (Regiao r : regioes) {
            if (r.getNome().equalsIgnoreCase(nome)) {
                return r;
            }
        }
        return null;
    }

    public List<Regiao> getTodasRegioes() {
        return regioes;
    }

    public String getGuiaPontuacao() {
        return guiaPontuacao;
    }
}
