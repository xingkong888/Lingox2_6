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

import java.util.ArrayList;

import cn.lingox.android.R;
import cn.lingox.android.entity.Photo;
import cn.lingox.android.entity.SpeakAndInterest;
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.JsonHelper;
public class PhotoTagsSelectDialog extends DialogFragment {

    private static Context context;
    private static String title;
    private static User user;


    private static ArrayList<String> tags;
    private static Handler handler;
    private ListView listView;
    private MySelcetAdapter adapter;
    private ArrayList<SpeakAndInterest> datas;
    private int checkedInterest = 0;

    public static PhotoTagsSelectDialog newInstance(String title1, Context context1, Object obj,
                                                    Handler handler1) {
        Photo photo;
        title = title1;
        context = context1;
        if (title.contentEquals("photo")) {
            photo = (Photo) obj;
        }
        if (title.contentEquals("interest")) {
            user = (User) obj;
        }
        handler = handler1;
        tags = new ArrayList<>();
        tags.clear();

        PhotoTagsSelectDialog editer = new PhotoTagsSelectDialog();
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
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        Button ok = (Button) view.findViewById(R.id.select_ok);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (title.contentEquals("photo")) {
                    dismiss();
                } else {
                    user.setInterests(null);
                    user.setInterests(tags);

                    Message msg = new Message();
                    msg.what = 1;
                    msg.obj = user.getInterests().toString().replace("[", "").replace("]", "");
                    handler.sendMessage(msg);
                    dismiss();
                }
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
        if (title.contentEquals("photo")) {
        } else {
            if (user.getInterests().size() > 0) {
                for (int i = 0; i < user.getInterests().size(); i++) {
                    if (!user.getInterests().get(i).isEmpty())
                        tags.add(user.getInterests().get(i));
                }
            }
        }

        String str;
        for (int i = 0; i < JsonHelper.getInstance().getAllTags().size(); i++) {
            str = JsonHelper.getInstance().getAllTags().get(i);
            SpeakAndInterest speakAndInterest = new SpeakAndInterest();
            speakAndInterest.setStr(str);
            speakAndInterest.setFlg(1);
            if (title.contentEquals("photo")) {
                for (int j = 0; j < tags.size(); j++) {
                    if (Integer.valueOf(tags.get(j)) == i) {
                        speakAndInterest.setFlg(2);
                    }
                }
            } else {
                for (int j = 0; j < tags.size(); j++) {
                    if (tags.get(j).contentEquals(str)) {
                        speakAndInterest.setFlg(2);
                    }
                }
            }
            datas.add(speakAndInterest);
        }
        adapter = new MySelcetAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (title.contentEquals("photo")) {
                    if (datas.get(position).getFlg() == 1) {
                        if (checkedInterest < 3) {
                            tags.add(String.valueOf(position));
                            checkedInterest++;
                            datas.get(position).setFlg(2);
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        tags.remove(String.valueOf(position));
                        checkedInterest--;
                        datas.get(position).setFlg(1);
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    if (datas.get(position).getFlg() == 1) {
                        if (checkedInterest < 3) {
                            tags.add(datas.get(position).getStr());
//                            checkedInterest++;
                            datas.get(position).setFlg(2);
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        tags.remove(datas.get(position).getStr());
//                        checkedInterest--;
                        datas.get(position).setFlg(1);
                        adapter.notifyDataSetChanged();
                    }
                }
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
