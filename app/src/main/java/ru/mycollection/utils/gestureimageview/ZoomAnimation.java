package ru.mycollection.utils.gestureimageview;

import android.graphics.PointF;

public class ZoomAnimation implements Animation {

    private boolean firstFrame = true;

    private float touchX;
    private float touchY;

    private float zoom;

    private float startX;
    private float startY;
    private float startScale;

    private float xDiff;
    private float yDiff;
    private float scaleDiff;

    private long totalTime = 0;

    private ZoomAnimationListener zoomAnimationListener;

    @Override
    public boolean update(GestureImageView view, long time) {
        if (firstFrame) {
            firstFrame = false;
            startX = view.getImageX();
            startY = view.getImageY();
            startScale = view.getScale();
            scaleDiff = (zoom * startScale) - startScale;
            if (scaleDiff > 0) {
                VectorF vector = new VectorF();
                vector.setStart(new PointF(touchX, touchY));
                vector.setEnd(new PointF(startX, startY));
                vector.calculateAngle();
                float length = vector.calculateLength();
                vector.length = length * zoom;
                vector.calculateEndPoint();
                xDiff = vector.end.x - startX;
                yDiff = vector.end.y - startY;
            } else {
                xDiff = view.getCenterX() - startX;
                yDiff = view.getCenterY() - startY;
            }
        }
        totalTime += time;
        long animationLengthMS = 200;
        float ratio = (float) totalTime / (float) animationLengthMS;
        if (ratio < 1) {
            if (ratio > 0) {
                float newScale = (ratio * scaleDiff) + startScale;
                float newX = (ratio * xDiff) + startX;
                float newY = (ratio * yDiff) + startY;
                if (zoomAnimationListener != null) {
                    zoomAnimationListener.onZoom(newScale, newX, newY);
                }
            }
            return true;
        } else {
            float newScale = scaleDiff + startScale;
            float newX = xDiff + startX;
            float newY = yDiff + startY;
            if (zoomAnimationListener != null) {
                zoomAnimationListener.onZoom(newScale, newX, newY);
                zoomAnimationListener.onComplete();
            }
            return false;
        }
    }

    void reset() {
        firstFrame = true;
        totalTime = 0;
    }

    void setZoom(float zoom) {
        this.zoom = zoom;
    }

    void setTouchX(float touchX) {
        this.touchX = touchX;
    }

    void setTouchY(float touchY) {
        this.touchY = touchY;
    }

    void setZoomAnimationListener(ZoomAnimationListener zoomAnimationListener) {
        this.zoomAnimationListener = zoomAnimationListener;
    }
}
