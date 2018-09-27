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
import java.util.Date;
import java.util.Objects;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static ru.money.DBHelper.DATABASE_NAME;
import static ru.money.ListActivity.LOG_TAG;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOG_TAG, "SettingsActivity is created");
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
            Log.i(LOG_TAG, "WRITE_EXTERNAL_STORAGE permission isn't granted, requesting");
        } else {
            Log.i(LOG_TAG, "Exporting database...");
            File data = Environment.getDataDirectory();
            File externalStorage = Environment.getExternalStorageDirectory();
            if (externalStorage.canWrite()) {
                // текущее время - уникальное название для файлов БД
                Date date = new Date();
                long time = date.getTime();
                File backupDB = new File(externalStorage, "/Exported Databases/");
                File currentDB = new File(data, "/data/" + getPackageName() + "/databases/" + DATABASE_NAME);
                if (backupDB.mkdirs()) {
                    backupDB = new File(backupDB, time + ".db");
                    Utils.copyFileToDirectory(currentDB, backupDB);
                }
                Log.i(LOG_TAG, "Database exported, exporting images");
                File backupData = new File(externalStorage, "/Exported Databases/" + time);
                File currentData = new File(data, "/data/" + getPackageName() + "/files/");
                if (backupData.mkdirs())
                    Utils.copyFolderToDirectory(currentData, backupData);
                Log.i(LOG_TAG, "Images exported");
                Toast.makeText(this, R.string.db_exported, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void importDatabase() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            Log.i(LOG_TAG, "READ_EXTERNAL_STORAGE permission isn't granted, requesting");
        } else {
            Log.i(LOG_TAG, "Opening import dialog");
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
                    break;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            if (Objects.requireNonNull(Objects.requireNonNull(data.getData()).getPath()).endsWith(".db")) {
                Log.i(LOG_TAG, "Importing database...");
                String idArr[] = Objects.requireNonNull(data.getData().getPath()).split(":");
                File newDB = new File(idArr[idArr.length - 1]);
                File dataFile = Environment.getDataDirectory();
                File oldDB = new File(dataFile, "/data/" + getPackageName() + "/databases/" + DATABASE_NAME);
                Utils.copyFileToDirectory(newDB, oldDB);
                Log.i(LOG_TAG, "Database imported, importing images");
                File newData = new File(newDB.getPath().replace(".db", ""));
                File oldData = new File(dataFile, "/data/" + getPackageName() + "/files/");
                Utils.copyFolderToDirectory(newData, oldData);
                Log.i(LOG_TAG, "Images imported");
            } else {
                Log.i(LOG_TAG, "Invalid database");
                Toast.makeText(this, R.string.db_invalid, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            Log.i(LOG_TAG, "Back button on toolbar selected, finishing");
            finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }
}
