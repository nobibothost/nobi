package com.gboard2.nobi;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CustomKeyManager {
    
    private static CustomKeyManager instance;
    private SharedPreferences prefs;
    private JSONObject customKeys;
    private String activeProfile = "Default";

    // --- UNDO / REDO STATE VARIABLES ---
    private List<String> historyStack = new ArrayList<>();
    private int historyIndex = -1;
    private int savedHistoryIndex = -1; 

    private CustomKeyManager(Context context) {
        prefs = context.getSharedPreferences("KeyCafeConfig", Context.MODE_PRIVATE);
        activeProfile = prefs.getString("active_layout_profile", "Default");
        loadConfig();
    }

    public static CustomKeyManager getInstance(Context context) {
        if (instance == null) {
            instance = new CustomKeyManager(context.getApplicationContext());
        }
        return instance;
    }

    public String getActiveProfile() {
        return activeProfile;
    }

    public void setActiveProfile(String profileName) {
        this.activeProfile = profileName;
        prefs.edit().putString("active_layout_profile", profileName).apply();
        loadConfig(); 
    }

    public List<String> getSavedProfiles() {
        Set<String> profiles = prefs.getStringSet("saved_profiles", new HashSet<String>());
        List<String> list = new ArrayList<>();
        list.add("Default"); 
        for (String p : profiles) {
            if (!p.equals("Default")) list.add(p);
        }
        return list;
    }

    public void createNewProfile(String profileName) {
        if (profileName == null || profileName.trim().isEmpty() || profileName.equalsIgnoreCase("Default")) return;
        
        Set<String> profiles = new HashSet<>(prefs.getStringSet("saved_profiles", new HashSet<String>()));
        profiles.add(profileName);
        prefs.edit().putStringSet("saved_profiles", profiles).apply();
        setActiveProfile(profileName); 
    }

    // --- NEW: RENAME & DELETE PROFILE LOGIC ---
    
    public boolean deleteProfile(String profileName) {
        if (profileName == null || profileName.equals("Default")) return false; // Block deleting Default
        
        Set<String> profiles = new HashSet<>(prefs.getStringSet("saved_profiles", new HashSet<String>()));
        if (profiles.contains(profileName)) {
            profiles.remove(profileName);
            prefs.edit().putStringSet("saved_profiles", profiles).apply();
            prefs.edit().remove("custom_keys_" + profileName).apply(); // Delete associated layout data
            
            if (activeProfile.equals(profileName)) {
                setActiveProfile("Default"); // Fallback to Default if active profile is deleted
            }
            return true;
        }
        return false;
    }

    public boolean renameProfile(String oldName, String newName) {
        if (oldName == null || oldName.equals("Default") || newName == null || newName.trim().isEmpty() || newName.equalsIgnoreCase("Default")) return false;
        
        Set<String> profiles = new HashSet<>(prefs.getStringSet("saved_profiles", new HashSet<String>()));
        if (profiles.contains(oldName) && !profiles.contains(newName)) { // Ensure new name doesn't overwrite an existing one
            profiles.remove(oldName);
            profiles.add(newName);
            prefs.edit().putStringSet("saved_profiles", profiles).apply();
            
            // Transfer JSON Data to new Profile Name
            String oldData = prefs.getString("custom_keys_" + oldName, "{}");
            prefs.edit().putString("custom_keys_" + newName, oldData).apply();
            prefs.edit().remove("custom_keys_" + oldName).apply(); // Delete old data
            
            if (activeProfile.equals(oldName)) {
                setActiveProfile(newName); // Keep user on the renamed profile
            }
            return true;
        }
        return false;
    }

    // --- END RENAME & DELETE LOGIC ---

    private String getProfileStorageKey() {
        return "custom_keys_" + activeProfile;
    }

    public void loadConfig() {
        if (activeProfile.equals("Default")) {
            customKeys = new JSONObject(); 
        } else {
            String jsonStr = prefs.getString(getProfileStorageKey(), "{}");
            try {
                customKeys = new JSONObject(jsonStr);
            } catch (JSONException e) {
                customKeys = new JSONObject();
            }
        }
        
        historyStack.clear();
        historyStack.add(customKeys.toString());
        historyIndex = 0;
        savedHistoryIndex = 0;
    }

    public void saveToHistory() {
        if (activeProfile.equals("Default")) return;
        
        while (historyStack.size() > historyIndex + 1) {
            historyStack.remove(historyStack.size() - 1);
        }
        
        historyStack.add(customKeys.toString());
        historyIndex++;
    }

    public void commitChanges() {
        if (activeProfile.equals("Default")) return;
        prefs.edit().putString(getProfileStorageKey(), customKeys.toString()).apply();
        savedHistoryIndex = historyIndex; 
    }

    public void undo() {
        if (canUndo()) {
            historyIndex--;
            try { customKeys = new JSONObject(historyStack.get(historyIndex)); } 
            catch (JSONException e) { e.printStackTrace(); }
        }
    }

    public void redo() {
        if (canRedo()) {
            historyIndex++;
            try { customKeys = new JSONObject(historyStack.get(historyIndex)); } 
            catch (JSONException e) { e.printStackTrace(); }
        }
    }

    public boolean canUndo() { return historyIndex > 0; }
    public boolean canRedo() { return historyIndex < historyStack.size() - 1; }
    public boolean hasUnsavedChanges() { return historyIndex != savedHistoryIndex; }

    private String getNormalizedId(String keyId) {
        if (keyId != null && keyId.length() == 1) {
            char c = keyId.charAt(0);
            if (Character.isLetter(c)) {
                return String.valueOf(Character.toLowerCase(c));
            }
        }
        return keyId;
    }

    public void setCustomMapping(String rawKeyId, String label, String actionType, String actionValue, String[] popups) {
        if (activeProfile.equals("Default")) return; 

        try {
            String keyId = getNormalizedId(rawKeyId);
            JSONObject keyData = getKeyData(rawKeyId);
            if (keyData == null) keyData = new JSONObject();
            
            keyData.put("label", label);
            keyData.put("actionType", actionType);
            keyData.put("actionValue", actionValue);

            JSONArray popupsArray = new JSONArray();
            for (int i = 0; i < 5; i++) {
                popupsArray.put(popups != null && i < popups.length ? popups[i] : "");
            }
            keyData.put("popups", popupsArray);

            customKeys.put(keyId, keyData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void resetKey(String rawKeyId) {
        if (activeProfile.equals("Default")) return;

        String keyId = getNormalizedId(rawKeyId);
        if (customKeys.has(keyId)) {
            customKeys.remove(keyId);
        }
    }

    private JSONObject getKeyData(String rawKeyId) {
        if (activeProfile.equals("Default")) return null;

        String keyId = getNormalizedId(rawKeyId);
        if (customKeys.has(keyId)) {
            try { return customKeys.getJSONObject(keyId); } 
            catch (JSONException e) { return null; }
        }
        return null;
    }

    public boolean isSwapped(String rawKeyId) {
        JSONObject data = getKeyData(rawKeyId);
        return data != null && data.optBoolean("isSwapped", false);
    }

    public void setSwapped(String rawKeyId, boolean swapped) {
        if (activeProfile.equals("Default")) return;
        try {
            String keyId = getNormalizedId(rawKeyId);
            JSONObject keyData = getKeyData(rawKeyId);
            if (keyData == null) keyData = new JSONObject();
            keyData.put("isSwapped", swapped);
            customKeys.put(keyId, keyData);
        } catch (JSONException e) { e.printStackTrace(); }
    }

    public String getLabel(String keyId) {
        JSONObject data = getKeyData(keyId);
        return data != null ? data.optString("label", null) : null;
    }

    public String getActionType(String keyId) {
        JSONObject data = getKeyData(keyId);
        return data != null ? data.optString("actionType", "DEFAULT") : "DEFAULT";
    }

    public String getValue(String keyId) {
        JSONObject data = getKeyData(keyId);
        return data != null ? data.optString("actionValue", null) : null;
    }

    public String[] getPopups(String keyId) {
        JSONObject data = getKeyData(keyId);
        if (data != null && data.has("popups")) {
            try {
                JSONArray array = data.getJSONArray("popups");
                String[] popups = new String[5];
                for (int i = 0; i < 5; i++) {
                    popups[i] = array.optString(i, "");
                }
                return popups;
            } catch (JSONException e) { return null; }
        }
        return null;
    }
}