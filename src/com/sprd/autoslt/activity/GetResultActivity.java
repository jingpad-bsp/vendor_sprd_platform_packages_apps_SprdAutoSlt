
package com.sprd.autoslt.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.view.KeyEvent;

import com.sprd.autoslt.R;
import com.sprd.autoslt.R.id;
import com.sprd.autoslt.R.layout;
import com.sprd.autoslt.sqlite.EngSqlite;
import com.sprd.autoslt.util.ListAdapter;
import com.sprd.autoslt.util.NavigationUtil;

public class GetResultActivity extends Activity {
    private static final String TAG = "GetResultActivity";
    private Context mContext;
    public TextView mTestResultView;
    private EngSqlite mEngSqlite;
    private String mBit;
    private static final int READ_BIT = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_result);
        mContext = this;
        mEngSqlite = EngSqlite.getInstance(mContext);
        ListView listView = (ListView) findViewById(R.id.listview_layout);
        ListAdapter listAdapter = new ListAdapter(mContext, mEngSqlite.queryData());
        listView.setAdapter(listAdapter);
        mTestResultView = (TextView) findViewById(R.id.test_result_text);
        Log.d(TAG, "onCreate end!");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (NavigationUtil.getNavigationBarShowState(mContext)) {
            Log.d(TAG, "GetResultActivity, hideNavigationBar...");
            NavigationUtil.hideNavigationBar(this);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "GetResultActivity, onKeyDown, keyCode=:" + keyCode);
        if (KeyEvent.KEYCODE_VOLUME_UP == keyCode) {
            finish();
        }
        return true;
    }
}
