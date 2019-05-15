package ru.mycollection.banknote;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.squareup.picasso.Picasso;

import ru.mycollection.BaseActivity;
import ru.mycollection.R;
import ru.mycollection.utils.gestureimageview.GestureImageView;

import static ru.mycollection.App.LOG_TAG;

public class BanknoteImageActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
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
        if (menuItem.getItemId() == android.R.id.home) {
            Log.i(LOG_TAG, "Back button on toolbar selected, finishing");
            onBackPressed();
        }
        return super.onOptionsItemSelected(menuItem);
    }
}
