package com.projeto.ethnos;

import com.projeto.ethnos.controller.JogoController;
import com.projeto.ethnos.model.*;
import com.projeto.ethnos.view.MercadoView;
import com.projeto.ethnos.view.TabuleiroView;
import com.projeto.ethnos.view.MaoView;
import com.projeto.ethnos.view.StatusView;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class EthnosApp extends Application {

    private Partida partida; 
    private Mercado mercado; 
    private Tabuleiro tabuleiro; 
    private Jogador jogadorPrincipal;
    private boolean modoContraIa;
    private int quantidadeJogadores;

    @Override
    public void start(Stage primaryStage) {
        configurarParametrosDaPartida();
        // Inicializa toda a lógica de negócio antes de carregar a interface
        inicializarModel();

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // 1. TOPO: MercadoView - Gerencia as cartas abertas e o deck de compra
        MercadoView mercadoView = new MercadoView(this.mercado);
        root.setTop(mercadoView);

        // 2. CENTRO: TabuleiroView - Exibe as regiões e marcadores de controle
        TabuleiroView tabuleiroView = new TabuleiroView(this.tabuleiro);
        root.setCenter(tabuleiroView);

        // 3. BASE: MaoView - Exibe as cartas do jogador e o botão de ação principal
        MaoView maoView = new MaoView(this.jogadorPrincipal);
        root.setBottom(maoView);

        // 4. DIREITA: StatusView - Painel lateral com dragões e pontos
        StatusView statusView = new StatusView(this.partida);
        root.setRight(statusView);

        // 5. CONTROLLER: O "cérebro" que conecta as Views aos Models
        // Ele escuta cliques na MaoView e MercadoView e executa na Partida
        JogoController controller = new JogoController(
            this.partida, 
            this.jogadorPrincipal, 
            mercadoView, 
            maoView, 
            tabuleiroView, 
            statusView
        );

        // Configuração da Janela (Resolução padrão para projetos de Software)
        Scene scene = new Scene(root, 1024, 768);
        primaryStage.setTitle("Ethnos - Sistemas de Informação UFF");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void configurarParametrosDaPartida() {
        ChoiceDialog<String> dialogModo = new ChoiceDialog<>("Contra IA", List.of("Contra IA", "Local (turnos)"));
        dialogModo.setTitle("Configuração da Partida");
        dialogModo.setHeaderText("Selecione o modo de jogo");
        dialogModo.setContentText("Modo:");
        Optional<String> modoEscolhido = dialogModo.showAndWait();
        this.modoContraIa = !modoEscolhido.isPresent() || "Contra IA".equals(modoEscolhido.get());

        TextInputDialog dialogQtd = new TextInputDialog("2");
        dialogQtd.setTitle("Configuração da Partida");
        dialogQtd.setHeaderText("Quantidade de jogadores");
        dialogQtd.setContentText("Jogadores (2 a 6):");
        Optional<String> qtdEscolhida = dialogQtd.showAndWait();
        int qtd = 2;
        if (qtdEscolhida.isPresent()) {
            try {
                qtd = Integer.parseInt(qtdEscolhida.get().trim());
            } catch (NumberFormatException ignored) {
                qtd = 2;
            }
        }
        this.quantidadeJogadores = Math.max(2, Math.min(6, qtd));
    }

    private void inicializarModel() {
        // Criando as regiões mapeadas por cores (necessário para a lógica do Líder)
        List<Regiao> regioes = Arrays.asList(
            new Regiao("Vermelho", Arrays.asList(0, 0, 0)), 
            new Regiao("Verde", Arrays.asList(0, 0, 0)),
            new Regiao("Amarelo", Arrays.asList(0, 0, 0)),
            new Regiao("Azul", Arrays.asList(0, 0, 0)),
            new Regiao("Roxo", Arrays.asList(0, 0, 0)),
            new Regiao("Cinza", Arrays.asList(0, 0, 0))
        );

        this.tabuleiro = new Tabuleiro(regioes);
        
        // Setup do Baralho com cartas fixas.
        // Bug corrigido: removemos dependência de aleatoriedade no setup inicial do mercado.
        Baralho baralho = new Baralho();
        baralho.adicionarCarta(new Carta("Anão", "Vermelho", "Anão"));
        baralho.adicionarCarta(new Carta("Elfo", "Verde", "Elfo"));
        baralho.adicionarCarta(new Carta("Minotauro", "Vermelho", "Minotauro"));
        baralho.adicionarCarta(new Carta("Centauro", "Amarelo", "Centauro"));
        baralho.adicionarCarta(new Carta("Esqueleto", "Azul", "Esqueleto"));
        baralho.adicionarCarta(new Carta("Gigante", "Roxo", "Gigante"));
        baralho.adicionarCarta(new Carta("Mago", "Cinza", "Mago"));
        baralho.adicionarCarta(new Carta("Dragão", "Cinza", "Dragão"));
        baralho.adicionarCarta(new Carta("Dragão", "Vermelho", "Dragão"));
        baralho.adicionarCarta(new Carta("Dragão", "Verde", "Dragão"));
        baralho.setupInicial(false); 

        this.mercado = new Mercado(); 
        
        // Mercado inicial fixo e previsível (sem sorteio aleatório)
        this.mercado.adicionarCartas(Arrays.asList(
            new Carta("Centauro", "Amarelo", "Centauro"),
            new Carta("Minotauro", "Vermelho", "Minotauro"),
            new Carta("Elfo", "Verde", "Elfo"),
            new Carta("Anão", "Vermelho", "Anão"),
            new Carta("Mago", "Cinza", "Mago")
        ));

        // Configuração inicial dos Jogadores
        List<Jogador> jogadores = new ArrayList<>();
        this.jogadorPrincipal = new Jogador("Você");
        jogadores.add(this.jogadorPrincipal);
        for (int i = 2; i <= this.quantidadeJogadores; i++) {
            boolean ehIa = this.modoContraIa && i > 1;
            String nome = ehIa ? "IA " + (i - 1) : "Jogador " + i;
            jogadores.add(new Jogador(nome, ehIa));
        }

        // Estado inicial da mão do jogador
        this.jogadorPrincipal.mao.addAll(Arrays.asList(
            new Carta("Anão", "Vermelho", "Anão"),
            new Carta("Troll", "Cinza", "Troll")
        ));

        // Instancia a partida que gerencia o fluxo global
        partida = new Partida(jogadores, baralho, mercado, tabuleiro);
        partida.distribuirFichas(); 
        partida.iniciarEra();
    }

    public static void main(String[] args) {
        launch(args);
    }
}