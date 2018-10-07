package ru.money.list;

import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.Menu;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import ru.money.R;

import static ru.money.App.height;

class ModeManager {

    private static String mode = "normal";
    private Context context;
    private Menu menu;
    private float fabY;
    private Toolbar toolbar;
    private FloatingActionButton floatingActionButton;

    ModeManager(Context context) {
        this.context = context;
        toolbar = ((AppCompatActivity) context).findViewById(R.id.toolbar);
        floatingActionButton = ((AppCompatActivity) context).findViewById(R.id.fab);
    }

    static String getMode() {
        return mode;
    }

    void setMenu(Menu menu) {
        this.menu = menu;
    }

    void setNormalMode() {
        mode = "normal";
        menu.clear();
        ((AppCompatActivity) context).getMenuInflater().inflate(R.menu.main_menu, menu);
        toolbar.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
        ObjectAnimator animator = (ObjectAnimator) AnimatorInflater.loadAnimator(context, R.animator.vertical_slide);
        animator.setFloatValues(floatingActionButton.getY(), fabY);
        animator.setTarget(floatingActionButton);
        animator.start();
    }

    void setEditMode() {
        mode = "edit";
        menu.clear();
        ((AppCompatActivity) context).getMenuInflater().inflate(R.menu.edit_menu, menu);
        toolbar.setBackgroundColor(context.getResources().getColor(R.color.editMode));
        ObjectAnimator animator = (ObjectAnimator) AnimatorInflater.loadAnimator(context, R.animator.vertical_slide);
        fabY = floatingActionButton.getY();
        animator.setFloatValues(floatingActionButton.getY(), height);
        animator.setTarget(floatingActionButton);
        animator.start();
    }
}
