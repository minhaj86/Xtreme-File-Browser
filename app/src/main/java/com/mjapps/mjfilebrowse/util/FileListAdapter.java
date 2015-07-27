package com.mjapps.mjfilebrowse.util;

/**
 * Created by kishan on 17/04/15.
 */

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mjapps.mjfilebrowse.R;
import com.mjapps.mjfilebrowse.activities.FileListActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FileListAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private static List<Integer> previousCheckBoxList=new ArrayList<>();
    private List<String> itemName;
    private Map<String, Integer> imgid;
    private boolean showCheckBoxes = false;
    private ListView lv;

    public FileListAdapter(Activity mContext, List<String> mItemName, Map<String, Integer> mImgid, ListView paramLv) {
        super(mContext, R.layout.list_file_item, mItemName);
        // TODO Auto-generated constructor stub

        this.context = mContext;
        this.itemName = mItemName;
        this.imgid = mImgid;
        this.lv = paramLv;
    }

    public View getView(final int position, View view, final ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.list_file_item, null, true);
        final CheckBox ck = (CheckBox) rowView.findViewById(R.id.multipleChoice);

        ck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ck.isChecked()) {
                    ((FileListActivity) context).populateSelectedFilePath(position);
                    previousCheckBoxList.add(position);
                } else {
                    ((FileListActivity) context).removeSelectedFilePath(position);
                    previousCheckBoxList.remove(position);
                }
            }
        });
        if (showCheckBoxes) {
            rowView.findViewById(R.id.multipleChoice).setVisibility(View.VISIBLE);

            for(int check:previousCheckBoxList){
                System.out.println(previousCheckBoxList.size());
                if(position==check){
                    ck.setChecked(true);
                }
            }
        } else {
            rowView.findViewById(R.id.multipleChoice).setVisibility(View.GONE);
            previousCheckBoxList.clear();
        }
        TextView txtTitle = (TextView) rowView.findViewById(R.id.title);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.list_image);
        if (!itemName.isEmpty()) {
            txtTitle.setText(itemName.get(position));
            imageView.setImageResource(imgid.get(itemName.get(position)));
        }
        return rowView;

    }


    public void update(List<String> files, Map<String, Integer> mImgid) {
        this.itemName = files;
        this.imgid = mImgid;
        this.notifyDataSetChanged();
    }

    public void update(boolean paramShowCheckBoxes) {
        this.showCheckBoxes = paramShowCheckBoxes;
        this.notifyDataSetChanged();
    }
}