package com.gboard2.nobi;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
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

public class ThemeSheet extends FrameLayout {

    private Context context;
    private ProKeyboardView keyboardView;
    private LinearLayout sheetContainer;
    private LinearLayout toolbar;
    private TextView title;
    private Drawable backIcon;
    private LinearLayout gridContainer;
    
    private boolean isShowing = false;
    private float revealCx = 0f;
    private float revealCy = 0f;
    private ThemeData.Theme currentTheme = ThemeData.getThemeByName("Default");

    public ThemeSheet(Context context, ProKeyboardView keyboardView) {
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
        
        if (isShowing) {
            renderThemes();
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
        title.setText("Themes");
        title.setTextColor(currentTheme.suggestionTextColor);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        title.setTypeface(null, Typeface.BOLD);

        LinearLayout.LayoutParams tParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        tParams.leftMargin = dpToPx(12);
        toolbar.addView(title, tParams);

        sheetContainer.addView(toolbar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(48)));

        ScrollView scroll = new ScrollView(context);
        gridContainer = new LinearLayout(context);
        gridContainer.setOrientation(LinearLayout.VERTICAL);
        gridContainer.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(24));
        scroll.addView(gridContainer);
        sheetContainer.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        renderThemes();
    }

    private View createKeyboardPreview(final ThemeData.Theme theme) {
        View preview = new View(context) {
            Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            Paint keyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            Paint specPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            Paint entPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);

                bgPaint.setColor(theme.bgColor);
                keyPaint.setColor(theme.keyBgColor);
                specPaint.setColor(theme.specialKeyBgColor);
                entPaint.setColor(theme.enterBgColor);
                
                borderPaint.setStyle(Paint.Style.STROKE);
                borderPaint.setStrokeWidth(dpToPx(1));
                borderPaint.setColor(Color.parseColor("#CFD8DC"));

                int w = getWidth();
                int h = getHeight();

                // Draw Background (Image or Solid)
                if (theme.backgroundImageRes != 0) {
                    try {
                        Drawable bgDrawable = context.getResources().getDrawable(theme.backgroundImageRes, null);
                        if (bgDrawable != null) {
                            bgDrawable.setBounds(0, 0, w, h);
                            Path clipPath = new Path();
                            clipPath.addRoundRect(new RectF(0, 0, w, h), dpToPx(6), dpToPx(6), Path.Direction.CW);
                            canvas.save();
                            canvas.clipPath(clipPath);
                            bgDrawable.draw(canvas);
                            canvas.restore();
                        }
                    } catch (Exception e) {
                        canvas.drawRoundRect(new RectF(0, 0, w, h), dpToPx(6), dpToPx(6), bgPaint);
                    }
                } else {
                    canvas.drawRoundRect(new RectF(0, 0, w, h), dpToPx(6), dpToPx(6), bgPaint);
                }
                
                canvas.drawRoundRect(new RectF(0, 0, w, h), dpToPx(6), dpToPx(6), borderPaint);
                
                float pad = dpToPx(2);
                float keyH = (h - (pad * 5)) / 4f;
                float keyW = (w - (pad * 11)) / 10f;
                float rad = dpToPx(2.5f);
                
                float startX = pad;
                float y = pad;

                for(int i=0; i<10; i++) {
                    canvas.drawRoundRect(new RectF(startX, y, startX+keyW, y+keyH), rad, rad, keyPaint);
                    startX += keyW + pad;
                }

                startX = pad + (keyW/2f);
                y += keyH + pad;
                for(int i=0; i<9; i++) {
                    canvas.drawRoundRect(new RectF(startX, y, startX+keyW, y+keyH), rad, rad, keyPaint);
                    startX += keyW + pad;
                }

                startX = pad;
                y += keyH + pad;
                float shiftW = keyW * 1.5f;
                canvas.drawRoundRect(new RectF(startX, y, startX+shiftW, y+keyH), rad, rad, specPaint);
                startX += shiftW + pad;
                for(int i=0; i<7; i++) {
                    canvas.drawRoundRect(new RectF(startX, y, startX+keyW, y+keyH), rad, rad, keyPaint);
                    startX += keyW + pad;
                }
                float delW = w - startX - pad;
                canvas.drawRoundRect(new RectF(startX, y, startX+delW, y+keyH), rad, rad, specPaint);

                startX = pad;
                y += keyH + pad;
                float symW = keyW * 1.5f;
                canvas.drawRoundRect(new RectF(startX, y, startX+symW, y+keyH), rad, rad, specPaint);
                startX += symW + pad;
                canvas.drawRoundRect(new RectF(startX, y, startX+keyW, y+keyH), rad, rad, specPaint); 
                startX += keyW + pad;
                float spaceW = keyW * 4f;
                canvas.drawRoundRect(new RectF(startX, y, startX+spaceW, y+keyH), rad, rad, keyPaint); 
                startX += spaceW + pad;
                canvas.drawRoundRect(new RectF(startX, y, startX+keyW, y+keyH), rad, rad, specPaint); 
                startX += keyW + pad;
                float entW = w - startX - pad;
                canvas.drawRoundRect(new RectF(startX, y, startX+entW, y+keyH), rad, rad, entPaint);
            }
        };
        return preview;
    }

    private void renderThemes() {
        gridContainer.removeAllViews();
        List<ThemeData.Theme> themes = ThemeData.getThemes();
        final SharedPreferences prefs = context.getSharedPreferences("GboardPrefs", Context.MODE_PRIVATE);
        String currentThemeName = prefs.getString("selected_theme", "Default");

        LinearLayout currentRow = null;
        for (int i = 0; i < themes.size(); i++) {
            if (i % 2 == 0) {
                currentRow = new LinearLayout(context);
                currentRow.setOrientation(LinearLayout.HORIZONTAL);
                currentRow.setWeightSum(2f);
                gridContainer.addView(currentRow, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }

            final ThemeData.Theme theme = themes.get(i);
            boolean isApplied = theme.name.equals(currentThemeName);
            
            LinearLayout card = new LinearLayout(context);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setGravity(Gravity.CENTER);
            card.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(12));
            
            GradientDrawable gd = new GradientDrawable();
            gd.setCornerRadius(dpToPx(10));
            if (isApplied) {
                gd.setColor(currentTheme.keyPressedColor);
                gd.setStroke(dpToPx(2), currentTheme.enterBgColor);
            } else {
                gd.setColor(currentTheme.keyBgColor);
                gd.setStroke(dpToPx(1), currentTheme.suggestionTextColor);
            }
            card.setBackground(gd);
            
            View preview = createKeyboardPreview(theme);
            LinearLayout.LayoutParams previewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(85));
            card.addView(preview, previewParams);

            LinearLayout nameRow = new LinearLayout(context);
            nameRow.setOrientation(LinearLayout.HORIZONTAL);
            nameRow.setGravity(Gravity.CENTER);

            LinearLayout.LayoutParams nrParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            nrParams.topMargin = dpToPx(10);
            
            TextView name = new TextView(context);
            name.setText(theme.name);
            name.setTextColor(currentTheme.textColor);
            name.setGravity(Gravity.CENTER);
            name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);

            if (isApplied) {
                name.setTypeface(null, Typeface.BOLD);
                name.setTextColor(currentTheme.enterBgColor);
            }
            nameRow.addView(name);

            if (isApplied) {
                TextView appliedText = new TextView(context);
                appliedText.setText(" Applied");
                appliedText.setTextColor(currentTheme.enterBgColor);
                appliedText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
                appliedText.setTypeface(null, Typeface.BOLD);
                nameRow.addView(appliedText);
            }

            card.addView(nameRow, nrParams);

            card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                    keyboardView.applyTheme(theme);
                    prefs.edit().putString("selected_theme", theme.name).apply();
                    renderThemes();
                }
            });

            LinearLayout.LayoutParams cParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            cParams.setMargins(dpToPx(6), dpToPx(6), dpToPx(6), dpToPx(6));
            currentRow.addView(card, cParams);
        }

        // Add dummy view if odd number of themes
        if (themes.size() % 2 != 0 && currentRow != null) {
            View dummy = new View(context);
            LinearLayout.LayoutParams dummyParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
            currentRow.addView(dummy, dummyParams);
        }
    }

    // --- INSTANT SHOW / HIDE ---
    public void show(float cx, float cy) {
        if (isShowing) return;
        isShowing = true;
        this.setVisibility(VISIBLE);
        this.revealCx = cx;
        this.revealCy = cy;
        renderThemes();
        
        sheetContainer.setAlpha(1f);
        sheetContainer.setScaleX(1f);
        sheetContainer.setScaleY(1f);
    }

    public void hide() {
        if (!isShowing) return;
        isShowing = false;
        setVisibility(GONE);
    }

    public boolean isShowing() { 
        return isShowing;
    }
}
