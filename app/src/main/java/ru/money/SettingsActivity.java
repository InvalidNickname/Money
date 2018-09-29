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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import static ru.money.DBHelper.DATABASE_NAME;
import static ru.money.ListActivity.LOG_TAG;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOG_TAG, "SettingsActivity is created");
        setContentView(R.layout.activity_settings);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
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
        if (Utils.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Log.i(LOG_TAG, "Exporting database...");
            File data = Environment.getDataDirectory();
            File externalStorage = Environment.getExternalStorageDirectory();
            if (externalStorage.canWrite()) {
                // текущее время - уникальное название для файлов БД
                long time = (new Date()).getTime();
                File backupFolder = new File(externalStorage, "/Exported Databases/");
                // создание папки /Exported Databases/, если её не существует
                if (backupFolder.exists() || backupFolder.mkdirs()) {
                    File backupDB = new File(backupFolder, time + ".db");
                    File currentDB = new File(data, "/data/" + getPackageName() + "/databases/" + DATABASE_NAME);
                    if (!backupDB.exists())
                        Utils.copyFileToDirectory(currentDB, backupDB);
                    Log.i(LOG_TAG, "Database exported, exporting images");
                }
                File backupData = new File(externalStorage, "/Exported Databases/" + time);
                // создание папки с уникальным названием. Если она существует - закончить экспорт
                if (backupData.mkdirs()) {
                    File currentData = new File(data, "/data/" + getPackageName() + "/files/");
                    Utils.copyFolderToDirectory(currentData, backupData);
                    Log.i(LOG_TAG, "Images exported");
                } else {
                    Toast.makeText(this, R.string.db_folder_already_exists, Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(this, R.string.db_exported, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void importDatabase() {
        if (Utils.checkPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
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
                    Toast.makeText(this, R.string.rw_permission_denied, Toast.LENGTH_LONG).show();
                    break;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // если файл не выбран - uri = null
        Uri uri = data != null ? data.getData() : null;
        if (uri != null) {
            String path = Utils.getPath(this, uri) == null ? uri.getPath() : Utils.getPath(this, uri);
            if (requestCode == 0 && resultCode == RESULT_OK) {
                if (path != null && path.endsWith(".db")) {
                    Log.i(LOG_TAG, "Importing database...");
                    File newDB = new File(path);
                    File dataFile = Environment.getDataDirectory();
                    File dbFolder = new File(dataFile, "/data/" + getPackageName() + "/databases/");
                    // создание папки /databases/, если её не существует
                    if (dbFolder.exists() || dbFolder.mkdirs()) {
                        File oldDB = new File(dataFile, "/data/" + getPackageName() + "/databases/" + DATABASE_NAME);
                        Utils.copyFileToDirectory(newDB, oldDB);
                        Log.i(LOG_TAG, "Database imported, importing images");
                    }
                    File oldData = new File(dataFile, "/data/" + getPackageName() + "/files/");
                    // создание папки /files/, если её не существует
                    if (oldData.exists() || oldData.mkdirs()) {
                        File newData = new File(path.substring(0, path.length() - 3));
                        Utils.copyFolderToDirectory(newData, oldData);
                        Log.i(LOG_TAG, "Images imported");
                    }
                } else {
                    Log.i(LOG_TAG, "Invalid database");
                    Toast.makeText(this, R.string.db_invalid, Toast.LENGTH_SHORT).show();
                }
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
