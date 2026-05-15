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

    private CustomKeyManager(Context context) {
        prefs = context.getSharedPreferences("KeyCafeConfig", Context.MODE_PRIVATE);
        // सक्रिय प्रोफ़ाइल को डेटाबेस से लोड करें
        activeProfile = prefs.getString("active_layout_profile", "Default");
        loadConfig();
    }

    public static CustomKeyManager getInstance(Context context) {
        if (instance == null) {
            instance = new CustomKeyManager(context.getApplicationContext());
        }
        return instance;
    }

    // --- मल्टी-प्रोफ़ाइल लॉजिक ---

    // सक्रिय प्रोफ़ाइल का नाम प्राप्त करें
    public String getActiveProfile() {
        return activeProfile;
    }

    // सक्रिय प्रोफ़ाइल सेट करें और उसे डेटाबेस में सेव करें
    public void setActiveProfile(String profileName) {
        this.activeProfile = profileName;
        prefs.edit().putString("active_layout_profile", profileName).apply();
        loadConfig(); // सक्रिय प्रोफ़ाइल बदलने पर कॉन्फ़िगरेशन को फिर से लोड करें
    }

    // सेव किए गए प्रोफ़ाइल की सूची प्राप्त करें
    public List<String> getSavedProfiles() {
        Set<String> profiles = prefs.getStringSet("saved_profiles", new HashSet<String>());
        List<String> list = new ArrayList<>();
        list.add("Default"); // 'डिफ़ॉल्ट' को हमेशा पहले स्थान पर रखें
        for (String p : profiles) {
            if (!p.equals("Default")) list.add(p);
        }
        return list;
    }

    // नया प्रोफ़ाइल बनाएँ
    public void createNewProfile(String profileName) {
        if (profileName == null || profileName.trim().isEmpty() || profileName.equalsIgnoreCase("Default")) return;
        
        Set<String> profiles = new HashSet<>(prefs.getStringSet("saved_profiles", new HashSet<String>()));
        profiles.add(profileName);
        prefs.edit().putStringSet("saved_profiles", profiles).apply();
        setActiveProfile(profileName); // नया प्रोफ़ाइल बनाने के बाद उसे सक्रिय करें
    }

    // प्रोफ़ाइल के लिए डेटाबेस की' प्राप्त करें
    private String getProfileStorageKey() {
        return "custom_keys_" + activeProfile;
    }

    // --- एंड मल्टी-प्रोफ़ाइल लॉजिक ---

    // सक्रिय प्रोफ़ाइल के लिए कॉन्फ़िगरेशन लोड करें
    public void loadConfig() {
        if (activeProfile.equals("Default")) {
            customKeys = new JSONObject(); // 'डिफ़ॉल्ट' प्रोफ़ाइल हमेशा खाली रहती है
            return;
        }
        
        String jsonStr = prefs.getString(getProfileStorageKey(), "{}");
        try {
            customKeys = new JSONObject(jsonStr);
        } catch (JSONException e) {
            customKeys = new JSONObject();
        }
    }

    // सक्रिय प्रोफ़ाइल के लिए कॉन्फ़िगरेशन सेव करें
    private void saveConfig() {
        if (activeProfile.equals("Default")) return; // 'डिफ़ॉल्ट' में बदलाव को रोकें
        prefs.edit().putString(getProfileStorageKey(), customKeys.toString()).apply();
    }

    private String getNormalizedId(String keyId) {
        if (keyId != null && keyId.length() == 1) {
            char c = keyId.charAt(0);
            if (Character.isLetter(c)) {
                return String.valueOf(Character.toLowerCase(c));
            }
        }
        return keyId;
    }

    // कस्टमाइज्ड की' डेटा सेट करें
    public void setCustomMapping(String rawKeyId, String label, String actionType, String actionValue, String[] popups) {
        if (activeProfile.equals("Default")) return; // 'डिफ़ॉल्ट' प्रोफ़ाइल में एडिट ब्लॉक करें

        try {
            String keyId = getNormalizedId(rawKeyId);
            JSONObject keyData = new JSONObject();
            keyData.put("label", label);
            keyData.put("actionType", actionType);
            keyData.put("actionValue", actionValue);

            JSONArray popupsArray = new JSONArray();
            for (int i = 0; i < 5; i++) {
                popupsArray.put(popups != null && i < popups.length ? popups[i] : "");
            }
            keyData.put("popups", popupsArray);

            customKeys.put(keyId, keyData);
            saveConfig(); // सेव करने के लिए कॉन्फ़िगरेशन को फिर से लोड करें
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // की' कस्टमाइजेशन को रीसेट करें
    public void resetKey(String rawKeyId) {
        if (activeProfile.equals("Default")) return;

        String keyId = getNormalizedId(rawKeyId);
        if (customKeys.has(keyId)) {
            customKeys.remove(keyId);
            saveConfig(); // सेव करने के लिए कॉन्फ़िगरेशन को फिर से लोड करें
        }
    }

    private JSONObject getKeyData(String rawKeyId) {
        if (activeProfile.equals("Default")) return null;

        String keyId = getNormalizedId(rawKeyId);
        if (customKeys.has(keyId)) {
            try {
                return customKeys.getJSONObject(keyId);
            } catch (JSONException e) {
                return null;
            }
        }
        return null;
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
            } catch (JSONException e) {
                return null;
            }
        }
        return null;
    }
}