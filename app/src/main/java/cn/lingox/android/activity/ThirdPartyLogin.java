package cn.lingox.android.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.CookieManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.EMCallBack;
import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;

import cn.lingox.android.R;
import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.ServerHelper;
import cn.lingox.android.task.GetContactList;
import cn.sharesdk.facebook.Facebook;
import cn.sharesdk.framework.FakeActivity;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;

/**
 * 中文注释
 * ShareSDK 官网地址 ： http://www.mob.com </br>
 * 1、这是用2.38版本的sharesdk，一定注意  </br>
 * 2、如果要咨询客服，请加企业QQ 4006852216 </br>
 * 3、咨询客服时，请把问题描述清楚，最好附带错误信息截图 </br>
 * 4、一般问题，集成文档中都有，请先看看集成文档；减少客服压力，多谢合作  ^_^</br></br></br>
 * <p/>
 * The password of demokey.keystore is 123456
 * *ShareSDK Official Website ： http://www.mob.com </br>
 * 1、Be carefully, this sample use the version of 2.11 sharesdk  </br>
 * 2、If you want to ask for help，please add our QQ whose number is 4006852216 </br>
 * 3、Please describe detail of the question , if you have the picture of the bugs or the bugs' log ,that is better </br>
 * 4、Usually, the answers of some normal questions is exist in our user guard pdf, please read it more carefully,thanks  ^_^
 */
public class ThirdPartyLogin extends FakeActivity implements OnClickListener, Callback, PlatformActionListener {
    // Incoming Intent Extras
    public static final String LOGOUT_REQUESTED = LingoXApplication.PACKAGE_NAME + ".LOGOUT_REQUESTED";
    public static final String REGISTRATION_COMPLETE = LingoXApplication.PACKAGE_NAME + ".REGISTRATION_COMPLETE";
    private static final int MSG_SMSSDK_CALLBACK = 1;
    private static final int MSG_AUTH_CANCEL = 2;
    private static final int MSG_AUTH_ERROR = 3;
    private static final int MSG_AUTH_COMPLETE = 4;
    private static final String LOG_TAG = "LoginActivity_New";
    private String smssdkAppkey;
    private String smssdkAppSecret;
    private OnLoginListener signupListener;
    private Handler handler;
    //短信验证的对话框
    private Dialog msgLoginDlg;
    // UI Elements
    private EditText usernameEditText;
    private EditText passwordEditText;
    private ImageView clear_name;
    private ImageView clear_pwd;

    private ImageView thirdWechat, thirdFacebook, thirdQQ, thirdWeibo;

    private TextView login;
    private TextView forgotPasswordButton;
    private TextView registerButton;

    private TextView skip;

    private ProgressDialog pd;
    private CookieManager cookieManager = CookieManager.getInstance();


    /**
     * 设置授权回调，用于判断是否进入注册
     */
    public void setOnLoginListener(OnLoginListener l) {
        this.signupListener = l;
    }

    public void onCreate() {
        // 初始化ui
        handler = new Handler(this);
        activity.setContentView(R.layout.activity_login_new);

        pd = new ProgressDialog(activity);
        pd.setCanceledOnTouchOutside(false);
        pd.setCancelable(false);

        if (activity.getIntent().hasExtra(LOGOUT_REQUESTED)) {
            pd.setMessage("Logging out...");
            pd.show();
            CacheHelper.getInstance().logout();
            LingoXApplication.getInstance().logout(new EMCallBack() {

                @Override
                public void onSuccess() {
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            pd.dismiss();
                        }
                    });
                }

                @Override
                public void onProgress(int progress, String status) {
                }

                @Override
                public void onError(int code, String message) {
                }
            });
        } else if (activity.getIntent().hasExtra(REGISTRATION_COMPLETE)) {
            Toast.makeText(activity, "Registration Complete!", Toast.LENGTH_SHORT).show();
        }
        initView();
    }

    private void initView() {
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        login = findViewById(R.id.login_button);
        login.setOnClickListener(this);
        forgotPasswordButton = findViewById(R.id.forgot_password);
        forgotPasswordButton.setOnClickListener(this);
        registerButton = findViewById(R.id.register_button);
        registerButton.setOnClickListener(this);
        clear_name = findViewById(R.id.iv_clear);
        clear_name.setOnClickListener(this);
        clear_pwd = findViewById(R.id.iv_clear2);
        clear_pwd.setOnClickListener(this);

        thirdWechat = findViewById(R.id.third_wechat);
        thirdWechat.setOnClickListener(this);
        thirdFacebook = findViewById(R.id.third_facebook);
        thirdFacebook.setOnClickListener(this);
        thirdQQ = findViewById(R.id.third_qq);
        thirdQQ.setOnClickListener(this);
        thirdWeibo = findViewById(R.id.third_weibo);
        thirdWeibo.setOnClickListener(this);

        if (!LingoXApplication.getInstance().getSkip()) {
            skip = findViewById(R.id.skip_button);
            skip.setOnClickListener(this);
        }

        usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                passwordEditText.setText(null);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (usernameEditText.getText().toString() != null
                        && !usernameEditText.getText().toString().equals("")) {
                    clear_name.setVisibility(View.VISIBLE);

                } else {
                    clear_name.setVisibility(View.INVISIBLE);
                }
            }
        });
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (passwordEditText.getText().toString() != null
                        && !passwordEditText.getText().toString().equals("")) {
                    clear_pwd.setVisibility(View.VISIBLE);
                } else {
                    clear_pwd.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_button:
                String username = usernameEditText.getText().toString().trim().toLowerCase();
                String password = passwordEditText.getText().toString();
                if (TextUtils.isEmpty(username)) {
                    Toast.makeText(activity, "Please enter your Username or Email address", Toast.LENGTH_SHORT).show();
                    usernameEditText.requestFocus();
                } else if (TextUtils.isEmpty(password)) {
                    Toast.makeText(activity, "Please enter your Password", Toast.LENGTH_SHORT).show();
                    passwordEditText.requestFocus();
                } else
                    new Login(username, password).execute();
                break;

            case R.id.forgot_password:
                String email = usernameEditText.getText().toString().trim();
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(activity, "Please enter your Email address to recover your password", Toast.LENGTH_LONG).show();
                    usernameEditText.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(activity, "Please enter a valid Email address", Toast.LENGTH_SHORT).show();
                    usernameEditText.requestFocus();
                } else
                    new ForgotPassword().execute(email);
                break;

            case R.id.register_button:
                // FIXME revert this test
                startActivity(new Intent(activity, RegisterActivity.class));
                finish();
                break;

            case R.id.iv_clear:
                usernameEditText.setText("");
                break;

            case R.id.iv_clear2:
                passwordEditText.setText("");
                break;

            case R.id.skip_button:
                LingoXApplication.getInstance().setSkip(true);
                startActivity(new Intent(activity, MainActivity.class));
                finish();
                break;
            //TODO 第三方登录
            case R.id.third_wechat:
                //微信登录
                //测试时，需要打包签名；sample测试时，用项目里面的demokey.keystore
                //打包签名apk,然后才能产生微信的登录
                Platform wechat = ShareSDK.getPlatform(Wechat.NAME);

                Toast.makeText(activity, "点了微信", Toast.LENGTH_LONG).show();
                authorize(wechat);
                break;
            case R.id.third_facebook:
                Platform facebook = ShareSDK.getPlatform(Facebook.NAME);

                Toast.makeText(activity, "点了Facebook", Toast.LENGTH_LONG).show();
                authorize(facebook);
                break;
            case R.id.third_qq:
                Platform qq = ShareSDK.getPlatform(QQ.NAME);

                Toast.makeText(activity, "点了QQ", Toast.LENGTH_LONG).show();
                authorize(qq);
                break;

            case R.id.third_weibo:
                Platform weibo = ShareSDK.getPlatform(SinaWeibo.NAME);

                Toast.makeText(activity, "点了微博", Toast.LENGTH_LONG).show();
                authorize(weibo);
                break;
        }
    }

    @Override
    public void onResume() {
        MobclickAgent.onResume(activity);
        super.onResume();
    }

    @Override
    public void onPause() {
        MobclickAgent.onPause(activity);
        super.onPause();
    }

    //执行授权,获取用户信息
    //文档：http://wiki.mob.com/Android_%E8%8E%B7%E5%8F%96%E7%94%A8%E6%88%B7%E8%B5%84%E6%96%99
    private void authorize(Platform plat) {

        plat.setPlatformActionListener(this);
        //关闭SSO授权
        plat.SSOSetting(true);
        plat.showUser(null);
    }

    public void onComplete(Platform platform, int action, HashMap<String, Object> res) {
        if (action == Platform.ACTION_USER_INFOR) {
            Message msg = new Message();
            msg.what = MSG_AUTH_COMPLETE;
            msg.obj = new Object[]{platform.getName(), res};
            handler.sendMessage(msg);

        }
    }

    public void onError(Platform platform, int action, Throwable t) {
        if (action == Platform.ACTION_USER_INFOR) {
            handler.sendEmptyMessage(MSG_AUTH_ERROR);
        }
        t.printStackTrace();
    }

    public void onCancel(Platform platform, int action) {
        if (action == Platform.ACTION_USER_INFOR) {
            handler.sendEmptyMessage(MSG_AUTH_CANCEL);
        }
    }

    @SuppressWarnings("unchecked")
    public boolean handleMessage(Message msg) {
        //授权成功后,获取用户信息，要自己解析，看看oncomplete里面的注释
        //ShareSDK只保存以下这几个通用值
        Platform pf = ShareSDK.getPlatform(getContext(), SinaWeibo.NAME);
//        pf.author();//这个方法每一次都会调用授权，出现授权界面
        //如果要删除授权信息，重新授权
        pf.getDb().removeAccount();
        //调用后，用户就得重新授权，否则下一次就不用授权
        switch (msg.what) {
            case MSG_AUTH_CANCEL: {
                //取消授权
                Toast.makeText(activity, "取消授权", Toast.LENGTH_SHORT).show();
            }
            break;
            case MSG_AUTH_ERROR: {
                //授权失败
                Toast.makeText(activity, "授权失败", Toast.LENGTH_SHORT).show();
            }
            break;
            case MSG_AUTH_COMPLETE: {
                Object[] objs = (Object[]) msg.obj;
                String platform = (String) objs[0];
                HashMap<String, Object> res = (HashMap<String, Object>) objs[1];

                //授权成功
                Toast.makeText(activity, "授权成功", Toast.LENGTH_SHORT).show();

//				LingoXApplication.getInstance().setSkip(false);
//				startActivity(new Intent(activity, MainActivity.class));
//				finish();

                //按正常登录进入应用
                //注册，并登录
//                new Register(String.valueOf(res.get("name")),
//                        String.valueOf(res.get("id")),
//                        String.valueOf(res.get("id"))
//                ).execute();
//
//				if (signupListener != null && signupListener.onSignin(platform, res)) {
//					SignupPage signupPage = new SignupPage();
//					signupPage.setOnLoginListener(signupListener);
//					signupPage.setPlatform(platform);
//					signupPage.show(activity, null);
//				}
            }
            break;
        }
        return false;
    }

    public void show(Context context) {
        ShareSDK.initSDK(context);
        super.show(context, null);
    }

    private class Login extends AsyncTask<Void, String, Boolean> {
        private String username, password;

        public Login(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd.setMessage("Logging in...");
            pd.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Log.d(LOG_TAG, "Login started");

            try {
                User user = ServerHelper.getInstance().login(username, password);
                CacheHelper.getInstance().setSelfInfo(user);
                CacheHelper.getInstance().setPassword(password);

                Log.d(LOG_TAG, "Login to LingoX Server successful");
                Log.d(LOG_TAG, "User: " + user);

                publishProgress("Getting contact list...", null);

                Log.d(LOG_TAG, "Getting contact list from LingoXServer");

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new GetContactList().execute();
                    }
                });
                return true;
            } catch (final Exception e) {
                publishProgress(null, "Login Failed: " + e.getMessage());
                Log.e(LOG_TAG, "Login unsuccessful: " + e.toString());
                CacheHelper.getInstance().logout();

                return false;
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            if (values[0] != null)
                pd.setMessage(values[0]);
            if (values[1] != null)
                Toast.makeText(activity, values[1], Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            pd.dismiss();
            if (success) {
                if (LingoXApplication.getInstance().getSkip()) {
                    MainActivity.getObj().finish();
                }
                LingoXApplication.getInstance().setSkip(false);
                startActivity(new Intent(activity, MainActivity.class));
                finish();
            }
        }
    }

    private class ForgotPassword extends AsyncTask<String, String, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd.setMessage("Recovering password...");
            pd.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            Log.d(LOG_TAG, "Forgot Password started");

            try {
                ServerHelper.getInstance().forgotPassword(params[0]);
                return true;
            } catch (final Exception e) {
                publishProgress(null, "Password recovery Failed: " + e.getMessage());
                Log.e(LOG_TAG, "Recovery failed: " + e.toString());
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            if (values[0] != null)
                pd.setMessage(values[0]);
            if (values[1] != null)
                Toast.makeText(activity, values[1], Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            pd.dismiss();
            if (success) {
                Toast.makeText(activity, "Recovery email sent!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class Register extends AsyncTask<Void, String, Boolean> {
        private String username, password, email;
        private ProgressDialog pd1;

        public Register(String username, String password, String email) {
            this.username = username;
            this.password = password;
            this.email = email;
            pd1 = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd1.setCanceledOnTouchOutside(false);
            pd1.setCancelable(false);
            pd1.setMessage("Registering LingoX Account...");
            pd1.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Log.d(LOG_TAG, "Registering the new user on LingoXServer");

            try {
                // LingoX server: Register the new user
                final User user = ServerHelper.getInstance().register(email, username, password);
                // Store user in Cache
                CacheHelper.getInstance().setSelfInfo(user);
                CacheHelper.getInstance().setPassword(password);

                Log.d(LOG_TAG, "The returned User data is: " + user.toString());

                return true;
            } catch (final Exception e) {
                Log.e("Register", "Other Exception caught: " + e.toString());
                publishProgress(null, "Registration failed: " + e.getMessage());
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            if (values[0] != null)
                pd.setMessage(values[0]);
            if (values[1] != null) {

            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            pd.dismiss();
            if (success) {
                Toast.makeText(activity, "注册成功", Toast.LENGTH_LONG).show();
                //new Login(username,password).execute();

            } else {
                Toast.makeText(activity, "注册失败", Toast.LENGTH_LONG).show();
            }
        }
    }
}
