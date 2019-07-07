package ru.mycollection.explorer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

import ru.mycollection.R;

class ExplorerRVAdapter extends RecyclerView.Adapter<ExplorerRVAdapter.CardViewHolder> {

    private final List<ExplorerItem> explorerItems;
    private final OnClickOnDirectory onClickOnDirectory;

    ExplorerRVAdapter(List<ExplorerItem> explorerItems, Context context) {
        this.explorerItems = explorerItems;
        onClickOnDirectory = (OnClickOnDirectory) context;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_card_explorer, viewGroup, false);
        return new CardViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final CardViewHolder cardViewHolder, final int i) {
        if ((new File(explorerItems.get(i).getPath())).isDirectory()) {
            cardViewHolder.icon.setImageResource(R.drawable.ic_folder);
        } else {
            cardViewHolder.icon.setImageResource(R.drawable.ic_db);
        }
        cardViewHolder.text.setText(explorerItems.get(i).getName());
        cardViewHolder.text.setOnClickListener(v -> {
            File file = new File(explorerItems.get(i).getPath());
            onClickOnDirectory.browseTo(file);
        });
    }

    @Override
    public int getItemCount() {
        return explorerItems.size();
    }

    interface OnClickOnDirectory {
        void browseTo(File file);
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {
        final TextView text;
        final ConstraintLayout layout;
        final ImageView icon;

        CardViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            text = itemView.findViewById(R.id.text);
            layout = itemView.findViewById(R.id.layout);
        }
    }
}
