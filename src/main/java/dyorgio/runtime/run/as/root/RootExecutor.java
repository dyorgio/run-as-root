/** *****************************************************************************
 * Copyright 2020 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************** */
package dyorgio.runtime.run.as.root;

import dyorgio.runtime.out.process.CallableSerializable;
import dyorgio.runtime.out.process.OneRunOutProcess;
import dyorgio.runtime.out.process.OutProcessUtils;
import dyorgio.runtime.out.process.RunnableSerializable;
import dyorgio.runtime.run.as.root.impl.LinuxRootProcessBuilderFactory;
import dyorgio.runtime.run.as.root.impl.MacRootProcessBuilderFactory;
import dyorgio.runtime.run.as.root.impl.WinRootProcessBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Locale;
import java.util.concurrent.Callable;

/**
 * Run serializable <code>Callable</code>s and <code>Runnable</code>s in another
 * JVM with elevated privileges.<br>
 * Every <code>run()</code> or <code>call()</code> creates a new JVM and destroy
 * it.<br>
 * Normally this class can be a singleton if classpath and jvmOptions are always
 * equals, otherwise create a new instance for every cenario.<br>
 * <br>
 *
 * @author dyorgio
 * @see CallableSerializable
 * @see RunnableSerializable
 */
public class RootExecutor implements Serializable {

    /**
     * System property flag to identify an run-as-root code at runtime.
     */
    public static final String RUNNING_AS_ROOT = "$RunnningAsRoot";

    private static final RootProcessBuilderFactory MANAGER;

    static {
        String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        if ((OS.contains("mac")) || (OS.contains("darwin"))) {
            MANAGER = new MacRootProcessBuilderFactory();
        } else if (OS.contains("win")) {
            MANAGER = new WinRootProcessBuilderFactory();
        } else if (OS.contains("nux")) {
            MANAGER = new LinuxRootProcessBuilderFactory();
        } else {
            throw new RuntimeException("Unsupported OS:" + OS);
        }
    }

    private final OneRunOutProcess outProcess;

    /**
     * Creates an instance with specific java options
     *
     * @param javaOptions JVM options (ex:"-xmx32m")
     * @throws java.io.IOException In case of out-process creation problems.
     */
    public RootExecutor(String... javaOptions) throws IOException {
        this(null, javaOptions);
    }

    /**
     * Creates an instance with specific classpath and java options
     *
     * @param classpath custom classpath, use <code>null</code> to use current
     * JVM classpath.
     * @param javaOptions JVM options (ex:"-xmx32m")
     * @throws java.io.IOException In case of out-process creation problems.
     * @see OutProcessUtils#getCurrentClasspath()
     * @see OutProcessUtils#generateClassPath(java.lang.Class...)
     */
    public RootExecutor(String classpath, String[] javaOptions) throws IOException {
        this(null, classpath, javaOptions);
    }

    /**
     * Creates an instance with specific tmp dir, classpath and java options
     *
     * @param tmpDir Temporary dir, use <code>null</code> to use default tmp
     * dir.
     * @param classpath custom classpath, use <code>null</code> to use current
     * JVM classpath.
     * @param javaOptions JVM options (ex:"-xmx32m")
     * @throws java.io.IOException In case of out-process creation problems.
     * @see OutProcessUtils#getCurrentClasspath()
     * @see OutProcessUtils#generateClassPath(java.lang.Class...)
     */
    public RootExecutor(File tmpDir, String classpath, String... javaOptions) throws IOException {
        this.outProcess = new OneRunOutProcess(tmpDir, MANAGER, classpath, javaOptions);
    }

    /**
     * Runs runnable in a new JVM with elevated privileges.
     *
     * @param runnable A <code>RunnableSerializable</code> to run.
     * @throws Exception If cannot create a new JVM.
     * @throws UserCanceledException If user cancel or close prompt.
     * @throws NotAuthorizedException If user doesn't have root privileges.
     * @see RunnableSerializable
     */
    public void run(RunnableSerializable runnable) throws Exception, UserCanceledException, NotAuthorizedException {
        execute(runnable, false);
    }

    /**
     * Calls runnable in a new JVM with elevated privileges.
     *
     * @param <T> Result type.
     * @param callable A <code>CallableSerializable</code> to be called.
     * @return The result.
     * @throws Exception If cannot create a new JVM.
     * @throws UserCanceledException If user cancel or close prompt.
     * @throws NotAuthorizedException If user doesn't have root privileges.
     * @see CallableSerializable
     */
    public <T extends Serializable> T call(CallableSerializable<T> callable) throws Exception, UserCanceledException, NotAuthorizedException {
        return (T) execute(callable, true);
    }

    private Serializable execute(final Serializable command, final boolean hasResult) throws Exception, UserCanceledException, NotAuthorizedException {
        if (System.getProperty(RUNNING_AS_ROOT) != null) {
            if (hasResult) {
                Callable<? extends Serializable> callable = (Callable<? extends Serializable>) command;
                return callable.call();
            } else {
                ((Runnable) command).run();
                return null;
            }

        }

        OneRunOutProcess.OutProcessResult<Serializable> result = outProcess.call(new RemoteCall(command, hasResult));

        MANAGER.handleCode(result.getReturnCode());

        return result.getResult();
    }

    private static final class RemoteCall implements CallableSerializable<Serializable> {

        private final Serializable command;
        private final boolean hasResult;

        private RemoteCall(final Serializable command, boolean hasResult) {
            this.command = command;
            this.hasResult = hasResult;
        }

        @Override
        public Serializable call() throws Exception {
            System.setProperty(RUNNING_AS_ROOT, "true");
            if (hasResult) {
                Callable<? extends Serializable> callable = (Callable<? extends Serializable>) command;
                return callable.call();
            } else {
                ((Runnable) command).run();
                return null;
            }
        }
    }
}
