package com.projeto.ethnos.model;

import java.util.List;

public class Bando {
    private int tamanho;
    private Carta lider;
    private List<Carta> cartas;

    public Bando(List<Carta> cartas, Carta lider) {
        this.cartas = List.copyOf(cartas);
        this.lider = lider;
        this.tamanho = this.cartas.size();
    }

    public int calcularPontos() {
        // Regra base: cada carta do bando vale 1 ponto.
        return tamanho;
    }

    public Carta getLider() {
        return lider;
    }

    public List<Carta> getCartas() {
        return cartas;
    }

    public int getTamanho() {
        return tamanho;
    }
}