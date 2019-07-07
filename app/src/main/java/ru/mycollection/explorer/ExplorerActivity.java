package ru.mycollection.explorer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ru.mycollection.BaseActivity;
import ru.mycollection.R;

import static ru.mycollection.App.LOG_TAG;

public class ExplorerActivity extends BaseActivity implements ExplorerRVAdapter.OnClickOnDirectory {

    private File current;
    private RecyclerView main;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explorer);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.top_dir));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear);
        }
        main = findViewById(R.id.main);
        main.setLayoutManager(new LinearLayoutManager(this));
        setData(Environment.getExternalStorageDirectory());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            Log.i(LOG_TAG, "Back button on toolbar selected");
            setResult(RESULT_CANCELED);
            finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void setData(File file) {
        current = file;
        if (isTopDir()) {
            toolbar.setTitle(getString(R.string.top_dir));
        } else {
            toolbar.setTitle(current.getName());
        }
        List<ExplorerItem> explorerItems = new ArrayList<>();
        for (File i : current.listFiles()) {
            if ((i.isDirectory() && !i.getName().startsWith(".")) || (i.isFile() && i.getName().endsWith(".db"))) {
                explorerItems.add(new ExplorerItem(i.getAbsolutePath(), i.getName()));
            }
        }
        main.setAdapter(new ExplorerRVAdapter(explorerItems, this));
    }

    @Override
    public void browseTo(File file) {
        if (file.isDirectory()) {
            setData(file);
        } else {
            Intent intent = new Intent();
            intent.putExtra("path", file.getAbsolutePath());
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (!isTopDir())
            setData(current.getParentFile());
    }

    private boolean isTopDir() {
        return current.getParentFile().listFiles() == null;
    }
}
