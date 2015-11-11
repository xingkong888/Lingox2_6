package cn.lingox.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import cn.lingox.android.R;
import cn.lingox.android.adapter.ContactAdapter;
import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.entity.User;

public class UserListActivity extends Activity implements OnClickListener {
    // Incoming Intent Extras
    public static final String USER_LIST = LingoXApplication.PACKAGE_NAME + ".USER_LIST";
    public static final String PAGE_TITLE = LingoXApplication.PACKAGE_NAME + ".PAGE_TITLE";
    // UI Elements
    private ListView listView;
    private ContactAdapter adapter;

    private ImageView anim;
    private AnimationDrawable animationDrawable;

    // Data Elements
    private String pageTitle;
    private ArrayList<User> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_following);

        Intent intent = getIntent();

        userList = intent.getParcelableArrayListExtra(USER_LIST);
        pageTitle = intent.getStringExtra(PAGE_TITLE);
        Collections.sort(userList, new Comparator<User>() {
            @Override
            public int compare(User lhs, User rhs) {
                return lhs.getHeader().compareTo(rhs.getHeader());
            }
        });
        initView();
        initData();
    }

    private void initView() {
        TextView title = (TextView) findViewById(R.id.tv_title);
        title.setText(pageTitle);
        LinearLayout backButton = (LinearLayout) findViewById(R.id.layout_back);
        backButton.setOnClickListener(this);
        listView = (ListView) findViewById(R.id.list);

        anim = (ImageView) findViewById(R.id.anim);
        animationDrawable = (AnimationDrawable) anim.getBackground();
    }

    private void initData() {
        if (userList.size() == 0) {
            startAnim();
            listView.setVisibility(View.GONE);
        } else {
            listView.setVisibility(View.VISIBLE);
            stopAnim();
            adapter = new ContactAdapter(UserListActivity.this,
                    R.layout.row_contact, userList);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    Intent intent = new Intent(UserListActivity.this,
                            UserInfoActivity.class);
                    intent.putExtra(UserInfoActivity.INTENT_USER_ID,
                            adapter.getItem(position).getId());
                    startActivity(intent);
                }
            });
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_back:
                UserListActivity.this.finish();
            default:
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

    private void startAnim() {
        if (!animationDrawable.isRunning()) {
            anim.setVisibility(View.VISIBLE);
            animationDrawable.start();
        }
    }

    private void stopAnim() {
        if (animationDrawable.isRunning()) {
            anim.setVisibility(View.GONE);
            animationDrawable.stop();
        }
    }
}