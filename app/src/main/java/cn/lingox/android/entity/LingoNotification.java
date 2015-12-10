package cn.lingox.android.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class LingoNotification implements Parcelable {
    // Constants
    public static final int NUMBER_OF_NOTIFICATION_TYPES = 11;
    //    public static final int NUMBER_OF_NOTIFICATION_TYPES = 7;
    public static final int TYPE_USER_FOLLOWED = 1;
    public static final int TYPE_PATH_JOINED = 2;
    public static final int TYPE_PATH_COMMENT = 3;
    public static final int TYPE_PATH_CHANGE = 4;
    public static final int TYPE_USER_COMMENT = 5;
    public static final int TYPE_INDENT_FINISH = 6;
    public static final int TYPE_TRAVEL_LIKED = 10;

    public static final Creator<LingoNotification> CREATOR = new Creator<LingoNotification>() {
        public LingoNotification createFromParcel(Parcel in) {
            return new LingoNotification(in);
        }

        public LingoNotification[] newArray(int size) {
            return new LingoNotification[size];
        }
    };
    private String id;
    private String user_id;
    private String user_src;
    private int type;
    private boolean read;
    private boolean seen;
    private boolean deleted;
    private String path_id;//local的活动id
    private String demand_id;//travel的活动id
    private String comment_id;
    private String createdAt;

    public LingoNotification() {
        this.id = "";
        this.user_id = "";
        this.user_src = "";
        this.type = 0;
        this.read = false;
        this.seen = false;
        this.deleted = false;
        this.path_id = "";
        this.demand_id = "";
        this.comment_id = "";
        this.createdAt = "";
    }

    public LingoNotification(String id, String user_id, String user_src, int type, boolean read, boolean seen, boolean deleted, String path_id, String demand_id, String comment_id, String createdAt) {
        this.id = id;
        this.user_id = user_id;
        this.user_src = user_src;
        this.type = type;
        this.read = read;
        this.seen = seen;
        this.deleted = deleted;
        this.path_id = path_id;
        this.demand_id = demand_id;
        this.comment_id = comment_id;
        this.createdAt = createdAt;
    }

    // Parcelable
    public LingoNotification(Parcel in) {
        this.id = in.readString();
        this.user_id = in.readString();
        this.user_src = in.readString();
        this.type = in.readInt();
        this.read = in.readByte() != 0;
        this.seen = in.readByte() != 0;
        this.deleted = in.readByte() != 0;
        this.path_id = in.readString();
        this.demand_id = in.readString();
        this.comment_id = in.readString();
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

    public String getUser_src() {
        return user_src;
    }

    public void setUser_src(String user_src) {
        this.user_src = user_src;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean getRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public boolean getSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getPath_id() {
        return path_id;
    }

    public void setPath_id(String path_id) {
        this.path_id = path_id;
    }

    public String getDemand_id() {
        return demand_id;
    }

    public void setDemand_id(String demand_id) {
        this.demand_id = demand_id;
    }

    public String getComment_id() {
        return comment_id;
    }

    public void setComment_id(String comment_id) {
        this.comment_id = comment_id;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "LingoNotification ["
                + "id=" + id
                + ", user_id=" + user_id
                + ", user_src=" + user_src
                + ", type=" + type
                + ", read=" + read
                + ", seen=" + seen
                + ", deleted=" + deleted
                + ", path_id=" + path_id
                + ", demand_id=" + demand_id
                + ", comment_id=" + comment_id
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
        dest.writeString(this.user_id);
        dest.writeString(this.user_src);
        dest.writeInt(this.type);
        dest.writeByte((byte) (this.read ? 1 : 0));
        dest.writeByte((byte) (this.seen ? 1 : 0));
        dest.writeByte((byte) (this.deleted ? 1 : 0));
        dest.writeString(this.path_id);
        dest.writeString(this.demand_id);
        dest.writeString(this.comment_id);
        dest.writeString(this.createdAt);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LingoNotification) {
            LingoNotification notification = (LingoNotification) obj;
            return this.getId().equals(notification.getId());
        }
        return super.equals(obj);
    }
}
