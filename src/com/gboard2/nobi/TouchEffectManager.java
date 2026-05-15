package com.gboard2.nobi;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TouchEffectManager {

    private Context context;
    private SharedPreferences prefs;
    private Paint effectPaint;
    private List<EffectParticle> particles = new ArrayList<>();

    // Total 8 Effects available
    public static final String[] EFFECT_TYPES = {
        "None", "Pulse", "Sparkle", "Ripple",
        "Neon", "Burst", "Rings", "Stars"
    };

    private static class EffectParticle {
        float x, y;
        float radius, maxRadius;
        float alpha;
        float speedX, speedY;
        int type; 
    }

    public TouchEffectManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences("GboardPrefs", Context.MODE_PRIVATE);
        effectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        effectPaint.setStyle(Paint.Style.FILL);
    }

    public void addEffect(float x, float y, int themeColor) {
        String effectType = prefs.getString("touch_effect_type", "None");
        if (effectType.equals("None")) return;

        if (effectType.equals("Pulse") || effectType.equals("Neon")) {
            EffectParticle p = new EffectParticle();
            p.x = x; p.y = y;
            p.radius = dpToPx(5);
            p.maxRadius = effectType.equals("Neon") ? dpToPx(50) : dpToPx(40);
            p.alpha = 1.0f;
            p.type = effectType.equals("Neon") ? 4 : 0; // 0=Pulse, 4=Neon
            particles.add(p);
        } else if (effectType.equals("Sparkle") || effectType.equals("Stars")) {
            int count = effectType.equals("Stars") ? 10 : 6;
            for (int i = 0; i < count; i++) {
                EffectParticle p = new EffectParticle();
                p.x = x; p.y = y;
                p.radius = effectType.equals("Stars") ? dpToPx(4) : dpToPx(3);
                p.alpha = 1.0f;
                p.speedX = (float) ((Math.random() - 0.5) * dpToPx(250));
                p.speedY = (float) ((Math.random() - 0.5) * dpToPx(250));
                p.type = effectType.equals("Stars") ? 7 : 1; 
                particles.add(p);
            }
        } else if (effectType.equals("Burst")) {
            for (int i = 0; i < 8; i++) {
                EffectParticle p = new EffectParticle();
                p.x = x; p.y = y;
                p.radius = dpToPx(2);
                p.alpha = 1.0f;
                double angle = (Math.PI * 2 * i) / 8;
                p.speedX = (float) (Math.cos(angle) * dpToPx(300));
                p.speedY = (float) (Math.sin(angle) * dpToPx(300));
                p.type = 5; // Burst line
                particles.add(p);
            }
        } else if (effectType.equals("Ripple") || effectType.equals("Rings")) {
            EffectParticle p = new EffectParticle();
            p.x = x; p.y = y;
            p.radius = dpToPx(2);
            p.maxRadius = dpToPx(60);
            p.alpha = 1.0f;
            p.type = 3; // Ripple
            particles.add(p);
            
            if (effectType.equals("Rings")) {
                EffectParticle p2 = new EffectParticle();
                p2.x = x; p2.y = y;
                p2.radius = dpToPx(2);
                p2.maxRadius = dpToPx(40);
                p2.alpha = 0.5f; // Starts fainter
                p2.type = 3;
                particles.add(p2);
            }
        }
    }

    public void updateAndDraw(Canvas canvas, float dt, int themeColor) {
        if (particles.isEmpty()) return;

        Iterator<EffectParticle> it = particles.iterator();
        while (it.hasNext()) {
            EffectParticle p = it.next();

            if (p.type == 0 || p.type == 4) { // Pulse & Neon
                p.radius += (p.maxRadius - p.radius) * (p.type == 4 ? 12f : 10f) * dt;
                p.alpha -= (p.type == 4 ? 1.5f : 2f) * dt;
                if (p.alpha <= 0) { it.remove(); continue; }
                
                effectPaint.setStyle(Paint.Style.STROKE);
                effectPaint.setStrokeWidth(p.type == 4 ? dpToPx(4) : dpToPx(2));
                if (p.type == 4) effectPaint.setShadowLayer(10f, 0, 0, themeColor);
                else effectPaint.clearShadowLayer();
                
                effectPaint.setColor(themeColor);
                effectPaint.setAlpha((int) (255 * Math.max(0, p.alpha)));
                canvas.drawCircle(p.x, p.y, p.radius, effectPaint);
                effectPaint.clearShadowLayer();
                
            } else if (p.type == 1 || p.type == 7) { // Sparkle & Stars
                p.x += p.speedX * dt;
                p.y += p.speedY * dt;
                p.alpha -= 1.5f * dt;
                p.radius *= 0.95f; 
                if (p.alpha <= 0) { it.remove(); continue; }
                
                effectPaint.setStyle(Paint.Style.FILL);
                effectPaint.setColor(themeColor);
                effectPaint.setAlpha((int) (255 * Math.max(0, p.alpha)));
                canvas.drawCircle(p.x, p.y, p.radius, effectPaint);
                
            } else if (p.type == 3) { // Ripple
                p.radius += dpToPx(150) * dt; // Linear expansion
                p.alpha -= 1.2f * dt;
                if (p.alpha <= 0) { it.remove(); continue; }
                
                effectPaint.setStyle(Paint.Style.STROKE);
                effectPaint.setStrokeWidth(dpToPx(1.5f));
                effectPaint.setColor(themeColor);
                effectPaint.setAlpha((int) (255 * Math.max(0, p.alpha)));
                canvas.drawCircle(p.x, p.y, p.radius, effectPaint);
                
            } else if (p.type == 5) { // Burst
                float oldX = p.x; float oldY = p.y;
                p.x += p.speedX * dt;
                p.y += p.speedY * dt;
                p.alpha -= 2f * dt;
                if (p.alpha <= 0) { it.remove(); continue; }
                
                effectPaint.setStyle(Paint.Style.STROKE);
                effectPaint.setStrokeWidth(dpToPx(2));
                effectPaint.setColor(themeColor);
                effectPaint.setAlpha((int) (255 * Math.max(0, p.alpha)));
                canvas.drawLine(oldX, oldY, p.x, p.y, effectPaint);
            }
        }
    }

    private int dpToPx(float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }
}
