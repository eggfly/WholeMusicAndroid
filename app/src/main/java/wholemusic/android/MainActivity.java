package wholemusic.android;

import android.Manifest;
import android.app.DownloadManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
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
import wholemusic.core.model.Music;
import wholemusic.core.model.MusicLink;


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
            final Music music = mMusicListAdapter.getData().get(position);
            MusicApi qq = MusicApiFactory.create(MusicProvider.QQ音乐);
            qq.getMusicLinkByIdAsync(music.getMusicId(), new RequestCallback<MusicLink>() {
                @Override
                public void onFailure(IOException e) {
                }

                @Override
                public void onSuccess(final MusicLink musicLink) {
                    UIDispatcher.post(new Runnable() {
                        @Override
                        public void run() {
                            DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(musicLink.getUrl()));
                            String filename = music.getName() + ".mp3";
                            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
                            request.setTitle(filename);
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE
                                    | DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                            downloadManager.enqueue(request);
                        }
                    });
                }
            });
        }
    };

    private void cancelCurrentAndStartSearchAsync(String query) {
        MusicApi qq = MusicApiFactory.create(MusicProvider.QQ音乐);
        qq.searchMusicAsync(query, 0, new RequestCallback<List<? extends Music>>() {
            @Override
            public void onFailure(IOException e) {
            }

            @Override
            public void onSuccess(final List<? extends Music> result) {
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
