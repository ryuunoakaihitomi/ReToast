package demo.retoast;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.Toast;

import org.joor.Reflect;

public class MainActivity extends Activity {

    @SuppressLint("ReToastInitializerCalling")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // There's no effect on Android 10 and after.
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            new AlertDialog.Builder(this)
                    .setTitle("Unsupported API level " + Build.VERSION.SDK_INT + " !")
                    .setMessage("API level is greater than 28(Android 9.0). \n\n" +
                            "Please read README.md carefully and run this demo on an older version of Android.")
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> finish())
                    .setCancelable(false)
                    .show();
            return;
        }

        setContentView(R.layout.activity_main);

        final Button
                toastBtn = findViewById(R.id.btn_toast),
                crowdedToastsBtn = findViewById(R.id.btn_crowded_toasts),
                stuckToastBtn = findViewById(R.id.btn_stuck_toast),
                bgToastBtn = findViewById(R.id.btn_background_toast),
                cancelToastBtn = findViewById(R.id.btn_cancel_toast),
                disableReToastBrn = findViewById(R.id.btn_disable_rt);

        toastBtn.setOnClickListener(v -> Toast.makeText(MainActivity.this, "Try to disable notification permission and see what happens.", Toast.LENGTH_LONG).show());
        // Test rate limit.
        crowdedToastsBtn.setOnClickListener(v -> {
            for (int i = 0; i < 5; i++) {
                Toast.makeText(getApplication(), "Toast #" + i, Toast.LENGTH_SHORT).show();
            }
        });
        // The way to trigger BadTokenException.
        stuckToastBtn.setOnClickListener(v -> {
            Toast.makeText(getApplication(), "A stuck toast.", Toast.LENGTH_SHORT).show();
            SystemClock.sleep(3_000);
        });
        Toast bgToast = Toast.makeText(getApplication(), "Background Toast", Toast.LENGTH_SHORT);
        bgToastBtn.setOnClickListener(v -> new Thread(() -> {
            SystemClock.sleep(5_000);
            bgToast.show();
        }).start());
        cancelToastBtn.setOnClickListener(v -> {
            Toast toastA = Toast.makeText(this, "A", Toast.LENGTH_SHORT);
            Toast toastB = Toast.makeText(this, "B", Toast.LENGTH_SHORT);
            toastA.show();
            toastA.cancel();
            toastB.show();
        });
        disableReToastBrn.setOnClickListener(v -> {
            Toast.makeText(getBaseContext(), "ReToast has been temporarily disabled.\nForce stop and restart this app, it will take effect again.", Toast.LENGTH_LONG).show();
            // Clear the proxy notification service.
            Reflect.onClass(Toast.class).set("sService", null);
        });
    }
}
