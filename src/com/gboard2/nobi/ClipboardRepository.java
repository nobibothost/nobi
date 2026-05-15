package com.gboard2.nobi;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ClipboardRepository {
    private SharedPreferences prefs;
    private List<ClipboardItem> items = new ArrayList<>();

    public ClipboardRepository(Context context) {
        prefs = context.getSharedPreferences("nobi_clipboard_pro", Context.MODE_PRIVATE);
        load();
    }

    public void load() {
        String jsonStr = prefs.getString("clipboard_data", "[]");
        items.clear();
        try {
            JSONArray array = new JSONArray(jsonStr);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String id = obj.optString("id", java.util.UUID.randomUUID().toString());
                String text = obj.getString("text");
                boolean pinned = obj.getBoolean("pinned");
                long ts = obj.optLong("timestamp", System.currentTimeMillis());
                int type = obj.optInt("type", 0);
                items.add(new ClipboardItem(id, text, pinned, ts, type));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        sortItems();
    }

    public void save() {
        try {
            JSONArray array = new JSONArray();
            for (ClipboardItem item : items) {
                JSONObject obj = new JSONObject();
                obj.put("id", item.id);
                obj.put("text", item.text);
                obj.put("pinned", item.isPinned);
                obj.put("timestamp", item.timestamp);
                obj.put("type", item.type);
                array.put(obj);
            }
            prefs.edit().putString("clipboard_data", array.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<ClipboardItem> getItems() {
        return new ArrayList<>(items);
    }

    public void addItem(String text) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).text.equals(text)) {
                ClipboardItem existing = items.remove(i);
                existing.timestamp = System.currentTimeMillis();
                items.add(0, existing);
                sortItems();
                save();
                return;
            }
        }
        
        items.add(0, new ClipboardItem(text, false));
        if (items.size() > 50) {
            for (int i = items.size() - 1; i >= 0; i--) {
                if (!items.get(i).isPinned && items.size() > 50) {
                    items.remove(i);
                }
            }
        }
        sortItems();
        save();
    }

    public void deleteItem(ClipboardItem item) {
        items.remove(item);
        save();
    }

    public void togglePin(ClipboardItem item) {
        item.isPinned = !item.isPinned;
        sortItems();
        save();
    }

    public void updateItemText(ClipboardItem item, String newText) {
        item.text = newText;
        save();
    }

    public void clearRecent() {
        List<ClipboardItem> pinnedOnly = new ArrayList<>();
        for (ClipboardItem item : items) {
            if (item.isPinned) pinnedOnly.add(item);
        }
        items.clear();
        items.addAll(pinnedOnly);
        save();
    }

    private void sortItems() {
        Collections.sort(items, new Comparator<ClipboardItem>() {
            @Override
            public int compare(ClipboardItem a, ClipboardItem b) {
                if (a.isPinned && !b.isPinned) return -1;
                if (!a.isPinned && b.isPinned) return 1;
                return Long.compare(b.timestamp, a.timestamp); 
            }
        });
    }
}
