package cn.lingox.android.widget.locationpicker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import cn.lingox.android.R;

public class CityListAdapter extends BaseAdapter {
    List<City> cities;
    LayoutInflater inflater;

    public CityListAdapter(Context context, List<City> countries) {
        super();
        this.cities = countries;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return cities.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View cellView = convertView;
        Cell cell;
        City city = cities.get(position);
        if (convertView == null) {
            cell = new Cell();
            cellView = inflater.inflate(R.layout.row_city_picker, null);
            cell.textView = (TextView) cellView.findViewById(R.id.row_title);
            cellView.setTag(cell);
        } else {
            cell = (Cell) cellView.getTag();
        }
        cell.textView.setText(city.getName());
        return cellView;
    }

    static class Cell {
        public TextView textView;
    }

}
