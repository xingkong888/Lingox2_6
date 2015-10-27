package cn.lingox.android.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import cn.lingox.android.R;
import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.task.GetUser;

public class UserInfoActivity extends ActionBarActivity {
    // INCOMING INTENT EXTRAS
    public static final String INTENT_USER_ID = LingoXApplication.PACKAGE_NAME + ".USER_ID";
    // OUTGOING BUNDLE TAGS
    public static final String USER = "USER";
    private static final String LOG_TAG = "UserInfoActivity";
    // Data Elements
    public User user = null;
    // UI Elements
    private ProgressBar progressBar;
    private UserInfoFragment infoFragment;
    private PathFragment pathFragment;

    private ImageView back;
    private TextView name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ensure that a UserID has been passed to this page
        if (!getIntent().hasExtra(INTENT_USER_ID)) {
            String errStr = "Severe Error: No UserID passed to Activity";
            Log.e(LOG_TAG, errStr);
            Toast.makeText(this, errStr, Toast.LENGTH_LONG).show();
            finish();
        }
        initView();
        if (!LingoXApplication.getInstance().getSkip()) {
            user = CacheHelper.getInstance().getUserInfo(getIntent().getStringExtra(INTENT_USER_ID));
        }
        if (user == null) {
            new GetUser(getIntent().getStringExtra(INTENT_USER_ID), new GetUser.Callback() {
                @Override
                public void onSuccess(User cbUser) {
                    user = cbUser;
                    afterUserLoaded();
                }

                @Override
                public void onFail() {
                    Toast.makeText(UserInfoActivity.this, "Error: Failed to download User details", Toast.LENGTH_LONG).show();
                }
            }).execute();
        } else {
            afterUserLoaded();
        }
    }

    private void initView() {
        setContentView(R.layout.activity_user_info);

        back = (ImageView) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        name = (TextView) findViewById(R.id.name);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    private void afterUserLoaded() {
        progressBar.setVisibility(View.GONE);
        infoFragment = new UserInfoFragment();
        pathFragment = new PathFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(USER, user);
        infoFragment.setArguments(bundle);
        pathFragment.setArguments(bundle);

        UserInfoActivityFragmentAdapter tabAdapter = new UserInfoActivityFragmentAdapter(getSupportFragmentManager());
        ViewPager viewPager = (ViewPager) findViewById(R.id.fragment_container_1);
        viewPager.setAdapter(tabAdapter);
        // (Number of fragments - 1) This prevents the edge tabs being recreated
        viewPager.setOffscreenPageLimit(2);
        if (getIntent().hasExtra(PathEditActivity.ADDED_PATH)) {
            viewPager.setCurrentItem(1);
        }
        name.setText(user.getNickname());
    }

    @Override
    protected void onResume() {
        MobclickAgent.onResume(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        MobclickAgent.onPause(this);
        super.onPause();
    }

    private class UserInfoActivityFragmentAdapter extends FragmentPagerAdapter {

        public UserInfoActivityFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return infoFragment;
                case 1:
                    return pathFragment;
            }
            return null;
        }

        // TODO When we add a photos tab, change to 3
        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.personal_info);
                case 1:
                    if (user.getId().equals(CacheHelper.getInstance().getSelfInfo().getId())) {
                        return "PERSONAL DISCOVERY";
                    } else {
                        return getString(R.string.personal_activity);
                    }
            }
            return null;
        }
    }
}