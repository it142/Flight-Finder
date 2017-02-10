package finder.flight.gr.flightfinderv02;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;


public class DetailActivity extends AppCompatActivity{
    DetailFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fragment = new DetailFragment();
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container_detail, fragment)
                    .commit();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if(id == R.id.action_next_itineraries) {
            fragment.next();
        }
        else if(id == R.id.action_buy) {
            Intent intent = new Intent(this, BuyActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
