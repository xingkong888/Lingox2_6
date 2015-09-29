package cn.lingox.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.umeng.analytics.MobclickAgent;

import cn.lingox.android.R;
import cn.lingox.android.constants.URLConstant;

public class AppUpdateActivity extends Activity implements OnClickListener {

    private Button update;
    private Button cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.umeng_update_dialog);
        initView();
    }

    private void initView() {
        update = (Button) findViewById(R.id.umeng_update_id_ok);
        cancel = (Button) findViewById(R.id.umeng_update_id_cancel);
        update.setOnClickListener(this);
        cancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.umeng_update_id_ok:
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse(URLConstant.APK_URL)));
                break;
            case R.id.umeng_update_id_cancel:
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
                break;
        }
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
}