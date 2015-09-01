package com.emojidex.emojidexandroid;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class CatalogAdapter extends BaseAdapter
{
    private Context context;
    private ArrayList<Drawable> images = new ArrayList<>();

    public CatalogAdapter (Context context, List<Emoji> emojis)
    {
        this.context = context;
        setImages(emojis);
    }

    private void setImages(List<Emoji> emojis)
    {
        EmojiFormat format = EmojiFormat.toFormat(context.getString(R.string.emoji_format_catalog));

        for (Emoji emoji : emojis)
        {
            images.add(emoji.getDrawable(format));
        }
    }

    @Override
    public int getCount()
    {
        return images.size();
    }

    @Override
    public Object getItem(int position)
    {
        return images.get(position);
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
            int size = (int)(context.getResources().getDisplayMetrics().density * 75);
            imageView.setLayoutParams(new AbsListView.LayoutParams(size, size));
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        }

        imageView.setImageDrawable(images.get(position));

        return imageView;
    }
}
