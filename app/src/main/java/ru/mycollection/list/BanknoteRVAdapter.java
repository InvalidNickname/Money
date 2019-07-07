package ru.mycollection.list;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

import ru.mycollection.R;
import ru.mycollection.banknote.BanknoteFullActivity;
import ru.mycollection.utils.RoundCornerTransformation;

public class BanknoteRVAdapter extends RecyclerView.Adapter<BanknoteRVAdapter.CardViewHolder> {

    private final List<Banknote> banknotes;

    BanknoteRVAdapter(List<Banknote> banknotes) {
        this.banknotes = banknotes;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_card_banknote, viewGroup, false);
        return new CardViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final CardViewHolder cardViewHolder, int i) {
        cardViewHolder.circulationTime.setText(banknotes.get(i).getCirculationTime());
        cardViewHolder.country.setText(banknotes.get(i).getCountry());
        cardViewHolder.title.setText(banknotes.get(i).getTitle());
        final Context context = cardViewHolder.circulationTime.getContext();
        if (!banknotes.get(i).getObversePath().equals("nothing")) {
            final Target target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    // если изображение вертикальное - повернуть на 90 градусов против часовой стрелки
                    Matrix matrix = new Matrix();
                    if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("rotate_vertical_image", true)
                            && bitmap.getHeight() > bitmap.getWidth())
                        matrix.postRotate(270);
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    cardViewHolder.image.setImageBitmap(bitmap);
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            };
            Picasso.get().load(context.getFileStreamPath(banknotes.get(i).getObversePath()))
                    .transform(new RoundCornerTransformation(20))
                    .into(target);
            cardViewHolder.image.setTag(target);
        } else Picasso.get().load(R.drawable.example_banknote).into(cardViewHolder.image);
        cardViewHolder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(context, BanknoteFullActivity.class);
            intent.putExtra("id", banknotes.get(i).getId());
            context.startActivity(intent);
        });
    }

    public List<Banknote> getList() {
        return banknotes;
    }

    @Override
    public int getItemCount() {
        return banknotes.size();
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {

        final CardView cardView;
        final TextView circulationTime;
        final TextView title;
        final TextView country;
        final ImageView image;

        CardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            circulationTime = itemView.findViewById(R.id.circulationTime);
            title = itemView.findViewById(R.id.title);
            image = itemView.findViewById(R.id.baseImage);
            country = itemView.findViewById(R.id.countryName);
        }
    }
}