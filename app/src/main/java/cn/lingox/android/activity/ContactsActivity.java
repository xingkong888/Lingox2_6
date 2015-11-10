package cn.lingox.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import cn.lingox.android.R;

public class ContactsActivity extends ActionBarActivity implements OnClickListener {
    private static final String LOG_TAG = "ContactsActivity";

    private ContactsFragment contactsFragment;
    private FollowersFragment followersFragment;

    // UI Elements
    private ImageView back;

    private ContactsActivityFragmentAdapter tabAdapter;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        initView();
    }

    private void initView() {
        back = (ImageView) findViewById(R.id.back_button);
        back.setOnClickListener(this);

        contactsFragment = new ContactsFragment();
        followersFragment = new FollowersFragment();

        tabAdapter = new ContactsActivityFragmentAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.fragment_contacts);
        viewPager.setAdapter(tabAdapter);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setCurrentItem(0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_button:
                //返回上一层
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }

    private class ContactsActivityFragmentAdapter extends FragmentPagerAdapter {
        public ContactsActivityFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0://关注
                    return contactsFragment;
                case 1://粉丝
                    return followersFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.following);
                case 1:
                    return getString(R.string.follower);
            }
            return null;
        }
    }
}