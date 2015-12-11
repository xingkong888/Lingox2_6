package cn.lingox.android.entity;

/**
 * 聊天和通知
 */
public class ChatAndNotify {

    private int type;//类型
    private Object obj;//实例

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }
}
