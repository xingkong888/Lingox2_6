package cn.lingox.android.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;

import cn.lingox.android.R;
import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.utils.SkipDialog;

/**
 * 收藏
 */
public class FavouriteActivity extends ActionBarActivity implements OnClickListener {
    // UI Elements
    private FavouriteLocalFragment localFragment;
    private FavouriteTravelFragment travelFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        if (LingoXApplication.getInstance().getSkip()) {
            SkipDialog.getDialog(this).show();
        }
    }

    /**
     * 实例化控件
     */
    private void initView() {
        setContentView(R.layout.activity_favourite);
        /************************ MAIN VIEW ********************/
        localFragment = new FavouriteLocalFragment();
        travelFragment = new FavouriteTravelFragment();
        MainActivityFragmentAdapter tabAdapter = new MainActivityFragmentAdapter(getSupportFragmentManager());
        ViewPager viewPager = (ViewPager) findViewById(R.id.fragment_container);
        viewPager.setAdapter(tabAdapter);
        // (Number of fragments - 1) This prevents the edge tabs being recreated
        // 除当前页外，预加载及保留的页面数   viewPager.setOffscreenPageLimit(2);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setCurrentItem(0);

        //返回按键
        findViewById(R.id.iv_back).setOnClickListener(this);
        findViewById(R.id.favourite_bar).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
            case R.id.favourite_bar:
                finish();
                break;
        }
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
                    return "Local";
                case 1:
                    return "Traveler";
            }
            return null;
        }
    }
}
