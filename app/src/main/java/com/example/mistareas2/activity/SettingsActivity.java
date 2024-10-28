package com.example.mistareas2.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.mistareas2.MainActivity;
import com.example.mistareas2.R;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "UserPreferences";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_LANGUAGE = "language";
    private Switch switchDarkMode;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch switchLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadThemePreference();
        setContentView(R.layout.settings_activity);

        switchDarkMode = findViewById(R.id.switch_dark_mode);
        switchLanguage = findViewById(R.id.switch_language);

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean(KEY_DARK_MODE, false);
        String language = sharedPreferences.getString(KEY_LANGUAGE, "es");

        // Set initial states of switches
        switchDarkMode.setChecked(isDarkMode);
        switchLanguage.setChecked(language.equals("en"));

        // Dark mode switch listener
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean(KEY_DARK_MODE, isChecked).apply();
            setAppTheme(isChecked);
            String message = isChecked ? getString(R.string.dark_mode_enabled) : getString(R.string.light_mode_enabled);
            Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_SHORT).show();
        });

        // Language switch listener
        switchLanguage.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String newLanguage = isChecked ? "en" : "es";
            sharedPreferences.edit().putString(KEY_LANGUAGE, newLanguage).apply();
            setAppLanguage(newLanguage);
            String message = isChecked ? getString(R.string.language_changed_en) : getString(R.string.language_changed_es);
            Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_SHORT).show();

            // Restart MainActivity to apply language changes
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadThemePreference() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean(KEY_DARK_MODE, false);
        setAppTheme(isDarkMode);
    }

    private void setAppTheme(boolean isDarkMode) {
        AppCompatDelegate.setDefaultNightMode(isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    private void setAppLanguage(String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);

        getApplicationContext().getResources().updateConfiguration(config, resources.getDisplayMetrics());
        recreate();

    }
}
