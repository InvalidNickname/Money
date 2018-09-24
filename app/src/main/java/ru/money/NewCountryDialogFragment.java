package ru.money;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Objects;

import static android.support.v7.app.AppCompatActivity.RESULT_OK;
import static ru.money.ContinentsActivity.width;

public class NewCountryDialogFragment extends DialogFragment implements View.OnClickListener {

    private OnAddListener onAddListener;
    private String selectedImage;
    private Context context;

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        LayoutInflater inflater = getActivity().getLayoutInflater();
        selectedImage = "nothing";
        builder.setView(inflater.inflate(R.layout.dialog_country, null))
                .setTitle(getResources().getString(R.string.add_new_country))
                .setPositiveButton(R.string.add, null)
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
        this.context = context;
        onAddListener = (OnAddListener) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        final AlertDialog d = (AlertDialog) getDialog();
        if (d != null) {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText editText = getDialog().findViewById(R.id.editText);
                    String name = editText.getText().toString().trim().replaceAll("\\s+", " "); // получение названия и форматирование
                    if (!name.equals("")) {
                        onAddListener.addNewCountry(name, selectedImage);
                        d.dismiss();
                    } else {
                        TextInputLayout textInputLayout = getDialog().findViewById(R.id.nameInput);
                        textInputLayout.setError(getString(R.string.country_name_error));
                    }
                }
            });
        }
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
            selectedImage = Utils.saveReturnedImageInFile(imageReturnedIntent, context, width / 10);
            File file = context.getFileStreamPath(selectedImage);
            Picasso.get().load(file).into(((ImageView) getDialog().findViewById(R.id.flag)));
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
