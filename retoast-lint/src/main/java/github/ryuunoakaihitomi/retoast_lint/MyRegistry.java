package github.ryuunoakaihitomi.retoast_lint;

import com.android.tools.lint.client.api.IssueRegistry;
import com.android.tools.lint.client.api.Vendor;
import com.android.tools.lint.detector.api.ApiKt;
import com.android.tools.lint.detector.api.Issue;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class MyRegistry extends IssueRegistry {

    @Nullable
    @Override
    public Vendor getVendor() {
        return new Vendor("ZQY", null, "https://github.com/ryuunoakaihitomi/ReToast/issues");
    }

    @Override
    public int getApi() {
        return ApiKt.CURRENT_API;
    }

    @Override
    public int getMinApi() {
        return 8;   // works with Studio 4.1 or later;
    }

    @NotNull
    @Override
    public List<Issue> getIssues() {
        return Arrays.asList(
                LibraryImportDetector.ISSUE_WAY,
                LibraryImportDetector.ISSUE_ENV,
                InitializerCallingDetector.ISSUE,
                ToastUsageDetector.ISSUE);
    }
}
