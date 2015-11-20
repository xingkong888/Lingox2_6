package cn.lingox.android.task;

import android.os.AsyncTask;
import android.util.Log;

import cn.lingox.android.entity.TravelEntity;
import cn.lingox.android.helper.ServerHelper;

/**
 * 删除
 */
public class DeleteTravel extends AsyncTask<Void, String, Boolean> {
    private static final String LOG_TAG = "DeleteTravel";

    private Callback callback;
    private String id;
    private TravelEntity travelEntity;

    public DeleteTravel(String id, Callback callback) {
        this.callback = callback;
        this.id = id;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            travelEntity = ServerHelper.getInstance().deleteTravel(id);
            return true;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to delete TravelEntity: " + e.toString());
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