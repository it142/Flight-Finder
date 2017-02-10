package finder.flight.gr.flightfinderv02;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;


public class SearchingActivity extends AppCompatActivity {

    ImageView imageView;
    TextView textView;


    Thread thread;
    boolean running = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searching);

        imageView = (ImageView) findViewById(R.id.imageView);
        textView = (TextView) findViewById(R.id.text_view_origin_dest);
        textView.setText(FlightData.FROM + " - " +FlightData.TO);

        imageView.setBackgroundDrawable(FlightData.animation);
        FlightData.animation.start();
        imageView.post(new Runnable() {
                            @Override
                            public void run() {
                                AnimationDrawable frameAnimation =
                                        (AnimationDrawable) imageView.getBackground();
                                frameAnimation.start();
                            }
                        });

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        FlightData.CURRENCY = prefs.getString(getString(R.string.pref_currency_key),
                getString(R.string.pref_currency_euros));

        String celsius = prefs.getString(getString(R.string.pref_temp_units_key),
                getString(R.string.pref_temp_units_celcius));

        FlightData.CELSIUS = celsius.equals("C");

        String nor = prefs.getString(getString(R.string.pref_nor_key), getString(R.string.pref_nor_default));


        try {
            int num = Integer.parseInt(nor);
            FlightData.NOR = num + "";
        }
        catch (NumberFormatException e) {
            FlightData.NOR = "all";
        }



        FetchFlights.fetch(FlightData.FROM, FlightData.TO,
                FlightData.DEPART_DATE, FlightData.RETURN_DATE);

        createThread();
    }

    private void createThread() {
        Runnable r = new Runnable() {

            @Override
            public void run() {
                running = true;
                Log.i("Search", "Thread Started");
                while(running) {
                    if(FetchFlights.airportsFound == FetchFlights.resultsFound && FetchFlights.airportsFound != 0 && FetchFlights.resultsFound != 0 &&
                            FetchFlights.shouldCalcWeather1 == -1 && FetchFlights.shouldCalcWeather2 == -1 && !FetchFlights.weather_dest && !FetchFlights.weather_origin) {
                        changeActivity();
                    }
                    else if(FetchFlights.airportsFound == FetchFlights.resultsFound && FetchFlights.airportsFound != 0 && FetchFlights.resultsFound != 0 &&
                            ((FetchFlights.shouldCalcWeather1 != -1 && FetchFlights.weather_origin) || (FetchFlights.weather_dest && FetchFlights.shouldCalcWeather2 != -1))) {
                        changeActivity();
                    }


                    if(FetchFlights.wrong) goToMain();

                }
            }
        };
        thread = new Thread(r);
        thread.start();
    }

    private void changeActivity() {
        running = false;
        Log.i("Search", "Thread Finished");
        Intent intent = new Intent(this, ResultActivity.class);
        startActivity(intent);

    }

    private void goToMain() {
        running = false;
        FlightData.restart();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

}
