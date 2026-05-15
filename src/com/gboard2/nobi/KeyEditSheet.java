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
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class KeyEditSheet {
    private Dialog dialog;
    private CustomKeyManager manager;
    private Context context;
    private KeyData currentKey;
    private Runnable onSaveCallback;

    public KeyEditSheet(Context context, KeyData key, Runnable onSave) {
        this.context = context;
        this.currentKey = key;
        this.onSaveCallback = onSave;
        this.manager = CustomKeyManager.getInstance(context);
        
        this.dialog = new Dialog(context);
        this.dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        buildUI();
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    private void buildUI() {
        // Main Container with Top Rounded Corners
        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(dpToPx(24), dpToPx(12), dpToPx(24), dpToPx(32));
        
        GradientDrawable sheetBg = new GradientDrawable();
        sheetBg.setColor(Color.WHITE); // Solid white background
        float cornerRadius = dpToPx(24);
        sheetBg.setCornerRadii(new float[]{cornerRadius, cornerRadius, cornerRadius, cornerRadius, 0, 0, 0, 0});
        if (Build.VERSION.SDK_INT >= 16) {
            mainLayout.setBackground(sheetBg);
        } else {
            mainLayout.setBackgroundDrawable(sheetBg);
        }

        // Top Drag Handle (Pill)
        View dragPill = new View(context);
        GradientDrawable pillBg = new GradientDrawable();
        pillBg.setColor(Color.parseColor("#CFD8DC"));
        pillBg.setCornerRadius(dpToPx(4));
        if (Build.VERSION.SDK_INT >= 16) dragPill.setBackground(pillBg);
        else dragPill.setBackgroundDrawable(pillBg);
        
        LinearLayout.LayoutParams pillParams = new LinearLayout.LayoutParams(dpToPx(40), dpToPx(5));
        pillParams.gravity = Gravity.CENTER_HORIZONTAL;
        pillParams.bottomMargin = dpToPx(20);
        mainLayout.addView(dragPill, pillParams);

        // Title View
        TextView titleView = new TextView(context);
        titleView.setText("Customize Key");
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f);
        titleView.setTextColor(Color.parseColor("#000000")); // Deep Black
        titleView.setTypeface(null, Typeface.BOLD);
        titleView.setGravity(Gravity.CENTER_HORIZONTAL);
        mainLayout.addView(titleView);

        // Subtitle View
        TextView subTitleView = new TextView(context);
        subTitleView.setText("Editing the layout for: " + currentKey.label);
        subTitleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        subTitleView.setTextColor(Color.parseColor("#546E7A")); // Darker Grey-Blue
        subTitleView.setGravity(Gravity.CENTER_HORIZONTAL);
        subTitleView.setPadding(0, 0, 0, dpToPx(24));
        mainLayout.addView(subTitleView);

        // Reusable Input Background (Cool Gray for Contrast)
        GradientDrawable inputBg = new GradientDrawable();
        inputBg.setColor(Color.parseColor("#F0F4F8")); 
        inputBg.setCornerRadius(dpToPx(12));
        inputBg.setStroke(dpToPx(1), Color.parseColor("#CFD8DC")); // Visible border

        // Label Input Section
        TextView labelHeading = new TextView(context);
        labelHeading.setText("DISPLAY LABEL");
        labelHeading.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
        labelHeading.setTextColor(Color.parseColor("#455A64")); // Strong Grey
        labelHeading.setTypeface(null, Typeface.BOLD);
        labelHeading.setPadding(dpToPx(4), 0, 0, dpToPx(6));
        mainLayout.addView(labelHeading);

        final EditText labelInput = new EditText(context);
        labelInput.setHint("Default character: " + currentKey.label);
        labelInput.setTextColor(Color.parseColor("#111111")); // Almost black for text
        labelInput.setHintTextColor(Color.parseColor("#78909C")); // Visible hint color
        labelInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
        labelInput.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
        if (Build.VERSION.SDK_INT >= 16) labelInput.setBackground(inputBg);
        else labelInput.setBackgroundDrawable(inputBg);
        
        String existingLabel = manager.getLabel(currentKey.code);
        labelInput.setText(existingLabel != null && !existingLabel.trim().isEmpty() ? existingLabel : "");
        
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        inputParams.bottomMargin = dpToPx(20);
        mainLayout.addView(labelInput, inputParams);
        
        // Action Spinner Section
        TextView actionHeading = new TextView(context);
        actionHeading.setText("KEY ACTION");
        actionHeading.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
        actionHeading.setTextColor(Color.parseColor("#455A64"));
        actionHeading.setTypeface(null, Typeface.BOLD);
        actionHeading.setPadding(dpToPx(4), 0, 0, dpToPx(6));
        mainLayout.addView(actionHeading);

        LinearLayout spinnerContainer = new LinearLayout(context);
        if (Build.VERSION.SDK_INT >= 16) spinnerContainer.setBackground(inputBg);
        else spinnerContainer.setBackgroundDrawable(inputBg);
        spinnerContainer.setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4));
        
        final Spinner actionSpinner = new Spinner(context);
        final String[] actions = {"DEFAULT", "WRITE", "DELETE", "SPACE", "BACKSPACE", "ENTER", "SELECT_ALL", "CUT", "COPY", "PASTE", "SEARCH"};
        
        // CRITICAL FIX: Custom ArrayAdapter to forcefully set text color and background to fix Dark Mode invisibility
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, actions) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(Color.parseColor("#111111")); // Visible text
                    ((TextView) view).setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
                }
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(Color.parseColor("#111111"));
                    ((TextView) view).setBackgroundColor(Color.WHITE); // Force white background in list
                    ((TextView) view).setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
                }
                return view;
            }
        };
        actionSpinner.setAdapter(adapter);
        spinnerContainer.addView(actionSpinner, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        
        LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        spinnerParams.bottomMargin = dpToPx(20);
        mainLayout.addView(spinnerContainer, spinnerParams);

        // Value Input Section
        final LinearLayout valueContainer = new LinearLayout(context);
        valueContainer.setOrientation(LinearLayout.VERTICAL);
        valueContainer.setVisibility(View.GONE);

        TextView valueHeading = new TextView(context);
        valueHeading.setText("CUSTOM TEXT TO WRITE");
        valueHeading.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
        valueHeading.setTextColor(Color.parseColor("#455A64"));
        valueHeading.setTypeface(null, Typeface.BOLD);
        valueHeading.setPadding(dpToPx(4), 0, 0, dpToPx(6));
        valueContainer.addView(valueHeading);

        final EditText valueInput = new EditText(context);
        valueInput.setHint("e.g. Hello World!");
        valueInput.setTextColor(Color.parseColor("#111111"));
        valueInput.setHintTextColor(Color.parseColor("#78909C"));
        valueInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
        valueInput.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
        if (Build.VERSION.SDK_INT >= 16) valueInput.setBackground(inputBg);
        else valueInput.setBackgroundDrawable(inputBg);
        
        String existingValue = manager.getValue(currentKey.code);
        valueInput.setText(existingValue != null ? existingValue : "");
        valueContainer.addView(valueInput, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        
        mainLayout.addView(valueContainer, inputParams);

        // Action Spinner Listener to Show/Hide Value Input
        String currentAction = manager.getActionType(currentKey.code);
        if (currentAction == null || currentAction.isEmpty()) currentAction = "DEFAULT";
        for (int i = 0; i < actions.length; i++) {
            if (actions[i].equals(currentAction)) {
                actionSpinner.setSelection(i);
                break;
            }
        }

        actionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (actions[position].equals("WRITE")) {
                    valueContainer.setVisibility(View.VISIBLE);
                } else {
                    valueContainer.setVisibility(View.GONE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // --- BUTTONS LAYOUT (RESET + SAVE) ---
        LinearLayout buttonLayout = new LinearLayout(context);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setPadding(0, dpToPx(10), 0, 0);

        // Reset Button (Soft Red)
        Button resetBtn = new Button(context);
        resetBtn.setText("Reset Key");
        resetBtn.setTextColor(Color.parseColor("#D32F2F")); // Deep red text
        resetBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
        resetBtn.setTypeface(null, Typeface.BOLD);
        resetBtn.setAllCaps(false);
        
        GradientDrawable resetBg = new GradientDrawable();
        resetBg.setColor(Color.parseColor("#FFEBEE")); // Light red bg
        resetBg.setCornerRadius(dpToPx(24));
        if (Build.VERSION.SDK_INT >= 16) resetBtn.setBackground(resetBg);
        else resetBtn.setBackgroundDrawable(resetBg);

        LinearLayout.LayoutParams resetParams = new LinearLayout.LayoutParams(0, dpToPx(50), 1f);
        resetParams.setMargins(0, 0, dpToPx(12), 0);
        resetBtn.setLayoutParams(resetParams);
        
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.resetKey(currentKey.code);
                currentKey.customLabel = null;
                currentKey.actionType = "DEFAULT";
                currentKey.actionValue = "";
                
                if (onSaveCallback != null) onSaveCallback.run();
                dialog.dismiss();
                Toast.makeText(context, "Restored to Default!", Toast.LENGTH_SHORT).show();
            }
        });

        // Save Button (Solid Blue Accent)
        Button saveBtn = new Button(context);
        saveBtn.setText("Save Changes");
        saveBtn.setTextColor(Color.WHITE);
        saveBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
        saveBtn.setTypeface(null, Typeface.BOLD);
        saveBtn.setAllCaps(false);
        
        GradientDrawable saveBg = new GradientDrawable();
        saveBg.setColor(Color.parseColor("#3B82F6")); // Vibrant Blue
        saveBg.setCornerRadius(dpToPx(24));
        if (Build.VERSION.SDK_INT >= 16) saveBtn.setBackground(saveBg);
        else saveBtn.setBackgroundDrawable(saveBg);

        LinearLayout.LayoutParams saveParams = new LinearLayout.LayoutParams(0, dpToPx(50), 1.5f);
        saveBtn.setLayoutParams(saveParams);
        
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String type = actionSpinner.getSelectedItem().toString();
                String labelText = labelInput.getText().toString();
                String valueText = valueInput.getText().toString();
                
                String[] existingPopups = manager.getPopups(currentKey.code);
                manager.setCustomMapping(currentKey.code, labelText, type, valueText, existingPopups);
                
                currentKey.customLabel = labelText;
                currentKey.actionType = type;
                currentKey.actionValue = valueText;
                
                if (onSaveCallback != null) onSaveCallback.run();
                dialog.dismiss();
            }
        });
        
        buttonLayout.addView(resetBtn);
        buttonLayout.addView(saveBtn);
        mainLayout.addView(buttonLayout);
        
        dialog.setContentView(mainLayout);

        // Required window configuration for proper BottomSheet behavior & transparency
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // Enables rounded corners
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.BOTTOM);
            
            if (context instanceof android.inputmethodservice.InputMethodService) {
                window.setType(WindowManager.LayoutParams.TYPE_INPUT_METHOD_DIALOG);
                android.os.IBinder token = ((android.inputmethodservice.InputMethodService) context).getWindow().getWindow().getAttributes().token;
                if (token != null) {
                    WindowManager.LayoutParams attr = window.getAttributes();
                    attr.token = token;
                    window.setAttributes(attr);
                }
            }
        }
    }

    public void show() {
        if (dialog != null) {
            dialog.show();
        }
    }
}