package com.projeto.ethnos.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Baralho de cartas + pilha de descarte. No diagrama: embaralhar(),
 * comprarDoTopo(), adicionarCarta(), estaVazio(), qntdRestante().
 *
 * GRASP Information Expert + High Cohesion: o baralho conhece e gerencia
 * suas próprias cartas (incluindo a reciclagem do descarte).
 */
public class Baralho {

    private final List<Carta> cartas = new ArrayList<>();
    private final List<Carta> descarte = new ArrayList<>();

    /** Operação do diagrama: embaralhar(). */
    public List<Carta> embaralhar() {
        Collections.shuffle(this.cartas);
        return this.cartas;
    }

    /** Operação do diagrama: adicionarCarta(carta). */
    public void adicionarCarta(Carta carta) {
        if (carta != null) {
            this.cartas.add(carta);
        }
    }

    public void adicionarCartas(List<Carta> cartas) {
        if (cartas != null) {
            this.cartas.addAll(cartas);
        }
    }

    public void descartarCarta(Carta carta) {
        if (carta != null) {
            this.descarte.add(carta);
        }
    }

    public void descartarCartas(List<Carta> cartas) {
        if (cartas != null) {
            this.descarte.addAll(cartas);
        }
    }

    /**
     * Operação do diagrama: comprarDoTopo(): Carta.
     *
     * Quando o monte zera, reciclamos o descarte automaticamente. Isso
     * garante que o jogo não trava por falta de cartas em partidas longas.
     */
    public Carta comprarDoTopo() {
        if (this.cartas.isEmpty() && !this.descarte.isEmpty()) {
            this.cartas.addAll(this.descarte);
            this.descarte.clear();
            embaralhar();
        }
        if (!this.cartas.isEmpty()) {
            return this.cartas.remove(this.cartas.size() - 1);
        }
        return null;
    }

    /** Operação do diagrama: estaVazio(). */
    public boolean estaVazio() {
        return this.cartas.isEmpty();
    }

    /** Indica que não há nem monte nem descarte (jogo "seco"). */
    public boolean semCartasDisponiveis() {
        return this.cartas.isEmpty() && this.descarte.isEmpty();
    }

    /** Operação do diagrama: qntdRestante(). */
    public int qntdRestante() {
        return this.cartas.size();
    }

    public int qntdDescarte() {
        return this.descarte.size();
    }
}
