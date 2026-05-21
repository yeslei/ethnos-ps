package com.projeto.ethnos.view;

import com.projeto.ethnos.model.Jogador;
import com.projeto.ethnos.model.Partida;
import com.projeto.ethnos.observer.Assinante;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * Painel lateral com status do jogo (era, rodada, turno, dragões, pontos).
 * Implementa Assinante para reagir a qualquer mudança.
 */
public class StatusView extends VBox implements Assinante {

    private Partida partidaModel;

    public StatusView(Partida partidaModel) {
        this.partidaModel = partidaModel;
        this.setPadding(new Insets(20));
        this.setSpacing(10);
        this.setPrefWidth(240);
        this.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #cccccc;");
        redesenhar();
    }

    @Override
    public void atualiza(Partida p) {
        this.partidaModel = p;
        redesenhar();
    }

    private void redesenhar() {
        this.getChildren().clear();

        Label titulo = new Label("STATUS DO JOGO");
        titulo.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        Label lblEra = new Label("Era: " + partidaModel.getEraAtual() + " / 3");
        Label lblRodada = new Label("Rodada: " + partidaModel.getRodadaAtual());
        Label lblTurno = new Label("Vez: " + partidaModel.getJogadorAtual().getNome());
        Label lblUltimaAcao = new Label("Última ação: " + partidaModel.getUltimaAcao());
        lblUltimaAcao.setWrapText(true);
        Label lblDica = new Label("Dica: clique na carta para selecionar, escolha o líder e a região.");
        lblDica.setWrapText(true);
        lblDica.setTextFill(Color.DARKSLATEGRAY);

        int dragoes = partidaModel.getDragoesRevelados();
        Label lblDragoes = new Label("Dragões: " + dragoes + " / 3");
        ProgressBar barraProgresso = new ProgressBar(dragoes / 3.0);
        barraProgresso.setPrefWidth(200);
        if (dragoes >= 2) barraProgresso.setStyle("-fx-accent: red;");

        this.getChildren().addAll(
            titulo, lblEra, lblRodada, lblTurno, lblUltimaAcao, lblDica,
            new Separator(), lblDragoes, barraProgresso
        );

        Label lblJogadores = new Label("Jogadores:");
        lblJogadores.setStyle("-fx-font-weight: bold;");
        this.getChildren().add(lblJogadores);

        for (Jogador j : partidaModel.getJogadores()) {
            VBox infoJogador = new VBox(2);
            Label nome = new Label("• " + j.getNome());
            if (j == partidaModel.getJogadorAtual()) {
                nome.setStyle("-fx-font-weight: bold; -fx-text-fill: #1E88E5;");
            }
            Label tipo = new Label("  Tipo: " + (j.isIa() ? "IA" : "Humano"));
            Label pontos = new Label("  Pontos: " + j.getPontuacao());
            Label cartas = new Label("  Mão: " + j.getMao().size() + " cartas");
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
