package cn.lingox.android.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 回复评论活动的评论
 */
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

    public PathReferenceReply() {
        user_id = "";
        content = "";
    }

    public PathReferenceReply(String user_id, String content) {
        this.content = content;
        this.user_id = user_id;
    }

    // Parcelable
    public PathReferenceReply(Parcel in) {
        this.user_id = in.readString();
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