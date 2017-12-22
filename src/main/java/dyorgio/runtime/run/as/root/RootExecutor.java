package dyorgio.runtime.run.as.root;

import dyorgio.runtime.out.process.CallableSerializable;
import dyorgio.runtime.out.process.OneRunOutProcess;
import dyorgio.runtime.out.process.RunnableSerializable;
import dyorgio.runtime.run.as.root.impl.LinuxRootProcessManager;
import dyorgio.runtime.run.as.root.impl.MacRootProcessManager;
import dyorgio.runtime.run.as.root.impl.WinRootProcessManager;
import java.io.IOException;
import java.io.Serializable;
import java.util.Locale;
import java.util.concurrent.Callable;

/**
 *
 * @author dyorgio
 */
public class RootExecutor implements Serializable {

    private static final String RUNNING_AS_ROOT = "$RunnningAsRoot";

    private static final RootProcessManager MANAGER;

    static {
        String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        if ((OS.contains("mac")) || (OS.contains("darwin"))) {
            MANAGER = new MacRootProcessManager();
        } else if (OS.contains("win")) {
            MANAGER = new WinRootProcessManager();
        } else if (OS.contains("nux")) {
            MANAGER = new LinuxRootProcessManager();
        } else {
            throw new RuntimeException("Unsupported OS:" + OS);
        }
    }

    private final String[] javaOptions;

    public RootExecutor(String... javaOptions) throws IOException {
        this.javaOptions = javaOptions;
    }

    public void run(RunnableSerializable runnable) throws Exception, UserCanceledException, NotAuthorizedException {
        execute(runnable, false);
    }

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
        
        OneRunOutProcess outProcess = new OneRunOutProcess(MANAGER, javaOptions);

        OneRunOutProcess.OutProcessResult<Serializable> result = outProcess.call(new CallableSerializable<Serializable>() {
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
        });

        MANAGER.handleCode(result.getReturnCode());

        return result.getResult();
    }
}
