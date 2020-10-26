# ReToast

This Android library is used to fix two serious bugs of `android.widget.Toast` with the easiest way.

## Introduction

**What bugs does it fix?**

* before 10 (29)

[Toasts are disabled if notifications are disabled for an app.](https://issuetracker.google.com/issues/36951147)

* 7.1 (25)

[Toast with UI stress causes crash.](https://android.googlesource.com/platform/frameworks/base/+/0df3702f533667a3825ecbce67db0853385a99ab)

**Why not use other library?**

Most library that fix these bugs are invasive, so we have to replace all existing Toasts, it can be pretty messy in some large project.

Unlike them, this library is globally effective to make less invasine. We can do all our work just like before.

## Usage

[ ![Download](https://api.bintray.com/packages/ryuunoakaihitomi/maven/retoast/images/download.svg) ](https://bintray.com/ryuunoakaihitomi/maven/retoast/_latestVersion)

module-level build.gradle:

```groovy
dependencies {
    implementation 'github.ryuunoakaihitomi.retoast:retoast:latest.release'
    ...
```