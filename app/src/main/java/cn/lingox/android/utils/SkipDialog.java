package cn.lingox.android.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import cn.lingox.android.R;
import cn.lingox.android.activity.LoginActivity;
import cn.lingox.android.activity.RegisterActivity;

/**
 * 跳过登录时，使用某些功能时的弹框提醒
 */

public class SkipDialog {
    private static Dialog dialog;

    public static Dialog getDialog(final Context context) {
        //Dialog
        dialog = new Dialog(context, R.style.MyDialog);

        View view11 = LayoutInflater.from(context).inflate(R.layout.dialog_layout, null);
        Button register = (Button) view11.findViewById(R.id.dialog_register);
        Button login = (Button) view11.findViewById(R.id.dialog_login);
        ImageView close = (ImageView) view11.findViewById(R.id.close);
        dialog.setContentView(view11);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent regIntent = new Intent(context, RegisterActivity.class);
                context.startActivity(regIntent);
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //登录
                Intent loginIntent = new Intent(context, LoginActivity.class);
                context.startActivity(loginIntent);
            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        return dialog;
    }
}
