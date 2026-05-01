package com.projeto.ethnos.model;

import java.util.ArrayList;
import java.util.List;

public class Jogador {
    private String nome;
    private int pontuacao;
    private int fichasRestantes;
    private boolean ia;

    // A mão precisa ser acessível para a Partida pegar as sobras no final da jogada (passo 12)
    public List<Carta> mao = new ArrayList<>(); 
    private List<Bando> bandos = new ArrayList<>();

    public Jogador(String nome) {
        this(nome, false);
    }

    public Jogador(String nome, boolean ia) {
        this.nome = nome;
        this.pontuacao = 0;
        this.fichasRestantes = 15;
        this.ia = ia;
    }

    public int distribuirFicha(Carta lider) {
        if (fichasRestantes > 0) {
            fichasRestantes--;
            return 1;
        }
        return 0;
    }

    public void recrutarAliado(Baralho baralho) {
        Carta comprada = baralho.comprarDoTopo();
        if (comprada != null) {
            mao.add(comprada);
        }
    }

    public void jogarBando(List<Carta> cartasSelecionadas, Carta lider) {
        // Passo 3 do diagrama de sequência: <<create>> Bando
        Bando novoBando = new Bando(cartasSelecionadas, lider);
        this.bandos.add(novoBando);
        
        // Remove da mão apenas as cartas que formaram o bando
        this.mao.removeAll(cartasSelecionadas);
    }

    public String getNome() {
        return nome;
    }

    public boolean isIa() {
        return ia;
    }

    public int getPontuacao() {
        return pontuacao;
    }

    public int getFichasRestantes() {
        return fichasRestantes;
    }

    public List<Bando> getBandos() {
        return bandos;
    }

    public void adicionarPontos(int pontos) {
        this.pontuacao += Math.max(0, pontos);
    }
}