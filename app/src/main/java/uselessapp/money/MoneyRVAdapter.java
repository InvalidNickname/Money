package uselessapp.money;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class MoneyRVAdapter extends RecyclerView.Adapter<MoneyRVAdapter.CardViewHolder> {

    private List<MoneyCard> moneyCardList;

    MoneyRVAdapter(List<MoneyCard> moneyCardList) {
        this.moneyCardList = moneyCardList;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_card_money, viewGroup, false);
        return new CardViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder cardViewHolder, int i) {
        cardViewHolder.circulationTime.setText(moneyCardList.get(i).circulationTime);
        cardViewHolder.country.setText(moneyCardList.get(i).country);
        cardViewHolder.image.setImageResource(moneyCardList.get(i).imageID);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return moneyCardList.size();
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        TextView circulationTime;
        TextView title;
        TextView country;
        ImageView image;

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