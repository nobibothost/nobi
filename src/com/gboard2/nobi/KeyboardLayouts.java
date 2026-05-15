package com.gboard2.nobi;

public class KeyboardLayouts {

    // Keyboard Modes
    public static final int MODE_TEXT = 0;
    public static final int MODE_SYMBOLS = 1;
    public static final int MODE_SYMBOLS_PAGE_2 = 2;
    public static final int MODE_NUMBER = 4;

    // Layout Matrices
    public static final String[][] TEXT = {
            {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"},
            {"Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"},
            {"A", "S", "D", "F", "G", "H", "J", "K", "L"},
            {"SHIFT", "Z", "X", "C", "V", "B", "N", "M", "DEL"},
            {"?123", "EMOJI", "SPACE", "LANG", "ENTER"}
    };
    
    public static final String[][] SYMBOLS = {
            {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"},
            {"@", "#", "$", "%", "&", "-", "+", "(", ")"}, 
            {"/", "_", ".", ",", ":", ";", "'", "\"", "!", "?"},
            {"=\\<", "*", "\\", "|", "<", ">", "[", "]", "DEL"},
            {"ABC", ",", "SPACE", ".", "LANG", "ENTER"}
    };

    public static final String[][] SYMBOLS_PAGE_2 = {
            {"~", "`", "|", "•", "√", "π", "÷", "×", "¶", "∆"},
            {"±", "≤", "≥", "≠", "≈", "∞", "µ", "∑", "Ω"},
            {"/", "_", ".", ",", ":", ";", "'", "\"", "!", "?"},
            {"?123", "€", "£", "¢", "¥", "^", "°", "=", "DEL"},
            {"ABC", ",", "SPACE", ".", "LANG", "ENTER"}
    };

    public static final String[][] NUMBER = {
            {"1", "2", "3", "-"},
            {"4", "5", "6", "SPACE"},
            {"7", "8", "9", "DEL"},
            {",", "0", ".", "ENTER"}
    };

    // Helper Methods
    public static float getKeyWeight(String label, int currentMode) {
        if (currentMode == MODE_NUMBER) return 1.0f;
        switch (label) {
            case "SPACE": return 4.8f;
            case "ENTER": return 1.6f;
            case "SHIFT": case "DEL": return 1.3f;
            case "?123": case "ABC": case "LANG": case "EMOJI": case "=\\<": return 1.2f;
            default: return 1.0f;
        }
    }

    public static String getDialerLetters(String digit) {
        switch(digit) {
            case "0": return "+";
            case "2": return "ABC"; case "3": return "DEF"; case "4": return "GHI"; case "5": return "JKL";
            case "6": return "MNO";
            case "7": return "PQRS"; case "8": return "TUV"; case "9": return "WXYZ";
            default: return null;
        }
    }

    // New Helper Method for Long Press Popups
    public static String[] getPopups(String label) {
        if (label == null) return null;
        switch (label) {
            // Numbers
            case "1": return new String[]{"¹", "½", "⅓", "¼", "⅛"};
            case "2": return new String[]{"²", "⅔"};
            case "3": return new String[]{"³", "¾", "⅜"};
            case "4": return new String[]{"⁴", "⅘"};
            case "5": return new String[]{"⁵", "⅝"};
            case "6": return new String[]{"⁶", "⅚"};
            case "7": return new String[]{"⁷", "⅞"};
            case "8": return new String[]{"⁸"};
            case "9": return new String[]{"⁹"};
            case "0": return new String[]{"ⁿ", "∅"};
            
            // Symbols
            case "-": return new String[]{"_", "—", "–", "·"};
            case "/": return new String[]{"\\", "|"};
            case "$": return new String[]{"¢", "£", "€", "¥", "₱", "₹"};
            case "&": return new String[]{"§"};
            case "+": return new String[]{"±"};
            case "(": return new String[]{"<", "[", "{"};
            case ")": return new String[]{">", "]", "}"};
            case "\"": return new String[]{"”", "“", "„", "»", "«"};
            case "'": return new String[]{"’", "‘", "‚", "‹", "›"};
            case "*": return new String[]{"★", "†", "‡"};
            case "!": return new String[]{"¡"};
            case "?": return new String[]{"¿"};
            case "%": return new String[]{"‰"};
            case "=": return new String[]{"≠", "≈", "∞"};
            case ".": return new String[]{"…"};
            case "•": return new String[]{"○", "●", "□", "■", "♤", "♡", "◇", "♧"};
            case "π": return new String[]{"Π"};
            case "¶": return new String[]{"§"};
            case "<": return new String[]{"≤", "«", "‹"};
            case ">": return new String[]{"≥", "»", "›"};
            case "[": return new String[]{"{"};
            case "]": return new String[]{"}"};
            
            // Default Fallback
            default: return null;
        }
    }
}
