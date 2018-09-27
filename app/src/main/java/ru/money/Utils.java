package ru.money;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

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
                try {
                    FileChannel src = new FileInputStream(file.getPath()).getChannel();
                    FileChannel dst = new FileOutputStream(destination + "/" + file.getName()).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
    }
}
