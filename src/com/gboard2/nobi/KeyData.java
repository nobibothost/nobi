package com.gboard2.nobi;

import android.graphics.RectF;

public class KeyData {
    public String label;
    public String code;
    public String altCode; 
    public RectF bounds;
    
    // UI state added for KeyboardRenderer animations and popups
    public boolean isPressed = false;
    public float touchX = 0f;
    public float touchY = 0f;
    public float rippleRadius = 0f;
    public float rippleAlpha = 0f;
    public float scaleProgress = 0f;

    // --- Custom Action Fields ---
    public String customLabel = null;
    public String actionType = "DEFAULT"; 
    public String actionValue = "";       

    public KeyData(String label, RectF bounds) {
        this.label = label;
        this.code = label;
        this.altCode = "";
        this.bounds = bounds;
    }

    public KeyData(String label, String code, String altCode) {
        this.label = label;
        this.code = code;
        this.altCode = altCode;
        this.bounds = new RectF();
    }

    // Helper method to get the correct label to draw
    public String getDisplayLabel() {
        if (customLabel != null && !customLabel.trim().isEmpty()) {
            return customLabel;
        }
        return label;
    }
}