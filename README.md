Run As Root
===============
[![Build Status](https://travis-ci.org/dyorgio/run-as-root.svg?branch=master)](https://travis-ci.org/dyorgio/run-as-root) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.dyorgio.runtime/run-as-root/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.dyorgio.runtime/run-as-root)

A java library to run pieces of code as root using native root password input dialog.

A new JVM is created in every execution and destroyed after completion.

This library uses [Out Process](https://github.com/dyorgio/out-process) to create new JVMs.

Why use it?
-----
* Ask user for credentials in Windows, Linux and macOS.
* Do privileged actions.
* Limit scope of root code execution.
* Upgrade permissions during runtime temporarily.

Usage
-----
```java
// Specify JVM options (optional)
RootExecutor rootExecutor = new RootExecutor("-Xmx64m");

// Execute privileged action without return
rootExecutor.run(() -> // Call your admin code here.));

// Execute privileged action with return
Object value = rootExecutor.call(() -> // Call your admin code with return here.));
System.out.println(value);
```

Tips
-----
* Init simple, after include this lib in your project just try to write a file in a protected folder to validate if it is working.
* Avoid big classpaths, it can be a problem in Windows. [#8](https://github.com/dyorgio/run-as-root/issues/8)

Maven
-----
```xml
<dependency>
    <groupId>com.github.dyorgio.runtime</groupId>
    <artifactId>run-as-root</artifactId>
    <version>1.2.3</version>
</dependency>
```
