/** *****************************************************************************
 * Copyright 2017 See AUTHORS file.
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

import dyorgio.runtime.out.process.ProcessBuilderFactory;

/**
 * Wraps out process commands into a elevate privileged command.
 *
 * @author dyorgio
 */
public interface RootProcessManager extends ProcessBuilderFactory {

    /**Verify out process return code and throw exception according with it value.
     * 
     * @param code Out process return code.
     * @throws NotAuthorizedException If used user doesn't have root privileges.
     * @throws UserCanceledException If user canceled or close prompt dialog.
     */
    void handleCode(int code) throws NotAuthorizedException, UserCanceledException;
}
