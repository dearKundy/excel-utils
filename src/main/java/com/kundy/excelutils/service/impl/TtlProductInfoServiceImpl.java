package com.kundy.excelutils.service.impl;

import com.kundy.excelutils.constant.ExcelFormat;
import com.kundy.excelutils.entity.ExcelHeaderInfo;
import com.kundy.excelutils.entity.po.TtlProductInfoPo;
import com.kundy.excelutils.mapper.TtlProductInfoMapper;
import com.kundy.excelutils.service.TtlProductInfoService;
import com.kundy.excelutils.utils.ExcelUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * @author kundy
 * @create 2019/2/16 11:22 AM
 */
@Service
@Slf4j
public class TtlProductInfoServiceImpl implements TtlProductInfoService {

    // 每个线程导出记录最大行数
    private static final int THREAD_MAX_ROW = 20000;

    @Autowired
    private TtlProductInfoMapper mapper;

    @Override
    public List<TtlProductInfoPo> listProduct(Map<String, Object> map) {
        return this.mapper.listProduct(map);
    }

    @Override
    public void export(HttpServletResponse response, String fileName) {
        ExcelUtils excelUtils = new ExcelUtils(this.multiThreadListProduct(), getHeaderInfo(), getFormatInfo());
        excelUtils.sendHttpResponse(response, fileName, excelUtils.getWorkbook());
    }

    // 获取表头信息
    private List<ExcelHeaderInfo> getHeaderInfo() {
        return Arrays.asList(
                new ExcelHeaderInfo(0, 0, 0, 0, "id"),
                new ExcelHeaderInfo(0, 0, 1, 1, "商品名称"),
                new ExcelHeaderInfo(0, 0, 2, 2, "类型ID"),
                new ExcelHeaderInfo(0, 0, 3, 3, "分类名称"),
                new ExcelHeaderInfo(0, 0, 4, 4, "品牌ID"),
                new ExcelHeaderInfo(0, 0, 5, 5, "品牌名称"),
                new ExcelHeaderInfo(0, 0, 6, 6, "商店ID"),
                new ExcelHeaderInfo(0, 0, 7, 7, "商店名称"),
                new ExcelHeaderInfo(0, 0, 8, 8, "价格"),
                new ExcelHeaderInfo(0, 0, 9, 9, "库存"),
                new ExcelHeaderInfo(0, 0, 10, 10, "销量"),
                new ExcelHeaderInfo(0, 0, 11, 11, "插入时间"),
                new ExcelHeaderInfo(0, 0, 12, 12, "更新时间"),
                new ExcelHeaderInfo(0, 0, 13, 13, "记录是否已经删除")
        );
    }

    // 获取格式化信息
    private Map<String, ExcelFormat> getFormatInfo() {
        Map<String, ExcelFormat> format = new HashMap<>();
        format.put("id", ExcelFormat.FORMAT_INTEGER);
        format.put("categoryId", ExcelFormat.FORMAT_INTEGER);
        format.put("branchId", ExcelFormat.FORMAT_INTEGER);
        format.put("shopId", ExcelFormat.FORMAT_INTEGER);
        format.put("price", ExcelFormat.FORMAT_DOUBLE);
        format.put("stock", ExcelFormat.FORMAT_INTEGER);
        format.put("salesNum", ExcelFormat.FORMAT_INTEGER);
        format.put("isDel", ExcelFormat.FORMAT_INTEGER);
        return format;
    }

    // 多线程查询报表
    private List<TtlProductInfoPo> multiThreadListProduct() {
        List<FutureTask<List<TtlProductInfoPo>>> tasks = new ArrayList<>();
        List<TtlProductInfoPo> productInfoPos = new ArrayList<>();

        int totalNum = 500000;
        int loopNum = new Double(Math.ceil(totalNum / THREAD_MAX_ROW)).intValue();
        log.info("多线程查询，总数：{},开启线程数：{}", totalNum, loopNum);
        long start = System.currentTimeMillis();

        executeTask(tasks, loopNum);

        for (FutureTask<List<TtlProductInfoPo>> task : tasks) {
            try {
                productInfoPos.addAll(task.get());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        log.info("查询结束，耗时:{}", System.currentTimeMillis() - start);
        return productInfoPos;
    }

    // 执行查询任务
    private void executeTask(List<FutureTask<List<TtlProductInfoPo>>> tasks, int loopNum) {
        for (int i = 0; i < loopNum; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("offset", i * THREAD_MAX_ROW);
            map.put("limit", THREAD_MAX_ROW);
            FutureTask<List<TtlProductInfoPo>> task = new FutureTask<>(new listThread(map));
            log.info("开始查询第{}条开始的{}条记录", i * THREAD_MAX_ROW, THREAD_MAX_ROW);
            new Thread(task).start();
            tasks.add(task);
        }
    }

    private class listThread implements Callable<List<TtlProductInfoPo>> {

        private Map<String, Object> map;

        public listThread(Map<String, Object> map) {
            this.map = map;
        }

        @Override
        public List<TtlProductInfoPo> call() {
            return listProduct(map);
        }
    }

}
