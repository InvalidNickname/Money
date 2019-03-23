package ru.mycollection.utils.gestureimageview;

import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

public class FlingListener extends SimpleOnGestureListener {

    private float velocityX;
    private float velocityY;

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        return true;
    }

    float getVelocityX() {
        return velocityX;
    }

    float getVelocityY() {
        return velocityY;
    }
}
