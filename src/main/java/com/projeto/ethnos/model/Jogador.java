package com.projeto.ethnos.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa um jogador da partida (humano ou IA).
 *
 * Padrões GRASP aplicados:
 *  - Creator: o método jogarBando() instancia Bando. Jogador é o criador
 *    apropriado porque (a) possui a mão de onde saem as cartas, (b) agrega
 *    Bandos (relação de composição no diagrama).
 *  - Information Expert: Jogador conhece seu próprio estado (pontuação,
 *    mão, fichas restantes) e é o responsável por mantê-lo coerente.
 *  - High Cohesion: Jogador só lida com responsabilidades de jogador; a
 *    lógica do turno fica em Partida (Controller).
 */
public class Jogador {

    private static final int FICHAS_INICIAIS = 15;

    private final String nome;
    private int pontuacao;
    private int fichasRestantes;
    private final boolean ia;

    /** Cartas que o jogador tem em mão (limite imposto por Partida). */
    private final List<Carta> mao = new ArrayList<>();

    /** Bandos já baixados pelo jogador na partida. */
    private final List<Bando> bandos = new ArrayList<>();

    /** Último líder utilizado — usado por poderes como o do Elfo. */
    private Carta ultimoLider;

    public Jogador(String nome) {
        this(nome, false);
    }

    public Jogador(String nome, boolean ia) {
        this.nome = nome;
        this.pontuacao = 0;
        this.fichasRestantes = FICHAS_INICIAIS;
        this.ia = ia;
    }

    /**
     * Operação do diagrama: distribuirFicha(lider).
     * Decrementa uma ficha do estoque do jogador (caso haja) e retorna 1
     * se conseguiu, 0 caso contrário.
     */
    public int distribuirFicha(Carta lider) {
        if (fichasRestantes > 0) {
            fichasRestantes--;
            return 1;
        }
        return 0;
    }

    /**
     * Operação do diagrama: recrutarAliado(baralho).
     * Compra uma carta do topo do baralho e adiciona à mão.
     * Não trata regras de fim de era (responsabilidade de Partida).
     */
    public void recrutarAliado(Baralho baralho) {
        Carta comprada = baralho.comprarDoTopo();
        if (comprada != null) {
            mao.add(comprada);
        }
    }

    /**
     * Operação do diagrama: jogarBando(cartas, lider).
     *
     * GRASP Creator: aqui Jogador instancia o Bando, conforme indicado no
     * diagrama pela operação <<create>>. As cartas usadas saem da mão.
     */
    public Bando jogarBando(List<Carta> cartasSelecionadas, Carta lider) {
        Bando novoBando = new Bando(cartasSelecionadas, lider);
        this.bandos.add(novoBando);
        this.mao.removeAll(cartasSelecionadas);
        this.ultimoLider = lider;
        return novoBando;
    }

    // --- Getters e ajustes pontuais ---

    public String getNome() {
        return nome;
    }

    public boolean isIa() {
        return ia;
    }

    public int getPontuacao() {
        return pontuacao;
    }

    public int getFichasRestantes() {
        return fichasRestantes;
    }

    public List<Carta> getMao() {
        // Devolvemos a lista real porque Partida precisa modificá-la
        // ao recrutar/devolver cartas. Encapsulamento parcial é uma decisão
        // de projeto MVP — Jogador continua sendo o único a "jogar bando".
        return mao;
    }

    public List<Bando> getBandos() {
        return List.copyOf(bandos);
    }

    public Carta getUltimoLider() {
        return ultimoLider;
    }

    public void adicionarPontos(int pontos) {
        this.pontuacao += Math.max(0, pontos);
    }

    public void removerPontos(int pontos) {
        this.pontuacao = Math.max(0, this.pontuacao - Math.max(0, pontos));
    }
}
