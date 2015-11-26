package cn.lingox.android.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;

import java.util.HashMap;

import cn.lingox.android.entity.TravelComment;
import cn.lingox.android.helper.ServerHelper;

/**
 * createcomment
 */
public class CreateCommentTravelEntity extends AsyncTask<Void, String, Boolean> {
    private static final String LOG_TAG = "UnLikeTravelEntity";

    private Callback callback;
    private HashMap<String, String> map;
    private TravelComment comment;
    private ProgressDialog pd;

    public CreateCommentTravelEntity(Context context,HashMap<String, String> map, Callback callback) {
        this.callback = callback;
        this.map = map;
        pd=new ProgressDialog(context);
        pd.setMessage("Uploading...");
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);
        pd.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            comment = ServerHelper.getInstance().createTravelComment(map);
            return true;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to unlike TravelEntity: " + e.toString());
            return false;
        }
    }

    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        pd.dismiss();
        if (success) {
            callback.onSuccess(comment);
        } else {
            callback.onFail();
        }
    }

    public interface Callback {
        void onSuccess(TravelComment comment);

        void onFail();
    }
}