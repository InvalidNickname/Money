package ru.mycollection.utils.gestureimageview;

import android.graphics.PointF;
import android.view.MotionEvent;

class VectorF {

    final PointF end = new PointF();
    private final PointF start = new PointF();
    float length;
    private float angle;

    void calculateEndPoint() {
        end.x = (float) Math.cos(angle) * length + start.x;
        end.y = (float) Math.sin(angle) * length + start.y;
    }

    void setStart(PointF p) {
        this.start.x = p.x;
        this.start.y = p.y;
    }

    void setEnd(PointF p) {
        this.end.x = p.x;
        this.end.y = p.y;
    }

    void set(MotionEvent event) {
        this.start.x = event.getX(0);
        this.start.y = event.getY(0);
        this.end.x = event.getX(1);
        this.end.y = event.getY(1);
    }

    float calculateLength() {
        length = MathUtils.distance(start, end);
        return length;
    }

    void calculateAngle() {
        angle = MathUtils.angle(start, end);
    }


}
