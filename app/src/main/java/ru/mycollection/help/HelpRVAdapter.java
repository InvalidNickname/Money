package ru.mycollection.help;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ru.mycollection.R;
import ru.mycollection.utils.DropDownView;

public class HelpRVAdapter extends RecyclerView.Adapter<HelpRVAdapter.CardViewHolder> {

    private final List<HelpItem> helpItems;

    HelpRVAdapter(List<HelpItem> helpItems) {
        this.helpItems = helpItems;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_card_help, viewGroup, false);
        return new CardViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final CardViewHolder cardViewHolder, final int i) {
        final Context context = cardViewHolder.text.getContext();
        if (i == 0) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, context.getResources().getDisplayMetrics()), 0, 0);
            cardViewHolder.layout.setLayoutParams(lp);
        }
        cardViewHolder.text.setText(helpItems.get(i).getText());
        cardViewHolder.title.setText(helpItems.get(i).getTitle());
        cardViewHolder.title.setOnClickListener(v -> {
            if (cardViewHolder.text.getMeasuredHeight() == 0) {
                DropDownView.expand(cardViewHolder.text);
                ((TextView) v).setCompoundDrawablesWithIntrinsicBounds(null, null, context.getDrawable(R.drawable.ic_collapse), null);
            } else {
                DropDownView.collapse(cardViewHolder.text);
                ((TextView) v).setCompoundDrawablesWithIntrinsicBounds(null, null, context.getDrawable(R.drawable.ic_expand), null);
            }
        });
    }

    @Override
    public int getItemCount() {
        return helpItems.size();
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {

        final TextView title;
        final TextView text;
        final LinearLayout layout;

        CardViewHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.text);
            title = itemView.findViewById(R.id.title);
            layout = itemView.findViewById(R.id.layout);
        }
    }
}
