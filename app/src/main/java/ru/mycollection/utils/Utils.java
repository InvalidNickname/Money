package ru.mycollection.utils;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;
import static ru.mycollection.App.LOG_TAG;
import static ru.mycollection.utils.DBHelper.DATABASE_NAME;

public class Utils {

    @NonNull
    public static String saveReturnedImageInFile(Intent returnedImage, @NonNull Context context, int newWidth) {
        Date date = new Date();
        String filename = date.getTime() + ".jpg";
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(Objects.requireNonNull(context).getContentResolver(), returnedImage.getData());
            bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newWidth * bitmap.getHeight() / bitmap.getWidth(), true);
            FileOutputStream out = context.openFileOutput(filename, MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filename;
    }

    public static void copyFileToDirectory(@NonNull File source, @NonNull File destination) {
        try {
            FileChannel src = new FileInputStream(source).getChannel();
            FileChannel dst = new FileOutputStream(destination).getChannel();
            dst.transferFrom(src, 0, src.size());
            src.close();
            dst.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void copyFolderToDirectory(File source, File destination) {
        File[] files = source.listFiles();
        if (files != null)
            for (File file : files)
                copyFileToDirectory(file, new File(destination + "/" + file.getName()));
    }

    public static boolean checkPermission(@NonNull Context context, @NonNull String permission) {
        int permissionCheck = ContextCompat.checkSelfPermission(context, permission);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((AppCompatActivity) context, new String[]{permission}, 1);
            Log.i(LOG_TAG, permission + " permission isn't granted, requesting");
            return false;
        }
        return true;
    }

    public static void deleteFromFiles(String name, Context context) {
        File categoryImage = new File(Environment.getDataDirectory(), "/data/" + context.getPackageName() + "/files/" + name);
        if (!categoryImage.delete()) Log.i(LOG_TAG, "Failed deleting " + name);
    }

    public static void updateFontScale(Context context) {
        Configuration configuration = context.getResources().getConfiguration();
        configuration.fontScale = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("text_size", false) ? 1.15f : 1;
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        metrics.scaledDensity = configuration.fontScale * metrics.density;
        context.getResources().updateConfiguration(configuration, metrics);
    }

    public static void backupDB(@NonNull Context context) {
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("backup", false)) {
            Log.i(LOG_TAG, "Backing up database");
            if (Utils.checkPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Log.i(LOG_TAG, "Exporting database...");
                File data = Environment.getDataDirectory();
                File externalStorage = Environment.getExternalStorageDirectory();
                if (externalStorage.canWrite()) {
                    File backupFolder = new File(externalStorage, "/Exported Databases/");
                    // создание папки /Exported Databases/, если её не существует
                    if (backupFolder.exists() || backupFolder.mkdirs()) {
                        File backupDB = new File(backupFolder, "auto_backup.db");
                        File currentDB = new File(data, "/data/" + context.getPackageName() + "/databases/" + DATABASE_NAME);
                        Utils.copyFileToDirectory(currentDB, backupDB);
                        Log.i(LOG_TAG, "Database exported, exporting images");
                    }
                    File backupData = new File(externalStorage, "/Exported Databases/auto_backup");
                    // создание папки с auto_backup. Если она существует - закончить экспорт
                    if (backupData.exists() || backupData.mkdirs()) {
                        File currentData = new File(data, "/data/" + context.getPackageName() + "/files/");
                        Utils.copyFolderToDirectory(currentData, backupData);
                        Log.i(LOG_TAG, "Images exported");
                    }
                }
            }
        }
    }
}
