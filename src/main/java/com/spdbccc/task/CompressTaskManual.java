package com.spdbccc.task;

import cn.hutool.core.io.FileUtil;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.swing.text.StyledEditorKit;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;


/**
 * @Date 2021/10/09
 * @Author xujin
 * @TODO 采用fork/join框架分割并行处理任务(1-12个月分割)
 * @Version 1.0
 */
@Component
public class CompressTaskManual extends RecursiveTask<Integer> {

    private static Logger logger = LoggerFactory.getLogger(CompressTaskManual.class);
    private static final int THRESHOLD = 5000;    //以多少文件量为分割临界点
    private int start;
    private int end;

    private String year;
    private String month;
    private String day;
    private File[] files;
    private String nasSrcPath;
    private String nasBakPath;
    private int bound;


    //构造函数初始化


    public CompressTaskManual(int start, int end, String year, String month, String day, File[] files, String nasSrcPath, String nasBakPath, int bound) {
        this.start = start;
        this.end = end;
        this.year = year;
        this.month = month;
        this.day = day;
        this.files = files;
        this.nasSrcPath = nasSrcPath;
        this.nasBakPath = nasBakPath;
        this.bound = bound;
    }

    public CompressTaskManual() {
    }

    @Override
    protected Integer compute() {
        Integer total = 0;
        //便利该天下所有的文件，5万一批
        List<File> fs = new ArrayList<File>(Arrays.asList(files));
        if (end - start <= bound) {   //小于50000个文件，单线程
            List<File> fileList = fs.subList(start - 1, end);
            StringBuilder sb = new StringBuilder(nasBakPath);
            sb.append(File.separator)
                    .append(year).append(File.separator)
                    .append(month).append(File.separator)
                    .append(day);  //   /nasbak/year/month/month/day
            new ComPressThreadManual(fileList, sb.toString()).start();
            total = fileList.size();
        } else { //分批处理
            int middle = (start + end) / 2;
            CompressTaskManual firstTask = new CompressTaskManual(start, middle, year, month, day, files, nasSrcPath, nasBakPath, bound);
            CompressTaskManual secondTask = new CompressTaskManual(middle + 1, end, year, month, day, files, nasSrcPath, nasBakPath, bound);
            invokeAll(firstTask, secondTask);
            total = firstTask.join() + secondTask.join();
        }
        return total;

    }


    public boolean compressTask(File[] fs, String year, String month, String day, String nasSrcPath, String nasBakPath, int bound) throws Exception {

        ForkJoinPool pool = new ForkJoinPool();
        ForkJoinTask<Integer> submit = pool.submit(new CompressTaskManual(1, fs.length, year, month, day, fs, nasSrcPath, nasBakPath, bound));
        Integer result = submit.get();
        if (fs.length == result) {
            logger.info("finish compress current date【year = {},month = {},day = {},current filesize = {},has compressed filesize = {}】 "
                    , year, month, day, fs.length, result);
            return true;
        } else {
            logger.warn("error compress current date【year = {},month = {},day = {},current filesize = {},has compressed filesize = {}】 "
                    , year, month, day, fs.length, result);
            return false;
        }
    }


    public boolean compressTaskByManual(String year, String month, String nasSrcPath, String nasBakPath, int bound) {

        long st = System.currentTimeMillis();
        File srcFile = new File(nasSrcPath);
        File bakFile = new File(nasBakPath);
        if (srcFile == null || bakFile == null) {
            logger.error("解析原nas目录或备份目录失败，可能其目录不存在");
            return false;
        }
        //获取源nas文件下的月份下的文件 pathname = /home/ns/year/month/
        String pathName = nasSrcPath + year + File.separator + month + File.separator;
        File monthFiles = new File(pathName);
        if (monthFiles == null) {
            logger.error("not find file file object(/src/year/month/) [year = {},month = {}]", year, month);
            return false;
        }
        File[] dayFiles = monthFiles.listFiles();   //该月份下所有的日期文件夹 如1号的文件  01
        if (dayFiles == null) {
            logger.info("not find day director  at current date [year = {},month = {}......", year, month);
            return false;
        }
        for (File files : dayFiles) { //便利月份下所属天数
            // 收集该天下所有的文件，存储起来，通知其它线程进行压缩
            if (files.isDirectory()) {
                File[] fs = files.listFiles();
                if (fs == null) {
                    logger.info("not find record files at current date [year = {},month = {},day = {}......", year, month, files.getName());
                    continue;
                }
                logger.info("get [year = {},month ={},day = {}] curent day files size = {}", year, month, files.getName(), fs.length);
                //开启线程压缩   fork/join
                try {
                    compressTask(fs, year, month, files.getName(), nasSrcPath, nasBakPath, bound);
                } catch (Exception e) {
                    logger.error("compress error 【year = {}.month = {},day = {}】 ......", year, month, files.getName(), e);
                    continue;
                }
            }
        }
        return true;
    }


    /**
     * @param year
     * @param month 月份
     * @param sday  起始日期
     * @param eday  结束日期
     * @return
     */
    public boolean compressByRange(String year, String month, String sday, String eday, String nasSrcPath, String nasBakPath, int bound) {

        File srcFile = new File(nasSrcPath);
        File bakFile = new File(nasBakPath);
        if (srcFile == null || bakFile == null) {
            logger.error("解析原nas目录或备份目录失败，可能其目录不存在");
            return false;
        }
        //获取源nas文件下的起始月份下的文件 pathname = /home/ns/year/month/
        String pathName = nasSrcPath + year + File.separator + month + File.separator;
        //便利月份
        File monthFiles = new File(pathName);
        if (monthFiles == null) {
            logger.error("not find file file object(/src/year/month/) [year = {},month = {}]", year, month);
            return false;
        }
        boolean flag = false;
        File[] dayFiles = monthFiles.listFiles();   //该月份下所有的日期文件夹 如1号的文件  01
        if (dayFiles == null) {
            logger.info("not find day director  at current date [year = {},month = {}......", year, month);
            return false;
        }

        for (File files : dayFiles) { //便利月份下所属天数
            boolean b = judgeTimeRange(files.getName(), sday, eday);
            // 收集该天下所有的文件，存储起来，通知其它线程进行压缩
            if (b && files.isDirectory()) {
                File[] fs = files.listFiles();
                if (fs == null) {
                    logger.info("not find record files at current date [year = {},month = {},day = {}......", year, month, files.getName());
                    continue;
                }
                logger.info("get [year = {},month ={},day = {}] curent day files size = {}", year, month, files.getName(), fs.length);
                //开启线程压缩   fork/join
                try {
                    flag = compressTask(fs, year, month, files.getName(), nasSrcPath, nasBakPath, bound);
                } catch (Exception e) {
                    logger.error("compress error 【year = {}.month = {},day = {}】 ......", year, month, files.getName(), e);
                }
            }
            continue;
        }
        return flag;
    }

    /**
     * @param currentDay 01
     * @param sday       01
     * @param eday       02
     * @return todo 判断日期是否在该范围内
     */
    public boolean judgeTimeRange(String currentDay, String sday, String eday) {

        int curr = Integer.valueOf(currentDay);
        int sd = Integer.valueOf(sday);
        int ed = Integer.valueOf(eday);

        return curr >= sd && curr <= ed ? true : false;
    }


    public static void rename() {
        File file = new File("D:\\videoTest\\2020\\01\\01");
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                FileUtil.rename(f, UUID.randomUUID().toString() + ".wav", true);
            }
        }
    }


    public static void main(String[] args) throws Exception {
//        long start = System.currentTimeMillis();
//        for (int i = 1; i<=5000000;i++){
//            System.out.println("get value is "+i);
//        }
//        long end = System.currentTimeMillis();
//        System.out.println("cost time = "+(end-start)+"ms");
//        String [] src = {"123","456","789"};
//        List<String> fileList = new ArrayList<>();
//        Collections.addAll(fileList,src);
//        String[] tar;
//        List<String> stringList = fileList.subList(0, 2);
//        for (String str: stringList){
//            System.out.println(str);
//        }

        // rename();

        // boolean flag = judgeTimeRange("01","02","05");
        //System.out.println("是否在指定区间内？result = "+flag);

    }

}
