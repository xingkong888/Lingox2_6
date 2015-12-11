package cn.lingox.android.entity;

/**
 * 语言和兴趣
 * 用于选择语言或兴趣时的弹框的实体类
 */
public class SpeakAndInterest {
    private String str;//
    private int flg;//标识是否被选中

    public int getFlg() {
        return flg;
    }

    public void setFlg(int flg) {
        this.flg = flg;
    }

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }
}