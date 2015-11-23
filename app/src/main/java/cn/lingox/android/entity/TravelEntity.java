package cn.lingox.android.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

import cn.lingox.android.app.LingoXApplication;

/**
 * 旅行者发布内容类
 */
public class TravelEntity implements Parcelable {

    public static final Creator<TravelEntity> CREATOR = new Creator<TravelEntity>() {
        public TravelEntity createFromParcel(Parcel in) {
            return new TravelEntity(in);
        }

        public TravelEntity[] newArray(int size) {
            return new TravelEntity[size];
        }
    };

    private String id;//内容的唯一标示
    private String user_id;//发布者的id
    private String country;//旅行地点--国家
    private String province;//旅行地点--省份
    private String city;//旅行地点--城市
    private String text;//详细描述
    private ArrayList<String> tags;//标签
    private long startTime;//开始时间
    private long endTime;//结束时间
    private String provide;//可提供
    private ArrayList<User> likeUsers;//likeUsers的用户
    private ArrayList<Comment> comments;//评论的用户
    private String createdAt;//创建时间
    private String updatedAt;// 修改时间，自动生成

    public TravelEntity() {
        id = "";
        user_id = "";
        country = "";
        province = "";
        city = "";
        text = "";
        tags = new ArrayList<>();
        startTime = -1;
        endTime = -1;
        provide = "";
        likeUsers = new ArrayList<>();
        comments = new ArrayList<>();
        createdAt = "";
        updatedAt = "";
    }

    public TravelEntity(Parcel in) {
        this.id = in.readString();
        this.user_id = in.readString();
        this.country = in.readString();
        this.province = in.readString();
        this.city = in.readString();
        this.text = in.readString();
        this.tags = in.createStringArrayList();
        this.startTime = in.readLong();
        this.endTime = in.readLong();
        this.provide = in.readString();
        this.likeUsers = in.createTypedArrayList(User.CREATOR);
        this.comments = in.createTypedArrayList(Comment.CREATOR);
        this.createdAt = in.readString();
        this.updatedAt = in.readString();

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<User> getLikeUsers() {
        return likeUsers;
    }

    public void setLikeUsers(ArrayList<User> likeUsers) {
        this.likeUsers = likeUsers;
    }

    public ArrayList<Comment> getComments() {
        return comments;
    }

    public void setComments(ArrayList<Comment> comments) {
        this.comments = comments;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getProvide() {
        return provide;
    }

    public void setProvide(String provide) {
        this.provide = provide;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * 添加新的comment
     *
     * @param comment comment的实例
     */
    public void addComment(Comment comment) {
        comments.add(comment);
    }

    /**
     * 移除一个comment
     *
     * @param comment comment的实例
     */
    public void removeComment(Comment comment) {
        int pos = findCommentInList(comment);
        if (pos != -1) {
            comments.remove(pos);
        }
    }

    /**
     * 在comment实例集合中查找
     *
     * @param comment 待查找的comment
     * @return 若存在，返回位置，否则返回-1
     */
    private int findCommentInList(Comment comment) {
        for (int i = 0; i < comments.size(); i++) {
            if (comments.get(i).getId().equals(comment.getId())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 根据国家、省份、城市，整合成一个字符串
     *
     * @return 返回位置字符串
     */
    public String getLocation() {
        return LingoXApplication.getInstance().
                getLocation(getCountry(), getProvince(), getCity());
    }

    /**
     * 设置位置
     *
     * @param location 整串位置字符串
     */
    public void setLocation(String location) {
        String[] str = location.split(", ");
        switch (str.length) {
            case 1://只有国家
                setCountry(str[0]);
                break;
            case 2://国家、省份
                setCountry(str[0]);
                setProvince(str[1]);
                break;
            case 3://国家、省份、城市
                setCountry(str[0]);
                setProvince(str[1]);
                setCity(str[2]);
                break;
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.user_id);
        dest.writeString(this.country);
        dest.writeString(this.province);
        dest.writeString(this.city);
        dest.writeString(this.text);
        dest.writeStringList(this.tags);
        dest.writeLong(this.startTime);
        dest.writeLong(this.endTime);
        dest.writeString(this.provide);
        dest.writeTypedList(this.likeUsers);
        dest.writeTypedList(this.comments);
        dest.writeString(this.createdAt);
        dest.writeString(this.updatedAt);

    }

    @Override
    public String toString() {
        return "id =" + id +
                ", user_id=" + user_id +
                ", country=" + country +
                ", province=" + province +
                ", city=" + city +
                ", text=" + text +
                ", tag=" + tags.toString() +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", provide=" + provide +
                ", likeUsers=" + likeUsers.toString() +
                ", comment=" + comments.toString() +
                ", createdAt=" + createdAt +
                ", updatedAt="+updatedAt
                ;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
