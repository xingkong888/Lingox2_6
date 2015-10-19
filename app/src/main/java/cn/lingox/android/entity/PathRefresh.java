package cn.lingox.android.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class PathRefresh implements Parcelable {
    // Constants
    public static final Creator<PathRefresh> CREATOR = new Creator<PathRefresh>() {
        public PathRefresh createFromParcel(Parcel in) {
            return new PathRefresh(in);
        }

        public PathRefresh[] newArray(int size) {
            return new PathRefresh[size];
        }
    };

    private String userSrc;//发起评论的用户id
    private String userTar;//被评论的用户id
    private String pathId;//活动id
    private String content;//评论内容
//    private String replay;

    // Parcelable
    public PathRefresh(Parcel in) {
        this.userSrc = in.readString();
        this.userTar = in.readString();
        this.pathId = in.readString();
        this.content = in.readString();
//        this.replay=in.readString();
    }

    @Override
    public String toString() {
        return "[ userSrc=" + userSrc +
                ", userTar=" + userTar +
                ", pathId=" + pathId +
                ", content=" + content +
//                ", replay="+replay+
                "]";
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userSrc);
        dest.writeString(userTar);
        dest.writeString(pathId);
        dest.writeString(content);
//        dest.writeString(replay);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}