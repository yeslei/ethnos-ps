package com.projeto.ethnos.view;

import com.projeto.ethnos.model.Jogador;
import com.projeto.ethnos.model.Partida;
import com.projeto.ethnos.model.Regiao;
import com.projeto.ethnos.model.Tabuleiro;
import com.projeto.ethnos.observer.Assinante;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * View do tabuleiro (regiões). Implementa Assinante para reagir ao
 * notifica() da Partida (novas fichas, nova era etc.).
 */
public class TabuleiroView extends GridPane implements Assinante {

    private final Tabuleiro tabuleiroModel;
    private Regiao regiaoSelecionada;

    public TabuleiroView(Tabuleiro tabuleiroModel) {
        this.tabuleiroModel = tabuleiroModel;
        this.setAlignment(Pos.CENTER);
        this.setHgap(20);
        this.setVgap(20);
        this.setPadding(new Insets(20));
        redesenhar();
    }

    @Override
    public void atualiza(Partida p) {
        redesenhar();
    }

    private void redesenhar() {
        this.getChildren().clear();
        String[] coresBase = {"#FFCCCC", "#CCFFCC", "#FFFFCC", "#CCFFFF", "#CCCCFF", "#DDDDDD"};
        List<Regiao> regioes = tabuleiroModel.getTodasRegioes();

        for (int i = 0; i < regioes.size(); i++) {
            Regiao regiao = regioes.get(i);
            VBox regiaoBox = new VBox(10);
            regiaoBox.setAlignment(Pos.CENTER);
            regiaoBox.setPrefSize(180, 180);

            String corFundo = i < coresBase.length ? coresBase[i] : "#FFFFFF";
            String borda = regiao == regiaoSelecionada ? "#1E88E5" : "#333333";
            String espessura = regiao == regiaoSelecionada ? "4px" : "2px";
            regiaoBox.setStyle("-fx-background-color: " + corFundo
                + "; -fx-border-color: " + borda + "; -fx-border-width: " + espessura + ";");

            Label nomeLabel = new Label("Região: " + regiao.getNome());
            Label marcadoresLabel = new Label("Fichas: " + regiao.getMarcadores().size());
            Label selecionadaLabel = new Label(regiao == regiaoSelecionada ? "Selecionada" : "");

            VBox listaJogadores = new VBox(2);
            listaJogadores.setAlignment(Pos.CENTER);
            for (Jogador j : regiao.getMarcadores()) {
                listaJogadores.getChildren().add(new Label("• " + j.getNome()));
            }

            regiaoBox.getChildren().addAll(nomeLabel, marcadoresLabel, selecionadaLabel, listaJogadores);
            regiaoBox.setOnMouseClicked(event -> selecionarRegiao(regiao));
            this.add(regiaoBox, i % 3, i / 3);
        }
    }

    private void selecionarRegiao(Regiao regiao) {
        this.regiaoSelecionada = regiao;
        redesenhar();
    }

    public Regiao getRegiaoSelecionada() {
        return regiaoSelecionada;
    }
}
