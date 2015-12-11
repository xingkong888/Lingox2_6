package cn.lingox.android.entity;

/**
 * 用户数据---用于第三方登录时使用
 * 第三方登录没实现
 */
public class UserInfo {
    //从第三方获取到的用户数据
    private String userIcon;//用户头像链接
    private String userName;//用户名
    private Gender userGender;//用户性别
    private String userNote;//（没看懂什么含义）

    public String getUserIcon() {
        return userIcon;
    }

    public void setUserIcon(String userIcon) {
        this.userIcon = userIcon;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Gender getUserGender() {
        return userGender;
    }

    public void setUserGender(Gender userGender) {
        this.userGender = userGender;
    }

    public String getUserNote() {
        return userNote;
    }

    public void setUserNote(String userNote) {
        this.userNote = userNote;
    }

    public enum Gender {BOY, GIRL}
}
