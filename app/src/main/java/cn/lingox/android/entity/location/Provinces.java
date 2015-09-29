package cn.lingox.android.entity.location;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/9/10.
 */
public class Provinces {
    private String provinces = "";
    private ArrayList<String> city = new ArrayList<>();

    public String getProvinces() {
        return provinces;
    }

    public void setProvinces(String provinces) {
        this.provinces = provinces;
    }

    public ArrayList<String> getCity() {
        return city;
    }

    public void setCity(ArrayList<String> city) {
        this.city = city;
    }

    @Override
    public String toString() {
        return "provinces=" + provinces
                + ",city=" + city.toString();
    }
}
