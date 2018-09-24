package ru.money;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Date;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(getPackageName(), "SettingsActivity is created");
        setContentView(R.layout.activity_settings);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.settings));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        ((ListView) findViewById(R.id.settingsList)).setOnItemClickListener(this);
        String versionName = "unknown";
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        ((TextView) findViewById(R.id.versionText)).setText(String.format(getResources().getString(R.string.version), versionName));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                exportDatabase();
                break;
            case 1:
                importDatabase();
                break;
        }
    }

    private void exportDatabase() {
        Log.i(getPackageName(), "Exporting database...");
        File data = Environment.getDataDirectory();
        if (Environment.getExternalStorageDirectory().canWrite()) {
            Log.i(getPackageName(), "Saving database...");
            String currentDBPath = "/data/" + getPackageName() + "/databases/" + "mainDB";
            File backupDB = new File(Environment.getExternalStorageDirectory() + "/Exported Databases/");
            File currentDB = new File(data, currentDBPath);
            //noinspection ResultOfMethodCallIgnored
            backupDB.mkdirs();
            Date date = new Date();
            backupDB = new File(backupDB, date.getTime() + ".db");
            try {
                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.i(getPackageName(), "Database exported");
            Toast.makeText(this, R.string.db_exported, Toast.LENGTH_SHORT).show();
        }
    }

    private void importDatabase() {
        Log.i(getPackageName(), "Opening import dialog");
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            if (data.getData().getPath().endsWith(".db")) {
                Log.i(getPackageName(), "Importing database...");
                String idArr[] = data.getData().getPath().split(":");
                File newDB = new File(idArr[1]);
                File dataFile = Environment.getDataDirectory();
                File oldDB = new File(dataFile, "/data/" + getPackageName() + "/databases/" + "mainDB");
                try {
                    FileChannel src = new FileInputStream(newDB).getChannel();
                    FileChannel dst = new FileOutputStream(oldDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.i(getPackageName(), "Database imported");
            } else {
                Log.i(getPackageName(), "Invalid database");
                Toast.makeText(this, R.string.db_invalid, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            Log.i(getPackageName(), "Back button on toolbar selected, finishing");
            finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }
}
