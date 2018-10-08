package ru.money;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import ru.money.dialog.BanknoteDialogFragment;
import ru.money.utils.DBHelper;

import static ru.money.App.LOG_TAG;
import static ru.money.utils.DBHelper.COLUMN_CIRCULATION;
import static ru.money.utils.DBHelper.COLUMN_COUNTRY;
import static ru.money.utils.DBHelper.COLUMN_DESCRIPTION;
import static ru.money.utils.DBHelper.COLUMN_ID;
import static ru.money.utils.DBHelper.COLUMN_NAME;
import static ru.money.utils.DBHelper.COLUMN_OBVERSE;
import static ru.money.utils.DBHelper.COLUMN_REVERSE;
import static ru.money.utils.DBHelper.TABLE_BANKNOTES;

public class BanknoteFullActivity extends AppCompatActivity implements BanknoteDialogFragment.OnUpdateListener {

    private SQLiteDatabase database;
    private String name, circulationTime, obversePath, reversePath, description, country;
    private int banknoteID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOG_TAG, "BanknoteFullActivity is created");
        database = DBHelper.getInstance(this).getDatabase();
        setContentView(R.layout.activity_banknote_full);
        getData();
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(name);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        setData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.banknote_menu, menu);
        return true;
    }

    private void getData() {
        Log.i(LOG_TAG, "Getting data...");
        banknoteID = getIntent().getIntExtra("id", 1);
        Cursor c = database.query(TABLE_BANKNOTES, null, COLUMN_ID + " = " + banknoteID, null, null, null, null);
        c.moveToFirst();
        name = c.getString(c.getColumnIndex(COLUMN_NAME));
        circulationTime = c.getString(c.getColumnIndex(COLUMN_CIRCULATION));
        obversePath = c.getString(c.getColumnIndex(COLUMN_OBVERSE));
        reversePath = c.getString(c.getColumnIndex(COLUMN_REVERSE));
        description = c.getString(c.getColumnIndex(COLUMN_DESCRIPTION));
        country = c.getString(c.getColumnIndex(COLUMN_COUNTRY));
        c.close();
        Log.i(LOG_TAG, "Data is got");
    }

    private void setData() {
        Log.i(LOG_TAG, "Setting data");
        // выделение жирным названия пункта списка
        SpannableStringBuilder country = new SpannableStringBuilder(String.format(getString(R.string.country_s), this.country));
        country.setSpan(new StyleSpan(Typeface.BOLD), 0, getString(R.string.country_s).length() - 2, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        ((TextView) findViewById(R.id.countryText)).setText(country);
        SpannableStringBuilder circulation = new SpannableStringBuilder(String.format(getString(R.string.circulation_time_s), this.circulationTime));
        circulation.setSpan(new StyleSpan(Typeface.BOLD), 0, getString(R.string.circulation_time_s).length() - 2, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        ((TextView) findViewById(R.id.circulationText)).setText(circulation);
        SpannableStringBuilder description = new SpannableStringBuilder(String.format(getString(R.string.description_s), this.description));
        description.setSpan(new StyleSpan(Typeface.BOLD), 0, getString(R.string.description_s).length() - 2, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        ((TextView) findViewById(R.id.descriptionText)).setText(description);
        if (!obversePath.equals("nothing")) {
            File file = getFileStreamPath(obversePath);
            Picasso.get().load(file).into((ImageView) findViewById(R.id.obverseImage));
        } else
            Picasso.get().load(R.drawable.example_banknote).into((ImageView) findViewById(R.id.obverseImage));
        if (!reversePath.equals("nothing")) {
            File file = getFileStreamPath(reversePath);
            Picasso.get().load(file).into((ImageView) findViewById(R.id.reverseImage));
        } else
            Picasso.get().load(R.drawable.example_banknote).into((ImageView) findViewById(R.id.reverseImage));
        Log.i(LOG_TAG, "Data is set");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                Log.i(LOG_TAG, "Back button on toolbar selected, finishing");
                finish();
                break;
            case R.id.delete:
                Log.i(LOG_TAG, "Opening delete dialog");
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(String.format(getString(R.string.delete_banknote), name))
                        .setNegativeButton(R.string.cancel, (dialog, id) -> {
                            Log.i(LOG_TAG, "Dialog cancelled");
                            dialog.cancel();
                        })
                        .setPositiveButton(R.string.yes, (dialog, id) -> {
                            Log.i(LOG_TAG, "Deleting banknote...");
                            database.delete(TABLE_BANKNOTES, COLUMN_ID + " = " + banknoteID, null);
                            Log.i(LOG_TAG, "Banknote deleted, closing dialog");
                            dialog.dismiss();
                            finish();
                        });
                AlertDialog alert = builder.create();
                alert.show();
                break;
            case R.id.update:
                Log.i(LOG_TAG, "Opening UpdateBanknoteDialog");
                BanknoteDialogFragment fragment = new BanknoteDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("id", banknoteID);
                fragment.setArguments(bundle);
                fragment.show(getSupportFragmentManager(), "update_banknote");
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void updateBanknote(String name, String circulationTime, String obversePath, String reversePath, String description, String country) {
        Log.i(LOG_TAG, "Updating banknote...");
        getSupportActionBar().setTitle(name);
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, name);
        cv.put(COLUMN_CIRCULATION, circulationTime);
        cv.put(COLUMN_COUNTRY, country);
        cv.put(COLUMN_OBVERSE, obversePath);
        cv.put(COLUMN_REVERSE, reversePath);
        cv.put(COLUMN_DESCRIPTION, description);
        database.update(TABLE_BANKNOTES, cv, COLUMN_ID + " = " + banknoteID, null);
        Log.i(LOG_TAG, "Banknote updated");
        getData();
        setData();
    }
}
