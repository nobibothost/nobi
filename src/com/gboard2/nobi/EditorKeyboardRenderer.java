package com.gboard2.nobi;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.view.inputmethod.EditorInfo;
import java.util.Arrays;

public class EditorKeyboardRenderer extends KeyboardRenderer {

    public EditorKeyboardRenderer(ProKeyboardView pkv) {
        super(pkv);
    }

    @Override
    public void draw(Canvas canvas) {
        long now = SystemClock.uptimeMillis();
        float dt = (lastFrameTime == 0) ? 0.016f : (now - lastFrameTime) / 1000f;
        lastFrameTime = now;
        boolean needsRedraw = false;

        EditorKeyboardView edView = (EditorKeyboardView) pkv;

        for (KeyData key : pkv.keys) {
            if (key.isPressed && key != edView.draggedKey) {
                key.scaleProgress = Math.min(1f, key.scaleProgress + dt * 15f);
                key.rippleRadius += dt * pkv.dpToPx(350); 
                key.rippleAlpha = Math.min(0.2f, key.rippleAlpha + dt * 4f);
            } else {
                key.scaleProgress = Math.max(0f, key.scaleProgress - dt * 12f);
                key.rippleRadius += dt * pkv.dpToPx(500); 
                key.rippleAlpha = Math.max(0f, key.rippleAlpha - dt * 1.5f);
            }
            if (key.scaleProgress > 0 || key.rippleAlpha > 0) needsRedraw = true;
        }

        if (pkv.themeManager.customBackgroundDrawable != null) {
            pkv.themeManager.customBackgroundDrawable.setBounds(0, 0, pkv.getWidth(), pkv.getHeight());
            pkv.themeManager.customBackgroundDrawable.draw(canvas);
        } else {
            canvas.drawRect(0, 0, pkv.getWidth(), pkv.getHeight(), pkv.themeManager.bgPaint);
        }

        float radius = pkv.dpToPx(8);
        CustomKeyManager ckm = CustomKeyManager.getInstance(pkv.getContext());
        
        for (KeyData key : pkv.keys) {
            RectF drawBounds = new RectF(key.bounds);
            float currentScale = 1.0f - (key.scaleProgress * 0.04f);
            float dx = drawBounds.width() * (1f - currentScale) / 2f;
            float dy = drawBounds.height() * (1f - currentScale) / 2f;
            drawBounds.inset(dx, dy);

            boolean isDragged = (edView.draggedKey != null && key == edView.draggedKey);
            boolean isTarget = (edView.targetKey != null && key == edView.targetKey);

            if (isDragged) {
                Paint faded = new Paint();
                faded.setColor(Color.parseColor("#1A000000"));
                canvas.drawRoundRect(drawBounds, radius, radius, faded);
                continue; 
            }

            String physicalLabel = key.label;
            String effLabel = ckm.getLabel(physicalLabel);
            if (effLabel == null || effLabel.isEmpty()) effLabel = physicalLabel;

            Paint currentBgPaint = pkv.themeManager.keyBgPaint;
            boolean isSpecialKey = false;
            boolean isEmojiKey = Arrays.asList(KeyboardData.TOP_ROW_EMOJIS).contains(effLabel);
            boolean isSlangKey = Arrays.asList(KeyboardData.TOP_ROW_SLANG).contains(effLabel);

            if (physicalLabel.equals("SHIFT") || physicalLabel.equals("DEL") || physicalLabel.equals("?123") || 
                physicalLabel.equals("ABC") || physicalLabel.equals("=\\<") || physicalLabel.equals("LANG") || physicalLabel.equals("EMOJI") ||
                (pkv.currentMode == ProKeyboardView.MODE_NUMBER && (physicalLabel.equals("-") || physicalLabel.equals("SPACE")))) {
                currentBgPaint = pkv.themeManager.specialKeyPaint;
                isSpecialKey = true;
            }
            if (isEmojiKey || isSlangKey) { currentBgPaint = pkv.themeManager.specialKeyPaint; isSpecialKey = true; }
            if (physicalLabel.equals("ENTER") && !key.isPressed) currentBgPaint = pkv.themeManager.enterBgPaint;

            boolean isSwapped = ckm.isSwapped(physicalLabel);
            boolean isEdited = false;
            
            String customAction = ckm.getActionType(physicalLabel);
            String customLabel = ckm.getLabel(physicalLabel);
            String customValue = ckm.getValue(physicalLabel);
            String[] customLpCheck = ckm.getPopups(physicalLabel);
            
            boolean hasPopups = false;
            if (customLpCheck != null) {
                for (String p : customLpCheck) {
                    if (p != null && !p.trim().isEmpty()) { hasPopups = true; break; }
                }
            }

            if (hasPopups || 
               (customAction != null && !customAction.equals("DEFAULT") && !customAction.equals("WRITE")) ||
               (customAction != null && customAction.equals("WRITE") && customValue != null && !customValue.equalsIgnoreCase(customLabel) && customValue.length() > 1)) {
                isEdited = true;
            } else if (!isSwapped && customLabel != null && !customLabel.isEmpty() && !customLabel.equalsIgnoreCase(physicalLabel)) {
                isEdited = true; 
            }

            boolean hasCustomColor = isSwapped || isEdited;
            String bgHex = ""; String strokeHex = ""; String textHex = "";

            if (isSwapped && isEdited) {
                bgHex = "#F3E5F5"; strokeHex = "#9C27B0"; textHex = "#6A1B9A";
            } else if (isSwapped) {
                bgHex = "#E3F2FD"; strokeHex = "#2196F3"; textHex = "#1565C0";
            } else if (isEdited) {
                bgHex = "#E8F5E9"; strokeHex = "#4CAF50"; textHex = "#2E7D32";
            }

            if (hasCustomColor) {
                Paint customBg = new Paint(Paint.ANTI_ALIAS_FLAG);
                customBg.setColor(Color.parseColor(bgHex)); 
                canvas.drawRoundRect(drawBounds, radius, radius, customBg);
                
                Paint customStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
                customStroke.setStyle(Paint.Style.STROKE);
                customStroke.setStrokeWidth(pkv.dpToPx(1.5f));
                customStroke.setColor(Color.parseColor(strokeHex)); 
                canvas.drawRoundRect(drawBounds, radius, radius, customStroke);
            } else {
                if (pkv.themeManager.customKeyDrawable != null) {
                    pkv.themeManager.customKeyDrawable.setBounds((int)drawBounds.left, (int)drawBounds.top, (int)drawBounds.right, (int)drawBounds.bottom);
                    pkv.themeManager.customKeyDrawable.draw(canvas);
                } else {
                    canvas.drawRoundRect(drawBounds, radius, radius, currentBgPaint);
                    if (currentBgPaint.getAlpha() < 255 && currentBgPaint.getAlpha() > 0) {
                        canvas.drawRoundRect(drawBounds, radius, radius, pkv.themeManager.glassBorderPaint);
                    }
                }
            }

            if (isTarget) {
                Paint targetStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
                targetStroke.setStyle(Paint.Style.STROKE);
                targetStroke.setStrokeWidth(pkv.dpToPx(3f));
                targetStroke.setColor(Color.parseColor("#FF9800")); 
                canvas.drawRoundRect(drawBounds, radius, radius, targetStroke);
            }
            
            if (key.rippleAlpha > 0) {
                if (physicalLabel.equals("ENTER")) pkv.themeManager.ripplePaint.setColor(Color.parseColor("#FFFFFF"));
                else if (isSpecialKey) pkv.themeManager.ripplePaint.setColor(Color.parseColor("#33000000")); 
                else pkv.themeManager.ripplePaint.setColor(Color.parseColor("#22000000")); 
                pkv.themeManager.ripplePaint.setAlpha((int) (255 * key.rippleAlpha));

                canvas.save();
                pkv.themeManager.clipPath.reset();
                pkv.themeManager.clipPath.addRoundRect(drawBounds, radius, radius, Path.Direction.CW);
                canvas.clipPath(pkv.themeManager.clipPath);
                canvas.drawCircle(key.touchX, key.touchY, key.rippleRadius, pkv.themeManager.ripplePaint);
                canvas.restore();
            }

            if (physicalLabel.equals("DEL")) drawEditorIconCenter(canvas, pkv.themeManager.iconBackspace, drawBounds, pkv.resizeController.userHeightScale, hasCustomColor, textHex);
            else if (physicalLabel.equals("SHIFT")) {
                if (pkv.isCapsLock) pkv.themeManager.activeShiftIcon = pkv.themeManager.iconCapslock;
                else if (pkv.isShifted) pkv.themeManager.activeShiftIcon = pkv.themeManager.iconShiftFilled; 
                else pkv.themeManager.activeShiftIcon = pkv.themeManager.iconShift; 
                drawEditorIconCenter(canvas, pkv.themeManager.activeShiftIcon, drawBounds, pkv.resizeController.userHeightScale, hasCustomColor, textHex);
            } else if (physicalLabel.equals("ENTER")) {
                Drawable actionIcon = pkv.themeManager.iconEnter;
                switch (pkv.currentImeAction) {
                    case EditorInfo.IME_ACTION_SEARCH: actionIcon = pkv.themeManager.iconSearch; break;
                    case EditorInfo.IME_ACTION_SEND: actionIcon = pkv.themeManager.iconSend; break;     
                    case EditorInfo.IME_ACTION_DONE: actionIcon = pkv.themeManager.iconDone; break;     
                    case EditorInfo.IME_ACTION_NEXT: actionIcon = pkv.themeManager.iconNext; break;
                }
                drawEditorIconCenter(canvas, actionIcon, drawBounds, pkv.resizeController.userHeightScale, hasCustomColor, textHex);
            } else if (physicalLabel.equals("EMOJI")) drawEditorIconCenter(canvas, pkv.themeManager.iconEmoji, drawBounds, pkv.resizeController.userHeightScale, hasCustomColor, textHex);
            else if (physicalLabel.equals("LANG")) drawEditorIconCenter(canvas, pkv.themeManager.iconLanguage, drawBounds, pkv.resizeController.userHeightScale, hasCustomColor, textHex);
            else if (physicalLabel.equals("SPACE")) {
                if (pkv.currentMode == ProKeyboardView.MODE_NUMBER) {
                    Paint p2 = new Paint(Paint.ANTI_ALIAS_FLAG);
                    p2.setStyle(Paint.Style.STROKE);
                    p2.setStrokeWidth(pkv.dpToPx(2) * pkv.resizeController.userHeightScale);
                    p2.setColor(hasCustomColor ? Color.parseColor(textHex) : pkv.themeManager.textPaint.getColor());
                    p2.setStrokeCap(Paint.Cap.ROUND);
                    p2.setStrokeJoin(Paint.Join.ROUND);
                    float ccx = drawBounds.centerX();
                    float ccy = drawBounds.centerY();
                    float spaceW = pkv.dpToPx(10) * pkv.resizeController.userHeightScale; 
                    float spaceH = pkv.dpToPx(4) * pkv.resizeController.userHeightScale; 
                    Path path = new Path();
                    path.moveTo(ccx - spaceW, ccy - spaceH);
                    path.lineTo(ccx - spaceW, ccy + spaceH);
                    path.lineTo(ccx + spaceW, ccy + spaceH);
                    path.lineTo(ccx + spaceW, ccy - spaceH);
                    canvas.drawPath(path, p2);
                } else {
                    pkv.themeManager.textPaint.setTextSize(pkv.dpToPx(14) * pkv.resizeController.userHeightScale);
                    int origColor = pkv.themeManager.textPaint.getColor();
                    pkv.themeManager.textPaint.setColor(hasCustomColor ? Color.parseColor(textHex) : (pkv.themeManager.activeTheme != null ? pkv.themeManager.activeTheme.suggestionTextColor : Color.parseColor("#90A4AE")));
                    canvas.drawText("Hinglish", drawBounds.centerX(), drawBounds.centerY() - ((pkv.themeManager.textPaint.descent() + pkv.themeManager.textPaint.ascent()) / 2), pkv.themeManager.textPaint);
                    pkv.themeManager.textPaint.setColor(origColor);
                }
            } else {
                String displayLabel = pkv.getDisplayLabel(physicalLabel);

                if (pkv.currentMode == ProKeyboardView.MODE_NUMBER && getDialerLetters(physicalLabel) != null) {
                    int origColorNum = pkv.themeManager.textPaint.getColor();
                    if (hasCustomColor) pkv.themeManager.textPaint.setColor(Color.parseColor(textHex));
                    
                    pkv.themeManager.textPaint.setTextSize(pkv.dpToPx(24) * pkv.resizeController.userHeightScale);
                    canvas.drawText(displayLabel, drawBounds.centerX(), drawBounds.centerY() - pkv.dpToPx(2), pkv.themeManager.textPaint);
                    pkv.themeManager.textPaint.setTextSize(pkv.dpToPx(10) * pkv.resizeController.userHeightScale);
                    
                    pkv.themeManager.textPaint.setColor(hasCustomColor ? Color.parseColor(textHex) : (pkv.themeManager.activeTheme != null ? pkv.themeManager.activeTheme.suggestionTextColor : Color.parseColor("#78909C")));
                    canvas.drawText(getDialerLetters(physicalLabel), drawBounds.centerX(), drawBounds.centerY() + pkv.dpToPx(13), pkv.themeManager.textPaint);
                    pkv.themeManager.textPaint.setColor(origColorNum);
                    
                } else if (isEmojiKey) {
                    pkv.themeManager.textPaint.setTextSize(pkv.dpToPx(24) * pkv.resizeController.userHeightScale);
                    pkv.themeManager.textPaint.setTypeface(android.graphics.Typeface.DEFAULT);
                    
                    float maxTextWidth = drawBounds.width() - pkv.dpToPx(8);
                    float currentSize = pkv.themeManager.textPaint.getTextSize();
                    while (pkv.themeManager.textPaint.measureText(displayLabel) > maxTextWidth && currentSize > pkv.dpToPx(6)) {
                        currentSize -= 1f;
                        pkv.themeManager.textPaint.setTextSize(currentSize);
                    }
                    canvas.drawText(displayLabel, drawBounds.centerX(), drawBounds.centerY() - ((pkv.themeManager.textPaint.descent() + pkv.themeManager.textPaint.ascent()) / 2), pkv.themeManager.textPaint);
                    
                } else {
                    int origColorText = pkv.themeManager.textPaint.getColor();
                    if (hasCustomColor) pkv.themeManager.textPaint.setColor(Color.parseColor(textHex)); 
                    
                    float initialSize = pkv.dpToPx(displayLabel.length() > 1 ? 14 : 22) * pkv.resizeController.userHeightScale;
                    pkv.themeManager.textPaint.setTextSize(initialSize);

                    if (isSlangKey) {
                        pkv.themeManager.textPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
                    } else if (pkv.themeManager.activeFont != null && !pkv.themeManager.activeFont.name.equals("Default")) {
                        pkv.themeManager.textPaint.setTypeface(pkv.themeManager.activeFont.keyboardTypeface);
                    } else {
                        pkv.themeManager.textPaint.setTypeface(android.graphics.Typeface.DEFAULT);
                    }

                    // Strict auto-fit
                    float maxTextWidth = drawBounds.width() - pkv.dpToPx(12); 
                    float currentTextSize = initialSize;
                    
                    while (pkv.themeManager.textPaint.measureText(displayLabel) > maxTextWidth && currentTextSize > pkv.dpToPx(6)) {
                        currentTextSize -= 1f;
                        pkv.themeManager.textPaint.setTextSize(currentTextSize);
                    }
                    
                    canvas.drawText(displayLabel, drawBounds.centerX(), drawBounds.centerY() - ((pkv.themeManager.textPaint.descent() + pkv.themeManager.textPaint.ascent()) / 2), pkv.themeManager.textPaint);
                    if (hasCustomColor) pkv.themeManager.textPaint.setColor(origColorText);
                }
            }

            String hintText = null;
            String[] customLp = ckm.getPopups(physicalLabel);
            
            if (customLp != null && customLp.length > 0 && customLp[0] != null && !customLp[0].isEmpty()) {
                hintText = customLp[0]; 
            } else {
                String[] defaultPopups = KeyboardLayouts.getPopups(effLabel);
                if (defaultPopups != null && defaultPopups.length > 0) {
                     hintText = defaultPopups[0];
                } else if (pkv.currentMode == ProKeyboardView.MODE_TEXT && KeyboardData.SWIPE_EMOJIS.containsKey(effLabel)) {
                     hintText = KeyboardData.SWIPE_EMOJIS.get(effLabel)[0];
                }
            }

            if (hintText != null && !hintText.isEmpty()) {
                Paint hintPaint = new Paint(pkv.themeManager.textPaint);
                hintPaint.setTypeface(android.graphics.Typeface.DEFAULT);
                hintPaint.clearShadowLayer();
                hintPaint.setTextSize(pkv.dpToPx(10) * pkv.resizeController.userHeightScale);
                
                if (hasCustomColor) {
                    hintPaint.setColor(Color.parseColor(textHex)); 
                } else if (pkv.themeManager.activeTheme != null) {
                    hintPaint.setColor(pkv.themeManager.activeTheme.suggestionTextColor);
                } else {
                    hintPaint.setColor(Color.parseColor("#4A90E2"));
                }
                canvas.drawText(hintText, drawBounds.right - pkv.dpToPx(8), drawBounds.top + pkv.dpToPx(12), hintPaint);
            }
        }
        
        if (edView.draggedKey != null) {
            drawFloatingKey(canvas, edView.draggedKey, edView.dragX, edView.dragY, radius, ckm);
            needsRedraw = true; 
        }

        if (needsRedraw) pkv.postInvalidateOnAnimation();
    }

    private void drawFloatingKey(Canvas canvas, KeyData key, float x, float y, float radius, CustomKeyManager ckm) {
        RectF bounds = new RectF(key.bounds);
        float dx = x - bounds.centerX();
        float dy = y - bounds.centerY();
        bounds.offset(dx, dy);
        
        float scale = 1.15f;
        float cx = bounds.centerX();
        float cy = bounds.centerY();
        bounds.left = cx - (bounds.width() / 2f) * scale;
        bounds.right = cx + (bounds.width() / 2f) * scale;
        bounds.top = cy - (bounds.height() / 2f) * scale;
        bounds.bottom = cy + (bounds.height() / 2f) * scale;

        Paint shadow = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadow.setColor(Color.parseColor("#55000000"));
        canvas.drawRoundRect(bounds.left+10, bounds.top+15, bounds.right+10, bounds.bottom+15, radius*scale, radius*scale, shadow);

        String physicalLabel = key.label;
        boolean isSwapped = ckm.isSwapped(physicalLabel);
        boolean isEdited = false;
        
        String customAction = ckm.getActionType(physicalLabel);
        String customLabel = ckm.getLabel(physicalLabel);
        String customValue = ckm.getValue(physicalLabel);
        String[] customLpCheck = ckm.getPopups(physicalLabel);
        
        boolean hasPopups = false;
        if (customLpCheck != null) {
            for (String p : customLpCheck) {
                if (p != null && !p.trim().isEmpty()) { hasPopups = true; break; }
            }
        }

        if (hasPopups || 
           (customAction != null && !customAction.equals("DEFAULT") && !customAction.equals("WRITE")) ||
           (customAction != null && customAction.equals("WRITE") && customValue != null && !customValue.equalsIgnoreCase(customLabel) && customValue.length() > 1)) {
            isEdited = true;
        } else if (!isSwapped && customLabel != null && !customLabel.isEmpty() && !customLabel.equalsIgnoreCase(physicalLabel)) {
            isEdited = true; 
        }

        boolean hasCustomColor = isSwapped || isEdited;
        String bgHex = ""; String strokeHex = ""; String textHex = "";

        if (isSwapped && isEdited) {
            bgHex = "#F3E5F5"; strokeHex = "#9C27B0"; textHex = "#6A1B9A";
        } else if (isSwapped) {
            bgHex = "#E3F2FD"; strokeHex = "#2196F3"; textHex = "#1565C0";
        } else if (isEdited) {
            bgHex = "#E8F5E9"; strokeHex = "#4CAF50"; textHex = "#2E7D32";
        }

        if (hasCustomColor) {
            Paint customBg = new Paint(Paint.ANTI_ALIAS_FLAG);
            customBg.setColor(Color.parseColor(bgHex)); 
            canvas.drawRoundRect(bounds, radius*scale, radius*scale, customBg);
            
            Paint customStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
            customStroke.setStyle(Paint.Style.STROKE);
            customStroke.setStrokeWidth(pkv.dpToPx(2f));
            customStroke.setColor(Color.parseColor(strokeHex)); 
            canvas.drawRoundRect(bounds, radius*scale, radius*scale, customStroke);
        } else {
            Paint bgPaint = pkv.themeManager.keyBgPaint;
            if (physicalLabel.equals("ENTER")) bgPaint = pkv.themeManager.enterBgPaint;
            else if (Arrays.asList("SHIFT", "DEL", "?123", "ABC", "=\\<", "LANG", "EMOJI").contains(physicalLabel)) bgPaint = pkv.themeManager.specialKeyPaint;
            
            canvas.drawRoundRect(bounds, radius*scale, radius*scale, bgPaint);
            if (bgPaint.getAlpha() < 255 && bgPaint.getAlpha() > 0) {
                canvas.drawRoundRect(bounds, radius*scale, radius*scale, pkv.themeManager.glassBorderPaint);
            }
        }

        String displayLabel = pkv.getDisplayLabel(physicalLabel);
        
        float initialSize = pkv.dpToPx(displayLabel.length() > 1 ? 14 : 22) * pkv.resizeController.userHeightScale * scale;
        pkv.themeManager.textPaint.setTextSize(initialSize);
        
        if (hasCustomColor) pkv.themeManager.textPaint.setColor(Color.parseColor(textHex));
        else pkv.themeManager.textPaint.setColor(pkv.themeManager.activeTheme != null ? pkv.themeManager.activeTheme.textColor : Color.BLACK);

        float maxTextWidth = bounds.width() - pkv.dpToPx(12);
        float currentSize = initialSize;
        while (pkv.themeManager.textPaint.measureText(displayLabel) > maxTextWidth && currentSize > pkv.dpToPx(6)) {
            currentSize -= 1f;
            pkv.themeManager.textPaint.setTextSize(currentSize);
        }

        canvas.drawText(displayLabel, bounds.centerX(), bounds.centerY() - ((pkv.themeManager.textPaint.descent() + pkv.themeManager.textPaint.ascent()) / 2), pkv.themeManager.textPaint);
    }

    private void drawEditorIconCenter(Canvas canvas, Drawable icon, RectF bounds, float scale, boolean hasCustomColor, String textHex) {
        if (icon == null) return;
        int iconSize = (int) (pkv.dpToPx(22) * scale); 
        int cxIcon = (int) bounds.centerX(), cyIcon = (int) bounds.centerY() - pkv.dpToPx(1);
        icon.setBounds(cxIcon - (iconSize / 2), cyIcon - (iconSize / 2), cxIcon + (iconSize / 2), cyIcon + (iconSize / 2));
        
        if (hasCustomColor) icon.setColorFilter(Color.parseColor(textHex), android.graphics.PorterDuff.Mode.SRC_IN);
        icon.draw(canvas);
        if (hasCustomColor) icon.clearColorFilter();
    }
}