package com.gboard2.nobi;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;

public class KeyboardTestActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the new ScrollView layout
        setContentView(R.layout.activity_test_keyboard);
        
        // Auto-focus the first standard text field when activity opens
        EditText testStandard = findViewById(R.id.et_test_standard);
        if (testStandard != null) {
            testStandard.requestFocus();
        }
    }
}
