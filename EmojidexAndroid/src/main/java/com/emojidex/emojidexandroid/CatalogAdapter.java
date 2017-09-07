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

    private boolean autoDownload = true;

    public CatalogAdapter (Context context, EmojiFormat format, List<Emoji> emojies)
    {
        this.context = context;
        this.format = format;
        this.emojies = new ArrayList<Emoji>(emojies);
    }

    public void autoDownloadImage(boolean flag)
    {
        autoDownload = flag;
    }

    public boolean isAutoDownloadImage()
    {
        return autoDownload;
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

        final Emoji emoji = emojies.get(position);
        if(emoji != null)
        {
            imageView.setContentDescription(emoji.getCode());
            imageView.setImageDrawable(emoji.getDrawable(format, -1, autoDownload));
        }

        return imageView;
    }
}
