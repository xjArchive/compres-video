package com.spdbccc.controller;

import cn.hutool.core.util.StrUtil;
import com.spdbccc.task.ComPressTask;
import com.spdbccc.task.CompressTaskManual;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Date 2021/9/17
 * @Author xujin
 * @TODO
 * @Version 1.0
 */
@RestController
public class CompressController {

    private Logger logger = LoggerFactory.getLogger(CompressController.class);

    @Autowired
    private ComPressTask comPressTask;

    @Autowired
    private CompressTaskManual compressTaskManual;
    ;

    @Value("${nas.srcPath}")
    private String nasSrcPath;  //nas源目录
    //private static String nasSrcPath  = "D:\\videoTest\\";  //nas源目录

    @Value("${nas.bakPath}")
    private String nasBakPath;   //nas备份目录
    //private  static  String nasBakPath = "D:\\videoTest1\\";   //nas备份目录

    @Value("${compress.bound}")
    private String bound;   //以多少进行分批

    /**
     * todo 手动调用   指定年份及月份
     *
     * @param year
     * @param month
     * @return
     */
    @RequestMapping("manual")
    public String compressByYearAndMonth(@RequestParam(value = "year") String year, @RequestParam(value = "month") String month) {
        logger.info("==========   start manual compress...【year = {},month = {}】============", year, month);
        if (StrUtil.isEmpty(year) || StrUtil.isEmpty(month)) {
            logger.error("bad params.......");
            return "bad params !!!!!  please check......";
        }
        boolean b = compressTaskManual.compressTaskByManual(year, month, nasSrcPath, nasBakPath, Integer.valueOf(bound));
        long end = System.currentTimeMillis();
        if (b) {
            logger.info("==========  end manual compress...【year = {},month = {}============", year, month);
            return "compress success [year = " + year + ",month = " + month + "]";
        } else {
            logger.warn("compress");
            return "compress fail [year = " + year + ",month = " + month + "]";
        }
    }

    @RequestMapping("auto")
    public String compressByYearAndMonth() {
        comPressTask.autoCompressTask();
        return "auto compress success...";
    }

    /**
     * @param year  指定年份
     * @param month 开始日期  eg:  01
     * @param range 结束日期  eg:  01-02
     * @return
     */
    @RequestMapping(value = "/manual/range")
    public String compressByBetweenDays(@RequestParam(value = "year") String year,
                                        @RequestParam(value = "month") String month,
                                        @RequestParam(value = "range") String range) {

        if (StrUtil.isNotEmpty(year) && StrUtil.isNotEmpty(month) && StrUtil.isNotEmpty(range)) {
            String[] split = range.split("-");
            if (split != null && split.length == 2) {
                compressTaskManual.compressByRange(year, month, split[0], split[1], nasSrcPath, nasBakPath, Integer.valueOf(bound));
                return "compress success.....";
            }
            //说明不是01-02这种格式，存在输入了一天的格式，，eg: 01
            compressTaskManual.compressByRange(year, month, range, range, nasSrcPath, nasBakPath, Integer.valueOf(bound));
            return "compress sucess";
        }
        return "valid request parms.....";
    }


}
