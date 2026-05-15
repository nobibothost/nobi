
package com.gboard2.nobi;

import java.util.HashMap;
import java.util.Map;

public class SwipeEmojiManager {
    public Map<String, String[]> swipeEmojis = new HashMap<>();

    public SwipeEmojiManager() {
        initSwipeEmojis();
    }

    private void initSwipeEmojis() {
        swipeEmojis.put("Q", new String[]{"😍", "😻", "😽", "💕", "💖"});
        swipeEmojis.put("W", new String[]{"🤩", "🌟", "✨", "💫", "🥳"});
        swipeEmojis.put("E", new String[]{"😘", "😗", "😙", "😚", "💋"});
        swipeEmojis.put("R", new String[]{"🥰", "💘", "💝", "💗", "💓"});
        swipeEmojis.put("T", new String[]{"😇", "👼", "🕊️", "🤍", "✨"});
        swipeEmojis.put("Y", new String[]{"😋", "😛", "😝", "😜", "🤪"});
        swipeEmojis.put("U", new String[]{"🤗", "🫶", "🤝", "👐", "🤲"});
        swipeEmojis.put("I", new String[]{"🤔", "🧐", "🤨", "💭", "🧠"});
        swipeEmojis.put("O", new String[]{"😌", "😪", "🤤", "🥱", "😴"});
        swipeEmojis.put("P", new String[]{"😱", "😨", "😰", "😥", "😓"});
        
        swipeEmojis.put("A", new String[]{"✊", "👊", "🤛", "🤜", "✋"});
        swipeEmojis.put("S", new String[]{"😼", "😸", "😹", "😺", "😽"});
        swipeEmojis.put("D", new String[]{"❤️", "🩷", "🧡", "💛", "💚"});
        swipeEmojis.put("F", new String[]{"😭", "😢", "💧", "💦", "🌧️"});
        swipeEmojis.put("G", new String[]{"😣", "😖", "😫", "😩", "🥺"});
        swipeEmojis.put("H", new String[]{"👍", "👎", "👏", "🙌", "🤝"});
        swipeEmojis.put("J", new String[]{"💔", "❤️‍🩹", "🖤", "🩶", "🤍"});
        swipeEmojis.put("K", new String[]{"🤏", "🤌", "✌️", "🤞", "🫰"});
        swipeEmojis.put("L", new String[]{"🥱", "😪", "😴", "💤", "🛌"});
        
        swipeEmojis.put("Z", new String[]{"😤", "😮‍💨", "💨", "😾", "👿"});
        swipeEmojis.put("X", new String[]{"😠", "😡", "🤬", "😾", "💢"});
        swipeEmojis.put("C", new String[]{"😡", "🤬", "👹", "👺", "💥"});
        swipeEmojis.put("V", new String[]{"🧑‍🦰", "👩‍🦰", "👨‍🦰", "👱", "🧔"});
        swipeEmojis.put("B", new String[]{"😏", "😒", "🙄", "😬", "😑"});
        swipeEmojis.put("N", new String[]{"😎", "🥸", "🤓", "🧐", "🤠"});
        swipeEmojis.put("M", new String[]{"🙈", "🙉", "🙊", "🐵", "🐒"});
    }
}
