package cn.lingox.android.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import com.easemob.util.HanziToPinyin;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import cn.lingox.android.Constant;
import cn.lingox.android.app.LingoXApplication;

/**
 * 用户信息实体类
 */
public class User implements Parcelable {

    public static final Creator<User> CREATOR = new Creator<User>() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };
    private static final String LOG_TAG = "User";

    private String id;//用户id---用户的唯一标示
    private String username;//登录账户-----也可用作唯一标示
    private String nickname;//用户昵称
    private String email;//用户邮箱----未做邮箱有效验证
    private String country;//国家
    private String province;//省份
    private String city;//城市
    private String speak;//语言
    private String avatar;//头像链接
    private String dateOfBirth;//生日
    private String gender;//性别
    private String profession;//职业
    private ArrayList<String> interests = new ArrayList<>();//兴趣
    private String signature;//自我介绍
    private double[] loc;//地理位置-----
    private String locString;//纬度
    private String loginTime;//经度
    private boolean online;//是否在线
    private boolean homeStay;//提供家庭住宿
    private boolean homeMeal;//提供家庭餐
    private boolean localGuide;//本地向导
    private int relation;

    private String createdAt;//注册时间
    private String visited;

    public User() {
        id = "";
        username = "";
        nickname = "";
        email = "";
        country = "";
        province = "";
        city = "";
        speak = "";
        avatar = "";
        dateOfBirth = "";
        gender = "";
        profession = "";
        interests.clear();
        signature = "";
        loc = new double[]{0, 0};
        locString = "";
        loginTime = "";
        online = false;
        homeMeal = false;
        homeStay = false;
        localGuide = false;
        createdAt = "";
        visited = "";
    }

    public User(String id, String username, String nickname, String email, String country, String province, String city,
                String speak,
                String avatar, String dateOfBirth, String gender, String profession,
                ArrayList<String> interests,
                String signature, double[] loc,
                String locString, String loginTime, boolean online,
                int relation, boolean localGuide, boolean homeMeal, boolean homeStay
            , String createdAt, String visited) {
        super();
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.email = email;
        this.country = country;
        this.province = province;
        this.city = city;
        this.speak = speak;
        this.avatar = avatar;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.profession = profession;
        this.interests = interests;
        this.signature = signature;
        this.loc = loc;
        this.locString = locString;
        this.loginTime = loginTime;
        this.online = online;
        this.relation = relation;

        this.localGuide = localGuide;
        this.homeStay = homeStay;
        this.homeMeal = homeMeal;
        this.createdAt = createdAt;
        this.visited = visited;
    }

    // Parcelable
    public User(Parcel in) {
        this.id = in.readString();
        this.username = in.readString();
        this.nickname = in.readString();
        this.email = in.readString();
        this.country = in.readString();
        this.province = in.readString();
        this.city = in.readString();
        this.speak = in.readString();
        this.avatar = in.readString();
        this.dateOfBirth = in.readString();
        this.gender = in.readString();
        this.profession = in.readString();
        this.interests = in.createStringArrayList();
        this.signature = in.readString();
        this.loc = in.createDoubleArray();
        this.locString = in.readString();
        this.loginTime = in.readString();
        this.online = in.readByte() != 0;
        this.relation = in.readInt();
        this.homeStay = in.readByte() != 0;
        this.homeMeal = in.readByte() != 0;
        this.localGuide = in.readByte() != 0;
        this.createdAt = in.readString();
        this.visited = in.readString();
    }

    public String getVisited() {
        return visited;
    }

    public void setVisited(String visited) {
        this.visited = visited;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setLocalGuide(boolean localGuide) {
        this.localGuide = localGuide;
    }

    public boolean getHomeStay() {
        return homeStay;
    }

    public void setHomeStay(boolean homeStay) {
        this.homeStay = homeStay;
    }

    public boolean getHomeMeal() {
        return homeMeal;
    }

    public void setHomeMeal(boolean homeMeal) {
        this.homeMeal = homeMeal;
    }

    public boolean getLocalGuidey() {
        return localGuide;
    }

    public String getId() {
        return id;
    }

    public void setId(String userId) {
        this.id = userId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getSpeak() {
        String[] strs = speak.split(", ");
        ArrayList<String> list = new ArrayList<>();
        for (String str : strs) {
            if (!str.isEmpty()) {
                list.add(str.trim());
            }
        }
        speak = list.toString().replace("[", "").replace("]", "").trim();
        return speak;
    }

    public void setSpeak(String speak) {
        this.speak = speak;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public ArrayList<String> getInterests() {
        return interests;
    }

    public void setInterests(ArrayList<String> interests) {
        this.interests = interests;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public double[] getLoc() {
        return loc;
    }

    public void setLoc(double[] loc) {
        this.loc = loc;
    }

    public String getLocString() {
        return locString;
    }

    public void setLocString(String locString) {
        this.locString = locString;
    }

    public String getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(String loginTime) {
        this.loginTime = loginTime;
    }

    public boolean getOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public int getRelation() {
        return relation;
    }

    public void setRelation(int relation) {
        this.relation = relation;
    }

    // This is a safety net method, it shouldn't be required,
    // but we use it just in case the nickname is null or empty

    /**
     * 预防昵称为空
     * 为空时返回账户
     *
     * @return 昵称或账户
     */
    public String getNicknameOrUsername() {
        if (TextUtils.isEmpty(nickname)) {
            return username;
        }
        return nickname;
    }

    /**
     * 获取该用户的国家、省份、城市
     *
     * @return 地址
     */
    public String getLocation() {
        return LingoXApplication.getInstance().getLocation(getCountry(), getProvince(), getCity());
    }

    /**
     * 作用：设置用户的地址
     * 功能：将包含有国家、省份、城市的字符串通过“,”分割开来，分别存储到相应的字段
     *
     * @param location 包含国家、省份、城市的字符串
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
    public String toString() {
        return "User ["
                + "id=" + id
                + ", username=" + username
                + ", nickname=" + nickname
                + ", email=" + email
                + ", country=" + country
                + ", province=" + province
                + ", city=" + city
                + ", speak=" + speak
                + ", avatar=" + avatar
                + ", dateOfBirth=" + dateOfBirth
                + ", gender=" + gender
                + ", profession=" + profession
                + ", interests=" + interests
                + ", signature=" + signature
                + ", loc=" + loc[0] + "," + loc[1]
                + ", locString=" + locString
                + ", loginTime=" + loginTime
                + ", online=" + online
                + ", relation=" + relation
                + ", homeMeal=" + homeMeal
                + ", homeStay=" + homeStay
                + ", loaclGuide=" + localGuide
                + ",createdAt=" + createdAt
                + ",visited=" + visited
                + "]";
    }

    // TODO Can be further improved to check for valid day and month values

    /**
     * 判断生日是否合法
     */
    public boolean hasProperlyFormedBirthDate() {
        if (TextUtils.isEmpty(dateOfBirth) || (dateOfBirth.length() != 8))
            return false;
        try {
            Integer.parseInt(getBirthDateDay());
            Integer.parseInt(getBirthDateMonth());
            Integer.parseInt(getBirthDateYear());
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    /**
     * 获取用户年龄
     *
     * @return 用户年龄
     */
    public int getUserAge() {
        if (!"Select Birthdate".contentEquals(dateOfBirth.trim())
                && !dateOfBirth.trim().isEmpty()
                && !"null".contentEquals(dateOfBirth.trim())) {
            GregorianCalendar cal = new GregorianCalendar();
            int y, m, d, a;
            int _year = Integer.parseInt(getBirthDateYear());
            int _month = Integer.parseInt(getBirthDateMonth());
            int _day = Integer.parseInt(getBirthDateDay());
            y = cal.get(Calendar.YEAR);
            m = cal.get(Calendar.MONTH) + 1;
            d = cal.get(Calendar.DAY_OF_MONTH);
            cal.set(_year, _month, _day);
            a = y - cal.get(Calendar.YEAR);
            if ((m < cal.get(Calendar.MONTH))
                    || ((m == cal.get(Calendar.MONTH))
                    && (d < cal.get(Calendar.DAY_OF_MONTH)))) {
                --a;
            }
            if (a < 0) {
                return 0;
            }
            return a;
        } else {
            return 0;
        }
    }

    /**
     * @return 生日---天
     */
    public String getBirthDateDay() {
        try {
            return dateOfBirth.substring(0, 2);
        } catch (Exception e) {
            Log.e(LOG_TAG, "getBirthDateDay: " + e.toString());
            return null;
        }
    }

    /**
     * @return 生日---月
     */
    public String getBirthDateMonth() {
        try {
            return dateOfBirth.substring(2, 4);
        } catch (Exception e) {
            Log.e(LOG_TAG, "getBirthDateDay: " + e.toString());
            return null;
        }
    }

    /**
     * @return 生日---年
     */
    public String getBirthDateYear() {
        try {
            return dateOfBirth.substring(4, 8);
        } catch (Exception e) {
            Log.e(LOG_TAG, "getBirthDateDay: " + e.toString());
            return null;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof User) {
            User user = (User) obj;
            return this.getId().equals(user.getId());
        }
        return super.equals(obj);
    }

    /**
     * 获取用户名的首字母，用户排序
     *
     * @return 用户名的首字母
     */
    public String getHeader() {
        String headerName = TextUtils.isEmpty(nickname) ? username : nickname;

        if (username.equals(Constant.NEW_FRIENDS_USERNAME)) {
            return ("");
        } else if (Character.isDigit(headerName.charAt(0))) {
            return ("#");
        } else {
            char header = (HanziToPinyin.getInstance().get(headerName).get(0).target)
                    .toLowerCase(Locale.getDefault()).charAt(0);
            if (header < 'a' || header > 'z') {
                return ("#");
            } else {
                return (String.valueOf(header));
            }
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.username);
        dest.writeString(this.nickname);
        dest.writeString(this.email);
        dest.writeString(this.country);
        dest.writeString(this.province);
        dest.writeString(this.city);
        dest.writeString(this.speak);
        dest.writeString(this.avatar);
        dest.writeString(this.dateOfBirth);
        dest.writeString(this.gender);
        dest.writeString(this.profession);
        dest.writeStringList(this.interests);
        dest.writeString(this.signature);
        dest.writeDoubleArray(this.loc);
        dest.writeString(this.locString);
        dest.writeString(this.loginTime);
        dest.writeByte((byte) (online ? 1 : 0));
        dest.writeInt(this.relation);

        dest.writeByte((byte) (homeMeal ? 1 : 0));
        dest.writeByte((byte) (homeStay ? 1 : 0));
        dest.writeByte((byte) (localGuide ? 1 : 0));
        dest.writeString(this.createdAt);
        dest.writeString(this.visited);
    }
}