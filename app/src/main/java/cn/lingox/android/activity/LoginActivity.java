package cn.lingox.android.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.EMCallBack;
import com.umeng.analytics.MobclickAgent;

import cn.lingox.android.R;
import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.ServerHelper;
import cn.lingox.android.task.GetContactList;


public class LoginActivity extends ActionBarActivity implements OnClickListener {

    // Incoming Intent Extras
    public static final String LOGOUT_REQUESTED = LingoXApplication.PACKAGE_NAME + ".LOGOUT_REQUESTED";
    public static final String REGISTRATION_COMPLETE = LingoXApplication.PACKAGE_NAME + ".REGISTRATION_COMPLETE";
    private static final String LOG_TAG = "LoginActivity";

    // UI Elements
    private EditText usernameEditText;
    private EditText passwordEditText;
    private ImageView clear_name;
    private ImageView clear_pwd;

    private TextView login;
    private TextView forgotPasswordButton;
    private TextView registerButton;

    private TextView skip;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pd = new ProgressDialog(LoginActivity.this);
        pd.setCanceledOnTouchOutside(false);
        pd.setCancelable(false);

        if (getIntent().hasExtra(LOGOUT_REQUESTED)) {
            pd.setMessage("Logging out...");
            pd.show();
            CacheHelper.getInstance().logout();
            LingoXApplication.getInstance().logout(new EMCallBack() {

                @Override
                public void onSuccess() {
                    LoginActivity.this.runOnUiThread(new Runnable() {
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
        } else if (getIntent().hasExtra(REGISTRATION_COMPLETE)) {
            Toast.makeText(this, "Registration Complete!", Toast.LENGTH_SHORT).show();
        }
        setContentView(R.layout.activity_login);
        initView();
    }

    private void initView() {
        usernameEditText = (EditText) findViewById(R.id.username);
        passwordEditText = (EditText) findViewById(R.id.password);
        login = (TextView) findViewById(R.id.login_button);
        login.setOnClickListener(this);
        forgotPasswordButton = (TextView) findViewById(R.id.forgot_password);
        forgotPasswordButton.setOnClickListener(this);
        registerButton = (TextView) findViewById(R.id.register_button);
        registerButton.setOnClickListener(this);
        clear_name = (ImageView) findViewById(R.id.iv_clear);
        clear_name.setOnClickListener(this);
        clear_pwd = (ImageView) findViewById(R.id.iv_clear2);
        clear_pwd.setOnClickListener(this);
        skip = (TextView) findViewById(R.id.skip_button);
        skip.setOnClickListener(this);

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
                if (!usernameEditText.getText().toString().equals("")) {
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
                if (!passwordEditText.getText().toString().equals("")) {
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
                    Toast.makeText(LoginActivity.this, "Please enter your Username or Email address", Toast.LENGTH_SHORT).show();
                    usernameEditText.requestFocus();
                } else if (TextUtils.isEmpty(password)) {
                    Toast.makeText(LoginActivity.this, "Please enter your Password", Toast.LENGTH_SHORT).show();
                    passwordEditText.requestFocus();
                } else
                    new Login(username, password).execute();
                break;

            case R.id.forgot_password:
                String email = usernameEditText.getText().toString().trim();
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(LoginActivity.this, "Please enter your Email address to recover your password", Toast.LENGTH_LONG).show();
                    usernameEditText.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(LoginActivity.this, "Please enter a valid Email address", Toast.LENGTH_SHORT).show();
                    usernameEditText.requestFocus();
                } else
                    new ForgotPassword().execute(email);
                break;
            case R.id.register_button:
                // FIXME revert this test
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
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
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
                break;
        }
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
//            Log.d(LOG_TAG, "Login started");
            try {
                User user = ServerHelper.getInstance().login(username, password);

                CacheHelper.getInstance().setSelfInfo(user);
                CacheHelper.getInstance().setPassword(password);
                publishProgress("Getting contact list...", null);
                runOnUiThread(new Runnable() {
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
                Toast.makeText(LoginActivity.this, values[1], Toast.LENGTH_SHORT).show();
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
                try {
                    ServerHelper.getInstance().loginTime(CacheHelper.getInstance().getSelfInfo().getId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
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
                Toast.makeText(LoginActivity.this, values[1], Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            pd.dismiss();
            if (success) {
                Toast.makeText(LoginActivity.this, "Recovery email sent!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}