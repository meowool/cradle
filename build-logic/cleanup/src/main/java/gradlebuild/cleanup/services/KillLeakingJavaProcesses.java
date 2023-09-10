/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gradlebuild.cleanup.services;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * NOTICE: this class is invoked via java command line, so we must NOT DEPEND ON ANY 3RD-PARTY LIBRARIES except JDK 11.
 *
 * Usage: java build-logic/cleanup/src/main/java/gradlebuild/cleanup/services/KillLeakingJavaProcesses.java
 */
public class KillLeakingJavaProcesses {
    private static final Pattern UNIX_PID_PATTERN = Pattern.compile("([0-9]+)");
    private static final Pattern WINDOWS_PID_PATTERN = Pattern.compile("([0-9]+)\\s*$");
    private static final String MY_PID = String.valueOf(ProcessHandle.current().pid());

    static String generateLeakingProcessKillPattern(String rootProjectDir) {
        String javaExecutable = "java(?:\\.exe)?";
        String kotlinCompilerDaemonPattern = "(?:" + Pattern.quote("-Dkotlin.environment.keepalive org.jetbrains.kotlin.daemon.KotlinCompileDaemon") + ")";
        String quotedRootProjectDir = Pattern.quote(rootProjectDir);
        String mainClassPattern = "(org\\.gradle\\.|[a-zA-Z]+)";
        String playServerPattern = "(play\\.core\\.server\\.NettyServer)";
        String javaProcessStackTracesMonitorPattern = "(JavaProcessStackTracesMonitor\\.java)";
        String classPathPattern1 = "(?:-cp.+" + "(" + quotedRootProjectDir + "|build\\\\tmp\\\\performance-test-files)" + ".+?" + mainClassPattern + ")";
        String classPathPattern2 = "(?:-classpath.+" + quotedRootProjectDir + ".+?" + Pattern.quote("\\build\\") + ".+?" + mainClassPattern + ")";
        String classPathPattern3 = "(?:-classpath.+" + quotedRootProjectDir + ".+?" + playServerPattern + ")";
        return "(?i)[/\\\\]" + "(" + javaExecutable + ".+?" + "(?:" +
            classPathPattern1 + "|" +
            classPathPattern2 + "|" +
            classPathPattern3 + "|" +
            kotlinCompilerDaemonPattern + "|" +
            javaProcessStackTracesMonitorPattern +
            ").+)";
    }

    public static void main(String[] args) {
        File rootProjectDir = new File(System.getProperty("user.dir"));

        cleanPsOutputFilesFromPreviousRun(rootProjectDir, args);

        List<String> psOutput = ps(rootProjectDir);

        writePsOutputToFile(rootProjectDir, psOutput);

        forEachLeakingJavaProcess(psOutput, rootProjectDir, pid -> {
            System.out.println("A process wasn't shutdown properly in a previous Gradle run. Killing process with PID " + pid);
            pkill(pid);
        });
    }

    private static void writePsOutputToFile(File rootProjectDir, List<String> psOutput) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
        File psOutFile = new File(rootProjectDir, timestamp + ".psoutput");

        try {
            Files.write(psOutFile.toPath(), psOutput);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void cleanPsOutputFilesFromPreviousRun(File rootProjectDir, String[] args) {
        if (args.length > 0 && "KILL_LEAKED_PROCESSES_FROM_PREVIOUS_BUILDS".equals(args[0])) {
            File[] psOutputs = rootProjectDir.listFiles((__, name) -> name.endsWith(".psoutput"));
            if (psOutputs != null) {
                Stream.of(psOutputs).forEach(File::delete);
            }
        }
    }

    static void pkill(String pid) {
        ExecResult execResult = run(isWindows() ? new String[]{"taskkill.exe", "/F", "/T", "/PID", pid} : new String[]{"kill", "-9", pid});
        if (execResult.code != 0) {
            System.out.println("Failed to kill daemon process " + pid + ". Maybe already killed?\nStdout:\n" + execResult.stdout + "\nStderr:\n" + execResult.stderr);
        }
    }

    static void forEachLeakingJavaProcess(File rootProjectDir, Consumer<String> action) {
        forEachLeakingJavaProcess(ps(rootProjectDir), rootProjectDir, action);
    }

    private static void forEachLeakingJavaProcess(List<String> psOutput, File rootProjectDir, Consumer<String> action) {
        Pattern commandLineArgsPattern = Pattern.compile(generateLeakingProcessKillPattern(rootProjectDir.getPath()));
        Pattern pidPattern = isWindows() ? WINDOWS_PID_PATTERN : UNIX_PID_PATTERN;

        psOutput.forEach(line -> {
            Matcher commandLineArgsMatcher = commandLineArgsPattern.matcher(line);
            Matcher pidMatcher = pidPattern.matcher(line);
            if (commandLineArgsMatcher.find() && pidMatcher.find()) {
                String pid = pidMatcher.group(1);
                if (!MY_PID.equals(pid)) {
                    action.accept(pid);
                }
            }
        });
    }

    private static List<String> ps(File rootProjectDir) {
        return run(determinePsCommand()).assertZeroExit().stdout.lines().collect(Collectors.toList());
    }

    private static String[] determinePsCommand() {
        if (isWindows()) {
            return new String[]{"wmic", "process", "get", "processid,commandline"};
        } else if (isMacOS()) {
            return new String[]{"ps", "x", "-o", "pid,command"};
        } else {
            return new String[]{"ps", "x", "-o", "pid,cmd"};
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    private static boolean isMacOS() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }

    private static class ExecResult {
        private final String[] args;
        private final int code;
        private final String stdout;
        private final String stderr;

        public ExecResult(String[] args, int code, String stdout, String stderr) {
            this.args = args;
            this.code = code;
            this.stdout = stdout;
            this.stderr = stderr;
        }

        @Override
        public String toString() {
            return "ExecResult{" +
                "code=" + code +
                "\n stdout='" + stdout + '\'' +
                "\n stderr='" + stderr + '\'' +
                '}';
        }

        ExecResult assertZeroExit() {
            if (code != 0) {
                throw new AssertionError(String.format("%s return:\n%s\n%s\n", Arrays.toString(args), stdout, stderr));
            }
            return this;
        }
    }

    private static ExecResult run(String... args) {
        try {
            Process process = new ProcessBuilder().command(args).start();
            CountDownLatch latch = new CountDownLatch(2);
            ByteArrayOutputStream stdout = connectStream(process.getInputStream(), latch);
            ByteArrayOutputStream stderr = connectStream(process.getErrorStream(), latch);

            process.waitFor(1, TimeUnit.MINUTES);
            latch.await(1, TimeUnit.MINUTES);
            return new ExecResult(args, process.exitValue(), stdout.toString(), stderr.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static ByteArrayOutputStream connectStream(InputStream forkedProcessOutput, CountDownLatch latch) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os, true);
        new Thread(() -> {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(forkedProcessOutput));
                String line;
                while ((line = reader.readLine()) != null) {
                    ps.println(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        }).start();
        return os;
    }
}
