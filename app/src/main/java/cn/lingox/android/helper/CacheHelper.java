package cn.lingox.android.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;

import cn.lingox.android.constants.StringConstant;
import cn.lingox.android.entity.LingoNotification;
import cn.lingox.android.entity.User;

public class CacheHelper {
    private static final String LOG_TAG = "CacheHelper";

    private static final String PREF_SELF = "LingoXSelf";
    private static final String PREF_SETTINGS = "LingoXSet";

    private static final String SHARED_KEY_SETTING_NOTIFICATION = "shared_key_setting_notification";
    private static final String SHARED_KEY_SETTING_SOUND = "shared_key_setting_sound";
    private static final String SHARED_KEY_SETTING_VIBRATE = "shared_key_setting_vibrate";
    private static final String SHARED_KEY_SETTING_SPEAKER = "shared_key_setting_speaker";
    private static final String KEY_SETTING_LANGUAGE = "key_setting_Language";

    // Cache Elements
    private static CacheHelper instance = null;
    private static SharedPreferences spSelf;
    private static SharedPreferences.Editor spSelfEditor;
    private static SharedPreferences spSettings;
    private static SharedPreferences.Editor spSettingsEditor;

    // Global Data Elements
    private HashMap<String, User> userCache = new HashMap<>();
    // <username, userId> This is so we can search for the UserID of a certain username
    private HashMap<String, String> userIdCache = new HashMap<>();
    private ArrayList<User> contactList = new ArrayList<>();
    private ArrayList<LingoNotification> notificationList = new ArrayList<>();


    private CacheHelper() {
    }

    public static synchronized CacheHelper getInstance() {
        if (instance == null)
            instance = new CacheHelper();
        return instance;
    }

    public void setContext(Context ctx) {
        spSelf = ctx.getSharedPreferences(PREF_SELF, Context.MODE_PRIVATE);
        spSelfEditor = spSelf.edit();
        spSettings = ctx.getSharedPreferences(PREF_SETTINGS, Context.MODE_PRIVATE);
        spSettingsEditor = spSettings.edit();
        if (CacheHelper.getInstance().getSettingLanguage() == null)
            CacheHelper.getInstance().setKeySettingLanguage(Locale.getDefault().getLanguage());
    }

    // Non-Persistant Cached Data
    public ArrayList<User> getContactList() {
        return contactList;
    }

    public void setContactList(ArrayList<User> contactList) {
        this.contactList.clear();
        this.contactList.addAll(contactList);
        sortContactList();
    }

    public void removeContact(User user) {
        if (!contactList.remove(user))
            Log.d(LOG_TAG, "Tried to remove contact from contact list that wasn't there");
    }

    public void addContact(User user) {
        contactList.add(user);
        sortContactList();
    }

    public void sortContactList() {
        Collections.sort(contactList, new Comparator<User>() {

            @Override
            public int compare(User lhs, User rhs) {
                return lhs.getNicknameOrUsername().compareTo(rhs.getNicknameOrUsername());
            }
        });
    }

    public void setNotificationList(ArrayList<LingoNotification> notificationList) {
        this.notificationList.clear();
        this.notificationList.addAll(notificationList);
    }

    // Cache Data
    public String getPassword() {
        return spSelf.getString(StringConstant.passwordStr, null);
    }

    public void setPassword(String password) {
        if (password != null) {
            spSelfEditor.putString(StringConstant.passwordStr, password).apply();
        }
    }

    public User getSelfInfo() {
        Gson gson = new Gson();
        String json = spSelf.getString(StringConstant.userInfoStr, "");
        if (TextUtils.isEmpty(json)) {
            Log.e(LOG_TAG, "getSelfInfo user null");
            return null;
        } else
            return gson.fromJson(json, User.class);
    }

    public void setSelfInfo(User user) {
        if (user != null) {
            Gson gson = new Gson();
            String json = gson.toJson(user);
            spSelfEditor.putString(StringConstant.userInfoStr, json).apply();
        } else {
            Log.e(LOG_TAG, "setSelfInfo user null");
        }
    }

    public void addUserInfo(User user) {
        if (user != null) {
            // This is so we can also get users by their username
            // This is because EMConversations are referenced by username
//            Log.d(LOG_TAG, "addUserInfo() adding pair (" + user.getUsername() + ", " + user.getId() + ")");
            userIdCache.put(user.getUsername(), user.getId());
//            Log.d(LOG_TAG, "addUserInfo() adding pair (" + user.getId() + ", " + user.toString() + ")");
            userCache.put(user.getId(), user);
        } else {
            Log.e(LOG_TAG, "addUserInfo: passed User was null");
        }
    }

    public User getUserInfo(String userId) {
        if (userId.equals(getSelfInfo().getId()))
            return getSelfInfo();
        if (userCache.containsKey(userId))
            return userCache.get(userId);
        Log.e(LOG_TAG, "getUserInfo: UserId '" + userId + "' was not in the cache");
        return null;
    }

    public User getUserInfo(String userId, int a) {
        if (userCache.containsKey(userId))
            return userCache.get(userId);
        return null;
    }

    public User getUserInfoFromUsername(String username) {
        if (userIdCache.containsKey(username))
            return getUserInfo(userIdCache.get(username));
        Log.e(LOG_TAG, "getUserInfoFromUsername: Username '" + username + "' was not in the cache");
        return null;
    }

    public boolean getSettingMsgNotification() {
        return spSettings.getBoolean(SHARED_KEY_SETTING_NOTIFICATION, true);
    }

    public void setSettingMsgNotification(boolean paramBoolean) {
        spSettingsEditor.putBoolean(SHARED_KEY_SETTING_NOTIFICATION, paramBoolean).apply();
    }

    // Settings
    public void setKeySettingLanguage(String language) {
        spSettingsEditor.putString(KEY_SETTING_LANGUAGE, language).apply();
    }

    public String getSettingLanguage() {
//        return spSettings.getString(KEY_SETTING_LANGUAGE, null);
        return "en";
    }

    public boolean isEnglishLanguage() {
        return spSettings.getString(KEY_SETTING_LANGUAGE, null).equals("en");

    }

    public boolean getSettingMsgSound() {
        return spSettings.getBoolean(SHARED_KEY_SETTING_SOUND, true);
    }

    public void setSettingMsgSound(boolean paramBoolean) {
        spSettingsEditor.putBoolean(SHARED_KEY_SETTING_SOUND, paramBoolean).apply();
    }

    public boolean getSettingMsgVibrate() {
        return spSettings.getBoolean(SHARED_KEY_SETTING_VIBRATE, true);
    }

    public void setSettingMsgVibrate(boolean paramBoolean) {
        spSettingsEditor.putBoolean(SHARED_KEY_SETTING_VIBRATE, paramBoolean).apply();
    }

    public boolean getSettingMsgSpeaker() {
        return spSettings.getBoolean(SHARED_KEY_SETTING_SPEAKER, true);
    }

    public void setSettingMsgSpeaker(boolean paramBoolean) {
        spSettingsEditor.putBoolean(SHARED_KEY_SETTING_SPEAKER, paramBoolean).apply();
    }

    // ---------- Tutorial ----------

    public boolean isLoggedIn() {
        try {
            if (getSelfInfo() != null && getPassword() != null)
                return true;
            else {
                logout();
                return false;
            }
        } catch (Exception e) {
            logout();
            Log.e(LOG_TAG, "isLoggedIn Exception caught: " + e.getMessage());
            return false;
        }
    }

    public void logout() {
        // Cache data
        spSelfEditor.clear();
        spSelfEditor.apply();

        // Non-Cache data
        contactList.clear();
        userIdCache.clear();
        userCache.clear();
        notificationList.clear();
    }

    public void addNotifications(ArrayList<LingoNotification> allUnseenNotifications) {
        for (LingoNotification notification : allUnseenNotifications) {
            if (!notificationList.contains(notification)) {
                notificationList.add(notification);
            }
        }
        Collections.sort(notificationList, new Comparator<LingoNotification>() {
            @Override
            public int compare(LingoNotification lhs, LingoNotification rhs) {
                try {
                    long lhsTime = JsonHelper.getInstance().sailsJSDateToTimestamp(lhs.getCreatedAt());
                    long rhsTime = JsonHelper.getInstance().sailsJSDateToTimestamp(rhs.getCreatedAt());
                    if (lhsTime == rhsTime) return 0;
                    if (lhsTime < rhsTime) return 1;
                    if (lhsTime > rhsTime) return -1;
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Notification comparator: " + e.toString());
                }
                return 0;
            }
        });
    }
}
