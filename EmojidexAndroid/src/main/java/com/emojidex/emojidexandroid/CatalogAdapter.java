package com.emojidex.emojidexandroid;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class CatalogAdapter extends BaseAdapter
{
    private Context context;
    private LayoutInflater layoutInflater;

    private ArrayList<Drawable> images = new ArrayList<>();

    private static class ViewHolder
    {
        public ImageView imageView;
    }

    public CatalogAdapter (Context context, List<Emoji> emojis)
    {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);

        setImages(emojis);
    }

    private void setImages(List<Emoji> emojis)
    {
        EmojiFormat format = EmojiFormat.toFormat(context.getString(R.string.emoji_format_default));

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
        ViewHolder holder;
        if (convertView == null)
        {
            convertView = layoutInflater.inflate(R.layout.grid_item, null);
            holder = new ViewHolder();
            holder.imageView = (ImageView)convertView.findViewById(R.id.grid_image);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)convertView.getTag();
        }

        holder.imageView.setImageDrawable(images.get(position));

        return convertView;
    }
}
