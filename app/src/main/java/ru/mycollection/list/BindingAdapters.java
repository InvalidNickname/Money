package ru.mycollection.list;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.BindingAdapter;
import androidx.preference.PreferenceManager;

import com.squareup.picasso.Picasso;

import ru.mycollection.R;
import ru.mycollection.dialog.CategoryDialogFragment;
import ru.mycollection.list.CategoryRVAdapter.OnAddListener;
import ru.mycollection.list.CategoryRVAdapter.OnDeleteListener;
import ru.mycollection.list.modemanager.Mode;
import ru.mycollection.list.modemanager.ModeManager;
import ru.mycollection.utils.RoundCornerTransformation;

import static ru.mycollection.App.LOG_TAG;

@SuppressWarnings("WeakerAccess")
public class BindingAdapters {

    // установка иконки категории
    @BindingAdapter("app:bind_icon_path")
    public static void loadImage(@NonNull ImageView view, String iconPath) {
        switch (iconPath) {
            case "nothing":
                Picasso.get().load(R.drawable.example_flag).into(view);
                break;
            case "no icon":
                Picasso.get().load(R.drawable.no_icon).into(view);
                break;
            default:
                Picasso.get().load(view.getContext().getFileStreamPath(iconPath)).transform(new RoundCornerTransformation(12)).into(view);
                break;
        }
    }

    // установка слушателей
    @BindingAdapter("app:bind_listener")
    public static void setListeners(ConstraintLayout layout, @NonNull final Category category) {
        final Context context = layout.getContext();
        final OnAddListener onAddListener = (OnAddListener) context;
        layout.setOnClickListener(v -> {
            if (ModeManager.getMode() == Mode.Normal || ModeManager.getMode() == Mode.Move)
                onAddListener.loadNewCategory(category.getId());
        });
        final OnDeleteListener onDeleteListener = (OnDeleteListener) context;
        layout.setOnLongClickListener(v -> {
            if (ModeManager.getMode() == Mode.Normal) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(context.getResources().getString(R.string.pick_action))
                        .setItems(new CharSequence[]{context.getString(R.string.delete_category_action), context.getString(R.string.edit_category_action)}, (dialog, which) -> {
                            switch (which) {
                                case 0:
                                    Log.i(LOG_TAG, "Opening delete dialog");
                                    AlertDialog.Builder builder2 = new AlertDialog.Builder(context);
                                    builder2.setTitle(String.format(context.getResources().getString(R.string.delete_country), category.getCategoryName()))
                                            .setMessage(R.string.delete_country_info)
                                            .setNegativeButton(R.string.cancel, (dialog2, id) -> dialog2.cancel())
                                            .setPositiveButton(R.string.ok, (dialog2, id) -> onDeleteListener.deleteCategory(category.getId()));
                                    AlertDialog alert = builder2.create();
                                    alert.show();
                                    ((TextView) alert.findViewById(android.R.id.message)).setTypeface(ResourcesCompat.getFont(context, R.font.abel));
                                    break;
                                case 1:
                                    Log.i(LOG_TAG, "Opening edit dialog");
                                    CategoryDialogFragment fragment = new CategoryDialogFragment();
                                    Bundle bundle = new Bundle();
                                    bundle.putInt("id", category.getId());
                                    fragment.setArguments(bundle);
                                    fragment.show(((AppCompatActivity) context).getSupportFragmentManager(), "edit_category");
                                    break;
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
            return false;
        });
    }

    // установка размера иконки сдвигом guideline
    @BindingAdapter("app:guideline_percent")
    public static void setGuideline(Guideline guideline, @NonNull Context context) {
        guideline.setGuidelinePercent(PreferenceManager.getDefaultSharedPreferences(context).getBoolean("icon_size", false) ? 0.25f : 0.2f);
    }
}
