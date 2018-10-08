package ru.money.settings;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import ru.money.R;
import ru.money.list.ListActivity;

import static ru.money.App.LOG_TAG;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOG_TAG, "SettingsActivity is created");
        setContentView(R.layout.activity_settings);
        getSupportFragmentManager().beginTransaction().add(new SettingsFragment(), null);
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        String versionName = "unknown";
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        ((TextView) findViewById(R.id.versionText)).setText(String.format(getResources().getString(R.string.version), versionName));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            Log.i(LOG_TAG, "Back button on toolbar selected, finishing");
            Intent intent = new Intent(this, ListActivity.class);
            intent.putExtra("update", true);
            startActivity(intent);
            finish();
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
}
