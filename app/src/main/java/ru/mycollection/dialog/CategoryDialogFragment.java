package ru.mycollection.dialog;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputLayout;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Objects;

import ru.mycollection.R;
import ru.mycollection.utils.DBHelper;
import ru.mycollection.utils.Utils;

import static androidx.appcompat.app.AppCompatActivity.RESULT_OK;
import static ru.mycollection.App.width;
import static ru.mycollection.utils.DBHelper.COLUMN_ID;
import static ru.mycollection.utils.DBHelper.COLUMN_IMAGE;
import static ru.mycollection.utils.DBHelper.COLUMN_NAME;
import static ru.mycollection.utils.DBHelper.TABLE_CATEGORIES;

public class CategoryDialogFragment extends DialogFragment implements View.OnClickListener {

    private boolean newCategory;
    private OnChangeListener onChangeListener;
    private String selectedImage, name, imagePath;
    private Context context;
    private boolean isDataSet = false;
    private int id = -1;

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        LayoutInflater inflater = getActivity().getLayoutInflater();
        selectedImage = "nothing";
        builder.setView(inflater.inflate(R.layout.dialog_category, null))
                .setTitle(newCategory ? getResources().getString(R.string.add_new_category) : getResources().getString(R.string.update_category))
                .setPositiveButton(newCategory ? R.string.add : R.string.update, null)
                .setNegativeButton(R.string.cancel, (dialog, id) -> CategoryDialogFragment.this.getDialog().cancel());
        if (!newCategory) getData();
        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        Bundle args = getArguments();
        // получение id банкноты, которую надо обновить. если id == -1, то банкноту надо создать
        if (args != null) {
            id = args.getInt("id");
            newCategory = false;
        } else {
            newCategory = true;
            selectedImage = "nothing";
        }
        onChangeListener = (OnChangeListener) context;
    }

    private void getData() {
        SQLiteDatabase database = DBHelper.getInstance(getContext()).getDatabase();
        Cursor c = database.query(TABLE_CATEGORIES, null, COLUMN_ID + " = " + id, null, null, null, null);
        if (c.moveToFirst()) {
            name = c.getString(c.getColumnIndex(COLUMN_NAME));
            imagePath = c.getString(c.getColumnIndex(COLUMN_IMAGE));
            c.close();
        }
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
                    if (newCategory)
                        onChangeListener.addNewCategory(name, selectedImage, "no category");
                    else onChangeListener.updateCategory(name, selectedImage, id);
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
        ImageView imageView = getDialog().findViewById(R.id.flag);
        Switch iconSwitch = getDialog().findViewById(R.id.iconSwitch);
        if (!newCategory && !isDataSet) {
            ((TextView) getDialog().findViewById(R.id.editText)).setText(name);
            selectedImage = imagePath;
            switch (selectedImage) {
                case "nothing":
                    Picasso.get().load(R.drawable.example_flag).into(imageView);
                    break;
                case "no icon":
                    imageView.setVisibility(View.GONE);
                    iconSwitch.setChecked(false);
                    break;
                default:
                    Picasso.get().load(context.getFileStreamPath(selectedImage)).into(imageView);
                    break;
            }
            isDataSet = true;
        }
        // отслеживание нажатий по иконке
        imageView.setOnClickListener(this);
        // переключатель необходимости иконки
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

    public interface OnChangeListener {
        void addNewCategory(String name, String flagPath, String category);

        void updateCategory(String name, String flagPath, int id);
    }
}
