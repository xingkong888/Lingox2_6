package cn.lingox.android.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;

import cn.lingox.android.R;

/**
 * 创建travel数据
 */
public class TravelEditActivity extends FragmentActivity implements OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_travel_edit);
    }

    @Override
    public void onClick(View v) {

    }
}