package com.projeto.ethnos.model.carta;

import com.projeto.ethnos.model.Carta;
import com.projeto.ethnos.model.Jogador;
import com.projeto.ethnos.model.Partida;
import com.projeto.ethnos.model.Regiao;
import com.projeto.ethnos.model.poder.PoderDoLider;

import java.util.List;

/** Trolls ganham pontos proporcionais ao tamanho do bando que lideram. */
public class Troll extends Carta {

    public Troll(String cor) {
        super("Troll", cor, "Troll");
    }

    @Override
    public PoderDoLider getPoder() {
        return new PoderTroll();
    }

    private static final class PoderTroll implements PoderDoLider {
        @Override
        public String executar(Partida partida, Jogador jogador, List<Carta> bando, Regiao regiao) {
            // +1 por carta do bando, mínimo 1.
            int bonus = Math.max(1, bando == null ? 1 : bando.size());
            jogador.adicionarPontos(bonus);
            return "Poder (Troll): +" + bonus + " pontos para " + jogador.getNome();
        }
    }
}
