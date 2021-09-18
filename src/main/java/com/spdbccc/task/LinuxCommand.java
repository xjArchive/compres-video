package com.spdbccc.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @Date 2021/9/17
 * @Author xujin
 * @TODO
 * @Version 1.0
 */
public class LinuxCommand {

    private static Logger logger = LoggerFactory.getLogger(LinuxCommand.class);
    private static final Charset UTF8 = StandardCharsets.UTF_8;

    public static ExecuteResult execute(List<String> commands) {

        StringBuilder success = new StringBuilder("");
        StringBuilder failed = new StringBuilder("");
        if (commands.isEmpty()) {
            return ExecuteResult.failed("Invaild commands: " + commands);
        }

        Process process = null;
        try {
            ProcessBuilder builder = new ProcessBuilder(commands);
            StringBuilder sb = new StringBuilder();
            for (String cmd : commands) {
                sb.append(cmd).append(" ");
            }
            logger.info("execute commands = {}", commands);
            process = builder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream(), UTF8))) {
            String readLine = null;
            while ((readLine = reader.readLine()) != null) {
                success.append(readLine).append("\n");
            }
        } catch (Exception e) {
            logger.error("faile2 = [{}]", process);
            return ExecuteResult.failed(e);
        }
        return ExecuteResult.success(success.toString());


    }

}
