package com.gboard2.nobi;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import java.util.ArrayList;
import java.util.List;

public class ToolbarManager {

    public static class ToolbarItem {
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

    public List<ToolbarItem> activeTools = new ArrayList<>();
    public List<ToolbarItem> inactiveTools = new ArrayList<>();
    
    public boolean isMoreFeaturesOpen = false;
    public float panelScale = 0f;
    public float inactiveStartYOffset = 0f;
    
    public ToolbarItem activeDragItem = null;
    public boolean isToolbarLongPressTriggered = false;
    public float toolbarTouchDownX = 0f;
    public float toolbarTouchDownY = 0f;
    public float dragPointerX = 0f;
    public float dragPointerY = 0f;
    public float dragOffsetX = 0f;
    public float dragOffsetY = 0f;
    public boolean isToolbarTouch = false;

    private Context context;
    private ProKeyboardView pkv;

    public ToolbarManager(Context context, ProKeyboardView pkv) {
        this.context = context;
        this.pkv = pkv;
    }

    public void initInfinityApplets() {
        activeTools.clear();
        inactiveTools.clear();
        SharedPreferences prefs = context.getSharedPreferences("GboardPrefs", Context.MODE_PRIVATE);
        
        String activeOrder = prefs.getString("toolbar_active_list", "TEXT_EDIT,CLIPBOARD,MIC,RESIZE");
        String inactiveOrder = prefs.getString("toolbar_inactive_list", "THEME,FONT,SETTINGS,LAYOUTS");
        
        // --- SMART INJECTION --- 
        // Agar aapke paas pehle se list saved hai aur usme LAYOUTS nahi hai, toh ye line usko auto-add kar degi.
        if (!activeOrder.contains("LAYOUTS") && !inactiveOrder.contains("LAYOUTS")) {
            inactiveOrder += ",LAYOUTS";
            prefs.edit().putString("toolbar_inactive_list", inactiveOrder).apply();
        }

        loadItemsIntoList(activeOrder.split(","), activeTools);
        loadItemsIntoList(inactiveOrder.split(","), inactiveTools);
    }

    private void loadItemsIntoList(String[] ids, List<ToolbarItem> list) {
        for (String id : ids) {
            if (id.trim().isEmpty()) continue;
            ToolbarItem item = new ToolbarItem();
            item.id = id;
            switch (id) {
                case "SETTINGS": item.icon = pkv.themeManager.iconSettings; item.label = "Settings"; break;
                case "FONT": item.icon = pkv.themeManager.iconFont; item.label = "Fonts"; break;
                case "RESIZE": item.icon = pkv.themeManager.iconResize; item.label = "Resize"; break;
                case "TEXT_EDIT": item.icon = pkv.themeManager.iconTextEditing; item.label = "Edit"; break;
                case "THEME": item.icon = pkv.themeManager.iconTheme; item.label = "Theme"; break;
                case "CLIPBOARD": item.icon = pkv.themeManager.iconClipboard; item.label = "Clipboard"; break;
                case "MIC": item.icon = pkv.themeManager.iconMic; item.label = "Voice"; break;
                case "LAYOUTS": item.icon = pkv.themeManager.iconLayouts; item.label = "Layouts"; break; // Added Tool Mapping
            }
            if (item.icon != null) {
                list.add(item);
            }
        }
    }

    public void updateToolbarTargets(boolean snap, int width, int height, int toolbarHeight) {
        if (width == 0) return;
        float toolbarAreaWidth = width - toolbarHeight; 
        float activeSlotWidth = toolbarAreaWidth / Math.max(1, activeTools.size());
        
        for (int i = 0; i < activeTools.size(); i++) {
            ToolbarItem item = activeTools.get(i);
            item.targetX = i * activeSlotWidth;
            item.targetY = 0;
            if (snap || item.currentX == -1f) {
                item.currentX = item.targetX;
                item.currentY = item.targetY;
            }
        }
        
        int MAX_COLS = 4;
        float inactiveColWidth = width / (float) MAX_COLS;
        float inactiveRowHeight = pkv.dpToPx(80);
        
        int numRows = inactiveTools.isEmpty() ? 1 : ((inactiveTools.size() - 1) / MAX_COLS) + 1;
        float availableHeight = height - toolbarHeight;
        float requiredHeight = numRows * inactiveRowHeight;
        inactiveStartYOffset = Math.max(0f, (availableHeight - requiredHeight) / 2f);
        
        for (int i = 0; i < inactiveTools.size(); i++) {
            ToolbarItem item = inactiveTools.get(i);
            int row = i / MAX_COLS;
            int col = i % MAX_COLS;
            int itemsInThisRow = Math.min(MAX_COLS, inactiveTools.size() - (row * MAX_COLS));
            float startXOffset = (width - (itemsInThisRow * inactiveColWidth)) / 2f;
            
            item.targetX = startXOffset + (col * inactiveColWidth);
            item.targetY = inactiveStartYOffset + (row * inactiveRowHeight);
            
            if (snap || item.currentX == -1f) {
                item.currentX = item.targetX;
                item.currentY = item.targetY;
            }
        }
    }

    public void saveToolbarOrder() {
        StringBuilder activeSb = new StringBuilder();
        for (int i = 0; i < activeTools.size(); i++) {
            activeSb.append(activeTools.get(i).id);
            if (i < activeTools.size() - 1) activeSb.append(",");
        }
        
        StringBuilder inactiveSb = new StringBuilder();
        for (int i = 0; i < inactiveTools.size(); i++) {
            inactiveSb.append(inactiveTools.get(i).id);
            if (i < inactiveTools.size() - 1) inactiveSb.append(",");
        }
        
        SharedPreferences prefs = context.getSharedPreferences("GboardPrefs", Context.MODE_PRIVATE);
        prefs.edit().putString("toolbar_active_list", activeSb.toString())
                    .putString("toolbar_inactive_list", inactiveSb.toString()).apply();
    }

    public void onToolbarItemClick(String id, float cx, float cy) {
        pkv.triggerHapticFeedback();
        isMoreFeaturesOpen = false; 
        pkv.postInvalidateOnAnimation();
        
        switch (id) {
            case "MIC": if (pkv.listener != null) pkv.listener.onKeyClick("CMD_VOICE_INPUT"); break;
            case "CLIPBOARD": if (pkv.clipboardBottomSheet != null) pkv.clipboardBottomSheet.show(cx, cy); break;
            case "THEME": if (pkv.themeSheet != null) pkv.themeSheet.show(cx, cy); break;
            case "TEXT_EDIT": if (pkv.textEditingSheet != null) pkv.textEditingSheet.show(cx, cy); break;
            case "RESIZE": pkv.resizeController.isResizing = true; pkv.invalidate(); break;
            case "FONT": if (pkv.fontSheet != null) pkv.fontSheet.show(cx, cy); break;
            case "SETTINGS": if (pkv.listener != null) pkv.listener.onKeyClick("CMD_OPEN_SETTINGS"); break;
            case "LAYOUTS": if (pkv.listener != null) pkv.listener.onKeyClick("CMD_OPEN_LAYOUTS"); break; // Assigned Click Event
        }
    }
}