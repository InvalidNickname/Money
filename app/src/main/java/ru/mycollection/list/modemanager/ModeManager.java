package ru.mycollection.list.modemanager;

import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.Menu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import ru.mycollection.R;

import static ru.mycollection.App.width;

public class ModeManager {

    private static Mode mode = Mode.Normal;
    private final Context context;
    private final Toolbar toolbar;
    private final FloatingActionButton floatingActionButton;
    private Menu menu;
    private float fabX;

    public ModeManager(@NonNull Context context) {
        this.context = context;
        toolbar = ((AppCompatActivity) context).findViewById(R.id.toolbar);
        floatingActionButton = ((AppCompatActivity) context).findViewById(R.id.fab);
    }

    @NonNull
    public static Mode getMode() {
        return mode;
    }

    public void setMode(Mode newMode) {
        ObjectAnimator animator = (ObjectAnimator) AnimatorInflater.loadAnimator(context, R.animator.slide);
        mode = newMode;
        menu.clear();
        switch (newMode) {
            case Normal:
                ((AppCompatActivity) context).getMenuInflater().inflate(R.menu.main_menu, menu);
                toolbar.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
                animator.setFloatValues(floatingActionButton.getX(), fabX);
                break;
            case Edit:
                ((AppCompatActivity) context).getMenuInflater().inflate(R.menu.edit_menu, menu);
                toolbar.setBackgroundColor(context.getResources().getColor(R.color.editMode));
                animator.setFloatValues(floatingActionButton.getX(), width);
                break;
            case Move:
                ((AppCompatActivity) context).getMenuInflater().inflate(R.menu.move_menu, menu);
                toolbar.setBackgroundColor(context.getResources().getColor(R.color.moveMode));
                animator.setFloatValues(floatingActionButton.getX(), width);
                break;
            case Search:
                ((AppCompatActivity) context).getMenuInflater().inflate(R.menu.search_menu, menu);
                toolbar.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
                animator.setFloatValues(floatingActionButton.getX(), width);
                break;
        }
        animator.setTarget(floatingActionButton);
        animator.start();
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
        fabX = floatingActionButton.getX();
    }
}
