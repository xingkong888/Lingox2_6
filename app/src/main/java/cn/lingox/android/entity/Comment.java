package cn.lingox.android.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 对local的活动的评论
 */
public class Comment implements Parcelable {
    public static final Creator<Comment> CREATOR = new Creator<Comment>() {
        public Comment createFromParcel(Parcel in) {
            return new Comment(in);
        }

        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };
    private String id;//id---唯一标示
    private String path_id;//活动的id
    private String user_id;//评论人的id
    private String user_tar;//被回复人的id
    private String text;//内容
    private String createdAt;//创建日期

    //删除会造成后续错误
    public Comment() {
        this.id = "";
        this.path_id = "";
        this.user_id = "";
        this.user_tar = "";
        this.text = "";
        this.createdAt = "";
    }

    public Comment(String id, String path_id, String user_id, String text, String createdAt, String user_tar) {
        this.id = id;
        this.path_id = path_id;
        this.user_id = user_id;
        this.user_tar = user_tar;
        this.text = text;
        this.createdAt = createdAt;
    }

    // Parcelable
    public Comment(Parcel in) {
        this.id = in.readString();
        this.path_id = in.readString();
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

    public String getPathId() {
        return path_id;
    }

    public void setPathId(String path_id) {
        this.path_id = path_id;
    }

    public String getUserId() {
        return user_id;
    }

    public void setUserId(String user_id) {
        this.user_id = user_id;
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
                + ", path_id=" + path_id
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
        dest.writeString(this.path_id);
        dest.writeString(this.user_id);
        dest.writeString(this.user_tar);
        dest.writeString(this.text);
        dest.writeString(this.createdAt);
    }
}
