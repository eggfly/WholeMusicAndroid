package wholemusic.android.util;

import java.io.File;

/**
 * Created by haohua on 2018/2/12.
 */

public class FileUtils {
    public static String combine(String... paths) {
        File file = new File(paths[0]);
        for (int i = 1; i < paths.length; i++) {
            file = new File(file, paths[i]);
        }
        return file.getPath();
    }
}
