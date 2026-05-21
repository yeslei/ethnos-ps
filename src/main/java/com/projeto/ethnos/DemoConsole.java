package com.projeto.ethnos;

import com.projeto.ethnos.factory.CartaFactory;
import com.projeto.ethnos.model.Baralho;
import com.projeto.ethnos.model.Carta;
import com.projeto.ethnos.model.Jogador;
import com.projeto.ethnos.model.Mercado;
import com.projeto.ethnos.model.Partida;
import com.projeto.ethnos.model.Regiao;
import com.projeto.ethnos.model.Tabuleiro;
import com.projeto.ethnos.observer.Assinante;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Demonstração do jogo em modo console.
 *
 * Esta classe existe por dois motivos:
 *
 *  1. Provar GRASP Low Coupling: a Partida pode rodar inteiramente sem
 *     JavaFX. Trocamos a "View" por um Assinante que apenas imprime no
 *     console e nada mais muda no modelo.
 *
 *  2. Ter um "Plan B" para a apresentação: se a máquina do professor
 *     não tiver JavaFX configurado, esta demo roda em qualquer JDK.
 *
 * Executar com:
 *     mvn compile exec:java -Dexec.mainClass=com.projeto.ethnos.DemoConsole
 * Ou via IDE diretamente.
 */
public class DemoConsole {

    public static void main(String[] args) {
        // Garante saída em UTF-8 mesmo em terminais Windows / locale "C".
        try {
            System.setOut(new java.io.PrintStream(System.out, true, "UTF-8"));
        } catch (java.io.UnsupportedEncodingException ignored) { /* nada a fazer */ }
        System.out.println("================================================");
        System.out.println("   ETHNOS - Demo de Console (Projeto UFF)");
        System.out.println("================================================\n");

        // ---- Setup do modelo (idêntico ao do EthnosApp) ----
        List<Regiao> regioes = Arrays.asList(
            new Regiao("Vermelho", Arrays.asList(8, 5, 2)),
            new Regiao("Verde",    Arrays.asList(8, 5, 2)),
            new Regiao("Cinza",    Arrays.asList(8, 5, 2))
        );
        Tabuleiro tabuleiro = new Tabuleiro(regioes);
        Baralho baralho = CartaFactory.montarBaralhoPadrao();
        Mercado mercado = new Mercado();

        List<Jogador> jogadores = new ArrayList<>(Arrays.asList(
            new Jogador("Alice"),
            new Jogador("Bob (IA)", true)
        ));

        Partida partida = new Partida(jogadores, baralho, mercado, tabuleiro);

        // ---- Observer: assinante de console em vez de JavaFX ----
        partida.adicionaAssinante(new AssinanteConsole());

        // ---- Inicia o jogo ----
        partida.distribuirFichas();
        partida.distribuirMaosIniciais();
        partida.iniciarEra();

        // Mostra mãos iniciais
        for (Jogador j : partida.getJogadores()) {
            System.out.println(j.getNome() + " começa com: " + j.getMao());
        }
        System.out.println();

        // ---- Alguns turnos automáticos ----
        // Alice (humano simulado) joga sempre o maior bando possível;
        // depois passa para a IA, que joga sozinha.
        int turnoMaximo = 30;
        for (int turno = 1; turno <= turnoMaximo && !partida.isJogoFinalizado(); turno++) {
            Jogador atual = partida.getJogadorAtual();
            System.out.println(">> Turno " + turno + " - " + atual.getNome());

            if (atual.isIa()) {
                partida.jogarTurnoIA();
            } else {
                jogadaAutomaticaAlice(partida, atual);
            }
            System.out.println();
        }

        // ---- Resultado final ----
        System.out.println("------------------------------------------------");
        System.out.println("RESULTADO FINAL");
        System.out.println("------------------------------------------------");
        for (Jogador j : partida.getJogadores()) {
            System.out.println("  " + j.getNome() + ": " + j.getPontuacao() + " pontos");
        }
        if (partida.isJogoFinalizado()) {
            System.out.println("Vencedor: " + partida.getNomesVencedores());
        } else {
            System.out.println("(Limite de turnos da demo atingido)");
        }
    }

    /**
     * Heurística simples: Alice escolhe o maior bando que consegue formar
     * com base em alguma carta da mão e uma região onde a regra do
     * N-ésimo marcador permita plantar a ficha.
     */
    private static void jogadaAutomaticaAlice(Partida partida, Jogador alice) {
        if (alice.getMao().isEmpty()) {
            partida.comprarAliado(alice, null);
            return;
        }
        Carta melhorLider = alice.getMao().get(0);
        List<Carta> melhorBando = new ArrayList<>(List.of(melhorLider));

        for (Carta cand : alice.getMao()) {
            List<Carta> bando = new ArrayList<>();
            for (Carta c : alice.getMao()) {
                if (c.getCor().equalsIgnoreCase(cand.getCor())
                    || c.getTribo().equalsIgnoreCase(cand.getTribo())) {
                    bando.add(c);
                }
            }
            if (bando.size() > melhorBando.size()) {
                melhorLider = cand;
                melhorBando = bando;
            }
        }

        // Procurar uma região onde a regra do N-ésimo marcador permita.
        Regiao regiaoEscolhida = null;
        Regiao preferida = partida.getTabuleiro().getRegiao(melhorLider.getCor());
        if (preferida != null
            && partida.contarMarcadoresDoJogadorNaRegiao(alice, preferida) < melhorBando.size()) {
            regiaoEscolhida = preferida;
        } else {
            for (Regiao r : partida.getTabuleiro().getTodasRegioes()) {
                if (partida.contarMarcadoresDoJogadorNaRegiao(alice, r) < melhorBando.size()) {
                    regiaoEscolhida = r;
                    break;
                }
            }
        }

        if (regiaoEscolhida == null) {
            // Bando muito pequeno pra qualquer região. Recruta em vez disso.
            partida.comprarAliado(alice, null);
            return;
        }

        partida.iniciarJogadaDoBando(alice, melhorBando, melhorLider, regiaoEscolhida);
    }

    /**
     * Assinante de console — a "View" desta demo.
     * Mostra que para integrar uma nova UI, basta implementar Assinante.
     */
    private static class AssinanteConsole implements Assinante {
        @Override
        public void atualiza(Partida p) {
            System.out.println("  [Observer] " + p.getUltimaAcao()
                + " | Era=" + p.getEraAtual()
                + " Rodada=" + p.getRodadaAtual()
                + " Dragões=" + p.getDragoesRevelados() + "/3");
        }
    }
}
