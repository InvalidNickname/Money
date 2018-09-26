package ru.money;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import java.util.Objects;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            Log.i(getPackageName(), "WRITE_EXTERNAL_STORAGE permission isn't granted, requesting");
        } else {
            Log.i(getPackageName(), "Exporting database...");
            File data = Environment.getDataDirectory();
            if (Environment.getExternalStorageDirectory().canWrite()) {
                Log.i(getPackageName(), "Exporting database...");
                String currentDBPath = "/data/" + getPackageName() + "/databases/" + "mainDB";
                File backupDB = new File(Environment.getExternalStorageDirectory() + "/Exported Databases/");
                File currentDB = new File(data, currentDBPath);
                //noinspection ResultOfMethodCallIgnored
                backupDB.mkdirs();
                Date date = new Date();
                long time = date.getTime();
                backupDB = new File(backupDB, time + ".db");
                try {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.i(getPackageName(), "Database exported, exporting images");
                String currentDataPath = "/data/" + getPackageName() + "/files/";
                File backupData = new File(Environment.getExternalStorageDirectory() + "/Exported Databases/" + time);
                File currentData = new File(data, currentDataPath);
                //noinspection ResultOfMethodCallIgnored
                backupData.mkdirs();
                File[] listOfFiles = currentData.listFiles();
                if (listOfFiles != null)
                    for (File file : listOfFiles) {
                        try {
                            FileChannel src = new FileInputStream(file.getPath()).getChannel();
                            FileChannel dst = new FileOutputStream(backupData + "/" + file.getName()).getChannel();
                            dst.transferFrom(src, 0, src.size());
                            src.close();
                            dst.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                Log.i(getPackageName(), "Images exported");
                Toast.makeText(this, R.string.db_exported, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void importDatabase() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            Log.i(getPackageName(), "READ_EXTERNAL_STORAGE permission isn't granted, requesting");
        } else {
            Log.i(getPackageName(), "Opening import dialog");
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setDataAndType(Uri.parse(Environment.getExternalStorageDirectory().getPath()), "*/*");
            startActivityForResult(intent, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, R.string.rw_permission_denied, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            if (Objects.requireNonNull(Objects.requireNonNull(data.getData()).getPath()).endsWith(".db")) {
                Log.i(getPackageName(), "Importing database...");
                String idArr[] = Objects.requireNonNull(data.getData().getPath()).split(":");
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
                Log.i(getPackageName(), "Database imported, importing images");
                File newData = new File(newDB.getPath().replace(".db", ""));
                File oldData = new File(dataFile, "/data/" + getPackageName() + "/files/");
                File[] listOfFiles = newData.listFiles();
                if (listOfFiles != null)
                    for (File file : listOfFiles) {
                        try {
                            FileChannel src = new FileInputStream(file.getPath()).getChannel();
                            FileChannel dst = new FileOutputStream(oldData + file.getName()).getChannel();
                            dst.transferFrom(src, 0, src.size());
                            src.close();
                            dst.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                Log.i(getPackageName(), "Images imported");
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
