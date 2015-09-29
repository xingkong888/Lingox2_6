package cn.lingox.android.entity;

import org.json.JSONObject;

// TODO this should be moved either to the Helper folder as as an inner class in ServerHelper
// the Entity folder is just for major Data Entities (ie server entities)
public class ReturnMsg {

    private int code;
    private JSONObject data;
    private String remark;

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
