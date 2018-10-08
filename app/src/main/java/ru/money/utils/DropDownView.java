package ru.money.utils;

import android.animation.ObjectAnimator;
import android.view.View;
import android.widget.TextView;

public class DropDownView {

    public static void expand(final TextView v) {
        v.setVisibility(View.INVISIBLE);
        v.post(() -> {
            int targetHeight = v.getLineHeight() * v.getLineCount();
            ObjectAnimator animation = ObjectAnimator.ofInt(v, "height", 0, targetHeight);
            animation.setDuration((int) (targetHeight * 2 / v.getContext().getResources().getDisplayMetrics().density));
            animation.start();
            v.setVisibility(View.VISIBLE);
        });
    }

    public static void collapse(final TextView v) {
        v.post(() -> {
            int targetHeight = v.getLineHeight() * v.getLineCount();
            ObjectAnimator animation = ObjectAnimator.ofInt(v, "height", targetHeight, 0);
            animation.setDuration((int) (targetHeight * 2 / v.getContext().getResources().getDisplayMetrics().density));
            animation.start();
        });
    }
}
