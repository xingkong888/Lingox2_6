package cn.lingox.android.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMChatOptions;
import com.umeng.analytics.MobclickAgent;

import java.util.Locale;

import cn.lingox.android.R;
import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.TimeHelper;


public class SettingsActivity extends Activity implements OnClickListener {

    private RelativeLayout rl_switch_sound;

    private RelativeLayout rl_switch_vibrate;

    private ImageView iv_switch_open_notification;

    private ImageView iv_switch_close_notification;

    private ImageView iv_switch_open_sound;

    private ImageView iv_switch_close_sound;

    private ImageView iv_switch_open_vibrate;

    private ImageView iv_switch_close_vibrate;

    private ImageView iv_switch_open_speaker;

    private ImageView iv_switch_close_speaker;

    private TextView textview1, textview2;


    private EMChatOptions chatOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_conversation_settings);

        RelativeLayout rl_switch_notification = (RelativeLayout) findViewById(R.id.rl_switch_notification);
        rl_switch_sound = (RelativeLayout) findViewById(R.id.rl_switch_sound);
        rl_switch_vibrate = (RelativeLayout) findViewById(R.id.rl_switch_vibrate);
        RelativeLayout rl_switch_speaker = (RelativeLayout) findViewById(R.id.rl_switch_speaker);

        iv_switch_open_notification = (ImageView) findViewById(R.id.iv_switch_open_notification);
        iv_switch_close_notification = (ImageView) findViewById(R.id.iv_switch_close_notification);
        iv_switch_open_sound = (ImageView) findViewById(R.id.iv_switch_open_sound);
        iv_switch_close_sound = (ImageView) findViewById(R.id.iv_switch_close_sound);
        iv_switch_open_vibrate = (ImageView) findViewById(R.id.iv_switch_open_vibrate);
        iv_switch_close_vibrate = (ImageView) findViewById(R.id.iv_switch_close_vibrate);
        iv_switch_open_speaker = (ImageView) findViewById(R.id.iv_switch_open_speaker);
        iv_switch_close_speaker = (ImageView) findViewById(R.id.iv_switch_close_speaker);
        Button logoutBtn = (Button) findViewById(R.id.btn_logout);
        LinearLayout back = (LinearLayout) findViewById(R.id.layout_back);
        logoutBtn.setText(getString(R.string.button_logout) + "(" + CacheHelper.getInstance().getSelfInfo().getUsername() + ")");

        textview1 = (TextView) findViewById(R.id.textview1);
        textview2 = (TextView) findViewById(R.id.textview2);

        LinearLayout blacklistContainer = (LinearLayout) findViewById(R.id.ll_black_list);
        LinearLayout llDiagnose = (LinearLayout) findViewById(R.id.ll_diagnose);
        blacklistContainer.setOnClickListener(this);
        rl_switch_notification.setOnClickListener(this);
        rl_switch_sound.setOnClickListener(this);
        rl_switch_vibrate.setOnClickListener(this);
        rl_switch_speaker.setOnClickListener(this);
        logoutBtn.setOnClickListener(this);
        back.setOnClickListener(this);
        llDiagnose.setOnClickListener(this);
        RelativeLayout rl_language = (RelativeLayout) findViewById(R.id.rl_language);
        rl_language.setOnClickListener(this);
        chatOptions = EMChatManager.getInstance().getChatOptions();
        if (chatOptions.getNotificationEnable()) {
            iv_switch_open_notification.setVisibility(View.VISIBLE);
            iv_switch_close_notification.setVisibility(View.INVISIBLE);
        } else {
            iv_switch_open_notification.setVisibility(View.INVISIBLE);
            iv_switch_close_notification.setVisibility(View.VISIBLE);
        }
        if (chatOptions.getNoticedBySound()) {
            iv_switch_open_sound.setVisibility(View.VISIBLE);
            iv_switch_close_sound.setVisibility(View.INVISIBLE);
        } else {
            iv_switch_open_sound.setVisibility(View.INVISIBLE);
            iv_switch_close_sound.setVisibility(View.VISIBLE);
        }
        if (chatOptions.getNoticedByVibrate()) {
            iv_switch_open_vibrate.setVisibility(View.VISIBLE);
            iv_switch_close_vibrate.setVisibility(View.INVISIBLE);
        } else {
            iv_switch_open_vibrate.setVisibility(View.INVISIBLE);
            iv_switch_close_vibrate.setVisibility(View.VISIBLE);
        }

        if (chatOptions.getUseSpeaker()) {
            iv_switch_open_speaker.setVisibility(View.VISIBLE);
            iv_switch_close_speaker.setVisibility(View.INVISIBLE);
        } else {
            iv_switch_open_speaker.setVisibility(View.INVISIBLE);
            iv_switch_close_speaker.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_switch_notification:
                if (iv_switch_open_notification.getVisibility() == View.VISIBLE) {
                    iv_switch_open_notification.setVisibility(View.INVISIBLE);
                    iv_switch_close_notification.setVisibility(View.VISIBLE);
                    rl_switch_sound.setVisibility(View.GONE);
                    rl_switch_vibrate.setVisibility(View.GONE);
                    textview1.setVisibility(View.GONE);
                    textview2.setVisibility(View.GONE);
                    chatOptions.setNotificationEnable(false);
                    EMChatManager.getInstance().setChatOptions(chatOptions);

                    CacheHelper.getInstance().setSettingMsgNotification(false);
                } else {
                    iv_switch_open_notification.setVisibility(View.VISIBLE);
                    iv_switch_close_notification.setVisibility(View.INVISIBLE);
                    rl_switch_sound.setVisibility(View.VISIBLE);
                    rl_switch_vibrate.setVisibility(View.VISIBLE);
                    textview1.setVisibility(View.VISIBLE);
                    textview2.setVisibility(View.VISIBLE);
                    chatOptions.setNotificationEnable(true);
                    EMChatManager.getInstance().setChatOptions(chatOptions);
                    CacheHelper.getInstance().setSettingMsgNotification(true);
                }
                break;

            case R.id.rl_switch_sound:
                if (iv_switch_open_sound.getVisibility() == View.VISIBLE) {
                    iv_switch_open_sound.setVisibility(View.INVISIBLE);
                    iv_switch_close_sound.setVisibility(View.VISIBLE);
                    chatOptions.setNoticeBySound(false);
                    EMChatManager.getInstance().setChatOptions(chatOptions);
                    CacheHelper.getInstance().setSettingMsgSound(false);
                } else {
                    iv_switch_open_sound.setVisibility(View.VISIBLE);
                    iv_switch_close_sound.setVisibility(View.INVISIBLE);
                    chatOptions.setNoticeBySound(true);
                    EMChatManager.getInstance().setChatOptions(chatOptions);
                    CacheHelper.getInstance().setSettingMsgSound(true);
                }
                break;

            case R.id.rl_switch_vibrate:
                if (iv_switch_open_vibrate.getVisibility() == View.VISIBLE) {
                    iv_switch_open_vibrate.setVisibility(View.INVISIBLE);
                    iv_switch_close_vibrate.setVisibility(View.VISIBLE);
                    chatOptions.setNoticedByVibrate(false);
                    EMChatManager.getInstance().setChatOptions(chatOptions);
                    CacheHelper.getInstance().setSettingMsgVibrate(false);
                } else {
                    iv_switch_open_vibrate.setVisibility(View.VISIBLE);
                    iv_switch_close_vibrate.setVisibility(View.INVISIBLE);
                    chatOptions.setNoticedByVibrate(true);
                    EMChatManager.getInstance().setChatOptions(chatOptions);
                    CacheHelper.getInstance().setSettingMsgVibrate(true);
                }
                break;

            case R.id.rl_switch_speaker:
                if (iv_switch_open_speaker.getVisibility() == View.VISIBLE) {
                    iv_switch_open_speaker.setVisibility(View.INVISIBLE);
                    iv_switch_close_speaker.setVisibility(View.VISIBLE);
                    chatOptions.setUseSpeaker(false);
                    EMChatManager.getInstance().setChatOptions(chatOptions);
                    CacheHelper.getInstance().setSettingMsgSpeaker(false);
                } else {
                    iv_switch_open_speaker.setVisibility(View.VISIBLE);
                    iv_switch_close_speaker.setVisibility(View.INVISIBLE);
                    chatOptions.setUseSpeaker(true);
                    EMChatManager.getInstance().setChatOptions(chatOptions);
                    CacheHelper.getInstance().setSettingMsgVibrate(true);
                }
                break;

            case R.id.btn_logout:
                LingoXApplication.getInstance().setSkip(false);
                logout();
                break;

            case R.id.ll_diagnose:
                startActivity(new Intent(SettingsActivity.this, DiagnoseActivity.class));
                break;

            case R.id.layout_back:
                SettingsActivity.this.finish();
                break;

            case R.id.rl_language:
                final CharSequence[] items = {getString(R.string.auto), getString(R.string.english), getString(R.string.chinese)};
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.pick_language));
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        if (item == 0)
                            setLocale(Locale.getDefault().getLanguage());
                        if (item == 1)
                            setLocale(Locale.ENGLISH.getLanguage());
                        else if (item == 2)
                            setLocale(Locale.CHINESE.getLanguage());

                    }
                }).show();
                break;
        }

    }

    private void setLocale(String lang) {
        CacheHelper.getInstance().setKeySettingLanguage(lang);
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        TimeHelper.getInstance().setLocale(myLocale);
        res.updateConfiguration(conf, dm);
        setResult(MainActivity.RESULT_CODE_RESET_LANGUAGE);
        finish();
    }

    void logout() {
        ChatFragment.getObj().setIsFirst(true);
        setResult(MainActivity.RESULT_CODE_LOGOUT);
        finish();
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
}
