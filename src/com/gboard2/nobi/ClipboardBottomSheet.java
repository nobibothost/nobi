package com.gboard2.nobi;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

public class ClipboardBottomSheet extends FrameLayout {

    private Context context;
    private ProKeyboardView.KeyboardListener keyboardListener;
    private ClipboardRepository repository;
    private ClipboardAdapter adapter;

    private View dimBackground;
    private LinearLayout sheetContainer;
    private TextView emptyStateText;
    private LinearLayout defaultToolbar;
    private TextView titleText;
    private Drawable backIcon;
    private Drawable deleteIcon;
    
    // Elevated to class level to manage scroll positions smoothly
    private ScrollView scrollView;
    
    private FrameLayout renameOverlay;
    private LinearLayout dialogBox;
    private TextView renameTitle;
    private EditText renameInput;
    private ClipboardItem itemToRename;
    
    private boolean isShowing = false;
    private float revealCx = 0f;
    private float revealCy = 0f;
    private ThemeData.Theme currentTheme = ThemeData.getThemeByName("Default");

    public ClipboardBottomSheet(Context context, ProKeyboardView.KeyboardListener listener) {
        super(context);
        this.context = context;
        this.keyboardListener = listener;
        this.repository = new ClipboardRepository(context);

        setupUI();
        loadData();
    }
    
    public void applyTheme(ThemeData.Theme theme) {
        if (theme == null) return;
        this.currentTheme = theme;
        
        if (sheetContainer != null) sheetContainer.setBackgroundColor(theme.bgColor);
        if (defaultToolbar != null) defaultToolbar.setBackgroundColor(theme.suggestionBgColor);
        if (titleText != null) titleText.setTextColor(theme.suggestionTextColor);
        if (emptyStateText != null) emptyStateText.setTextColor(theme.textColor);
        
        if (backIcon != null) backIcon.setTint(theme.suggestionTextColor);
        if (deleteIcon != null) deleteIcon.setTint(theme.suggestionTextColor);
        
        if (dialogBox != null) {
            GradientDrawable bg = (GradientDrawable) dialogBox.getBackground();
            if (bg != null) bg.setColor(theme.bgColor);
            if (renameTitle != null) renameTitle.setTextColor(theme.textColor);
            if (renameInput != null) {
                renameInput.setTextColor(theme.textColor);
                renameInput.setHintTextColor(theme.suggestionTextColor);
            }
        }
        
        if (adapter != null) {
            adapter.applyTheme(theme);
        }
    }

    public void addCopiedText(String text) {
        repository.addItem(text);
        if (isShowing) loadData();
    }

    private int dpToPx(float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    private void setupUI() {
        this.setVisibility(GONE);

        dimBackground = new View(context);
        dimBackground.setBackgroundColor(Color.parseColor("#99000000"));
        dimBackground.setAlpha(0f);
        addView(dimBackground, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        dimBackground.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });

        sheetContainer = new LinearLayout(context);
        sheetContainer.setOrientation(LinearLayout.VERTICAL);
        sheetContainer.setBackgroundColor(currentTheme.bgColor);
        LayoutParams sheetParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        sheetParams.topMargin = 0; 
        addView(sheetContainer, sheetParams);

        setupToolbar();

        emptyStateText = new TextView(context);
        emptyStateText.setText("Clipboard is empty");
        emptyStateText.setTextColor(currentTheme.textColor);
        emptyStateText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        emptyStateText.setGravity(Gravity.CENTER);
        emptyStateText.setVisibility(GONE);

        scrollView = new ScrollView(context);
        scrollView.setClipToPadding(false);
        scrollView.setPadding(dpToPx(6), dpToPx(2), dpToPx(6), dpToPx(24));

        LinearLayout scrollContainer = new LinearLayout(context);
        scrollContainer.setOrientation(LinearLayout.VERTICAL);
        scrollContainer.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        
        scrollView.addView(scrollContainer);

        adapter = new ClipboardAdapter(context, scrollContainer, new ClipboardAdapter.AdapterListener() {
            @Override
            public void onItemClick(ClipboardItem item) {
                if (keyboardListener != null) {
                    keyboardListener.onKeyClick("PASTE_CLIPBOARD:" + item.text);
                    performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                }
            }

            @Override
            public void onPinToggled(ClipboardItem item) {
                repository.togglePin(item);
                loadData();
                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            }

            @Override
            public void onItemDeleted(ClipboardItem item) {
                repository.deleteItem(item);
                loadData();
                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            }
            
            @Override
            public void onRequestRename(ClipboardItem item) {
                showRenameOverlay(item);
            }
        });

        FrameLayout contentArea = new FrameLayout(context);
        contentArea.addView(scrollView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        contentArea.addView(emptyStateText, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        
        sheetContainer.addView(contentArea, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f));

        setupRenameOverlay();
    }

    private void setupToolbar() {
        int barHeight = dpToPx(48);

        defaultToolbar = new LinearLayout(context);
        defaultToolbar.setOrientation(LinearLayout.HORIZONTAL);
        defaultToolbar.setGravity(Gravity.CENTER_VERTICAL);
        defaultToolbar.setBackgroundColor(currentTheme.suggestionBgColor);
        defaultToolbar.setPadding(dpToPx(8), 0, dpToPx(8), 0);

        ImageView backBtn = new ImageView(context);
        backIcon = context.getResources().getDrawable(R.drawable.ic_back_arrow, null);
        if (backIcon != null) backIcon.setTint(currentTheme.suggestionTextColor);
        backBtn.setImageDrawable(backIcon);
        backBtn.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));
        defaultToolbar.addView(backBtn, new LinearLayout.LayoutParams(dpToPx(48), dpToPx(48)));

        titleText = new TextView(context);
        titleText.setText("Clipboard");
        titleText.setTextColor(currentTheme.suggestionTextColor);
        titleText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        titleText.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        titleParams.leftMargin = dpToPx(12);
        defaultToolbar.addView(titleText, titleParams);

        ImageView clearBtn = new ImageView(context);
        deleteIcon = context.getResources().getDrawable(R.drawable.ic_delete, null);
        if (deleteIcon != null) deleteIcon.setTint(currentTheme.suggestionTextColor);
        clearBtn.setImageDrawable(deleteIcon);
        clearBtn.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));
        defaultToolbar.addView(clearBtn, new LinearLayout.LayoutParams(dpToPx(48), dpToPx(48)));

        sheetContainer.addView(defaultToolbar, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, barHeight));
        backBtn.setOnClickListener(new OnClickListener() { @Override public void onClick(View v) { hide(); } });

        clearBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                repository.clearRecent();
                loadData();
                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            }
        });
    }

    private void setupRenameOverlay() {
        renameOverlay = new FrameLayout(context);
        renameOverlay.setBackgroundColor(Color.parseColor("#80000000"));
        renameOverlay.setVisibility(GONE);
        renameOverlay.setClickable(true); 
        
        dialogBox = new LinearLayout(context);
        dialogBox.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(currentTheme.bgColor);
        bg.setCornerRadius(dpToPx(12));
        dialogBox.setBackground(bg);
        dialogBox.setPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(12));

        FrameLayout.LayoutParams dialogParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogParams.gravity = Gravity.CENTER;
        dialogParams.setMargins(dpToPx(24), 0, dpToPx(24), 0);
        
        renameTitle = new TextView(context);
        renameTitle.setText("Edit Item");
        renameTitle.setTextColor(currentTheme.textColor);
        renameTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        renameTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        dialogBox.addView(renameTitle, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        
        renameInput = new EditText(context);
        renameInput.setTextColor(currentTheme.textColor);
        renameInput.setHintTextColor(currentTheme.suggestionTextColor);
        renameInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);

        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        inputParams.topMargin = dpToPx(16);
        dialogBox.addView(renameInput, inputParams);
        
        LinearLayout btnRow = new LinearLayout(context);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(Gravity.RIGHT);

        LinearLayout.LayoutParams btnRowParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnRowParams.topMargin = dpToPx(20);
        dialogBox.addView(btnRow, btnRowParams);
        
        TextView btnCancel = new TextView(context);
        btnCancel.setText("Cancel");
        btnCancel.setTextColor(Color.parseColor("#546E7A"));
        btnCancel.setPadding(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8));
        btnRow.addView(btnCancel);
        
        TextView btnSave = new TextView(context);
        btnSave.setText("Save");
        btnSave.setTextColor(Color.parseColor("#4A90E2")); 
        btnSave.setTypeface(null, android.graphics.Typeface.BOLD);
        btnSave.setPadding(dpToPx(16), dpToPx(8), dpToPx(8), dpToPx(8));
        btnRow.addView(btnSave);

        renameOverlay.addView(dialogBox, dialogParams);
        addView(renameOverlay, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        
        btnCancel.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) { hideRenameOverlay(); }
        });

        btnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemToRename != null) {
                    repository.updateItemText(itemToRename, renameInput.getText().toString());
                    loadData();
                }
                hideRenameOverlay();
            }
        });
    }

    private void showRenameOverlay(ClipboardItem item) {
        itemToRename = item;
        renameInput.setText(item.text);
        renameInput.setSelection(item.text.length());
        renameOverlay.setVisibility(VISIBLE);
        renameOverlay.setAlpha(1f);
    }

    private void hideRenameOverlay() {
        renameOverlay.setVisibility(GONE); 
        itemToRename = null;
    }

    private void loadData() {
        // Current scroll position ko save kar rahe hain taaki wapas top par na bhaage
        final int currentScrollY = (scrollView != null) ? scrollView.getScrollY() : 0;
        
        List<ClipboardItem> currentItems = repository.getItems();
        adapter.updateData(currentItems);
        emptyStateText.setVisibility(currentItems.isEmpty() ? VISIBLE : GONE);
        
        // Data load hone ke baad list ko silently wapas usi scroll position par set karna
        if (scrollView != null) {
            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    scrollView.scrollTo(0, currentScrollY);
                }
            });
        }
    }

    // --- INSTANT SHOW / HIDE ---
    public void show(float cx, float cy) {
        if (isShowing) return;
        isShowing = true;
        this.setVisibility(VISIBLE);
        this.revealCx = cx;
        this.revealCy = cy;

        dimBackground.setAlpha(1f);
        sheetContainer.setAlpha(1f);
        sheetContainer.setScaleX(1f);
        sheetContainer.setScaleY(1f);
        
        // Micro-delay taaki view immediately show ho aur background me data load ho (Zero Stutter/Fluctuation)
        postDelayed(new Runnable() {
            @Override
            public void run() {
                loadData();
            }
        }, 15);
    }

    public void hide() {
        if (!isShowing) return;
        isShowing = false;
        
        if (adapter != null) {
            adapter.cancelLoading();
        }
        setVisibility(GONE);
    }
    
    public boolean isShowing() {
        return isShowing;
    }
}
