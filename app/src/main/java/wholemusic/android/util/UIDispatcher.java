package wholemusic.android.util;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by haohua on 2018/2/11.
 */

public class UIDispatcher {
    public static void post(Runnable r) {
        new Handler(Looper.getMainLooper()).post(r);
    }
}
