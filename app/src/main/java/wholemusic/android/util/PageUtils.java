package wholemusic.android.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

/**
 * Created by haohua on 2018/2/12.
 */

public class PageUtils {
    public static void startDownloadManager(Context context) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage("com.android.providers.downloads.ui");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }
}
