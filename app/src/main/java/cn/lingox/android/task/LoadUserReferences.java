package cn.lingox.android.task;

import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;

import cn.lingox.android.entity.Reference;
import cn.lingox.android.helper.ServerHelper;

/**
 * 获取用户的评论
 */
public class LoadUserReferences extends AsyncTask<Void, String, Boolean> {
    private String userId;
    private Callback callback;
    private ArrayList<Reference> list;

    public LoadUserReferences(String userId, Callback callback) {
        this.userId = userId;
        this.callback = callback;
        list = new ArrayList<>();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean success = false;
        try {
            list.addAll(ServerHelper.getInstance().getUsersReferences(userId));
            success = true;
        } catch (Exception e) {
            Log.e("LoadUserReferences", e.toString());
        }
        return success;
    }


    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        if (success) {
            callback.onSuccess(list);
        } else {
            callback.onFail();
        }
    }

    public interface Callback {
        void onSuccess(ArrayList<Reference> list);

        void onFail();
    }
}