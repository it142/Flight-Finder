package finder.flight.gr.flightfinderv02;

import android.app.Fragment;
import android.content.Intent;
import android.net.ParseException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class ResultFragment extends Fragment {

    FlightAdapter flightAdapter;

    ImageView imageView1 , imageView2;
    TextView desc1, temp1, desc2, temp2;
    TextView textView1, textView2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.resultfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_refresh) {
            FlightData.restart();
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        flightAdapter = new FlightAdapter(getActivity(), FlightData.flights);
        View rootView = inflater.inflate(R.layout.fragment_result, container, false);

        imageView1 = (ImageView) rootView.findViewById(R.id.image1);
        imageView2 = (ImageView) rootView.findViewById(R.id.image2);
        desc1 = (TextView) rootView.findViewById(R.id.desc1);
        desc2 = (TextView) rootView.findViewById(R.id.desc2);
        temp1 = (TextView) rootView.findViewById(R.id.temp1);
        temp2 = (TextView) rootView.findViewById(R.id.temp2);

        if(FetchFlights.shouldCalcWeather1 != -1) {
            imageView1.setImageResource(getIconResourceForWeatherCondition(FlightData.weather_origin.id));
            try {
                desc1.setText(getDesc(FlightData.weather_origin.weather));
                temp1.setText(getTemp(FlightData.weather_origin.temp));
            }
            catch (JSONException e) {
                Log.e("ADASDA", e.toString());
            }
        }
        else imageView1.setImageResource(R.drawable.light_clouds);
        if(FetchFlights.shouldCalcWeather2 != -1) {
            imageView2.setImageResource(getIconResourceForWeatherCondition(FlightData.weather_dest.id));
            try {
                desc2.setText(getDesc(FlightData.weather_dest.weather));
                temp2.setText(getTemp(FlightData.weather_dest.temp));
            }
            catch (JSONException e) {
                Log.e("ADASDA", e.toString());
            }
        }
        else imageView2.setImageResource(R.drawable.light_clouds);

        textView1 = (TextView) rootView.findViewById(R.id.list_text_view_origin_first) ;
        textView2 = (TextView) rootView.findViewById(R.id.list_text_view_dest_first) ;

        textView1.setText(FlightData.FROM);
        textView2.setText(FlightData.TO);


        ListView listView = (ListView) rootView.findViewById(R.id.listview_flights);
        listView.setAdapter(flightAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FlightData.details_depart = new ArrayList<Detail>();
                FlightData.details_return = new ArrayList<Detail>();
                FlightData.position = position;
                FlightData.index = 0;

                try {
                    JSONObject outbound = FlightData.flights.get(position).itineraries.getJSONObject(0).getJSONObject("outbound");
                    JSONObject inbound = FlightData.flights.get(position).itineraries.getJSONObject(0).getJSONObject("inbound");

                    checkDetails(outbound.getJSONArray("flights"), inbound.getJSONArray("flights"));
                }
                catch (Exception e) {
                    Log.e("ResultFragment JSON", e.toString());
                }

            }
        });

        return rootView;
    }




    private void checkDetails(JSONArray flights1, JSONArray flights2) throws JSONException{
        Other.restart();

        // Check if all details are found
        for(int i = 0; i < flights1.length(); i++) {
            String code = flights1.getJSONObject(i).getJSONObject("origin").getString("airport");
            if(!FlightData.from_airport_code.contains(code) &&
                    !FlightData.to_airport_code.contains(code) && !Other.lookFor.contains(code)) {
                Log.i("RESULTFRAGMENT", code + " needs to be found!");
                Other.toBeFound++;
                Other.lookFor.add(code);
                Other.FindAirportTask task= new Other.FindAirportTask(code);
                task.execute();
            }
            code = flights1.getJSONObject(i).getString("operating_airline");
            if(!FlightData.codes.contains(code) && !Other.lookFor.contains(code)) {
                Log.i("RESULTFRAGMENT", code + " needs to be found!");
                Other.toBeFound++;
                Other.lookFor.add(code);
                Other.FindAirlineTask task= new Other.FindAirlineTask(code);
                task.execute();
            }
        }

        for(int i = 0; i < flights2.length(); i++) {
            String code = flights2.getJSONObject(i).getJSONObject("origin").getString("airport");
            if(!FlightData.from_airport_code.contains(code) &&
                    !FlightData.to_airport_code.contains(code) && !Other.lookFor.contains(code)) {
                Log.i("RESULTFRAGMENT", code + " needs to be found!");
                Other.toBeFound++;
                Other.lookFor.add(code);
                Other.FindAirportTask task= new Other.FindAirportTask(code);
                task.execute();
            }
            code = flights2.getJSONObject(i).getString("operating_airline");
            if(!FlightData.codes.contains(code) && !Other.lookFor.contains(code)) {
                Log.i("RESULTFRAGMENT", code + " needs to be found!");
                Other.toBeFound++;
                Other.lookFor.add(code);
                Other.FindAirlineTask task= new Other.FindAirlineTask(code);
                task.execute();
            }
        }

        Log.i("MUST FIND", Other.toBeFound + "");
        if(Other.toBeFound >= 1) createThread(flights1, flights2);
        else ready(flights1, flights2);

    }

    private Thread thread;
    private boolean running;

    private void createThread(final JSONArray flights1, final JSONArray flights2) {
        Runnable r = new Runnable() {

            @Override
            public void run() {
                running = true;
                Log.i("Search", "Thread Started");
                while(running) {
                    if(Other.toBeFound == Other.found && Other.toBeFound != 0 && Other.found != 0) {
                        Log.i("CALLED", Other.toBeFound + " " + Other.found);
                        //Other.toBeFound = Other.found = 0;
                        ready(flights1, flights2);
                    }

                }
            }
        };
        thread = new Thread(r);
        thread.start();
    }

    private void ready(JSONArray flights1, JSONArray flights2) {
        running = false;
        Log.i("RESULT FRAGMENT", "DONE LOOKING!");
        for(int i = 0; i < Other.codes.size(); i++) {
            Log.i("FOUND", Other.codes.get(i) + " | " + Other.values.get(i));
        }
        try {
            getDetails(flights1, 0);
            getDetails(flights2, 1);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }



        Intent intent = new Intent(getActivity(), DetailActivity.class);
        startActivity(intent);
    }

    private void getDetails(JSONArray flights, int type) throws JSONException, java.text.ParseException{
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        Calendar calendar = Calendar.getInstance();

        for(int i = 0; i < flights.length(); i++) {
            JSONObject obj = flights.getJSONObject(i);
            Detail d = new Detail();
            calendar.setTime(df.parse(obj.getString("departs_at")));
            String hours = Integer.toString(calendar.get(Calendar.HOUR_OF_DAY));
            if(hours.length() == 1) hours = "0" + hours;
            String mins = Integer.toString(calendar.get(Calendar.MINUTE));
            if(mins.length() == 1) mins = "0" + mins;
            d.timeStart = hours + ":" + mins;
            calendar.setTime(df.parse(obj.getString("arrives_at")));
            hours = Integer.toString(calendar.get(Calendar.HOUR_OF_DAY));
            if(hours.length() == 1) hours = "0" + hours;
            mins = Integer.toString(calendar.get(Calendar.MINUTE));
            if(mins.length() == 1) mins = "0" + mins;
            d.timeFinish = hours + ":" + mins;

            d.airline = getLabel(obj.getString("operating_airline"));
            d.airportOrigin = getLabel(obj.getJSONObject("origin").getString("airport"));
            d.airportDest = getLabel(obj.getJSONObject("destination").getString("airport"));

            if(type == 0) {
                FlightData.details_depart.add(d);
            }
            else if(type == 1) {
                FlightData.details_return.add(d);
            }
        }


    }

    private String getLabel(String code) {
        if(FlightData.codes.contains(code)) {
            return  FlightData.airlines.get(FlightData.codes.indexOf(code));
        }
        else if(FlightData.from_airport_code.contains(code)) {
            return FlightData.from_airport_label.get(FlightData.from_airport_code.indexOf(code));
        }
        else if(FlightData.to_airport_code.contains(code)) {
            return FlightData.to_airport_label.get(FlightData.to_airport_code.indexOf(code));
        }
        else if(Other.codes.contains(code)) {
            return Other.values.get(Other.codes.indexOf(code));
        }

        return null;
    }


    private int getIconResourceForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.rain;
        } else if (weatherId == 511) {
            return R.drawable.snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.snow;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.storm;
        } else if (weatherId == 800) {
            return R.drawable.clear;
        } else if (weatherId == 801) {
            return R.drawable.light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.clouds;
        }
        return -1;
    }

    private String getDesc(JSONObject obj) throws  JSONException{
        String ret = obj.getString("description");
        ret = ret.substring(0 , 1).toUpperCase() + ret.substring(1, ret.length());
        return ret;
    }
    private  String getTemp(JSONObject obj) throws  JSONException{
        String ret = null;
        if(FlightData.CELSIUS) {
            int min = (int)Double.parseDouble(obj.getString("min"));
            int max = (int)Double.parseDouble(obj.getString("max"));
            ret = min + "°C - " + max + "°C";
        }
        else {
            int min = 9 * (int)Double.parseDouble(obj.getString("min")) / 5 + 32;
            int max = 9 * (int)Double.parseDouble(obj.getString("max")) / 5 + 32;
            ret = min + "°F - " + max + "°F";
        }
        return ret;
    }

    public void back() {
        if(running) return;
        FlightData.restart();
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
    }
}
