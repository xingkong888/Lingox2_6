package cn.lingox.android.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class PathReference implements Parcelable {
    // Constants
    public static final Creator<PathReference> CREATOR = new Creator<PathReference>() {
        public PathReference createFromParcel(Parcel in) {
            return new PathReference(in);
        }

        public PathReference[] newArray(int size) {
            return new PathReference[size];
        }
    };

    private String id;//评论id
    private String user_id;//发起评论的用户id
    private String user_name;//发起评论的用户名
    //    private String user_tar;//被评论的用户id
    private String path_id;//活动id
    private String content;//评论内容
    private ArrayList<PathReferenceReply> replys;

    public PathReference() {
        id = "";
        user_id = "";
        user_name = "";
        path_id = "";
        content = "";
        replys = new ArrayList<>();
    }

    // Parcelable
    public PathReference(Parcel in) {
        this.id = in.readString();
        this.user_id = in.readString();
        this.user_name = in.readString();
//        this.user_tar = in.readString();
        this.path_id = in.readString();
        this.content = in.readString();
        this.replys = in.createTypedArrayList(PathReferenceReply.CREATOR);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPath_id() {
        return path_id;
    }

    public void setPath_id(String path_id) {
        this.path_id = path_id;
    }

    public ArrayList<PathReferenceReply> getReplys() {
        return replys;
    }

    public void setReplys(ArrayList<PathReferenceReply> replys) {
        this.replys = replys;
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
                ", path_id=" + path_id +
                ", user_name=" + user_name +
                ", content=" + content +
                ", replys=" + replys +
                ", id=" + id +
                "]";
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(user_id);
        dest.writeString(user_name);
        dest.writeString(path_id);
        dest.writeString(content);
        dest.writeTypedList(replys);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}