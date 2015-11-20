package cn.lingox.android.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import cn.lingox.android.R;
import cn.lingox.android.utils.CircularImageView;

/**
 * 创建travel数据
 */
public class TravelViewActivity extends Activity implements OnClickListener {
    public static final String TRAVEL_VIEW = "travelView";//传递travel的实例

    private ImageView back;
    private CircularImageView avatar;
    private TextView userName, location, travelingTime, tag, describe, provide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel);

        initView();
    }

    private void initView() {
        back = (ImageView) findViewById(R.id.travel_view_back);
        back.setOnClickListener(this);

        avatar = (CircularImageView) findViewById(R.id.travel_view_avatar);
        userName = (TextView) findViewById(R.id.travel_view_name);
        travelingTime = (TextView) findViewById(R.id.travel_view_time);
        tag = (TextView) findViewById(R.id.travel_view_tag);
        describe = (TextView) findViewById(R.id.travel_view_describe);
        provide = (TextView) findViewById(R.id.travel_view_provide);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.travel_view_back:
                finish();
                break;
        }
    }
}