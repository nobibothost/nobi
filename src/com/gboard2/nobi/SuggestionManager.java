
package com.gboard2.nobi;

import java.util.ArrayList;
import java.util.List;

public class SuggestionManager {
    public boolean showToolbar = true;
    public List<String> currentSuggestions = new ArrayList<>();

    public void setSuggestions(List<String> suggestions, boolean showToolbar) {
        this.currentSuggestions = suggestions;
        this.showToolbar = showToolbar;
    }
}
