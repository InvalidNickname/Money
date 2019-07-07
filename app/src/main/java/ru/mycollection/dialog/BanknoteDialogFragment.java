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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.textfield.TextInputLayout;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Objects;

import ru.mycollection.R;
import ru.mycollection.utils.DBHelper;
import ru.mycollection.utils.Utils;

import static androidx.appcompat.app.AppCompatActivity.RESULT_OK;
import static ru.mycollection.App.width;
import static ru.mycollection.utils.DBHelper.COLUMN_CIRCULATION;
import static ru.mycollection.utils.DBHelper.COLUMN_COUNTRY;
import static ru.mycollection.utils.DBHelper.COLUMN_DESCRIPTION;
import static ru.mycollection.utils.DBHelper.COLUMN_ID;
import static ru.mycollection.utils.DBHelper.COLUMN_NAME;
import static ru.mycollection.utils.DBHelper.COLUMN_OBVERSE;
import static ru.mycollection.utils.DBHelper.COLUMN_REVERSE;
import static ru.mycollection.utils.DBHelper.TABLE_BANKNOTES;

public class BanknoteDialogFragment extends DialogFragment {

    private OnChangeListener onChangeListener;
    private String selectedObverse, selectedReverse;
    private String name, circulationTime, obversePath, reversePath, description, country;
    private boolean isDataSet = false, newBanknote;
    private Context context;
    private int id = -1;

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        LayoutInflater inflater = getActivity().getLayoutInflater();
        String namedItem = String.format(getString(R.string.add_new_banknote), PreferenceManager.getDefaultSharedPreferences(getContext()).getString("item_name", ""));
        builder.setView(inflater.inflate(R.layout.dialog_banknote, null))
                .setTitle(newBanknote ? namedItem : getString(R.string.update_banknote))
                .setPositiveButton(newBanknote ? R.string.add : R.string.update, null)
                .setNegativeButton(R.string.cancel, (dialog, id) -> getDialog().cancel());
        if (!newBanknote) getData();
        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        final AlertDialog d = (AlertDialog) getDialog();
        if (d != null) {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                // получение данных
                EditText editName = getDialog().findViewById(R.id.editName);
                String name = editName.getText().toString().trim().replaceAll("\\s+", " ");
                EditText editTime = getDialog().findViewById(R.id.editTime);
                String time = editTime.getText().toString().trim().replaceAll("\\s+", " ");
                EditText editDescription = getDialog().findViewById(R.id.editDescription);
                String description = editDescription.getText().toString().trim().replaceAll("^ +| +$|( )+", " ");
                EditText editCountry = getDialog().findViewById(R.id.editCountry);
                String country = editCountry.getText().toString().trim().replaceAll("\\s+", " ");
                // если описание пустое, оно заменяется на "нет описания"
                if (description.equals("")) description = getString(R.string.no_description);
                // добавление возможно только если заполнены "название" и "страна"
                if (!name.equals("") && !country.equals("")) {
                    if (newBanknote)
                        onChangeListener.addNewBanknote(name, time, selectedObverse, selectedReverse, description, country);
                    else
                        onChangeListener.updateBanknote(name, time, selectedObverse, selectedReverse, description, country);
                    d.dismiss();
                } else {
                    TextInputLayout nameInput = getDialog().findViewById(R.id.nameInput);
                    nameInput.setError(name.equals("") ? getString(R.string.banknote_name_error) : null);
                    TextInputLayout countryInput = getDialog().findViewById(R.id.countryInput);
                    countryInput.setError(name.equals("") ? getString(R.string.country_name_error) : null);
                }
            });
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        Bundle args = getArguments();
        // получение id банкноты, которую надо обновить. если id == -1, то банкноту надо создать
        if (args != null) {
            id = args.getInt("id");
            newBanknote = false;
        } else {
            newBanknote = true;
            selectedReverse = "nothing";
            selectedObverse = "nothing";
        }
        onChangeListener = (OnChangeListener) context;
    }

    @Override
    public void onStart() {
        super.onStart();
        // чтобы диалог нельзя было закрыть, случайно нажав вне него
        getDialog().setCanceledOnTouchOutside(false);
        ImageView obverse = getDialog().findViewById(R.id.obverse);
        ImageView reverse = getDialog().findViewById(R.id.reverse);
        if (!newBanknote && !isDataSet) {
            ((TextView) getDialog().findViewById(R.id.editName)).setText(name);
            ((TextView) getDialog().findViewById(R.id.editTime)).setText(circulationTime);
            ((TextView) getDialog().findViewById(R.id.editDescription)).setText(description);
            ((TextView) getDialog().findViewById(R.id.editCountry)).setText(country);
            selectedObverse = obversePath;
            selectedReverse = reversePath;
            if (!obversePath.equals("nothing")) {
                File file = context.getFileStreamPath(obversePath);
                Picasso.get().load(file).into(obverse);
            } else Picasso.get().load(R.drawable.example_banknote).into(obverse);
            if (!reversePath.equals("nothing")) {
                File file = context.getFileStreamPath(reversePath);
                Picasso.get().load(file).into(reverse);
            } else Picasso.get().load(R.drawable.example_banknote).into(reverse);
            isDataSet = true;
        }
        obverse.setOnClickListener(v -> {
            if (Utils.checkPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto, 1);
            }
        });
        reverse.setOnClickListener(v -> {
            if (Utils.checkPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto, 2);
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if (resultCode == RESULT_OK & requestCode == 1) {
            selectedObverse = Utils.saveReturnedImageInFile(imageReturnedIntent, context, width);
            File file = context.getFileStreamPath(selectedObverse);
            Picasso.get().load(file).into(((ImageView) getDialog().findViewById(R.id.obverse)));
        }
        if (resultCode == RESULT_OK & requestCode == 2) {
            selectedReverse = Utils.saveReturnedImageInFile(imageReturnedIntent, context, width);
            File file = context.getFileStreamPath(selectedReverse);
            Picasso.get().load(file).into(((ImageView) getDialog().findViewById(R.id.reverse)));
        }
    }

    private void getData() {
        SQLiteDatabase database = DBHelper.getInstance(getContext()).getDatabase();
        Cursor c = database.query(TABLE_BANKNOTES, null, COLUMN_ID + " = " + id, null, null, null, null);
        if (c.moveToFirst()) {
            name = c.getString(c.getColumnIndex(COLUMN_NAME));
            circulationTime = c.getString(c.getColumnIndex(COLUMN_CIRCULATION));
            obversePath = c.getString(c.getColumnIndex(COLUMN_OBVERSE));
            reversePath = c.getString(c.getColumnIndex(COLUMN_REVERSE));
            description = c.getString(c.getColumnIndex(COLUMN_DESCRIPTION));
            country = c.getString(c.getColumnIndex(COLUMN_COUNTRY));
            c.close();
        }
    }

    public interface OnChangeListener {
        void addNewBanknote(String name, String circulationTime, String obversePath, String reversePath, String description, String country);

        void updateBanknote(String name, String circulationTime, String obversePath, String reversePath, String description, String country);
    }
}
