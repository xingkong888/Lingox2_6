package cn.lingox.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;

import cn.lingox.android.R;
import cn.lingox.android.adapter.PathReferenceReplyAdapter;
import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.entity.Indent;
import cn.lingox.android.entity.Path;
import cn.lingox.android.entity.PathReference;
import cn.lingox.android.entity.PathReferenceReply;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.ServerHelper;
import cn.lingox.android.helper.WritePathReferenceDialog;

public class LocalReferenceActivity extends Activity implements OnClickListener {

    public static final String PATH = "path";
    private static final String LOG_TAG = "LocalReferenceActivity";
    // UI Elements
    private ImageView addReference;
    private ExpandableListView listView;
    private PathReferenceReplyAdapter adapter;

    private ArrayList<HashMap<String, String>> groups;
    private ArrayList<ArrayList<HashMap<String, String>>> childs;

    private ProgressBar pb;

    private ImageView anim;
    private AnimationDrawable animationDrawable;
    private String pathId, userId;
    private int type = 0;
    private Path path;

    private boolean isSelf = false;

    private ArrayList<PathReference> list;

    //回复评论
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            adapter.notifyDataSetChanged();
        }
    };
    //评论
    private Handler handler1 = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            new CreatePathReference(msg.obj.toString()).execute();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path_reference);
        if (getIntent().hasExtra(PATH)) {
            path = getIntent().getParcelableExtra(PATH);
            pathId = path.getId();
            userId = path.getUserId();
            type = path.getType();
        } else {
            Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show();
            finish();
        }
        if (!LingoXApplication.getInstance().getSkip()) {
            isSelf = userId.contentEquals(CacheHelper.getInstance().getSelfInfo().getId());
        }
        initView();
    }

    private void initView() {
        anim = (ImageView) findViewById(R.id.anim);
        animationDrawable = (AnimationDrawable) anim.getBackground();
        pb = (ProgressBar) findViewById(R.id.progress);
        addReference = (ImageView) findViewById(R.id.iv_add_reference);
        // If we are viewing our own references
        // TODO implement reference managing for own reference page

        addReference.setOnClickListener(this);
        //返回
        findViewById(R.id.layout_back).setOnClickListener(this);
        //添加
        findViewById(R.id.layout_add).setOnClickListener(this);

        listView = (ExpandableListView) findViewById(R.id.path_reference_list);
        listView.setGroupIndicator(null);
        groups = new ArrayList<>();
        childs = new ArrayList<>();
        adapter = new PathReferenceReplyAdapter(this, groups, childs, handler);
        listView.setAdapter(adapter);
        listView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return true;
            }
        });
        if (pathId != null) {
            new LoadPathReferences().execute(pathId);
        }
    }

    private void initData() {
        if (groups.size() == 0) {
            startAnim();
            //判断当前用户是否为活动发起者
            //若是，则显示删除图标
            //若不是，判断当前用户是否在活动参加人列表中
            // 若在，显示添加图标
            //若不在，不能进行任何操作
            if (!LingoXApplication.getInstance().getSkip()) {
                if (isSelf) {
                    addReference.setVisibility(View.VISIBLE);
                } else {
                    new GetMessage().execute();
                }
            } else {
                addReference.setVisibility(View.GONE);
            }
        } else {
            //判断当前用户是否为活动发起者
            //若是，则显示删除图标
            //若不是，判断当前用户是否在活动参加人列表中
            // 若在，显示添加图标
            //若不在，不能进行任何操作
            if (!LingoXApplication.getInstance().getSkip()) {
                if (isSelf) {
                    addReference.setVisibility(View.INVISIBLE);
                } else {
                    new GetMessage().execute();
                }
            } else {
                addReference.setVisibility(View.GONE);
            }
            stopAnim();
        }
        adapter.notifyDataSetChanged();
        //将所有项设置成默认展开
        int groupCount = listView.getCount();
        for (int i = 0; i < groupCount; i++) {
            listView.expandGroup(i);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_back:
                finish();
                break;
            case R.id.iv_add_reference:
                WritePathReferenceDialog.newInstance(handler1, this).show(getFragmentManager(), "");
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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

    public void startAnim() {
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

    private class CreatePathReference extends AsyncTask<Void, Void, Boolean> {
        private HashMap<String, String> map;
        private String content;

        public CreatePathReference(String content) {
            this.content = content;
        }

        @Override
        protected void onPreExecute() {
            map = new HashMap<>();
            map.put("userId", CacheHelper.getInstance().getSelfInfo().getId());
            map.put("pathId", pathId);
            map.put("content", content);
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                ServerHelper.getInstance().createPathReference(map);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                //成功
                new LoadPathReferences().execute(pathId);
            }
        }
    }

    //下载活动的评论
    private class LoadPathReferences extends AsyncTask<String, Void, Boolean> {

        private HashMap<String, String> map;
        private HashMap<String, String> group;
        private HashMap<String, String> child;
        private ArrayList<HashMap<String, String>> tempChildList;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            map = new HashMap<>();
            list = new ArrayList<>();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            boolean success = false;
            map.put("pathId", params[0]);
            try {
                list.addAll(ServerHelper.getInstance().getPathReference(map));
                if (list.size() > 0) {
                    //获取评论，存入group
                    PathReference reference;
                    for (int i = 0, j = list.size(); i < j; i++) {
                        reference = list.get(i);
                        group = new HashMap<>();
                        group.put("user_id", reference.getUser_id());
                        group.put("referenceId", reference.getId());
                        group.put("content", reference.getContent());
                        groups.add(group);
                        if (!reference.getReplys().isEmpty()) {
                            PathReferenceReply reply;
                            for (int a = 0, b = reference.getReplys().size(); a < b; a++) {
                                reply = reference.getReplys().get(a);
                                tempChildList = new ArrayList<>();
                                child = new HashMap<>();
                                child.put("user_id", reply.getUser_id());
                                child.put("content", reply.getContent());
                                tempChildList.add(child);
                                childs.add(tempChildList);
                            }
                        } else {
                            childs.add(new ArrayList<HashMap<String, String>>());
                        }
                    }
                }
                success = true;
            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception caught: " + e.toString());
            }
            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            pb.setVisibility(View.INVISIBLE);
            if (success) {
                initData();
            } else {
                startAnim();
                Toast.makeText(getApplicationContext(), "Failed to get  References", Toast.LENGTH_LONG).show();
            }
        }
    }

    //获取当前用户与活动发起者之间是否存在申请关系
    //若存在，判断申请的状态及是否到期
    //若为同意且时间到期，则显示“+”，否则不显示
    private class GetMessage extends AsyncTask<Void, Void, Boolean> {
        private HashMap<String, String> map;
        private ArrayList<Indent> list;
        private boolean isExist = false;

        @Override
        protected void onPreExecute() {
            map = new HashMap<>();
            map.put("tarId", path.getUserId());
            map.put("userId", CacheHelper.getInstance().getSelfInfo().getId());
            list = new ArrayList<>();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                list.addAll(ServerHelper.getInstance().getApplication(map));
                if (list.size() > 0) {
                    //两者之间有申请
                    //判断其中是否包含本活动
                    for (Indent indent : list) {
                        if (indent.getPathId().contentEquals(pathId)) {
                            //包含本活动，判断申请状态
                            //1待处理 2申请者取消  3拒绝 4同意 5时间到
                            switch (indent.getState()) {
                                case 1:
                                case 2:
                                case 3:
                                case 5:
                                    isExist = false;
                                    break;
                                case 4:
                                    //判断时间是否到
                                    if (indent.getEndTime() <= (System.currentTimeMillis() / 1000L)) {
                                        //显示“+”
                                        isExist = true;
                                    }
                                    break;
                            }
                        }
                    }
                } else {
                    //两者之间无申请
                    isExist = false;
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage());
                isExist = false;
            }
            return isExist;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                //显示“+”
                addReference.setVisibility(View.VISIBLE);
            } else {
                //不显示“+”
                addReference.setVisibility(View.INVISIBLE);
            }
        }
    }
}
