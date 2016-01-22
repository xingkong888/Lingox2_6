package cn.lingox.android.activity;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.chat.EMGroupManager;
import com.easemob.exceptions.EaseMobException;
import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.FeedbackAgent;
import com.umeng.update.UmengUpdateAgent;

import java.util.ArrayList;

import cn.lingox.android.R;
import cn.lingox.android.adapter.MySpinnerAdapter;
import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.entity.Path;
import cn.lingox.android.entity.TravelEntity;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.UIHelper;
import cn.lingox.android.task.CheckForUpdates;
import cn.lingox.android.utils.FileUtil;
import cn.lingox.android.utils.SkipDialog;


public class MainActivity extends ActionBarActivity implements OnClickListener, ChatFragment.ShowNum {
    // REQUEST CODES
    public static final int REQUEST_CODE_SETTINGS = 101;
    // RESULT CODES
    public static final int RESULT_CODE_LOGOUT = 201;
    public static final int RESULT_CODE_RESET_LANGUAGE = 202;
    //travel的添加的请求码
    public static final int ADD_TRAVEL = 1101;
    //local的添加的请求码
    public static final int ADD_PATH = 1102;

    public static final String SEARCH = "Search";
    private static final String LOG_TAG = "MainActivity";
    private static MainActivity mainActivity;
    //两次点击返回键的时间间隔
    private long clickTime;
    // UI Elements
    private LocalFragment localFragment;
    private TravelFragment travelFragment;
    // UI Elements
    private ImageView photo;
    //    private ImageView flag;
    private MainActivityFragmentAdapter tabAdapter;
    private ViewPager viewPager;
    private DrawerLayout sideDrawers;
    private ActionBarDrawerToggle sideDrawerToggle;
    private TextView num;
    //标识右侧边栏是否打开；true打开、false未打开
    private boolean rightSildOpen = false;
    private LinearLayout rightSild;
    //用于添加新的体验
    private ImageView add;
    private PopupWindow popWin;
    //自定义下拉选择
    private TextView showLocation;//显示选中地区
    private ImageView dropDown;//箭头旋转动画
    private ListView listView;//显示所有地区
    //welcome
    private LinearLayout welcome;
    //表示viewpager当前页
    private int page = 0;

    public static MainActivity getObj() {
        return mainActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UmengUpdateAgent.update(this);
        /****************获取屏幕的宽度************/
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        LingoXApplication.getInstance().setWidth(dm.widthPixels);
        /************************************/
        mainActivity = this;
        initView();
        if (LingoXApplication.getInstance().getSkip()) {
            SkipDialog.getDialog(this).show();
        }
    }

    @Override
    protected void onStart() {
        setAvatar();
        super.onStart();
    }

    /**
     * 实例化控件
     */
    private void initView() {
        setContentView(R.layout.activity_main);
        /********************************* TOOLBAR **********************************/
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_activity_toolbar);
        toolbar.setTitle("");//默认使用清单文件中的lable
        setSupportActionBar(toolbar);
/***********************************************************************************/
        //未读信息数据展示
        findViewById(R.id.show_num).setOnClickListener(this);
        num = (TextView) findViewById(R.id.num);
        sideDrawers = (DrawerLayout) findViewById(R.id.drawer_layout);
        sideDrawerToggle = new ActionBarDrawerToggle(
                this, sideDrawers, toolbar, R.string.drawer_open, R.string.drawer_closed) {

            @Override
            public void onDrawerOpened(View drawerView) {
                //打开抽屉----drawerView为要打开的抽屉
                switch (drawerView.getId()) {
                    case R.id.left_drawer://左侧边
                        if (rightSildOpen) {
                            sideDrawers.closeDrawer(rightSild);
                            rightSildOpen = false;
                        }
                        break;
                    case R.id.right_drawer://右侧边
                        rightSildOpen = true;
                        break;
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                //关闭抽屉----drawerView为要关闭的抽屉
                switch (drawerView.getId()) {
                    case R.id.left_drawer://左侧边
                        super.onDrawerClosed(drawerView);
                        break;
                    case R.id.right_drawer://右侧边
                        rightSildOpen = false;
                        break;
                }
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                if (drawerView.getId() == R.id.left_drawer) {
                    super.onDrawerSlide(drawerView, slideOffset);
                }
            }
        };
        sideDrawers.setDrawerListener(sideDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        /*********************** LEFT MENU *******************************/
        //follow/following
        findViewById(R.id.layout_contact_list).setOnClickListener(this);
        //favourite收藏
        findViewById(R.id.layout_favourite).setOnClickListener(this);
        //setting
        findViewById(R.id.layout_set).setOnClickListener(this);
        //feedback
        findViewById(R.id.layout_feedback).setOnClickListener(this);
        //info
        findViewById(R.id.layout_info).setOnClickListener(this);
        //头像
        findViewById(R.id.layout_top).setOnClickListener(this);
        photo = (ImageView) findViewById(R.id.avatar_info);
        photo.setOnClickListener(this);
//        //国旗
//        flag = (ImageView) findViewById(R.id.iv_flag);

        /*********************************************************************/
        /*************************RIGHT MENU*********************************/
        rightSild = (LinearLayout) findViewById(R.id.right_drawer);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        final Fragment mContent = new ChatFragment();
        ft.add(R.id.reply, mContent);
        ft.commit();
        /***********************************************************/
        /************************ MAIN VIEW ********************/
        if (getIntent().hasExtra("welcome")) {
            welcome = (LinearLayout) findViewById(R.id.welcome);
            welcome.setVisibility(View.VISIBLE);
            //选择local
            findViewById(R.id.welcome_local).setOnClickListener(this);
            //选择travel
            findViewById(R.id.welcome_travel).setOnClickListener(this);
            //back
            findViewById(R.id.welcome_back).setOnClickListener(this);
        }
/****************************spinner*******************************/
        findViewById(R.id.my_spinner).setOnClickListener(this);
        showLocation = (TextView) findViewById(R.id.spinner_select_location);
        dropDown = (ImageView) findViewById(R.id.spinner_drop_down);
        listView = (ListView) findViewById(R.id.spinner_lv);
        final ArrayList<String> list = new ArrayList<>();
//        list.add("All");
        list.add("Beijing");
        list.add("Shanghai");
        list.add("Guangzhou");
        showLocation.setText(list.get(0));
        MySpinnerAdapter adapter = new MySpinnerAdapter(this, list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (page) {
                    case 0://local
                        localFragment.refreshList(position);
                        break;
                    case 1://travel
                        travelFragment.refreshList(position);
                        break;
                }
                showLocation.setText(list.get(position));
                dropDown.setImageResource(R.drawable.drop_down_down);
                listView.setVisibility(View.GONE);
            }
        });
/*****************************************************************/
        add = (ImageView) findViewById(R.id.add_experience);
        add.setOnClickListener(this);

        localFragment = new LocalFragment();
        travelFragment = new TravelFragment();
        tabAdapter = new MainActivityFragmentAdapter(fm);
        viewPager = (ViewPager) findViewById(R.id.fragment_container);
        viewPager.setAdapter(tabAdapter);
        // (Number of fragments - 1) This prevents the edge tabs being recreated
        // 除当前页外，预加载及保留的页面数   viewPager.setOffscreenPageLimit(2);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setCurrentItem(0);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                page = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        chooseExperience();
    }

    // 这个是自定义打开抽屉的方法，可以在需要的时候调用，
    // 定义两个整型变量作为参数来决定打开哪一个抽屉
    //---用于控制右边栏
    private void rightDrawer() {
        if (!rightSildOpen) {
            //打开侧边栏
            rightSildOpen = false;
            sideDrawers.openDrawer(rightSild);
        } else {
            //关闭侧边栏
            rightSildOpen = true;
            sideDrawers.closeDrawer(rightSild);
        }
    }

    /**
     * 设置数据
     */
    private void initDate() {
        //如果是登录进来的，显示名字，否则，显示为空
        if (!LingoXApplication.getInstance().getSkip()) {
            //用户昵称
            ((TextView) findViewById(R.id.tv_nickname)).setText(CacheHelper.getInstance().getSelfInfo().getNickname());
            //用户地址
            ((TextView) findViewById(R.id.tv_user_add)).setText(
                    CacheHelper.getInstance().getSelfInfo().getProvince().isEmpty() ?
                            CacheHelper.getInstance().getSelfInfo().getProvince() + ", " + CacheHelper.getInstance().getSelfInfo().getCountry() :
                            CacheHelper.getInstance().getSelfInfo().getCountry());
        } else {
            ((TextView) findViewById(R.id.tv_nickname)).setText("");
            ((TextView) findViewById(R.id.tv_user_add)).setText("");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_contact_list://follow/following
                if (!LingoXApplication.getInstance().getSkip()) {
                    Intent contactListIntent = new Intent(this, ContactsActivity.class);
                    startActivity(contactListIntent);
                } else {
                    SkipDialog.getDialog(this).show();
                }
                break;
            case R.id.layout_favourite://收藏
                if (!LingoXApplication.getInstance().getSkip()) {
                    Intent intent = new Intent(this, FavouriteActivity.class);
                    startActivity(intent);
                } else {
                    SkipDialog.getDialog(this).show();
                }
                break;

            case R.id.layout_set://设置
                if (!LingoXApplication.getInstance().getSkip()) {
                    Intent settingsIntent = new Intent(this, SettingsActivity.class);
                    startActivityForResult(settingsIntent, MainActivity.REQUEST_CODE_SETTINGS);
                } else {
                    SkipDialog.getDialog(this).show();
                }
                break;
            case R.id.layout_feedback://反馈
                FeedbackAgent agent = new FeedbackAgent(this);
                agent.startFeedbackActivity();
                break;
            case R.id.layout_info://about us
                Uri uri = Uri.parse("http://lingox.cn");
                Intent aboutUsIntent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(aboutUsIntent);
                break;
            case R.id.layout_top://头像
                if (!LingoXApplication.getInstance().getSkip()) {
                    Intent userInfoIntent = new Intent(this, UserInfoActivity.class);
                    userInfoIntent.putExtra(UserInfoActivity.INTENT_USER_ID, CacheHelper.getInstance().getSelfInfo().getId());
                    startActivity(userInfoIntent);
                    /*activity跳转动画
                    原因：暂时只有这一处添加了动画
                    overridePendingTransition(R.anim.push_top_in, R.anim.push_bottom_out);
                    */
                } else {
                    SkipDialog.getDialog(this).show();
                }
                break;
            case R.id.show_num://展示未读信息数量
                //打开右侧栏
                rightDrawer();
                break;
            case R.id.add_experience://添加体验
                //展示PopupWindow
                popWin.showAtLocation(add, Gravity.BOTTOM, 0, 0);
                break;
            case R.id.welcome_local://选择local
                welcome.setVisibility(View.GONE);
                break;
            case R.id.welcome_travel://选择travel
                viewPager.setCurrentItem(1);
                welcome.setVisibility(View.GONE);
                break;
            case R.id.welcome_back://back
                welcome.setVisibility(View.GONE);
                break;
            case R.id.my_spinner://自定义spinner
                listView.setVisibility(View.VISIBLE);
                dropDown.setImageResource(R.drawable.drop_down_up);
                break;
        }
    }

    /**
     * 选择发布local或travel
     */
    private void chooseExperience() {
        View view = LinearLayout.inflate(this, R.layout.experience_choose, null);

        view.findViewById(R.id.t1).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LocalEditActivity.class);
                startActivityForResult(intent, ADD_PATH);
                popWin.dismiss();
            }
        });

        view.findViewById(R.id.t2).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TravelEditActivity.class);
                startActivityForResult(intent, ADD_TRAVEL);
                popWin.dismiss();
            }
        });

        popWin = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, false);
        // 需要设置一下此参数，点击外边可消失
        popWin.setBackgroundDrawable(new BitmapDrawable());
        // 设置点击窗口外边窗口消失
        popWin.setOutsideTouchable(true);
        // 设置此参数获得焦点，否则无法点击
        popWin.setFocusable(true);
        //设置弹出、淡出的动画效果
        popWin.setAnimationStyle(R.style.mypopwindow_anim_style);
    }

    /**
     * 设置头像及国旗
     */
    private void setAvatar() {
        if (!LingoXApplication.getInstance().getSkip()) {
            tabAdapter.notifyDataSetChanged();
            UIHelper.getInstance().imageViewSetPossiblyEmptyUrl(MainActivity.this, photo,
                    CacheHelper.getInstance().getSelfInfo().getAvatar(), "circular");
//            ImageHelper.getInstance().loadFlag(flag, JsonHelper.getInstance().getCodeFromCountry(
//                    CacheHelper.getInstance().getSelfInfo().getCountry()), 2);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SETTINGS:
                switch (resultCode) {
                    case RESULT_CODE_LOGOUT://退出登录
                        Intent logoutIntent = new Intent(this, LoginActivity.class);
                        logoutIntent.putExtra(LoginActivity.LOGOUT_REQUESTED, true);
                        startActivity(logoutIntent);
                        finish();
                        break;
                    case RESULT_CODE_RESET_LANGUAGE://重置语言----已停用
                        Intent resetLanguage = new Intent(this, SplashActivity.class);
                        startActivity(resetLanguage);
                        finish();
                        break;
                }
                break;
            case ADD_TRAVEL://添加新的travel的问题
                if (data.hasExtra(TravelEditActivity.TRAVEL_CREATE)) {
                    travelFragment.refershView((TravelEntity) data.getParcelableExtra(TravelEditActivity.TRAVEL_CREATE));
                }
                break;
            case ADD_PATH://添加新的local的体验
                if (resultCode == LocalEditActivity.RESULT_OK && data.hasExtra(LocalEditActivity.ADDED_PATH)) {
                    final Path path = data.getParcelableExtra(LocalEditActivity.ADDED_PATH);
                    localFragment.addPath(path);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                //创建群聊id
                                EMGroupManager.getInstance().createPublicGroup(path.getTitle().equals("") ? CacheHelper.getInstance().getSelfInfo().getNickname() :
                                        path.getTitle(), path.getText(), null, true);
                            } catch (EaseMobException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
                break;
            case LocalFragment.EDIT_PATH://修改了数据
                if (resultCode == LocalViewActivity.RESULT_OK) {
                    if (data.hasExtra(LocalViewActivity.EDITED_PATH)) {
                        localFragment.modifyPath((Path) data.getParcelableExtra(LocalViewActivity.EDITED_PATH));
                    } else if (data.hasExtra(LocalViewActivity.DELETED_PATH)) {
                        localFragment.removePath((Path) data.getParcelableExtra(LocalViewActivity.DELETED_PATH));
                    }
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        new CheckForUpdates(this).execute();
        MobclickAgent.onResume(this);
        MobclickAgent.onPageStart("MainActivity");
        initDate();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("MainActivity");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        sideDrawerToggle.syncState();
        super.onPostCreate(savedInstanceState);
    }

    /**
     * 键盘按键事件
     *
     * @param keyCode 按键码
     * @param event   事件
     * @return boolean
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (sideDrawers.isDrawerOpen(Gravity.START)) {
                sideDrawers.closeDrawers();
            } else if (sideDrawers.isDrawerOpen(Gravity.END)) {
                sideDrawers.closeDrawers();
            } else if (popWin.isShowing()) {
                popWin.dismiss();
            } else if (listView.getVisibility() == View.VISIBLE) {
                dropDown.setImageResource(R.drawable.drop_down_down);
                listView.setVisibility(View.GONE);
            } else {
                exitSystem();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 退出应用
     */
    private void exitSystem() {
        if ((System.currentTimeMillis() - clickTime) > 2000) {
            //再次按下返回按钮,退出程序
            Toast.makeText(getApplicationContext(), "Press the back button once again and exit the program", Toast.LENGTH_SHORT).show();
            clickTime = System.currentTimeMillis();
        } else {
            new Thread() {
                @Override
                public void run() {
                    try {
                        FileUtil.deleteDir();
                        Thread.sleep(700);
                        MainActivity.this.finish();
                    } catch (Exception e) {
                        Log.e(LOG_TAG, e.getMessage());
                    }
                }
            }.start();
        }
    }

    /**
     * 展示chatFragment页面未读信息的条数
     *
     * @param unread 未读信息数量
     */
    @Override
    public void showMessageNum(int unread) {
        if (unread <= 0) {
            num.setVisibility(View.GONE);
        } else {
            num.setVisibility(View.VISIBLE);
        }
//        if (unread <= 99) {
//            num.setVisibility(View.VISIBLE);
//            num.setText(String.valueOf(unread));
//        } else if (unread > 99) {
//            num.setVisibility(View.VISIBLE);
//            num.setText(getString(R.string.ninety_nine));
//        }
    }

    /**
     * ViewPager的适配器
     */
    private class MainActivityFragmentAdapter extends FragmentPagerAdapter {
        public MainActivityFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return localFragment;
                case 1:
                    return travelFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "LOCAL EXPERIENCES";
                case 1:
                    return "TRAVELER REQUESTS";
            }
            return null;
        }
    }
}