package ru.mycollection.banknote;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import ru.mycollection.R;
import ru.mycollection.utils.gestureimageview.GestureImageView;

import static ru.mycollection.App.LOG_TAG;

public class BanknoteImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOG_TAG, "BanknoteImageActivity is created");
        setContentView(R.layout.activity_banknote_image);
        setToolbar();
        GestureImageView image = findViewById(R.id.image);
        Picasso.get().load(getFileStreamPath(getIntent().getStringExtra("image"))).into(image);
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getIntent().getStringExtra("name"));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                Log.i(LOG_TAG, "Back button on toolbar selected, finishing");
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }
}
