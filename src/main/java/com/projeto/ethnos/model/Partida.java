package com.projeto.ethnos.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

public class Partida {
    private static final int LIMITE_MAO = 10;
    private List<Jogador> jogadores;
    private Baralho baralho;
    private Mercado mercado;
    private Tabuleiro tabuleiro;
    private int eraAtual;
    private int dragoesRevelados;
    private int indiceJogadorAtual;
    private boolean jogoFinalizado;
    private List<Jogador> vencedores;
    private int rodadaAtual;
    private String ultimaAcao;
    private int turnoPowerRodada = -1;
    private Jogador turnoPowerJogador = null;

    public Partida(List<Jogador> jogadores, Baralho baralho, Mercado mercado, Tabuleiro tabuleiro) {
        this.jogadores = jogadores;
        this.baralho = baralho;
        this.mercado = mercado;
        this.tabuleiro = tabuleiro;
        this.eraAtual = 0;
        this.dragoesRevelados = 0;
        this.indiceJogadorAtual = 0;
        this.jogoFinalizado = false;
        this.vencedores = new ArrayList<>();
        this.rodadaAtual = 1;
        this.ultimaAcao = "Partida iniciada";
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

    private void finalizarEra() {
        // Essencial: a limpeza acontece NO FIM da era, não no início.
        // - descarta as cartas abertas do mercado
        // - descarta a mão de todos os jogadores
        // Observação: os marcadores no tabuleiro permanecem (representam presença/controle).
        baralho.descartarCartas(mercado.retirarTodas());
        for (Jogador jogador : jogadores) {
            baralho.descartarCartas(new ArrayList<>(jogador.mao));
            jogador.mao.clear();
        }
    }

    public List<Carta> revelarCartasIniciais() {
        List<Carta> reveladas = new ArrayList<>();
        while (mercado.getCartasDisponiveis().size() < 5 && !baralho.semCartasDisponiveis()) {
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
        if (jogador.mao.size() >= LIMITE_MAO) {
            // Regra essencial: no Ethnos a mão tem limite.
            throw new IllegalArgumentException("Limite de mão atingido (10 cartas). Jogue um bando antes de recrutar.");
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
        }

        // BUG corrigido: mesmo se não houver carta para comprar, o turno precisa terminar.
        // Caso contrário, a IA (ou o humano) fica "preso" e pode tentar comprar em loop.
        proximoJogador();
    }

    public void verificarFimDeEra() {
        if (jogoFinalizado) {
            return;
        }

        // BUG corrigido: o baralho "não acabou" se ainda existe descarte para reciclar.
        if (dragoesRevelados >= 3 || baralho.semCartasDisponiveis()) {
            finalizarEra();

            // Essencial: pontuação de fim de era por ranking de fichas em cada região.
            // Implementação: conta marcadores por jogador e aplica valoresPontuacao por posição.
            for (Regiao regiao : tabuleiro.getTodasRegioes()) {
                List<Jogador> ranking = regiao.rankingJogadoresPorMarcadores();
                for (int pos = 0; pos < ranking.size(); pos++) {
                    Jogador jogador = ranking.get(pos);
                    int pontos = regiao.getPontuacao(pos);
                    jogador.adicionarPontos(pontos);
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
        int maiorPontuacao = Integer.MIN_VALUE;
        vencedores.clear();

        for (Jogador jogador : jogadores) {
            if (jogador.getPontuacao() > maiorPontuacao) {
                maiorPontuacao = jogador.getPontuacao();
                vencedores.clear();
                vencedores.add(jogador);
            } else if (jogador.getPontuacao() == maiorPontuacao) {
                vencedores.add(jogador);
            }
        }

        if (vencedores.size() == 1) {
            System.out.println("Fim do jogo! Vencedor: " + vencedores.get(0).getNome());
        } else if (!vencedores.isEmpty()) {
            System.out.println("Fim do jogo com empate entre: " + getNomesVencedores());
        }
    }

    public int getDragoesRevelados() { return dragoesRevelados; }
    public int getEraAtual() { return eraAtual; }
    public int getRodadaAtual() { return rodadaAtual; }
    public String getUltimaAcao() { return ultimaAcao; }
    public boolean isJogoFinalizado() { return jogoFinalizado; }
    public Jogador getJogadorAtual() { return jogadores.get(indiceJogadorAtual); }
    public List<Jogador> getJogadores() { return jogadores; }
    public Baralho getBaralho() { return baralho; }
    public Mercado getMercado() { return mercado; }
    public List<Jogador> getVencedores() { return List.copyOf(vencedores); }
    public boolean isEmpateFinal() { return vencedores.size() > 1; }

    public String getNomesVencedores() {
        StringBuilder nomes = new StringBuilder();
        for (int i = 0; i < vencedores.size(); i++) {
            nomes.append(vencedores.get(i).getNome());
            if (i < vencedores.size() - 1) {
                nomes.append(", ");
            }
        }
        return nomes.toString();
    }
    
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

        // BUG corrigido: evitamos aplicar o poder mais de uma vez no mesmo turno do jogador.
        // (O poder já é aplicado automaticamente ao jogar o bando; o botão serve como "teste" manual.)
        if (turnoPowerJogador == jogador && turnoPowerRodada == rodadaAtual) {
            ultimaAcao = "Poder já foi usado neste turno (" + jogador.getNome() + ")";
            return;
        }

        // Comentário de bugfix: antes o método ativaPoder() não fazia nada no fluxo real.
        // Agora o poder do líder gera efeito concreto de partida.
        lider.ativaPoder();
        String tribo = lider.tribo == null ? "" : lider.tribo.toLowerCase();
        switch (tribo) {
            case "anão":
                jogador.adicionarPontos(1);
                ultimaAcao = "Poder (Anão): +" + 1 + " ponto para " + jogador.getNome();
                break;
            case "gigante":
                jogador.adicionarPontos(2);
                ultimaAcao = "Poder (Gigante): +" + 2 + " pontos para " + jogador.getNome();
                break;
            case "elfo":
                if (!bando.isEmpty()) {
                    jogador.mao.add(bando.get(0));
                }
                ultimaAcao = "Poder (Elfo): recupera 1 carta para a mão de " + jogador.getNome();
                break;
            case "dragão":
                dragoesRevelados = Math.max(0, dragoesRevelados - 1);
                ultimaAcao = "Poder (Dragão): reduz contador de dragões para " + dragoesRevelados;
                break;
            default:
                ultimaAcao = "Poder (" + lider.tribo + "): sem efeito implementado";
                break;
        }

        turnoPowerJogador = jogador;
        turnoPowerRodada = rodadaAtual;
    }

    public void jogarTurnoIA() {
        if (jogoFinalizado || jogadores.isEmpty()) {
            return;
        }
        Jogador ia = getJogadorAtual();
        if (!ia.isIa()) {
            return;
        }

        // Melhoria: a IA agora tenta formar o maior bando válido possível
        // antes de simplesmente baixar a primeira carta da mão.
        if (!ia.mao.isEmpty()) {
            JogadaIA jogada = escolherMelhorJogadaIA(ia);
            iniciarJogadaDoBando(ia, jogada.bando(), jogada.lider(), jogada.regiao());
        } else {
            comprarAliado(ia, escolherCartaMercadoParaIA(ia));
        }
    }

    private JogadaIA escolherMelhorJogadaIA(Jogador ia) {
        Carta melhorLider = ia.mao.get(0);
        List<Carta> melhorBando = List.of(melhorLider);

        for (Carta candidata : ia.mao) {
            List<Carta> bandoAtual = new ArrayList<>();
            for (Carta carta : ia.mao) {
                boolean combinaCor = carta.cor.equalsIgnoreCase(candidata.cor);
                boolean combinaTribo = carta.tribo.equalsIgnoreCase(candidata.tribo);
                if (combinaCor || combinaTribo) {
                    bandoAtual.add(carta);
                }
            }

            if (bandoAtual.size() > melhorBando.size()) {
                melhorLider = candidata;
                melhorBando = bandoAtual;
            }
        }

        Regiao destino = escolherMelhorRegiaoParaIA(melhorLider);
        return new JogadaIA(melhorLider, melhorBando, destino);
    }

    private Regiao escolherMelhorRegiaoParaIA(Carta lider) {
        Regiao regiaoDaCor = tabuleiro.getRegiao(lider.cor);
        if (regiaoDaCor != null) {
            return regiaoDaCor;
        }

        // Caso a cor da carta não tenha correspondência direta, escolhemos a região
        // com maior pontuação base disponível.
        return tabuleiro.getTodasRegioes().stream()
            .max(Comparator.comparingInt(regiao -> regiao.getPontuacao(0)))
            .orElse(tabuleiro.getTodasRegioes().get(0));
    }

    private Carta escolherCartaMercadoParaIA(Jogador ia) {
        for (Carta cartaMercado : mercado.getCartasDisponiveis()) {
            for (Carta cartaMao : ia.mao) {
                boolean combinaCor = cartaMercado.cor.equalsIgnoreCase(cartaMao.cor);
                boolean combinaTribo = cartaMercado.tribo.equalsIgnoreCase(cartaMao.tribo);
                if (combinaCor || combinaTribo) {
                    return cartaMercado;
                }
            }
        }
        return null;
    }

    private record JogadaIA(Carta lider, List<Carta> bando, Regiao regiao) {
    }

    private void proximoJogador() {
        if (!jogadores.isEmpty()) {
            int anterior = indiceJogadorAtual;
            indiceJogadorAtual = (indiceJogadorAtual + 1) % jogadores.size();
            // Incrementa rodada quando volta ao primeiro jogador (ciclo completo).
            if (anterior == jogadores.size() - 1 && indiceJogadorAtual == 0) {
                rodadaAtual++;
            }
        }
    }

    public void distribuirFichas() {
        for (Jogador jogador : jogadores) {
            jogador.distribuirFicha(null);
        }
    }
}