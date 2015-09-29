package cn.lingox.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import cn.lingox.android.R;
import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.entity.Reference;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.ServerHelper;

public class ReferenceDialog extends Activity implements OnClickListener {
    // Intent Extras
    static final String ADDED_REFERENCE = LingoXApplication.PACKAGE_NAME + ".ADDED_REFERENCE";
    static final String REFERENCE_BEFORE_EDIT = LingoXApplication.PACKAGE_NAME + ".REFERENCE_BEFORE_EDIT";
    static final String REFERENCE_AFTER_EDIT = LingoXApplication.PACKAGE_NAME + ".REFERENCE_AFTER_EDIT";
    static final String DELETED_REFERENCE = LingoXApplication.PACKAGE_NAME + ".DELETED_REFERENCE";

    // Result Codes
    static final int FAILURE = -1;
    static final int SUCCESS = 0;
    static final int SUCCESS_ADD = 1;
    static final int SUCCESS_EDIT = 2;
    static final int SUCCESS_DELETE = 3;

    // UI Elements
    private TextView cancel;
    private TextView ok;
    private TextView delete;
    private EditText evContent;
    private TextView title;

    // Data Elements
    private String userId;
    private int requestCode;
    private Reference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_reference);

        Intent intent = getIntent();
        userId = intent.getStringExtra(ReferenceActivity.INTENT_TARGET_USER_ID);
        requestCode = intent.getIntExtra(ReferenceActivity.INTENT_REQUEST_CODE,
                ReferenceActivity.VIEW_REFERENCE);
        if (intent.hasExtra(ReferenceActivity.INTENT_REFERENCE))
            reference = intent
                    .getParcelableExtra(ReferenceActivity.INTENT_REFERENCE);

        initView();
    }

    private void initView() {
        evContent = (EditText) findViewById(R.id.et_content);
        ok = (TextView) findViewById(R.id.tv_ok);
        ok.setOnClickListener(this);
        cancel = (TextView) findViewById(R.id.tv_cancel);
        cancel.setOnClickListener(this);
        title = (TextView) findViewById(R.id.title_reference_dialog);
        switch (requestCode) {

            case ReferenceActivity.ADD_REFERENCE:
                break;
            case ReferenceActivity.EDIT_REFERENCE:
                delete = (TextView) findViewById(R.id.tv_delete);
                delete.setOnClickListener(this);
                delete.setVisibility(View.VISIBLE);
                title.setText(getString(R.string.edit_reference));
                title.setVisibility(View.VISIBLE);
                evContent.setText(reference.getContent());
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_ok:
                final String content = evContent.getText().toString();
                final String title = evContent.getText().toString();
                switch (requestCode) {
                    case ReferenceActivity.VIEW_REFERENCE:
                        setResult(SUCCESS);
                        finish();
                        break;

                    case ReferenceActivity.ADD_REFERENCE:
                        new Thread() {
                            public void run() {
                                try {
                                    Reference returnReference = ServerHelper.getInstance()
                                            .createReference(
                                                    CacheHelper.getInstance().getSelfInfo().getId(),
                                                    userId, title, content);
                                    Intent returnIntent = new Intent();
                                    returnIntent.putExtra(ADDED_REFERENCE,
                                            returnReference);
                                    setResult(SUCCESS_ADD, returnIntent);
                                    finish();
                                } catch (Exception e) {
                                    Intent returnIntent = new Intent();
                                    returnIntent.putExtra("remark", e.getMessage());
                                    setResult(FAILURE, returnIntent);
                                    finish();
                                }
                            }
                        }.start();
                        break;

                    case ReferenceActivity.EDIT_REFERENCE:
                        new Thread() {
                            public void run() {
                                try {
                                    Reference editedReference = ServerHelper.getInstance()
                                            .editReference(reference.getId(), title,
                                                    content);

                                    Intent returnIntent = new Intent();
                                    // TODO we dont need both of these
                                    returnIntent.putExtra(REFERENCE_BEFORE_EDIT,
                                            reference);
                                    returnIntent.putExtra(REFERENCE_AFTER_EDIT,
                                            editedReference);
                                    setResult(SUCCESS_EDIT, returnIntent);
                                    finish();
                                } catch (Exception e) {
                                    Intent returnIntent = new Intent();
                                    returnIntent.putExtra("remark", e.getMessage());
                                    setResult(FAILURE, returnIntent);
                                    finish();
                                }
                            }
                        }.start();
                        break;
                }

                break;
            case R.id.tv_cancel:
                setResult(0);
                finish();
                break;
            case R.id.tv_delete:
                new Thread() {
                    public void run() {
                        try {
                            Reference deletedReference = ServerHelper.getInstance()
                                    .deleteReference(reference.getId());

                            Log.d("Dialog_Reference", "Deleted reference result : "
                                    + deletedReference);
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra(DELETED_REFERENCE,
                                    deletedReference);
                            setResult(SUCCESS_DELETE, returnIntent);
                            finish();
                        } catch (Exception e) {
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra("remark", e.getMessage());
                            setResult(FAILURE, returnIntent);
                            finish();
                        }
                    }
                }.start();
                break;
        }
    }
}
