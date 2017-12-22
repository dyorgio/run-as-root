package dyorgio.runtime.run.as.root.impl;

import dyorgio.runtime.run.as.root.NotAuthorizedException;
import dyorgio.runtime.run.as.root.RootProcessManager;
import dyorgio.runtime.run.as.root.UserCanceledException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author dyorgio
 */
public class LinuxRootProcessManager implements RootProcessManager {

    @Override
    public ProcessBuilder create(List<String> commands) {
        List<String> copy = new ArrayList<>();
        copy.add("pkexec");
        copy.addAll(commands);
        return new ProcessBuilder(copy).inheritIO();
    }

    @Override
    public void handleCode(int code) throws NotAuthorizedException, UserCanceledException {
        switch (code) {
            case 127:
                throw new NotAuthorizedException();
            case 126:
                throw new UserCanceledException();
        }
    }
}
