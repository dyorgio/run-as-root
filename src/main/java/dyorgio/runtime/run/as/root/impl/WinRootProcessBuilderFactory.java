/** *****************************************************************************
 * Copyright 2018 See AUTHORS file.
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
package dyorgio.runtime.run.as.root.impl;

import dyorgio.runtime.run.as.root.NotAuthorizedException;
import dyorgio.runtime.run.as.root.UserCanceledException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import dyorgio.runtime.run.as.root.RootProcessBuilderFactory;

/**
 * Elevate out process in Windows platform.
 *
 * @author dyorgio
 */
public class WinRootProcessBuilderFactory implements RootProcessBuilderFactory {

    static final File ELEVATE_EXECUTABLE_PATH;

    static {
        boolean isX86 = System.getenv("ProgramFiles(x86)") == null;
        String libName = isX86 ? "Elevate32.exe" : "Elevate64.exe";

        URL url = WinRootProcessBuilderFactory.class.getResource(libName);
        if (url == null) {
            url = WinRootProcessBuilderFactory.class.getResource("/win/elevate/" + libName);
        }
        if (url != null) {
            try {
                ELEVATE_EXECUTABLE_PATH = File.createTempFile("run-as-root", "-elevate");
                ELEVATE_EXECUTABLE_PATH.deleteOnExit();
                try (InputStream openStream = url.openStream()) {
                    try (OutputStream output = new FileOutputStream(ELEVATE_EXECUTABLE_PATH)) {
                        byte[] buffer = new byte[32 * 1024];
                        int readed;
                        while ((readed = openStream.read(buffer)) != -1) {
                            output.write(buffer, 0, readed);
                        }
                        output.flush();
                    }
                }

            } catch (Exception ex) {
                throw new RuntimeException("Unable to extract Elevate executable.", ex);
            }
        } else {
            throw new RuntimeException("Unable to find Elevate executable.");
        }
    }

    @Override
    public ProcessBuilder create(List<String> commands) throws Exception {

        List<String> elevateCommands = new ArrayList();
        elevateCommands.add("\"" + ELEVATE_EXECUTABLE_PATH.getAbsolutePath() + "\"");
        elevateCommands.add("-s 0");
        elevateCommands.add("-w");
        elevateCommands.add("--");
        elevateCommands.addAll(commands);

        return new ProcessBuilder(elevateCommands);
    }

    @Override
    public void handleCode(int code) throws NotAuthorizedException, UserCanceledException {
        switch (code) {
            case 1223:
                throw new UserCanceledException();
        }
    }
}
