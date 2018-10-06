package ru.money.help;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ru.money.R;

import static ru.money.App.LOG_TAG;

public class HelpActivity extends AppCompatActivity {

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
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                Log.i(LOG_TAG, "Back button on toolbar selected, finishing");
                finish();
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void setData() {
        String[] titles = getResources().getStringArray(R.array.help_titles);
        String[] texts = getResources().getStringArray(R.array.help_texts);
        List<HelpItem> helpItemList = new ArrayList<>();
        for (int i = 0; i < titles.length; i++)
            helpItemList.add(new HelpItem(titles[i], texts[i]));
        RecyclerView main = findViewById(R.id.main);
        main.setLayoutManager(new LinearLayoutManager(this));
        main.setAdapter(new HelpRVAdapter(helpItemList));
    }
}
