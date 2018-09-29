package ru.money;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
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

import static android.content.Context.MODE_PRIVATE;
import static ru.money.ListActivity.LOG_TAG;

class Utils {

    static String saveReturnedImageInFile(Intent returnedImage, Context context, int newWidth) {
        Date date = new Date();
        String filename = date.getTime() + ".png";
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(Objects.requireNonNull(context).getContentResolver(), returnedImage.getData());
            bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newWidth * bitmap.getHeight() / bitmap.getWidth(), true);
            FileOutputStream out = context.openFileOutput(filename, MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filename;
    }

    static String saveReturnedImageInFile(Intent returnedImage, Context context) {
        Date date = new Date();
        String filename = date.getTime() + ".png";
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(Objects.requireNonNull(context).getContentResolver(), returnedImage.getData());
            FileOutputStream out = context.openFileOutput(filename, MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filename;
    }

    static void copyFileToDirectory(File source, File destination) {
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

    static void copyFolderToDirectory(File source, File destination) {
        File[] listOfFiles = source.listFiles();
        if (listOfFiles != null)
            for (File file : listOfFiles) {
                copyFileToDirectory(file, new File(destination + "/" + file.getName()));
            }
    }

    static String getPath(Context context, Uri uri) {
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

    static boolean checkPermission(Context context, String permission) {
        int permissionCheck = ContextCompat.checkSelfPermission(context, permission);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((AppCompatActivity) context, new String[]{permission}, 1);
            Log.i(LOG_TAG, permission + " permission isn't granted, requesting");
            return false;
        }
        return true;
    }

    static void deleteFromFiles(String name, Context context) {
        File categoryImage = new File(Environment.getDataDirectory(), "/data/" + context.getPackageName() + "/files/" + name);
        if (!categoryImage.delete())
            Log.i(LOG_TAG, "Failed deleting " + name);
    }
}
