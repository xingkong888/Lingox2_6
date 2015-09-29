package cn.lingox.android.activity;

import android.support.v7.app.ActionBarActivity;

import com.easemob.chat.EMChatManager;

public class BaseActivity extends ActionBarActivity {
    @Override
    protected void onResume() {
        super.onResume();
        EMChatManager.getInstance().activityResumed();
    }

}