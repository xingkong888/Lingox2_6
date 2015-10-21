package cn.lingox.android.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;

import cn.lingox.android.R;
import cn.lingox.android.adapter.PathReferenceReplyAdapter;
import cn.lingox.android.entity.PathReference;
import cn.lingox.android.entity.PathReferenceReply;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.ServerHelper;

public class PathReferenceActivity extends Activity implements OnClickListener {

    public static final String PATH_ID = "path_id";
    public static final String USER_ID = "user_id";
    public static final String TYPE = "type";
    private static final String LOG_TAG = "PathReferenceActivity";
    // UI Elements
    private ImageView addReference;
    private LinearLayout back, add;
    private ExpandableListView listView;
    private PathReferenceReplyAdapter adapter;

    private ArrayList<HashMap<String, String>> groups;
    private ArrayList<ArrayList<HashMap<String, String>>> childs;

    private ProgressBar pb;

    private ImageView anim;
    private AnimationDrawable animationDrawable;
    private String pathId, userId;
    private int type = 0;

    private boolean isSelf = false;

    private ArrayList<PathReference> list;


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            adapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path_reference);
        if (getIntent().hasExtra(PATH_ID) && getIntent().hasExtra(USER_ID) && getIntent().hasExtra(TYPE)) {
            pathId = getIntent().getStringExtra(PATH_ID);
            userId = getIntent().getStringExtra(USER_ID);
            type = getIntent().getIntExtra(TYPE, 0);
        } else {
            Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show();
            finish();
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

        back = (LinearLayout) findViewById(R.id.layout_back);
        back.setOnClickListener(this);

        add = (LinearLayout) findViewById(R.id.layout_add);
        add.setOnClickListener(this);

        listView = (ExpandableListView) findViewById(R.id.path_reference_list);
        listView.setGroupIndicator(null);
        if (pathId != null) {
            new LoadPathReferences().execute(pathId);
        }
    }

    private void initData() {
        if (groups.size() == 0) {
            startAnim();
            if (type == 1) {//local
                addReference.setVisibility(View.INVISIBLE);
            } else {
                addReference.setVisibility(View.VISIBLE);
            }
        } else {
            addReference.setVisibility(View.INVISIBLE);
            stopAnim();
        }
        adapter = new PathReferenceReplyAdapter(this, groups, childs, handler);
        listView.setAdapter(adapter);
        listView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return true;
            }
        });
        //将所有项设置成默认展开
        int groupCount = listView.getCount();
        for (int i = 0; i < groupCount; i++) {
            listView.expandGroup(i);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.iv_add_reference:
                new AlertDialog.Builder(this)
                        .setNegativeButton("NO", null)
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                new CreatePathReference().execute();
                            }
                        })
                        .create().show();
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

    private class CreatePathReference extends AsyncTask<Void, Void, Boolean> {
        private HashMap<String, String> map;

        @Override
        protected void onPreExecute() {
            map = new HashMap<>();
            map.put("userId", CacheHelper.getInstance().getSelfInfo().getId());
            map.put("pathId", pathId);
            map.put("content", "测试");
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                ServerHelper.getInstance().createPathReference(map);
                return true;
            } catch (Exception e) {
                Log.d("星期", "创建活动评论错误：" + e.getMessage());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                //成功
            }
        }
    }

    //下载活动的评论
    private class LoadPathReferences extends AsyncTask<String, String, Boolean> {

        private HashMap<String, String> map;
        private HashMap<String, String> group;
        private HashMap<String, String> child;
        private ArrayList<HashMap<String, String>> tempChildList;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            map = new HashMap<>();
            list = new ArrayList<>();
            groups = new ArrayList<>();
            childs = new ArrayList<>();
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
                        if (reference.getReplys() != null) {
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
}
