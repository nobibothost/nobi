package com.gboard2.nobi;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import java.util.Arrays;

public class PopupRenderer {

    private ProKeyboardView pkv;

    public PopupRenderer(ProKeyboardView pkv) {
        this.pkv = pkv;
    }

    public void drawPopupPreview(Canvas canvas, KeyData key) {
        // FIXED: Removed strict currentMode checking. Now it directly verifies if the label exists in dictionaries.
        boolean isEmojiSwipe = pkv.touchHandler.isLongPressTriggered && pkv.swipeEmojiManager.swipeEmojis.containsKey(key.label);
        boolean isSymbolSwipe = pkv.touchHandler.isLongPressTriggered && pkv.swipeSymbolManager.swipeSymbols.containsKey(key.label);
        boolean isSwipePopup = isEmojiSwipe || isSymbolSwipe;

        float popupWidth = key.bounds.width() * 1.5f;
        float popupHeight = key.bounds.height() * 1.3f;

        if (isSwipePopup) {
            popupWidth = pkv.dpToPx(130) * pkv.resizeController.userHeightScale;
            popupHeight = pkv.dpToPx(130) * pkv.resizeController.userHeightScale;
        }
        
        float cxPopup = key.bounds.centerX();
        if (cxPopup - (popupWidth / 2) < pkv.dpToPx(4)) cxPopup = (popupWidth / 2) + pkv.dpToPx(4);
        else if (cxPopup + (popupWidth / 2) > pkv.getWidth() - pkv.dpToPx(4)) cxPopup = pkv.getWidth() - (popupWidth / 2) - pkv.dpToPx(4);

        float cyPopup = key.bounds.top - (popupHeight / 2) + pkv.dpToPx(4);
        if (cyPopup - (popupHeight / 2) < 0) cyPopup = (popupHeight / 2) + pkv.dpToPx(2);

        RectF popupBounds = new RectF(cxPopup - (popupWidth / 2), cyPopup - (popupHeight / 2), cxPopup + (popupWidth / 2), cyPopup + (popupHeight / 2));

        pkv.themeManager.popupBgPaint.setColor(pkv.themeManager.activeTheme != null ? pkv.themeManager.activeTheme.keyBgColor : Color.WHITE);
        canvas.drawRoundRect(popupBounds, pkv.dpToPx(12), pkv.dpToPx(12), pkv.themeManager.popupBgPaint); 
        
        pkv.themeManager.popupTextPaint.setColor(pkv.themeManager.activeTheme != null ? pkv.themeManager.activeTheme.textColor : Color.parseColor("#263238"));

        boolean isEmojiKey = Arrays.asList(ProKeyboardView.TOP_ROW_EMOJIS).contains(key.label);
        boolean isSlangKey = Arrays.asList(ProKeyboardView.TOP_ROW_SLANG).contains(key.label);
        
        if (isSwipePopup) {
            String[] items = isEmojiSwipe ? pkv.swipeEmojiManager.swipeEmojis.get(key.label) : pkv.swipeSymbolManager.swipeSymbols.get(key.label);
            float pCx = popupBounds.centerX();
            float pCy = popupBounds.centerY();
            float spacing = pkv.dpToPx(38) * pkv.resizeController.userHeightScale;

            float[][] coords = {
                {pCx, pCy}, 
                {pCx, pCy - spacing}, 
                {pCx, pCy + spacing}, 
                {pCx - spacing, pCy}, 
                {pCx + spacing, pCy}  
            };

            pkv.themeManager.popupTextPaint.setTypeface(android.graphics.Typeface.DEFAULT);

            for (int i = 0; i < 5; i++) {
                float textSize = (i == pkv.touchHandler.currentSwipeDirection) ? pkv.dpToPx(28) : pkv.dpToPx(16);
                pkv.themeManager.popupTextPaint.setTextSize(textSize * pkv.resizeController.userHeightScale);
                pkv.themeManager.popupTextPaint.setAlpha((i == pkv.touchHandler.currentSwipeDirection) ? 255 : 100);
                canvas.drawText(items[i], coords[i][0], coords[i][1] - ((pkv.themeManager.popupTextPaint.descent() + pkv.themeManager.popupTextPaint.ascent()) / 2), pkv.themeManager.popupTextPaint);
            }
            pkv.themeManager.popupTextPaint.setAlpha(255);

        } else if (isEmojiKey) {
            pkv.themeManager.popupTextPaint.setTypeface(android.graphics.Typeface.DEFAULT);
            pkv.themeManager.popupTextPaint.setTextSize(pkv.dpToPx(32) * pkv.resizeController.userHeightScale);
            canvas.drawText(key.label, popupBounds.centerX(), popupBounds.centerY() - ((pkv.themeManager.popupTextPaint.descent() + pkv.themeManager.popupTextPaint.ascent()) / 2), pkv.themeManager.popupTextPaint);

        } else if (isSlangKey) {
            pkv.themeManager.popupTextPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            float maxTextWidth = popupBounds.width() - pkv.dpToPx(8);
            float textSize = pkv.dpToPx(16) * pkv.resizeController.userHeightScale; 
            pkv.themeManager.popupTextPaint.setTextSize(textSize);

            while (pkv.themeManager.popupTextPaint.measureText(pkv.getDisplayLabel(key.label)) > maxTextWidth && textSize > pkv.dpToPx(8)) {
                textSize -= pkv.dpToPx(0.5f);
                pkv.themeManager.popupTextPaint.setTextSize(textSize);
            }
            canvas.drawText(pkv.getDisplayLabel(key.label), popupBounds.centerX(), popupBounds.centerY() - ((pkv.themeManager.popupTextPaint.descent() + pkv.themeManager.popupTextPaint.ascent()) / 2), pkv.themeManager.popupTextPaint);

        } else {
            pkv.themeManager.popupTextPaint.setTextSize(pkv.dpToPx(28) * pkv.resizeController.userHeightScale);
            if (pkv.themeManager.activeFont != null && !pkv.themeManager.activeFont.name.equals("Default")) {
                pkv.themeManager.popupTextPaint.setTypeface(pkv.themeManager.activeFont.keyboardTypeface);
            } else {
                pkv.themeManager.popupTextPaint.setTypeface(android.graphics.Typeface.DEFAULT);
            }
            canvas.drawText(pkv.getDisplayLabel(key.label), popupBounds.centerX(), popupBounds.centerY() - ((pkv.themeManager.popupTextPaint.descent() + pkv.themeManager.popupTextPaint.ascent()) / 2), pkv.themeManager.popupTextPaint);
        }
    }
}
