package uselessapp.money;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class CountryRVAdapter extends RecyclerView.Adapter<CountryRVAdapter.CardViewHolder> {

    private List<CountryCard> cardList;

    CountryRVAdapter(List<CountryCard> cardList) {
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
        cardViewHolder.flag.setImageResource(cardList.get(i).imageID);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {

        TextView country;
        ImageView flag;

        CardViewHolder(View itemView) {
            super(itemView);
            country = itemView.findViewById(R.id.country);
            flag = itemView.findViewById(R.id.baseImage);
        }
    }
}