package com.gboard2.nobi;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.view.HapticFeedbackConstants;
import android.view.View;

public class SoundManager {

    private Context context;
    private SharedPreferences prefs;
    private AudioManager audioManager;

    // Total 8 Sounds available
    public static final String[] SOUND_TYPES = {
        "System", "Mechanical", "Click", "Pop", 
        "Typewriter", "Wood", "Water", "Retro"
    };

    public SoundManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences("GboardPrefs", Context.MODE_PRIVATE);
        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public void playSoundAndHaptic(View view, String keyLabel) {
        boolean isHapticEnabled = prefs.getBoolean("vibrate_on_keypress", true);
        boolean isSoundEnabled = prefs.getBoolean("sound_on_keypress", false);
        
        float volume = prefs.getFloat("sound_volume", 1.0f);
        String soundType = prefs.getString("sound_type", "System");

        if (isHapticEnabled && view != null) {
            try {
                if (Build.VERSION.SDK_INT >= 27) {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_PRESS);
                } else {
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                }
            } catch (Exception e) {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            }
        }

        if (isSoundEnabled && audioManager != null) {
            int soundEffect = AudioManager.FX_KEYPRESS_STANDARD;
            
            // Map custom sounds to Android built-in AudioManager constants for variation
            if (soundType.equals("Pop")) {
                soundEffect = AudioManager.FX_KEYPRESS_SPACEBAR;
            } else if (soundType.equals("Click")) {
                soundEffect = AudioManager.FX_KEYPRESS_RETURN;
            } else if (soundType.equals("Mechanical")) {
                soundEffect = AudioManager.FX_KEYPRESS_DELETE;
            } else if (soundType.equals("Typewriter")) {
                soundEffect = AudioManager.FX_FOCUS_NAVIGATION_UP;
            } else if (soundType.equals("Wood")) {
                soundEffect = AudioManager.FX_FOCUS_NAVIGATION_DOWN;
            } else if (soundType.equals("Water")) {
                soundEffect = AudioManager.FX_FOCUS_NAVIGATION_LEFT;
            } else if (soundType.equals("Retro")) {
                soundEffect = AudioManager.FX_FOCUS_NAVIGATION_RIGHT;
            } else {
                // System default behavior mapping
                if (keyLabel != null) {
                    if (keyLabel.equals("SPACE")) soundEffect = AudioManager.FX_KEYPRESS_SPACEBAR;
                    else if (keyLabel.equals("DEL")) soundEffect = AudioManager.FX_KEYPRESS_DELETE;
                    else if (keyLabel.equals("ENTER")) soundEffect = AudioManager.FX_KEYPRESS_RETURN;
                }
            }

            audioManager.playSoundEffect(soundEffect, volume);
        }
    }
}
