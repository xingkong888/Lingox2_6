package cn.lingox.android.activity;

import java.util.HashMap;

import cn.lingox.android.entity.UserInfo;

public interface OnLoginListener {
    /**
     * 授权完成调用此接口，返回授权数据，如果需要注册，则返回true
     */
    boolean onSignin(String platform, HashMap<String, Object> res);

    /**
     * 填写完注册信息后调用此接口，返回true表示数据合法，注册页面可以关闭
     */
    boolean onSignUp(UserInfo info);

}
