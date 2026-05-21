package com.projeto.ethnos.view;

import javafx.scene.paint.Color;

import java.util.Locale;

/**
 * Shared palette to keep region and card colors consistent.
 */
public final class UiPalette {

    private UiPalette() {
    }

    public static String getRegionBackground(String cor) {
        switch (normalize(cor)) {
            case "vermelho": return "#C76B64";
            case "verde": return "#6E8C6A";
            case "amarelo": return "#C9AE63";
            case "azul": return "#6B88B8";
            case "roxo": return "#7B6D9A";
            case "cinza": return "#9B9B9B";
            default: return "#E6DCC7";
        }
    }

    public static Color getCardStripe(String cor) {
        return Color.web(getRegionBackground(cor));
    }

    public static String getParchmentBackground() {
        return "#E8DDC8";
    }

    public static String getWoodBorder() {
        return "#6B4E2E";
    }

    private static String normalize(String cor) {
        if (cor == null) return "";
        return cor.toLowerCase(Locale.ROOT).trim();
    }
}
