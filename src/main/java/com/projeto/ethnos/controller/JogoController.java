package com.projeto.ethnos.controller;

import com.projeto.ethnos.model.Carta;
import com.projeto.ethnos.model.Jogador;
import com.projeto.ethnos.model.Partida;
import com.projeto.ethnos.model.Regiao;
import com.projeto.ethnos.view.MaoView;
import com.projeto.ethnos.view.MercadoView;
import com.projeto.ethnos.view.StatusView;
import com.projeto.ethnos.view.TabuleiroView;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.util.List;

public class JogoController {

    private Partida partida;
    private Jogador jogadorPrincipal;
    private MercadoView mercadoView;
    private MaoView maoView;
    private TabuleiroView tabuleiroView;
    private StatusView statusView;

    public JogoController(Partida partida, Jogador jogadorPrincipal, MercadoView mercadoView, MaoView maoView, TabuleiroView tabuleiroView, StatusView statusView) {
        this.partida = partida;
        this.jogadorPrincipal = jogadorPrincipal;
        this.mercadoView = mercadoView;
        this.maoView = maoView;
        this.tabuleiroView = tabuleiroView;
        this.statusView = statusView;
        
        configurarBotoes();
    }

    private void configurarBotoes() {
        // Ação: Jogar Bando
        this.maoView.getBtnJogar().setOnAction(event -> acaoJogarBando());
        this.maoView.getBtnAtivarPoder().setOnAction(event -> acaoAtivarPoder());
        
        // Ação: Recrutar Aliado (Comprar do deck)
        this.mercadoView.getDeckCompra().setOnMouseClicked(event -> acaoRecrutarAliado());
    }

    private void acaoJogarBando() {
        if (partida.isJogoFinalizado()) {
            return;
        }
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
        Regiao regiaoSelecionada = tabuleiroView.getRegiaoSelecionada();
        if (lider == null) {
            mostrarAviso("Seleção inválida", "Selecione um líder na lista antes de jogar o bando.");
            return;
        }
        if (!cartasSelecionadas.contains(lider)) {
            mostrarAviso("Seleção inválida", "O líder escolhido precisa estar entre as cartas selecionadas.");
            return;
        }
        try {
            partida.iniciarJogadaDoBando(jogadorDaVez, cartasSelecionadas, lider, regiaoSelecionada);
        } catch (IllegalArgumentException ex) {
            mostrarAviso("Ação não permitida", ex.getMessage());
            return;
        }

        // Melhoria: evita que seleções antigas "vazem" para o próximo turno.
        maoView.limparSelecaoCartas();
        mercadoView.limparSelecao();

        atualizarTelas();
        executarTurnoIASeNecessario();
    }

    private void acaoRecrutarAliado() {
        if (partida.isJogoFinalizado()) {
            return;
        }
        Jogador jogadorDaVez = partida.getJogadorAtual();
        if (jogadorDaVez.isIa()) {
            mostrarInfo("Aguarde", "É o turno da IA.");
            return;
        }

        Carta escolhidaNoMercado = mercadoView.getCartaSelecionada();
        try {
            partida.comprarAliado(jogadorDaVez, escolhidaNoMercado);
        } catch (IllegalArgumentException ex) {
            mostrarAviso("Ação não permitida", ex.getMessage());
            return;
        }

        // Melhoria: ao recrutar, limpamos a seleção do mercado.
        mercadoView.limparSelecao();
        
        atualizarTelas();
        executarTurnoIASeNecessario();
    }

    private void acaoAtivarPoder() {
        Jogador jogadorDaVez = partida.getJogadorAtual();
        if (jogadorDaVez.isIa()) {
            mostrarInfo("Aguarde", "É o turno da IA.");
            return;
        }
        Carta lider = maoView.getLiderSelecionado();
        Regiao regiao = tabuleiroView.getRegiaoSelecionada();
        if (lider == null || regiao == null) {
            mostrarAviso("Seleção inválida", "Selecione líder e região para ativar o poder.");
            return;
        }
        partida.aplicarPoderDoLider(jogadorDaVez, lider, List.of(lider), regiao);
        atualizarTelas();
    }

    private void executarTurnoIASeNecessario() {
        while (!partida.isJogoFinalizado() && partida.getJogadorAtual().isIa()) {
            partida.jogarTurnoIA();
            atualizarTelas();
        }
    }

    private void atualizarTelas() {
        Jogador jogadorDaVez = partida.getJogadorAtual();
        boolean turnoHumano = !jogadorDaVez.isIa() && !partida.isJogoFinalizado();

        // Essencial: impede ações fora do turno (principalmente durante a IA).
        this.maoView.getBtnJogar().setDisable(!turnoHumano);
        this.maoView.getBtnAtivarPoder().setDisable(!turnoHumano);
        this.mercadoView.getDeckCompra().setDisable(!turnoHumano);

        this.maoView.setJogadorModel(jogadorDaVez);
        maoView.atualizarVisualizacao();
        mercadoView.atualizarVisualizacao();
        statusView.atualizarVisualizacao();
        tabuleiroView.atualizarVisualizacao();
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
}