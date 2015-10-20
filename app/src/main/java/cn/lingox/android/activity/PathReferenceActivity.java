package cn.lingox.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;

import cn.lingox.android.R;
import cn.lingox.android.adapter.ReferenceAdapter;
import cn.lingox.android.entity.PathRefresh;

public class PathReferenceActivity extends Activity implements OnClickListener {

    public static final String PATH_ID = "path_id";
    private static final String LOG_TAG = "PathReferenceActivity";
    // UI Elements
    private ImageView addReference;
    private LinearLayout back, add;
    private ListView listView;
    private ReferenceAdapter arrayAdapter;

    private ImageView anim;
    private AnimationDrawable animationDrawable;

    private ProgressBar pb;

    private String pathId;

    private ArrayList<PathRefresh> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reference);
        if (getIntent().hasExtra(PATH_ID)) {
            pathId = getIntent().getStringExtra(PATH_ID);
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
//        if (ownReferencesPage) {
//            addReference.setVisibility(View.INVISIBLE);
//        } else {
//            addReference.setVisibility(View.VISIBLE);
//        }
        addReference.setOnClickListener(this);

        back = (LinearLayout) findViewById(R.id.layout_back);
        back.setOnClickListener(this);

        add = (LinearLayout) findViewById(R.id.layout_add);
        add.setOnClickListener(this);

        listView = (ListView) findViewById(R.id.list);
    }

    private void initData() {
    }

    @Override
    public void onClick(View v) {
    }

//    @Override
//    public void finish() {
//        Intent intent = new Intent();
//        intent.putExtra(INTENT_USER_REFERENCE, referenceList);
//        setResult(RESULT_OK, intent);
//        super.finish();
//    }

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

    //下载活动的评论
    private class LoadPathReferences extends AsyncTask<String, String, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            boolean success = false;
            try {
//                list.addAll(ServerHelper.getInstance().getPathsReferences(params[0]));
//                success = true;
//                for (int i = 0; i < list.size(); i++) {
//                    try {
//                        User user = ServerHelper.getInstance().getUserInfo(list.get(i).getUserSrcId());
//                        CacheHelper.getInstance().addUserInfo(user);
//                    } catch (Exception e2) {
//                        Log.e(LOG_TAG, "Inner Exception caught: " + e2.toString());
//                    }
//                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception caught: " + e.toString());
            }
            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
//            pb.setVisibility(View.INVISIBLE);
//            if (success) {
//                initData();
//            } else {
//                Toast.makeText(getApplicationContext(), "Failed to get User's References", Toast.LENGTH_LONG).show();
//            }
        }
    }
}
