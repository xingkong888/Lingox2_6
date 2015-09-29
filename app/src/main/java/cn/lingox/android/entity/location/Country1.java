package cn.lingox.android.entity.location;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/9/10.
 */
public class Country1 {
    private String country = "";
    private String countryCode = "";
    private ArrayList<Provinces> provinces = new ArrayList<>();

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public ArrayList<Provinces> getProvinces() {
        return provinces;
    }

    public void setProvinces(ArrayList<Provinces> provinces) {
        this.provinces = provinces;
    }

    @Override
    public String toString() {
        return "country=" + country
                + ",provinces=" + provinces.toString();
    }
}
