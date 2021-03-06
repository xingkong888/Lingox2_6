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
import cn.lingox.android.entity.TravelComment;
import cn.lingox.android.entity.TravelEntity;
import cn.lingox.android.entity.User;

// TODO Fix debug and error logging
// TODO Fix exception throws/catches (better names/messages)

public class ServerHelper {

    private static final String APPVERSION = LingoXApplication.getInstance().getAppVersion();
    private static String LOG_TAG = "ServerHelper";
    private static ServerHelper instance = null;

    private ServerHelper() {
    }

    public static synchronized ServerHelper getInstance() {
        if (instance == null)
            instance = new ServerHelper();
        return instance;
    }

    /**
     * json数据解析
     *
     * @param jsonStr 待解析的json数据
     * @return ReturnMsg的实例：解析成功；null：解析失败
     */
    private ReturnMsg checkReturnMsg(String jsonStr) {
        if (jsonStr != null && !"".equals(jsonStr)) {
            JSONObject jobj;
            try {
                jobj = new JSONObject(jsonStr);
                return new ReturnMsg(jobj.getInt("code"), jobj.getJSONObject("data"), jobj.getString("remark"));
            } catch (JSONException e) {
                Log.e(LOG_TAG, "checkReturnMessage(): Error happened: " + e.getMessage());
                return null;
            }
        } else {
            Log.e(LOG_TAG, "checkReturnMessage(): jsonStr is null or empty");
            return new ReturnMsg(StatusCodeConstant.STATUS_JSON_ERR, null, "Received no data from the server");
        }
    }

    /**
     * 版本更新
     *
     * @param currentVer 版本号
     * @return true 有新版本 false 没有新版本
     */
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

    /**
     * 用户注册
     *
     * @param email    邮箱
     * @param userName 用户名
     * @param password 用户密码
     * @return 用户实例
     * @throws Exception
     */
    public User register(String email, String userName, String password) throws Exception {

        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.emailStr, email);
        params.put(StringConstant.usernameStr, userName);
        params.put(StringConstant.passwordStr, password);
        params.put(StringConstant.verStr, APPVERSION);
        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_REGISTER, params);
//        Log.d(LOG_TAG, "register: " + jsonStr);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "register:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "register: Return message code not positive");
            throw new Exception(rmsg.getRemark());
        }
//        Log.d(LOG_TAG, "register: User Info: " + user);
        return JsonHelper.getInstance().jsonToBean(rmsg.getData().toString(), User.class);
    }

    /**
     * 获取双方关系
     *
     * @param userId1 当前用户id
     * @param userId2 目标用户id
     * @return true两者已相互关注 false没有相互关注
     * @throws Exception
     */
    public boolean getBothFollowed(String userId1, String userId2) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("userId", userId1);
        params.put("userTar", userId2);
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_GET_USER_RELATION, params);
        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "getBothFollowed:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "getBothFollowed: Return message code not positive");
            throw new Exception(rmsg.getRemark());
        }
        return rmsg.getData().getBoolean("bothFollowed");
    }

    /**
     * 用户登录
     *
     * @param emailOrUsername 邮箱或用户名
     * @param password        密码
     * @return 用户实例
     * @throws Exception
     */
    public User login(String emailOrUsername, String password) throws Exception {

        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.emailOrUsernameStr, emailOrUsername);
        params.put(StringConstant.passwordStr, password);
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_LOGIN, params);
        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "login:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "login: Return message code not positive");
            throw new Exception(rmsg.getRemark());
        }
        return JsonHelper.getInstance().jsonToBean(
                rmsg.getData().toString(), User.class);
    }

    /**
     * 找回密码
     *
     * @param email 邮箱
     * @throws Exception
     */
    public void forgotPassword(String email) throws Exception {

        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.emailStr, email);
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_FORGOT_PASSWORD, params);
        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "forgotPassword:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "forgotPassword: Return message code not positive");
            Log.e(LOG_TAG, "Remark: " + rmsg.getRemark());
            throw new Exception(rmsg.getRemark());
        }
    }

    /**
     * 更换头像
     *
     * @param user_id 用户id
     * @param avatar  头像
     * @return 头像链接
     * @throws Exception
     */
    public String uploadAvatar(String user_id, Bitmap avatar) throws Exception {

        String jsonStr = MsgSender.postAvatarToNet(URLConstant.URL_UPLOAD_AVATAR, user_id, avatar);
        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "uploadAvatar:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "uploadAvatar: Return message code not positive");
            Log.e(LOG_TAG, "Remark: " + rmsg.getRemark());
            throw new Exception("Failed to upload Avatar!");
        }
        return rmsg.getData().getString(StringConstant.avatarStr);
    }

    /**
     * 更新用户信息
     *
     * @param params 更新信息
     * @return 用户实例
     * @throws Exception
     */
    public User updateUserInfo(Map<String, String> params) throws Exception {
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_UPDATE_INFO, params);
        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "updateUserInfo:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "updateUserInfo: Return message code not positive");
            throw new Exception("Failed to update User info!");
        }
        return JsonHelper.getInstance().jsonToBean(
                rmsg.getData().toString(), User.class);
    }

    // Contact
    // Returns the new relation code of the contact
    public int userRelationChange(String my_user_id, String tar_user_id, int user_relation) throws Exception {

        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.userSourceStr, my_user_id);
        params.put(StringConstant.userTargetStr, tar_user_id);
        params.put(StringConstant.userRelationStr,
                String.valueOf(user_relation));
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_CHANGE_RELATION, params);
        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "userRelationChange:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "userRelationChange: Return message code not positive");
            throw new Exception("Failed to change User relation!");
        }

        return rmsg.getData().getInt(StringConstant.userRelationStr);
    }

    /**
     * @param user_src_id 用户id
     * @return 用户实例的集合
     * @throws Exception
     */
    public ArrayList<User> getContactList(String user_src_id) throws Exception {

        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.userSourceStr, user_src_id);
        params.put(StringConstant.userIdStr, CacheHelper.getInstance().getSelfInfo().getId());
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_GET_CONTACT_LIST, params);
        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "getContactList:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "getContactList: Return message code not positive: " + rmsg.getRemark());
            throw new Exception("Failed to get Contact List!");
        }

        ArrayList<User> contactList = new ArrayList<>();
        JSONArray jsonArray = rmsg.getData().getJSONArray("contacts");
        for (int i = 0, j = jsonArray.length(); i < j; i++) {
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

    /**
     * 获取用户信息详情
     *
     * @param tar_user_id 目标id
     * @return 用户实例
     * @throws Exception
     */
    public User getUserInfo(String tar_user_id) throws Exception {
        if (LingoXApplication.getInstance().getSkip()) {
            return getUserInfo("547a6dbda06d0fd45b41bc89", tar_user_id);
        } else {
            return getUserInfo(CacheHelper.getInstance().getSelfInfo().getId(), tar_user_id);
        }
    }

    /**
     * 获取用户信息详情
     *
     * @param my_user_id  当前用户id
     * @param tar_user_id 目标用户id
     * @return 用户实例
     * @throws Exception
     */
    private User getUserInfo(String my_user_id, String tar_user_id) throws Exception {

        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.userSourceStr, my_user_id);
        params.put(StringConstant.userTargetStr, tar_user_id);
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_GET_USER_INFO, params);
        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "getUserInfo:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "getUserInfo: Return message code not positive");
            throw new Exception("Failed to get User's info!");
        }

        User returnUser = JsonHelper.getInstance().jsonToBean(rmsg.getData().toString(), User.class);
        CacheHelper.getInstance().addUserInfo(returnUser);

        return returnUser;
    }

    /**
     * 获取用户Following的数据
     *
     * @param user_tar_id 用户id
     * @return 用户实例的集合
     * @throws Exception
     */
    public ArrayList<User> getUserFollowing(String user_tar_id) throws Exception {

        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.userTargetStr, user_tar_id);
        params.put(StringConstant.userIdStr, CacheHelper.getInstance().getSelfInfo().getId());
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_GET_USER_FOLLOWING, params);
        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "getUserFollowing:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "getUserFollowing: Return message code not positive: " + rmsg.getRemark());
            throw new Exception("Failed to get User's Followers!");
        }

        ArrayList<User> contactList = new ArrayList<>();
        JSONArray jsonArray = rmsg.getData().getJSONArray(
                "contacts");
        for (int i = 0, j = jsonArray.length(); i < j; i++) {
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

    /**
     * 提示服务器用户登录，修改登录时间
     *
     * @param user_id 用户id
     * @throws Exception
     */
    public void loginTime(String user_id) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.userIdStr, user_id);

        MsgSender.postJsonToNet(URLConstant.URL_LOGINTIME, params);

        User user = CacheHelper.getInstance().getSelfInfo();
        user.setLoginTime(String.valueOf(System.currentTimeMillis() / 1000L));
        CacheHelper.getInstance().setSelfInfo(user);
    }

    /**
     * members页面数据获取
     *
     * @param user_id    当前用户id
     * @param searchType 搜索类型
     * @param params     数据
     * @param page       页码
     * @return 用户实例的集合
     * @throws Exception
     */
    public ArrayList<User> searchUser(String user_id, int searchType, Map<String, String> params, int page) throws Exception {
        if (params == null) {
            params = new HashMap<>();
        }
        params.put(StringConstant.userIdStr, user_id);
        params.put(StringConstant.searchType, String.valueOf(searchType));
        params.put("page", String.valueOf(page));
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_SEARCH_USER, params);
        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "searchUser:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "searchUser: Return message code not positive");
            throw new Exception("Failed to search for users!");
        }

        LingoXApplication.getInstance().setUserPageCount(rmsg.getData().getInt("pageCount"));

        ArrayList<User> searchResult = new ArrayList<>();

        JSONArray jsonArray = rmsg.getData().getJSONArray(
                StringConstant.searchResult);
        for (int i = 0, j = jsonArray.length(); i < j; i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            User user = JsonHelper
                    .getInstance().jsonToBean(
                            jsonObject.toString(),
                            User.class);

            searchResult.add(user);
            CacheHelper.getInstance().addUserInfo(user);
        }
        return searchResult;
    }
    // References

    /**
     * 默认返回第一页数据
     *
     * @return 用户实例的集合
     * @throws Exception
     */
    public ArrayList<User> searchUserDefault() throws Exception {
        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_DEFAULT_USER);
        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "searchUser:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "searchUser: Return message code not positive");
            throw new Exception("Failed to search for users!");
        }
        ArrayList<User> searchResult = new ArrayList<>();

        JSONArray jsonArray = rmsg.getData().getJSONArray(
                StringConstant.searchResult);
        for (int i = 0, j = jsonArray.length(); i < j; i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            User user = JsonHelper
                    .getInstance().jsonToBean(
                            jsonObject.toString(),
                            User.class);

            searchResult.add(user);
            CacheHelper.getInstance().addUserInfo(user);
        }
        return searchResult;
    }

    /**
     * 创建和修改评论
     *
     * @param referenceId 评论id，用于修改，创建是传 ""空值
     * @param user_src_id 当前用户id ，用于创建
     * @param user_tar_id 目标用户id。用于创建
     * @param title       标题，与content重复
     * @param content     内容
     * @return 评论的实例
     * @throws Exception
     */
    public Reference reference(String referenceId, String user_src_id, String user_tar_id, String title,
                               String content) throws Exception {
        Map<String, String> params = new HashMap<>();
        String jsonStr;
        params.put(StringConstant.referenceTitle, title);
        params.put(StringConstant.referenceContent, content);
        params.put(StringConstant.verStr, APPVERSION);
        if (referenceId.isEmpty()) {
            //创建
            params.put(StringConstant.userSourceStr, user_src_id);
            params.put(StringConstant.userTargetStr, user_tar_id);
            jsonStr = MsgSender.postJsonToNet(URLConstant.URL_CREATE_REFERENCE, params);
            Log.d(LOG_TAG, "CreateReference: " + jsonStr);
        } else {
            //修改
            params.put(StringConstant.referenceId, referenceId);
            jsonStr = MsgSender.postJsonToNet(URLConstant.URL_EDIT_REFERENCE, params);
            Log.d(LOG_TAG, "EditReference: " + jsonStr);
        }
        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "createReference:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "createReference: Return message code not positive");
            throw new Exception("Failed to create Reference!");
        }

        return JsonHelper.getInstance().jsonToBean(rmsg.getData().toString(), Reference.class);
    }

    /**
     * 回复某条评论
     *
     * @param referenceId 评论id
     * @param reply       回复内容
     * @return 评论的实例
     * @throws Exception
     */
    public Reference replyReference(String referenceId, String reply) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.referenceId, referenceId);
        params.put("reply", reply);
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_EDIT_REFERENCE, params);
        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "replyReference:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "replyReference: Return message code not positive");
            throw new Exception("Failed to reply Reference!");
        }

        return JsonHelper.getInstance().jsonToBean(
                rmsg.getData().toString(), Reference.class);
    }

    /**
     * 删除评论
     *
     * @param referenceId 评论id
     * @return 评论的实例
     * @throws Exception
     */
    public Reference deleteReference(String referenceId) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.referenceId, referenceId);
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_DELETE_REFERENCE, params);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "deleteReference:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "deleteReference: Return message code not positive");
            throw new Exception("Failed to delete Reference!");
        }

        return JsonHelper.getInstance().jsonToBean(
                rmsg.getData().toString(), Reference.class);
    }

    /**
     * 获取用户的评论
     *
     * @param userId 用户id
     * @return 评论实例的集合
     * @throws Exception
     */
    public ArrayList<Reference> getUsersReferences(String userId) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.userIdStr, userId);
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_GET_USERS_REFERENCES, params);
        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "getUsersReferences:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "getUsersReferences: Return message code not positive: " + rmsg.getRemark());
            throw new Exception("Failed to get Reference items!");
        }

        ArrayList<Reference> referenceArray = new ArrayList<>();
        JSONArray jsonArray = rmsg.getData().getJSONArray(
                "references");
        for (int i = 0, j = jsonArray.length(); i < j; i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            referenceArray.add((Reference) JsonHelper
                    .getInstance().jsonToBean(
                            jsonObject.toString(),
                            Reference.class));
        }
        return referenceArray;
    }

    /**
     * 获取某一活动详细数据
     *
     * @param pathId 活动id
     * @return 活动实例
     * @throws Exception
     */
    public Path getPath(String pathId) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.pathId, pathId);
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_GET_PATH, params);
        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "getPath:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "getPath: Return message code not positive: " + rmsg.getRemark());
            throw new Exception("Failed to get Path!");
        }

        return JsonHelper.getInstance().jsonToBean(rmsg.getData().toString(), Path.class);
    }

    /**
     * discover页面数据获取
     *
     * @param page 分页加载页码，从1开始
     * @return 活动实例的集合
     * @throws Exception
     */
    public ArrayList<Path> getAllPaths(int page) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_GET_ALL_PATHS, params);
        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "getAllPaths:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "getAllPaths: Return message code not positive: " + rmsg.getRemark());
            throw new Exception("Failed to get Path items!");
        }

        ArrayList<Path> pathArray = new ArrayList<>();
        JSONArray jsonArray = rmsg.getData().getJSONArray(StringConstant.paths);
        for (int i = 0, j = jsonArray.length(); i < j; i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            pathArray.add((Path) JsonHelper
                    .getInstance().jsonToBean(
                            jsonObject.toString(),
                            Path.class));
        }
        return pathArray;
    }

    /**
     * 根据地理位置获取活动
     *
     * @param country       国家
     * @param province      省市
     * @param city          城市
     * @param localOrTravel 本地或旅行者
     * @param page          页码
     * @param postJson      需要提交的数据
     * @return 活动实例的集合
     * @throws Exception
     */
    public ArrayList<Path> getPathsByLocation(String country, String province, String city, int localOrTravel, int page, ArrayList<String> postJson) throws Exception {
        Map<String, String> params = new HashMap<>();
        if (!country.isEmpty()) {
            params.put(StringConstant.pathChosenCountry, country);
        }
        if (!province.isEmpty()) {
            params.put(StringConstant.pathChosenProvince, province);
        }
        if (!city.isEmpty()) {
            params.put(StringConstant.pathChosenCity, city);
        }
//        if (localOrTravel != 0) {
//            params.put("type", String.valueOf(localOrTravel));
//        }
        params.put("page", String.valueOf(page));
//        if (postJson.size() > 0) {
//            params.put("tags", postJson.toString());
//        }
        params.put(StringConstant.verStr, APPVERSION);
        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_GET_PATHS_BY_LOCATION, params);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "getPathsByLocation:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "getPathsByLocation: Return message code not positive: " + rmsg.getRemark());
            throw new Exception("Failed to get Path items!");
        }

        ArrayList<Path> pathArray = new ArrayList<>();
        JSONArray jsonArray = rmsg.getData().getJSONArray(StringConstant.paths);
        for (int i = 0, j = jsonArray.length(); i < j; i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            pathArray.add((Path) JsonHelper
                    .getInstance().jsonToBean(
                            jsonObject.toString(),
                            Path.class));
        }
        return pathArray;
    }

    /**
     * 获取某一用户参加和创建的活动
     *
     * @param userId 用户id
     * @param page   页码
     * @return 活动实例的集合
     * @throws Exception
     */
    public ArrayList<Path> getUsersPaths(String userId, int page) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.userIdStr, userId);
        params.put("page", String.valueOf(page));
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_GET_USERS_PATHS, params);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "getUsersPaths:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "getUsersPaths: Return message code not positive");
            throw new Exception("Failed to get Path items!");
        }

        ArrayList<Path> pathArray = new ArrayList<>();
        JSONArray jsonArray = rmsg.getData().getJSONArray(StringConstant.paths);
        for (int i = 0, j = jsonArray.length(); i < j; i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            pathArray.add((Path) JsonHelper
                    .getInstance().jsonToBean(
                            jsonObject.toString(),
                            Path.class));
        }
        return pathArray;
    }

    /**
     * 获取某一用户收藏的local
     *
     * @param userId 用户id
     * @param page   页码
     * @return 活动实例的集合
     * @throws Exception
     */
    public ArrayList<Path> getUsersFavouritePaths(String userId, int page) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.userIdStr, userId);
        params.put("page", String.valueOf(page));
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_FAVOURITE_LOCAL, params);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "getUsersFavouritePaths:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "getUsersFavouritePaths: Return message code not positive");
            throw new Exception("Failed to get Path items!");
        }

        ArrayList<Path> pathArray = new ArrayList<>();
        JSONArray jsonArray = rmsg.getData().getJSONArray(StringConstant.paths);
        for (int i = 0, j = jsonArray.length(); i < j; i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            pathArray.add((Path) JsonHelper
                    .getInstance().jsonToBean(
                            jsonObject.toString(),
                            Path.class));
        }
        return pathArray;
    }

    /**
     * 创建和修改活动
     *
     * @param flag 标识是创建“create”、修改“edit”
     * @param path 活动的实例
     * @return 活动实例
     * @throws Exception
     */
    public Path path(String flag, Path path) throws Exception {
        Map<String, String> params = new HashMap<>();
        String jsonStr = "";
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

        switch (flag) {
            case "create"://创建
                params.put(StringConstant.userIdStr, path.getUserId());
//                Log.d("星期",params.toString());
                jsonStr = MsgSender.postJsonToNet(URLConstant.URL_CREATE_PATH, params);
//                Log.d("星期",jsonStr);
                break;
            case "edit"://修改
                params.put(StringConstant.pathId, path.getId());
                jsonStr = MsgSender.postJsonToNet(URLConstant.URL_EDIT_PATH, params);
                break;
        }

        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "path:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "path: Return message code not positive");
            throw new Exception("Failed to create Path!" + rmsg.getRemark());
        }

        return JsonHelper.getInstance().jsonToBean(
                rmsg.getData().toString(), Path.class);
    }

    /**
     * 删除活动
     *
     * @param pathId 活动id
     * @return 活动的实例
     * @throws Exception
     */
    public Path deletePath(String pathId) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.pathId, pathId);
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_DELETE_PATH, params);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "deletePath:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "deletePath: Return message code not positive");
            throw new Exception("Failed to delete Path!");
        }
        return JsonHelper.getInstance().jsonToBean(
                rmsg.getData().toString(), Path.class);
    }

    /**
     * 同意参加活动
     *
     * @param pathId 活动id
     * @param userId 申请人id---当前用户
     * @return 活动的实例
     * @throws Exception
     */
    public Path acceptPath(String pathId, String userId) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.pathId, pathId);
        params.put(StringConstant.userIdStr, userId);
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_ACCEPT_PATH, params);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "acceptPath:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "acceptPath: Return message code not positive");
            throw new Exception("Failed to accept Path!");
        }
        return JsonHelper.getInstance().jsonToBean(
                rmsg.getData().toString(), Path.class);
    }

    /**
     * 拒绝参加活动
     *
     * @param pathId 活动的id
     * @param userId 用户id
     * @return 活动的实例
     * @throws Exception
     */
    public Path unAcceptPath(String pathId, String userId) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.pathId, pathId);
        params.put(StringConstant.userIdStr, userId);
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_UNACCEPT_PATH, params);
        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "unAcceptPath:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "unAcceptPath: Return message code not positive");
            throw new Exception("Failed to unAccept Path!");
        }

        return JsonHelper.getInstance().jsonToBean(
                rmsg.getData().toString(), Path.class);
    }

    /**
     * 上传活动图片
     *
     * @param path_id 活动id
     * @param image   图片实例
     * @return 活动图片的url集合
     * @throws Exception
     */
    public ArrayList<String> uploadPathImage(String path_id, Bitmap image) throws Exception {
        String jsonStr = MsgSender.postPathImageToNet(URLConstant.URL_UPLOAD_PATH_IMAGE, path_id, image);
        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "uploadPathImage:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "uploadPathImage: Return message code not positive");
            Log.e(LOG_TAG, "Remark: " + rmsg.getRemark());
            throw new Exception("Failed to upload Activity image!" + rmsg.getRemark());
        }

        String image21 = rmsg.getData().getString("image21");
        String image11 = rmsg.getData().getString("image11");
        String imageUrl = rmsg.getData().getString(StringConstant.pathImageUrl);
        ArrayList<String> list = new ArrayList<>();
        list.add(imageUrl);
        list.add(image11);
        list.add(image21);

        return list;
    }

    /**
     * 创建local的活动评论
     *
     * @param user_id   用户id
     * @param userTarId 目标用户id
     * @param path_id   活动id
     * @param text      评论内容
     * @return 评论的实例
     * @throws Exception
     */
    public Comment createComment(String user_id, String userTarId, String path_id, String text) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.userIdStr, user_id);
        params.put(StringConstant.pathId, path_id);
        params.put(StringConstant.commentText, text);
        if (userTarId != null) {
            params.put(StringConstant.commentReplyUser, userTarId);
        }
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_CREATE_COMMENT, params);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "createComment:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "createComment: Return message code not positive");
            throw new Exception("Failed to create Comment!");
        }
        return JsonHelper.getInstance().jsonToBean(
                rmsg.getData().toString(), Comment.class);
    }

    /**
     * 删除
     *
     * @param commentId 评论id
     * @return 评论的实例
     * @throws Exception
     */
    public Comment deleteComment(String commentId) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.commentId, commentId);
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_DELETE_COMMENT, params);
        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "deleteComment:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "deleteComment: Return message code not positive");
            throw new Exception("Failed to delete Comment!");
        }
        return JsonHelper.getInstance().jsonToBean(
                rmsg.getData().toString(), Comment.class);
    }

    /**
     * 上传个人相册
     *
     * @param user_id     用户id---当前用户
     * @param description 图片描述
     * @param image       图片实例
     * @return 图片的url
     * @throws Exception
     */
    public String uploadPhoto(String user_id, String description, Bitmap image) throws Exception {

        String jsonStr = MsgSender.postPhotoToNet(URLConstant.URL_UPLOAD_PHOTO, user_id, description, image);
        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "uploadImage:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "uploadImage: Return message code not positive");
            Log.e(LOG_TAG, "Remark: " + rmsg.getRemark());
            throw new Exception("Failed to upload Image!");
        }
        return rmsg.getData().getString(StringConstant.photoStr);
    }

    /**
     * 获取用户相册
     *
     * @param user_id 用户id
     * @return photo的集合
     * @throws Exception
     */
    public ArrayList<Photo> getUsersPhotos(String user_id) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.userIdStr, user_id);
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_GET_USERS_PHOTOS, params);
        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "getUsersPhotos:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "getUsersPhotos: Return message code not positive");
            throw new Exception("Failed to get User's Photos!");
        }

        ArrayList<Photo> photoArray = new ArrayList<>();
        JSONArray jsonArray = rmsg.getData().getJSONArray(StringConstant.photos);
        for (int i = 0, j = jsonArray.length(); i < j; i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            photoArray.add((Photo) JsonHelper
                    .getInstance().jsonToBean(
                            jsonObject.toString(),
                            Photo.class));
        }

        return photoArray;
    }

    /**
     * 修改图片信息
     *
     * @param photo photo的实例
     * @return photo的实例
     * @throws Exception
     */
    public Photo editPhoto(Photo photo) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.photoId, photo.getId());
        params.put(StringConstant.photoDescription, photo.getDescription());
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_EDIT_PHOTO, params);
        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "editPhoto:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "editPhoto: Return message code not positive");
            throw new Exception("Failed to edit Photo!");
        }
        return JsonHelper.getInstance().jsonToBean(
                rmsg.getData().toString(), Photo.class);
    }

    /**
     * 删除图片
     *
     * @param photoId 图片id
     * @return photo的实例
     * @throws Exception
     */
    public Photo deletePhoto(String photoId) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.photoId, photoId);
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_DELETE_PHOTO, params);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "deletePhoto:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "deletePhoto: Return message code not positive");
            throw new Exception("Failed to delete Photo!");
        }
        return JsonHelper.getInstance().jsonToBean(
                rmsg.getData().toString(), Photo.class);
    }

    /**
     * 获取用户所有通知
     *
     * @param userId 用户id----当前用户
     * @return 通知实例的集合
     * @throws Exception
     */
    public ArrayList<LingoNotification> getAllNotifications(String userId) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.userIdStr, userId);
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_GET_ALL_NOTIFICATIONS, params);
        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "getAllNotifications:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "getAllNotifications: Return message code not positive");
            throw new Exception("Failed to get all Notifications");
        }

        ArrayList<LingoNotification> notificationArray = new ArrayList<>();
        JSONArray jsonArray = rmsg.getData().getJSONArray(StringConstant.notifications);
        for (int i = 0, j = jsonArray.length(); i < j; i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            notificationArray.add((LingoNotification) JsonHelper
                    .getInstance().jsonToBean(
                            jsonObject.toString(),
                            LingoNotification.class));
        }
        return notificationArray;
    }

    /**
     * 获取用户新通知
     *
     * @return 通知实例的集合
     * @throws Exception
     */
    public ArrayList<LingoNotification> getAllNewNotifications() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.userIdStr, CacheHelper.getInstance().getSelfInfo().getId());
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_GET_ALL_NEW_NOTIFICATIONS, params);
        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "getAllNewNotifications:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "getAllNewNotifications: Return message code not positive");
            throw new Exception("Failed to get all Notifications");
        }

        ArrayList<LingoNotification> notificationArray = new ArrayList<>();
        JSONArray jsonArray = rmsg.getData().getJSONArray(StringConstant.notifications);
        for (int i = 0, j = jsonArray.length(); i < j; i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            notificationArray.add((LingoNotification) JsonHelper
                    .getInstance().jsonToBean(
                            jsonObject.toString(),
                            LingoNotification.class));
        }

        return notificationArray;
    }

    /**
     * 删除通知
     *
     * @param notificationId 通知id
     * @return 通知实例
     * @throws Exception
     */
    public LingoNotification deleteNotification(String notificationId) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.notificationId, notificationId);
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_DELETE_NOTIFICATION, params);
        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "deleteNotification:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "deleteNotification: Return message code not positive");
            throw new Exception("Failed to delete Notification!");
        }

        return JsonHelper.getInstance().jsonToBean(
                rmsg.getData().toString(), LingoNotification.class);
    }

    /**
     * 将通知标注为已读
     *
     * @param notificationId 通知id
     * @throws Exception
     */
    public void readNotification(String notificationId) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.notificationId, notificationId);
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_READ_NOTIFICATIONS, params);
        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "readNotification:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "readNotification: Return message code not positive");
            throw new Exception("Failed to mark Notification as read!");
        }
    }

    /**
     * 创建和修改个人旅行计划
     *
     * @param flag   标识是创建还是修改
     * @param travel travel实例
     * @throws Exception
     */
    public void travel(String flag, Travel travel) throws Exception {
        Map<String, String> params = new HashMap<>();
        String jsonStr = "";
        params.put(StringConstant.createExperienceStartTimeStr, String.valueOf(travel.getStartTime()));
        params.put(StringConstant.createExperienceEndTimeStr, String.valueOf(travel.getEndTime()));
        params.put(StringConstant.createExperienceCountryStr, travel.getCountry());
        params.put(StringConstant.createExperienceProvinceStr, travel.getProvince());
        params.put(StringConstant.createExperienceCityStr, travel.getCity());
        params.put(StringConstant.createExperienceTagsStr, "[]");
        params.put(StringConstant.verStr, APPVERSION);
        switch (flag) {
            case "create"://创建
                params.put(StringConstant.createExperienceUserIdStr, CacheHelper.getInstance().getSelfInfo().getId());
                jsonStr = MsgSender.postJsonToNet(URLConstant.URL_EXPERIENCES_CREATE, params);
                break;

            case "edit"://修改
                params.put(StringConstant.createExperienceUserIdStr, CacheHelper.getInstance().getSelfInfo().getId());
                jsonStr = MsgSender.postJsonToNet(URLConstant.URL_EXPERIENCES_CREATE, params);
                break;
        }

        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "Travel:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "Travel: Return message code not positive");
            throw new Exception("Failed to mark Notification as read!");
        }
    }

    /**
     * 获取用户的个人旅行计划
     *
     * @param userId 用户id
     * @return 计划集合
     * @throws Exception
     */
    public ArrayList<Travel> getExperiences(String userId) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.getExperienceStr, userId);
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_EXPERIENCES_GET, params);

        JSONObject obj = new JSONObject(jsonStr);
        //加一个json解析
        ArrayList<Travel> list = JsonHelper.getInstance().jsonToTravel(obj.getJSONArray("data").toString());

        if (obj.getInt("code") != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "readNotification: Return message code not positive");
            throw new Exception("Failed to mark Notification as read!");
        }
        return list;
    }

    /**
     * 删除个人旅行计划
     *
     * @param experienceId 被删除的计划id
     * @throws Exception
     */
    public void deleteExperiences(String experienceId) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.deleteExperienceStr, experienceId);
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_EXPERIENCES_DELETE, params);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "readNotification:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "readNotification: Return message code not positive");
            throw new Exception("Failed to mark Notification as read!");
        }
    }

    //申请活动信息
    public Indent createApplication(HashMap<String, String> params) throws Exception {

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_CREATE_APPLICATION, params);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "createApplication:Parse failure");
            throw new Exception("Parse failure");
        }
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

    //获取活动通知
    public ArrayList<Indent> getApplication(HashMap<String, String> params) throws Exception {

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_GET_APPLICATION, params);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "getApplication:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "getApplication: Return message code not positive");
            throw new Exception("Failed to getApplication");
        }
        //解析
        String json = rmsg.getData().get("applications").toString();
        JSONArray jsonArray = new JSONArray(json);
        ArrayList<Indent> datas = new ArrayList<>();
        for (int i = 0, j = jsonArray.length(); i < j; i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            datas.add((Indent) JsonHelper
                    .getInstance().jsonToBean(
                            jsonObject.toString(),
                            Indent.class));
        }
        return datas;
    }

    //同意、拒绝、取消等状态的改变
    public Indent editApplication(HashMap<String, String> params) throws Exception {
        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_EDIT_APPLICATION, params);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "editApplication:Parse failure");
            throw new Exception("Parse failure");
        }
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

    //判断是否已存在申请
    public boolean existApplication(HashMap<String, String> params) throws Exception {
        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_EXIST_APPLICATION, params);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "existApplication:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "existApplication: Return message code not positive");
            throw new Exception("Failed to existApplication");
        }
        //解析
        return "true".contentEquals(rmsg.getData().get("exists").toString());
    }

    // 活动评论相关

    /**
     * 创建一个活动评论
     *
     * @param params 数据
     * @return 活动的评论
     * @throws Exception
     */
    public PathReference createPathReference(HashMap<String, String> params) throws Exception {

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_PATHREFERENCE_CREATE, params);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "createPathReference:Parse failure");
            throw new Exception("Parse failure");
        }
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
     * 删除一条活动的评论
     */
    public PathReference deletePathReference(HashMap<String, String> params) throws Exception {

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_PATHREFERENCE_DELETE, params);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "deletePathReference:Parse failure");
            throw new Exception("Parse failure");
        }
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
     * @return 活动的评论
     * @throws Exception
     */
    public PathReference createPathReplyReference(String referenceId, String userId, String name, String content) throws Exception {
        HashMap<String, String> params = new HashMap<>();
        params.put("referenceId", referenceId);
        params.put("userId", userId);
        params.put("userName", name);
        params.put("content", content);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_PATHREFERENCE_REPLY, params);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "createPathReplyReference:Parse failure");
            throw new Exception("Parse failure");
        }
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
     *暂时没用
     * @param params 数据
     * @return 活动的评论
     * @throws Exception
     */
//    public PathReference deletePathReplyReference(HashMap<String, String> params) throws Exception {
//
//        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_PATHREFERENCE_DELETEREPLY, params);
//
//        ReturnMsg rmsg = checkReturnMsg(jsonStr);
//        if (rmsg==null){
//            Log.e(LOG_TAG, "deletePathReplyReference:Parse failure");
//            throw new Exception("Parse failure");
//        }
//        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
//            Log.e(LOG_TAG, "deletePathReplyReference: Return message code not positive");
//            throw new Exception("Failed to  deletePathReplyReference");
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
     * 获取一个活动的评论
     *
     * @param params 数据
     * @return 活动评论实例的集合
     * @throws Exception
     */
    public ArrayList<PathReference> getPathReference(HashMap<String, String> params) throws Exception {

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_PATHREFERENCE_GETREFERENCE, params);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "getPathReference:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "getPathReference: Return message code not positive");
            throw new Exception(rmsg.getRemark());
        }
        //解析
        String json = rmsg.getData().toString();
        JSONObject jsonObject = new JSONObject(json);
        JSONArray jsonArray = jsonObject.getJSONArray("references");
        ArrayList<PathReference> list = new ArrayList<>();

        for (int i = 0, j = jsonArray.length(); i < j; i++) {
            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
            list.add((PathReference) JsonHelper
                    .getInstance().jsonToBean(
                            jsonObject1.toString(),
                            PathReference.class));
        }
        return list;
    }
/*********************************旅行者发布问题**************************************************************************/
    /**
     * 一次性获取所有的travel数据--分页加载
     *
     * @param page 页码
     * @return travel的实例集合
     * @throws Exception 抛出异常
     */
    public ArrayList<TravelEntity> getAllTravel(int page) throws Exception {
        return getAllTravel("", "", "", page);
    }

    /**
     * 获取travel数据--查询
     *
     * @param country  国家
     * @param province 省份
     * @param city     城市
     * @param page     页码
     * @return 集合
     * @throws Exception
     */
    public ArrayList<TravelEntity> getAllTravel(String country, String province, String city, int page) throws Exception {

        HashMap<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));
        params.put("country", country);
        params.put("province", province);
        params.put("city", city);
        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_TRAVEL_GETALL, params);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "getAllTravel:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "getAllTravel: Return message code not positive");
            throw new Exception(rmsg.getRemark());
        }
        //解析
        String json = rmsg.getData().toString();
        JSONObject jsonObject = new JSONObject(json);
        JSONArray jsonArray = jsonObject.getJSONArray("demands");
        ArrayList<TravelEntity> list = new ArrayList<>();

        for (int i = 0, j = jsonArray.length(); i < j; i++) {
            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
            list.add((TravelEntity) JsonHelper.getInstance().jsonToBean(jsonObject1.toString(), TravelEntity.class));
        }
        return list;
    }

    /**
     * 获取指定用户的travel
     *
     * @param userId 用户id
     * @param page   页码
     * @return travel的实例集合
     * @throws Exception 抛出异常
     */
    public ArrayList<TravelEntity> getUserTravel(String userId, int page) throws Exception {

        HashMap<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));
        params.put("userId", userId);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_USER_TRAVEL_GET, params);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "getUserTravel:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "getUserTravel: Return message code not positive");
            throw new Exception(rmsg.getRemark());
        }
        //解析
        String json = rmsg.getData().toString();
        JSONObject jsonObject = new JSONObject(json);
        JSONArray jsonArray = jsonObject.getJSONArray("demands");
        ArrayList<TravelEntity> list = new ArrayList<>();

        for (int i = 0, j = jsonArray.length(); i < j; i++) {
            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
            list.add((TravelEntity) JsonHelper.getInstance().jsonToBean(jsonObject1.toString(), TravelEntity.class));
        }
        return list;
    }

    /**
     * 获取指定用户的收藏的travel
     *
     * @param userId 用户id
     * @param page   页码
     * @return travel的实例集合
     * @throws Exception 抛出异常
     */
    public ArrayList<TravelEntity> getUserFavouriteTravel(String userId, int page) throws Exception {

        HashMap<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));
        params.put("userId", userId);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_FAVOURITE_TRAVEL, params);
        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "getUserTravel:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "getUserFavouriteTravel: Return message code not positive");
            throw new Exception(rmsg.getRemark());
        }
        //解析
        String json = rmsg.getData().toString();
        JSONObject jsonObject = new JSONObject(json);
        JSONArray jsonArray = jsonObject.getJSONArray("demands");
        ArrayList<TravelEntity> list = new ArrayList<>();

        for (int i = 0, j = jsonArray.length(); i < j; i++) {
            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
            list.add((TravelEntity) JsonHelper.getInstance().jsonToBean(jsonObject1.toString(), TravelEntity.class));
        }
        return list;
    }

    /**
     * 获取某一指定的travel
     *
     * @param id 指定travel的id
     * @return 对应id的travel实例
     * @throws Exception 抛出异常
     */
    public TravelEntity getTravel(String id) throws Exception {
        HashMap<String, String> params = new HashMap<>();
        params.put("demandId", id);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_TRAVEL_GET, params);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "getTravel:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "getTravel: Return message code not positive");
            throw new Exception(rmsg.getRemark());
        }
        //解析
        String json = rmsg.getData().toString();
        JSONObject jsonObject = new JSONObject(json);
        return (TravelEntity) JsonHelper
                .getInstance().jsonToBean(
                        jsonObject.toString(),
                        TravelEntity.class);
    }

    /**
     * 旅行者发布新的问题
     *
     * @param params 创建所需参数的集合
     * @return 创建的成功的实例
     * @throws Exception 抛出异常
     */
    public TravelEntity createTravel(HashMap<String, String> params) throws Exception {

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_TRAVEL_CREATE, params);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "createTravel:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "createTravel: Return message code not positive");
            throw new Exception(rmsg.getRemark());
        }
        //解析
        String json = rmsg.getData().toString();
        JSONObject jsonObject = new JSONObject(json);
        return (TravelEntity) JsonHelper
                .getInstance().jsonToBean(
                        jsonObject.toString(),
                        TravelEntity.class);
    }

    /**
     * 修改
     *
     * @param params 修改的数据集合
     * @return 修改成功后的实例
     * @throws Exception 抛出异常
     */
    public TravelEntity editTravel(HashMap<String, String> params) throws Exception {

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_TRAVEL_EDIT, params);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "editTravel:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "editTravel: Return message code not positive");
            throw new Exception(rmsg.getRemark());
        }
        //解析
        String json = rmsg.getData().toString();
        JSONObject jsonObject = new JSONObject(json);
        return (TravelEntity) JsonHelper
                .getInstance().jsonToBean(
                        jsonObject.toString(),
                        TravelEntity.class);
    }

    /**
     * 删除
     *
     * @param id 将要删除的数据的id
     * @return 删除成功后返回该数据实例
     * @throws Exception 抛出异常
     */
    public TravelEntity deleteTravel(String id) throws Exception {
        HashMap<String, String> params = new HashMap<>();
        params.put("demandId", id);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_TRAVEL_DELETE, params);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "deleteTravel:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "deleteTravel: Return message code not positive");
            throw new Exception(rmsg.getRemark());
        }
        //解析
        String json = rmsg.getData().toString();
        JSONObject jsonObject = new JSONObject(json);
        return (TravelEntity) JsonHelper
                .getInstance().jsonToBean(
                        jsonObject.toString(),
                        TravelEntity.class);
    }

    /**
     * like
     *
     * @param params 必要的参数
     * @return 返回结果
     * @throws Exception 异常
     */
    public TravelEntity likeTravel(HashMap<String, String> params) throws Exception {

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_TRAVEL_LIKE, params);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "likeTravel:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "likeTravel: Return message code not positive");
            throw new Exception(rmsg.getRemark());
        }
        //解析
        String json = rmsg.getData().toString();
        JSONObject jsonObject = new JSONObject(json);
        return (TravelEntity) JsonHelper.getInstance().jsonToBean(jsonObject.toString(), TravelEntity.class);
    }

    /**
     * unlike
     *
     * @param params 必要的参数
     * @return 返回结果
     * @throws Exception 异常
     */
    public TravelEntity unLikeTravel(HashMap<String, String> params) throws Exception {

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_TRAVEL_UNLIKE, params);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "unLikeTravel:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "unLikeTravel: Return message code not positive");
            throw new Exception(rmsg.getRemark());
        }
        //解析
        String json = rmsg.getData().toString();
        JSONObject jsonObject = new JSONObject(json);
        return (TravelEntity) JsonHelper.getInstance().jsonToBean(jsonObject.toString(), TravelEntity.class);
    }

    /**
     * @param params 必要的参数
     * @return 评论的实例
     * @throws Exception
     */
    public TravelComment createTravelComment(HashMap<String, String> params) throws Exception {

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_TRAVEL_COMMENT_CREATE, params);
        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "createTravelComment:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "createTravelComment: Return message code not positive");
            throw new Exception("Failed to create TravelComment!");
        }
        return JsonHelper.getInstance().jsonToBean(
                rmsg.getData().toString(), TravelComment.class);
    }

    /**
     * 删除
     *
     * @param commentId 评论id
     * @return 评论的实例
     * @throws Exception
     */
    public TravelComment deleteTravelComment(String commentId) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(StringConstant.commentId, commentId);
        params.put(StringConstant.verStr, APPVERSION);

        String jsonStr = MsgSender.postJsonToNet(URLConstant.URL_TRAVEL_COMMENT_DELETE, params);

        ReturnMsg rmsg = checkReturnMsg(jsonStr);
        if (rmsg == null) {
            Log.e(LOG_TAG, "deleteTravelComment:Parse failure");
            throw new Exception("Parse failure");
        }
        if (rmsg.getCode() != StatusCodeConstant.STATUS_POSITIVE) {
            Log.e(LOG_TAG, "deleteTravelComment: Return message code not positive");
            throw new Exception("Failed to delete TravelComment!");
        }
        return JsonHelper.getInstance().jsonToBean(rmsg.getData().toString(), TravelComment.class);
    }
    /**********************************************************************************************/
}
