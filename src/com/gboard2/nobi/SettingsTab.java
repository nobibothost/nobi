package com.gboard2.nobi;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsTab extends ScrollView {

    public SettingsTab(final Context context, SharedPreferences prefs) {
        super(context);
        setClipToPadding(false);
        setPadding(0, 0, 0, UIHelpers.dpToPx(context, 100)); // Padding adjusted for top flush
        
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);

        // --- TAB HEADER ---
        LinearLayout headerContainer = new LinearLayout(context);
        headerContainer.setOrientation(LinearLayout.VERTICAL);
        headerContainer.setPadding(UIHelpers.dpToPx(context, 24), UIHelpers.dpToPx(context, 24), UIHelpers.dpToPx(context, 24), UIHelpers.dpToPx(context, 16));

        TextView title = new TextView(context);
        title.setText("Settings");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28f);
        title.setTextColor(Color.parseColor("#111111"));
        title.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        headerContainer.addView(title);

        TextView subtitle = new TextView(context);
        subtitle.setText("Configure your typing experience, haptics, sounds, and dictionaries.");
        subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        subtitle.setTextColor(Color.parseColor("#78909C"));
        subtitle.setPadding(0, UIHelpers.dpToPx(context, 4), 0, 0);
        headerContainer.addView(subtitle);

        container.addView(headerContainer);
        
        // --- SYSTEM SETUP ---
        container.addView(UIHelpers.createSectionHeader(context, "SYSTEM SETUP"));
        LinearLayout setupCard = UIHelpers.createCardContainer(context);
        LinearLayout btnEnable = (LinearLayout) UIHelpers.createSettingRow(context, "⚙️", "#E5E5EA", "Enable in Settings", "Activate Gboard Pro", UIHelpers.getArrowIcon(context));
        btnEnable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                context.startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS));
            }
        });
        setupCard.addView(btnEnable);
        setupCard.addView(UIHelpers.createDivider(context));
        
        LinearLayout btnSelect = (LinearLayout) UIHelpers.createSettingRow(context, "⌨️", "#E5E5EA", "Select Keyboard", "Set as default method", UIHelpers.getArrowIcon(context));
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.showInputMethodPicker();
            }
        });
        setupCard.addView(btnSelect);
        container.addView(setupCard);
        
        // --- CLOUD DICTIONARY ---
        container.addView(UIHelpers.createSectionHeader(context, "CLOUD DICTIONARY"));
        LinearLayout syncCard = UIHelpers.createCardContainer(context);
        LinearLayout btnSync = (LinearLayout) UIHelpers.createSettingRow(context, "☁️", "#E5FFE5", "Update Dictionary", "Download new global words", UIHelpers.getArrowIcon(context));
        btnSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                Toast.makeText(context, "Checking for updates...", Toast.LENGTH_SHORT).show();
                GlobalCloudSync.downloadDictionaryUpdate(context, false);
            }
        });
        syncCard.addView(btnSync);
        container.addView(syncCard);

        // --- KEYBOARD ROWS ---
        container.addView(UIHelpers.createSectionHeader(context, "KEYBOARD ROWS"));
        LinearLayout rowCard = UIHelpers.createCardContainer(context);
        rowCard.addView(UIHelpers.createSettingRow(context, "🔢", "#E5F1FF", "Number Row", "Show numbers on top of letters", UIHelpers.createModernToggle(context, prefs, "show_number_row", false, null)));
        rowCard.addView(UIHelpers.createDivider(context));
        rowCard.addView(UIHelpers.createSettingRow(context, "😀", "#FFF0E5", "Emoji Row", "Show quick emojis on top", UIHelpers.createModernToggle(context, prefs, "show_emoji_row", true, null)));
        rowCard.addView(UIHelpers.createDivider(context));
        rowCard.addView(UIHelpers.createSettingRow(context, "💬", "#E5FFE5", "Slang Row", "Show hinglish slang shortcuts", UIHelpers.createModernToggle(context, prefs, "show_slang_row", true, null)));
        container.addView(rowCard);
        
        // --- SYMBOLS BEHAVIOR ---
        container.addView(UIHelpers.createSectionHeader(context, "SYMBOLS BEHAVIOR"));
        LinearLayout symCard = UIHelpers.createCardContainer(context);
        symCard.addView(UIHelpers.createSettingRow(context, "➗", "#F5E5FF", "Math in Symbols", "Replace emojis with math symbols", UIHelpers.createModernToggle(context, prefs, "replace_emoji_symbol", false, null)));
        container.addView(symCard);

        // --- ADVANCED CUSTOMIZATION ---
        container.addView(UIHelpers.createSectionHeader(context, "ADVANCED CUSTOMIZATION"));
        LinearLayout customCard = UIHelpers.createCardContainer(context);
        customCard.addView(UIHelpers.createSettingRow(context, "🛠️", "#FFE5F1", "Custom Key Editor Mode", "Tap keys to edit actions (Syncs with Toolbar)", UIHelpers.createModernToggle(context, prefs, "is_editor_mode", false, null)));
        container.addView(customCard);
        
        // --- GENERAL ---
        container.addView(UIHelpers.createSectionHeader(context, "GENERAL"));
        LinearLayout generalCard = UIHelpers.createCardContainer(context);
        generalCard.addView(UIHelpers.createSettingRow(context, "📳", "#E5F1FF", "Haptic Feedback", "Vibrate on keypress", UIHelpers.createModernToggle(context, prefs, "vibrate_on_keypress", true, null)));
        generalCard.addView(UIHelpers.createDivider(context));
        generalCard.addView(UIHelpers.createSettingRow(context, "🔊", "#FFF0E5", "Sound on Keypress", "Play tick sound", UIHelpers.createModernToggle(context, prefs, "sound_on_keypress", false, null)));
        container.addView(generalCard);

        addView(container);
    }
}