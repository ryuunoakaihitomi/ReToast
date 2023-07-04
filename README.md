# ReToast

[![Build CI](https://github.com/ryuunoakaihitomi/ReToast/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/ryuunoakaihitomi/ReToast/actions/workflows/build.yml)
[![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2Fryuunoakaihitomi%2Fmaven-repository%2Fmaster%2Fgithub%2Fryuunoakaihitomi%2Fretoast%2Fretoast%2Fmaven-metadata.xml)](https://github.com/ryuunoakaihitomi/maven-repository)

## Introduction

This Android library is used to fix some serious bugs of `android.widget.Toast` with the easiest way.

## Details

**What bugs does it fix?**

* before 10 (<29): Notification Permission

[Always allow toasts from foreground apps (The commit to fix).](https://android.googlesource.com/platform/frameworks/base/+/58b2453ed69197d765c7254241d9966ee49a3efb)

* from 8.1 to 9.0 (27-28): Rate Limit

[Each package can enqueue one toast at a time.](https://android.googlesource.com/platform/frameworks/base/+/4ee785b698211b5ccce104e226b073ffbb12df55)

[Allow apps to queue multiple toast messages.](https://android.googlesource.com/platform/frameworks/base/+/a7ed0abe18556847e3cd6e1e4c03a29a0c96fb50)

* 7.1 (25): BadTokenException

[Toast with UI stress causes crash.](https://android.googlesource.com/platform/frameworks/base/+/0df3702f533667a3825ecbce67db0853385a99ab)

**Why not use other libraries?**

Most library that fix these bugs are invasive, so we have to replace all existing Toasts, it can be pretty messy in some large projects.

Unlike them, this library is globally effective and uses [`ContentProvider.onCreate()`](https://developer.android.com/reference/android/content/ContentProvider#onCreate()) to automatically initialize in order to make less invasive (Initializing is almost instant).
We can continue doing all our other work just like before.

## Usage

Import the library in app(module-level) build.gradle:

```groovy
dependencies {
    repositories {
        maven {
            url 'https://raw.githubusercontent.com/ryuunoakaihitomi/maven-repository/master' // 👈
        }
    }
    runtimeOnly 'github.ryuunoakaihitomi.retoast:retoast:2.0.0' // 👈
    // ...
}
```

### For Android studio Arctic Fox 2020.3.1+

If you encountered this error,
> Build was configured to prefer settings repositories over project repositories but repository 'maven' was added by build file 'app\build.gradle'

you have to perform it in two steps...

① Import the maven repository in settings.gradle:

```groovy
dependencyResolutionManagement {
    repositories {
        // ...
        // 👇
        maven {
            url 'https://raw.githubusercontent.com/ryuunoakaihitomi/maven-repository/master'
        }
    }
}
```

② Import the library in app(module-level) build.gradle

```groovy
dependencies {
    // ...
    runtimeOnly 'github.ryuunoakaihitomi.retoast:retoast:2.0.0'
}
```

**THAT'S ALL!** We can use [`Toast`](https://developer.android.com/reference/android/widget/Toast)
or other fancy Toast-based UI libraries as before, but there's no need to worry about the bugs
anymore.

## Compatibility

If the app's minSdkVersion >= API Level 29, it's almost impossible to encounter these bugs, which
have been fixed on platforms where the app is compatible (Tested on Android 10 and 11 release). We'd
better remove ReToast from the app. In this case, the library will throw
a `UnsupportedOperationException` in order to remind us to remove it.

Since Android 10, the vulnerability used by this library does have also
been [fixed](https://cs.android.com/android/_/android/platform/frameworks/base/+/58b2453ed69197d765c7254241d9966ee49a3efb)
, so even if these ~~features~~<sup id="further_compatibility">[1](#rate-limit)</sup> bugs reappear, we just cannot let it work on the newer platforms any
more. 😟

ReToast also ships with some embedded lint rules to guide us use it in the right way as much as possible.
However, there is no guarantee that these rules will function properly all the time.
Documentations are always the most reliable.

## Acknowledgments

### References

Inspired by these two articles (are all in Chinese), this library is made.
These were supposed to be unrelated stuff, but I suddenly found the connection between them.

[解决通知关闭Toast失效问题](https://blog.csdn.net/qq331710168/article/details/85320098)

[Android 7.X Toast Bug](https://www.jianshu.com/p/c8e00943afc9)

### Assistance

[DeweyReed](https://github.com/DeweyReed) helped find a [bug](https://github.com/DeweyReed/ClipboardCleaner/pull/16#issuecomment-788558879).

---

<!-- https://stackoverflow.com/questions/25579868/how-to-add-footnotes-to-github-flavoured-markdown -->

<b id="rate-limit"><sup>[1](#further_compatibility)</sup> </b><sub>Android 12 did bring back rate limit for all apps, the compatibility change is [RATE_LIMIT_TOASTS](https://developer.android.com/about/versions/12/reference/compat-framework-changes#rate_limit_toasts). </sub>
