package cn.lingox.android.helper;

import android.app.Activity;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import cn.lingox.android.R;
import cn.lingox.android.entity.CachePath;
import cn.lingox.android.entity.Path;

public class PathEditDialog {
    private static PathEditDialog instance = null;
    private static Dialog dialog;

    public static synchronized PathEditDialog getInstance() {
        if (instance == null)
            instance = new PathEditDialog();
        return instance;
    }

    public Dialog showDialog(final Activity context, final Path newPath) {
        //Dialog
        dialog = new Dialog(context, R.style.MyDialog);

        View view = LayoutInflater.from(context).inflate(R.layout.row_path_edit_dialog, null);
        Button save = (Button) view.findViewById(R.id.path_dialog_save);
        Button notSave = (Button) view.findViewById(R.id.path_dialog_not_save);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                context.finish();
                if (newPath.getType() != 0) {
                    CachePath.getInstance().setLocalOrTraveler(newPath.getType());
                }
                if (!newPath.getLocation().isEmpty()) {
                    CachePath.getInstance().setLocation(newPath.getLocation());
                }
                if (!newPath.getTitle().isEmpty()) {
                    CachePath.getInstance().setTitle(newPath.getTitle());
                }
                if (!newPath.getText().isEmpty()) {
                    CachePath.getInstance().setDescription(newPath.getText());
                }
                if (!newPath.getImage().isEmpty()) {
                    CachePath.getInstance().setImage(newPath.getImage());
                }
                if (newPath.getDateTime() != 0) {
                    CachePath.getInstance().setStartTime(newPath.getDateTime());
                }
                if (newPath.getEndDateTime() != 0) {
                    CachePath.getInstance().setEndTime(newPath.getEndDateTime());
                }
                if (!newPath.getAvailableTime().isEmpty()) {
                    CachePath.getInstance().setAvabilableTime(newPath.getAvailableTime());
                }
                if (newPath.getCapacity() != 0) {
                    CachePath.getInstance().setGroupSize(newPath.getCapacity());
                }
                if (!newPath.getCost().isEmpty()) {
                    CachePath.getInstance().setBudget(newPath.getCost());
                }
                if (!newPath.getDetailAddress().isEmpty()) {
                    CachePath.getInstance().setAddress(newPath.getDetailAddress());
                }
            }
        });

        notSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                context.finish();
                CachePath.getInstance().setNothing();
            }
        });

        dialog.setContentView(view);
        return dialog;
    }
}
