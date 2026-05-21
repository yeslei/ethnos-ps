package com.projeto.ethnos.model.carta;

import com.projeto.ethnos.model.Carta;
import com.projeto.ethnos.model.Jogador;
import com.projeto.ethnos.model.Partida;
import com.projeto.ethnos.model.Regiao;
import com.projeto.ethnos.model.poder.PoderDoLider;

import java.util.List;

/** Gigantes dão um bônus de pontos fixo (regra simplificada do jogo original). */
public class Gigante extends Carta {

    public Gigante(String cor) {
        super("Gigante", cor, "Gigante");
    }

    @Override
    public PoderDoLider getPoder() {
        return new PoderGigante();
    }

    private static final class PoderGigante implements PoderDoLider {
        @Override
        public String executar(Partida partida, Jogador jogador, List<Carta> bando, Regiao regiao) {
            jogador.adicionarPontos(2);
            return "Poder (Gigante): +2 pontos para " + jogador.getNome();
        }
    }
}
