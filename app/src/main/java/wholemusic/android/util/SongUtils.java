package wholemusic.android.util;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;

import java.util.ArrayList;

import wholemusic.core.model.Album;
import wholemusic.core.model.Artist;
import wholemusic.core.model.MusicLink;
import wholemusic.core.model.Song;

/**
 * Created by haohua on 2018/2/12.
 */

public class SongUtils {
    public static String getArtistsString(Song song) {
        ArrayList<String> names = new ArrayList<>();
        for (Artist artist : song.getArtists()) {
            names.add(artist.getName());
        }
        return TextUtils.join(", ", names);
    }

    private static String generateSongPath(Song song) {
        Album album = song.getAlbum();
        String path = FileUtils.combine(song.getMusicProvider().toString(),
                album.getName(), generateSongFilename(song));
        return path;
    }

    private static String generateSongFilename(Song song) {
        return song.getName() + " - " + getArtistsString(song) + ".mp3";
    }

    public static void downloadSong(Context context, Song song, MusicLink link) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(link.getUrl()));
        String filepath = generateSongPath(song);
        String filename = generateSongFilename(song);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filepath);
        request.setTitle(filename);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE
                | DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        downloadManager.enqueue(request);
    }
}
