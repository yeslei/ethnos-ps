package com.projeto.ethnos.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Mercado: cartas abertas disponíveis para recrutamento. No diagrama está
 * descrito com cartasAbertas: 0..*.
 *
 * Padrões GRASP:
 *  - Information Expert: Mercado conhece suas cartas abertas e
 *    é o responsável por operar sobre elas.
 *  - High Cohesion: única responsabilidade — gerenciar as cartas abertas.
 */
public class Mercado {

    private final List<Carta> cartasAbertas = new ArrayList<>();

    /** Operação do diagrama: adicionarCartas(cartas). */
    public void adicionarCartas(List<Carta> cartas) {
        if (cartas != null) {
            this.cartasAbertas.addAll(cartas);
        }
    }

    /**
     * Operação do diagrama: comprarCarta().
     * Remove e retorna a primeira carta disponível.
     */
    public Carta comprarCarta() {
        if (cartasAbertas.isEmpty()) {
            return null;
        }
        return cartasAbertas.remove(0);
    }

    /** Compra uma carta específica (escolhida pela UI). */
    public Carta comprarCarta(Carta escolhida) {
        if (escolhida == null) {
            return null;
        }
        int idx = cartasAbertas.indexOf(escolhida);
        if (idx < 0) {
            return null;
        }
        return cartasAbertas.remove(idx);
    }

    /** Operação do diagrama: getCartasDisponiveis(). */
    public List<Carta> getCartasDisponiveis() {
        return List.copyOf(cartasAbertas);
    }

    /** Retira todas as cartas (usado no fim de era para limpar o mercado). */
    public List<Carta> retirarTodas() {
        List<Carta> todas = new ArrayList<>(cartasAbertas);
        cartasAbertas.clear();
        return todas;
    }

    public int tamanho() {
        return cartasAbertas.size();
    }
}
