
package com.gboard2.nobi;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;

public class ThemeManager {

    public Paint keyBgPaint, keyPressedPaint, enterBgPaint, textPaint, bgPaint;
    public Paint suggestionBgPaint, suggestionTextPaint;
    public Paint popupBgPaint, popupTextPaint;
    public Paint specialKeyPaint, ripplePaint;
    public Paint glassBorderPaint;
    // For transparent theme borders
    public Path clipPath;
    
    public Drawable iconBackspace, iconShift, iconShiftFilled, iconCapslock, iconEnter, iconEmoji, iconLanguage;
    public Drawable iconSearch, iconSend, iconDone, iconNext, iconClipboard, iconTextEditing, iconMic, iconResize, iconTheme, iconFont, iconSettings;
    public Drawable activeShiftIcon;
    // Custom Image Backgrounds
    public Drawable customBackgroundDrawable;
    public Drawable customKeyDrawable;
    
    public FontData.FontStyle activeFont;
    public ThemeData.Theme activeTheme;
    public boolean showSlangRow = true;
    public boolean showEmojiRow = true;
    public boolean showNumberRow = false;
    public boolean replaceEmojiInSymbols = false;
    private ProKeyboardView pkv;
    private Context context;

    public ThemeManager(Context context, ProKeyboardView pkv) {
        this.context = context;
        this.pkv = pkv;
        initPaints();
        loadIcons();
        
        SharedPreferences prefs = context.getSharedPreferences("GboardPrefs", Context.MODE_PRIVATE);
        String savedThemeName = prefs.getString("selected_theme", "Default");
        String savedFontName = prefs.getString("selected_font", "Default");
        activeFont = FontData.getFontByName(savedFontName);
        activeTheme = ThemeData.getThemeByName(savedThemeName);
        applyTheme(activeTheme);
        activeShiftIcon = iconShift;
    }

    private void initPaints() {
        bgPaint = new Paint();
        bgPaint.setStyle(Paint.Style.FILL);
        suggestionBgPaint = new Paint();
        suggestionBgPaint.setStyle(Paint.Style.FILL);

        suggestionTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        suggestionTextPaint.setTextAlign(Paint.Align.CENTER);

        keyBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        keyBgPaint.setStyle(Paint.Style.FILL);

        specialKeyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        specialKeyPaint.setStyle(Paint.Style.FILL);
        keyPressedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        keyPressedPaint.setStyle(Paint.Style.FILL);

        enterBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        enterBgPaint.setStyle(Paint.Style.FILL);
        
        glassBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glassBorderPaint.setStyle(Paint.Style.STROKE);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);
        popupBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        popupBgPaint.setColor(Color.WHITE);
        popupBgPaint.setStyle(Paint.Style.FILL);
        popupBgPaint.setShadowLayer(pkv.dpToPx(6), 0f, pkv.dpToPx(3), Color.parseColor("#33000000"));
        
        popupTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        popupTextPaint.setColor(Color.parseColor("#263238"));
        popupTextPaint.setTextAlign(Paint.Align.CENTER);

        ripplePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ripplePaint.setStyle(Paint.Style.FILL);
        clipPath = new Path();
    }

    private void loadIcons() {
        iconBackspace = context.getResources().getDrawable(R.drawable.ic_backspace, null);
        iconShift = context.getResources().getDrawable(R.drawable.ic_shift, null);
        iconShiftFilled = context.getResources().getDrawable(R.drawable.ic_shift_filled, null);
        iconCapslock = context.getResources().getDrawable(R.drawable.ic_capslock, null);
        iconEnter = context.getResources().getDrawable(R.drawable.ic_enter, null);
        iconEmoji = context.getResources().getDrawable(R.drawable.ic_emoji, null);
        iconLanguage = context.getResources().getDrawable(R.drawable.ic_language, null);
        iconSearch = context.getResources().getDrawable(R.drawable.ic_search, null);
        iconSend = context.getResources().getDrawable(R.drawable.ic_send, null);
        iconDone = context.getResources().getDrawable(R.drawable.ic_done, null);
        iconNext = context.getResources().getDrawable(R.drawable.ic_next, null);
        iconClipboard = context.getResources().getDrawable(R.drawable.ic_clipboard, null);
        iconTextEditing = context.getResources().getDrawable(R.drawable.ic_text_editing, null);
        iconMic = context.getResources().getDrawable(R.drawable.ic_mic, null);
        iconResize = context.getResources().getDrawable(R.drawable.ic_resize, null);
        iconTheme = context.getResources().getDrawable(R.drawable.ic_theme, null);
        iconFont = context.getResources().getDrawable(R.drawable.ic_font, null);
        
        int settingsResId = context.getResources().getIdentifier("ic_settings", "drawable", context.getPackageName());
        if (settingsResId != 0) {
            iconSettings = context.getResources().getDrawable(settingsResId, null);
        } else {
            iconSettings = null;
        }
    }

    public void loadRowPreferences() {
        SharedPreferences prefs = context.getSharedPreferences("GboardPrefs", Context.MODE_PRIVATE);
        showSlangRow = prefs.getBoolean("show_slang_row", true);
        showEmojiRow = prefs.getBoolean("show_emoji_row", true);
        showNumberRow = prefs.getBoolean("show_number_row", false);
        replaceEmojiInSymbols = prefs.getBoolean("replace_emoji_symbol", false);
    }

    public void applyTheme(ThemeData.Theme theme) {
        this.activeTheme = theme;
        bgPaint.setColor(theme.bgColor);
        keyBgPaint.setColor(theme.keyBgColor);
        specialKeyPaint.setColor(theme.specialKeyBgColor);
        keyPressedPaint.setColor(theme.keyPressedColor);
        enterBgPaint.setColor(theme.enterBgColor);
        textPaint.setColor(theme.textColor);
        suggestionBgPaint.setColor(theme.suggestionBgColor);
        suggestionTextPaint.setColor(theme.suggestionTextColor);
        
        // Handle Neon Glow Layer
        if (theme.textGlowRadius > 0f) {
            textPaint.setShadowLayer(theme.textGlowRadius, 0f, 0f, theme.textGlowColor);
        } else {
            textPaint.clearShadowLayer();
        }

        // Handle Image Resources
        if (theme.backgroundImageRes != 0) {
            try { 
                customBackgroundDrawable = context.getResources().getDrawable(theme.backgroundImageRes, null);
            } 
            catch (Exception e) { 
                customBackgroundDrawable = null;
            }
        } else {
            customBackgroundDrawable = null;
        }
        
        if (theme.keyImageRes != 0) {
            try { 
                customKeyDrawable = context.getResources().getDrawable(theme.keyImageRes, null);
            } 
            catch (Exception e) { 
                customKeyDrawable = null;
            }
        } else {
            customKeyDrawable = null;
        }

        // Glassmorphism Border Setup (Ice/Glass Effect)
        // Ice effect demands a solid white line with high transparency, creating a glassy edge
        glassBorderPaint.setColor(Color.WHITE);
        glassBorderPaint.setAlpha(70); // ~27% opacity for sharp ice edges
        glassBorderPaint.setStrokeWidth(pkv.dpToPx(1.2f));
        // Slightly thicker for clarity

        int iconTint = theme.textColor;
        if (iconBackspace != null) iconBackspace.setTint(iconTint);
        if (iconShift != null) iconShift.setTint(iconTint);
        if (iconEmoji != null) iconEmoji.setTint(iconTint);
        if (iconLanguage != null) iconLanguage.setTint(iconTint);
        if (iconClipboard != null) iconClipboard.setTint(iconTint);
        if (iconTextEditing != null) iconTextEditing.setTint(iconTint);
        if (iconMic != null) iconMic.setTint(iconTint);
        if (iconResize != null) iconResize.setTint(iconTint);
        if (iconTheme != null) iconTheme.setTint(iconTint);
        if (iconFont != null) iconFont.setTint(iconTint);
        if (iconSettings != null) iconSettings.setTint(iconTint);
        
        if (pkv.clipboardBottomSheet != null) pkv.clipboardBottomSheet.applyTheme(theme);
        if (pkv.textEditingSheet != null) pkv.textEditingSheet.applyTheme(theme);
        if (pkv.emojiSheet != null) pkv.emojiSheet.applyTheme(theme);
        if (pkv.themeSheet != null) pkv.themeSheet.applyTheme(theme);
        if (pkv.fontSheet != null) pkv.fontSheet.applyTheme(theme);
        pkv.invalidate();
    }

    public void applyFont(FontData.FontStyle font) {
        this.activeFont = font;
        pkv.invalidate();
    }
}
