package com.projeto.ethnos.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Uma região do tabuleiro. No diagrama, Regiao tem nome e valoresPontuacao
 * (lista de pontos por colocação 1º/2º/3º).
 *
 * Padrão GRASP: Information Expert.
 *  - Quem sabe quem colocou marcadores na região? A própria região.
 *  - Quem sabe quantos pontos cada posição vale? A própria região.
 *  - Portanto, o cálculo do ranking de jogadores e da pontuação por
 *    colocação fica aqui (e não na Partida), conforme o princípio.
 */
public class Regiao {

    private final String nome;
    private final List<Integer> valoresPontuacao;
    private final List<Jogador> marcadores = new ArrayList<>();

    public Regiao(String nome, List<Integer> valoresPontuacao) {
        this.nome = nome;
        this.valoresPontuacao = List.copyOf(valoresPontuacao);
    }

    /** Operação do diagrama: adicionarMarcador(jogador). */
    public void adicionarMarcador(Jogador jogador) {
        if (jogador != null) {
            this.marcadores.add(jogador);
        }
    }

    /**
     * Operação do diagrama: getPontuacao(posicao).
     * Compatibilidade com a interface original — usa a era 3 (todas as posições contam).
     */
    public int getPontuacao(int posicao) {
        return getPontuacao(posicao, 3);
    }

    /**
     * Regra do Ethnos: na era N, apenas as N primeiras colocações pontuam,
     * e os valores são lidos das N maiores faixas da região (do menor pro maior).
     *
     *  Era 1: 1º colocado ganha valoresPontuacao[0] (o menor valor da região).
     *  Era 2: 1º ganha valoresPontuacao[1], 2º ganha valoresPontuacao[0].
     *  Era 3: 1º ganha valoresPontuacao[2], 2º ganha valoresPontuacao[1], 3º ganha valoresPontuacao[0].
     *
     * Isso corresponde à mecânica das fichas de pontuação reveladas em cada era
     * (era 1 = 1 ponto, era 2 = 3 pontos, era 3 = 6 pontos, na configuração padrão).
     *
     * @param posicao posição no ranking (0 = primeiro lugar)
     * @param era     era atual da partida (1, 2 ou 3)
     */
    public int getPontuacao(int posicao, int era) {
        if (posicao < 0 || era < 1) return 0;
        // Na era N, só as N primeiras posições pontuam.
        if (posicao >= era) return 0;
        // Valor entregue à posição p (0=1º lugar) na era N:
        //   valoresPontuacao[(N - 1) - p]
        int indiceValor = (era - 1) - posicao;
        if (indiceValor < 0 || indiceValor >= valoresPontuacao.size()) return 0;
        return valoresPontuacao.get(indiceValor);
    }

    /**
     * GRASP Information Expert: a região é a especialista em calcular
     * o ranking porque ela detém a lista de marcadores.
     *
     * Critério de desempate: ordem alfabética do nome (estável e simples).
     */
    public List<Jogador> rankingJogadoresPorMarcadores() {
        Map<Jogador, Integer> contagem = new HashMap<>();
        for (Jogador j : marcadores) {
            contagem.merge(j, 1, Integer::sum);
        }
        List<Jogador> ranking = new ArrayList<>(contagem.keySet());
        ranking.sort(
            Comparator.<Jogador>comparingInt(j -> contagem.getOrDefault(j, 0))
                .reversed()
                .thenComparing(Jogador::getNome)
        );
        return ranking;
    }

    public String getNome() {
        return nome;
    }

    public List<Jogador> getMarcadores() {
        return List.copyOf(marcadores);
    }

    public List<Integer> getValoresPontuacao() {
        return valoresPontuacao;
    }
}
