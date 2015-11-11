package cn.lingox.android.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;

import cn.lingox.android.R;
import cn.lingox.android.activity.imagechooser.PhotoDialog;
import cn.lingox.android.activity.select_area.SelectCountry;
import cn.lingox.android.constants.StringConstant;
import cn.lingox.android.entity.CachePath;
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.ServerHelper;
import cn.lingox.android.task.UploadAvatar;

public class RegisterActivity2 extends FragmentActivity implements OnClickListener {
    private static final String LOG_TAG = "RegisterActivity2";
    private static final int SELECTLOCATION = 125;

    private Button locationButton;
    private ImageView userAvatar;
    private EditText userNickname;
    private String country = "";
    private String province = "";
    private String city = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_2);
        initView();
    }

    private void initView() {
        ImageView backButton = (ImageView) findViewById(R.id.back_button);
        backButton.setVisibility(View.INVISIBLE);
        TextView continueButton = (TextView) findViewById(R.id.continue_button);
        continueButton.setOnClickListener(this);
        locationButton = (Button) findViewById(R.id.location_button);
        locationButton.setOnClickListener(this);
        userAvatar = (ImageView) findViewById(R.id.register_2_avatar);
        userAvatar.setOnClickListener(this);
        userNickname = (EditText) findViewById(R.id.register_2_nickname);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.continue_button:
                continueRegistration();
                break;

            case R.id.location_button:
                Intent intent1 = new Intent(this, SelectCountry.class);
                intent1.putExtra(SelectCountry.SELECTLOCATION, SELECTLOCATION);
                startActivityForResult(intent1, SELECTLOCATION);
                break;

            case R.id.register_2_avatar:
                Intent intent = new Intent(this, PhotoDialog.class);
                intent.putExtra(PhotoDialog.REQUESTED_IMAGE, PhotoDialog.REQUEST_AVATAR);
                startActivityForResult(intent, PhotoDialog.REQUEST_AVATAR);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SELECTLOCATION:
                String str = data.getStringExtra(SelectCountry.SELECTED);
                if (!str.isEmpty()) {
                    setLocation(str);
                    CachePath.getInstance().setLocation(str);
                    locationButton.setText(str);
                }
                break;
            case PhotoDialog.REQUEST_AVATAR:
                if (resultCode != RESULT_OK)
                    Log.d(LOG_TAG, "onActivityResult -> PHOTO_RESULT -> not RESULT_OK");
                else {
                    Uri avatarUri = data.getParcelableExtra(PhotoDialog.SELECTED_SINGLE_IMAGE);
                    new UploadAvatar(this, userAvatar, avatarUri).execute();
                }
                break;
        }
    }

    public void setLocation(String location) {
        String[] str = location.split(", ");
        switch (str.length) {
            case 1://只有国家
                country = str[0];
                break;
            case 2://国家、省份
                country = str[0];
                province = str[1];
                break;
            case 3://国家、省份、城市
                country = str[0];
                province = str[1];
                city = str[2];
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Toast.makeText(this, "Please complete your registration!", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void continueRegistration() {
        if (TextUtils.isEmpty(userNickname.getText().toString().trim())) {
            Toast.makeText(this, "Please enter your nickname", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, String> params = new HashMap<>();
        params.put(StringConstant.userIdStr, CacheHelper.getInstance().getSelfInfo().getId());
        params.put(StringConstant.nicknameStr, userNickname.getText().toString().trim());
        if (!country.isEmpty()) {
            params.put(StringConstant.countryStr, country);
        }
        if (!province.isEmpty()) {
            params.put(StringConstant.provinceStr, province);
        }
        if (!city.isEmpty()) {
            params.put(StringConstant.cityStr, city);
        }
        new ContinueRegistration(params).execute();
    }

    @Override
    protected void onResume() {
        MobclickAgent.onResume(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        MobclickAgent.onPause(this);
        super.onPause();
    }

    private class ContinueRegistration extends AsyncTask<Void, String, Boolean> {
        private HashMap<String, String> userInfo;
        private ProgressDialog pd;

        public ContinueRegistration(HashMap<String, String> userInfo) {
            this.userInfo = userInfo;
            this.pd = new ProgressDialog(RegisterActivity2.this);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd.setCanceledOnTouchOutside(false);
            pd.setCancelable(false);
            pd.setMessage("Updating account information...");
            pd.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                User user = ServerHelper.getInstance().updateUserInfo(userInfo);
                CacheHelper.getInstance().setSelfInfo(user);
                ServerHelper.getInstance().login(user.getEmail(), CacheHelper.getInstance().getPassword());
                return true;
            } catch (Exception e) {
                Log.e("Registration stage 2", "Other Exception caught: " + e.toString());
                publishProgress(null, "Update information failed: " + e.getMessage());
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            if (values[0] != null) {
                pd.setMessage(values[0]);
            }
            if (values[1] != null) {
                Toast.makeText(RegisterActivity2.this, values[1], Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            pd.dismiss();
            if (success) {
                Intent mIntent = new Intent(RegisterActivity2.this, MainActivity.class);
                RegisterActivity2.this.startActivity(mIntent);
                finish();
            }
        }
    }
}