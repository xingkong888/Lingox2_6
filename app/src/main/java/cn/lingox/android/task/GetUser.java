package cn.lingox.android.task;

import android.os.AsyncTask;
import android.util.Log;

import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.ServerHelper;

public class GetUser extends AsyncTask<Void, String, Boolean> {
    private static final String LOG_TAG = "GetUser";

    private String userId;
    private User user;
    private Callback callback;

    public GetUser(String userId, Callback callback) {
        this.userId = userId;
        this.callback = callback;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        Log.d(LOG_TAG, "Getting User data");
        try {
            if (!LingoXApplication.getInstance().getSkip()) {
                user = ServerHelper.getInstance().getUserInfo(
                        CacheHelper.getInstance().getSelfInfo().getId(), userId);
            } else {
                user = ServerHelper.getInstance().getUserInfo(
                        "55eb1e7aaeed0c53301de44a", userId);
            }
            CacheHelper.getInstance().addUserInfo(user);
            return true;
        } catch (final Exception e) {
            Log.e(LOG_TAG, "GetUser exception: " + e.toString());
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            callback.onSuccess(user);
        } else {
            callback.onFail();
        }
    }

    public interface Callback {
        void onSuccess(User user);

        void onFail();
    }
}