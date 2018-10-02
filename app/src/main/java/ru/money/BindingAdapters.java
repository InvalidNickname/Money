package ru.money;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Objects;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.BindingAdapter;
import ru.money.CategoryRVAdapter.OnDeleteListener;

import static ru.money.ListActivity.mode;

public class BindingAdapters {

    // установка иконки категории
    @BindingAdapter({"bind:iconPath"})
    public static void loadImage(ImageView view, String iconPath) {
        if (!iconPath.equals("nothing")) {
            Picasso.get().load(view.getContext().getFileStreamPath(iconPath)).transform(new RoundCornerTransformation(12)).into(view);
        } else {
            Picasso.get().load(R.drawable.example_flag).into(view);
        }
    }

    // установка слушателей
    @SuppressLint("ClickableViewAccessibility")
    @BindingAdapter({"bind:listener"})
    public static void setListeners(final CardView layout, final Category category) {
        final Context context = layout.getContext();
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode.equals("normal")) {
                    Intent intent = new Intent(context, ListActivity.class);
                    intent.putExtra("parent", category.getId());
                    context.startActivity(intent);
                }
            }
        });
        final OnDeleteListener onDeleteListener = (OnDeleteListener) layout.getContext();
        layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mode.equals("normal")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(String.format(context.getResources().getString(R.string.delete_country), category.getCategoryName()))
                            .setMessage(R.string.delete_country_info)
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            })
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    onDeleteListener.deleteCategory(category.getId());
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                    ((TextView) Objects.requireNonNull(alert.getWindow()).findViewById(android.R.id.message)).setTypeface(ResourcesCompat.getFont(context, R.font.abel));
                } else if (mode.equals("edit")) {
                    layout.findViewById(R.id.divider).setVisibility(View.GONE);
                }
                return false;
            }
        });
    }

}
