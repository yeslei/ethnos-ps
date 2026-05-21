package com.projeto.ethnos;

import com.projeto.ethnos.controller.JogoController;
import com.projeto.ethnos.factory.CartaFactory;
import com.projeto.ethnos.model.Baralho;
import com.projeto.ethnos.model.Jogador;
import com.projeto.ethnos.model.Mercado;
import com.projeto.ethnos.model.Partida;
import com.projeto.ethnos.model.Regiao;
import com.projeto.ethnos.model.Tabuleiro;
import com.projeto.ethnos.view.MaoView;
import com.projeto.ethnos.view.MercadoView;
import com.projeto.ethnos.view.StatusView;
import com.projeto.ethnos.view.TabuleiroView;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Ponto de entrada do jogo. Responsável por:
 *  1. Coletar parâmetros da partida (modo, qtd. jogadores, tribos).
 *  2. Instanciar o modelo (Baralho via Factory, Mercado, Tabuleiro,
 *     Jogadores, Partida).
 *  3. Instanciar as Views.
 *  4. Inscrever as Views como Assinantes da Partida (GoF Observer).
 *  5. Conectar o Controller MVC aos botões e exibir a janela.
 */
public class EthnosApp extends Application {

    private Partida partida;
    private Mercado mercado;
    private Tabuleiro tabuleiro;
    private Jogador jogadorPrincipal;
    private boolean modoContraIa;
    private int quantidadeJogadores;
    private List<String> tribosEscolhidas;

    @Override
    public void start(Stage primaryStage) {
        configurarParametrosDaPartida();
        if (!exibirTelaSetupDeTribos()) {
            // Usuário fechou a janela de setup; encerra.
            primaryStage.close();
            return;
        }
        inicializarModel();

        // Views
        MercadoView mercadoView = new MercadoView(this.mercado);
        TabuleiroView tabuleiroView = new TabuleiroView(this.tabuleiro);
        MaoView maoView = new MaoView(this.jogadorPrincipal);
        StatusView statusView = new StatusView(this.partida);

        // GoF Observer: cada View se inscreve como Assinante da Partida.
        partida.adicionaAssinante(mercadoView);
        partida.adicionaAssinante(tabuleiroView);
        partida.adicionaAssinante(maoView);
        partida.adicionaAssinante(statusView);

        // Layout principal
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setTop(mercadoView);
        root.setCenter(tabuleiroView);
        root.setBottom(maoView);
        root.setRight(statusView);

        new JogoController(this.partida, mercadoView, maoView, tabuleiroView, statusView);

        Scene scene = new Scene(root, 1100, 800);
        primaryStage.setTitle("Ethnos — Projeto de Software (UFF)");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void configurarParametrosDaPartida() {
        ChoiceDialog<String> dialogModo = new ChoiceDialog<>(
            "Contra IA", List.of("Contra IA", "Local (turnos)"));
        dialogModo.setTitle("Configuração da Partida");
        dialogModo.setHeaderText("Selecione o modo de jogo");
        dialogModo.setContentText("Modo:");
        Optional<String> modoEscolhido = dialogModo.showAndWait();
        this.modoContraIa = !modoEscolhido.isPresent()
            || "Contra IA".equals(modoEscolhido.get());

        TextInputDialog dialogQtd = new TextInputDialog("2");
        dialogQtd.setTitle("Configuração da Partida");
        dialogQtd.setHeaderText("Quantidade de jogadores");
        dialogQtd.setContentText("Jogadores (2 a 6):");
        Optional<String> qtdEscolhida = dialogQtd.showAndWait();
        int qtd = 2;
        if (qtdEscolhida.isPresent()) {
            try {
                qtd = Integer.parseInt(qtdEscolhida.get().trim());
            } catch (NumberFormatException ignored) { /* mantém 2 */ }
        }
        this.quantidadeJogadores = Math.max(2, Math.min(6, qtd));
    }

    /**
     * Tela inicial de seleção de tribos (regra original do Ethnos:
     * o jogador escolhe quais tribos vão participar antes do jogo começar).
     *
     * Retorna true se o usuário confirmou a seleção, false se cancelou.
     */
    private boolean exibirTelaSetupDeTribos() {
        Stage setupStage = new Stage();
        setupStage.setTitle("Setup - Escolha das Tribos");

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        Label titulo = new Label("Escolha quais tribos vão participar da partida");
        titulo.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        Label instrucao = new Label("Recomendado: escolha entre 4 e 6 tribos para uma partida balanceada");
        instrucao.setStyle("-fx-text-fill: #666666;");

        VBox checkBoxes = new VBox(8);
        checkBoxes.setAlignment(Pos.CENTER_LEFT);
        checkBoxes.setPadding(new Insets(10, 50, 10, 50));

        List<CheckBox> caixas = new ArrayList<>();
        // Pré-marca todas para o caso de o usuário só querer clicar em "Iniciar".
        for (String tribo : CartaFactory.TRIBOS_DISPONIVEIS) {
            CheckBox cb = new CheckBox(tribo);
            cb.setSelected(true);
            cb.setStyle("-fx-font-size: 14px;");
            caixas.add(cb);
            checkBoxes.getChildren().add(cb);
        }

        Button btnIniciar = new Button("Iniciar Partida");
        btnIniciar.setStyle(
            "-fx-background-color: #4CAF50; -fx-text-fill: white; "
            + "-fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 8 20 8 20;");

        final boolean[] confirmado = { false };

        btnIniciar.setOnAction(e -> {
            List<String> selecionadas = new ArrayList<>();
            for (CheckBox cb : caixas) {
                if (cb.isSelected()) selecionadas.add(cb.getText());
            }
            if (selecionadas.size() < 3) {
                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle("Seleção insuficiente");
                alert.setHeaderText(null);
                alert.setContentText("Escolha pelo menos 3 tribos para a partida.");
                alert.showAndWait();
                return;
            }
            this.tribosEscolhidas = selecionadas;
            confirmado[0] = true;
            setupStage.close();
        });

        root.getChildren().addAll(titulo, instrucao, checkBoxes, btnIniciar);

        Scene scene = new Scene(root, 400, 480);
        setupStage.setScene(scene);
        setupStage.showAndWait();

        return confirmado[0];
    }

    private void inicializarModel() {
        // Tabuleiro com 6 regiões. Valores [1, 3, 6] correspondem às fichas
        // de pontuação reveladas a cada era no jogo original:
        //   Era 1 -> 1ª colocação ganha 1 ponto
        //   Era 2 -> 1ª ganha 3, 2ª ganha 1
        //   Era 3 -> 1ª ganha 6, 2ª ganha 3, 3ª ganha 1
        List<Regiao> regioes = Arrays.asList(
            new Regiao("Vermelho", Arrays.asList(1, 3, 6)),
            new Regiao("Verde",    Arrays.asList(1, 3, 6)),
            new Regiao("Amarelo",  Arrays.asList(1, 3, 6)),
            new Regiao("Azul",     Arrays.asList(1, 3, 6)),
            new Regiao("Roxo",     Arrays.asList(1, 3, 6)),
            new Regiao("Cinza",    Arrays.asList(1, 3, 6))
        );
        this.tabuleiro = new Tabuleiro(regioes);

        // GoF Factory Method: baralho montado pela CartaFactory com as
        // tribos escolhidas pelo jogador no setup.
        Baralho baralho = CartaFactory.montarBaralhoComTribos(this.tribosEscolhidas);

        // Mercado iniciado vazio; será preenchido por revelarCartasRaca().
        this.mercado = new Mercado();

        // Jogadores
        List<Jogador> jogadores = new ArrayList<>();
        this.jogadorPrincipal = new Jogador("Você");
        jogadores.add(this.jogadorPrincipal);
        for (int i = 2; i <= this.quantidadeJogadores; i++) {
            boolean ehIa = this.modoContraIa;
            String nome = ehIa ? "IA " + (i - 1) : "Jogador " + i;
            jogadores.add(new Jogador(nome, ehIa));
        }

        // Partida — GRASP Controller / GoF Observer (Subject).
        partida = new Partida(jogadores, baralho, mercado, tabuleiro);
        partida.distribuirFichas();
        partida.distribuirMaosIniciais();
        partida.iniciarEra();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
