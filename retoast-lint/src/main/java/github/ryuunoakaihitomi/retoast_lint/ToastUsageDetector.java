package github.ryuunoakaihitomi.retoast_lint;

import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.ClassContext;
import com.android.tools.lint.detector.api.ClassScanner;
import com.android.tools.lint.detector.api.Context;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("UnstableApiUsage")
public class ToastUsageDetector extends Detector implements ClassScanner {

    private static final boolean DEBUG = false;

    /**
     * Only for launching this {@link Detector}.
     */
    static final Issue ISSUE = Issue.create(
            "ReToastNoToastUsage", "", "",
            Category.MESSAGES, 1, Severity.INFORMATIONAL,
            new Implementation(ToastUsageDetector.class, Scope.CLASS_FILE_SCOPE))
            .setAndroidSpecific(true);

    private int mCount;

    @Nullable
    @Override
    public List<String> getApplicableCallOwners() {
        return Collections.singletonList("android/widget/Toast");
    }

    /**
     * Toast.show() must be called before ReToast has effect, so we don't care about the situation of "just instantiate".
     *
     * @see com.android.tools.lint.detector.api.ClassScanner#checkCall(ClassContext, ClassNode, MethodNode, MethodInsnNode)
     */
    @Override
    public void checkCall(@NotNull ClassContext context, @NotNull ClassNode classNode, @NotNull MethodNode method, @NotNull MethodInsnNode call) {
        if (DEBUG) {
            CommonUtils.log(String.format(Locale.ENGLISH,
                    "Toast #%d\tclass=%s, method=%s, call=%s",
                    mCount, classNode.name, method.name, call.name));
        }
        mCount++;
    }

    @Override
    public void afterCheckRootProject(@NotNull Context context) {
        if (mCount == 0) {
            context.log(null,
                    "ReToast: This project does not seem to use android.widget.Toast effectively...");
        } else if (DEBUG) {
            CommonUtils.log("This project has " + mCount + " Toast usage.");
        }
    }
}
