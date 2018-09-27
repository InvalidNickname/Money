package ru.money;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import static ru.money.ListActivity.mode;

public class CategoryRVAdapter extends RecyclerView.Adapter<CategoryRVAdapter.CardViewHolder> {

    private final Context context;
    private final List<Category> cardList;
    private OnDeleteListener onDeleteListener;

    CategoryRVAdapter(List<Category> cardList, Context context) {
        this.cardList = cardList;
        this.context = context;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_card_category, viewGroup, false);
        onDeleteListener = (OnDeleteListener) context;
        return new CardViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder cardViewHolder, @SuppressLint("RecyclerView") final int i) {
        cardViewHolder.country.setText(cardList.get(i).categoryName);
        cardViewHolder.count.setText(String.valueOf(cardList.get(i).count));
        if (!cardList.get(i).imagePath.equals("nothing")) {
            File file = context.getFileStreamPath(cardList.get(i).imagePath);
            Picasso.get().load(file).transform(new RoundCornerTransformation(12)).into(cardViewHolder.image);
        } else
            Picasso.get().load(R.drawable.example_flag).into(cardViewHolder.image);
        cardViewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode.equals("normal")) {
                    Intent intent = new Intent(context, ListActivity.class);
                    intent.putExtra("parent", cardList.get(i).id);
                    context.startActivity(intent);
                }
            }
        });
        cardViewHolder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mode.equals("normal")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(String.format(context.getResources().getString(R.string.delete_country), cardList.get(i).categoryName))
                            .setMessage(R.string.delete_country_info)
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            })
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    onDeleteListener.deleteCategory(cardList.get(i).id);
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
                return false;
            }
        });
    }

    List<Category> getCardList() {
        return cardList;
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }

    public interface OnDeleteListener {
        void deleteCategory(int id);
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {

        final TextView country;
        final ImageView image;
        final TextView count;
        final ConstraintLayout cardView;

        CardViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.categoryCard);
            country = itemView.findViewById(R.id.categoryName);
            image = itemView.findViewById(R.id.baseImage);
            count = itemView.findViewById(R.id.count);
        }
    }
}