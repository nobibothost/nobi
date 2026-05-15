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
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

public class ThemesTab extends ScrollView {

    private Context context;
    private SharedPreferences prefs;
    private LinearLayout gridContainer; // The container holding the theme cards

    public ThemesTab(Context context, SharedPreferences prefs) {
        super(context);
        this.context = context;
        this.prefs = prefs;

        setClipToPadding(false);
        // Changed top padding to 0 so the header is flush with the top
        setPadding(0, 0, 0, UIHelpers.dpToPx(context, 100));
        
        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setOrientation(LinearLayout.VERTICAL);

        // --- NEW: TAB HEADER ---
        LinearLayout headerContainer = new LinearLayout(context);
        headerContainer.setOrientation(LinearLayout.VERTICAL);
        headerContainer.setPadding(UIHelpers.dpToPx(context, 24), UIHelpers.dpToPx(context, 24), UIHelpers.dpToPx(context, 24), UIHelpers.dpToPx(context, 16));

        TextView title = new TextView(context);
        title.setText("Themes");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28f);
        title.setTextColor(Color.parseColor("#111111"));
        title.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        headerContainer.addView(title);

        TextView subtitle = new TextView(context);
        subtitle.setText("Customize the look, colors, and background of your keyboard.");
        subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        subtitle.setTextColor(Color.parseColor("#78909C"));
        subtitle.setPadding(0, UIHelpers.dpToPx(context, 4), 0, 0);
        headerContainer.addView(subtitle);

        mainLayout.addView(headerContainer);

        // Grid Container to hold your original theme cards
        gridContainer = new LinearLayout(context);
        gridContainer.setOrientation(LinearLayout.VERTICAL);
        gridContainer.setPadding(UIHelpers.dpToPx(context, 10), 0, UIHelpers.dpToPx(context, 10), 0);
        mainLayout.addView(gridContainer);
        
        addView(mainLayout);
        
        render();
    }

    public void render() {
        gridContainer.removeAllViews(); // Clear only the grid items, header stays intact

        List<ThemeData.Theme> themes = ThemeData.getThemes();
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
            
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(Color.parseColor(UIHelpers.COLOR_CARD));
            bg.setCornerRadius(UIHelpers.dpToPx(context, 12));
            if (isApplied) {
                bg.setStroke(UIHelpers.dpToPx(context, 2), Color.parseColor(UIHelpers.COLOR_ACCENT));
            }
            if (Build.VERSION.SDK_INT >= 16) card.setBackground(bg);
            else card.setBackgroundDrawable(bg);

            if (Build.VERSION.SDK_INT >= 21) card.setElevation(UIHelpers.dpToPx(context, 2));
            
            card.setPadding(UIHelpers.dpToPx(context, 12), UIHelpers.dpToPx(context, 12), UIHelpers.dpToPx(context, 12), UIHelpers.dpToPx(context, 12));

            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
            cardParams.setMargins(UIHelpers.dpToPx(context, 10), UIHelpers.dpToPx(context, 10), UIHelpers.dpToPx(context, 10), UIHelpers.dpToPx(context, 10));
            card.setLayoutParams(cardParams);

            View preview = createKeyboardPreview(theme);
            card.addView(preview, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UIHelpers.dpToPx(context, 80)));
            
            LinearLayout infoRow = new LinearLayout(context);
            infoRow.setOrientation(LinearLayout.VERTICAL);
            infoRow.setGravity(Gravity.CENTER);
            infoRow.setPadding(0, UIHelpers.dpToPx(context, 12), 0, 0);
            
            TextView name = new TextView(context);
            name.setText(theme.name);
            name.setTextColor(Color.parseColor(isApplied ? UIHelpers.COLOR_ACCENT : UIHelpers.COLOR_TEXT_PRIMARY));
            name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            name.setTypeface(null, Typeface.BOLD);
            name.setGravity(Gravity.CENTER);
            infoRow.addView(name, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            
            LinearLayout badgeContainer = new LinearLayout(context);
            badgeContainer.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UIHelpers.dpToPx(context, 24));
            badgeParams.topMargin = UIHelpers.dpToPx(context, 4);

            if (isApplied) {
                TextView badge = new TextView(context);
                badge.setText("ACTIVE");
                badge.setTextColor(Color.WHITE);
                badge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9);
                badge.setTypeface(null, Typeface.BOLD);
                badge.setPadding(UIHelpers.dpToPx(context, 8), UIHelpers.dpToPx(context, 2), UIHelpers.dpToPx(context, 8), UIHelpers.dpToPx(context, 2));
                
                GradientDrawable badgeBg = new GradientDrawable();
                badgeBg.setColor(Color.parseColor(UIHelpers.COLOR_ACCENT));
                badgeBg.setCornerRadius(UIHelpers.dpToPx(context, 10));
                if (Build.VERSION.SDK_INT >= 16) badge.setBackground(badgeBg);
                else badge.setBackgroundDrawable(badgeBg);
                
                badgeContainer.addView(badge);
            }
            
            infoRow.addView(badgeContainer, badgeParams);
            card.addView(infoRow);
            
            card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                    prefs.edit().putString("selected_theme", theme.name).apply();
                    render(); 
                }
            });

            currentRow.addView(card);
        }

        if (themes.size() % 2 != 0 && currentRow != null) {
            View dummy = new View(context);
            LinearLayout.LayoutParams dummyParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
            currentRow.addView(dummy, dummyParams);
        }
    }

    private View createKeyboardPreview(final ThemeData.Theme theme) {
        return new View(context) {
            Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            Paint keyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            Paint specPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            Paint entPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);

                bgPaint.setColor(theme.bgColor);
                keyPaint.setColor(theme.keyBgColor);
                specPaint.setColor(theme.specialKeyBgColor);
                entPaint.setColor(theme.enterBgColor);
                
                int w = getWidth(); int h = getHeight();

                if (theme.backgroundImageRes != 0) {
                    try {
                        Drawable bgDrawable = context.getResources().getDrawable(theme.backgroundImageRes, null);
                        if (bgDrawable != null) {
                            bgDrawable.setBounds(0, 0, w, h);
                            Path clipPath = new Path();
                            clipPath.addRoundRect(new RectF(0, 0, w, h), UIHelpers.dpToPx(context, 6), UIHelpers.dpToPx(context, 6), Path.Direction.CW);
                            canvas.save();
                            canvas.clipPath(clipPath);
                            bgDrawable.draw(canvas);
                            canvas.restore();
                        }
                    } catch (Exception e) {
                        canvas.drawRoundRect(new RectF(0, 0, w, h), UIHelpers.dpToPx(context, 6), UIHelpers.dpToPx(context, 6), bgPaint);
                    }
                } else {
                    canvas.drawRoundRect(new RectF(0, 0, w, h), UIHelpers.dpToPx(context, 6), UIHelpers.dpToPx(context, 6), bgPaint);
                }
                
                float pad = UIHelpers.dpToPx(context, 2.5f);
                float keyH = (h - (pad * 5)) / 4f;
                float keyW = (w - (pad * 11)) / 10f;
                float rad = UIHelpers.dpToPx(context, 3);
                
                float startX = pad; float y = pad;

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
                startX = pad; y += keyH + pad;

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
    }
}