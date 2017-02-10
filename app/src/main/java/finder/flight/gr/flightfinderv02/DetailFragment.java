package finder.flight.gr.flightfinderv02;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;


public class DetailFragment extends Fragment {

    DetailAdapter detailAdapter1;
    DetailAdapter detailAdapter2;

    ListView listDepart;
    ListView listReturn;

    private Thread thread;
    private boolean running;
    private boolean done = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

    }
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        detailAdapter1 = new DetailAdapter(getActivity(), FlightData.details_depart);
        detailAdapter2 = new DetailAdapter(getActivity(), FlightData.details_return);

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        listDepart = (ListView) rootView.findViewById(R.id.list_departure);
        listReturn = (ListView) rootView.findViewById(R.id.list_return);

        listDepart.setAdapter(detailAdapter1);
        listReturn.setAdapter(detailAdapter2);

        return rootView;
    }

    public void next() {

        JSONArray iti = FlightData.flights.get(FlightData.position).itineraries;

        if(FlightData.index + 1 < iti.length()) FlightData.index++;
        else FlightData.index = 0;
        Log.i("DetailFragment", FlightData.index + " " + iti.length());


        try {
            JSONObject outbound = iti.getJSONObject(FlightData.index).getJSONObject("outbound");
            JSONObject inbound = iti.getJSONObject(FlightData.index).getJSONObject("inbound");

            checkDetails(outbound.getJSONArray("flights"), inbound.getJSONArray("flights"));
        }
        catch (Exception e) {
            Log.e("DETAIL", e.toString());
        }
    }

    private void checkDetails(JSONArray flights1, JSONArray flights2) throws JSONException{
        Other.restart();

        // Check if all details are found
        for(int i = 0; i < flights1.length(); i++) {
            String code = flights1.getJSONObject(i).getJSONObject("origin").getString("airport");
            if(!FlightData.from_airport_code.contains(code) &&
                    !FlightData.to_airport_code.contains(code) && !Other.lookFor.contains(code)) {
                Log.i("DETAILFRAGMENT", code + " needs to be found!");
                Other.toBeFound++;
                Other.lookFor.add(code);
                Other.FindAirportTask task= new Other.FindAirportTask(code);
                task.execute();
            }
            code = flights1.getJSONObject(i).getString("operating_airline");
            if(!FlightData.codes.contains(code) && !Other.lookFor.contains(code)) {
                Log.i("DETAILFRAGMENT", code + " needs to be found!");
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
                Log.i("DETAILFRAGMENT", code + " needs to be found!");
                Other.toBeFound++;
                Other.lookFor.add(code);
                Other.FindAirportTask task= new Other.FindAirportTask(code);
                task.execute();
            }
            code = flights2.getJSONObject(i).getString("operating_airline");
            if(!FlightData.codes.contains(code) && !Other.lookFor.contains(code)) {
                Log.i("DETAILFRAGMENT", code + " needs to be found!");
                Other.toBeFound++;
                Other.lookFor.add(code);
                Other.FindAirlineTask task= new Other.FindAirlineTask(code);
                task.execute();
            }
        }

        Log.i("MUST FIND", Other.toBeFound + "");
        if(Other.toBeFound >= 1) {
            done = false;
            createThread(flights1, flights2);
        }
        else ready(flights1, flights2);

    }



    private void createThread(final JSONArray flights1, final JSONArray flights2) {

        running = true;
        thread = new Thread(new Runnable() {

            @Override
            public void run() {

                Log.i("Search", "Thread Started");
                while(running) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(Other.toBeFound == Other.found && Other.toBeFound != 0 && Other.found != 0 && !done) {
                                Log.i("CALLED", Other.toBeFound + " " + Other.found);
                                done = true;
                                ready(flights1, flights2);

                            }
                        }
                    });
                }
            }
        });
        thread.start();
    }

    private void ready(JSONArray flights1, JSONArray flights2) {
        running = false;
        Log.i("DETAIL FRAGMENT", "DONE LOOKING!");
        for(int i = 0; i < Other.codes.size(); i++) {
            Log.i("FOUND", Other.codes.get(i) + " | " + Other.values.get(i));
        }
        try {
            detailAdapter1.clear();
            detailAdapter2.clear();
            getDetails(flights1, 0);
            getDetails(flights2, 1);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
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
                detailAdapter1.add(d);
            }
            else if(type == 1) {
                detailAdapter2.add(d);
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

}
