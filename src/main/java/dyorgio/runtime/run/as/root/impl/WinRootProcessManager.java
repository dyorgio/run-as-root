package dyorgio.runtime.run.as.root.impl;

import dyorgio.runtime.run.as.root.NotAuthorizedException;
import dyorgio.runtime.run.as.root.RootProcessManager;
import dyorgio.runtime.run.as.root.UserCanceledException;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/**
 *
 * @author dyorgio
 */
public class WinRootProcessManager implements RootProcessManager {

    private static final String COMMAND_TO_ADMIN = "@echo off\n"
            + "\n"
            + ":: BatchGotAdmin\n"
            + ":-------------------------------------\n"
            + "REM  --> Check for permissions\n"
            + ">nul 2>&1 \"%SYSTEMROOT%\\system32\\cacls.exe\" \"%SYSTEMROOT%\\system32\\config\\system\"\n"
            + "\n"
            + "REM --> If error flag set, we do not have admin.\n"
            + "if '%errorlevel%' NEQ '0' (\n"
            + "    echo Requesting administrative privileges...\n"
            + "    goto UACPrompt\n"
            + ") else ( goto gotAdmin )\n"
            + "\n"
            + ":UACPrompt\n"
            + "    echo Set UAC = CreateObject^(\"Shell.Application\"^) > \"%temp%\\getadmin.vbs\"\n"
            + "    set params = %*:\"=\"\"\n"
            + "    echo UAC.ShellExecute \"cmd.exe\", \"/c %~s0 %params%\", \"\", \"runas\", 1 >> \"%temp%\\getadmin.vbs\"\n"
            + "\n"
            + "    \"%temp%\\getadmin.vbs\"\n"
            + "    del \"%temp%\\getadmin.vbs\"\n"
            + "    exit /B\n"
            + "\n"
            + ":gotAdmin\n"
            + "    pushd \"%CD%\"\n"
            + "    CD /D \"%~dp0\"\n"
            + ":--------------------------------------\n";

    @Override
    public ProcessBuilder create(List<String> commands) throws Exception {

        File file = File.createTempFile("tmp", "run-as-root");

        StringBuilder builder = new StringBuilder(COMMAND_TO_ADMIN);
        for (String command : commands) {
            builder.append(command).append(' ');
        }

        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(builder.toString().getBytes());
            out.flush();
        }

        return new ProcessBuilder("cmd.exe", "/c", file.getAbsolutePath()).inheritIO();
    }

    @Override
    public void handleCode(int code) throws NotAuthorizedException, UserCanceledException {
        System.err.println("CODE:" + code);
    }
}