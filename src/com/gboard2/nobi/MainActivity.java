package com.gboard2.nobi;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private FrameLayout contentContainer;
    private HomeTab tabHome;
    private ThemesTab tabThemes;
    private SettingsTab tabSettings;
    
    private TextView textHome, textThemes, textSettings;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        prefs = getSharedPreferences("GboardPrefs", Context.MODE_PRIVATE);
        
        setupModernLayout();
    }

    private void setupModernLayout() {
        FrameLayout rootFrame = new FrameLayout(this);
        rootFrame.setBackgroundColor(Color.parseColor(UIHelpers.COLOR_BG));
        
        // --- MAIN CONTENT (Header + Tabs) ---
        LinearLayout mainContent = new LinearLayout(this);
        mainContent.setOrientation(LinearLayout.VERTICAL);
        
        // Horizontal Header Row
        LinearLayout headerRow = new LinearLayout(this);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);
        headerRow.setPadding(UIHelpers.dpToPx(this, 24), UIHelpers.dpToPx(this, 32), UIHelpers.dpToPx(this, 24), UIHelpers.dpToPx(this, 16));
        
        // Title Layout (Left side)
        LinearLayout titleLayout = new LinearLayout(this);
        titleLayout.setOrientation(LinearLayout.VERTICAL);
        
        TextView title = new TextView(this);
        title.setText("Gboard Pro");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
        title.setTextColor(Color.parseColor(UIHelpers.COLOR_TEXT_PRIMARY));
        title.setTypeface(null, Typeface.BOLD);
        
        TextView subtitle = new TextView(this);
        subtitle.setText("Customize your typing experience");
        subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        subtitle.setTextColor(Color.parseColor(UIHelpers.COLOR_TEXT_SECONDARY));
        subtitle.setPadding(0, UIHelpers.dpToPx(this, 4), 0, 0);
        
        titleLayout.addView(title);
        titleLayout.addView(subtitle);
        
        headerRow.addView(titleLayout, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        // --- CUSTOM DRAWN KEYBOARD ICON (Top Right Corner) ---
        LinearLayout testIconContainer = new LinearLayout(this);
        testIconContainer.setGravity(Gravity.CENTER);
        
        GradientDrawable bgCircle = new GradientDrawable();
        bgCircle.setShape(GradientDrawable.OVAL);
        bgCircle.setColor(Color.parseColor("#E3F2FD")); // Light blue accent background
        if (Build.VERSION.SDK_INT >= 16) testIconContainer.setBackground(bgCircle);
        else testIconContainer.setBackgroundDrawable(bgCircle);

        // Drawing custom keyboard shape directly via Canvas (No emojis/symbols)
        View customKeyboardIcon = new View(this) {
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                int pad = UIHelpers.dpToPx(getContext(), 4);
                float w = getWidth() - pad * 2;
                float h = getHeight() - pad * 2;
                float top = pad + h * 0.15f;
                h = h * 0.7f; 
                
                p.setColor(Color.parseColor(UIHelpers.COLOR_ACCENT));
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(UIHelpers.dpToPx(getContext(), 1.5f));
                canvas.drawRoundRect(new RectF(pad, top, pad + w, top + h), UIHelpers.dpToPx(getContext(), 3), UIHelpers.dpToPx(getContext(), 3), p);

                p.setStyle(Paint.Style.FILL);
                float kw = w * 0.15f; float kh = h * 0.2f;
                float gapX = w * 0.08f;
                float gapY = h * 0.15f;
                
                for(int i=0; i<4; i++) {
                    float kx = pad + gapX + i * (kw + gapX);
                    float ky = top + gapY;
                    canvas.drawRoundRect(new RectF(kx, ky, kx + kw, ky + kh), 1, 1, p);
                }
                float sx = pad + w * 0.25f;
                float sy = top + h - gapY - kh; float sw = w * 0.5f;
                canvas.drawRoundRect(new RectF(sx, sy, sx + sw, sy + kh), 1, 1, p);
            }
        };

        testIconContainer.addView(customKeyboardIcon, new LinearLayout.LayoutParams(UIHelpers.dpToPx(this, 26), UIHelpers.dpToPx(this, 26)));

        testIconContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                showTestKeyboardDialog();
            }
        });

        // Add container to header
        headerRow.addView(testIconContainer, new LinearLayout.LayoutParams(UIHelpers.dpToPx(this, 44), UIHelpers.dpToPx(this, 44)));
        mainContent.addView(headerRow);

        // --- CONTENT CONTAINER ---
        contentContainer = new FrameLayout(this);
        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        mainContent.addView(contentContainer, contentParams);

        // Initialize Tabs
        tabHome = new HomeTab(this, prefs, new Runnable() {
            @Override
            public void run() {
                switchTab(1);
            }
        });
        tabThemes = new ThemesTab(this, prefs);
        tabSettings = new SettingsTab(this, prefs);
        
        contentContainer.addView(tabHome);
        contentContainer.addView(tabThemes);
        contentContainer.addView(tabSettings);

        rootFrame.addView(mainContent, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // --- FLOATING BOTTOM NAVIGATION ---
        LinearLayout bottomNavWrapper = new LinearLayout(this);
        bottomNavWrapper.setPadding(UIHelpers.dpToPx(this, 20), UIHelpers.dpToPx(this, 10), UIHelpers.dpToPx(this, 20), UIHelpers.dpToPx(this, 20));
        bottomNavWrapper.setGravity(Gravity.CENTER);
        
        bottomNavWrapper.setClipChildren(false);
        bottomNavWrapper.setClipToPadding(false);

        LinearLayout bottomNav = new LinearLayout(this);
        bottomNav.setOrientation(LinearLayout.HORIZONTAL);
        bottomNav.setWeightSum(3f); // Changed from 4f to 3f

        GradientDrawable navBg = new GradientDrawable();
        navBg.setColor(Color.parseColor(UIHelpers.COLOR_CARD));
        navBg.setCornerRadius(UIHelpers.dpToPx(this, 30));
        if (Build.VERSION.SDK_INT >= 21) bottomNav.setElevation(UIHelpers.dpToPx(this, 15)); 
        bottomNav.setBackground(navBg);
        bottomNav.setPadding(UIHelpers.dpToPx(this, 8), UIHelpers.dpToPx(this, 8), UIHelpers.dpToPx(this, 8), UIHelpers.dpToPx(this, 8));

        bottomNav.addView(createNavItem("🏠", "Home", 0), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        bottomNav.addView(createNavItem("🎨", "Themes", 1), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        bottomNav.addView(createNavItem("⚙️", "Settings", 2), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)); // Index changed to 2

        bottomNavWrapper.addView(bottomNav, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        FrameLayout.LayoutParams navWrapperParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        navWrapperParams.gravity = Gravity.BOTTOM;
        rootFrame.addView(bottomNavWrapper, navWrapperParams);

        setContentView(rootFrame);
        switchTab(0);
    }

    private void showTestKeyboardDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(UIHelpers.dpToPx(this, 20), UIHelpers.dpToPx(this, 20), UIHelpers.dpToPx(this, 20), UIHelpers.dpToPx(this, 20));
        
        TextView title = new TextView(this);
        title.setText("Test Your Keyboard");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(Color.parseColor(UIHelpers.COLOR_TEXT_PRIMARY));
        title.setPadding(0, 0, 0, UIHelpers.dpToPx(this, 16));
        container.addView(title);
        
        final EditText editText = new EditText(this);
        editText.setHint("Type here to test themes & sounds...");
        editText.setTextColor(Color.parseColor(UIHelpers.COLOR_TEXT_PRIMARY));
        editText.setHintTextColor(Color.parseColor(UIHelpers.COLOR_TEXT_SECONDARY));
        
        GradientDrawable edBg = new GradientDrawable();
        edBg.setColor(Color.parseColor("#F5F7FA"));
        edBg.setCornerRadius(UIHelpers.dpToPx(this, 8));
        edBg.setStroke(UIHelpers.dpToPx(this, 1), Color.parseColor("#CFD8DC"));
        if (Build.VERSION.SDK_INT >= 16) editText.setBackground(edBg);
        else editText.setBackgroundDrawable(edBg);
        
        editText.setPadding(UIHelpers.dpToPx(this, 16), UIHelpers.dpToPx(this, 16), UIHelpers.dpToPx(this, 16), UIHelpers.dpToPx(this, 16));
        editText.setLines(4);
        editText.setGravity(Gravity.TOP | Gravity.START);
        
        container.addView(editText);
        builder.setView(container);
        
        final AlertDialog dialog = builder.create();
        
        // Remove default white background of the dialog to match theme
        if (dialog.getWindow() != null) {
            GradientDrawable windowBg = new GradientDrawable();
            windowBg.setColor(Color.parseColor(UIHelpers.COLOR_CARD));
            windowBg.setCornerRadius(UIHelpers.dpToPx(this, 16));
            dialog.getWindow().setBackgroundDrawable(windowBg);
        }
        
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface d) {
                editText.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        dialog.show();
    }

    private LinearLayout createNavItem(String iconStr, final String label, final int index) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER);
        item.setPadding(0, UIHelpers.dpToPx(this, 8), 0, UIHelpers.dpToPx(this, 8));

        TextView icon = new TextView(this);
        icon.setText(iconStr);
        icon.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        icon.setGravity(Gravity.CENTER);

        TextView text = new TextView(this);
        text.setText(label);
        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        text.setGravity(Gravity.CENTER);
        text.setTypeface(null, Typeface.BOLD);
        text.setPadding(0, UIHelpers.dpToPx(this, 2), 0, 0);

        if (index == 0) textHome = text;
        else if (index == 1) textThemes = text;
        else if (index == 2) textSettings = text; // Mapped to 2 instead of 3

        item.addView(icon);
        item.addView(text);

        item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                switchTab(index);
            }
        });

        item.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).start();
                else if(event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                return false;
            }
        });

        return item;
    }

    private void switchTab(int index) {
        tabHome.setVisibility(View.GONE);
        tabThemes.setVisibility(View.GONE);
        tabSettings.setVisibility(View.GONE);
        
        textHome.setTextColor(Color.parseColor(UIHelpers.COLOR_TEXT_SECONDARY));
        textThemes.setTextColor(Color.parseColor(UIHelpers.COLOR_TEXT_SECONDARY));
        textSettings.setTextColor(Color.parseColor(UIHelpers.COLOR_TEXT_SECONDARY));

        View activeTab = null;
        if (index == 0) {
            activeTab = tabHome;
            textHome.setTextColor(Color.parseColor(UIHelpers.COLOR_ACCENT));
            tabHome.render();
        } else if (index == 1) {
            activeTab = tabThemes;
            textThemes.setTextColor(Color.parseColor(UIHelpers.COLOR_ACCENT));
            tabThemes.render();
        } else if (index == 2) {
            activeTab = tabSettings;
            textSettings.setTextColor(Color.parseColor(UIHelpers.COLOR_ACCENT));
        }

        if (activeTab != null) {
            activeTab.setVisibility(View.VISIBLE);
            activeTab.setAlpha(0f);
            activeTab.setTranslationY(UIHelpers.dpToPx(this, 20));
            activeTab.animate().alpha(1f).translationY(0).setDuration(300).start();
        }
    }
}
