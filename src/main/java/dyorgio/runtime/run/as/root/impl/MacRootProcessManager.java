package dyorgio.runtime.run.as.root.impl;

import dyorgio.runtime.run.as.root.NotAuthorizedException;
import dyorgio.runtime.run.as.root.RootProcessManager;
import dyorgio.runtime.run.as.root.UserCanceledException;
import java.util.List;

/**
 *
 * @author dyorgio
 */
public class MacRootProcessManager implements RootProcessManager {

    @Override
    public ProcessBuilder create(List<String> commands) {
        StringBuilder builder = new StringBuilder();
        for (String command : commands) {
            builder.append(command).append(' ');
        }

        return new ProcessBuilder("osascript", "-e",//
                "do shell script \"" + builder + " 2>&1\" with administrator privileges").inheritIO();
    }

    @Override
    public void handleCode(int code) throws NotAuthorizedException, UserCanceledException {
        switch (code) {
            case 1:
                throw new UserCanceledException();
        }
    }

}
