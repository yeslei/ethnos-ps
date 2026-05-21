package com.projeto.ethnos.view;

import com.projeto.ethnos.model.Carta;
import com.projeto.ethnos.model.Jogador;
import com.projeto.ethnos.model.Partida;
import com.projeto.ethnos.observer.Assinante;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;

/**
 * Mão do jogador da vez. Implementa Assinante: a cada notifica() da Partida,
 * lê o estado e redesenha-se.
 *
 * UX da escolha de líder (refeita na Parte 2):
 *  - Clique simples seleciona a carta para o bando.
 *  - O botão "Marcar como líder" promove a carta selecionada para líder
 *    (uma estrela amarela aparece sobre ela).
 *  - O líder fica destacado visualmente enquanto outras cartas podem ser
 *    selecionadas livremente para compor o bando.
 *  - Um painel de instruções deixa o fluxo explícito.
 */
public class MaoView extends VBox implements Assinante {

    private Jogador jogadorModel;
    private final Button btnJogar;
    private final Button btnMarcarLider;
    private Carta liderSelecionado;
    private final List<CartaView> cartasVisuaisExibidas = new ArrayList<>();
    private final Label lblLiderAtual;

    public MaoView(Jogador jogadorModel) {
        this.jogadorModel = jogadorModel;

        this.btnJogar = new Button("Jogar Bando");
        this.btnJogar.setStyle(
            "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; "
            + "-fx-font-size: 13px; -fx-padding: 8 16 8 16;");

        this.btnMarcarLider = new Button("Marcar como líder ★");
        this.btnMarcarLider.setStyle(
            "-fx-background-color: #FFC107; -fx-text-fill: #333; -fx-font-weight: bold; "
            + "-fx-padding: 8 16 8 16;");
        this.btnMarcarLider.setOnAction(e -> promoverParaLider());

        this.lblLiderAtual = new Label("Líder: (nenhum)");
        this.lblLiderAtual.setStyle("-fx-font-weight: bold; -fx-text-fill: #B8860B;");

        this.setAlignment(Pos.CENTER);
        this.setPadding(new Insets(10));
        this.setSpacing(10);

        redesenhar();
    }

    @Override
    public void atualiza(Partida p) {
        // Quando o turno muda, o jogador "da vez" pode mudar.
        Jogador novoJogador = p.getJogadorAtual();
        if (novoJogador != this.jogadorModel) {
            // Resetar líder ao trocar de jogador.
            this.liderSelecionado = null;
        }
        this.jogadorModel = novoJogador;
        // Se o líder ainda existir na nova mão, mantém. Se não, limpa.
        if (this.liderSelecionado != null && !jogadorModel.getMao().contains(this.liderSelecionado)) {
            this.liderSelecionado = null;
        }
        redesenhar();
    }

    private void promoverParaLider() {
        List<Carta> selecionadas = getCartasSelecionadas();
        if (selecionadas.size() != 1) {
            return; // só faz sentido com exatamente uma selecionada
        }
        this.liderSelecionado = selecionadas.get(0);
        redesenhar();
    }

    private void redesenhar() {
        this.getChildren().clear();
        this.cartasVisuaisExibidas.clear();

        Label lblMao = new Label("Mão de " + jogadorModel.getNome()
            + " (" + jogadorModel.getMao().size() + " cartas):");
        lblMao.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // Atualiza label do líder atual
        if (this.liderSelecionado != null) {
            this.lblLiderAtual.setText("Líder: " + this.liderSelecionado.getTribo()
                + " (" + this.liderSelecionado.getCor() + ") ★");
        } else {
            this.lblLiderAtual.setText("Líder: (nenhum - selecione uma carta e clique em 'Marcar como líder')");
        }

        // Linha de cartas com indicador visual de líder
        HBox cartasBox = new HBox(10);
        cartasBox.setAlignment(Pos.CENTER);
        for (Carta c : jogadorModel.getMao()) {
            StackPane container = new StackPane();
            CartaView visualCarta = new CartaView(c);
            container.getChildren().add(visualCarta);

            // Se essa carta é o líder atual, sobrepõe uma estrela amarela
            if (c == this.liderSelecionado) {
                Label estrela = new Label("★");
                estrela.setTextFill(Color.GOLD);
                estrela.setFont(Font.font("System", FontWeight.BOLD, 32));
                estrela.setStyle("-fx-effect: dropshadow(gaussian, black, 4, 0.5, 0, 0);");
                StackPane.setAlignment(estrela, Pos.TOP_RIGHT);
                container.getChildren().add(estrela);
            }

            cartasBox.getChildren().add(container);
            this.cartasVisuaisExibidas.add(visualCarta);

            visualCarta.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                // Clique simples só seleciona. Promover para líder é via botão.
            });
        }

        // Painel de ações
        HBox acoes = new HBox(10, this.btnMarcarLider, this.btnJogar);
        acoes.setAlignment(Pos.CENTER);

        // Painel de instruções
        Label instrucao = new Label(
            "1) Clique nas cartas que comporão o bando.  "
            + "2) Selecione UMA delas e clique 'Marcar como líder'.  "
            + "3) Clique numa região do tabuleiro.  "
            + "4) Clique 'Jogar Bando'.");
        instrucao.setWrapText(true);
        instrucao.setStyle("-fx-text-fill: #555555; -fx-font-style: italic;");
        instrucao.setMaxWidth(900);

        this.getChildren().addAll(lblMao, cartasBox, this.lblLiderAtual, acoes, instrucao);
    }

    public Button getBtnJogar() { return btnJogar; }
    public Button getBtnMarcarLider() { return btnMarcarLider; }
    public Carta getLiderSelecionado() { return liderSelecionado; }

    public List<Carta> getCartasSelecionadas() {
        List<Carta> selecionadas = new ArrayList<>();
        for (CartaView cv : cartasVisuaisExibidas) {
            if (cv.isSelecionada()) selecionadas.add(cv.getCartaModel());
        }
        return selecionadas;
    }

    public void limparSelecaoCartas() {
        for (CartaView cv : cartasVisuaisExibidas) cv.setSelecionada(false);
        this.liderSelecionado = null;
        redesenhar();
    }
}
