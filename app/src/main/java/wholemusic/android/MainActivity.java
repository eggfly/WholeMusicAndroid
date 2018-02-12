package wholemusic.android;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;
import wholemusic.core.api.MusicApi;
import wholemusic.core.api.MusicApiFactory;
import wholemusic.core.api.MusicProvider;
import wholemusic.core.api.RequestCallback;
import wholemusic.core.model.Song;


@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();
    private TextView mTextMessage;
    private RecyclerView mMusicListRecyclerView;
    private MusicListAdapter mMusicListAdapter;
    private EditText mSearchEditText;

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            final String query = s.toString();
            cancelCurrentAndStartSearchAsync(query);
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    private MusicListAdapter.OnItemClickListener mOnMusicItemClickListener = new MusicListAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            final Song music = mMusicListAdapter.getData().get(position);
            Intent intent = new Intent(MainActivity.this, SongInfoActivity.class);
            intent.putExtra(SongInfoActivity.EXTRA_SONG, music);
            startActivity(intent);
        }
    };

    private void cancelCurrentAndStartSearchAsync(String query) {
        if (!TextUtils.isEmpty(query)) {
            MusicApi api = MusicApiFactory.create(MusicProvider.网易云音乐);
            api.searchMusicAsync(query, 0, new RequestCallback<List<? extends Song>>() {
                @Override
                public void onFailure(IOException e) {
                }

                @Override
                public void onSuccess(final List<? extends Song> result) {
                    if (result != null) {
                        mMusicListRecyclerView.post(new Runnable() {
                            @Override
                            public void run() {
                                mMusicListAdapter.setData(result);
                                mMusicListAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }
            });
        } else {
            mMusicListAdapter.setData(new ArrayList<Song>());
            mMusicListAdapter.notifyDataSetChanged();
        }
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    protected void grantPermission() {
        Log.d(TAG, "onPermissionGranted");
    }

    @OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    protected void showRationale(PermissionRequest request) {
        Log.d(TAG, "showRationale");
    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    protected void onPermissionDenied() {
        Log.d(TAG, "onPermissionDenied");
    }

    @OnNeverAskAgain(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    protected void onNeverAskAgain() {
        Log.d(TAG, "onNeverAskAgain");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextMessage = (TextView) findViewById(R.id.message);
        mSearchEditText = (EditText) findViewById(R.id.editText);
        mSearchEditText.removeTextChangedListener(mTextWatcher);
        mSearchEditText.addTextChangedListener(mTextWatcher);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        mMusicListRecyclerView = (RecyclerView) findViewById(R.id.music_list_recycler_view);
        mMusicListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mMusicListAdapter = new MusicListAdapter(this);
        mMusicListAdapter.setOnItemClickListener(mOnMusicItemClickListener);
        mMusicListRecyclerView.setAdapter(mMusicListAdapter);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Android 6.0以上的动态权限获取
        MainActivityPermissionsDispatcher.grantPermissionWithPermissionCheck(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };
}
