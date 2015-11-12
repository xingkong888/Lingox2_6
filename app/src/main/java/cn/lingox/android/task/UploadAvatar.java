package cn.lingox.android.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import cn.lingox.android.R;
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.ServerHelper;
import cn.lingox.android.helper.UIHelper;
import cn.lingox.android.utils.FileUtil;

public class UploadAvatar extends AsyncTask<Void, String, Boolean> {
    private static final String LOG_TAG = "UploadAvatar";
    private Context context;
    private ImageView avatarImageView;
    private ProgressDialog pd;
    private Uri photo;

    public UploadAvatar(Context context, ImageView avatarImageView, Uri photo) {
        super();
        this.context = context;
        this.avatarImageView = avatarImageView;
        this.pd = ProgressDialog.show(context, context.getString(R.string.uploading), context.getString(R.string.please_wait), true, false);
        this.photo = photo;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            User user = CacheHelper.getInstance().getSelfInfo();
            user.setAvatar(ServerHelper.getInstance().uploadAvatar(user.getId(), FileUtil.getImg(photo.getPath(), context)));
            CacheHelper.getInstance().setSelfInfo(user);
            return true;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to upload avatar: " + e.toString());
            return false;
        }
    }

    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        if (success) {
            UIHelper.getInstance().imageViewSetPossiblyEmptyUrl(context, avatarImageView, CacheHelper.getInstance().getSelfInfo().getAvatar(), "circular");
        } else {
            Toast.makeText(context, context.getString(R.string.fail_upload_avatar), Toast.LENGTH_SHORT).show();
        }
        pd.dismiss();
    }
}
