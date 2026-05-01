package com.projeto.ethnos.model;

import java.util.List;

public class Tabuleiro {
    private List<Regiao> regioes;

    public Tabuleiro(List<Regiao> regioes) {
        this.regioes = regioes;
    }

    public Regiao getRegiao(String cor) {
        for (Regiao r : regioes) {
            if (r.getNome().equalsIgnoreCase(cor)) {
                return r;
            }
        }
        return null;
    }

    public List<Regiao> getTodasRegioes() {
        return this.regioes;
    }
}