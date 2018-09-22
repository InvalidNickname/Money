package uselessapp.money;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;

public class BanknoteFullActivity extends AppCompatActivity implements UpdateBanknoteDialogFragment.OnUpdateListener {

    private DBHelper dbHelper;
    private SQLiteDatabase database;
    private String name, circulationTime, obversePath, reversePath, description, country;
    private int banknoteID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banknote_full);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
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
        dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();
        banknoteID = getIntent().getIntExtra("id", 1);
        Cursor c = database.query("banknotes", null, "_id = ?", new String[]{String.valueOf(banknoteID)}, null, null, null);
        c.moveToFirst();
        name = c.getString(c.getColumnIndex("name"));
        circulationTime = c.getString(c.getColumnIndex("circulation"));
        obversePath = c.getString(c.getColumnIndex("obverse"));
        reversePath = c.getString(c.getColumnIndex("reverse"));
        description = c.getString(c.getColumnIndex("description"));
        country = c.getString(c.getColumnIndex("country"));
        c.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }

    private void setData() {
        ((TextView) findViewById(R.id.countryText)).setText(String.format(getString(R.string.country_s), country));
        ((TextView) findViewById(R.id.circulationText)).setText(String.format(getString(R.string.circulation_time_s), circulationTime));
        ((TextView) findViewById(R.id.descriptionText)).setText(String.format(getString(R.string.description_s), description));
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.delete:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(String.format(getString(R.string.delete_banknote), name))
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                database.delete("banknotes", "_id = ?", new String[]{String.valueOf(banknoteID)});
                                dialog.dismiss();
                                finish();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                break;
            case R.id.update:
                DialogFragment newFragment = new UpdateBanknoteDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("id", banknoteID);
                newFragment.setArguments(bundle);
                newFragment.show(getSupportFragmentManager(), "update_banknote");
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void updateBanknote(String name, String circulationTime, String obversePath, String reversePath, String description) {
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("circulation", circulationTime);
        cv.put("country", country);
        cv.put("obverse", obversePath);
        cv.put("reverse", reversePath);
        cv.put("description", description);
        database.update("banknotes", cv, "_id=?", new String[]{String.valueOf(banknoteID)});
        getData();
        setData();
    }
}
