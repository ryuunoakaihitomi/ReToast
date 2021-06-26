package github.ryuunoakaihitomi.retoast_lint;

import com.android.tools.lint.client.api.UElementHandler;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Location;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.android.tools.lint.detector.api.SourceCodeScanner;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.UCallExpression;
import org.jetbrains.uast.UClass;
import org.jetbrains.uast.UClassLiteralExpression;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UImportStatement;
import org.jetbrains.uast.util.UastExpressionUtils;

import java.util.Arrays;
import java.util.List;

import static github.ryuunoakaihitomi.retoast_lint.CommonUtils.log;
import static github.ryuunoakaihitomi.retoast_lint.CommonUtils.logEmptyLine;

@SuppressWarnings("UnstableApiUsage")
public class InitializerCallingDetector extends Detector implements SourceCodeScanner {

    private static final boolean DEBUG = false;

    @SuppressWarnings("SpellCheckingInspection")
    private static final String LIBRARY_INITIALIZER_CLASS_NAME = "github.ryuunoakaihitomi.retoast._Initializer";
    static final Issue ISSUE = Issue.create(
            "ReToastInitializerCalling",
            "You should not call ReToast initializer.",
            "The initializer class of `ReToast` has been called accidentally. " +
                    "It is a `ContentProvider` just used to automatically initialize.\n" +
                    "**Not for developer!**",
            Category.CORRECTNESS, 6, Severity.WARNING,
            new Implementation(InitializerCallingDetector.class, Scope.JAVA_FILE_SCOPE))
            .addMoreInfo("https://developer.android.com/reference/android/content/ContentProvider?hl=en#onCreate()")
            .setAndroidSpecific(true);

    @Nullable
    @Override
    public List<Class<? extends UElement>> getApplicableUastTypes() {
        if (DEBUG) {
            log("getApplicableUastTypes() called.");
        }
        return Arrays.asList(UImportStatement.class, UCallExpression.class, UClassLiteralExpression.class, UClass.class);
    }

    @Nullable
    @Override
    public UElementHandler createUastHandler(@NotNull JavaContext context) {
        return new UElementHandler() {

            /**
             * "class Klz {
             *      ...
             * } "
             */
            @Override
            public void visitClass(@NotNull UClass node) {
                if (!DEBUG) return;
                log("visitClass() called.");
                String[] split = node.asRenderString().split(System.lineSeparator());
                int len = split.length;
                log(node.getContainingFile().getName() + ", LOC = " + len);
                for (int i = 0; i < len; i++) {
                    log("" + i + "\t| " + split[i]);
                }
                logEmptyLine();
            }

            /**
             * "ForClass.class"
             */
            @Override
            public void visitClassLiteralExpression(@NotNull UClassLiteralExpression node) {
                if (verify(node.getExpression())) {
                    report(node);
                }
            }

            @Override
            public void visitImportStatement(@NotNull UImportStatement node) {
                if (verify(node.getImportReference())) {
                    report(node);
                }
            }

            /**
             * {@link UCallExpression#getClassReference()} for constructors.
             * {@link UCallExpression#getReceiver()} for normal method.call()s.
             *
             * @param node 👆
             */
            @Override
            public void visitCallExpression(@NotNull UCallExpression node) {
                if (DEBUG) {
                    String s = node.asRenderString();
                    boolean isMultiLine = s.split(System.lineSeparator()).length > 1;
                    if (UastExpressionUtils.isMethodCall(node)) {
                        log(s + " is a method call.", isMultiLine);
                    }
                    if (UastExpressionUtils.isConstructorCall(node)) {
                        log(s + " is a constructor call.", isMultiLine);
                    }
                }
                if (verify(node.getClassReference()) || verify(node.getReceiver())) {
                    report(node);
                }
            }

            private boolean verify(UElement e) {
                return e != null && LIBRARY_INITIALIZER_CLASS_NAME.equals(e.asRenderString());
            }

            private void report(UElement scope) {
                Location location = context.getLocation(scope);
                context.report(ISSUE, scope, location, "`ReToast`'s initializer is not for you",
                        fix().name("Remove ReToast calling statement(s)").replace().range(location).build());
            }
        };
    }
}
