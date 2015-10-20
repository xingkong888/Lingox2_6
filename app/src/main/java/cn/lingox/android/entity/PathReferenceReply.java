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
    private String content;//回复内容

    // Parcelable
    public PathReferenceReply(Parcel in) {
        this.user_id = in.readString();
        this.content = in.readString();
    }

    @Override
    public String toString() {
        return "[ user_id=" + user_id +
                ", content=" + content +
                "]";
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(user_id);
        dest.writeString(content);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}