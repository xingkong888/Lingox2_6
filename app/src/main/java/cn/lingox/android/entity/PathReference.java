package cn.lingox.android.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * 活动的评论
 */
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
    private String path_id;//活动id
    private String content;//评论内容
    private ArrayList<PathReferenceReply> replies;//该条评论的回复评论

    public PathReference() {
        id = "";
        user_id = "";
        path_id = "";
        content = "";
        replies = new ArrayList<>();
    }

    // Parcelable
    public PathReference(Parcel in) {
        this.id = in.readString();
        this.user_id = in.readString();
        this.path_id = in.readString();
        this.content = in.readString();
        this.replies = in.createTypedArrayList(PathReferenceReply.CREATOR);
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

    public ArrayList<PathReferenceReply> getReplies() {
        return replies;
    }

    public void setReplies(ArrayList<PathReferenceReply> replies) {
        this.replies = replies;
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
                ", path_id=" + path_id +
                ", content=" + content +
                ", replies=" + replies +
                ", id=" + id +
                "]";
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(user_id);
        dest.writeString(path_id);
        dest.writeString(content);
        dest.writeTypedList(replies);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}