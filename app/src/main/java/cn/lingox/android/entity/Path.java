package cn.lingox.android.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;

import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.helper.JsonHelper;

public class Path implements Parcelable {
    // Constants
    public static final int TYPE_TRAVELLER = 2;
    public static final int TYPE_LOCAL = 1;
    public static final Creator<Path> CREATOR = new Creator<Path>() {
        public Path createFromParcel(Parcel in) {
            return new Path(in);
        }

        public Path[] newArray(int size) {
            return new Path[size];
        }
    };

    private static final String LOG_TAG = "Path";
    private String id;                    // DB id
    private String user_id;
    private String text;
    private String cost;
    private String title;
    private long dateTime;
    private long endDateTime;
    private long createdTime;
    private int capacity;
    private String image;    // ArrayList of image URLs
    private String chosenCountry;
    private String province;
    private String chosenCity;
    private ArrayList<User> acceptedUsers;
    private ArrayList<Comment> comments;
    private String createdAt;
    private int type;
    private String hxGroupId;
    // Variables that are not linked to the Server
    private String nonDBLocationString;
    private String detailAddress;
    private String latitude;
    private String longitude;
    private ArrayList<String> tags;


    public Path() {
        this.id = "";
        this.user_id = "";
        this.title = "";
        this.text = "";
        this.cost = "";
        this.dateTime = 0;
        this.endDateTime = 0;
        this.createdTime = 0;
        this.capacity = 0;
        this.image = "";
        this.chosenCountry = "";
        this.province = "";
        this.chosenCity = "";
        this.detailAddress = "";
        this.latitude = "";
        this.longitude = "";
        this.acceptedUsers = new ArrayList<>();
        this.comments = new ArrayList<>();
        this.createdAt = "";
        this.type = 0;
        this.hxGroupId = "";
        this.tags = new ArrayList<>();
    }

    public Path(String id,
                String user_id,
                String title,
                String text,
                String cost,
                String image,
                long dateTime,
                long createdTime,
                int capacity,
                String chosenCountry,
                String province,
                String chosenCity,
                String detailAddress,
                String latitude,
                String longitude,
                ArrayList<User> acceptedUsers,
                ArrayList<Comment> comments,
                String createdAt,
                int type,
                String hxGroupId, ArrayList<String> tags) {
        this.id = id;
        this.user_id = user_id;
        this.title = title;
        this.text = text;
        this.cost = cost;
        this.image = image;
        this.dateTime = dateTime;
        this.createdTime = createdTime;
        this.capacity = capacity;
        this.chosenCountry = chosenCountry;
        this.province = province;
        this.chosenCity = chosenCity;
        this.detailAddress = detailAddress;
        this.latitude = latitude;
        this.longitude = longitude;
        this.acceptedUsers = acceptedUsers;
        this.comments = comments;
        this.createdAt = createdAt;
        this.type = type;
        this.hxGroupId = hxGroupId;
        this.tags = tags;
    }

    // Parcelable
    public Path(Parcel in) {
        this.id = in.readString();
        this.user_id = in.readString();
        this.title = in.readString();
        this.text = in.readString();
        this.cost = in.readString();
        this.dateTime = in.readLong();
        this.endDateTime = in.readLong();
        this.createdTime = in.readLong();
        this.capacity = in.readInt();
        this.image = in.readString();
        this.chosenCountry = in.readString();
        this.province = in.readString();
        this.chosenCity = in.readString();
        this.detailAddress = in.readString();
        this.latitude = in.readString();
        this.longitude = in.readString();
        this.acceptedUsers = in.createTypedArrayList(User.CREATOR);
        this.comments = in.createTypedArrayList(Comment.CREATOR);
        this.createdAt = in.readString();
        this.nonDBLocationString = in.readString();
        this.type = in.readInt();
        this.hxGroupId = in.readString();

        this.tags = in.createStringArrayList();
    }

    public long getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(long endDateTime) {
        this.endDateTime = endDateTime;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return user_id;
    }

    public void setUserId(String id) {
        this.user_id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getChosenCountry() {
        return chosenCountry;
    }

    public void setChosenCountry(String chosenCountry) {
        this.chosenCountry = chosenCountry;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getChosenCity() {
        return chosenCity;
    }

    public void setChosenCity(String chosenCity) {
        this.chosenCity = chosenCity;
        this.nonDBLocationString = null;
    }

    public ArrayList<User> getAcceptedUsers() {
        return acceptedUsers;
    }

    public void setAcceptedUsers(ArrayList<User> acceptedUsers) {
        this.acceptedUsers = acceptedUsers;
    }

    public ArrayList<Comment> getComments() {
        return comments;
    }

    public void setComments(ArrayList<Comment> comments) {
        this.comments = comments;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getHxGroupId() {
        return hxGroupId;
    }

    public void setHxGroupId(String hxGroupId) {
        this.hxGroupId = hxGroupId;
    }

    public String getDetailAddress() {
        return detailAddress;
    }

    public void setDetailAddress(String detailAddress) {
        this.detailAddress = detailAddress;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    @Override
    public String toString() {
        return "Path ["
                + "id=" + id
                + ", user_id=" + user_id
                + ", title=" + title
                + ", text=" + text
                + ", cost=" + cost
                + ", dateTime=" + dateTime
                + ", endDateTime=" + endDateTime
                + ", createdTime=" + createdTime
                + ", capacity=" + capacity
                + ", image=" + image
                + ", chosenCountry=" + chosenCountry
                + ", province=" + province
                + ", chosenCity=" + chosenCity
                + ", detailAddress=" + detailAddress
                + ", latitude=" + latitude
                + ", longitude=" + longitude
                + ", acceptedUsers=" + acceptedUsers
                + ", comments=" + comments
                + ", createdAt=" + createdAt
                + ", type=" + type
                + ", tags=" + tags
                + ", hxGroupId=" + hxGroupId
                + "]";
    }

    public boolean hasUserAccepted(String userId) {
        for (User users : acceptedUsers) {
            if (users.getId().equals(userId))
                return true;
        }
        return false;
    }

    public String getLocationString() {
        nonDBLocationString = LingoXApplication.getInstance().getLocation(getChosenCountry(), getProvince(), getChosenCity());
        if (!nonDBLocationString.isEmpty()) {
            if (!getDetailAddress().isEmpty()) {
                return nonDBLocationString = nonDBLocationString
                        + ",\t" + getDetailAddress().trim();
            } else {
                return nonDBLocationString;
            }
        } else {
            if (!getDetailAddress().isEmpty()) {
                return nonDBLocationString = getDetailAddress().trim();
            } else {
                return "";
            }
        }
    }

    public String getLocation() {
        return LingoXApplication.getInstance().getLocation(getChosenCountry(), getProvince(), getChosenCity());

    }

    public void setLocation(String location) {
        String[] str = location.split(", ");
        switch (str.length) {
            case 1://只有国家
                setChosenCountry(str[0]);
                break;
            case 2://国家、省份
                setChosenCountry(str[0]);
                setProvince(str[1]);
                break;
            case 3://国家、省份、城市
                setChosenCountry(str[0]);
                setProvince(str[1]);
                setChosenCity(str[2]);
                break;
        }
    }


    public void addComment(Comment comment) {
        comments.add(comment);
    }

    public void removeComment(Comment comment) {
        int pos = findCommentInList(comment);
        if (pos != -1)
            comments.remove(pos);
    }

    private int findCommentInList(Comment comment) {
        for (int i = 0; i < comments.size(); i++) {
            if (comments.get(i).getId().equals(comment.getId())) {
                return i;
            }
        }
        return -1;
    }

    public void addAcceptedUser(User user) {
        acceptedUsers.add(user);
    }

    public void removeAcceptedUser(User user) {
        int i = -1;
        for (User users : acceptedUsers) {
            if (users.getId().equals(user.getId())) {
                i = acceptedUsers.indexOf(users);
                break;
            }
        }
        if (i != -1)
            acceptedUsers.remove(i);
        else
            Log.e(LOG_TAG, "removeAcceptedUser(): failed to remove user from acceptedUser list");
    }

    public String getShareText() {
//        return "LingoX Activity:    \n" + JsonHelper.getInstance().parseTimestamp(Long.valueOf(dateTime.split("-")[0])) + "\n\n"
        return "LingoX Activity:    \n" + JsonHelper.getInstance().parseTimestamp(dateTime) + "\n\n"
                + ((this.type == TYPE_LOCAL) ? "Local" : "Traveller") + " at:    \n" + getLocationString() + "\n\n"
                + "Event:    \n" + text + "\n\n"
                + "Cost:    \n" + cost + "\n\n"
                + "Shared using LingoX Android!";
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.user_id);
        dest.writeString(this.title);
        dest.writeString(this.text);
        dest.writeString(this.cost);
        dest.writeLong(this.dateTime);
        dest.writeLong(this.endDateTime);
        dest.writeLong(this.createdTime);
        dest.writeInt(this.capacity);
        dest.writeString(this.image);
        dest.writeString(this.chosenCountry);
        dest.writeString(this.province);
        dest.writeString(this.chosenCity);
        dest.writeString(this.detailAddress);
        dest.writeString(this.latitude);
        dest.writeString(this.longitude);
        dest.writeTypedList(this.acceptedUsers);
        dest.writeTypedList(this.comments);
        dest.writeString(this.createdAt);
        dest.writeString(this.nonDBLocationString);
        dest.writeInt(this.type);
        dest.writeString(this.hxGroupId);
        dest.writeStringList(this.tags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Path) {
            Path path = (Path) obj;
            return this.getId().equals(getId());
        }
        return super.equals(obj);
    }


}