package com.projeto.ethnos.model.carta;

import com.projeto.ethnos.model.Carta;
import com.projeto.ethnos.model.Jogador;
import com.projeto.ethnos.model.Partida;
import com.projeto.ethnos.model.Regiao;
import com.projeto.ethnos.model.poder.PoderDoLider;

import java.util.List;

/**
 * Elfos recuperam uma carta do próprio bando para a mão. Representa a
 * habilidade da tribo de "preservar" recursos.
 */
public class Elfo extends Carta {

    public Elfo(String cor) {
        super("Elfo", cor, "Elfo");
    }

    @Override
    public PoderDoLider getPoder() {
        return new PoderElfo();
    }

    @Override
    public String getDescricaoPoder() {
        return "Recupera 1 carta do bando para a mao";
    }

    private static final class PoderElfo implements PoderDoLider {
        @Override
        public String executar(Partida partida, Jogador jogador, List<Carta> bando, Regiao regiao) {
            // Retorna uma carta do bando (diferente do líder) para a mão.
            Carta lider = jogador.getUltimoLider();
            for (Carta carta : bando) {
                if (carta != lider) {
                    jogador.getMao().add(carta);
                    return "Poder (Elfo): recupera " + carta + " para a mão";
                }
            }
            return "Poder (Elfo): bando muito pequeno, nada para recuperar";
        }
    }
}
