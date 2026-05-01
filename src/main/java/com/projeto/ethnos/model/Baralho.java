package com.projeto.ethnos.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Baralho {
    private List<Carta> cartas = new ArrayList<>();

    private List<Carta> embaralhar() {
        Collections.shuffle(this.cartas);
        return this.cartas;
    }

    public void adicionarCarta(Carta carta) {
        this.cartas.add(carta);
    }

    public Carta comprarDoTopo() {
        if (!estaVazio()) {
            return this.cartas.remove(this.cartas.size() - 1);
        }
        return null;
    }

    public boolean estaVazio() {
        return this.cartas.isEmpty();
    }

    public int qntdRestante() {
        return this.cartas.size();
    }
    
    // Método auxiliar para preparar o jogo
    public void setupInicial() {
        setupInicial(true);
    }

    public void setupInicial(boolean embaralhar) {
        // Correção: agora o setup pode ser determinístico para depuração e jogo local previsível.
        if (embaralhar) {
            embaralhar();
        }
    }
}