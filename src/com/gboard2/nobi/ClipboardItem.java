package com.gboard2.nobi;

import java.util.UUID;

public class ClipboardItem {
    public String id;
    public String text;
    public boolean isPinned;
    public long timestamp;
    public int type; // 0 = Text, 1 = Link

    public ClipboardItem(String text, boolean isPinned) {
        this.id = UUID.randomUUID().toString();
        this.text = text;
        this.isPinned = isPinned;
        this.timestamp = System.currentTimeMillis();
        this.type = (text.startsWith("http://") || text.startsWith("https://")) ? 1 : 0;
    }

    public ClipboardItem(String id, String text, boolean isPinned, long timestamp, int type) {
        this.id = id;
        this.text = text;
        this.isPinned = isPinned;
        this.timestamp = timestamp;
        this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ClipboardItem that = (ClipboardItem) obj;
        return isPinned == that.isPinned && id.equals(that.id) && text.equals(that.text);
    }
}
