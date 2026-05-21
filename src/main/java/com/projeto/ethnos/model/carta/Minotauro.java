package com.projeto.ethnos.model.carta;

import com.projeto.ethnos.model.Carta;
import com.projeto.ethnos.model.Jogador;
import com.projeto.ethnos.model.Partida;
import com.projeto.ethnos.model.Regiao;
import com.projeto.ethnos.model.poder.PoderDoLider;

import java.util.List;

/**
 * Minotauros são "agressivos": colocam uma ficha adicional na região onde o
 * bando foi posicionado. Isso simula a regra original do Ethnos onde o
 * Minotauro permite plantar duas fichas.
 */
public class Minotauro extends Carta {

    public Minotauro(String cor) {
        super("Minotauro", cor, "Minotauro");
    }

    @Override
    public PoderDoLider getPoder() {
        return new PoderMinotauro();
    }

    private static final class PoderMinotauro implements PoderDoLider {
        @Override
        public String executar(Partida partida, Jogador jogador, List<Carta> bando, Regiao regiao) {
            if (regiao == null) {
                return "Poder (Minotauro): sem região alvo";
            }
            regiao.adicionarMarcador(jogador);
            return "Poder (Minotauro): ficha adicional em " + regiao.getNome();
        }
    }
}
