package ru.mycollection.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;

import androidx.annotation.NonNull;

import com.squareup.picasso.Transformation;

public class RoundCornerTransformation implements Transformation {

    private final int corner;

    public RoundCornerTransformation(int corner) {
        this.corner = corner;
    }

    @Override
    public Bitmap transform(@NonNull Bitmap source) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        Bitmap output = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        canvas.drawRoundRect(0, 0, source.getWidth(), source.getHeight(),
                source.getWidth() / (float) corner, source.getWidth() / (float) corner, paint);
        if (source != output) source.recycle();
        return output;
    }

    @NonNull
    @Override
    public String key() {
        return "roundCorner";
    }
}
