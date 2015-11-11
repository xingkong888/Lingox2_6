package cn.lingox.android.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;

import java.util.List;

import cn.lingox.android.R;
import cn.lingox.android.adapter.GroupAdapter;
import it.sephiroth.android.library.widget.HListView;

/**
 * 群聊管理
 * 暂未实现
 */
public class GroupDetailsActivity extends Activity {
    private EMGroup group;

    private List<String> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);

        String str = getIntent().getStringExtra("groupId");
        try {
            //根据群聊ID从服务器获取群聊信息
            group = EMGroupManager.getInstance().getGroupFromServer(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        //保存获取下来的群聊信息
//        EMGroupManager.getInstance().createOrUpdateLocalGroup(group);
        list = group.getMembers();//获取群成员
        initView();
    }

    private void initView() {
        HListView groupSize = (HListView) findViewById(R.id.group_detail);
        if (list != null) {
            groupSize.setAdapter(new GroupAdapter(this, list));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void back(View view) {
    }
}
