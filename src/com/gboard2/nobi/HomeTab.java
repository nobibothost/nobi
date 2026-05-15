package com.gboard2.nobi;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.List;

public class HomeTab extends ScrollView {

    private Context context;
    private SharedPreferences prefs;
    private LinearLayout container;
    private Runnable onViewAllThemes;
    private SoundManager sampleSoundManager;
    
    private boolean isSoundsExpanded = false;
    private boolean isEffectsExpanded = false;

    public HomeTab(Context context, SharedPreferences prefs, Runnable onViewAllThemes) {
        super(context);
        this.context = context;
        this.prefs = prefs;
        this.onViewAllThemes = onViewAllThemes;
        this.sampleSoundManager = new SoundManager(context);

        setClipToPadding(false);
        // Changed top padding to 0 so the new stylish header flushes to the top
        setPadding(0, 0, 0, dpToPx(100));
        
        container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        addView(container);

        render();
    }

    public void render() {
        container.removeAllViews();
        buildTabHeader(); // Add the stylish Main Header first
        buildUI();        // Then add the rest of your original content
    }

    // --- NEW: TAB HEADER ---
    private void buildTabHeader() {
        LinearLayout headerContainer = new LinearLayout(context);
        headerContainer.setOrientation(LinearLayout.VERTICAL);
        headerContainer.setPadding(dpToPx(24), dpToPx(24), dpToPx(24), dpToPx(16));

        TextView title = new TextView(context);
        title.setText("Dashboard");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28f);
        title.setTextColor(Color.parseColor("#111111"));
        title.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        headerContainer.addView(title);

        TextView subtitle = new TextView(context);
        subtitle.setText("Welcome to Gboard Pro. Monitor your stats and active features.");
        subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        subtitle.setTextColor(Color.parseColor("#78909C"));
        subtitle.setPadding(0, dpToPx(4), 0, 0);
        headerContainer.addView(subtitle);

        container.addView(headerContainer);
    }

    private void buildUI() {
        // --- 1. THEMES SECTION ---
        container.addView(createHeaderWithAction("THEMES", "View All", new OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                if (onViewAllThemes != null) onViewAllThemes.run();
            }
        }));
        
        LinearLayout themesCard = createMainCard();
        buildThemesGrid(themesCard);
        container.addView(themesCard);

        // --- 2. SOUNDS SECTION ---
        container.addView(createHeaderWithAction("SOUNDS", isSoundsExpanded ? "View Less" : "View All", new OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                isSoundsExpanded = !isSoundsExpanded;
                render();
            }
        }));

        LinearLayout soundsCard = createMainCard();
        buildSoundsGrid(soundsCard);
        container.addView(soundsCard);

        // --- 3. TOUCH EFFECTS SECTION ---
        container.addView(createHeaderWithAction("TOUCH EFFECTS", isEffectsExpanded ? "View Less" : "View All", new OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                isEffectsExpanded = !isEffectsExpanded;
                render();
            }
        }));

        LinearLayout effectsCard = createMainCard();
        buildEffectsGrid(effectsCard);
        container.addView(effectsCard);
    }

    private void buildThemesGrid(LinearLayout parent) {
        List<ThemeData.Theme> allThemes = ThemeData.getThemes();
        String currentTheme = prefs.getString("selected_theme", "Default");
        int count = Math.min(4, allThemes.size());
        
        LinearLayout currentRow = null;
        for (int i = 0; i < count; i++) {
            if (i % 2 == 0) {
                currentRow = new LinearLayout(context);
                currentRow.setOrientation(LinearLayout.HORIZONTAL);
                currentRow.setWeightSum(2f);
                parent.addView(currentRow, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            
            final ThemeData.Theme theme = allThemes.get(i);
            boolean isApplied = theme.name.equals(currentTheme);
            
            LinearLayout itemBox = new LinearLayout(context);
            itemBox.setOrientation(LinearLayout.VERTICAL);
            itemBox.setGravity(Gravity.CENTER);
            itemBox.setPadding(dpToPx(8), dpToPx(10), dpToPx(8), dpToPx(10));
            
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(dpToPx(10));
            bg.setColor(Color.parseColor(isApplied ? "#E3F2FD" : "#F5F7FA"));
            bg.setStroke(dpToPx(isApplied ? 2 : 1), Color.parseColor(isApplied ? UIHelpers.COLOR_ACCENT : "#CFD8DC"));
            itemBox.setBackground(bg);

            View preview = createThemePreview(theme);
            LinearLayout.LayoutParams pParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(45));
            pParams.bottomMargin = dpToPx(8);
            itemBox.addView(preview, pParams);

            TextView title = new TextView(context);
            title.setText(theme.name);
            title.setTextColor(Color.parseColor(isApplied ? UIHelpers.COLOR_ACCENT : UIHelpers.COLOR_TEXT_PRIMARY));
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            title.setGravity(Gravity.CENTER);
            itemBox.addView(title);

            itemBox.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                    prefs.edit().putString("selected_theme", theme.name).apply();
                    render();
                }
            });
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            params.setMargins(dpToPx(6), dpToPx(6), dpToPx(6), dpToPx(6));
            currentRow.addView(itemBox, params);
        }
    }

    private View createThemePreview(final ThemeData.Theme theme) {
        return new View(context) {
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                float w = getWidth(); float h = getHeight();
                float r = dpToPx(4);

                p.setColor(theme.bgColor);
                canvas.drawRoundRect(new RectF(0, 0, w, h), r, r, p);

                p.setColor(theme.keyBgColor);
                float kw = (w - dpToPx(10)) / 5f;
                float kh = (h - dpToPx(8)) / 3f;
                for(int i=0; i<5; i++) {
                    canvas.drawRoundRect(new RectF(dpToPx(2) + i*(kw+dpToPx(1.5f)), dpToPx(2), dpToPx(2) + i*(kw+dpToPx(1.5f)) + kw, dpToPx(2)+kh), dpToPx(1), dpToPx(1), p);
                }
                p.setColor(theme.enterBgColor);
                canvas.drawRoundRect(new RectF(w*0.25f, h-kh-dpToPx(2), w*0.75f, h-dpToPx(2)), dpToPx(1), dpToPx(1), p);
            }
        };
    }

    private void buildSoundsGrid(LinearLayout parent) {
        LinearLayout volumeRow = new LinearLayout(context);
        volumeRow.setOrientation(LinearLayout.HORIZONTAL);
        volumeRow.setGravity(Gravity.CENTER_VERTICAL);
        volumeRow.setPadding(dpToPx(8), 0, dpToPx(8), dpToPx(12));
        
        TextView volIcon = new TextView(context);
        volIcon.setText("🔊");
        volIcon.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        
        SeekBar volumeSlider = new SeekBar(context);
        volumeSlider.setMax(100);
        volumeSlider.setProgress((int)(prefs.getFloat("sound_volume", 1.0f) * 100));
        volumeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    prefs.edit().putFloat("sound_volume", progress / 100f).apply();
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {
                sampleSoundManager.playSoundAndHaptic(null, "SPACE"); 
            }
        });
        
        volumeRow.addView(volIcon);
        volumeRow.addView(volumeSlider, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        parent.addView(volumeRow);

        String[] sounds = SoundManager.SOUND_TYPES;
        String activeSound = prefs.getString("sound_type", "System");
        int count = isSoundsExpanded ? sounds.length : 4;
        
        LinearLayout currentRow = null;
        for (int i = 0; i < count; i++) {
            if (i % 2 == 0) {
                currentRow = new LinearLayout(context);
                currentRow.setOrientation(LinearLayout.HORIZONTAL);
                currentRow.setWeightSum(2f);
                parent.addView(currentRow, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            
            final String soundName = sounds[i];
            boolean isApplied = soundName.equals(activeSound);
            
            LinearLayout itemBox = createGridItemBox(soundName, isApplied, false);
            itemBox.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                    prefs.edit().putString("sound_type", soundName).apply();
                    sampleSoundManager.playSoundAndHaptic(null, "SPACE"); 
                    render();
                }
            });
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            params.setMargins(dpToPx(6), dpToPx(6), dpToPx(6), dpToPx(6));
            currentRow.addView(itemBox, params);
        }
    }

    private void buildEffectsGrid(LinearLayout parent) {
        String[] effects = TouchEffectManager.EFFECT_TYPES;
        String activeEffect = prefs.getString("touch_effect_type", "None");
        int count = isEffectsExpanded ? effects.length : 4;
        
        LinearLayout currentRow = null;
        for (int i = 0; i < count; i++) {
            if (i % 2 == 0) {
                currentRow = new LinearLayout(context);
                currentRow.setOrientation(LinearLayout.HORIZONTAL);
                currentRow.setWeightSum(2f);
                parent.addView(currentRow, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            
            final String effectName = effects[i];
            boolean isApplied = effectName.equals(activeEffect);
            
            LinearLayout itemBox = new LinearLayout(context);
            itemBox.setOrientation(LinearLayout.VERTICAL);
            itemBox.setGravity(Gravity.CENTER);
            itemBox.setPadding(dpToPx(8), dpToPx(12), dpToPx(8), dpToPx(12));
            
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(dpToPx(10));
            bg.setColor(Color.parseColor(isApplied ? "#E3F2FD" : "#F5F7FA"));
            bg.setStroke(dpToPx(isApplied ? 2 : 1), Color.parseColor(isApplied ? UIHelpers.COLOR_ACCENT : "#CFD8DC"));
            itemBox.setBackground(bg);

            View previewView = createEffectPreviewView(effectName, isApplied);
            LinearLayout.LayoutParams pParams = new LinearLayout.LayoutParams(dpToPx(36), dpToPx(36));
            pParams.bottomMargin = dpToPx(8);
            itemBox.addView(previewView, pParams);

            TextView title = new TextView(context);
            title.setText(effectName);
            title.setTextColor(Color.parseColor(isApplied ? UIHelpers.COLOR_ACCENT : UIHelpers.COLOR_TEXT_PRIMARY));
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            title.setGravity(Gravity.CENTER);
            itemBox.addView(title);
            
            itemBox.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                    prefs.edit().putString("touch_effect_type", effectName).apply();
                    render();
                }
            });
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            params.setMargins(dpToPx(6), dpToPx(6), dpToPx(6), dpToPx(6));
            currentRow.addView(itemBox, params);
        }
    }

    private View createEffectPreviewView(final String effectName, final boolean isApplied) {
        return new View(context) {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            float animPhase = 0f;

            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                animPhase += 0.05f;
                if (animPhase > 1.0f) animPhase = 0f;

                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                int color = Color.parseColor(isApplied ? UIHelpers.COLOR_ACCENT : "#90A4AE");
                paint.setColor(color);
                
                if (effectName.equals("None")) {
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(dpToPx(1.5f));
                    canvas.drawLine(cx-5, cy-5, cx+5, cy+5, paint);
                } else if (effectName.equals("Pulse") || effectName.equals("Neon")) {
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(dpToPx(2));
                    float radius = dpToPx(4) + (animPhase * dpToPx(12));
                    paint.setAlpha((int)(255 * (1.0f - animPhase)));
                    canvas.drawCircle(cx, cy, radius, paint);
                } else if (effectName.equals("Sparkle") || effectName.equals("Stars")) {
                    paint.setStyle(Paint.Style.FILL);
                    float offset = animPhase * dpToPx(10);
                    canvas.drawCircle(cx + offset, cy - offset, dpToPx(2), paint);
                    canvas.drawCircle(cx - offset, cy + offset, dpToPx(2), paint);
                    canvas.drawCircle(cx, cy, dpToPx(3), paint);
                } else if (effectName.equals("Ripple") || effectName.equals("Rings")) {
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(dpToPx(1));
                    canvas.drawCircle(cx, cy, dpToPx(4) + (animPhase * dpToPx(10)), paint);
                    canvas.drawCircle(cx, cy, dpToPx(2) + (animPhase * dpToPx(5)), paint);
                } else if (effectName.equals("Burst")) {
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(dpToPx(1.5f));
                    for(int i=0; i<6; i++) {
                        double ang = (Math.PI*2*i)/6;
                        float r1 = dpToPx(3) + (animPhase*dpToPx(5));
                        float r2 = r1 + dpToPx(4);
                        canvas.drawLine(cx+(float)Math.cos(ang)*r1, cy+(float)Math.sin(ang)*r1, cx+(float)Math.cos(ang)*r2, cy+(float)Math.sin(ang)*r2, paint);
                    }
                }
                
                postInvalidateOnAnimation(); 
            }
        };
    }

    private LinearLayout createHeaderWithAction(String titleStr, String actionStr, OnClickListener actionListener) {
        LinearLayout hl = new LinearLayout(context);
        hl.setOrientation(LinearLayout.HORIZONTAL);
        hl.setGravity(Gravity.CENTER_VERTICAL);
        hl.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(8));
        TextView t = new TextView(context);
        t.setText(titleStr); t.setTextColor(Color.parseColor("#78909C"));
        t.setTextSize(12); t.setTypeface(null, Typeface.BOLD);
        hl.addView(t, new LinearLayout.LayoutParams(0, -2, 1f));
        TextView a = new TextView(context);
        a.setText(actionStr); a.setTextColor(Color.parseColor(UIHelpers.COLOR_ACCENT));
        a.setTextSize(12); a.setTypeface(null, Typeface.BOLD);
        a.setPadding(dpToPx(8), dpToPx(4), dpToPx(4), dpToPx(4));
        a.setOnClickListener(actionListener);
        hl.addView(a);
        return hl;
    }

    private LinearLayout createMainCard() {
        LinearLayout c = new LinearLayout(context);
        c.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable g = new GradientDrawable();
        g.setColor(Color.parseColor(UIHelpers.COLOR_CARD));
        g.setCornerRadius(dpToPx(12));
        c.setBackground(g);
        if (Build.VERSION.SDK_INT >= 21) c.setElevation(dpToPx(2));
        c.setPadding(dpToPx(8), dpToPx(12), dpToPx(8), dpToPx(12));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(dpToPx(12), 0, dpToPx(12), dpToPx(8));
        c.setLayoutParams(p);
        return c;
    }

    private LinearLayout createGridItemBox(String titleStr, boolean isApplied, boolean isTheme) {
        LinearLayout b = new LinearLayout(context);
        b.setOrientation(LinearLayout.VERTICAL); 
        b.setGravity(Gravity.CENTER);
        b.setPadding(dpToPx(8), dpToPx(14), dpToPx(8), dpToPx(14));
        
        GradientDrawable g = new GradientDrawable();
        g.setCornerRadius(dpToPx(8));
        g.setColor(Color.parseColor(isApplied ? "#E3F2FD" : "#F5F7FA"));
        g.setStroke(dpToPx(isApplied ? 2 : 1), Color.parseColor(isApplied ? UIHelpers.COLOR_ACCENT : "#CFD8DC"));
        b.setBackground(g);
        
        TextView t = new TextView(context);
        t.setText(titleStr); 
        t.setTextColor(Color.parseColor(isApplied ? UIHelpers.COLOR_ACCENT : UIHelpers.COLOR_TEXT_PRIMARY));
        t.setTextSize(isTheme ? 11 : 12); 
        t.setTypeface(null, isApplied ? Typeface.BOLD : Typeface.NORMAL);
        t.setGravity(Gravity.CENTER);
        b.addView(t);
        return b;
    }

    private int dpToPx(float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }
}