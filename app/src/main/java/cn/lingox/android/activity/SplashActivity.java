package cn.lingox.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.umeng.analytics.AnalyticsConfig;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Locale;

import cn.lingox.android.R;
import cn.lingox.android.adapter.ViewPagerAdapter;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.ServerHelper;
import cn.lingox.android.utils.FileUtil;

public class SplashActivity extends Activity {
    private static final int[] pics = {R.drawable.guide_page1, R.drawable.guide_page2};
    // UI Elements
    private ViewPager viewPager;
    private ImageView[] points;
    private int currentIndex;

    private boolean misScrolled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLocale(CacheHelper.getInstance().getSettingLanguage());

        MobclickAgent.openActivityDurationTrack(false);
        MobclickAgent.updateOnlineConfig(this);
        MobclickAgent.setCatchUncaughtExceptions(true);
        AnalyticsConfig.enableEncrypt(true);
        checkFile();
        if (CacheHelper.getInstance().isLoggedIn()) {
            new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                ServerHelper.getInstance().loginTime(CacheHelper.getInstance().getSelfInfo().getId());
                                Thread.sleep(200);
                                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                                finish();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
            ).start();
            return;
        }
        initView();
    }


    private void setLocale(String lang) {
        if (lang != null) {
            Locale locale = new Locale(lang);
            Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration conf = res.getConfiguration();
            conf.locale = locale;
            res.updateConfiguration(conf, dm);
        }
    }

    private void initView() {
        setContentView(R.layout.activity_splash);
        ArrayList<View> views = new ArrayList<>();
        viewPager = (ViewPager) findViewById(R.id.viewpager);

        ViewPagerAdapter vpAdapter = new ViewPagerAdapter(views);
        RelativeLayout rootLayout = (RelativeLayout) findViewById(R.id.splash_root);
        AlphaAnimation animation = new AlphaAnimation(0.3f, 1.0f);
        animation.setDuration(2000);
        rootLayout.startAnimation(animation);
        LayoutInflater inflater = getLayoutInflater();

        for (int pic : pics) {
            View v = inflater.inflate(R.layout.item_view, null);
            ImageView image = (ImageView) v.findViewById(R.id.image);
            image.setImageResource(pic);
            views.add(v);
        }
        viewPager.setAdapter(vpAdapter);
        viewPager.setOnPageChangeListener(new pageListener());
        initPoint();
    }

    private void initPoint() {
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.ll);

        points = new ImageView[pics.length];
        for (int i = 0; i < pics.length; i++) {
            points[i] = (ImageView) linearLayout.getChildAt(i);
            points[i].setEnabled(true);
            points[i].setOnClickListener(new pointListener());
            points[i].setTag(i);
        }

        currentIndex = 0;
        points[currentIndex].setEnabled(false);
    }

    private void setCurView(int position) {
        if (position < 0 || position >= pics.length) {
            return;
        }
        viewPager.setCurrentItem(position);
        setCurDot(position);
    }

    private void setCurDot(int positon) {
        if (positon < 0 || positon >= pics.length || currentIndex == positon) {
            return;
        }
        points[positon].setEnabled(false);
        points[currentIndex].setEnabled(true);

        currentIndex = positon;
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        SplashActivity.this.finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    private void checkFile() {
        new Thread() {
            @Override
            public void run() {
                try {
                    FileUtil.getFileSize();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private class pageListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int state) {
            switch (state) {
                case ViewPager.SCROLL_STATE_DRAGGING:
                    misScrolled = false;
                    break;
                case ViewPager.SCROLL_STATE_SETTLING:
                    misScrolled = true;
                    break;
                case ViewPager.SCROLL_STATE_IDLE:
                    if (viewPager.getCurrentItem() == viewPager.getAdapter().getCount() - 1 && !misScrolled) {
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                        SplashActivity.this.finish();
//                        ThirdPartyLogin tpl = new ThirdPartyLogin();
//                        tpl.show(SplashActivity.this);
//                         SplashActivity.this.finish();
                    }
                    misScrolled = true;
                    break;
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            setCurDot(position);
        }
    }

    private class pointListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            int position = (Integer) v.getTag();
            setCurView(position);
            setCurDot(position);
        }
    }
}