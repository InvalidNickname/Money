package ru.mycollection.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

import ru.mycollection.R;

public class SearchDialogFragment extends DialogFragment {

    private OnSearchListener onSearchListener;

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_search, null))
                .setPositiveButton(getResources().getString(R.string.search), null)
                .setNegativeButton(R.string.cancel, (dialog, id) -> SearchDialogFragment.this.getDialog().cancel());
        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        onSearchListener = (OnSearchListener) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        final AlertDialog d = (AlertDialog) getDialog();
        if (d != null) {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                EditText editText = getDialog().findViewById(R.id.editText);
                String name = editText.getText().toString();
                if (!name.equals("")) {
                    if (((CheckBox) getDialog().findViewById(R.id.search_in_name)).isChecked()) {
                        if (((CheckBox) getDialog().findViewById(R.id.search_in_desc)).isChecked()) {
                            onSearchListener.searchForBanknote(name, 3); // поиск по названию и описанию
                            d.dismiss();
                        } else {
                            onSearchListener.searchForBanknote(name, 2); // поиск по названию
                            d.dismiss();
                        }
                    } else if (((CheckBox) getDialog().findViewById(R.id.search_in_desc)).isChecked()) {
                        onSearchListener.searchForBanknote(name, 1); // поиск по описанию
                        d.dismiss();
                    }
                } else {
                    TextInputLayout textInputLayout = getDialog().findViewById(R.id.nameInput);
                    textInputLayout.setError(getString(R.string.empty_search_string));
                }
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // чтобы диалог нельзя было закрыть, случайно нажав вне него
        getDialog().setCanceledOnTouchOutside(false);
    }

    public interface OnSearchListener {
        void searchForBanknote(String name, int searchMode);
    }
}
