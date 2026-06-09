package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getDateList(begin, end);
        String shopId = BaseContext.getCurrentShopId();

        List<BigDecimal> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime dayBegin = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.MAX);
            BigDecimal turnover = orderMapper.sumByStatusAndTime(shopId, dayBegin, dayEnd);
            turnover = turnover == null ? BigDecimal.ZERO : turnover;
            turnoverList.add(turnover);
        }

        return TurnoverReportVO.builder()
                .dateList(joinList(dateList))
                .turnoverList(joinList(turnoverList))
                .build();
    }

    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getDateList(begin, end);
        String shopId = BaseContext.getCurrentShopId();

        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        int totalOrderCount = 0;
        int validOrderCount = 0;

        for (LocalDate date : dateList) {
            LocalDateTime dayBegin = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.MAX);

            Integer orderCount = orderMapper.countByTime(shopId, dayBegin, dayEnd);
            Integer validCount = orderMapper.countValidByTime(shopId, dayBegin, dayEnd);

            orderCountList.add(orderCount);
            validOrderCountList.add(validCount);
            totalOrderCount += orderCount;
            validOrderCount += validCount;
        }

        double orderCompletionRate = totalOrderCount == 0 ? 0 : (double) validOrderCount / totalOrderCount;

        return OrderReportVO.builder()
                .dateList(joinList(dateList))
                .orderCountList(joinList(orderCountList))
                .validOrderCountList(joinList(validOrderCountList))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getDateList(begin, end);

        List<Integer> totalUserList = new ArrayList<>();
        List<Integer> newUserList = new ArrayList<>();

        for (LocalDate date : dateList) {
            LocalDateTime dayBegin = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.MAX);

            // 截止当天的总用户数（简化：使用当前总数）
            Integer totalUsers = userMapper.count();
            Integer newUsers = userMapper.countByCreateTime(dayBegin, dayEnd);

            totalUserList.add(totalUsers);
            newUserList.add(newUsers);
        }

        return UserReportVO.builder()
                .dateList(joinList(dateList))
                .totalUserList(joinList(totalUserList))
                .newUserList(joinList(newUserList))
                .build();
    }

    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        String shopId = BaseContext.getCurrentShopId();

        List<Map<String, Object>> top10 = orderDetailMapper.getTop10(shopId, beginTime, endTime);

        List<String> nameList = new ArrayList<>();
        List<Integer> numberList = new ArrayList<>();
        for (Map<String, Object> map : top10) {
            nameList.add((String) map.get("name"));
            numberList.add(((Number) map.get("number")).intValue());
        }

        return SalesTop10ReportVO.builder()
                .nameList(joinList(nameList))
                .numberList(joinList(numberList))
                .build();
    }

    @Override
    public BusinessDataVO getBusinessData(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getDateList(begin, end);
        String shopId = BaseContext.getCurrentShopId();

        BigDecimal totalTurnover = BigDecimal.ZERO;
        int totalValidOrderCount = 0;
        int totalOrderCount = 0;
        int totalNewUsers = 0;

        for (LocalDate date : dateList) {
            LocalDateTime dayBegin = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.MAX);

            BigDecimal turnover = orderMapper.sumByStatusAndTime(shopId, dayBegin, dayEnd);
            turnover = turnover == null ? BigDecimal.ZERO : turnover;
            totalTurnover = totalTurnover.add(turnover);

            Integer validCount = orderMapper.countValidByTime(shopId, dayBegin, dayEnd);
            totalValidOrderCount += validCount;

            Integer orderCount = orderMapper.countByTime(shopId, dayBegin, dayEnd);
            totalOrderCount += orderCount;

            Integer newUsers = userMapper.countByCreateTime(dayBegin, dayEnd);
            totalNewUsers += newUsers;
        }

        double orderCompletionRate = totalOrderCount == 0 ? 0 : (double) totalValidOrderCount / totalOrderCount;
        double unitPrice = totalValidOrderCount == 0 ? 0 : totalTurnover.divide(BigDecimal.valueOf(totalValidOrderCount), 2, RoundingMode.HALF_UP).doubleValue();

        return BusinessDataVO.builder()
                .turnover(totalTurnover.doubleValue())
                .validOrderCount(totalValidOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .newUsers(totalNewUsers)
                .build();
    }

    @Override
    public void export(HttpServletResponse response) {
        LocalDate begin = LocalDate.now().plusDays(-30);
        LocalDate end = LocalDate.now().plusDays(-1);
        List<LocalDate> dateList = getDateList(begin, end);

        // 概览数据
        BusinessDataVO businessData = getBusinessData(begin, end);

        // 每日明细数据
        String shopId = BaseContext.getCurrentShopId();
        List<BusinessDataVO> dailyDataList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime dayBegin = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.MAX);

            BigDecimal turnover = orderMapper.sumByStatusAndTime(shopId, dayBegin, dayEnd);
            turnover = turnover == null ? BigDecimal.ZERO : turnover;

            Integer validOrderCount = orderMapper.countValidByTime(shopId, dayBegin, dayEnd);
            Integer orderCount = orderMapper.countByTime(shopId, dayBegin, dayEnd);

            double orderCompletionRate = orderCount == 0 ? 0 : (double) validOrderCount / orderCount;
            double unitPrice = validOrderCount == 0 ? 0 : turnover.divide(BigDecimal.valueOf(validOrderCount), 2, RoundingMode.HALF_UP).doubleValue();
            Integer newUsers = userMapper.countByCreateTime(dayBegin, dayEnd);

            dailyDataList.add(BusinessDataVO.builder()
                    .turnover(turnover.doubleValue())
                    .validOrderCount(validOrderCount)
                    .orderCompletionRate(orderCompletionRate)
                    .unitPrice(unitPrice)
                    .newUsers(newUsers)
                    .build());
        }

        // 基于模板写入Excel
        InputStream templateStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try {
            Workbook workbook = new XSSFWorkbook(templateStream);
            Sheet sheet = workbook.getSheetAt(0);

            // 填充概览数据行（Row 2 = 日期范围, Row 4/5 = 概览）
            // Row 2: 日期范围 (B2:G2 merged)
            Row row2 = sheet.getRow(1);
            Cell dateRangeCell = row2.getCell(1);
            if (dateRangeCell == null) {
                dateRangeCell = row2.createCell(1);
            }
            String beginStr = begin.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String endStr = end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            dateRangeCell.setCellValue(beginStr + " 至 " + endStr);

            // Row 4: 营业额(C4), 订单完成率(E4), 新增用户数(G4)
            Row row4 = sheet.getRow(3);
            row4.getCell(2).setCellValue(businessData.getTurnover());
            row4.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            row4.getCell(6).setCellValue(businessData.getNewUsers());

            // Row 5: 有效订单(C5), 平均客单价(E5)
            Row row5 = sheet.getRow(4);
            row5.getCell(2).setCellValue(businessData.getValidOrderCount());
            row5.getCell(4).setCellValue(businessData.getUnitPrice());

            // 填充明细数据（从第8行开始，索引7）
            for (int i = 0; i < dateList.size(); i++) {
                Row row = sheet.getRow(7 + i);
                if (row == null) {
                    row = sheet.createRow(7 + i);
                }
                BusinessDataVO dailyData = dailyDataList.get(i);

                Cell dateCell = row.createCell(1);
                dateCell.setCellValue(dateList.get(i).toString());

                Cell turnoverCell = row.createCell(2);
                turnoverCell.setCellValue(dailyData.getTurnover());

                Cell validCell = row.createCell(3);
                validCell.setCellValue(dailyData.getValidOrderCount());

                Cell completionCell = row.createCell(4);
                completionCell.setCellValue(dailyData.getOrderCompletionRate());

                Cell priceCell = row.createCell(5);
                priceCell.setCellValue(dailyData.getUnitPrice());

                Cell newUsersCell = row.createCell(6);
                newUsersCell.setCellValue(dailyData.getNewUsers());
            }

            // 通过输出流下载文件
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment;filename=运营数据报表.xlsx");
            workbook.write(response.getOutputStream());
            workbook.close();
        } catch (IOException e) {
            log.error("导出运营数据报表失败", e);
        }
    }

    private List<LocalDate> getDateList(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        while (!begin.isAfter(end)) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }
        return dateList;
    }

    private String joinList(List<?> list) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(list.get(i));
        }
        return sb.toString();
    }
}
