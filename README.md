# ReToast

[ ![Download](https://api.bintray.com/packages/ryuunoakaihitomi/maven/retoast/images/download.svg) ](https://bintray.com/ryuunoakaihitomi/maven/retoast/_latestVersion)

This Android library is used to fix two serious bugs of `android.widget.Toast` with the easiest way.

## Introduction

**What bugs does it fix?**

* before 10 (29): Notification Permission

[Toasts are disabled if notifications are disabled for an app.](https://issuetracker.google.com/issues/36951147)

[Always allow toasts from foreground apps (The commit to fix).](https://android.googlesource.com/platform/frameworks/base/+/58b2453ed69197d765c7254241d9966ee49a3efb)

* from 8.1(27) to 9.0(28): Rate Limit

[Each package can enqueue one toast at a time.](https://android.googlesource.com/platform/frameworks/base/+/4ee785b698211b5ccce104e226b073ffbb12df55)

[Allow apps to queue multiple toast messages.](https://android.googlesource.com/platform/frameworks/base/+/a7ed0abe18556847e3cd6e1e4c03a29a0c96fb50)

* 7.1 (25): BadTokenException

[Toast with UI stress causes crash.](https://android.googlesource.com/platform/frameworks/base/+/0df3702f533667a3825ecbce67db0853385a99ab)

**Why not use other libraries?**

Most library that fix these bugs are invasive, so we have to replace all existing Toasts, it can be pretty messy in some large projects.

Unlike them, this library is globally effective and uses [`ContentProvider.onCreate()`](https://developer.android.com/reference/android/content/ContentProvider#onCreate()) to automatically initialize in order to make less invasive.
We can continue doing all our work just like before.

## Usage

Import the library in app(module-level) build.gradle:

```groovy
dependencies {
    implementation 'github.ryuunoakaihitomi.retoast:retoast:latest.release'
    ...
```

**THAT'S ALL!** We can use [`Toast`](https://developer.android.com/reference/android/widget/Toast) or other fancy Toast-based UI libraries as before, but there's no need to worry about the bugs anymore.

## Compatibility

If the app's minSdkVersion >= API Level 29, it's almost impossible to encounter these bugs(fixed). We'd better remove ReToast from the app.