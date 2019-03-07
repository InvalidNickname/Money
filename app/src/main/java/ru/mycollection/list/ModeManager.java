package ru.mycollection.list;

import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.Menu;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import ru.mycollection.R;

import static ru.mycollection.App.width;

class ModeManager {

    private static String mode = "normal";
    private final Context context;
    private final Toolbar toolbar;
    private final FloatingActionButton floatingActionButton;
    private Menu menu;
    private float fabX;

    ModeManager(@NonNull Context context) {
        this.context = context;
        toolbar = ((AppCompatActivity) context).findViewById(R.id.toolbar);
        floatingActionButton = ((AppCompatActivity) context).findViewById(R.id.fab);
    }

    @NonNull
    static String getMode() {
        return mode;
    }

    void setMenu(Menu menu) {
        this.menu = menu;
        fabX = floatingActionButton.getX();
    }

    void setNormalMode() {
        mode = "normal";
        menu.clear();
        ((AppCompatActivity) context).getMenuInflater().inflate(R.menu.main_menu, menu);
        toolbar.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
        ObjectAnimator animator = (ObjectAnimator) AnimatorInflater.loadAnimator(context, R.animator.slide);
        animator.setFloatValues(floatingActionButton.getX(), fabX);
        animator.setTarget(floatingActionButton);
        animator.start();
    }

    void setEditMode() {
        mode = "edit";
        menu.clear();
        ((AppCompatActivity) context).getMenuInflater().inflate(R.menu.edit_menu, menu);
        toolbar.setBackgroundColor(context.getResources().getColor(R.color.editMode));
        ObjectAnimator animator = (ObjectAnimator) AnimatorInflater.loadAnimator(context, R.animator.slide);
        animator.setFloatValues(floatingActionButton.getX(), width);
        animator.setTarget(floatingActionButton);
        animator.start();
    }
}
