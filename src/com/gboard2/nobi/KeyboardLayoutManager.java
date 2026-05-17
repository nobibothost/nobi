package com.gboard2.nobi;

import android.graphics.RectF;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KeyboardLayoutManager {

    private ProKeyboardView pkv;

    public KeyboardLayoutManager(ProKeyboardView pkv) {
        this.pkv = pkv;
    }

    public float getKeyWeight(String label) {
        if (pkv.currentMode == ProKeyboardView.MODE_NUMBER) return 1.0f;
        
        CustomKeyManager ckm = CustomKeyManager.getInstance(pkv.getContext());
        String effLabel = ckm.getLabel(label);
        if (effLabel == null || effLabel.isEmpty()) effLabel = label;

        if (Arrays.asList(KeyboardData.TOP_ROW_SLANG).contains(label)) return 0.9f;
        if (Arrays.asList(KeyboardData.TOP_ROW_EMOJIS).contains(label)) return 1.0f;
        
        switch (effLabel) {
            case "SPACE": 
                return (pkv.currentMode == ProKeyboardView.MODE_TEXT) ? 6.2f : 4.2f;
            case "ENTER": 
                return (pkv.currentMode == ProKeyboardView.MODE_TEXT) ? 1.8f : 1.6f; 
            case "ABC": 
                return 1.6f;
            case "?123": 
                return 1.8f;
            case "SHIFT": case "DEL": case "=\\<": 
                return 1.6f;
            case "LANG": 
                return 1.0f;
            case "EMOJI": 
                return 1.2f;
            default: 
                return 1.0f;
        }
    }

    public void buildKeys() {
        if (pkv.getWidth() == 0 || pkv.getHeight() == 0) return;
        pkv.keys.clear();

        List<String[]> dynamicLayout = new ArrayList<>();
        
        boolean currShowEmoji = pkv.themeManager.showEmojiRow;
        boolean currShowSlang = pkv.themeManager.showSlangRow;
        boolean currShowNum = pkv.themeManager.showNumberRow;

        if (pkv.themeManager.replaceEmojiInSymbols && (pkv.currentMode == ProKeyboardView.MODE_SYMBOLS || pkv.currentMode == ProKeyboardView.MODE_SYMBOLS_PAGE_2)) {
            currShowNum = true;
        }
        
        if (pkv.currentMode == ProKeyboardView.MODE_NUMBER) {
            dynamicLayout.add(new String[]{"1", "2", "3", "-"});
            dynamicLayout.add(new String[]{"4", "5", "6", "SPACE"});
            dynamicLayout.add(new String[]{"7", "8", "9", "DEL"});
            dynamicLayout.add(new String[]{",", "0", ".", "ENTER"});
        } else {
            if (currShowSlang) {
                dynamicLayout.add(KeyboardData.TOP_ROW_SLANG);
            }
            if (currShowEmoji) {
                dynamicLayout.add(KeyboardData.TOP_ROW_EMOJIS);
            }
            
            if (currShowNum) {
                dynamicLayout.add(new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"});
            }
            
            if (pkv.currentMode == ProKeyboardView.MODE_TEXT) {
                dynamicLayout.add(new String[]{"Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"});
                dynamicLayout.add(new String[]{"A", "S", "D", "F", "G", "H", "J", "K", "L"});
                dynamicLayout.add(new String[]{"SHIFT", "Z", "X", "C", "V", "B", "N", "M", "DEL"});
                dynamicLayout.add(new String[]{"?123", "EMOJI", "SPACE", "ENTER"});
            } 
            else if (pkv.currentMode == ProKeyboardView.MODE_SYMBOLS) {
                if (!currShowNum) {
                    dynamicLayout.add(new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"});
                } else {
                    dynamicLayout.add(new String[]{"~", "`", "|", "•", "√", "π", "÷", "×", "¶", "∆"});
                }
                dynamicLayout.add(new String[]{"@", "#", "$", "%", "&", "-", "+", "(", ")", "/"});
                dynamicLayout.add(new String[]{"_", ".", ",", ":", ";", "'", "\"", "!", "?"});
                dynamicLayout.add(new String[]{"=\\<", "*", "\\", "|", "<", ">", "[", "]", "DEL"});
                dynamicLayout.add(new String[]{"ABC", ",", "SPACE", ".", "LANG", "ENTER"});
            } 
            else if (pkv.currentMode == ProKeyboardView.MODE_SYMBOLS_PAGE_2) {
                if (!currShowNum) {
                    dynamicLayout.add(new String[]{"~", "`", "|", "•", "√", "π", "÷", "×", "¶", "∆"});
                } else {
                    dynamicLayout.add(new String[]{"©", "®", "™", "✓", "[", "]", "{", "}", "<", ">"});
                }
                dynamicLayout.add(new String[]{"±", "≤", "≥", "≠", "≈", "∞", "µ", "∑", "Ω", "/"});
                dynamicLayout.add(new String[]{"_", ".", ",", ":", ";", "'", "\"", "!", "?"});
                dynamicLayout.add(new String[]{"?123", "€", "£", "¢", "¥", "^", "°", "=", "DEL"});
                dynamicLayout.add(new String[]{"ABC", ",", "SPACE", ".", "LANG", "ENTER"});
            }
        }
        
        String[][] layout = dynamicLayout.toArray(new String[0][]);

        float padding = pkv.dpToPx(5);
        float suggestionStripHeight = pkv.getToolbarHeight();
        float remainingHeight = pkv.getHeight() - suggestionStripHeight;
        
        int rowCount = layout.length;
        int mainRowCount = (pkv.currentMode == ProKeyboardView.MODE_NUMBER) ? rowCount : (pkv.currentMode == ProKeyboardView.MODE_TEXT ? 4 : 5);
        int extraRowCount = Math.max(0, rowCount - mainRowCount);
        
        float totalHeightWeight = 0;
        for (int row = 0; row < rowCount; row++) {
            if (row < extraRowCount) {
                totalHeightWeight += 0.7f;
            } else {
                totalHeightWeight += 1.0f;
            }
        }
        
        float baseRowHeight = (remainingHeight - (padding * (rowCount + 1))) / totalHeightWeight;
        float baseKeyWidth = (pkv.getWidth() - (padding * 11)) / 10f;
        if (pkv.currentMode == ProKeyboardView.MODE_NUMBER) {
            baseKeyWidth = (pkv.getWidth() - (padding * 5)) / 4f;
        }

        float currentY = suggestionStripHeight + padding;
        for (int row = 0; row < rowCount; row++) {
            String[] rowKeys = layout[row];
            float totalWeight = 0;
            for (String key : rowKeys) totalWeight += getKeyWeight(key);
            float totalRowWidth = (totalWeight * baseKeyWidth) + (padding * (rowKeys.length - 1));
            float currentX = (pkv.getWidth() - totalRowWidth) / 2f;
            float currentRowHeight = (row < extraRowCount) ? (baseRowHeight * 0.7f) : baseRowHeight;
            
            for (String label : rowKeys) {
                float keyWidth = baseKeyWidth * getKeyWeight(label);
                // 100% PURE: No prefix hacks. Just the original physical label.
                RectF bounds = new RectF(currentX, currentY, currentX + keyWidth, currentY + currentRowHeight);
                pkv.keys.add(new KeyData(label, bounds));
                currentX += keyWidth + padding;
            }
            currentY += currentRowHeight + padding;
        }
        pkv.postInvalidateOnAnimation();
    }
}