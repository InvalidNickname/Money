package ru.mycollection.list;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import ru.mycollection.R;
import ru.mycollection.databinding.LayoutCardCategoryBinding;

public class CategoryRVAdapter extends RecyclerView.Adapter<CategoryRVAdapter.CardViewHolder> {

    private final List<Category> cardList;

    CategoryRVAdapter(List<Category> cardList) {
        this.cardList = cardList;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        LayoutCardCategoryBinding binding = DataBindingUtil.inflate(inflater, R.layout.layout_card_category, parent, false);
        return new CardViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        holder.bind(cardList.get(position));
    }

    public List<Category> getList() {
        return cardList;
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }

    public interface OnDeleteListener {
        void deleteCategory(int id);
    }

    public interface OnAddListener {
        void loadNewCategory(int id);
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {

        final LayoutCardCategoryBinding binding;

        CardViewHolder(LayoutCardCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Category category) {
            binding.setCategory(category);
            binding.executePendingBindings();
        }
    }
}