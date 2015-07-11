package browser.xtreme.com.xtremefbrowser.util;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import browser.xtreme.com.xtremefbrowser.R;

/**
 * Created by kishan on 04/05/15.
 */
public class ArrayAdapterWithIcon extends ArrayAdapter<String> {
    private List<String> items;
    private List<Integer> images;
    private Activity context;

    public ArrayAdapterWithIcon(Activity context, List<String> items, List<Integer> images) {
        super(context, R.layout.layout_share, items);
        this.images = images;
        this.items = items;
        this.context = context;
    }

    public ArrayAdapterWithIcon(Activity context, String[] items, Integer[] images) {
        super(context, R.layout.layout_share, items);
        this.images = Arrays.asList(images);
        this.items = Arrays.asList(items);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_share, null, true);
        TextView textView = (TextView) view.findViewById(R.id.myOption);
        ImageView image = (ImageView) view.findViewById(R.id.myIcon);
        image.setImageResource(images.get(position));
        textView.setText(items.get(position));
        return view;
    }

}