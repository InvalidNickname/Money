package ru.mycollection.utils.gestureimageview;

public class FlingAnimation implements Animation {

    private float velocityX;
    private float velocityY;

    private FlingAnimationListener listener;

    @Override
    public boolean update(GestureImageView view, long time) {
        float seconds = (float) time / 1000.0f;
        float dx = velocityX * seconds;
        float dy = velocityY * seconds;
        float factor = 0.95f;
        velocityX *= factor;
        velocityY *= factor;
        float threshold = 10;
        boolean active = (Math.abs(velocityX) > threshold && Math.abs(velocityY) > threshold);
        if (listener != null) {
            listener.onMove(dx, dy);
            if (!active) {
                listener.onComplete();
            }
        }
        return active;
    }

    void setVelocityX(float velocityX) {
        this.velocityX = velocityX;
    }

    void setVelocityY(float velocityY) {
        this.velocityY = velocityY;
    }

    void setListener(FlingAnimationListener listener) {
        this.listener = listener;
    }
}
