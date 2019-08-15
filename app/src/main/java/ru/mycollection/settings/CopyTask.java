package ru.mycollection.settings;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import ru.mycollection.dialog.ProgressDialogFragment;

import static ru.mycollection.utils.DBHelper.DATABASE_NAME;
import static ru.mycollection.utils.Utils.copyFileToDirectory;

class CopyTask extends AsyncTask<Void, Integer, Void> {

    private final WeakReference<Context> contextWeakReference;
    private final String source, destination, title, subtitle;
    private final Task task;
    private ProgressDialogFragment dialog;
    private OnPostExecute postExecute;

    CopyTask(Context context, String source, String destination, String title, String subtitle, Task task) {
        this.contextWeakReference = new WeakReference<>(context);
        this.source = source;
        this.destination = destination;
        this.title = title;
        this.subtitle = subtitle;
        this.task = task;
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    protected Void doInBackground(Void... voids) {
        File currentData;
        File[] files;
        switch (task) {
            case UNZIP:
                try {
                    byte[] buffer = new byte[1024];
                    ZipInputStream zis = new ZipInputStream(new FileInputStream(source));
                    // подсчёт размера архива
                    int length = 0;
                    ZipEntry zipEntry = zis.getNextEntry();
                    while (zipEntry != null) {
                        length++;
                        zipEntry = zis.getNextEntry();
                    }
                    zis.closeEntry();
                    zis.close();
                    zis = new ZipInputStream(new FileInputStream(source));
                    zipEntry = zis.getNextEntry();
                    int i = 0;
                    while (zipEntry != null) {
                        File dest = new File(destination, new File(zipEntry.getName()).getName());
                        dest.getParentFile().mkdirs();
                        if (!dest.exists()) dest.createNewFile();
                        FileOutputStream fos = new FileOutputStream(dest);
                        int len = zis.read(buffer);
                        while (len > 0) {
                            fos.write(buffer, 0, len);
                            len = zis.read(buffer);
                        }
                        fos.close();
                        zipEntry = zis.getNextEntry();
                        if (i == 0) publishProgress(i = 1, length);
                        else publishProgress(++i);
                    }
                    zis.closeEntry();
                    zis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case ZIP:
                try {
                    // создание файла архива
                    File dest = new File(destination);
                    if (!dest.exists()) dest.createNewFile();
                    // запись архива
                    ZipOutputStream zippedData = new ZipOutputStream(new FileOutputStream(dest));
                    currentData = new File(source);
                    if (currentData.isDirectory()) {
                        files = currentData.listFiles();
                        for (int i = 0; i < files.length; i++) {
                            zipFile(zippedData, files[i]);
                            if (i == 0) publishProgress(1, files.length);
                            else publishProgress(i + 1);
                        }
                    } else if (currentData.isFile()) {
                        zipFile(zippedData, currentData);
                    }
                    zippedData.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case COPY:
                File backupData = new File(destination);
                currentData = new File(source);
                files = currentData.listFiles();
                if (currentData.isDirectory()) {
                    for (int i = 0; i < files.length; i++) {
                        if (isCancelled()) {
                            return null;
                        } else {
                            copyFileToDirectory(files[i], new File(backupData + "/" + files[i].getName()));
                            if (i == 0) publishProgress(1, files.length);
                            else publishProgress(i + 1);
                        }
                    }
                } else if (currentData.isFile()) {
                    copyFileToDirectory(currentData, new File(backupData + "/" + currentData.getName()));
                }
                break;
        }
        return null;
    }

    private void zipFile(ZipOutputStream zippedData, File file) throws IOException {
        if (file.getName().equals(DATABASE_NAME) || file.getName().endsWith(".jpg")) {
            if (!file.getName().equals(DATABASE_NAME))
                zippedData.putNextEntry(new ZipEntry(file.getParentFile().getName() + "/" + file.getName()));
            else {
                zippedData.putNextEntry(new ZipEntry(file.getName()));
            }
            FileInputStream in = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int len = in.read(buffer);
            while (len >= 0) {
                zippedData.write(buffer, 0, len);
                len = in.read(buffer);
            }
            in.close();
        }
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
        if (postExecute != null) postExecute.doAction();
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        dialog.dismiss();
    }

    void setAction(OnPostExecute postExecute) {
        this.postExecute = postExecute;
    }

    enum Task {
        ZIP,
        UNZIP,
        COPY
    }

    interface OnPostExecute {
        void doAction();
    }
}
