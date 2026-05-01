package com.projeto.ethnos.model;

import java.util.List;
import java.util.ArrayList;

public class Partida {
    private List<Jogador> jogadores;
    private Baralho baralho;
    private Mercado mercado;
    private Tabuleiro tabuleiro;
    private int eraAtual;
    private int dragoesRevelados;
    private int indiceJogadorAtual;
    private boolean jogoFinalizado;

    public Partida(List<Jogador> jogadores, Baralho baralho, Mercado mercado, Tabuleiro tabuleiro) {
        this.jogadores = jogadores;
        this.baralho = baralho;
        this.mercado = mercado;
        this.tabuleiro = tabuleiro;
        this.eraAtual = 0;
        this.dragoesRevelados = 0;
        this.indiceJogadorAtual = 0;
        this.jogoFinalizado = false;
    }

    public void registrarCompraDeCarta(Carta carta) {
        if (jogoFinalizado || carta == null) {
            return;
        }

        // BUG corrigido: antes a lógica não validava ciclo de era completo.
        // Agora toda revelação/compra passa por verificarFimDeEra().
        if (carta != null && "Dragão".equalsIgnoreCase(carta.tribo)) {
            dragoesRevelados++;
            System.out.println("Um dragão foi revelado! Total: " + dragoesRevelados);
        }
        verificarFimDeEra();
    }

    public void iniciarEra() {
        if (jogoFinalizado) {
            return;
        }

        eraAtual++;
        dragoesRevelados = 0;
        revelarCartasIniciais();
    }

    public List<Carta> revelarCartasIniciais() {
        List<Carta> reveladas = new ArrayList<>();
        while (mercado.getCartasDisponiveis().size() < 5 && !baralho.estaVazio()) {
            Carta topo = baralho.comprarDoTopo();
            if (topo == null) {
                break;
            }
            registrarCompraDeCarta(topo);
            if (!"Dragão".equalsIgnoreCase(topo.tribo)) {
                mercado.adicionarCartas(List.of(topo));
                reveladas.add(topo);
            }
            if (jogoFinalizado) {
                break;
            }
        }
        return reveladas;
    }

    public void comprarAliado(Jogador jogador, Carta cartaMercadoSelecionada) {
        if (jogoFinalizado || jogador == null) {
            return;
        }
        if (jogador != getJogadorAtual()) {
            throw new IllegalArgumentException("Não é o turno deste jogador.");
        }

        Carta cartaComprada;
        if (cartaMercadoSelecionada != null) {
            cartaComprada = mercado.comprarCarta(cartaMercadoSelecionada);
        } else {
            cartaComprada = baralho.comprarDoTopo();
        }

        if (cartaComprada != null) {
            jogador.mao.add(cartaComprada);
            registrarCompraDeCarta(cartaComprada);
            revelarCartasIniciais();
            proximoJogador();
        }
    }

    public void verificarFimDeEra() {
        if (jogoFinalizado) {
            return;
        }

        if (dragoesRevelados >= 3 || baralho.estaVazio()) {
            // Pontuação simplificada por presença/dominância em cada região
            for (Regiao regiao : tabuleiro.getTodasRegioes()) {
                for (Jogador jogador : regiao.getMarcadores()) {
                    jogador.adicionarPontos(regiao.getPontuacao(0));
                }
            }

            if (eraAtual >= 3) {
                finalizarJogo();
            } else {
                iniciarEra();
            }
        }
    }

    public void finalizarJogo() {
        this.jogoFinalizado = true;
        Jogador vencedor = null;
        for (Jogador jogador : jogadores) {
            if (vencedor == null || jogador.getPontuacao() > vencedor.getPontuacao()) {
                vencedor = jogador;
            }
        }
        if (vencedor != null) {
            System.out.println("Fim do jogo! Vencedor: " + vencedor.getNome());
        }
    }

    public int getDragoesRevelados() { return dragoesRevelados; }
    public int getEraAtual() { return eraAtual; }
    public boolean isJogoFinalizado() { return jogoFinalizado; }
    public Jogador getJogadorAtual() { return jogadores.get(indiceJogadorAtual); }
    public List<Jogador> getJogadores() { return jogadores; }
    public Baralho getBaralho() { return baralho; }
    public Mercado getMercado() { return mercado; }
    
    public void iniciarJogadaDoBando(Jogador j, List<Carta> bando, Carta lider, Regiao regiaoEscolhida) {
        if (jogoFinalizado || j == null || bando == null || bando.isEmpty() || lider == null) {
            return;
        }
        if (j != getJogadorAtual()) {
            throw new IllegalArgumentException("Não é o turno deste jogador.");
        }
        if (!bando.contains(lider)) {
            throw new IllegalArgumentException("O líder precisa estar dentro do bando selecionado.");
        }
        if (regiaoEscolhida == null) {
            throw new IllegalArgumentException("Selecione uma região para posicionar a ficha.");
        }

        boolean todasMesmoTipo = true;
        for (Carta carta : bando) {
            boolean combinaCor = carta.cor.equalsIgnoreCase(lider.cor);
            boolean combinaTribo = carta.tribo.equalsIgnoreCase(lider.tribo);
            if (!combinaCor && !combinaTribo) {
                todasMesmoTipo = false;
                break;
            }
        }
        if (!todasMesmoTipo) {
            throw new IllegalArgumentException("Todas as cartas do bando devem combinar em cor ou tribo com o líder.");
        }

        regiaoEscolhida.adicionarMarcador(j);
        j.adicionarPontos(regiaoEscolhida.getPontuacao(0));

        // Regra prática: o líder define a região "preferida" por cor.
        // Se jogador escolher outra região, mantemos a escolha manual para suportar UX pedida.
        Regiao regiaoDaCor = this.tabuleiro.getRegiao(lider.cor);
        if (regiaoDaCor == null) {
            System.out.println("Aviso: líder sem região de cor correspondente no tabuleiro.");
        }

        j.jogarBando(bando, lider);
        j.adicionarPontos(calcularPontosDoBando(lider, bando));

        // As sobras vão para o mercado e a mão é limpa ao final da jogada.
        this.mercado.adicionarCartas(j.mao);
        j.mao.clear();

        aplicarPoderDoLider(j, lider, bando, regiaoEscolhida);
        proximoJogador();
        revelarCartasIniciais();
    }

    private int calcularPontosDoBando(Carta lider, List<Carta> bando) {
        int base = bando.size();
        if ("Anão".equalsIgnoreCase(lider.tribo)) {
            // Poder passivo simples: anões valorizam banda numerosa.
            return base + 1;
        }
        return base;
    }

    public void aplicarPoderDoLider(Jogador jogador, Carta lider, List<Carta> bando, Regiao regiaoEscolhida) {
        if (lider == null || jogador == null) {
            return;
        }

        // Comentário de bugfix: antes o método ativaPoder() não fazia nada no fluxo real.
        // Agora o poder do líder gera efeito concreto de partida.
        lider.ativaPoder();
        String tribo = lider.tribo == null ? "" : lider.tribo.toLowerCase();
        switch (tribo) {
            case "anão":
                jogador.adicionarPontos(1);
                break;
            case "gigante":
                jogador.adicionarPontos(2);
                break;
            case "elfo":
                if (!bando.isEmpty()) {
                    jogador.mao.add(bando.get(0));
                }
                break;
            case "dragão":
                dragoesRevelados = Math.max(0, dragoesRevelados - 1);
                break;
            default:
                break;
        }
    }

    public void jogarTurnoIA() {
        if (jogoFinalizado || jogadores.isEmpty()) {
            return;
        }
        Jogador ia = getJogadorAtual();
        if (!ia.isIa()) {
            return;
        }

        // IA básica: tenta jogar um bando de 1 carta; se não conseguir, recruta do topo.
        if (!ia.mao.isEmpty()) {
            Carta lider = ia.mao.get(0);
            Regiao destino = tabuleiro.getRegiao(lider.cor);
            List<Carta> bando = List.of(lider);
            iniciarJogadaDoBando(ia, bando, lider, destino != null ? destino : tabuleiro.getTodasRegioes().get(0));
        } else {
            comprarAliado(ia, null);
        }
    }

    private void proximoJogador() {
        if (!jogadores.isEmpty()) {
            indiceJogadorAtual = (indiceJogadorAtual + 1) % jogadores.size();
        }
    }

    public void distribuirFichas() {
        for (Jogador jogador : jogadores) {
            jogador.distribuirFicha(null);
        }
    }
}