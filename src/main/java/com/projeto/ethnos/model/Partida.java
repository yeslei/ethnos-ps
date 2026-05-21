package com.projeto.ethnos.model;

import com.projeto.ethnos.model.carta.Dragao;
import com.projeto.ethnos.model.poder.PoderDoLider;
import com.projeto.ethnos.observer.Assinante;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Classe central do jogo.
 *
 * Padrões aplicados (todos referenciados no diagrama de classes da Parte 2):
 *
 *  - GoF Observer (papel de Subject):
 *      Mantém uma lista de Assinantes e fornece adicionaAssinante,
 *      removeAssinante e notifica(). Todas as ações de turno chamam
 *      notifica() ao final para que as Views se atualizem.
 *
 *  - GRASP Controller:
 *      Partida concentra a coordenação das operações de sistema (jogar
 *      bando, recrutar aliado, iniciar/terminar era). É o ponto de entrada
 *      a partir da UI/Controller.
 *
 *  - GRASP Low Coupling:
 *      Partida não conhece JavaFX. Conversa com a UI apenas pela interface
 *      Assinante. Trocar a UI (JavaFX → console, web, mobile) não afeta
 *      esta classe.
 *
 *  - GoF Strategy (delegação):
 *      Os poderes dos líderes não vivem aqui. Partida delega para
 *      lider.getPoder().executar(...). O switch por tribo desapareceu.
 *
 *  - GoF Factory Method (indireta):
 *      A criação de cartas é responsabilidade de CartaFactory. Partida
 *      recebe um baralho já populado, mantendo-se desacoplada das
 *      classes concretas de Carta.
 */
public class Partida {

    private static final int LIMITE_MAO = 10;
    private static final int ERAS_TOTAIS = 3;
    private static final int DRAGOES_FIM_ERA = 3;
    private static final int CARTAS_ABERTAS_MERCADO = 5;

    // --- Estado do jogo ---
    private final List<Jogador> jogadores;
    private final Baralho baralho;
    private final Mercado mercado;
    private final Tabuleiro tabuleiro;

    private int eraAtual;
    private int dragoesRevelados;
    private int indiceJogadorAtual;
    private int rodadaAtual;

    private boolean jogoFinalizado;
    private final List<Jogador> vencedores = new ArrayList<>();
    private String ultimaAcao;
    private boolean setupInicialAtivo;

    // --- Observer (papel de Subject) ---
    private final List<Assinante> assinantes = new ArrayList<>();

    public Partida(List<Jogador> jogadores, Baralho baralho, Mercado mercado, Tabuleiro tabuleiro) {
        if (jogadores == null || jogadores.size() < 2 || jogadores.size() > 6) {
            throw new IllegalArgumentException("Partida exige de 2 a 6 jogadores");
        }
        this.jogadores = new ArrayList<>(jogadores);
        this.baralho = baralho;
        this.mercado = mercado;
        this.tabuleiro = tabuleiro;
        this.eraAtual = 0;
        this.dragoesRevelados = 0;
        this.indiceJogadorAtual = 0;
        this.rodadaAtual = 1;
        this.jogoFinalizado = false;
        this.ultimaAcao = "Partida iniciada";
        this.setupInicialAtivo = true;
    }

    // ====================================================================
    // OBSERVER: gerência de assinantes
    // ====================================================================

    public void adicionaAssinante(Assinante obs) {
        if (obs != null && !assinantes.contains(obs)) {
            assinantes.add(obs);
        }
    }

    public void removeAssinante(Assinante obs) {
        assinantes.remove(obs);
    }

    /** Notifica todos os assinantes sobre uma mudança de estado. */
    public void notifica() {
        // Cópia defensiva: assinantes podem se remover durante atualiza().
        for (Assinante a : new ArrayList<>(assinantes)) {
            a.atualiza(this);
        }
    }

    // ====================================================================
    // SETUP (chamado por EthnosApp uma vez no início)
    // ====================================================================

    public void distribuirFichas() {
        for (Jogador j : jogadores) {
            j.distribuirFicha(null);
        }
    }

    /**
     * Distribuição inicial das mãos. A quantidade varia com o número de
     * jogadores. Dragões revelados durante a distribuição inicial vão para
     * o descarte sem contar para o fim de era.
     */
    public void distribuirMaosIniciais() {
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
            while (jogador.getMao().size() < cartasPorJogador && !baralho.semCartasDisponiveis()) {
                Carta comprada = baralho.comprarDoTopo();
                if (comprada == null) {
                    break;
                }
                if (Dragao.ehDragao(comprada)) {
                    baralho.descartarCarta(comprada);
                    continue;
                }
                jogador.getMao().add(comprada);
            }
        }
        ultimaAcao = "Mãos distribuídas (" + cartasPorJogador + " cartas por jogador)";
        this.setupInicialAtivo = false;
    }

    /** Operação do diagrama: iniciarEra(). */
    public void iniciarEra() {
        if (jogoFinalizado) return;
        eraAtual++;
        dragoesRevelados = 0;
        revelarCartasRaca(setupInicialAtivo);
        notifica();
    }

    /**
     * Operação do diagrama: revelaCartasRaca().
     * Preenche o mercado até atingir o tamanho-alvo. Dragões revelados
     * fora do setup contam para o fim de era.
     */
    public List<Carta> revelarCartasRaca() {
        return revelarCartasRaca(false);
    }

    private List<Carta> revelarCartasRaca(boolean ignorarDragoes) {
        List<Carta> reveladas = new ArrayList<>();
        // Evita loop infinito em situações degeneradas.
        int limite = baralho.qntdRestante() + baralho.qntdDescarte() + 10;
        int compras = 0;
        while (mercado.tamanho() < CARTAS_ABERTAS_MERCADO && !baralho.semCartasDisponiveis()) {
            if (compras++ >= limite) break;
            Carta topo = baralho.comprarDoTopo();
            if (topo == null) break;
            if (Dragao.ehDragao(topo)) {
                baralho.descartarCarta(topo);
                if (!ignorarDragoes) {
                    dragoesRevelados++;
                    if (dragoesRevelados < DRAGOES_FIM_ERA) {
                        ultimaAcao = "Dragão " + dragoesRevelados + "/3 revelado (sem efeito)";
                    } else {
                        ultimaAcao = "Terceiro dragão revelado - fim da era!";
                    }
                }
            } else {
                mercado.adicionarCartas(List.of(topo));
                reveladas.add(topo);
            }
        }
        return reveladas;
    }

    // ====================================================================
    // OPERAÇÕES DE TURNO (chamadas pelo Controller)
    // ====================================================================

    /**
     * Recrutar Aliado: o jogador da vez compra uma carta do mercado
     * (se especificada) ou do topo do baralho.
     */
    public void comprarAliado(Jogador jogador, Carta cartaMercadoSelecionada) {
        if (jogoFinalizado || jogador == null) return;
        if (jogador != getJogadorAtual()) {
            throw new IllegalArgumentException("Não é o turno deste jogador.");
        }
        if (jogador.getMao().size() >= LIMITE_MAO) {
            throw new IllegalArgumentException(
                "Limite de mão atingido (" + LIMITE_MAO + " cartas). Jogue um bando antes de recrutar.");
        }

        Carta comprada;
        if (cartaMercadoSelecionada != null) {
            comprada = mercado.comprarCarta(cartaMercadoSelecionada);
        } else {
            comprada = baralho.comprarDoTopo();
        }

        if (comprada != null) {
            if (Dragao.ehDragao(comprada)) {
                baralho.descartarCarta(comprada);
                dragoesRevelados++;
                if (dragoesRevelados < DRAGOES_FIM_ERA) {
                    ultimaAcao = "Dragão " + dragoesRevelados + "/3 revelado ao recrutar (sem efeito)";
                } else {
                    ultimaAcao = "Terceiro dragão revelado ao recrutar - fim da era!";
                }
            } else {
                jogador.getMao().add(comprada);
                ultimaAcao = jogador.getNome() + " recrutou " + comprada;
            }
            revelarCartasRaca();
        } else {
            ultimaAcao = "Sem cartas para recrutar";
        }

        verificarFimDeEra();
        proximoJogador();
        notifica();
    }

    /**
     * Operação principal: jogar um bando.
     *
     * Passos:
     *  1. Validar dono do turno, líder dentro do bando e região válida.
     *  2. Validar combinação de cor/tribo do bando com o líder.
     *  3. Pedir ao Jogador que crie o Bando (Creator).
     *  4. Adicionar marcador na região e contabilizar pontos do bando.
     *  5. Aplicar o poder do líder via Strategy (lider.getPoder()).
     *  6. Devolver sobras da mão para o mercado.
     *  7. Verificar fim de era, avançar turno, notificar.
     */
    public void iniciarJogadaDoBando(Jogador j, List<Carta> bando, Carta lider, Regiao regiao) {
        if (jogoFinalizado || j == null || bando == null || bando.isEmpty() || lider == null) {
            return;
        }
        if (j != getJogadorAtual()) {
            throw new IllegalArgumentException("Não é o turno deste jogador.");
        }
        if (!bando.contains(lider)) {
            throw new IllegalArgumentException("O líder precisa estar dentro do bando selecionado.");
        }
        if (regiao == null) {
            throw new IllegalArgumentException("Selecione uma região para posicionar a ficha.");
        }

        // Validação de coerência do bando.
        for (Carta carta : bando) {
            boolean combinaCor = carta.getCor().equalsIgnoreCase(lider.getCor());
            boolean combinaTribo = carta.getTribo().equalsIgnoreCase(lider.getTribo());
            if (!combinaCor && !combinaTribo) {
                throw new IllegalArgumentException(
                    "Todas as cartas do bando devem combinar em cor ou tribo com o líder.");
            }
        }

        // Regra do Ethnos: para plantar o N-ésimo marcador na região, o bando
        // precisa ter no mínimo N cartas. Ou seja, o tamanho do bando deve ser
        // estritamente maior que a quantidade de marcadores que o jogador já
        // tem na região. Minotauro como líder reduz o requisito em 1.
        int marcadoresPresentes = contarMarcadoresDoJogadorNaRegiao(j, regiao);
        int tamanhoMinimo = marcadoresPresentes + 1;
        if (lider instanceof com.projeto.ethnos.model.carta.Minotauro) {
            tamanhoMinimo = Math.max(1, tamanhoMinimo - 1);
        }
        if (bando.size() < tamanhoMinimo) {
            throw new IllegalArgumentException(
                "Bando muito pequeno para essa região: você já tem "
                + marcadoresPresentes + " marcador(es) lá, o bando precisa de pelo menos "
                + tamanhoMinimo + " carta(s).");
        }

        // GRASP Creator: o Jogador cria o Bando.
        Bando bandoCriado = j.jogarBando(bando, lider);

        // Marcar presença na região e contar pontos.
        regiao.adicionarMarcador(j);
        j.adicionarPontos(bandoCriado.calcularPontos());

        // Sobras da mão vão para o mercado (regra do jogo).
        if (!j.getMao().isEmpty()) {
            mercado.adicionarCartas(new ArrayList<>(j.getMao()));
            j.getMao().clear();
        }

        // GoF Strategy: aplica o poder do líder polimorficamente.
        lider.ativaPoder(); // operação do diagrama (apenas registra)
        aplicarPoderDoLider(j, lider, bandoCriado.getCartas(), regiao);

        ultimaAcao = j.getNome() + " baixou " + bandoCriado.getTamanho()
                   + " cartas em " + regiao.getNome() + " (líder: " + lider.getTribo() + ")";

        verificarFimDeEra();
        proximoJogador();
        revelarCartasRaca();
        notifica();
    }

    /**
     * Aplica o poder do líder via Strategy. Não conhece as tribos
     * específicas: apenas pede o PoderDoLider à carta e executa.
     */
    public void aplicarPoderDoLider(Jogador jogador, Carta lider, List<Carta> bando, Regiao regiao) {
        if (lider == null || jogador == null) return;
        PoderDoLider poder = lider.getPoder();
        if (poder == null) {
            ultimaAcao = "Líder sem poder ativo (" + lider.getTribo() + ")";
            return;
        }
        String resultado = poder.executar(this, jogador, bando, regiao);
        if (resultado != null) {
            ultimaAcao = resultado;
        }
    }

    /**
     * Helper usado por poderes (Mago, Centauro) que precisam comprar uma
     * carta do baralho. Cuida de revelar dragões e respeitar limite de mão.
     */
    public Carta comprarParaMao(Jogador jogador) {
        if (jogador.getMao().size() >= LIMITE_MAO) return null;
        Carta comprada = baralho.comprarDoTopo();
        if (comprada == null) return null;
        if (Dragao.ehDragao(comprada)) {
            baralho.descartarCarta(comprada);
            dragoesRevelados++;
            return null;
        }
        jogador.getMao().add(comprada);
        return comprada;
    }

    /** Conta quantos marcadores um jogador já tem em uma região específica. */
    public int contarMarcadoresDoJogadorNaRegiao(Jogador jogador, Regiao regiao) {
        if (jogador == null || regiao == null) return 0;
        int contagem = 0;
        for (Jogador j : regiao.getMarcadores()) {
            if (j == jogador) contagem++;
        }
        return contagem;
    }

    // ====================================================================
    // FIM DE ERA E DE JOGO
    // ====================================================================

    /** Operação do diagrama: verificarFimDeEra(). */
    public void verificarFimDeEra() {
        if (jogoFinalizado) return;
        if (dragoesRevelados >= DRAGOES_FIM_ERA || baralho.semCartasDisponiveis()) {
            finalizarEra();
            if (eraAtual >= ERAS_TOTAIS) {
                finalizarJogo();
            } else {
                iniciarEra();
            }
        }
    }

    private void finalizarEra() {
        // Pontuação por região, baseada em ranking de marcadores (Information Expert).
        // Na era N, apenas as N primeiras colocações pontuam (regra oficial).
        for (Regiao r : tabuleiro.getTodasRegioes()) {
            List<Jogador> ranking = r.rankingJogadoresPorMarcadores();
            for (int pos = 0; pos < ranking.size(); pos++) {
                Jogador j = ranking.get(pos);
                int pontos = r.getPontuacao(pos, eraAtual);
                j.adicionarPontos(pontos);
            }
        }

        // Limpeza: mercado e mãos vão para o descarte.
        baralho.descartarCartas(mercado.retirarTodas());
        for (Jogador j : jogadores) {
            baralho.descartarCartas(new ArrayList<>(j.getMao()));
            j.getMao().clear();
        }
        ultimaAcao = "Fim da Era " + eraAtual + ": pontuação contabilizada";
    }

    /** Operação do diagrama: finalizarJogo(). */
    public void finalizarJogo() {
        this.jogoFinalizado = true;
        vencedores.clear();
        int maiorPontuacao = jogadores.stream()
            .mapToInt(Jogador::getPontuacao).max().orElse(0);
        for (Jogador j : jogadores) {
            if (j.getPontuacao() == maiorPontuacao) {
                vencedores.add(j);
            }
        }
        if (!vencedores.isEmpty()) {
            ultimaAcao = "Fim do jogo. " + getNomesVencedores() + " venceu com "
                       + maiorPontuacao + " pontos.";
        }
    }

    // ====================================================================
    // IA SIMPLES (não é um padrão por si só, mas mantida para demonstração)
    // ====================================================================

    public void jogarTurnoIA() {
        if (jogoFinalizado || jogadores.isEmpty()) return;
        Jogador ia = getJogadorAtual();
        if (!ia.isIa()) return;

        if (!ia.getMao().isEmpty()) {
            JogadaIA jogada = escolherMelhorJogadaIA(ia);
            if (jogada != null && jogada.regiao != null) {
                iniciarJogadaDoBando(ia, jogada.bando, jogada.lider, jogada.regiao);
                return;
            }
            // Nenhum bando válido para nenhuma região -> recruta.
            comprarAliado(ia, escolherCartaMercadoParaIA(ia));
        } else {
            comprarAliado(ia, escolherCartaMercadoParaIA(ia));
        }
    }

    private JogadaIA escolherMelhorJogadaIA(Jogador ia) {
        Carta melhorLider = ia.getMao().get(0);
        List<Carta> melhorBando = List.of(melhorLider);

        for (Carta candidata : ia.getMao()) {
            List<Carta> bandoAtual = new ArrayList<>();
            for (Carta carta : ia.getMao()) {
                boolean combinaCor = carta.getCor().equalsIgnoreCase(candidata.getCor());
                boolean combinaTribo = carta.getTribo().equalsIgnoreCase(candidata.getTribo());
                if (combinaCor || combinaTribo) {
                    bandoAtual.add(carta);
                }
            }
            if (bandoAtual.size() > melhorBando.size()) {
                melhorLider = candidata;
                melhorBando = bandoAtual;
            }
        }
        Regiao regiaoValida = escolherRegiaoValidaParaIA(ia, melhorLider, melhorBando.size());
        if (regiaoValida == null) return null;
        return new JogadaIA(melhorLider, melhorBando, regiaoValida);
    }

    /**
     * Escolhe uma região onde a regra do N-ésimo marcador permita plantar.
     * Tenta primeiro a região da cor do líder, depois as outras.
     */
    private Regiao escolherRegiaoValidaParaIA(Jogador ia, Carta lider, int tamanhoBando) {
        Regiao preferida = tabuleiro.getRegiao(lider.getCor());
        int tamanhoMinExtra = (lider instanceof com.projeto.ethnos.model.carta.Minotauro) ? 1 : 0;

        if (preferida != null
            && contarMarcadoresDoJogadorNaRegiao(ia, preferida) < tamanhoBando + tamanhoMinExtra) {
            return preferida;
        }
        for (Regiao r : tabuleiro.getTodasRegioes()) {
            if (contarMarcadoresDoJogadorNaRegiao(ia, r) < tamanhoBando + tamanhoMinExtra) {
                return r;
            }
        }
        return null;
    }

    private Carta escolherCartaMercadoParaIA(Jogador ia) {
        for (Carta cm : mercado.getCartasDisponiveis()) {
            for (Carta cMao : ia.getMao()) {
                if (cm.getCor().equalsIgnoreCase(cMao.getCor())
                    || cm.getTribo().equalsIgnoreCase(cMao.getTribo())) {
                    return cm;
                }
            }
        }
        return null;
    }

    private static final class JogadaIA {
        final Carta lider;
        final List<Carta> bando;
        final Regiao regiao;
        JogadaIA(Carta l, List<Carta> b, Regiao r) { this.lider = l; this.bando = b; this.regiao = r; }
    }

    // ====================================================================
    // Avanço de turno
    // ====================================================================

    private void proximoJogador() {
        if (jogadores.isEmpty()) return;
        int anterior = indiceJogadorAtual;
        indiceJogadorAtual = (indiceJogadorAtual + 1) % jogadores.size();
        if (anterior == jogadores.size() - 1 && indiceJogadorAtual == 0) {
            rodadaAtual++;
        }
    }

    // ====================================================================
    // Getters de consulta para a UI
    // ====================================================================

    public int getEraAtual() { return eraAtual; }
    public int getDragoesRevelados() { return dragoesRevelados; }
    public int getRodadaAtual() { return rodadaAtual; }
    public boolean isJogoFinalizado() { return jogoFinalizado; }
    public String getUltimaAcao() { return ultimaAcao; }
    public Jogador getJogadorAtual() { return jogadores.get(indiceJogadorAtual); }
    public List<Jogador> getJogadores() { return List.copyOf(jogadores); }
    public Baralho getBaralho() { return baralho; }
    public Mercado getMercado() { return mercado; }
    public Tabuleiro getTabuleiro() { return tabuleiro; }
    public List<Jogador> getVencedores() { return List.copyOf(vencedores); }
    public boolean isEmpateFinal() { return vencedores.size() > 1; }

    public String getNomesVencedores() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vencedores.size(); i++) {
            sb.append(vencedores.get(i).getNome());
            if (i < vencedores.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }
}
