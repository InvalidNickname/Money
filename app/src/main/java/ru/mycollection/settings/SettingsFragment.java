package ru.mycollection.settings;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;

import java.io.File;
import java.util.Date;

import ru.mycollection.R;
import ru.mycollection.dialog.ItemNameDialogFragment;
import ru.mycollection.help.HelpActivity;
import ru.mycollection.utils.Utils;

import static android.app.Activity.RESULT_OK;
import static ru.mycollection.App.LOG_TAG;
import static ru.mycollection.utils.DBHelper.DATABASE_NAME;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static CopyTask copyTask;
    private Context context;

    // добавление настроек
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);
        PreferenceManager.setDefaultValues(context, R.xml.preferences, false);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void setPreferenceScreen(PreferenceScreen preferenceScreen) {
        if (preferenceScreen != null) removeExtraSpace(preferenceScreen);
        super.setPreferenceScreen(preferenceScreen);
    }

    private void removeExtraSpace(Preference preference) {
        preference.setIconSpaceReserved(false);
        if (preference instanceof PreferenceGroup) {
            for (int i = 0; i < ((PreferenceGroup) preference).getPreferenceCount(); i++)
                removeExtraSpace(((PreferenceGroup) preference).getPreference(i));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // слушатель нажатие на кнопку экспорта
        Preference export = findPreference("export");
        if (export != null) {
            export.setOnPreferenceClickListener(preference -> {
                exportDatabase();
                return true;
            });
        }
        // слушатель нажатия на кнопку импорта
        Preference importPref = findPreference("import");
        if (importPref != null) {
            importPref.setOnPreferenceClickListener(preference -> {
                importDatabase();
                return true;
            });
        }
        // слушатель нажатия на кнопку помощи
        Preference helpPref = findPreference("help");
        if (helpPref != null) {
            helpPref.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(context, HelpActivity.class);
                startActivity(intent);
                return true;
            });
        }
        // слушатель нажатия на кнопку изменения названия
        Preference editItemNamePref = findPreference("item_name");
        if (editItemNamePref != null) {
            editItemNamePref.setOnPreferenceClickListener(preference -> {
                ItemNameDialogFragment dialogFragment = new ItemNameDialogFragment();
                dialogFragment.setCancelable(false);
                dialogFragment.show(((AppCompatActivity) context).getSupportFragmentManager(), "item_name");
                return true;
            });
        }
        // установка текста версии
        String versionName = "unknown";
        try {
            versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Preference versionPref = findPreference("version");
        if (versionPref != null) {
            versionPref.setTitle(String.format(getResources().getString(R.string.version), versionName));
        }

        Preference editItemName = findPreference("item_name");
        if (editItemName != null) {
            editItemName.setSummary(String.format(getResources().getString(R.string.current_item_name), PreferenceManager.getDefaultSharedPreferences(context).getString("item_name", "")));
        }
        // запрет на overscroll, без него выглядит лучше
        getListView().setOverScrollMode(View.OVER_SCROLL_NEVER);
    }

    private void importDatabase() {
        if (Utils.checkPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Log.i(LOG_TAG, "Opening import dialog");
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setDataAndType(Uri.parse(Environment.getExternalStorageDirectory().getPath()), "*/*");
            startActivityForResult(intent, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, R.string.rw_permission_denied, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    void cancelTask() {
        if (copyTask != null && copyTask.getStatus() == AsyncTask.Status.RUNNING) {
            copyTask.cancel(true);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @NonNull String key) {
        switch (key) {
            case "text_size":
                // изменение размера шрифта
                Log.i(LOG_TAG, "Text size changed");
                Utils.updateFontScale(context);
            case "item_name":
            case "status_bar":
                ((AppCompatActivity) context).recreate();
                break;
        }
    }

    private void exportDatabase() {
        if (Utils.checkPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
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
                    File currentDB = new File(data, "/data/" + context.getPackageName() + "/databases/" + DATABASE_NAME);
                    if (!backupDB.exists()) Utils.copyFileToDirectory(currentDB, backupDB);
                    Log.i(LOG_TAG, "Database exported, exporting images");
                }
                File backupData = new File(externalStorage, "/Exported Databases/" + time);
                // создание папки с уникальным названием. Если она существует - закончить экспорт
                if (backupData.mkdirs()) {
                    File currentData = new File(data, "/data/" + context.getPackageName() + "/files/");
                    copyTask = new CopyTask(
                            getContext(),
                            currentData.getPath(),
                            backupData.getPath(),
                            getString(R.string.export_in_progress),
                            getString(R.string.export_in_progress_subtitle));
                    copyTask.execute();
                    Log.i(LOG_TAG, "Images exported");
                } else {
                    Toast.makeText(context, R.string.db_folder_already_exists, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // если файл не выбран - uri = null
        Uri uri = data != null ? data.getData() : null;
        if (uri != null) {
            String path = Utils.getPath(context, uri);
            if (requestCode == 0 && resultCode == RESULT_OK) {
                if (path != null && path.endsWith(".db")) {
                    Log.i(LOG_TAG, "Importing database...");
                    File newDB = new File(path);
                    File dataFile = Environment.getDataDirectory();
                    File dbFolder = new File(dataFile, "/data/" + context.getPackageName() + "/databases/");
                    // создание папки /databases/, если её не существует
                    if (dbFolder.exists() || dbFolder.mkdirs()) {
                        File oldDB = new File(dataFile, "/data/" + context.getPackageName() + "/databases/" + DATABASE_NAME);
                        Utils.copyFileToDirectory(newDB, oldDB);
                        Log.i(LOG_TAG, "Database imported, importing images");
                    }
                    File oldData = new File(dataFile, "/data/" + context.getPackageName() + "/files/");
                    // создание папки /files/, если её не существует
                    if (oldData.exists() || oldData.mkdirs()) {
                        File newData = new File(path.substring(0, path.length() - 3));
                        copyTask = new CopyTask(
                                getContext(),
                                newData.getPath(),
                                oldData.getPath(),
                                getString(R.string.import_in_progress),
                                getString(R.string.import_in_progress_subtitle));
                        copyTask.execute();
                        Log.i(LOG_TAG, "Images imported");
                    }
                } else {
                    Log.i(LOG_TAG, "Invalid database");
                    Toast.makeText(context, R.string.db_invalid, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
