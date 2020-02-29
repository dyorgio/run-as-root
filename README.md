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
rootExecutor.run(() -> System.out.println("Call your admin code here."));

// Execute privileged action with return
String value = rootExecutor.call(() -> System.getProperty("Call your admin code with return here."));
System.out.println(value);
```

Maven
-----
```xml
<dependency>
    <groupId>com.github.dyorgio.runtime</groupId>
    <artifactId>run-as-root</artifactId>
    <version>1.2.2</version>
</dependency>
```
