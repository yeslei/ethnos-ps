package com.projeto.ethnos.view;

import com.projeto.ethnos.model.Carta;
import com.projeto.ethnos.model.Mercado;
import com.projeto.ethnos.model.Partida;
import com.projeto.ethnos.observer.Assinante;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/**
 * View do Mercado. Implementa Assinante (GoF Observer) — recebe atualiza(p)
 * sempre que a Partida muda de estado.
 */
public class MercadoView extends HBox implements Assinante {

    private final Mercado mercadoModel;
    private final VBox deckCompra;
    private final List<CartaView> cartasVisuais = new ArrayList<>();

    public MercadoView(Mercado mercadoModel) {
        this.mercadoModel = mercadoModel;

        this.setPadding(new Insets(10));
        this.setSpacing(15);
        this.setAlignment(Pos.CENTER);
        this.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #ccc;");

        this.deckCompra = new VBox();
        this.deckCompra.setAlignment(Pos.CENTER);
        this.deckCompra.setStyle("-fx-border-color: black; -fx-background-color: #777; -fx-pref-width: 80px; -fx-pref-height: 110px;");
        Label lblCompra = new Label("Comprar");
        lblCompra.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        this.deckCompra.getChildren().add(lblCompra);

        this.deckCompra.setOnMouseEntered(e -> this.deckCompra.setStyle(
            "-fx-border-color: white; -fx-border-width: 2px; -fx-background-color: #555; -fx-pref-width: 80px; -fx-pref-height: 110px;"));
        this.deckCompra.setOnMouseExited(e -> this.deckCompra.setStyle(
            "-fx-border-color: black; -fx-border-width: 1px; -fx-background-color: #777; -fx-pref-width: 80px; -fx-pref-height: 110px;"));

        redesenhar();
    }

    /**
     * GoF Observer — callback chamado pela Partida via notifica().
     * Não usamos diretamente o argumento porque já temos a referência
     * para Mercado; passar Partida permitiria a outras views reagirem a
     * mais informações (turno, era etc.).
     */
    @Override
    public void atualiza(Partida p) {
        redesenhar();
    }

    private void redesenhar() {
        this.getChildren().clear();
        this.cartasVisuais.clear();

        Label titulo = new Label("Mercado de Cartas:");
        titulo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        this.getChildren().add(titulo);

        for (Carta c : mercadoModel.getCartasDisponiveis()) {
            CartaView visualCarta = new CartaView(c);
            this.getChildren().add(visualCarta);
            this.cartasVisuais.add(visualCarta);

            visualCarta.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                if (visualCarta.isSelecionada()) {
                    for (CartaView outra : cartasVisuais) {
                        if (outra != visualCarta) outra.setSelecionada(false);
                    }
                }
            });
        }

        this.getChildren().add(this.deckCompra);
    }

    public VBox getDeckCompra() {
        return this.deckCompra;
    }

    public Carta getCartaSelecionada() {
        for (CartaView cv : cartasVisuais) {
            if (cv.isSelecionada()) return cv.getCartaModel();
        }
        return null;
    }

    public void limparSelecao() {
        for (CartaView cv : cartasVisuais) cv.setSelecionada(false);
    }
}
