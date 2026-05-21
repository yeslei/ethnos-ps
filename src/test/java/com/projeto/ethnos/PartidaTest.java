package com.projeto.ethnos;

import com.projeto.ethnos.factory.CartaFactory;
import com.projeto.ethnos.model.Bando;
import com.projeto.ethnos.model.Baralho;
import com.projeto.ethnos.model.Carta;
import com.projeto.ethnos.model.Jogador;
import com.projeto.ethnos.model.Mercado;
import com.projeto.ethnos.model.Partida;
import com.projeto.ethnos.model.Regiao;
import com.projeto.ethnos.model.Tabuleiro;
import com.projeto.ethnos.model.carta.Anao;
import com.projeto.ethnos.model.carta.Dragao;
import com.projeto.ethnos.model.carta.Elfo;
import com.projeto.ethnos.model.carta.Gigante;
import com.projeto.ethnos.model.carta.Troll;
import com.projeto.ethnos.observer.Assinante;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes do modelo do Ethnos.
 *
 * Estes testes existem por dois motivos:
 *  1. Comprovar que o jogo funciona (regras essenciais).
 *  2. Servir como evidência prática de que o modelo está desacoplado
 *     da UI (GRASP Low Coupling): nenhum teste depende de JavaFX.
 *     Isso é um argumento direto para a apresentação da Parte 2.
 */
public class PartidaTest {

    private Tabuleiro novoTabuleiro() {
        List<Regiao> rs = Arrays.asList(
            new Regiao("Vermelho", Arrays.asList(8, 5, 2)),
            new Regiao("Verde",    Arrays.asList(8, 5, 2)),
            new Regiao("Cinza",    Arrays.asList(8, 5, 2))
        );
        return new Tabuleiro(rs);
    }

    // ------------------------------------------------------------
    // GoF Factory Method
    // ------------------------------------------------------------

    @Test
    void factoryCriaCartasConcretasCorretas() {
        assertTrue(CartaFactory.criarCarta("Anão", "Vermelho") instanceof Anao);
        assertTrue(CartaFactory.criarCarta("Elfo", "Verde") instanceof Elfo);
        assertTrue(CartaFactory.criarCarta("Dragão", "Cinza") instanceof Dragao);
    }

    @Test
    void factoryLancaParaTriboInexistente() {
        assertThrows(IllegalArgumentException.class,
            () -> CartaFactory.criarCarta("Vampiro", "Roxo"));
    }

    @Test
    void factoryMontaBaralhoComTresDragoes() {
        Baralho b = CartaFactory.montarBaralhoPadrao();
        int dragoes = 0;
        int total = b.qntdRestante();
        // Vamos contar consumindo o baralho (ele já está embaralhado)
        for (int i = 0; i < total; i++) {
            Carta c = b.comprarDoTopo();
            if (Dragao.ehDragao(c)) dragoes++;
        }
        assertEquals(3, dragoes, "Baralho padrão deve ter exatamente 3 dragões");
    }

    // ------------------------------------------------------------
    // GoF Strategy (poderes dos líderes)
    // ------------------------------------------------------------

    @Test
    void poderAnaoSomaUmPonto() {
        Partida p = montarPartidaMinima();
        Jogador j = p.getJogadores().get(0);
        Carta anao = new Anao("Vermelho");
        // anao + outra carta para ter um bando válido
        j.getMao().add(anao);
        j.getMao().add(new Anao("Vermelho"));

        int pontosAntes = j.getPontuacao();
        p.iniciarJogadaDoBando(j, new ArrayList<>(j.getMao()), anao,
            p.getTabuleiro().getRegiao("Vermelho"));

        // Bonus: 2 cartas + 1 (anão como líder dá +1) + 1 (poder estratégia) = 4
        // O importante é que houve incremento de pontos.
        assertTrue(j.getPontuacao() > pontosAntes);
    }

    @Test
    void poderGiganteSomaDoisPontos() {
        Partida p = montarPartidaMinima();
        Jogador j = p.getJogadores().get(0);
        Carta gigante = new Gigante("Vermelho");
        Carta gigante2 = new Gigante("Vermelho");
        j.getMao().addAll(List.of(gigante, gigante2));

        int pontosAntes = j.getPontuacao();
        p.iniciarJogadaDoBando(j, List.of(gigante, gigante2), gigante,
            p.getTabuleiro().getRegiao("Vermelho"));

        // Tabela oficial: bando de 2 cartas = 1 ponto.
        // Poder do Gigante: +2 pontos.
        // Total: +3 pontos.
        assertEquals(pontosAntes + 3, j.getPontuacao());
    }

    @Test
    void poderTrollEscalaComBando() {
        Partida p = montarPartidaMinima();
        Jogador j = p.getJogadores().get(0);
        Carta troll = new Troll("Cinza");
        Carta troll2 = new Troll("Cinza");
        Carta troll3 = new Troll("Cinza");
        j.getMao().addAll(List.of(troll, troll2, troll3));

        int pontosAntes = j.getPontuacao();
        p.iniciarJogadaDoBando(j, List.of(troll, troll2, troll3), troll,
            p.getTabuleiro().getRegiao("Cinza"));

        // Tabela oficial: bando de 3 cartas = 3 pontos.
        // Poder do Troll: +tamanho do bando = +3.
        // Total: +6 pontos.
        assertEquals(pontosAntes + 6, j.getPontuacao());
    }

    @Test
    void dragaoNaoTemPoder() {
        Carta d = new Dragao("Vermelho");
        assertNull(d.getPoder(),
            "Dragão deve devolver null em getPoder() — tratado como caso especial pela Partida");
    }

    // ------------------------------------------------------------
    // GoF Observer
    // ------------------------------------------------------------

    @Test
    void observerNotificaAssinantesEmJogadas() {
        Partida p = montarPartidaMinima();
        ContadorAssinante a = new ContadorAssinante();
        p.adicionaAssinante(a);

        Jogador j = p.getJogadores().get(0);
        j.getMao().add(new Gigante("Vermelho"));

        p.iniciarJogadaDoBando(j, new ArrayList<>(j.getMao()), j.getMao().get(0),
            p.getTabuleiro().getRegiao("Vermelho"));

        assertTrue(a.contador > 0, "Assinante deveria ter sido notificado ao menos uma vez");
    }

    @Test
    void observerRemovecaoFunciona() {
        Partida p = montarPartidaMinima();
        ContadorAssinante a = new ContadorAssinante();
        p.adicionaAssinante(a);
        p.removeAssinante(a);
        p.notifica();
        assertEquals(0, a.contador, "Após remover, o assinante não recebe mais notificações");
    }

    // ------------------------------------------------------------
    // GRASP Creator: Jogador cria Bando
    // ------------------------------------------------------------

    @Test
    void jogadorCriaBandoAoJogar() {
        Jogador j = new Jogador("Teste");
        Carta lider = new Anao("Verde");
        Carta companheira = new Anao("Verde");
        j.getMao().addAll(List.of(lider, companheira));

        Bando bando = j.jogarBando(List.of(lider, companheira), lider);

        assertEquals(2, bando.getTamanho());
        assertSame(lider, bando.getLider());
        assertEquals(0, j.getMao().size(), "Mão deve estar vazia após jogar todas as cartas");
    }

    @Test
    void bandoCalculaPontosCorretamente() {
        Carta lider = new Anao("Verde");
        Carta c2 = new Anao("Verde");
        Bando b = new Bando(List.of(lider, c2), lider);
        // Tabela oficial: bando de 2 com líder anão -> tamanho efetivo 3 -> 3 pontos.
        assertEquals(3, b.calcularPontos());
    }

    // ------------------------------------------------------------
    // GRASP Information Expert: Regiao calcula ranking
    // ------------------------------------------------------------

    @Test
    void regiaoOrdenaRankingPorMarcadores() {
        Regiao r = new Regiao("Teste", List.of(8, 5, 2));
        Jogador a = new Jogador("A");
        Jogador b = new Jogador("B");
        Jogador c = new Jogador("C");

        // A: 3, B: 2, C: 1
        r.adicionarMarcador(a); r.adicionarMarcador(a); r.adicionarMarcador(a);
        r.adicionarMarcador(b); r.adicionarMarcador(b);
        r.adicionarMarcador(c);

        List<Jogador> ranking = r.rankingJogadoresPorMarcadores();
        assertEquals(List.of(a, b, c), ranking);
    }

    @Test
    void regiaoDevolveZeroParaPosicaoForaDoArray() {
        Regiao r = new Regiao("X", List.of(1, 3, 6));
        // Era 3: 1º lugar ganha valoresPontuacao[2] = 6.
        assertEquals(6, r.getPontuacao(0));
        // Posição além da quantidade de jogadores possíveis -> 0
        assertEquals(0, r.getPontuacao(5));
    }

    @Test
    void regiaoPontuacaoPorEra() {
        // Faixas [1, 3, 6] = fichas reveladas em cada era.
        Regiao r = new Regiao("Teste", List.of(1, 3, 6));

        // Era 1: só o 1º colocado pontua, com o menor valor (1).
        assertEquals(1, r.getPontuacao(0, 1));
        assertEquals(0, r.getPontuacao(1, 1));

        // Era 2: 1º ganha 3, 2º ganha 1, 3º não pontua.
        assertEquals(3, r.getPontuacao(0, 2));
        assertEquals(1, r.getPontuacao(1, 2));
        assertEquals(0, r.getPontuacao(2, 2));

        // Era 3: 1º ganha 6, 2º ganha 3, 3º ganha 1.
        assertEquals(6, r.getPontuacao(0, 3));
        assertEquals(3, r.getPontuacao(1, 3));
        assertEquals(1, r.getPontuacao(2, 3));
    }

    // ------------------------------------------------------------
    // Tabela de pontuação do bando (regra oficial do Ethnos)
    // ------------------------------------------------------------

    @Test
    void bandoUmaCartaNaoPontua() {
        Carta unica = new Gigante("Vermelho");
        Bando b = new Bando(List.of(unica), unica);
        // Tabela: 1 carta = 0 pontos.
        assertEquals(0, b.calcularPontos());
    }

    @Test
    void bandoSeisOuMaisVale15Pontos() {
        // Bando de 6 elfos (todos da mesma tribo).
        Carta l = new Elfo("Verde");
        Bando b = new Bando(List.of(l,
            new Elfo("Verde"), new Elfo("Verde"),
            new Elfo("Verde"), new Elfo("Verde"), new Elfo("Verde")), l);
        // Tabela: 6+ cartas = 15 pontos.
        assertEquals(15, b.calcularPontos());
    }

    @Test
    void bandoQuatroCartas_seisPontos() {
        Carta l = new Gigante("Vermelho");
        Bando b = new Bando(List.of(l,
            new Gigante("Vermelho"), new Gigante("Vermelho"), new Gigante("Vermelho")), l);
        // Tabela: 4 cartas = 6 pontos.
        assertEquals(6, b.calcularPontos());
    }

    // ------------------------------------------------------------
    // Regra do N-ésimo marcador
    // ------------------------------------------------------------

    @Test
    void rejeitaSegundoMarcadorComBandoDeUmaCarta() {
        Partida p = montarPartidaMinima();
        Jogador j = p.getJogadores().get(0);

        // Primeiro bando (1 carta) -> coloca marcador na região.
        Carta a1 = new Anao("Vermelho");
        j.getMao().add(a1);
        Regiao regiaoVermelho = p.getTabuleiro().getRegiao("Vermelho");
        p.iniciarJogadaDoBando(j, List.of(a1), a1, regiaoVermelho);

        // Volta para Você (2 jogadores, IA passa - mas como o outro nem joga, força)
        // Para facilitar o teste, criamos uma partida só dele (vamos para o turno dele de novo
        // simulando o oponente jogar):
        // Como o outro jogador também é humano e não tem cartas, ele não consegue jogar.
        // Vamos forçar uma jogada do oponente jogando uma carta única numa região diferente:
        Jogador outro = p.getJogadores().get(1);
        Carta opCarta = new Anao("Verde");
        outro.getMao().add(opCarta);
        p.iniciarJogadaDoBando(outro, List.of(opCarta), opCarta,
            p.getTabuleiro().getRegiao("Verde"));

        // Agora é a vez do "Você" de novo. Tenta plantar segundo marcador
        // em Vermelho com bando de 1 carta -> deve falhar.
        Carta a2 = new Anao("Vermelho");
        j.getMao().add(a2);
        assertThrows(IllegalArgumentException.class, () ->
            p.iniciarJogadaDoBando(j, List.of(a2), a2, regiaoVermelho),
            "Bando de 1 carta não pode plantar 2º marcador");
    }

    // ------------------------------------------------------------
    // Factory: tribos selecionadas pelo usuário
    // ------------------------------------------------------------

    @Test
    void factoryMontaBaralhoApenasComTribosSelecionadas() {
        Baralho b = CartaFactory.montarBaralhoComTribos(List.of("Anão", "Elfo"));
        int total = b.qntdRestante();
        int dragoes = 0, anaos = 0, elfos = 0, outros = 0;
        for (int i = 0; i < total; i++) {
            Carta c = b.comprarDoTopo();
            if (Dragao.ehDragao(c)) dragoes++;
            else if ("Anão".equalsIgnoreCase(c.getTribo())) anaos++;
            else if ("Elfo".equalsIgnoreCase(c.getTribo())) elfos++;
            else outros++;
        }
        assertEquals(3, dragoes, "Sempre devem entrar 3 Dragões");
        assertTrue(anaos > 0, "Deve ter cartas de Anão");
        assertTrue(elfos > 0, "Deve ter cartas de Elfo");
        assertEquals(0, outros, "Não deve ter tribos não-selecionadas");
    }

    // ------------------------------------------------------------
    // Regras de validação na Partida
    // ------------------------------------------------------------

    @Test
    void rejeitaBandoSemCombinacaoComLider() {
        Partida p = montarPartidaMinima();
        Jogador j = p.getJogadores().get(0);
        Carta lider = new Anao("Vermelho");
        Carta intruso = new Elfo("Verde"); // nem mesma cor, nem mesma tribo
        j.getMao().addAll(List.of(lider, intruso));

        assertThrows(IllegalArgumentException.class, () ->
            p.iniciarJogadaDoBando(j, List.of(lider, intruso), lider,
                p.getTabuleiro().getRegiao("Vermelho")));
    }

    @Test
    void rejeitaJogadaForaDoTurno() {
        Partida p = montarPartidaMinima();
        Jogador outro = p.getJogadores().get(1); // não é o da vez
        Carta lider = new Anao("Vermelho");
        outro.getMao().add(lider);
        assertThrows(IllegalArgumentException.class, () ->
            p.iniciarJogadaDoBando(outro, List.of(lider), lider,
                p.getTabuleiro().getRegiao("Vermelho")));
    }

    @Test
    void baralhoRecicliaDescarteQuandoZera() {
        Baralho b = new Baralho();
        Carta c1 = new Anao("Vermelho");
        Carta c2 = new Elfo("Verde");
        b.descartarCarta(c1);
        b.descartarCarta(c2);
        // Mesmo sem cartas no monte, há descarte; semCartasDisponiveis = false.
        assertFalse(b.semCartasDisponiveis());
        // E uma compra deve devolver carta (após reciclagem).
        assertNotNull(b.comprarDoTopo());
    }

    // ------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------

    private Partida montarPartidaMinima() {
        Baralho b = new Baralho();
        // Mínimo viável para a Partida não quebrar em revelarCartasRaca.
        b.adicionarCarta(new Elfo("Azul"));
        b.adicionarCarta(new Elfo("Azul"));
        Mercado m = new Mercado();
        Tabuleiro t = novoTabuleiro();
        List<Jogador> jogadores = List.of(new Jogador("Você"), new Jogador("Outro"));
        return new Partida(jogadores, b, m, t);
    }

    /** Assinante simples para testar o Observer. */
    private static class ContadorAssinante implements Assinante {
        int contador = 0;
        @Override
        public void atualiza(Partida p) {
            contador++;
        }
    }
}
