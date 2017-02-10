package finder.flight.gr.flightfinderv02;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;


public class ResultActivity extends AppCompatActivity {

    ResultFragment fragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        fragment = new ResultFragment();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        if(savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container_result, fragment).commit();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            fragment.back();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
