package com.gboard2.nobi;

import android.content.Context;
import android.content.SharedPreferences;
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
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmojiSheet extends FrameLayout {

    private Context context;
    private ProKeyboardView.KeyboardListener listener;
    private LinearLayout sheetContainer;
    private LinearLayout defaultToolbar;
    private TextView titleText;
    private Drawable backIcon;
    private LinearLayout bottomBar;
    private TextView btnAbc;
    private TextView btnDel;
    private LinearLayout gridContainer;
    private LinearLayout recentSectionContainer;
    private ScrollView scrollView;
    
    private boolean isShowing = false;
    private float revealCx = 0f;
    private float revealCy = 0f;
    
    private SharedPreferences prefs;
    private List<String> recentEmojis;
    private Map<String, View> categoryHeaders = new HashMap<>();
    private ThemeData.Theme currentTheme = ThemeData.getThemeByName("Default");
    
    private HorizontalScrollView tabScroll;
    private List<TextView> tabViews = new ArrayList<>();
    private String currentActiveTab = "";
    
    private Handler repeatHandler = new Handler(Looper.getMainLooper());
    private Runnable activeRepeatRunnable;

    private int loadedCategoryIndex = 0;
    private Handler chunkHandler = new Handler(Looper.getMainLooper());
    private Runnable chunkLoader = new Runnable() {
        @Override
        public void run() {
            if (loadedCategoryIndex < EmojiData.CATEGORY_NAMES.length) {
                addCategorySection(gridContainer, EmojiData.CATEGORY_NAMES[loadedCategoryIndex], EmojiData.ALL_CATEGORIES[loadedCategoryIndex]);
                loadedCategoryIndex++;
                chunkHandler.postDelayed(this, 25);
            }
        }
    };

    public EmojiSheet(Context context, ProKeyboardView.KeyboardListener listener) {
        super(context);
        this.context = context;
        this.listener = listener;
        prefs = context.getSharedPreferences("GboardPrefs", Context.MODE_PRIVATE);
        loadRecents();
        setupUI();
    }
    
    public void applyTheme(ThemeData.Theme theme) {
        if (theme == null) return;
        this.currentTheme = theme;
        
        if (sheetContainer != null) sheetContainer.setBackgroundColor(theme.bgColor);
        if (defaultToolbar != null) defaultToolbar.setBackgroundColor(theme.suggestionBgColor);
        if (titleText != null) titleText.setTextColor(theme.suggestionTextColor);
        if (backIcon != null) backIcon.setTint(theme.suggestionTextColor);
        if (tabScroll != null) tabScroll.setBackgroundColor(theme.suggestionBgColor);
        if (bottomBar != null) bottomBar.setBackgroundColor(theme.suggestionBgColor);
        
        for (TextView tab : tabViews) {
            if (tab.getText().toString().equals(currentActiveTab)) {
                tab.setTextColor(theme.enterBgColor);
            } else {
                tab.setTextColor(theme.suggestionTextColor);
            }
        }
        
        if (btnAbc != null) btnAbc.setTextColor(theme.textColor);
        if (btnDel != null) btnDel.setTextColor(theme.textColor);
        
        if (isShowing) {
            chunkHandler.removeCallbacks(chunkLoader);
            gridContainer.removeAllViews();
            recentSectionContainer.removeAllViews();
            gridContainer.addView(recentSectionContainer);
            categoryHeaders.clear();
            renderEmojisProgressively();
        }
    }

    private void loadRecents() {
        String recentsStr = prefs.getString("recent_emojis", "");
        recentEmojis = new ArrayList<>();
        if (!recentsStr.isEmpty()) {
            String[] split = recentsStr.split(",");
            for (String e : split) {
                if (!e.trim().isEmpty() && !recentEmojis.contains(e)) {
                    recentEmojis.add(e);
                }
            }
        }
    }

    private void saveRecent(String emoji) {
        recentEmojis.remove(emoji);
        recentEmojis.add(0, emoji);
        if (recentEmojis.size() > 40) {
            recentEmojis = recentEmojis.subList(0, 40);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < recentEmojis.size(); i++) {
            sb.append(recentEmojis.get(i));
            if (i < recentEmojis.size() - 1) sb.append(",");
        }
        prefs.edit().putString("recent_emojis", sb.toString()).apply();
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

        defaultToolbar = new LinearLayout(context);
        defaultToolbar.setOrientation(LinearLayout.HORIZONTAL);
        defaultToolbar.setGravity(Gravity.CENTER_VERTICAL);
        defaultToolbar.setBackgroundColor(currentTheme.suggestionBgColor);
        defaultToolbar.setPadding(dpToPx(8), 0, dpToPx(8), 0);

        ImageView backBtn = new ImageView(context);
        backIcon = context.getResources().getDrawable(R.drawable.ic_back_arrow, null);
        if (backIcon != null) {
            backIcon.setTint(currentTheme.suggestionTextColor);
            backBtn.setImageDrawable(backIcon);
        }
        backBtn.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));
        defaultToolbar.addView(backBtn, new LinearLayout.LayoutParams(dpToPx(48), dpToPx(48)));

        titleText = new TextView(context);
        titleText.setText("Emojis");
        titleText.setTextColor(currentTheme.suggestionTextColor);
        titleText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        titleText.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        titleParams.leftMargin = dpToPx(12);
        defaultToolbar.addView(titleText, titleParams);

        sheetContainer.addView(defaultToolbar, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, dpToPx(48)));
        backBtn.setOnClickListener(new View.OnClickListener() { 
            @Override public void onClick(View v) { hide(); } 
        });

        tabScroll = new HorizontalScrollView(context);
        tabScroll.setHorizontalScrollBarEnabled(false);
        tabScroll.setBackgroundColor(currentTheme.suggestionBgColor);
        
        LinearLayout tabsContainer = new LinearLayout(context);
        tabsContainer.setOrientation(LinearLayout.HORIZONTAL);
        tabsContainer.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));

        String[] allTabs = new String[EmojiData.CATEGORY_NAMES.length + 1];
        allTabs[0] = "Recent";
        System.arraycopy(EmojiData.CATEGORY_NAMES, 0, allTabs, 1, EmojiData.CATEGORY_NAMES.length);

        tabViews.clear();
        for (final String tabName : allTabs) {
            TextView tab = new TextView(context);
            tab.setText(tabName);
            tab.setTextColor(currentTheme.suggestionTextColor);
            tab.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            tab.setPadding(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8));
            
            tab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                    View targetView = categoryHeaders.get(tabName);
                    
                    if (tabName.equals("Recent") && recentSectionContainer.getChildCount() > 0) {
                        scrollView.smoothScrollTo(0, recentSectionContainer.getTop());
                    } else if (targetView != null && scrollView != null) {
                        scrollView.smoothScrollTo(0, targetView.getTop() + recentSectionContainer.getHeight());
                    }
                }
            });
            
            tabViews.add(tab);
            tabsContainer.addView(tab);
        }
        tabScroll.addView(tabsContainer);
        sheetContainer.addView(tabScroll, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        
        scrollView = new ScrollView(context);
        scrollView.setClipToPadding(false);
        scrollView.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));

        gridContainer = new LinearLayout(context);
        gridContainer.setOrientation(LinearLayout.VERTICAL);
        
        recentSectionContainer = new LinearLayout(context);
        recentSectionContainer.setOrientation(LinearLayout.VERTICAL);
        gridContainer.addView(recentSectionContainer, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        scrollView.addView(gridContainer, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f);
        sheetContainer.addView(scrollView, scrollParams);

        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int scrollY = scrollView.getScrollY();
                String activeCategory = "Recent"; 

                if (recentSectionContainer.getChildCount() > 0 && scrollY < recentSectionContainer.getHeight() - dpToPx(20)) {
                    activeCategory = "Recent";
                } else {
                    for (int i = EmojiData.CATEGORY_NAMES.length - 1; i >= 0; i--) {
                        String catName = EmojiData.CATEGORY_NAMES[i];
                        View header = categoryHeaders.get(catName);
                        if (header != null) {
                            int headerTop = header.getTop() + recentSectionContainer.getHeight();
                            if (scrollY >= headerTop - dpToPx(30)) { 
                                activeCategory = catName;
                                break;
                            }
                        }
                    }
                }

                if (!activeCategory.equals(currentActiveTab)) {
                    updateTabSelection(activeCategory);
                }
            }
        });

        bottomBar = new LinearLayout(context);
        bottomBar.setOrientation(LinearLayout.HORIZONTAL);
        bottomBar.setBackgroundColor(currentTheme.suggestionBgColor);
        bottomBar.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
        
        btnAbc = createActionButton("ABC");
        btnAbc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                hide();
            }
        });

        View spacer = new View(context);
        
        btnDel = createActionButton("DEL");
        btnDel.setOnTouchListener(new View.OnTouchListener() {
            private boolean isLongPressing = false;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                        if (listener != null) listener.onKeyClick("DEL");
                        
                        isLongPressing = true;
                        activeRepeatRunnable = new Runnable() {
                            @Override
                            public void run() {
                                if (isLongPressing && listener != null) {
                                    listener.onKeyClick("DEL");
                                    performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                                    repeatHandler.postDelayed(this, 50); 
                                }
                            }
                        };
                        repeatHandler.postDelayed(activeRepeatRunnable, 400); 
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

        bottomBar.addView(btnAbc, new LinearLayout.LayoutParams(dpToPx(60), dpToPx(40)));
        bottomBar.addView(spacer, new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1f));
        bottomBar.addView(btnDel, new LinearLayout.LayoutParams(dpToPx(60), dpToPx(40)));
        sheetContainer.addView(bottomBar, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }

    private void updateTabSelection(String activeTabName) {
        currentActiveTab = activeTabName;
        for (TextView tab : tabViews) {
            if (tab.getText().toString().equals(activeTabName)) {
                tab.setTextColor(currentTheme.enterBgColor);
                tab.setTypeface(null, android.graphics.Typeface.BOLD);
                
                int tabLeft = tab.getLeft();
                int tabRight = tab.getRight();
                int scrollX = tabScroll.getScrollX();
                int scrollWidth = tabScroll.getWidth();
                
                if (tabLeft < scrollX || tabRight > scrollX + scrollWidth) {
                    tabScroll.smoothScrollTo(tabLeft - dpToPx(40), 0);
                }
            } else {
                tab.setTextColor(currentTheme.suggestionTextColor);
                tab.setTypeface(null, android.graphics.Typeface.NORMAL);
            }
        }
    }

    private TextView createActionButton(String label) {
        TextView btn = new TextView(context);
        btn.setText(label);
        btn.setTextColor(currentTheme.textColor);
        btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        btn.setTypeface(null, android.graphics.Typeface.BOLD);
        btn.setGravity(Gravity.CENTER);

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
        return btn;
    }

    private void renderRecentSection() {
        recentSectionContainer.removeAllViews();
        if (recentEmojis.isEmpty()) {
            recentSectionContainer.setVisibility(GONE);
            return;
        }
        recentSectionContainer.setVisibility(VISIBLE);
        addCategorySection(recentSectionContainer, "Recent", recentEmojis.toArray(new String[0]));
    }

    private void renderEmojisProgressively() {
        renderRecentSection();
        loadedCategoryIndex = 0;
        chunkHandler.post(chunkLoader);
    }

    private void addCategorySection(ViewGroup targetContainer, String title, String[] emojis) {
        TextView titleView = new TextView(context);
        titleView.setText(title);
        titleView.setTextColor(currentTheme.textColor);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(dpToPx(8), dpToPx(16), dpToPx(8), dpToPx(8));
        targetContainer.addView(titleView, titleParams);
        
        categoryHeaders.put(title, titleView);

        int columns = 8;
        LinearLayout currentRow = null;

        for (int i = 0; i < emojis.length; i++) {
            if (i % columns == 0) {
                currentRow = new LinearLayout(context);
                currentRow.setOrientation(LinearLayout.HORIZONTAL);
                currentRow.setWeightSum(columns);
                targetContainer.addView(currentRow, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            }

            final String emojiStr = emojis[i];
            TextView emojiView = new TextView(context);
            emojiView.setText(emojiStr);
            emojiView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
            emojiView.setGravity(Gravity.CENTER);
            emojiView.setPadding(0, dpToPx(10), 0, dpToPx(10));
            emojiView.setClickable(true);

            emojiView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            v.setBackgroundColor(currentTheme.keyPressedColor);
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            v.setBackgroundColor(Color.TRANSPARENT);
                            break;
                    }
                    return false; 
                }
            });

            emojiView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                    // FIXED: Removed "EMOJI_INPUT:" prefix to allow direct typing
                    if (listener != null) listener.onKeyClick(emojiStr);
                    saveRecent(emojiStr);
                    renderRecentSection(); 
                }
            });

            LinearLayout.LayoutParams emojiParams = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
            currentRow.addView(emojiView, emojiParams);
        }

        if (emojis.length % columns != 0 && currentRow != null) {
            int remaining = columns - (emojis.length % columns);
            for (int i = 0; i < remaining; i++) {
                View emptyView = new View(context);
                currentRow.addView(emptyView, new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));
            }
        }
    }

    // --- INSTANT SHOW / HIDE ---
    public void show(float cx, float cy) {
        if (isShowing) return;
        isShowing = true;
        this.setVisibility(VISIBLE);
        this.revealCx = cx;
        this.revealCy = cy;
        
        loadRecents(); 
        updateTabSelection(recentEmojis.isEmpty() ? EmojiData.CATEGORY_NAMES[0] : "Recent");
        
        chunkHandler.removeCallbacks(chunkLoader);
        gridContainer.removeAllViews();
        recentSectionContainer.removeAllViews();
        gridContainer.addView(recentSectionContainer);
        categoryHeaders.clear();
        scrollView.scrollTo(0, 0);
        
        sheetContainer.setAlpha(1f);
        sheetContainer.setScaleX(1f);
        sheetContainer.setScaleY(1f);
        
        renderEmojisProgressively();
    }

    public void hide() {
        if (!isShowing) return;
        isShowing = false;
        
        chunkHandler.removeCallbacks(chunkLoader);
        setVisibility(GONE);
    }

    public boolean isShowing() {
        return isShowing;
    }
}
