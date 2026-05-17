package com.gboard2.nobi;

import android.content.Context;
import android.graphics.Canvas;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import java.util.ArrayList;
import java.util.List;

public class ProKeyboardView extends FrameLayout {

    public static final int MODE_TEXT = 0;
    public static final int MODE_SYMBOLS = 1;
    public static final int MODE_SYMBOLS_PAGE_2 = 2;
    public static final int MODE_NUMBER = 4;

    public ThemeManager themeManager;
    public SuggestionManager suggestionManager;
    public SwipeEmojiManager swipeEmojiManager;
    public SwipeSymbolManager swipeSymbolManager;
    public ResizeController resizeController;
    public ToolbarManager toolbarManager;
    public KeyboardLayoutManager layoutManager;
    public PopupRenderer popupRenderer;
    public KeyboardRenderer renderer;
    public KeyboardTouchHandler touchHandler;
    public SoundManager soundManager;
    public TouchEffectManager touchEffectManager;

    public ClipboardBottomSheet clipboardBottomSheet;
    public TextEditingSheet textEditingSheet;
    public EmojiSheet emojiSheet;
    public ThemeSheet themeSheet;
    public FontSheet fontSheet;
    public LayoutProfileSheet layoutProfileSheet;
    public KeyboardListener listener;

    public List<KeyData> keys = new ArrayList<>();
    
    public int currentMode = MODE_TEXT;
    public int currentImeAction = EditorInfo.IME_ACTION_UNSPECIFIED;
    public boolean isShifted = false;
    public boolean isCapsLock = false;
    public long lastShiftPressTime = 0;

    public interface KeyboardListener {
        void onKeyClick(String key);
    }

    public ProKeyboardView(Context context) {
        super(context);
        setWillNotDraw(false);
        themeManager = new ThemeManager(context, this);
        suggestionManager = new SuggestionManager();
        swipeEmojiManager = new SwipeEmojiManager();
        swipeSymbolManager = new SwipeSymbolManager();
        resizeController = new ResizeController(context);
        toolbarManager = new ToolbarManager(context, this);
        layoutManager = new KeyboardLayoutManager(this);
        popupRenderer = new PopupRenderer(this);
        renderer = new KeyboardRenderer(this); 
        touchHandler = new KeyboardTouchHandler(this);
        
        soundManager = new SoundManager(context);
        touchEffectManager = new TouchEffectManager(context);

        toolbarManager.initInfinityApplets();
    }

    public void setKeyboardListener(KeyboardListener listener) {
        this.listener = listener;
        if (clipboardBottomSheet != null) removeView(clipboardBottomSheet);
        if (textEditingSheet != null) removeView(textEditingSheet);
        if (emojiSheet != null) removeView(emojiSheet);
        if (themeSheet != null) removeView(themeSheet);
        if (fontSheet != null) removeView(fontSheet);
        if (layoutProfileSheet != null) removeView(layoutProfileSheet); 
        
        clipboardBottomSheet = new ClipboardBottomSheet(getContext(), this.listener);
        textEditingSheet = new TextEditingSheet(getContext(), this.listener);
        emojiSheet = new EmojiSheet(getContext(), this.listener);
        themeSheet = new ThemeSheet(getContext(), this);
        fontSheet = new FontSheet(getContext(), this);
        layoutProfileSheet = new LayoutProfileSheet(getContext(), this); 
        
        if (themeManager.activeTheme != null) {
            clipboardBottomSheet.applyTheme(themeManager.activeTheme);
            textEditingSheet.applyTheme(themeManager.activeTheme);
            emojiSheet.applyTheme(themeManager.activeTheme);
            themeSheet.applyTheme(themeManager.activeTheme);
            fontSheet.applyTheme(themeManager.activeTheme);
            layoutProfileSheet.applyTheme(themeManager.activeTheme);
        }
        
        addView(clipboardBottomSheet, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(textEditingSheet, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(emojiSheet, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(themeSheet, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(fontSheet, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(layoutProfileSheet, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }
    
    public void applyTheme(ThemeData.Theme theme) {
        themeManager.applyTheme(theme);
        if(layoutProfileSheet != null) layoutProfileSheet.applyTheme(theme);
    }

    public void applyFont(FontData.FontStyle font) {
        themeManager.applyFont(font);
    }

    public boolean isCapsLockOn() { return isCapsLock; }

    public void setShifted(boolean shifted) {
        if (isCapsLock) return;
        this.isShifted = shifted;
        postInvalidateOnAnimation();
    }
    
    public void setSuggestions(List<String> suggestions, boolean showToolbar) {
        suggestionManager.setSuggestions(suggestions, showToolbar);
        postInvalidateOnAnimation();
    }

    public void resetKeyboardState() {
        toolbarManager.isMoreFeaturesOpen = false;
        toolbarManager.panelScale = 0f; 
        
        if (clipboardBottomSheet != null && clipboardBottomSheet.isShowing()) clipboardBottomSheet.hide();
        if (textEditingSheet != null && textEditingSheet.isShowing()) textEditingSheet.hide();
        if (emojiSheet != null && emojiSheet.isShowing()) emojiSheet.hide();
        if (themeSheet != null && themeSheet.isShowing()) themeSheet.hide();
        if (fontSheet != null && fontSheet.isShowing()) fontSheet.hide();
        if (layoutProfileSheet != null && layoutProfileSheet.isShowing()) layoutProfileSheet.hide(); 
        
        if (!isCapsLock) isShifted = false;
        
        resizeController.isResizing = false;
        resizeController.pressingDone = false;
        resizeController.isDraggingResize = false;
        currentMode = MODE_TEXT; 
        
        if (getWidth() > 0 && getHeight() > 0) layoutManager.buildKeys();
        postInvalidateOnAnimation();
    }

    public void setImeOptions(EditorInfo info) {
        resetKeyboardState();
        int inputClass = info.inputType & InputType.TYPE_MASK_CLASS;
        
        if (inputClass == InputType.TYPE_CLASS_PHONE || inputClass == InputType.TYPE_CLASS_NUMBER || inputClass == InputType.TYPE_CLASS_DATETIME) {
            currentMode = MODE_NUMBER;
        } else {
            currentMode = MODE_TEXT;
        }

        if ((info.inputType & InputType.TYPE_TEXT_FLAG_MULTI_LINE) != 0 || inputClass == InputType.TYPE_NULL) {
            currentImeAction = EditorInfo.IME_ACTION_NONE;
        } else {
            currentImeAction = info.imeOptions & EditorInfo.IME_MASK_ACTION;
        }
        
        if (getWidth() > 0 && getHeight() > 0) layoutManager.buildKeys();
        requestLayout(); 
        postInvalidateOnAnimation();
    }

    public void addCopiedText(String text) {
        if (clipboardBottomSheet != null) clipboardBottomSheet.addCopiedText(text);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        themeManager.loadRowPreferences();
        int width = MeasureSpec.getSize(widthMeasureSpec);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        
        float extraRatio = 0f;
        float singleExtraRowRatio = 0.06f;
        
        if (themeManager.showSlangRow) extraRatio += singleExtraRowRatio;
        if (themeManager.showEmojiRow) extraRatio += singleExtraRowRatio;
        if (themeManager.showNumberRow) extraRatio += singleExtraRowRatio;
        
        double heightRatio = 0.30 + extraRatio;
        int targetHeight = (int) (metrics.heightPixels * heightRatio * resizeController.userHeightScale);
        
        int exactHeightSpec = MeasureSpec.makeMeasureSpec(targetHeight, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, exactHeightSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        layoutManager.buildKeys();
        toolbarManager.updateToolbarTargets(true, w, h, (int)getToolbarHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        renderer.draw(canvas);

        CustomKeyManager ckm = CustomKeyManager.getInstance(getContext());
        
        if (touchHandler != null && touchHandler.activeKey != null && touchHandler.isLongPressTriggered && !resizeController.isResizing) {
            
            String physicalLabel = touchHandler.activeKey.label;
            String effLabel = ckm.getLabel(physicalLabel);
            if (effLabel == null || effLabel.isEmpty()) effLabel = physicalLabel;

            String[] customPopups = ckm.getPopups(physicalLabel);
            boolean hasExplicitPopups = false;
            
            if (customPopups != null) {
                for (String p : customPopups) {
                    if (p != null && !p.trim().isEmpty()) { hasExplicitPopups = true; break; }
                }
            }

            if (hasExplicitPopups) {
                String center = (customPopups[0] != null && !customPopups[0].isEmpty()) ? customPopups[0] : effLabel;
                String up = (customPopups[1] != null && !customPopups[1].isEmpty()) ? customPopups[1] : center;
                String down = (customPopups[2] != null && !customPopups[2].isEmpty()) ? customPopups[2] : center;
                String left = (customPopups[3] != null && !customPopups[3].isEmpty()) ? customPopups[3] : center;
                String right = (customPopups[4] != null && !customPopups[4].isEmpty()) ? customPopups[4] : center;

                if (swipeSymbolManager != null) swipeSymbolManager.swipeSymbols.put(physicalLabel, new String[]{center, up, down, left, right});
                if (swipeEmojiManager != null) swipeEmojiManager.swipeEmojis.put(physicalLabel, new String[]{center, up, down, left, right});
            } else {
                String[] defaultPopups = KeyboardLayouts.getPopups(effLabel);
                if (defaultPopups != null) {
                    String center = (defaultPopups.length > 0 && defaultPopups[0] != null) ? defaultPopups[0] : effLabel;
                    String up = (defaultPopups.length > 1 && defaultPopups[1] != null) ? defaultPopups[1] : center;
                    String down = (defaultPopups.length > 2 && defaultPopups[2] != null) ? defaultPopups[2] : center;
                    String left = (defaultPopups.length > 3 && defaultPopups[3] != null) ? defaultPopups[3] : center;
                    String right = (defaultPopups.length > 4 && defaultPopups[4] != null) ? defaultPopups[4] : center;
                    
                    if (swipeSymbolManager != null) swipeSymbolManager.swipeSymbols.put(physicalLabel, new String[]{center, up, down, left, right});
                    if (swipeEmojiManager != null) swipeEmojiManager.swipeEmojis.put(physicalLabel, new String[]{center, up, down, left, right});
                } else if (KeyboardData.SWIPE_EMOJIS.containsKey(effLabel)) {
                    String[] defaultEmojis = KeyboardData.SWIPE_EMOJIS.get(effLabel);
                    String center = (defaultEmojis.length > 0 && defaultEmojis[0] != null) ? defaultEmojis[0] : effLabel;
                    String up = (defaultEmojis.length > 1 && defaultEmojis[1] != null) ? defaultEmojis[1] : center;
                    String down = (defaultEmojis.length > 2 && defaultEmojis[2] != null) ? defaultEmojis[2] : center;
                    String left = (defaultEmojis.length > 3 && defaultEmojis[3] != null) ? defaultEmojis[3] : center;
                    String right = (defaultEmojis.length > 4 && defaultEmojis[4] != null) ? defaultEmojis[4] : center;
                    
                    if (swipeSymbolManager != null) swipeSymbolManager.swipeSymbols.put(physicalLabel, new String[]{center, up, down, left, right});
                    if (swipeEmojiManager != null) swipeEmojiManager.swipeEmojis.put(physicalLabel, new String[]{center, up, down, left, right});
                }
            }
            popupRenderer.drawPopupPreview(canvas, touchHandler.activeKey);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!touchHandler.handleTouch(event)) {
            return super.onTouchEvent(event);
        }
        return true;
    }

    public int dpToPx(float dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    public float getToolbarHeight() {
        return dpToPx(44);
    }

    public void triggerHapticFeedback() {
        soundManager.playSoundAndHaptic(this, null);
    }

    public void triggerHapticFeedback(String keyLabel) {
        soundManager.playSoundAndHaptic(this, keyLabel);
    }

    public String getDisplayLabel(String rawLabel) {
        CustomKeyManager ckm = CustomKeyManager.getInstance(getContext());
        String customLabel = ckm.getLabel(rawLabel);
        
        if (customLabel != null && !customLabel.isEmpty()) {
            if (customLabel.length() == 1 && Character.isLetter(customLabel.charAt(0)) && currentMode == MODE_TEXT) {
                return (isShifted || isCapsLock) ? customLabel.toUpperCase() : customLabel.toLowerCase();
            }
            return customLabel;
        }
        
        String label = rawLabel;
        if (rawLabel.length() == 1 && Character.isLetter(rawLabel.charAt(0)) && currentMode == MODE_TEXT) {
            label = (isShifted || isCapsLock) ? rawLabel.toUpperCase() : rawLabel.toLowerCase();
        }
        if (themeManager.activeFont != null && currentMode == MODE_TEXT && rawLabel.length() == 1 && Character.isLetter(rawLabel.charAt(0))) {
            return FontData.convertText(label, themeManager.activeFont);
        }
        return label;
    }
}