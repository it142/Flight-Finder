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
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.jar.Pack200;

import javax.xml.parsers.FactoryConfigurationError;

import static finder.flight.gr.flightfinderv02.BuildConfig.AMADEUS_API_KEY;
import static finder.flight.gr.flightfinderv02.BuildConfig.IATA_API_KEY;
import static finder.flight.gr.flightfinderv02.BuildConfig.OPEN_WEATHER_MAP_API_KEY;


public class FetchFlights {
    public static boolean weather_origin = false;
    public static boolean weather_dest = false;
    public static int shouldCalcWeather1 = -1;
    public static int shouldCalcWeather2 = -1;

    public static int airportsFound = 0;
    public static int resultsFound = 0;

    public static boolean wrong = false;

    public static void fetch(String... params) {

        if(!checkDatesValid(params[2], params[3])) {
            wrong = true;
            return;
        }

        FlightData.flights = new ArrayList<Flight>();

        shouldCalcWeather1 = checkIfShouldCalcWeather(params[2]);
        shouldCalcWeather2 = checkIfShouldCalcWeather(params[3]);

        if(shouldCalcWeather1 >= 0 && shouldCalcWeather1 <= 16) {
            WeatherFinder task = new WeatherFinder();
            task.execute(Integer.toString(shouldCalcWeather1), params[0], "from");

        }
        if(shouldCalcWeather2 >= 0 && shouldCalcWeather2 <= 16) {
            WeatherFinder task = new WeatherFinder();
            task.execute(Integer.toString(shouldCalcWeather2), params[1], "to");

        }

        AirportFinder task1 = new AirportFinder();
        task1.execute(params[0], "from", "false");

        AirportFinder task2 = new AirportFinder();
        task2.execute(params[1], "to", "false");
    }

    private static boolean checkDatesValid(String date1, String date2) {
        if(isLegalDate(date1) && isLegalDate(date2)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            try {
                Date dateOne = dateFormat.parse(date1);
                Date dateTwo = dateFormat.parse(date2);

                long difference = dateTwo.getTime() - dateOne.getTime();

                if(difference >= 0) return true;
            }
            catch (Exception e) {
                Log.e("Wrong Date Format", e.toString());
            }
        }

        return false;
    }

    private static boolean isLegalDate(String s) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);
        return sdf.parse(s, new ParsePosition(0)) != null;
    }

    private static int checkIfShouldCalcWeather(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        try {
            Date date2 = dateFormat.parse(date);

            long difference = date2.getTime() - Calendar.getInstance().getTimeInMillis();

            int elapsedDays = (int) (difference / (1000 * 60 * 60 * 24));


            Log.i("Elapsed Days", elapsedDays + "");

            if(elapsedDays <= 16) return elapsedDays;
        }
        catch (Exception e) {
            Log.e("Wrong Date Format", e.toString());
        }

        return -1;
    }

    public static class WeatherFinder extends AsyncTask<String, Void, Weather> {

        private Weather getDataFromJson(String json, int index, String to) throws JSONException {

            JSONArray list = new JSONObject(json).getJSONArray("list");

            Weather w = new Weather();
            JSONObject obj = list.getJSONObject(index);
            w.temp = obj.getJSONObject("temp");
            w.weather = obj.getJSONArray("weather").getJSONObject(0);

            w.id = w.weather.getInt("id");

            if(to.equals("to")) {
                FlightData.weather_dest = w;
                FetchFlights.weather_dest = true;
            }
            else {
                FlightData.weather_origin = w;
                FetchFlights.weather_origin = true;
            }

            return null;
        }

        @Override
        protected Weather doInBackground(String... params) {
            HttpURLConnection con = null;
            BufferedReader reader = null;
            String json = "";

            try {
                final String baseUrl = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String apiKeyParam = "appid";
                final String placeParam = "q";
                final String unitsParam = "units";
                final String cntParam = "cnt";

                Uri builtUri = Uri.parse(baseUrl).buildUpon()
                        .appendQueryParameter(apiKeyParam, OPEN_WEATHER_MAP_API_KEY)
                        .appendQueryParameter(placeParam, params[1])
                        .appendQueryParameter(unitsParam, "metric")
                        .appendQueryParameter(cntParam, "16")
                        .build();

                URL url = new URL(builtUri.toString());

                Log.i("Weather Finder URL", builtUri.toString());

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
                Log.e("Weather Task IO1", e.toString());
            }
            finally {
                if(con != null) con.disconnect();
                if(reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e("Weather Task IO2", e.toString());
                    }
                }
            }

            try {
                return getDataFromJson(json, Integer.parseInt(params[0]), params[2]);
            }
            catch (JSONException e) {
                Log.e("Weather Task JSON", e.toString());
            }
            return null;
        }
    }

    public static class AirportFinder extends AsyncTask<String, Void, String> {

        private String getAirportFromJson(String json, boolean isTo, boolean justLoad) throws JSONException{
            JSONArray airportJson = new JSONArray(json);
            for (int i = 0; i < airportJson.length(); i++) {
                String[] data = new String[2];
                data[0] = airportJson.getJSONObject(i).getString("value");
                data[1] = airportJson.getJSONObject(i).getString("label");
                Log.i("Airport Task", data[1]);
                if (isTo) {
                    FlightData.to_airport_code.add(data[0]);
                    FlightData.to_airport_label.add(data[1]);
                }
                else {
                    FlightData.from_airport_code.add(data[0]);
                    FlightData.from_airport_label.add(data[1]);
                }


            }

            if(justLoad) return null;
            if(isTo) return "good";
            else return null;
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
                        .appendQueryParameter(placeParam, params[0])
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
                return getAirportFromJson(json, (params[1] == "to"), Boolean.parseBoolean(params[2]));
            } catch (JSONException e) {
                Log.e("Airport Task JSON", e.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if(s != null) {
                FetchFlights.airportsFound = FlightData.from_airport_code.size() * FlightData.to_airport_code.size();
                Log.i("CHECKING", FetchFlights.airportsFound + " " + FetchFlights.resultsFound);

                if(FetchFlights.airportsFound == 0) {
                    FetchFlights.wrong = true;
                    return;
                }


                int k = 0;
                for(int i = 0; i < FlightData.from_airport_code.size(); i++) {
                    for (int j = 0; j < FlightData.to_airport_code.size(); j++) {

                        FlightFinder task = new FlightFinder();
                        task.execute(
                                FlightData.from_airport_code.get(i),
                                FlightData.to_airport_code.get(j),
                                FlightData.DEPART_DATE,
                                FlightData.RETURN_DATE,
                                FlightData.CURRENCY.substring(0, 3),
                                Integer.toString(k)
                        );
                        k++;
                    }
                }
            }
        }
    }

    public static class FlightFinder extends AsyncTask<String, Void, String[]> {

        public FlightFinder() {
        }

        private String getFlightsFromJson(String json) throws JSONException {
            return new JSONObject(json).getJSONArray("results").toString();
        }

        @Override
        protected String[] doInBackground(String... params) {
            HttpURLConnection con = null;
            BufferedReader reader = null;

            String json = "";
            try {

                final String baseUrl = "https://api.sandbox.amadeus.com/v1.2/flights/low-fare-search?";
                final String apiKeyParam = "apikey";
                final String originParam = "origin";
                final String destParam = "destination";
                final String departParam = "departure_date";
                final String returnParam = "return_date";
                final String curParam = "currency";
                final String numberParam = "number_of_results";

                Uri builtUri = null;
                if(FlightData.NOR == "all") {
                    builtUri = Uri.parse(baseUrl).buildUpon()
                            .appendQueryParameter(apiKeyParam, AMADEUS_API_KEY)
                            .appendQueryParameter(originParam, params[0])
                            .appendQueryParameter(destParam, params[1])
                            .appendQueryParameter(departParam, params[2])
                            .appendQueryParameter(returnParam, params[3])
                            .appendQueryParameter(curParam, params[4])
                            //.appendQueryParameter(numberParam, "1")
                            .build();
                }
                else {
                    builtUri = Uri.parse(baseUrl).buildUpon()
                            .appendQueryParameter(apiKeyParam, AMADEUS_API_KEY)
                            .appendQueryParameter(originParam, params[0])
                            .appendQueryParameter(destParam, params[1])
                            .appendQueryParameter(departParam, params[2])
                            .appendQueryParameter(returnParam, params[3])
                            .appendQueryParameter(curParam, params[4])
                            .appendQueryParameter(numberParam, FlightData.NOR)
                            .build();
                }

                Log.i("Flight Task", builtUri.toString());

                URL url = new  URL(builtUri.toString());

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
                Log.i("Flight Task", json);


            }
            catch(Exception e) {
                Log.e("Flight Task", e.toString());
            }
            finally {
                if(con != null) con.disconnect();
                if(reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e("Flight Task", e.toString());
                    }
                }
            }

            try {
                return new String[] {getFlightsFromJson(json), params[5] };
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String[] params) {
            try {
                JSONArray jsonArray = new JSONArray(params[0]);

                    for(int i = 0; i < jsonArray.length(); i++) {
                        AirlineFinder task = new AirlineFinder();

                        try {
                            if(i == jsonArray.length() - 1) task.execute(jsonArray.getJSONObject(i).toString(), params[1], "last");
                            else task.execute(jsonArray.getJSONObject(i).toString(), params[1], "");
                        }
                        catch (JSONException e) {
                            Log.e("Flight Finder", e.toString());
                        }
                    }

            }
            catch (Exception e) {
                FetchFlights.resultsFound++;
                Log.i("CHECKING1", FetchFlights.airportsFound + " " + FetchFlights.resultsFound);
            }

        }
    }

    public static class AirlineFinder extends AsyncTask<String, Void, Flight> {

        public AirlineFinder() {
        }

        private Flight createFlightFromData(String... params) throws JSONException {
            Flight f = new Flight();

            f.doneIndex = Integer.parseInt(params[1]);
            if(params[2].equals("last")) f.last = true;
            else f.last = false;

            JSONObject res = new JSONObject(params[0]);

            JSONArray iti = res.getJSONArray("itineraries");
            f.itineraries = iti;

            JSONArray flightsOnGo = iti.getJSONObject(0).getJSONObject("outbound").getJSONArray("flights");
            f.flightsOnGo = flightsOnGo.length() + "";
            f.originS = flightsOnGo.getJSONObject(0).getJSONObject("origin").getString("airport");

            JSONArray flightsOnReturn = iti.getJSONObject(0).getJSONObject("inbound").getJSONArray("flights");
            f.flightsOnReturn = flightsOnReturn.length() + "";
            f.destinationS = flightsOnReturn.getJSONObject(0).getJSONObject("origin").getString("airport");

            String price = res.getJSONObject("fare").getString("total_price");
            f.price = price;

            return f;
        }

        private String searchAirline(String code) {
            HttpURLConnection con = null;
            BufferedReader reader = null;

            String json = "";

            try {
                final String baseUrl = "https://iatacodes.org/api/v6/airlines?";
                final String apiKeyParam = "api_key";
                final String codeParam = "code";

                Uri builtUri = Uri.parse(baseUrl).buildUpon()
                        .appendQueryParameter(apiKeyParam, IATA_API_KEY)
                        .appendQueryParameter(codeParam, code)
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
                return new JSONObject(json).getJSONArray("response").getJSONObject(0).getString("name");
            }
            catch (JSONException e) {
                Log.e("Airline Finder JSON", e.toString());
            }
            return null;
        }

        @Override
        protected Flight doInBackground(String... params) {
            try {
                JSONObject res = new JSONObject(params[0]);

                JSONArray flights = res.getJSONArray("itineraries").getJSONObject(0).getJSONObject("outbound").getJSONArray("flights");
                for(int i = 0; i < flights.length(); i++) {
                    String code = flights.getJSONObject(i).getString("operating_airline");
                    if(!FlightData.codes.contains(code)) {
                        FlightData.codes.add(code);

                        String airline = searchAirline(code);
                        FlightData.airlines.add(airline);

                        Log.i("AIRLINE", code + ": " + airline);
                    }
                }
            } catch (JSONException e) {
                Log.e("Airline Finder JSON", e.toString());
            }


            try {
                return createFlightFromData(params[0], params[1], params[2]);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Flight f) {

            if(f != null) {
                FlightData.flights.add(f);
                if(f.last) FetchFlights.resultsFound++;
                Log.i("CHECKING2", FetchFlights.airportsFound + " " + FetchFlights.resultsFound);
            }
        }
    }
}
