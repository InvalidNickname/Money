package ru.money.settings;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;
import java.util.Date;
import java.util.Objects;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroupAdapter;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceViewHolder;
import androidx.recyclerview.widget.RecyclerView;
import ru.money.R;
import ru.money.help.HelpActivity;
import ru.money.utils.Utils;

import static android.app.Activity.RESULT_OK;
import static ru.money.App.LOG_TAG;
import static ru.money.utils.DBHelper.DATABASE_NAME;

@SuppressWarnings("WeakerAccess")
public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    // добавление настроек
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);
        PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);
    }

    // убирает отступ под иконку во всем списке
    @Override
    protected RecyclerView.Adapter onCreateAdapter(PreferenceScreen preferenceScreen) {
        return new PreferenceGroupAdapter(preferenceScreen) {
            @SuppressLint("RestrictedApi")
            @Override
            public void onBindViewHolder(PreferenceViewHolder holder, int position) {
                super.onBindViewHolder(holder, position);
                if (getItem(position) instanceof PreferenceCategory)
                    setZeroPaddingToLayoutChildren(holder.itemView);
                else
                    holder.itemView.findViewById(R.id.icon_frame).setVisibility(getItem(position).getIcon() == null ? View.GONE : View.VISIBLE);
            }
        };
    }

    private void setZeroPaddingToLayoutChildren(View view) {
        if (!(view instanceof ViewGroup))
            return;
        ViewGroup viewGroup = (ViewGroup) view;
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            setZeroPaddingToLayoutChildren(viewGroup.getChildAt(i));
            viewGroup.setPaddingRelative(0, viewGroup.getPaddingTop(), viewGroup.getPaddingEnd(), viewGroup.getPaddingBottom());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // слушатель нажатие на кнопку экспорта
        Preference export = findPreference("export");
        export.setOnPreferenceClickListener(preference -> {
            exportDatabase();
            return true;
        });
        // слушатель нажатия на кнопку импорта
        Preference importPref = findPreference("import");
        importPref.setOnPreferenceClickListener(preference -> {
            importDatabase();
            return true;
        });
        // слушатель нажатия на кнопку помощи
        Preference helpPref = findPreference("help");
        helpPref.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(getActivity(), HelpActivity.class);
            startActivity(intent);
            return true;
        });
        // запрет на overscroll, без него выглядит лучше
        getListView().setOverScrollMode(View.OVER_SCROLL_NEVER);
    }

    private void exportDatabase() {
        if (Utils.checkPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
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
                    File currentDB = new File(data, "/data/" + getActivity().getPackageName() + "/databases/" + DATABASE_NAME);
                    if (!backupDB.exists())
                        Utils.copyFileToDirectory(currentDB, backupDB);
                    Log.i(LOG_TAG, "Database exported, exporting images");
                }
                File backupData = new File(externalStorage, "/Exported Databases/" + time);
                // создание папки с уникальным названием. Если она существует - закончить экспорт
                if (backupData.mkdirs()) {
                    File currentData = new File(data, "/data/" + getActivity().getPackageName() + "/files/");
                    Utils.copyFolderToDirectory(currentData, backupData);
                    Log.i(LOG_TAG, "Images exported");
                } else {
                    Toast.makeText(getActivity(), R.string.db_folder_already_exists, Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(getActivity(), R.string.db_exported, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void importDatabase() {
        if (Utils.checkPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
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
                    Toast.makeText(getActivity(), R.string.rw_permission_denied, Toast.LENGTH_LONG).show();
                    break;
                }
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("text_size")) {
            // изменение размера шрифта
            Log.i(LOG_TAG, "Text size changed");
            Utils.updateFontScale(getActivity());
            // перезапуск активити для изменения размера шрифта в ней
            getActivity().recreate();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // если файл не выбран - uri = null
        Uri uri = data != null ? data.getData() : null;
        if (uri != null) {
            String path = Utils.getPath(getActivity(), uri) == null ? Objects.requireNonNull(uri.getPath()).replaceFirst("/root", "") : Utils.getPath(getActivity(), uri);
            if (requestCode == 0 && resultCode == RESULT_OK) {
                if (path != null && path.endsWith(".db")) {
                    Log.i(LOG_TAG, "Importing database...");
                    File newDB = new File(path);
                    File dataFile = Environment.getDataDirectory();
                    File dbFolder = new File(dataFile, "/data/" + getActivity().getPackageName() + "/databases/");
                    // создание папки /databases/, если её не существует
                    if (dbFolder.exists() || dbFolder.mkdirs()) {
                        File oldDB = new File(dataFile, "/data/" + getActivity().getPackageName() + "/databases/" + DATABASE_NAME);
                        Utils.copyFileToDirectory(newDB, oldDB);
                        Log.i(LOG_TAG, "Database imported, importing images");
                    }
                    File oldData = new File(dataFile, "/data/" + getActivity().getPackageName() + "/files/");
                    // создание папки /files/, если её не существует
                    if (oldData.exists() || oldData.mkdirs()) {
                        File newData = new File(path.substring(0, path.length() - 3));
                        Utils.copyFolderToDirectory(newData, oldData);
                        Log.i(LOG_TAG, "Images imported");
                    }
                } else {
                    Log.i(LOG_TAG, "Invalid database");
                    Toast.makeText(getActivity(), R.string.db_invalid, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}
