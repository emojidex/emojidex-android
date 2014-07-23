package org.genshin.emojidexandroid;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.List;

/**
 * Created by nazuki on 14/01/08.
 */
public class EmojidexKeyboardView extends KeyboardView {
    protected Context context;
    protected LayoutInflater inflater;

    protected PopupWindow popup;
    protected Keyboard.Key key;
    protected String emojiName;

    protected ImageButton imageButton;
    protected boolean first;
    protected boolean registered;

    /**
     * Construct EmojidexKeyboardView object.
     * @param context
     * @param attrs
     * @param defStyle
     * @param inflater
     */
    public EmojidexKeyboardView(Context context, AttributeSet attrs, int defStyle, LayoutInflater inflater) {
        super(context, attrs, defStyle);
        this.context = context;
        this.inflater = inflater;
    }

    /**
     * Behavior when long pressed.
     * @param popupKey
     * @return
     */
    @Override
    public boolean onLongPress(android.inputmethodservice.Keyboard.Key popupKey)
    {
        key = popupKey;
        emojiName = String.valueOf(key.popupCharacters);
        createPopupWindow();

        return true;
    }

    /**
     * Create PopupWindow.
     */
    protected void createPopupWindow()
    {
        closePopup();

        // Create popup window.
        View view = inflater.inflate(R.layout.popup_favorite, null);
        popup = new PopupWindow(this);
        popup.setContentView(view);
        popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.showAtLocation(this, Gravity.CENTER_HORIZONTAL, 0, -this.getHeight());

        // Set emoji data.
        TextView textView = (TextView)view.findViewById(R.id.favorite_name);
        textView.setText(":" + key.popupCharacters + ":");
        textView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData.Item item = new ClipData.Item(":" + key.popupCharacters + ":");
                String[] mimeType = new String[1];
                mimeType[0] = ClipDescription.MIMETYPE_TEXT_PLAIN;
                ClipData data = new ClipData(new ClipDescription("text_data", mimeType), item);
                manager.setPrimaryClip(data);
                Toast.makeText(context, R.string.clipboard, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        ImageView icon = (ImageView)view.findViewById(R.id.popup_favorite_image);
        icon.setImageDrawable(key.icon);

        // Set register button.
        imageButton = (ImageButton)view.findViewById(R.id.favorite_register_button);
        imageButton.setOnClickListener(createListener());

        // Set star(favorite) icon from current state.
        setCurrentState();

        // Set close button.
        Button closeButton = (Button)view.findViewById(R.id.popup_close_button);
        closeButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v) {
                closePopup();
            }
        });

        // Set stamp send button.
        Button stampSendButton = (Button)view.findViewById(R.id.popup_stamp_send_button);
        stampSendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                closePopup();

                final String temporaryFilePath = "tmp.png";
                FileOutputStream fos = null;
                try {
                    // Get emoji data.
                    final EmojiData emojiData = EmojiDataManager.create(context).getEmojiData(emojiName);

                    // Convert image to byte array.
                    final Bitmap bmp = emojiData.getStamp();
                    final ByteArrayOutputStream os = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, os);
                    os.flush();
                    final byte[] w = os.toByteArray();
                    os.close();

                    // Save byte array to temporary file.
                    fos = context.openFileOutput(temporaryFilePath, Context.MODE_WORLD_READABLE);
                    fos.write(w, 0, w.length);
                    fos.flush();

                    // Send intent.
                    final Uri uri = Uri.fromFile(context.getFileStreamPath(temporaryFilePath));
                    final ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
                    final ActivityManager.RunningTaskInfo taskInfo =  am.getRunningTasks(1).get(0);

                    final Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("image/png");
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    intent.setPackage(taskInfo.baseActivity.getPackageName());

                    final Intent proxyIntent = new Intent(context, ProxyActivity.class);
                    proxyIntent.putExtra(Intent.EXTRA_INTENT, intent);
                    proxyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    context.startActivity(proxyIntent);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    try
                    {
                        if (fos != null)
                            fos.close();
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * Create onClickListener.
     * @return
     */
    protected OnClickListener createListener()
    {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Change icon.
                if (registered)
                {
                    imageButton.setImageResource(android.R.drawable.star_big_off);
                    registered = false;
                }
                else
                {
                    imageButton.setImageResource(android.R.drawable.star_big_on);
                    registered = true;
                }
            }
        };
    }

    /**
     * Set star(favorite) icon from current state.
     */
    protected void setCurrentState()
    {
        if (FileOperation.searchEmoji(context, FileOperation.FAVORITES, emojiName))
        {
            imageButton.setImageResource(android.R.drawable.star_big_on);
            first = true;
            registered = true;
        }
        else
        {
            imageButton.setImageResource(android.R.drawable.star_big_off);
            first = false;
            registered = false;
        }
    }

    /**
     * Close popup window.
     */
    public void closePopup()
    {
        // If changed favorite state, save current state.
        if (registered != first)
        {
            if (registered)
                FileOperation.save(context, emojiName, FileOperation.FAVORITES);
            else
                FileOperation.delete(context, emojiName, FileOperation.FAVORITES);
        }

        if (popup != null)
        {
            popup.dismiss();
            popup = null;
        }
    }
}
