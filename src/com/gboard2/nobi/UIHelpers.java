package com.gboard2.nobi;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class UIHelpers {

    // Premium Colors
    public static final String COLOR_BG = "#F2F2F7";
    public static final String COLOR_CARD = "#FFFFFF";
    public static final String COLOR_TEXT_PRIMARY = "#000000";
    public static final String COLOR_TEXT_SECONDARY = "#8E8E93";
    public static final String COLOR_ACCENT = "#007AFF"; 
    public static final String COLOR_TOGGLE_ON = "#34C759";
    public static final String COLOR_TOGGLE_OFF = "#E5E5EA";

    public static int dpToPx(Context context, float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    public static View createModernToggle(final Context context, final SharedPreferences prefs, final String prefKey, boolean defaultValue, final Runnable onToggle) {
        final FrameLayout toggleContainer = new FrameLayout(context);
        final boolean[] isChecked = {prefs.getBoolean(prefKey, defaultValue)};
        
        final GradientDrawable trackBg = new GradientDrawable();
        trackBg.setCornerRadius(dpToPx(context, 16));
        trackBg.setColor(isChecked[0] ? Color.parseColor(COLOR_TOGGLE_ON) : Color.parseColor(COLOR_TOGGLE_OFF));
        
        final View track = new View(context);
        if (Build.VERSION.SDK_INT >= 16) track.setBackground(trackBg);
        else track.setBackgroundDrawable(trackBg);
        
        FrameLayout.LayoutParams trackParams = new FrameLayout.LayoutParams(dpToPx(context, 50), dpToPx(context, 30));
        trackParams.gravity = Gravity.CENTER_VERTICAL;
        toggleContainer.addView(track, trackParams);
        
        final View thumb = new View(context);
        GradientDrawable thumbBg = new GradientDrawable();
        thumbBg.setShape(GradientDrawable.OVAL);
        thumbBg.setColor(Color.WHITE);
        if (Build.VERSION.SDK_INT >= 21) thumb.setElevation(dpToPx(context, 2));
        
        if (Build.VERSION.SDK_INT >= 16) thumb.setBackground(thumbBg);
        else thumb.setBackgroundDrawable(thumbBg);
        
        final FrameLayout.LayoutParams thumbParams = new FrameLayout.LayoutParams(dpToPx(context, 26), dpToPx(context, 26));
        thumbParams.gravity = Gravity.CENTER_VERTICAL;
        thumbParams.leftMargin = dpToPx(context, 2);
        toggleContainer.addView(thumb, thumbParams);
        
        thumb.setTranslationX(isChecked[0] ? dpToPx(context, 20) : 0);

        toggleContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                isChecked[0] = !isChecked[0];
                prefs.edit().putBoolean(prefKey, isChecked[0]).apply();
                
                thumb.animate().translationX(isChecked[0] ? dpToPx(context, 20) : 0).setDuration(250).start();
                
                int colorFrom = isChecked[0] ? Color.parseColor(COLOR_TOGGLE_OFF) : Color.parseColor(COLOR_TOGGLE_ON);
                int colorTo = isChecked[0] ? Color.parseColor(COLOR_TOGGLE_ON) : Color.parseColor(COLOR_TOGGLE_OFF);
                
                ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                colorAnimation.setDuration(250);
                colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        trackBg.setColor((int) animator.getAnimatedValue());
                        track.invalidate();
                    }
                });
                colorAnimation.start();
                
                if (onToggle != null) onToggle.run();
            }
        });
        
        return toggleContainer;
    }

    public static View createSettingRow(Context context, String iconStr, String iconBgColor, String title, String subtitle, View accessory) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dpToPx(context, 16), dpToPx(context, 16), dpToPx(context, 16), dpToPx(context, 16));
        
        TextView iconView = new TextView(context);
        iconView.setText(iconStr);
        iconView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        iconView.setGravity(Gravity.CENTER);
        GradientDrawable iconBg = new GradientDrawable();
        iconBg.setColor(Color.parseColor(iconBgColor));
        iconBg.setCornerRadius(dpToPx(context, 8));
        if (Build.VERSION.SDK_INT >= 16) iconView.setBackground(iconBg);
        else iconView.setBackgroundDrawable(iconBg);
        
        row.addView(iconView, new LinearLayout.LayoutParams(dpToPx(context, 32), dpToPx(context, 32)));
        
        LinearLayout textLayout = new LinearLayout(context);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        textLayout.setPadding(dpToPx(context, 16), 0, dpToPx(context, 16), 0);
        
        TextView titleView = new TextView(context);
        titleView.setText(title);
        titleView.setTextColor(Color.parseColor(COLOR_TEXT_PRIMARY));
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        titleView.setTypeface(null, Typeface.BOLD);
        textLayout.addView(titleView);
        
        if (subtitle != null && !subtitle.isEmpty()) {
            TextView subView = new TextView(context);
            subView.setText(subtitle);
            subView.setTextColor(Color.parseColor(COLOR_TEXT_SECONDARY));
            subView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            subView.setPadding(0, dpToPx(context, 2), 0, 0);
            textLayout.addView(subView);
        }
        
        row.addView(textLayout, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        if (accessory != null) row.addView(accessory);
        
        return row;
    }

    public static LinearLayout createCardContainer(Context context) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor(COLOR_CARD));
        bg.setCornerRadius(dpToPx(context, 16));
        if (Build.VERSION.SDK_INT >= 16) card.setBackground(bg);
        else card.setBackgroundDrawable(bg);
        
        if (Build.VERSION.SDK_INT >= 21) card.setElevation(dpToPx(context, 2));
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(dpToPx(context, 20), 0, dpToPx(context, 20), dpToPx(context, 20));
        card.setLayoutParams(params);
        
        return card;
    }

    public static View createDivider(Context context) {
        View line = new View(context);
        line.setBackgroundColor(Color.parseColor("#E5E5EA"));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(context, 1));
        params.setMargins(dpToPx(context, 64), 0, 0, 0); 
        line.setLayoutParams(params);
        return line;
    }

    public static TextView createSectionHeader(Context context, String title) {
        TextView header = new TextView(context);
        header.setText(title);
        header.setTextColor(Color.parseColor(COLOR_TEXT_SECONDARY));
        header.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        header.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(dpToPx(context, 36), dpToPx(context, 16), dpToPx(context, 20), dpToPx(context, 8));
        header.setLayoutParams(params);
        return header;
    }

    public static View getArrowIcon(Context context) {
        TextView arrow = new TextView(context);
        arrow.setText("⟩");
        arrow.setTextColor(Color.parseColor("#C7C7CC"));
        arrow.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        arrow.setTypeface(null, Typeface.BOLD);
        return arrow;
    }
}
