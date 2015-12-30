package cn.lingox.android.task;

import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;

import cn.lingox.android.entity.Path;
import cn.lingox.android.helper.ServerHelper;

/**
 * 获取用户参加的travel
 */
public class GetFavouriteLocal extends AsyncTask<Void, String, Boolean> {
    private static final String LOG_TAG = "GetUserTravelEntity";

    private Callback callback;
    private int page;
    private String userId;
    private ArrayList<Path> list = new ArrayList<>();

    public GetFavouriteLocal(String userId, int page, Callback callback) {
        this.callback = callback;
        this.page = page;
        this.userId = userId;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            list.addAll(ServerHelper.getInstance().getUsersFavouritePaths(userId, page));
            return true;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to get all TravelEntity: " + e.toString());
            return false;
        }
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
        void onSuccess(ArrayList<Path> list);

        void onFail();
    }
}