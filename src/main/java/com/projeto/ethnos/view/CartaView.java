package com.projeto.ethnos.view;

import com.projeto.ethnos.model.Carta;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.input.MouseEvent;

public class CartaView extends StackPane {
    
    private Carta cartaModel;
    private boolean selecionada = false; // Novo: guarda o estado do clique
    private Rectangle fundo; // Passou a ser atributo para podermos mudar a cor dele depois

    public CartaView(Carta cartaModel) {
        this.cartaModel = cartaModel;
        
        fundo = new Rectangle(80, 110);
        fundo.setFill(Color.WHITE);
        fundo.setStroke(Color.BLACK);
        fundo.setStrokeWidth(1);
        fundo.setArcWidth(10);
        fundo.setArcHeight(10);

        Rectangle faixaCor = new Rectangle(78, 25);
        faixaCor.setFill(obterCorVisual(cartaModel.cor));

        Label triboLabel = new Label(cartaModel.tribo);
        triboLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: black;");

        VBox conteudo = new VBox(5);
        conteudo.setAlignment(Pos.TOP_CENTER);
        conteudo.getChildren().addAll(faixaCor, triboLabel);

        this.getChildren().addAll(fundo, conteudo);
        
        // --- NOVOS EVENTOS DE MOUSE ---
        
        // Evento de Clique para Selecionar/Deselecionar
        // Usando addEventHandler para permitir que a View "pai" adicione lógica extra
        // (ex.: seleção única) sem sobrescrever este clique.
        this.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> toggleSelecao());

        // Ajuste no Hover para não apagar a seleção
        this.setOnMouseEntered(e -> { if (!selecionada) fundo.setStrokeWidth(3); });
        this.setOnMouseExited(e -> { if (!selecionada) fundo.setStrokeWidth(1); });
    }

    // Método que altera o visual quando clicado
    private void toggleSelecao() {
        setSelecionada(!selecionada);
    }

    private Color obterCorVisual(String corTexto) {
        if (corTexto == null) return Color.LIGHTGRAY;
        switch (corTexto.toLowerCase()) {
            case "vermelho": return Color.SALMON;
            case "verde": return Color.LIGHTGREEN;
            case "amarelo": return Color.KHAKI;
            case "azul": return Color.LIGHTBLUE;
            case "roxo": return Color.THISTLE;
            case "cinza": return Color.DARKGRAY;
            default: return Color.WHITE;
        }
    }
    
    public Carta getCartaModel() { return cartaModel; }
    
    // Novo getter para o Controller saber se ela está selecionada
    public boolean isSelecionada() { return selecionada; }

    public void setSelecionada(boolean selecionada) {
        this.selecionada = selecionada;
        if (selecionada) {
            fundo.setStroke(Color.DODGERBLUE);
            fundo.setStrokeWidth(4);
            this.setTranslateY(-15); // Efeito visual: carta sobe um pouco
        } else {
            fundo.setStroke(Color.BLACK);
            fundo.setStrokeWidth(1);
            this.setTranslateY(0); // Carta volta pra posição original
        }
    }
}