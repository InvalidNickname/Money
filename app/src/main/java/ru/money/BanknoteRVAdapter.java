package ru.money;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

public class BanknoteRVAdapter extends RecyclerView.Adapter<BanknoteRVAdapter.CardViewHolder> {

    private final List<Banknote> banknoteList;
    private final Context context;

    BanknoteRVAdapter(List<Banknote> banknoteList, Context context) {
        this.banknoteList = banknoteList;
        this.context = context;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_card_banknote, viewGroup, false);
        return new CardViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder cardViewHolder, @SuppressLint("RecyclerView") final int i) {
        cardViewHolder.circulationTime.setText(banknoteList.get(i).circulationTime);
        cardViewHolder.country.setText(banknoteList.get(i).country);
        cardViewHolder.title.setText(banknoteList.get(i).title);
        if (!banknoteList.get(i).obversePath.equals("nothing")) {
            File file = context.getFileStreamPath(banknoteList.get(i).obversePath);
            Picasso.get().load(file).transform(new RoundCornerTransformation(20)).into(cardViewHolder.image);
        } else
            Picasso.get().load(R.drawable.example_banknote).into(cardViewHolder.image);
        cardViewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, BanknoteFullActivity.class);
                intent.putExtra("id", banknoteList.get(i).id);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return banknoteList.size();
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {

        final CardView cardView;
        final TextView circulationTime;
        final TextView title;
        final TextView country;
        final ImageView image;

        CardViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            circulationTime = itemView.findViewById(R.id.circulationTime);
            title = itemView.findViewById(R.id.title);
            image = itemView.findViewById(R.id.baseImage);
            country = itemView.findViewById(R.id.country);
        }
    }
}