package com.gboard2.nobi;

import android.content.Context;
import android.graphics.Canvas;
import android.widget.Toast;

public class EditorKeyboardView extends ProKeyboardView {

    public EditorKeyboardView(Context context) {
        super(context);
        
        // प्योर टाइपिंग रेंडरर को हमारे कस्टम हाईलाइटिंग रेंडरर से ओवरराइड करें
        this.renderer = new EditorKeyboardRenderer(this);
        
        // कस्टम एडिटर लिसनर सेट करें, ताकि यह कभी टाइप करने का प्रयास न करें
        this.setKeyboardListener(new KeyboardListener() {
            @Override
            public void onKeyClick(String rawKey) {
                if (rawKey == null || rawKey.isEmpty()) return;
                
                // --- 'डिफ़ॉल्ट' प्रोफ़ाइल एडिट ब्लॉक ---
                CustomKeyManager manager = CustomKeyManager.getInstance(getContext());
                if (manager.getActiveProfile().equals("Default")) {
                    Toast.makeText(getContext(), "डिफ़ॉल्ट लेआउट एडिट नहीं किया जा सकता। कृपया नया प्रोफ़ाइल बनाएँ।", Toast.LENGTH_LONG).show();
                    return; // एडिटिंग ब्लॉक करें
                }
                
                if (rawKey.startsWith("CMD_")) {
                    Toast.makeText(getContext(), "System commands cannot be edited directly.", Toast.LENGTH_SHORT).show();
                    return;
                }

                KeyData editorKey = new KeyData(rawKey, rawKey, "");
                KeyEditSheet sheet = new KeyEditSheet(getContext(), editorKey, new Runnable() {
                    @Override
                    public void run() {
                        // उपयोगकर्ता द्वारा की' को सेव या रीसेट करने पर UI को रीफ्रेश करें
                        layoutManager.buildKeys();
                        postInvalidateOnAnimation();
                    }
                });
                sheet.show();
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // ओवरराइड फिक्स: super.onDraw को कॉल न करें, केवल रेंडरर को कॉल करें
        if (renderer != null) {
            renderer.draw(canvas);
        }
    }
}