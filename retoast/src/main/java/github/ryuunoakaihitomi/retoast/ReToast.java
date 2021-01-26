package github.ryuunoakaihitomi.retoast;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

final class ReToast {

    private static final String TAG = "ReToast";

    private ReToast() {
    }

    @SuppressWarnings("JavaReflectionMemberAccess")
    static void install() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            try {
                @SuppressLint("DiscouragedPrivateApi")
                Method getService = Toast.class.getDeclaredMethod("getService");
                getService.setAccessible(true);
                final Object iNotificationManager = getService.invoke(null);
                @SuppressLint("PrivateApi")
                Object iNotificationManagerProxy = Proxy.newProxyInstance(
                        Thread.currentThread().getContextClassLoader(),
                        new Class[]{Class.forName("android.app.INotificationManager")},
                        new InvocationHandler() {
                            @Override
                            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                                if ("enqueueToast".equals(method.getName())) {
                                    // duration: LENGTH_SHORT = 0; LENGTH_LONG = 1;
                                    int duration = (int) args[2];
                                    Log.d(TAG, "pkg = " + args[0] + ", duration = " + (duration == 0 ? "short" : "long"));
                                    args[0] = "android";
                                    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
                                        final Object tn = args[1];
                                        Looper mainLooper = Looper.getMainLooper();
                                        if (!mainLooper.isCurrentThread()) {
                                            Log.i(TAG, "Toast.show() is not in main thread.");
                                            new Handler(mainLooper).post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    hookTn(tn);
                                                }
                                            });
                                        } else {
                                            hookTn(tn);
                                        }
                                    }
                                }
                                return method.invoke(iNotificationManager, args);
                            }
                        });
                Field sService = Toast.class.getDeclaredField("sService");
                sService.setAccessible(true);
                sService.set(null, iNotificationManagerProxy);
            } catch (Throwable e) {
                Log.e(TAG, "install", e);
            }
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
            Log.d(TAG, "handleMessage: action = " + getActionString(msg.what));
            logUnsafeContext();
            try {
                mHandler.handleMessage(msg);
            } catch (WindowManager.BadTokenException e) {
                Log.i(TAG, "handleMessage: BadTokenException has been caught!", e);
            }
        }
    }
}
