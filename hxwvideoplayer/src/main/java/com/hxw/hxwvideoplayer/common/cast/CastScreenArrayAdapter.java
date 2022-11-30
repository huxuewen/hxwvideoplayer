package com.hxw.hxwvideoplayer.common.cast;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hxw.hxwvideoplayer.R;
import com.hxw.hxwvideoplayer.common.cast.DeviceDisplay;

import org.fourthline.cling.model.meta.Icon;

import java.util.List;

/**
 * @author hu xuewen
 * @date 2022/1/15 13:56
 */
public class CastScreenArrayAdapter<T extends DeviceDisplay> extends ArrayAdapter<T> {

    private int resource;

    public CastScreenArrayAdapter(@NonNull Context context, int resource) {
        super(context, resource);
        this.resource = resource;
    }

    public CastScreenArrayAdapter(@NonNull Context context, int resource, @NonNull List<T> objects) {
        super(context, resource, objects);
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        T item = getItem(position);
        Icon[] icons = item.getDevice().getIcons();
        byte[] icon = null;
        if (icons.length > 0) {
            icon = icons[0].getData();
        }
        String title = item.getDevice().getDetails().getFriendlyName();

        ViewHolder viewHolder;
        View view;
        if (convertView == null) {
            Log.i("HomeItemAdaptor.java", "convertView无值");
            view = LayoutInflater.from(getContext()).inflate(resource, parent,
                    false);
            viewHolder = new ViewHolder();
            viewHolder.icon = (ImageView) view.findViewById(R.id.cast_screen_icon);
            viewHolder.title = (TextView) view.findViewById(R.id.cast_screen_title);
            view.setTag(viewHolder);
        } else {
            Log.i("HomeItemAdaptor.java", "convertView有值");
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        Bitmap bitmap;
        if (icon == null) {
            BitmapDrawable drawable = (BitmapDrawable) view.getResources().getDrawable(R.drawable.tv, view.getContext().getTheme());
            bitmap = drawable.getBitmap();
        } else {
            bitmap = BitmapFactory.decodeByteArray(icon, 0, icon.length);
        }

        viewHolder.icon.setImageBitmap(bitmap);
        viewHolder.title.setText(title);
        return view;
    }

    class ViewHolder {
        ImageView icon;
        TextView title;
    }
}
