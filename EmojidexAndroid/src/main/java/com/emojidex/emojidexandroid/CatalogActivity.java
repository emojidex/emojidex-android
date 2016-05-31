package com.emojidex.emojidexandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.RadioButton;

import com.emojidex.libemojidex.Emojidex.Service.User;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;

public class CatalogActivity extends Activity
{
    static CatalogActivity currentInstance = null;

    private GridView gridView;

    private Emojidex emojidex;
    private String currentCategory = null;
    private List<Emoji> currentCatalog = null;

    private SaveDataManager historyManager;
    private SaveDataManager searchManager;
    private SaveDataManager indexManager;

    private HorizontalScrollView categoryScrollView;
    private ViewGroup categoriesView;
    private Button categoryAllButton;

    private boolean isPick = false;

    private Handler handler;

    private AdView adView;
    private FirebaseAnalytics analytics;

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

        // Create handler.
        handler = new Handler();

        // Emoji download.
        currentInstance = this;
        if( !new EmojidexUpdater(this).startUpdateThread() )
            new EmojidexIndexUpdater(this).startUpdateThread();

        initAds();
        setAdsVisibility();
    }

    private void initData()
    {
        emojidex = Emojidex.getInstance();
        emojidex.initialize(this);

        historyManager = SaveDataManager.getInstance(this, SaveDataManager.Type.CatalogHistory);
        historyManager.load();
        searchManager = SaveDataManager.getInstance(this, SaveDataManager.Type.CatalogSearch);
        searchManager.load();
        indexManager = SaveDataManager.getInstance(this, SaveDataManager.Type.Index);
        indexManager.load();
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

        if(categoryName.equals(getString(R.string.ime_category_id_history)))
        {
            List<String> emojiNames = historyManager.getEmojiNames();
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
        }
        else if(categoryName.equals(getString(R.string.ime_category_id_all)))
        {
            currentCatalog = emojidex.getAllEmojiList();
        }
        else
        {
            currentCatalog = emojidex.getEmojiList(categoryName);
        }

        if(currentCatalog != null)
            gridView.setAdapter(new CatalogAdapter(this, currentCatalog));
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
        final String emojiName = emoji.getName();

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
            emojies.add(emojidex.getEmoji(emojiName));
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
        changeCategory(category);
    }

    void invalidate()
    {
        handler.post(new Runnable() {
            @Override
            public void run() {
                ((CatalogAdapter)gridView.getAdapter()).notifyDataSetChanged();
            }
        });
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
}
