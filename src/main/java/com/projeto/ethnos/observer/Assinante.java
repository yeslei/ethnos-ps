package com.projeto.ethnos.observer;

import com.projeto.ethnos.model.Partida;

/**
 * Padrão GoF: Observer (papel do Observer).
 *
 * Esta interface aparece explicitamente no diagrama de classes atualizado
 * (classe abstrata "Assinante" com a operação atualiza(p: Partida): Void).
 *
 * As Views (MercadoView, MaoView, TabuleiroView, StatusView) implementam
 * esta interface. A Partida atua como Subject e mantém uma lista de
 * assinantes; quando seu estado muda (jogada, troca de turno, fim de era),
 * ela chama notifica() que dispara atualiza(this) em cada assinante.
 *
 * Benefícios diretos no projeto:
 *  - O modelo (Partida) deixa de conhecer JavaFX. Pode ser testado sem UI.
 *  - Acoplamento baixo (GRASP Low Coupling) entre Model e View.
 *  - Adicionar uma nova View (ex.: HistoricoView, MiniMapaView) não muda
 *    o código da Partida — basta o construtor da nova view se inscrever.
 */
public interface Assinante {

    /**
     * Notificação enviada pela Partida quando seu estado muda.
     * Cada assinante decide o que fazer com a partida (geralmente, ler o
     * estado e redesenhar sua parte da UI).
     */
    void atualiza(Partida p);
}
