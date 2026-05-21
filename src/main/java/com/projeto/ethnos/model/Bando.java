package com.projeto.ethnos.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa um conjunto de cartas baixadas por um Jogador, com um líder.
 *
 * Padrão GRASP: Creator.
 *  - O criador natural de Bando é Jogador, porque Jogador "agrega" Bandos
 *    (relação composta no diagrama: Jogador <>-- Possui 1..* Bando) e
 *    possui as informações necessárias para criar (a mão e o líder).
 *  - A operação <<create>> aparece destacada no diagrama de classes.
 *
 * Bando é deliberadamente imutável após criação: representa uma jogada
 * concluída. Isso simplifica o raciocínio sobre estado (alta coesão).
 */
public class Bando {

    private final List<Carta> cartas;
    private final Carta lider;
    private final int tamanho;

    public Bando(List<Carta> cartas, Carta lider) {
        if (cartas == null || cartas.isEmpty()) {
            throw new IllegalArgumentException("Bando precisa ter ao menos uma carta");
        }
        if (lider == null) {
            throw new IllegalArgumentException("Bando precisa de um líder");
        }
        if (!cartas.contains(lider)) {
            throw new IllegalArgumentException("Líder deve estar no bando");
        }
        this.cartas = new ArrayList<>(cartas);
        this.lider = lider;
        this.tamanho = this.cartas.size();
    }

    /**
     * Tabela oficial do Ethnos: pontos por tamanho do bando.
     * Índice = tamanho do bando. Posição 0 não é usada.
     *
     *  1 carta  -> 0 pontos (não vale glória)
     *  2 cartas -> 1
     *  3 cartas -> 3
     *  4 cartas -> 6
     *  5 cartas -> 10
     *  6+       -> 15 (teto)
     */
    private static final int[] TABELA_PONTOS_POR_TAMANHO = { 0, 0, 1, 3, 6, 10, 15 };

    /**
     * GRASP Information Expert: o próprio Bando sabe calcular sua pontuação base,
     * pois detém a informação necessária (tamanho e líder).
     *
     * A partir da Parte 2, segue a tabela oficial do jogo (regra das gemas/glória).
     * O bônus do Anão (+1 no tamanho efetivo) é tratado como "como se o bando
     * tivesse uma carta a mais" para efeito desta tabela, respeitando a regra
     * do jogo original.
     */
    public int calcularPontos() {
        int tamanhoEfetivo = tamanho;
        if ("Anão".equalsIgnoreCase(lider.getTribo())) {
            tamanhoEfetivo += 1;
        }
        // Teto: 6 ou mais cartas valem 15 pontos.
        int indice = Math.min(tamanhoEfetivo, TABELA_PONTOS_POR_TAMANHO.length - 1);
        return TABELA_PONTOS_POR_TAMANHO[indice];
    }

    public Carta getLider() {
        return lider;
    }

    public List<Carta> getCartas() {
        return List.copyOf(cartas);
    }

    public int getTamanho() {
        return tamanho;
    }
}
