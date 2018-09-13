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

public class NewCountryDialogFragment extends DialogFragment implements View.OnClickListener {

    OnAddListener onAddListener;
    String selectedImage;

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        LayoutInflater inflater = getActivity().getLayoutInflater();
        selectedImage = "nothing";
        builder.setView(inflater.inflate(R.layout.dialog_add_country, null))
                .setTitle(getResources().getString(R.string.add_new_country))
                .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        EditText editText = getDialog().findViewById(R.id.editText);
                        String name = editText.getText().toString().replaceAll("\\s+", ""); // получение названия и форматирование
                        onAddListener.addNewCountry(name, selectedImage);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        NewCountryDialogFragment.this.getDialog().cancel();
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
        ImageView imageView = getDialog().findViewById(R.id.flag);
        imageView.setOnClickListener(this);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if (resultCode == RESULT_OK & requestCode == 1) {
            selectedImage = Objects.requireNonNull(imageReturnedIntent.getData()).toString();
            Picasso.get().load(selectedImage).into(((ImageView) getDialog().findViewById(R.id.flag)));
        }
    }

    @Override
    public void onClick(View v) {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, 1);
    }

    public interface OnAddListener {
        void addNewCountry(String name, String flagPath);
    }
}
