package ru.mycollection.utils.gestureimageview;

public class MoveAnimation implements Animation {

    private boolean firstFrame = true;

    private float startX;
    private float startY;

    private float targetX;
    private float targetY;
    private long totalTime = 0;

    private MoveAnimationListener moveAnimationListener;

    @Override
    public boolean update(GestureImageView view, long time) {
        totalTime += time;

        if (firstFrame) {
            firstFrame = false;
            startX = view.getImageX();
            startY = view.getImageY();
        }

        long animationTimeMS = 100;
        if (totalTime < animationTimeMS) {

            float ratio = (float) totalTime / animationTimeMS;

            float newX = ((targetX - startX) * ratio) + startX;
            float newY = ((targetY - startY) * ratio) + startY;

            if (moveAnimationListener != null) {
                moveAnimationListener.onMove(newX, newY);
            }

            return true;
        } else {
            if (moveAnimationListener != null) {
                moveAnimationListener.onMove(targetX, targetY);
            }
        }

        return false;
    }

    void setMoveAnimationListener(MoveAnimationListener moveAnimationListener) {
        this.moveAnimationListener = moveAnimationListener;
    }
}
