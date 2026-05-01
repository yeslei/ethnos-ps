package com.projeto.ethnos.view;

import com.projeto.ethnos.model.Carta;
import com.projeto.ethnos.model.Mercado;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.input.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class MercadoView extends HBox {

    private Mercado mercadoModel;
    private VBox deckCompra; // Transformamos em atributo!
    private List<CartaView> cartasVisuais = new ArrayList<>();

    public MercadoView(Mercado mercadoModel) {
        this.mercadoModel = mercadoModel;
        
        this.setPadding(new Insets(10));
        this.setSpacing(15);
        this.setAlignment(Pos.CENTER);
        this.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #ccc;");
        
        // Criação e estilização do Deck de Compra
        this.deckCompra = new VBox();
        this.deckCompra.setAlignment(Pos.CENTER);
        this.deckCompra.setStyle("-fx-border-color: black; -fx-background-color: #777; -fx-pref-width: 80px; -fx-pref-height: 110px;");
        Label lblCompra = new Label("Comprar");
        lblCompra.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        this.deckCompra.getChildren().add(lblCompra);
        
        // Efeito visual ao passar o mouse
        this.deckCompra.setOnMouseEntered(e -> this.deckCompra.setStyle("-fx-border-color: white; -fx-border-width: 2px; -fx-background-color: #555; -fx-pref-width: 80px; -fx-pref-height: 110px;"));
        this.deckCompra.setOnMouseExited(e -> this.deckCompra.setStyle("-fx-border-color: black; -fx-border-width: 1px; -fx-background-color: #777; -fx-pref-width: 80px; -fx-pref-height: 110px;"));

        atualizarVisualizacao();
    }

    public void atualizarVisualizacao() {
        this.getChildren().clear(); 
        this.cartasVisuais.clear();
        
        Label titulo = new Label("Mercado de Cartas:");
        titulo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        this.getChildren().add(titulo);

        for (Carta c : mercadoModel.getCartasDisponiveis()) {
            CartaView visualCarta = new CartaView(c);
            this.getChildren().add(visualCarta);
            this.cartasVisuais.add(visualCarta);

            // Melhoria de UX: seleção única no mercado (evita comprar a carta errada).
            visualCarta.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                if (visualCarta.isSelecionada()) {
                    for (CartaView outra : cartasVisuais) {
                        if (outra != visualCarta) {
                            outra.setSelecionada(false);
                        }
                    }
                }
            });
        }

        // Adiciona o deck sempre por último na tela
        this.getChildren().add(this.deckCompra);
    }

    // Getter para o Controller adicionar o evento de clique!
    public VBox getDeckCompra() {
        return this.deckCompra;
    }

    public Carta getCartaSelecionada() {
        for (CartaView cartaView : cartasVisuais) {
            if (cartaView.isSelecionada()) {
                return cartaView.getCartaModel();
            }
        }
        return null;
    }
}