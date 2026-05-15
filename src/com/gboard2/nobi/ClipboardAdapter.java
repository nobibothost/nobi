
package com.gboard2.nobi;

import android.animation.LayoutTransition;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ClipboardAdapter {

    private Context context;
    private List<ClipboardItem> items = new ArrayList<>();
    private AdapterListener listener;
    private LinearLayout container;
    private ThemeData.Theme currentTheme = ThemeData.getThemeByName("Default");

    // Progressive Loading Handler
    private Handler chunkHandler = new Handler(Looper.getMainLooper());

    public interface AdapterListener {
        void onItemClick(ClipboardItem item);
        void onPinToggled(ClipboardItem item);
        void onItemDeleted(ClipboardItem item);
        void onRequestRename(ClipboardItem item);
    }

    public ClipboardAdapter(Context context, LinearLayout container, AdapterListener listener) {
        this.context = context;
        this.container = container;
        this.listener = listener;

        LayoutTransition lt = new LayoutTransition();
        if (Build.VERSION.SDK_INT >= 16) {
            lt.enableTransitionType(LayoutTransition.CHANGING);
        }
        this.container.setLayoutTransition(lt);
    }
    
    public void applyTheme(ThemeData.Theme theme) {
        if (theme == null) return;
        this.currentTheme = theme;
        renderAll();
    }

    public void updateData(List<ClipboardItem> newItems) {
        this.items.clear();
        this.items.addAll(newItems);
        renderAll();
    }

    public List<ClipboardItem> getItems() {
        return items;
    }
    
    public void cancelLoading() {
        chunkHandler.removeCallbacksAndMessages(null);
    }

    private int dpToPx(float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    private void renderAll() {
        container.removeAllViews();
        cancelLoading();
        List<ClipboardItem> pinnedList = new ArrayList<>();
        List<ClipboardItem> recentList = new ArrayList<>();
        
        for (ClipboardItem item : items) {
            if (item.isPinned) pinnedList.add(item);
            else recentList.add(item);
        }

        renderSectionProgressive("Recent", recentList);
        renderSection("Pinned", pinnedList);
    }

    private void renderSection(String title, List<ClipboardItem> sectionItems) {
        if (sectionItems.isEmpty()) return;

        // --- Section Header ---
        TextView header = new TextView(context);
        header.setText(title);
        header.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        header.setTextColor(currentTheme.textColor);
        header.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        headerParams.setMargins(dpToPx(10), dpToPx(12), dpToPx(6), dpToPx(6));
        container.addView(header, headerParams);

        // --- Section Staggered Grid ---
        LinearLayout gridLayout = new LinearLayout(context);
        gridLayout.setOrientation(LinearLayout.HORIZONTAL);
        gridLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout col1 = new LinearLayout(context);
        col1.setOrientation(LinearLayout.VERTICAL);
        gridLayout.addView(col1, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        LinearLayout col2 = new LinearLayout(context);
        col2.setOrientation(LinearLayout.VERTICAL);
        gridLayout.addView(col2, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        LayoutTransition lt = new LayoutTransition();
        if (Build.VERSION.SDK_INT >= 16) {
            lt.enableTransitionType(LayoutTransition.CHANGING);
        }
        col1.setLayoutTransition(lt);
        col2.setLayoutTransition(lt);

        container.addView(gridLayout);

        int h1 = 0;
        int h2 = 0;
        
        for (ClipboardItem item : sectionItems) {
            View cardView = createCardView(item);
            int lengthForHeight = Math.min(item.text.length(), 150);
            int simulatedHeight = lengthForHeight < 20 ? 1 : (lengthForHeight / 20) + 1;
            
            if (h1 <= h2) {
                col1.addView(cardView);
                h1 += simulatedHeight;
            } else {
                col2.addView(cardView);
                h2 += simulatedHeight;
            }
        }
    }

    private void renderSectionProgressive(String title, final List<ClipboardItem> sectionItems) {
        if (sectionItems.isEmpty()) return;

        TextView header = new TextView(context);
        header.setText(title);
        header.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        header.setTextColor(currentTheme.textColor);
        header.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        headerParams.setMargins(dpToPx(10), dpToPx(12), dpToPx(6), dpToPx(6));
        container.addView(header, headerParams);

        LinearLayout gridLayout = new LinearLayout(context);
        gridLayout.setOrientation(LinearLayout.HORIZONTAL);
        gridLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        
        final LinearLayout col1 = new LinearLayout(context);
        col1.setOrientation(LinearLayout.VERTICAL);
        gridLayout.addView(col1, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        final LinearLayout col2 = new LinearLayout(context);
        col2.setOrientation(LinearLayout.VERTICAL);
        gridLayout.addView(col2, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        LayoutTransition lt = new LayoutTransition();
        if (Build.VERSION.SDK_INT >= 16) {
            lt.enableTransitionType(LayoutTransition.CHANGING);
        }
        col1.setLayoutTransition(lt);
        col2.setLayoutTransition(lt);

        container.addView(gridLayout);

        final int[] heights = {0, 0};
        final int[] index = {0};

        Runnable loader = new Runnable() {
            @Override
            public void run() {
                int limit = index[0] + 5;
                // Load 5 cards per frame to prevent freezing
                while (index[0] < sectionItems.size() && index[0] < limit) {
                    ClipboardItem item = sectionItems.get(index[0]);
                    View cardView = createCardView(item);
                    
                    int lengthForHeight = Math.min(item.text.length(), 150);
                    int simulatedHeight = lengthForHeight < 20 ? 1 : (lengthForHeight / 20) + 1;
                    
                    if (heights[0] <= heights[1]) {
                        col1.addView(cardView);
                        heights[0] += simulatedHeight;
                    } else {
                        col2.addView(cardView);
                        heights[1] += simulatedHeight;
                    }
                    index[0]++;
                }
                if (index[0] < sectionItems.size()) {
                    chunkHandler.postDelayed(this, 10);
                }
            }
        };
        chunkHandler.post(loader);
    }

    private View createCardView(final ClipboardItem item) {
        final FrameLayout swipeContainer = new FrameLayout(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
        swipeContainer.setLayoutParams(params);
        
        // --- Red Reveal Background for Swipe Left ---
        LinearLayout redBg = new LinearLayout(context);
        GradientDrawable bgShape = new GradientDrawable();
        bgShape.setColor(Color.parseColor("#E53935")); // Red fixed color
        bgShape.setCornerRadius(dpToPx(12));
        redBg.setBackground(bgShape);
        redBg.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        redBg.setPadding(0, 0, dpToPx(16), 0);
        
        ImageView deleteIcon = new ImageView(context);
        Drawable d = context.getResources().getDrawable(R.drawable.ic_delete, null);
        if (d != null) d.setTint(Color.WHITE);
        deleteIcon.setImageDrawable(d);
        redBg.addView(deleteIcon, new LinearLayout.LayoutParams(dpToPx(24), dpToPx(24)));
        swipeContainer.addView(redBg, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        
        // --- Main Card ---
        final LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));
        card.setClickable(true);
        
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(currentTheme.keyBgColor);
        cardBg.setStroke(0, Color.TRANSPARENT);
        cardBg.setCornerRadius(dpToPx(12));
        card.setBackground(cardBg);
        if (Build.VERSION.SDK_INT >= 21) {
            card.setElevation(0f);
        }

        // --- SMART TRUNCATION TO PREVENT UI LAG ---
        String displayStr = item.text;
        if (displayStr.length() > 150) {
            displayStr = displayStr.substring(0, 150) + "...";
        }

        TextView textView = new TextView(context);
        textView.setText(displayStr);
        textView.setTextColor(currentTheme.textColor);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        textView.setMaxLines(4);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        card.addView(textView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Bottom Type Icon (Only if Link)
        if (item.type == 1) {
            LinearLayout bottomRow = new LinearLayout(context);
            bottomRow.setOrientation(LinearLayout.HORIZONTAL);
            bottomRow.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams bottomParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            bottomParams.topMargin = dpToPx(8);
            card.addView(bottomRow, bottomParams);

            ImageView typeIcon = new ImageView(context);
            Drawable linkIcon = context.getResources().getDrawable(R.drawable.ic_language, null);
            if (linkIcon != null) linkIcon.setTint(currentTheme.textColor);
            typeIcon.setImageDrawable(linkIcon);
            bottomRow.addView(typeIcon, new LinearLayout.LayoutParams(dpToPx(16), dpToPx(16)));
        }

        swipeContainer.addView(card, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        
        // --- Native Touch & Swipe Engine ---
        card.setOnTouchListener(new View.OnTouchListener() {
            private float startX;
            private float startY;
            private boolean isSwiping = false;
            
            private Runnable longPressRunnable = new Runnable() {
                @Override
                public void run() {
                    if (!isSwiping) {
                        card.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                        showContextMenu(card, item);
                    }
                }
            };

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getRawX();
                        startY = event.getRawY();
                        isSwiping = false;
                        v.animate().scaleX(0.96f).scaleY(0.96f).setDuration(100).start();
                        v.postDelayed(longPressRunnable, 400);
                        return true;
                        
                    case MotionEvent.ACTION_MOVE:
                        float dx = event.getRawX() - startX;
                        float dy = event.getRawY() - startY;
                        
                        if (dx > 0) dx = 0; // Prevent right swipe completely
                        
                        if (!isSwiping && Math.abs(dx) > dpToPx(10) && Math.abs(dx) > Math.abs(dy)) {
                            v.removeCallbacks(longPressRunnable);
                            isSwiping = true;
                            swipeContainer.getParent().requestDisallowInterceptTouchEvent(true); 
                        }
                        
                        if (isSwiping) {
                            v.setTranslationX(dx);
                            v.setAlpha(1.0f - (Math.abs(dx) / (float) v.getWidth()));
                        } else if (Math.abs(dy) > dpToPx(10)) {
                            v.removeCallbacks(longPressRunnable);
                        }
                        break;
                        
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        v.removeCallbacks(longPressRunnable);
                        v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                        
                        if (isSwiping) {
                            if (Math.abs(v.getTranslationX()) > v.getWidth() / 2.5f) {
                                float endX = v.getTranslationX() > 0 ? v.getWidth() : -v.getWidth();
                                v.animate().translationX(endX).alpha(0f).setDuration(200).withEndAction(new Runnable() {
                                    public void run() {
                                        listener.onItemDeleted(item);
                                    }
                                }).start();
                            } else {
                                v.animate().translationX(0).alpha(1f).setDuration(200).start();
                            }
                        } else if (event.getAction() == MotionEvent.ACTION_UP && Math.abs(event.getRawX() - startX) < dpToPx(10) && Math.abs(event.getRawY() - startY) < dpToPx(10)) {
                            // Tap to Paste (Uses Original Item Full Text)
                            listener.onItemClick(item);
                        }
                        break;
                }
                return true;
            }
        });

        return swipeContainer;
    }

    private void showContextMenu(View anchor, final ClipboardItem item) {
        LinearLayout menuLayout = new LinearLayout(context);
        menuLayout.setOrientation(LinearLayout.VERTICAL);
        menuLayout.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
        
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(currentTheme.bgColor);
        bg.setCornerRadius(dpToPx(12));
        menuLayout.setBackground(bg);
        if (Build.VERSION.SDK_INT >= 21) menuLayout.setElevation(dpToPx(6));
        
        final PopupWindow popup = new PopupWindow(menuLayout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, false);
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setOutsideTouchable(true);
        popup.setTouchable(true);
        
        menuLayout.addView(createMenuItem("Paste", new Runnable() {
            @Override public void run() { popup.dismiss(); listener.onItemClick(item); }
        }));
        
        String pinTitle = item.isPinned ? "Unpin" : "Pin";
        menuLayout.addView(createMenuItem(pinTitle, new Runnable() {
            @Override public void run() { popup.dismiss(); listener.onPinToggled(item); }
        }));
        
        menuLayout.addView(createMenuItem("Rename", new Runnable() {
            @Override public void run() { popup.dismiss(); listener.onRequestRename(item); }
        }));
        
        menuLayout.addView(createMenuItem("Delete", new Runnable() {
            @Override public void run() { popup.dismiss(); listener.onItemDeleted(item); }
        }));
        
        menuLayout.setScaleX(0.9f);
        menuLayout.setScaleY(0.9f);
        menuLayout.setAlpha(0f);
        menuLayout.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(150).start();

        int xOffset = (anchor.getWidth() / 2) - dpToPx(60); 
        int yOffset = -(anchor.getHeight() / 2) - dpToPx(80);
        popup.showAsDropDown(anchor, xOffset, yOffset);
    }

    private TextView createMenuItem(String title, final Runnable action) {
        TextView tv = new TextView(context);
        tv.setText(title);
        tv.setPadding(dpToPx(24), dpToPx(12), dpToPx(48), dpToPx(12)); 
        tv.setTextColor(currentTheme.textColor);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        
        StateListDrawable states = new StateListDrawable();
        GradientDrawable pressed = new GradientDrawable();
        pressed.setColor(currentTheme.keyPressedColor); 
        pressed.setCornerRadius(dpToPx(8));
        
        GradientDrawable normal = new GradientDrawable();
        normal.setColor(Color.TRANSPARENT);
        normal.setCornerRadius(dpToPx(8));
        
        states.addState(new int[]{android.R.attr.state_pressed}, pressed);
        states.addState(new int[]{}, normal);
        
        if (Build.VERSION.SDK_INT >= 16) {
            tv.setBackground(states);
        } else {
            tv.setBackgroundDrawable(states);
        }
        
        tv.setClickable(true);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { action.run(); }
        });
        
        return tv;
    }
}
