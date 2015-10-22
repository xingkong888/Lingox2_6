package cn.lingox.android.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class PathReferenceReply implements Parcelable {
    // Constants
    public static final Creator<PathReferenceReply> CREATOR = new Creator<PathReferenceReply>() {
        public PathReferenceReply createFromParcel(Parcel in) {
            return new PathReferenceReply(in);
        }

        public PathReferenceReply[] newArray(int size) {
            return new PathReferenceReply[size];
        }
    };

    private String user_id;//回复的用户id
    private String user_name;//回复的用户名
    private String content;//回复内容

    public PathReferenceReply() {
        user_id = "";
        content = "";

    }

    public PathReferenceReply(String user_id, String name, String content) {
        this.content = content;
        this.user_name = name;
        this.user_id = user_id;
    }

    // Parcelable
    public PathReferenceReply(Parcel in) {
        this.user_id = in.readString();
        this.user_name = in.readString();
        this.content = in.readString();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    @Override
    public String toString() {
        return "[ user_id=" + user_id +
                ", name=" + user_name +
                ", content=" + content +
                "]";
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(user_id);
        dest.writeString(user_name);
        dest.writeString(content);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}