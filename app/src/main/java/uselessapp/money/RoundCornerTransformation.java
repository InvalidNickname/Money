package uselessapp.money;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;

import com.squareup.picasso.Transformation;

class RoundCornerTransformation implements Transformation {

    private int corner;

    RoundCornerTransformation(int corner) {
        this.corner = corner;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        final Bitmap output = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);
        canvas.drawRoundRect(0, 0, source.getWidth(), source.getHeight(), source.getWidth() / corner, source.getWidth() / corner, paint);
        if (source != output)
            source.recycle();
        return output;
    }

    @Override
    public String key() {
        return "roundCorner";
    }
}
