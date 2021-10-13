package com.spdbccc.task;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Date 2021/10/09
 * @Author xujin
 * @TODO 定时迁移处理前一天的录音
 * @Version 1.0
 */
@Component
public class ComPressTask {

    private static Logger logger = LoggerFactory.getLogger(ComPressTask.class);

    private static final String Auto_COMPRESS = "1";
    private static final String MANUAL_COMPRESS = "0";

    @Autowired
    private CompressTaskManual compressTaskManual;

    @Value("${nas.srcPath}")
    private String nasSrcPath;  //nas源目录
    //private static String nasSrcPath  = "D:\\videoTest\\";  //nas源目录

    @Value("${nas.bakPath}")
    private String nasBakPath;   //nas备份目录
    //private  static  String nasBakPath = "D:\\videoTest1\\";   //nas备份目录
    @Value("${auto.compress}")
    private String isAutoCompress;   //是否启用自动压缩任务

    @Value("${compress.bound}")
    private String bound;   //以多少进行分批

    @Scheduled(cron = "${auto.time}")
    public void autoCompressTask() {
        String way = Auto_COMPRESS;
        if (StrUtil.equals(isAutoCompress, Auto_COMPRESS)) {   //启用自动压缩
            //获取当前年份，月份，日期
            String[] dates = getCompressDate();
            logger.info("==========  start auto compress...【year = {},month = {},day = {}===========", dates[0], dates[1], dates[2]);
            boolean b = compressTaskByAuto(dates[0], dates[1], dates[2], nasSrcPath, nasBakPath, Integer.valueOf(bound));
            if (b) {
                logger.info("==========  end auto compress...【year = {},month = {},day = {}】 ============", dates[0], dates[1], dates[2]);
            } else {
                logger.info("==========  end auto compress compress  faile cause of [压缩不完全或者没有找到待压缩文件等] 【year = {},month = {},day = {}】============", dates[0], dates[1], dates[2]);
            }
        } else {
            way = MANUAL_COMPRESS;
        }
        logger.info("judge compress way 【manual(0)  or auto(1)　?】 result = {} ....", way);
    }


    public boolean compressTaskByAuto(String year, String month, String day, String nasSrcPath, String nasBakPath, int bound) {

        File srcFile = new File(nasSrcPath);
        File bakFile = new File(nasBakPath);
        if (srcFile == null || bakFile == null) {
            logger.error("解析原nas目录或备份目录失败，可能其目录不存在");
            return false;
        }
        //获取源nas文件下的月份下的文件 pathname = /home/ns/year/month/
        String pathName = nasSrcPath + year + File.separator + month + File.separator + day + File.separator;
        File dayFile = new File(pathName);
        if (dayFile == null) {
            logger.error("not find file file object(/src/year/month/day) [year = {},month = {},day = {}]", year, month, day);
            return false;
        }
        File[] dayFiles = dayFile.listFiles();
        if (dayFiles == null) {
            logger.info("not find record files at current date [year = {},month = {},day = {}]......", year, month, day);
            return false;
        }
        //开启线程压缩   fork/join
        try {
            return compressTaskManual.compressTask(dayFiles, year, month, day, nasSrcPath, nasBakPath, bound);
        } catch (Exception e) {
            logger.error("compress error 【year = {}.month = {},day = {}】 ......", year, month, day, e);
        }
        return false;
    }


    /**
     * 解析待压缩文件所在文件目录
     * step 1 判断nas备份目录是否存在，不存在，则新建
     */
    /*public void parseFile() {
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
                                    //开启线程压缩   fork/join
                                    new ComPressThread(files, sb.toString()).start();
                                }
                            }
                        }
                    }
                }
            }
        }
    }*/
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


    /**
     * 解析前一天日期格式
     *
     * @return year month day
     */
    public String[] getCompressDate() {

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd ");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MONTH, -1);
        Date time = calendar.getTime();
        String format1 = format.format(time);
        return format1.split("-");
    }


}
