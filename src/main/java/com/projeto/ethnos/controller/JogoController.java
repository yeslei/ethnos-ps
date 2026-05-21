package com.projeto.ethnos.controller;

import com.projeto.ethnos.model.Carta;
import com.projeto.ethnos.model.Jogador;
import com.projeto.ethnos.model.Partida;
import com.projeto.ethnos.model.Regiao;
import com.projeto.ethnos.sound.SoundEffects;
import com.projeto.ethnos.view.MaoView;
import com.projeto.ethnos.view.MercadoView;
import com.projeto.ethnos.view.StatusView;
import com.projeto.ethnos.view.TabuleiroView;
import javafx.animation.PauseTransition;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.util.Duration;

import java.util.List;

/**
 * Controller do MVC: traduz eventos da UI JavaFX em chamadas à Partida.
 *
 * É importante distinguir dois "Controllers" diferentes neste projeto:
 *
 *  1. GRASP Controller: é a classe Partida — ela é quem recebe as
 *     "operações de sistema" (jogar bando, recrutar) e coordena os
 *     objetos de negócio.
 *
 *  2. MVC Controller (esta classe): traduz cliques/eventos do JavaFX
 *     em chamadas para Partida. Ela não conhece regras do jogo — apenas
 *     captura entrada, faz validações superficiais de UI e delega.
 *
 * Essa separação preserva GRASP Low Coupling: a Partida nunca importa
 * classes de javafx.
 */
public class JogoController {

    private final Partida partida;
    private final MercadoView mercadoView;
    private final MaoView maoView;
    private final TabuleiroView tabuleiroView;
    private final StatusView statusView;

    public JogoController(Partida partida,
                          MercadoView mercadoView,
                          MaoView maoView,
                          TabuleiroView tabuleiroView,
                          StatusView statusView) {
        this.partida = partida;
        this.mercadoView = mercadoView;
        this.maoView = maoView;
        this.tabuleiroView = tabuleiroView;
        this.statusView = statusView;

        configurarBotoes();
        atualizarHabilitacao();
    }

    private void configurarBotoes() {
        this.maoView.getBtnJogar().setOnAction(e -> acaoJogarBando());
        this.mercadoView.getDeckCompra().setOnMouseClicked(e -> acaoRecrutarAliado());
    }

    private void acaoJogarBando() {
        if (partida.isJogoFinalizado()) return;
        Jogador jogadorDaVez = partida.getJogadorAtual();
        if (jogadorDaVez.isIa()) {
            mostrarInfo("Aguarde", "É o turno da IA.");
            return;
        }

        List<Carta> cartasSelecionadas = maoView.getCartasSelecionadas();
        if (cartasSelecionadas.isEmpty()) {
            mostrarAviso("Seleção inválida", "Selecione cartas na sua mão primeiro.");
            return;
        }

        Carta lider = maoView.getLiderSelecionado();
        Regiao regiao = partida.getTabuleiro().getRegiao(lider.getCor());

        if (lider == null) {
            mostrarAviso("Seleção inválida",
                "Marque uma carta como líder antes de jogar (botão 'Marcar como líder').");
            return;
        }
        if (!cartasSelecionadas.contains(lider)) {
            mostrarAviso("Seleção inválida",
                "O líder marcado não está mais entre as cartas selecionadas. "
                + "Selecione a carta líder novamente ou marque outra.");
            return;
        }
        if (regiao == null) {
            mostrarAviso("Ação não permitida", "Nao existe regiao para a cor do lider.");
            return;
        }

        try {
            partida.iniciarJogadaDoBando(jogadorDaVez, cartasSelecionadas, lider, regiao);
        } catch (IllegalArgumentException ex) {
            mostrarAviso("Ação não permitida", ex.getMessage());
            return;
        }

        SoundEffects.playBando();
        mostrarConfirmacaoJogada(jogadorDaVez, lider, regiao);

        maoView.limparSelecaoCartas();
        mercadoView.limparSelecao();
        atualizarHabilitacao();
        executarTurnoIASeNecessario();
    }

    private void acaoRecrutarAliado() {
        if (partida.isJogoFinalizado()) return;
        Jogador jogadorDaVez = partida.getJogadorAtual();
        if (jogadorDaVez.isIa()) {
            mostrarInfo("Aguarde", "É o turno da IA.");
            return;
        }

        Carta escolhida = mercadoView.getCartaSelecionada();
        try {
            partida.comprarAliado(jogadorDaVez, escolhida);
        } catch (IllegalArgumentException ex) {
            mostrarAviso("Ação não permitida", ex.getMessage());
            return;
        }

        mercadoView.limparSelecao();
        atualizarHabilitacao();
        executarTurnoIASeNecessario();
    }

    private void executarTurnoIASeNecessario() {
        if (partida.isJogoFinalizado() || !partida.getJogadorAtual().isIa()) return;
        atualizarHabilitacao();

        PauseTransition pausa = new PauseTransition(Duration.millis(900));
        pausa.setOnFinished(e -> {
            partida.jogarTurnoIA();
            String ultima = partida.getUltimaAcao();
            if (ultima != null && ultima.contains("jogou um bando")) {
                SoundEffects.playBando();
            }
            atualizarHabilitacao();
            if (!partida.isJogoFinalizado() && partida.getJogadorAtual().isIa()) {
                executarTurnoIASeNecessario();
            }
        });
        pausa.play();
    }

    /** Bloqueia botões durante turno de IA / fim de jogo. */
    private void atualizarHabilitacao() {
        boolean turnoHumano = !partida.getJogadorAtual().isIa() && !partida.isJogoFinalizado();
        maoView.getBtnJogar().setDisable(!turnoHumano);
        maoView.getBtnMarcarLider().setDisable(!turnoHumano);
        mercadoView.getDeckCompra().setDisable(!turnoHumano);
    }

    private void mostrarAviso(String titulo, String mensagem) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void mostrarInfo(String titulo, String mensagem) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void mostrarConfirmacaoJogada(Jogador jogador, Carta lider, Regiao regiao) {
        String descricao = partida.getUltimaAcaoPoder();
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Jogada confirmada");
        alert.setHeaderText("Regiao definida pela cor do lider");
        StringBuilder texto = new StringBuilder();
        texto.append("Jogador: ").append(jogador.getNome())
            .append("\nLider: ").append(lider.getTribo())
            .append("\nRegiao: ").append(regiao.getNome());
        if (descricao != null && !descricao.isBlank()) {
            texto.append("\nPoder: ").append(descricao);
        } else {
            texto.append("\nPoder: sem poder ativo");
        }
        alert.setContentText(texto.toString());
        alert.showAndWait();
    }
}
