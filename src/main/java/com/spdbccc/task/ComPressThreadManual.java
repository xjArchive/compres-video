package com.spdbccc.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @Date 2021/10/09
 * @Author xujin
 * @TODO
 * @Version 1.0
 */
public class ComPressThreadManual extends Thread {
    private static Logger logger = LoggerFactory.getLogger(ComPressThreadManual.class);

    private List<File> fs;

    private String targetPath;

    public ComPressThreadManual(List<File> fs, String targetPath) {
        this.fs = fs;
        this.targetPath = targetPath;
    }

    /**
     * 压缩线程
     */
    public void run() {
        logger.info("start excute compress thread【nasBakPath = {}】....", targetPath);
        for (File tmp : fs) {   //开始处理压缩逻辑
            List<String> commands = new ArrayList<>();
            //src->target
            //目标目录的解析
            String name = tmp.getName();   //文件名称,包括后缀
            //String s =  targetPath+File.separator+name;   //目标路径/home/2020/02/20/1.wav
            File des = new File(targetPath);
            if (!des.exists()) {
                des.mkdirs();
            }
            String tar = des.getAbsolutePath() + File.separator + tmp.getName();
            if (new File(tar).exists()) {
                continue;
            }
            logger.info("get compress filename = {}", tar);
            commands.add("/bin/sh");
            commands.add("-c");
            commands.add(String.format("sox %s -r 8000 -c 1 -e gsm-full-rate %s", tmp.getAbsolutePath(), tar));
            commands.add("2>/dev/null");   //忽略警告信息
            ExecuteResult er = LinuxCommand.execute(commands);
            logger.info("result linux = [{}]", er);
            if (er.isSuccess()) {
                logger.info("compress complete ! new file: [{}]", tar);
            } else {
                logger.error("compress faile! source file: [{}]", tmp.getAbsolutePath());
            }
        }
    }

}
