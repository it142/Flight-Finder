package finder.flight.gr.flightfinderv02;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;

import java.util.ArrayList;


public class FlightData {

    // Found airports
    public static ArrayList<String> from_airport_code = new ArrayList<String>();
    public static ArrayList<String> from_airport_label = new ArrayList<String>();
    public static ArrayList<String> to_airport_code = new ArrayList<String>();
    public static ArrayList<String> to_airport_label = new ArrayList<String>();

    // Initial Data
    public static String DEPART_DATE;
    public static String RETURN_DATE;
    public static String FROM;
    public static String TO;

    public static String CURRENCY = "EUR€";
    public static String NOR;
    public static boolean CELSIUS;

    // Searching Animation
    public static AnimationDrawable animation;
    public static boolean animation_loaded;

    // Final Flight Results
    public static ArrayList<Flight> flights;

    // Final Weather Results
    public static Weather weather_origin;
    public static Weather weather_dest;

    //Found Airlines
    public static ArrayList<String> codes = new ArrayList<String>();
    public static ArrayList<String> airlines = new ArrayList<String>();

    // Details
    public static ArrayList<Detail> details_depart;
    public static ArrayList<Detail> details_return;

    // Current Flight
    public static int position;
    public static int index;

    public static void restart() {
        // This class
        from_airport_code = new ArrayList<String>();
        from_airport_label = new ArrayList<String>();
        to_airport_code = new ArrayList<String>();
        to_airport_label = new ArrayList<String>();
        DEPART_DATE = new String();
        RETURN_DATE = new String();
        FROM = new String();
        TO = new String();
        weather_origin = null;
        weather_dest = null;
        codes = new ArrayList<String>();
        airlines = new ArrayList<String>();

        // FetchFlights
        FetchFlights.weather_origin = false;
        FetchFlights.weather_dest = false;
        FetchFlights.shouldCalcWeather1 = -1;
        FetchFlights.shouldCalcWeather2 = -1;
        FetchFlights.airportsFound = 0;
        FetchFlights.resultsFound = 0;
        FetchFlights.wrong = false;

        // Other
        Other.restart();
    }

}
