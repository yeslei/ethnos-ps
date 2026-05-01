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
    private boolean setupInicialAtivo;

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
        this.setupInicialAtivo = true;
    }

    public void registrarCompraDeCarta(Carta carta) {
        if (jogoFinalizado || carta == null) {
            return;
        }

        // BUG corrigido: não verificamos fim de era aqui.
        // Motivo: o setup inicial (revelar mercado) também compra do topo e pode revelar Dragões.
        // O fim de era deve ser verificado após uma ação real de turno (comprar/jogar bando),
        // evitando começar o jogo já pulando eras automaticamente.
        if (carta != null && "Dragão".equalsIgnoreCase(carta.tribo)) {
            dragoesRevelados++;
            System.out.println("Um dragão foi revelado! Total: " + dragoesRevelados);
        }
    }

    public void iniciarEra() {
        if (jogoFinalizado) {
            return;
        }

        eraAtual++;
        dragoesRevelados = 0;
        // Setup pedido: não permitir Dragões no setup inicial (mãos/mercado).
        // Após o setup, dragões voltam a poder aparecer normalmente.
        revelarCartasIniciais(setupInicialAtivo);
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
        return revelarCartasIniciais(false);
    }

    private List<Carta> revelarCartasIniciais(boolean ignorarDragoes) {
        List<Carta> reveladas = new ArrayList<>();
        // Segurança: se durante o setup só existirem Dragões disponíveis, não podemos ficar em loop infinito
        // tentando completar o mercado. Limitamos o número de compras com base no tamanho do monte+descarte.
        int limiteCompras = baralho.qntdRestante() + baralho.qntdDescarte() + 10;
        int comprasFeitas = 0;
        while (mercado.getCartasDisponiveis().size() < 5 && !baralho.semCartasDisponiveis()) {
            if (comprasFeitas++ >= limiteCompras) {
                ultimaAcao = "Setup: não foi possível completar o mercado sem Dragões";
                break;
            }
            Carta topo = baralho.comprarDoTopo();
            if (topo == null) {
                break;
            }
            if ("Dragão".equalsIgnoreCase(topo.tribo)) {
                // Setup inicial: não revela/conta Dragões no começo.
                // Eles são descartados e podem voltar mais tarde via reciclagem.
                baralho.descartarCarta(topo);
                if (!ignorarDragoes) {
                    registrarCompraDeCarta(topo);
                }
            } else {
                // Só conta compras/revelações não-dragão para manter consistência de logs/última ação.
                registrarCompraDeCarta(topo);
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
            if ("Dragão".equalsIgnoreCase(cartaComprada.tribo)) {
                // BUG corrigido: Dragões não entram na mão; eles são revelados e vão para o descarte.
                baralho.descartarCarta(cartaComprada);
                registrarCompraDeCarta(cartaComprada);
                ultimaAcao = "Dragão revelado ao recrutar (contador: " + dragoesRevelados + ")";
            } else {
                jogador.mao.add(cartaComprada);
                registrarCompraDeCarta(cartaComprada);
                ultimaAcao = "Recrutou " + cartaComprada + " para a mão";
            }
            revelarCartasIniciais();
        } else {
            ultimaAcao = "Sem cartas para recrutar";
        }

        // BUG corrigido: mesmo se não houver carta para comprar, o turno precisa terminar.
        // Caso contrário, a IA (ou o humano) fica "preso" e pode tentar comprar em loop.
        verificarFimDeEra();
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
        verificarFimDeEra();
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
                // BUG corrigido: se o bando tiver só 1 carta (o líder), não deve "voltar" a própria carta.
                // Recupera uma carta do bando diferente do líder, quando existir.
                Carta recuperada = null;
                for (Carta carta : bando) {
                    if (carta != lider) {
                        recuperada = carta;
                        break;
                    }
                }
                if (recuperada != null) {
                    jogador.mao.add(recuperada);
                    ultimaAcao = "Poder (Elfo): recupera " + recuperada + " para a mão de " + jogador.getNome();
                } else {
                    ultimaAcao = "Poder (Elfo): sem carta para recuperar (bando com 1 carta)";
                }
                break;
            case "dragão":
                dragoesRevelados = Math.max(0, dragoesRevelados - 1);
                ultimaAcao = "Poder (Dragão): reduz contador de dragões para " + dragoesRevelados;
                break;
            case "centauro":
                // Compra 1 carta do topo (respeitando limite de mão e regras de dragões).
                comprarCartaTopoParaMao(jogador);
                break;
            case "minotauro":
                // Bônus simples de agressividade.
                jogador.adicionarPontos(2);
                ultimaAcao = "Poder (Minotauro): +" + 2 + " pontos para " + jogador.getNome();
                break;
            case "esqueleto":
                // MVP: efeito simples (dreno simbólico).
                Jogador alvo = escolherAlvoParaEsqueleto(jogador);
                if (alvo != null) {
                    jogador.adicionarPontos(1);
                    ultimaAcao = "Poder (Esqueleto): +" + 1 + " ponto (dreno de " + alvo.getNome() + ")";
                } else {
                    ultimaAcao = "Poder (Esqueleto): sem alvo";
                }
                break;
            case "mago":
                // Compra 1 carta do mercado (primeira); se vazio, compra do topo.
                Carta mercadoCarta = mercado.comprarCarta();
                if (mercadoCarta != null) {
                    if ("Dragão".equalsIgnoreCase(mercadoCarta.tribo)) {
                        baralho.descartarCarta(mercadoCarta);
                        registrarCompraDeCarta(mercadoCarta);
                        ultimaAcao = "Poder (Mago): revelou Dragão no mercado";
                    } else if (jogador.mao.size() < LIMITE_MAO) {
                        jogador.mao.add(mercadoCarta);
                        ultimaAcao = "Poder (Mago): comprou do mercado " + mercadoCarta;
                    } else {
                        baralho.descartarCarta(mercadoCarta);
                        ultimaAcao = "Poder (Mago): mão cheia, carta do mercado foi descartada";
                    }
                } else {
                    comprarCartaTopoParaMao(jogador);
                }
                revelarCartasIniciais();
                break;
            case "troll":
                // +1 ponto por carta do bando (mínimo 1)
                int bonus = Math.max(1, bando != null ? bando.size() : 1);
                jogador.adicionarPontos(bonus);
                ultimaAcao = "Poder (Troll): +" + bonus + " pontos para " + jogador.getNome();
                break;
            default:
                ultimaAcao = "Poder (" + lider.tribo + "): sem efeito implementado";
                break;
        }

        turnoPowerJogador = jogador;
        turnoPowerRodada = rodadaAtual;
    }

    private void comprarCartaTopoParaMao(Jogador jogador) {
        if (jogador.mao.size() >= LIMITE_MAO) {
            ultimaAcao = "Poder: mão cheia (limite 10), sem compra";
            return;
        }
        Carta comprada = baralho.comprarDoTopo();
        if (comprada == null) {
            ultimaAcao = "Poder: sem cartas para comprar";
            return;
        }
        if ("Dragão".equalsIgnoreCase(comprada.tribo)) {
            baralho.descartarCarta(comprada);
            registrarCompraDeCarta(comprada);
            ultimaAcao = "Poder: revelou Dragão (contador: " + dragoesRevelados + ")";
            return;
        }
        jogador.mao.add(comprada);
        ultimaAcao = "Poder: comprou " + comprada + " do topo";
    }

    private Jogador escolherAlvoParaEsqueleto(Jogador atual) {
        for (Jogador j : jogadores) {
            if (j != atual) {
                return j;
            }
        }
        return null;
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

    public void distribuirMaosIniciais() {
        // Assunção de MVP: mão inicial varia com número de jogadores.
        // (Isso evita começar com "2 cartas fixas" e deixa o jogo mais consistente.)
        int qtdJogadores = jogadores.size();
        int cartasPorJogador;
        if (qtdJogadores <= 2) {
            cartasPorJogador = 6;
        } else if (qtdJogadores <= 4) {
            cartasPorJogador = 5;
        } else {
            cartasPorJogador = 4;
        }

        for (Jogador jogador : jogadores) {
            while (jogador.mao.size() < cartasPorJogador && !baralho.semCartasDisponiveis()) {
                Carta comprada = baralho.comprarDoTopo();
                if (comprada == null) {
                    break;
                }
                if ("Dragão".equalsIgnoreCase(comprada.tribo)) {
                    // Setup pedido: Dragões não entram na mão inicial e nem contam como revelados aqui.
                    baralho.descartarCarta(comprada);
                    continue;
                }
                jogador.mao.add(comprada);
            }
        }

        ultimaAcao = "Mãos iniciais distribuídas (" + cartasPorJogador + " cartas por jogador)";
        // Encerra o modo de setup: a partir daqui dragões podem aparecer normalmente.
        this.setupInicialAtivo = false;
    }
}