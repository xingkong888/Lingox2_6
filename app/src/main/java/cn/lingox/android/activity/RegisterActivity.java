package cn.lingox.android.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.lingox.android.R;
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.ServerHelper;

public class RegisterActivity extends BaseActivity implements OnClickListener {

    private EditText userNameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText passwordConfirmEditText;
    private ImageView clearName;
    private ImageView clearPassword;
    private ImageView clearEmail;
    private ImageView clearPasswordConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        userNameEditText = (EditText) findViewById(R.id.username);
        passwordEditText = (EditText) findViewById(R.id.password);
        passwordConfirmEditText = (EditText) findViewById(R.id.confirm_password);
        emailEditText = (EditText) findViewById(R.id.email);
        clearEmail = (ImageView) findViewById(R.id.iv_clear);
        clearName = (ImageView) findViewById(R.id.iv_clear2);
        clearPassword = (ImageView) findViewById(R.id.iv_clear3);
        clearPasswordConfirm = (ImageView) findViewById(R.id.iv_clear4);
        TextView registerButton = (TextView) findViewById(R.id.register_button);
        TextView cancelButton = (TextView) findViewById(R.id.cancel_button);
        ImageView backButton = (ImageView) findViewById(R.id.back_button);
        registerButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        backButton.setOnClickListener(this);
        clearEmail.setOnClickListener(this);
        clearName.setOnClickListener(this);
        clearPassword.setOnClickListener(this);
        clearPasswordConfirm.setOnClickListener(this);
        userNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!"".equals(userNameEditText.getText().toString())) {
                    clearName.setVisibility(View.VISIBLE);
                } else {
                    clearName.setVisibility(View.INVISIBLE);
                }
            }
        });
        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!"".equals(emailEditText.getText().toString())) {
                    clearEmail.setVisibility(View.VISIBLE);
                } else {
                    clearEmail.setVisibility(View.INVISIBLE);
                }
            }
        });
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!"".equals(passwordEditText.getText().toString())) {
                    clearPassword.setVisibility(View.VISIBLE);
                } else {
                    clearPassword.setVisibility(View.INVISIBLE);
                }
            }
        });
        passwordConfirmEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!"".equals(passwordConfirmEditText.getText().toString())) {
                    clearPasswordConfirm.setVisibility(View.VISIBLE);
                } else {
                    clearPasswordConfirm.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.register_button:
                String username = userNameEditText.getText().toString().toLowerCase().trim();
                String password = passwordEditText.getText().toString().trim();
                String confirm_pwd = passwordConfirmEditText.getText().toString().trim();
                String email = emailEditText.getText().toString().toLowerCase().trim();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(this, "Please enter an Email address!", Toast.LENGTH_SHORT).show();
                    emailEditText.requestFocus();
                    return;
                } else if (!isEmailValid(email)) {
                    Toast.makeText(v.getContext(), "Email address is invalid!", Toast.LENGTH_SHORT).show();
                    emailEditText.requestFocus();
                    return;
                } else if (TextUtils.isEmpty(username)) {
                    Toast.makeText(this, "Please enter a Username!", Toast.LENGTH_SHORT).show();
                    userNameEditText.requestFocus();
                    return;
                } else if (!isUsernameValid(username)) {
                    //提示用户正确的用户名格式
                    Toast.makeText(this, "Username must contain only letters and numbers, and be between 6-15 characters!",
                            Toast.LENGTH_LONG).show();
                    userNameEditText.requestFocus();
                    return;
                } else if (TextUtils.isEmpty(password)) {
                    Toast.makeText(this, "Please enter a Password!", Toast.LENGTH_SHORT).show();
                    passwordEditText.requestFocus();
                    return;
                } else if (TextUtils.isEmpty(confirm_pwd)) {
                    Toast.makeText(this, "Please confirm your Password!", Toast.LENGTH_SHORT).show();
                    passwordConfirmEditText.requestFocus();
                    return;
                } else if (!password.equals(confirm_pwd)) {
                    Toast.makeText(this, "Your passwords do not match!", Toast.LENGTH_SHORT).show();
                    passwordConfirmEditText.requestFocus();
                    return;
                } else {
                    new Register(username, password, email).execute();
                }
                break;

            case R.id.back_button:
            case R.id.cancel_button:
                goBack();
                break;

            case R.id.iv_clear:
                emailEditText.setText("");
                break;

            case R.id.iv_clear2:
                userNameEditText.setText("");
                break;

            case R.id.iv_clear3:
                passwordEditText.setText("");
                break;

            case R.id.iv_clear4:
                passwordConfirmEditText.setText("");
                break;
        }
    }

    /**
     * 返回登录页面
     */
    private void goBack() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * 判断是否为正确的Email格式
     *
     * @param email 输入的字符串
     * @return true 正确的email格式 false 错误的email格式
     */
    private boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * 判断用户名是否合法
     *
     * @param username 输入的用户名
     * @return true 合法 false 不合法
     */
    private boolean isUsernameValid(String username) {
        String regEx = "^[a-z0-9_-]{6,15}$";
        Matcher matcherObj = Pattern.compile(regEx).matcher(username.toLowerCase());
        return matcherObj.matches();
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
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

    //注册用户
    private class Register extends AsyncTask<Void, String, Boolean> {
        private String username, password, email;
        private ProgressDialog pd;

        public Register(String username, String password, String email) {
            this.username = username;
            this.password = password;
            this.email = email;
            this.pd = new ProgressDialog(RegisterActivity.this);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd.setCanceledOnTouchOutside(false);
            pd.setCancelable(false);
            pd.setMessage("Registering LingoX Account...");
            pd.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                // LingoX server: Register the new user
                User user = ServerHelper.getInstance().register(email, username, password);
                // Store user in Cache
                CacheHelper.getInstance().setSelfInfo(user);
                CacheHelper.getInstance().setPassword(password);
                return true;
            } catch (final Exception e) {
//                Log.e("Register", "Other Exception caught: " + e.toString());
                publishProgress(null, "Registration failed: " + e.getMessage());
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
                Toast.makeText(RegisterActivity.this, values[1], Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            pd.dismiss();
            if (success) {
                Intent registerIntent = new Intent(RegisterActivity.this, RegisterActivity2.class);
                RegisterActivity.this.startActivity(registerIntent);
                RegisterActivity.this.finish();
            }
        }
    }
}
