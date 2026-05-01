package com.projeto.ethnos.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Baralho {
    private List<Carta> cartas = new ArrayList<>();
    private List<Carta> descarte = new ArrayList<>();

    private List<Carta> embaralhar() {
        Collections.shuffle(this.cartas);
        return this.cartas;
    }

    public void adicionarCarta(Carta carta) {
        this.cartas.add(carta);
    }

    public void adicionarCartas(List<Carta> cartas) {
        this.cartas.addAll(cartas);
    }

    public void descartarCarta(Carta carta) {
        if (carta != null) {
            this.descarte.add(carta);
        }
    }

    public void descartarCartas(List<Carta> cartas) {
        if (cartas != null && !cartas.isEmpty()) {
            this.descarte.addAll(cartas);
        }
    }

    public Carta comprarDoTopo() {
        // Essencial: recicla o descarte quando o baralho acabar,
        // evitando que a partida "morra" por falta de cartas.
        if (this.cartas.isEmpty() && !this.descarte.isEmpty()) {
            this.cartas.addAll(this.descarte);
            this.descarte.clear();
            embaralhar();
        }
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

    public int qntdDescarte() {
        return this.descarte.size();
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