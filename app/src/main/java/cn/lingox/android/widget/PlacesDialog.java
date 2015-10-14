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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.lingox.android.R;
import cn.lingox.android.entity.User;
import cn.lingox.android.entity.location.Country1;
import cn.lingox.android.helper.JsonHelper;

/**
 * Created by wuyou on 2015/1/29.
 */
public class PlacesDialog extends DialogFragment implements
        Comparator<Country1> {

    private static TextView text;
    private static Context context;
    private static String title;
    private static User user;
    private static Handler handler;
    private static ArrayList<String> data = new ArrayList<>();
    private static ArrayList<String> saveData = new ArrayList<>();
    ;
    private ArrayList<Country1> datas;
    private ListView listView;
    private PlacesAdapter adapter;
    private Button ok, cancel;
    private List<Country1> allCountriesList;
    private ArrayList<String> list = new ArrayList<>();


    public static PlacesDialog newInstance(String title1, Context context1, User user1, TextView text1, Handler handler1,
                                           ArrayList<String> data1) {

        title = title1;
        context = context1;
        user = user1;
        text = text1;
        handler = handler1;
        data.clear();
        data.addAll(data1);
        saveData.clear();
        saveData.addAll(data1);

        PlacesDialog editer = new PlacesDialog();
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        editer.setArguments(bundle);
        return editer;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate view
        View view = inflater.inflate(R.layout.places_country_picker, null);

        listView = (ListView) view.findViewById(R.id.country_picker_listview);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        ok = (Button) view.findViewById(R.id.ok);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                list.clear();
                for (int i = 0; i < data.size(); i++) {
                    list.add(data.get(i).trim());
                }
                text.setText(list.toString().replace("[", "").replace("]", ""));
                user.setVisited(data.toString().replace("[", "").replace("]", ""));
                Message msg = new Message();
                msg.obj = data;
                handler.sendMessage(msg);
                dismiss();
            }
        });
        cancel = (Button) view.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        allCountriesList = JsonHelper.getInstance().getCountries();
        Collections.sort(allCountriesList, this);
        datas = new ArrayList<>();
        datas.addAll(allCountriesList);
        adapter = new PlacesAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int a = data.size();
                if (a == 0) {
                    data.add(datas.get(position).getCountry());
                } else {
                    for (int i = 0; i < a; i++) {
                        if (data.get(i).trim().equals(datas.get(position).getCountry().trim())) {
                            data.remove(i);
                            break;
                        } else if (i == data.size() - 1) {
                            data.add(datas.get(position).getCountry());
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });

        return view;
    }

    /**
     * Support sorting the countries list
     */
    @Override
    public int compare(Country1 lhs, Country1 rhs) {
        return lhs.getCountry().compareTo(rhs.getCountry());
    }

    class PlacesAdapter extends BaseAdapter {
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

            viewHolder.box.setText(datas.get(position).getCountry());
            viewHolder.box.setTag(position);
            viewHolder.box.setChecked(false);
            for (int i = 0; i < data.size(); i++) {
                if (datas.get(position).getCountry().equals(data.get(i).trim())) {
                    viewHolder.box.setChecked(true);
                    break;
                }
            }
            return convertView;
        }

        class ViewHolder {
            CheckBox box;
        }
    }
}
