package cn.lingox.android.activity;

import android.app.ProgressDialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChat;
import com.easemob.util.EMLog;
import com.umeng.analytics.MobclickAgent;

import cn.lingox.android.R;

public class DiagnoseActivity extends BaseActivity implements OnClickListener {
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnose);

        TextView currentVersion = (TextView) findViewById(R.id.tv_version);
        findViewById(R.id.button_uploadlog).setOnClickListener(this);
        String strVersion = "";
        try {
            strVersion = getVersionName();
        } catch (Exception ignored) {
        }
        if (!TextUtils.isEmpty(strVersion)) {
            currentVersion.setText(strVersion);
        } else {
            currentVersion.setText("Failed to find version");
        }
    }

    public void back(View view) {
        finish();
    }

    private String getVersionName() throws Exception {
        PackageManager packageManager = getPackageManager();
        PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(), 0);

        return packInfo.versionName;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_uploadlog:
                uploadlog();
                break;
        }
    }

    public void uploadlog() {
        if (progressDialog == null)
            progressDialog = new ProgressDialog(this);
        // TODO English
        progressDialog.setMessage("Uploading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        EMChat.getInstance().uploadLog(new EMCallBack() {

            @Override
            public void onSuccess() {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        Toast.makeText(DiagnoseActivity.this, "Log uplodaded successfully", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onProgress(final int progress, String status) {
            }

            @Override
            public void onError(int code, String message) {
                EMLog.e("DiagnoseActivity", message);
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        Toast.makeText(DiagnoseActivity.this, "Error uploading log", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
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
