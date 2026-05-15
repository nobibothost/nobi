package com.gboard2.nobi;

import java.util.HashMap;
import java.util.Map;

public class SwipeSymbolManager {
    public Map<String, String[]> swipeSymbols = new HashMap<>();

    public SwipeSymbolManager() {
        initSwipeSymbols();
    }

    private void initSwipeSymbols() {
        // Array Format: [Center (Hint/Default), Up, Down, Left, Right]

        // --- Standard Symbols (Row 1 & 2) ---
        swipeSymbols.put("@", new String[]{"@", "©", "®", "™", "℅"});
        swipeSymbols.put("#", new String[]{"#", "№", "§", "¶", "⌗"});
        swipeSymbols.put("$", new String[]{"$", "¢", "€", "£", "¥"});
        swipeSymbols.put("%", new String[]{"%", "‰", "‱", "÷", "×"});
        swipeSymbols.put("&", new String[]{"&", "§", "¶", "©", "®"});
        swipeSymbols.put("-", new String[]{"-", "—", "–", "−", "_"});
        swipeSymbols.put("+", new String[]{"+", "±", "×", "÷", "⊕"});
        swipeSymbols.put("(", new String[]{"(", "[", "{", "<", "⟨"});
        swipeSymbols.put(")", new String[]{")", "]", "}", ">", "⟩"});
        
        // --- Address / Punctuation Row ---
        swipeSymbols.put("/", new String[]{"/", "\\", "|", "¦", "÷"});
        swipeSymbols.put("_", new String[]{"_", "-", "—", "–", "·"});
        swipeSymbols.put(".", new String[]{".", "…", "•", "·", "°"});
        swipeSymbols.put(",", new String[]{",", ";", "‚", "„", "’"});
        swipeSymbols.put(":", new String[]{":", ";", "⁝", "⋮", "·"});
        swipeSymbols.put(";", new String[]{";", ":", "⁏", "‚", "„"});
        swipeSymbols.put("'", new String[]{"'", "‘", "’", "‚", "»"});
        swipeSymbols.put("\"", new String[]{"\"", "“", "”", "„", "«"});
        swipeSymbols.put("!", new String[]{"!", "¡", "‼", "❗", "❕"});
        swipeSymbols.put("?", new String[]{"?", "¿", "‽", "❓", "❔"});
        
        // --- Math & Brackets Row ---
        swipeSymbols.put("=\\<", new String[]{"=\\<", "≠", "≈", "≡", "∞"}); 
        swipeSymbols.put("*", new String[]{"*", "★", "☆", "✦", "✧"});
        swipeSymbols.put("\\", new String[]{"\\", "/", "|", "¦", "÷"});
        swipeSymbols.put("|", new String[]{"|", "¦", "/", "\\", "‖"});
        swipeSymbols.put("<", new String[]{"<", "≤", "«", "‹", "⟨"});
        swipeSymbols.put(">", new String[]{">", "≥", "»", "›", "⟩"});
        swipeSymbols.put("[", new String[]{"[", "{", "(", "⟨", "«"});
        swipeSymbols.put("]", new String[]{"]", "}", ")", "⟩", "»"});

        // --- Page 2 Symbols ---
        swipeSymbols.put("~", new String[]{"~", "≈", "≅", "≃", "∼"});
        swipeSymbols.put("`", new String[]{"`", "'", "‘", "’", "‚"});
        swipeSymbols.put("•", new String[]{"•", "○", "●", "□", "■"});
        swipeSymbols.put("√", new String[]{"√", "∛", "∜", "∝", "∞"});
        swipeSymbols.put("π", new String[]{"π", "Π", "Ω", "µ", "∆"});
        swipeSymbols.put("÷", new String[]{"÷", "×", "/", "+", "-"});
        swipeSymbols.put("×", new String[]{"×", "÷", "*", "+", "-"});
        swipeSymbols.put("¶", new String[]{"¶", "§", "©", "®", "™"});
        swipeSymbols.put("∆", new String[]{"∆", "∇", "∤", "∥", "∦"});
        
        swipeSymbols.put("±", new String[]{"±", "+", "-", "∓", "⊕"});
        swipeSymbols.put("≤", new String[]{"≤", "<", "≪", "≦", "≲"});
        swipeSymbols.put("≥", new String[]{"≥", ">", "≫", "≧", "≳"});
        swipeSymbols.put("≠", new String[]{"≠", "=", "≈", "≡", "≄"});
        swipeSymbols.put("≈", new String[]{"≈", "=", "≠", "≡", "≃"});
        swipeSymbols.put("∞", new String[]{"∞", "∝", "∅", "∈", "∉"});
        swipeSymbols.put("µ", new String[]{"µ", "π", "Ω", "∑", "∫"});
        swipeSymbols.put("∑", new String[]{"∑", "∫", "∬", "∭", "∮"});
        swipeSymbols.put("Ω", new String[]{"Ω", "µ", "π", "Ω", "∞"});
        
        swipeSymbols.put("€", new String[]{"€", "£", "$", "¥", "¢"});
        swipeSymbols.put("£", new String[]{"£", "€", "$", "¥", "¢"});
        swipeSymbols.put("¢", new String[]{"¢", "$", "€", "£", "¥"});
        swipeSymbols.put("¥", new String[]{"¥", "€", "£", "$", "¢"});
        swipeSymbols.put("^", new String[]{"^", "↑", "↓", "←", "→"});
        swipeSymbols.put("°", new String[]{"°", "′", "″", "℃", "℉"});

        // --- Number Row Fallbacks (In case user long presses numbers) ---
        swipeSymbols.put("1", new String[]{"1", "¹", "½", "⅓", "¼"});
        swipeSymbols.put("2", new String[]{"2", "²", "⅔", "½", "¾"});
        swipeSymbols.put("3", new String[]{"3", "³", "¾", "⅜", "⅝"});
        swipeSymbols.put("4", new String[]{"4", "⁴", "⅘", "¼", "¾"});
        swipeSymbols.put("5", new String[]{"5", "⁵", "⅝", "⅚", "⅞"});
        swipeSymbols.put("6", new String[]{"6", "⁶", "⅚", "⅙", "⅞"});
        swipeSymbols.put("7", new String[]{"7", "⁷", "⅞", "⅛", "⅜"});
        swipeSymbols.put("8", new String[]{"8", "⁸", "⅜", "⅝", "⅞"});
        swipeSymbols.put("9", new String[]{"9", "⁹", "ⁿ", "∅", "∞"});
        swipeSymbols.put("0", new String[]{"0", "ⁿ", "∅", "°", "‰"});

        // --- EMOJI TO NUMBER MAP (1 to 0) ---
        swipeSymbols.put("😢", new String[]{"1", "¹", "½", "⅓", "¼"});
        swipeSymbols.put("🤤", new String[]{"2", "²", "⅔", "½", "¾"});
        swipeSymbols.put("😒", new String[]{"3", "³", "¾", "⅜", "⅝"});
        swipeSymbols.put("😁", new String[]{"4", "⁴", "⅘", "¼", "¾"});
        swipeSymbols.put("🤭", new String[]{"5", "⁵", "⅝", "⅚", "⅞"});
        swipeSymbols.put("😊", new String[]{"6", "⁶", "⅚", "⅙", "⅞"});
        swipeSymbols.put("🔥", new String[]{"7", "⁷", "⅞", "⅛", "⅜"});
        swipeSymbols.put("🤣", new String[]{"8", "⁸", "⅜", "⅝", "⅞"});
        swipeSymbols.put("😂", new String[]{"9", "⁹", "ⁿ", "∅", "∞"});
        swipeSymbols.put("🙂", new String[]{"0", "ⁿ", "∅", "°", "‰"});
    }
}
