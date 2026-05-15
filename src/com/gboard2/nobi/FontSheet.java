package com.gboard2.nobi;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

public class FontSheet extends FrameLayout {

    private Context context;
    private ProKeyboardView keyboardView;
    private LinearLayout sheetContainer;
    private LinearLayout toolbar;
    private TextView title;
    private Drawable backIcon;
    private LinearLayout listContainer;
    
    private boolean mIsShowing = false;
    private float revealCx = 0f;
    private float revealCy = 0f;
    private ThemeData.Theme currentTheme = ThemeData.getThemeByName("Default");

    public FontSheet(Context context, ProKeyboardView keyboardView) {
        super(context);
        this.context = context;
        this.keyboardView = keyboardView;
        setupUI();
    }
    
    public void applyTheme(ThemeData.Theme theme) {
        if (theme == null) return;
        this.currentTheme = theme;
        
        if (sheetContainer != null) sheetContainer.setBackgroundColor(theme.bgColor);
        if (toolbar != null) toolbar.setBackgroundColor(theme.suggestionBgColor);
        if (title != null) title.setTextColor(theme.suggestionTextColor);
        if (backIcon != null) backIcon.setTint(theme.suggestionTextColor);
        
        if (mIsShowing) {
            renderFonts();
        }
    }

    private int dpToPx(float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    private void setupUI() {
        this.setVisibility(GONE);

        sheetContainer = new LinearLayout(context);
        sheetContainer.setOrientation(LinearLayout.VERTICAL);
        sheetContainer.setBackgroundColor(currentTheme.bgColor);
        addView(sheetContainer, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        toolbar = new LinearLayout(context);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        toolbar.setBackgroundColor(currentTheme.suggestionBgColor);
        toolbar.setPadding(dpToPx(8), 0, dpToPx(8), 0);

        ImageView backBtn = new ImageView(context);
        backIcon = context.getResources().getDrawable(R.drawable.ic_back_arrow, null);
        if (backIcon != null) {
            backIcon.setTint(currentTheme.suggestionTextColor);
        }
        backBtn.setImageDrawable(backIcon);
        backBtn.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));

        backBtn.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public void onClick(View v) { 
                hide(); 
            } 
        });

        toolbar.addView(backBtn, new LinearLayout.LayoutParams(dpToPx(48), dpToPx(48)));
        
        title = new TextView(context);
        title.setText("Fonts & Styles");
        title.setTextColor(currentTheme.suggestionTextColor);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        title.setTypeface(null, Typeface.BOLD);

        LinearLayout.LayoutParams tParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        tParams.leftMargin = dpToPx(12);
        toolbar.addView(title, tParams);

        sheetContainer.addView(toolbar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(48)));

        ScrollView scroll = new ScrollView(context);
        listContainer = new LinearLayout(context);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        listContainer.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(24));
        scroll.addView(listContainer);
        sheetContainer.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        renderFonts();
    }

    private void renderFonts() {
        listContainer.removeAllViews();
        List<FontData.FontStyle> fonts = FontData.getFonts();
        final SharedPreferences prefs = context.getSharedPreferences("GboardPrefs", Context.MODE_PRIVATE);
        String currentFont = prefs.getString("selected_font", "Default");

        for (final FontData.FontStyle font : fonts) {
            boolean isApplied = font.name.equals(currentFont);

            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
            
            GradientDrawable gd = new GradientDrawable();
            gd.setColor(isApplied ? currentTheme.keyPressedColor : currentTheme.keyBgColor);
            gd.setCornerRadius(dpToPx(8));
            if (isApplied) {
                gd.setStroke(dpToPx(1), currentTheme.enterBgColor);
            }
            row.setBackground(gd);

            TextView preview = new TextView(context);
            String previewText = font.name.equals("Default") ? "Default Font" : font.name;
            preview.setText(FontData.convertText(previewText, font));
            preview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            preview.setTextColor(currentTheme.textColor);
            preview.setTypeface(font.keyboardTypeface);

            LinearLayout.LayoutParams pParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            row.addView(preview, pParams);
            
            if (isApplied) {
                TextView applied = new TextView(context);
                applied.setText("\u2713");
                applied.setTextColor(currentTheme.enterBgColor);
                applied.setTypeface(null, Typeface.BOLD);
                row.addView(applied);
            }

            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                    keyboardView.applyFont(font);
                    prefs.edit().putString("selected_font", font.name).apply();
                    renderFonts();
                }
            });

            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rowParams.bottomMargin = dpToPx(8);
            listContainer.addView(row, rowParams);
        }
    }

    // --- INSTANT SHOW / HIDE ---
    public void show(float cx, float cy) {
        if (mIsShowing) return;
        mIsShowing = true;
        this.setVisibility(VISIBLE);
        this.revealCx = cx;
        this.revealCy = cy;
        renderFonts();
        
        sheetContainer.setAlpha(1f);
        sheetContainer.setScaleX(1f);
        sheetContainer.setScaleY(1f);
    }

    public void hide() {
        if (!mIsShowing) return;
        mIsShowing = false;
        setVisibility(GONE);
    }

    public boolean isShowing() { 
        return mIsShowing;
    }
}
