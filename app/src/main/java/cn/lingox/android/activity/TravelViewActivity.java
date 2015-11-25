package cn.lingox.android.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import cn.lingox.android.R;
import cn.lingox.android.adapter.TravelLikeAdapter;
import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.constants.StringConstant;
import cn.lingox.android.entity.Comment;
import cn.lingox.android.entity.TravelEntity;
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.ImageHelper;
import cn.lingox.android.helper.JsonHelper;
import cn.lingox.android.helper.TimeHelper;
import cn.lingox.android.helper.UIHelper;
import cn.lingox.android.task.DeleteTravelEntity;
import cn.lingox.android.task.GetUser;
import cn.lingox.android.utils.CircularImageView;
import cn.lingox.android.utils.SkipDialog;
import cn.lingox.android.widget.MyScrollView;
import cn.lingox.android.widget.ScrollViewListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import it.sephiroth.android.library.widget.HListView;

/**
 * 创建travel数据
 */
public class TravelViewActivity extends Activity implements OnClickListener, ScrollViewListener {
    public static final String TRAVEL_VIEW = "travelView";//传递travel的实例
    public static final String DELETE = "delete";//删除
    public static final String EDIT = "edit";//删除
    private static final int EDIT_TRAVEL = 2102;

    private ImageView delete, edit;
    private ImageView like, chat;
    private ImageView flg;

    //数组长度必须为2 第一个为x坐标，第二个为y坐标
    private int[] startLocations = new int[2];
    private int[] endLocations = new int[2];
    private int scrollViewHight;
    private int commentHeight;
    private int height;

    private RelativeLayout travelLayout;
    private LinearLayout threeLayout;

    //comments
    private LinearLayout commentLayout;
    private TextView commentNum;
    private LinearLayout commentList;
    private ArrayList<Comment> commentDatas = new ArrayList<>();
    //commite
    private LinearLayout commitLayout;
    private EditText commitEdit;
    private Button commit;
    //like
    private LinearLayout likeLayout;
    private TextView likeNum;
    private HListView likeList;
    private ArrayList<User> likeDatas;
    private TravelLikeAdapter likeAdapter;

    private CircularImageView avatar;
    private TextView userName, location, travelingTime, tag, describe, provide;

    private TravelEntity travelEntity;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel);
        //初始化控件
        initView();
        //设置数据
        setData();
    }

    /**
     * 初始化控件
     */
    private void initView() {
        ImageView back = (ImageView) findViewById(R.id.travel_view_back);
        back.setOnClickListener(this);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        height = dm.heightPixels;

        MyScrollView scrollView = (MyScrollView) findViewById(R.id.path_view_scroll_view);
        scrollView.setScrollViewListener(this);

        delete = (ImageView) findViewById(R.id.iv_delete);
        delete.setOnClickListener(this);
        edit = (ImageView) findViewById(R.id.iv_edit);
        edit.setOnClickListener(this);

        chat = (ImageView) findViewById(R.id.iv_chat);
        chat.setOnClickListener(this);
        like = (ImageView) findViewById(R.id.path_accept_button);
        like.setOnClickListener(this);
        like.setTag(0);
        //分享
        findViewById(R.id.path_share_button).setOnClickListener(this);
        //包含like、chat和share的layout
        threeLayout = (LinearLayout) findViewById(R.id.path_view_like_chat_share);
        travelLayout = (RelativeLayout) findViewById(R.id.travel_layout);

        avatar = (CircularImageView) findViewById(R.id.travel_view_avatar);
        userName = (TextView) findViewById(R.id.travel_view_name);
        flg = (ImageView) findViewById(R.id.travel_country_flg);
        travelingTime = (TextView) findViewById(R.id.travel_view_time);
        tag = (TextView) findViewById(R.id.travel_view_tag);
        describe = (TextView) findViewById(R.id.travel_view_describe);
        provide = (TextView) findViewById(R.id.travel_view_provide);
        location = (TextView) findViewById(R.id.travel_view_location);
        //comment
        commentLayout = (LinearLayout) findViewById(R.id.path_view_commit);
        commentNum = (TextView) findViewById(R.id.path_comments_num);
        commentList = (LinearLayout) findViewById(R.id.path_view_comments_list);
        //commite
        commitLayout = (LinearLayout) findViewById(R.id.path_view_comment_bar);
        commitEdit = (EditText) findViewById(R.id.comment_text_box);
        commit = (Button) findViewById(R.id.btn_reply);
        commit.setOnClickListener(this);
        //like
        likeLayout = (LinearLayout) findViewById(R.id.path_view_like);
        likeNum = (TextView) findViewById(R.id.path_particpants_num);
        likeList = (HListView) findViewById(R.id.path_view_joined_user_list);
        likeDatas = new ArrayList<>();
        likeAdapter = new TravelLikeAdapter(this, likeDatas);
        likeList.setAdapter(likeAdapter);
        if (LingoXApplication.getInstance().getSkip()) {
            likeList.setClickable(false);
        }
    }

    /**
     * 设置数据
     */
    private void setData() {
        Intent intent = getIntent();
        if (intent.hasExtra(TRAVEL_VIEW)) {
            travelEntity = intent.getParcelableExtra(TRAVEL_VIEW);
        } else {
            finish();
            return;
        }
        //判断是否为自己
        if (travelEntity.getUser_id().equals(CacheHelper.getInstance().getSelfInfo().getId())) {
            //自己
            chat.setVisibility(View.GONE);
            edit.setVisibility(View.VISIBLE);
            delete.setVisibility(View.VISIBLE);
        }
        //获取用户信息
        user = CacheHelper.getInstance().getUserInfo(travelEntity.getUser_id());
        if (user == null) {
            new GetUser(travelEntity.getUser_id(), new GetUser.Callback() {
                @Override
                public void onSuccess(User user) {
                    //设置用户名
                    userName.setText(user.getNicknameOrUsername());
                    //设置头像
                    UIHelper.getInstance().imageViewSetPossiblyEmptyUrl(TravelViewActivity.this, avatar, user.getAvatar(), "circular");
                    //国旗
                    ImageHelper.getInstance().loadFlag(flg, JsonHelper.getInstance().getCodeFromCountry(user.getCountry()), 2);
                }

                @Override
                public void onFail() {

                }
            }).execute();
        } else {
            //设置用户名
            userName.setText(user.getNicknameOrUsername());
            //设置头像
            UIHelper.getInstance().imageViewSetPossiblyEmptyUrl(this, avatar, user.getAvatar(), "circular");
            //国旗
            ImageHelper.getInstance().loadFlag(flg, JsonHelper.getInstance().getCodeFromCountry(user.getCountry()), 2);
        }
        //设置标签
//        if (travelEntity.getTags().size() > 0) {
//            ArrayList<String> list=new ArrayList<>();
//            ArrayList<PathTags> tags=LingoXApplication.getInstance().getDatas();
//            for (int i = 0, j = travelEntity.getTags().size(); i < j; i++) {
//                list.add(tags.get(Integer.valueOf(travelEntity.getTags().get(i))).getTag());
//            }
//            tag.setText(list.toString().replace("[","").replace("]",""));
//        }
        //设置地点
        location.setText(travelEntity.getLocation());
        //设置时间段
        travelingTime.setText(TimeHelper.getInstance().parseTimestampToDate(travelEntity.getStartTime())
                + "-" + TimeHelper.getInstance().parseTimestampToDate(travelEntity.getEndTime()));
        //设置问题
        describe.setText(travelEntity.getText());
        //设置可提供
        provide.setText(travelEntity.getProvide());
        //设置like
        likeDatas.addAll(travelEntity.getLikeUsers());
        if (likeDatas.size() > 0) {
            //有数据
            likeLayout.setVisibility(View.VISIBLE);
            likeNum.setText(String.valueOf(likeDatas.size()));
            likeAdapter.notifyDataSetChanged();
        } else {
            likeLayout.setVisibility(View.GONE);
        }
        //设置comment
        commentDatas.addAll(travelEntity.getComments());
        if (commentDatas.size() > 0) {
            //有数据
            commentLayout.setVisibility(View.VISIBLE);
            commentNum.setText(String.valueOf(commentDatas.size()));

        } else {
            commentLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.travel_view_back:
                finish();
                break;
            case R.id.path_share_button:
                showShare();
                break;
            case R.id.iv_chat:
                if (!LingoXApplication.getInstance().getSkip()) {
                    Intent chatIntent = new Intent(TravelViewActivity.this, ChatActivity.class);
                    chatIntent.putExtra("username", user.getUsername());
                    chatIntent.putExtra(StringConstant.nicknameStr, user.getNickname());
                    chatIntent.putExtra("chatType", ChatActivity.CHATTYPE_SINGLE);
                    startActivity(chatIntent);
                } else {
                    SkipDialog.getDialog(this).show();
                }
                break;
            case R.id.iv_edit:
                if (!LingoXApplication.getInstance().getSkip()) {
                    Intent editPathIntent = new Intent(this, TravelEditActivity.class);
                    editPathIntent.putExtra(TravelEditActivity.TRAVEL_EDIT, travelEntity);
                    startActivityForResult(editPathIntent, EDIT_TRAVEL);
                } else {
                    SkipDialog.getDialog(this).show();
                }
                break;
            case R.id.iv_delete:
                if (!LingoXApplication.getInstance().getSkip()) {
                    new AlertDialog.Builder(this)
                            .setTitle("Are you sure to delete?")
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new DeleteTravelEntity(travelEntity.getId(), new DeleteTravelEntity.Callback() {
                                @Override
                                public void onSuccess(TravelEntity entity) {
                                    try {
                                        Intent intent = new Intent();
                                        intent.putExtra(TravelViewActivity.DELETE, travelEntity);
                                        setResult(RESULT_OK, intent);
                                        finish();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onFail() {

                                }
                            }).execute();
                        }
                    }).create().show();
                } else {
                    SkipDialog.getDialog(this).show();
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
//        oks.setTitle(path.getTitle());
        // text是分享文本，所有平台都需要这个字段
        oks.setText(travelEntity.getText());
        // url仅在微信（包括好友和朋友圈）中使用
        oks.setUrl("http://lingox.cn/viewActivity?demandId=" + travelEntity.getId());
        // 启动分享GUI
        oks.show(this);
    }

    @Override
    public void onScrollChanged(final MyScrollView scrollView1, int x, int y, int oldx, int oldy) {
        if (!LingoXApplication.getInstance().getSkip()) {
//            pathCommentsNum.getLocationInWindow(startLocations);
            threeLayout.getLocationInWindow(startLocations);
            travelLayout.getLocationInWindow(endLocations);
            if (scrollViewHight <= endLocations[1]) {
                scrollViewHight = endLocations[1];
                commentHeight = startLocations[1];
            }
            if (Math.abs(commentHeight - height) <= y) {
                commitLayout.setVisibility(View.VISIBLE);
            } else {
                commitLayout.setVisibility(View.GONE);
            }
        }
    }
}