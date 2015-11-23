package cn.lingox.android.constants;

public class URLConstant {

//    http://www.lingox.cn/json/activity_tags.json 活动标签

    public static final String MAIN_URL = "http://182.92.239.194:1337/";//正式版接口
//    public static final String MAIN_URL = "http://192.168.1.222:1338/";//测试接口

    // TODO implement these two functionalities into the server (maybe? for the apk it might cause trouble with the qr code)
    public static final String APK_URL = "http://182.92.239.194/app/LingoX.apk";
    public static final String VER_URL = "http://182.92.239.194/app/minVer.txt";
    public static final String URL_DEFAULT_USER = MAIN_URL + "recommend/getRecommendUsers";
    //个人信息——旅行记录
    private static final String URL_EXPERIENCES = MAIN_URL + "experience/";
    public static final String URL_EXPERIENCES_CREATE = URL_EXPERIENCES + "create";//创建
    public static final String URL_EXPERIENCES_DELETE = URL_EXPERIENCES + "delete";//删除
    public static final String URL_EXPERIENCES_GET = URL_EXPERIENCES + "getUsersExperiences";//更新
    // URL for User model
    private static final String URL_USER_MODEL = MAIN_URL + "User/";
    public static final String URL_LOGINTIME = URL_USER_MODEL + "updateloginTime";
    public static final String URL_REGISTER = URL_USER_MODEL + "register";
    public static final String URL_LOGIN = URL_USER_MODEL + "login";
    public static final String URL_UPLOAD_AVATAR = URL_USER_MODEL + "uploadAvatar";
    public static final String URL_UPDATE_INFO = URL_USER_MODEL + "updateInfo";
    public static final String URL_FORGOT_PASSWORD = URL_USER_MODEL + "forgotPassword";
    //获取订单
    private static final String URL_APPLICATION_MODEL = MAIN_URL + "application/";
    public static final String URL_GET_APPLICATION = URL_APPLICATION_MODEL + "getApplication";
    public static final String URL_CREATE_APPLICATION = URL_APPLICATION_MODEL + "createApplication";
    public static final String URL_EDIT_APPLICATION = URL_APPLICATION_MODEL + "editApplication";
    public static final String URL_EXIST_APPLICATION = URL_APPLICATION_MODEL + "applicationExists";

    // URL for Contact model
    private static final String URL_CONTACT_MODEL = MAIN_URL + "Contact/";
    public static final String URL_CHANGE_RELATION = URL_CONTACT_MODEL + "changeRelation";
    public static final String URL_GET_CONTACT_LIST = URL_CONTACT_MODEL + "getContactList";
    public static final String URL_GET_USER_INFO = URL_CONTACT_MODEL + "getUserInfo";
    public static final String URL_GET_USER_FOLLOWING = URL_CONTACT_MODEL + "getUserFollowing";
    //获取双方的follow关系 true 相互 false 其他
    public static final String URL_GET_USER_RELATION = URL_CONTACT_MODEL + "isBothFollowed";
    // URL for Search
    private static final String URL_SEARCH = MAIN_URL + "Search/";
    public static final String URL_SEARCH_USER = URL_SEARCH + "searchUser";
    // URL for References
    private static final String URL_REFERENCE = MAIN_URL + "Reference/";
    public static final String URL_CREATE_REFERENCE = URL_REFERENCE + "createReference";
    public static final String URL_EDIT_REFERENCE = URL_REFERENCE + "editReference";
    public static final String URL_DELETE_REFERENCE = URL_REFERENCE + "deleteReference";
    public static final String URL_GET_USERS_REFERENCES = URL_REFERENCE + "getUsersReferences";

    // URL for Paths
    private static final String URL_PATH = MAIN_URL + "Path/";
    public static final String URL_GET_PATH = URL_PATH + "getPath";
    public static final String URL_GET_ALL_PATHS = URL_PATH + "getAllPaths";
    public static final String URL_GET_USERS_PATHS = URL_PATH + "getUsersPaths";
    public static final String URL_CREATE_PATH = URL_PATH + "createPath";
    public static final String URL_EDIT_PATH = URL_PATH + "editPath";
    public static final String URL_DELETE_PATH = URL_PATH + "deletePath";
    public static final String URL_ACCEPT_PATH = URL_PATH + "acceptPath";
    public static final String URL_UNACCEPT_PATH = URL_PATH + "unAcceptPath";
    public static final String URL_GET_PATHS_BY_LOCATION = URL_PATH + "getPathsByLocation";
    public static final String URL_UPLOAD_PATH_IMAGE = URL_PATH + "uploadPathImage";

    // URL for Comments
    private static final String URL_COMMENT = MAIN_URL + "Comment/";
    public static final String URL_CREATE_COMMENT = URL_COMMENT + "createComment";
    public static final String URL_DELETE_COMMENT = URL_COMMENT + "deleteComment";

    // URL for Photos
    private static final String URL_PHOTO = MAIN_URL + "Photo/";
    public static final String URL_GET_USERS_PHOTOS = URL_PHOTO + "getUsersPhotos";
    public static final String URL_UPLOAD_PHOTO = URL_PHOTO + "uploadPhoto";
    public static final String URL_EDIT_PHOTO = URL_PHOTO + "editPhoto";
    public static final String URL_DELETE_PHOTO = URL_PHOTO + "deletePhoto";

    // URL for Notifications
    private static final String URL_NOTIFICATION = MAIN_URL + "Notification/";
    public static final String URL_DELETE_NOTIFICATION = URL_NOTIFICATION + "deleteNotification";
    public static final String URL_GET_ALL_NOTIFICATIONS = URL_NOTIFICATION + "getAllNotifications";
    public static final String URL_GET_ALL_NEW_NOTIFICATIONS = URL_NOTIFICATION + "getAllNewNotifications";
    public static final String URL_READ_NOTIFICATIONS = URL_NOTIFICATION + "readNotification";

    //对活动进行评论
    private static final String URL_PATHREFERENCE = MAIN_URL + "pathReference/";
    public static final String URL_PATHREFERENCE_CREATE = URL_PATHREFERENCE + "create";
    public static final String URL_PATHREFERENCE_DELETE = URL_PATHREFERENCE + "delete";
    public static final String URL_PATHREFERENCE_GETREFERENCE = URL_PATHREFERENCE + "getPathsReferences";
    public static final String URL_PATHREFERENCE_REPLY = URL_PATHREFERENCE + "reply";
    public static final String URL_PATHREFERENCE_DELETEREPLY = URL_PATHREFERENCE + "deleteReply";
    /*********************************
     * 旅行者发布相关接口
     ***************************************************/
    //活动
    private static final String URL_TRAVEL_BASE = MAIN_URL + "demand/";
    public static final String URL_TRAVEL_CREATE = URL_TRAVEL_BASE + "createDemand";//创建
    public static final String URL_TRAVEL_EDIT = URL_TRAVEL_BASE + "editDemand";//修改
    public static final String URL_TRAVEL_DELETE = URL_TRAVEL_BASE + "deleteDemand";//删除
    public static final String URL_TRAVEL_GET = URL_TRAVEL_BASE + "getDemand";//获取单个
    public static final String URL_TRAVEL_GETALL = URL_TRAVEL_BASE + "getAllDemands";//获取所有
    public static final String URL_TRAVEL_LIKE = URL_TRAVEL_BASE + "likeDemand";//添加like
    public static final String URL_TRAVEL_UNLIKE = URL_TRAVEL_BASE + "unLikeDemand";//移除like

    //评论
    private static final String URL_TRAVEL_COMMENT_BASE = MAIN_URL + "demandComment/";
    public static final String URL_TRAVEL_COMMENT_CREATE = URL_TRAVEL_COMMENT_BASE + "createComment";//创建
    public static final String URL_TRAVEL_COMMENT_EDIT = URL_TRAVEL_COMMENT_BASE + "editComment";//修改
    public static final String URL_TRAVEL_COMMENT_DELETE = URL_TRAVEL_COMMENT_BASE + "deleteComment";//删除
/******************************************************************************************************************/
}