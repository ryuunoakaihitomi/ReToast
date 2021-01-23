package demo.retoast;

import android.app.Activity;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.Toast;

import github.ryuunoakaihitomi.retoast.ReToastInitializer;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button
                toastBtn = findViewById(R.id.btn_toast),
                crowdedToastsBtn = findViewById(R.id.btn_crowded_toasts),
                stuckToastBtn = findViewById(R.id.btn_stuck_toast),
                bgToastBtn = findViewById(R.id.btn_background_toast),
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
        disableReToastBrn.setOnClickListener(v -> {
            // The way to DISABLE ReToast without removing the library. WE SHOULD NOT DO THIS!
            getPackageManager().setComponentEnabledSetting(
                    new ComponentName(getApplication(), ReToastInitializer.class),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        });
    }
}
