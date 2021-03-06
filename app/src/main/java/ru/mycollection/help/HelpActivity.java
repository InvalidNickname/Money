package ru.mycollection.help;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ru.mycollection.BaseActivity;
import ru.mycollection.R;

import static ru.mycollection.App.LOG_TAG;

public class HelpActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.help_title));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        setData();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            Log.i(LOG_TAG, "Back button on toolbar selected, finishing");
            finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void setData() {
        String[] titles = getResources().getStringArray(R.array.help_titles);
        String[] texts = getResources().getStringArray(R.array.help_texts);
        List<HelpItem> helpItems = new ArrayList<>();
        for (int i = 0; i < titles.length; i++) helpItems.add(new HelpItem(titles[i], texts[i]));
        RecyclerView main = findViewById(R.id.main);
        main.setLayoutManager(new LinearLayoutManager(this));
        main.setAdapter(new HelpRVAdapter(helpItems));
    }
}
