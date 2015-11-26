package cn.lingox.android.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Andrew on 07/02/2015.
 */
public class TravelComment implements Parcelable {
    public static final Creator<TravelComment> CREATOR = new Creator<TravelComment>() {
        public TravelComment createFromParcel(Parcel in) {
            return new TravelComment(in);
        }

        public TravelComment[] newArray(int size) {
            return new TravelComment[size];
        }
    };
    private String id;
    private String demand_id;
    private String user_id;
    private String user_tar;
    private String text;
    private String createdAt;

    //删除会造成后续错误
    public TravelComment() {
        this.id = "";
        this.demand_id = "";
        this.user_id = "";
        this.user_tar = "";
        this.text = "";
        this.createdAt = "";
    }

    public TravelComment(String id, String demand_id, String user_id, String text, String createdAt, String user_tar) {
        this.id = id;
        this.demand_id = demand_id;
        this.user_id = user_id;
        this.user_tar = user_tar;
        this.text = text;
        this.createdAt = createdAt;
    }

    // Parcelable
    public TravelComment(Parcel in) {
        this.id = in.readString();
        this.demand_id = in.readString();
        this.user_id = in.readString();
        this.user_tar = in.readString();
        this.text = in.readString();
        this.createdAt = in.readString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getDemand_id() {
        return demand_id;
    }

    public void setDemand_id(String demand_id) {
        this.demand_id = demand_id;
    }

    public String getUser_tar() {
        return user_tar;
    }

    public void setUser_tar(String user_tar) {
        this.user_tar = user_tar;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Comment ["
                + "id=" + id
                + ", demand_id=" + demand_id
                + ", user_id=" + user_id
                + ", user_tar=" + user_tar
                + ", text=" + text
                + ", createdAt=" + createdAt
                + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.demand_id);
        dest.writeString(this.user_id);
        dest.writeString(this.user_tar);
        dest.writeString(this.text);
        dest.writeString(this.createdAt);
    }
}
