package com.projeto.ethnos.view;

import com.projeto.ethnos.model.Carta;
import com.projeto.ethnos.model.Jogador;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class MaoView extends VBox {

    private Jogador jogadorModel;
    private Button btnJogar;
    private Button btnAtivarPoder;
    private ComboBox<Carta> comboLider;
    
    // Novo: Lista para rastrearmos as cartas desenhadas na tela
    private List<CartaView> cartasVisuaisExibidas = new ArrayList<>(); 

    public MaoView(Jogador jogadorModel) {
        this.jogadorModel = jogadorModel;
        this.btnJogar = new Button("Jogar Bando");
        this.btnAtivarPoder = new Button("Ativar Poder do Líder");
        this.comboLider = new ComboBox<>();
        this.btnJogar.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        this.btnAtivarPoder.setStyle("-fx-background-color: #7E57C2; -fx-text-fill: white; -fx-font-weight: bold;");
        
        this.setAlignment(Pos.CENTER);
        this.setPadding(new Insets(10));
        this.setSpacing(10);
        
        atualizarVisualizacao();
    }

    public void atualizarVisualizacao() {
        this.getChildren().clear();
        this.cartasVisuaisExibidas.clear(); // Limpamos a lista antiga

        Label lblMao = new Label("Mão de " + jogadorModel.getNome() + ":");
        lblMao.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        HBox cartasBox = new HBox(10);
        cartasBox.setAlignment(Pos.CENTER);

        for (Carta c : jogadorModel.mao) {
            CartaView visualCarta = new CartaView(c);
            cartasBox.getChildren().add(visualCarta);
            
            // Guardamos a referência da carta desenhada
            this.cartasVisuaisExibidas.add(visualCarta); 
        }

        this.comboLider.getItems().setAll(jogadorModel.mao);
        if (!this.comboLider.getItems().isEmpty()) {
            this.comboLider.setValue(this.comboLider.getItems().get(0));
        }
        this.comboLider.setPromptText("Selecione o líder");

        HBox acoes = new HBox(10, this.btnJogar, this.btnAtivarPoder);
        acoes.setAlignment(Pos.CENTER);

        this.getChildren().addAll(lblMao, cartasBox, this.comboLider, acoes);
    }

    public Button getBtnJogar() { return this.btnJogar; }
    public Button getBtnAtivarPoder() { return this.btnAtivarPoder; }
    public Carta getLiderSelecionado() { return this.comboLider.getValue(); }
    public void setJogadorModel(Jogador jogadorModel) { this.jogadorModel = jogadorModel; }

    // NOVO MÉTODO: Varre a tela e devolve pro Controller só as cartas reais que foram clicadas
    public List<Carta> getCartasSelecionadas() {
        List<Carta> selecionadas = new ArrayList<>();
        for (CartaView cv : cartasVisuaisExibidas) {
            if (cv.isSelecionada()) {
                selecionadas.add(cv.getCartaModel());
            }
        }
        return selecionadas;
    }
}