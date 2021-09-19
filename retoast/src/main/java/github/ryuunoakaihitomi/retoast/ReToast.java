package github.ryuunoakaihitomi.retoast;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Objects;

@SuppressLint("PrivateApi")
final class ReToast {

    private static final String TAG = "ReToast";
    private static final boolean DEBUG;

    static {
        boolean debug;
        try {
            Application app = (Application) Class.forName("android.app.ActivityThread").getMethod("currentApplication").invoke(null);
            Objects.requireNonNull(app);
            debug = (app.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) > 0;
        } catch (Exception e) {
            debug = Debug.isDebuggerConnected();
            Log.e(TAG, "static initializer", e);
        }
        DEBUG = debug;
    }

    private ReToast() {
    }

    static void install() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (Runtime.getRuntime().availableProcessors() > 1) {
                new Thread(() -> {
                    installSync();
                    if (DEBUG) Log.i(TAG, "run: Done.");
                }).start();
            } else {
                installSync();
                if (DEBUG)
                    Log.w(TAG, "install: Done. This operation could be a little slow without multi-core support.");
            }
        }
    }

    /**
     * This method is very expensive as it relies heavily on reflection.
     */
    @SuppressWarnings({"JavaReflectionMemberAccess", "SoonBlockedPrivateApi"})
    private static void installSync() {
        try {
            @SuppressLint("DiscouragedPrivateApi")
            Method getService = Toast.class.getDeclaredMethod("getService");
            getService.setAccessible(true);
            final Object iNotificationManager = getService.invoke(null);
            Object iNotificationManagerProxy = Proxy.newProxyInstance(
                    Thread.currentThread().getContextClassLoader(),
                    new Class[]{Class.forName("android.app.INotificationManager")},
                    (proxy, method, args) -> {
                        final String methodName = method.getName(), /* PackageManagerService. */ PLATFORM_PACKAGE_NAME = "android";
                        switch (methodName) {
                            case "cancelToast":
                                if (DEBUG) Log.d(TAG, "cancel, pkg = " + args[0]);
                                args[0] = PLATFORM_PACKAGE_NAME;
                                break;
                            case "enqueueToast":
                            case "enqueueToastEx":  // For some Huawei devices. (Tested on HUAWEI/PAR-AL00/HWPAR:8.1.0/HUAWEIPAR-AL00/113(C00):user/release-keys, EMUI 8.2.0)
                                if (DEBUG) {
                                    Log.d(TAG, "enqueue, pkg = " + args[0] + ", duration = " + ((int) args[2] == Toast.LENGTH_SHORT ? "short" : "long"));
//                                    Log.d(TAG, "enqueue -> methodName = " + methodName + ", FP: " + Build.FINGERPRINT);
                                }
                                args[0] = PLATFORM_PACKAGE_NAME;
                                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
                                    final Object tn = args[1];
                                    Looper mainLooper = Looper.getMainLooper();
                                    if (!mainLooper.isCurrentThread()) {
                                        if (DEBUG)
                                            Log.i(TAG, "Toast.show() is not in main thread.");
                                        new Handler(mainLooper).post(() -> hookTn(tn));
                                    } else {
                                        hookTn(tn);
                                    }
                                }
                                break;
                            default:
                                if (DEBUG) {
                                    Log.d(TAG, "proxy: methodName = " + methodName +
                                            ", args = " + Arrays.toString(args));
                                }
                        }
                        return method.invoke(iNotificationManager, args);
                    });
            Field sService = Toast.class.getDeclaredField("sService");
            sService.setAccessible(true);
            sService.set(null, iNotificationManagerProxy);
        } catch (Throwable e) {
            Log.e(TAG, "installSync", e);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static void hookTn(Object tn) {
        try {
            final Field mHandler = tn.getClass().getDeclaredField("mHandler");
            mHandler.setAccessible(true);
            mHandler.set(tn, new SafeHandlerProxy(tn, (Handler) mHandler.get(tn)));
        } catch (IllegalAccessException | NoSuchFieldException e) {
            Log.e(TAG, "hookTn", e);
        }
    }

    private static final class SafeHandlerProxy extends Handler {
        private final Object mAssociatedTn;
        private final Handler mHandler;

        @SuppressWarnings("deprecation") // since 30
        public SafeHandlerProxy(Object tn, Handler handler) {
            mAssociatedTn = tn;
            mHandler = handler;
        }

        private static String getActionString(int action) {
            // SHOW = 0; HIDE = 1; CANCEL = 2;
            switch (action) {
                case 0:
                    return "show";
                case 1:
                    return "hide";
                case 2:
                    return "cancel";
            }
            return Integer.toString(action);
        }

        private void logUnsafeContext() {
            try {
                final Field mNextView = mAssociatedTn.getClass().getDeclaredField("mNextView");
                mNextView.setAccessible(true);
                final View nextView = (View) mNextView.get(mAssociatedTn);
                if (nextView != null && nextView.getContext() instanceof Activity) {
                    Log.w(TAG, "Consider using application instead of activity(" +
                            ((Activity) nextView.getContext()).getLocalClassName() +
                            ") as context to prevent memory leak.");
                }
            } catch (Exception e) {
                Log.v(TAG, "logUnsafeContext", e);
            }
        }

        @Override
        public void handleMessage(Message msg) {
            if (DEBUG) {
                Log.d(TAG, "handleMessage: action = " + getActionString(msg.what));
                logUnsafeContext();
            }
            try {
                mHandler.handleMessage(msg);
            } catch (WindowManager.BadTokenException e) {
                if (DEBUG) Log.i(TAG, "handleMessage: BadTokenException has been caught!", e);
            }
        }
    }
}
