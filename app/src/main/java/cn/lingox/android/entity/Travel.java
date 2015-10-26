package cn.lingox.android.entity;

import android.os.Parcel;
import android.os.Parcelable;

import cn.lingox.android.app.LingoXApplication;

/**
 * Created by Administrator on 2015/6/25.
 */
public class Travel implements Parcelable {

    public static final Creator<Travel> CREATOR = new Creator<Travel>() {
        public Travel createFromParcel(Parcel in) {
            return new Travel(in);
        }

        public Travel[] newArray(int size) {
            return new Travel[size];
        }
    };

    private String country;
    private String province;
    private String city;
    private long startTime;
    private long endTime;
    private String id;
    private String createTime;
    private String updateTime;
    private String tags;

    public Travel() {
        country = "";
        province = "";
        city = "";
        startTime = 0;
        endTime = 0;
        id = "";
        createTime = "";
        updateTime = "";
        tags = "";
    }

    public Travel(Parcel in) {
        this.country = in.readString();
        this.province = in.readString();
        this.city = in.readString();
        this.startTime = in.readLong();
        this.endTime = in.readLong();
        this.id = in.readString();
        this.createTime = in.readString();
        this.updateTime = in.readString();
        this.tags = in.readString();

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
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

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.country);
        dest.writeString(this.province);
        dest.writeString(this.city);
        dest.writeLong(this.startTime);
        dest.writeLong(this.endTime);
        dest.writeString(this.id);
        dest.writeString(this.createTime);
        dest.writeString(this.updateTime);
        dest.writeString(this.tags);

    }

    @Override
    public String toString() {
        return "country=" + country
                + ", province=" + province
                + ", city=" + city
                + ", start=" + startTime
                + ", end=" + endTime
                + ", id=" + id
                + ", createTime=" + createTime
                + ", updateTime=" + updateTime
                + ", tags=" + tags;

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

    @Override
    public int describeContents() {
        return 0;
    }
}
