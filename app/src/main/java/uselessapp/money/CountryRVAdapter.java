package uselessapp.money;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class CountryRVAdapter extends RecyclerView.Adapter<CountryRVAdapter.CardViewHolder> {

    private List<Country> cardList;

    CountryRVAdapter(List<Country> cardList) {
        this.cardList = cardList;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_card_country, viewGroup, false);
        return new CardViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder cardViewHolder, int i) {
        cardViewHolder.country.setText(cardList.get(i).country);
        cardViewHolder.count.setText(String.valueOf(cardList.get(i).count));
        Picasso.get().load(Uri.parse(cardList.get(i).flagPath)).into(cardViewHolder.flag);
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

        CardViewHolder(View itemView) {
            super(itemView);
            country = itemView.findViewById(R.id.country);
            flag = itemView.findViewById(R.id.baseImage);
            count = itemView.findViewById(R.id.count);
        }
    }
}