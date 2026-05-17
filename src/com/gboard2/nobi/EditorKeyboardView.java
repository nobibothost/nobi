package com.gboard2.nobi;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.SystemClock;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

public class EditorKeyboardView extends ProKeyboardView {

    public KeyData draggedKey = null;
    public KeyData targetKey = null;
    public float dragX = 0, dragY = 0;
    
    public Runnable onStateChangedCallback;
    
    private boolean isDraggingKey = false;
    private KeyData potentialDragKey = null;
    private Handler handler = new Handler();
    private static final long LONG_PRESS_TIMEOUT = 400; 

    private List<String> unswappableKeys = Arrays.asList("SHIFT", "?123", "ABC", "=\\<", "LANG", "EMOJI");

    private Runnable longPressRunnable = new Runnable() {
        @Override
        public void run() {
            if (potentialDragKey != null) {
                CustomKeyManager manager = CustomKeyManager.getInstance(getContext());
                if (manager.getActiveProfile().equals("Default")) {
                    Toast.makeText(getContext(), "Default layout cannot be rearranged. Create a new profile.", Toast.LENGTH_SHORT).show();
                    potentialDragKey = null;
                    return;
                }
                
                isDraggingKey = true;
                draggedKey = potentialDragKey;
                dragX = draggedKey.bounds.centerX();
                dragY = draggedKey.bounds.centerY();
                performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                
                MotionEvent cancelEvent = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_CANCEL, 0, 0, 0);
                EditorKeyboardView.super.onTouchEvent(cancelEvent);
                cancelEvent.recycle();
                
                invalidate();
            }
        }
    };

    public EditorKeyboardView(Context context) {
        super(context);
        this.renderer = new EditorKeyboardRenderer(this);
        
        this.setKeyboardListener(new KeyboardListener() {
            @Override
            public void onKeyClick(String rawKey) {
                if (rawKey == null || rawKey.isEmpty()) return;
                
                final CustomKeyManager manager = CustomKeyManager.getInstance(getContext());
                if (manager.getActiveProfile().equals("Default")) {
                    Toast.makeText(getContext(), "Default layout cannot be edited. Please create a new profile.", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (rawKey.startsWith("CMD_")) {
                    Toast.makeText(getContext(), "System commands cannot be edited directly.", Toast.LENGTH_SHORT).show();
                    return;
                }

                KeyData editorKey = new KeyData(rawKey, rawKey, "");
                KeyEditSheet sheet = new KeyEditSheet(getContext(), editorKey, new Runnable() {
                    @Override
                    public void run() {
                        manager.saveToHistory(); 
                        refreshLayout(); 
                        postInvalidateOnAnimation();
                        if (onStateChangedCallback != null) onStateChangedCallback.run(); 
                    }
                });
                sheet.show();
            }
        });
    }

    private KeyData findKey(float x, float y) {
        for (KeyData k : keys) {
            if (k.bounds.contains(x, y)) return k;
        }
        return null;
    }

    private boolean canBeDragged(KeyData k) {
        if (k == null) return false;
        String lbl = k.label;
        if (lbl.startsWith("CUSTOM_")) lbl = lbl.substring(7);
        return !unswappableKeys.contains(lbl);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                potentialDragKey = findKey(x, y);
                if (potentialDragKey != null && canBeDragged(potentialDragKey)) {
                    handler.postDelayed(longPressRunnable, LONG_PRESS_TIMEOUT);
                }
                super.onTouchEvent(event);
                return true;

            case MotionEvent.ACTION_MOVE:
                if (isDraggingKey) {
                    dragX = x;
                    dragY = y;
                    KeyData hoverKey = findKey(x, y);
                    if (hoverKey != null && canBeDragged(hoverKey)) {
                        targetKey = hoverKey;
                    } else {
                        targetKey = null;
                    }
                    invalidate(); 
                    return true;
                } else if (potentialDragKey != null) {
                    float dx = Math.abs(x - potentialDragKey.bounds.centerX());
                    float dy = Math.abs(y - potentialDragKey.bounds.centerY());
                    if (dx > dpToPx(10) || dy > dpToPx(10)) {
                        handler.removeCallbacks(longPressRunnable);
                        potentialDragKey = null;
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                handler.removeCallbacks(longPressRunnable);
                if (isDraggingKey) {
                    if (targetKey != null && targetKey != draggedKey) {
                        swapKeys(draggedKey, targetKey);
                    }
                    
                    isDraggingKey = false;
                    draggedKey = null;
                    targetKey = null;
                    potentialDragKey = null;
                    
                    refreshLayout(); 
                    invalidate();
                    return true; 
                }
                potentialDragKey = null;
                break;
        }

        if (!isDraggingKey) {
            return super.onTouchEvent(event);
        }
        return true;
    }

    private class KeyProfile {
        String label; String action; String value; String[] popups;
    }

    private KeyProfile getProfileForSwap(String rawId) {
        CustomKeyManager ckm = CustomKeyManager.getInstance(getContext());
        KeyProfile p = new KeyProfile();
        p.label = ckm.getLabel(rawId);
        p.action = ckm.getActionType(rawId);
        p.value = ckm.getValue(rawId);
        p.popups = ckm.getPopups(rawId);

        if (p.label == null || p.label.isEmpty()) p.label = rawId;
        if (p.action == null || p.action.equals("DEFAULT")) {
            if (rawId.equals("DEL")) { p.action = "DELETE"; p.value = ""; }
            else if (rawId.equals("SPACE")) { p.action = "SPACE"; p.value = ""; }
            else if (rawId.equals("ENTER")) { p.action = "ENTER"; p.value = ""; }
            else { p.action = "WRITE"; p.value = rawId; }
        }
        
        if (p.popups == null) p.popups = new String[]{"", "", "", "", ""};
        return p;
    }

    private void swapKeys(KeyData k1, KeyData k2) {
        CustomKeyManager ckm = CustomKeyManager.getInstance(getContext());
        String id1 = k1.label.startsWith("CUSTOM_") ? k1.label.substring(7) : k1.label;
        String id2 = k2.label.startsWith("CUSTOM_") ? k2.label.substring(7) : k2.label;

        KeyProfile p1 = getProfileForSwap(id1);
        KeyProfile p2 = getProfileForSwap(id2);

        ckm.setCustomMapping(id1, p2.label, p2.action, p2.value, p2.popups);
        ckm.setCustomMapping(id2, p1.label, p1.action, p1.value, p1.popups);
        
        ckm.setSwapped(id1, !p2.label.equalsIgnoreCase(id1));
        ckm.setSwapped(id2, !p1.label.equalsIgnoreCase(id2));
        
        ckm.saveToHistory(); 
        if (onStateChangedCallback != null) onStateChangedCallback.run(); 

        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (renderer != null) {
            renderer.draw(canvas);
        }
    }

    @Override
    public float getToolbarHeight() {
        return 0f; 
    }
}