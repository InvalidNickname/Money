package uselessapp.money;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class NewBanknoteDialogFragment extends DialogFragment {

    OnAddListener onAddListener;
    String selectedObverse, selectedReverse;

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_add_banknote, null))
                .setTitle(getResources().getString(R.string.add_new_banknote))
                .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        EditText editName = getDialog().findViewById(R.id.editName);
                        String name = editName.getText().toString();
                        EditText editTime = getDialog().findViewById(R.id.editTime);
                        String time = editTime.getText().toString();
                        EditText editDescription = getDialog().findViewById(R.id.editDescription);
                        String description = editDescription.getText().toString();
                        onAddListener.addNewBanknote(name, time, selectedObverse, selectedReverse, description);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        NewBanknoteDialogFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onAddListener = (OnAddListener) context;
    }

    @Override
    public void onStart() {
        super.onStart();
        ImageView obverse = getDialog().findViewById(R.id.obverse);
        obverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto, 1);
            }
        });
        ImageView reverse = getDialog().findViewById(R.id.reverse);
        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto, 2);
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if (resultCode == RESULT_OK & requestCode == 1) {
            selectedObverse = Objects.requireNonNull(imageReturnedIntent.getData()).toString();
            Picasso.get().load(selectedObverse).into(((ImageView) getDialog().findViewById(R.id.obverse)));
        }
        if (resultCode == RESULT_OK & requestCode == 2) {
            selectedReverse = Objects.requireNonNull(imageReturnedIntent.getData()).toString();
            Picasso.get().load(selectedReverse).into(((ImageView) getDialog().findViewById(R.id.reverse)));
        }
    }

    public interface OnAddListener {
        void addNewBanknote(String name, String circulationTime, String obversePath, String reversePath, String description);
    }
}
