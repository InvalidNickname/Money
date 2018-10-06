package ru.money.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import static android.content.Context.MODE_PRIVATE;
import static ru.money.App.LOG_TAG;

public class Utils {

    public static String saveReturnedImageInFile(Intent returnedImage, Context context, int newWidth) {
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

    public static void copyFileToDirectory(File source, File destination) {
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

    public static void copyFolderToDirectory(File source, File destination) {
        File[] listOfFiles = source.listFiles();
        if (listOfFiles != null)
            for (File file : listOfFiles) {
                copyFileToDirectory(file, new File(destination + "/" + file.getName()));
            }
    }

    public static String getPath(Context context, Uri uri) {
        if (DocumentsContract.isDocumentUri(context, uri)) {
            if (Objects.equals(uri.getAuthority(), "com.android.externalstorage.documents")) {
                String idArr[] = DocumentsContract.getDocumentId(uri).split(":");
                if (idArr.length == 2) {
                    String type = idArr[0];
                    String realDocId = idArr[1];
                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + realDocId;
                    }
                }
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};
            Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
            try {
                if (cursor != null) {
                    int column_index = cursor.getColumnIndexOrThrow("_data");
                    if (cursor.moveToFirst()) {
                        String string = cursor.getString(column_index);
                        cursor.close();
                        return string;
                    }
                }
            } catch (Exception ignored) {
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static boolean checkPermission(Context context, String permission) {
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
        if (!categoryImage.delete())
            Log.i(LOG_TAG, "Failed deleting " + name);
    }

    public static void changeFontScale(boolean isChecked, Context context) {
        Configuration configuration = context.getResources().getConfiguration();
        configuration.fontScale = isChecked ? 1.15f : 1;
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        metrics.scaledDensity = configuration.fontScale * metrics.density;
        context.getResources().updateConfiguration(configuration, metrics);
    }

    public static void updateFontScale(Context context) {
        Configuration configuration = context.getResources().getConfiguration();
        configuration.fontScale = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("text_size", false) ? 1.15f : 1;
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        metrics.scaledDensity = configuration.fontScale * metrics.density;
        context.getResources().updateConfiguration(configuration, metrics);
    }
}
