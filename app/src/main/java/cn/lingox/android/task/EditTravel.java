package cn.lingox.android.task;

import android.os.AsyncTask;
import android.util.Log;

import java.util.HashMap;

import cn.lingox.android.entity.TravelEntity;
import cn.lingox.android.helper.ServerHelper;

/**
 * 修改
 */
public class EditTravel extends AsyncTask<Void, String, Boolean> {
    private static final String LOG_TAG = "EditTravel";

    private Callback callback;
    private HashMap<String, String> maps;
    private TravelEntity travelEntity;

    public EditTravel(HashMap<String, String> params, Callback callback) {
        this.callback = callback;
        this.maps = params;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            travelEntity = ServerHelper.getInstance().editTravel(maps);
            return true;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to edit TravelEntity: " + e.toString());
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