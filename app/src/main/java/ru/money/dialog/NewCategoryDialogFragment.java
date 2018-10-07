package ru.money.dialog;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;

import com.google.android.material.textfield.TextInputLayout;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import ru.money.R;
import ru.money.utils.Utils;

import static androidx.appcompat.app.AppCompatActivity.RESULT_OK;
import static ru.money.App.width;

public class NewCategoryDialogFragment extends DialogFragment implements View.OnClickListener {

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
        builder.setView(inflater.inflate(R.layout.dialog_category, null))
                .setTitle(getResources().getString(R.string.add_new_country))
                .setPositiveButton(R.string.add, null)
                .setNegativeButton(R.string.cancel, (dialog, id) -> NewCategoryDialogFragment.this.getDialog().cancel());
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
            positiveButton.setOnClickListener(v -> {
                EditText editText = getDialog().findViewById(R.id.editText);
                String name = editText.getText().toString().trim().replaceAll("\\s+", " "); // получение названия и форматирование
                if (!name.equals("")) {
                    if (!((Switch) getDialog().findViewById(R.id.iconSwitch)).isChecked())
                        selectedImage = "no icon";
                    onAddListener.addNewCategory(name, selectedImage, "no category");
                    d.dismiss();
                } else {
                    TextInputLayout textInputLayout = getDialog().findViewById(R.id.nameInput);
                    textInputLayout.setError(getString(R.string.country_name_error));
                }
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // чтобы диалог нельзя было закрыть, случайно нажав вне него
        getDialog().setCanceledOnTouchOutside(false);
        // отслеживание нажатий по иконке
        ImageView imageView = getDialog().findViewById(R.id.flag);
        imageView.setOnClickListener(this);
        // переключатель необходимости иконки
        Switch iconSwitch = getDialog().findViewById(R.id.iconSwitch);
        iconSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> getDialog().findViewById(R.id.flag).setVisibility(isChecked ? View.VISIBLE : View.GONE));
    }

    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if (resultCode == RESULT_OK & requestCode == 1) {
            selectedImage = Utils.saveReturnedImageInFile(imageReturnedIntent, context, width / 8);
            File file = context.getFileStreamPath(selectedImage);
            Picasso.get().load(file).into(((ImageView) getDialog().findViewById(R.id.flag)));
        }
    }

    @Override
    public void onClick(View v) {
        if (Utils.checkPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickPhoto, 1);
        }
    }

    public interface OnAddListener {
        void addNewCategory(String name, String flagPath, String category);
    }

}