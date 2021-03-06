package github.ryuunoakaihitomi.retoast;

import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

/**
 * <b>We should never use it directly!</b> It is not for developers.
 * <p>
 * It must be public in order to initialize {@link ReToast} automatically.
 */
public final class _Initializer extends ContentProvider {

    private static final String TAG = "ReToast_Initializer";

    // @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public static void main(String[] args) {
        ReToast.install();
    }

    @Override
    public boolean onCreate() {
//        Debug.startMethodTracing();
        final Context context = getContext();
        final ApplicationInfo applicationInfo = context.getApplicationInfo();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            final int minSdkVersion = applicationInfo.minSdkVersion;
            if (minSdkVersion > Build.VERSION_CODES.P && minSdkVersion != Build.VERSION_CODES.CUR_DEVELOPMENT) {
                if ((applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) == 0) {
                    Log.w(TAG, "onCreate: Disable on the release version.");
                    context.getPackageManager().setComponentEnabledSetting(new ComponentName(context, getClass()), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                }
                throw new UnsupportedOperationException("ReToast is no longer useful when minSdkVersion > 28! Please remove the library.");
            }
        }
        Log.i(TAG, "onCreate: Re:Toast - " + context.getPackageName());
        ReToast.install();
//        Log.d(TAG, "onCreate: end");
//        Debug.stopMethodTracing();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
