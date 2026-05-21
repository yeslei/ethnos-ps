package com.projeto.ethnos.model.poder;

import com.projeto.ethnos.model.Carta;
import com.projeto.ethnos.model.Jogador;
import com.projeto.ethnos.model.Partida;
import com.projeto.ethnos.model.Regiao;

import java.util.List;

/**
 * Padrão GoF: Strategy.
 *
 * Cada tribo possui um poder de líder diferente. Em vez de concentrar toda a
 * lógica num switch gigante dentro de Partida (o que viola Open/Closed e gera
 * baixa coesão), encapsulamos o poder em objetos que implementam esta interface.
 *
 * Cada subclasse de Carta vai fornecer sua própria implementação de PoderDoLider,
 * e Partida apenas delega a execução: lider.getPoder().executar(...).
 *
 * Benefícios:
 *  - Adicionar uma nova tribo não exige alterar Partida (Open/Closed).
 *  - Cada poder é testável em isolado (alta coesão).
 *  - Substituível em tempo de execução se quisermos variantes de regras.
 */
public interface PoderDoLider {

    /**
     * Executa o poder do líder.
     *
     * @param partida  a partida em andamento (fornece acesso a baralho, mercado,
     *                 contador de dragões, etc.)
     * @param jogador  o jogador que está aplicando o poder
     * @param bando    as cartas do bando que foi baixado (inclui o líder)
     * @param regiao   a região onde o bando foi posicionado
     * @return descrição textual do efeito (para exibir na "última ação" da UI)
     */
    String executar(Partida partida, Jogador jogador, List<Carta> bando, Regiao regiao);
}
