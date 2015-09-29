package cn.lingox.android.entity;

import java.util.ArrayList;

public class CachePath {

    private static CachePath instance = null;
    private int localOrTraveler = 3;
    private String location = "";
    private String title = "";
    private String description = "";
    private ArrayList<String> tags = new ArrayList<>();//活动标签，最多三个
    private boolean photo = false;
    private String image = "";
    private long startTime = 0;
    private long endTime = 0;
    private String address = "";
    private int groupSize = 0;
    private String budget = "";

    public static synchronized CachePath getInstance() {
        if (instance == null) {
            instance = new CachePath();
        }
        return instance;
    }

    public void setNothing() {
        localOrTraveler = 3;
        location = "";
        title = "";
        description = "";
        tags = null;
        photo = false;
        startTime = 0;
        endTime = 0;
        address = "";
        groupSize = 0;
        budget = "";
        image = "";
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getGroupSize() {
        return groupSize;
    }

    public void setGroupSize(int groupSize) {
        this.groupSize = groupSize;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBudget() {
        return budget;
    }

    public void setBudget(String budget) {
        this.budget = budget;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getLocalOrTraveler() {
        return localOrTraveler;
    }

    public void setLocalOrTraveler(int localOrTraveler) {
        this.localOrTraveler = localOrTraveler;
    }

    public boolean getPhoto() {
        return photo;
    }

    public void setPhoto(boolean photo) {
        this.photo = photo;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

}