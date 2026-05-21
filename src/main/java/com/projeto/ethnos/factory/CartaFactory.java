package com.projeto.ethnos.factory;

import com.projeto.ethnos.model.Baralho;
import com.projeto.ethnos.model.Carta;
import com.projeto.ethnos.model.carta.Anao;
import com.projeto.ethnos.model.carta.Centauro;
import com.projeto.ethnos.model.carta.Dragao;
import com.projeto.ethnos.model.carta.Elfo;
import com.projeto.ethnos.model.carta.Esqueleto;
import com.projeto.ethnos.model.carta.Gigante;
import com.projeto.ethnos.model.carta.Mago;
import com.projeto.ethnos.model.carta.Minotauro;
import com.projeto.ethnos.model.carta.Troll;

import java.util.List;
import java.util.Locale;

/**
 * Padrão GoF: Factory Method.
 *
 * Centraliza a criação de cartas. Isolar a instanciação em uma fábrica
 * traz três benefícios concretos para o projeto:
 *
 *  1. EthnosApp, Partida e os testes deixam de depender das classes
 *     concretas (Anao, Elfo, ...). Eles dependem apenas de Carta e desta
 *     fábrica. Acoplamento baixo (GRASP Low Coupling).
 *
 *  2. Adicionar uma nova tribo significa adicionar uma subclasse de Carta
 *     e um case aqui; o resto do sistema não muda (Open/Closed).
 *
 *  3. Centraliza decisões: por exemplo, se quiséssemos sortear a cor da
 *     carta, esse sorteio fica num único lugar.
 *
 * Observação: também oferecemos um método utilitário para montar o
 * baralho padrão usado no jogo. Isso poderia, no futuro, virar um
 * Abstract Factory (DeckFactory) caso quiséssemos suportar variantes
 * de tema (ex.: tema medieval x tema espacial).
 */
public final class CartaFactory {

    /**
     * Lista de todas as tribos disponíveis (sem Dragão, que é especial e
     * adicionado automaticamente). Útil para a tela de seleção no setup.
     */
    public static final List<String> TRIBOS_DISPONIVEIS = List.of(
        "Anão", "Elfo", "Gigante", "Mago", "Troll", "Minotauro", "Centauro", "Esqueleto"
    );

    /**
     * Monta um baralho com APENAS as tribos selecionadas pelo jogador no setup.
     * Os 3 Dragões são sempre adicionados (regra do jogo).
     *
     * Esta é a forma "Ethnos-fiel" de iniciar a partida: o jogador escolhe
     * quais facções vão entrar nessa partida específica, mudando a estratégia
     * entre uma partida e outra.
     *
     * Distribuição: para cada tribo escolhida, criamos 3 cartas em cores
     * variadas. Isso garante baralhos balanceados independente da seleção.
     */
    public static Baralho montarBaralhoComTribos(List<String> tribosSelecionadas) {
        if (tribosSelecionadas == null || tribosSelecionadas.isEmpty()) {
            return montarBaralhoPadrao();
        }
        Baralho b = new Baralho();
        String[] coresDisponiveis = { "Vermelho", "Verde", "Amarelo", "Azul", "Roxo", "Cinza" };
        int corIndice = 0;
        for (String tribo : tribosSelecionadas) {
            // 3 cartas por tribo, em cores rotacionadas para garantir variedade.
            for (int i = 0; i < 3; i++) {
                String cor = coresDisponiveis[corIndice % coresDisponiveis.length];
                b.adicionarCarta(criarCarta(tribo, cor));
                corIndice++;
            }
        }
        // Dragões sempre entram (3 cartas, conforme regra original).
        b.adicionarCarta(criarCarta("Dragão", "Vermelho"));
        b.adicionarCarta(criarCarta("Dragão", "Verde"));
        b.adicionarCarta(criarCarta("Dragão", "Cinza"));
        b.embaralhar();
        return b;
    }

    private CartaFactory() {
        // Fábrica estática; impedimos instanciação.
    }

    /**
     * Cria uma carta a partir do nome da tribo e da cor.
     * É o Factory Method propriamente dito.
     */
    public static Carta criarCarta(String tribo, String cor) {
        if (tribo == null) {
            throw new IllegalArgumentException("Tribo não pode ser nula");
        }
        switch (tribo.toLowerCase(Locale.ROOT)) {
            case "anão":
            case "anao":
                return new Anao(cor);
            case "elfo":
                return new Elfo(cor);
            case "gigante":
                return new Gigante(cor);
            case "mago":
                return new Mago(cor);
            case "troll":
                return new Troll(cor);
            case "minotauro":
                return new Minotauro(cor);
            case "centauro":
                return new Centauro(cor);
            case "esqueleto":
                return new Esqueleto(cor);
            case "dragão":
            case "dragao":
                return new Dragao(cor);
            default:
                throw new IllegalArgumentException("Tribo desconhecida: " + tribo);
        }
    }

    /**
     * Monta um baralho padrão com a distribuição balanceada de cartas
     * usada no MVP. Os Dragões são adicionados separadamente para a
     * regra de fim de era funcionar.
     */
    public static Baralho montarBaralhoPadrao() {
        Baralho b = new Baralho();

        // Anões — força em quantidade.
        b.adicionarCarta(criarCarta("Anão", "Vermelho"));
        b.adicionarCarta(criarCarta("Anão", "Vermelho"));
        b.adicionarCarta(criarCarta("Anão", "Amarelo"));
        b.adicionarCarta(criarCarta("Anão", "Cinza"));

        // Elfos — versáteis.
        b.adicionarCarta(criarCarta("Elfo", "Verde"));
        b.adicionarCarta(criarCarta("Elfo", "Verde"));
        b.adicionarCarta(criarCarta("Elfo", "Azul"));
        b.adicionarCarta(criarCarta("Elfo", "Roxo"));

        // Minotauros — agressivos.
        b.adicionarCarta(criarCarta("Minotauro", "Vermelho"));
        b.adicionarCarta(criarCarta("Minotauro", "Verde"));
        b.adicionarCarta(criarCarta("Minotauro", "Cinza"));

        // Centauros — exploradores.
        b.adicionarCarta(criarCarta("Centauro", "Amarelo"));
        b.adicionarCarta(criarCarta("Centauro", "Amarelo"));
        b.adicionarCarta(criarCarta("Centauro", "Azul"));

        // Esqueletos — combate.
        b.adicionarCarta(criarCarta("Esqueleto", "Azul"));
        b.adicionarCarta(criarCarta("Esqueleto", "Roxo"));
        b.adicionarCarta(criarCarta("Esqueleto", "Cinza"));

        // Gigantes — pontos brutos.
        b.adicionarCarta(criarCarta("Gigante", "Roxo"));
        b.adicionarCarta(criarCarta("Gigante", "Roxo"));
        b.adicionarCarta(criarCarta("Gigante", "Vermelho"));

        // Magos — controle de mão.
        b.adicionarCarta(criarCarta("Mago", "Cinza"));
        b.adicionarCarta(criarCarta("Mago", "Verde"));
        b.adicionarCarta(criarCarta("Mago", "Amarelo"));

        // Trolls — multiplicadores.
        b.adicionarCarta(criarCarta("Troll", "Cinza"));
        b.adicionarCarta(criarCarta("Troll", "Vermelho"));
        b.adicionarCarta(criarCarta("Troll", "Roxo"));

        // 3 Dragões — fim de era.
        b.adicionarCarta(criarCarta("Dragão", "Vermelho"));
        b.adicionarCarta(criarCarta("Dragão", "Verde"));
        b.adicionarCarta(criarCarta("Dragão", "Cinza"));

        b.embaralhar();
        return b;
    }
}
