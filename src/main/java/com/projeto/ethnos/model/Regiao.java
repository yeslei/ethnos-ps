package com.projeto.ethnos.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Regiao {
    private String nome;
    private List<Integer> valoresPontuacao;
    
    // Novo: Lista que representa as fichas (marcadores) dos jogadores nesta região
    private List<Jogador> marcadores = new ArrayList<>();

    public Regiao(String nome, List<Integer> valoresPontuacao) {
        this.nome = nome;
        this.valoresPontuacao = valoresPontuacao;
    }

    public void adicionarMarcador(Jogador jogador) {
        this.marcadores.add(jogador); // Salva a ficha de verdade
        System.out.println("Marcador de " + jogador.getNome() + " adicionado na região " + this.nome);
    }

    public Map<Jogador, Integer> contarMarcadoresPorJogador() {
        Map<Jogador, Integer> contagem = new HashMap<>();
        for (Jogador j : marcadores) {
            contagem.put(j, contagem.getOrDefault(j, 0) + 1);
        }
        return contagem;
    }

    public List<Jogador> rankingJogadoresPorMarcadores() {
        Map<Jogador, Integer> contagem = contarMarcadoresPorJogador();
        List<Jogador> ranking = new ArrayList<>(contagem.keySet());
        ranking.sort(Comparator
            .<Jogador>comparingInt(j -> contagem.getOrDefault(j, 0))
            .reversed()
            .thenComparing(Jogador::getNome));
        return ranking;
    }

    public int getPontuacao(int posicao) {
        if (posicao >= 0 && posicao < valoresPontuacao.size()) {
            return valoresPontuacao.get(posicao);
        }
        return 0;
    }

    public String getNome() { return nome; }
    
    // Getter para a tela poder ler quem tem ficha aqui
    public List<Jogador> getMarcadores() { return marcadores; }
}