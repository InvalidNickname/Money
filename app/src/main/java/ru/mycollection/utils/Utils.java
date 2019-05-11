package ru.mycollection.utils;

import android.Manifest;
import android.content.ContentUris;
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
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.Objects;

import ru.mycollection.R;

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
        File[] listOfFiles = source.listFiles();
        if (listOfFiles != null)
            for (File file : listOfFiles)
                copyFileToDirectory(file, new File(destination + "/" + file.getName()));
    }

    public static String getPath(final Context context, final Uri uri) {
        if (DocumentsContract.isDocumentUri(context, uri)) {
            if ("com.android.externalstorage.documents".equals(uri.getAuthority())) {
                final String[] split = DocumentsContract.getDocumentId(uri).split(":");
                if ("primary".equalsIgnoreCase(split[0]))
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                final String id = DocumentsContract.getDocumentId(uri);
                System.out.println(id);
                if (id.startsWith("raw:")) {
                    return id.replaceFirst("raw:", "");
                }
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) return getDataColumn(context, uri);
        else if ("file".equalsIgnoreCase(uri.getScheme())) return uri.getPath();
        return null;
    }

    private static String getDataColumn(Context context, Uri uri) {
        final String column = "_data";
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            String[] strings = cursor.getColumnNames();
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getColumnCount(); i++) {
                if (column.equals(strings[i])) {
                    final int column_index = cursor.getColumnIndexOrThrow(column);
                    String path = cursor.getString(column_index);
                    cursor.close();
                    return path;
                }
            }
        }
        return Objects.requireNonNull(uri.getPath()).replaceFirst("/root", "");
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

    public static void runLayoutAnimation(RecyclerView recyclerView) {
        Context context = recyclerView.getContext();
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(context, R.anim.list_animation);
        recyclerView.setLayoutAnimation(controller);
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }
}