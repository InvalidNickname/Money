package ru.money;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import static androidx.appcompat.app.AppCompatActivity.RESULT_OK;
import static ru.money.DBHelper.COLUMN_COUNTRY;
import static ru.money.DBHelper.TABLE_BANKNOTES;

public class BanknoteDialogFragment extends DialogFragment {

    private OnAddListener onAddListener;
    private OnUpdateListener onUpdateListener;
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
        Bundle args = getArguments();
        // получние id банкноты, которую надо обновить. если id == -1, то банкноту надо создать
        if (args != null) {
            id = args.getInt("id");
            newBanknote = false;
        } else {
            newBanknote = true;
            selectedReverse = "nothing";
            selectedObverse = "nothing";
        }
        builder.setView(inflater.inflate(R.layout.dialog_banknote, null))
                .setTitle(newBanknote ? getString(R.string.add_new_banknote) : getString(R.string.update_banknote))
                .setPositiveButton(newBanknote ? R.string.add : R.string.update, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        EditText editName = getDialog().findViewById(R.id.editName);
                        String name = editName.getText().toString().trim().replaceAll("\\s+", " ");
                        EditText editTime = getDialog().findViewById(R.id.editTime);
                        String time = editTime.getText().toString().trim().replaceAll("\\s+", " ");
                        EditText editDescription = getDialog().findViewById(R.id.editDescription);
                        String description = editDescription.getText().toString().trim().replaceAll("\\s+", " ");
                        EditText editCountry = getDialog().findViewById(R.id.editCountry);
                        String country = editCountry.getText().toString().trim().replaceAll("\\s+", " ");
                        if (description.equals(""))
                            description = getString(R.string.no_description);
                        if (newBanknote)
                            onAddListener.addNewBanknote(name, time, selectedObverse, selectedReverse, description, country);
                        else
                            onUpdateListener.updateBanknote(name, time, selectedObverse, selectedReverse, description, country);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        BanknoteDialogFragment.this.getDialog().cancel();
                    }
                });
        if (!newBanknote)
            getData();
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    void setOnAddListener(Context context) {
        onAddListener = (OnAddListener) context;
    }

    void setOnUpdateListener(Context context) {
        onUpdateListener = (OnUpdateListener) context;
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
            } else
                Picasso.get().load(R.drawable.example_banknote).into(obverse);
            if (!reversePath.equals("nothing")) {
                File file = context.getFileStreamPath(reversePath);
                Picasso.get().load(file).into(reverse);
            } else
                Picasso.get().load(R.drawable.example_banknote).into(reverse);
            isDataSet = true;
        }
        obverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto, 1);
            }
        });
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
            selectedObverse = Utils.saveReturnedImageInFile(imageReturnedIntent, context);
            File file = context.getFileStreamPath(selectedObverse);
            Picasso.get().load(file).into(((ImageView) getDialog().findViewById(R.id.obverse)));
        }
        if (resultCode == RESULT_OK & requestCode == 2) {
            selectedReverse = Utils.saveReturnedImageInFile(imageReturnedIntent, context);
            File file = context.getFileStreamPath(selectedReverse);
            Picasso.get().load(file).into(((ImageView) getDialog().findViewById(R.id.reverse)));
        }
    }

    private void getData() {
        DBHelper dbHelper = new DBHelper(getContext());
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor c = database.query(TABLE_BANKNOTES, null, "_id = ?", new String[]{String.valueOf(id)}, null, null, null);
        if (c.moveToFirst()) {
            name = c.getString(c.getColumnIndex("name"));
            circulationTime = c.getString(c.getColumnIndex("circulation"));
            obversePath = c.getString(c.getColumnIndex("obverse"));
            reversePath = c.getString(c.getColumnIndex("reverse"));
            description = c.getString(c.getColumnIndex("description"));
            country = c.getString(c.getColumnIndex(COLUMN_COUNTRY));
            c.close();
        }
        dbHelper.close();
    }

    public interface OnAddListener {
        void addNewBanknote(String name, String circulationTime, String obversePath, String reversePath, String description, String country);
    }

    public interface OnUpdateListener {
        void updateBanknote(String name, String circulationTime, String obversePath, String reversePath, String description, String country);
    }
}
