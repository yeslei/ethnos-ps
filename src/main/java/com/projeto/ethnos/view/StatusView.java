package com.projeto.ethnos.view;

import com.projeto.ethnos.model.Jogador;
import com.projeto.ethnos.model.Partida;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class StatusView extends VBox {

    private Partida partidaModel;

    public StatusView(Partida partidaModel) {
        this.partidaModel = partidaModel;
        this.setPadding(new Insets(20));
        this.setSpacing(15);
        this.setPrefWidth(220);
        this.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #cccccc;");
        atualizarVisualizacao();
    }

    public void atualizarVisualizacao() {
        this.getChildren().clear();

        Label titulo = new Label("STATUS DO JOGO");
        titulo.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        Label lblEra = new Label("Era: " + partidaModel.getEraAtual());
        Label lblTurno = new Label("Vez: " + partidaModel.getJogadorAtual().getNome());
        Label lblDica = new Label("Dica: escolha região no tabuleiro e líder na mão.");
        lblDica.setTextFill(Color.DARKSLATEGRAY);

        // Contador de Dragões
        int dragoes = partidaModel.getDragoesRevelados();
        Label lblDragoes = new Label("Dragões: " + dragoes + " / 3");
        
        ProgressBar barraProgresso = new ProgressBar(dragoes / 3.0);
        barraProgresso.setPrefWidth(180);
        if (dragoes >= 2) barraProgresso.setStyle("-fx-accent: red;");

        this.getChildren().addAll(titulo, lblEra, lblTurno, lblDica, new Separator(), lblDragoes, barraProgresso);

        // Lista de Jogadores e Pontos
        Label lblJogadores = new Label("Jogadores:");
        lblJogadores.setStyle("-fx-font-weight: bold;");
        this.getChildren().add(lblJogadores);

        for (Jogador j : partidaModel.getJogadores()) {
            VBox infoJogador = new VBox(2);
            Label nome = new Label("• " + j.getNome());
            Label tipo = new Label("  Tipo: " + (j.isIa() ? "IA" : "Humano"));
            Label pontos = new Label("  Pontos: " + j.getPontuacao());
            Label cartas = new Label("  Cartas na mão: " + j.mao.size());
            tipo.setTextFill(Color.GRAY);
            pontos.setTextFill(Color.DARKBLUE);
            cartas.setTextFill(Color.GRAY);
            infoJogador.getChildren().addAll(nome, tipo, pontos, cartas);
            this.getChildren().add(infoJogador);
        }

        if (partidaModel.isJogoFinalizado()) {
            Label encerrado = new Label("Jogo encerrado");
            encerrado.setStyle("-fx-text-fill: #B71C1C; -fx-font-weight: bold;");
            Label resultadoFinal;
            if (partidaModel.isEmpateFinal()) {
                resultadoFinal = new Label("Empate: " + partidaModel.getNomesVencedores());
            } else {
                resultadoFinal = new Label("Vencedor: " + partidaModel.getNomesVencedores());
            }
            resultadoFinal.setStyle("-fx-font-weight: bold;");
            this.getChildren().addAll(new Separator(), encerrado, resultadoFinal);
        }
    }
}