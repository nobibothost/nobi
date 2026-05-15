package com.gboard2.nobi;

import android.graphics.Color;
import java.util.ArrayList;
import java.util.List;

public class ThemeData {

    public static class Theme {
        public String name;
        public int bgColor;
        public int keyBgColor;
        public int specialKeyBgColor;
        public int keyPressedColor;
        public int enterBgColor;
        public int textColor;
        public int suggestionBgColor;
        public int suggestionTextColor;
        
        // --- ADVANCED IMAGE & GLOW PROPERTIES ---
        public int backgroundImageRes;
        public int keyImageRes;        
        public int textGlowColor;      
        public float textGlowRadius;   

        public Theme(String name, int bgColor, int keyBgColor, int specialKeyBgColor, int keyPressedColor, int enterBgColor, int textColor, int suggestionBgColor, int suggestionTextColor, int backgroundImageRes, int keyImageRes, int textGlowColor, float textGlowRadius) {
            this.name = name;
            this.bgColor = bgColor;
            this.keyBgColor = keyBgColor;
            this.specialKeyBgColor = specialKeyBgColor;
            this.keyPressedColor = keyPressedColor;
            this.enterBgColor = enterBgColor;
            this.textColor = textColor;
            this.suggestionBgColor = suggestionBgColor;
            this.suggestionTextColor = suggestionTextColor;
            this.backgroundImageRes = backgroundImageRes;
            this.keyImageRes = keyImageRes;
            this.textGlowColor = textGlowColor;
            this.textGlowRadius = textGlowRadius;
        }
    }

    public static List<Theme> getThemes() {
        List<Theme> themes = new ArrayList<>();

        // 1. DEFAULT GBOARD THEME
        themes.add(new Theme(
                "Default",
                Color.parseColor("#E4E7EB"), 
                Color.parseColor("#FFFFFF"), 
                Color.parseColor("#D4DADC"), 
                Color.parseColor("#CFD8DC"), 
                Color.parseColor("#4A90E2"), 
                Color.parseColor("#263238"), 
                Color.parseColor("#F5F7FA"), 
                Color.parseColor("#263238"),
                0, 0, Color.TRANSPARENT, 0f
        ));

        // 2. DARK THEME
        themes.add(new Theme(
                "Dark Gray",
                Color.parseColor("#1C1C1E"),
                Color.parseColor("#2C2C2E"),
                Color.parseColor("#3A3A3C"),
                Color.parseColor("#48484A"),
                Color.parseColor("#0A84FF"),
                Color.WHITE,
                Color.parseColor("#2C2C2E"),
                Color.WHITE,
                0, 0, Color.TRANSPARENT, 0f
        ));

        // 3. PURE BLACK (OLED)
        themes.add(new Theme(
                "OLED Night",
                Color.BLACK,
                Color.parseColor("#121212"),
                Color.parseColor("#1F1F1F"),
                Color.parseColor("#333333"),
                Color.parseColor("#007AFF"),
                Color.WHITE,
                Color.BLACK,
                Color.WHITE,
                0, 0, Color.TRANSPARENT, 0f
        ));

        // 4. NEON GLASS (ICE EFFECT WITH BUTTERFLY IMAGE)
        themes.add(new Theme(
                "Neon Glass",
                Color.parseColor("#000000"),   // Base BG (covered by image)
                Color.parseColor("#1AFFFFFF"), // Key BG: 10% White for glassy ice effect
                Color.parseColor("#26FFFFFF"), // Special Key: 15% White
                Color.parseColor("#40FFFFFF"), // Pressed: 25% White
                Color.parseColor("#00E5FF"),   // Enter Key: Bright Cyan
                Color.WHITE,                   // Text Color: White
                Color.parseColor("#80000000"), // Suggestion BG: 50% Black to make it readable over image
                Color.parseColor("#00E5FF"),   // Suggestion Text: Neon Cyan
                R.drawable.butterfly,          // Background Image
                0,                             // No custom key image (we use code-drawn glass)
                Color.parseColor("#00E5FF"),   // Text Glow Color
                15f                            // Increased Text Glow Radius for stronger neon
        ));

        // 5. RED THEME
        themes.add(new Theme(
                "Crimson",
                Color.parseColor("#3D0000"),
                Color.parseColor("#950101"),
                Color.parseColor("#FF0000"),
                Color.parseColor("#3D0000"),
                Color.parseColor("#FF0000"),
                Color.WHITE,
                Color.parseColor("#3D0000"),
                Color.WHITE,
                0, 0, Color.TRANSPARENT, 0f
        ));

        // 6. PURPLE THEME
        themes.add(new Theme(
                "Lavender",
                Color.parseColor("#F3E5F5"),
                Color.parseColor("#CE93D8"),
                Color.parseColor("#AB47BC"),
                Color.parseColor("#8E24AA"),
                Color.parseColor("#7B1FA2"),
                Color.BLACK,
                Color.parseColor("#E1BEE7"),
                Color.BLACK,
                0, 0, Color.TRANSPARENT, 0f
        ));

        // 7. NEON CYBERPUNK
        themes.add(new Theme(
                "Cyberpunk",
                Color.parseColor("#1A1A2E"),
                Color.parseColor("#16213E"),
                Color.parseColor("#0F3460"),
                Color.parseColor("#E94560"),
                Color.parseColor("#E94560"),
                Color.WHITE,
                Color.parseColor("#1A1A2E"),
                Color.parseColor("#E94560"),
                0, 0, Color.parseColor("#E94560"), 8f 
        ));

        // 8. OCEAN BLUE
        themes.add(new Theme(
                "Ocean Blue",
                Color.parseColor("#E0F7FA"),
                Color.parseColor("#FFFFFF"),
                Color.parseColor("#B2EBF2"),
                Color.parseColor("#80DEEA"),
                Color.parseColor("#00BCD4"),
                Color.parseColor("#006064"),
                Color.parseColor("#E0F7FA"),
                Color.parseColor("#00838F"),
                0, 0, Color.TRANSPARENT, 0f
        ));

        // 9. FOREST MINT
        themes.add(new Theme(
                "Forest Mint",
                Color.parseColor("#E8F5E9"),
                Color.parseColor("#FFFFFF"),
                Color.parseColor("#C8E6C9"),
                Color.parseColor("#A5D6A7"),
                Color.parseColor("#4CAF50"),
                Color.parseColor("#1B5E20"),
                Color.parseColor("#E8F5E9"),
                Color.parseColor("#2E7D32"),
                0, 0, Color.TRANSPARENT, 0f
        ));

        // 10. SUNSET ORANGE
        themes.add(new Theme(
                "Sunset Orange",
                Color.parseColor("#FFF3E0"),
                Color.parseColor("#FFFFFF"),
                Color.parseColor("#FFE0B2"),
                Color.parseColor("#FFCC80"),
                Color.parseColor("#FF9800"),
                Color.parseColor("#E65100"),
                Color.parseColor("#FFF3E0"),
                Color.parseColor("#EF6C00"),
                0, 0, Color.TRANSPARENT, 0f
        ));

        // 11. GOLD LUXURY
        themes.add(new Theme(
                "Gold Luxury",
                Color.parseColor("#263238"),
                Color.parseColor("#37474F"),
                Color.parseColor("#455A64"),
                Color.parseColor("#546E7A"),
                Color.parseColor("#FFD700"),
                Color.WHITE,
                Color.parseColor("#263238"),
                Color.parseColor("#FFD700"),
                0, 0, Color.parseColor("#FFD700"), 5f 
        ));

        return themes;
    }

    public static Theme getThemeByName(String name) {
        for (Theme t : getThemes()) {
            if (t.name.equals(name)) return t;
        }
        return getThemes().get(0);
    }
}
