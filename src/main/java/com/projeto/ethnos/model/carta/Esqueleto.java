package com.projeto.ethnos.model.carta;

import com.projeto.ethnos.model.Carta;
import com.projeto.ethnos.model.Jogador;
import com.projeto.ethnos.model.Partida;
import com.projeto.ethnos.model.Regiao;
import com.projeto.ethnos.model.poder.PoderDoLider;

import java.util.List;

/**
 * Esqueletos têm um efeito de "dreno": o jogador ganha 1 ponto e o
 * oponente líder em pontos perde 1 (não menos que zero).
 */
public class Esqueleto extends Carta {

    public Esqueleto(String cor) {
        super("Esqueleto", cor, "Esqueleto");
    }

    @Override
    public PoderDoLider getPoder() {
        return new PoderEsqueleto();
    }

    @Override
    public String getDescricaoPoder() {
        return "Rouba 1 ponto do jogador com mais pontos";
    }

    private static final class PoderEsqueleto implements PoderDoLider {
        @Override
        public String executar(Partida partida, Jogador jogador, List<Carta> bando, Regiao regiao) {
            Jogador alvo = null;
            int maior = Integer.MIN_VALUE;
            for (Jogador j : partida.getJogadores()) {
                if (j != jogador && j.getPontuacao() > maior) {
                    maior = j.getPontuacao();
                    alvo = j;
                }
            }
            if (alvo == null) {
                return "Poder (Esqueleto): sem alvos disponíveis";
            }
            alvo.removerPontos(1);
            jogador.adicionarPontos(1);
            return "Poder (Esqueleto): drenou 1 ponto de " + alvo.getNome();
        }
    }
}
