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

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class InitializerCallingDetector extends Detector implements SourceCodeScanner {

    static final Issue ISSUE = Issue.create(
            "ReToastInitializerCalling",
            "The initializer class of ReToast has been called accidentally.",
            "It is a `ContentProvider` just used to automatically initialize.\n" +
                    "**Not for developer!**",
            Category.CORRECTNESS, 6, Severity.WARNING,
            new Implementation(InitializerCallingDetector.class, Scope.JAVA_FILE_SCOPE))
            .addMoreInfo("https://developer.android.com/reference/android/content/ContentProvider?hl=en#onCreate()")
            .setAndroidSpecific(true);

    @SuppressWarnings("SpellCheckingInspection")
    private static final String LIBRARY_INITIALIZER_CLASS_NAME = "github.ryuunoakaihitomi.retoast._Initializer";

    private static final boolean DEBUG = false;

    @Nullable
    @Override
    public List<Class<? extends UElement>> getApplicableUastTypes() {
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
                if (DEBUG) {
                    PrintStream p = System.out;
                    p.println("DEBUG: visitClass() node output...");
                    String[] split = node.asRenderString().split(System.lineSeparator());
                    int len = split.length;
                    for (int i = 0; i < len; i++) {
                        p.println("" + i + "\t|" + split[i]);
                    }
                    p.println();
                }
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
                if (verify(node.getClassReference()) || verify(node.getReceiver())) {
                    report(node);
                }
            }

            private boolean verify(UElement e) {
                return e != null && LIBRARY_INITIALIZER_CLASS_NAME.equals(e.asRenderString());
            }

            private void report(UElement scope) {
                Location location = context.getLocation(scope);
                context.report(ISSUE, scope, location, "ReToast's initializer is not for you.",
                        fix().name("Remove ReToast calling statement(s)").replace().range(location).build());
            }
        };
    }
}
