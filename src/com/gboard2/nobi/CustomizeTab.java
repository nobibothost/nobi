package com.gboard2.nobi;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class CustomizeTab extends FrameLayout {

    private CustomKeyManager keyManager;
    private ProKeyboardView previewKeyboard;

    public CustomizeTab(Context context) {
        super(context);
        keyManager = CustomKeyManager.getInstance(context);

        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(Color.parseColor("#F5F5F5"));
        mainLayout.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        ScrollView scrollView = new ScrollView(context);
        LinearLayout scrollContent = new LinearLayout(context);
        scrollContent.setOrientation(LinearLayout.VERTICAL);
        
        TextView title = new TextView(context);
        title.setText("KeyCafe - Visual Mapping");
        title.setTextSize(22);
        title.setTextColor(Color.BLACK);
        title.setPadding(40, 60, 40, 10);
        title.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        
        TextView subtitle = new TextView(context);
        subtitle.setText("Tap any key below to edit. You can even design custom long-press popups visually!");
        subtitle.setTextSize(14);
        subtitle.setTextColor(Color.DKGRAY);
        subtitle.setPadding(40, 0, 40, 30);

        LinearLayout toggleRow = new LinearLayout(context);
        toggleRow.setOrientation(LinearLayout.HORIZONTAL);
        toggleRow.setGravity(Gravity.CENTER);
        toggleRow.setPadding(20, 0, 20, 20);

        final Button btnText = new Button(context);
        btnText.setText("Text Layout");
        btnText.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));

        final Button btnSym = new Button(context);
        btnSym.setText("Symbols Layout");
        btnSym.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));

        btnText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (previewKeyboard != null) {
                    previewKeyboard.currentMode = ProKeyboardView.MODE_TEXT;
                    previewKeyboard.layoutManager.buildKeys();
                    previewKeyboard.postInvalidateOnAnimation();
                }
            }
        });

        btnSym.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (previewKeyboard != null) {
                    previewKeyboard.currentMode = ProKeyboardView.MODE_SYMBOLS;
                    previewKeyboard.layoutManager.buildKeys();
                    previewKeyboard.postInvalidateOnAnimation();
                }
            }
        });

        toggleRow.addView(btnText);
        toggleRow.addView(btnSym);

        scrollContent.addView(title);
        scrollContent.addView(subtitle);
        scrollContent.addView(toggleRow);
        scrollView.addView(scrollContent);

        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f);
        mainLayout.addView(scrollView, scrollParams);

        previewKeyboard = new ProKeyboardView(context);
        
        if(previewKeyboard.suggestionManager != null) {
            previewKeyboard.suggestionManager.showToolbar = false; 
        }

        previewKeyboard.setKeyboardListener(new ProKeyboardView.KeyboardListener() {
            @Override
            public void onKeyClick(String rawKey) {
                showEditDialog(getCleanKey(rawKey));
            }
        });

        LinearLayout.LayoutParams kbParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        kbParams.bottomMargin = dpToPx(90); 
        mainLayout.addView(previewKeyboard, kbParams);

        addView(mainLayout);
    }

    private int dpToPx(float dp) {
        return (int) (dp * getContext().getResources().getDisplayMetrics().density + 0.5f);
    }

    private String getCleanKey(String rawKey) {
        if (rawKey == null) return "";
        if (rawKey.startsWith("EMOJI_INPUT:")) {
            return rawKey.substring(12);
        }
        return rawKey;
    }

    private void showEditDialog(final String originalKey) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Remap Key: " + originalKey);

        ScrollView scroll = new ScrollView(getContext());
        LinearLayout dialogLayout = new LinearLayout(getContext());
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(50, 40, 50, 20);

        final EditText inputLabel = new EditText(getContext());
        inputLabel.setHint("Display Character (e.g. ★)");
        String currentLabel = keyManager.getLabel(originalKey);
        if (currentLabel != null) inputLabel.setText(currentLabel);
        dialogLayout.addView(inputLabel);

        final EditText inputValue = new EditText(getContext());
        inputValue.setHint("Click Output (e.g. Hello)");
        String currentValue = keyManager.getValue(originalKey);
        if (currentValue != null) inputValue.setText(currentValue);
        dialogLayout.addView(inputValue);

        TextView popupHint = new TextView(getContext());
        popupHint.setText("Long-Press Directional Popup:");
        popupHint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        popupHint.setPadding(0, 40, 0, 10);
        dialogLayout.addView(popupHint);

        String[] currentPopups = keyManager.getPopups(originalKey);
        String[] defaultPopups = null;
        if (previewKeyboard != null && previewKeyboard.swipeSymbolManager != null) {
            defaultPopups = previewKeyboard.swipeSymbolManager.swipeSymbols.get(originalKey);
        }

        final String[] newPopups = new String[5];
        
        // BUG FIX: Strictly use currentPopups if they exist, to allow empty slots to remain empty.
        if (currentPopups != null && currentPopups.length == 5) {
            System.arraycopy(currentPopups, 0, newPopups, 0, 5);
        } else if (defaultPopups != null && defaultPopups.length == 5) {
            System.arraycopy(defaultPopups, 0, newPopups, 0, 5);
        } else {
            newPopups[0] = ""; newPopups[1] = ""; newPopups[2] = ""; newPopups[3] = ""; newPopups[4] = "";
        }

        LinearLayout crossContainer = new LinearLayout(getContext());
        crossContainer.setOrientation(LinearLayout.VERTICAL);
        crossContainer.setGravity(Gravity.CENTER);

        int btnSize = dpToPx(55);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(btnSize, btnSize);
        btnParams.setMargins(10, 10, 10, 10);

        final String defaultDisplay = (currentLabel != null && !currentLabel.isEmpty()) ? currentLabel : originalKey;

        View.OnClickListener slotListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int slot = (int) v.getTag();
                showSlotEditDialog((Button) v, newPopups, slot, defaultDisplay);
            }
        };

        Button btnUp = createSlotButton(newPopups[1], defaultDisplay, 1, btnParams, slotListener, false);
        Button btnLeft = createSlotButton(newPopups[3], defaultDisplay, 3, btnParams, slotListener, false);
        Button btnCenter = createSlotButton(newPopups[0], defaultDisplay, 0, btnParams, slotListener, true);
        Button btnRight = createSlotButton(newPopups[4], defaultDisplay, 4, btnParams, slotListener, false);
        Button btnDown = createSlotButton(newPopups[2], defaultDisplay, 2, btnParams, slotListener, false);

        LinearLayout row1 = new LinearLayout(getContext());
        row1.setGravity(Gravity.CENTER);
        row1.addView(btnUp);

        LinearLayout row2 = new LinearLayout(getContext());
        row2.setGravity(Gravity.CENTER);
        row2.addView(btnLeft);
        row2.addView(btnCenter);
        row2.addView(btnRight);

        LinearLayout row3 = new LinearLayout(getContext());
        row3.setGravity(Gravity.CENTER);
        row3.addView(btnDown);

        crossContainer.addView(row1);
        crossContainer.addView(row2);
        crossContainer.addView(row3);
        
        dialogLayout.addView(crossContainer);
        scroll.addView(dialogLayout);
        builder.setView(scroll);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                keyManager.setCustomMapping(originalKey, inputLabel.getText().toString(), inputValue.getText().toString(), newPopups);
                if (previewKeyboard != null) {
                    previewKeyboard.layoutManager.buildKeys();
                    previewKeyboard.postInvalidateOnAnimation();
                }
            }
        });

        builder.setNeutralButton("Reset Default", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                keyManager.resetKey(originalKey);
                if (previewKeyboard != null) {
                    previewKeyboard.layoutManager.buildKeys();
                    previewKeyboard.postInvalidateOnAnimation();
                }
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private Button createSlotButton(String currentText, String fallbackText, int index, LinearLayout.LayoutParams params, View.OnClickListener listener, boolean isCenter) {
        Button b = new Button(getContext());
        b.setLayoutParams(params);
        
        String displayText = (currentText == null || currentText.isEmpty()) ? fallbackText : currentText;
        b.setText(displayText);
        
        if (displayText.length() > 3) {
            b.setTextSize(10);
        } else {
            b.setTextSize(18);
        }
        
        b.setMaxLines(1);
        b.setEllipsize(TextUtils.TruncateAt.END);
        
        b.setTag(index);
        b.setOnClickListener(listener);
        b.setPadding(0, 0, 0, 0);

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dpToPx(8));
        if (isCenter) {
            bg.setColor(Color.parseColor("#4A90E2"));
            b.setTextColor(Color.WHITE);
        } else {
            bg.setColor(Color.parseColor("#E0E0E0"));
            b.setTextColor(Color.BLACK);
        }
        b.setBackground(bg);
        return b;
    }

    private void showSlotEditDialog(final Button btn, final String[] popups, final int index, final String fallbackText) {
        AlertDialog.Builder b = new AlertDialog.Builder(getContext());
        String dirName = index == 0 ? "Center" : (index == 1 ? "Up" : (index == 2 ? "Down" : (index == 3 ? "Left" : "Right")));
        b.setTitle("Set " + dirName + " Symbol");

        final EditText et = new EditText(getContext());
        et.setText(popups[index]);
        et.setHint("Enter single symbol/text");
        b.setView(et);

        b.setPositiveButton("Set", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface d, int w) {
                popups[index] = et.getText().toString();
                String newDisplay = popups[index].isEmpty() ? fallbackText : popups[index];
                btn.setText(newDisplay);
                if (newDisplay.length() > 3) btn.setTextSize(10);
                else btn.setTextSize(18);
            }
        });
        b.setNegativeButton("Clear", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface d, int w) {
                popups[index] = "";
                btn.setText(fallbackText);
                if (fallbackText.length() > 3) btn.setTextSize(10);
                else btn.setTextSize(18);
            }
        });
        b.show();
    }
}
