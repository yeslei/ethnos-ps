package com.projeto.ethnos.model.carta;

import com.projeto.ethnos.model.Carta;
import com.projeto.ethnos.model.Jogador;
import com.projeto.ethnos.model.Partida;
import com.projeto.ethnos.model.Regiao;
import com.projeto.ethnos.model.poder.PoderDoLider;

import java.util.List;

/**
 * Centauros recebem uma carta extra do baralho ao baixar bando. Simboliza
 * a velocidade/exploração da tribo.
 */
public class Centauro extends Carta {

    public Centauro(String cor) {
        super("Centauro", cor, "Centauro");
    }

    @Override
    public PoderDoLider getPoder() {
        return new PoderCentauro();
    }

    private static final class PoderCentauro implements PoderDoLider {
        @Override
        public String executar(Partida partida, Jogador jogador, List<Carta> bando, Regiao regiao) {
            Carta comprada = partida.comprarParaMao(jogador);
            if (comprada == null) {
                return "Poder (Centauro): sem cartas para comprar";
            }
            return "Poder (Centauro): exploração rendeu " + comprada;
        }
    }
}
