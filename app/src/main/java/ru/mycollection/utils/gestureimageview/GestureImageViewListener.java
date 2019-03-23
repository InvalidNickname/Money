package ru.mycollection.utils.gestureimageview;

public interface GestureImageViewListener {

    void onTouch(float x, float y);

    void onScale(float scale);

    void onPosition(float x, float y);

}
