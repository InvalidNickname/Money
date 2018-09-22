package uselessapp.money;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
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

public class CountryRVAdapter extends RecyclerView.Adapter<CountryRVAdapter.CardViewHolder> {

    private OnDeleteListener onDeleteListener;
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
        onDeleteListener = (OnDeleteListener) context;
        return new CardViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder cardViewHolder, @SuppressLint("RecyclerView") final int i) {
        cardViewHolder.country.setText(cardList.get(i).country);
        cardViewHolder.count.setText(String.valueOf(cardList.get(i).count));
        if (!cardList.get(i).flagPath.equals("nothing")) {
            File file = context.getFileStreamPath(cardList.get(i).flagPath);
            Picasso.get().load(file).transform(new RoundCornerTransformation(12)).into(cardViewHolder.flag);
        } else
            Picasso.get().load(R.drawable.example_flag).into(cardViewHolder.flag);
        cardViewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, BanknoteListActivity.class);
                intent.putExtra("country", cardList.get(i).country);
                context.startActivity(intent);
            }
        });
        cardViewHolder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(String.format(context.getResources().getString(R.string.delete_country), cardList.get(i).country))
                        .setMessage(R.string.delete_country_info)
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                onDeleteListener.deleteCountry(i);
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                return false;
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

    public interface OnDeleteListener {
        void deleteCountry(int id);
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