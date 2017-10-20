package com.emojidex.emojidexandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.RadioButton;

import com.emojidex.emojidexandroid.comparator.ScoreComparator;
import com.emojidex.emojidexandroid.downloader.DownloadListener;
import com.emojidex.emojidexandroid.downloader.arguments.ImageDownloadArguments;
import com.emojidex.libemojidex.Emojidex.Service.User;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CatalogActivity extends Activity
{
    private GridView gridView;
    private CatalogAdapter adapter;

    private Emojidex emojidex;
    private EmojiFormat format;
    private String currentCategory = null;
    private List<Emoji> currentCatalog = null;

    private SaveDataManager historyManager;
    private FavoriteManager favoriteManager;
    private SaveDataManager searchManager;
    private SaveDataManager indexManager;

    private HorizontalScrollView categoryScrollView;
    private ViewGroup categoriesView;
    private Button categoryAllButton;

    private boolean isPick = false;

    private AdView adView;
    private FirebaseAnalytics analytics;

    private EmojidexIndexUpdater indexUpdater = null;
    private final int indexLoadPageCount = 2;

    private final CustomDownloadListener downloadListener = new CustomDownloadListener();

    private final List<Drawable> animationDrawables = new ArrayList<Drawable>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        setTheme(R.style.IMETheme);

        initData();
        initGridView();
        initCategory();

        // Check intent.
        final Intent intent = getIntent();
        final String action = intent.getAction();
        final String type = intent.getType();

        if(action.equals(Intent.ACTION_PICK) || action.equals(Intent.ACTION_GET_CONTENT))
        {
            if("image/png".equals(type) || "image/*".equals(type))
            {
                isPick = true;
            }
        }

        // Emoji download.
        indexUpdater = new EmojidexIndexUpdater(this, EmojidexKeyboard.create(this).getKeyCountMax());
        indexUpdater.startUpdateThread(indexLoadPageCount);

        new EmojidexUpdater(this).startUpdateThread();

        initAds();
        setAdsVisibility();
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        emojidex.addDownloadListener(downloadListener);
    }

    @Override
    protected void onStop()
    {
        emojidex.removeDownloadListener(downloadListener);

        super.onStop();
    }

    private void initData()
    {
        new CacheAnalyzer().analyze(this);

        emojidex = Emojidex.getInstance();
        emojidex.initialize(this);

        format = EmojiFormat.toFormat(getString(R.string.emoji_format_catalog));

        historyManager = SaveDataManager.getInstance(this, SaveDataManager.Type.CatalogHistory);
        historyManager.load();
        favoriteManager = FavoriteManager.getInstance(this);
        favoriteManager.load();
        searchManager = SaveDataManager.getInstance(this, SaveDataManager.Type.CatalogSearch);
        searchManager.load();
        indexManager = SaveDataManager.getInstance(this, SaveDataManager.Type.Index);
        indexManager.load();

        // Initialize user data.
        final UserData userdata = UserData.getInstance();
        userdata.init(this);
    }

    private void initGridView()
    {
        gridView = (GridView)findViewById(R.id.grid_view);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                final Emoji emoji = (Emoji)parent.getAdapter().getItem(position);
                sendEmoji(emoji);
            }
        });

        gridView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom)
            {
                onListUpdate();
            }
        });

        gridView.setOnScrollListener(new GridViewScrollListener());
    }

    private void initCategory()
    {
        CategoryManager categoryManager = CategoryManager.getInstance();
        categoryManager.initialize(this);

        categoryScrollView = (HorizontalScrollView)findViewById(R.id.catalog_category_scrollview);
        categoriesView = (ViewGroup)findViewById(R.id.catalog_categories);
        categoryAllButton = (Button)findViewById(R.id.catalog_category_button_all);

        for(int i = 0; i < categoryManager.getCategoryCount(); i++)
        {
            RadioButton newButton = new RadioButton(this);

            newButton.setText(categoryManager.getCategoryText(i));
            newButton.setContentDescription(categoryManager.getCategoryId(i));
            newButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    onClickCategoryButton(v);
                }
            });

            categoriesView.addView(newButton);
        }
    }

    public void onClickCategoryButton(View v)
    {
        changeCategory(v.getContentDescription().toString());
    }

    private void changeCategory(String categoryName)
    {
        if(categoryName.equals(currentCategory)) return;

        currentCategory = categoryName;

        Comparator<Emoji> comparator = null;

        if(categoryName.equals(getString(R.string.ime_category_id_history)))
        {
            List<String> emojiNames = historyManager.getEmojiNames();
            currentCatalog = createEmojiList(emojiNames);
        }
        else if(categoryName.equals(getString(R.string.ime_category_id_favorite)))
        {
            final List<String> emojiNames = favoriteManager.getEmojiNames();
            currentCatalog = createEmojiList(emojiNames);
        }
        else if(categoryName.equals(getString(R.string.ime_category_id_search)))
        {
            List<String> emojiNames = searchManager.getEmojiNames();
            currentCatalog = createEmojiList(emojiNames);
        }
        else if(categoryName.equals(getString(R.string.ime_category_id_index)))
        {
            List<String> emojiNames = indexManager.getEmojiNames();
            currentCatalog = createEmojiList(emojiNames);

            // Sort.
            comparator = new ScoreComparator();
        }
        else if(categoryName.equals(getString(R.string.ime_category_id_all)))
        {
            currentCatalog = emojidex.getAllEmojiList();

            // Sort.
            comparator = new ScoreComparator();
        }
        else
        {
            currentCatalog = emojidex.getEmojiList(categoryName);

            // Sort.
            comparator = new ScoreComparator();
        }

        if(currentCatalog == null)
            currentCatalog = new ArrayList<Emoji>();
        else if(comparator != null)
            Collections.sort(currentCatalog, comparator);

        adapter = new CatalogAdapter(this, format, currentCatalog);
        gridView.setAdapter(adapter);
    }

    public void onClickSearchButton(View view)
    {
        Intent intent = new Intent(CatalogActivity.this, SearchActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("Catalog", true);
        startActivity(intent);
    }

    private void sendEmoji(Emoji emoji)
    {
        final SealDownloader downloader = new SealDownloader(this);
        final String emojiName = emoji.getCode();

        downloader.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (downloader.isCanceled())
                    return;

                historyManager.addFirst(emojiName);

                final SealGenerator generator = new SealGenerator(CatalogActivity.this);
                generator.generate(emojiName);

                if (generator.useLowQuality()) {
                    final AlertDialog.Builder alertDialog = new AlertDialog.Builder(CatalogActivity.this);
                    alertDialog.setMessage(R.string.send_seal_not_found);
                    alertDialog.setPositiveButton(R.string.send_seal_not_found_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            sendIntent(generator.getUri());
                        }
                    });
                    alertDialog.show();

                    return;
                }

                sendIntent(generator.getUri());
            }
        });

        downloader.download(emojiName);
    }

    private void sendIntent(Uri uri)
    {
        analytics.logEvent("sealkit_send_seal", new Bundle());

        if(isPick)
        {
            final Intent intent = new Intent();
            intent.setData(uri);
            setResult(RESULT_OK, intent);
            finish();
        }
        else
        {
            final Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/png");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(Intent.createChooser(intent, null));
        }
    }

    private ArrayList<Emoji> createEmojiList(List<String> emojiNames)
    {
        emojidex = Emojidex.getInstance();

        ArrayList<Emoji> emojies = new ArrayList<>(emojiNames.size());
        for (String emojiName : emojiNames)
        {
            final Emoji emoji = emojidex.getEmoji(emojiName);
            if(emoji != null)
                emojies.add(emoji);
        }

        return emojies;
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        historyManager.save();
        searchManager.save();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        initStartCategory();
    }

    /**
     * Initialize start category.
     */
    private void initStartCategory()
    {
        // Load start category from preference.
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        final String key = getString(R.string.preference_key_start_category);
        final String defaultCategory = getString(R.string.ime_category_id_index);
        final String searchCategory = getString(R.string.ime_category_id_search);
        final String startCategory = pref.getString(key, defaultCategory);

        // If start category is "search", always initialize keyboard.
        if(startCategory.equals(searchCategory))
        {
            searchManager.load();
            currentCategory = null;
        }

        // If current category is not null, skip initialize.
        if(currentCategory != null)
            return;

        // Initialize scroll position.
        categoryScrollView.scrollTo(0, 0);

        // Search category.
        final int childCount = categoriesView.getChildCount();
        for(int i = 0;  i < childCount;  ++i)
        {
            final Button button = (Button)categoriesView.getChildAt(i);
            if(button.getContentDescription().equals(startCategory))
            {
                pref.edit().putString(key, defaultCategory).commit();
                button.performClick();
                return;
            }
        }

        // If start category is not found, use category "all".
        pref.edit().putString(key, defaultCategory).commit();
        categoryAllButton.performClick();
    }

    void reloadCategory()
    {
        final String category = currentCategory;
        currentCategory = null;
        int position = gridView.getFirstVisiblePosition();
        changeCategory(category);
        gridView.setSelection(position);
    }

    void invalidate(String emojiName)
    {
        final int first = gridView.getFirstVisiblePosition();
        for(int i = 0;  i < gridView.getChildCount();  ++i)
        {
            final ListAdapter adapter = gridView.getAdapter();
            final View view = gridView.getChildAt(i);
            final Emoji emoji = (Emoji)adapter.getItem(first + i);

            if( emoji.getCode().equals(emojiName) )
            {
                adapter.getView(first + i, view, gridView);
                return;
            }
        }
    }

    /**
     * init AdMob, Firebase.
     */
    private void initAds() {
        adView = (AdView) findViewById(R.id.catalog_adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        analytics = FirebaseAnalytics.getInstance(this);
        analytics.logEvent("sealkit_app_open", new Bundle());
    }

    /**
     * show/hide AdMob.
     */
    private void setAdsVisibility()
    {
        UserData userData = UserData.getInstance();

        if (!userData.isLogined())
        {
            adView.setVisibility(View.VISIBLE);
            return;
        }

        User user = new User();
        if (user.authorize(userData.getUsername(), userData.getAuthToken()))
        {
            if (user.getPremium())
                adView.setVisibility(View.GONE);
        }
    }

    private void getIndexMore()
    {
        if(     currentCategory == null
            ||  !currentCategory.equals("index")    )
            return;
        int count = gridView.getCount() / indexUpdater.getLimit() + indexLoadPageCount;
        indexUpdater.startUpdateThread(count, true);
    }

    private void initAnimation()
    {
        final boolean isAnimating = !animationDrawables.isEmpty();
        animationDrawables.clear();

        final int count = gridView.getChildCount();
        for(int i = 0;  i < count;  ++i)
        {
            final Drawable drawable = ((ImageView)gridView.getChildAt(i)).getDrawable();
            if(drawable instanceof AnimationDrawable)
                animationDrawables.add(drawable);
        }

        // Skip if not found animation emoji or already animating.
        if(animationDrawables.isEmpty() || isAnimating)
            return;

        // Start animation.
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run()
            {
                if(animationDrawables.isEmpty())
                    return;

                for(Drawable drawable : animationDrawables)
                    drawable.invalidateSelf();

                handler.postDelayed(this, 100);
            }
        });
    }

    private void downloadImages()
    {
        final int count = gridView.getChildCount();
        final ImageDownloadArguments[] argumentsArray = new ImageDownloadArguments[count];
        final int first = gridView.getFirstVisiblePosition();
        for(int i = 0;  i < count;  ++i)
        {
            final Emoji e = (Emoji)adapter.getItem(first + i);
            argumentsArray[i] =
                    new ImageDownloadArguments(e.getCode())
                            .setFormat(format)
            ;
        }
        emojidex.getEmojiDownloader().downloadImages(
                argumentsArray
        );
    }

    private void onListUpdate()
    {
        initAnimation();
        downloadImages();
    }

    private class GridViewScrollListener implements AbsListView.OnScrollListener
    {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState)
        {
            switch(scrollState)
            {
                case SCROLL_STATE_IDLE:
                    onListUpdate();
                    break;
                case SCROLL_STATE_TOUCH_SCROLL:
                case SCROLL_STATE_FLING:
                default:
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
        {
            if(firstVisibleItem + visibleItemCount == totalItemCount)
            {
                getIndexMore();
            }
        }
    }

    /**
     * Custom emojidex download downloadListener.
     */
    private class CustomDownloadListener extends DownloadListener
    {
        @Override
        public void onDownloadJson(int handle, String... emojiNames)
        {
            reloadCategory();
        }

        @Override
        public void onDownloadImage(int handle, String emojiName, EmojiFormat format)
        {
            if(CatalogActivity.this.format.equals(format))
                invalidate(emojiName);
        }
    }
}
