package ru.mycollection.utils.gestureimageview;

public interface ZoomAnimationListener {

    void onZoom(float scale, float x, float y);

    void onComplete();

}
