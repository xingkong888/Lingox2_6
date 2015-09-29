package cn.lingox.android.widget.locationpicker;

import org.json.JSONArray;

import java.util.ArrayList;

public class Country {
    private String code;
    private String name;
    private ArrayList<City> cities;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<City> getCities() {
        return cities;
    }

    public void setCities(ArrayList<City> cities) {
        this.cities = cities;
    }

    public void setCities(JSONArray jsonCities) throws Exception {
        if (code == null)
            throw new Exception("This Country1 has no code. A code must be set before we can set the cities from a JSONArray");

        // Instantiate or empty the cities list
        if (cities == null)
            cities = new ArrayList<City>();
        else
            cities.clear();

        for (int i = 0; i < jsonCities.length(); i++) {
            City city = new City();
            city.setCode(code + String.valueOf(i));
            city.setName(jsonCities.getString(i));
            cities.add(city);
        }
    }
}