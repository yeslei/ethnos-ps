package com.projeto.ethnos.view;

import com.projeto.ethnos.model.Carta;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Representação visual de uma Carta. Não participa do Observer (é um
 * componente "filho" das Views que assinam Partida).
 */
public class CartaView extends StackPane {

    private final Carta cartaModel;
    private boolean selecionada = false;
    private final Rectangle fundo;

    public CartaView(Carta cartaModel) {
        this.cartaModel = cartaModel;

        fundo = new Rectangle(80, 110);
        fundo.setFill(Color.WHITE);
        fundo.setStroke(Color.BLACK);
        fundo.setStrokeWidth(1);
        fundo.setArcWidth(10);
        fundo.setArcHeight(10);

        Rectangle faixaCor = new Rectangle(78, 25);
        faixaCor.setFill(obterCorVisual(cartaModel.getCor()));

        Label triboLabel = new Label(cartaModel.getTribo());
        triboLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: black;");

        VBox conteudo = new VBox(5);
        conteudo.setAlignment(Pos.TOP_CENTER);
        conteudo.getChildren().addAll(faixaCor, triboLabel);

        this.getChildren().addAll(fundo, conteudo);

        this.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> toggleSelecao());
        this.setOnMouseEntered(e -> { if (!selecionada) fundo.setStrokeWidth(3); });
        this.setOnMouseExited(e -> { if (!selecionada) fundo.setStrokeWidth(1); });
    }

    private void toggleSelecao() {
        setSelecionada(!selecionada);
    }

    private Color obterCorVisual(String corTexto) {
        if (corTexto == null) return Color.LIGHTGRAY;
        switch (corTexto.toLowerCase()) {
            case "vermelho": return Color.SALMON;
            case "verde":    return Color.LIGHTGREEN;
            case "amarelo":  return Color.KHAKI;
            case "azul":     return Color.LIGHTBLUE;
            case "roxo":     return Color.THISTLE;
            case "cinza":    return Color.DARKGRAY;
            default:         return Color.WHITE;
        }
    }

    public Carta getCartaModel() { return cartaModel; }
    public boolean isSelecionada() { return selecionada; }

    public void setSelecionada(boolean selecionada) {
        this.selecionada = selecionada;
        if (selecionada) {
            fundo.setStroke(Color.DODGERBLUE);
            fundo.setStrokeWidth(4);
            this.setTranslateY(-15);
        } else {
            fundo.setStroke(Color.BLACK);
            fundo.setStrokeWidth(1);
            this.setTranslateY(0);
        }
    }
}
