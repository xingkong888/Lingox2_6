package cn.lingox.android.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import cn.lingox.android.R;
import cn.lingox.android.activity.imagechooser.AddPhotosActivity;
import cn.lingox.android.adapter.UserPhotosAdapter;
import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.constants.StringConstant;
import cn.lingox.android.entity.Photo;
import cn.lingox.android.entity.Reference;
import cn.lingox.android.entity.Travel;
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.ImageHelper;
import cn.lingox.android.helper.JsonHelper;
import cn.lingox.android.helper.ServerHelper;
import cn.lingox.android.helper.TimeHelper;
import cn.lingox.android.helper.UIHelper;
import cn.lingox.android.task.LoadUserReferences;
import cn.lingox.android.widget.PhotoTagsSelectDialog;
import cn.lingox.android.widget.PlacesDialog;
import cn.lingox.android.widget.SelectProfesionalDialog;
import cn.lingox.android.widget.SelectSpeakDialog;
import it.sephiroth.android.library.widget.HListView;

public class UserInfoFragment extends Fragment implements OnClickListener {
    // Intent messages
    public static final String TARGET_USER_ID = LingoXApplication.PACKAGE_NAME + ".TARGET_USER_ID";
    public static final String TARGET_USER_NAME = LingoXApplication.PACKAGE_NAME + ".TARGET_USER_NAME";
    public static final String AVATAR_URL = LingoXApplication.PACKAGE_NAME + "AVATAR_URL";
    public static final String REFERENCES = LingoXApplication.PACKAGE_NAME + ".REFERENCES";

    // REQUEST CODES
    public static final int EDIT_USER = 101;
    public static final int EDIT_REFERENCES = 102;
    // Bundle Args
    static final String USER = "USER";
    private static final String LOG_TAG = "UserInfoFragment";
    private static final int ADD_PHOTOS = 103;
    private static final int ADD_TRAVEL = 104;

    private static final int SEEALL_TRAVEL = 107;
    //是否提供
    private boolean local = false;//false 不提供 true 提供
    private boolean meal = false;
    private boolean stay = false;
    private View v;
    // UI Elements
    private TextView userInsterest, time, userName, userIdAndPlace, userSexAndAge, userFollow, userFollowing, userReference,
            userEdit, userAddFollow, userSpeak, userAge, userSex, userLocal, userMeal, userStay,
            availableLocal, availableMeal, availableStay, localTitle, travelTitle, localNothing1, travelNothing1, localNothing2, travelNothing2,
            aboutSelf1, aboutSelf2, userInfoProfessional, userInfoPlaces, about;
    private ImageView userAvatar, flag, photoAdd, travelAdd, aboutEdit,
            jiantou0, jiantou1, jiantou2, jiantou3;
    private ProgressBar photosProgressBar;
    private RelativeLayout layout_photo, layout_travel, layout_tag;
    private UserPhotosAdapter photoAdapter;
    private LinearLayout travelContent, layoutSelf, layoutSpeak, layoutAge, layoutSex, editOrChat;
    // Data Elements
    private User user;
    private ArrayList<User> userFollowingList = new ArrayList<>();
    private ArrayList<User> userFollowList = new ArrayList<>();
    private ArrayList<Reference> referenceList = new ArrayList<>();
    private ArrayList<Photo> photoList = new ArrayList<>();
    //旅行计划
    private ArrayList<Travel> travelList;
    //个人标签
    private Boolean requestingOthersData;
    private EditText tagsView;//about
    private TextView line;
    private boolean editOrOk = false;//表示标签状态 false：完成 true：编辑

    private LinearLayout follow, following, reference;

    private ArrayList<String> placesList = new ArrayList<>();
    private ArrayList<String> list = new ArrayList<>();
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            userInsterest.setText((String) msg.obj);
            new UpdateUserInfo("interest").execute();
        }
    };

    private Handler handler1 = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            new UpdateUserInfo("speak").execute();
        }
    };
    private Handler handler2 = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            new UpdateUserInfo("places").execute();
            placesList.clear();
            placesList.addAll((ArrayList<String>) msg.obj);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_user_info, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getArguments().containsKey(USER)) {
            user = getArguments().getParcelable(USER);
        }
        requestingOthersData =
                !LingoXApplication.getInstance().getSkip() &&
                        user != null &&
                        user.getId().contentEquals(CacheHelper.getInstance().getSelfInfo().getId());
        initView();
        initData();
    }

    private void initView() {

        if (requestingOthersData) {
            //如果不是自己的个人信息页面，将home meal等三个选项隐藏
            v.findViewById(R.id.userinfo_include_tag).setVisibility(View.VISIBLE);
        }
        tagsView = (EditText) v.findViewById(R.id.tags_layout);

        layout_tag = (RelativeLayout) v.findViewById(R.id.userinfo_self);
        userAvatar = (ImageView) v.findViewById(R.id.userinfo_avatar);
        flag = (ImageView) v.findViewById(R.id.userinfo_countryImg);
        photoAdd = (ImageView) v.findViewById(R.id.local_add);
        photoAdd.setOnClickListener(this);
        travelAdd = (ImageView) v.findViewById(R.id.travel_add);
        travelAdd.setOnClickListener(this);
        aboutEdit = (ImageView) v.findViewById(R.id.about_edit);
        aboutEdit.setOnClickListener(this);
        line = (TextView) v.findViewById(R.id.userinfo_line);
        jiantou0 = (ImageView) v.findViewById(R.id.jiantou_0);
        jiantou1 = (ImageView) v.findViewById(R.id.jiantou_1);
        jiantou2 = (ImageView) v.findViewById(R.id.jiantou_2);
        jiantou3 = (ImageView) v.findViewById(R.id.jiantou_3);
        localTitle = (TextView) v.findViewById(R.id.userinfo_local_title);
        travelTitle = (TextView) v.findViewById(R.id.userinfo_travel_title);
        localNothing1 = (TextView) v.findViewById(R.id.userinfo_local_nothing1);
        travelNothing1 = (TextView) v.findViewById(R.id.userinfo_travel_nothing1);
        localNothing2 = (TextView) v.findViewById(R.id.userinfo_local_nothing2);
        travelNothing2 = (TextView) v.findViewById(R.id.userinfo_travel_nothing2);
        aboutSelf1 = (TextView) v.findViewById(R.id.userinfo_about_self1);
        aboutSelf2 = (TextView) v.findViewById(R.id.userinfo_about_self2);
        about = (TextView) v.findViewById(R.id.userinfo_about);
        time = (TextView) v.findViewById(R.id.userinfo_time);
        availableMeal = (TextView) v.findViewById(R.id.userinfo_available_meal);
        availableLocal = (TextView) v.findViewById(R.id.userinfo_available_local);
        availableStay = (TextView) v.findViewById(R.id.userinfo_available_stay);
        userName = (TextView) v.findViewById(R.id.userinfo_name);
        userIdAndPlace = (TextView) v.findViewById(R.id.userinfo_id);
        userSexAndAge = (TextView) v.findViewById(R.id.userinfo_sex);

        follow = (LinearLayout) v.findViewById(R.id.layout_follow);
        follow.setOnClickListener(this);
        follow.setClickable(false);
        following = (LinearLayout) v.findViewById(R.id.layout_following);
        following.setOnClickListener(this);
        following.setClickable(false);
        reference = (LinearLayout) v.findViewById(R.id.layout_reference);
        reference.setOnClickListener(this);
        reference.setClickable(false);

        userFollowing = (TextView) v.findViewById(R.id.userinfo_follow);
        userFollow = (TextView) v.findViewById(R.id.userinfo_following);
        userReference = (TextView) v.findViewById(R.id.userinfo_reference);
        userAddFollow = (TextView) v.findViewById(R.id.userinfo_add_follow);
        userAddFollow.setOnClickListener(this);
        //“Chat”按钮
        v.findViewById(R.id.userinfo_chat).setOnClickListener(this);
        //“+Reference”按钮
        v.findViewById(R.id.userinfo_add_reference).setOnClickListener(this);
        userEdit = (TextView) v.findViewById(R.id.userinfo_edit);
        userEdit.setOnClickListener(this);
        if (!requestingOthersData) {
            userEdit.setVisibility(View.INVISIBLE);
        }
        userSpeak = (TextView) v.findViewById(R.id.userinfo_speak_info);
        userAge = (TextView) v.findViewById(R.id.userinfo_age_info);
        userSex = (TextView) v.findViewById(R.id.userinfo_gender_info);
        userInsterest = (TextView) v.findViewById(R.id.userinfo_interest_info);
        userLocal = (TextView) v.findViewById(R.id.userinfo_tag2);
        userMeal = (TextView) v.findViewById(R.id.userinfo_tag1);
        userStay = (TextView) v.findViewById(R.id.userinfo_tag3);
        userInfoPlaces = (TextView) v.findViewById(R.id.userinfo_places_info);
        userInfoProfessional = (TextView) v.findViewById(R.id.userinfo_professional_info);

        v.findViewById(R.id.layout_available_local).setOnClickListener(this);
        v.findViewById(R.id.layout_available_meal).setOnClickListener(this);
        v.findViewById(R.id.layout_available_stay).setOnClickListener(this);

        editOrChat = (LinearLayout) v.findViewById(R.id.userinfo_edit_chat);
        layoutSelf = (LinearLayout) v.findViewById(R.id.layout_self);
        layoutSpeak = (LinearLayout) v.findViewById(R.id.layout_speak);
        layoutAge = (LinearLayout) v.findViewById(R.id.layout_age);
        layoutSex = (LinearLayout) v.findViewById(R.id.layout_gender);

        RelativeLayout layoutAvatar = (RelativeLayout) v.findViewById(R.id.asdfasdf);
        travelContent = (LinearLayout) v.findViewById(R.id.travel_content);
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;//屏幕的宽度
        LayoutParams params1 = layoutAvatar.getLayoutParams();
        params1.height = width;
        layoutAvatar.setLayoutParams(params1);
        LayoutParams params2 = userAvatar.getLayoutParams();
        params2.height = width;
        userAvatar.setLayoutParams(params2);

        layout_photo = (RelativeLayout) v.findViewById(R.id.userinfo_photo);
        layout_travel = (RelativeLayout) v.findViewById(R.id.userinfo_travel);

        photosProgressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        HListView photoListView = (HListView) v.findViewById(R.id.user_photo_hlist);
        photoListView.setFocusable(false);
        photoAdapter = new UserPhotosAdapter(getActivity(), photoList);
        photoListView.setAdapter(photoAdapter);
        photoListView.setOnItemClickListener(new it.sephiroth.android.library.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(it.sephiroth.android.library.widget.AdapterView<?> adapterView, View view, int position, long l) {
                showPic(position);
            }
        });
    }

    /**
     * 在其他地方有调用
     *
     * @param user 用户实例
     */
    public void setUser(User user) {
        this.user = user;
        initData();
    }

    /**
     * 设置数据
     */
    private void initData() {
        if (requestingOthersData) {
            editOrChat.setVisibility(View.INVISIBLE);
            userEdit.setVisibility(View.VISIBLE);
            about.setText(String.format(getString(R.string.user_about), "Me"));
        } else {
            userSexAndAge.setVisibility(View.INVISIBLE);
            photoAdd.setVisibility(View.INVISIBLE);
            travelAdd.setVisibility(View.INVISIBLE);
            aboutEdit.setVisibility(View.INVISIBLE);
            jiantou0.setVisibility(View.INVISIBLE);
            jiantou1.setVisibility(View.INVISIBLE);
            jiantou2.setVisibility(View.INVISIBLE);
            jiantou3.setVisibility(View.INVISIBLE);
            line.setVisibility(View.GONE);
            tagsView.setTextColor(Color.BLACK);
            about.setText(String.format(getString(R.string.user_about), user.getNickname()));
        }
        local = user.getLocalGuidey();
        meal = user.getHomeMeal();
        stay = user.getHomeStay();
        // Non-Null Values
        userName.setText(user.getNickname());

        //设置登录和注册时间
        if (user.getLoginTime().isEmpty()) {
            time.setText(new StringBuilder().append("Member Since ").append(
                    TimeHelper.getInstance().parseTimestampToDate(user.getCreatedAt(), "UserInfo")));
        } else {
            time.setText(new StringBuilder().append("Member Since ")
                    .append(TimeHelper.getInstance().parseTimestampToDate(user.getCreatedAt(), "UserInfo"))
                    .append(" Active:")
                    .append(TimeHelper.getInstance().parseTimestampToTime(Long.valueOf(user.getLoginTime()) * 1000L)));
        }
        // TODO completely hide the view for each attribute if its not set
        // Possibly Null Values
        if (user.hasProperlyFormedBirthDate()) {
            if (!"".equals(user.getGender())) {
                if (requestingOthersData) {
                    switch (user.getGender()) {
                        case "Male":
                            userSexAndAge.setText(
                                    new StringBuilder().append(getString(R.string.userinfo_male)).append(", ").append(
                                            user.getUserAge()));
                            break;
                        case "Female":
                            userSexAndAge.setText(new StringBuilder().append(getString(R.string.userinfo_female)).append(", ").append(
                                    user.getUserAge()));
                            break;
                    }
                } else {
                    userSexAndAge.setVisibility(View.INVISIBLE);
                    layoutAge.setVisibility(View.VISIBLE);
                    layoutSex.setVisibility(View.VISIBLE);
                    userAge.setText(String.valueOf(user.getUserAge()));
                    userSex.setText(user.getGender());
                }
            }
        } else {
            if (!user.getGender().isEmpty()) {
                if (requestingOthersData) {
                    switch (user.getGender()) {
                        case "Male":
                            userSexAndAge.setText(getString(R.string.userinfo_male));
                            break;
                        case "Female":
                            userSexAndAge.setText(getString(R.string.userinfo_female));
                            break;
                    }
                } else {
                    userSexAndAge.setVisibility(View.INVISIBLE);
                    layoutAge.setVisibility(View.VISIBLE);
                    layoutSex.setVisibility(View.VISIBLE);
                    userAge.setText(String.valueOf(user.getUserAge()));
                    userSex.setText(user.getGender());
                }
            }
        }

        if (!requestingOthersData) {
            String name = user.getNickname();
            localTitle.setText(String.format(getString(R.string.album), name));
            localNothing1.setText(String.format(getString(R.string.album_content), name));
            localNothing2.setText(String.format(getString(R.string.album_content), name));

            travelTitle.setText(String.format(getString(R.string.travel_plans), name));
            travelNothing1.setText(String.format(getString(R.string.travel_content), name));
            travelNothing2.setText(String.format(getString(R.string.travel_content), name));

            aboutSelf1.setText(String.format(getString(R.string.about_self), name));
            aboutSelf2.setText(String.format(getString(R.string.about_self), name));
        }

        UIHelper uiHelper = UIHelper.getInstance();
        if (requestingOthersData && user.getSignature().isEmpty()) {
            layoutSelf.setVisibility(View.VISIBLE);
        } else if (requestingOthersData && !user.getSignature().isEmpty()) {
            layoutSelf.setVisibility(View.VISIBLE);
        } else if (!user.getSignature().isEmpty()) {
            layoutSelf.setVisibility(View.VISIBLE);
        }
        if (!requestingOthersData) {
            userSpeak.setHint("");
            userInsterest.setHint("");
            userInfoPlaces.setHint("");
            userInfoProfessional.setHint("");
        }
        //职业
        if (requestingOthersData) {
            userInfoProfessional.setOnClickListener(this);
            userInfoProfessional.setTextColor(Color.rgb(25, 143, 153));
        } else if (!user.getProfession().isEmpty()) {
            userInfoProfessional.setHint("");
            userInfoProfessional.setEnabled(false);
        }
        userInfoProfessional.setText(user.getProfession());
        //speak
        if (requestingOthersData) {
            userSpeak.setOnClickListener(this);
            layoutSpeak.setVisibility(View.VISIBLE);
            userSpeak.setTextColor(Color.rgb(25, 143, 153));
        } else if (!user.getSpeak().isEmpty()) {
            userSpeak.setHint("");
            userSpeak.setEnabled(false);
            layoutSpeak.setVisibility(View.VISIBLE);
        }
        userSpeak.setText(user.getSpeak());

        String str2;
        if (requestingOthersData) {
            userInsterest.setTextColor(Color.rgb(25, 143, 153));
            str2 = user.getInterests().toString().substring(1, user.getInterests().toString().length() - 1);
            userInsterest.setText(str2);
            userInsterest.setOnClickListener(this);
        } else if (user.getInterests().size() > 0) {
            if (!user.getInterests().get(0).isEmpty()) {
                userInsterest.setHint("");
                str2 = user.getInterests().toString().substring(1, user.getInterests().toString().length() - 1);
                userInsterest.setText(str2);
            } else {
                userInsterest.setHint("");
            }
        }
        if (requestingOthersData) {
            userInfoPlaces.setOnClickListener(this);
            userInfoPlaces.setTextColor(Color.rgb(25, 143, 153));
        } else if (!user.getVisited().isEmpty()) {
            userInfoPlaces.setHint("");
            userInfoPlaces.setEnabled(false);
        }
        userIdAndPlace.setText(new StringBuilder().append("ID:").append(user.getUsername()));
        if (!user.getLocation().isEmpty()) {
            userIdAndPlace.setText(new StringBuilder().
                    append(userIdAndPlace.getText()).append(", ").append(user.getLocation()));
        }
        if (user.getHomeMeal()) {
            userMeal.setVisibility(View.VISIBLE);
            meal = true;
            availableMeal.setTextColor(Color.rgb(25, 143, 153));
            availableMeal.setCompoundDrawablesWithIntrinsicBounds(null, null, getActivity().getResources().getDrawable(R.drawable.personal_done_48dp), null);
        }
        if (user.getHomeStay()) {
            userStay.setVisibility(View.VISIBLE);
            stay = true;
            availableStay.setTextColor(Color.rgb(25, 143, 153));
            availableStay.setCompoundDrawablesWithIntrinsicBounds(null, null, getActivity().getResources().getDrawable(R.drawable.personal_done_48dp), null);
        }
        if (user.getLocalGuidey()) {
            userLocal.setVisibility(View.VISIBLE);
            local = true;
            availableLocal.setTextColor(Color.rgb(25, 143, 153));
            availableLocal.setCompoundDrawablesWithIntrinsicBounds(null, null, getActivity().getResources().getDrawable(R.drawable.personal_done_48dp), null);
        }
        if (user.getRelation() == 1) {
            userAddFollow.setTextColor(Color.rgb(255, 224, 130));
            userAddFollow.setText(getString(R.string.user_unfollow));
        } else {
            userAddFollow.setText(getString(R.string.user_follow));
        }

        String countryCode = JsonHelper.getInstance().getCodeFromCountry(user.getCountry());
        uiHelper.imageViewSetPossiblyEmptyUrl(getActivity(), userAvatar, user.getAvatar(), "original");
        ImageHelper.getInstance().loadFlag(flag, countryCode, 1);
        if (!user.getSignature().isEmpty()) {
            layout_tag.setVisibility(View.GONE);
            tagsView.setText(user.getSignature());
        }
        new LoadUserPhotos().execute();
        new getUserExperience().execute();
    }

    @Override
    public void onResume() {
        super.onResume();
        new LoadUserFollowing().execute();
        new LoadFollowUser().execute();

        new LoadUserReferences(user.getId(), new LoadUserReferences.Callback() {
            @Override
            public void onSuccess(ArrayList<Reference> list) {
                referenceList.clear();
                referenceList.addAll(list);
                for (int i = 0, j = referenceList.size(); i < j; i++) {
                    try {
                        User user = ServerHelper.getInstance().getUserInfo(referenceList.get(i).getUserSrcId());
                        CacheHelper.getInstance().addUserInfo(user);
                    } catch (Exception e2) {
                        Log.e(LOG_TAG, "Inner Exception caught: " + e2.toString());
                    }
                }
                userReference.setText(String.valueOf(referenceList.size()));
                reference.setClickable(true);
            }

            @Override
            public void onFail() {
                Toast.makeText(getActivity(), getString(R.string.fail_get_reference), Toast.LENGTH_LONG).show();
            }
        }).execute();
        MobclickAgent.onPageStart("UserInfoFragment");
    }

    @Override
    public void onClick(View v) {
        Intent mIntent;
        switch (v.getId()) {
            case R.id.userinfo_professional_info:
                SelectProfesionalDialog.newInstance(getActivity(), user, userInfoProfessional).show(getFragmentManager(), "professional");
                break;
            case R.id.userinfo_interest_info:
                PhotoTagsSelectDialog.newInstance("interest", getActivity(), user, handler).show(getFragmentManager(), "interest");
                break;
            case R.id.travel_add:
                mIntent = new Intent(getActivity(), AddTravelActivity.class);
                startActivityForResult(mIntent, ADD_TRAVEL);
                break;
            case R.id.local_add:
                mIntent = new Intent(getActivity(), AddPhotosActivity.class);
                startActivityForResult(mIntent, ADD_PHOTOS);
                break;
            case R.id.about_edit:
                if (editOrOk) {
                    editOrOk = false;
                    if (tagsView.getText().toString().isEmpty()) {
                        layout_tag.setVisibility(View.VISIBLE);
                    }
                    line.setVisibility(View.GONE);
                    tagsView.setEnabled(false);
                    tagsView.setTextColor(Color.BLACK);
                    aboutEdit.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.personal_edit_18dp));
                    new UpdateUserInfo("signature").execute();
                } else {
                    aboutEdit.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.personal_done_24dp));
                    layout_tag.setVisibility(View.GONE);
                    tagsView.setEnabled(true);
                    line.setVisibility(View.VISIBLE);
                    editOrOk = true;
                }
                break;
            case R.id.userinfo_speak_info:
                SelectSpeakDialog.newInstance(getActivity(), user, userSpeak, handler1).show(getFragmentManager(), "speak");
                break;
            case R.id.layout_follow:
                MobclickAgent.onEvent(getActivity(), "members_follower");
                mIntent = new Intent(getActivity(), UserListActivity.class);
                mIntent.putParcelableArrayListExtra(UserListActivity.USER_LIST, userFollowingList);
                mIntent.putExtra(UserListActivity.PAGE_TITLE, getString(R.string.user_following));
                startActivity(mIntent);
                break;
            case R.id.layout_following:
                MobclickAgent.onEvent(getActivity(), "members_following");
                mIntent = new Intent(getActivity(), UserListActivity.class);
                mIntent.putParcelableArrayListExtra(UserListActivity.USER_LIST, userFollowList);
                mIntent.putExtra(UserListActivity.PAGE_TITLE, getString(R.string.user_followers));
                startActivity(mIntent);
                break;
            case R.id.userinfo_add_reference:
                MobclickAgent.onEvent(getActivity(), "members_add_reference");
                mIntent = new Intent(getActivity(), ReferenceActivity.class);
                mIntent.putExtra(TARGET_USER_ID, user.getId());
                mIntent.putExtra(TARGET_USER_NAME, user.getNickname());
                mIntent.putParcelableArrayListExtra(REFERENCES, referenceList);
                mIntent.putExtra("addReference", 1);
                startActivityForResult(mIntent, EDIT_REFERENCES);
                break;
            case R.id.layout_reference:
                MobclickAgent.onEvent(getActivity(), "members_reference");
                mIntent = new Intent(getActivity(), ReferenceActivity.class);
                mIntent.putExtra(TARGET_USER_ID, user.getId());
                mIntent.putExtra(TARGET_USER_NAME, user.getNickname());
                mIntent.putParcelableArrayListExtra(REFERENCES, referenceList);
                startActivityForResult(mIntent, EDIT_REFERENCES);
                break;
            case R.id.userinfo_add_follow:
                new ChangeRelation().execute();
                break;
            case R.id.userinfo_chat:
                MobclickAgent.onEvent(getActivity(), "members_chat");
                Intent chatIntent = new Intent(getActivity(), ChatActivity.class);
                chatIntent.putExtra("username", user.getUsername());
                chatIntent.putExtra(StringConstant.nicknameStr, user.getNickname());
                chatIntent.putExtra("chatType", ChatActivity.CHATTYPE_SINGLE);
                startActivity(chatIntent);
                break;
            case R.id.userinfo_edit:
                Intent editIntent = new Intent(getActivity(), EditInfoActivity.class);
                startActivityForResult(editIntent, EDIT_USER);
                break;
            case R.id.layout_available_local:
                if (!local) {
                    user.setLocalGuide(false);
                    local = true;
                    userLocal.setVisibility(View.VISIBLE);
                    availableLocal.setTextColor(Color.rgb(25, 143, 153));
                    availableLocal.setCompoundDrawablesWithIntrinsicBounds(null, null, getActivity().getResources().getDrawable(R.drawable.personal_done_48dp), null);
                } else {
                    user.setLocalGuide(true);
                    local = false;
                    userLocal.setVisibility(View.INVISIBLE);
                    availableLocal.setTextColor(Color.rgb(171, 171, 171));
                    availableLocal.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                }
                new UpdateUserInfo("local").execute();
                break;
            case R.id.layout_available_meal:
                if (!meal) {
                    user.setHomeMeal(false);
                    meal = true;
                    userMeal.setVisibility(View.VISIBLE);
                    availableMeal.setTextColor(Color.rgb(25, 143, 153));
                    availableMeal.setCompoundDrawablesWithIntrinsicBounds(null, null, getActivity().getResources().getDrawable(R.drawable.personal_done_48dp), null);
                } else {
                    user.setHomeMeal(true);
                    meal = false;
                    userMeal.setVisibility(View.INVISIBLE);
                    availableMeal.setTextColor(Color.rgb(171, 171, 171));
                    availableMeal.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                }
                new UpdateUserInfo("meal").execute();
                break;
            case R.id.layout_available_stay:
                if (!stay) {
                    user.setHomeStay(false);
                    stay = true;
                    userStay.setVisibility(View.VISIBLE);
                    availableStay.setTextColor(Color.rgb(25, 143, 153));
                    availableStay.setCompoundDrawablesWithIntrinsicBounds(null, null, getActivity().getResources().getDrawable(R.drawable.personal_done_48dp), null);
                } else {
                    user.setHomeStay(true);
                    stay = false;
                    userStay.setVisibility(View.INVISIBLE);
                    availableStay.setTextColor(Color.rgb(171, 171, 171));
                    availableStay.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                }
                new UpdateUserInfo("stay").execute();
                break;
            case R.id.userinfo_places_info:
                PlacesDialog.newInstance("places", getActivity(), user, userInfoPlaces, handler2, placesList).show(getFragmentManager(), "places");
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case EDIT_USER:
                //FIXME The check for result code is commented out so that even when we change avatar, we still reload user info
                user = CacheHelper.getInstance().getSelfInfo();
                setUser(user);
                break;
            case EDIT_REFERENCES:
                if (resultCode == ReferenceActivity.RESULT_OK) {
                    referenceList.clear();
                    referenceList.addAll(data.<Reference>getParcelableArrayListExtra(ReferenceActivity.INTENT_USER_REFERENCE));
                    userReference.setText(String.valueOf(referenceList.size()));
                }
                break;
            case ADD_PHOTOS:
                new LoadUserPhotos().execute();
                break;
            case ADD_TRAVEL:
                if (data.hasExtra("Travel")) {
                    travelList.add(0, data.<Travel>getParcelableExtra("Travel"));
                    initData();
                }
                break;
            case SEEALL_TRAVEL:
                travelList.clear();
                travelList.addAll(data.<Travel>getParcelableArrayListExtra("TravelList"));
                updateTravel(travelList);
                break;
            case 888:
                if (data.<Photo>getParcelableArrayListExtra(PhotoViewActivity.PHOTO_LIST).isEmpty()) {
                    photoList.clear();
                    photoAdapter.notifyDataSetChanged();
                    layout_photo.setVisibility(View.VISIBLE);
                } else {
                    photoList.clear();
                    photoList.addAll(data.<Photo>getParcelableArrayListExtra(PhotoViewActivity.PHOTO_LIST));
                    photoAdapter.notifyDataSetChanged();
                    layout_photo.setVisibility(View.GONE);
                }
                break;
        }
    }

    private void showPic(int position) {
        Intent intent = new Intent(getActivity(),
                PhotoViewActivity.class);
        intent.putParcelableArrayListExtra(PhotoViewActivity.PHOTO_LIST, photoList);
        intent.putExtra(PhotoViewActivity.PHOTO_POSITION, position);
        if (!user.getId().equals(CacheHelper.getInstance().getSelfInfo().getId())) {
            intent.putExtra(PhotoViewActivity.OTHERS_PHOTOS, true);
        } else {
            intent.putExtra(PhotoViewActivity.OTHERS_PHOTOS, false);
        }
        startActivityForResult(intent, 888);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("UserInfoFragment");
    }

    protected void updateTravel(ArrayList<Travel> list) {
        travelContent.removeAllViews();
        if (list.size() > 0) {
            Collections.reverse(list);
        }
        View view;
        int num;//标识旅行经历的个数
        if (list.size() == 0) {
            layout_travel.setVisibility(View.VISIBLE);
            num = 0;
        } else if (list.size() <= 3) {
            layout_travel.setVisibility(View.INVISIBLE);
            num = list.size();
        } else {
            layout_travel.setVisibility(View.INVISIBLE);
            num = 4;
        }
        for (int i = 0; i < num; i++) {
            view = getTravelView(i);
            if (i < 3) {
                travelContent.addView(view);
            } else if (i == 3) {
                View seeAll = getActivity().getLayoutInflater().inflate(R.layout.row_travel_see_all, travelContent, false);
                final OnClickListener seeAllClickListener = new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), ShowTravelActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("user", user);
                        intent.putExtras(bundle);
                        intent.putParcelableArrayListExtra("TravelList", travelList);
                        startActivityForResult(intent, SEEALL_TRAVEL);
                    }
                };
                seeAll.setOnClickListener(seeAllClickListener);
                travelContent.addView(seeAll);
            }
        }
    }

    public View getTravelView(int position) {
        return getTravelView(travelList.get(position));
    }

    private View getTravelView(final Travel travel) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.row_travel_include, travelContent, false);
        TextView endTime = (TextView) view.findViewById(R.id.travel_time_end);
        TextView startTime = (TextView) view.findViewById(R.id.travel_time_start);
        TextView location = (TextView) view.findViewById(R.id.travel_title);

        endTime.setText(JsonHelper.getInstance().parseTimestamp(travel.getEndTime(), 2));
        startTime.setText(JsonHelper.getInstance().parseTimestamp(travel.getStartTime(), 2));
        location.setText(travel.getLocation());
        if (requestingOthersData) {
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), ShowTravelActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("user", user);
                    intent.putExtras(bundle);
                    intent.putParcelableArrayListExtra("TravelList", travelList);
                    startActivityForResult(intent, SEEALL_TRAVEL);
                }
            });
        }
        return view;
    }

    //TODO 提交数据更新
    private class UpdateUserInfo extends cn.lingox.android.video.util.AsyncTask<Void, String, Boolean> {
        private ProgressDialog pd;
        private String flag;

        public UpdateUserInfo(String flg) {
            pd = new ProgressDialog(getActivity());
            pd.setCanceledOnTouchOutside(false);
            pd.setCancelable(false);
            flag = flg;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd.setMessage(getString(R.string.updating_account_info));
            pd.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            HashMap<String, String> updateParams = new HashMap<>();
            updateParams.put(StringConstant.userIdStr, user.getId());
            switch (flag) {
                case "local":
                    updateParams.put(StringConstant.localStr, String.valueOf(local));
                    break;
                case "meal":
                    updateParams.put(StringConstant.mealStr, String.valueOf(meal));
                    break;
                case "stay":
                    updateParams.put(StringConstant.stayStr, String.valueOf(stay));
                    break;
                case "signature"://存储标签
                    user.setSignature(tagsView.getText().toString());
                    updateParams.put(StringConstant.signatureStr, user.getSignature());
                    break;
                case "speak":
                    updateParams.put(StringConstant.speakStr, user.getSpeak());
                    break;
                case "interest":
                    updateParams.put(StringConstant.interestsStr, JsonHelper.getInstance().getInterestsJson(userInsterest.getText().toString()));
                    break;
                case "places":
                    updateParams.put(StringConstant.visitedStr, user.getVisited());
                    break;
            }
            try {
                User returnUser = ServerHelper.getInstance().updateUserInfo(updateParams);

                CacheHelper.getInstance().setSelfInfo(returnUser);
                return true;
            } catch (final Exception e) {
                Log.e(LOG_TAG, "UpdateUserInfo exception caught: " + e.toString());
                publishProgress(null, "Error updating account information");
                return false;
            }
        }

        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            if (values[0] != null)
                pd.setMessage(values[0]);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            pd.dismiss();
            if (!success) {
                Toast.makeText(getActivity(), "Failure to submit", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * 获取旅行记录
     */
    private class getUserExperience extends AsyncTask<Void, String, Boolean> {
        HashMap<String, String> updateParams = new HashMap<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            travelList = new ArrayList<>();
            updateParams.put(StringConstant.userIdStr, user.getId());
            placesList.clear();
            list.clear();
            if (user.getVisited().length() > 0) {
                String[] s = user.getVisited().split(",");
                for (String value : s) {
                    placesList.add(value.trim());
                    list.add(value.trim());
                }
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                travelList = ServerHelper.getInstance().getExperiences(user.getId());
                long time = System.currentTimeMillis() / 1000L;
                for (int i = travelList.size() - 1; i >= 0; i--) {
                    if (travelList.get(i).getEndTime() < time) {
                        for (int j = 0, a = placesList.size(); j < a; j++) {
                            if (placesList.get(j).contentEquals(travelList.get(i).getCountry())) {
                                break;
                            } else if (j == placesList.size() - 1) {
                                placesList.add(travelList.get(i).getCountry());
                                list.add(travelList.get(i).getCountry().trim());
                            }
                        }
                        ServerHelper.getInstance().deleteExperiences(travelList.get(i).getId());
                        travelList.remove(i);
                    }
                }
                updateParams.put(StringConstant.visitedStr, placesList.toString().replace("[", "").replace("]", ""));
                ServerHelper.getInstance().updateUserInfo(updateParams);
                return true;
            } catch (final Exception e) {
                Log.e(LOG_TAG, "getUserExperience caught: " + e.toString());
                publishProgress(null, "Error updating account information");
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (success) {
                userInfoPlaces.setText(list.toString().replace("[", "").replace("]", ""));
                user.setVisited(placesList.toString().replace("[", "").replace("]", ""));
                updateTravel(travelList);
            }
        }
    }

    private class ChangeRelation extends AsyncTask<Void, String, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            userAddFollow.setClickable(false);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
//            Log.d(LOG_TAG, "ChangeRelation started");
            final int newRelationCode = user.getRelation() == 1 ? 2 : 1;
            try {
                // If we are already Following them, set relationCode to unfollow and vice versa
                user.setRelation(newRelationCode);
                ServerHelper.getInstance().userRelationChange(CacheHelper.getInstance().getSelfInfo().getId(), user.getId(), newRelationCode);
                CacheHelper.getInstance().addUserInfo(user);
                return true;
            } catch (final Exception e) {
                Log.e(LOG_TAG, "ChangeRelation exception caught: " + e.toString());
                if (newRelationCode == 1) {
                    publishProgress(null, getString(R.string.fail_follow));
                } else {
                    publishProgress(null, getString(R.string.fail_unfollow));
                }
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            if (values[1] != null)
                Toast.makeText(getActivity(), values[1], Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);

            if (success) {
                try {
                    if (user.getRelation() == 1) {
                        MobclickAgent.onEvent(getActivity(), "members_follow", new HashMap<String, String>().put("follow", "unfollow"));

                        CacheHelper.getInstance().addContact(user);
                        new Thread() {
                            @Override
                            public void run() {
                                publishProgress(null, getString(R.string.follow) + " " + user.getNickname() + "!");
                            }
                        }.start();
                        userAddFollow.setTextColor(Color.rgb(255, 224, 130));
                        userAddFollow.setText(getString(R.string.user_unfollow));
                    } else {
                        MobclickAgent.onEvent(getActivity(), "members_follow", new HashMap<String, String>().put("follow", "follow"));
                        userAddFollow.setTextColor(Color.rgb(255, 255, 255));
                        userAddFollow.setText(getString(R.string.user_follow));
                        CacheHelper.getInstance().removeContact(user);
                        new Thread() {
                            @Override
                            public void run() {
                                publishProgress(null, getString(R.string.unfollow) + user.getNickname() + "!");
                            }
                        }.start();
                    }

                    new LoadUserFollowing().execute();
                    new LoadFollowUser().execute();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            userAddFollow.setClickable(true);
        }
    }

    /**
     * 下载用户相册
     */
    private class LoadUserPhotos extends AsyncTask<Void, String, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            photosProgressBar.setVisibility(View.VISIBLE);
            photoList.clear();
            photoAdapter.notifyDataSetChanged();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                photoList.addAll(ServerHelper.getInstance()
                        .getUsersPhotos(user.getId()));
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (isAdded()) {
                if (success) {
                    photoAdapter.notifyDataSetChanged();
                    if (photoList.isEmpty()) {
                        layout_photo.setVisibility(View.VISIBLE);
                    } else {
                        layout_photo.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(getActivity(),
                            getString(R.string.fail_get_photo),
                            Toast.LENGTH_LONG).show();
                }
            }
            photosProgressBar.setVisibility(View.GONE);
        }
    }

    private class LoadUserFollowing extends AsyncTask<Void, String, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            userFollowingList.clear();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                userFollowingList.addAll(ServerHelper.getInstance().getContactList(user.getId()));
                return true;
            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception caught: " + e.toString());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (isAdded()) {
                if (success) {
                    userFollowing.setText(String.valueOf(userFollowingList.size()));
                    following.setClickable(true);
                } else {
                    Toast.makeText(getActivity(), "Failed to get User's Contacts", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private class LoadFollowUser extends AsyncTask<Void, String, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            userFollowList.clear();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                userFollowList.addAll(ServerHelper.getInstance().getUserFollowing(user.getId()));
                return true;
            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception caught: " + e.toString());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (isAdded()) {
                if (success) {
                    userFollow.setText(String.valueOf(userFollowList.size()));
                    follow.setClickable(true);
                } else {
                    Toast.makeText(getActivity(), "Failed to get User's Followers", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
