package com.projeto.ethnos.model;

import java.util.ArrayList;
import java.util.List;

public class Mercado {
    private List<Carta> cartasAbertas = new ArrayList<>();

    public void adicionarCartas(List<Carta> cartas) {
        this.cartasAbertas.addAll(cartas);
    }

    public Carta comprarCarta() {
        if (!cartasAbertas.isEmpty()) {
            return cartasAbertas.remove(0); 
        }
        return null;
    }

    public Carta comprarCarta(Carta cartaEscolhida) {
        if (cartaEscolhida == null) {
            return null;
        }
        int idx = cartasAbertas.indexOf(cartaEscolhida);
        if (idx < 0) {
            return null;
        }
        return cartasAbertas.remove(idx);
    }

    public List<Carta> getCartasDisponiveis() {
        return cartasAbertas;
    }
}