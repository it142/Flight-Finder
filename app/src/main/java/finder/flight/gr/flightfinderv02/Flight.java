package finder.flight.gr.flightfinderv02;

import org.json.JSONArray;


public class Flight {

    public String originS,
                  destinationS,
                  price,
                  flightsOnGo,
                  flightsOnReturn;

    public JSONArray itineraries;

    // Ασχετα αλλα χρησιμα
    public int doneIndex;
    public boolean last;
}
