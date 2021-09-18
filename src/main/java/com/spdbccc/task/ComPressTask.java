package com.spdbccc.task;

import cn.hutool.core.util.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Date 2021/9/17
 * @Author xujin
 * @TODO
 * @Version 1.0
 */
@Component
public class ComPressTask {

    private static Logger logger = LoggerFactory.getLogger(ComPressTask.class);

    @Value("${nas.bakPath}")
    private String nasBakPath;   //nas备份目录

    // private  static  String nasBakPath = "D:\\file\\compressTest\\nas-bak\\";   //nas备份目录


    @Value("${nas.srcPath}")
    private String nasSrckPath;  //nas源目录
    // private static String nasSrckPath  = "D:\\file\\compressTest\\nas\\";  //nas源目录

    ExecutorService executorService = Executors.newFixedThreadPool(5);

    /**
     * 解析待压缩文件所在文件目录
     * step 1 判断nas备份目录是否存在，不存在，则新建
     */
    public void parseFile() {
        File bakFile = new File(nasBakPath);
        File srcFile = new File(nasSrckPath);
        if (srcFile == null || bakFile == null) {
            logger.error("解析原nas目录或备份目录失败，可能其目录不存在");
            return;
        }

        //获取源目录下所有的文件
        File[] srcFiles = srcFile.listFiles();
        if (srcFiles != null) {
            for (File file : srcFiles) {
                StringBuilder sb = new StringBuilder(nasBakPath);
                //目录名为2020
                if (file.isDirectory()) {
                    //列出月份
                    sb.append(file.getName());  //拼接目标年
                    File[] months = file.listFiles();
                    for (File monthFile : months) { //遍历月份下的文件夹
                        sb.append(File.separator + monthFile.getName());   //拼接目标月份
                        if (monthFile.isDirectory()) {
                            File[] days = monthFile.listFiles();
                            List<File> fileList = new ArrayList<>();
                            for (File dayFiles : days) {  //遍历日下所有的文件
                                sb.append(File.separator + dayFiles.getName());   //拼接目标日
                                // 收集该天下所有的文件，存储起来，通知其它线程进行压缩
                                if (dayFiles.isDirectory()) {
                                    File[] files = dayFiles.listFiles();
                                    //开启线程压缩
                                    new ComPressThread(files, sb.toString()).start();
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    public void parseFileByConf() {
        //文件服务器目录   /home/nas/yyyy/mm/dd/a.wav
        File bakFile = new File(nasBakPath);  // /home/nas/2020
        File srcFile = new File(nasSrckPath);  //nas/nas-bak
        if (srcFile == null || bakFile == null) {
            logger.error("解析原nas目录或备份目录失败，可能其目录不存在");
            return;
        }
        //获取源目录下所有的文件
        File[] srcFiles = srcFile.listFiles();
        if (srcFiles != null) {
            for (File file : srcFiles) {
                StringBuilder sb = new StringBuilder(nasBakPath);
                //目录名为2020
                if (file.isDirectory()) {
                    //列出月份
                    sb.append(file.getName());  //拼接目标年
                    File[] months = file.listFiles();
                    for (File monthFile : months) { //遍历月份下的文件夹
                        sb.append(File.separator + monthFile.getName());   //拼接目标月份
                        if (monthFile.isDirectory()) {
                            File[] days = monthFile.listFiles();
                            List<File> fileList = new ArrayList<>();
                            for (File dayFiles : days) {  //遍历日下所有的文件
                                sb.append(File.separator + dayFiles.getName());   //拼接目标日
                                // 收集该天下所有的文件，存储起来，通知其它线程进行压缩
                                if (dayFiles.isDirectory()) {
                                    File[] files = dayFiles.listFiles();
                                    //开启线程SOX压缩
                                    new ComPressThread(files, sb.toString()).start();
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * 获取到了待压缩的文件列表集合
     *
     * @param
     */
//    public static void compress(List<File> files){
//
//    }
    public static void main(String[] args) {
        //compress();
        String nasBakPath = "D:\\file\\compressTest\\nas\\2020\\01\\10\\test.txt";
        File file = new File(nasBakPath);
        System.out.println(file.getName());
        System.out.println(file.getAbsolutePath());

//        String dirTest = "E:\\file\\compressTest\\nas\\2020\\01\\03";
//        File file1 = new File(dirTest);
//        if (!file1.exists()){
//            file1.mkdirs();
//        }
        //parseFile();
    }


}
