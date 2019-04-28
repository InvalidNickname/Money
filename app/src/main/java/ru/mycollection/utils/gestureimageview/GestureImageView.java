package ru.mycollection.utils.gestureimageview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ViewGroup.LayoutParams;

import androidx.appcompat.widget.AppCompatImageView;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class GestureImageView extends AppCompatImageView {

    private static final String GLOBAL_NS = "http://schemas.android.com/apk/res/android";
    private static final String LOCAL_NS = "http://schemas.polites.com/android";
    private final Semaphore drawLock = new Semaphore(0);
    private boolean isLandscaped;
    private Animator animator;
    private Drawable drawable;
    private float x = 0, y = 0;
    private boolean layout = false;
    private float scaleAdjust = 1.0f;
    private float startingScale = -1.0f;
    private float maxScale = 5.0f;
    private float minScale = 0.75f;
    private float fitScaleHorizontal = 1.0f;
    private float fitScaleVertical = 1.0f;
    private float centerX;
    private float centerY;
    private Float startX, startY;
    private int resId = -1;
    private int displayHeight;
    private int displayWidth;
    private int deviceOrientation = -1;
    private GestureImageViewListener gestureImageViewListener;
    private GestureImageViewTouchListener gestureImageViewTouchListener;
    private OnTouchListener customOnTouchListener;
    private OnClickListener onClickListener;

    public GestureImageView(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs);
    }

    public GestureImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        String scaleType = attrs.getAttributeValue(GLOBAL_NS, "scaleType");
        if (scaleType == null || scaleType.trim().length() == 0) {
            setScaleType(ScaleType.CENTER_INSIDE);
        }
        String strStartX = attrs.getAttributeValue(LOCAL_NS, "start-x");
        String strStartY = attrs.getAttributeValue(LOCAL_NS, "start-y");
        if (strStartX != null && strStartX.trim().length() > 0) {
            startX = Float.parseFloat(strStartX);
        }
        if (strStartY != null && strStartY.trim().length() > 0) {
            startY = Float.parseFloat(strStartY);
        }
        setStartingScale(attrs.getAttributeFloatValue(LOCAL_NS, "start-scale", startingScale));
        setMinScale(attrs.getAttributeFloatValue(LOCAL_NS, "min-scale", minScale));
        setMaxScale(attrs.getAttributeFloatValue(LOCAL_NS, "max-scale", maxScale));
        initImage();
    }

    public GestureImageView(Context context) {
        super(context);
        setScaleType(ScaleType.CENTER_INSIDE);
        initImage();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (drawable != null) {
            int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                displayHeight = MeasureSpec.getSize(heightMeasureSpec);

                if (getLayoutParams().width == LayoutParams.WRAP_CONTENT) {
                    float ratio = (float) getImageWidth() / (float) getImageHeight();
                    displayWidth = Math.round((float) displayHeight * ratio);
                } else {
                    displayWidth = MeasureSpec.getSize(widthMeasureSpec);
                }
            } else {
                displayWidth = MeasureSpec.getSize(widthMeasureSpec);
                if (getLayoutParams().height == LayoutParams.WRAP_CONTENT) {
                    float ratio = (float) getImageHeight() / (float) getImageWidth();
                    displayHeight = Math.round((float) displayWidth * ratio);
                } else {
                    displayHeight = MeasureSpec.getSize(heightMeasureSpec);
                }
            }
        } else {
            displayHeight = MeasureSpec.getSize(heightMeasureSpec);
            displayWidth = MeasureSpec.getSize(widthMeasureSpec);
        }
        setMeasuredDimension(displayWidth, displayHeight);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed || !layout) {
            setupCanvas(displayWidth, displayHeight, getResources().getConfiguration().orientation);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupCanvas(int measuredWidth, int measuredHeight, int orientation) {
        if (deviceOrientation != orientation) {
            layout = false;
            deviceOrientation = orientation;
        }
        if (drawable != null && !layout) {
            int imageWidth = getImageWidth();
            int imageHeight = getImageHeight();
            int hWidth = Math.round(((float) imageWidth / 2.0f));
            int hHeight = Math.round(((float) imageHeight / 2.0f));
            measuredWidth -= (getPaddingLeft() + getPaddingRight());
            measuredHeight -= (getPaddingTop() + getPaddingBottom());
            computeCropScale(imageWidth, imageHeight, measuredWidth, measuredHeight);
            if (startingScale <= 0.0f) {
                computeStartingScale(imageWidth, imageHeight, measuredWidth, measuredHeight);
            }
            scaleAdjust = startingScale;
            this.centerX = (float) measuredWidth / 2.0f;
            this.centerY = (float) measuredHeight / 2.0f;
            if (startX == null) {
                x = centerX;
            } else {
                x = startX;
            }
            if (startY == null) {
                y = centerY;
            } else {
                y = startY;
            }
            gestureImageViewTouchListener = new GestureImageViewTouchListener(this, measuredWidth, measuredHeight);
            if (isLandscape()) {
                gestureImageViewTouchListener.setMinScale(minScale * fitScaleHorizontal);
            } else {
                gestureImageViewTouchListener.setMinScale(minScale * fitScaleVertical);
            }
            gestureImageViewTouchListener.setMaxScale(maxScale * startingScale);
            gestureImageViewTouchListener.setFitScaleHorizontal(fitScaleHorizontal);
            gestureImageViewTouchListener.setFitScaleVertical(fitScaleVertical);
            gestureImageViewTouchListener.setCanvasHeight(measuredHeight);
            gestureImageViewTouchListener.setOnClickListener(onClickListener);
            drawable.setBounds(-hWidth, -hHeight, hWidth, hHeight);
            super.setOnTouchListener((v, event) -> {
                if (customOnTouchListener != null) {
                    customOnTouchListener.onTouch(v, event);
                }
                return gestureImageViewTouchListener.onTouch(v, event);
            });
            layout = true;
        }
    }

    private void computeCropScale(int imageWidth, int imageHeight, int measuredWidth, int measuredHeight) {
        fitScaleHorizontal = (float) measuredWidth / (float) imageWidth;
        fitScaleVertical = (float) measuredHeight / (float) imageHeight;
    }

    private void computeStartingScale(int imageWidth, int imageHeight, int measuredWidth, int measuredHeight) {
        float wRatio = (float) imageWidth / (float) measuredWidth;
        float hRatio = (float) imageHeight / (float) measuredHeight;
        isLandscaped = wRatio > hRatio;
        if (wRatio > hRatio) {
            startingScale = fitScaleHorizontal;
        } else {
            startingScale = fitScaleVertical;
        }
    }

    private boolean isRecycled() {
        if (drawable instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            if (bitmap != null) {
                return bitmap.isRecycled();
            }
        }
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (layout) {
            if (drawable != null && !isRecycled()) {
                canvas.save();
                float scale = 1.0f;
                float adjustedScale = scale * scaleAdjust;
                canvas.translate(x, y);
                if (adjustedScale != 1.0f) {
                    canvas.scale(adjustedScale, adjustedScale);
                }
                drawable.draw(canvas);
                canvas.restore();
            }
            if (drawLock.availablePermits() <= 0) {
                drawLock.release();
            }
        }
    }

    public boolean waitForDraw(long timeout) throws InterruptedException {
        return drawLock.tryAcquire(timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void onAttachedToWindow() {
        animator = new Animator(this, "GestureImageViewAnimator");
        animator.start();
        if (drawable == null) {
            setImageResource(resId);
        }
        super.onAttachedToWindow();
    }

    public void animationStart(Animation animation) {
        if (animator != null) {
            animator.play(animation);
        }
    }

    public void animationStop() {
        if (animator != null) {
            animator.cancel();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (animator != null) {
            animator.finish();
        }
        super.onDetachedFromWindow();
    }

    private void initImage() {
        if (drawable != null) {
            drawable.setFilterBitmap(true);
        }
        if (!layout) {
            requestLayout();
            redraw();
        }
    }

    public void setImageBitmap(Bitmap image) {
        drawable = new BitmapDrawable(getResources(), image);
        initImage();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        this.drawable = drawable;
        initImage();
    }

    public void setImageResource(int id) {
        this.resId = id;
        setImageDrawable(getContext().getResources().getDrawable(id));
    }

    public int getScaledHeight() {
        return Math.round(getImageHeight() * getScale());
    }

    public int getImageWidth() {
        if (drawable != null) {
            return drawable.getIntrinsicWidth();
        }
        return 0;
    }

    public int getImageHeight() {
        if (drawable != null) {
            return drawable.getIntrinsicHeight();
        }
        return 0;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void redraw() {
        postInvalidate();
    }

    private void setMinScale(float min) {
        this.minScale = min;
        if (gestureImageViewTouchListener != null) {
            gestureImageViewTouchListener.setMinScale(min * fitScaleHorizontal);
        }
    }

    private void setMaxScale(float max) {
        this.maxScale = max;
        if (gestureImageViewTouchListener != null) {
            gestureImageViewTouchListener.setMaxScale(max * startingScale);
        }
    }

    public float getScale() {
        return scaleAdjust;
    }

    public void setScale(float scale) {
        scaleAdjust = scale;
    }

    public float getImageX() {
        return x;
    }

    public float getImageY() {
        return y;
    }

    public GestureImageViewListener getGestureImageViewListener() {
        return gestureImageViewListener;
    }

    @Override
    public Drawable getDrawable() {
        return drawable;
    }

    @Override
    public void setOnTouchListener(OnTouchListener l) {
        this.customOnTouchListener = l;
    }

    public float getCenterX() {
        return centerX;
    }

    public float getCenterY() {
        return centerY;
    }

    public boolean isLandscape() {
        return isLandscaped;
    }

    private void setStartingScale(float startingScale) {
        this.startingScale = startingScale;
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        this.onClickListener = l;
        if (gestureImageViewTouchListener != null) {
            gestureImageViewTouchListener.setOnClickListener(l);
        }
    }

}
