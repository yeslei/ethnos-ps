package com.projeto.ethnos.model;

import com.projeto.ethnos.model.poder.PoderDoLider;

/**
 * Classe abstrata que representa uma carta do jogo.
 *
 * No diagrama de classes atualizado, Carta aparece em itálico (abstrata),
 * pois cada tribo (Anão, Elfo, Gigante, Mago, Troll, Minotauro, Centauro,
 * Esqueleto, Dragão) é uma subclasse concreta com seu próprio poder.
 *
 * Padrões aplicados nesta hierarquia:
 *  - GoF Strategy: o poder do líder é um objeto PoderDoLider associado à
 *    carta (getPoder()); cada subclasse fornece sua estratégia concreta.
 *  - GoF Factory Method: a criação das subclasses é feita por CartaFactory,
 *    isolando a UI/setup do conhecimento sobre quais classes existem.
 *  - GRASP Polymorphism: o comportamento ativaPoder() é resolvido por
 *    polimorfismo (subclasses sobrescrevem) ao invés de switch por tipo.
 *  - GRASP Information Expert: cada subclasse conhece seu próprio poder.
 */
public abstract class Carta {

    /** Nome legível da carta (geralmente igual à tribo). */
    protected final String nome;

    /** Cor (vermelho, verde, amarelo, azul, roxo, cinza). */
    protected final String cor;

    /** Tribo (Anão, Elfo, Gigante, ...). */
    protected final String tribo;

    protected Carta(String nome, String cor, String tribo) {
        this.nome = nome;
        this.cor = cor;
        this.tribo = tribo;
    }

    /**
     * Retorna a estratégia de poder associada a esta carta.
     * Subclasses devolvem instâncias de PoderDoLider.
     *
     * Por padrão devolvemos null para representar "sem poder" — usado
     * principalmente por cartas especiais como Dragão.
     */
    public PoderDoLider getPoder() {
        return null;
    }

    /**
     * Operação herdada do diagrama de classes original. Mantida por
     * compatibilidade com o diagrama de sequência da Parte 1.
     *
     * A implementação real do efeito está em getPoder().executar(...) e é
     * chamada por Partida durante o turno.
     */
    public void ativaPoder() {
        // Apenas registra no console; o efeito real vem do Strategy.
        System.out.println("Poder ativado: " + tribo);
    }

    /**
     * Texto curto para UI descrevendo o poder da carta.
     */
    public String getDescricaoPoder() {
        return "Sem poder";
    }

    public String getNome() {
        return nome;
    }

    public String getCor() {
        return cor;
    }

    public String getTribo() {
        return tribo;
    }

    @Override
    public String toString() {
        return tribo + " (" + cor + ")";
    }
}
