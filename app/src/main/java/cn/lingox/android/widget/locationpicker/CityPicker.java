package cn.lingox.android.widget.locationpicker;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import cn.lingox.android.R;

public class CityPicker extends DialogFragment implements Comparator<City> {
    private EditText searchEditText;
    private ListView cityListView;
    private CityListAdapter adapter;
    private List<City> allCitiesList;
    private List<City> selectedCitiesList;
    private CityPickerListener listener;
    private Country country;

    public static CityPicker newInstance(String dialogTitle, Country country) {
        CityPicker picker = new CityPicker();
        Bundle bundle = new Bundle();
        bundle.putString("dialogTitle", dialogTitle);
        picker.setArguments(bundle);
        picker.setCountry(country);
        return picker;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public void setListener(CityPickerListener listener) {
        this.listener = listener;
    }

    public EditText getSearchEditText() {
        return searchEditText;
    }

    public ListView getCountryListView() {
        return cityListView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate view
        View view = inflater.inflate(R.layout.country_picker, null);

        // Get cities from the country
        allCitiesList = country.getCities();

        // Sort the all cities list based on city's name
        Collections.sort(allCitiesList, this);

        // Initialize selected cities with all cities
        selectedCitiesList = new ArrayList<City>();
        selectedCitiesList.addAll(allCitiesList);

        // Set dialog title if show as dialog
        Bundle args = getArguments();
        if (args != null) {
            String dialogTitle = args.getString("dialogTitle");
            getDialog().setTitle(dialogTitle);

            int width = getResources().getDimensionPixelSize(
                    R.dimen.cp_dialog_width);
            int height = getResources().getDimensionPixelSize(
                    R.dimen.cp_dialog_height);
            getDialog().getWindow().setLayout(width, height);
        }

        // Get view components
        searchEditText = (EditText) view
                .findViewById(R.id.country_picker_search);
        cityListView = (ListView) view
                .findViewById(R.id.country_picker_listview);

        // Set adapter
        adapter = new CityListAdapter(getActivity(), selectedCitiesList);
        cityListView.setAdapter(adapter);

        // Inform listener
        cityListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (listener != null) {
                    City city = selectedCitiesList.get(position);
                    listener.onSelectCity(city);
                }
            }
        });

        // Search for which countries matched user query
        searchEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                search(s.toString());
            }
        });

        return view;
    }

    private void search(String text) {
        selectedCitiesList.clear();

        for (City city : allCitiesList) {
            if (city.getName().toLowerCase(Locale.ENGLISH)
                    .contains(text.toLowerCase())) {
                selectedCitiesList.add(city);
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public int compare(City lhs, City rhs) {
        return lhs.getName().compareTo(rhs.getName());
    }
}
