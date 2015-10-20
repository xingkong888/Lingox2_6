package cn.lingox.android.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class Reference implements Parcelable {

    public static final Creator<Reference> CREATOR = new Creator<Reference>() {
        public Reference createFromParcel(Parcel in) {
            return new Reference(in);
        }

        public Reference[] newArray(int size) {
            return new Reference[size];
        }
    };
    private String id;
    private String title;
    private String content;
    private String user_src;
    private String user_tar;
    private String createAt;
    private String updatedAt;
    private String reply;

    public Reference(String id, String title, String content, String userSrcId,
                     String userTarId, String createAt, String updatedAt, String reply) {
        super();
        this.id = id;
        this.title = title;
        this.content = content;
        this.user_src = userSrcId;
        this.user_tar = userTarId;
        this.createAt = createAt;
        this.updatedAt = updatedAt;
        this.reply = reply;
    }

    // Parcelable
    public Reference(Parcel in) {
        String[] data = new String[8];

        in.readStringArray(data);
        this.id = data[0];
        this.title = data[1];
        this.content = data[2];
        this.user_src = data[3];
        this.user_tar = data[4];
        this.createAt = data[5];
        this.updatedAt = data[6];
        this.reply = data[7];
    }

    public String getCreateAt() {
        return createAt;
    }

    public void setCreateAt(String createAt) {
        this.createAt = createAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUserSrcId() {
        return user_src;
    }

    public void setUserSrcId(String userSrcId) {
        this.user_src = userSrcId;
    }

    public String getUserTarId() {
        return user_tar;
    }

    public void setUserTarId(String userTarId) {
        this.user_tar = userTarId;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    @Override
    public String toString() {
        return "Reference ["
                + "id=" + id
                + ", title=" + title
                + ", content=" + content
                + ", userSrcId=" + user_src
                + ", userTarId=" + user_tar
                + ", createAt=" + createAt
                + ", updatedAt=" + updatedAt
                + ", reply=" + reply
                + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{
                this.id,
                this.title,
                this.content,
                this.user_src,
                this.user_tar,
                this.createAt,
                this.updatedAt,
                this.reply
        });
    }
}
