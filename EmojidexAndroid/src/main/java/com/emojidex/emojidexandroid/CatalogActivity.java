package com.emojidex.emojidexandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.RadioButton;
import android.widget.Toast;

import com.emojidex.emojidexandroid.animation.EmojidexAnimationDrawable;
import com.emojidex.emojidexandroid.animation.updater.AnimationUpdater;
import com.emojidex.emojidexandroid.comparator.EmojiComparator;
import com.emojidex.emojidexandroid.downloader.DownloadListener;
import com.emojidex.emojidexandroid.downloader.arguments.ImageDownloadArguments;
import com.emojidex.libemojidex.Emojidex.Service.User;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class CatalogActivity extends Activity
{
    private static final String EMOJIDEX_URL = "https://www.emojidex.com";
    private static final String EMOJIDEX_QUERY = "?user_agent=emojidexNativeClient";

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
    private SaveDataManager myEmojiManager;

    private HorizontalScrollView categoryScrollView;
    private ViewGroup categoriesView;
    private Button categoryAllButton;
    private Button myEmojiButton;
    private ImageButton loginButton;
    private ImageButton followingButton;
    private ImageButton followersButton;

    private boolean isPick = false;

    private AdView adView;
    private FirebaseAnalytics analytics;

    private EmojidexIndexUpdater indexUpdater = null;
    private EmojidexMyEmojiUpdater myEmojiUpdater = null;
    private final int indexLoadPageCount = 2;

    private final CustomDownloadListener downloadListener = new CustomDownloadListener();

    private final List<EmojidexAnimationDrawable> animationDrawables = new ArrayList<EmojidexAnimationDrawable>();
    private final CustomAnimationUpdater animationUpdater = new CustomAnimationUpdater();

    private EmojiComparator.SortType currentSortType;
    private boolean standardOnly;

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
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float viewSize = getResources().getDimension(R.dimen.catalog_icon_size) + 10 * metrics.density;
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        int columns = (int)(point.x / viewSize);
        int rows = (int)(point.y / viewSize - 1);

        indexUpdater = new EmojidexIndexUpdater(this, columns * rows);
        indexUpdater.startUpdateThread(indexLoadPageCount);

        new EmojidexUpdater(this).startUpdateThread();

        UserData userdata = UserData.getInstance();
        if (userdata.getUsername() != null && !userdata.getUsername().equals("")) {
            myEmojiUpdater = new EmojidexMyEmojiUpdater(this, userdata.getUsername());
            myEmojiUpdater.startUpdateThread(false);
        }

        initAds();
        setAdsVisibility();

        loginButton = (ImageButton) findViewById(R.id.catalog_login);
        followingButton = (ImageButton) findViewById(R.id.catalog_following);
        followersButton = (ImageButton) findViewById(R.id.catalog_followers);
        setMenuButtonsVisibility();
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
        myEmojiManager = SaveDataManager.getInstance(this, SaveDataManager.Type.MyEmoji);
        myEmojiManager.load();

        // Initialize user data.
        final UserData userdata = UserData.getInstance();
        userdata.init(this);

        currentSortType = getSortType();
        standardOnly = isStandardOnly();
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

        myEmojiButton = (Button)findViewById(R.id.catalog_my_emoji);
    }

    public void onClickCategoryButton(View v)
    {
        changeCategory(v.getContentDescription().toString());
    }

    private void changeCategory(String categoryName)
    {
        if (categoryName == null) return;
        if (currentSortType == null) currentSortType = EmojiComparator.SortType.SCORE;
        if (categoryName.equals(currentCategory) &&
                currentSortType.equals(getSortType()) && standardOnly == isStandardOnly()) return;

        currentCategory = categoryName;
        currentSortType = getSortType();
        standardOnly = isStandardOnly();

        Comparator<Emoji> comparator = null;

        if(categoryName.equals(getString(R.string.ime_category_id_history)))
        {
            List<String> emojiNames = historyManager.getEmojiNames();
            currentCatalog = createEmojiList(emojiNames, false);
        }
        else if(categoryName.equals(getString(R.string.ime_category_id_favorite)))
        {
            final List<String> emojiNames = favoriteManager.getEmojiNames();
            currentCatalog = createEmojiList(emojiNames, false);
        }
        else if(categoryName.equals(getString(R.string.ime_category_id_search)))
        {
            List<String> emojiNames = searchManager.getEmojiNames();
            currentCatalog = createEmojiList(emojiNames, false);
        }
        else if(categoryName.equals(getString(R.string.ime_category_id_index)))
        {
            List<String> emojiNames = indexManager.getEmojiNames();
            currentCatalog = createEmojiList(emojiNames, standardOnly);

            // Sort.
            comparator = new EmojiComparator(currentSortType);
        }
        else if(categoryName.equals(getString(R.string.ime_category_id_all)))
        {
            currentCatalog = setEmojiList(emojidex.getAllEmojiList(), standardOnly);

            // Sort.
            comparator = new EmojiComparator(currentSortType);
        }
        else if (categoryName.equals(getString(R.string.ime_category_id_my_emoji)))
        {
            List<String> emojiNames = myEmojiManager.getEmojiNames();
            currentCatalog = createEmojiList(emojiNames, false);

            // Sort.
            comparator = new EmojiComparator(currentSortType);
        }
        else
        {
            currentCatalog = setEmojiList(emojidex.getEmojiList(categoryName), standardOnly);

            // Sort.
            comparator = new EmojiComparator(currentSortType);
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

    /**
     * Click filter button.
     * @param v button
     */
    public void onClickFilterButton(View v)
    {
        final Intent intent = new Intent(CatalogActivity.this, FilterActivity.class);
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

    private ArrayList<Emoji> createEmojiList(List<String> emojiNames, boolean standardOnly)
    {
        emojidex = Emojidex.getInstance();

        ArrayList<Emoji> emojies = new ArrayList<>(emojiNames.size());
        for (String emojiName : emojiNames)
        {
            final Emoji emoji = emojidex.getEmoji(emojiName);
            if(emoji != null)
                if (!standardOnly || emoji.isStandard()) emojies.add(emoji);
        }

        return emojies;
    }

    private List<Emoji> setEmojiList(List<Emoji> emojies, boolean standardOnly)
    {
        if (!standardOnly) return emojies;

        List<Emoji> removeEmojies = new ArrayList<>();
        for (Emoji emoji : emojies) {
            if (!emoji.isStandard()) removeEmojies.add(emoji);
        }
        emojies.removeAll(removeEmojies);

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

        // after filtering.
        changeCategory(currentCategory);

        Intent intent = getIntent();
        String action = intent.getAction();
        Set categories = intent.getCategories();

        if (Intent.ACTION_VIEW.equals(action) && categories.contains(Intent.CATEGORY_BROWSABLE))
        {
            // emojidex login
            if ("login".equals(intent.getStringExtra("action")))
            {
                if (intent.hasExtra("auth_token") && intent.hasExtra("username"))
                {
                    String authToken = intent.getStringExtra("auth_token");
                    String username = intent.getStringExtra("username");
                    intent.removeExtra("auth_token");
                    intent.removeExtra("username");

                    UserData userdata = UserData.getInstance();
                    userdata.setUserData(authToken, username);

                    final HistoryManager hm = HistoryManager.getInstance(this);
                    final FavoriteManager fm = FavoriteManager.getInstance(this);
                    hm.saveBackup();
                    fm.saveBackup();
                    hm.loadFromUser();
                    fm.loadFromUser();

                    Toast.makeText(getApplicationContext(),
                            getString(R.string.menu_login_success) + userdata.getUsername(),
                            Toast.LENGTH_SHORT).show();
                    analytics.logEvent(FirebaseAnalytics.Event.LOGIN, new Bundle());
                }
                else
                {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.menu_login_cancel), Toast.LENGTH_SHORT).show();
                }
            }
        }

        // set visibility.
        setAdsVisibility();
        setMenuButtonsVisibility();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
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

    void invalidate(String... emojiNames)
    {
        final int first = gridView.getFirstVisiblePosition();
        for(int i = 0;  i < gridView.getChildCount();  ++i)
        {
            final ListAdapter adapter = gridView.getAdapter();
            final View view = gridView.getChildAt(i);
            final Emoji emoji = (Emoji)adapter.getItem(first + i);

            for(String emojiName : emojiNames)
            {
                if( emoji.getCode().equals(emojiName) )
                {
                    adapter.getView(first + i, view, gridView);
                    break;
                }
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
        int count = indexManager.getEmojiNames().size() / indexUpdater.getLimit() + indexLoadPageCount;
        indexUpdater.startUpdateThread(count, true);
    }

    private void initAnimation()
    {
        animationDrawables.clear();

        final int count = gridView.getChildCount();
        for(int i = 0;  i < count;  ++i)
        {
            final Drawable drawable = ((ImageView)gridView.getChildAt(i)).getDrawable();
            if(drawable instanceof EmojidexAnimationDrawable)
                animationDrawables.add((EmojidexAnimationDrawable)drawable);
        }

        // If found animation emoji, start animation.
        if(animationDrawables.isEmpty())
            emojidex.removeAnimationUpdater(animationUpdater);
        else
            emojidex.addAnimationUpdater(animationUpdater);
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
        public void onDownloadImages(int handle, EmojiFormat format, String... emojiNames)
        {
            if(CatalogActivity.this.format.equals(format))
                invalidate(emojiNames);
        }
    }

    /**
     * Emojidex login.
     * @param v button
     */
    public void onClickLoginButton(View v)
    {
        Uri uri = Uri.parse(EMOJIDEX_URL + "/mobile_app/login" + EMOJIDEX_QUERY);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    /**
     * Show emojidex following users page
     * @param v button
     */
    public void onClickFollowingButton(View v)
    {
        Uri uri = Uri.parse(EMOJIDEX_URL + "/following");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
        analytics.logEvent("show_following", new Bundle());
    }

    /**
     * Show emojidex follower users page
     * @param v button
     */
    public void onClickFollowersButton(View v)
    {
        Uri uri = Uri.parse(EMOJIDEX_URL + "/followers");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
        analytics.logEvent("show_followers", new Bundle());
    }

    /**
     * Set menu buttons visibility.
     */
    private void setMenuButtonsVisibility()
    {
        UserData userData = UserData.getInstance();
        if (userData.isLogined())
        {
            loginButton.setVisibility(View.GONE);
            myEmojiButton.setVisibility(View.VISIBLE);
            followingButton.setVisibility(View.VISIBLE);
            User user = new User();
            if (user.authorize(userData.getUsername(), userData.getAuthToken()) && (user.getPremium() || user.getPro()))
                followersButton.setVisibility(View.VISIBLE);
            else
                followersButton.setVisibility(View.GONE);
        }
        else
        {
            loginButton.setVisibility(View.VISIBLE);
            myEmojiButton.setVisibility(View.GONE);
            followingButton.setVisibility(View.GONE);
            followersButton.setVisibility(View.GONE);
        }
    }

    /**
     * Custom emojidex animation updater.
     */
    private class CustomAnimationUpdater implements AnimationUpdater
    {
        @Override
        public void update()
        {
            for (Drawable drawable : animationDrawables)
                drawable.invalidateSelf();
        }

        @Override
        public Collection<EmojidexAnimationDrawable> getDrawables()
        {
            return animationDrawables;
        }
    }

    /**
     * Get standard emoji only preference
     * @return true or false
     */
    private boolean isStandardOnly()
    {
        SharedPreferences pref = getSharedPreferences(FilterActivity.PREF_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(getString(R.string.preference_key_standard_only), false);
    }

    /**
     * Get sort type preference
     * @return sort type
     */
    private EmojiComparator.SortType getSortType()
    {
        UserData userdata = UserData.getInstance();
        User user = new User();

        if (userdata.isLogined() &&
                user.authorize(userdata.getUsername(), userdata.getAuthToken()) && (user.getPremium() || user.getPro()))
        {
            SharedPreferences pref = getSharedPreferences(FilterActivity.PREF_NAME, Context.MODE_PRIVATE);
            int sortType = pref.getInt(getString(R.string.preference_key_sort_type), EmojiComparator.SortType.SCORE.getValue());
            return EmojiComparator.SortType.fromInt(sortType);
        }
        else
        {
            return EmojiComparator.SortType.SCORE;
        }
    }
}
