package com.projeto.ethnos.model;

public class Carta {
    public String nome;
    public String cor;
    public String tribo;

    public Carta(String nome, String cor, String tribo) {
        this.nome = nome;
        this.cor = cor;
        this.tribo = tribo;
    }

    public void ativaPoder() {
        System.out.println("Poder ativado!");
    }

    @Override
    public String toString() {
        return tribo + " (" + cor + ")";
    }
}