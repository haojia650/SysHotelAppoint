package com.example.web.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.web.dto.RoomMatchDto;
import com.example.web.dto.query.AdminHotelDataAnalysisQueryInput;
import com.example.web.dto.query.HotelDataAnalysisQueryInput;
import com.example.web.entity.*;
import com.example.web.enums.AppointStatusEnum;
import com.example.web.enums.RoomStatusEnum;
import com.example.web.mapper.*;
import com.example.web.service.BaseService;
import com.example.web.tools.Extension;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BaseServiceImpl implements BaseService {

    @Autowired
    private AppUserMapper AppUserMapper;
    @Autowired
    private RoomMapper RoomMapper;

    @Autowired
    private RoomDetMapper RoomDetMapper;

    @Autowired
    private FavourableSettingMapper FavourableSettingMapper;
    @Autowired
    private RoomPriceMapper RoomPriceMapper;

    @Autowired
    private AppointMapper AppointMapper;
    /**
     * 操作数据库的RoomMatch表mapper对象
     */
    @Autowired
    private RoomMatchMapper RoomMatchMapper;

    @Autowired
    private HotelMapper HotelMapper;

    @Autowired
    private StorageRecordMapper StorageRecordMapper;

    @Autowired
    private EmployeeMapper EmployeeMapper;

    @Autowired
    private GuestInfoMapper GuestInfoMapper;

    /**
     * 得到房间选择时间段的价格
     *
     * @param hotelId
     * @param roomId
     * @return
     */
    public Double GetRoomPriceByDateList(Integer hotelId, Integer roomId, LocalDateTime checkInDate,
                                         LocalDateTime checkOutDate, Boolean isIgnoreFavourable) {
        Double price = 0.0;

        for (LocalDateTime date = checkInDate; date.isBefore(checkOutDate); date = date.plusDays(1)) {
            price += GetRoomPriceByDate(hotelId, roomId, date, isIgnoreFavourable);
        }
        return price;
    }

    /**
     * 得到房间选择某天的价格
     *
     * @param hotelId
     * @param roomId
     * @return
     */
    public Double GetRoomPriceByDate(Integer hotelId, Integer roomId, LocalDateTime date, Boolean isIgnoreFavourable) {
        Room room = RoomMapper.selectById(roomId);
        // 平常价格
        Double price = Extension.ToFixed0(((room.getMaxPrice() + room.getMinPrice()) / 2));

        // 查询当天的价格
        RoomPrice roomPrice = RoomPriceMapper.selectList(Wrappers.<RoomPrice>lambdaQuery()
                .eq(RoomPrice::getHotelId, hotelId)
                .eq(RoomPrice::getRoomId, roomId)
                .ge(RoomPrice::getBeginActiveTime, date)
                .le(RoomPrice::getEndActiveTime, date)).stream().findFirst().orElse(null);
        if (roomPrice != null) {
            price = roomPrice.getPrice();
        }

        // 查询当天满足的折扣
        FavourableSetting favourableSetting = FavourableSettingMapper
                .selectList(Wrappers.<FavourableSetting>lambdaQuery()
                        .eq(FavourableSetting::getHotelId, hotelId)
                        .ge(FavourableSetting::getBeginActiveTime, date)
                        .le(FavourableSetting::getEndActiveTime, date))
                .stream().findFirst().orElse(null);
        if (favourableSetting != null) {
            price = Extension.ToFixed0(price * (1 - favourableSetting.getDiscount()));
        }

        return price;
    }

    /**
     * 得到房间剩余数量
     */
    public Long GetRemainingRoomNumber(Integer hotelId, Integer roomId, LocalDateTime checkInDate,
                                       LocalDateTime checkOutDate) {

        // 1. 获取该房型的总房间数（从roomdet表）
        Long totalRooms = RoomDetMapper.selectCount(
                Wrappers.<RoomDet>lambdaQuery()
                        .eq(RoomDet::getHotelId, hotelId)
                        .eq(RoomDet::getRoomId, roomId)
        );

        if (totalRooms == 0) {
            return 0L;
        }

        // 2. 查询在指定时间段内被占用的房间数量（从roommatch表）
        // 占用状态：预约中(2)、入住中(3)
        // 时间重叠判断：预订开始时间 < 退房时间 AND 预订结束时间 > 入住时间
        List<RoomMatch> occupiedRooms = RoomMatchMapper.selectList(
                Wrappers.<RoomMatch>lambdaQuery()
                        .eq(RoomMatch::getHotelId, hotelId)
                        .eq(RoomMatch::getRoomId, roomId)
                        .in(RoomMatch::getRoomStatus,
                                RoomStatusEnum.预约中.index(),
                                RoomStatusEnum.入住中.index()) // 只统计预约中和入住中的房间
                        .lt(RoomMatch::getBeginActiveTime, checkOutDate)
                        .gt(RoomMatch::getEndActiveTime, checkInDate)
//                       .select(RoomMatch::getRoomDetId)
        );

        long allocatedCount = occupiedRooms.stream()
                .map(RoomMatch::getRoomDetId)
                .filter(id -> id != null)
                .distinct()
                .count();

        // 预分配：RoomDetId为null的记录数
        long preAllocatedCount = occupiedRooms.stream()
                .filter(rm -> rm.getRoomDetId() == null)
                .count();

        // 总占用 = 已分配 + 预分配
        long totalOccupied = allocatedCount + preAllocatedCount;

        // 3. 计算剩余房间数
        long remaining = totalRooms - totalOccupied;
        return Math.max(remaining, 0L);

//        List<Integer> uniqueRoomDetIds = occupiedRooms.stream()
//                .map(RoomMatch::getRoomDetId)
//                .filter(id -> id != null) // 过滤掉空值
//                .distinct()
//                .collect(Collectors.toList());
//
//        // 3. 计算剩余房间数
//        long remaining = totalRooms - uniqueRoomDetIds.size();
//
//        // 4. 防止负数
//        return Math.max(remaining, 0L);

//        // 总房间数量
//        Long roomDetCount = RoomDetMapper.selectCount(Wrappers.<RoomDet>lambdaQuery()
//                .eq(RoomDet::getHotelId, hotelId)
//                .eq(RoomDet::getRoomId, roomId));
//
//
//        List<Appoint> validAppoints = AppointMapper.selectList(
//                Wrappers.<Appoint>lambdaQuery()
//                        .eq(Appoint::getHotelId, hotelId)
//                        .eq(Appoint::getRoomId, roomId)
//                        .notIn(Appoint::getAppointStatus,
//                                AppointStatusEnum.用户取消.index(),
//                                AppointStatusEnum.酒店取消.index(),
//                                AppointStatusEnum.系统取消.index(),
//                                AppointStatusEnum.完成退房.index(), // 新增：排除已退房订单
//                                AppointStatusEnum.完成.index()) // 新增：排除已完成评价的订单
//                        .ge(Appoint::getBeginAppointTime, checkInDate)
//                        .le(Appoint::getEndAppointTime, checkOutDate)
//                        .select(Appoint::getQty) // 只查询需要的字段，提高性能
//        );
//
//        // 3. 求和（空列表返回0）
//        long totalBookedRooms = validAppoints.stream()
//                .mapToLong(Appoint::getQty)
//                .sum();
//
//        // 4. 计算剩余数量（防止负数）
//        return Math.max(roomDetCount - totalBookedRooms, 0L);
//

    }

    /**
     * 得到某天某个房间的使用情况
     */
    @SneakyThrows
    @Override
    public RoomMatchDto GetRoomMatch(Integer hotelId, Integer roomId, Integer roomDetId, LocalDateTime date) {
        RoomMatch roomMatch = RoomMatchMapper.selectList(Wrappers.<RoomMatch>lambdaQuery()
                        .eq(RoomMatch::getHotelId, hotelId)
                        .eq(RoomMatch::getRoomId, roomId)
                        .eq(RoomMatch::getRoomDetId, roomDetId)
                        .ge(RoomMatch::getBeginActiveTime, date)
                        .le(RoomMatch::getEndActiveTime, date).orderByDesc(RoomMatch::getCreationTime)).stream().findFirst()
                .orElse(null);
        if (roomMatch != null) {
            return roomMatch.MapToDto();
        }
        return null;
    }

    /**
     * 根据地址获取对应的酒店ids
     */
    @SneakyThrows
    @Override
    public List<Integer> GetHotelIdsByAddress(String address) {
        List<Hotel> hotels = HotelMapper.selectList(Wrappers.<Hotel>lambdaQuery()
                .like(Hotel::getProviceCityArea, address));
        return hotels.stream().map(Hotel::getId).collect(Collectors.toList());
    }

    /**
     * 酒店数据综合分析
     */
    @SneakyThrows
    @Override
    public HashMap<String, Object> HotelDataAnalysis(HotelDataAnalysisQueryInput input) {
        HashMap<String, Object> result = new HashMap<>();
        Integer hotelId = input.getHotelId();
        LocalDateTime startDate = input.getStartTime();
        LocalDateTime endDate = input.getEndTime();

        // 查询该时间段内所有预约
        List<Appoint> appoints = AppointMapper.selectList(Wrappers.<Appoint>lambdaQuery()
                .eq(hotelId != null, Appoint::getHotelId, hotelId)
                .ge(startDate != null, Appoint::getBeginAppointTime, startDate)
                .le(endDate != null, Appoint::getEndAppointTime, endDate));

        if (appoints == null || appoints.isEmpty()) {
            result.put("message", "该时间段内无预订数据");
            return result;
        }

        // 1. 预约状态统计
        int totalAppointCount = appoints.size();
        int successAppointCount = 0;
        int cancelByUserCount = 0;
        int cancelByHotelCount = 0;
        int cancelBySystemCount = 0;
        int pendingCount = 0;

        for (int i = 0; i < appoints.size(); i++) {
            Appoint appoint = appoints.get(i);
            Integer status = appoint.getAppointStatus();

            if (status == null)
                continue;

            if (status.equals(AppointStatusEnum.待入住.index())) {
                successAppointCount++;
            } else if (status.equals(AppointStatusEnum.用户取消.index())) {
                cancelByUserCount++;
            } else if (status.equals(AppointStatusEnum.酒店取消.index())) {
                cancelByHotelCount++;
            } else if (status.equals(AppointStatusEnum.系统取消.index())) {
                cancelBySystemCount++;
            } else if (status.equals(AppointStatusEnum.待确定.index())) {
                pendingCount++;
            }
        }

        HashMap<String, Object> statusStats = new HashMap<>();
        statusStats.put("总预约数", totalAppointCount);
        statusStats.put("预约成功数", successAppointCount);
        statusStats.put("用户取消数", cancelByUserCount);
        statusStats.put("酒店取消数", cancelByHotelCount);
        statusStats.put("系统取消数", cancelBySystemCount);
        statusStats.put("待确认数", pendingCount);
        statusStats.put("成功率", totalAppointCount > 0 ? (double) successAppointCount / totalAppointCount : 0);
        statusStats.put("用户取消率", totalAppointCount > 0 ? (double) cancelByUserCount / totalAppointCount : 0);
        result.put("预约状态统计", statusStats);

        // 2. 按季节统计预约量和收入
        HashMap<String, Integer> seasonAppointCount = new HashMap<>();
        HashMap<String, Double> seasonRevenue = new HashMap<>();
        seasonAppointCount.put("春季", 0);
        seasonAppointCount.put("夏季", 0);
        seasonAppointCount.put("秋季", 0);
        seasonAppointCount.put("冬季", 0);
        seasonRevenue.put("春季", 0.0);
        seasonRevenue.put("夏季", 0.0);
        seasonRevenue.put("秋季", 0.0);
        seasonRevenue.put("冬季", 0.0);

        double totalRevenue = 0.0;

        for (int i = 0; i < appoints.size(); i++) {
            Appoint appoint = appoints.get(i);

            if (appoint.getBeginAppointTime() == null)
                continue;

            int month = appoint.getBeginAppointTime().getMonthValue();
            String season;

            if (month >= 3 && month <= 5) {
                season = "春季";
            } else if (month >= 6 && month <= 8) {
                season = "夏季";
            } else if (month >= 9 && month <= 11) {
                season = "秋季";
            } else {
                season = "冬季";
            }

            seasonAppointCount.put(season, seasonAppointCount.get(season) + 1);

            Double price = appoint.getTotalMoney();
            if (price != null) {
                seasonRevenue.put(season, seasonRevenue.get(season) + price);
                totalRevenue += price;
            }
        }

        result.put("季节预约统计", seasonAppointCount);
        result.put("季节收入统计", seasonRevenue);
        result.put("总收入", totalRevenue);

        // 3. 房间类型受欢迎度分析
        if (hotelId != null) {
            List<Room> rooms = RoomMapper.selectList(Wrappers.<Room>lambdaQuery()
                    .eq(Room::getHotelId, hotelId));

            if (rooms != null && !rooms.isEmpty()) {
                HashMap<Integer, String> roomIdToName = new HashMap<>();
                HashMap<String, Integer> roomTypeBookingCount = new HashMap<>();
                HashMap<String, Double> roomTypeRevenue = new HashMap<>();

                for (Room room : rooms) {
                    if (room.getId() != null && room.getTitle() != null) {
                        roomIdToName.put(room.getId(), room.getTitle());
                        roomTypeBookingCount.put(room.getTitle(), 0);
                        roomTypeRevenue.put(room.getTitle(), 0.0);
                    }
                }

                for (int i = 0; i < appoints.size(); i++) {
                    Appoint appoint = appoints.get(i);
                    Integer roomId = appoint.getRoomId();

                    if (roomId != null && roomIdToName.containsKey(roomId)) {
                        String roomName = roomIdToName.get(roomId);
                        roomTypeBookingCount.put(roomName, roomTypeBookingCount.get(roomName) + 1);

                        Double price = appoint.getTotalMoney();
                        if (price != null) {
                            roomTypeRevenue.put(roomName, roomTypeRevenue.get(roomName) + price);
                        }
                    }
                }

                result.put("房型预约统计", roomTypeBookingCount);
                result.put("房型收入统计", roomTypeRevenue);
            }
        }

        // 4. 入住时长分析
        int oneDayStay = 0;
        int twoDayStay = 0;
        int threeDayStay = 0;
        int fourToSevenDayStay = 0;
        int moreThanSevenDayStay = 0;

        for (int i = 0; i < appoints.size(); i++) {
            Appoint appoint = appoints.get(i);

            if (appoint.getBeginAppointTime() != null && appoint.getEndAppointTime() != null) {
                long days = java.time.Duration.between(
                        appoint.getBeginAppointTime(),
                        appoint.getEndAppointTime()).toDays();

                if (days == 1) {
                    oneDayStay++;
                } else if (days == 2) {
                    twoDayStay++;
                } else if (days == 3) {
                    threeDayStay++;
                } else if (days >= 4 && days <= 7) {
                    fourToSevenDayStay++;
                } else if (days > 7) {
                    moreThanSevenDayStay++;
                }
            }
        }

        HashMap<String, Integer> stayDurationStats = new HashMap<>();
        stayDurationStats.put("入住1天", oneDayStay);
        stayDurationStats.put("入住2天", twoDayStay);
        stayDurationStats.put("入住3天", threeDayStay);
        stayDurationStats.put("入住4-7天", fourToSevenDayStay);
        stayDurationStats.put("入住超过7天", moreThanSevenDayStay);
        result.put("入住时长统计", stayDurationStats);

        // 5. 周内/周末预订分析
        int weekdayBookings = 0;
        int weekendBookings = 0;
        double weekdayRevenue = 0.0;
        double weekendRevenue = 0.0;

        for (int i = 0; i < appoints.size(); i++) {
            Appoint appoint = appoints.get(i);

            if (appoint.getBeginAppointTime() != null) {
                int dayOfWeek = appoint.getBeginAppointTime().getDayOfWeek().getValue();
                boolean isWeekend = (dayOfWeek == 6 || dayOfWeek == 7);

                if (isWeekend) {
                    weekendBookings++;
                    if (appoint.getTotalMoney() != null) {
                        weekendRevenue += appoint.getTotalMoney();
                    }
                } else {
                    weekdayBookings++;
                    if (appoint.getTotalMoney() != null) {
                        weekdayRevenue += appoint.getTotalMoney();
                    }
                }
            }
        }

        HashMap<String, Object> weekdayWeekendStats = new HashMap<>();
        weekdayWeekendStats.put("工作日预订数", weekdayBookings);
        weekdayWeekendStats.put("周末预订数", weekendBookings);
        weekdayWeekendStats.put("工作日收入", weekdayRevenue);
        weekdayWeekendStats.put("周末收入", weekendRevenue);
        weekdayWeekendStats.put("周末预订比例", totalAppointCount > 0 ? (double) weekendBookings / totalAppointCount : 0);
        result.put("工作日/周末统计", weekdayWeekendStats);

        // 6. 提前预订天数分析
        int sameDayBooking = 0;
        int oneDayAdvance = 0;
        int twoDaysAdvance = 0;
        int threeToSevenDaysAdvance = 0;
        int moreThanSevenDaysAdvance = 0;

        for (int i = 0; i < appoints.size(); i++) {
            Appoint appoint = appoints.get(i);

            if (appoint.getCreationTime() != null && appoint.getBeginAppointTime() != null) {
                long daysInAdvance = java.time.Duration.between(
                        appoint.getCreationTime(),
                        appoint.getBeginAppointTime()).toDays();

                if (daysInAdvance == 0) {
                    sameDayBooking++;
                } else if (daysInAdvance == 1) {
                    oneDayAdvance++;
                } else if (daysInAdvance == 2) {
                    twoDaysAdvance++;
                } else if (daysInAdvance >= 3 && daysInAdvance <= 7) {
                    threeToSevenDaysAdvance++;
                } else if (daysInAdvance > 7) {
                    moreThanSevenDaysAdvance++;
                }
            }
        }

        HashMap<String, Integer> advanceBookingStats = new HashMap<>();
        advanceBookingStats.put("当天预订", sameDayBooking);
        advanceBookingStats.put("提前1天", oneDayAdvance);
        advanceBookingStats.put("提前2天", twoDaysAdvance);
        advanceBookingStats.put("提前3-7天", threeToSevenDaysAdvance);
        advanceBookingStats.put("提前超过7天", moreThanSevenDaysAdvance);
        result.put("提前预订天数统计", advanceBookingStats);

        return result;
    }

    /**
     * 酒店运营综合分析统计
     */
    public HashMap<String, Object> SynthesizeStatistics(HotelDataAnalysisQueryInput input) {
        HashMap<String, Object> result = new HashMap<>();

        // 1. 查询全量预约订单
        List<Appoint> allAppoints = AppointMapper.selectList(Wrappers.lambdaQuery());

        // 空数据安全处理（前端专用兜底）
        if (allAppoints == null || allAppoints.isEmpty()) {
            result.put("totalBooking", 0);
            result.put("totalIncome", 0.00);
            result.put("avgStayDays", 0);
            result.put("completionRate", 0.00);
            result.put("bookingStatusStats", new HashMap<>());
            result.put("monthlyBookingCount", new HashMap<>());
            result.put("monthlyIncomeCount", new HashMap<>());
            result.put("timeSlotStats", new HashMap<>());
            result.put("customerStats", new HashMap<String, Integer>(){{
                put("新客户", 0); put("回头客", 0);
            }});
            result.put("advanceBookingStats", new HashMap<String, Integer>(){{
                put("当天预订",0);put("提前1-3天",0);put("提前4-7天",0);
                put("提前8-14天",0);put("提前15-30天",0);put("提前30天以上",0);
            }});
            // 前端必须的返回格式
            HashMap<String, Object> response = new HashMap<>();
            response.put("Success", true);
            response.put("Data", result);
            return response;
        }

        // ===================== 2. 初始化统计变量（严格匹配你的枚举） =====================
        int waitConfirm = 0;    // 待确定 6
        int checkInWait = 0;    // 待入住 1
        int completed = 0;      // 已完成（2已入住+5完成+8完成退房）
        int cancelByUser = 0;   // 用户取消 3
        int cancelByHotel = 0;  // 酒店取消 4
        int cancelBySystem = 0; // 系统取消 10
        // 时段统计
        int morning = 0, afternoon = 0, evening = 0, midnight = 0;
        // 核心指标
        int totalBooking = allAppoints.size();
        double totalIncome = 0.0;
        int validOrder = 0;
        long totalStayDays = 0;
        // 月度统计
        HashMap<Integer, Integer> monthlyBookingCount = new HashMap<>();
        HashMap<Integer, Double> monthlyIncomeCount = new HashMap<>();
        // 提前预订统计
        int advance0 = 0, advance1_3 = 0, advance4_7 = 0, advance8_14 = 0, advance15_30 = 0, advance30Plus = 0;
        // 客户统计
        int oldCustomer = 0;

        // ===================== 3. 核心遍历统计 =====================
        for (int i = 0; i < allAppoints.size(); i++) {
            Appoint appoint = allAppoints.get(i);
            if (appoint == null) continue;

            // 基础数据
            Double orderMoney = appoint.getTotalMoney() == null ? 0.0 : appoint.getTotalMoney();
            totalIncome += orderMoney;
            Integer status = appoint.getAppointStatus();
            LocalDateTime beginTime = appoint.getBeginAppointTime();
            LocalDateTime endTime = appoint.getEndAppointTime();

            // 计算入住天数
            if (beginTime != null && endTime != null) {
                long days = ChronoUnit.DAYS.between(beginTime, endTime);
                totalStayDays += Math.max(days, 0);
            }

            // ===================== 【关键】严格匹配你的枚举状态 =====================
            if (status == null) continue;
            if (status == AppointStatusEnum.待确定.index()) {
                waitConfirm++;
            } else if (status == AppointStatusEnum.待入住.index()) {
                checkInWait++;
                validOrder++;
            } else if (status == AppointStatusEnum.已入住.index()
                    || status == AppointStatusEnum.完成.index()
                    || status == AppointStatusEnum.完成退房.index()) {
                completed++;
                validOrder++;
            } else if (status == AppointStatusEnum.用户取消.index()) {
                cancelByUser++;
            } else if (status == AppointStatusEnum.酒店取消.index()) {
                cancelByHotel++;
            } else if (status == AppointStatusEnum.系统取消.index()) {
                cancelBySystem++;
            }

            // 时段 + 月度统计
            if (beginTime != null) {
                int hour = beginTime.getHour();
                if (hour >= 6 && hour <= 12) morning++;
                else if (hour > 12 && hour <= 18) afternoon++;
                else if (hour > 18 && hour <= 22) evening++;
                else midnight++;

                int month = beginTime.getMonthValue();
                monthlyBookingCount.put(month, monthlyBookingCount.getOrDefault(month, 0) + 1);
                monthlyIncomeCount.put(month, monthlyIncomeCount.getOrDefault(month, 0.0) + orderMoney);

                // 提前预订天数
                long betweenDays = ChronoUnit.DAYS.between(beginTime, LocalDateTime.now());
                if (betweenDays == 0) advance0++;
                else if (betweenDays <= 3) advance1_3++;
                else if (betweenDays <= 7) advance4_7++;
                else if (betweenDays <= 14) advance8_14++;
                else if (betweenDays <= 30) advance15_30++;
                else advance30Plus++;
            }

            // 客户统计
            if (appoint.getToUserId() != null) oldCustomer++;
        }

        // ===================== 4. 计算衍生指标 =====================
        double avgStayDays = validOrder == 0 ? 0 : (double) totalStayDays / validOrder;
        double completionRate = totalBooking == 0 ? 0 : (double) validOrder / totalBooking * 100;

        // ===================== 5. 封装前端100%匹配的字段 =====================
        result.put("totalBooking", totalBooking);
        result.put("totalIncome", String.format("%.2f", totalIncome));
        result.put("avgStayDays", (int) Math.round(avgStayDays));
        result.put("completionRate", String.format("%.2f", completionRate));

        // 订单状态统计（前端专用）
        HashMap<String, Object> bookingStatusStats = new HashMap<>();
        bookingStatusStats.put("待确定", waitConfirm);
        bookingStatusStats.put("待入住", checkInWait);
        bookingStatusStats.put("已完成", completed);
        bookingStatusStats.put("用户取消", cancelByUser);
        bookingStatusStats.put("酒店取消", cancelByHotel);
        bookingStatusStats.put("系统取消", cancelBySystem);
        result.put("bookingStatusStats", bookingStatusStats);

        // 时段分布
        HashMap<String, Integer> timeSlotStats = new HashMap<>();
        timeSlotStats.put("早晨预约(6:00-12:00)", morning);
        timeSlotStats.put("下午预约(12:00-18:00)", afternoon);
        timeSlotStats.put("晚间预约(18:00-22:00)", evening);
        timeSlotStats.put("夜间预约(22:00-6:00)", midnight);
        result.put("timeSlotStats", timeSlotStats);

        // 月度数据
        result.put("monthlyBookingCount", monthlyBookingCount);
        result.put("monthlyIncomeCount", monthlyIncomeCount);

        // 客户分析
        HashMap<String, Integer> customerStats = new HashMap<>();
        customerStats.put("新客户", 0);
        customerStats.put("回头客", oldCustomer);
        result.put("customerStats", customerStats);

        // 提前预订分析
        HashMap<String, Integer> advanceBookingStats = new HashMap<>();
        advanceBookingStats.put("当天预订", advance0);
        advanceBookingStats.put("提前1-3天", advance1_3);
        advanceBookingStats.put("提前4-7天", advance4_7);
        advanceBookingStats.put("提前8-14天", advance8_14);
        advanceBookingStats.put("提前15-30天", advance15_30);
        advanceBookingStats.put("提前30天以上", advance30Plus);
        result.put("advanceBookingStats", advanceBookingStats);

        // 包装成前端需要的格式
        HashMap<String, Object> response = new HashMap<>();
        response.put("Success", true);
        response.put("Data", result);
        return response;
    }

}
