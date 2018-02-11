package wholemusic.android;

import android.Manifest;
import android.app.DownloadManager;
import android.net.Uri;
import android.os.AsyncTask;
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
import java.io.InterruptedIOException;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;
import wholemusic.core.api.framework.MusicApi;
import wholemusic.core.api.framework.model.Music;
import wholemusic.core.api.impl.qq.QQMusicApi;


@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();
    private TextView mTextMessage;
    private RecyclerView mMusicListRecyclerView;
    private MusicListAdapter mMusicListAdapter;
    private EditText mSearchEditText;
    private SearchMusicTask mCurrentSearchTask;

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
            QQMusicApi qq = new QQMusicApi();
            GetMusicLinkTask task = new GetMusicLinkTask(qq, music.getMusicId(), new Function<String, Void>() {
                @Override
                public Void apply(String url) {
                    DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                    String filename = music.getName() + ".mp3";
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
                    request.setTitle(filename);
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE
                            | DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    // TODO: request external storage permission
                    downloadManager.enqueue(request);
                    return null;
                }
            });
            task.execute();
        }
    };

    private void cancelCurrentAndStartSearchAsync(String query) {
        if (mCurrentSearchTask != null && mCurrentSearchTask.getStatus() == AsyncTask.Status.RUNNING) {
            mCurrentSearchTask.cancel(true);
        }
        mCurrentSearchTask = new SearchMusicTask(query);
        mCurrentSearchTask.execute();
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

    private class SearchMusicTask extends AsyncTask<Void, Void, List<? extends Music>> {
        private final String mQuery;

        public SearchMusicTask(String query) {
            mQuery = query;
        }

        @Override
        protected List<? extends Music> doInBackground(Void... voids) {
            QQMusicApi qq = new QQMusicApi();
            try {
                List<? extends Music> result = qq.searchMusic(mQuery);
                System.out.println(result);
                return result;
            } catch (InterruptedIOException e) {
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<? extends Music> result) {
            if (!isCancelled()) {
                if (result != null) {
                    mMusicListAdapter.setData(result);
                    mMusicListAdapter.notifyDataSetChanged();
                }
            }
        }
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

    private class GetMusicLinkTask extends AsyncTask<Void, Void, String> {
        private final MusicApi mMusicApi;
        private final String mMusicId;
        private final Function<String, Void> mCallback;

        public GetMusicLinkTask(MusicApi api, String musicId, Function<String, Void> callback) {
            mMusicApi = api;
            mMusicId = musicId;
            mCallback = callback;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                return mMusicApi.getMusicLinkById(mMusicId).getUrl();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String url) {
            mCallback.apply(url);
        }
    }
}
