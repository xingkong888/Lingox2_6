package cn.lingox.android.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroupManager;
import com.easemob.exceptions.EaseMobException;
import com.squareup.picasso.Picasso;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;

import cn.lingox.android.R;
import cn.lingox.android.adapter.LocalJoinedUsersAdapter;
import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.constants.StringConstant;
import cn.lingox.android.entity.Comment;
import cn.lingox.android.entity.Path;
import cn.lingox.android.entity.PathTags;
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.CreateIndentDialog;
import cn.lingox.android.helper.JsonHelper;
import cn.lingox.android.helper.ServerHelper;
import cn.lingox.android.helper.UIHelper;
import cn.lingox.android.task.GetUser;
import cn.lingox.android.utils.CircularImageView;
import cn.lingox.android.utils.CreateTagView;
import cn.lingox.android.utils.SkipDialog;
import cn.lingox.android.widget.CheckOverSizeTextView;
import cn.lingox.android.widget.MyScrollView;
import cn.lingox.android.widget.ScrollViewListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import it.sephiroth.android.library.widget.HListView;

/**
 * 本地人发布的活动详情页
 */
public class LocalViewActivity extends ActionBarActivity implements View.OnClickListener, ScrollViewListener {
    // Incoming Intent Extras
    public static final String PATH_TO_VIEW = LingoXApplication.PACKAGE_NAME + ".PATH_TO_VIEW";
    public static final String PATH_TO_VIEW_ID = LingoXApplication.PACKAGE_NAME + ".PATH_TO_VIEW_ID";
    // Returning Intent Extras
    public static final String EDITED_PATH = LingoXApplication.PACKAGE_NAME + ".EDITED_PATH";
    public static final String DELETED_PATH = LingoXApplication.PACKAGE_NAME + ".DELETED_PATH";
    // Outgoing Request Codes
    public static final int EDIT_PATH = 101;
    private static final String LOG_TAG = "LocalViewActivity";
    // UI Elements
    private ProgressBar loadingBar;

    private ImageView menu;
    private LinearLayout favourite, delete, edit, share, groupChat;
    private ImageView like;//是否已收藏
    private PopupWindow mPopupWindow;//弹出框

    private ImageView chat;//聊天图标
    private ImageView avatar;//用户头像
    private ImageView background;//活动背景图
    private TextView userName;//用户名
    private TextView location;//用户地址
    private TextView title;//活动标题
    //    private TextView pathDateTimeInfo, pathEndTimeInfo;//开始、结束时间
    private CheckOverSizeTextView details;//活动内容描述
    private TextView more;
    private TextView cost;//活动花费
    //    private TextView pathGroudSizeInfo;//群组人数----弃用
    private TextView experienceLocation;//活动地址
    private InputMethodManager manager;//软键盘管理器

    private TextView pathCommentsNum;//评论者人数
    private TextView pathJoinedUserNum;//收藏人数
    //
    private HListView joinedUsersListView;//收藏用户头像
    private LinearLayout commentsSend, commentsListView;
    private RelativeLayout layout;//申请成功后的群聊引导
    private EditText commentEditText;//评论编辑框
    private Button commentSendButton;//评论提交按钮

    private int height;
    private int scrollViewHeight;
    private int commentHeight;
    private LinearLayout pathView;//整个视图
    //    private LinearLayout pathTime;
    private LinearLayout likeLayout;
    private LinearLayout commitLayout;
    //    private LinearLayout layoutThree;
    private TextView availableTime;
    //标签
    private ViewGroup tagLayout = null;
    //加入

    private LinearLayout join;
    // Data Elements
    private Path path = null;
    private User user;
    private ArrayList<User> joinedUsersList = new ArrayList<>();
    private LocalJoinedUsersAdapter joinedUsersAdapter;
    private ArrayList<Comment> commentsList = new ArrayList<>();
    private boolean ownPath;
    private boolean replyEveryOne = true;
    private User replyUser;

    private boolean isApply = false;
    //数组长度必须为2 第一个为x坐标，第二个为y坐标
    private int[] startLocations = new int[2];
    private int[] endLocations = new int[2];

    private ArrayList<PathTags> datas;
    //    private String[] tags;
    private ArrayList<String> tags;
    private UIHelper uiHelper = UIHelper.getInstance();
    private HashMap<String, String> map = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        if (intent.hasExtra(PATH_TO_VIEW)) {
            path = intent.getParcelableExtra(PATH_TO_VIEW);
        }
        if (!LingoXApplication.getInstance().getSkip()) {
            map.put("userId", CacheHelper.getInstance().getSelfInfo().getId());
        }
        initView();
        if (path == null && intent.getStringExtra(PATH_TO_VIEW_ID).isEmpty()) {
            Toast.makeText(this, "Activity does not exist", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (path != null) {
                map.put("pathId", path.getId());
            } else if (!intent.getStringExtra(PATH_TO_VIEW_ID).isEmpty()) {
                map.put("pathId", intent.getStringExtra(PATH_TO_VIEW_ID));
            }
            new GetPathInfo().execute(intent.getStringExtra(PATH_TO_VIEW_ID));
        }
    }

    private void initView() {
        setContentView(R.layout.activity_view_local);
        loadingBar = (ProgressBar) findViewById(R.id.loading_bar);
        //加入
        join = (LinearLayout) findViewById(R.id.join_experience);
        join.setOnClickListener(this);

        //标题栏
        ImageView back = (ImageView) findViewById(R.id.local_back);
        back.setOnClickListener(this);
        menu = (ImageView) findViewById(R.id.local_menu);
        menu.setOnClickListener(this);

        View popupView = getLayoutInflater().inflate(R.layout.menu_pop, null);
        favourite = (LinearLayout) popupView.findViewById(R.id.menu_favourite);
        favourite.setOnClickListener(this);
        like = (ImageView) popupView.findViewById(R.id.menu_iv_favourite);
        delete = (LinearLayout) popupView.findViewById(R.id.menu_del);
        delete.setOnClickListener(this);
        edit = (LinearLayout) popupView.findViewById(R.id.menu_edit);
        edit.setOnClickListener(this);
        share = (LinearLayout) popupView.findViewById(R.id.menu_share);
        share.setOnClickListener(this);
        groupChat = (LinearLayout) popupView.findViewById(R.id.menu_group_chat);
        groupChat.setOnClickListener(this);
        like = (ImageView) popupView.findViewById(R.id.menu_iv_favourite);
        mPopupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mPopupWindow.setTouchable(true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
        //背景图和标题
        background = (ImageView) findViewById(R.id.local_background);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        height = dm.heightPixels;
        ViewGroup.LayoutParams params1 = background.getLayoutParams();
        params1.height = width;
        background.setLayoutParams(params1);

        title = (TextView) findViewById(R.id.local_title);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, width / 2, 0, 0);
        title.setLayoutParams(params);
        //内容和更多
        details = (CheckOverSizeTextView) findViewById(R.id.local_details);
        details.setOnOverLineChangedListener(new CheckOverSizeTextView.OnOverSizeChangedListener() {
            @Override
            public void onChanged(boolean isOverSize) {
                if (isOverSize) {
                    more.setVisibility(View.VISIBLE);
                } else {
                    more.setVisibility(View.GONE);
                }
            }
        });
        more = (TextView) findViewById(R.id.local_more);
        more.setOnClickListener(this);
        //个人信息
        avatar = (CircularImageView) findViewById(R.id.local_avatar);
        avatar.setOnClickListener(this);
        userName = (TextView) findViewById(R.id.local_name);
        chat = (ImageView) findViewById(R.id.chat_local);
        chat.setOnClickListener(this);
        location = (TextView) findViewById(R.id.local_location);
        //活动地址---点击可跳转到地图
        experienceLocation = (TextView) findViewById(R.id.experience_location);
        experienceLocation.setOnClickListener(this);
        //空闲时间/活动时间
        availableTime = (TextView) findViewById(R.id.available_time);
        //标签
        tagLayout = (ViewGroup) findViewById(R.id.local_tag_layout);
        //花费
        cost = (TextView) findViewById(R.id.local_cost);
        //软键盘管理器
        manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        MyScrollView scrollView = (MyScrollView) findViewById(R.id.path_view_scroll_view);
        scrollView.setScrollViewListener(this);
        pathCommentsNum = (TextView) findViewById(R.id.path_comments_num);
        pathJoinedUserNum = (TextView) findViewById(R.id.path_particpants_num);
        //收藏者
        joinedUsersListView = (HListView) findViewById(R.id.path_view_joined_user_list);
        joinedUsersAdapter = new LocalJoinedUsersAdapter(this, joinedUsersList);
        joinedUsersListView.setAdapter(joinedUsersAdapter);
        if (LingoXApplication.getInstance().getSkip()) {
            joinedUsersListView.setClickable(false);
        }
        //comment
        commentsListView = (LinearLayout) findViewById(R.id.path_view_comments_list);
        commentsSend = (LinearLayout) findViewById(R.id.path_view_comment_bar);
        commentEditText = (EditText) findViewById(R.id.comment_text_box);
        commentSendButton = (Button) findViewById(R.id.btn_reply);
        commentSendButton.setOnClickListener(this);
        commitLayout = (LinearLayout) findViewById(R.id.path_view_commit);
        //like
        likeLayout = (LinearLayout) findViewById(R.id.path_view_like);

        pathView = (LinearLayout) findViewById(R.id.path_view);
        //申请之后的引导
        layout = (RelativeLayout) findViewById(R.id.path_view_yindao);
        layout.setOnClickListener(this);

        datas = LingoXApplication.getInstance().getDatas();
    }

    private void setData() {
        if (!LingoXApplication.getInstance().getSkip()) {
            ownPath = (CacheHelper.getInstance().getSelfInfo().getId().equals(user.getId()));
            if (ownPath) {
                chat.setVisibility(View.GONE);
                join.setVisibility(View.GONE);
                delete.setVisibility(View.VISIBLE);
                edit.setVisibility(View.VISIBLE);
                groupChat.setVisibility(!TextUtils.isEmpty(path.getHxGroupId()) ? View.VISIBLE : View.GONE);
            } else {
                delete.setVisibility(View.GONE);
                edit.setVisibility(View.GONE);
                chat.setVisibility(View.VISIBLE);
                join.setVisibility(View.VISIBLE);
            }
        }
        title.setText(path.getTitle());
        joinedUsersList.clear();
        joinedUsersList.addAll(path.getAcceptedUsers());
        if (joinedUsersList.size() > 0) {
            likeLayout.setVisibility(View.VISIBLE);
            pathJoinedUserNum.setText(String.valueOf(joinedUsersList.size()));
            joinedUsersAdapter.notifyDataSetChanged();
        } else {
            likeLayout.setVisibility(View.GONE);
        }

        if (path.getComments().size() > 0) {
            commitLayout.setVisibility(View.VISIBLE);
            commentsList.clear();
            commentsList.addAll(path.getComments());
            pathCommentsNum.setText(String.valueOf(commentsList.size()));
            loadComments();
        } else {
            commitLayout.setVisibility(View.GONE);
        }
        uiHelper.imageViewSetPossiblyEmptyUrl(this, avatar, user.getAvatar(), "circular");
        Picasso.with(this).load(path.getImage11()).into(background);
        uiHelper.textViewSetPossiblyNullString(userName, user.getNickname());
        uiHelper.textViewSetPossiblyNullString(location, user.getLocation());

        if (path.getDateTime() > 0 || path.getEndDateTime() > 0) {
            availableTime.setVisibility(View.GONE);
        } else {
            availableTime.setVisibility(View.VISIBLE);
            availableTime.setText(path.getAvailableTime());
        }
        uiHelper.textViewSetPossiblyNullString(details, path.getText());

        uiHelper.textViewSetPossiblyNullString(experienceLocation, path.getLocationString());
        //判断花费类型
        String costStr = "";
        switch (path.getCost()) {
            case "gift"://礼物
                costStr = getString(R.string.gift);
                break;
            case "AA"://AA
                costStr = path.getCost();
                break;
            case "hosted"://将来做本地人
                costStr = getString(R.string.being_hosted);
                break;
            case "share"://分享经历
                costStr = getString(R.string.share_experience);
                break;
        }
        uiHelper.textViewSetPossiblyNullString(cost, costStr);
        if (TextUtils.isEmpty(path.getHxGroupId())) {
            groupChat.setVisibility(View.GONE);
        }
        if (path.getTags().size() > 0) {
            tags = new ArrayList<>();
            for (int a = 0, b = path.getTags().size(); a < b; a++) {
                tags.add(datas.get(Integer.valueOf(path.getTags().get(a))).getTag());
            }
        }


        //添加标签
        CreateTagView.addTagView(tags, tagLayout, this);
    }

    //设置键盘的显示与隐藏
    public void setModeKeyboard(View view) {
        if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null)
                manager.hideSoftInputFromWindow(getCurrentFocus()
                        .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } else {
            manager.showSoftInput(view, 0);
        }
    }

    /**
     * 活动修改后，重新设置界面上的数据
     */
    private void pathEdited() {
        title.setText(path.getTitle());
        Picasso.with(this).load(path.getImage()).into(background);
        uiHelper.textViewSetPossiblyNullString(details, path.getText());
//        if (path.getDateTime() != 0 || path.getEndDateTime() != 0) {
//            pathTime.setVisibility(View.VISIBLE);
//            if (path.getDateTime() != 0 && path.getDateTime() != -1) {
//                uiHelper.textViewSetPossiblyNullString(pathDateTimeInfo, JsonHelper.getInstance().parseTimestamp(path.getDateTime(), 1));
//            }
//            if (path.getEndDateTime() != 0) {
//                uiHelper.textViewSetPossiblyNullString(pathEndTimeInfo, JsonHelper.getInstance().parseTimestamp(path.getEndDateTime(), 1));
//            }
//        } else
        if (!path.getAvailableTime().isEmpty()) {
            availableTime.setVisibility(View.VISIBLE);
//            pathTime.setVisibility(View.GONE);
            availableTime.setText(path.getAvailableTime());
        }
        uiHelper.textViewSetPossiblyNullString(cost, path.getCost());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finishedViewing();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.menu_del://删除
                mPopupWindow.dismiss();
                new AlertDialog.Builder(this)
                        .setTitle("Are you sure to delete?")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new Thread() {
                                    @Override
                                    public void run() {
                                        try {
                                            path = ServerHelper.getInstance().deletePath(path.getId());
                                            Intent intent = new Intent();
                                            intent.putExtra(LocalViewActivity.DELETED_PATH, path);
                                            setResult(RESULT_OK, intent);
                                            finish();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }.start();
                            }
                        }).create().show();
                break;
            case R.id.menu_share://分享
                mPopupWindow.dismiss();
                MobclickAgent.onEvent(LocalViewActivity.this, "discover_share");
                showShare();
                break;
            case R.id.menu_favourite://收藏
                mPopupWindow.dismiss();
                if (!LingoXApplication.getInstance().getSkip()) {
                    if (!ownPath) {
                        if (path.hasUserAccepted(CacheHelper.getInstance().getSelfInfo().getId())) {
                            new UnAcceptPath().execute();
                        } else {
                            new AcceptPath().execute();
                        }
                    } else {
                        Intent mIntent = new Intent(this, UserListActivity.class);
                        mIntent.putParcelableArrayListExtra(UserListActivity.USER_LIST, joinedUsersList);
                        mIntent.putExtra(UserListActivity.PAGE_TITLE, getString(R.string.joined_users));
                        startActivity(mIntent);
                    }
                } else {
                    SkipDialog.getDialog(this).show();
                }
                break;
            case R.id.menu_group_chat://群聊
                mPopupWindow.dismiss();
                new JoinGroupChat().execute();
                break;
            case R.id.local_avatar://用户头像
                if (!LingoXApplication.getInstance().getSkip()) {
                    MobclickAgent.onEvent(this, "discover_avatar");
                    Intent userInfoIntent = new Intent(this, UserInfoActivity.class);
                    userInfoIntent.putExtra(UserInfoActivity.INTENT_USER_ID, user.getId());
                    startActivity(userInfoIntent);
                } else {
                    SkipDialog.getDialog(this).show();
                }
                break;
            case R.id.chat_local://聊天
                Intent chatIntent = new Intent(LocalViewActivity.this, ChatActivity.class);
                chatIntent.putExtra("username", user.getUsername());
                chatIntent.putExtra(StringConstant.nicknameStr, user.getNickname());
                chatIntent.putExtra("chatType", ChatActivity.CHATTYPE_SINGLE);
                startActivity(chatIntent);
                break;
            case R.id.menu_edit://编辑
                mPopupWindow.dismiss();
                if (!LingoXApplication.getInstance().getSkip()) {
                    MobclickAgent.onEvent(LocalViewActivity.this, "discover_message", new HashMap<String, String>().put("message", "edit"));
                    Intent editPathIntent = new Intent(this, LocalEditActivity.class);
                    editPathIntent.putExtra(LocalEditActivity.PATH_TO_EDIT, path);
                    startActivityForResult(editPathIntent, EDIT_PATH);
                } else {
                    SkipDialog.getDialog(this).show();
                }
                break;
            case R.id.btn_reply://回复评论
                if (commentEditText.getText().toString().isEmpty()) {
                    Toast.makeText(this, getString(R.string.enter_comment), Toast.LENGTH_SHORT).show();
                } else if (replyEveryOne) {
                    new PostComment().execute();
                } else {
                    new ReplyComment(replyUser.getId(), commentEditText.getText().toString()).execute();
                    replyEveryOne = true;
                    commentEditText.setHint("");
                }
                break;
            case R.id.path_view_yindao://引导页
                layout.setVisibility(View.GONE);
                break;
            case R.id.local_back://返回
                finishedViewing();
                break;
            case R.id.local_menu://菜单
                mPopupWindow.showAsDropDown(menu, -100, 0);
                break;
            case R.id.local_more://展示更多的介绍
                details.displayAll();
                break;
            case R.id.join_experience://加入
                if (!LingoXApplication.getInstance().getSkip()) {
                    MobclickAgent.onEvent(LocalViewActivity.this, "discover_message", new HashMap<String, String>().put("message", "chat"));
                    new GetExist(map).execute();
                } else {
                    SkipDialog.getDialog(this).show();
                }
                break;
            case R.id.experience_location://活动地址，跳转到地图
                if (path.getLatitude().isEmpty() || path.getLongitude().isEmpty() || path.getDetailAddress().isEmpty()) {
                    Toast.makeText(this, "Address or latitude and longitude error.", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(this, AMapActivity.class);
                    intent.putExtra("Latitude", path.getLatitude());//纬度
                    intent.putExtra("Longitude", path.getLongitude());//经度
                    intent.putExtra("address", path.getDetailAddress());//地址
                    startActivity(intent);
                }
                break;
        }
    }

    // TODO Comments in English as well
    private void showShare() {
        ShareSDK.initSDK(this);
        OnekeyShare oks = new OnekeyShare();
        // 分享时Notification的图标和文字
        oks.setNotification(R.drawable.app_icon, getString(R.string.app_name));
        // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
        oks.setTitle(path.getTitle());
        // text是分享文本，所有平台都需要这个字段
        oks.setText(path.getText());
        // url仅在微信（包括好友和朋友圈）中使用
        oks.setImageUrl(path.getImage());
        oks.setImagePath(path.getImage());
        oks.setUrl("http://lingox.cn/viewActivity?" + StringConstant.pathId + "=" + path.getId());
        // 启动分享GUI
        oks.show(this);
    }

    /**
     * 返回上一级
     */
    private void finishedViewing() {
        if (layout.isShown()) {
            //若群聊引导页显示，则隐藏
            layout.setVisibility(View.GONE);
        } else if (!replyEveryOne) {
            //若正在回复某人，则清空回复框
            replyEveryOne = true;
            commentEditText.setHint("");
        } else {
            //返回上一页
            Intent editedIntent = new Intent();
            editedIntent.putExtra(EDITED_PATH, path);
            setResult(RESULT_OK, editedIntent);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        //置空，加速GC回收图片资源内存
        background.setImageDrawable(null);
        super.onDestroy();
    }

    /*
    *系统回退键
     */
    @Override
    public void onBackPressed() {
        finishedViewing();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case EDIT_PATH:
                switch (resultCode) {
                    case RESULT_OK:
                        if (data.hasExtra(LocalEditActivity.DELETED_PATH)) {
                            Intent deletedIntent = new Intent();
                            deletedIntent.putExtra(DELETED_PATH, data.getParcelableExtra(LocalEditActivity.DELETED_PATH));
                            setResult(RESULT_OK, deletedIntent);
                            finish();
                            break;
                        } else {
                            path = data.getParcelableExtra(LocalEditActivity.EDITED_PATH);
                            pathEdited();
                            break;
                        }
                }
                break;
        }
    }

    /**
     * 移除评论
     *
     * @param position 移除评论的位置
     */
    private void removeComment(int position) {
        path.removeComment(commentsList.get(position));
        commentsList.remove(position);
        if (commentsList.size() <= 0) {
            commitLayout.setVisibility(View.GONE);
        }
        pathCommentsNum.setText(String.valueOf(commentsList.size()));
        commentsListView.removeViewAt(position);
    }

    /**
     * 添加评论
     *
     * @param comment 添加评论的实例
     */
    private void addComment(Comment comment) {
        path.addComment(comment);
        commentsList.add(comment);
        pathCommentsNum.setText(String.valueOf(commentsList.size()));
        commentsListView.addView(getCommentView(commentsList.size() - 1));
        commentEditText.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(commentEditText.getWindowToken(), 0);
    }

    /**
     * 加载所以的评论----为评论创建展示控件
     */
    private void loadComments() {
        commentsListView.removeAllViews();
        for (int i = 0, j = commentsList.size(); i < j; i++) {
            commentsListView.addView(getCommentView(i));
        }
    }

    /**
     * 创建评论展示控件
     *
     * @param position 评论的位置
     * @return 控件
     */
    private View getCommentView(final int position) {
        View rowView = getLayoutInflater().inflate(R.layout.row_path_comment, null);
        final Comment comment = commentsList.get(position);

        ImageView userAvatar = (ImageView) rowView.findViewById(R.id.comment_user_avatar);
        if (!LingoXApplication.getInstance().getSkip()) {
            if (CacheHelper.getInstance().getSelfInfo().getId().contentEquals(comment.getUserId())) {
                ImageView delete = (ImageView) rowView.findViewById(R.id.path_del);
                delete.setVisibility(View.VISIBLE);
            } else {
                ImageView replay = (ImageView) rowView.findViewById(R.id.path_replay);
                replay.setVisibility(View.VISIBLE);
            }
        }
        TextView userNickname = (TextView) rowView.findViewById(R.id.comment_user_nickname);
        TextView commentText = (TextView) rowView.findViewById(R.id.comment_text);
        TextView commentDateTime = (TextView) rowView.findViewById(R.id.comment_date_time);
        TextView replyTarName = (TextView) rowView.findViewById(R.id.reply_tar_name);

        uiHelper.textViewSetPossiblyNullString(commentText, comment.getText());
        uiHelper.textViewSetPossiblyNullString(commentDateTime,
                JsonHelper.getInstance().parseSailsJSDate(comment.getCreatedAt()));
        new LoadCommentUser(userNickname, userAvatar, comment.getUserId()).execute();
        if (!comment.getUser_tar().isEmpty()) {
            new LoadReplyUser(comment.getUser_tar(), replyTarName).execute();
        }
        if (!LingoXApplication.getInstance().getSkip()) {
            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (comment.getUserId().equals(CacheHelper.getInstance().getSelfInfo().getId())) {
                        CommentDialog commentDialog = new CommentDialog(comment);
                        commentDialog.setCanceledOnTouchOutside(true);
                        commentDialog.show();
                    } else {
                        replyOthers(comment);
                    }
                }
            });
        }
        return rowView;
    }

    /**
     * 回复某人
     *
     * @param comment “”
     */
    private void replyOthers(final Comment comment) {
        replyUser = CacheHelper.getInstance().getUserInfo(comment.getUserId());
        commentEditText.setHint((getString(R.string.reply_comment)) + " " + replyUser.getNickname() + ":");
        replyEveryOne = false;
    }

    @Override
    protected void onResume() {
        MobclickAgent.onResume(this);
        MobclickAgent.onPageStart("LocalViewActivity");
        super.onResume();
    }

    @Override
    protected void onPause() {
        MobclickAgent.onPause(this);
        MobclickAgent.onPageEnd("LocalViewActivity");
        super.onPause();
    }

    @Override
    public void onScrollChanged(final MyScrollView scrollView1, int x, int y, int oldx, int oldy) {
        if (!LingoXApplication.getInstance().getSkip()) {
//            pathCommentsNum.getLocationInWindow(startLocations);
            chat.getLocationInWindow(startLocations);
            pathView.getLocationInWindow(endLocations);
            if (scrollViewHeight <= endLocations[1]) {
                scrollViewHeight = endLocations[1];
                commentHeight = startLocations[1];
            }
            if (Math.abs(commentHeight - height) <= y) {
                commentsSend.setVisibility(View.VISIBLE);
                join.setVisibility(View.GONE);
            } else {
                commentsSend.setVisibility(View.GONE);
                join.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * 下载活动数据
     */
    private class GetPathInfo extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(String... params) {
            if (path == null) {
                try {
                    path = ServerHelper.getInstance().getPath(params[0]);
                } catch (Exception e) {
                    path = null;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (path == null) {
                Toast.makeText(LocalViewActivity.this, getString(R.string.fail_down_path), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            if (LingoXApplication.getInstance().getSkip()) {
                new GetUser(path.getUserId(), new GetUser.Callback() {
                    @Override
                    public void onSuccess(User cbUser) {
                        user = cbUser;
                        CacheHelper.getInstance().addUserInfo(user);
                        setData();
                        loadingBar.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onFail() {
                    }
                }).execute();
            } else {
                user = CacheHelper.getInstance().getUserInfo(path.getUserId());
                new GetUserInfo().execute(user.getId());
            }
        }
    }

    private class GetUserInfo extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            user = CacheHelper.getInstance().getUserInfo(params[0]);
            if (user == null) {
                try {
                    user = ServerHelper.getInstance().getUserInfo(params[0]);
                } catch (Exception e) {
                    user = null;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (user == null) {
                Toast.makeText(LocalViewActivity.this, getString(R.string.fail_down_user), Toast.LENGTH_SHORT).show();
                finish();
            }
            loadingBar.setVisibility(View.INVISIBLE);
            setData();
        }
    }

    /**
     * 上传活动的评论
     */
    private class PostComment extends AsyncTask<Void, Void, Comment> {
        private String str;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            commentSendButton.setEnabled(false);
            str = commentEditText.getText().toString();
        }

        @Override
        protected Comment doInBackground(Void... params) {
            Comment comment;
            try {
                comment = ServerHelper.getInstance().createComment(CacheHelper.getInstance().getSelfInfo().getId(), null, path.getId(), str);
            } catch (Exception e) {
                comment = null;
            }
            return comment;
        }

        @Override
        protected void onPostExecute(Comment comment) {
            super.onPostExecute(comment);
            if (comment == null) {
                Toast.makeText(LocalViewActivity.this, getString(R.string.fail_comment), Toast.LENGTH_SHORT).show();
            } else {
                MobclickAgent.onEvent(LocalViewActivity.this, "discover_comment", new HashMap<String, String>().put("comment", "discover"));
                addComment(comment);
                commitLayout.setVisibility(View.VISIBLE);
                commentEditText.setText("");
            }
            commentSendButton.setEnabled(true);
        }
    }

    private class DeleteComment extends AsyncTask<Integer, Void, Boolean> {
        private Integer position;

        @Override
        protected Boolean doInBackground(Integer... params) {
            position = params[0];
            try {
                ServerHelper.getInstance().deleteComment(commentsList.get(position).getId());
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (success) {
                removeComment(position);
            } else {
                Toast.makeText(LocalViewActivity.this, getString(R.string.fail_comment_del), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class AcceptPath extends AsyncTask<Void, Void, Boolean> {
        private ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(LocalViewActivity.this);
            pd.setMessage("Loading...");
            pd.show();
            pd.setCancelable(false);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                ServerHelper.getInstance().acceptPath(path.getId(), CacheHelper.getInstance().getSelfInfo().getId());
                return true;
            } catch (final Exception e) {
                Log.e(LOG_TAG, e.getMessage());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            pd.dismiss();
            if (success) {
                MobclickAgent.onEvent(LocalViewActivity.this, "discover_like", new HashMap<String, String>().put("like", "dislike"));
                if (!TextUtils.isEmpty(path.getHxGroupId())) {
                    layout.setVisibility(View.VISIBLE);
                }
                path.addAcceptedUser(CacheHelper.getInstance().getSelfInfo());
                joinedUsersAdapter.addItem(CacheHelper.getInstance().getSelfInfo());
                pathJoinedUserNum.setText(String.valueOf((Integer.parseInt(pathJoinedUserNum.getText().toString()) + 1)));
                joinedUsersAdapter.notifyDataSetChanged();
                joinedUsersListView.setVisibility(View.VISIBLE);
                likeLayout.setVisibility(View.VISIBLE);
                like.setImageResource(R.drawable.active_like_24dp);
                like.setTag(1);
                if (!path.getHxGroupId().isEmpty()) {
                    groupChat.setVisibility(View.VISIBLE);
                }
            } else {
                Toast.makeText(LocalViewActivity.this, getString(R.string.fail_jion), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class UnAcceptPath extends AsyncTask<Void, Void, Boolean> {
        private ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(LocalViewActivity.this);
            pd.setMessage("Loading...");
            pd.show();
            pd.setCancelable(false);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                ServerHelper.getInstance().unAcceptPath(path.getId(), CacheHelper.getInstance().getSelfInfo().getId());
                return true;
            } catch (final Exception e) {
                Log.e(LOG_TAG, e.getMessage());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            pd.dismiss();
            if (success) {
                MobclickAgent.onEvent(LocalViewActivity.this, "discover_like", new HashMap<String, String>().put("like", "dislike"));
                path.removeAcceptedUser(CacheHelper.getInstance().getSelfInfo());
                joinedUsersAdapter.removeItem(CacheHelper.getInstance().getSelfInfo());
                joinedUsersAdapter.notifyDataSetChanged();
                if (joinedUsersList.size() == 0) {
                    joinedUsersListView.setVisibility(View.GONE);
                    likeLayout.setVisibility(View.GONE);
                }
                pathJoinedUserNum.setText(String.valueOf(joinedUsersList.size()));
                like.setImageResource(R.drawable.active_dislike_24dp);
                like.setTag(0);
                if (!path.getHxGroupId().isEmpty()) {
                    groupChat.setVisibility(View.GONE);
                }
            } else {
                Toast.makeText(LocalViewActivity.this, getString(R.string.fail_jion), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class LoadCommentUser extends AsyncTask<Void, Void, Boolean> {
        private TextView userNickname;
        private ImageView userAvatar;
        private String userId;
        private User commentUser;

        public LoadCommentUser(TextView userNickname, ImageView userAvatar, String userId) {
            this.userNickname = userNickname;
            this.userAvatar = userAvatar;
            this.userId = userId;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            userNickname.setText("Loading...");
            userAvatar.setImageResource(R.drawable.default_avatar);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                boolean ownComment;
                if (LingoXApplication.getInstance().getSkip()) {
                    ownComment = false;
                } else {
                    ownComment = (CacheHelper.getInstance().getSelfInfo().getId().equals(userId));
                }
                if (ownComment) {
                    commentUser = CacheHelper.getInstance().getSelfInfo();
                } else {
                    commentUser = CacheHelper.getInstance().getUserInfo(userId);
                }
                if (commentUser == null) {
                    commentUser = ServerHelper.getInstance().getUserInfo(userId);
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "failed to get Comment's User's info from server");
                Log.e(LOG_TAG, e.getMessage());
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                uiHelper.textViewSetPossiblyNullString(userNickname, commentUser.getNickname());
                uiHelper.imageViewSetPossiblyEmptyUrl(LocalViewActivity.this, userAvatar, commentUser.getAvatar(), "");
                final View.OnClickListener userClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent mIntent = new Intent(LocalViewActivity.this, UserInfoActivity.class);
                        mIntent.putExtra(UserInfoActivity.INTENT_USER_ID, commentUser.getId());
                        startActivity(mIntent);
                    }
                };
                if (!LingoXApplication.getInstance().getSkip()) {
                    userAvatar.setOnClickListener(userClickListener);
                }
                userAvatar.setLongClickable(false);
            }
        }
    }

    private class CommentDialog extends Dialog implements View.OnClickListener {
        private Comment comment;

        public CommentDialog(Comment comment) {
            super(LocalViewActivity.this, R.style.MyDialogStyle);
            this.comment = comment;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.dialog_comment_options);
            TextView deleteButton = (TextView) findViewById(R.id.dialog_comment_delete);
            deleteButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.dialog_comment_delete:
                    new DeleteComment().execute(commentsList.indexOf(comment));
                    dismiss();
                    break;
            }
        }
    }

    private class ReplyComment extends AsyncTask<Void, Void, Comment> {
        private String userSrcId;
        private String userTarId;
        private String commentText;

        public ReplyComment(String userId, String commentText) {
            this.userTarId = userId;
            this.commentText = commentText;
            this.userSrcId = CacheHelper.getInstance().getSelfInfo().getId();
        }

        @Override
        protected void onPreExecute() {
//            commentSendButton.setEnabled(false);
        }

        @Override
        protected Comment doInBackground(Void... params) {
            Comment comment;
            try {
                comment = ServerHelper.getInstance().createComment(userSrcId, userTarId, path.getId(), commentText);
            } catch (Exception e) {
                comment = null;
            }
            return comment;
        }

        @Override
        protected void onPostExecute(Comment comment) {
            super.onPostExecute(comment);
            if (comment == null) {
                Toast.makeText(LocalViewActivity.this, getString(R.string.fail_comment), Toast.LENGTH_SHORT).show();
            } else {
                MobclickAgent.onEvent(LocalViewActivity.this, "discover_comment", new HashMap<String, String>().put("comment", "user"));
                addComment(comment);
//                commentEditText.setText("");
            }
//            commentSendButton.setEnabled(true);
        }
    }

    private class LoadReplyUser extends AsyncTask<String, Void, User> {
        private String userTar;
        private TextView tarName;

        public LoadReplyUser(String user_tar, TextView tarName) {
            this.userTar = user_tar;
            this.tarName = tarName;
        }

        @Override
        protected User doInBackground(String... params) {
            User targetUser;
            boolean isTargetUs = !LingoXApplication.getInstance().getSkip()
                    && CacheHelper.getInstance().getSelfInfo().getId().equals(userTar);
            if (isTargetUs) {
                targetUser = CacheHelper.getInstance().getSelfInfo();
            } else {
                targetUser = CacheHelper.getInstance().getUserInfo(userTar);
            }
            if (targetUser == null && userTar != null) {
                try {
                    targetUser = ServerHelper.getInstance().getUserInfo(userTar);
                } catch (Exception e) {
                    return null;
                }
            }
            return targetUser;
        }

        @Override
        protected void onPostExecute(User user) {
            if (user != null) {
                tarName.setText(String.format(getString(R.string.replied_to), user.getNickname()));
                tarName.setVisibility(View.VISIBLE);
            }
            super.onPostExecute(user);
        }
    }

    private class JoinGroupChat extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            pathGroupChat.setClickable(false);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                if (!TextUtils.isEmpty(path.getHxGroupId()) &&
                        !EMGroupManager.getInstance()
                                .getGroupFromServer(path.getHxGroupId())
                                .getMembers()
                                .contains(EMChatManager.getInstance().getCurrentUser())
                        ) {
                    EMGroupManager.getInstance().joinGroup(path.getHxGroupId());
                }
                return true;
            } catch (EaseMobException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aVoid) {
            super.onPostExecute(aVoid);
            if (aVoid) {
                MobclickAgent.onEvent(LocalViewActivity.this, "discover_group_chat");
                Intent intent = new Intent(LocalViewActivity.this, ChatActivity.class);
                intent.putExtra("chatType", ChatActivity.CHATTYPE_GROUP);
                intent.putExtra("groupId", path.getHxGroupId());
                startActivity(intent);
//                pathGroupChat.setClickable(true);
            } else {
                Toast.makeText(LocalViewActivity.this, "Sorry,there is no group.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class GetExist extends AsyncTask<Void, Void, Boolean> {
        private HashMap<String, String> maps;

        public GetExist(HashMap<String, String> map) {
            this.maps = map;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                isApply = ServerHelper.getInstance().existApplication(maps);
                return true;
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage());
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                if (path.getType() == 2 && (path.getEndDateTime() - System.currentTimeMillis() / 1000L) <= 0) {
                    isApply = true;
                }
                if (isApply) {
                    Intent chatIntent = new Intent(LocalViewActivity.this, ChatActivity.class);
                    chatIntent.putExtra("username", user.getUsername());
                    chatIntent.putExtra(StringConstant.nicknameStr, user.getNickname());
                    chatIntent.putExtra("chatType", ChatActivity.CHATTYPE_SINGLE);
                    startActivity(chatIntent);
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putString("pathId", path.getId());
                    bundle.putString("tarId", path.getUserId());
                    bundle.putString("username", user.getUsername());
                    bundle.putInt("type", path.getType());
                    bundle.putString(StringConstant.nicknameStr, user.getNickname());
                    CreateIndentDialog dialog = new CreateIndentDialog();
                    dialog.setArguments(bundle);
                    dialog.show(getFragmentManager(), "");
                }
            }
        }
    }
}
