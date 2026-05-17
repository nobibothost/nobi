package com.gboard2.nobi;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import java.util.Arrays;

public class KeyboardTouchHandler {

    private ProKeyboardView pkv;
    public KeyData activeKey = null;
    public KeyData popupKey = null;
    public float lastTouchX = 0;
    public float lastTouchY = 0;
    public float keyTouchDownX = 0f;
    public float keyTouchDownY = 0f;
    public int currentSwipeDirection = 0;
    public boolean isLongPressTriggered = false;
    public int repeatDelay = 220;
    public Handler handler = new Handler(Looper.getMainLooper());
    
    public Runnable toolbarLongPressRunnable = new Runnable() {
        @Override
        public void run() {
            if (pkv.toolbarManager.activeDragItem != null && pkv.toolbarManager.isToolbarTouch) {
                pkv.toolbarManager.isToolbarLongPressTriggered = true;
                pkv.toolbarManager.activeDragItem.isDragging = true;
                pkv.triggerHapticFeedback(); 
                pkv.postInvalidateOnAnimation();
            }
        }
    };

    public Runnable repeatRunnable = new Runnable() {
        @Override
        public void run() {
            if (activeKey != null && isLongPressTriggered) {
                if (activeKey.label.equals("DEL")) {
                    pkv.triggerHapticFeedback("DEL");
                    if (pkv.listener != null) {
                        if (repeatDelay <= 80) {
                            pkv.listener.onKeyClick("DEL_BATCH");
                        } else {
                            pkv.listener.onKeyClick("DEL");
                        }
                    }
                    repeatDelay = Math.max(40, repeatDelay - 30);
                    handler.postDelayed(this, repeatDelay);
                }
            }
        }
    };

    public KeyboardTouchHandler(ProKeyboardView pkv) {
        this.pkv = pkv;
    }

    // NEW FIX: Helper method to perfectly sync Visual Popups and Actions based on Active Layout
    private String[] getActivePopups(String physicalLabel, String effLabel, CustomKeyManager ckm) {
        String[] customPopups = ckm.getPopups(physicalLabel);
        boolean hasExplicitPopups = false;
        if (customPopups != null) {
            for (String p : customPopups) {
                if (p != null && !p.trim().isEmpty()) { hasExplicitPopups = true; break; }
            }
        }
        
        if (hasExplicitPopups) {
            String center = (customPopups[0] != null && !customPopups[0].isEmpty()) ? customPopups[0] : effLabel;
            String up = (customPopups.length > 1 && customPopups[1] != null && !customPopups[1].isEmpty()) ? customPopups[1] : center;
            String down = (customPopups.length > 2 && customPopups[2] != null && !customPopups[2].isEmpty()) ? customPopups[2] : center;
            String left = (customPopups.length > 3 && customPopups[3] != null && !customPopups[3].isEmpty()) ? customPopups[3] : center;
            String right = (customPopups.length > 4 && customPopups[4] != null && !customPopups[4].isEmpty()) ? customPopups[4] : center;
            return new String[]{center, up, down, left, right};
        } else {
            String[] defaultPopups = KeyboardLayouts.getPopups(effLabel);
            if (defaultPopups != null) {
                String center = (defaultPopups.length > 0 && defaultPopups[0] != null) ? defaultPopups[0] : effLabel;
                String up = (defaultPopups.length > 1 && defaultPopups[1] != null) ? defaultPopups[1] : center;
                String down = (defaultPopups.length > 2 && defaultPopups[2] != null) ? defaultPopups[2] : center;
                String left = (defaultPopups.length > 3 && defaultPopups[3] != null) ? defaultPopups[3] : center;
                String right = (defaultPopups.length > 4 && defaultPopups[4] != null) ? defaultPopups[4] : center;
                return new String[]{center, up, down, left, right};
            } else if (KeyboardData.SWIPE_EMOJIS.containsKey(effLabel)) {
                String[] defaultEmojis = KeyboardData.SWIPE_EMOJIS.get(effLabel);
                String center = (defaultEmojis.length > 0 && defaultEmojis[0] != null) ? defaultEmojis[0] : effLabel;
                String up = (defaultEmojis.length > 1 && defaultEmojis[1] != null) ? defaultEmojis[1] : center;
                String down = (defaultEmojis.length > 2 && defaultEmojis[2] != null) ? defaultEmojis[2] : center;
                String left = (defaultEmojis.length > 3 && defaultEmojis[3] != null) ? defaultEmojis[3] : center;
                String right = (defaultEmojis.length > 4 && defaultEmojis[4] != null) ? defaultEmojis[4] : center;
                return new String[]{center, up, down, left, right};
            }
        }
        return null;
    }

    public boolean handleTouch(MotionEvent event) {
        if (pkv.clipboardBottomSheet != null && pkv.clipboardBottomSheet.isShowing()) return true;
        if (pkv.textEditingSheet != null && pkv.textEditingSheet.isShowing()) return true;
        if (pkv.emojiSheet != null && pkv.emojiSheet.isShowing()) return true;
        if (pkv.themeSheet != null && pkv.themeSheet.isShowing()) return true;
        if (pkv.fontSheet != null && pkv.fontSheet.isShowing()) return true;

        if (pkv.resizeController.isResizing) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    RectF hitBox = pkv.resizeController.getDoneButtonBounds(pkv.getWidth(), pkv.getHeight());
                    hitBox.inset(-pkv.dpToPx(20), -pkv.dpToPx(20)); 
                    if (hitBox.contains(event.getX(), event.getY())) { 
                        pkv.resizeController.pressingDone = true;
                        pkv.resizeController.isDraggingResize = false;
                    } else { 
                        pkv.resizeController.pressingDone = false;
                        pkv.resizeController.isDraggingResize = true; 
                        pkv.resizeController.dragStartY = event.getRawY(); 
                        pkv.resizeController.initialScaleOnDrag = pkv.resizeController.userHeightScale; 
                    }
                    return true;

                case MotionEvent.ACTION_MOVE:
                    if (pkv.resizeController.pressingDone) {
                        RectF hitBoxMove = pkv.resizeController.getDoneButtonBounds(pkv.getWidth(), pkv.getHeight());
                        hitBoxMove.inset(-pkv.dpToPx(20), -pkv.dpToPx(20));
                        if (!hitBoxMove.contains(event.getX(), event.getY())) pkv.resizeController.pressingDone = false;
                    } else if (pkv.resizeController.isDraggingResize) { 
                        float dy = event.getRawY() - pkv.resizeController.dragStartY;
                        float baseH = pkv.getContext().getResources().getDisplayMetrics().heightPixels * 0.36f; 
                        float newScale = pkv.resizeController.initialScaleOnDrag - (dy / baseH);
                        newScale = Math.max(0.7f, Math.min(1.35f, newScale));
                        if (newScale != pkv.resizeController.userHeightScale) { 
                            pkv.resizeController.userHeightScale = newScale;
                            pkv.requestLayout(); 
                            pkv.invalidate();
                        }
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                    if (pkv.resizeController.pressingDone) {
                        pkv.resizeController.isResizing = false;
                        pkv.resizeController.pressingDone = false; 
                        pkv.triggerHapticFeedback();
                        SharedPreferences.Editor editor = pkv.getContext().getSharedPreferences("GboardPrefs", Context.MODE_PRIVATE).edit();
                        editor.putFloat("keyboard_scale", pkv.resizeController.userHeightScale); 
                        editor.apply(); 
                        pkv.requestLayout(); 
                        pkv.invalidate();
                    }
                    pkv.resizeController.isDraggingResize = false;
                    return true;
                case MotionEvent.ACTION_CANCEL:
                    pkv.resizeController.pressingDone = false;
                    pkv.resizeController.isDraggingResize = false;
                    return true;
            }
            return true;
        }

        float x = event.getX(), y = event.getY();
        float toolbarHeight = pkv.getToolbarHeight();
        float toolbarAreaWidth = pkv.getWidth() - toolbarHeight; 
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (y <= toolbarHeight || pkv.toolbarManager.isMoreFeaturesOpen) {
                    pkv.toolbarManager.isToolbarTouch = true;
                    pkv.toolbarManager.toolbarTouchDownX = x;
                    pkv.toolbarManager.toolbarTouchDownY = y;
                    
                    if (y <= toolbarHeight && x >= toolbarAreaWidth) {
                        pkv.triggerHapticFeedback();
                        pkv.toolbarManager.isMoreFeaturesOpen = !pkv.toolbarManager.isMoreFeaturesOpen;
                        pkv.postInvalidateOnAnimation();
                        return true;
                    }
                    
                    boolean shouldShowTools = pkv.suggestionManager.showToolbar || pkv.toolbarManager.isMoreFeaturesOpen;

                    if (y <= toolbarHeight && shouldShowTools) {
                        float activeSlotWidth = toolbarAreaWidth / Math.max(1, pkv.toolbarManager.activeTools.size());
                        for (ToolbarManager.ToolbarItem item : pkv.toolbarManager.activeTools) {
                            if (x >= item.currentX && x <= item.currentX + activeSlotWidth) {
                                pkv.toolbarManager.activeDragItem = item;
                                item.isPressed = true;
                                pkv.toolbarManager.dragOffsetX = x - item.currentX;
                                pkv.toolbarManager.dragOffsetY = y - item.currentY;
                                pkv.toolbarManager.isToolbarLongPressTriggered = false;
                                handler.postDelayed(toolbarLongPressRunnable, 300);
                                pkv.postInvalidateOnAnimation();
                                return true;
                            }
                        }
                    } else if (pkv.toolbarManager.isMoreFeaturesOpen && y > toolbarHeight) {
                        float inactiveColWidth = pkv.getWidth() / 4f;
                        float inactiveRowHeight = pkv.dpToPx(80);
                        float relativeY = y - toolbarHeight;

                        for (ToolbarManager.ToolbarItem item : pkv.toolbarManager.inactiveTools) {
                            if (x >= item.currentX && x <= item.currentX + inactiveColWidth &&
                                relativeY >= item.currentY && relativeY <= item.currentY + inactiveRowHeight) {
                
                                pkv.toolbarManager.activeDragItem = item;
                                item.isPressed = true;
                                pkv.toolbarManager.dragOffsetX = x - item.currentX;
                                pkv.toolbarManager.dragOffsetY = relativeY - item.currentY;
                                pkv.toolbarManager.isToolbarLongPressTriggered = false;
                                handler.postDelayed(toolbarLongPressRunnable, 300);
                                pkv.postInvalidateOnAnimation();
                                return true;
                            }
                        }
                    }
                    return true;
                }
                
                pkv.toolbarManager.isToolbarTouch = false;
                lastTouchX = x; lastTouchY = y;
                KeyData touchedKey = findKeyAt(x, y);

                if (touchedKey != null) {
                    pkv.triggerHapticFeedback(touchedKey.label);
                    int themeColor = pkv.themeManager.activeTheme != null ? pkv.themeManager.activeTheme.enterBgColor : 0xFF4A90E2;
                    pkv.touchEffectManager.addEffect(x, y, themeColor);
                    
                    activeKey = touchedKey;
                    activeKey.isPressed = true;
                    activeKey.touchX = x;
                    activeKey.touchY = y;
                    activeKey.rippleRadius = pkv.dpToPx(10);
                    activeKey.rippleAlpha = 0f;
                    keyTouchDownX = x;
                    keyTouchDownY = y;
                    currentSwipeDirection = 0;
                    
                    isLongPressTriggered = false;
                    repeatDelay = 220;
                    handler.postDelayed(new Runnable() { 
                        @Override 
                        public void run() { 
                            isLongPressTriggered = true; 
                            pkv.touchHandler.popupKey = activeKey; // Ensure popup renderer knows what to draw
                            pkv.postInvalidateOnAnimation(); 
                        } 
                    }, 220);

                    handler.postDelayed(repeatRunnable, 220); 
                    
                    pkv.postInvalidateOnAnimation();
                }
                return true;

            case MotionEvent.ACTION_MOVE:
                pkv.toolbarManager.dragPointerX = x;
                pkv.toolbarManager.dragPointerY = y;
                if (pkv.toolbarManager.isToolbarTouch) {
                    if (pkv.toolbarManager.activeDragItem != null) {
                        if (pkv.toolbarManager.isToolbarLongPressTriggered) {
                            float activeSlotWidth = toolbarAreaWidth / Math.max(1, pkv.toolbarManager.activeTools.size() + 1);
                            float inactiveColWidth = pkv.getWidth() / 4f;
                            float inactiveRowHeight = pkv.dpToPx(80);
                            
                            pkv.toolbarManager.activeDragItem.currentX = x - pkv.toolbarManager.dragOffsetX;

                            if (y < toolbarHeight) {
                                pkv.toolbarManager.activeDragItem.currentY = y - pkv.toolbarManager.dragOffsetY;
                            } else {
                                pkv.toolbarManager.activeDragItem.currentY = (y - toolbarHeight) - pkv.toolbarManager.dragOffsetY;
                                if (!pkv.toolbarManager.isMoreFeaturesOpen) {
                                    pkv.toolbarManager.isMoreFeaturesOpen = true;
                                }
                            }
                            
                            if (y < toolbarHeight) {
                                int newIndex = (int) ((pkv.toolbarManager.activeDragItem.currentX + (activeSlotWidth / 2f)) / activeSlotWidth);
                                newIndex = Math.max(0, Math.min(newIndex, pkv.toolbarManager.activeTools.size()));
                                
                                if (pkv.toolbarManager.inactiveTools.contains(pkv.toolbarManager.activeDragItem)) {
                                    pkv.toolbarManager.inactiveTools.remove(pkv.toolbarManager.activeDragItem);
                                    pkv.toolbarManager.activeTools.add(newIndex, pkv.toolbarManager.activeDragItem);
                                    pkv.triggerHapticFeedback();
                                    pkv.toolbarManager.updateToolbarTargets(false, pkv.getWidth(), pkv.getHeight(), (int)toolbarHeight);
                                } else {
                                    int oldIndex = pkv.toolbarManager.activeTools.indexOf(pkv.toolbarManager.activeDragItem);
                                    if (newIndex >= pkv.toolbarManager.activeTools.size()) newIndex = pkv.toolbarManager.activeTools.size() - 1;
                                    if (oldIndex != -1 && newIndex != oldIndex) {
                                        pkv.toolbarManager.activeTools.remove(oldIndex);
                                        pkv.toolbarManager.activeTools.add(newIndex, pkv.toolbarManager.activeDragItem);
                                        pkv.triggerHapticFeedback();
                                        pkv.toolbarManager.updateToolbarTargets(false, pkv.getWidth(), pkv.getHeight(), (int)toolbarHeight);
                                    }
                                }
                            } else if (pkv.toolbarManager.isMoreFeaturesOpen) {
                                int row = (int) ((pkv.toolbarManager.activeDragItem.currentY + (inactiveRowHeight / 2f) - pkv.toolbarManager.inactiveStartYOffset) / inactiveRowHeight);
                                row = Math.max(0, row);
                                int simulatedTotal = pkv.toolbarManager.inactiveTools.contains(pkv.toolbarManager.activeDragItem) ? pkv.toolbarManager.inactiveTools.size() : pkv.toolbarManager.inactiveTools.size() + 1;
                                int itemsInRow = Math.max(1, Math.min(4, simulatedTotal - (row * 4)));
                                float startXOffset = (pkv.getWidth() - (itemsInRow * inactiveColWidth)) / 2f;

                                int col = (int) ((pkv.toolbarManager.activeDragItem.currentX - startXOffset + (inactiveColWidth / 2f)) / inactiveColWidth);
                                col = Math.max(0, Math.min(col, 3));
                                int newIndex = row * 4 + col;
                                newIndex = Math.max(0, Math.min(newIndex, pkv.toolbarManager.inactiveTools.size()));

                                if (pkv.toolbarManager.activeTools.contains(pkv.toolbarManager.activeDragItem)) {
                                    pkv.toolbarManager.activeTools.remove(pkv.toolbarManager.activeDragItem);
                                    pkv.toolbarManager.inactiveTools.add(newIndex, pkv.toolbarManager.activeDragItem);
                                    pkv.triggerHapticFeedback();
                                    pkv.toolbarManager.updateToolbarTargets(false, pkv.getWidth(), pkv.getHeight(), (int)toolbarHeight);
                                } else {
                                    int oldIndex = pkv.toolbarManager.inactiveTools.indexOf(pkv.toolbarManager.activeDragItem);
                                    if (newIndex >= pkv.toolbarManager.inactiveTools.size()) newIndex = pkv.toolbarManager.inactiveTools.size() - 1;
                                    if (oldIndex != -1 && newIndex != oldIndex) {
                                        pkv.toolbarManager.inactiveTools.remove(oldIndex);
                                        pkv.toolbarManager.inactiveTools.add(newIndex, pkv.toolbarManager.activeDragItem);
                                        pkv.triggerHapticFeedback();
                                        pkv.toolbarManager.updateToolbarTargets(false, pkv.getWidth(), pkv.getHeight(), (int)toolbarHeight);
                                    }
                                }
                            }
                            pkv.postInvalidateOnAnimation();
                        } else {
                            if (Math.abs(x - pkv.toolbarManager.toolbarTouchDownX) > pkv.dpToPx(10) || Math.abs(y - pkv.toolbarManager.toolbarTouchDownY) > pkv.dpToPx(10)) {
                                handler.removeCallbacks(toolbarLongPressRunnable);
                                pkv.toolbarManager.activeDragItem.isPressed = false;
                                pkv.toolbarManager.activeDragItem = null;
                                pkv.postInvalidateOnAnimation();
                            }
                        }
                    }
                    return true;
                }
                
                // NEW FIX: Dynamic swipe eligibility calculation (Replaces stale static maps)
                boolean isSwipeEligible = false;
                if (activeKey != null) {
                    CustomKeyManager ckm = CustomKeyManager.getInstance(pkv.getContext());
                    String effLabelMove = ckm.getLabel(activeKey.label);
                    if (effLabelMove == null || effLabelMove.isEmpty()) effLabelMove = activeKey.label;
                    isSwipeEligible = getActivePopups(activeKey.label, effLabelMove, ckm) != null;
                }

                if (isLongPressTriggered && activeKey != null && isSwipeEligible) {
                    float dx = x - keyTouchDownX;
                    float dy = y - keyTouchDownY;
                    float threshold = pkv.dpToPx(20);
                    int newDirection = 0;

                    if (Math.abs(dx) > threshold || Math.abs(dy) > threshold) {
                        if (Math.abs(dx) > Math.abs(dy)) {
                            newDirection = dx > 0 ? 4 : 3; 
                        } else {
                            newDirection = dy > 0 ? 2 : 1; 
                        }
                    }
                    if (currentSwipeDirection != newDirection) {
                        currentSwipeDirection = newDirection;
                        pkv.triggerHapticFeedback();
                        pkv.postInvalidateOnAnimation();
                    }
                    return true;
                }
                
                float dxMove = Math.abs(x - lastTouchX), dyMove = Math.abs(y - lastTouchY);
                if (dxMove > pkv.dpToPx(8) || dyMove > pkv.dpToPx(8)) {
                    KeyData moveKey = findKeyAt(x, y);

                    if (activeKey != moveKey) {
                        handler.removeCallbacksAndMessages(null);
                        isLongPressTriggered = false;
                        popupKey = null;
                        if (activeKey != null) activeKey.isPressed = false;
                        activeKey = moveKey;

                        if (activeKey != null) {
                            activeKey.isPressed = true;
                            activeKey.touchX = x; 
                            activeKey.touchY = y;
                            activeKey.rippleRadius = pkv.dpToPx(10); 
                            activeKey.rippleAlpha = 0f;
                            
                            int themeColor = pkv.themeManager.activeTheme != null ? pkv.themeManager.activeTheme.enterBgColor : 0xFF4A90E2;
                            pkv.touchEffectManager.addEffect(x, y, themeColor);
                        }
                        pkv.postInvalidateOnAnimation();
                    }
                    lastTouchX = x;
                    lastTouchY = y;
                }
                return true;

            case MotionEvent.ACTION_UP:
                if (pkv.toolbarManager.isToolbarTouch) {
                    pkv.toolbarManager.isToolbarTouch = false;
                    handler.removeCallbacks(toolbarLongPressRunnable);
                    
                    boolean shouldShowTools = pkv.suggestionManager.showToolbar || pkv.toolbarManager.isMoreFeaturesOpen;

                    if (pkv.toolbarManager.activeDragItem != null) {
                        if (pkv.toolbarManager.isToolbarLongPressTriggered) {
                            pkv.toolbarManager.activeDragItem.isDragging = false;
                            pkv.toolbarManager.activeDragItem.isPressed = false;
                            pkv.toolbarManager.updateToolbarTargets(false, pkv.getWidth(), pkv.getHeight(), (int)toolbarHeight);
                            pkv.toolbarManager.saveToolbarOrder();
                        } else {
                            pkv.toolbarManager.activeDragItem.isPressed = false;
                            float cxStart = 0f;
                            float cyStart = 0f;
                            if (pkv.toolbarManager.activeTools.contains(pkv.toolbarManager.activeDragItem)) {
                                cxStart = pkv.toolbarManager.activeDragItem.currentX + ((toolbarAreaWidth / Math.max(1, pkv.toolbarManager.activeTools.size())) / 2f);
                                cyStart = toolbarHeight / 2f;
                            } else {
                                cxStart = pkv.toolbarManager.activeDragItem.currentX + ((pkv.getWidth() / 4f) / 2f);
                                cyStart = toolbarHeight + pkv.toolbarManager.activeDragItem.currentY + (pkv.dpToPx(80) / 2f);
                            }
                            pkv.toolbarManager.onToolbarItemClick(pkv.toolbarManager.activeDragItem.id, cxStart, cyStart);
                        }
                        pkv.toolbarManager.activeDragItem = null;
                        pkv.postInvalidateOnAnimation();
                    } else if (!shouldShowTools && y <= toolbarHeight && x < toolbarAreaWidth) {
                        if (Math.abs(x - pkv.toolbarManager.toolbarTouchDownX) < pkv.dpToPx(15) && Math.abs(y - pkv.toolbarManager.toolbarTouchDownY) < pkv.dpToPx(15)) {
                            if (!pkv.suggestionManager.currentSuggestions.isEmpty()) {
                                String picked = "";
                                if (x < toolbarAreaWidth * 0.33f) {
                                    picked = pkv.suggestionManager.currentSuggestions.get(0);
                                } else if (x < toolbarAreaWidth * 0.66f) {
                                    picked = pkv.suggestionManager.currentSuggestions.size() > 1 ? pkv.suggestionManager.currentSuggestions.get(1) : pkv.suggestionManager.currentSuggestions.get(0);
                                } else {
                                    picked = pkv.suggestionManager.currentSuggestions.size() > 2 ? pkv.suggestionManager.currentSuggestions.get(2) : pkv.suggestionManager.currentSuggestions.get(0);
                                }
                                if (pkv.listener != null) pkv.listener.onKeyClick("SUG:" + picked);
                                pkv.triggerHapticFeedback();
                            }
                        }
                    }
                    return true;
                }
                
                handler.removeCallbacksAndMessages(null);

                if (activeKey != null) {
                    String physicalLabel = activeKey.label;
                    
                    CustomKeyManager ckm = CustomKeyManager.getInstance(pkv.getContext());
                    String effLabel = ckm.getLabel(physicalLabel);
                    if (effLabel == null || effLabel.isEmpty()) effLabel = physicalLabel;

                    // Fetch dynamically resolved popups array to avoid static map bugs
                    String[] activePopups = getActivePopups(physicalLabel, effLabel, ckm);

                    if (isLongPressTriggered && activePopups != null) {
                        String selectedSymbol = activePopups[currentSwipeDirection];
                        if (selectedSymbol != null && !selectedSymbol.isEmpty()) {
                            if (pkv.listener != null) pkv.listener.onKeyClick(selectedSymbol);
                        }
                    } 
                    else if (!isLongPressTriggered || !physicalLabel.equals("DEL")) { 
                        
                        // New Logic: Check effLabel directly for rearranged System features!
                        if (effLabel.equals("?123")) { pkv.currentMode = ProKeyboardView.MODE_SYMBOLS;
                            pkv.requestLayout(); pkv.layoutManager.buildKeys(); } 
                        else if (effLabel.equals("=\\<")) { pkv.currentMode = ProKeyboardView.MODE_SYMBOLS_PAGE_2;
                            pkv.layoutManager.buildKeys(); } 
                        else if (effLabel.equals("ABC")) { pkv.currentMode = ProKeyboardView.MODE_TEXT;
                            pkv.requestLayout(); pkv.layoutManager.buildKeys(); } 
                        else if (effLabel.equals("EMOJI")) { 
                            if (pkv.emojiSheet != null) pkv.emojiSheet.show(pkv.getWidth() / 2f, pkv.getHeight() / 2f);
                        } 
                        else if (effLabel.equals("SHIFT")) {
                            long now = System.currentTimeMillis();
                            if (pkv.isCapsLock) { 
                                pkv.isCapsLock = false; 
                                if (pkv.listener != null) pkv.listener.onKeyClick("SHIFT"); 
                            } 
                            else if (now - pkv.lastShiftPressTime < 300) { 
                                pkv.isCapsLock = true;
                                pkv.isShifted = true; 
                                pkv.postInvalidateOnAnimation();
                            } 
                            else { 
                                if (pkv.listener != null) pkv.listener.onKeyClick("SHIFT"); 
                            }
                            pkv.lastShiftPressTime = now;
                        } else if (pkv.listener != null) {
                            String textToCommit = physicalLabel;
                            if (physicalLabel.length() == 1 && Character.isLetter(physicalLabel.charAt(0)) && pkv.currentMode == ProKeyboardView.MODE_TEXT) {
                                textToCommit = (pkv.isShifted || pkv.isCapsLock) ? physicalLabel.toUpperCase() : physicalLabel.toLowerCase();
                            } else if (Arrays.asList(KeyboardData.TOP_ROW_SLANG).contains(physicalLabel)) {
                                textToCommit = (pkv.isShifted || pkv.isCapsLock) ? physicalLabel.toUpperCase() : physicalLabel.toLowerCase();
                                textToCommit += " "; 
                            }
                            
                            if (pkv.themeManager.activeFont != null && pkv.currentMode == ProKeyboardView.MODE_TEXT && !pkv.themeManager.activeFont.name.equals("Default")) {
                                textToCommit = FontData.convertText(textToCommit, pkv.themeManager.activeFont);
                            }
                            
                            pkv.listener.onKeyClick(textToCommit); // GboardService will intercept and handle mappings!
                        }
                    }
                    activeKey.isPressed = false;
                    activeKey = null; 
                    popupKey = null;
                    pkv.postInvalidateOnAnimation();
                }
                return true;

            case MotionEvent.ACTION_CANCEL:
                if (pkv.toolbarManager.isToolbarTouch) {
                    pkv.toolbarManager.isToolbarTouch = false;
                    handler.removeCallbacks(toolbarLongPressRunnable);
                    if (pkv.toolbarManager.activeDragItem != null) {
                        pkv.toolbarManager.activeDragItem.isDragging = false;
                        pkv.toolbarManager.activeDragItem.isPressed = false;
                        pkv.toolbarManager.activeDragItem = null;
                        pkv.toolbarManager.updateToolbarTargets(false, pkv.getWidth(), pkv.getHeight(), (int)toolbarHeight);
                        pkv.postInvalidateOnAnimation();
                    }
                    return true;
                }
                
                handler.removeCallbacksAndMessages(null);
                if (activeKey != null) {
                    activeKey.isPressed = false;
                    activeKey = null;
                    popupKey = null;
                    pkv.postInvalidateOnAnimation();
                }
                return true;
        }
        return false;
    }

    private KeyData findKeyAt(float x, float y) {
        if (y < pkv.getToolbarHeight() || pkv.toolbarManager.isMoreFeaturesOpen) return null;
        
        for (KeyData key : pkv.keys) {
            if (key.bounds.contains(x, y)) {
                return key;
            }
        }
        
        KeyData closestKey = null;
        float minDistance = Float.MAX_VALUE;
        float maxAllowedDistSq = pkv.dpToPx(40) * pkv.dpToPx(40); 
        
        for (KeyData key : pkv.keys) {
            float cx = key.bounds.centerX();
            float cy = key.bounds.centerY();
            float distSq = (x - cx) * (x - cx) + (y - cy) * (y - cy);
            
            if (distSq < minDistance && distSq < maxAllowedDistSq) {
                minDistance = distSq;
                closestKey = key;
            }
        }
        return closestKey;
    }
}