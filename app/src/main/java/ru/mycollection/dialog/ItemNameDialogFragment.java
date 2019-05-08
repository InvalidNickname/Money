package ru.mycollection.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import java.util.Objects;

import ru.mycollection.R;

public class ItemNameDialogFragment extends DialogFragment {

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_item_name, null))
                .setPositiveButton(getResources().getString(R.string.ok), null)
                .setTitle(getResources().getString(R.string.item_name_title));
        return builder.create();
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
                    // с заглавной буквы, убирает пробелы в начале и конце
                    name = name.trim().replaceFirst(name.substring(0, 0), name.substring(0, 0).toUpperCase());
                    PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString("item_name", name).apply();
                    d.dismiss();
                } else {
                    editText.setHintTextColor(Color.RED);
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


}
