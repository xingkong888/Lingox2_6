package cn.lingox.android.widget;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import cn.lingox.android.R;
import cn.lingox.android.helper.JsonHelper;

/**
 * Created by wuyou on 2015/1/29.
 */
public class SearchDialog extends DialogFragment {

    private static String which;//标识是哪一个选项
    private static TextView text;
    private static Context context;
    private static String title;
    private ListView listView;
    private MySelcetAdapter adapter;
    private ArrayList<String> datas;

    private LinearLayout layout;

    public static SearchDialog newInstance(String title1, Context context1, TextView text1, String which1) {

        title = title1;
        context = context1;
        which = which1;
        text = text1;

        SearchDialog editer = new SearchDialog();
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        editer.setArguments(bundle);
        return editer;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate view
        View view = inflater.inflate(R.layout.select_dialog, null);

        listView = (ListView) view.findViewById(R.id.listview);

        layout = (LinearLayout) view.findViewById(R.id.okOrclose);
        layout.setVisibility(View.INVISIBLE);

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        // }
        initData();
        return view;
    }

    private void initData() {
        datas = new ArrayList<>();
        for (String str : JsonHelper.getInstance().getLanguages()) {
            datas.add(str);
        }
        adapter = new MySelcetAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                if (position != 0) {
                text.setText("" + datas.get(position));
//                }
                dismiss();
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
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = new ViewHolder();
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.textview, null);
                viewHolder.item = (TextView) convertView.findViewById(R.id.item);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.item.setText(datas.get(position));
            return convertView;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        class ViewHolder {
            TextView item;
        }
    }
}
