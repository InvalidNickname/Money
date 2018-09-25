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

public class UpdateBanknoteDialogFragment extends DialogFragment {

    private OnUpdateListener onUpdateListener;
    private String selectedObverse, selectedReverse, name, circulationTime, obversePath, reversePath, description;
    private int id;
    private boolean isDataSet = false;
    private Context context;

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        LayoutInflater inflater = getActivity().getLayoutInflater();
        Bundle args = getArguments();
        if (args != null) {
            id = args.getInt("id");
        }
        builder.setView(inflater.inflate(R.layout.dialog_banknote, null))
                .setTitle(getResources().getString(R.string.update_banknote))
                .setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        EditText editName = getDialog().findViewById(R.id.editName);
                        String name = editName.getText().toString().trim().replaceAll("\\s+", " ");
                        EditText editTime = getDialog().findViewById(R.id.editTime);
                        String time = editTime.getText().toString().trim().replaceAll("\\s+", " ");
                        EditText editDescription = getDialog().findViewById(R.id.editDescription);
                        String description = editDescription.getText().toString().trim().replaceAll("\\s+", " ");
                        if (description.equals(""))
                            description = getString(R.string.no_description);
                        System.out.println(selectedObverse);
                        onUpdateListener.updateBanknote(name, time, selectedObverse, selectedReverse, description);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        UpdateBanknoteDialogFragment.this.getDialog().cancel();
                    }
                });
        getData();
        return builder.create();
    }

    private void getData() {
        DBHelper dbHelper = new DBHelper(getContext());
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor c = database.query("banknotes", null, "_id = ?", new String[]{String.valueOf(id)}, null, null, null);
        c.moveToFirst();
        name = c.getString(c.getColumnIndex("name"));
        circulationTime = c.getString(c.getColumnIndex("circulation"));
        obversePath = c.getString(c.getColumnIndex("obverse"));
        reversePath = c.getString(c.getColumnIndex("reverse"));
        description = c.getString(c.getColumnIndex("description"));
        c.close();
        dbHelper.close();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        onUpdateListener = (OnUpdateListener) context;
    }

    @Override
    public void onStart() {
        super.onStart();
        ImageView obverse = getDialog().findViewById(R.id.obverse);
        ImageView reverse = getDialog().findViewById(R.id.reverse);
        if (!isDataSet) {
            ((TextView) getDialog().findViewById(R.id.editName)).setText(name);
            ((TextView) getDialog().findViewById(R.id.editTime)).setText(circulationTime);
            ((TextView) getDialog().findViewById(R.id.editDescription)).setText(description);
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

    public interface OnUpdateListener {
        void updateBanknote(String name, String circulationTime, String obversePath, String reversePath, String description);
    }
}
