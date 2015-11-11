package cn.lingox.android.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;

import cn.lingox.android.R;
import cn.lingox.android.adapter.ShowIndentAdapter;
import cn.lingox.android.entity.Indent;

public class ShowMoreIndentActivity extends ActionBarActivity {

    private ArrayList<Indent> datas = new ArrayList<>();
    private String username = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_indent);
        if (getIntent().hasExtra("Indents")) {
            datas.addAll((ArrayList) getIntent().getParcelableArrayListExtra("Indents"));
        }
        if (getIntent().hasExtra("username")) {
            username = getIntent().getStringExtra("username");
        }
        initView();
    }

    private void initView() {
        ImageView back = (ImageView) findViewById(R.id.show_indent_back);
        back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView name = (TextView) findViewById(R.id.show_indent_username);
        ListView listView = (ListView) findViewById(R.id.show_indent_list);
        listView.setAdapter(new ShowIndentAdapter(this, datas));

        name.setText(username);
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}