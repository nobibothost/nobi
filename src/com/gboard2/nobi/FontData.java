package com.gboard2.nobi;

import android.graphics.Typeface;
import java.util.ArrayList;
import java.util.List;

public class FontData {

    public static class FontStyle {
        public String name;
        public Typeface keyboardTypeface;
        public String[] upperArr;
        public String[] lowerArr;

        public FontStyle(String name, Typeface tf, String upper, String lower) {
            this.name = name;
            this.keyboardTypeface = tf;
            this.upperArr = parseUnicode(upper);
            this.lowerArr = parseUnicode(lower);
        }

        private String[] parseUnicode(String str) {
            List<String> list = new ArrayList<>();
            int length = str.length();
            for (int offset = 0; offset < length; ) {
                int codepoint = str.codePointAt(offset);
                list.add(new String(Character.toChars(codepoint)));
                offset += Character.charCount(codepoint);
            }
            return list.toArray(new String[0]);
        }
    }

    public static List<FontStyle> getFonts() {
        List<FontStyle> fonts = new ArrayList<>();

        // 1. DEFAULT
        fonts.add(new FontStyle("Default", Typeface.DEFAULT, 
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ", 
                "abcdefghijklmnopqrstuvwxyz"));

        // 2. SCRIPT / CURSIVE
        fonts.add(new FontStyle("Script", Typeface.create("serif", Typeface.ITALIC), 
                "𝓐𝓑𝓒𝓓𝓔𝓕𝓖𝓗𝓘𝓙𝓚𝓛𝓜𝓝𝓞𝓟𝓠𝓡𝓢𝓣𝓤𝓥𝓦𝓧𝓨𝓩", 
                "𝓪𝓫𝓬𝓭𝓮𝓯𝓰𝓱𝓲𝓳𝓴𝓵𝓶𝓷𝓸𝓹𝓺𝓻𝓼𝓽𝓾𝓿𝔀𝔁𝔂𝔃"));

        // 3. DOUBLE STRUCK / HOLLOW
        fonts.add(new FontStyle("Hollow", Typeface.SANS_SERIF, 
                "𝔸𝔹ℂ𝔻𝔼𝔽𝔾ℍ𝕀𝕁𝕂𝕃𝕄ℕ𝕆ℙℚℝ𝕊𝕋𝕌𝕍𝕎𝕏𝕐ℤ", 
                "𝕒𝕓𝕔𝕕𝕖𝕗𝕘𝕙𝕚𝕛𝕜𝕝𝕞𝕟𝕠𝕡𝕢𝕣𝕤𝕥𝕦𝕧𝕨𝕩𝕪𝕫"));

        // 4. BOLD SERIF
        fonts.add(new FontStyle("Bold", Typeface.defaultFromStyle(Typeface.BOLD), 
                "𝐀𝐁𝐂𝐃𝐄𝐅𝐆𝐇𝐈𝐉𝐊𝐋𝐌𝐍𝐎𝐏𝐐𝐑𝐒𝐓𝐔𝐕𝐖𝐗𝐘𝐙", 
                "𝐚𝐛𝐜𝐝𝐞𝐟𝐠𝐡𝐢𝐣𝐤𝐥𝐦𝐧𝐨𝐩𝐪𝐫𝐬𝐭𝐮𝐯𝐰𝐱𝐲𝐳"));

        // 5. MONOSPACE
        fonts.add(new FontStyle("Typewriter", Typeface.MONOSPACE, 
                "𝙰𝙱𝙲𝙳𝙴𝙵𝙶Ｈ𝙸𝙹𝙺𝙻𝙼𝙽𝙾𝙿𝚀𝚁𝚂𝚃𝚄𝚅𝚆𝚇𝚈𝚉", 
                "𝚊𝚋𝚌𝚍𝚎𝚏𝚐𝚑𝚒𝚓𝚔𝚕𝚖𝚗𝚘𝚙𝚚𝚛𝚜𝚝𝚞𝚟𝚠𝚡𝚢𝚣"));

        // 6. GOTHIC
        fonts.add(new FontStyle("Gothic", Typeface.SERIF, 
                "𝔄𝔅ℭ𝔇𝔈𝔉𝔊ℌℑ𝔍𝔎𝔏𝔐𝔑𝔒𝔓𝔔ℜ𝔖𝔗𝔘𝔙𝔚𝔛𝔜ℨ", 
                "𝔞𝔟𝔠𝔡𝔢𝔣𝔤𝔥𝔦𝔧𝔨𝔩𝔪𝔫𝔬𝔭𝔮𝔯𝔰𝔱𝔲𝔳𝔴𝔵𝔶𝔷"));

        // 7. BOLD SANS-SERIF
        fonts.add(new FontStyle("Bold Sans", Typeface.defaultFromStyle(Typeface.BOLD), 
                "𝗔𝗕𝗖𝗗𝗘𝗙𝗚𝗛𝗜𝗝𝗞𝗟𝗠𝗡𝗢𝗣𝗤𝗥𝗦𝗧𝗨𝗩𝗪𝗫𝗬𝗭", 
                "𝗮𝗯𝗰𝗱𝗲𝗳𝗴𝗵𝗶𝗷𝗸𝗹𝗺𝗻𝗼𝗽𝗾𝗿𝘀𝘁𝘂𝘃𝘄𝘅𝘆𝘇"));

        // 8. ITALIC SANS-SERIF
        fonts.add(new FontStyle("Italic Sans", Typeface.defaultFromStyle(Typeface.ITALIC), 
                "𝘈𝘉𝘊𝘋𝘌𝘍𝘎𝘏𝘐𝘑𝘒𝘓𝘔𝘕𝘖𝘗𝘘𝘙𝘚𝘛𝘜𝘝𝘞𝘟𝘠𝘡", 
                "𝘢𝘣𝘤𝘥𝘦𝘧𝘨𝘩𝘪𝘫𝘬𝘭𝘮𝘯𝘰𝘱𝘲𝘳𝘴𝘵𝘶𝘷𝘸𝘹𝘺𝘻"));

        // 9. BOLD ITALIC SANS-SERIF
        fonts.add(new FontStyle("Bold Italic", Typeface.defaultFromStyle(Typeface.BOLD_ITALIC), 
                "𝘼𝘽𝘾𝘿𝙀𝙁𝙂𝙃𝙄𝙅𝙆𝙇𝙈𝙉𝙊𝙋𝙌𝙍𝙎𝙏𝙐𝙑𝙒𝙓𝙔𝙕", 
                "𝙖𝙗𝙘𝙙𝙚𝙛𝙜𝙝𝙞𝙟𝙠𝙡𝙢𝙣𝙤𝙥𝙦𝙧𝙨𝙩𝙪𝙫𝙬𝙭𝙮𝙯"));

        return fonts;
    }

    public static FontStyle getFontByName(String name) {
        for (FontStyle f : getFonts()) {
            if (f.name.equals(name)) return f;
        }
        return getFonts().get(0);
    }

    public static String convertText(String text, FontStyle style) {
        if (style.name.equals("Default")) return text;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            
            if (c >= 'A' && c <= 'Z') {
                int index = c - 'A';
                if (index < style.upperArr.length) {
                    sb.append(style.upperArr[index]);
                } else {
                    sb.append(c);
                }
            } else if (c >= 'a' && c <= 'z') {
                int index = c - 'a';
                if (index < style.lowerArr.length) {
                    sb.append(style.lowerArr[index]);
                } else {
                    sb.append(c);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
