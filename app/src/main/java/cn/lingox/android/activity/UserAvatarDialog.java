package cn.lingox.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import cn.lingox.android.R;
import cn.lingox.android.helper.UIHelper;

public class UserAvatarDialog extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.avatar);
        initView();
        super.onCreate(savedInstanceState);
    }

    private void initView() {
        ImageView avatarPic = (ImageView) findViewById(R.id.avatar);
        Intent intent = getIntent();
        String url = intent.getStringExtra(UserInfoFragment.AVATAR_URL);
        UIHelper.getInstance().imageViewSetPossiblyEmptyUrl(this, avatarPic, url);
    }
}
