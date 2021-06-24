package github.ryuunoakaihitomi.retoast_lint;

public class CommonUtils {

    private CommonUtils() {
    }

    static void log(String msg) {
        System.out.println(msg);
    }

    static void logEmptyLine() {
        log("");
    }

    static void log(String msg, boolean shouldSeparate) {
        if (shouldSeparate) {
            logEmptyLine();
            log(msg);
            logEmptyLine();
        } else {
            log(msg);
        }
    }
}
