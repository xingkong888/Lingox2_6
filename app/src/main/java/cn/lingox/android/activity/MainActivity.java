package cn.lingox.android.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.FeedbackAgent;
import com.umeng.update.UmengUpdateAgent;

import cn.lingox.android.R;
import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.ImageHelper;
import cn.lingox.android.helper.JsonHelper;
import cn.lingox.android.helper.ServerHelper;
import cn.lingox.android.helper.UIHelper;
import cn.lingox.android.utils.FileUtil;
import cn.lingox.android.utils.SkipDialog;


public class MainActivity extends ActionBarActivity implements OnClickListener, ChatFragment.showNum {
    // REQUEST CODES
    public static final int REQUEST_CODE_SETTINGS = 101;
    // RESULT CODES
    public static final int RESULT_CODE_LOGOUT = 201;
    public static final int RESULT_CODE_RESET_LANGUAGE = 202;
    public static final String SEARCH = "Search";
    private static final String LOG_TAG = "MainActivity";
    private static MainActivity mainActivity;
    //两次返回退出
    private long clickTime;
    // UI Elements
    private ChatFragment chatFragment;
    private PathFragment pathFragment;
    private NearByFragment nearByFragment;
    // UI Elements
    private ImageView photo;
    private ImageView flag;
    private MainActivityFragmentAdapter tabAdapter;
    private ViewPager viewPager;
    private DrawerLayout sideDrawers;
    private ActionBarDrawerToggle sideDrawerToggle;

    private RelativeLayout showNumLayout;
    private TextView num;

    private boolean rightSideDrawerOpen = false;

    private ImageView search;

    private int unread = -1;

    public static MainActivity getObj() {
        return mainActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UmengUpdateAgent.update(this);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        LingoXApplication.getInstance().setWidth(dm.widthPixels);
        mainActivity = this;
        initView();
        if (LingoXApplication.getInstance().getSkip()) {
            SkipDialog.getDialog(this).show();
        }
    }

    @Override
    protected void onStart() {
        // TODO Move to onResume?
        setAvatar();
        super.onStart();
    }

    private void initView() {
        setContentView(R.layout.activity_main);
        // ----- TOOLBAR -----
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_activity_toolbar);
        setSupportActionBar(toolbar);

        showNumLayout = (RelativeLayout) findViewById(R.id.show_num);
        showNumLayout.setOnClickListener(this);
        num = (TextView) findViewById(R.id.num);
        search = (ImageView) findViewById(R.id.search);
        search.setOnClickListener(this);
        sideDrawers = (DrawerLayout) findViewById(R.id.drawer_layout);
        sideDrawerToggle = new ActionBarDrawerToggle(
                this,
                sideDrawers,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_closed
        ) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                switch (drawerView.getId()) {
                    case R.id.left_drawer:
                        if (rightSideDrawerOpen) {
                            rightSideDrawerOpen = false;
                            sideDrawers.closeDrawer(Gravity.RIGHT);
                        }
                        super.onDrawerSlide(drawerView, slideOffset);
                        break;
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                switch (drawerView.getId()) {
                    case R.id.left_drawer:
                        super.onDrawerOpened(drawerView);
                        break;
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                switch (drawerView.getId()) {
                    case R.id.left_drawer:
                        super.onDrawerClosed(drawerView);
                        break;
                }
            }
        };
        sideDrawers.setDrawerListener(sideDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        // ----- LEFT MENU -----
        TextView contact_list = (TextView) findViewById(R.id.layout_contact_list);
        contact_list.setOnClickListener(this);
        TextView set = (TextView) findViewById(R.id.layout_set);
        set.setOnClickListener(this);
        photo = (ImageView) findViewById(R.id.avatar_info);
        photo.setOnClickListener(this);
        TextView feedback = (TextView) findViewById(R.id.layout_feedback);
        feedback.setOnClickListener(this);
        TextView info = (TextView) findViewById(R.id.layout_info);
        info.setOnClickListener(this);
        flag = (ImageView) findViewById(R.id.iv_flag);
        // ----- MAIN VIEW -----
        chatFragment = new ChatFragment();
        pathFragment = new PathFragment();
        nearByFragment = new NearByFragment();
        tabAdapter = new MainActivityFragmentAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.fragment_container);
        viewPager.setAdapter(tabAdapter);
        // (Number of fragments - 1) This prevents the edge tabs being recreated
        viewPager.setOffscreenPageLimit(2);
        viewPager.setCurrentItem(1);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0://chat界面
                        search.setClickable(false);
                        search.setVisibility(View.INVISIBLE);
                        showNumLayout.setEnabled(false);
                        showNumLayout.setVisibility(View.INVISIBLE);
                        break;
                    case 1://活动
                    case 2://个人
                        search.setClickable(true);
                        search.setVisibility(View.VISIBLE);
                        if (unread > 0) {
                            showNumLayout.setEnabled(true);
                            showNumLayout.setVisibility(View.VISIBLE);
                        }
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        findViewById(R.id.asdfg).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.asdfg, pathFragment)
                        .commit();
            }
        });
    }

    private void initDate() {
        if (!LingoXApplication.getInstance().getSkip()) {//如果是登录进来的，显示名字
            ((TextView) findViewById(R.id.tv_nickname)).setText(CacheHelper.getInstance().getSelfInfo().getNickname());
            ((TextView) findViewById(R.id.tv_username)).setText("ID:" + CacheHelper.getInstance().getSelfInfo().getUsername());
        } else {
            ((TextView) findViewById(R.id.tv_nickname)).setText("");
            ((TextView) findViewById(R.id.tv_username)).setText("");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_contact_list:
                if (!LingoXApplication.getInstance().getSkip()) {
                    Intent contactListIntent = new Intent(this, ContactsActivity.class);
                    startActivity(contactListIntent);
                } else {
                    SkipDialog.getDialog(this).show();
                }
                break;
            case R.id.layout_set:
                if (!LingoXApplication.getInstance().getSkip()) {
                    Intent settingsIntent = new Intent(this, SettingsActivity.class);
                    startActivityForResult(settingsIntent, MainActivity.REQUEST_CODE_SETTINGS);
                } else {
                    SkipDialog.getDialog(this).show();
                }
                break;
            case R.id.layout_feedback:
                FeedbackAgent agent = new FeedbackAgent(this);
                agent.startFeedbackActivity();
                break;
            case R.id.layout_info:
                Uri uri = Uri.parse("http://lingox.cn");
                Intent aboutUsIntent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(aboutUsIntent);
                break;
            case R.id.avatar_info:
                if (!LingoXApplication.getInstance().getSkip()) {
                    Intent userInfoIntent = new Intent(this, UserInfoActivity.class);
                    userInfoIntent.putExtra(UserInfoActivity.INTENT_USER_ID, CacheHelper
                            .getInstance().getSelfInfo().getId());
                    startActivity(userInfoIntent);
                    overridePendingTransition(R.anim.push_top_in, R.anim.push_bottom_out);
                } else {
                    SkipDialog.getDialog(this).show();
                }
                break;
            case R.id.search:
                //TODO 获取当前fragment的位置
                Intent intent = new Intent(this, SearchActivity.class);
                switch (viewPager.getCurrentItem()) {
                    case 1://活动
                        intent.putExtra(SEARCH, 1);
                        break;
                    case 2://个人
                        intent.putExtra(SEARCH, 2);
                        break;
                }
                startActivity(intent);
                break;
            case R.id.show_num:
                //TODO 获取当前fragment的位置
                viewPager.setCurrentItem(0);
                break;
        }
    }

    private void setAvatar() {
        if (!LingoXApplication.getInstance().getSkip()) {
            tabAdapter.notifyDataSetChanged();
            UIHelper.getInstance().imageViewSetPossiblyEmptyUrl(MainActivity.this, photo,
                    CacheHelper.getInstance().getSelfInfo().getAvatar(), "circular");
            ImageHelper.getInstance().loadFlag(flag, JsonHelper.getInstance().getCodeFromCountry(
                    CacheHelper.getInstance().getSelfInfo().getCountry()), 2);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SETTINGS:
                switch (resultCode) {
                    case RESULT_CODE_LOGOUT:
                        Intent logoutIntent = new Intent(this, LoginActivity.class);
                        logoutIntent.putExtra(LoginActivity.LOGOUT_REQUESTED, true);
                        startActivity(logoutIntent);
                        finish();
                        break;
                    case RESULT_CODE_RESET_LANGUAGE:
                        Intent resetLanguage = new Intent(this, SplashActivity.class);
                        startActivity(resetLanguage);
                        finish();
                        break;
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        new CheckForUpdates().execute();
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

    @Override
    public void onBackPressed() {
        if (sideDrawers.isDrawerOpen(Gravity.LEFT)) {
            sideDrawers.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //退出应用
    private void exit() {
        if ((System.currentTimeMillis() - clickTime) > 2000) {
            Toast.makeText(getApplicationContext(), "Press the back button once again and exit the program", Toast.LENGTH_SHORT).show();
            clickTime = System.currentTimeMillis();
        } else {
            new Thread(){
                @Override
                public void run() {
                    try {
                        FileUtil.deleteDir();
                        Thread.sleep(700);
                        MainActivity.this.finish();
                    }catch (Exception e){

                    }
                }
            }.start();
        }
    }

    /**
     * 展示chatfragment页面未读信息的条数
     *
     * @param unread
     */
    @Override
    public void showMessageNum(int unread) {
        this.unread = unread;
        if (this.unread > 0) {
            if (viewPager.getCurrentItem() != 0) {
                showNumLayout.setEnabled(true);
                showNumLayout.setVisibility(View.VISIBLE);
            } else {
                showNumLayout.setVisibility(View.INVISIBLE);
                showNumLayout.setEnabled(false);
            }
            if (this.unread > 99) {
                num.setText("99+");
            } else {
                num.setText(String.valueOf(this.unread));
            }
        }
    }

    private class MainActivityFragmentAdapter extends FragmentPagerAdapter {
        public MainActivityFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return chatFragment;
                case 1:
                    return pathFragment;
                case 2:
                    return nearByFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.chats);
                case 1:
                    return getString(R.string.discover);
                case 2:
                    return getString(R.string.nearby);
            }
            return null;
        }
    }

    /**
     * 检查APP版本更新
     */
    private class CheckForUpdates extends AsyncTask<Void, Void, Boolean> {
        private boolean checkUpdata = false;

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                PackageManager packageManager = getPackageManager();
                PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(), 0);
                int versionCode = packInfo.versionCode;
                checkUpdata = ServerHelper.getInstance().requireUpdate(versionCode);
                return checkUpdata;
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(LOG_TAG, e.toString());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean requireUpdate) {
            super.onPostExecute(requireUpdate);
            if (requireUpdate) {
                Intent intent = new Intent(MainActivity.this, AppUpdateActivity.class);
                startActivity(intent);
            }
        }
    }
}