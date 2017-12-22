package dyorgio.runtime.run.as.root;

import dyorgio.runtime.out.process.ProcessBuilderFactory;

/**
 *
 * @author dyorgio
 */
public interface RootProcessManager extends ProcessBuilderFactory {

    void handleCode(int code) throws NotAuthorizedException, UserCanceledException;
}
