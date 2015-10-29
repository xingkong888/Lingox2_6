package cn.lingox.android.activity;

import android.app.ProgressDialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChat;
import com.easemob.util.EMLog;
import com.umeng.analytics.MobclickAgent;

import cn.lingox.android.R;

public class DiagnoseActivity extends BaseActivity implements OnClickListener {
    private TextView currentVersion;
    private Button uploadLog;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnose);

        currentVersion = (TextView) findViewById(R.id.tv_version);
        uploadLog = (Button) findViewById(R.id.button_uploadlog);
        uploadLog.setOnClickListener(this);
        String strVersion = "";
        try {
            strVersion = getVersionName();
        } catch (Exception e) {
        }
        if (!TextUtils.isEmpty(strVersion))
            currentVersion.setText(strVersion);
        else
            currentVersion.setText("Failed to find version");
    }

    public void back(View view) {
        finish();
    }

    private String getVersionName() throws Exception {
        PackageManager packageManager = getPackageManager();
        PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(),
                0);
        String version = packInfo.versionName;
        return version;
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
                        // TODO English
                        Toast.makeText(DiagnoseActivity.this, "Log uplodaded successfully",
                                Toast.LENGTH_SHORT).show();
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
                        // TODO English
                        Toast.makeText(DiagnoseActivity.this, "Error uploading log",
                                Toast.LENGTH_SHORT).show();
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
