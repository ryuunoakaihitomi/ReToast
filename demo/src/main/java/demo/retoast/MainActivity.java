package demo.retoast;

import android.app.Activity;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Build;
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
                stickyToastBtn = findViewById(R.id.btn_sticky_toast),
                disableReToastBrn = findViewById(R.id.btn_disable_rt);

        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.N_MR1) {
            stickyToastBtn.setEnabled(false);
        }

        toastBtn.setOnClickListener(v -> Toast.makeText(MainActivity.this, "Try to disable notification permission and see what happens.", Toast.LENGTH_LONG).show());
        stickyToastBtn.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "An invisible toast.", Toast.LENGTH_SHORT).show();
            SystemClock.sleep(3_000);
        });
        disableReToastBrn.setOnClickListener(v -> {
            // The way to DISABLE ReToast without removing the library. WE SHOULD NOT DO THIS!
            getPackageManager().setComponentEnabledSetting(
                    new ComponentName(getApplication(), ReToastInitializer.class),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        });
    }
}
