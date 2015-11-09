package cn.lingox.android.activity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.HashMap;

import cn.lingox.android.R;
import cn.lingox.android.activity.imagechooser.PhotoDialog;
import cn.lingox.android.activity.select_area.SelectCountry;
import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.constants.StringConstant;
import cn.lingox.android.entity.CachePath;
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.ServerHelper;
import cn.lingox.android.helper.UIHelper;
import cn.lingox.android.task.UploadAvatar;
import cn.lingox.android.utils.CircularImageView;
import cn.lingox.android.utils.FileUtil;

public class EditInfoActivity extends FragmentActivity implements OnClickListener {
    public static final String AVATAR_URL = LingoXApplication.PACKAGE_NAME + "AVATAR_URL";
    private static final String LOG_TAG = "EditInfoActivity";
    private static final int SELECTLOCATION = 147;

    // Data Elements
    private User user;

    // UI Elements
    private TextView editMale, editFemale, editAgeInfo, editCounty, editOK;
    private EditText editNicknameInfo;
    private CircularImageView editAvatar;

    private ImageView xiaoyuandian_male, xiaoyuandian_female, editName;

    private LinearLayout back;

    private RelativeLayout maleLayout, femaleLayout;

    private HashMap<String, String> updateParams = new HashMap<>();
    private String nickName = "";
    private String age = "";
    private String gender = "";

    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int month, int day) {
            editAgeInfo.setText(new StringBuilder()
                            .append(String.valueOf(year)).append("-")
                            .append(String.format("%02d", month + 1)).append("-")
                            .append(String.format("%02d", day))
            );
            age = ("" + String.format("%02d", day) + "" + String.format("%02d", month + 1) + "" + year);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_info);
        user = CacheHelper.getInstance().getSelfInfo();
        initView();
        initData();
    }

    private void initView() {
        back = (LinearLayout) findViewById(R.id.layout_back);
        back.setOnClickListener(this);

        maleLayout = (RelativeLayout) findViewById(R.id.male_layout);
        maleLayout.setOnClickListener(this);
        femaleLayout = (RelativeLayout) findViewById(R.id.female_layout);
        femaleLayout.setOnClickListener(this);

        editNicknameInfo = (EditText) findViewById(R.id.edit_nickname);
        editNicknameInfo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        editMale = (TextView) findViewById(R.id.edit_gender_male);
        editFemale = (TextView) findViewById(R.id.edit_gender_female);
        editAgeInfo = (TextView) findViewById(R.id.edit_age_info);
        editAgeInfo.setOnClickListener(this);
        editCounty = (TextView) findViewById(R.id.edit_country_info);
        editCounty.setOnClickListener(this);
        editOK = (TextView) findViewById(R.id.edit_ok);
        editOK.setOnClickListener(this);

        editAvatar = (CircularImageView) findViewById(R.id.edit_avatar);
        editAvatar.setOnClickListener(this);

        xiaoyuandian_male = (ImageView) findViewById(R.id.xiaoyuandian);
        xiaoyuandian_female = (ImageView) findViewById(R.id.xiaoyuandian2);

        editName = (ImageView) findViewById(R.id.userinfo_edit);
        editName.setOnClickListener(this);
    }

    private void initData() {
        UIHelper.getInstance().imageViewSetPossiblyEmptyUrl(this, editAvatar, user.getAvatar(), "circular");
        if (!user.getNickname().isEmpty()) {
            editNicknameInfo.setText(user.getNickname());
        }
        editCounty.setText(LingoXApplication.getInstance().getLocation(
                user.getCountry(), user.getProvince(), user.getCity()
        ));
        if (!user.getGender().isEmpty()) {
            switch (user.getGender()) {
                case "Male":
                    editMale.setTextColor(Color.rgb(25, 143, 153));
                    editFemale.setTextColor(Color.rgb(0, 0, 0));
                    xiaoyuandian_male.setImageResource(R.drawable.edit_open);
                    xiaoyuandian_female.setImageResource(R.drawable.edit_off);
                    break;
                case "Female":
                    editMale.setTextColor(Color.rgb(0, 0, 0));
                    editFemale.setTextColor(Color.rgb(25, 143, 153));
                    xiaoyuandian_male.setImageResource(R.drawable.edit_off);
                    xiaoyuandian_female.setImageResource(R.drawable.edit_open);
                    break;
            }
        }
        if (user.hasProperlyFormedBirthDate()) {
            UIHelper.getInstance().textViewSetPossiblyNullString(editAgeInfo, user.getBirthDateYear() + "-" + user.getBirthDateMonth() + "-" + user.getBirthDateDay());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.userinfo_edit:
                editNicknameInfo.setFocusable(true);
                editNicknameInfo.setFocusableInTouchMode(true);
                editNicknameInfo.clearFocus();
                editNicknameInfo.requestFocus();
                break;

            case R.id.edit_avatar:
                //选择头像
                Intent intent = new Intent(this, PhotoDialog.class);
                intent.putExtra(PhotoDialog.REQUESTED_IMAGE, PhotoDialog.REQUEST_AVATAR);
                startActivityForResult(intent, PhotoDialog.REQUEST_AVATAR);
                break;
            case R.id.male_layout:
                //设置性别
                gender = "Male";
                editMale.setTextColor(Color.rgb(25, 143, 153));
                editFemale.setTextColor(Color.rgb(0, 0, 0));
                xiaoyuandian_male.setImageResource(R.drawable.edit_open);
                xiaoyuandian_female.setImageResource(R.drawable.edit_off);
                break;
            case R.id.female_layout:
                //设置性别
                gender = "Female";
                editMale.setTextColor(Color.rgb(0, 0, 0));
                editFemale.setTextColor(Color.rgb(25, 143, 153));
                xiaoyuandian_male.setImageResource(R.drawable.edit_off);
                xiaoyuandian_female.setImageResource(R.drawable.edit_open);
                break;
            case R.id.edit_age_info:
                //年龄
                chooseBirthDate();
                break;
            case R.id.edit_country_info:
                //国家
                Intent intent1 = new Intent(this, SelectCountry.class);
                intent1.putExtra(SelectCountry.SELECTLOCATION, SELECTLOCATION);
                startActivityForResult(intent1, SELECTLOCATION);
                break;
            case R.id.edit_ok:
                //提交数据
                new UpdateUserInfo().execute();
                break;
            case R.id.layout_back:
                finish();
                break;
        }
    }

    private void chooseBirthDate() {
        DatePickerDialog dateDialog;
        if (!user.hasProperlyFormedBirthDate()) {
            Calendar c = Calendar.getInstance();
            dateDialog = new DatePickerDialog(this, mDateSetListener,
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH));
        } else {
            dateDialog = new DatePickerDialog(
                    this, mDateSetListener,
                    Integer.parseInt(user.getBirthDateYear()),
                    Integer.parseInt(user.getBirthDateMonth()) - 1,
                    Integer.parseInt(user.getBirthDateDay()));
        }
        dateDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SELECTLOCATION:
                String str = data.getStringExtra(SelectCountry.SELECTED);
                if (!str.isEmpty()) {
                    user.setLocation(str);
                    CachePath.getInstance().setLocation(str);
                    editCounty.setText(str);
                }
                break;
            case PhotoDialog.REQUEST_AVATAR:
                if (resultCode != RESULT_OK)
                    Log.d(LOG_TAG, "onActivityResult -> PHOTO_RESULT -> not RESULT_OK");
                else {
                    Uri avatarUri = data.getParcelableExtra(PhotoDialog.SELECTED_SINGLE_IMAGE);
                    editAvatar.setImageBitmap(FileUtil.getImg(avatarUri.getPath(), this));
                    new UploadAvatar(this, editAvatar, avatarUri).execute();
                }
                break;
        }
    }

    private class UpdateUserInfo extends AsyncTask<Void, String, Boolean> {
        private ProgressDialog pd;

        public UpdateUserInfo() {
            pd = new ProgressDialog(EditInfoActivity.this);
            pd.setCanceledOnTouchOutside(false);
            pd.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            editOK.setClickable(false);
            pd.setMessage("Load...");
            pd.show();
            nickName = editNicknameInfo.getText().toString();
            updateParams.put(StringConstant.userIdStr, user.getId());
            if (!TextUtils.isEmpty(gender) && !gender.contentEquals(user.getGender())) {
                updateParams.put(StringConstant.genderStr, gender);
            }
            if (!TextUtils.isEmpty(nickName) && !nickName.contentEquals(user.getNickname())) {
                updateParams.put(StringConstant.nicknameStr, nickName);
            }
            if (!TextUtils.isEmpty(age)) {
                updateParams.put(StringConstant.birthStr, age);
            }
            if (!user.getCountry().isEmpty()) {
                updateParams.put(StringConstant.countryStr, user.getCountry());
            }
            if (!user.getProvince().isEmpty()) {
                updateParams.put(StringConstant.provinceStr, user.getProvince());
            }
            if (!user.getCity().isEmpty()) {
                updateParams.put(StringConstant.cityStr, user.getCity());
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                CacheHelper.getInstance().setSelfInfo(ServerHelper.getInstance().updateUserInfo(updateParams));
                return true;
            } catch (final Exception e) {
                Log.e(LOG_TAG, "UpdateUserInfo exception caught: " + e.toString());
                publishProgress(null, getString(R.string.error_undate));
                return false;
            }
        }

        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            if (values[0] != null)
                pd.setMessage(values[0]);
            if (values[1] != null)
                Toast.makeText(EditInfoActivity.this, values[1], Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            pd.dismiss();
            editOK.setClickable(true);
            if (success) {
                setResult(RESULT_OK);
                finish();
            }
        }
    }
}