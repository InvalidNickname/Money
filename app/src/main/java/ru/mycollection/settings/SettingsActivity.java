package ru.mycollection.settings;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import ru.mycollection.R;
import ru.mycollection.list.ListActivity;

import static ru.mycollection.App.LOG_TAG;

public class SettingsActivity extends AppCompatActivity {

    private SettingsFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOG_TAG, "SettingsActivity is created");
        setContentView(R.layout.activity_settings);
        fragment = new SettingsFragment();
        getSupportFragmentManager().beginTransaction().add(fragment, "settings");
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, ListActivity.class);
        intent.putExtra("update", true);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onPause() {
        fragment.cancelTask();
        super.onPause();
    }
}
