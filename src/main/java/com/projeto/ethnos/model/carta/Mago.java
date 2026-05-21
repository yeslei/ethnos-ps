package com.projeto.ethnos.model.carta;

import com.projeto.ethnos.model.Carta;
import com.projeto.ethnos.model.Jogador;
import com.projeto.ethnos.model.Partida;
import com.projeto.ethnos.model.Regiao;
import com.projeto.ethnos.model.poder.PoderDoLider;

import java.util.List;

/** Magos compram uma carta do baralho ao baixar bando. */
public class Mago extends Carta {

    public Mago(String cor) {
        super("Mago", cor, "Mago");
    }

    @Override
    public PoderDoLider getPoder() {
        return new PoderMago();
    }

    private static final class PoderMago implements PoderDoLider {
        @Override
        public String executar(Partida partida, Jogador jogador, List<Carta> bando, Regiao regiao) {
            // O Mago "saca" do topo. A própria Partida cuida do caso de Dragão revelado.
            Carta comprada = partida.comprarParaMao(jogador);
            if (comprada == null) {
                return "Poder (Mago): sem cartas disponíveis para sacar";
            }
            return "Poder (Mago): saca " + comprada + " do baralho";
        }
    }
}
