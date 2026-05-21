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

        fundo = new Rectangle(88, 120);
        fundo.setFill(Color.web(UiPalette.getParchmentBackground()));
        fundo.setStroke(Color.web(UiPalette.getWoodBorder()));
        fundo.setStrokeWidth(1.5);
        fundo.setArcWidth(10);
        fundo.setArcHeight(10);

        Rectangle faixaCor = new Rectangle(84, 24);
        faixaCor.setFill(UiPalette.getCardStripe(cartaModel.getCor()));

        Label triboLabel = new Label(cartaModel.getTribo());
        triboLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2B1B12; -fx-font-size: 12px;");

        Label poderLabel = new Label(cartaModel.getDescricaoPoder());
        poderLabel.setWrapText(true);
        poderLabel.setMaxWidth(80);
        poderLabel.setStyle("-fx-text-fill: #4D3B2A; -fx-font-size: 9px;");

        VBox conteudo = new VBox(4);
        conteudo.setAlignment(Pos.TOP_CENTER);
        conteudo.getChildren().addAll(faixaCor, triboLabel, poderLabel);

        this.getChildren().addAll(fundo, conteudo);

        this.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> toggleSelecao());
        this.setOnMouseEntered(e -> { if (!selecionada) fundo.setStrokeWidth(3); });
        this.setOnMouseExited(e -> { if (!selecionada) fundo.setStrokeWidth(1); });
    }

    private void toggleSelecao() {
        setSelecionada(!selecionada);
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
