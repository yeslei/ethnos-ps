package com.projeto.ethnos.model;

import java.util.ArrayList;
import java.util.List;

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