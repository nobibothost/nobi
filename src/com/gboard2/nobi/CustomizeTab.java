package com.gboard2.nobi;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class CustomizeTab extends FrameLayout {

    private EditorKeyboardView previewKeyboard;
    private CustomKeyManager keyManager;
    
    // UI Elements
    private TextView btnText;
    private TextView btnSym;
    private TextView profileSelectorText;

    public CustomizeTab(final Context context) {
        super(context);
        keyManager = CustomKeyManager.getInstance(context);

        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(Color.parseColor(UIHelpers.COLOR_BG));
        mainLayout.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        ScrollView scrollView = new ScrollView(context);
        LinearLayout scrollContent = new LinearLayout(context);
        scrollContent.setOrientation(LinearLayout.VERTICAL);

        // --- TAB HEADER ---
        LinearLayout headerContainer = new LinearLayout(context);
        headerContainer.setOrientation(LinearLayout.VERTICAL);
        headerContainer.setPadding(dpToPx(context, 24), dpToPx(context, 24), dpToPx(context, 24), dpToPx(context, 8));

        TextView title = new TextView(context);
        title.setText("Layout Profiles");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28f); // Matched to 28sp uniform
        title.setTextColor(Color.parseColor("#111111")); 
        title.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        headerContainer.addView(title);

        TextView subtitle = new TextView(context);
        subtitle.setText("Create layouts for gaming, coding, etc. Default layout cannot be edited.");
        subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        subtitle.setTextColor(Color.parseColor("#78909C")); 
        subtitle.setPadding(0, dpToPx(context, 4), 0, dpToPx(context, 16));
        headerContainer.addView(subtitle);

        // --- SMART DROPDOWN & NEW BUTTON ROW ---
        LinearLayout profileActionsRow = new LinearLayout(context);
        profileActionsRow.setOrientation(LinearLayout.HORIZONTAL);
        profileActionsRow.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout selectorBtn = new LinearLayout(context);
        selectorBtn.setOrientation(LinearLayout.HORIZONTAL);
        selectorBtn.setGravity(Gravity.CENTER_VERTICAL);
        
        GradientDrawable selectorBg = new GradientDrawable();
        selectorBg.setColor(Color.WHITE);
        selectorBg.setCornerRadius(dpToPx(context, 12));
        selectorBg.setStroke(dpToPx(context, 1), Color.parseColor("#E0E0E0"));
        if (Build.VERSION.SDK_INT >= 16) selectorBtn.setBackground(selectorBg);
        else selectorBtn.setBackgroundDrawable(selectorBg);
        
        selectorBtn.setPadding(dpToPx(context, 16), dpToPx(context, 12), dpToPx(context, 16), dpToPx(context, 12));
        LinearLayout.LayoutParams selParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        selParams.setMargins(0, 0, dpToPx(context, 12), 0);
        selectorBtn.setLayoutParams(selParams);

        profileSelectorText = new TextView(context);
        profileSelectorText.setText(keyManager.getActiveProfile());
        profileSelectorText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
        profileSelectorText.setTextColor(Color.parseColor("#212121"));
        profileSelectorText.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        profileSelectorText.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        
        TextView dropdownArrow = new TextView(context);
        dropdownArrow.setText("▼");
        dropdownArrow.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f);
        dropdownArrow.setTextColor(Color.parseColor("#9E9E9E"));
        
        selectorBtn.addView(profileSelectorText);
        selectorBtn.addView(dropdownArrow);
        
        selectorBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showProfileSelectorDialog(context);
            }
        });

        TextView newBtn = new TextView(context);
        newBtn.setText("+ New");
        newBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        newBtn.setTextColor(Color.WHITE);
        newBtn.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        newBtn.setPadding(dpToPx(context, 20), dpToPx(context, 12), dpToPx(context, 20), dpToPx(context, 12));
        newBtn.setGravity(Gravity.CENTER);
        
        GradientDrawable newBg = new GradientDrawable();
        newBg.setColor(Color.parseColor("#4A90E2"));
        newBg.setCornerRadius(dpToPx(context, 12));
        if (Build.VERSION.SDK_INT >= 16) newBtn.setBackground(newBg);
        else newBtn.setBackgroundDrawable(newBg);

        newBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showNewProfileDialog(context);
            }
        });

        profileActionsRow.addView(selectorBtn);
        profileActionsRow.addView(newBtn);
        headerContainer.addView(profileActionsRow);
        
        scrollContent.addView(headerContainer);

        // --- STYLISH SEGMENTED CONTROL (TOGGLE ROW) ---
        LinearLayout toggleContainer = new LinearLayout(context);
        toggleContainer.setOrientation(LinearLayout.HORIZONTAL);
        
        GradientDrawable toggleBg = new GradientDrawable();
        toggleBg.setColor(Color.parseColor("#E4E7EB"));
        toggleBg.setCornerRadius(dpToPx(context, 100)); 
        if (Build.VERSION.SDK_INT >= 16) toggleContainer.setBackground(toggleBg);
        else toggleContainer.setBackgroundDrawable(toggleBg);
        toggleContainer.setPadding(dpToPx(context, 4), dpToPx(context, 4), dpToPx(context, 4), dpToPx(context, 4));

        LinearLayout.LayoutParams toggleParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        toggleParams.setMargins(dpToPx(context, 24), dpToPx(context, 16), dpToPx(context, 24), dpToPx(context, 24));
        toggleContainer.setLayoutParams(toggleParams);

        btnText = new TextView(context);
        btnText.setText("Alphabets");
        btnText.setGravity(Gravity.CENTER);
        btnText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        btnText.setPadding(0, dpToPx(context, 12), 0, dpToPx(context, 12));
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        btnText.setLayoutParams(btnParams);

        btnSym = new TextView(context);
        btnSym.setText("Symbols");
        btnSym.setGravity(Gravity.CENTER);
        btnSym.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        btnSym.setPadding(0, dpToPx(context, 12), 0, dpToPx(context, 12));
        btnSym.setLayoutParams(btnParams);

        toggleContainer.addView(btnText);
        toggleContainer.addView(btnSym);
        scrollContent.addView(toggleContainer);

        scrollView.addView(scrollContent);

        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f);
        mainLayout.addView(scrollView, scrollParams);

        // --- EDITOR KEYBOARD INJECTION ---
        try {
            previewKeyboard = new EditorKeyboardView(context);
            if(previewKeyboard.suggestionManager != null) {
                previewKeyboard.suggestionManager.showToolbar = false; 
            }
            LinearLayout.LayoutParams kbParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            kbParams.bottomMargin = dpToPx(context, 90); 
            mainLayout.addView(previewKeyboard, kbParams);
            
            updateToggleUI(context);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // --- CLICK LISTENERS FOR TOGGLES ---
        btnText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (previewKeyboard != null && previewKeyboard.currentMode != ProKeyboardView.MODE_TEXT) {
                    previewKeyboard.currentMode = ProKeyboardView.MODE_TEXT;
                    previewKeyboard.layoutManager.buildKeys();
                    previewKeyboard.postInvalidateOnAnimation();
                    updateToggleUI(context);
                }
            }
        });

        btnSym.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (previewKeyboard != null && previewKeyboard.currentMode != ProKeyboardView.MODE_SYMBOLS) {
                    previewKeyboard.currentMode = ProKeyboardView.MODE_SYMBOLS;
                    previewKeyboard.layoutManager.buildKeys();
                    previewKeyboard.postInvalidateOnAnimation();
                    updateToggleUI(context);
                }
            }
        });

        addView(mainLayout);
    }

    private void showProfileSelectorDialog(final Context context) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        LinearLayout rootLayout = new LinearLayout(context);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setPadding(dpToPx(context, 20), dpToPx(context, 24), dpToPx(context, 20), dpToPx(context, 24));
        
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(dpToPx(context, 16));
        if (Build.VERSION.SDK_INT >= 16) rootLayout.setBackground(bg);
        else rootLayout.setBackgroundDrawable(bg);

        TextView title = new TextView(context);
        title.setText("Select Layout Profile");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
        title.setTextColor(Color.parseColor("#111111"));
        title.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        title.setPadding(0, 0, 0, dpToPx(context, 16));
        rootLayout.addView(title);

        ScrollView listScroll = new ScrollView(context);
        LinearLayout listLayout = new LinearLayout(context);
        listLayout.setOrientation(LinearLayout.VERTICAL);

        List<String> profiles = keyManager.getSavedProfiles();
        String activeProfile = keyManager.getActiveProfile();

        for (final String profile : profiles) {
            final TextView item = new TextView(context);
            item.setText(profile);
            item.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
            item.setPadding(dpToPx(context, 16), dpToPx(context, 16), dpToPx(context, 16), dpToPx(context, 16));
            
            if (profile.equals(activeProfile)) {
                item.setTextColor(Color.parseColor("#4A90E2"));
                item.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                
                GradientDrawable activeBg = new GradientDrawable();
                activeBg.setColor(Color.parseColor("#F0F8FF")); 
                activeBg.setCornerRadius(dpToPx(context, 8));
                if (Build.VERSION.SDK_INT >= 16) item.setBackground(activeBg);
                else item.setBackgroundDrawable(activeBg);
            } else {
                item.setTextColor(Color.parseColor("#424242"));
            }

            item.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!profile.equals(keyManager.getActiveProfile())) {
                        keyManager.setActiveProfile(profile);
                        profileSelectorText.setText(profile);
                        if (previewKeyboard != null) {
                            previewKeyboard.layoutManager.buildKeys();
                            previewKeyboard.postInvalidateOnAnimation();
                        }
                        if (profile.equals("Default")) {
                            Toast.makeText(context, "Default Layout selected. Editing is disabled.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    dialog.dismiss();
                }
            });
            listLayout.addView(item);
        }

        listScroll.addView(listLayout);
        rootLayout.addView(listScroll);
        
        dialog.setContentView(rootLayout, new ViewGroup.LayoutParams(dpToPx(context, 300), ViewGroup.LayoutParams.WRAP_CONTENT));
        dialog.show();
    }

    private void showNewProfileDialog(final Context context) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        LinearLayout rootLayout = new LinearLayout(context);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setPadding(dpToPx(context, 24), dpToPx(context, 24), dpToPx(context, 24), dpToPx(context, 24));
        
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(dpToPx(context, 16));
        if (Build.VERSION.SDK_INT >= 16) rootLayout.setBackground(bg);
        else rootLayout.setBackgroundDrawable(bg);

        TextView title = new TextView(context);
        title.setText("New Layout Profile");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
        title.setTextColor(Color.parseColor("#111111"));
        title.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        title.setPadding(0, 0, 0, dpToPx(context, 16));
        rootLayout.addView(title);

        final EditText input = new EditText(context);
        input.setHint("e.g. Coding Layout, Gaming");
        input.setHintTextColor(Color.parseColor("#B0BEC5"));
        input.setTextColor(Color.parseColor("#212121"));
        input.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
        input.setPadding(dpToPx(context, 16), dpToPx(context, 12), dpToPx(context, 16), dpToPx(context, 12));
        
        GradientDrawable inputBg = new GradientDrawable();
        inputBg.setColor(Color.parseColor("#F5F7FA"));
        inputBg.setCornerRadius(dpToPx(context, 8));
        if (Build.VERSION.SDK_INT >= 16) input.setBackground(inputBg);
        else input.setBackgroundDrawable(inputBg);
        
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        inputParams.setMargins(0, 0, 0, dpToPx(context, 24));
        rootLayout.addView(input, inputParams);

        LinearLayout btnRow = new LinearLayout(context);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(Gravity.RIGHT);

        TextView btnCancel = new TextView(context);
        btnCancel.setText("Cancel");
        btnCancel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        btnCancel.setTextColor(Color.parseColor("#757575"));
        btnCancel.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        btnCancel.setPadding(dpToPx(context, 16), dpToPx(context, 10), dpToPx(context, 16), dpToPx(context, 10));
        
        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        TextView btnCreate = new TextView(context);
        btnCreate.setText("Create");
        btnCreate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        btnCreate.setTextColor(Color.WHITE);
        btnCreate.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        btnCreate.setPadding(dpToPx(context, 24), dpToPx(context, 10), dpToPx(context, 24), dpToPx(context, 10));
        
        GradientDrawable createBg = new GradientDrawable();
        createBg.setColor(Color.parseColor("#4A90E2"));
        createBg.setCornerRadius(dpToPx(context, 8));
        if (Build.VERSION.SDK_INT >= 16) btnCreate.setBackground(createBg);
        else btnCreate.setBackgroundDrawable(createBg);

        btnCreate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = input.getText().toString().trim();
                if (!name.isEmpty() && !name.equalsIgnoreCase("Default")) {
                    keyManager.createNewProfile(name);
                    profileSelectorText.setText(name); 
                    if (previewKeyboard != null) {
                        previewKeyboard.layoutManager.buildKeys();
                        previewKeyboard.postInvalidateOnAnimation();
                    }
                    Toast.makeText(context, "Profile '" + name + "' activated!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(context, "Please enter a valid unique name.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnRow.addView(btnCancel);
        btnRow.addView(btnCreate);
        rootLayout.addView(btnRow, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        dialog.setContentView(rootLayout, new ViewGroup.LayoutParams(dpToPx(context, 320), ViewGroup.LayoutParams.WRAP_CONTENT));
        dialog.show();
    }

    private int dpToPx(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    private void updateToggleUI(Context context) {
        boolean isTextActive = (previewKeyboard != null && previewKeyboard.currentMode == ProKeyboardView.MODE_TEXT);

        GradientDrawable activeBg = new GradientDrawable();
        activeBg.setColor(Color.WHITE);
        activeBg.setCornerRadius(dpToPx(context, 100)); 

        if (isTextActive) {
            if (Build.VERSION.SDK_INT >= 16) btnText.setBackground(activeBg);
            else btnText.setBackgroundDrawable(activeBg);
            btnText.setTextColor(Color.parseColor("#111111"));
            btnText.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            
            if (Build.VERSION.SDK_INT >= 16) btnSym.setBackground(null);
            else btnSym.setBackgroundDrawable(null);
            btnSym.setTextColor(Color.parseColor("#78909C"));
            btnSym.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        } else {
            if (Build.VERSION.SDK_INT >= 16) btnSym.setBackground(activeBg);
            else btnSym.setBackgroundDrawable(activeBg);
            btnSym.setTextColor(Color.parseColor("#111111"));
            btnSym.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            
            if (Build.VERSION.SDK_INT >= 16) btnText.setBackground(null);
            else btnText.setBackgroundDrawable(null);
            btnText.setTextColor(Color.parseColor("#78909C"));
            btnText.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        }
    }
}