package finder.flight.gr.flightfinderv02;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


public class FlightAdapter extends ArrayAdapter<Flight> {

    Context context;
    ArrayList<Flight> data;

    public FlightAdapter(Context context, ArrayList<Flight> data) {
        super(context, 0, data);

        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ViewHolder holder = null;

        if(rowView == null) {
            rowView = LayoutInflater.from(context).inflate(R.layout.list_item_flight, parent, false);
            holder = new ViewHolder(rowView);
            rowView.setTag(holder);
        }
        else {
            holder = (ViewHolder) rowView.getTag();
        }

        holder.originView.setText(data.get(position).originS);
        holder.destView.setText(data.get(position).destinationS);
        holder.priceView.setText(data.get(position).price + FlightData.CURRENCY.substring(3,4));

        holder.stopsGoView.setImageResource(context.getResources().getIdentifier(
                "arrows" + Integer.toString(Integer.parseInt(data.get(position).flightsOnGo) - 1),
                "drawable",
                context.getPackageName()
        ));

        holder.stopsReturnView.setImageResource(context.getResources().getIdentifier(
                "arrows" + Integer.toString(Integer.parseInt(data.get(position).flightsOnReturn) - 1),
                "drawable",
                context.getPackageName()
        ));


        return rowView;
    }

    public static class ViewHolder {

        public TextView originView,
                        destView,
                        priceView;
        public ImageView stopsGoView,
                         stopsReturnView;

        public ViewHolder(View view) {
            originView = (TextView) view.findViewById(R.id.list_text_view_origin);
            destView = (TextView) view.findViewById(R.id.list_text_view_dest);
            priceView = (TextView) view.findViewById(R.id.list_text_view_price);
            stopsGoView = (ImageView) view.findViewById(R.id.list_image_view);
            stopsReturnView = (ImageView) view.findViewById(R.id.list_image_view2);
        }


    }
}
