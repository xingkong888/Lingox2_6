package cn.lingox.android.helper;

import android.graphics.Bitmap;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.constants.StatusCodeConstant;
import cn.lingox.android.constants.StringConstant;
import cn.lingox.android.constants.URLConstant;
import cn.lingox.android.entity.Comment;
import cn.lingox.android.entity.Indent;
import cn.lingox.android.entity.LingoNotification;
import cn.lingox.android.entity.Path;
import cn.lingox.android.entity.PathReference;
import cn.lingox.android.entity.Photo;
import cn.lingox.android.entity.Reference;
import cn.lingox.android.entity.ReturnMsg;
import cn.lingox.android.entity.Travel;
import cn.lingox.android.entity.User;

// TODO Fix debug and error logging
// TODO Fix exception throws/catches (better names/messages)

public class ServerHelper {

    private static final String APPVERSION = LingoXApplication.getInstance().getAppVersion();
    private static String LOG_TAG = "ServerHelper";
    private static ServerHelper instance = null;
    private ArrayList<Indent> datas;

    private ServerHelper() {
    }

    public static synchronized ServerHelper getInstance() {
        if (instance == null)
            instance = new ServerHelper();
        return instance;
    }
    // Version

    private ReturnMsg checkReturnMsg(String jsonStr) {
        if (jsonStr != null && !jsonStr.equals("")) {
            JSONObject jobj;
            try {
                jobj = new JSONObject(jsonStr);
                ReturnMsg rmsg = new ReturnMsg(jobj.getInt("code"),
                        jobj.getJSONObject("data"), jobj.getString("remark"));
                Log.d(LOG_TAG, "Return message remark: " + jobj.getString("remark"));
                return rmsg;
            } catch (JSONException e) {
                Log.e(LOG_TAG, "checkReturnMessage(): Error happened: " + e.getMessage());
                return null;
            }
        } else {
            Log.e(LOG_TAG, "checkReturnMessage(): jsonStr is null or empty");
            return new ReturnMsg(StatusCodeConstant.STATUS_JSON_ERR, null,
                    "Received no data from the server");
        }
    }

    public boolean requireUpdate(int currentVer) {
        int minVer;
        try {
            URL url = new URL(URLConstant.VER_URL);
            Scanner s = new Scanner(url.openStream());
            minVer = s.nextInt();
            s.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return (currentVer < minVer);
    }

    // User
    public User register(String email, String userName, String password) throws Exception {

        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.emailStr, email);
        params.put(StringConstant.usernameStr, userName);
        params.put(StringConstant.passwordStr, password);
        params.put(StringConstant.verStr, APPVERSION);
        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_REGISTER, params);

        Log.d(LOG_TAG, "register: " + jsonStr);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "register: Return message code not positive");
            throw new Exception(rmsg.getRemark());
        }

        User user = JsonHelper.getInstance().jsonToBean(
                rmsg.getData().toString(),
                User.class);

        Log.d(LOG_TAG, "register: User Info: " + user);

        return user;
    }

    //获取双方关系
    public boolean getBothFollowed(String userId1, String userId2) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("user_id", userId1);
        params.put("user_tar", userId2);
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_GET_USER_RELATION, params);

        Log.d(LOG_TAG, "getBothFollowed: " + jsonStr);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "getBothFollowed: Return message code not positive");
            throw new Exception(rmsg.getRemark());
        }
        if (rmsg.getData().getBoolean("bothFollowed")) {
            return true;
        } else {
            return false;
        }
    }

    public User login(String emailOrUsername, String password) throws Exception {

        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.emailOrUsernameStr, emailOrUsername);
        params.put(StringConstant.passwordStr, password);
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_LOGIN, params);

        Log.d(LOG_TAG, "login: " + jsonStr);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "login: Return message code not positive");
            throw new Exception(rmsg.getRemark());
        }

        User user = JsonHelper.getInstance().jsonToBean(
                rmsg.getData().toString(),
                User.class);

        Log.d(LOG_TAG, "login: User Info: " + user);

        return user;
    }

    public void forgotPassword(String email) throws Exception {

        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.emailStr, email);
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_FORGOT_PASSWORD, params);

        Log.d(LOG_TAG, "forgotPassword: " + jsonStr);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "forgotPassword: Return message code not positive");
            Log.e(LOG_TAG, "Remark: " + rmsg.getRemark());
            throw new Exception(rmsg.getRemark());
        }

        Log.d(LOG_TAG, "forgotPassword: password successfully recovered");
    }

    public String uploadAvatar(String user_id, Bitmap avatar) throws Exception {

        String jsonStr = MsgSender.postAvatarToNet(URLConstant.URL_UPLOAD_AVATAR,
                user_id, avatar);

        Log.d(LOG_TAG, "uploadAvatar: " + jsonStr);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "uploadAvatar: Return message code not positive");
            Log.e(LOG_TAG, "Remark: " + rmsg.getRemark());
            throw new Exception("Failed to upload Avatar!");
        }

        String avatarPath = rmsg.getData().getString(StringConstant.avatarStr);

        Log.d(LOG_TAG, "uploadAvatar: Avatar path: " + avatarPath);

        return avatarPath;
    }

    public User updateUserInfo(Map<String, String> params) throws Exception {
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_UPDATE_INFO, params);

        Log.d(LOG_TAG, "updateUserInfo: " + jsonStr);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "updateUserInfo: Return message code not positive");
            throw new Exception("Failed to update User info!");
        }

        User user = JsonHelper.getInstance().jsonToBean(
                rmsg.getData().toString(),
                User.class);
//        Log.d(LOG_TAG, "updateUserInfo: User Info: " + user);
        return user;
    }

    // Contact
    // Returns the new relation code of the contact
    public int userRelationChange(String my_user_id, String tar_user_id,
                                  int user_relation) throws Exception {

        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.userSourceStr, my_user_id);
        params.put(StringConstant.userTargetStr, tar_user_id);
        params.put(StringConstant.userRelationStr,
                String.valueOf(user_relation));
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_CHANGE_RELATION, params);

        Log.d(LOG_TAG, "userRelationChange: " + jsonStr);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "userRelationChange: Return message code not positive");
            throw new Exception("Failed to change User relation!");
        }

        int relationCode = rmsg.getData().getInt(StringConstant.userRelationStr);

        Log.d(LOG_TAG, "userRelationChange: New relation code: " + relationCode);

        return relationCode;
    }

    public ArrayList<User> getContactList(String user_src_id) throws Exception {

        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.userSourceStr, user_src_id);
        params.put(StringConstant.userIdStr, CacheHelper.getInstance().getSelfInfo().getId());
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_GET_CONTACT_LIST, params);

        Log.d(LOG_TAG, "getContactList: " + jsonStr);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "getContactList: Return message code not positive: " + rmsg.getRemark());
            throw new Exception("Failed to get Contact List!");
        }

        ArrayList<User> contactList = new ArrayList<>();
        JSONArray jsonArray = rmsg.getData().getJSONArray(
                "contacts");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            User user = JsonHelper
                    .getInstance().jsonToBean(
                            jsonObject.toString(),
                            User.class);
            contactList.add(user);
            CacheHelper.getInstance().addUserInfo(user);
        }
        Log.d(LOG_TAG, "getContactList: Contact List: " + contactList);
        return contactList;
    }

    public User getUserInfo(String tar_user_id) throws Exception {
        return getUserInfo(CacheHelper.getInstance().getSelfInfo().getId(), tar_user_id);
    }

    public User getUserInfo(String my_user_id, String tar_user_id) throws Exception {

        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.userSourceStr, my_user_id);
        params.put(StringConstant.userTargetStr, tar_user_id);
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_GET_USER_INFO, params);

        Log.d(LOG_TAG, "getUserInfo: " + jsonStr);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "getUserInfo: Return message code not positive");
            throw new Exception("Failed to get User's info!");
        }

        User returnUser = JsonHelper.getInstance().jsonToBean(
                rmsg.getData().toString(),
                User.class);
        CacheHelper.getInstance().addUserInfo(returnUser);

        Log.d(LOG_TAG, "getUserInfo: User's info: " + returnUser);
        return returnUser;
    }

    public ArrayList<User> getUserFollowing(String user_tar_id) throws Exception {

        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.userTargetStr, user_tar_id);
        params.put(StringConstant.userIdStr, CacheHelper.getInstance().getSelfInfo().getId());
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_GET_USER_FOLLOWING, params);

        Log.d(LOG_TAG, "getUserFollowing: " + jsonStr);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "getUserFollowing: Return message code not positive: " + rmsg.getRemark());
            throw new Exception("Failed to get User's Followers!");
        }

        ArrayList<User> contactList = new ArrayList<>();
        JSONArray jsonArray = rmsg.getData().getJSONArray(
                "contacts");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            User user = JsonHelper
                    .getInstance().jsonToBean(
                            jsonObject.toString(),
                            User.class);
            contactList.add(user);
            CacheHelper.getInstance().addUserInfo(user);
        }

        return contactList;
    }

    //// TODO: 提示服务器用户登录
    public void loginTime(String user_id) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.userIdStr, user_id);

        MsgSender.postJsonToNet(URLConstant.URL_LOGINTIME, params);

        User user = CacheHelper.getInstance().getSelfInfo();
        user.setLoginTime(String.valueOf(System.currentTimeMillis() / 1000L));
        CacheHelper.getInstance().setSelfInfo(user);
    }

    // Search
    // TODO use page
    public ArrayList<User> searchUser(String user_id, int searchType, Map<String, String> params, int page) throws Exception {
        if (params == null)
            params = new HashMap<>();
        params.put(StringConstant.userIdStr, user_id);
        params.put(StringConstant.searchType, String.valueOf(searchType));
        params.put("page", String.valueOf(page));
        params.put(StringConstant.verStr, APPVERSION);


        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_SEARCH_USER, params);

        Log.d(LOG_TAG, "searchUser: " + jsonStr);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "searchUser: Return message code not positive");
            throw new Exception("Failed to search for users!");
        }

        LingoXApplication.getInstance().setUserPageCount(rmsg.getData().getInt("pageCount"));

        ArrayList<User> searchResult = new ArrayList<>();

        JSONArray jsonArray = rmsg.getData().getJSONArray(
                StringConstant.searchResult);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            User user = JsonHelper
                    .getInstance().jsonToBean(
                            jsonObject.toString(),
                            User.class);

            searchResult.add(user);
            CacheHelper.getInstance().addUserInfo(user);
        }

        Log.d(LOG_TAG, "searchUser: Found users: " + searchResult);

        return searchResult;
    }
    // References

    public ArrayList<User> searchUserDefault() throws Exception {

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_DEFAULT_USER);
//        Log.d(LOG_TAG, "searchUser: " + jsonStr);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "searchUser: Return message code not positive");
            throw new Exception("Failed to search for users!");
        }
        ArrayList<User> searchResult = new ArrayList<>();

        JSONArray jsonArray = rmsg.getData().getJSONArray(
                StringConstant.searchResult);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            User user = JsonHelper
                    .getInstance().jsonToBean(
                            jsonObject.toString(),
                            User.class);

            searchResult.add(user);
            CacheHelper.getInstance().addUserInfo(user);
        }

        Log.d(LOG_TAG, "searchUser: Found users: " + searchResult);

        return searchResult;
    }

    public Reference createReference(String user_src_id, String user_tar_id, String title,
                                     String content) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.userSourceStr, user_src_id);
        params.put(StringConstant.userTargetStr, user_tar_id);
        params.put(StringConstant.referenceTitle, title);
        params.put(StringConstant.referenceContent, content);
        params.put(StringConstant.verStr, APPVERSION);


        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_CREATE_REFERENCE, params);

        Log.d(LOG_TAG, "createReference: " + jsonStr);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "createReference: Return message code not positive");
            throw new Exception("Failed to create Reference!");
        }

        Reference returnReference = JsonHelper.getInstance().jsonToBean(
                rmsg.getData().toString(),
                Reference.class);

        Log.d(LOG_TAG, "createReference: Reference created: " + returnReference);

        return returnReference;
    }


    public Reference editReference(String referenceId, String title,
                                   String content) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.referenceId, referenceId);
        params.put(StringConstant.referenceTitle, title);
        params.put(StringConstant.referenceContent, content);
        params.put(StringConstant.verStr, APPVERSION);


        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_EDIT_REFERENCE, params);

        Log.d(LOG_TAG, "editReference: " + jsonStr);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "editReference: Return message code not positive");
            throw new Exception("Failed to edit Reference!");
        }

        Reference returnReference = JsonHelper.getInstance().jsonToBean(
                rmsg.getData().toString(),
                Reference.class);

        Log.d(LOG_TAG, "editReference: Reference edited: " + returnReference);

        return returnReference;
    }

    public Reference editReference(String referenceId, String reply) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.referenceId, referenceId);
        params.put("reply", reply);
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_EDIT_REFERENCE, params);

        Log.d(LOG_TAG, "editReference: " + jsonStr);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "editReference: Return message code not positive");
            throw new Exception("Failed to edit Reference!");
        }

        Reference returnReference = JsonHelper.getInstance().jsonToBean(
                rmsg.getData().toString(),
                Reference.class);

        Log.d(LOG_TAG, "editReference: Reference edited: " + returnReference);

        return returnReference;
    }

    public Reference deleteReference(String referenceId) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.referenceId, referenceId);
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_DELETE_REFERENCE, params);

        Log.d(LOG_TAG, "deleteReference: " + jsonStr);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "deleteReference: Return message code not positive");
            throw new Exception("Failed to delete Reference!");
        }

        Reference returnReference = JsonHelper.getInstance().jsonToBean(
                rmsg.getData().toString(),
                Reference.class);

        Log.d(LOG_TAG, "deleteReference: Reference deleted: " + returnReference);

        return returnReference;
    }

    // Paths

    public ArrayList<Reference> getUsersReferences(String userId) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.userIdStr, userId);
        params.put(StringConstant.verStr, APPVERSION);


        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_GET_USERS_REFERENCES, params);

        Log.d(LOG_TAG, "getUsersReferences: " + jsonStr);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "getUsersReferences: Return message code not positive: " + rmsg.getRemark());
            throw new Exception("Failed to get Reference items!");
        }

        ArrayList<Reference> referenceArray = new ArrayList<>();
        JSONArray jsonArray = rmsg.getData().getJSONArray(
                "references");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            referenceArray.add((Reference) JsonHelper
                    .getInstance().jsonToBean(
                            jsonObject.toString(),
                            Reference.class));
        }

        Log.d(LOG_TAG, "getUsersReferences: References: " + referenceArray);

        return referenceArray;
    }

    public Path getPath(String pathId) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.pathId, pathId);
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_GET_PATH, params);

        Log.d(LOG_TAG, "getPath: " + jsonStr);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "getPath: Return message code not positive: " + rmsg.getRemark());
            throw new Exception("Failed to get Path!");
        }

        Path path = JsonHelper.getInstance().jsonToBean(
                rmsg.getData().toString(),
                Path.class);

        Log.d(LOG_TAG, "getPath: Path: " + path);

        return path;
    }

    public ArrayList<Path> getAllPaths(int page) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("longitude", LingoXApplication.getInstance().getLongitude());
        params.put(" latitude", LingoXApplication.getInstance().getLatitude());
        params.put("page", String.valueOf(page));
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_GET_ALL_PATHS, params);

        Log.d(LOG_TAG, "getAllPaths: " + jsonStr);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "getAllPaths: Return message code not positive: " + rmsg.getRemark());
            throw new Exception("Failed to get Path items!");
        }

        ArrayList<Path> pathArray = new ArrayList<>();
        JSONArray jsonArray = rmsg.getData().getJSONArray(StringConstant.paths);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            pathArray.add((Path) JsonHelper
                    .getInstance().jsonToBean(
                            jsonObject.toString(),
                            Path.class));
        }

        Log.d(LOG_TAG, "getAllPaths: Paths: " + pathArray.size());

        return pathArray;
    }

    public ArrayList<Path> getPathsByLocation(String country, String province,
                                              String city, int localOrTravel, int page, ArrayList<String> postJson) throws Exception {
        Map<String, String> params = new HashMap<>();
        if (!country.isEmpty())
            params.put(StringConstant.pathChosenCountry, country);
//        else
//            Log.d(LOG_TAG, "getPathsByLocation: countryCode was null");
        //throw new Exception("ServerHelper, getPathsByLocation: countryCode was null");
        if (!province.isEmpty()) {
            params.put(StringConstant.pathChosenProvince, province);
        }
        if (!city.isEmpty())
            params.put(StringConstant.pathChosenCity, city);
//        else
//            Log.d(LOG_TAG, "getPathsByLocation: cityCode was null");
        if (localOrTravel != 0)
            params.put("type", String.valueOf(localOrTravel));
//        else
//            Log.d(LOG_TAG, "getPathsByLocation: type was null");

        params.put("page", String.valueOf(page));
        if (postJson.size() > 0) {
            params.put("tags", postJson.toString());
        }

        params.put(StringConstant.verStr, APPVERSION);

//Log.d("星期",params.toString());

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_GET_PATHS_BY_LOCATION, params);

        Log.d(LOG_TAG, "getPathsByLocation: " + jsonStr);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        LingoXApplication.getInstance().setPathPageCount(rmsg.getData().getInt("pageCount"));

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "getPathsByLocation: Return message code not positive: " + rmsg.getRemark());
            throw new Exception("Failed to get Path items!");
        }

        ArrayList<Path> pathArray = new ArrayList<>();
        JSONArray jsonArray = rmsg.getData().getJSONArray(StringConstant.paths);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            pathArray.add((Path) JsonHelper
                    .getInstance().jsonToBean(
                            jsonObject.toString(),
                            Path.class));
        }

        Log.d(LOG_TAG, "getPathsByLocation: Paths: " + pathArray);

        return pathArray;
    }

    public ArrayList<Path> getUsersPaths(String userId, int page) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.userIdStr, userId);
        params.put("page", String.valueOf(page));
        params.put(StringConstant.verStr, APPVERSION);


        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_GET_USERS_PATHS, params);

        Log.d(LOG_TAG, "getUsersPaths: " + jsonStr);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "getUsersPaths: Return message code not positive");
            throw new Exception("Failed to get Path items!");
        }

        LingoXApplication.getInstance().setPathPageCount(rmsg.getData().getInt("pageCount"));

        ArrayList<Path> pathArray = new ArrayList<>();
        JSONArray jsonArray = rmsg.getData().getJSONArray(StringConstant.paths);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            pathArray.add((Path) JsonHelper
                    .getInstance().jsonToBean(
                            jsonObject.toString(),
                            Path.class));
        }
        Log.d(LOG_TAG, "getUsersPaths: Paths: " + pathArray);
        return pathArray;
    }

    public Path createPath(Path path) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.userIdStr, path.getUserId());
        params.put(StringConstant.pathTitle, path.getTitle());
        params.put(StringConstant.pathText, path.getText());
        params.put(StringConstant.pathCost, path.getCost());
        params.put(StringConstant.pathDateTime, String.valueOf(path.getDateTime()));
        params.put(StringConstant.pathEndDateTime, String.valueOf(path.getEndDateTime()));
        params.put(StringConstant.pathCreatedTime, String.valueOf(path.getCreatedTime()));
        params.put(StringConstant.pathAvailableTime, path.getAvailableTime());
        params.put(StringConstant.pathCapacity, String.valueOf(path.getCapacity()));
        params.put(StringConstant.pathImage, path.getImage());
        params.put(StringConstant.pathChosenCountry, path.getChosenCountry());
        params.put(StringConstant.pathChosenProvince, path.getProvince());
        params.put(StringConstant.pathChosenCity, path.getChosenCity());
        params.put(StringConstant.pathDetailAddress, path.getDetailAddress());
        params.put(StringConstant.pathLatitude, path.getLatitude());
        params.put(StringConstant.pathLongitude, path.getLongitude());
        params.put(StringConstant.pathType, String.valueOf(path.getType()));
        params.put(StringConstant.pathHXGroupId, path.getHxGroupId());

        params.put(StringConstant.pathTags, String.valueOf(path.getTags()));
        params.put(StringConstant.verStr, APPVERSION);
        Log.d("星期", "params=" + params.toString());

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_CREATE_PATH, params);

        Log.d(LOG_TAG, "createPath: " + jsonStr);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "createPath: Return message code not positive");
            throw new Exception("Failed to create Path!");
        }

        Path returnPath = JsonHelper.getInstance().jsonToBean(
                rmsg.getData().toString(),
                Path.class);

        Log.d(LOG_TAG, "createPath: Path created: " + returnPath);

        return returnPath;
    }

    public Path editPath(String pathId, Path path) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.pathId, pathId);
        params.put(StringConstant.pathTitle, path.getTitle());
        params.put(StringConstant.pathText, path.getText());
        params.put(StringConstant.pathCost, path.getCost());
        params.put(StringConstant.pathDateTime, String.valueOf(path.getDateTime()));
        params.put(StringConstant.pathEndDateTime, String.valueOf(path.getEndDateTime()));
        params.put(StringConstant.pathCreatedTime, String.valueOf(path.getCreatedTime()));
        params.put(StringConstant.pathAvailableTime, path.getAvailableTime());
        params.put(StringConstant.pathCapacity, String.valueOf(path.getCapacity()));
        params.put(StringConstant.pathImage, path.getImage());
        params.put(StringConstant.pathChosenCountry, path.getChosenCountry());
        params.put(StringConstant.pathChosenProvince, path.getProvince());
        params.put(StringConstant.pathChosenCity, path.getChosenCity());
        params.put(StringConstant.pathDetailAddress, path.getDetailAddress());
        params.put(StringConstant.pathLatitude, path.getLatitude());
        params.put(StringConstant.pathLongitude, path.getLongitude());
        params.put(StringConstant.pathType, String.valueOf(path.getType()));
        params.put(StringConstant.pathHXGroupId, path.getHxGroupId());

        params.put(StringConstant.pathTags, String.valueOf(path.getTags()));
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_EDIT_PATH, params);


        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "editPath: Return message code not positive");
            throw new Exception("Failed to edit Path!");
        }

        Path returnPath = JsonHelper.getInstance().jsonToBean(
                rmsg.getData().toString(),
                Path.class);

        Log.d(LOG_TAG, "editPath: Path edited: " + returnPath);

        return returnPath;
    }

    public Path deletePath(String pathId) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.pathId, pathId);
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_DELETE_PATH, params);

        Log.d(LOG_TAG, "deletePath: " + jsonStr);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "deletePath: Return message code not positive");
            throw new Exception("Failed to delete Path!");
        }

        Path returnPath = JsonHelper.getInstance().jsonToBean(
                rmsg.getData().toString(),
                Path.class);

        Log.d(LOG_TAG, "deletePath: Path deleted: " + returnPath);

        return returnPath;
    }

    public Path acceptPath(String pathId, String userId) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.pathId, pathId);
        params.put(StringConstant.userIdStr, userId);
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_ACCEPT_PATH, params);

        Log.d(LOG_TAG, "acceptPath: " + jsonStr);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "acceptPath: Return message code not positive");
            throw new Exception("Failed to accept Path!");
        }

        Path returnPath = JsonHelper.getInstance().jsonToBean(
                rmsg.getData().toString(),
                Path.class);

//        Log.d(LOG_TAG, "acceptPath: Path accepted: " + returnPath);

        return returnPath;
    }

    public Path unAcceptPath(String pathId, String userId) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.pathId, pathId);
        params.put(StringConstant.userIdStr, userId);
        params.put(StringConstant.verStr, APPVERSION);


        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_UNACCEPT_PATH, params);

        Log.d(LOG_TAG, "unAcceptPath: " + jsonStr);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "unAcceptPath: Return message code not positive");
            throw new Exception("Failed to unAccept Path!");
        }

        Path returnPath = JsonHelper.getInstance().jsonToBean(
                rmsg.getData().toString(),
                Path.class);

        Log.d(LOG_TAG, "unAcceptPath: Path unAccepted: " + returnPath);

        return returnPath;
    }

    public String uploadPathImage(String path_id, Bitmap image) throws Exception {
        String jsonStr = MsgSender.postPathImageToNet(URLConstant.URL_UPLOAD_PATH_IMAGE, path_id, image);

        Log.d(LOG_TAG, "uploadPathImage: " + jsonStr);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "uploadPathImage: Return message code not positive");
            Log.e(LOG_TAG, "Remark: " + rmsg.getRemark());
            throw new Exception("Failed to upload Activity image!");
        }

        String imagePath = rmsg.getData().getString(StringConstant.pathImageUrl);

        Log.d(LOG_TAG, "uploadPathImage: Path image path: " + imagePath);

        return imagePath;
    }

    public Comment createComment(String user_id, String userTarId, String path_id, String text) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.userIdStr, user_id);
        params.put(StringConstant.pathId, path_id);
        params.put(StringConstant.commentText, text);
        if (userTarId != null)
            params.put(StringConstant.commentReplyUser, userTarId);
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_CREATE_COMMENT, params);

        Log.d(LOG_TAG, "createComment: " + jsonStr);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "createComment: Return message code not positive");
            throw new Exception("Failed to create Comment!");
        }

        Comment returnComment = JsonHelper.getInstance().jsonToBean(
                rmsg.getData().toString(),
                Comment.class);

        Log.d(LOG_TAG, "createComment: Comment created: " + returnComment);
        return returnComment;
    }

//    public Comment editComment(String commentId, String text) throws Exception {
//        Map<String, String> params = new HashMap<>();
//        params.put(StringConstant.commentId, commentId);
//        params.put(StringConstant.commentText, text);
//        params.put(StringConstant.verStr, APPVERSION);
//
//        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_EDIT_COMMENT, params);
//
//        Log.d(LOG_TAG, "editComment: " + jsonStr);
//
//        ReturnMsg rmsg = checkReturnMsg(jsonStr);
//
//        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
//            Log.e(LOG_TAG, "editComment: Return message code not positive");
//            throw new Exception("Failed to edit Comment!");
//        }
//
//        Comment returnComment = JsonHelper.getInstance().jsonToBean(
//                rmsg.getData().toString(),
//                Comment.class);
//
//        Log.d(LOG_TAG, "editComment: Comment edited: " + returnComment);
//        return returnComment;
//    }

    public Comment deleteComment(String commentId) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.commentId, commentId);
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_DELETE_COMMENT, params);

        Log.d(LOG_TAG, "deleteComment: " + jsonStr);
        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "deleteComment: Return message code not positive");
            throw new Exception("Failed to delete Comment!");
        }

        Comment returnComment = JsonHelper.getInstance().jsonToBean(
                rmsg.getData().toString(),
                Comment.class);

//        Log.d(LOG_TAG, "deleteComment: Comment deleted: " + returnComment);
        return returnComment;
    }

    public String uploadPhoto(String user_id, String description, Bitmap image) throws Exception {

        String jsonStr = MsgSender.postPhotoToNet(
                URLConstant.URL_UPLOAD_PHOTO, user_id, description, image);

        Log.d(LOG_TAG, "uploadPhoto: " + jsonStr);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "uploadImage: Return message code not positive");
            Log.e(LOG_TAG, "Remark: " + rmsg.getRemark());
            throw new Exception("Failed to upload Image!");
        }

        String imagePath = rmsg.getData().getString(StringConstant.photoStr);

        Log.d(LOG_TAG, "uploadImage: Image path: " + imagePath);

        return imagePath;
    }

    public ArrayList<Photo> getUsersPhotos(String user_id) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.userIdStr, user_id);
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_GET_USERS_PHOTOS, params);

        Log.d(LOG_TAG, "getUsersPhotos: " + jsonStr);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "getUsersPhotos: Return message code not positive");
            throw new Exception("Failed to get User's Photos!");
        }

        ArrayList<Photo> photoArray = new ArrayList<>();
        JSONArray jsonArray = rmsg.getData().getJSONArray(StringConstant.photos);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            photoArray.add((Photo) JsonHelper
                    .getInstance().jsonToBean(
                            jsonObject.toString(),
                            Photo.class));
        }

        Log.d(LOG_TAG, "getUsersPhotos: Photos: " + photoArray);

        return photoArray;
    }

    public Photo editPhoto(Photo photo) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.photoId, photo.getId());
        params.put(StringConstant.photoDescription, photo.getDescription());
        params.put(StringConstant.verStr, APPVERSION);

        Log.d(LOG_TAG, "editPhoto: " + params.toString());

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_EDIT_PHOTO, params);

        Log.d(LOG_TAG, "editPhoto: " + jsonStr);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "editPhoto: Return message code not positive");
            throw new Exception("Failed to edit Photo!");
        }

        Photo returnPhoto = JsonHelper.getInstance().jsonToBean(
                rmsg.getData().toString(),
                Photo.class);

        Log.d(LOG_TAG, "editPhoto: Photo edited: " + returnPhoto);

        return returnPhoto;
    }

    public Photo deletePhoto(String photoId) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.photoId, photoId);
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_DELETE_PHOTO, params);

        Log.d(LOG_TAG, "deletePhoto: " + jsonStr);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "deletePhoto: Return message code not positive");
            throw new Exception("Failed to delete Photo!");
        }

        Photo returnPhoto = JsonHelper.getInstance().jsonToBean(
                rmsg.getData().toString(),
                Photo.class);

        Log.d(LOG_TAG, "deletePhoto: Photo deleted: " + returnPhoto);

        return returnPhoto;
    }

    public ArrayList<LingoNotification> getAllNotifications(String userId) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.userIdStr, userId);
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_GET_ALL_NOTIFICATIONS, params);

        Log.d(LOG_TAG, "getAllNotifications: " + jsonStr);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "getAllNotifications: Return message code not positive");
            throw new Exception("Failed to get all Notifications");
        }

        ArrayList<LingoNotification> notificationArray = new ArrayList<>();
        JSONArray jsonArray = rmsg.getData().getJSONArray(StringConstant.notifications);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            notificationArray.add((LingoNotification) JsonHelper
                    .getInstance().jsonToBean(
                            jsonObject.toString(),
                            LingoNotification.class));
        }

        Log.d(LOG_TAG, "num=" + notificationArray.size() + "getAllNotifications: Notifications: " + notificationArray);

        return notificationArray;
    }

    public ArrayList<LingoNotification> getAllNewNotifications() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.userIdStr, CacheHelper.getInstance().getSelfInfo().getId());
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_GET_ALL_NEW_NOTIFICATIONS, params);

        Log.d(LOG_TAG, "getAllNewNotifications: " + jsonStr);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "getAllNewNotifications: Return message code not positive");
            throw new Exception("Failed to get all Notifications");
        }

        ArrayList<LingoNotification> notificationArray = new ArrayList<>();
        JSONArray jsonArray = rmsg.getData().getJSONArray(StringConstant.notifications);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            notificationArray.add((LingoNotification) JsonHelper
                    .getInstance().jsonToBean(
                            jsonObject.toString(),
                            LingoNotification.class));
        }

        Log.d(LOG_TAG, "getAllNewNotifications: Notifications: " + notificationArray);

        return notificationArray;
    }

    public LingoNotification deleteNotification(String notificationId) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.notificationId, notificationId);
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_DELETE_NOTIFICATION, params);

        Log.d(LOG_TAG, "deleteNotification: " + jsonStr);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "deleteNotification: Return message code not positive");
            throw new Exception("Failed to delete Notification!");
        }

        LingoNotification returnNotification = JsonHelper.getInstance().jsonToBean(
                rmsg.getData().toString(),
                LingoNotification.class);

        Log.d(LOG_TAG, "deleteNotification: Notification deleted: " + returnNotification);

        return returnNotification;
    }

    public void readNotification(String notificationId) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.notificationId, notificationId);
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_READ_NOTIFICATIONS, params);
        Log.d(LOG_TAG, "readNotification: " + jsonStr);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "readNotification: Return message code not positive");
            throw new Exception("Failed to mark Notification as read!");
        }

        Log.d(LOG_TAG, "readNotification: Notification read");
    }

    //个人信息——旅行记录
    public void createExperiences(String userId, String startTime, String endTime, String country, String proivnce, String city, String tags) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.createExperienceUserIdStr, userId);
        params.put(StringConstant.createExperienceStartTimeStr, startTime);
        params.put(StringConstant.createExperienceEndTimeStr, endTime);
        params.put(StringConstant.createExperienceCountryStr, country);
        params.put(StringConstant.createExperienceProvinceStr, proivnce);
        params.put(StringConstant.createExperienceCityStr, city);
        params.put(StringConstant.createExperienceTagsStr, tags);
        params.put(StringConstant.verStr, APPVERSION);
//        Log.d("星期","travel>>>>"+params.toString());
        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_EXPERIENCES_CREATE, params);
//        Log.d("星期","travel>>>>"+jsonStr.toString());

        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "readNotification: Return message code not positive");
            throw new Exception("Failed to mark Notification as read!");
        }
    }

    public ArrayList<Travel> getExperiences(String userId) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.getExperienceStr, userId);
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_EXPERIENCES_GET, params);
//        Log.d(LOG_TAG,"travel>>>>"+jsonStr.toString());

        JSONObject obj = new JSONObject(jsonStr);
        //加一个json解析
        ArrayList<Travel> list = JsonHelper.getInstance().jsonToTravel(obj.getJSONArray("data").toString());

        if (obj.getInt("code") != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "readNotification: Return message code not positive");
            throw new Exception("Failed to mark Notification as read!");
        }
//        Log.d(LOG_TAG,"travel>>>>"+list.toString());
        return list;
    }

    public void editExperiences(String experienceId, String startTime, String endTime, String country, String province, String city, String tags) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.editExperienceExperienceIdStr, experienceId);
        params.put(StringConstant.editExperienceStartTimeStr, startTime);
        params.put(StringConstant.editExperienceEndTimeStr, endTime);
        params.put(StringConstant.editExperienceCountryStr, country);
        params.put(StringConstant.editExperienceProvinceStr, province);
        params.put(StringConstant.editExperienceCityStr, city);
        params.put(StringConstant.editExperienceTagsStr, tags);
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_EXPERIENCES_EDIT, params);
        Log.d(LOG_TAG, "editExperiences " + jsonStr);

        JSONObject obj = new JSONObject(jsonStr);
        //加一个json解析
        ArrayList<Travel> list = JsonHelper.getInstance().jsonToTravel(obj.getJSONArray("data").toString());

        if (obj.getInt("code") != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "readNotification: Return message code not positive");
            throw new Exception("Failed to mark Notification as read!");
        }
    }

    public void deleteExperiences(String experienceId) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.deleteExperienceStr, experienceId);
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_EXPERIENCES_DELETE, params);
        Log.d(LOG_TAG, "deleteExperiences " + jsonStr);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "readNotification: Return message code not positive");
            throw new Exception("Failed to mark Notification as read!");
        }
    }

    //订单信息
    public Indent createApplication(HashMap<String, String> params) throws Exception {

        Log.d("星期", params.toString());

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_CREATE_APPLICATION, params);
        Log.d(LOG_TAG, "createApplication " + jsonStr);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "createApplication: Return message code not positive");
            throw new Exception("Failed to  createApplication");
        }
        //解析
        String json = rmsg.getData().toString();
        JSONObject jsonObject = new JSONObject(json);
        return (Indent) JsonHelper
                .getInstance().jsonToBean(
                        jsonObject.toString(),
                        Indent.class);
    }

    public ArrayList<Indent> getApplication(HashMap<String, String> params) throws Exception {

//        Log.d("星期", params.toString());
        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_GET_APPLICATION, params);
        Log.d(LOG_TAG, "getApplication " + jsonStr);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "getApplication: Return message code not positive");
            throw new Exception("Failed to getApplication");
        }
        //解析
        String json = rmsg.getData().get("applications").toString();
        JSONArray jsonArray = new JSONArray(json);
        datas = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            datas.add((Indent) JsonHelper
                    .getInstance().jsonToBean(
                            jsonObject.toString(),
                            Indent.class));
        }
        return datas;
    }

    public Indent editApplication(HashMap<String, String> params) throws Exception {
        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_EDIT_APPLICATION, params);
        Log.d(LOG_TAG, "editApplication " + jsonStr);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "editApplication: Return message code not positive");
            throw new Exception("Failed to editApplication");
        }
        //解析
        String json = rmsg.getData().toString();
        JSONObject jsonObject = new JSONObject(json);
        return (Indent) JsonHelper
                .getInstance().jsonToBean(
                        jsonObject.toString(),
                        Indent.class);
    }

    public boolean existApplication(HashMap<String, String> params) throws Exception {
//        Log.d("星期",params.toString());
        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_EXIST_APPLICATION, params);
        Log.d(LOG_TAG, "existApplication " + jsonStr);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "existApplication: Return message code not positive");
            throw new Exception("Failed to existApplication");
        }
        //解析
        if ("true".contentEquals(rmsg.getData().get("exists").toString())) {
            return true;
        } else {
            return false;
        }
    }

    // 活动评论相关

    /**
     * 创建一个活动评论
     *
     * @param params
     * @return
     * @throws Exception
     */
    public PathReference createPathReference(HashMap<String, String> params) throws Exception {
        Log.d("星期", params.toString());

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_PATHREFERENCE_CREATE, params);
        Log.d(LOG_TAG, "createPathReference " + jsonStr);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "createPathReference: Return message code not positive");
            throw new Exception("Failed to  createPathReference");
        }
        //解析
        String json = rmsg.getData().toString();
        JSONObject jsonObject = new JSONObject(json);
        return (PathReference) JsonHelper
                .getInstance().jsonToBean(
                        jsonObject.toString(),
                        PathReference.class);
    }

    /**
     *
     * @param params
     * @return
     * @throws Exception
     */
//    public PathReference editPathReference(HashMap<String,String> params) throws Exception{
//        Log.d("星期", params.toString());
//
//        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_PATHREFERENCE_EDIT, params);
//        Log.d(LOG_TAG, "editPathReference " + jsonStr);
//
//        ReturnMsg rmsg = checkReturnMsg(jsonStr);
//
//        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
//            Log.e(LOG_TAG, "editPathReference: Return message code not positive");
//            throw new Exception("Failed to  editPathReference");
//        }
//        //解析
//        String json = rmsg.getData().toString();
//        JSONObject jsonObject = new JSONObject(json);
//        return (PathReference) JsonHelper
//                .getInstance().jsonToBean(
//                        jsonObject.toString(),
//                        PathReference.class);
//    }

    /**
     * 删除一条活动的评论
     *
     * @String referenceId
     */
    public PathReference deletePathReference(HashMap<String, String> params) throws Exception {
        Log.d("星期", params.toString());

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_PATHREFERENCE_DELETE, params);
        Log.d(LOG_TAG, "deletePathReference " + jsonStr);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "deletePathReference: Return message code not positive");
            throw new Exception("Failed to  deletePathReference");
        }
        //解析
        String json = rmsg.getData().toString();
        JSONObject jsonObject = new JSONObject(json);
        return (PathReference) JsonHelper
                .getInstance().jsonToBean(
                        jsonObject.toString(),
                        PathReference.class);
    }

    /**
     * 给活动的评论发表回复
     *
     * @return
     * @throws Exception
     */
    public PathReference createPathReplyReference(String referenceId, String userId, String name, String content) throws Exception {
        HashMap<String, String> params = new HashMap<>();
        params.put("referenceId", referenceId);
        params.put("userId", userId);
        params.put("userName", name);
        params.put("content", content);

//        Log.d("星期", params.toString());

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_PATHREFERENCE_REPLY, params);
        Log.d(LOG_TAG, "createPathReplyReference " + jsonStr);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "createPathReplyReference: Return message code not positive");
            throw new Exception("Failed to  createPathReplyReference");
        }
        //解析
        String json = rmsg.getData().toString();
        JSONObject jsonObject = new JSONObject(json);
        return (PathReference) JsonHelper
                .getInstance().jsonToBean(
                        jsonObject.toString(),
                        PathReference.class);
    }

    /**
     * 删除一条活动评论的回复
     *
     * @param params
     * @return
     * @throws Exception
     */
    public PathReference deletePathReplyReference(HashMap<String, String> params) throws Exception {
//        Log.d("星期", params.toString());

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_PATHREFERENCE_DELETEREPLY, params);
        Log.d(LOG_TAG, "deletePathReplyReference " + jsonStr);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "deletePathReplyReference: Return message code not positive");
            throw new Exception("Failed to  deletePathReplyReference");
        }
        //解析
        String json = rmsg.getData().toString();
        JSONObject jsonObject = new JSONObject(json);
        return (PathReference) JsonHelper
                .getInstance().jsonToBean(
                        jsonObject.toString(),
                        PathReference.class);
    }

    /**
     * 获取一个活动的评论
     *
     * @param params
     * @return
     * @throws Exception
     */
    public ArrayList<PathReference> getPathReference(HashMap<String, String> params) throws Exception {
//        Log.d("星期", params.toString());

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_PATHREFERENCE_GETREFERENCE, params);
        Log.d(LOG_TAG, "getPathReference " + jsonStr);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);

        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "getPathReference: Return message code not positive");
            throw new Exception(rmsg.getRemark());
//            throw new Exception("Failed to getPathReference");
        }
        //解析
        String json = rmsg.getData().toString();
        JSONObject jsonObject = new JSONObject(json);
        JSONArray jsonArray = jsonObject.getJSONArray("references");
        ArrayList<PathReference> list = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
            list.add((PathReference) JsonHelper
                    .getInstance().jsonToBean(
                            jsonObject1.toString(),
                            PathReference.class));
        }

//        Log.d("getPathReference", list.size() + ">>>>" + list.toString());

        return list;
    }

}
