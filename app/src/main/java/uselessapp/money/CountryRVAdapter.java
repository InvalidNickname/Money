package uselessapp.money;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class CountryRVAdapter extends RecyclerView.Adapter<CountryRVAdapter.CardViewHolder> {

    private Context context;
    private List<Country> cardList;

    CountryRVAdapter(List<Country> cardList, Context context) {
        this.cardList = cardList;
        this.context = context;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_card_country, viewGroup, false);
        return new CardViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder cardViewHolder, @SuppressLint("RecyclerView") final int i) {
        cardViewHolder.country.setText(cardList.get(i).country);
        cardViewHolder.count.setText(String.valueOf(cardList.get(i).count));
        Picasso.get().load(Uri.parse(cardList.get(i).flagPath)).into(cardViewHolder.flag);
        cardViewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, BanknoteListActivity.class);
                intent.putExtra("country", cardList.get(i).country);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {

        TextView country;
        ImageView flag;
        TextView count;
        CardView cardView;

        CardViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            country = itemView.findViewById(R.id.country);
            flag = itemView.findViewById(R.id.baseImage);
            count = itemView.findViewById(R.id.count);
        }
    }
}