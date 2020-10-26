package github.ryuunoakaihitomi.retoast;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RestrictTo;

/**
 * <b>NOT FOR USE BY OUTSIDE.</b>
 * <p>
 * It must be public in order to initialize {@link ReToast} automatically.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
public final class ReToastInitializer extends ContentProvider {

    private static final String TAG = "ReToastInitializer";

    @Override
    public boolean onCreate() {
        Context context = getContext();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && context.getApplicationInfo().minSdkVersion > Build.VERSION_CODES.P) {
            throw new UnsupportedOperationException("ReToast is no longer useful after API Level 28! Please remove the library.");
        } else {
            Log.i(TAG, "onCreate: ReToast! " + context.getPackageName());
            ReToast.install();
        }
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
