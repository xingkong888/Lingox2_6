package cn.lingox.android.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;

import cn.lingox.android.R;
import cn.lingox.android.adapter.ReferenceAdapter;
import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.entity.Reference;
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.ServerHelper;
import cn.lingox.android.task.LoadUserReferences;

public class ReferenceActivity extends Activity implements OnClickListener {
    public static final String INTENT_USER_REFERENCE = LingoXApplication.PACKAGE_NAME + ".USER_REFERENCE";
    // Intent Extras
    public static final String INTENT_TARGET_USER_ID = LingoXApplication.PACKAGE_NAME + ".TARGET_USER_ID";
    public static final String INTENT_TARGET_USER_NAME = LingoXApplication.PACKAGE_NAME + ".TARGET_USER_NAME";
    public static final String INTENT_REFERENCE = LingoXApplication.PACKAGE_NAME + ".REFERENCE";
    public static final String INTENT_REQUEST_CODE = LingoXApplication.PACKAGE_NAME + ".REQUEST_CODE";
    // Request code
    static final int ADD_REFERENCE = 1;
    static final int EDIT_REFERENCE = 2;
    static final int VIEW_REFERENCE = 3;
    private static final String LOG_TAG = "ReferenceActivity";
    // Data Elements
    private boolean ownReferencesPage;
    private ArrayList<Reference> referenceList;
    private String userId;
    private String userName;

    private boolean isBothFollowed = false;

    // UI Elements
    private ListView listView;
    private ReferenceAdapter arrayAdapter;

    private ImageView anim;
    private AnimationDrawable animationDrawable;

    private ProgressBar pb;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            getReference(userId);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reference);

        Intent intent = getIntent();
        userId = intent.getStringExtra(UserInfoFragment.TARGET_USER_ID);
        userName = intent.getStringExtra(UserInfoFragment.TARGET_USER_NAME);
        ownReferencesPage = CacheHelper.getInstance().getSelfInfo().getId().equals(userId);
        initView();
        if (intent.hasExtra(UserInfoFragment.REFERENCES)) {
            referenceList = intent.getParcelableArrayListExtra(UserInfoFragment.REFERENCES);
            initData();
        } else {
            referenceList = new ArrayList<>();
            pb.setVisibility(View.VISIBLE);
            getReference(userId);
        }
    }

    private void initView() {
        anim = (ImageView) findViewById(R.id.anim);
        animationDrawable = (AnimationDrawable) anim.getBackground();
        pb = (ProgressBar) findViewById(R.id.progress);
        ImageView addReference = (ImageView) findViewById(R.id.iv_add_reference);

        // If we are viewing our own references
        // TODO implement reference managing for own reference page
        if (ownReferencesPage) {
            addReference.setVisibility(View.INVISIBLE);
        } else {
            addReference.setVisibility(View.VISIBLE);
        }
        addReference.setOnClickListener(this);
        //返回按钮
        findViewById(R.id.layout_back).setOnClickListener(this);
        //添加按钮
        findViewById(R.id.layout_add).setOnClickListener(this);

        listView = (ListView) findViewById(R.id.list);
    }

    private void initData() {
        if (referenceList.size() == 0) {
            startAnim();
            listView.setVisibility(View.GONE);
        } else {
            stopAnim();
            listView.setVisibility(View.VISIBLE);
            arrayAdapter = new ReferenceAdapter(this, referenceList, userId, handler);
            listView.setAdapter(arrayAdapter);
            updateList();
        }
    }

    private void updateList() {
        arrayAdapter.notifyDataSetChanged();
    }

    /**
     * 获取用户的评论
     *
     * @param id 用户id
     */
    private void getReference(String id) {
        new LoadUserReferences(id, new LoadUserReferences.Callback() {
            @Override
            public void onSuccess(ArrayList<Reference> list) {
                referenceList.clear();
                referenceList.addAll(list);
                for (int i = 0, j = referenceList.size(); i < j; i++) {
                    try {
                        User user = ServerHelper.getInstance().getUserInfo(referenceList.get(i).getUserSrcId());
                        CacheHelper.getInstance().addUserInfo(user);
                    } catch (Exception e2) {
                        Log.e(LOG_TAG, "Inner Exception caught: " + e2.toString());
                    }
                }
                pb.setVisibility(View.INVISIBLE);
                initData();
            }

            @Override
            public void onFail() {
                Toast.makeText(getApplicationContext(), "Failed to get User's References", Toast.LENGTH_LONG).show();
            }
        }).execute();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_add_reference:
                if (getIntent().hasExtra(UserInfoFragment.REFERENCES)) {
                    new GetBothFollowed().execute();
                } else {
                    Intent intent = new Intent(this, ReferenceDialog.class);
                    intent.putExtra(INTENT_TARGET_USER_ID, userId);
                    intent.putExtra(INTENT_TARGET_USER_NAME, userName);
                    intent.putExtra(INTENT_REQUEST_CODE, ADD_REFERENCE);
                    startActivityForResult(intent, ADD_REFERENCE);
                }
                break;
            case R.id.layout_back:
                ReferenceActivity.this.finish();
                break;
        }
    }

    @Override
    public void finish() {
        Intent intent = new Intent();
        intent.putExtra(INTENT_USER_REFERENCE, referenceList);
        setResult(RESULT_OK, intent);
        super.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == ReferenceDialog.FAILURE) {
            if (data != null)
                if (data.hasExtra("remark"))
                    Toast.makeText(this, data.getStringExtra("remark"),
                            Toast.LENGTH_LONG).show();
        } else {
            switch (requestCode) {
                case ADD_REFERENCE:
                    if (resultCode == ReferenceDialog.SUCCESS_ADD) {
                        referenceList.add((Reference) data
                                .getParcelableExtra(ReferenceDialog.ADDED_REFERENCE));
                        updateList();
                    }
                    break;
                case EDIT_REFERENCE:
                    if (resultCode == ReferenceDialog.SUCCESS_EDIT) {
                        int referenceIndex = findReferenceInList(referenceList, (Reference) data.getParcelableExtra(ReferenceDialog.REFERENCE_BEFORE_EDIT));
                        referenceList.set(referenceIndex, (Reference) data.getParcelableExtra(ReferenceDialog.REFERENCE_AFTER_EDIT));
                    } else if (resultCode == ReferenceDialog.SUCCESS_DELETE) {
                        int referenceIndex = findReferenceInList(referenceList, (Reference) data.getParcelableExtra(ReferenceDialog.DELETED_REFERENCE));
                        referenceList.remove(referenceIndex);
                    }
                    updateList();
                    break;
                case VIEW_REFERENCE:
                    // Do nothing
                    break;
            }
        }
    }

    // Helper methods
    private int findReferenceInList(ArrayList<Reference> list, Reference ref) {
        for (Reference refs : list) {
            if (refs.getId().equals(ref.getId())) {
                return list.indexOf(refs);
            }
        }
        return -1;
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

    //获取双方是否相互
    private class GetBothFollowed extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                isBothFollowed = ServerHelper.getInstance().getBothFollowed(
                        CacheHelper.getInstance().getSelfInfo().getId(), userId);
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage());
            }
            return isBothFollowed;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                //相互follow
                Intent intent = new Intent(ReferenceActivity.this, ReferenceDialog.class);
                intent.putExtra(INTENT_TARGET_USER_ID, userId);
                intent.putExtra(INTENT_TARGET_USER_NAME, userName);
                intent.putExtra(INTENT_REQUEST_CODE, ADD_REFERENCE);
                startActivityForResult(intent, ADD_REFERENCE);
            } else {
                new AlertDialog.Builder(ReferenceActivity.this)
                        .setMessage("You two need to follow each other")
                        .create().show();
            }
        }
    }
}
