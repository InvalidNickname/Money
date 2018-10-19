package ru.money.settings;

import android.Manifest;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Date;

import androidx.appcompat.app.AppCompatActivity;
import ru.money.dialog.ProgressDialog;
import ru.money.utils.Utils;

import static ru.money.App.LOG_TAG;
import static ru.money.utils.DBHelper.DATABASE_NAME;
import static ru.money.utils.Utils.copyFileToDirectory;

class CopyTask extends AsyncTask<Void, Integer, Void> {

    private final WeakReference<Context> contextWeakReference;
    private ProgressDialog dialog;

    CopyTask(Context context) {
        this.contextWeakReference = new WeakReference<>(context);
    }

    @Override
    protected void onPreExecute() {
        Context context = contextWeakReference.get();
        dialog = new ProgressDialog();
        dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "progress");
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Context context = contextWeakReference.get();
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
                    if (!backupDB.exists()) copyFileToDirectory(currentDB, backupDB);
                    Log.i(LOG_TAG, "Database exported, exporting images");
                }
                File backupData = new File(externalStorage, "/Exported Databases/" + time);
                // создание папки с уникальным названием. Если она существует - закончить экспорт
                if (backupData.mkdirs()) {
                    File currentData = new File(data, "/data/" + context.getPackageName() + "/files/");
                    File[] listOfFiles = currentData.listFiles();
                    if (listOfFiles != null)
                        for (int i = 0; i < listOfFiles.length; i++) {
                            if (i == 0) publishProgress(i, listOfFiles.length);
                            else publishProgress(i);
                            copyFileToDirectory(listOfFiles[i], new File(backupData + "/" + listOfFiles[i].getName()));
                        }
                    Log.i(LOG_TAG, "Images exported");
                } else {
                    // TODO db error
                    return null;
                }
                // TODO db exported
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... integers) {
        super.onProgressUpdate();
        if (integers.length == 2) dialog.setMax(integers[1]);
        dialog.setProgress(integers[0]);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        dialog.dismiss();
    }
}
