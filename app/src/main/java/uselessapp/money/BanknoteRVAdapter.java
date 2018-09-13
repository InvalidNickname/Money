package uselessapp.money;

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

public class BanknoteRVAdapter extends RecyclerView.Adapter<BanknoteRVAdapter.CardViewHolder> {

    private List<Banknote> banknoteList;

    BanknoteRVAdapter(List<Banknote> banknoteList) {
        this.banknoteList = banknoteList;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_card_banknote, viewGroup, false);
        return new CardViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder cardViewHolder, int i) {
        cardViewHolder.circulationTime.setText(banknoteList.get(i).circulationTime);
        cardViewHolder.country.setText(banknoteList.get(i).country);
        cardViewHolder.title.setText(banknoteList.get(i).title);
        Picasso.get().load(Uri.parse(banknoteList.get(i).obversePath)).into(cardViewHolder.image);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return banknoteList.size();
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {

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