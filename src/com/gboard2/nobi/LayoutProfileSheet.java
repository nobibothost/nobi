package com.gboard2.nobi;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class LayoutProfileSheet extends FrameLayout {
    private ProKeyboardView pkv;
    private CustomKeyManager keyManager;
    private LinearLayout listContainer;
    private boolean isShowing = false;
    private GradientDrawable bgDrawable;

    public LayoutProfileSheet(Context context, ProKeyboardView pkv) {
        super(context);
        this.pkv = pkv;
        this.keyManager = CustomKeyManager.getInstance(context);

        setVisibility(View.GONE);
        setClickable(true);

        bgDrawable = new GradientDrawable();
        bgDrawable.setColor(Color.parseColor("#FAFAFA"));
        bgDrawable.setCornerRadii(new float[]{dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16), 0, 0, 0, 0});
        if (Build.VERSION.SDK_INT >= 16) {
            setBackground(bgDrawable);
        } else {
            setBackgroundDrawable(bgDrawable);
        }

        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(0, dpToPx(12), 0, 0);

        // Header
        LinearLayout header = new LinearLayout(context);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(16));

        TextView title = new TextView(context);
        title.setText("Choose Layout Profile");
        title.setTextColor(Color.parseColor("#111111"));
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        title.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        header.addView(title, titleParams);

        TextView closeBtn = new TextView(context);
        closeBtn.setText("✖");
        closeBtn.setTextColor(Color.parseColor("#757575"));
        closeBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        closeBtn.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
        closeBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });
        header.addView(closeBtn);

        mainLayout.addView(header);

        ScrollView scroll = new ScrollView(context);
        listContainer = new LinearLayout(context);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        listContainer.setPadding(dpToPx(16), 0, dpToPx(16), dpToPx(16));
        scroll.addView(listContainer);

        mainLayout.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addView(mainLayout, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public void applyTheme(ThemeData.Theme theme) {
        if (theme != null) {
            bgDrawable.setColor(theme.suggestionBgColor);
            if (Build.VERSION.SDK_INT >= 16) setBackground(bgDrawable);
            else setBackgroundDrawable(bgDrawable);
        }
    }

    private void loadProfiles() {
        listContainer.removeAllViews();
        List<String> profiles = keyManager.getSavedProfiles();
        String activeProfile = keyManager.getActiveProfile();

        for (final String profile : profiles) {
            final TextView item = new TextView(getContext());
            item.setText(profile);
            item.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
            item.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.bottomMargin = dpToPx(8);
            
            if (profile.equals(activeProfile)) {
                item.setTextColor(Color.parseColor("#4A90E2"));
                item.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                
                GradientDrawable activeBg = new GradientDrawable();
                activeBg.setColor(Color.parseColor("#F0F8FF")); 
                activeBg.setCornerRadius(dpToPx(8));
                if (Build.VERSION.SDK_INT >= 16) item.setBackground(activeBg);
                else item.setBackgroundDrawable(activeBg);
            } else {
                item.setTextColor(Color.parseColor("#424242"));
                item.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            }

            item.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!profile.equals(keyManager.getActiveProfile())) {
                        keyManager.setActiveProfile(profile);
                        if (pkv != null) {
                            pkv.layoutManager.buildKeys();
                            pkv.postInvalidateOnAnimation();
                        }
                        Toast.makeText(getContext(), "Layout changed to: " + profile, Toast.LENGTH_SHORT).show();
                    }
                    hide();
                }
            });
            listContainer.addView(item, params);
        }
    }

    public void show() {
        if (!isShowing) {
            loadProfiles();
            setVisibility(View.VISIBLE);
            setAlpha(0f);
            setTranslationY(getHeight() > 0 ? getHeight() : dpToPx(300));
            animate().alpha(1f).translationY(0).setDuration(250).start();
            isShowing = true;
        }
    }

    public void hide() {
        if (isShowing) {
            animate().alpha(0f).translationY(getHeight() > 0 ? getHeight() : dpToPx(300)).setDuration(200).withEndAction(new Runnable() {
                @Override
                public void run() {
                    setVisibility(View.GONE);
                }
            }).start();
            isShowing = false;
        }
    }

    public boolean isShowing() {
        return isShowing;
    }

    private int dpToPx(float dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }
}