
package com.gboard2.nobi;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.RectF;

public class ResizeController {
    public boolean isResizing = false;
    public float userHeightScale = 1.0f;
    public boolean pressingDone = false;
    public boolean isDraggingResize = false;
    public float dragStartY = 0f;
    public float initialScaleOnDrag = 1.0f;

    private Context context;

    public ResizeController(Context context) {
        this.context = context;
        SharedPreferences prefs = context.getSharedPreferences("GboardPrefs", Context.MODE_PRIVATE);
        userHeightScale = prefs.getFloat("keyboard_scale", 1.0f);
    }

    public int dpToPx(float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    public RectF getDoneButtonBounds(int width, int height) {
        float resizeCx = width / 2f;
        float resizeCy = height / 2f;
        return new RectF(resizeCx - dpToPx(60), resizeCy - dpToPx(24), resizeCx + dpToPx(60), resizeCy + dpToPx(24));
    }
}
