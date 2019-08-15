package ru.mycollection.settings;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import ru.mycollection.explorer.ExplorerActivity;
import ru.mycollection.help.HelpActivity;
import ru.mycollection.utils.Utils;

import static android.app.Activity.RESULT_OK;
import static ru.mycollection.App.LOG_TAG;
import static ru.mycollection.utils.DBHelper.DATABASE_NAME;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static CopyTask copyTask;
    private Context context;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void delete(File file) {
        if (file.isDirectory()) {
            if (file.list().length == 0) {
                file.delete();
            } else {
                for (String temp : file.list()) {
                    File fileDelete = new File(file, temp);
                    delete(fileDelete);
                }
                if (file.list().length == 0) {
                    file.delete();
                }
            }
        } else {
            file.delete();
        }
    }

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
            //Log.i(LOG_TAG, "Opening import dialog");
            //Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            //intent.setDataAndType(Uri.parse(Environment.getExternalStorageDirectory().getPath()), "*/*");
            //startActivityForResult(intent, 0);

            Intent intent = new Intent(getActivity(), ExplorerActivity.class);
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
                // создание архива
                File backupData = new File(externalStorage, "/Exported Databases/" + time + ".cdb");
                File backupFolder = new File(externalStorage, "/Exported Databases/");
                File currentData = new File(data, "/data/" + context.getPackageName() + "/files/");
                // создание папки /Exported Databases/, если её не существует
                if (backupFolder.exists() || backupFolder.mkdirs()) {
                    File currentDB = new File(data, "/data/" + context.getPackageName() + "/databases/" + DATABASE_NAME);
                    Utils.copyFileToDirectory(currentDB, currentData);
                    copyTask = new CopyTask(
                            getContext(),
                            currentData.getPath(),
                            backupData.getPath(),
                            getString(R.string.export_in_progress),
                            getString(R.string.export_in_progress_subtitle),
                            CopyTask.Task.ZIP);
                    copyTask.execute();
                    Log.i(LOG_TAG, "Images exported");
                }
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            String path = data.getStringExtra("path");
            File dataFile = Environment.getDataDirectory();
            File dbFolder = new File(dataFile, "/data/" + context.getPackageName() + "/databases/");
            dbFolder.mkdirs();
            File oldData = new File(dataFile, "/data/" + context.getPackageName() + "/files/");
            delete(oldData);
            oldData.mkdirs();
            if (path != null && path.endsWith(".db")) {
                Log.i(LOG_TAG, "Importing database...");
                Utils.copyFileToDirectory(new File(path), new File(dbFolder, DATABASE_NAME));
                Log.i(LOG_TAG, "Database imported, importing images");
                File newData = new File(path.substring(0, path.length() - 3));
                copyTask = new CopyTask(
                        getContext(),
                        newData.getPath(),
                        oldData.getPath(),
                        getString(R.string.import_in_progress),
                        getString(R.string.import_in_progress_subtitle),
                        CopyTask.Task.COPY);
                copyTask.execute();
                Log.i(LOG_TAG, "Images imported");
            } else if (path != null && path.endsWith(".cdb")) {
                Log.i(LOG_TAG, "Importing database...");
                copyTask = new CopyTask(
                        getContext(),
                        path,
                        oldData.getPath(),
                        getString(R.string.import_in_progress),
                        getString(R.string.import_in_progress_subtitle),
                        CopyTask.Task.UNZIP);
                copyTask.setAction(() -> Utils.copyFileToDirectory(new File(oldData, DATABASE_NAME), new File(dbFolder, DATABASE_NAME)));
                copyTask.execute();
                Log.i(LOG_TAG, "Images imported");
            }
        }
    }
}
