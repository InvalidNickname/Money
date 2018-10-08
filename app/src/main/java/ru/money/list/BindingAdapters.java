package ru.money.list;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.BindingAdapter;
import ru.money.R;
import ru.money.list.CategoryRVAdapter.OnAddListener;
import ru.money.list.CategoryRVAdapter.OnDeleteListener;
import ru.money.utils.RoundCornerTransformation;

@SuppressWarnings("WeakerAccess")
public class BindingAdapters {

    // установка иконки категории
    @BindingAdapter({"app:bind_icon_path"})
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
    @BindingAdapter({"app:bind_listener"})
    public static void setListeners(ConstraintLayout layout, @NonNull final Category category) {
        final Context context = layout.getContext();
        final OnAddListener onAddListener = (OnAddListener) context;
        layout.setOnClickListener(v -> {
            if (ModeManager.getMode().equals("normal"))
                onAddListener.loadNewCategory(category.getId());
        });
        final OnDeleteListener onDeleteListener = (OnDeleteListener) context;
        layout.setOnLongClickListener(v -> {
            if (ModeManager.getMode().equals("normal")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(String.format(context.getResources().getString(R.string.delete_country), category.getCategoryName()))
                        .setMessage(R.string.delete_country_info)
                        .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.cancel())
                        .setPositiveButton(R.string.ok, (dialog, id) -> onDeleteListener.deleteCategory(category.getId()));
                AlertDialog alert = builder.create();
                alert.show();
                ((TextView) alert.findViewById(android.R.id.message)).setTypeface(ResourcesCompat.getFont(context, R.font.abel));
            }
            return false;
        });
    }

}
