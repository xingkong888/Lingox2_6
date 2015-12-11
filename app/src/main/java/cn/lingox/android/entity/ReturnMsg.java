package cn.lingox.android.entity;

import org.json.JSONObject;

//this should be moved either to the Helper folder as as an inner class in ServerHelper
// the Entity folder is just for major Data Entities (ie server entities)

/**
 * 解析服务器返回数据
 */
public class ReturnMsg {
    private int code;//返回码
    private JSONObject data;//数据主体
    private String remark;//注释

    public ReturnMsg(int code, JSONObject data, String remark) {
        super();
        this.code = code;
        this.data = data;
        this.remark = remark;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public JSONObject getData() {
        return data;
    }

    public void setData(JSONObject data) {
        this.data = data;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "ReturnMsg [code=" + code + ", data=" + data + ", remark="
                + remark + "]";
    }
}
