package cn.lingox.android.entity;

import android.os.Parcel;
import android.os.Parcelable;

import cn.lingox.android.app.LingoXApplication;

/**
 * Created by Andrew on 23/01/2015.
 */
public class Photo implements Parcelable {
    public static final Creator<Photo> CREATOR = new Creator<Photo>() {
        public Photo createFromParcel(Parcel in) {
            return new Photo(in);
        }

        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };
    private String id;            // DB id
    private String description;
    private String url;
    private String country;
    private String province;
    private String city;
    private String tags;

    public Photo() {
        this.id = "";
        this.description = "";
        this.url = "";
        this.country = "";
        this.province = "";
        this.city = "";
        this.tags = "";
    }

    public Photo(String id,
                 String description,
                 String url, String country, String province, String city, String tags) {
        this.id = id;
        this.description = description;
        this.url = url;
        this.country = country;
        this.province = province;
        this.city = city;
        this.tags = tags;
    }

    // Parcelable
    public Photo(Parcel in) {
        this.id = in.readString();
        this.description = in.readString();
        this.url = in.readString();
        this.country = in.readString();
        this.province = in.readString();
        this.city = in.readString();
        this.tags = in.readString();
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    @Override
    public String toString() {
        return "Photo ["
                + "id=" + id
                + ", description=" + description
                + ", url=" + url
                + ", country=" + country
                + ", province=" + province
                + ", city=" + city
                + ",tags=" + tags
                + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.description);
        dest.writeString(this.url);
        dest.writeString(this.country);
        dest.writeString(this.province);
        dest.writeString(this.city);
        dest.writeString(this.tags);
    }

    public String getLocation() {
        return LingoXApplication.getInstance().getLocation(
                getCountry(), getProvince(), getCity()
        );
    }

    public void setLocation(String location) {
        String[] str = location.split(", ");
        switch (str.length) {
            case 1://只有国家
                setCountry(str[0]);
                break;
            case 2://国家、省份
                setCountry(str[0]);
                setProvince(str[1]);
                break;
            case 3://国家、省份、城市
                setCountry(str[0]);
                setProvince(str[1]);
                setCity(str[2]);
                break;
        }
    }
}
