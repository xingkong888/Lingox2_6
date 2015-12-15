package cn.lingox.android.task;

import android.os.AsyncTask;
import android.util.Log;

import cn.lingox.android.entity.TravelComment;
import cn.lingox.android.helper.ServerHelper;

/**
 * 删除旅行者的体验的评论的异步任务
 * deletecomment
 */
public class DelCommentTravelEntity extends AsyncTask<Void, String, Boolean> {
    private static final String LOG_TAG = "DelCommentTravelEntity";

    private Callback callback;
    private String id;
    private TravelComment comment;

    public DelCommentTravelEntity(String id, Callback callback) {
        this.callback = callback;
        this.id = id;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            comment = ServerHelper.getInstance().deleteTravelComment(id);
            return true;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to delete comment: " + e.toString());
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        if (success) {
            callback.onSuccess(comment);
        } else {
            callback.onFail();
        }
    }

    /**
     * 回调接口
     */
    public interface Callback {
        void onSuccess(TravelComment comment);

        void onFail();
    }
}