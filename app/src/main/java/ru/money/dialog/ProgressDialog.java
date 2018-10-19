package ru.money.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import ru.money.R;

public class ProgressDialog extends DialogFragment {

    private ProgressBar progressBar;
    private TextView progressText;
    private int max;

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_progress, null));
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        // чтобы диалог нельзя было закрыть, случайно нажав вне него
        getDialog().setCanceledOnTouchOutside(false);
        progressBar = getDialog().findViewById(R.id.progressBar);
        progressText = getDialog().findViewById(R.id.textView);
    }

    public void setMax(int max) {
        this.max = max;
        progressBar.setMax(max);
    }

    public void setProgress(int i) {
        progressBar.setProgress(i);
        progressText.setText(String.format(getString(R.string.a_of_b), i, max));
    }
}
