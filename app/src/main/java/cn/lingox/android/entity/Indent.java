package cn.lingox.android.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class Indent implements Parcelable {
    public static final Creator<Indent> CREATOR = new Creator<Indent>() {
        public Indent createFromParcel(Parcel in) {
            return new Indent(in);
        }

        public Indent[] newArray(int size) {
            return new Indent[size];
        }
    };
    private String id = "";//id
    private String userId = "";//自己的id
    private String tarId = "";//申请人的id
    private int state = 0;//订单状态 1待处理 2申请者取消  3拒绝 4同意 5时间到
    private String pathId = "";//申请的活动的id
    private String pathTitle = "";//活动标题
    private long startTime = 0;//申请的开始时间
    private long endTime = 0;//申请的结束时间
    private int participants = 0;//参与人数
    private String freeTime = "";//local参加traveler的活动
    private String reason = "";//申请人取消申请的理由
    private boolean notified = false;//活动结束 true已发通知 false未发通知

    public Indent() {
    }

    // Parcelable
    public Indent(Parcel in) {
        this.id = in.readString();
        this.userId = in.readString();
        this.tarId = in.readString();
        this.state = in.readInt();
        this.pathId = in.readString();
        this.pathTitle = in.readString();
        this.startTime = in.readLong();
        this.endTime = in.readLong();
        this.participants = in.readInt();
        this.freeTime = in.readString();
        this.reason = in.readString();
        this.notified = in.readByte() != 0;

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public int getParticipants() {
        return participants;
    }

    public void setParticipants(int participants) {
        this.participants = participants;
    }

    public String getFreeTime() {
        return freeTime;
    }

    public void setFreeTime(String freeTime) {
        this.freeTime = freeTime;
    }

    public String getPathId() {
        return pathId;
    }

    public void setPathId(String pathId) {
        this.pathId = pathId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getTarId() {
        return tarId;
    }

    public void setTarId(String tarId) {
        this.tarId = tarId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPathTitle() {
        return pathTitle;
    }

    public void setPathTitle(String pathTitle) {
        this.pathTitle = pathTitle;
    }

    public boolean getNotified() {
        return notified;
    }

    public void setNotified(boolean notified) {
        this.notified = notified;
    }

    @Override
    public String toString() {
        return "id=" + id
                + ",userId=" + userId
                + ",tarId=" + tarId
                + ",state=" + state
                + ",pathId=" + pathId
                + ",pathTitle=" + pathTitle
                + ",startTime=" + startTime
                + ",endTime=" + endTime
                + ",participants=" + participants
                + ",freeTime=" + freeTime
                + ",reason=" + reason
                + ",notified=" + notified
                ;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(userId);
        dest.writeString(tarId);
        dest.writeInt(state);
        dest.writeString(pathId);
        dest.writeString(pathTitle);
        dest.writeLong(startTime);
        dest.writeLong(endTime);
        dest.writeInt(participants);
        dest.writeString(freeTime);
        dest.writeString(reason);
        dest.writeByte((byte) (notified ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

}