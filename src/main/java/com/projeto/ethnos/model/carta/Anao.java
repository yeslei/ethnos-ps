package com.projeto.ethnos.model.carta;

import com.projeto.ethnos.model.Carta;
import com.projeto.ethnos.model.Jogador;
import com.projeto.ethnos.model.Partida;
import com.projeto.ethnos.model.Regiao;
import com.projeto.ethnos.model.poder.PoderDoLider;

import java.util.List;

/**
 * Subclasse concreta de Carta. Anões são uma tribo "leal": ganham um bônus
 * de pontuação extra quando lideram um bando.
 *
 * O poder é fornecido como uma classe interna implementando PoderDoLider
 * (GoF Strategy). Esse padrão se repete em todas as subclasses de Carta.
 */
public class Anao extends Carta {

    public Anao(String cor) {
        super("Anão", cor, "Anão");
    }

    @Override
    public PoderDoLider getPoder() {
        return new PoderAnao();
    }

    @Override
    public String getDescricaoPoder() {
        return "+1 ponto ao liderar um bando";
    }

    private static final class PoderAnao implements PoderDoLider {
        @Override
        public String executar(Partida partida, Jogador jogador, List<Carta> bando, Regiao regiao) {
            // Poder do Anão: +1 ponto fixo por liderar um bando.
            jogador.adicionarPontos(1);
            return "Poder (Anão): +1 ponto para " + jogador.getNome();
        }
    }
}
