package cn.lingox.android.task;

import android.os.AsyncTask;
import android.util.Log;

import java.util.HashMap;

import cn.lingox.android.entity.TravelEntity;
import cn.lingox.android.helper.ServerHelper;

/**
 * like
 */
public class LikeTravelEntity extends AsyncTask<Void, String, Boolean> {
    private static final String LOG_TAG = "LikeTravelEntity";

    private Callback callback;
    private HashMap<String, String> map;
    private TravelEntity travelEntity;

    public LikeTravelEntity(HashMap<String, String> map, Callback callback) {
        this.callback = callback;
        this.map = map;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            travelEntity = ServerHelper.getInstance().likeTravel(map);
            return true;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to like TravelEntity: " + e.toString());
            return false;
        }
    }

    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        if (success) {
            callback.onSuccess(travelEntity);
        } else {
            callback.onFail();
        }
    }

    public interface Callback {
        void onSuccess(TravelEntity entity);

        void onFail();
    }
}