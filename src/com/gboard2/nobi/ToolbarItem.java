package com.gboard2.nobi;

import android.graphics.drawable.Drawable;

public class ToolbarItem {
    public String id;
    public String label;
    public Drawable icon;
    public float currentX = 0f;
    public float currentY = 0f;
    public float targetX = 0f;
    public float targetY = 0f;
    public boolean isDragging = false;
    public boolean isPressed = false;
    public float scale = 1.0f;
}
