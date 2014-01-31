package org.genshin.emojidexandroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Created by nazuki on 2014/01/31.
 */
public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    public void setKeyboard(View v)
    {

//        // TODO test
//        Log.e("test", "" + Settings.Secure.getString(this.getContentResolver(),
//                Settings.Secure.DEFAULT_INPUT_METHOD));
//        InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
//        List<InputMethodInfo> inputMethodInfoList = imm.getEnabledInputMethodList();
//        for (int i = 0; i < inputMethodInfoList.size(); ++i) {
//            InputMethodInfo inputMethodInfo = inputMethodInfoList.get(i);
//            CharSequence label = inputMethodInfo.loadLabel(getPackageManager());
//            Log.v("label", String.valueOf(label));
//            Log.e("test", "id:" + inputMethodInfo.getId());
//        }
//        // switchInputMethod(imeId);

    }

    public void deleteAllFavorites(View v)
    {

    }

    public void deleteAllHistories(View v)
    {

    }

    /**
     * back to the main activity.
     * @param v
     */
    public void backToMainActivity(View v)
    {
        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
        startActivity(intent);
    }
}
