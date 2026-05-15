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

// This renderer extends the base one, but overrides the draw method to ONLY highlight edited keys in the Editor
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

        for (KeyData key : pkv.keys) {
            if (key.isPressed) {
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
        
        float toolbarHeight = pkv.getToolbarHeight();
        canvas.drawRect(0, 0, pkv.getWidth(), toolbarHeight, pkv.themeManager.suggestionBgPaint);
        float toolbarAreaWidth = pkv.getWidth() - toolbarHeight; 
        
        float cxMore = pkv.getWidth() - (toolbarHeight / 2f);
        float cyMore = toolbarHeight / 2f;

        float radius = pkv.dpToPx(8);
        pkv.touchHandler.popupKey = null; 
        CustomKeyManager ckm = CustomKeyManager.getInstance(pkv.getContext());
        
        for (KeyData key : pkv.keys) {
            Paint currentBgPaint = pkv.themeManager.keyBgPaint;
            boolean isSpecialKey = false;
            boolean isEmojiKey = Arrays.asList(ProKeyboardView.TOP_ROW_EMOJIS).contains(key.label);
            boolean isSlangKey = Arrays.asList(ProKeyboardView.TOP_ROW_SLANG).contains(key.label);

            if (key.label.equals("SHIFT") || key.label.equals("DEL") || key.label.equals("?123") || 
                key.label.equals("ABC") || key.label.equals("=\\<") || key.label.equals("LANG") || key.label.equals("EMOJI") ||
                (pkv.currentMode == ProKeyboardView.MODE_NUMBER && (key.label.equals("-") || key.label.equals("SPACE")))) {
                currentBgPaint = pkv.themeManager.specialKeyPaint;
                isSpecialKey = true;
            }
            if (isEmojiKey || isSlangKey) { currentBgPaint = pkv.themeManager.specialKeyPaint; isSpecialKey = true; }
            if (key.label.equals("ENTER") && !key.isPressed) currentBgPaint = pkv.themeManager.enterBgPaint;

            RectF drawBounds = new RectF(key.bounds);
            float currentScale = 1.0f - (key.scaleProgress * 0.04f);
            float dx = drawBounds.width() * (1f - currentScale) / 2f;
            float dy = drawBounds.height() * (1f - currentScale) / 2f;
            drawBounds.inset(dx, dy);
            
            // --- EDIT MODE HIGHLIGHT LOGIC ---
            boolean isEdited = false;
            String customAction = ckm.getActionType(key.label);
            String customLabel = ckm.getLabel(key.label);
            String customValue = ckm.getValue(key.label);
            String[] customLpCheck = ckm.getPopups(key.label);
            
            if ((customAction != null && !customAction.equals("DEFAULT") && !customAction.equals("Write Text")) || 
                (customLabel != null && !customLabel.trim().isEmpty()) ||
                (customValue != null && !customValue.trim().isEmpty()) ||
                (customLpCheck != null && customLpCheck.length > 0 && customLpCheck[0] != null && !customLpCheck[0].isEmpty())) {
                isEdited = true;
            }

            // KEY BACKGROUND RENDERING (OVERRIDES THEME IF EDITED)
            if (isEdited) {
                Paint editedBg = new Paint(Paint.ANTI_ALIAS_FLAG);
                editedBg.setColor(Color.parseColor("#E8F5E9")); 
                canvas.drawRoundRect(drawBounds, radius, radius, editedBg);
                
                Paint editedStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
                editedStroke.setStyle(Paint.Style.STROKE);
                editedStroke.setStrokeWidth(pkv.dpToPx(1.5f));
                editedStroke.setColor(Color.parseColor("#4CAF50")); 
                canvas.drawRoundRect(drawBounds, radius, radius, editedStroke);
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
            
            if (key.rippleAlpha > 0) {
                if (key.label.equals("ENTER")) pkv.themeManager.ripplePaint.setColor(Color.parseColor("#FFFFFF"));
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

            // THEME OVERRIDE FOR ICONS AND TEXT IF KEY IS EDITED
            if (key.label.equals("DEL")) drawEditorIconCenter(canvas, pkv.themeManager.iconBackspace, drawBounds, pkv.resizeController.userHeightScale, isEdited);
            else if (key.label.equals("SHIFT")) {
                if (pkv.isCapsLock) pkv.themeManager.activeShiftIcon = pkv.themeManager.iconCapslock;
                else if (pkv.isShifted) pkv.themeManager.activeShiftIcon = pkv.themeManager.iconShiftFilled; 
                else pkv.themeManager.activeShiftIcon = pkv.themeManager.iconShift; 
                drawEditorIconCenter(canvas, pkv.themeManager.activeShiftIcon, drawBounds, pkv.resizeController.userHeightScale, isEdited);
            } else if (key.label.equals("ENTER")) {
                Drawable actionIcon = pkv.themeManager.iconEnter;
                switch (pkv.currentImeAction) {
                    case EditorInfo.IME_ACTION_SEARCH: actionIcon = pkv.themeManager.iconSearch; break;
                    case EditorInfo.IME_ACTION_SEND: actionIcon = pkv.themeManager.iconSend; break;     
                    case EditorInfo.IME_ACTION_DONE: actionIcon = pkv.themeManager.iconDone; break;     
                    case EditorInfo.IME_ACTION_NEXT: actionIcon = pkv.themeManager.iconNext; break;
                }
                drawEditorIconCenter(canvas, actionIcon, drawBounds, pkv.resizeController.userHeightScale, isEdited);
            } else if (key.label.equals("EMOJI")) drawEditorIconCenter(canvas, pkv.themeManager.iconEmoji, drawBounds, pkv.resizeController.userHeightScale, isEdited);
            else if (key.label.equals("LANG")) drawEditorIconCenter(canvas, pkv.themeManager.iconLanguage, drawBounds, pkv.resizeController.userHeightScale, isEdited);
            else if (key.label.equals("SPACE")) {
                if (pkv.currentMode == ProKeyboardView.MODE_NUMBER) {
                    Paint p2 = new Paint(Paint.ANTI_ALIAS_FLAG);
                    p2.setStyle(Paint.Style.STROKE);
                    p2.setStrokeWidth(pkv.dpToPx(2) * pkv.resizeController.userHeightScale);
                    p2.setColor(isEdited ? Color.parseColor("#1B5E20") : pkv.themeManager.textPaint.getColor());
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
                    pkv.themeManager.textPaint.setColor(isEdited ? Color.parseColor("#1B5E20") : (pkv.themeManager.activeTheme != null ? pkv.themeManager.activeTheme.suggestionTextColor : Color.parseColor("#90A4AE")));
                    canvas.drawText("Hinglish", drawBounds.centerX(), drawBounds.centerY() - ((pkv.themeManager.textPaint.descent() + pkv.themeManager.textPaint.ascent()) / 2), pkv.themeManager.textPaint);
                    pkv.themeManager.textPaint.setColor(origColor);
                }
            } else {
                String displayLabel = pkv.getDisplayLabel(key.label);

                if (pkv.currentMode == ProKeyboardView.MODE_NUMBER && getDialerLetters(key.label) != null) {
                    int origColorNum = pkv.themeManager.textPaint.getColor();
                    if (isEdited) pkv.themeManager.textPaint.setColor(Color.parseColor("#1B5E20"));
                    
                    pkv.themeManager.textPaint.setTextSize(pkv.dpToPx(24) * pkv.resizeController.userHeightScale);
                    canvas.drawText(key.label, drawBounds.centerX(), drawBounds.centerY() - pkv.dpToPx(2), pkv.themeManager.textPaint);
                    pkv.themeManager.textPaint.setTextSize(pkv.dpToPx(10) * pkv.resizeController.userHeightScale);
                    
                    pkv.themeManager.textPaint.setColor(isEdited ? Color.parseColor("#2E7D32") : (pkv.themeManager.activeTheme != null ? pkv.themeManager.activeTheme.suggestionTextColor : Color.parseColor("#78909C")));
                    canvas.drawText(getDialerLetters(key.label), drawBounds.centerX(), drawBounds.centerY() + pkv.dpToPx(13), pkv.themeManager.textPaint);
                    
                    pkv.themeManager.textPaint.setColor(origColorNum);
                } else if (isEmojiKey) {
                    pkv.themeManager.textPaint.setTextSize(pkv.dpToPx(22) * pkv.resizeController.userHeightScale);
                    pkv.themeManager.textPaint.setTypeface(android.graphics.Typeface.DEFAULT);
                    canvas.drawText(key.label, drawBounds.centerX(), drawBounds.centerY() - ((pkv.themeManager.textPaint.descent() + pkv.themeManager.textPaint.ascent()) / 2), pkv.themeManager.textPaint);
                } else if (isSlangKey) {
                    int origColorSlang = pkv.themeManager.textPaint.getColor();
                    if (isEdited) pkv.themeManager.textPaint.setColor(Color.parseColor("#1B5E20"));
                    
                    pkv.themeManager.textPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
                    float maxTextWidth = drawBounds.width() - pkv.dpToPx(4); 
                    float textSize = pkv.dpToPx(12) * pkv.resizeController.userHeightScale;
                    pkv.themeManager.textPaint.setTextSize(textSize);

                    while (pkv.themeManager.textPaint.measureText(displayLabel) > maxTextWidth && textSize > pkv.dpToPx(6)) {
                        textSize -= pkv.dpToPx(0.5f);
                        pkv.themeManager.textPaint.setTextSize(textSize);
                    }
                    canvas.drawText(displayLabel, drawBounds.centerX(), drawBounds.centerY() - ((pkv.themeManager.textPaint.descent() + pkv.themeManager.textPaint.ascent()) / 2), pkv.themeManager.textPaint);

                    if (isEdited) pkv.themeManager.textPaint.setColor(origColorSlang);
                } else {
                    int origColorText = pkv.themeManager.textPaint.getColor();
                    if (isEdited) pkv.themeManager.textPaint.setColor(Color.parseColor("#1B5E20")); 
                    
                    pkv.themeManager.textPaint.setTextSize(pkv.dpToPx(displayLabel.length() > 1 ? 14 : 22) * pkv.resizeController.userHeightScale);

                    if (pkv.themeManager.activeFont != null && !pkv.themeManager.activeFont.name.equals("Default")) {
                        pkv.themeManager.textPaint.setTypeface(pkv.themeManager.activeFont.keyboardTypeface);
                    } else {
                        pkv.themeManager.textPaint.setTypeface(android.graphics.Typeface.DEFAULT);
                    }
                    canvas.drawText(displayLabel, drawBounds.centerX(), drawBounds.centerY() - ((pkv.themeManager.textPaint.descent() + pkv.themeManager.textPaint.ascent()) / 2), pkv.themeManager.textPaint);

                    if (isEdited) pkv.themeManager.textPaint.setColor(origColorText);
                }
            }

            // --- UNIFIED HINT RENDERING FOR EDITOR KEYBOARD ---
            String hintText = null;
            String[] customLp = ckm.getPopups(key.label);
            
            if (customLp != null && customLp.length > 0 && customLp[0] != null && !customLp[0].isEmpty()) {
                hintText = customLp[0]; // Custom Popup overrides defaults
            } else if (pkv.currentMode == ProKeyboardView.MODE_TEXT && pkv.swipeEmojiManager != null && pkv.swipeEmojiManager.swipeEmojis.containsKey(key.label)) {
                hintText = pkv.swipeEmojiManager.swipeEmojis.get(key.label)[0];
            }

            if (hintText != null && !hintText.isEmpty()) {
                Paint hintPaint = new Paint(pkv.themeManager.textPaint);
                hintPaint.setTypeface(android.graphics.Typeface.DEFAULT);
                hintPaint.clearShadowLayer();
                hintPaint.setTextSize(pkv.dpToPx(10) * pkv.resizeController.userHeightScale);
                
                // Highlight Override for Editor Background
                if (isEdited) {
                    hintPaint.setColor(Color.parseColor("#2E7D32")); 
                } else if (pkv.themeManager.activeTheme != null) {
                    hintPaint.setColor(pkv.themeManager.activeTheme.suggestionTextColor);
                } else {
                    hintPaint.setColor(Color.parseColor("#4A90E2"));
                }
                
                canvas.drawText(hintText, drawBounds.right - pkv.dpToPx(8), drawBounds.top + pkv.dpToPx(12), hintPaint);
            }
            
            // NOTE: The single-press tap popup logic has been completely removed 
            // from the EditorKeyboardRenderer so no popups show up on click!
        }

        int effectColor = pkv.themeManager.activeTheme != null ? pkv.themeManager.activeTheme.enterBgColor : Color.parseColor("#4A90E2");
        pkv.touchEffectManager.updateAndDraw(canvas, dt, effectColor);
        needsRedraw = true;

        float targetPanelScale = pkv.toolbarManager.isMoreFeaturesOpen ? 1.0f : 0.0f;
        pkv.toolbarManager.panelScale += (targetPanelScale - pkv.toolbarManager.panelScale) * 15f * dt;
        if (Math.abs(targetPanelScale - pkv.toolbarManager.panelScale) > 0.01f) needsRedraw = true;
        
        if (pkv.toolbarManager.panelScale > 0.01f) {
            canvas.save();
            float maxRadius = (float) Math.hypot(cxMore, pkv.getHeight() - cyMore);
            float currentRadius = maxRadius * pkv.toolbarManager.panelScale;
            
            Path revealPath = new Path();
            revealPath.addCircle(cxMore, cyMore, currentRadius, Path.Direction.CW);
            canvas.clipPath(revealPath);
            
            canvas.drawRect(0, toolbarHeight, pkv.getWidth(), pkv.getHeight(), pkv.themeManager.bgPaint);
            
            float inactiveColWidth = pkv.getWidth() / 4f;
            float inactiveRowHeight = pkv.dpToPx(80);

            pkv.themeManager.textPaint.setTextSize(pkv.dpToPx(12));
            pkv.themeManager.textPaint.clearShadowLayer(); 
            pkv.themeManager.textPaint.setTypeface(android.graphics.Typeface.DEFAULT);
            pkv.themeManager.textPaint.setColor(pkv.themeManager.activeTheme != null ? pkv.themeManager.activeTheme.textColor : Color.BLACK);

            for (ToolbarManager.ToolbarItem item : pkv.toolbarManager.inactiveTools) {
                if (!item.isDragging) {
                    item.currentX += (item.targetX - item.currentX) * 15f * dt;
                    item.currentY += (item.targetY - item.currentY) * 15f * dt;
                    if (Math.abs(item.targetX - item.currentX) > 1f || Math.abs(item.targetY - item.currentY) > 1f) needsRedraw = true;
                }
                
                float targetScale = item.isDragging ? 1.2f : (item.isPressed ? 0.85f : 1.0f);
                item.scale += (targetScale - item.scale) * 20f * dt;
                if (Math.abs(targetScale - item.scale) > 0.01f) needsRedraw = true;

                if (!item.isDragging) {
                    float drawY = toolbarHeight + item.currentY;
                    RectF bounds = new RectF(item.currentX, drawY, item.currentX + inactiveColWidth, drawY + inactiveRowHeight - pkv.dpToPx(24));
                    drawEditorIconCenter(canvas, item.icon, bounds, item.scale, false);
                    canvas.drawText(item.label, bounds.centerX(), bounds.bottom + pkv.dpToPx(16), pkv.themeManager.textPaint);
                }
            }
            canvas.restore();
        }

        if (pkv.toolbarManager.activeDragItem != null && pkv.toolbarManager.activeDragItem.isDragging) {
            Paint dragShadow = new Paint(Paint.ANTI_ALIAS_FLAG);
            dragShadow.setColor(Color.parseColor("#33000000"));
            
            float renderX = pkv.toolbarManager.activeDragItem.currentX;
            float renderY = pkv.toolbarManager.activeDragItem.currentY;
            float sWidth = (pkv.toolbarManager.dragPointerY < toolbarHeight) ? (toolbarAreaWidth / Math.max(1, pkv.toolbarManager.activeTools.size() + 1)) : (pkv.getWidth() / 4f);
            float sHeight = (pkv.toolbarManager.dragPointerY < toolbarHeight) ? toolbarHeight : pkv.dpToPx(80);
            float finalRenderY = (pkv.toolbarManager.dragPointerY < toolbarHeight) ? renderY : (toolbarHeight + renderY);

            RectF dragBounds = new RectF(renderX, finalRenderY, renderX + sWidth, finalRenderY + sHeight);
            
            canvas.drawRoundRect(dragBounds, pkv.dpToPx(8), pkv.dpToPx(8), dragShadow);
            drawEditorIconCenter(canvas, pkv.toolbarManager.activeDragItem.icon, dragBounds, pkv.toolbarManager.activeDragItem.scale, false);

            if (pkv.toolbarManager.dragPointerY >= toolbarHeight) {
                pkv.themeManager.textPaint.setTextSize(pkv.dpToPx(12));
                pkv.themeManager.textPaint.clearShadowLayer();
                pkv.themeManager.textPaint.setTypeface(android.graphics.Typeface.DEFAULT);
                canvas.drawText(pkv.toolbarManager.activeDragItem.label, dragBounds.centerX(), dragBounds.bottom - pkv.dpToPx(8), pkv.themeManager.textPaint);
            }
        }

        if (pkv.resizeController.isResizing) {
            canvas.drawColor(Color.parseColor("#D9000000"));
            float resizeCx = pkv.getWidth() / 2f;
            float resizeCy = pkv.getHeight() / 2f;

            Paint dragPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            dragPaint.setColor(Color.parseColor("#FFFFFF"));
            canvas.drawRoundRect(resizeCx - pkv.dpToPx(30), pkv.dpToPx(10), resizeCx + pkv.dpToPx(30), pkv.dpToPx(15), pkv.dpToPx(2.5f), pkv.dpToPx(2.5f), dragPaint);
            
            Paint doneBg = new Paint(Paint.ANTI_ALIAS_FLAG);
            doneBg.setColor(Color.parseColor("#4A90E2"));
            RectF doneBounds = pkv.resizeController.getDoneButtonBounds(pkv.getWidth(), pkv.getHeight());
            canvas.drawRoundRect(doneBounds, pkv.dpToPx(24), pkv.dpToPx(24), doneBg);
            
            Paint whiteText = new Paint(Paint.ANTI_ALIAS_FLAG);
            whiteText.setColor(Color.WHITE);
            whiteText.setTextAlign(Paint.Align.CENTER);
            whiteText.setTextSize(pkv.dpToPx(16));
            whiteText.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            canvas.drawText("Done", doneBounds.centerX(), doneBounds.centerY() - ((whiteText.descent() + whiteText.ascent()) / 2), whiteText);
            
            Paint hintPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            hintPaint.setColor(Color.parseColor("#B0BEC5"));
            hintPaint.setTextAlign(Paint.Align.CENTER);
            hintPaint.setTextSize(pkv.dpToPx(14));
            canvas.drawText("Drag up or down to resize keyboard", resizeCx, doneBounds.top - pkv.dpToPx(20), hintPaint);
            return;
        }

        // NOTE: pkv.popupRenderer.drawPopupPreview(...) removed from the end of method
        if (needsRedraw) pkv.postInvalidateOnAnimation();
    }

    private void drawEditorIconCenter(Canvas canvas, Drawable icon, RectF bounds, float scale, boolean isEdited) {
        if (icon == null) return;
        int iconSize = (int) (pkv.dpToPx(22) * scale); 
        int cxIcon = (int) bounds.centerX(), cyIcon = (int) bounds.centerY() - pkv.dpToPx(1);
        icon.setBounds(cxIcon - (iconSize / 2), cyIcon - (iconSize / 2), cxIcon + (iconSize / 2), cyIcon + (iconSize / 2));
        
        if (isEdited) {
            icon.setColorFilter(Color.parseColor("#1B5E20"), android.graphics.PorterDuff.Mode.SRC_IN);
        }
        
        icon.draw(canvas);
        
        if (isEdited) {
            icon.clearColorFilter();
        }
    }
}