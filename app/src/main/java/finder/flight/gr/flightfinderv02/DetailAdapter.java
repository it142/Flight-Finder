package finder.flight.gr.flightfinderv02;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;


public class DetailAdapter extends ArrayAdapter<Detail> {

    Context context;
    ArrayList<Detail> data;

    public DetailAdapter(Context context, ArrayList<Detail> data) {
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
            rowView = LayoutInflater.from(context).inflate(R.layout.list_item_detail, parent, false);
            holder = new ViewHolder(rowView);
            rowView.setTag(holder);
        }
        else {
            holder = (ViewHolder) rowView.getTag();
        }

        holder.airlineView.setText(data.get(position).airline);
        holder.airportOriginView.setText(data.get(position).airportOrigin);
        holder.airportDestView.setText(data.get(position).airportDest);
        holder.timeStartView.setText(data.get(position).timeStart);
        holder.timeFinishView.setText(data.get(position).timeFinish);

        return rowView;
    }

    public static class ViewHolder {
        public TextView airlineView,
                        timeStartView,
                        timeFinishView,
                        airportOriginView,
                        airportDestView;

        public ViewHolder(View view) {
            airlineView = (TextView) view.findViewById(R.id.list_item_textview_airline);
            airportOriginView = (TextView) view.findViewById(R.id.list_item_textview_origin);
            airportDestView = (TextView) view.findViewById(R.id.list_item_textview_dest);
            timeStartView = (TextView) view.findViewById(R.id.list_item_textview_time_start);
            timeFinishView = (TextView) view.findViewById(R.id.list_item_textview_time_finish);
        }
    }
}
