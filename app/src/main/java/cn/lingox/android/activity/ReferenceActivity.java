package cn.lingox.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;

import cn.lingox.android.R;
import cn.lingox.android.adapter.ReferenceAdapter;
import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.entity.Reference;
import cn.lingox.android.helper.CacheHelper;

public class ReferenceActivity extends Activity implements OnClickListener {
    public static final String INTENT_USER_REFERENCE = LingoXApplication.PACKAGE_NAME + ".USER_REFERENCE";
    // Intent Extras
    public static final String INTENT_TARGET_USER_ID = LingoXApplication.PACKAGE_NAME + ".TARGET_USER_ID";
    public static final String INTENT_TARGET_USER_NAME = LingoXApplication.PACKAGE_NAME + ".TARGET_USER_NAME";
    static final String INTENT_REFERENCE = LingoXApplication.PACKAGE_NAME + ".REFERENCE";
    static final String INTENT_REQUEST_CODE = LingoXApplication.PACKAGE_NAME + ".REQUEST_CODE";
    // Request code
    static final int ADD_REFERENCE = 1;
    static final int EDIT_REFERENCE = 2;
    static final int VIEW_REFERENCE = 3;

    // Data Elements
    private boolean ownReferencesPage;
    private ArrayList<Reference> referenceList;
    private String userId;
    private String userName;

    // UI Elements
    private ImageView addReference;
    private LinearLayout back, add;
    private ListView listView;
    private ReferenceAdapter arrayAdapter;

    private int addRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reference);
        Intent intent = getIntent();
        addRef = intent.getIntExtra("addReference", 0);

        userId = intent.getStringExtra(UserInfoFragment.TARGET_USER_ID);
        userName = intent.getStringExtra(UserInfoFragment.TARGET_USER_NAME);
        referenceList = intent.getParcelableArrayListExtra(UserInfoFragment.REFERENCES);
        ownReferencesPage = CacheHelper.getInstance().getSelfInfo().getId().equals(userId);
        initView();
        initData();
    }

    private void initView() {
        addReference = (ImageView) findViewById(R.id.iv_add_reference);

        // If we are viewing our own references
        // TODO implement reference managing for own reference page
        if (ownReferencesPage) {
            addReference.setVisibility(View.INVISIBLE);
        } else {
            addReference.setVisibility(View.VISIBLE);
        }
        addReference.setOnClickListener(this);

        back = (LinearLayout) findViewById(R.id.layout_back);
        back.setOnClickListener(this);

        add = (LinearLayout) findViewById(R.id.layout_add);
        add.setOnClickListener(this);

        listView = (ListView) findViewById(R.id.list);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Reference selectedReference = referenceList.get(position);
                boolean ownReference = CacheHelper.getInstance().getSelfInfo().getId().equals(selectedReference.getUserSrcId());
                if (ownReference) {
                    Intent intent = new Intent(ReferenceActivity.this, ReferenceDialog.class);
                    intent.putExtra(INTENT_REFERENCE, selectedReference);
                    intent.putExtra(INTENT_TARGET_USER_ID, userId);
                    intent.putExtra(INTENT_TARGET_USER_NAME, userName);
                    intent.putExtra(INTENT_REQUEST_CODE, EDIT_REFERENCE);
                    startActivityForResult(intent, EDIT_REFERENCE);
                } else {
                    Intent userInfoIntent = new Intent(ReferenceActivity.this, UserInfoActivity.class);
                    userInfoIntent.putExtra(UserInfoActivity.INTENT_USER_ID, selectedReference.getUserSrcId());
                    startActivity(userInfoIntent);
                }
            }
        });

        if (addRef == 1) {
            Intent intent = new Intent(this, ReferenceDialog.class);
            intent.putExtra(INTENT_TARGET_USER_ID, userId);
            intent.putExtra(INTENT_TARGET_USER_NAME, userName);
            intent.putExtra(INTENT_REQUEST_CODE, ADD_REFERENCE);
            startActivityForResult(intent, ADD_REFERENCE);
        }
    }

    private void initData() {
        arrayAdapter = new ReferenceAdapter(this, referenceList);
        listView.setAdapter(arrayAdapter);
        updateList();
    }

    private void updateList() {
        arrayAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_add_reference:
                Intent intent = new Intent(this, ReferenceDialog.class);
                intent.putExtra(INTENT_TARGET_USER_ID, userId);
                intent.putExtra(INTENT_TARGET_USER_NAME, userName);
                intent.putExtra(INTENT_REQUEST_CODE, ADD_REFERENCE);
                startActivityForResult(intent, ADD_REFERENCE);
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
}
