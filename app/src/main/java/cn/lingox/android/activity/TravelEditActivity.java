package cn.lingox.android.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import cn.lingox.android.R;

/**
 * 创建travel数据
 */
public class TravelEditActivity extends FragmentActivity implements OnClickListener {

    private ImageView bg, colse, back;
    private LinearLayout page1, page2, page3, page4;
    private EditText traveling, describe;
    private ListView listView;
    private Button from, to;
    private EditText provide;
    private Button next;
    private TextView pageNum;

    private int page = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_travel_edit);

        initView();
    }

    private void initView() {
        bg = (ImageView) findViewById(R.id.travel_bg);
        next = (Button) findViewById(R.id.travel_edit_next);
        next.setOnClickListener(this);
//        colse=(ImageView)findViewById(R.id.travel_edit_close);
//        colse.setOnClickListener(this);
        back = (ImageView) findViewById(R.id.travel_edit_back);
        back.setOnClickListener(this);
        pageNum = (TextView) findViewById(R.id.travel_edit_num);

//        第一页
        page1 = (LinearLayout) findViewById(R.id.travel_edit_page_1);
        traveling = (EditText) findViewById(R.id.travel_page_1_edit);
//        第二页
        page2 = (LinearLayout) findViewById(R.id.travel_edit_page_2);
        describe = (EditText) findViewById(R.id.travel_page_2_describe);
        listView = (ListView) findViewById(R.id.travel_page_2_tags);
//        第三页
        page3 = (LinearLayout) findViewById(R.id.travel_edit_page_3);
        from = (Button) findViewById(R.id.travel_page_3_from);
        to = (Button) findViewById(R.id.travel_page_3_to);
//        第四页
        page4 = (LinearLayout) findViewById(R.id.travel_edit_page_4);
        provide = (EditText) findViewById(R.id.travel_page_4_edit);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.travel_edit_next:
                nextClick(0);
                break;
//            case R.id.travel_edit_close:
////                nextClick(0);
//                break;
            case R.id.travel_edit_back:
                nextClick(1);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        nextClick(1);
    }

    /**
     * 0 on behalf of the next
     * 1 on behalf of the back
     * page1-->填写要去的地方
     * page2-->填写详细描述及选择类型
     * page3-->选择时间
     * page4-->填写当你作为本地人的时候，你能提供什么
     *
     * @param nextOrBack on behalf of the page number
     */
    private void nextClick(int nextOrBack) {
        if (nextOrBack == 0) {
            page++;
        } else if (nextOrBack == 1) {
            page--;
        }
        if (page > 0) {
            switch (page) {
                case 1://填写要去的地方
                    page1.setVisibility(View.VISIBLE);
                    page2.setVisibility(View.GONE);
                    page3.setVisibility(View.GONE);
                    page4.setVisibility(View.GONE);
                    pageNum.setText("1/4");
                    bg.setImageResource(R.drawable.active_map_01_320dp520dp);
                    break;
                case 2://填写详细描述及选择类型
                    if (traveling.getText().toString().isEmpty()) {
                        page--;
                        break;
                    }
                    page2.setVisibility(View.VISIBLE);
                    page1.setVisibility(View.GONE);
                    page3.setVisibility(View.GONE);
                    page4.setVisibility(View.GONE);
                    pageNum.setText("2/4");
                    bg.setImageResource(R.drawable.active_map_02_320dp520dp);
                    break;
                case 3://选择时间
                    if (describe.getText().toString().isEmpty()) {
                        page--;
                        break;
                    }
                    page3.setVisibility(View.VISIBLE);
                    page2.setVisibility(View.GONE);
                    page1.setVisibility(View.GONE);
                    page4.setVisibility(View.GONE);
                    pageNum.setText("3/4");
                    bg.setImageResource(R.drawable.active_map_03_320dp520dp);
                    break;
                case 4://填写当你作为本地人的时候，你能提供什么
                    if (from.getText().toString().isEmpty() || to.getText().toString().isEmpty()) {
                        page--;
                        break;
                    }
                    page4.setVisibility(View.VISIBLE);
                    page2.setVisibility(View.GONE);
                    page3.setVisibility(View.GONE);
                    page1.setVisibility(View.GONE);
                    pageNum.setText("4/4");
                    bg.setImageResource(R.drawable.active_map_04_320dp520dp);
                    break;
                case 5://提交数据
                    if (provide.getText().toString().isEmpty()) {
                        page--;
                        break;
                    }
                    break;
            }
            if (page >= 4) {
                next.setText(getString(R.string.create));
            } else {
                next.setText(getString(R.string.path_edit_next));
            }
        } else {
            if (page <= 0) {
                finish();
            } else {
                back.setVisibility(View.VISIBLE);
                pageNum.setVisibility(View.VISIBLE);
            }
        }
    }
}