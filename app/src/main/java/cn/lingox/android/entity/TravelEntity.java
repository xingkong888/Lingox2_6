package cn.lingox.android.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 旅行者发布内容类
 */
public class TravelEntity implements Parcelable {

    public static final Creator<TravelEntity> CREATOR = new Creator<TravelEntity>() {
        public TravelEntity createFromParcel(Parcel in) {
            return new TravelEntity(in);
        }

        public TravelEntity[] newArray(int size) {
            return new TravelEntity[size];
        }
    };

    private String traveling;//旅行地点
    private String describe;//详细描述
    private String tag;//标签
    private long fromTime;//开始时间
    private long toTime;//结束时间
    private String provide;//可提供

    //    private String url;//用户头像链接
//    private String userName;//用户名
    public TravelEntity() {
    }

    public TravelEntity(Parcel in) {
        this.traveling = in.readString();
        this.describe = in.readString();
        this.tag = in.readString();
        this.fromTime = in.readLong();
        this.toTime = in.readLong();
        this.provide = in.readString();
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public long getFromTime() {
        return fromTime;
    }

    public void setFromTime(long fromTime) {
        this.fromTime = fromTime;
    }

    public String getProvide() {
        return provide;
    }

    public void setProvide(String provide) {
        this.provide = provide;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public long getToTime() {
        return toTime;
    }

    public void setToTime(long toTime) {
        this.toTime = toTime;
    }

    public String getTraveling() {
        return traveling;
    }

    public void setTraveling(String traveling) {
        this.traveling = traveling;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.traveling);
        dest.writeString(this.describe);
        dest.writeString(this.tag);
        dest.writeLong(this.fromTime);
        dest.writeLong(this.toTime);
        dest.writeString(this.provide);
    }

    @Override
    public String toString() {
        return "traveling=" + traveling +
                ", describe=" + describe +
                ", tag=" + tag +
                ", fromTime=" + fromTime +
                ", toTime=" + toTime +
                ", provide=" + provide;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
