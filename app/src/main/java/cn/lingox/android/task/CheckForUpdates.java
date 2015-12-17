package cn.lingox.android.task;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import cn.lingox.android.activity.AppUpdateActivity;
import cn.lingox.android.helper.ServerHelper;

/**
 * 检查应用更新
 */
public class CheckForUpdates extends AsyncTask<Void, String, Boolean> {
    private Context context;

    public CheckForUpdates(Context context) {
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            PackageInfo packInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            int versionCode = packInfo.versionCode;
            return ServerHelper.getInstance().requireUpdate(versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean requireUpdate) {
        super.onPostExecute(requireUpdate);
        if (requireUpdate) {
            Intent intent = new Intent(context, AppUpdateActivity.class);
            context.startActivity(intent);
        }
    }
}