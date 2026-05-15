package com.gboard2.nobi;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

public class GlobalCloudSync {
    
    private static final String SERVER_URL = "https://nobiboard.onrender.com/api/save_word";
    private static final String GET_URL = "https://nobiboard.onrender.com/api/get_all_words";
    private static final String API_SECRET_KEY = "gB0@rD_n0b1_Sync_7x!9$Pz#2kLm@8vWq";
    private static final String OFFLINE_PREFS = "GboardOfflineSync";

    public static void pushWordToServer(final Context context, final String word, final boolean isRetry) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(SERVER_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setRequestProperty("x-api-key", API_SECRET_KEY);
                    conn.setConnectTimeout(15000);
                    conn.setReadTimeout(15000);
                    conn.setDoOutput(true);

                    String jsonPayload = "{\"word\": \"" + word + "\"}";
                    OutputStream os = conn.getOutputStream();
                    os.write(jsonPayload.getBytes("UTF-8"));
                    os.close();

                    int responseCode = conn.getResponseCode();
                    conn.disconnect();

                    if (responseCode == 200) {
                        if (isRetry && context != null) {
                            SharedPreferences prefs = context.getSharedPreferences(OFFLINE_PREFS, Context.MODE_PRIVATE);
                            prefs.edit().remove(word).apply();
                        }
                    } else {
                        if (!isRetry && context != null) {
                            saveOfflineWord(context, word);
                        }
                    }
                } catch (Exception e) {
                    if (!isRetry && context != null) {
                        saveOfflineWord(context, word);
                    }
                }
            }
        }).start();
    }

    private static void saveOfflineWord(Context context, String word) {
        SharedPreferences prefs = context.getSharedPreferences(OFFLINE_PREFS, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(word, true).apply();
    }

    public static void syncOfflineWords(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences prefs = context.getSharedPreferences(OFFLINE_PREFS, Context.MODE_PRIVATE);
                Map<String, ?> pendingWords = prefs.getAll();
                for (String word : pendingWords.keySet()) {
                    pushWordToServer(context, word, true);
                    try {
                        Thread.sleep(500); 
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public static void downloadDictionaryUpdate(final Context context, final boolean isSilent) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(GET_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setRequestProperty("x-api-key", API_SECRET_KEY);
                    conn.setConnectTimeout(30000);
                    conn.setReadTimeout(30000);

                    int responseCode = conn.getResponseCode();
                    if (responseCode == 200) {
                        InputStream in = conn.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        StringBuilder result = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            result.append(line);
                        }
                        reader.close();
                        conn.disconnect();

                        JSONObject jsonResponse = new JSONObject(result.toString());
                        JSONArray wordsArray = jsonResponse.getJSONArray("words");

                        SharedPreferences prefs = context.getSharedPreferences("GboardPersonalDict", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        
                        int newWordsAdded = 0;
                        for (int i = 0; i < wordsArray.length(); i++) {
                            String serverWord = wordsArray.getString(i);
                            if (!prefs.contains(serverWord)) {
                                editor.putInt(serverWord, 1);
                                newWordsAdded++;
                            }
                        }
                        editor.apply();

                        final int finalCount = newWordsAdded;
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                if (!isSilent) {
                                    if (finalCount > 0) {
                                        Toast.makeText(context, "Success! " + finalCount + " new words synced.", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(context, "Dictionary is already up to date.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });

                    } else {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                if (!isSilent) {
                                    Toast.makeText(context, "Sync Error: Server disconnected.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e("GlobalCloudSync", "Error downloading dict", e);
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (!isSilent) {
                                Toast.makeText(context, "Sync Failed. Check Internet Connection.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        }).start();
    }
}
