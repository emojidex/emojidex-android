package com.emojidex.emojidexandroid;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by nazuki on 14/01/08.
 */
public class EmojidexKeyboardView extends KeyboardView {
    protected Context context;
    protected LayoutInflater inflater;

    protected PopupWindow popup;
    protected Keyboard.Key key;

    protected TextView popupTextView;
    protected ImageView popupIcon;
    protected LinearLayout variantsLayoutArea;
    protected View variantsArrowLeft;
    protected View variantsArrowRight;
    protected HorizontalScrollView variantsScrollView;
    protected LinearLayout variantsLayout;
    protected String emojiName;
    protected EmojiFormat format;
    protected final float iconSize;

    protected ImageButton imageButton;
    protected boolean first;
    protected boolean registered;

    private final FavoriteManager favoriteManager;

    /**
     * Construct EmojidexKeyboardView object.
     * @param context
     * @param attrs
     * @param defStyle
     */
    public EmojidexKeyboardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        this.inflater = LayoutInflater.from(context);

        format = EmojiFormat.toFormat(context.getResources().getString(R.string.emoji_format_key));
        iconSize = context.getResources().getDimension(R.dimen.ime_key_icon_size);

        favoriteManager = FavoriteManager.getInstance(context);
    }

    /**
     * Behavior when long pressed.
     * @param popupKey
     * @return
     */
    @Override
    public boolean onLongPress(Keyboard.Key popupKey)
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

        final Runnable showPopup = new Runnable()
        {
            @Override
            public void run()
            {
                final View parent = EmojidexKeyboardView.this;

                // Create popup window.
                View view = inflater.inflate(R.layout.popup_favorite, null);
                popup = new PopupWindow(parent);
                popup.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.background_blank));
                popup.setOutsideTouchable(true);
                popup.setFocusable(false);
                popup.setContentView(view);
                popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
                popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
                popup.setOnDismissListener(new PopupWindow.OnDismissListener()
                {
                    @Override
                    public void onDismiss()
                    {
                        // If changed favorite state, save current state.
                        if (registered != first)
                        {
                            if (registered)
                                favoriteManager.addFirst(emojiName);
                            else
                                favoriteManager.remove(emojiName);
                        }
                    }
                });
                popup.showAtLocation(parent, Gravity.CENTER, 0, -parent.getHeight());

                // Set emoji data.
                popupTextView = (TextView)view.findViewById(R.id.favorite_name);
                popupTextView.setText(":" + key.popupCharacters + ":");
                popupTextView.setOnLongClickListener(new OnLongClickListener() {
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
                popupIcon = (ImageView)view.findViewById(R.id.popup_favorite_image);
                popupIcon.setImageDrawable(key.icon);

                // Set register button.
                imageButton = (ImageButton)view.findViewById(R.id.favorite_register_button);
                imageButton.setOnClickListener(createListener());

                // Set star(favorite) icon from current state.
                setCurrentState();

                // Set close button.
                Button closeButton = (Button)view.findViewById(R.id.popup_close_button);
                closeButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        closePopup();
                    }
                });

                // Set seal send button.
                final ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
                final Button sealSendButton = (Button)view.findViewById(R.id.popup_seal_send_button);

                String packageName;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                {
                    final LollipopTaskManager ltm = LollipopTaskManager.getInstance(context);
                    packageName = ltm.getCurrentActivity();
                }
                else
                {
                    packageName = am.getRunningTasks(1).get(0).baseActivity.getPackageName();
                }
                final String targetPackageName = packageName;

                final Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("image/png");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                intent.setPackage(targetPackageName);

                if(context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isEmpty())
                {
                    sealSendButton.setVisibility(GONE);
                }
                else
                {
                    sealSendButton.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            closePopup();

                            // Send intent.
                            final Intent proxyIntent = new Intent(context, SendSealActivity.class);
                            proxyIntent.putExtra(SendSealActivity.EXTRA_EMOJI_NAME, emojiName);
                            proxyIntent.putExtra(SendSealActivity.EXTRA_PACKAGE_NAME, targetPackageName);
                            proxyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                            context.startActivity(proxyIntent);
                        }
                    });
                }

                // Set variants.
                final Context viewContext = view.getContext();
                final FrameLayout variantsMain = (FrameLayout)view.findViewById(R.id.favorite_variants_main);
                variantsScrollView = new CustomHorizontalScrollView(viewContext);
                variantsLayout = new CustomLinearLayout(viewContext);
                variantsLayout.setOrientation(LinearLayout.HORIZONTAL);
                variantsScrollView.addView(variantsLayout);
                variantsMain.addView(variantsScrollView);

                variantsLayoutArea = (LinearLayout)view.findViewById(R.id.favorite_variants_area);

                final int repeatInterval = 100;
                final int scrollSpeed = 500 * repeatInterval / 1000;

                variantsArrowLeft = view.findViewById(R.id.favorite_variants_button_left);
                new RepeatClickHelper(variantsArrowLeft, repeatInterval);
                variantsArrowLeft.setVisibility(INVISIBLE);
                variantsArrowLeft.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        variantsScrollView.smoothScrollBy(-scrollSpeed, 0);
                    }
                });

                variantsArrowRight = view.findViewById(R.id.favorite_variants_button_right);
                new RepeatClickHelper(variantsArrowRight, repeatInterval);
                variantsArrowRight.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        variantsScrollView.smoothScrollBy(scrollSpeed, 0);
                    }
                });

                setVariants();
            }
        };

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
            final String keyIgnore = context.getString(R.string.preference_key_usagestats_check_ignore);
            final boolean ignore = prefs.getBoolean(keyIgnore, false);

            if( !ignore )
            {
                final AppOpsManager aom = (AppOpsManager)context.getSystemService(Context.APP_OPS_SERVICE);
                final int uid = android.os.Process.myUid();
                final int mode = aom.checkOp(AppOpsManager.OPSTR_GET_USAGE_STATS, uid, context.getPackageName());
                boolean result = (mode == AppOpsManager.MODE_ALLOWED);
                if(mode == AppOpsManager.MODE_DEFAULT)
                {
                    result = context.checkPermission("android.permission.PACKAGE_USAGE_STATS", android.os.Process.myPid(), uid) == PackageManager.PERMISSION_GRANTED;
                }

                if(!result)
                {
                    final View dialogView = LayoutInflater.from(context).inflate(R.layout.usagestats_dialog, null);
                    final CheckBox ignoreCheckbox = (CheckBox)dialogView.findViewById(R.id.ignoreCheckBox);

                    final PopupWindow dialog = new PopupWindow(this);
                    dialog.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.background_blank));
                    dialog.setOutsideTouchable(true);
                    dialog.setFocusable(true);
                    dialog.setContentView(dialogView);
                    dialog.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
                    dialog.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
                    dialog.setOnDismissListener(new PopupWindow.OnDismissListener()
                    {
                        @Override
                        public void onDismiss()
                        {
                            // Update preference.
                            prefs.edit().putBoolean(keyIgnore, ignoreCheckbox.isChecked()).commit();
                        }
                    });

                    dialogView.findViewById(R.id.negativeButton).setOnClickListener(new OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            dialog.dismiss();
                            showPopup.run();
                        }
                    });

                    dialogView.findViewById(R.id.positiveButton).setOnClickListener(new OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            dialog.dismiss();
                            if(EmojidexIME.currentInstance != null)
                                EmojidexIME.currentInstance.hideWindow();
                            context.startActivity(
                                    new Intent("android.settings.USAGE_ACCESS_SETTINGS")
                                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            );
                        }
                    });

                    popup = dialog;
                    dialog.showAtLocation(this, Gravity.CENTER, 0, -this.getHeight());

                    return;
                }
            }
        }

        showPopup.run();
    }

    protected void setVariants()
    {
        final Emojidex emojidex = Emojidex.getInstance();
        Emoji emoji = emojidex.getEmoji(emojiName);

        variantsLayoutArea.setVisibility(GONE);

        // Skip if emoji is not found.
        if(emoji == null)
            return;

        // Reference base emoji if has base emoji.
        final String base = emoji.getBase();
        if(base != null)
        {
            emoji = emojidex.getEmoji(base);
            if(emoji == null)
                return;
        }

        // Skip if emoji not has variants.
        final List<String> variants = emoji.getVariants();
        if(variants == null || variants.size() <= 1)
            return;

        // Create variants buttons.
        variantsLayout.removeAllViews();
        for (String name : variants)
        {
            final Emoji variant = emojidex.getEmoji(name);
            if(variant == null)
                continue;

            final ImageButton button = new ImageButton(context);
            final BitmapDrawable drawable = variant.getDrawable(format);
            drawable.setTargetDensity((int) (drawable.getBitmap().getDensity() * iconSize / drawable.getIntrinsicWidth()));
            button.setImageDrawable(drawable);
            button.setBackground(null);
            button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Input emoji to current input connection.
                    EmojidexIME.currentInstance.commitEmoji(variant);

                    // Change keyboard page.
                    EmojidexIME.currentInstance.changeKeyboard(variant);

                    // Close popup.
                    closePopup();
                }
            });
            button.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    changeEmoji(variant);
                    return true;
                }
            });
            variantsLayout.addView(button);
        }

        // Visible variants are.
        if(variantsLayout.getChildCount() > 1)
            variantsLayoutArea.setVisibility(VISIBLE);
    }

    protected void changeEmoji(Emoji emoji)
    {
        emojiName = emoji.getName();
        popupTextView.setText(":" + emojiName + ":");

        final BitmapDrawable icon = emoji.getDrawable(format);
        icon.setTargetDensity((int) (icon.getBitmap().getDensity() * iconSize / icon.getIntrinsicWidth()));
        popupIcon.setImageDrawable(icon);

        setCurrentState();
        setVariants();
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
        if (favoriteManager.contains(emojiName))
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
     * @return  false if popup is not opened.
     */
    public boolean closePopup()
    {
        if (popup != null)
        {
            popup.dismiss();
            popup = null;
            return true;
        }
        return false;
    }

    /**
     * Custom HorizontalScrollView.
     */
    private class CustomHorizontalScrollView extends HorizontalScrollView
    {
        public CustomHorizontalScrollView(Context context) {
            super(context);
        }

        @Override
        protected void onScrollChanged(int l, int t, int oldl, int oldt) {
            super.onScrollChanged(l, t, oldl, oldt);

            final int maxl = variantsLayout.getWidth() - getWidth();

            if(l <= 0 && oldl > 0)
                variantsArrowLeft.setVisibility(INVISIBLE);
            else if(l > 0 && oldl <= 0)
                variantsArrowLeft.setVisibility(VISIBLE);

            else if(l >= maxl && oldl < maxl)
                variantsArrowRight.setVisibility(INVISIBLE);
            else if(l < maxl && oldl >= maxl)
                variantsArrowRight.setVisibility(VISIBLE);
        }
    }

    /**
     * Custom LinearLayout.
     */
    private class CustomLinearLayout extends LinearLayout
    {
        public CustomLinearLayout(Context context) {
            super(context);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);

            if(getWidth() <= variantsScrollView.getWidth())
                variantsArrowRight.setVisibility(INVISIBLE);
        }
    }

    /**
     * Repeat click helper.
     */
    private class RepeatClickHelper implements OnTouchListener
    {
        final View targetView;
        final Handler handler = new Handler();
        final Runnable runnable;

        boolean isRepeatFinish;

        /**
         * Construct object.
         * @param view              Target view.
         */
        public RepeatClickHelper(View view)
        {
            this(view, 100);
        }

        @Override
        protected void finalize() throws Throwable {
            targetView.setOnTouchListener(null);
            isRepeatFinish = true;

            super.finalize();
        }

        /**
         * Construct object.
         * @param view              Target view.
         * @param repeatInterval    Repeat interval.(ms)
         */
        public RepeatClickHelper(View view, final int repeatInterval)
        {
            targetView = view;

            targetView.setOnTouchListener(this);

            runnable = new Runnable() {
                @Override
                public void run() {
                    if(isRepeatFinish)
                        return;

                    targetView.performClick();

                    handler.postDelayed(this, repeatInterval);
                }
            };
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            final int action = event.getAction();
            switch(action)
            {
                case MotionEvent.ACTION_DOWN:
                    isRepeatFinish = false;
                    handler.post(runnable);
                    break;
                case MotionEvent.ACTION_UP:
                    isRepeatFinish = true;
                    break;
                default:
                    break;
            }
            return true;
        }
    }

    private static class LollipopTaskManager
    {
        private static LollipopTaskManager instance = null;

        private final UsageStatsManager usm;
        private long begin;
        private String currentActivity;

        public static LollipopTaskManager getInstance(Context context)
        {
            if(instance == null)
                instance = new LollipopTaskManager(context);
            return instance;
        }

        private LollipopTaskManager(Context context)
        {
            usm = (UsageStatsManager)context.getSystemService("usagestats");
            begin = 0;
            currentActivity = "";
        }

        public String getCurrentActivity()
        {
            final long end = System.currentTimeMillis();

            final UsageEvents events = usm.queryEvents(begin, end);
            final UsageEvents.Event event = new UsageEvents.Event();
            while(events.getNextEvent(event))
            {
                currentActivity = event.getPackageName();
            }

            begin = end;

            return currentActivity;
        }
    }
}
