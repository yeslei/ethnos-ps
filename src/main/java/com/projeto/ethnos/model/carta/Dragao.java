package com.projeto.ethnos.model.carta;

import com.projeto.ethnos.model.Carta;

/**
 * Dragão é uma carta especial: ela não vai para a mão dos jogadores,
 * é apenas "revelada" e contribui para o contador de dragões da partida.
 *
 * Quando 3 dragões são revelados, a era termina (regra central do Ethnos).
 *
 * Esta subclasse intencionalmente NÃO retorna um PoderDoLider: o método
 * herdado de Carta devolve null, e a Partida trata dragões como caso
 * especial (controle de fim de era). Isso preserva o princípio de
 * Polimorfismo (GRASP) sem precisar de instanceof.
 */
public class Dragao extends Carta {

    public Dragao(String cor) {
        super("Dragão", cor, "Dragão");
    }

    /**
     * Conveniência para a Partida verificar dragões sem usar instanceof
     * nem comparar strings.
     */
    public static boolean ehDragao(Carta c) {
        return c instanceof Dragao;
    }

    @Override
    public String getDescricaoPoder() {
        return "Sem poder (fim de era ao revelar)";
    }
}
