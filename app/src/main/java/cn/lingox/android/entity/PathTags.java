package cn.lingox.android.entity;

/**
 * 活动标签
 */
public class PathTags {
    private String tag;
    private int type; // 0 未选择 1选中

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "tag=" + tag + ", type=" + type;
    }
}