package cn.lingox.android.task;

import android.os.AsyncTask;

import java.util.ArrayList;

import cn.lingox.android.entity.TravelEntity;
import cn.lingox.android.helper.ServerHelper;

/**
 * 获取用户参加的travel
 */
public class GetFavouriteTravel extends AsyncTask<Void, String, Boolean> {

    private Callback callback;
    private int page;
    private String userId;
    private ArrayList<TravelEntity> list = new ArrayList<>();

    public GetFavouriteTravel(String userId, int page, Callback callback) {
        this.callback = callback;
        this.page = page;
        this.userId = userId;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            list.addAll(ServerHelper.getInstance().getUserFavouriteTravel(userId, page));
            return true;
        } catch (Exception e) {
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
        void onSuccess(ArrayList<TravelEntity> list);

        void onFail();
    }
}