package com.gboard2.nobi;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.inputmethodservice.InputMethodService;
import android.os.SystemClock;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GboardService extends InputMethodService {

    private ProKeyboardView keyboardView;
    private long lastSpaceTime = 0;
    private boolean isAutoShifted = false;
    private boolean forceLowercase = false; 
    private ClipboardManager clipboardManager;
    private ClipboardManager.OnPrimaryClipChangedListener clipListener;
    private boolean isSelectModeOn = false; 
    
    private SmartDictionary smartDictionary;
    private String lastLearnedWord = "";
    private String previousLearnedWord = "";
    private String potentialMistake = ""; 
    
    private String normalizeFontText(String text) {
        if (text == null) return "";
        String normalized = java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFKD);
        normalized = normalized.replaceAll("\\p{M}", ""); 
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < normalized.length(); i++) {
            char c = normalized.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '@' || c == '.') {
                sb.append(c);
            }
        }
        return sb.toString();
    }
    
    private class SmartDictionary {
        private SharedPreferences prefs;
        private SharedPreferences correctionPrefs; 
        private Map<String, Integer> dictionary = new HashMap<>();
        private Context context;
        
        private class FuzzyMatch {
            String word;
            int distance;
            int freq;
            FuzzyMatch(String w, int d, int f) {
                this.word = w;
                this.distance = d;
                this.freq = f;
            }
        }

        public SmartDictionary(Context context) {
            this.context = context;
            prefs = context.getSharedPreferences("GboardPersonalDict", Context.MODE_PRIVATE);
            correctionPrefs = context.getSharedPreferences("GboardCorrections", Context.MODE_PRIVATE);
            loadDictionary();
        }

        private void loadDictionary() {
            Map<String, ?> allEntries = prefs.getAll();
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                if (entry.getValue() instanceof Integer) {
                    dictionary.put(entry.getKey(), (Integer) entry.getValue());
                }
            }
        }

        public void learnWord(String rawWord, String rawPrevWord) {
            if (rawWord == null || rawWord.trim().length() <= 1) return;
            String word = normalizeFontText(rawWord).toLowerCase().trim();
            
            if (word.contains("@") || word.matches(".*(http|www|\\.[a-z]{2,}).*")) {
                return;
            }
            if (!word.matches("[a-z]+")) return;
            
            int count = dictionary.containsKey(word) ? dictionary.get(word) : 0;
            dictionary.put(word, count + 1);
            prefs.edit().putInt(word, count + 1).apply();
            GlobalCloudSync.pushWordToServer(context, word, false);
            
            if (rawPrevWord != null && rawPrevWord.length() > 0) {
                String prevWord = normalizeFontText(rawPrevWord).toLowerCase().trim();
                if (prevWord.matches("[a-z]+")) {
                    String bigram = prevWord + " " + word;
                    int bCount = dictionary.containsKey(bigram) ? dictionary.get(bigram) : 0;
                    dictionary.put(bigram, bCount + 1);
                    prefs.edit().putInt(bigram, bCount + 1).apply();
                    GlobalCloudSync.pushWordToServer(context, bigram, false);
                }
            }
        }

        public void learnCorrection(String mistake, String correction) {
            if (mistake == null || correction == null) return;
            String m = normalizeFontText(mistake).toLowerCase().trim();
            String c = normalizeFontText(correction).toLowerCase().trim();
            
            if (m.length() > 1 && c.length() > 1 && !m.equals(c) && m.matches("[a-z]+") && c.matches("[a-z]+")) {
                int dist = levenshtein(m, c);
                if (dist > 0 && dist <= 3) {
                    correctionPrefs.edit().putString(m, c).apply();
                }
            }
        }

        public void unlearnWord(String rawWord) {
            if (rawWord == null || rawWord.trim().length() <= 1) return;
            String word = normalizeFontText(rawWord).toLowerCase().trim();
            if (dictionary.containsKey(word)) {
                int count = dictionary.get(word);
                if (count > 1) {
                    dictionary.put(word, count - 1);
                    prefs.edit().putInt(word, count - 1).apply();
                } else {
                    dictionary.remove(word);
                    prefs.edit().remove(word).apply();
                }
            }
        }

        private int levenshtein(String a, String b) {
            int[][] dp = new int[a.length() + 1][b.length() + 1];
            for (int i = 0; i <= a.length(); i++) {
                for (int j = 0; j <= b.length(); j++) {
                    if (i == 0) dp[i][j] = j;
                    else if (j == 0) dp[i][j] = i;
                    else {
                        int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                        dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
                    }
                }
            }
            return dp[a.length()][b.length()];
        }

        private String formatWord(String word, boolean forceCaps, String originalPrefix) {
            if (forceCaps) return word.toUpperCase();
            if (originalPrefix.length() > 0 && Character.isUpperCase(originalPrefix.charAt(0))) {
                if (word.length() > 0) return word.substring(0, 1).toUpperCase() + word.substring(1);
            }
            return word;
        }

        public List<String> getSuggestions(String rawPrefix, String rawPrevWord, boolean forceCaps) {
            List<String> suggestions = new ArrayList<>();
            String originalPrefix = normalizeFontText(rawPrefix);
            String prefix = originalPrefix.toLowerCase().trim();
            String prevWord = normalizeFontText(rawPrevWord).toLowerCase().trim();
            
            if (originalPrefix.contains("@") && !originalPrefix.contains(".com")) {
                String[] parts = originalPrefix.split("@");
                String namePart = parts.length > 0 ? parts[0] : "";
                suggestions.add(namePart + "@yahoo.com");  
                suggestions.add(namePart + "@gmail.com");  
                suggestions.add(namePart + "@outlook.com");
                return suggestions;
            }

            if (prefix.length() == 0 && prevWord.length() > 0) {
                String targetBigramPrefix = prevWord + " ";
                List<Map.Entry<String, Integer>> nextWords = new ArrayList<>();
                for (Map.Entry<String, Integer> entry : dictionary.entrySet()) {
                    if (entry.getKey().startsWith(targetBigramPrefix) && !entry.getKey().equals(targetBigramPrefix)) {
                        nextWords.add(entry);
                    }
                }
                Collections.sort(nextWords, new Comparator<Map.Entry<String, Integer>>() {
                    public int compare(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b) {
                        return b.getValue().compareTo(a.getValue());
                    }
                });
                
                List<String> nextCandidates = new ArrayList<>();
                for (Map.Entry<String, Integer> e : nextWords) {
                    String predictedWord = e.getKey().substring(targetBigramPrefix.length());
                    nextCandidates.add(formatWord(predictedWord, forceCaps, originalPrefix));
                }
                
                String left = "", center = "", right = "";
                if (nextCandidates.size() >= 1) center = nextCandidates.get(0);
                if (nextCandidates.size() >= 2) left = nextCandidates.get(1);
                if (nextCandidates.size() >= 3) right = nextCandidates.get(2);
                
                suggestions.add(left);
                suggestions.add(center);
                suggestions.add(right);
                return suggestions;
            }

            if (prefix.length() == 0) {
                return suggestions;
            }

            List<String> bestCandidates = new ArrayList<>();
            String learnedCorrection = correctionPrefs.getString(prefix, null);
            if (learnedCorrection != null) {
                String formattedCorrection = formatWord(learnedCorrection, forceCaps, originalPrefix);
                if (!bestCandidates.contains(formattedCorrection)) {
                    bestCandidates.add(formattedCorrection);
                }
            }

            List<Map.Entry<String, Integer>> exactMatches = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : dictionary.entrySet()) {
                if (!entry.getKey().contains(" ") && entry.getKey().startsWith(prefix)) {
                    exactMatches.add(entry);
                }
            }
            Collections.sort(exactMatches, new Comparator<Map.Entry<String, Integer>>() {
                public int compare(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b) {
                    return b.getValue().compareTo(a.getValue());
                }
            });
            
            for (Map.Entry<String, Integer> exactMatch : exactMatches) {
                String formatted = formatWord(exactMatch.getKey(), forceCaps, originalPrefix);
                if (!bestCandidates.contains(formatted)) {
                    bestCandidates.add(formatted);
                }
            }

            if (bestCandidates.size() < 3 && prefix.length() >= 2) {
                List<FuzzyMatch> fuzzyMatches = new ArrayList<>();
                for (Map.Entry<String, Integer> entry : dictionary.entrySet()) {
                    String dictWord = entry.getKey();
                    if (dictWord.contains(" ")) continue; 
                    if (Math.abs(dictWord.length() - prefix.length()) <= 2) {
                        int dist = levenshtein(prefix, dictWord);
                        if (dist <= 2) { 
                            fuzzyMatches.add(new FuzzyMatch(dictWord, dist, entry.getValue()));
                        }
                    }
                }
                Collections.sort(fuzzyMatches, new Comparator<FuzzyMatch>() {
                    public int compare(FuzzyMatch a, FuzzyMatch b) {
                        if (a.distance != b.distance) return Integer.compare(a.distance, b.distance);
                        return Integer.compare(b.freq, a.freq);
                    }
                });
                
                for (FuzzyMatch fm : fuzzyMatches) {
                    String formatted = formatWord(fm.word, forceCaps, originalPrefix);
                    if (!bestCandidates.contains(formatted)) {
                        bestCandidates.add(formatted);
                    }
                }
            }

            String typed = formatWord(originalPrefix, forceCaps, originalPrefix);
            String left = "", center = "", right = "";
            
            if (bestCandidates.isEmpty()) {
                center = typed;
            } else {
                center = bestCandidates.get(0);
                List<String> remaining = new ArrayList<>();
                for (String c : bestCandidates) {
                    if (!c.equalsIgnoreCase(center)) remaining.add(c);
                }
                if (typed.equalsIgnoreCase(center)) {
                    if (remaining.size() > 0) left = remaining.get(0);
                    if (remaining.size() > 1) right = remaining.get(1);
                } else {
                    left = typed;
                    if (remaining.size() > 0) right = remaining.get(0);
                }
            }
            suggestions.add(left);
            suggestions.add(center);
            suggestions.add(right);
            return suggestions;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        smartDictionary = new SmartDictionary(this);
        clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipListener = new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                if (clipboardManager.hasPrimaryClip()) {
                    ClipData clip = clipboardManager.getPrimaryClip();
                    if (clip != null && clip.getItemCount() > 0) {
                        CharSequence text = clip.getItemAt(0).getText();
                        if (!TextUtils.isEmpty(text)) {
                            if (keyboardView != null) {
                                keyboardView.addCopiedText(text.toString());
                            } else {
                                try {
                                    ClipboardRepository repo = new ClipboardRepository(GboardService.this);
                                    repo.addItem(text.toString());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        };
        clipboardManager.addPrimaryClipChangedListener(clipListener);
        checkAndSyncOnInstallOrUpdate();
    }
    
    private void checkAndSyncOnInstallOrUpdate() {
        try {
            SharedPreferences prefs = getSharedPreferences("GboardAppInfo", Context.MODE_PRIVATE);
            int savedVersion = prefs.getInt("app_version_code", -1);
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            int currentVersion = pInfo.versionCode;
            
            if (savedVersion != currentVersion) {
                GlobalCloudSync.downloadDictionaryUpdate(this, true);
                prefs.edit().putInt("app_version_code", currentVersion).apply();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (clipboardManager != null && clipListener != null) {
            clipboardManager.removePrimaryClipChangedListener(clipListener);
        }
    }

    @Override
    public View onCreateInputView() {
        keyboardView = new ProKeyboardView(this);
        keyboardView.setKeyboardListener(new ProKeyboardView.KeyboardListener() {
            @Override
            public void onKeyClick(String rawKey) {
                if (rawKey == null) return;
                
                InputConnection ic = getCurrentInputConnection();
                if (ic == null) return;

                // --- CUSTOM KEYCAFE ADVANCED LOGIC WITH NORMALIZATION ---
                CustomKeyManager ckm = CustomKeyManager.getInstance(GboardService.this);
                String actionType = ckm.getActionType(rawKey);
                String customValue = ckm.getValue(rawKey);

                if (actionType != null && !actionType.equals("DEFAULT")) {
                    switch (actionType) {
                        case "WRITE":
                            if (customValue != null && !customValue.isEmpty()) ic.commitText(customValue, 1);
                            break;
                        case "DELETE":
                        case "BACKSPACE":
                            GboardService.this.sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
                            break;
                        case "SPACE":
                            ic.commitText(" ", 1);
                            break;
                        case "ENTER":
                            EditorInfo editorInfo = getCurrentInputEditorInfo();
                            int action = editorInfo.imeOptions & EditorInfo.IME_MASK_ACTION;
                            boolean isMultiLine = (editorInfo.inputType & InputType.TYPE_TEXT_FLAG_MULTI_LINE) != 0;
                            if (isMultiLine || editorInfo.inputType == InputType.TYPE_NULL || action == EditorInfo.IME_ACTION_NONE || action == EditorInfo.IME_ACTION_UNSPECIFIED) {
                                ic.commitText("\n", 1);
                            } else {
                                ic.performEditorAction(action);
                            }
                            break;
                        case "SELECT_ALL":
                            ic.performContextMenuAction(android.R.id.selectAll);
                            break;
                        case "COPY":
                            ic.performContextMenuAction(android.R.id.copy);
                            break;
                        case "CUT":
                            ic.performContextMenuAction(android.R.id.cut);
                            break;
                        case "PASTE":
                            ic.performContextMenuAction(android.R.id.paste);
                            break;
                        case "SEARCH":
                            ic.performEditorAction(EditorInfo.IME_ACTION_SEARCH);
                            break;
                    }
                    updateAutoCaps(ic);
                    return;
                }

                SharedPreferences prefs = getSharedPreferences("GboardPrefs", Context.MODE_PRIVATE);
                String savedFontName = prefs.getString("selected_font", "Default");
                FontData.FontStyle currentFont = FontData.getFontByName(savedFontName);
                
                String cleanCmd = rawKey.trim().toUpperCase();

                boolean isSpace = cleanCmd.equals("SPACE") || cleanCmd.equals("SPAC") || rawKey.equals(" ") || rawKey.equals(FontData.convertText("SPACE", currentFont)) || rawKey.equals(FontData.convertText("space", currentFont)) || rawKey.equals(FontData.convertText("SPAC", currentFont));
                boolean isDel = cleanCmd.equals("DEL") || cleanCmd.equals("DELETE") || cleanCmd.equals("DELE") || cleanCmd.equals("DEL_BATCH") || rawKey.equals(FontData.convertText("DEL", currentFont)) || rawKey.equals(FontData.convertText("del", currentFont)) || rawKey.equals(FontData.convertText("DELETE", currentFont)) || rawKey.equals(FontData.convertText("DELE", currentFont));
                boolean isEnter = cleanCmd.equals("ENTER") || cleanCmd.equals("RETURN") || cleanCmd.equals("ENTE") || rawKey.equals("\n") || rawKey.equals(FontData.convertText("ENTER", currentFont)) || rawKey.equals(FontData.convertText("enter", currentFont));
                boolean isShift = cleanCmd.equals("SHIFT") || cleanCmd.equals("CAPS") || cleanCmd.equals("SHIF") || rawKey.equals(FontData.convertText("SHIFT", currentFont)) || rawKey.equals(FontData.convertText("shift", currentFont));
                boolean isLang = cleanCmd.equals("LANG") || rawKey.equals(FontData.convertText("LANG", currentFont)) || rawKey.equals(FontData.convertText("lang", currentFont));
                boolean isPageCmd = cleanCmd.equals("?123") || cleanCmd.equals("ABC") || cleanCmd.equals("EMOJI") || cleanCmd.equals("=\\<") || cleanCmd.equals("1/3") || cleanCmd.equals("2/3") || cleanCmd.equals("3/3") || cleanCmd.equals("1/2") || cleanCmd.equals("2/2");

                if (!isShift) {
                    forceLowercase = false;
                }

                if (isShift) {
                    if (isAutoShifted || keyboardView.isShifted) {
                        isAutoShifted = false;
                        forceLowercase = true;
                        keyboardView.setShifted(false);
                    } else {
                        forceLowercase = false;
                        keyboardView.setShifted(true);
                    }
                    return;
                }

                if (isSpace) {
                    String wordToLearn = getWordBeforeCursor(ic);
                    if (!TextUtils.isEmpty(wordToLearn)) {
                        smartDictionary.learnWord(wordToLearn, previousLearnedWord);
                        if (!TextUtils.isEmpty(potentialMistake) && !potentialMistake.equalsIgnoreCase(wordToLearn)) {
                            smartDictionary.learnCorrection(potentialMistake, wordToLearn);
                        }
                        potentialMistake = "";
                        previousLearnedWord = wordToLearn;
                        lastLearnedWord = wordToLearn; 
                    }
                    long now = System.currentTimeMillis();
                    CharSequence beforeCursor = ic.getTextBeforeCursor(1, 0);
                    if (now - lastSpaceTime < 400 && beforeCursor != null && beforeCursor.toString().equals(" ")) {
                        ic.deleteSurroundingText(1, 0);
                        ic.commitText(". ", 1);
                        lastSpaceTime = 0; 
                    } else {
                        ic.commitText(" ", 1);
                        lastSpaceTime = now;
                    }
                    updateAutoCaps(ic);
                    return; 
                }

                if (isDel) {
                    if (TextUtils.isEmpty(potentialMistake)) {
                        String wordBeforeDel = getWordBeforeCursor(ic);
                        if (!TextUtils.isEmpty(wordBeforeDel)) {
                            potentialMistake = wordBeforeDel;
                        }
                    }

                    CharSequence beforeCursorCheck = ic.getTextBeforeCursor(50, 0);
                    if (beforeCursorCheck != null && !TextUtils.isEmpty(lastLearnedWord)) {
                        String textBefore = beforeCursorCheck.toString();
                        if (textBefore.endsWith(lastLearnedWord + " ") || textBefore.endsWith(lastLearnedWord)) {
                            smartDictionary.unlearnWord(lastLearnedWord);
                            lastLearnedWord = ""; 
                            previousLearnedWord = "";
                        }
                    }
                    CharSequence selectedText = ic.getSelectedText(0);
                    if (TextUtils.isEmpty(selectedText)) {
                        GboardService.this.sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
                        if(cleanCmd.equals("DEL_BATCH")) GboardService.this.sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
                    } else {
                        ic.commitText("", 1);
                    }
                    updateAutoCaps(ic);
                    return; 
                }

                if (isEnter) {
                    String wordToLearn = getWordBeforeCursor(ic);
                    if (!TextUtils.isEmpty(wordToLearn)) {
                        smartDictionary.learnWord(wordToLearn, previousLearnedWord);
                        if (!TextUtils.isEmpty(potentialMistake) && !potentialMistake.equalsIgnoreCase(wordToLearn)) {
                            smartDictionary.learnCorrection(potentialMistake, wordToLearn);
                        }
                        potentialMistake = "";
                        previousLearnedWord = wordToLearn;
                        lastLearnedWord = wordToLearn; 
                    }
                    EditorInfo editorInfo = getCurrentInputEditorInfo();
                    int action = editorInfo.imeOptions & EditorInfo.IME_MASK_ACTION;
                    boolean isMultiLine = (editorInfo.inputType & InputType.TYPE_TEXT_FLAG_MULTI_LINE) != 0;
                    
                    if (isMultiLine || editorInfo.inputType == InputType.TYPE_NULL || action == EditorInfo.IME_ACTION_NONE || action == EditorInfo.IME_ACTION_UNSPECIFIED) {
                        ic.commitText("\n", 1);
                    } else {
                        ic.performEditorAction(action);
                    }
                    updateAutoCaps(ic);
                    return; 
                }

                if (isLang || isPageCmd) {
                    if(isLang) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) imm.showInputMethodPicker();
                    }
                    return;
                }

                if (rawKey.equals("CMD_OPEN_SETTINGS")) {
                    try {
                        Intent intent = new Intent(GboardService.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        requestHideSelf(0);
                    } catch (Exception e) { e.printStackTrace(); }
                    return;
                }

                if (rawKey.equals("CMD_MOVE_LEFT")) { sendCursorCommand(ic, KeyEvent.KEYCODE_DPAD_LEFT); return; }
                if (rawKey.equals("CMD_MOVE_RIGHT")) { sendCursorCommand(ic, KeyEvent.KEYCODE_DPAD_RIGHT); return; }
                if (rawKey.equals("CMD_MOVE_UP")) { sendCursorCommand(ic, KeyEvent.KEYCODE_DPAD_UP); return; }
                if (rawKey.equals("CMD_MOVE_DOWN")) { sendCursorCommand(ic, KeyEvent.KEYCODE_DPAD_DOWN); return; }
                if (rawKey.equals("CMD_MOVE_HOME")) { jumpCursor(ic, true); return; }
                if (rawKey.equals("CMD_MOVE_END")) { jumpCursor(ic, false); return; }
                if (rawKey.equals("CMD_SELECT_TOGGLE")) { isSelectModeOn = !isSelectModeOn; return; }
                if (rawKey.equals("CMD_SELECT_RESET")) { isSelectModeOn = false; return; }
                if (rawKey.equals("CMD_SELECT_ALL")) { ic.performContextMenuAction(android.R.id.selectAll); return; }
                if (rawKey.equals("CMD_COPY")) { ic.performContextMenuAction(android.R.id.copy); isSelectModeOn = false; return; }
                if (rawKey.equals("CMD_PASTE")) { ic.performContextMenuAction(android.R.id.paste); return; }
                if (rawKey.equals("CMD_CUT")) { ic.performContextMenuAction(android.R.id.cut); isSelectModeOn = false; return; }

                if (rawKey.startsWith("SUG:")) {
                    String suggestion = rawKey.substring(4);
                    if (suggestion.trim().isEmpty()) return; 
                    
                    String currentWord = getWordBeforeCursor(ic);
                    
                    if (currentWord.length() > 0 && !currentWord.equalsIgnoreCase(suggestion)) {
                         smartDictionary.learnCorrection(currentWord, suggestion);
                    }
                    
                    if (currentWord.length() > 0) ic.deleteSurroundingText(currentWord.length(), 0);
                    suggestion = suggestion.toLowerCase();

                    if (keyboardView.isCapsLockOn()) suggestion = suggestion.toUpperCase();
                    else if (isAutoShifted || keyboardView.isShifted) {
                        for (int i = 0; i < suggestion.length(); i++) {
                            if (Character.isLetter(suggestion.charAt(i))) {
                                suggestion = suggestion.substring(0, i) + Character.toUpperCase(suggestion.charAt(i)) + suggestion.substring(i + 1);
                                break;
                            }
                        }
                    }
                    
                    String formattedSuggestion = FontData.convertText(suggestion, currentFont);
                    ic.commitText(formattedSuggestion + " ", 1);
                    smartDictionary.learnWord(suggestion, previousLearnedWord);
                    previousLearnedWord = suggestion;
                    lastLearnedWord = suggestion;
                    
                    potentialMistake = "";
                    if (isAutoShifted || keyboardView.isShifted) {
                        isAutoShifted = false;
                        keyboardView.setShifted(false);
                    }
                    updateAutoCaps(ic);
                    return;
                }

                if (rawKey.startsWith("EMOJI_INPUT:")) {
                    return;
                }

                if (rawKey.startsWith("PASTE_CLIPBOARD:")) {
                    ic.commitText(rawKey.substring(16), 1);
                    return;
                }

                if (isSpace || isDel || isEnter || isShift || isLang || isPageCmd) {
                    return;
                }

                if (rawKey.equals(".") || rawKey.equals(",") || rawKey.equals("?") || rawKey.equals("!")) {
                    String wordToLearn = getWordBeforeCursor(ic);
                    if (!TextUtils.isEmpty(wordToLearn)) {
                        smartDictionary.learnWord(wordToLearn, previousLearnedWord);
                        if (!TextUtils.isEmpty(potentialMistake) && !potentialMistake.equalsIgnoreCase(wordToLearn)) {
                            smartDictionary.learnCorrection(potentialMistake, wordToLearn);
                        }
                        potentialMistake = "";
                        previousLearnedWord = wordToLearn;
                        lastLearnedWord = wordToLearn; 
                    }
                }
                
                if (rawKey.matches(".*[a-zA-Z]+.*")) {
                    String textToCommit = rawKey.toLowerCase();
                    if (keyboardView.isCapsLockOn()) {
                        textToCommit = textToCommit.toUpperCase();
                    } else if (isAutoShifted || keyboardView.isShifted) {
                        for (int i = 0; i < textToCommit.length(); i++) {
                            if (Character.isLetter(textToCommit.charAt(i))) {
                                textToCommit = textToCommit.substring(0, i) + Character.toUpperCase(textToCommit.charAt(i)) + textToCommit.substring(i + 1);
                                break;
                            }
                        }
                        isAutoShifted = false;
                        keyboardView.setShifted(false);
                    }
                    
                    String formattedText = FontData.convertText(textToCommit, currentFont);
                    if (rawKey.length() > 1 && rawKey.equals(rawKey.toUpperCase())) {
                        ic.commitText(formattedText, 1);
                        smartDictionary.learnWord(textToCommit, previousLearnedWord);
                        previousLearnedWord = textToCommit;
                        lastLearnedWord = textToCommit;
                    } else {
                        ic.commitText(formattedText, 1);
                    }
                } else {
                    ic.commitText(rawKey, 1);
                }
                updateAutoCaps(ic);
            }
        });
        return keyboardView;
    }

    private void sendCursorCommand(InputConnection ic, int keyEventCode) {
        long now = SystemClock.uptimeMillis();
        if (isSelectModeOn) {
            ic.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_LEFT, 0, KeyEvent.META_SHIFT_ON | KeyEvent.META_SHIFT_LEFT_ON));
            ic.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyEventCode, 0, KeyEvent.META_SHIFT_ON | KeyEvent.META_SHIFT_LEFT_ON));
            ic.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_UP, keyEventCode, 0, KeyEvent.META_SHIFT_ON | KeyEvent.META_SHIFT_LEFT_ON));
            ic.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT, 0, 0));
        } else {
            ic.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyEventCode, 0, 0));
            ic.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_UP, keyEventCode, 0, 0));
        }
    }

    private void jumpCursor(InputConnection ic, boolean toStart) {
        if (ic == null) return;
        ExtractedText extText = ic.getExtractedText(new ExtractedTextRequest(), 0);
        if (extText != null && extText.text != null) {
            int length = extText.text.length();
            if (isSelectModeOn) {
                int currentStart = extText.selectionStart;
                int currentEnd = extText.selectionEnd;
                if (currentStart < 0) currentStart = 0;
                if (currentEnd < 0) currentEnd = 0;
                
                if (toStart) ic.setSelection(0, currentEnd);
                else ic.setSelection(currentStart, length);
            } else {
                if (toStart) ic.setSelection(0, 0);
                else ic.setSelection(length, length);
            }
        } else {
            int code = toStart ? KeyEvent.KEYCODE_MOVE_HOME : KeyEvent.KEYCODE_MOVE_END;
            sendCursorCommand(ic, code);
        }
    }

    private String getWordBeforeCursor(InputConnection ic) {
        if (ic == null) return "";
        CharSequence before = ic.getTextBeforeCursor(50, 0);
        if (TextUtils.isEmpty(before)) return "";
        String text = before.toString();
        
        int breakIndex = -1;
        for (int i = text.length() - 1; i >= 0; i--) {
            char c = text.charAt(i);
            if (Character.isLowSurrogate(c) || Character.isHighSurrogate(c)) {
                continue;
            }
            if (Character.isWhitespace(c) || c == ' ' || c == ',' || c == '.' || c == '?' || c == '!' || c == '\n') {
                if (c != '@' && c != '.') {
                    breakIndex = i;
                    break;
                }
            }
        }
        if (breakIndex == text.length() - 1) return "";
        return text.substring(breakIndex + 1);
    }

    private void updateSmartToolbar(InputConnection ic) {
        if (keyboardView == null) return;
        CharSequence fullTextBefore = ic.getTextBeforeCursor(20, 0);
        if (TextUtils.isEmpty(fullTextBefore)) {
            previousLearnedWord = "";
            lastLearnedWord = "";
            keyboardView.setSuggestions(new ArrayList<String>(), true);
            return;
        }

        String currentWord = getWordBeforeCursor(ic);
        if (!TextUtils.isEmpty(currentWord)) {
            boolean forceCaps = keyboardView.isCapsLockOn();
            List<String> suggestions = smartDictionary.getSuggestions(currentWord, previousLearnedWord, forceCaps);
            keyboardView.setSuggestions(suggestions, false); 
        } else {
            List<String> suggestions = smartDictionary.getSuggestions("", previousLearnedWord, keyboardView.isCapsLockOn());
            boolean hasRealSuggestion = false;
            for(String s : suggestions) if(!s.trim().isEmpty()) hasRealSuggestion = true;
            if (hasRealSuggestion) keyboardView.setSuggestions(suggestions, false);
            else keyboardView.setSuggestions(new ArrayList<String>(), true);
        }
    }

    @Override
    public void onFinishInputView(boolean finishingInput) {
        super.onFinishInputView(finishingInput);
        if (keyboardView != null) keyboardView.resetKeyboardState();
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        isSelectModeOn = false; 
        forceLowercase = false; 
        
        previousLearnedWord = "";
        lastLearnedWord = "";
        potentialMistake = "";
        
        if (keyboardView != null) {
            keyboardView.resetKeyboardState();
            keyboardView.setImeOptions(info);
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) updateAutoCaps(ic);
        }
        GlobalCloudSync.syncOfflineWords(this);
    }

    private void updateAutoCaps(InputConnection ic) {
        if (ic == null || keyboardView == null) return;
        updateSmartToolbar(ic);
        EditorInfo info = getCurrentInputEditorInfo();
        if (info != null && info.inputType == InputType.TYPE_NULL) {
            isAutoShifted = false;
            keyboardView.setShifted(false);
            return;
        }
        CharSequence beforeCursor = ic.getTextBeforeCursor(3, 0);
        if (TextUtils.isEmpty(beforeCursor)) isAutoShifted = !forceLowercase;
        else {
            String text = beforeCursor.toString();
            isAutoShifted = (text.endsWith(". ") || text.endsWith("! ") || text.endsWith("? ") || text.endsWith("\n")) && !forceLowercase;
        }
        if (!keyboardView.isCapsLockOn()) keyboardView.setShifted(isAutoShifted);
    }
}