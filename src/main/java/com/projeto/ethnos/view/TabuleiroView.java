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
        List<Regiao> regioes = tabuleiroModel.getTodasRegioes();

        for (int i = 0; i < regioes.size(); i++) {
            Regiao regiao = regioes.get(i);
            VBox regiaoBox = new VBox(10);
            regiaoBox.setAlignment(Pos.CENTER);
            regiaoBox.setPrefSize(200, 190);

            String corFundo = UiPalette.getRegionBackground(regiao.getNome());
            regiaoBox.setStyle("-fx-background-color: " + corFundo
                + "; -fx-border-color: " + UiPalette.getWoodBorder()
                + "; -fx-border-width: 2px; -fx-background-radius: 6px; -fx-border-radius: 6px;");

            Label nomeLabel = new Label("Região: " + regiao.getNome());
            nomeLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2B1B12; -fx-font-family: 'Papyrus', 'Serif';");
            Label marcadoresLabel = new Label("Fichas: " + regiao.getMarcadores().size());
            marcadoresLabel.setStyle("-fx-text-fill: #3B2A1C; -fx-font-family: 'Papyrus', 'Serif';");
            Label pontuacaoLabel = new Label(montarTextoPontuacao(regiao));
            pontuacaoLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #3B2A1C;");

            VBox listaJogadores = new VBox(2);
            listaJogadores.setAlignment(Pos.CENTER);
            for (Jogador j : regiao.getMarcadores()) {
                Label nomeJogador = new Label("• " + j.getNome());
                nomeJogador.setStyle("-fx-text-fill: #2B1B12; -fx-font-family: 'Papyrus', 'Serif';");
                listaJogadores.getChildren().add(nomeJogador);
            }

            regiaoBox.getChildren().addAll(nomeLabel, pontuacaoLabel, marcadoresLabel, listaJogadores);
            this.add(regiaoBox, i % 3, i / 3);
        }
    }

    private String montarTextoPontuacao(Regiao regiao) {
        List<Integer> valores = regiao.getValoresPontuacao();
        if (valores.isEmpty()) return "Pontuacao: -";
        StringBuilder sb = new StringBuilder("Pontuacao: ");
        for (int i = valores.size() - 1; i >= 0; i--) {
            int pos = valores.size() - i;
            sb.append(pos).append("º=").append(valores.get(i));
            if (i > 0) sb.append("  ");
        }
        return sb.toString();
    }
}
