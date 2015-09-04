package com.emojidex.emojidexandroid;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class CatalogAdapter extends BaseAdapter
{
    private final Context context;
    private final EmojiFormat format;
    private final ArrayList<Emoji> emojies;

    public CatalogAdapter (Context context, List<Emoji> emojies)
    {
        this.context = context;
        format = EmojiFormat.toFormat(context.getString(R.string.emoji_format_catalog));
        this.emojies = new ArrayList<Emoji>(emojies);
    }

    @Override
    public int getCount()
    {
        return emojies.size();
    }

    @Override
    public Object getItem(int position)
    {
        return emojies.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ImageView imageView = (ImageView)convertView;

        if (imageView == null)
        {
            imageView = new ImageView(context);
            int size = (int)context.getResources().getDimension(R.dimen.catalog_icon_size);
            imageView.setLayoutParams(new AbsListView.LayoutParams(size, size));
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        }

        imageView.setImageDrawable(emojies.get(position).getDrawable(format));

        return imageView;
    }
}
