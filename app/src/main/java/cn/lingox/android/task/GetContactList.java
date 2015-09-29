package cn.lingox.android.task;

import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;

import cn.lingox.android.entity.User;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.ServerHelper;

public class GetContactList extends AsyncTask<Void, String, Boolean> {
    private static final String LOG_TAG = "GetContactList";

    private Callback callback;
    private ArrayList<User> contactList = new ArrayList<>();

    public GetContactList(Callback callback) {
        this.callback = callback;
    }

    public GetContactList() {
        this.callback = new Callback() {
            @Override
            public void onSuccess(ArrayList<User> contactList) {
                Log.d(LOG_TAG, "Retrieved contact list successfully");
            }

            @Override
            public void onFail() {
                Log.d(LOG_TAG, "Failed to retrieve contact list");
            }
        };
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            contactList.addAll(
                    ServerHelper.getInstance().getContactList(
                            CacheHelper.getInstance().getSelfInfo().getId()));
            return true;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to get Contact List: " + e.toString());
            return false;
        }
    }

    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        if (success)
            callback.onSuccess(contactList);
        else
            callback.onFail();
    }

    public interface Callback {
        void onSuccess(ArrayList<User> contactList);

        void onFail();
    }
}