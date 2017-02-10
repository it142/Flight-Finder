package finder.flight.gr.flightfinderv02;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.Calendar;


public class FlightFragment extends Fragment {

    EditText fromView, toView, dd, dm, dy, rd, rm ,ry;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        fromView = (EditText) rootView.findViewById(R.id.editText_from);
        toView = (EditText) rootView.findViewById(R.id.editText_to);
        dd = (EditText) rootView.findViewById(R.id.editText_departD);
        dm = (EditText) rootView.findViewById(R.id.editText_departM);
        dy = (EditText) rootView.findViewById(R.id.editText_departY);
        rd = (EditText) rootView.findViewById(R.id.editText_returnD);
        rm = (EditText) rootView.findViewById(R.id.editText_returnM);
        ry = (EditText) rootView.findViewById(R.id.editText_returnY);

        fromView.setText("Thessaloniki");
        toView.setText("Athens");

        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, 1);

        dd.setText(c.get(Calendar.DAY_OF_MONTH) + "");
        dm.setText(c.get(Calendar.MONTH) + 1 + "");
        dy.setText(c.get(Calendar.YEAR) + "");

        c.add(Calendar.DAY_OF_MONTH, 2);
        rd.setText(c.get(Calendar.DAY_OF_MONTH) + "");
        rm.setText(c.get(Calendar.MONTH) + 1 + "");
        ry.setText(c.get(Calendar.YEAR) + "");

        return rootView;
    }

    public void searchFlight(View v) {
        //ΣΗΜΑΝΤΙΚΟ
        //Εδώ πρέπει να ελέγχεται η εγκυρότητα της ημερομηνίας.

        String d = dd.getText().toString();
        if(d.length() == 1) d = "0" + d;

        String m = dm.getText().toString();
        if(m.length() == 1) m = "0" + m;

        String departD = dy.getText().toString() + "-" +
                m + "-" + d;

        d = rd.getText().toString();
        if(d.length() == 1) d = "0" + d;

        m = rm.getText().toString();
        if(m.length() == 1) m = "0" + m;

        String returnD = ry.getText().toString() + "-" +
                m + "-" + d;

        String origin = fromView.getText().toString();
        String destination = toView.getText().toString();

        FlightData.FROM = origin;
        FlightData.TO = destination;
        FlightData.DEPART_DATE = departD;
        FlightData.RETURN_DATE = returnD;

        Intent intent = new Intent(getActivity(), SearchingActivity.class);
        startActivity(intent);
    }


}
