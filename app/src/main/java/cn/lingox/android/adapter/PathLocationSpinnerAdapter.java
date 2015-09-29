package cn.lingox.android.adapter;

import android.app.Activity;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import cn.lingox.android.R;

/**
 * Created by Andrew on 05/02/2015.
 */
public class PathLocationSpinnerAdapter extends ArrayAdapter<Pair<String, String>> {
    // Data Elements
    private Activity context;
    private ArrayList<Pair<String, String>> pathLocationList;
    private LayoutInflater inflater;

    public PathLocationSpinnerAdapter(Activity context, ArrayList<Pair<String, String>> pLocList) {
        super(context, R.layout.row_spinner_path_loc, pLocList);
        this.context = context;
        this.pathLocationList = pLocList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return pathLocationList.size();
    }

    @Override
    public Pair<String, String> getItem(int position) {
        return pathLocationList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Pair<String, String> locationPair = pathLocationList.get(position);

        View rowView = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
        TextView text = (TextView) rowView.findViewById(android.R.id.text1);
        if (locationPair.first == null)
            text.setText(context.getResources().getString(R.string.all_location));

        return rowView;
    }

    @Override
    public View getDropDownView(final int position, View convertView, ViewGroup parent) {
        Pair<String, String> locationPair = pathLocationList.get(position);

        View rowView = inflater.inflate(R.layout.row_spinner_path_loc, parent, false);
        TextView text = (TextView) rowView.findViewById(R.id.path_loc_text);
        ImageView deleteButton = (ImageView) rowView.findViewById(R.id.delete_path_loc);
        if (locationPair.first == null)
            text.setText(context.getResources().getString(R.string.all_location));
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pathLocationList.remove(position);
                notifyDataSetChanged();
            }
        });

        // "All Locations"
        if (position == 0)
            rowView.findViewById(R.id.delete_path_loc).setVisibility(View.GONE);
        // Add another of the above when we implement "My Location" for row 1
        return rowView;
    }
}
