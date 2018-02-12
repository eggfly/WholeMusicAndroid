package wholemusic.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import wholemusic.android.util.PageUtils;
import wholemusic.android.util.SongUtils;
import wholemusic.android.util.UIDispatcher;
import wholemusic.core.api.MusicApi;
import wholemusic.core.api.MusicApiFactory;
import wholemusic.core.api.RequestCallback;
import wholemusic.core.api.SimpleRequestCallback;
import wholemusic.core.model.Album;
import wholemusic.core.model.MusicLink;
import wholemusic.core.model.Song;

/**
 * Created by haohua on 2018/2/12.
 */

public class SongInfoActivity extends AppCompatActivity {
    public static final String EXTRA_SONG = "song";
    private Song mSong;

    private RecyclerView mMusicMenuRecyclerView;
    private Button mDownloadButton;
    private Button mViewAlbumButton;

    private Button mDownloadAlbumButton;

    private TextView mSongNameTextView;
    private TextView mArtistNameTextView;
    private TextView mAlbumNameTextView;

    private View.OnClickListener mDownloadButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MusicApi api = MusicApiFactory.create(mSong.getMusicProvider());
            api.getMusicLinkByIdAsync(mSong.getSongId(), new RequestCallback<MusicLink>() {
                @Override
                public void onFailure(IOException e) {
                }

                @Override
                public void onSuccess(final MusicLink musicLink) {
                    UIDispatcher.post(new Runnable() {
                        @Override
                        public void run() {
                            SongUtils.downloadSong(SongInfoActivity.this, mSong, musicLink);
                        }
                    });
                }
            });
        }
    };
    private View.OnClickListener mViewAlbumButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(SongInfoActivity.this, R.string.view_album, Toast.LENGTH_SHORT).show();
        }
    };

    private View.OnClickListener mDownloadAlbumButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String albumId = mSong.getAlbum().getAlbumId();
            MusicApiFactory.create(mSong.getMusicProvider()).getAlbumInfoById(new SimpleRequestCallback<Album>() {
                @Override
                public void onSuccess(Album album) {
                    final List<? extends Song> albumSongs = album.getSongs();
                    ArrayList<String> songIds = new ArrayList<>();
                    for (Song song : albumSongs) {
                        songIds.add(song.getSongId());
                    }
                    MusicApiFactory.create(mSong.getMusicProvider()).getMusicLinkByIdsAsync(new SimpleRequestCallback<List<? extends MusicLink>>() {
                        @Override
                        public void onSuccess(final List<? extends MusicLink> musicLinks) {
                            for (MusicLink link : musicLinks) {
                                for (Song albumSong : albumSongs) {
                                    if (TextUtils.equals(albumSong.getSongId(), link.getSongId())) {
                                        SongUtils.downloadSong(SongInfoActivity.this, albumSong, link);
                                        break;
                                    }
                                }
                            }
                            UIDispatcher.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(SongInfoActivity.this, R.string.task_added, Toast.LENGTH_SHORT).show();
                                    PageUtils.startDownloadManager(SongInfoActivity.this);
                                }
                            });
                        }
                    }, songIds.toArray(new String[]{}));
                }
            }, albumId);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 不要title bar
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_song_info);

        mSongNameTextView = (TextView) findViewById(R.id.song_name_tv);
        mArtistNameTextView = (TextView) findViewById(R.id.artist_name_tv);
        mAlbumNameTextView = (TextView) findViewById(R.id.album_name_tv);
        mMusicMenuRecyclerView = (RecyclerView) findViewById(R.id.music_menu_recycler_view);
        mDownloadButton = (Button) findViewById(R.id.download_btn);
        mDownloadButton.setOnClickListener(mDownloadButtonOnClickListener);
        mViewAlbumButton = (Button) findViewById(R.id.view_album_btn);
        mViewAlbumButton.setOnClickListener(mViewAlbumButtonOnClickListener);
        mDownloadAlbumButton = (Button) findViewById(R.id.download_album_btn);
        mDownloadAlbumButton.setOnClickListener(mDownloadAlbumButtonOnClickListener);

        Intent intent = getIntent();
        mSong = (Song) intent.getSerializableExtra(EXTRA_SONG);

        mSongNameTextView.setText(mSong.getName());
        mArtistNameTextView.setText(SongUtils.getArtistsString(mSong));
        mAlbumNameTextView.setText(mSong.getAlbum().getName());
    }
}
