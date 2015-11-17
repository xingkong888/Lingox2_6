package cn.lingox.android.widget;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import cn.lingox.android.R;
import cn.lingox.android.entity.SpeakAndInterest;
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.JsonHelper;

/**
 * Created by wuyou on 2015/1/29.
 */
public class SelectDialog extends DialogFragment {

    private static TextView text;
    private static Context context;
    private static User user;
    private static ArrayList<String> speakDatas = new ArrayList<>();
    private static Handler handler;
    private ListView listView;
    private MySelcetAdapter adapter;
    private ArrayList<SpeakAndInterest> datas;

    public static SelectDialog newInstance(String title1, Context context1, User user1, TextView text1, Handler handler1) {

        context = context1;
        user = user1;
        text = text1;
        handler = handler1;
        speakDatas.clear();

        SelectDialog editer = new SelectDialog();
        Bundle bundle = new Bundle();
        bundle.putString("title", title1);
        editer.setArguments(bundle);
        return editer;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate view
        View view = inflater.inflate(R.layout.select_dialog, null);

        listView = (ListView) view.findViewById(R.id.listview);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        Button ok = (Button) view.findViewById(R.id.select_ok);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user.setSpeak(speakDatas.toString().replace("[", "").replace("]", ""));
                text.setTextColor(Color.rgb(25, 143, 153));
                text.setText("");
                text.setText(speakDatas.toString().replace("[", "").replace("]", ""));

                Message msg = new Message();
                msg.obj = speakDatas.toString().replace("[", "").replace("]", "");
                handler.sendMessage(msg);
                dismiss();
            }
        });
        Button cancel = (Button) view.findViewById(R.id.select_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        initData();
        return view;
    }

    private void initData() {
        datas = new ArrayList<>();
        String[] strs = text.getText().toString().split(",");
        for (String str : strs) {
            if (!str.isEmpty()) {
                speakDatas.add(str.trim());
            }
        }
        for (String str : JsonHelper.getInstance().getLanguages()) {
            SpeakAndInterest speakAndInterest = new SpeakAndInterest();
            speakAndInterest.setStr(str);
            speakAndInterest.setFlg(1);
            for (int i = 0, j = speakDatas.size(); i < j; i++) {
                if (speakDatas.get(i).contentEquals(str)) {
                    speakAndInterest.setFlg(2);
                }
            }
            datas.add(speakAndInterest);
        }

        adapter = new MySelcetAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (datas.get(position).getFlg() == 2) {
                    speakDatas.remove(datas.get(position).getStr());
                    datas.get(position).setFlg(1);
                } else {
                    speakDatas.add(datas.get(position).getStr());
                    datas.get(position).setFlg(2);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    class MySelcetAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return datas.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.row_path_edit_3_item, null);
                viewHolder.box = (CheckBox) convertView.findViewById(R.id.path_edit_item);
                viewHolder.box.setTextColor(Color.rgb(0, 0, 0));
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.box.setText(datas.get(position).getStr());
            if (datas.get(position).getFlg() == 1) {
                viewHolder.box.setChecked(false);
            } else {
                viewHolder.box.setChecked(true);
            }
            return convertView;
        }

        class ViewHolder {
            CheckBox box;
        }
    }
}
