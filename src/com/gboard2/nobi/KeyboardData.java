package com.gboard2.nobi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeyboardData {
    public static final int MODE_TEXT = 0;
    public static final int MODE_SYMBOLS = 1;
    public static final int MODE_SYMBOLS_PAGE_2 = 2;
    public static final int MODE_NUMBER = 4;

    public static final String[] TOP_ROW_SLANG = {"AWESOME", "HAHHAAHHA", "LMAO", "ACHA", "JI", "ESA KYA", "HAA", "YRR", "HMMMM HMMMM", "OHK", "LOVE YOU"};
    public static final String[] TOP_ROW_EMOJIS = {"😥", "🤤", "😒", "😁", "🤭", "😊", "🔥", "🤣", "😂", "🙂"};
    public static final Map<String, String[]> SWIPE_EMOJIS = new HashMap<>();

    static {
        // [Center (Hint), Up, Down, Left, Right]
        SWIPE_EMOJIS.put("Q", new String[]{"😍", "😻", "😽", "💕", "💖"});
        SWIPE_EMOJIS.put("W", new String[]{"🤩", "🌟", "✨", "💫", "🥳"});
        SWIPE_EMOJIS.put("E", new String[]{"😘", "😗", "😙", "😚", "💋"});
        SWIPE_EMOJIS.put("R", new String[]{"🥰", "💘", "💝", "💗", "💓"});
        SWIPE_EMOJIS.put("T", new String[]{"😇", "👼", "🕊️", "🤍", "✨"});
        SWIPE_EMOJIS.put("Y", new String[]{"😋", "😛", "😝", "😜", "🤪"});
        SWIPE_EMOJIS.put("U", new String[]{"🤗", "🫶", "🤝", "👐", "🤲"});
        SWIPE_EMOJIS.put("I", new String[]{"🤔", "🧐", "🤨", "💭", "🧠"});
        SWIPE_EMOJIS.put("O", new String[]{"😌", "😪", "🤤", "🥱", "😴"});
        SWIPE_EMOJIS.put("P", new String[]{"😱", "😨", "😰", "😥", "😓"});
        
        SWIPE_EMOJIS.put("A", new String[]{"✊", "👊", "🤛", "🤜", "✋"});
        SWIPE_EMOJIS.put("S", new String[]{"😼", "😸", "😹", "😺", "😽"});
        SWIPE_EMOJIS.put("D", new String[]{"❤️", "🩷", "🧡", "💛", "💚"});
        SWIPE_EMOJIS.put("F", new String[]{"😭", "😢", "💧", "💦", "🌧️"});
        SWIPE_EMOJIS.put("G", new String[]{"😣", "😖", "😫", "😩", "🥺"});
        SWIPE_EMOJIS.put("H", new String[]{"👍", "👎", "👏", "🙌", "🤝"});
        SWIPE_EMOJIS.put("J", new String[]{"💔", "❤️‍🩹", "🖤", "🩶", "🤍"});
        SWIPE_EMOJIS.put("K", new String[]{"🤏", "🤌", "✌️", "🤞", "🫰"});
        SWIPE_EMOJIS.put("L", new String[]{"🥱", "😪", "😴", "💤", "🛌"});
        
        SWIPE_EMOJIS.put("Z", new String[]{"😤", "😮‍💨", "💨", "😾", "👿"});
        SWIPE_EMOJIS.put("X", new String[]{"😠", "😡", "🤬", "😾", "💢"});
        SWIPE_EMOJIS.put("C", new String[]{"😡", "🤬", "👹", "👺", "💥"});
        SWIPE_EMOJIS.put("V", new String[]{"🧑‍🦰", "👩‍🦰", "👨‍🦰", "👱", "🧔"});
        SWIPE_EMOJIS.put("B", new String[]{"😏", "😒", "🙄", "😬", "😑"});
        SWIPE_EMOJIS.put("N", new String[]{"😎", "🥸", "🤓", "🧐", "🤠"});
        SWIPE_EMOJIS.put("M", new String[]{"🙈", "🙉", "🙊", "🐵", "🐒"});
    }

    public static String[][] getDynamicLayout(int currentMode, boolean showSlangRow, boolean showEmojiRow, boolean showNumberRow, boolean replaceEmojiInSymbols) {
        List<String[]> dynamicLayout = new ArrayList<>();
        boolean currShowEmoji = showEmojiRow;
        boolean currShowNum = showNumberRow;

        if (replaceEmojiInSymbols && (currentMode == MODE_SYMBOLS || currentMode == MODE_SYMBOLS_PAGE_2)) {
            currShowEmoji = false;
            currShowNum = true;
        }

        if (currentMode == MODE_NUMBER) {
            dynamicLayout.add(new String[]{"1", "2", "3", "-"});
            dynamicLayout.add(new String[]{"4", "5", "6", "SPACE"});
            dynamicLayout.add(new String[]{"7", "8", "9", "DEL"});
            dynamicLayout.add(new String[]{",", "0", ".", "ENTER"});
        } else {
            if (showSlangRow) dynamicLayout.add(TOP_ROW_SLANG);
            if (currShowEmoji) dynamicLayout.add(TOP_ROW_EMOJIS);
            if (currShowNum) {
                dynamicLayout.add(new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"});
            }
            
            if (currentMode == MODE_TEXT) {
                dynamicLayout.add(new String[]{"Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"});
                dynamicLayout.add(new String[]{"A", "S", "D", "F", "G", "H", "J", "K", "L"});
                dynamicLayout.add(new String[]{"SHIFT", "Z", "X", "C", "V", "B", "N", "M", "DEL"});
                dynamicLayout.add(new String[]{"?123", "EMOJI", "SPACE", "ENTER"});
            } else if (currentMode == MODE_SYMBOLS) {
                dynamicLayout.add(new String[]{"@", "#", "$", "_", "&", "-", "+", "(", ")", "/"});
                dynamicLayout.add(new String[]{"=\\<", "*", "\"", "'", ":", ";", "!", "?", "DEL"});
                dynamicLayout.add(new String[]{"ABC", ",", "SPACE", ".", "LANG", "ENTER"});
            } else if (currentMode == MODE_SYMBOLS_PAGE_2) {
                dynamicLayout.add(new String[]{"~", "`", "|", "•", "√", "π", "÷", "×", "¶", "∆"});
                dynamicLayout.add(new String[]{"£", "¢", "€", "¥", "^", "°", "=", "{", "}", "\\"});
                dynamicLayout.add(new String[]{"?123", "%", "©", "®", "™", "✓", "[", "]", "DEL"});
                dynamicLayout.add(new String[]{"ABC", ",", "SPACE", ".", "LANG", "ENTER"});
            }
        }
        return dynamicLayout.toArray(new String[0][]);
    }

    public static float getKeyWeight(String label, int currentMode) {
        if (currentMode == MODE_NUMBER) return 1.0f;
        if (Arrays.asList(TOP_ROW_SLANG).contains(label)) return 0.9f;
        if (Arrays.asList(TOP_ROW_EMOJIS).contains(label)) return 1.0f; // Adjusted to perfectly fill width for 10 keys
        
        switch (label) {
            case "SPACE": return (currentMode == MODE_TEXT) ? 6.2f : 4.2f;
            case "ENTER": case "ABC": return (currentMode == MODE_TEXT) ? 1.8f : 1.6f; 
            case "?123": return 1.8f;
            case "SHIFT": case "DEL": case "=\\<": return 1.6f;
            case "LANG": return 1.0f;
            case "EMOJI": return 1.2f;
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
}
