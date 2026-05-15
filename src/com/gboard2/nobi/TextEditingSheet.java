package com.gboard2.nobi;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TextEditingSheet extends FrameLayout {

    private Context context;
    private ProKeyboardView.KeyboardListener listener;
    
    private LinearLayout sheetContainer;
    private LinearLayout toolbar;
    private TextView titleText;
    private ImageView backBtn;
    private Drawable backIcon;
    private LinearLayout controlsContainer;
    
    private boolean isShowing = false;
    private float revealCx = 0f;
    private float revealCy = 0f;
    
    private boolean isSelectModeOn = false;
    private TextView selectBtn; 
    private ThemeData.Theme currentTheme = ThemeData.getThemeByName("Default");
    
    private Handler repeatHandler = new Handler(Looper.getMainLooper());
    private Runnable activeRepeatRunnable;

    public TextEditingSheet(Context context, ProKeyboardView.KeyboardListener listener) {
        super(context);
        this.context = context;
        this.listener = listener;
        setupUI();
    }
    
    public void applyTheme(ThemeData.Theme theme) {
        if (theme == null) return;
        this.currentTheme = theme;
        
        if (sheetContainer != null) sheetContainer.setBackgroundColor(theme.bgColor);
        if (toolbar != null) toolbar.setBackgroundColor(theme.suggestionBgColor);
        if (titleText != null) titleText.setTextColor(theme.suggestionTextColor);
        if (backIcon != null) backIcon.setTint(theme.suggestionTextColor);
        
        if (controlsContainer != null) {
            controlsContainer.removeAllViews();
            buildGrid();
        }
        updateSelectBtnVisuals();
    }

    private int dpToPx(float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    private void setupUI() {
        this.setVisibility(GONE);

        sheetContainer = new LinearLayout(context);
        sheetContainer.setOrientation(LinearLayout.VERTICAL);
        sheetContainer.setBackgroundColor(currentTheme.bgColor);
        LayoutParams sheetParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        sheetParams.topMargin = 0; 
        addView(sheetContainer, sheetParams);

        int barHeight = dpToPx(48);

        toolbar = new LinearLayout(context);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        toolbar.setBackgroundColor(currentTheme.suggestionBgColor);
        toolbar.setPadding(dpToPx(8), 0, dpToPx(8), 0);

        backBtn = new ImageView(context);
        backIcon = context.getResources().getDrawable(R.drawable.ic_back_arrow, null);
        if (backIcon != null) backIcon.setTint(currentTheme.suggestionTextColor);
        backBtn.setImageDrawable(backIcon);
        backBtn.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));
        toolbar.addView(backBtn, new LinearLayout.LayoutParams(dpToPx(48), dpToPx(48)));

        backBtn.setOnClickListener(new OnClickListener() { @Override public void onClick(View v) { hide(); } });

        titleText = new TextView(context);
        titleText.setText("Text editing");
        titleText.setTextColor(currentTheme.suggestionTextColor);
        titleText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        titleText.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        titleParams.leftMargin = dpToPx(12);
        toolbar.addView(titleText, titleParams);

        sheetContainer.addView(toolbar, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, barHeight));

        controlsContainer = new LinearLayout(context);
        controlsContainer.setOrientation(LinearLayout.VERTICAL);
        controlsContainer.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
        
        LinearLayout.LayoutParams controlsParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f);
        sheetContainer.addView(controlsContainer, controlsParams);
        
        buildGrid();
    }
    
    private void buildGrid() {
        LinearLayout topRow = new LinearLayout(context);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setWeightSum(50f);
        LinearLayout.LayoutParams topRowParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 3f);
        controlsContainer.addView(topRow, topRowParams);

        topRow.addView(createLightButton("←", "CMD_MOVE_LEFT", true), getGridParams(14f, true));

        LinearLayout centerCol = new LinearLayout(context);
        centerCol.setOrientation(LinearLayout.VERTICAL);
        centerCol.addView(createLightButton("↑", "CMD_MOVE_UP", true), getGridParams(1f, false));
        
        selectBtn = (TextView) createLightButton("Select", "CMD_SELECT_TOGGLE", false);
        centerCol.addView(selectBtn, getGridParams(1f, false));

        centerCol.addView(createLightButton("↓", "CMD_MOVE_DOWN", true), getGridParams(1f, false));
        LinearLayout.LayoutParams centerColParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 10f);
        topRow.addView(centerCol, centerColParams);

        topRow.addView(createLightButton("→", "CMD_MOVE_RIGHT", true), getGridParams(14f, true));

        LinearLayout actionCol = new LinearLayout(context);
        actionCol.setOrientation(LinearLayout.VERTICAL);
        actionCol.addView(createLightButton("Select all", "CMD_SELECT_ALL", false), getGridParams(1f, false));
        actionCol.addView(createLightButton("Copy", "CMD_COPY", false), getGridParams(1f, false));
        actionCol.addView(createLightButton("Paste", "CMD_PASTE", false), getGridParams(1f, false));
        LinearLayout.LayoutParams actionColParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 12f);
        topRow.addView(actionCol, actionColParams);


        LinearLayout bottomRow = new LinearLayout(context);
        bottomRow.setOrientation(LinearLayout.HORIZONTAL);
        bottomRow.setWeightSum(50f);
        LinearLayout.LayoutParams bottomRowParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        controlsContainer.addView(bottomRow, bottomRowParams);

        bottomRow.addView(createLightButton("|←", "CMD_MOVE_HOME", true), getGridParams(19f, true));
        bottomRow.addView(createLightButton("→|", "CMD_MOVE_END", true), getGridParams(19f, true));
        bottomRow.addView(createLightButton("⌫", "DEL", true), getGridParams(12f, true));
    }

    private LinearLayout.LayoutParams getGridParams(float weight, boolean isHorizontal) {
        int w = isHorizontal ? 0 : ViewGroup.LayoutParams.MATCH_PARENT;
        int h = isHorizontal ? ViewGroup.LayoutParams.MATCH_PARENT : 0;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(w, h, weight);
        int m = dpToPx(3); 
        params.setMargins(m, m, m, m);
        return params;
    }

    private View createLightButton(final String label, final String command, final boolean supportLongPress) {
        final TextView btn = new TextView(context);
        btn.setText(label);
        btn.setTextColor(currentTheme.textColor);
        btn.setGravity(Gravity.CENTER);
        
        if (label.equals("Select") || label.equals("Select all") || label.equals("Copy") || label.equals("Paste")) {
            btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        } else {
            btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        }
        
        GradientDrawable normalBg = new GradientDrawable();
        normalBg.setColor(currentTheme.keyBgColor);
        normalBg.setCornerRadius(dpToPx(6)); 
        
        GradientDrawable pressedBg = new GradientDrawable();
        pressedBg.setColor(currentTheme.keyPressedColor);
        pressedBg.setCornerRadius(dpToPx(6));
        
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_pressed}, pressedBg);
        states.addState(new int[]{}, normalBg);

        if (Build.VERSION.SDK_INT >= 16) {
            btn.setBackground(states);
        } else {
            btn.setBackgroundDrawable(states);
        }

        btn.setClickable(true);

        btn.setOnTouchListener(new OnTouchListener() {
            private boolean isLongPressing = false;
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                    
                        if (command.equals("CMD_SELECT_TOGGLE")) {
                            toggleSelectMode();
                        } else if (listener != null) {
                            listener.onKeyClick(command);
                            if(command.equals("CMD_COPY") || command.equals("CMD_CUT")) {
                                resetSelectMode();
                            }
                        }

                        if (supportLongPress) {
                            isLongPressing = true;
                            activeRepeatRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    if (isLongPressing && listener != null) {
                                        listener.onKeyClick(command);
                                        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                                        repeatHandler.postDelayed(this, 60); 
                                    }
                                }
                            };
                            repeatHandler.postDelayed(activeRepeatRunnable, 400); 
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        v.setPressed(false);
                        isLongPressing = false;
                        if (activeRepeatRunnable != null) {
                            repeatHandler.removeCallbacks(activeRepeatRunnable);
                            activeRepeatRunnable = null;
                        }
                        return true;
                }
                return false;
            }
        });
        
        return btn;
    }

    private void toggleSelectMode() {
        isSelectModeOn = !isSelectModeOn;
        if (listener != null) listener.onKeyClick("CMD_SELECT_TOGGLE");
        updateSelectBtnVisuals();
    }

    private void resetSelectMode() {
        isSelectModeOn = false;
        if (listener != null) listener.onKeyClick("CMD_SELECT_RESET");
        updateSelectBtnVisuals();
    }

    private void updateSelectBtnVisuals() {
        if (selectBtn == null) return;
        GradientDrawable bg = (GradientDrawable) selectBtn.getBackground().getCurrent();
        if (isSelectModeOn) {
            selectBtn.setTextColor(currentTheme.keyBgColor);
            bg.setColor(currentTheme.enterBgColor);
        } else {
            selectBtn.setTextColor(currentTheme.textColor);
            bg.setColor(currentTheme.keyBgColor);
        }
    }

    // --- INSTANT SHOW / HIDE ---
    public void show(float cx, float cy) {
        if (isShowing) return;
        isShowing = true;
        this.setVisibility(VISIBLE);
        this.revealCx = cx;
        this.revealCy = cy;
        resetSelectMode(); 

        sheetContainer.setAlpha(1f);
        sheetContainer.setScaleX(1f);
        sheetContainer.setScaleY(1f);
    }

    public void hide() {
        if (!isShowing) return;
        isShowing = false;
        resetSelectMode();
        setVisibility(GONE);
    }
    
    public boolean isShowing() {
        return isShowing;
    }
}
