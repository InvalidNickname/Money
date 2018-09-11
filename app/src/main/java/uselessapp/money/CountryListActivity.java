package uselessapp.money;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

public class CountryListActivity extends AppCompatActivity {

    private List<CountryCard> cardList = new ArrayList<>();

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
        cardList.add(new CountryCard("Azerbaijan",R.drawable.example_image));
        cardList.add(new CountryCard("Azerbaijan",R.drawable.example_image));
        cardList.add(new CountryCard("Azerbaijan",R.drawable.example_image));
        cardList.add(new CountryCard("Azerbaijan",R.drawable.example_image));
        cardList.add(new CountryCard("Azerbaijan",R.drawable.example_image));
        cardList.add(new CountryCard("Azerbaijan",R.drawable.example_image));

        CountryRVAdapter countryRVAdapter = new CountryRVAdapter(cardList);
        mainView.setAdapter(countryRVAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void add(View view) {
        DialogFragment newFragment = new AddCountryDialog();
        newFragment.show(getSupportFragmentManager(),"add_country");
    }
}
