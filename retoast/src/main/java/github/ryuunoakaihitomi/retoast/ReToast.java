package github.ryuunoakaihitomi.retoast;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
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

    static void install() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            try {
                @SuppressWarnings("JavaReflectionMemberAccess")
                @SuppressLint("DiscouragedPrivateApi")
                Method getService = Toast.class.getDeclaredMethod("getService");
                getService.setAccessible(true);
                // Do not be inline to avoid endless loop.
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
                                        Object tn = args[1];
                                        Field mHandler = tn.getClass().getDeclaredField("mHandler");
                                        mHandler.setAccessible(true);
                                        mHandler.set(tn, new HandlerProxy((Handler) mHandler.get(tn)));
                                    }
                                }
                                return method.invoke(iNotificationManager, args);
                            }
                        });
                @SuppressWarnings("JavaReflectionMemberAccess")
                Field sService = Toast.class.getDeclaredField("sService");
                sService.setAccessible(true);
                sService.set(null, iNotificationManagerProxy);
            } catch (Throwable e) {
                Log.e(TAG, null, e);
            }
        }
    }

    private static final class HandlerProxy extends Handler {
        private final Handler mHandler;

        @SuppressWarnings("deprecation") // since 30
        public HandlerProxy(Handler handler) {
            mHandler = handler;
        }

        private static String getToastActionString(int action) {
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

        @Override
        public void handleMessage(Message msg) {
            // SHOW = 0; HIDE = 1; CANCEL = 2;
            Log.d(TAG, "handleMessage: action = " + getToastActionString(msg.what));
            try {
                mHandler.handleMessage(msg);
            } catch (WindowManager.BadTokenException e) {
                Log.i(TAG, "handleMessage: BadTokenException has been caught!", e);
            }
        }
    }
}
