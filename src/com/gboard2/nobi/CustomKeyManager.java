package com.gboard2.nobi;

import android.content.Context;

public class CustomKeyManager {
    
    private static CustomKeyManager instance;

    // Disabled constructor
    private CustomKeyManager(Context context) {
        // Feature permanently disabled
    }

    public static CustomKeyManager getInstance(Context context) {
        if (instance == null) {
            instance = new CustomKeyManager(context.getApplicationContext());
        }
        return instance;
    }

    // Disabled mapping setter
    public void setCustomMapping(String originalKey, String label, String value, String[] popups) {
        // Do nothing
    }

    // Return null to bypass custom label
    public String getLabel(String originalKey) {
        return null;
    }

    // Return null to bypass custom input value
    public String getValue(String originalKey) {
        return null;
    }

    // Return null to bypass custom popups
    public String[] getPopups(String originalKey) {
        return null;
    }

    // Disabled reset logic
    public void resetKey(String originalKey) {
        // Do nothing
    }
}
