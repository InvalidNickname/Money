package ru.mycollection.settings;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.lang.ref.WeakReference;

import ru.mycollection.dialog.ProgressDialogFragment;

import static ru.mycollection.utils.Utils.copyFileToDirectory;

class CopyTask extends AsyncTask<Void, Integer, Void> {

    private final WeakReference<Context> contextWeakReference;
    private final String source, destination, title, subtitle;
    private ProgressDialogFragment dialog;

    CopyTask(Context context, String source, String destination, String title, String subtitle) {
        this.contextWeakReference = new WeakReference<>(context);
        this.source = source;
        this.destination = destination;
        this.title = title;
        this.subtitle = subtitle;
    }

    @Override
    protected void onPreExecute() {
        Context context = contextWeakReference.get();
        dialog = new ProgressDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("subtitle", subtitle);
        dialog.setArguments(bundle);
        dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "progress");
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        File backupData = new File(destination);
        File currentData = new File(source);
        File[] listOfFiles = currentData.listFiles();
        if (listOfFiles != null)
            for (int i = 0; i < listOfFiles.length; i++) {
                if (isCancelled()) {
                    return null;
                } else {
                    if (i == 0) publishProgress(i, listOfFiles.length);
                    else publishProgress(i);
                    copyFileToDirectory(listOfFiles[i], new File(backupData + "/" + listOfFiles[i].getName()));
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

    @Override
    protected void onCancelled() {
        super.onCancelled();
        dialog.dismiss();
    }
}
