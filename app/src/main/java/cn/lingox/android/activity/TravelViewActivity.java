package cn.lingox.android.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import cn.lingox.android.R;
import cn.lingox.android.adapter.TravelLikeAdapter;
import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.constants.StringConstant;
import cn.lingox.android.entity.PathTags;
import cn.lingox.android.entity.TravelComment;
import cn.lingox.android.entity.TravelEntity;
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.ImageHelper;
import cn.lingox.android.helper.JsonHelper;
import cn.lingox.android.helper.ServerHelper;
import cn.lingox.android.helper.TimeHelper;
import cn.lingox.android.helper.UIHelper;
import cn.lingox.android.task.CreateCommentTravelEntity;
import cn.lingox.android.task.DelCommentTravelEntity;
import cn.lingox.android.task.DeleteTravelEntity;
import cn.lingox.android.task.GetTravelEntity;
import cn.lingox.android.task.GetUser;
import cn.lingox.android.task.LikeTravelEntity;
import cn.lingox.android.task.UnLikeTravelEntity;
import cn.lingox.android.utils.CircularImageView;
import cn.lingox.android.utils.CreateTagView;
import cn.lingox.android.utils.SkipDialog;
import cn.lingox.android.widget.MyScrollView;
import cn.lingox.android.widget.NewTextView;
import cn.lingox.android.widget.ScrollViewListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import it.sephiroth.android.library.widget.HListView;

/**
 * travel数据详情
 */
public class TravelViewActivity extends Activity implements OnClickListener, ScrollViewListener {
    public static final String TRAVEL_VIEW = "travelView";//传递travel的实例
    public static final String DELETE = "delete";//删除
    public static final String EDIT = "edit";//删除
    public static final String TRAVEL_ID = "demand_id";
    private static final int EDIT_TRAVEL = 2102;
    private ImageView chat;
    private ImageView flg;

    /*用于判断评论编辑栏是否隐藏----暂时屏蔽掉，不使用
    //数组长度必须为2 第一个为x坐标，第二个为y坐标
    private int[] startLocations = new int[2];
    private int[] endLocations = new int[2];
    private int scrollViewHight;
    private int commentHeight;
    private int width;
    private int height;

    private RelativeLayout travelLayout;
    private LinearLayout threeLayout;
*/
    //标签
    private ViewGroup tagsView = null;
    private LinearLayout tagLayout;
    //comments
    private LinearLayout commentLayout;
    private TextView commentNum;
    private LinearLayout commentList;
    private ArrayList<TravelComment> commentDatas = new ArrayList<>();
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
    private TextView userName, location,provide;

    private TravelEntity travelEntity;
    private User user;

    private boolean ownTravel = false;//false 不是自己的活动   true是自己的活动
    private User replyUser;
    private boolean replyEveryOne = false;//true回复  false不回复


    private TextView startDay;//开始时间----日
    private TextView startMonth;//开始时间----月
    private TextView endDay;//结束时间---日
    private TextView endMonth;//结束时间----月
    private TextView travelingLocation;//旅行地点
//    private TextView request;//问题题目---貌似没有
    private TextView detail;//问题描述
    private ImageView menu;//右上角菜单
    private LinearLayout favourite,delete,edit,share;
    private ImageView like;//是否已收藏
    private PopupWindow mPopupWindow;//弹出框

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel);
        //初始化控件
        initView();
        Intent intent = getIntent();
        if (intent.hasExtra(TRAVEL_VIEW)) {
            travelEntity = intent.getParcelableExtra(TRAVEL_VIEW);
            //设置数据
            setData();
        } else if (intent.hasExtra(TRAVEL_ID)) {
            //根据id，下载数据
            new GetTravelEntity(intent.getStringExtra(TRAVEL_ID), new GetTravelEntity.Callback() {
                @Override
                public void onSuccess(TravelEntity entity) {
                    travelEntity = entity;
                    //设置数据
                    setData();
                }

                @Override
                public void onFail() {
                    Toast.makeText(TravelViewActivity.this, "Download failed", Toast.LENGTH_SHORT).show();
                }
            }).execute();
        } else {
            finish();
        }
    }

    /**
     * 初始化控件
     */
    private void initView() {
        //菜单
        menu=(ImageView)findViewById(R.id.travel_menu);
        menu.setOnClickListener(this);

        View popupView = getLayoutInflater().inflate(R.layout.menu_travel, null);
        favourite=(LinearLayout)popupView.findViewById(R.id.menu_travel_favourite);
        favourite.setOnClickListener(this);
        delete=(LinearLayout)popupView.findViewById(R.id.menu_travel_del);
        delete.setOnClickListener(this);
        edit=(LinearLayout)popupView.findViewById(R.id.menu_travel_edit);
        edit.setOnClickListener(this);
        share=(LinearLayout)popupView.findViewById(R.id.menu_travel_share);
        share.setOnClickListener(this);
        like=(ImageView)popupView.findViewById(R.id.menu_iv_favourite);
        mPopupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mPopupWindow.setTouchable(true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
        //开始时间
         startDay=(TextView)findViewById(R.id.start_day);
         startMonth=(TextView)findViewById(R.id.start_month);
       //结束时间
         endDay=(TextView)findViewById(R.id.end_day);
         endMonth=(TextView)findViewById(R.id.end_month);
        //旅行地点
         travelingLocation=(TextView)findViewById(R.id.traveling_location);
        //头像
        avatar = (CircularImageView) findViewById(R.id.travel_view_avatar);
        avatar.setOnClickListener(this);
        //名字
        userName = (TextView) findViewById(R.id.travel_view_name);
        userName.setOnClickListener(this);
        //国旗
        flg = (ImageView) findViewById(R.id.travel_country_flg);
        //旅行者的地址
        location = (TextView) findViewById(R.id.travel_location);
//        //问题题目-----好像没有
//         request=(TextView)findViewById(R.id.travel_request);
        //描述
         detail=(TextView)findViewById(R.id.travel_detail);
        //标签
        tagsView = (ViewGroup) findViewById(R.id.travel_view_tag);
        tagLayout = (LinearLayout) findViewById(R.id.tag);
        //返回
        ImageView back = (ImageView) findViewById(R.id.travel_view_back);
        back.setOnClickListener(this);
        //可提供
        provide = (TextView) findViewById(R.id.travel_view_provide);
        //聊天
        chat = (ImageView) findViewById(R.id.chat_travel);
        chat.setOnClickListener(this);
        //用于判断评论编辑栏是否隐藏，获取屏幕高度----暂时屏蔽掉，不使用
//        DisplayMetrics dm = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(dm);
//        width = dm.widthPixels;
//        height = dm.heightPixels;
        //包含like、chat和share的layout
//        threeLayout = (LinearLayout) findViewById(R.id.like_chat_share);
//        travelLayout = (RelativeLayout) findViewById(R.id.travel_layout);

        MyScrollView scrollView = (MyScrollView) findViewById(R.id.path_view_scroll_view);
        scrollView.setScrollViewListener(this);

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
/*****************************************comment*******************************************/
    /**
     * 设置数据
     */
    private void setData() {
        commitLayout.setVisibility(View.VISIBLE);
        //判断是否为自己
        if (travelEntity.getUser_id().equals(CacheHelper.getInstance().getSelfInfo().getId())) {
            //自己
            ownTravel = true;
            chat.setVisibility(View.GONE);
            edit.setVisibility(View.VISIBLE);
            delete.setVisibility(View.VISIBLE);
            favourite.setVisibility(View.GONE);
        } else {//不是自己的
            ownTravel = false;
            chat.setVisibility(View.VISIBLE);
            delete.setVisibility(View.GONE);
            edit.setVisibility(View.GONE);
            favourite.setVisibility(View.VISIBLE);
        }
        //获取用户信息
        user = CacheHelper.getInstance().getUserInfo(travelEntity.getUser_id());
        if (user == null) {
            new GetUser(travelEntity.getUser_id(), new GetUser.Callback() {
                @Override
                public void onSuccess(User user) {
                    //设置用户名
                    userName.setText(user.getNicknameOrUsername());
                    //设置地址
                    location.setText(user.getLocation());
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
            //设置地址
            location.setText(user.getLocation());
            //设置头像
            UIHelper.getInstance().imageViewSetPossiblyEmptyUrl(this, avatar, user.getAvatar(), "circular");
            //国旗
            ImageHelper.getInstance().loadFlag(flg, JsonHelper.getInstance().getCodeFromCountry(user.getCountry()), 2);
        }
        //设置标签
        if (travelEntity.getTags().size() > 0) {
            ArrayList<String> list = new ArrayList<>();
            ArrayList<PathTags> tags = LingoXApplication.getInstance().getDatas();
            for (int i = 0, j = travelEntity.getTags().size(); i < j; i++) {
                list.add(tags.get(Integer.valueOf(travelEntity.getTags().get(i))).getTag());
            }
            //添加标签
            CreateTagView.addTagView(list, tagsView, this);
        }else{
            tagLayout.setVisibility(View.GONE);
        }
        //设置旅游地点
        travelingLocation.setText(travelEntity.getProvince().isEmpty()?
                travelEntity.getCountry():travelEntity.getProvince()+", "+travelEntity.getCountry());
        //设置时间段
        startDay.setText(TimeHelper.getInstance().parseTimestampToDate(travelEntity.getStartTime()).split("-")[1]);
        startMonth.setText(TimeHelper.getInstance().parseTimestampToDate(travelEntity.getStartTime()).split("-")[0]);
        endDay.setText(TimeHelper.getInstance().parseTimestampToDate(travelEntity.getEndTime()).split("-")[1]);
        endMonth.setText(TimeHelper.getInstance().parseTimestampToDate(travelEntity.getEndTime()).split("-")[0]);
        //设置问题
        detail.setText(travelEntity.getText());
        NewTextView.justify(detail, detail.getWidth());
        /*TextView分散对齐------效果不怎么好看
        NewTextView.justify(describe, describe.getWidth());
        */

        //设置可提供
        provide.setText(travelEntity.getProvide());
        //设置like
        if (!LingoXApplication.getInstance().getSkip()) {
            likeDatas.addAll(travelEntity.getLikeUsers());
            if (likeDatas.size() > 0) {
                //有数据
                likeLayout.setVisibility(View.VISIBLE);
                likeNum.setText(String.valueOf(likeDatas.size()));
                likeAdapter.notifyDataSetChanged();
            } else {
                likeLayout.setVisibility(View.GONE);
            }
            if (ownTravel) {
                like.setImageResource(R.drawable.active_likepeople_24dp);
                like.setTag(1);
            } else {
                like.setImageResource(
                        travelEntity.hasUserLiked(CacheHelper.getInstance().getSelfInfo().getId())
                                ? R.drawable.active_like_24dp : R.drawable.active_dislike_24dp);
                like.setTag(travelEntity.hasUserLiked(CacheHelper.getInstance().getSelfInfo().getId()) ? 0 : 1);
            }
        }

        //设置comment
        commentDatas.addAll(travelEntity.getComments());
        if (commentDatas.size() > 0) {
            //有数据
            commentLayout.setVisibility(View.VISIBLE);
            commentNum.setText(String.valueOf(commentDatas.size()));
            loadComments();
        } else {
            commentLayout.setVisibility(View.GONE);
        }
    }

    /**
     * 判断当前用户是否已like
     *
     * @return false尚未like  true已like
     */
    private boolean userAccepted() {
        for (User users : travelEntity.getLikeUsers()) {
            if (users.getId().equals(CacheHelper.getInstance().getSelfInfo().getId()))
                return true;
        }
        return false;
    }

    /**
     * 设置评论的提交参数
     *
     * @return 参数集合
     */
    private HashMap<String, String> setCommentCommit() {
        HashMap<String, String> params = new HashMap<>();
        params.put(StringConstant.userIdStr, CacheHelper.getInstance().getSelfInfo().getId());
        params.put("demandId", travelEntity.getId());
        params.put("text", commitEdit.getText().toString());
        if (replyEveryOne) {
            params.put("replyUser", replyUser.getId());
        }
        return params;
    }

    /**
     * 移除一条评论
     *
     * @param position 需要移除的评论的位置
     */
    private void removeComment(int position) {
        travelEntity.removeComment(commentDatas.get(position));
        commentDatas.remove(position);
//        if (commentDatas.size() <= 0) {
//            commitLayout.setVisibility(View.GONE);
//        }
        commentNum.setText(String.valueOf(commentDatas.size()));
        commentList.removeViewAt(position);
    }

    /**
     * 添加新的评论
     *
     * @param comment 新评论的实例
     */
    private void addComment(TravelComment comment) {
        travelEntity.addComment(comment);
        commentDatas.add(comment);
        commentNum.setText(String.valueOf(commentDatas.size()));
        commentList.addView(getCommentView(commentDatas.size() - 1));
        commitEdit.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(commitEdit.getWindowToken(), 0);
    }

    /**
     * 加载评论视图
     */
    private void loadComments() {
        commentList.removeAllViews();
        for (int i = 0, j = commentDatas.size(); i < j; i++) {
            commentList.addView(getCommentView(i));
        }
    }

    /**
     * 创建评论item---------可以用listView替换，与ScrollView冲突
     *
     * @param position 位置
     * @return 视图
     */
    private View getCommentView(final int position) {
        View rowView = getLayoutInflater().inflate(R.layout.row_path_comment, null);
        final TravelComment comment = commentDatas.get(position);
        ImageView userAvatar = (ImageView) rowView.findViewById(R.id.comment_user_avatar);
        if (!LingoXApplication.getInstance().getSkip()) {
            if (CacheHelper.getInstance().getSelfInfo().getId().contentEquals(comment.getUser_id())) {
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

        UIHelper.getInstance().textViewSetPossiblyNullString(commentText, comment.getText());
        UIHelper.getInstance().textViewSetPossiblyNullString(commentDateTime, JsonHelper.getInstance().parseSailsJSDate(comment.getCreatedAt()));
        new LoadCommentUser(userNickname, userAvatar, comment.getUser_id()).execute();
        if (!comment.getUser_tar().isEmpty()) {
            new LoadReplyUser(comment.getUser_tar(), replyTarName).execute();
        }
        if (!LingoXApplication.getInstance().getSkip()) {
            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (comment.getUser_id().equals(CacheHelper.getInstance().getSelfInfo().getId())) {
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
     * 点击回复某人
     *
     * @param comment 点击的评论实例
     */
    private void replyOthers(final TravelComment comment) {
        replyUser = CacheHelper.getInstance().getUserInfo(comment.getUser_id());
        commitEdit.setText("");
        commitEdit.setHint((getString(R.string.reply_comment)) + " " + replyUser.getNickname() + ":");
        replyEveryOne = true;
    }

    /*********************************************************************************************/
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.travel_view_name://名字
            case R.id.travel_view_avatar://头像
                Intent intent = new Intent(this, UserInfoActivity.class);
                intent.putExtra(UserInfoActivity.INTENT_USER_ID, travelEntity.getUser_id());
                startActivity(intent);
                break;
            case R.id.btn_reply://回复
                if (commitEdit.getText().toString().isEmpty()) {
                    Toast.makeText(this, getString(R.string.enter_comment), Toast.LENGTH_SHORT).show();
                } else {
                    commit.setClickable(false);
                    new CreateCommentTravelEntity(this, setCommentCommit(), new CreateCommentTravelEntity.Callback() {
                        @Override
                        public void onSuccess(TravelComment comment) {
                            replyEveryOne = false;
                            addComment(comment);
                            commit.setClickable(true);
                            commitEdit.setText("");
                            commitEdit.setHint("");
                            commentLayout.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onFail() {
                            commit.setClickable(true);
                        }
                    }).execute();
                }
                break;
            case R.id.travel_view_back://返回
                finishedViewing();
                break;
            case R.id.chat_travel://聊天
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
            case R.id.travel_menu://菜单
                mPopupWindow.showAsDropDown(menu,-100,0);
                break;
            case R.id.menu_travel_share://分享
                mPopupWindow.dismiss();
                showShare();
                break;
            case R.id.menu_travel_edit://编辑
                mPopupWindow.dismiss();
                if (!LingoXApplication.getInstance().getSkip()) {
                    Intent editPathIntent = new Intent(this, TravelEditActivity.class);
                    editPathIntent.putExtra(TravelEditActivity.TRAVEL_EDIT, travelEntity);
                    startActivityForResult(editPathIntent, EDIT_TRAVEL);
                } else {
                    SkipDialog.getDialog(this).show();
                }
                break;
            case R.id.menu_travel_del://删除
                mPopupWindow.dismiss();
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
                            new DeleteTravelEntity(TravelViewActivity.this, travelEntity.getId(), new DeleteTravelEntity.Callback() {
                                @Override
                                public void onSuccess(TravelEntity entity) {
                                    try {
                                        Intent delIntent = new Intent();
                                        delIntent.putExtra(TravelViewActivity.DELETE, travelEntity);
                                        setResult(RESULT_OK, delIntent);
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
                break;case R.id.menu_travel_favourite://收藏
                mPopupWindow.dismiss();
                if (!LingoXApplication.getInstance().getSkip()) {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("demandId", travelEntity.getId());
                    map.put("userId", CacheHelper.getInstance().getSelfInfo().getId());
                    if (!ownTravel) {//不是自己的
                        if (userAccepted()) {
                            new UnLikeTravelEntity(this, map, new UnLikeTravelEntity.Callback() {
                                @Override
                                public void onSuccess(TravelEntity entity) {
                                    likeDatas.remove(CacheHelper.getInstance().getSelfInfo());
                                    travelEntity.setLikeUsers(likeDatas);
                                    like.setImageResource(R.drawable.active_dislike_24dp);
                                    like.setTag(0);
                                    likeAdapter.notifyDataSetChanged();
                                    if (likeDatas.size() == 0) {
                                        likeList.setVisibility(View.GONE);
                                        likeLayout.setVisibility(View.GONE);
                                    }
                                    likeNum.setText(String.valueOf(likeDatas.size()));
                                }

                                @Override
                                public void onFail() {
                                    Toast.makeText(TravelViewActivity.this, getString(R.string.fail_jion), Toast.LENGTH_SHORT).show();
                                }
                            }).execute();
                        } else {
                            //like成功，添加到数据源中，并刷新
                            new LikeTravelEntity(this,map, new LikeTravelEntity.Callback() {
                                @Override
                                public void onSuccess(TravelEntity entity) {
                                    likeDatas.add(CacheHelper.getInstance().getSelfInfo());
                                    likeNum.setText(String.valueOf((Integer.parseInt(likeNum.getText().toString()) + 1)));
                                    likeAdapter.notifyDataSetChanged();
                                    travelEntity.setLikeUsers(likeDatas);
                                    like.setImageResource(R.drawable.active_like_24dp);
                                    like.setTag(1);
                                    likeList.setVisibility(View.VISIBLE);
                                    likeLayout.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onFail() {
                                    Toast.makeText(TravelViewActivity.this, getString(R.string.fail_jion), Toast.LENGTH_SHORT).show();
                                }
                            }).execute();
                        }
                    } else {
                        Intent mIntent = new Intent(this, UserListActivity.class);
                        mIntent.putParcelableArrayListExtra(UserListActivity.USER_LIST, travelEntity.getLikeUsers());
                        mIntent.putExtra(UserListActivity.PAGE_TITLE, getString(R.string.joined_users));
                        startActivity(mIntent);
                    }
                } else {
                    SkipDialog.getDialog(this).show();
                }
                break;
        }
    }

    /*
      *系统回退键
       */
    @Override
    public void onBackPressed() {
        finishedViewing();
    }

    /**
     * 返回上一级
     */
    private void finishedViewing() {
        if (replyEveryOne) {
            replyEveryOne = false;
            commitEdit.setHint("");
        } else {
            Intent editedIntent = new Intent();
            editedIntent.putExtra(EDIT, travelEntity);
            setResult(RESULT_OK, editedIntent);
            finish();
        }
    }

    // 分享
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

    /**
     * 根据MyScrollView的滚动距离，判断是否隐藏评论编辑
     *
     * @param scrollView1 控件MyScrollView的实例
     * @param x           x轴的当前坐标
     * @param y           y轴的当前坐标
     * @param oldx        x轴的老坐标
     * @param oldy        y轴的老坐标
     */
    @Override
    public void onScrollChanged(final MyScrollView scrollView1, int x, int y, int oldx, int oldy) {
//        if (!LingoXApplication.getInstance().getSkip()) {
//            threeLayout.getLocationInWindow(startLocations);
//            travelLayout.getLocationInWindow(endLocations);
//            if (scrollViewHight <= endLocations[1]) {
//                scrollViewHight = endLocations[1];
//                commentHeight = startLocations[1];
//            }
//            if (Math.abs(commentHeight - height) <= y) {
//                commitLayout.setVisibility(View.VISIBLE);
//            } else {
//                commitLayout.setVisibility(View.GONE);
//            }
//        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case EDIT_TRAVEL:
                    if (data.hasExtra(TravelEditActivity.TRAVEL_EDIT)) {
                        travelEntity = data.getParcelableExtra(TravelEditActivity.TRAVEL_EDIT);
                        setData();
                    }
                    break;
            }
        }
    }

    /**
     * 删除评论是的提示框
     */
    private class CommentDialog extends Dialog implements View.OnClickListener {
        private TravelComment comment;

        public CommentDialog(TravelComment comment) {
            super(TravelViewActivity.this, R.style.MyDialogStyle);
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
                    new DelCommentTravelEntity(comment.getId(), new DelCommentTravelEntity.Callback() {
                        @Override
                        public void onSuccess(TravelComment result) {
                            removeComment(commentDatas.indexOf(comment));
                        }

                        @Override
                        public void onFail() {

                        }
                    }).execute();
                    dismiss();
                    break;
            }
        }
    }

    /**
     * 加载评论用户的信息
     */
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
                if (!LingoXApplication.getInstance().getSkip()
                        && (CacheHelper.getInstance().getSelfInfo().getId().equals(userId))) {
                    commentUser = CacheHelper.getInstance().getSelfInfo();
                } else {
                    commentUser = CacheHelper.getInstance().getUserInfo(userId);
                }
                if (commentUser == null) {
                    commentUser = ServerHelper.getInstance().getUserInfo(userId);
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            //FIXME
            if (success) {
                UIHelper.getInstance().textViewSetPossiblyNullString(userNickname, commentUser.getNickname());
                UIHelper.getInstance().imageViewSetPossiblyEmptyUrl(TravelViewActivity.this, userAvatar, commentUser.getAvatar(), "");
                final View.OnClickListener userClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent mIntent = new Intent(TravelViewActivity.this, UserInfoActivity.class);
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

    /**
     * 加载回复评论的用户信息
     */
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
            boolean isTargetUs = !LingoXApplication.getInstance().getSkip() &&
                    CacheHelper.getInstance().getSelfInfo().getId().equals(userTar);
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
                tarName.setText(" " + getString(R.string.replied_to) + " " + user.getNickname());
                tarName.setVisibility(View.VISIBLE);
            }
            super.onPostExecute(user);
        }
    }
}