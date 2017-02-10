package finder.flight.gr.flightfinderv02;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static finder.flight.gr.flightfinderv02.BuildConfig.AMADEUS_API_KEY;
import static finder.flight.gr.flightfinderv02.BuildConfig.IATA_API_KEY;


public class Other {

    public static int toBeFound = 0;
    public static int found = 0;

    public static ArrayList<String> lookFor = new ArrayList<String>();

    public static ArrayList<String> values = new ArrayList<String>();
    public static ArrayList<String> codes = new ArrayList<String>();

    public static void restart() {
        toBeFound = 0;
        found = 0;

        lookFor = new ArrayList<String>();

        values = new ArrayList<String>();
        codes = new ArrayList<String>();
    }

    public static class FindAirportTask extends AsyncTask<String, Void, String> {

        String codeToBeFound;

        public FindAirportTask(String code) {
            codeToBeFound = code;
        }

        private void getDataFromJson(String json) throws JSONException{
            JSONArray airportJson = new JSONArray(json);
                values.add(airportJson.getJSONObject(0).getString("label"));
                codes.add(airportJson.getJSONObject(0).getString("value"));
            found++;

        }

        @Override
        protected String doInBackground(String... params) {
                HttpURLConnection con = null;
                BufferedReader reader = null;
                String json = "";

                try {
                    final String baseUrl = "https://api.sandbox.amadeus.com/v1.2/airports/autocomplete?";
                    final String apiKeyParam = "apikey";
                    final String placeParam = "term";

                    Uri builtUri = Uri.parse(baseUrl).buildUpon()
                            .appendQueryParameter(apiKeyParam, AMADEUS_API_KEY)
                            .appendQueryParameter(placeParam, codeToBeFound)
                            .build();

                    URL url = new URL(builtUri.toString());

                    Log.i("Airport Finder URL", builtUri.toString());

                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.connect();

                    InputStream in = con.getInputStream();
                    StringBuffer buffer = new StringBuffer();

                    if(in == null) return null;

                    reader = new BufferedReader(new InputStreamReader(in));

                    String line;

                    while((line = reader.readLine()) != null) {
                        buffer.append(line + "\n");
                    }

                    if(buffer.length() == 0) return null;

                    json = buffer.toString();
                }
                catch (Exception e) {
                    Log.e("Airport Task IO1", e.toString());
                }
                finally {
                    if(con != null) con.disconnect();
                    if(reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            Log.e("Airport Task IO2", e.toString());
                        }
                    }
                }

            try {
                getDataFromJson(json);
            }
            catch (Exception e) {}

            return null;
        }
    }

    public static class FindAirlineTask extends AsyncTask<String, Void, String> {

        String codeToBeFound;

        public FindAirlineTask(String code) {
            codeToBeFound = code;
        }

        private void getDataFromJson(String json) throws JSONException {
            String name = new JSONObject(json).getJSONArray("response").getJSONObject(0).getString("name");
            values.add(name);
            codes.add(codeToBeFound);
            found++;
        }

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection con = null;
            BufferedReader reader = null;

            String json = "";

            try {
                final String baseUrl = "https://iatacodes.org/api/v6/airlines?";
                final String apiKeyParam = "api_key";
                final String codeParam = "code";

                Uri builtUri = Uri.parse(baseUrl).buildUpon()
                        .appendQueryParameter(apiKeyParam, IATA_API_KEY)
                        .appendQueryParameter(codeParam, codeToBeFound)
                        .build();

                URL url = new URL(builtUri.toString());

                Log.i("Airline Finder URL", builtUri.toString());

                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.connect();

                InputStream in = con.getInputStream();
                StringBuffer buffer = new StringBuffer();

                if(in == null) return null;

                reader = new BufferedReader(new InputStreamReader(in));

                String line;

                while((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if(buffer.length() == 0) return null;

                json = buffer.toString();

            }
            catch (Exception e) {
                Log.e("Airline Task", e.toString());
            }
            finally {
                if (con != null) con.disconnect();
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e("Airline Task", e.toString());
                    }
                }
            }

            try {
                getDataFromJson(json);
            }
            catch (Exception e) {}
            return null;
        }
    }
}
