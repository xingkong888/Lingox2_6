package cn.lingox.android.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroupManager;
import com.easemob.exceptions.EaseMobException;
import com.squareup.picasso.Picasso;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import cn.lingox.android.R;
import cn.lingox.android.adapter.PathJoinedUsersAdapter;
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
import cn.lingox.android.utils.DpToPx;
import cn.lingox.android.utils.SkipDialog;
import cn.lingox.android.widget.MyScrollView;
import cn.lingox.android.widget.ScrollViewListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import it.sephiroth.android.library.widget.HListView;

public class PathViewActivity extends ActionBarActivity implements View.OnClickListener, ScrollViewListener {
    // Incoming Intent Extras
    public static final String PATH_TO_VIEW = LingoXApplication.PACKAGE_NAME + ".PATH_TO_VIEW";
    public static final String PATH_TO_VIEW_ID = LingoXApplication.PACKAGE_NAME + ".PATH_TO_VIEW_ID";
    // Returning Intent Extras
    public static final String EDITED_PATH = LingoXApplication.PACKAGE_NAME + ".EDITED_PATH";
    public static final String DELETED_PATH = LingoXApplication.PACKAGE_NAME + ".DELETED_PATH";
    // Outgoing Request Codes
    public static final int EDIT_PATH = 101;
    private static final String LOG_TAG = "PathViewActivity";
    private static final String FILE_NAME = "/app_icon.jpg";
    private static String appIconImagePath;
    // UI Elements
    private MyScrollView scrollView;
    private ProgressBar loadingBar;
    private ImageView chat;
    private ImageView pathUserAvatar;
    private ImageView pathBackground;
    private TextView pathUserNickname;
    private TextView pathTitle;
    private TextView pathDateTimeInfo, pathEndTimeInfo;
    private TextView pathActivity;
    private TextView pathBudgetInfo;
    private TextView pathGroudSizeInfo;
    private TextView pathLocationInfo;
    private InputMethodManager manager;
    private TextView pathTraveler;
    private TextView pathLocal;
    private ImageView pathAcceptButton;
    private ImageView pathGroupChat;

    private TextView pathCommentsNum;
    private TextView pathJoinedUserNum;

    private HListView joinedUsersListView;
    private LinearLayout commentsListView, commentsSend;
    private RelativeLayout layout;
    private EditText commentEditText;
    private Button commentSendButton;

    // Data Elements
    private Path path = null;
    private User user;
    private ArrayList<User> joinedUsersList = new ArrayList<>();
    private PathJoinedUsersAdapter joinedUsersAdapter;
    private ArrayList<Comment> commentsList = new ArrayList<>();
    private boolean ownPath;
    private boolean replyEveryOne = true;
    private User replyUser;

    private ImageView delete, edit;

    private int width;
    private int height;
    private int scrollViewHight;
    private LinearLayout pathView, pathTime, likeLayout, commitLayout, layoutThree;
    private boolean hasMeasured = false;
    private int commentHeight;

    private boolean isApply = false;
    //数组长度必须为2 第一个为x坐标，第二个为y坐标
    private int[] startLocations = new int[2];
    private int[] endLocations = new int[2];

    private ArrayList<PathTags> datas;
    private String[] tags;
    private ViewGroup tagsView = null;
    private UIHelper uiHelper = UIHelper.getInstance();
    private HashMap<String, String> map = new HashMap<>();
    private TextView availableTime, available;

    //TODO Change all of the AsyncTasks (ie. getPathInfo getUserInfo getCommentUserInfo etc etc) into a better solution (maybe 1 big asynctask that loads them all in one go)

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
            new CreateAppIconFile().execute();
            new GetPathInfo().execute(intent.getStringExtra(PATH_TO_VIEW_ID));
        }
    }

    private void initView() {
        setContentView(R.layout.activity_view_path);

        pathTime = (LinearLayout) findViewById(R.id.dududu);

        manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        scrollView = (MyScrollView) findViewById(R.id.path_view_scroll_view);
        scrollView.setScrollViewListener(this);

        loadingBar = (ProgressBar) findViewById(R.id.loading_bar);
        chat = (ImageView) findViewById(R.id.iv_chat);
        chat.setOnClickListener(this);

        delete = (ImageView) findViewById(R.id.iv_delete);
        delete.setOnClickListener(this);
        edit = (ImageView) findViewById(R.id.iv_edit);
        edit.setOnClickListener(this);
        findViewById(R.id.back).setOnClickListener(this);

        pathBackground = (ImageView) findViewById(R.id.path_background);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;
        height = dm.heightPixels;
        ViewGroup.LayoutParams params1 = pathBackground.getLayoutParams();
        params1.height = width;
        pathBackground.setLayoutParams(params1);

        pathUserAvatar = (ImageView) findViewById(R.id.path_user_avatar);
        pathUserAvatar.setOnClickListener(this);

        pathUserNickname = (TextView) findViewById(R.id.path_user_name);
        availableTime = (TextView) findViewById(R.id.available_time);
        available = (TextView) findViewById(R.id.ddd);

        pathActivity = (TextView) findViewById(R.id.path_activity);
        pathTitle = (TextView) findViewById(R.id.path_title);

        pathDateTimeInfo = (TextView) findViewById(R.id.path_datetime_info);
        pathEndTimeInfo = (TextView) findViewById(R.id.path_endtime_info);
        pathLocationInfo = (TextView) findViewById(R.id.path_detail_address);
        pathBudgetInfo = (TextView) findViewById(R.id.path_cost_info);
        pathGroudSizeInfo = (TextView) findViewById(R.id.capacity_activity_info);

        pathCommentsNum = (TextView) findViewById(R.id.path_comments_num);
        pathJoinedUserNum = (TextView) findViewById(R.id.path_particpants_num);

        pathTraveler = (TextView) findViewById(R.id.path_traveler);
        pathLocal = (TextView) findViewById(R.id.path_local);

        findViewById(R.id.path_show_reference).setOnClickListener(this);

        pathAcceptButton = (ImageView) findViewById(R.id.path_accept_button);
        pathAcceptButton.setOnClickListener(this);
        pathAcceptButton.setTag(0);
        pathGroupChat = (ImageView) findViewById(R.id.group_chat);
        pathGroupChat.setOnClickListener(this);
        findViewById(R.id.path_share_button).setOnClickListener(this);

        joinedUsersListView = (HListView) findViewById(R.id.path_view_joined_user_list);
        joinedUsersAdapter = new PathJoinedUsersAdapter(this, joinedUsersList);
        joinedUsersListView.setAdapter(joinedUsersAdapter);
        if (LingoXApplication.getInstance().getSkip()) {
            joinedUsersListView.setClickable(false);
        }

        commentsListView = (LinearLayout) findViewById(R.id.path_view_comments_list);
        commentsSend = (LinearLayout) findViewById(R.id.path_view_comment_bar);
        //包含like、chat和share的layout
        layoutThree = (LinearLayout) findViewById(R.id.path_view_like_chat_share);

        likeLayout = (LinearLayout) findViewById(R.id.path_view_like);
        commitLayout = (LinearLayout) findViewById(R.id.path_view_commit);

        commentEditText = (EditText) findViewById(R.id.comment_text_box);
        commentSendButton = (Button) findViewById(R.id.btn_reply);
        commentSendButton.setOnClickListener(this);
        pathView = (LinearLayout) findViewById(R.id.path_view);
        tagsView = (ViewGroup) findViewById(R.id.path_tag_layout);

        layout = (RelativeLayout) findViewById(R.id.path_view_yindao);
        layout.setOnClickListener(this);

        datas = LingoXApplication.getInstance().getDatas();
    }

    private void setData() {
        if (!LingoXApplication.getInstance().getSkip()) {
            ownPath = (CacheHelper.getInstance().getSelfInfo().getId().equals(user.getId()));
            if (ownPath) {
                chat.setVisibility(View.GONE);
                delete.setVisibility(View.VISIBLE);
                edit.setVisibility(View.VISIBLE);
                pathAcceptButton.setImageResource(R.drawable.active_likepeople_24dp);
                pathAcceptButton.setTag(1);
                pathGroupChat.setVisibility(!TextUtils.isEmpty(path.getHxGroupId()) ? View.VISIBLE : View.GONE);
            } else {
                delete.setVisibility(View.GONE);
                edit.setVisibility(View.GONE);
                chat.setVisibility(View.VISIBLE);
                pathAcceptButton.setImageResource(path.hasUserAccepted(
                        CacheHelper.getInstance().getSelfInfo().getId()) ? R.drawable.active_like_24dp : R.drawable.active_dislike_24dp);
                pathAcceptButton.setTag(path.hasUserAccepted(
                        CacheHelper.getInstance().getSelfInfo().getId()) ? 0 : 1);
                if ((int) pathAcceptButton.getTag() == 0) {
                    pathGroupChat.setVisibility(View.VISIBLE);
                }
            }
        }
        pathTitle.setText(path.getTitle());
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
        uiHelper.imageViewSetPossiblyEmptyUrl(this, pathUserAvatar, user.getAvatar(), "circular");
        Picasso.with(this).load(path.getImage11()).into(pathBackground);
        uiHelper.textViewSetPossiblyNullString(pathUserNickname, user.getNickname());

        switch (path.getType()) {
            case 1://本地人
                pathLocal.setVisibility(View.VISIBLE);
                break;
            case 2://旅行者
                pathTraveler.setVisibility(View.VISIBLE);
                break;
        }
        if (path.getDateTime() > 0 || path.getEndDateTime() > 0) {
            pathTime.setVisibility(View.VISIBLE);
            availableTime.setVisibility(View.GONE);
            available.setVisibility(View.GONE);
            if (path.getDateTime() != 0 && path.getDateTime() != -1) {
                uiHelper.textViewSetPossiblyNullString(pathDateTimeInfo,
                        JsonHelper.getInstance().parseTimestamp(
                                path.getDateTime(), 1));
            }
            if (path.getEndDateTime() != 0) {
                uiHelper.textViewSetPossiblyNullString(pathEndTimeInfo,
                        JsonHelper.getInstance().parseTimestamp(path.getEndDateTime(), 1));
            }
        } else {
            availableTime.setVisibility(View.VISIBLE);
            available.setVisibility(View.VISIBLE);
            pathTime.setVisibility(View.GONE);
            availableTime.setText(path.getAvailableTime());
        }
        uiHelper.textViewSetPossiblyNullString(pathActivity, path.getText());

        uiHelper.textViewSetPossiblyNullString(pathLocationInfo, path.getLocationString());
        uiHelper.textViewSetPossiblyNullString(pathBudgetInfo, path.getCost());
        if (TextUtils.isEmpty(path.getHxGroupId())) {
            pathGroupChat.setVisibility(View.GONE);
        }
        if (path.getTags().size() > 0) {
            tags = new String[path.getTags().size()];
            for (int a = 0; a < path.getTags().size(); a++) {
                tags[a] = path.getTags().get(a);
            }
        }
        addTagView(tags);
    }

    public void addTagView(String[] tags) {
        /**
         * 标签之间的间距 px
         */
        final int itemMargins = 25;
        /**
         * 标签的行间距 px
         */
        final int lineMargins = 25;
        tagsView.removeAllViews();
        final int containerWidth = width - DpToPx.dip2px(this, 100);
        final LayoutInflater inflater = this.getLayoutInflater();
        /** 用来测量字符的宽度 */
        final Paint paint = new Paint();
        TextView textView = (TextView) inflater.inflate(R.layout.row_tag_include, null);
        int itemPadding = textView.getCompoundPaddingLeft() + textView.getCompoundPaddingRight();
        final LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        tvParams.setMargins(0, 0, itemMargins, 0);
        paint.setTextSize(textView.getTextSize());
        LinearLayout layout = new LinearLayout(this);
        layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        layout.setOrientation(LinearLayout.HORIZONTAL);
        tagsView.addView(layout);
        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, lineMargins, 0, 0);
        /** 一行剩下的空间 **/
        int remainWidth = containerWidth;
        // 表示数组长度
        int length = path.getTags().size();
        String text;
        float itemWidth;
        for (int i = 0; i < length; ++i) {
            try {
                text = datas.get(Integer.valueOf(tags[i])).getTag();
            } catch (Exception e) {
                text = tags[i];
            }

            itemWidth = paint.measureText(text) + itemPadding;
            if (remainWidth - itemWidth > 25) {
                addItemView(inflater, layout, tvParams, text);
            } else {
                resetTextViewMarginsRight(layout);
                layout = new LinearLayout(this);
                layout.setLayoutParams(params);
                layout.setOrientation(LinearLayout.HORIZONTAL);
                /** 将前面那一个textview加入新的一行 */
                addItemView(inflater, layout, tvParams, text);
                tagsView.addView(layout);
                remainWidth = containerWidth;
            }
            remainWidth = (int) (remainWidth - itemWidth + 0.5f) - itemMargins;
        }
        if (length > 0) {
            resetTextViewMarginsRight(layout);
        }
    }

    /*****************
     * 将每行最后一个textview的MarginsRight去掉
     *********************************/
    private void resetTextViewMarginsRight(ViewGroup viewGroup) {
        final TextView tempTextView = (TextView) viewGroup.getChildAt(viewGroup.getChildCount() - 1);
        tempTextView
                .setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void addItemView(LayoutInflater inflater, ViewGroup viewGroup, ViewGroup.LayoutParams params, final String text) {
        final TextView tvItem = (TextView) inflater.inflate(R.layout.row_tag_include, null);
        tvItem.setText(text);
        viewGroup.addView(tvItem, params);
    }

    public void setModeKeyboard(View view) {
        if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null)
                manager.hideSoftInputFromWindow(getCurrentFocus()
                        .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } else {
            manager.showSoftInput(view, 0);
        }
    }

    private void pathEdited() {
        pathTitle.setText(path.getTitle());
        switch (path.getType()) {
            case 1://本地人
                pathLocal.setVisibility(View.VISIBLE);
                pathTraveler.setVisibility(View.INVISIBLE);
                break;
            case 2://旅行者
                pathLocal.setVisibility(View.INVISIBLE);
                pathTraveler.setVisibility(View.VISIBLE);
                break;
        }
        Picasso.with(this).load(path.getImage()).into(pathBackground);
//        uiHelper.imageViewSetPossiblyEmptyUrl(this, pathBackground, path.getImage());
        uiHelper.textViewSetPossiblyNullString(pathActivity, path.getText());
        if (path.getDateTime() != 0 || path.getEndDateTime() != 0) {
            pathTime.setVisibility(View.VISIBLE);
            if (path.getDateTime() != 0 && path.getDateTime() != -1) {
                uiHelper.textViewSetPossiblyNullString(pathDateTimeInfo, JsonHelper.getInstance().parseTimestamp(path.getDateTime(), 1));
            }
            if (path.getEndDateTime() != 0) {
                uiHelper.textViewSetPossiblyNullString(pathEndTimeInfo, JsonHelper.getInstance().parseTimestamp(path.getEndDateTime(), 1));
            }
        } else if (!path.getAvailableTime().isEmpty()) {
            availableTime.setVisibility(View.VISIBLE);
            pathTime.setVisibility(View.GONE);
            availableTime.setText(path.getAvailableTime());
        }
        if (path.getType() == 2) {
            uiHelper.textViewSetPossiblyNullString(pathLocationInfo, path.getLocationString());
        } else {
            uiHelper.textViewSetPossiblyNullString(pathLocationInfo, path.getLocationString() + "," + path.getDetailAddress());
        }
        uiHelper.textViewSetPossiblyNullString(pathBudgetInfo, path.getCost());
        uiHelper.textViewSetPossiblyNullString(pathGroudSizeInfo, String.valueOf(path.getCapacity()));
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
            case R.id.iv_delete:
                new android.app.AlertDialog.Builder(this)
                        .setTitle("Are you sure to delete?")
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    path = ServerHelper.getInstance().deletePath(path.getId());
                                    Intent intent = new Intent();
                                    intent.putExtra(PathViewActivity.DELETED_PATH, path);
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
            case R.id.back_button:
                finishedViewing();
                break;
            case R.id.path_share_button:
                MobclickAgent.onEvent(PathViewActivity.this, "discover_share");
                showShare();
                break;
            case R.id.path_accept_button:
                if (!LingoXApplication.getInstance().getSkip()) {
                    if (!ownPath) {
                        if (path.hasUserAccepted(CacheHelper.getInstance().getSelfInfo().getId()))
                            new UnAcceptPath().execute();
                        else
                            new AcceptPath().execute();
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
            case R.id.group_chat:
                new joinGroupChat().execute();
                break;
            case R.id.path_user_avatar:
                if (!LingoXApplication.getInstance().getSkip()) {
                    MobclickAgent.onEvent(this, "discover_avatar");
                    Intent userInfoIntent = new Intent(this, UserInfoActivity.class);
                    userInfoIntent.putExtra(UserInfoActivity.INTENT_USER_ID, user.getId());
                    startActivity(userInfoIntent);
                } else {
                    SkipDialog.getDialog(this).show();
                }
                break;
            case R.id.iv_chat:
                if (!LingoXApplication.getInstance().getSkip()) {
                    MobclickAgent.onEvent(PathViewActivity.this, "discover_message", new HashMap<String, String>().put("message", "chat"));
                    new GetExist().execute(map);
                } else {
                    SkipDialog.getDialog(this).show();
                }
                break;
            case R.id.iv_edit:
                if (!LingoXApplication.getInstance().getSkip()) {
                    MobclickAgent.onEvent(PathViewActivity.this, "discover_message", new HashMap<String, String>().put("message", "edit"));
                    Intent editPathIntent = new Intent(this, PathEditActivity.class);
                    editPathIntent.putExtra(PathEditActivity.PATH_TO_EDIT, path);
                    startActivityForResult(editPathIntent, EDIT_PATH);
                } else {
                    SkipDialog.getDialog(this).show();
                }
                break;
            case R.id.btn_reply:
                if (commentEditText.getText().toString().isEmpty())
                    Toast.makeText(this, getString(R.string.enter_comment), Toast.LENGTH_SHORT).show();
                else if (replyEveryOne) {
                    new PostComment().execute();
                } else {
                    new ReplyComment(replyUser.getId(), commentEditText.getText().toString()).execute();
                    replyEveryOne = true;
                    commentEditText.setHint("");
                }
                break;
            case R.id.path_view_yindao:
                layout.setVisibility(View.GONE);
                break;
            case R.id.path_show_reference:
                Intent intent = new Intent(this, PathReferenceActivity.class);
                intent.putExtra(PathReferenceActivity.PATH, path);
                startActivity(intent);
                break;
            case R.id.back:
                if (layout.isShown()) {
                    layout.setVisibility(View.GONE);
                } else {
                    if (!replyEveryOne) {
                        replyEveryOne = true;
                        commentEditText.setHint("");
                    } else {
                        finishedViewing();
                    }
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

    private void finishedViewing() {
        pathBackground.setImageDrawable(null);
        Intent editedIntent = new Intent();
        editedIntent.putExtra(EDITED_PATH, path);
        setResult(RESULT_OK, editedIntent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case EDIT_PATH:
                switch (resultCode) {
                    case RESULT_OK:
                        if (data.hasExtra(PathEditActivity.DELETED_PATH)) {
                            Intent deletedIntent = new Intent();
                            deletedIntent.putExtra(DELETED_PATH, data.getParcelableExtra(PathEditActivity.DELETED_PATH));
                            setResult(RESULT_OK, deletedIntent);
                            finish();
                            break;
                        } else {
                            path = data.getParcelableExtra(PathEditActivity.EDITED_PATH);
                            pathEdited();
                            break;
                        }
                }
                break;
        }
    }

    private void removeComment(int position) {
        path.removeComment(commentsList.get(position));
        commentsList.remove(position);
        pathCommentsNum.setText(String.valueOf(commentsList.size()));
        commentsListView.removeViewAt(position);
    }

    private void addComment(Comment comment) {
        path.addComment(comment);
        commentsList.add(comment);
        pathCommentsNum.setText(String.valueOf(commentsList.size()));
        commentsListView.addView(getCommentView(commentsList.size() - 1));
        commentEditText.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(commentEditText.getWindowToken(), 0);
        scrollView.fullScroll(View.FOCUS_DOWN);
    }

    private void loadComments() {
        commentsListView.removeAllViews();
        for (int i = 0; i < commentsList.size(); i++) {
            commentsListView.addView(getCommentView(i));
        }
        if (!hasMeasured) {
            scrollView.smoothScrollTo(0, 0);
        }
    }

    public View getCommentView(final int position) {
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
        new LoadReplyUser(comment.getUser_tar(), replyTarName).execute();
        if (!LingoXApplication.getInstance().getSkip()) {
            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (comment.getUserId().equals(CacheHelper.getInstance().getSelfInfo().getId())) {
                        CommentDialog commentDialog = new CommentDialog(comment);
                        commentDialog.setCanceledOnTouchOutside(true);
                        commentDialog.show();
                    } else
                        replyOthers(comment);
                }
            });
        }
        return rowView;
    }

    private void replyOthers(final Comment comment) {
        replyUser = CacheHelper.getInstance().getUserInfo(comment.getUserId());
        commentEditText.setHint((getString(R.string.reply_comment)) + " " + replyUser.getNickname() + ":");
        replyEveryOne = false;
    }

    @Override
    protected void onResume() {
        MobclickAgent.onResume(this);
        MobclickAgent.onPageStart("PathViewActivity");
        super.onResume();
    }

    @Override
    protected void onPause() {
        MobclickAgent.onPause(this);
        MobclickAgent.onPageEnd("PathViewActivity");
        super.onPause();
    }

    @Override
    public void onScrollChanged(final MyScrollView scrollView1, int x, int y, int oldx, int oldy) {
        if (!LingoXApplication.getInstance().getSkip()) {
//            pathCommentsNum.getLocationInWindow(startLocations);
            layoutThree.getLocationInWindow(startLocations);
            pathView.getLocationInWindow(endLocations);
            if (scrollViewHight <= endLocations[1]) {
                scrollViewHight = endLocations[1];
                commentHeight = startLocations[1];
            } else {
                hasMeasured = true;
            }
//            Log.d("星期","commentHeight="+commentHeight+">>> height="+height+">>>> y="+y);
            if (Math.abs(commentHeight - height) <= y) {
                commentsSend.setVisibility(View.VISIBLE);
            } else {
                commentsSend.setVisibility(View.GONE);
            }
        }
    }

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
                Toast.makeText(PathViewActivity.this, getString(R.string.fail_down_path), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(PathViewActivity.this, getString(R.string.fail_down_user), Toast.LENGTH_SHORT).show();
                finish();
            }
            loadingBar.setVisibility(View.INVISIBLE);
            setData();
        }
    }

    // TODO do we need a loading bar or similar indicator?
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
                Toast.makeText(PathViewActivity.this, getString(R.string.fail_comment), Toast.LENGTH_SHORT).show();
            } else {
                MobclickAgent.onEvent(PathViewActivity.this, "discover_comment", new HashMap<String, String>().put("comment", "discover"));
                addComment(comment);
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
            } else
                Toast.makeText(PathViewActivity.this, getString(R.string.fail_comment_del), Toast.LENGTH_SHORT).show();
        }
    }

    private class AcceptPath extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pathAcceptButton.setClickable(false);
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
            if (success) {
                MobclickAgent.onEvent(PathViewActivity.this, "discover_like", new HashMap<String, String>().put("like", "like"));
                if (!TextUtils.isEmpty(path.getHxGroupId())) {
                    pathGroupChat.setVisibility(View.VISIBLE);
                    layout.setVisibility(View.VISIBLE);
                }
                path.addAcceptedUser(CacheHelper.getInstance().getSelfInfo());
                joinedUsersAdapter.addItem(CacheHelper.getInstance().getSelfInfo());
                pathJoinedUserNum.setText(String.valueOf((Integer.parseInt(pathJoinedUserNum.getText().toString()) + 1)));
                joinedUsersAdapter.notifyDataSetChanged();
                pathAcceptButton.setImageResource(R.drawable.active_like_24dp);
                pathAcceptButton.setTag(1);
                joinedUsersListView.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(PathViewActivity.this, getString(R.string.fail_jion), Toast.LENGTH_SHORT).show();
            }
            pathAcceptButton.setClickable(true);
        }
    }

    private class UnAcceptPath extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pathAcceptButton.setClickable(false);
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
            if (success) {
                MobclickAgent.onEvent(PathViewActivity.this, "discover_like", new HashMap<String, String>().put("like", "dislike"));
                path.removeAcceptedUser(CacheHelper.getInstance().getSelfInfo());
                pathAcceptButton.setImageResource(R.drawable.active_dislike_24dp);
                pathAcceptButton.setTag(0);
                joinedUsersAdapter.removeItem(CacheHelper.getInstance().getSelfInfo());
                joinedUsersAdapter.notifyDataSetChanged();
                if (joinedUsersList.size() == 0)
                    joinedUsersListView.setVisibility(View.GONE);
                pathJoinedUserNum.setText(String.valueOf(joinedUsersList.size()));
                pathGroupChat.setVisibility(View.GONE);
            } else {
                Toast.makeText(PathViewActivity.this, getString(R.string.fail_jion), Toast.LENGTH_SHORT).show();
            }
            pathAcceptButton.setClickable(true);
        }
    }

    private class LoadCommentUser extends AsyncTask<Void, Void, Void> {
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
        protected Void doInBackground(Void... params) {
            try {
                boolean ownComment = (CacheHelper.getInstance().getSelfInfo().getId().equals(userId));
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
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //FIXME
            uiHelper.textViewSetPossiblyNullString(userNickname, commentUser.getNickname());
            uiHelper.imageViewSetPossiblyEmptyUrl(PathViewActivity.this, userAvatar, commentUser.getAvatar(), "");
            final View.OnClickListener userClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent mIntent = new Intent(PathViewActivity.this, UserInfoActivity.class);
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

    public class CommentDialog extends Dialog implements View.OnClickListener {
        private Comment comment;

        public CommentDialog(Comment comment) {
            super(PathViewActivity.this, R.style.MyDialogStyle);
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
            commentSendButton.setEnabled(false);
        }

        @Override
        protected Comment doInBackground(Void... params) {
            Comment comment;
            try {
                comment = ServerHelper.getInstance().createComment
                        (userSrcId, userTarId, path.getId(), commentText);
            } catch (Exception e) {
                comment = null;
            }
            return comment;
        }

        @Override
        protected void onPostExecute(Comment comment) {
            super.onPostExecute(comment);
            if (comment == null) {
                Toast.makeText(PathViewActivity.this, getString(R.string.fail_comment), Toast.LENGTH_SHORT).show();
            } else {
                MobclickAgent.onEvent(PathViewActivity.this, "discover_comment", new HashMap<String, String>().put("comment", "user"));
                addComment(comment);
                commentEditText.setText("");
            }
            commentSendButton.setEnabled(true);
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
            boolean isTargetUs = CacheHelper.getInstance().getSelfInfo().getId().equals(userTar);
            if (isTargetUs) {
                targetUser = CacheHelper.getInstance().getSelfInfo();
            } else {
                targetUser = CacheHelper.getInstance().getUserInfo(userTar);
            }
            if (targetUser == null) {
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
                tarName.setText(" " + getString(R.string.replied_to) + " " + user.getNickname());
                tarName.setVisibility(View.VISIBLE);
            }
            super.onPostExecute(user);
        }
    }

    private class CreateAppIconFile extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (Environment.MEDIA_MOUNTED.equals(Environment
                    .getExternalStorageState())
                    && Environment.getExternalStorageDirectory().exists()) {
                appIconImagePath = Environment.getExternalStorageDirectory().
                        getAbsolutePath() + FILE_NAME;
            } else {
                appIconImagePath = getApplication().getFilesDir().getAbsolutePath()
                        + FILE_NAME;
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                File file = new File(appIconImagePath);
                if (!file.exists()) {
                    file.createNewFile();
                    Bitmap pic = BitmapFactory.decodeResource(getResources(),
                            R.drawable.app_share_icon);
                    FileOutputStream fos = new FileOutputStream(file);
                    pic.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.flush();
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                appIconImagePath = null;
                Log.e(LOG_TAG, e.toString());
            }
            return null;
        }
    }

    private class joinGroupChat extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pathGroupChat.setClickable(false);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (!TextUtils.isEmpty(path.getHxGroupId()) && !EMGroupManager.getInstance()
                        .getGroupFromServer(path.getHxGroupId())
                        .getMembers().contains(
                                EMChatManager.getInstance().getCurrentUser())
                        )
                    EMGroupManager.getInstance().joinGroup(path.getHxGroupId());
            } catch (EaseMobException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            MobclickAgent.onEvent(PathViewActivity.this, "discover_group_chat");
            Intent intent = new Intent(PathViewActivity.this, ChatActivity.class);
            intent.putExtra("chatType", ChatActivity.CHATTYPE_GROUP);
            intent.putExtra("groupId", path.getHxGroupId());
            startActivity(intent);
            pathGroupChat.setClickable(true);
        }
    }

    private class GetExist extends AsyncTask<HashMap<String, String>, Void, Boolean> {

        @Override
        protected Boolean doInBackground(HashMap<String, String>... params) {
            try {
                isApply = ServerHelper.getInstance().existApplication(params[0]);
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
                    Intent chatIntent = new Intent(PathViewActivity.this, ChatActivity.class);
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
