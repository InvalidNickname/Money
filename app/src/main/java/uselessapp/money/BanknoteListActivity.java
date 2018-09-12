package uselessapp.money;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

public class BanknoteListActivity extends AppCompatActivity {

    private List<Banknote> banknoteList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        RecyclerView mainView = findViewById(R.id.main);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mainView.setLayoutManager(layoutManager);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        banknoteList.add(new Banknote("Azerbaijan", "10000 dollars", "2000BC-2019", R.drawable.example_image));
        banknoteList.add(new Banknote("Azerbaijan", "10000 dollars", "2000BC-2019", R.drawable.example_image));
        banknoteList.add(new Banknote("Azerbaijan", "10000 dollars", "2000BC-2019", R.drawable.example_image));
        banknoteList.add(new Banknote("Azerbaijan", "10000 dollars", "2000BC-2019", R.drawable.example_image));
        banknoteList.add(new Banknote("Azerbaijan", "10000 dollars", "2000BC-2019", R.drawable.example_image));

        BanknoteRVAdapter banknoteRvAdapter = new BanknoteRVAdapter(banknoteList);
        mainView.setAdapter(banknoteRvAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void add(View view) {
    }
}
