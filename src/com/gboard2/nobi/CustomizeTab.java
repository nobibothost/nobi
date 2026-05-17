package com.gboard2.nobi;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class CustomizeTab extends FrameLayout {

    private EditorKeyboardView previewKeyboard;
    private CustomKeyManager keyManager;
    
    // UI Elements
    private TextView btnText;
    private TextView btnSym;
    private TextView profileSelectorText;
    
    // Action Bar Elements
    private TextView btnUndo;
    private TextView btnRedo;
    private TextView btnSave;

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

        // --- SMART PROFILE HEADER WITH 3-DOT MENU ---
        LinearLayout headerContainer = new LinearLayout(context);
        headerContainer.setOrientation(LinearLayout.VERTICAL);
        headerContainer.setPadding(dpToPx(context, 24), dpToPx(context, 24), dpToPx(context, 24), dpToPx(context, 8));

        LinearLayout titleRow = new LinearLayout(context);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        
        TextView title = new TextView(context);
        title.setText("Layout Profiles");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f);
        title.setTextColor(Color.parseColor("#111111")); 
        title.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        titleRow.addView(title, titleParams);

        ImageView btnMoreOptions = new ImageView(context);
        int iconResId = context.getResources().getIdentifier("ic_more_vert", "drawable", context.getPackageName());
        if (iconResId != 0) {
            btnMoreOptions.setImageResource(iconResId);
        }
        btnMoreOptions.setPadding(dpToPx(context, 8), dpToPx(context, 8), dpToPx(context, 8), dpToPx(context, 8));
        
        LinearLayout.LayoutParams moreParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        moreParams.setMargins(0, 0, dpToPx(context, 55), 0); 
        btnMoreOptions.setLayoutParams(moreParams);
        
        btnMoreOptions.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomizeTab.this.showMoreOptionsDialog(context);
            }
        });
        titleRow.addView(btnMoreOptions);
        
        headerContainer.addView(titleRow);

        TextView subtitle = new TextView(context);
        subtitle.setText("Tap to edit, long-press to swap keys. Long-press a profile to Rename/Delete.\nColors: Green (Edited), Blue (Swapped), Purple (Both).");
        subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
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
                CustomizeTab.this.showProfileSelectorDialog(context);
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
                CustomizeTab.this.showNewProfileDialog(context);
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
        toggleParams.setMargins(dpToPx(context, 24), dpToPx(context, 16), dpToPx(context, 24), dpToPx(context, 16));
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

        // --- ACTION BAR (UNDO / REDO / SAVE) ---
        LinearLayout actionBar = new LinearLayout(context);
        actionBar.setOrientation(LinearLayout.HORIZONTAL);
        actionBar.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams actParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        actParams.setMargins(dpToPx(context, 24), dpToPx(context, 0), dpToPx(context, 24), dpToPx(context, 24));
        actionBar.setLayoutParams(actParams);

        btnUndo = createActionButton(context, "⟲", "Undo", "#F5F7FA", "#455A64");
        btnRedo = createActionButton(context, "⟳", "Redo", "#F5F7FA", "#455A64");
        btnSave = createActionButton(context, "💾", "Save Layout", "#4CAF50", "#FFFFFF");

        LinearLayout.LayoutParams smallParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        smallParams.setMargins(0, 0, dpToPx(context, 8), 0);
        
        LinearLayout.LayoutParams flexParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        
        actionBar.addView(btnUndo, smallParams);
        actionBar.addView(btnRedo, smallParams);
        
        View spacer = new View(context);
        actionBar.addView(spacer, flexParams);
        
        actionBar.addView(btnSave, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        btnUndo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                keyManager.undo();
                CustomizeTab.this.refreshKeyboardAndUI(context);
            }
        });

        btnRedo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                keyManager.redo();
                CustomizeTab.this.refreshKeyboardAndUI(context);
            }
        });

        btnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                keyManager.commitChanges();
                CustomizeTab.this.updateActionButtonsState();
                Toast.makeText(context, "Layout saved successfully!", Toast.LENGTH_SHORT).show();
            }
        });

        scrollContent.addView(actionBar);
        scrollView.addView(scrollContent);

        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f);
        mainLayout.addView(scrollView, scrollParams);

        // --- EDITOR KEYBOARD INJECTION ---
        try {
            previewKeyboard = new EditorKeyboardView(context);
            if(previewKeyboard.suggestionManager != null) {
                previewKeyboard.suggestionManager.showToolbar = false; 
            }
            
            previewKeyboard.onStateChangedCallback = new Runnable() {
                @Override
                public void run() {
                    CustomizeTab.this.updateActionButtonsState();
                }
            };

            LinearLayout.LayoutParams kbParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            kbParams.bottomMargin = dpToPx(context, 90); 
            mainLayout.addView(previewKeyboard, kbParams);
            
            updateToggleUI(context);
            updateActionButtonsState();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // --- CLICK LISTENERS FOR TOGGLES ---
        btnText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (previewKeyboard != null && previewKeyboard.currentMode != ProKeyboardView.MODE_TEXT) {
                    previewKeyboard.currentMode = ProKeyboardView.MODE_TEXT;
                    // FIXED: Replaced refreshLayout with layoutManager.buildKeys()
                    previewKeyboard.layoutManager.buildKeys();
                    previewKeyboard.postInvalidateOnAnimation();
                    CustomizeTab.this.updateToggleUI(context);
                }
            }
        });

        btnSym.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (previewKeyboard != null && previewKeyboard.currentMode != ProKeyboardView.MODE_SYMBOLS) {
                    previewKeyboard.currentMode = ProKeyboardView.MODE_SYMBOLS;
                    // FIXED: Replaced refreshLayout with layoutManager.buildKeys()
                    previewKeyboard.layoutManager.buildKeys();
                    previewKeyboard.postInvalidateOnAnimation();
                    CustomizeTab.this.updateToggleUI(context);
                }
            }
        });

        addView(mainLayout);
    }

    private TextView createActionButton(Context context, String icon, String text, String bgColor, String textColor) {
        TextView btn = new TextView(context);
        btn.setText(icon + "  " + text);
        btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        btn.setTextColor(Color.parseColor(textColor));
        btn.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        btn.setPadding(dpToPx(context, 16), dpToPx(context, 10), dpToPx(context, 16), dpToPx(context, 10));
        btn.setGravity(Gravity.CENTER);
        
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor(bgColor)); 
        bg.setCornerRadius(dpToPx(context, 100)); 
        if (Build.VERSION.SDK_INT >= 16) btn.setBackground(bg);
        else btn.setBackgroundDrawable(bg);
        
        return btn;
    }

    private void updateActionButtonsState() {
        if (keyManager.canUndo()) {
            btnUndo.setAlpha(1.0f);
            btnUndo.setEnabled(true);
        } else {
            btnUndo.setAlpha(0.4f);
            btnUndo.setEnabled(false);
        }

        if (keyManager.canRedo()) {
            btnRedo.setAlpha(1.0f);
            btnRedo.setEnabled(true);
        } else {
            btnRedo.setAlpha(0.4f);
            btnRedo.setEnabled(false);
        }

        if (keyManager.hasUnsavedChanges()) {
            btnSave.setAlpha(1.0f);
            btnSave.setEnabled(true);
        } else {
            btnSave.setAlpha(0.4f);
            btnSave.setEnabled(false);
        }
    }

    private void refreshKeyboardAndUI(Context context) {
        if (previewKeyboard != null) {
            // FIXED: Replaced refreshLayout with layoutManager.buildKeys()
            previewKeyboard.layoutManager.buildKeys(); 
            previewKeyboard.postInvalidateOnAnimation();
        }
        updateActionButtonsState();
    }

    private void executeProfileSwitch(String profile, Context context, Dialog dialog) {
        keyManager.setActiveProfile(profile);
        profileSelectorText.setText(profile);
        refreshKeyboardAndUI(context);
        
        if (profile.equals("Default")) {
            Toast.makeText(context, "Default Layout selected. Editing is disabled.", Toast.LENGTH_SHORT).show();
        }
        if (dialog != null) dialog.dismiss();
    }

    private void showMoreOptionsDialog(final Context context) {
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
        title.setText("Backup Controls");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
        title.setTextColor(Color.parseColor("#111111"));
        title.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        title.setPadding(0, 0, 0, dpToPx(context, 20));
        rootLayout.addView(title);

        TextView btnExport = new TextView(context);
        btnExport.setText("📤  Export Current Layout"); 
        btnExport.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
        btnExport.setTextColor(Color.parseColor("#2E7D32"));
        btnExport.setPadding(dpToPx(context, 16), dpToPx(context, 14), dpToPx(context, 16), dpToPx(context, 14));
        GradientDrawable expBg = new GradientDrawable();
        expBg.setColor(Color.parseColor("#E8F5E9"));
        expBg.setCornerRadius(dpToPx(context, 8));
        if (Build.VERSION.SDK_INT >= 16) btnExport.setBackground(expBg);
        else btnExport.setBackgroundDrawable(expBg);
        
        btnExport.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                keyManager.commitChanges();
                CustomizeTab.this.exportLayoutsToZip(context);
            }
        });

        TextView btnImport = new TextView(context);
        btnImport.setText("📥  Import File (.zip)");
        btnImport.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
        btnImport.setTextColor(Color.parseColor("#1565C0"));
        btnImport.setPadding(dpToPx(context, 16), dpToPx(context, 14), dpToPx(context, 16), dpToPx(context, 14));
        GradientDrawable impBg = new GradientDrawable();
        impBg.setColor(Color.parseColor("#E3F2FD"));
        impBg.setCornerRadius(dpToPx(context, 8));
        if (Build.VERSION.SDK_INT >= 16) btnImport.setBackground(impBg);
        else btnImport.setBackgroundDrawable(impBg);
        
        btnImport.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                try {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("application/zip");
                    ((android.app.Activity) context).startActivityForResult(intent, 1001);
                } catch (Exception e) {
                    Toast.makeText(context, "No File Manager found!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnParams.bottomMargin = dpToPx(context, 12);
        
        rootLayout.addView(btnExport, btnParams);
        rootLayout.addView(btnImport, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        dialog.setContentView(rootLayout, new ViewGroup.LayoutParams(dpToPx(context, 280), ViewGroup.LayoutParams.WRAP_CONTENT));
        dialog.show();
    }

    private void exportLayoutsToZip(Context context) {
        try {
            String activeProfile = keyManager.getActiveProfile();
            
            File backupDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "GboardPro");
            if (!backupDir.exists()) backupDir.mkdirs();
            
            String fileName = "Gboard2 layout " + activeProfile + ".zip";
            File zipFile = new File(backupDir, fileName);
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
            
            SharedPreferences sourcePrefs = context.getSharedPreferences("KeyCafeConfig", Context.MODE_PRIVATE);
            JSONObject mainObj = new JSONObject();
            
            String profileKey = "custom_keys_" + activeProfile;
            String profileData = sourcePrefs.getString(profileKey, "{}");
            
            try {
                mainObj.put(profileKey, new JSONObject(profileData));
            } catch (Exception e) {
                mainObj.put(profileKey, profileData);
            }
            
            JSONArray profilesArray = new JSONArray();
            profilesArray.put(activeProfile);
            mainObj.put("__profiles_list__", profilesArray);

            ZipEntry entry = new ZipEntry("backup_profiles.json");
            zos.putNextEntry(entry);
            byte[] dataBytes = mainObj.toString(4).getBytes(StandardCharsets.UTF_8);
            zos.write(dataBytes, 0, dataBytes.length);
            zos.closeEntry();
            zos.close();
            
            Toast.makeText(context, "Saved: Documents/GboardPro/" + fileName, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void handleImportUri(Uri uri) {
        try {
            InputStream is = getContext().getContentResolver().openInputStream(uri);
            if (is == null) {
                Toast.makeText(getContext(), "Failed to open selected file.", Toast.LENGTH_SHORT).show();
                return;
            }
            
            ZipInputStream zis = new ZipInputStream(is);
            ZipEntry entry = zis.getNextEntry();
            
            if (entry != null && entry.getName().equals("backup_profiles.json")) {
                StringBuilder sb = new StringBuilder();
                byte[] buffer = new byte[1024];
                int read;
                while ((read = zis.read(buffer)) != -1) {
                    sb.append(new String(buffer, 0, read, StandardCharsets.UTF_8));
                }
                
                JSONObject mainObj = new JSONObject(sb.toString());
                SharedPreferences targetPrefs = getContext().getSharedPreferences("KeyCafeConfig", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = targetPrefs.edit();
                
                Set<String> currentProfiles = new HashSet<>(targetPrefs.getStringSet("saved_profiles", new HashSet<String>()));
                
                Iterator<String> keys = mainObj.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    if (key.equals("__profiles_list__")) {
                        JSONArray arr = mainObj.optJSONArray(key);
                        if (arr != null) {
                            for (int i = 0; i < arr.length(); i++) {
                                currentProfiles.add(arr.getString(i)); 
                            }
                        }
                    } else {
                        Object val = mainObj.get(key);
                        String layoutDataStr = (val instanceof JSONObject || val instanceof JSONArray) ? val.toString() : String.valueOf(val);
                        editor.putString(key, layoutDataStr);
                        
                        if (key.startsWith("custom_keys_")) {
                            String extractedName = key.substring(12);
                            if (!extractedName.isEmpty() && !extractedName.equals("Default")) {
                                currentProfiles.add(extractedName);
                            }
                        }
                    }
                }
                
                editor.putStringSet("saved_profiles", currentProfiles);
                editor.apply();
                zis.closeEntry();
                
                keyManager.loadConfig();
                profileSelectorText.setText(keyManager.getActiveProfile());
                CustomizeTab.this.refreshKeyboardAndUI(getContext());
                Toast.makeText(getContext(), "Layouts imported successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Invalid file format. Please select a valid Gboard backup zip.", Toast.LENGTH_LONG).show();
            }
            zis.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Import error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
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
                        if (keyManager.hasUnsavedChanges()) {
                            AlertDialog.Builder b = new AlertDialog.Builder(context);
                            b.setTitle("Unsaved Changes");
                            b.setMessage("You have unsaved changes in the current layout. If you switch now, they will be discarded.");
                            b.setPositiveButton("Discard & Switch", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int w) {
                                    CustomizeTab.this.executeProfileSwitch(profile, context, dialog);
                                }
                            });
                            b.setNegativeButton("Cancel", null);
                            b.show();
                        } else {
                            CustomizeTab.this.executeProfileSwitch(profile, context, dialog);
                        }
                    } else {
                        dialog.dismiss();
                    }
                }
            });

            item.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (profile.equals("Default")) {
                        Toast.makeText(context, "Default layout cannot be deleted or renamed.", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    CustomizeTab.this.showProfileOptionsDialog(context, profile, dialog);
                    return true;
                }
            });

            listLayout.addView(item);
        }

        listScroll.addView(listLayout);
        rootLayout.addView(listScroll);
        
        dialog.setContentView(rootLayout, new ViewGroup.LayoutParams(dpToPx(context, 300), ViewGroup.LayoutParams.WRAP_CONTENT));
        dialog.show();
    }

    private void showProfileOptionsDialog(final Context context, final String profileName, final Dialog parentDialog) {
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
        title.setText("Profile: " + profileName);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
        title.setTextColor(Color.parseColor("#111111"));
        title.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        title.setPadding(0, 0, 0, dpToPx(context, 24));
        rootLayout.addView(title);

        TextView btnRename = new TextView(context);
        btnRename.setText("✏️  Rename Layout");
        btnRename.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
        btnRename.setTextColor(Color.parseColor("#1565C0"));
        btnRename.setPadding(dpToPx(context, 16), dpToPx(context, 14), dpToPx(context, 16), dpToPx(context, 14));
        
        GradientDrawable renameBg = new GradientDrawable();
        renameBg.setColor(Color.parseColor("#E3F2FD"));
        renameBg.setCornerRadius(dpToPx(context, 8));
        if (Build.VERSION.SDK_INT >= 16) btnRename.setBackground(renameBg);
        else btnRename.setBackgroundDrawable(renameBg);
        
        btnRename.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                CustomizeTab.this.showRenameProfileDialog(context, profileName, parentDialog);
            }
        });

        TextView btnDelete = new TextView(context);
        btnDelete.setText("🗑️  Delete Layout");
        btnDelete.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
        btnDelete.setTextColor(Color.parseColor("#C62828"));
        btnDelete.setPadding(dpToPx(context, 16), dpToPx(context, 14), dpToPx(context, 16), dpToPx(context, 14));
        
        GradientDrawable deleteBg = new GradientDrawable();
        deleteBg.setColor(Color.parseColor("#FFEBEE"));
        deleteBg.setCornerRadius(dpToPx(context, 8));
        if (Build.VERSION.SDK_INT >= 16) btnDelete.setBackground(deleteBg);
        else btnDelete.setBackgroundDrawable(deleteBg);

        btnDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                    .setTitle("Delete Layout")
                    .setMessage("Are you sure you want to permanently delete '" + profileName + "'?")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface d, int w) {
                            keyManager.deleteProfile(profileName);
                            profileSelectorText.setText(keyManager.getActiveProfile());
                            CustomizeTab.this.refreshKeyboardAndUI(context);
                            dialog.dismiss();
                            parentDialog.dismiss();
                            Toast.makeText(context, "Profile deleted", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            }
        });

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnParams.bottomMargin = dpToPx(context, 12);
        
        rootLayout.addView(btnRename, btnParams);
        rootLayout.addView(btnDelete, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        dialog.setContentView(rootLayout, new ViewGroup.LayoutParams(dpToPx(context, 300), ViewGroup.LayoutParams.WRAP_CONTENT));
        dialog.show();
    }

    private void showRenameProfileDialog(final Context context, final String oldName, final Dialog parentDialog) {
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
        title.setText("Rename Layout");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
        title.setTextColor(Color.parseColor("#111111"));
        title.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        title.setPadding(0, 0, 0, dpToPx(context, 16));
        rootLayout.addView(title);

        final EditText input = new EditText(context);
        input.setText(oldName);
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

        TextView btnSave = new TextView(context);
        btnSave.setText("Save");
        btnSave.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        btnSave.setTextColor(Color.WHITE);
        btnSave.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        btnSave.setPadding(dpToPx(context, 24), dpToPx(context, 10), dpToPx(context, 24), dpToPx(context, 10));
        
        GradientDrawable saveBg = new GradientDrawable();
        saveBg.setColor(Color.parseColor("#4A90E2"));
        saveBg.setCornerRadius(dpToPx(context, 8));
        if (Build.VERSION.SDK_INT >= 16) btnSave.setBackground(saveBg);
        else btnSave.setBackgroundDrawable(saveBg);

        btnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = input.getText().toString().trim();
                if (!newName.isEmpty() && keyManager.renameProfile(oldName, newName)) {
                    profileSelectorText.setText(keyManager.getActiveProfile());
                    dialog.dismiss();
                    parentDialog.dismiss();
                    Toast.makeText(context, "Profile renamed to '" + newName + "'!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Invalid or duplicate name.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnRow.addView(btnCancel);
        btnRow.addView(btnSave);
        rootLayout.addView(btnRow, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        dialog.setContentView(rootLayout, new ViewGroup.LayoutParams(dpToPx(context, 320), ViewGroup.LayoutParams.WRAP_CONTENT));
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
                    CustomizeTab.this.refreshKeyboardAndUI(context);
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