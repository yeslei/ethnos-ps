package com.projeto.ethnos.model;

import com.projeto.ethnos.model.carta.Dragao;

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
     * Reorganiza o baralho para uma nova era garantindo que dragões só
     * apareçam na segunda metade do monte.
     */
    public void prepararNovaEraComDragaoNaSegundaMetade() {
        if (!this.descarte.isEmpty()) {
            this.cartas.addAll(this.descarte);
            this.descarte.clear();
        }
        if (this.cartas.isEmpty()) return;

        List<Carta> dragoes = new ArrayList<>();
        List<Carta> normais = new ArrayList<>();
        for (Carta c : this.cartas) {
            if (Dragao.ehDragao(c)) {
                dragoes.add(c);
            } else {
                normais.add(c);
            }
        }

        Collections.shuffle(normais);
        Collections.shuffle(dragoes);

        int total = normais.size() + dragoes.size();
        int limitePrimeiraMetade = total / 2;
        int qtdNormaisPrimeira = Math.min(limitePrimeiraMetade, normais.size());

        List<Carta> novaOrdem = new ArrayList<>(total);
        novaOrdem.addAll(normais.subList(0, qtdNormaisPrimeira));

        List<Carta> segundaMetade = new ArrayList<>();
        if (qtdNormaisPrimeira < normais.size()) {
            segundaMetade.addAll(normais.subList(qtdNormaisPrimeira, normais.size()));
        }
        segundaMetade.addAll(dragoes);
        Collections.shuffle(segundaMetade);
        novaOrdem.addAll(segundaMetade);

        this.cartas.clear();
        this.cartas.addAll(novaOrdem);
    }

    /**
     * Operação do diagrama: comprarDoTopo(): Carta.
     *
     * Quando o monte zera, reciclamos o descarte automaticamente. Isso
     * garante que o jogo não trava por falta de cartas em partidas longas.
     */
    public Carta comprarDoTopo() {
        if (this.cartas.isEmpty() && !this.descarte.isEmpty()) {
            prepararNovaEraComDragaoNaSegundaMetade();
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
