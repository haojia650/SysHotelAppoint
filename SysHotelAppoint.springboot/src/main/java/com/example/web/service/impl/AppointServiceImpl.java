package com.example.web.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.web.dto.*;
import com.example.web.dto.query.*;
import com.example.web.entity.*;
import com.example.web.mapper.*;
import com.example.web.enums.*;
import com.example.web.service.*;
import com.example.web.tools.dto.*;
import com.example.web.tools.exception.CustomException;
import com.example.web.service.MessageNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import lombok.SneakyThrows;
import com.example.web.tools.*;

/**
 * 预约记录功能实现类
 */
@Service
public class AppointServiceImpl extends ServiceImpl<AppointMapper, Appoint> implements AppointService {

	 /**
     * 操作数据库AppUser表mapper对象
     */
    @Autowired
    private AppUserMapper AppUserMapper;
    /**
     * 操作数据库的Appoint表mapper对象
     */
    @Autowired
    private AppointMapper AppointMapper;
    @Autowired
    private RoomDetMapper  RoomDetMapper;                        
    @Autowired
    private RoomMapper  RoomMapper;                        
    @Autowired
    private HotelMapper  HotelMapper;
    @Autowired
    private AppointDetMapper AppointDetMapper;
    @Autowired
    private GuestInfoMapper GuestInfoMapper;
    @Autowired
    private RoomPriceMapper RoomPriceMapper;
    @Autowired
    private FavourableSettingMapper FavourableSettingMapper;
    @Autowired
    private RoomMatchMapper RoomMatchMapper;

    @Autowired
    private HotelIntegralMapper HotelIntegralMapper;

    @Autowired
    private MessageNoticeService MessageNoticeService;

    @Autowired
    private BaseService BaseService;

   /**
     * 构建表查询sql
     */
    private LambdaQueryWrapper<Appoint> BuilderQuery(AppointPagedInput input) {
       //声明一个支持预约记录查询的(拉姆达)表达式
        LambdaQueryWrapper<Appoint> queryWrapper = Wrappers.<Appoint>lambdaQuery()
                .eq(input.getId() != null && input.getId() != 0, Appoint::getId, input.getId());
   //如果前端搜索传入查询条件则拼接查询条件
        if (Extension.isNotNullOrEmpty(input.getNo())) {
             queryWrapper = queryWrapper.like(Appoint::getNo, input.getNo());
       	 }
        if (Extension.isNotNullOrEmpty(input.getComment())) {
             queryWrapper = queryWrapper.like(Appoint::getComment, input.getComment());
       	 }
        if (Extension.isNotNullOrEmpty(input.getPayType())) {
             queryWrapper = queryWrapper.like(Appoint::getPayType, input.getPayType());
       	 }

        if (input.getHotelId() != null) {
            queryWrapper = queryWrapper.eq(Appoint::getHotelId, input.getHotelId());
       	 }

        if (input.getAppointStatus() != null) {
            queryWrapper = queryWrapper.eq(Appoint::getAppointStatus, input.getAppointStatus());
       	 }

        if (input.getRoomId() != null) {
            queryWrapper = queryWrapper.eq(Appoint::getRoomId, input.getRoomId());
       	 }



        if (input.getToUserId() != null) {
            queryWrapper = queryWrapper.eq(Appoint::getToUserId, input.getToUserId());
       	 }
        if (input.getBeginAppointTimeRange() != null && !input.getBeginAppointTimeRange().isEmpty()) {
            queryWrapper = queryWrapper.le(Appoint::getBeginAppointTime, input.getBeginAppointTimeRange().get(1));
            queryWrapper = queryWrapper.ge(Appoint::getBeginAppointTime, input.getBeginAppointTimeRange().get(0));
        }
        if (input.getPayTimeRange() != null && !input.getPayTimeRange().isEmpty()) {
            queryWrapper = queryWrapper.le(Appoint::getPayTime, input.getPayTimeRange().get(1));
            queryWrapper = queryWrapper.ge(Appoint::getPayTime, input.getPayTimeRange().get(0));
        }
        if (input.getEndAppointTimeRange() != null && !input.getEndAppointTimeRange().isEmpty()) {
            queryWrapper = queryWrapper.le(Appoint::getEndAppointTime, input.getEndAppointTimeRange().get(1));
            queryWrapper = queryWrapper.ge(Appoint::getEndAppointTime, input.getEndAppointTimeRange().get(0));
        }
 
     if(Extension.isNotNullOrEmpty(input.getKeyWord()))
        {
			queryWrapper=queryWrapper.and(i->i
          	   .like(Appoint::getNo,input.getKeyWord()).or()   	 
          	   .like(Appoint::getComment,input.getKeyWord()).or()   	 
          	   .like(Appoint::getPayType,input.getKeyWord()).or()   	 
        );
                                       
 		   }
    
      return queryWrapper;
    }
  
    /**
     * 处理预约记录对于的外键数据
     */
   private List<AppointDto> DispatchItem(List<AppointDto> items) throws InvocationTargetException, IllegalAccessException {
          
       for (AppointDto item : items) {           
          	            
           //查询出关联的RoomDet表信息           
//            RoomDet  RoomDetEntity= RoomDetMapper.selectById(item.getRoomDetId());
//            item.setRoomDetDto(RoomDetEntity!=null?RoomDetEntity.MapToDto():new RoomDetDto());
           
          	            
           //查询出关联的AppUser表信息           
            AppUser  ToUserEntity= AppUserMapper.selectById(item.getToUserId());
            item.setToUserDto(ToUserEntity!=null?ToUserEntity.MapToDto():new AppUserDto());              
           
          	            
           //查询出关联的Room表信息           
            Room  RoomEntity= RoomMapper.selectById(item.getRoomId());
            item.setRoomDto(RoomEntity!=null?RoomEntity.MapToDto():new RoomDto());              
           
          	            
           //查询出关联的Hotel表信息           
            Hotel  HotelEntity= HotelMapper.selectById(item.getHotelId());
            item.setHotelDto(HotelEntity!=null?HotelEntity.MapToDto():new HotelDto());

            //查询出预约明细
           List<AppointDet> appointDets = AppointDetMapper
                   .selectList(Wrappers.<AppointDet>lambdaQuery().eq(AppointDet::getAppointId, item.getId()));
           item.setAppointDetDtos(Extension.copyBeanList(appointDets,AppointDetDto.class));

           //查询出房间分配
           List<RoomMatch> roomMatches = RoomMatchMapper
                   .selectList(Wrappers.<RoomMatch>lambdaQuery().eq(RoomMatch::getAppointId, item.getId()));
           item.setRoomMatchDtos(Extension.copyBeanList(roomMatches,RoomMatchDto.class));
           for (RoomMatchDto roomMatchDto : item.getRoomMatchDtos()) {
               RoomDet roomDet = RoomDetMapper.selectById(roomMatchDto.getRoomDetId());
               roomMatchDto.setRoomDetDto(roomDet != null ? roomDet.MapToDto() : new RoomDetDto());
           }

       }
       
     return items; 
   }


  
    /**
     * 预约记录分页查询
     */
    @SneakyThrows
    @Override
    public PagedResult<AppointDto> List(AppointPagedInput input) {
        //构建where条件+排序
        LambdaQueryWrapper<Appoint> queryWrapper = BuilderQuery(input);
        // 动态排序处理
        if (input.getSortItem() != null) {
            // 根据字段名动态排序
            queryWrapper.last("ORDER BY " + input.getSortItem().getFieldName()
                    + (input.getSortItem().getIsAsc() ? " ASC" : " DESC"));
        } else {
            // 默认按创建时间从大到小排序
            queryWrapper = queryWrapper.orderByDesc(Appoint::getCreationTime);
        }

        //构建一个分页查询的model
        Page<Appoint> page = new Page<>(input.getPage(), input.getLimit());
         //从数据库进行分页查询获取预约记录数据
        IPage<Appoint> pageRecords= AppointMapper.selectPage(page, queryWrapper);
        //获取所有满足条件的数据行数
        Long totalCount= AppointMapper.selectCount(queryWrapper);
        //把Appoint实体转换成Appoint传输模型
        List<AppointDto> items= Extension.copyBeanList(pageRecords.getRecords(),AppointDto.class);

		   DispatchItem(items);
        //返回一个分页结构给前端
        return PagedResult.GetInstance(items,totalCount);

    }
  
    /**
     * 单个预约记录查询
     */
    @SneakyThrows
    @Override
    public AppointDto Get(AppointPagedInput input) {
       if(input.getId()==null)
        {
         return new AppointDto();
        }
      
       PagedResult<AppointDto> pagedResult = List(input);
        return pagedResult.getItems().stream().findFirst().orElse(new AppointDto()); 
    }

    /**
     *预约记录创建或者修改
     */
    @SneakyThrows
    @Override
    public AppointDto CreateOrEdit(AppointDto input) {
        //声明一个预约记录实体
        Appoint Appoint=input.MapToEntity();  
        //调用数据库的增加或者修改方法
        saveOrUpdate(Appoint);
        //把传输模型返回给前端
        return Appoint.MapToDto();
    }
    /**
     * 预约记录删除
     */
    @Override
    public void Delete(IdInput input) {
        Appoint entity = AppointMapper.selectById(input.getId());
        AppointMapper.deleteById(entity);

        //删除明细
        AppointDetMapper.delete(Wrappers.<AppointDet>lambdaQuery().eq(AppointDet::getAppointId, input.getId()));

        // 删除房间匹配
        RoomMatchMapper.delete(Wrappers.<RoomMatch>lambdaQuery().eq(RoomMatch::getAppointId, input.getId()));
    }

    /**
     * 预约记录批量删除
     */
    @Override
    public void BatchDelete(IdsInput input) {
        for (Integer id : input.getIds()) {
            IdInput idInput = new IdInput();
            idInput.setId(id);
            Delete(idInput);
        }
    }

    /**
     *用户预订
     */
    @SneakyThrows
    @Override
    public AppointDto ToOrder(AppointDto input) {

// 【锁开始】锁定【酒店ID+房型ID】，不同酒店/房型的下单互不阻塞
        String lockKey = input.getHotelId() + "_" + input.getRoomId();
        synchronized (lockKey.intern()) {

            // ========== 1. 先校验库存（锁内校验，保证原子性） ==========
            Long remainingRoom = BaseService.GetRemainingRoomNumber(
                    input.getHotelId(),
                    input.getRoomId(),
                    input.getBeginAppointTime(),
                    input.getEndAppointTime()
            );
            if (remainingRoom < input.getQty()) {
                throw new CustomException("当前房型剩余房间不足，预订失败");
            }

            // ========== 2. 原有下单逻辑完全不变 ==========
            Room room = RoomMapper.selectById(input.getRoomId());
            input.setNo("D" + Extension.GenerateOrderNumber());
            input.setAppointStatus(AppointStatusEnum.待支付.index());
            // 计算总金额
            double totalMoney = calculateTotalMoney(room, input.getBeginAppointTime(), input.getEndAppointTime(), input.getQty());
            input.setTotalMoney(totalMoney);
            Appoint appoint = input.MapToEntity();
            saveOrUpdate(appoint);

            // 旅客明细插入
            List<GuestInfo> guestInfos = GuestInfoMapper.selectList(Wrappers.<GuestInfo>lambdaQuery().in(GuestInfo::getId, input.getGuestIds()));
            for (GuestInfo guestInfo : guestInfos) {
                AppointDet appointDet = new AppointDet();
                appointDet.setAppointId(appoint.getId());
                appointDet.setAge(Extension.GetAge(guestInfo.getBirth()) + "");
                appointDet.setSex(guestInfo.getSex());
                appointDet.setName(guestInfo.getName());
                appointDet.setPhone(guestInfo.getPhone());
                AppointDetMapper.insert(appointDet);
            }

            // 积分抵扣逻辑
            Boolean useIntegralFlag = input.getUseIntegral();
            if (useIntegralFlag != null && useIntegralFlag) {
                HotelIntegral hotelIntegral = new HotelIntegral();
                hotelIntegral.setUserId(input.getToUserId());
                hotelIntegral.setHotelId(input.getHotelId());
                hotelIntegral.setIntegralValue(-room.getIntegral() * input.getQty());
                hotelIntegral.setType("酒店预订");
                hotelIntegral.setTitle("酒店预订完成积分抵扣");
                hotelIntegral.setRelativeNo(appoint.getNo());
                HotelIntegralMapper.insert(hotelIntegral);
            }

            // 房间预分配逻辑
            for (int i = 0; i < input.getQty(); i++) {
                RoomMatch roomMatch = new RoomMatch();
                roomMatch.setAppointId(appoint.getId());
                roomMatch.setHotelId(input.getHotelId());
                roomMatch.setRoomId(input.getRoomId());
                roomMatch.setRoomStatus(RoomStatusEnum.预约中.index());
                roomMatch.setBeginActiveTime(input.getBeginAppointTime());
                roomMatch.setEndActiveTime(input.getEndAppointTime());
                roomMatch.setRoomDetId(null);
                RoomMatchMapper.insert(roomMatch);
            }

            return appoint.MapToDto();
        }
        // 【锁结束】
    }

    /**
     * 支付
     */
    @SneakyThrows
    @Override
    public AppointDto Payment(AppointDto input){
        Appoint appoint = AppointMapper.selectById(input.getId());
        Room room = RoomMapper.selectById(appoint.getRoomId());
        Hotel hotel = HotelMapper.selectById(appoint.getHotelId());
        AppUser toUser = AppUserMapper.selectById(appoint.getToUserId());

        appoint.setPayType(input.getPayType());
        if (room.getIsImmediatelyConfirm() == true) {
            appoint.setAppointStatus(AppointStatusEnum.待入住.index());
        } else {
            appoint.setAppointStatus(AppointStatusEnum.待确定.index());
            String Content = "单号：" + appoint.getNo() + "\n"
                    + "电话：" + toUser.getPhoneNumber() + "\n"
                    + "房间：" + room.getTitle() + "\n"
                    + "房间数量：" + appoint.getQty()
                    + "使用时间：" + appoint.getBeginAppointTime() + "至" + appoint.getEndAppointTime();
            //发送给酒店管理员邮箱
            MessageNoticeService.AddSendEmailRecord(hotel.getHotelUserId(),"有新的订单需要确认,单号:" + appoint.getNo(), Content,
                    LocalDateTime.now());
        }
        appoint.setPayTime(LocalDateTime.now());
        //调用数据库的增加或者修改方法
        saveOrUpdate(appoint);
        return appoint.MapToDto();
    }

    /**
     *用户预订取消
     */
    @SneakyThrows
    @Override
    public void UserCancel(IdInput input) {

        Appoint appoint = AppointMapper.selectById(input.getId());
        Room room = RoomMapper.selectById(appoint.getRoomId());

        Hotel hotel = HotelMapper.selectById(appoint.getHotelId());
        AppUser toUser = AppUserMapper.selectById(appoint.getToUserId());

        //一个月内只允许同1个酒店取消500次
        List<Appoint> appoints = AppointMapper
                .selectList(Wrappers.<Appoint>lambdaQuery().eq(Appoint::getHotelId, appoint.getHotelId())
                        .eq(Appoint::getAppointStatus, AppointStatusEnum.用户取消.index())
                        .ge(Appoint::getCreationTime, LocalDateTime.now().minusMonths(1)));
        if (appoints.size() >= 500) {
            throw new CustomException("一个月内只允许同酒店取消500次");
        }

        if (appoint.getAppointStatus() == AppointStatusEnum.待支付.index()) {
            // 待支付：直接用户取消
            appoint.setAppointStatus(AppointStatusEnum.用户取消.index());
        } else if (appoint.getAppointStatus() == AppointStatusEnum.待确定.index()) {
            if (room.getIsFreeCancel() == true) {
                // 支持免费取消：直接用户取消
                appoint.setAppointStatus(AppointStatusEnum.用户取消.index());
            } else {
                // 不支持免费取消：改为 申请取消（等待酒店处理）
                appoint.setAppointStatus(AppointStatusEnum.申请取消.index());
            }
        } else if (appoint.getAppointStatus() == AppointStatusEnum.待入住.index()) {
            // 待入住：改为 申请取消
            appoint.setAppointStatus(AppointStatusEnum.申请取消.index());

            String Content = "单号：" + appoint.getNo() + "\n"
                    + "电话：" + toUser.getPhoneNumber() + "\n"
                    + "房间：" + room.getTitle() + "\n"
                    + "房间数量：" + appoint.getQty()
                    + "使用时间：" + appoint.getBeginAppointTime() + "至" + appoint.getEndAppointTime()
                    + "申请了取消,请及时处理";

            //发送给酒店管理员邮箱
            MessageNoticeService.AddSendEmailRecord(hotel.getHotelUserId(),"有新的订单进行申请取消,单号:" + appoint.getNo(),
                    Content,
                    LocalDateTime.now());

        } else {
            throw new CustomException("当前状态不支持取消");
        }

        // ✅ 保留空指针修复：计算退款金额（仅赋值，不修改状态）
        Double totalMoney = appoint.getTotalMoney();
        double realTotal = totalMoney == null ? 0.0 : totalMoney;
        // 不支持免费取消时，提前计算好退款金额，供酒店端参考
        if (!room.getIsFreeCancel()) {
            appoint.setReturnMoney(realTotal * 0.9);
        }

        // 保存修改
        saveOrUpdate(appoint);
        ReturnIntegral(appoint);

        // ========== 新增：删除预分配占位记录 ==========
        RoomMatchMapper.delete(
                Wrappers.<RoomMatch>lambdaQuery()
                        .eq(RoomMatch::getAppointId, appoint.getId())
                        .isNull(RoomMatch::getRoomDetId) // 只删除未分配具体房间的占位记录
                        .eq(RoomMatch::getRoomStatus, RoomStatusEnum.预约中.index())
        );


    }

    /**
     * 积分退还
     */
    private void ReturnIntegral(Appoint input) {
        //查询该订单预约使用的积分数量
        List<HotelIntegral> hotelIntegrals = HotelIntegralMapper
                .selectList(Wrappers.<HotelIntegral>lambdaQuery()
                    .eq(HotelIntegral::getType, "酒店预订")
                        .eq(HotelIntegral::getRelativeNo, input.getNo()));
        if (hotelIntegrals.size() > 0) {
            //求和积分
            double integralValue = hotelIntegrals.stream().mapToDouble(HotelIntegral::getIntegralValue).sum();

            HotelIntegral hotelIntegral = new HotelIntegral();
            hotelIntegral.setUserId(input.getToUserId());
            hotelIntegral.setHotelId(input.getHotelId());
            hotelIntegral.setIntegralValue(Math.abs(integralValue));
            hotelIntegral.setType("酒店预订取消");
            hotelIntegral.setTitle("酒店预订取消完成积分返还");
            hotelIntegral.setRelativeNo(input.getNo());
            HotelIntegralMapper.insert(hotelIntegral);
        }



    }

    /**
     *酒店处理用户预订取消
     */
    @SneakyThrows
    @Override
    public void HotelDowithUserCancel(AppointDto input) {
        Appoint appoint = AppointMapper.selectById(input.getId());

        appoint.setAppointStatus(AppointStatusEnum.酒店取消.index());
        Double totalMoney = appoint.getTotalMoney();
        double realTotal = totalMoney == null ? 0.0 : totalMoney;
        if(realTotal < input.getReturnMoney()){
            throw new CustomException("退款金额不能大于订单金额");
        }
        appoint.setReturnMoney(input.getReturnMoney());
        appoint.setAppointStatus(AppointStatusEnum.用户取消.index());
        // 调用数据库的增加或者修改方法
        saveOrUpdate(appoint);

        ReturnIntegral(appoint);
    }

    /**
     *酒店处理预订确认
     */
    @SneakyThrows
    @Override
    public void HotelConfirm(AppointDto input) {
        Appoint appoint = AppointMapper.selectById(input.getId());
        AppUser toUser = AppUserMapper.selectById(appoint.getToUserId());
        Hotel hotel = HotelMapper.selectById(appoint.getHotelId());
        AppUser hotelUser = AppUserMapper.selectById(hotel.getHotelUserId());
        appoint.setAppointStatus(AppointStatusEnum.待入住.index());
        // 调用数据库的增加或者修改方法
        saveOrUpdate(appoint);

        Room room = RoomMapper.selectById(appoint.getRoomId());

        // 构建通知内容
        String Content = "尊敬的" + toUser.getName() + "，您预约的酒店「" + hotel.getName()
                + "」已于" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                + "确认接单。\n\n预订信息：\n"
                + "• 房型：" + room.getTitle() + "\n"
                + "• 房间数量：" + appoint.getQty() + "\n"
                + "• 入住日期："
                + appoint.getBeginAppointTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                + "\n"
                + "• 退房日期："
                + appoint.getEndAppointTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "\n"
                + "• 订单号：" + appoint.getNo() + "\n\n"
                + "酒店联系电话：" + hotelUser.getPhoneNumber() + "\n"
                + "如需了解更多信息，请登录账户查看详情或直接联系酒店。\n"
                + "祝您入住愉快！";

        // 发送给用户邮箱
        MessageNoticeService.AddSendEmailRecord(toUser.getId(), "您的酒店预订已确认 - " + hotel.getName(),
                Content,
                LocalDateTime.now());
    }

    /**
     * 酒店处理预订取消
     */
    @SneakyThrows
    public void HotelCancel(AppointDto input) {
        Appoint appoint = AppointMapper.selectById(input.getId());
        AppUser toUser = AppUserMapper.selectById(appoint.getToUserId());
        Hotel hotel = HotelMapper.selectById(appoint.getHotelId());
        AppUser hotelUser = AppUserMapper.selectById(hotel.getHotelUserId());
        Room room = RoomMapper.selectById(appoint.getRoomId());

        appoint.setAppointStatus(AppointStatusEnum.酒店取消.index());

        // ✅ 修复1：安全处理订单总金额
        Double totalMoney = appoint.getTotalMoney();
        double realTotal = (totalMoney != null) ? totalMoney : 0.0;

        // ✅ 修复2：安全处理退款金额（关键！这里就是报错的地方）
        Double returnMoneyObj = input.getReturnMoney();
        double realReturnMoney = (returnMoneyObj != null) ? returnMoneyObj : 0.0;

        // 现在比较的是两个基本类型，不会有NPE
        if (realTotal < realReturnMoney) {
            throw new CustomException("退款金额不能大于订单金额");
        }

        // ✅ 修复3：设置退款金额时也确保不为null
        appoint.setReturnMoney(returnMoneyObj != null ? returnMoneyObj : 0.0);
        appoint.setAppointStatus(AppointStatusEnum.酒店取消.index());

        // 调用数据库的增加或者修改方法
        saveOrUpdate(appoint);

        ReturnIntegral(appoint);

        // 构建通知内容
        String Content = "尊敬的" + toUser.getName() + "，您预约的酒店「" + hotel.getName()
                + "」已于" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                + "拒绝入住\n\n预订信息：\n"
                + "• 房型：" + room.getTitle() + "\n"
                + "• 房间数量：" + appoint.getQty() + "\n"
                + "• 入住日期："
                + appoint.getBeginAppointTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                + "\n"
                + "• 退房日期："
                + appoint.getEndAppointTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "\n"
                + "• 订单号：" + appoint.getNo() + "\n\n"
                + "酒店联系电话：" + hotelUser.getPhoneNumber() + "\n"
                + "如需了解更多信息，请登录账户查看详情或直接联系酒店。\n"
                + "祝您入住愉快！";

        // 发送给用户邮箱
        MessageNoticeService.AddSendEmailRecord(toUser.getId(), "您的酒店预订已确认 - " + hotel.getName(),
                Content,
                LocalDateTime.now());
    }

    /**
     * 自动取消未付款的订单
     */
    @SneakyThrows
    @Override
    public void AutoCancel() {

        List<Appoint> appoints = AppointMapper
                .selectList(Wrappers.<Appoint>lambdaQuery().eq(Appoint::getAppointStatus, AppointStatusEnum.待支付.index())
                        .le(Appoint::getCreationTime, LocalDateTime.now().minusMinutes(15)));
        for (Appoint appoint : appoints) {
            appoint.setAppointStatus(AppointStatusEnum.系统取消.index());
            // 调用数据库的增加或者修改方法
            saveOrUpdate(appoint);
            ReturnIntegral(appoint);

            // ========== 新增：删除预分配占位记录 ==========
            RoomMatchMapper.delete(
                    Wrappers.<RoomMatch>lambdaQuery()
                            .eq(RoomMatch::getAppointId, appoint.getId())
                            .isNull(RoomMatch::getRoomDetId)
                            .eq(RoomMatch::getRoomStatus, RoomStatusEnum.预约中.index())
            );
        }
    }

    /**
     * 获取指定房间在某一天的实际价格（优先取价格表，否则取房间默认 MinPrice）
     */
    private double getPriceForDate(Integer roomId, LocalDate date) {
        RoomPrice price = RoomPriceMapper.selectOne(
                Wrappers.<RoomPrice>lambdaQuery()
                        .eq(RoomPrice::getRoomId, roomId)
                        .le(RoomPrice::getBeginActiveTime, date.atStartOfDay())
                        .ge(RoomPrice::getEndActiveTime, date.atStartOfDay())
                        .last("limit 1")
        );
        if (price != null) {
            return price.getPrice();
        }
        Room room = RoomMapper.selectById(roomId);
        return room.getMinPrice();
    }

    /**
     * 获取指定房间在某一天的折扣（0~1，无优惠则返回 1.0）
     */
    private double getDiscountForDate(Integer roomId, LocalDate date) {
        FavourableSetting setting = FavourableSettingMapper.selectOne(
                Wrappers.<FavourableSetting>lambdaQuery()
                        .eq(FavourableSetting::getRoomId, roomId)
                        .le(FavourableSetting::getBeginActiveTime, date.atStartOfDay())
                        .ge(FavourableSetting::getEndActiveTime, date.atStartOfDay())
                        .last("limit 1")
        );
        return setting != null ? setting.getDiscount() : 1.0;
    }

    /**
     * 计算订单总金额（按天累加价格和折扣）
     * @param room 房间实体（用于取默认价格）
     * @param beginTime 入住时间
     * @param endTime 离店时间
     * @param qty 房间数量
     * @return 总金额
     */
    private double calculateTotalMoney(Room room, LocalDateTime beginTime, LocalDateTime endTime, int qty) {
        LocalDate startDate = beginTime.toLocalDate();
        LocalDate endDate = endTime.toLocalDate();
        long days = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        days = Math.max(days, 1); // 至少一天

        double total = 0.0;
        for (int i = 0; i < days; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            double price = getPriceForDate(room.getId(), currentDate);
            double discount = getDiscountForDate(room.getId(), currentDate);
            total += price * discount;
        }
        return total * qty;
    }

    /**
     * 退房
     */
    @SneakyThrows
    @Override
    public void CheckOut(AppointDto input) {
        Appoint appoint = AppointMapper.selectById(input.getId());
        appoint.setAppointStatus(AppointStatusEnum.完成退房.index());
        //调用数据库的增加或者修改方法
        saveOrUpdate(appoint);

        //查询出房间匹配
        List<RoomMatch> roomMatches = RoomMatchMapper
                .selectList(Wrappers.<RoomMatch>lambdaQuery().eq(RoomMatch::getAppointId, appoint.getId()));
        for (RoomMatch roomMatch : roomMatches) {
            roomMatch.setRoomStatus(RoomStatusEnum.已退房.index());
            RoomMatchMapper.updateById(roomMatch);
        }
        //赠送积分
        HotelIntegral hotelIntegral = new HotelIntegral();
        hotelIntegral.setUserId(appoint.getToUserId());
        hotelIntegral.setHotelId(appoint.getHotelId());
        hotelIntegral.setIntegralValue(Extension.ToFixed0(appoint.getTotalMoney()));
        if (hotelIntegral.getIntegralValue() < 10) {
            hotelIntegral.setIntegralValue(10.0);
        }
        hotelIntegral.setType("酒店退房完成");
        hotelIntegral.setTitle("酒店退房完成赠送积分");
        hotelIntegral.setRelativeNo(appoint.getNo());
        HotelIntegralMapper.insert(hotelIntegral);
    }

    /**
     * 评价
     */
    @SneakyThrows
    @Override
    public void Comment(AppointDto input) {
        Appoint appoint = AppointMapper.selectById(input.getId());
        appoint.setAppointStatus(AppointStatusEnum.完成.index());
        appoint.setComment(input.getComment());
        appoint.setCommentScore(input.getCommentScore());
        //调用数据库的增加或者修改方法
        saveOrUpdate(appoint);

        //如果评价是好评 赠送10积分 大于3则是好评
        if (input.getCommentScore() > 3) {
            HotelIntegral hotelIntegral = new HotelIntegral();
            hotelIntegral.setUserId(appoint.getToUserId());
            hotelIntegral.setHotelId(appoint.getHotelId());
            hotelIntegral.setIntegralValue(10.0);
            hotelIntegral.setType("酒店评价完成");
            hotelIntegral.setTitle("酒店评价完成赠送积分");
        }
    }

    /**
     * 酒店评价
     */
    @SneakyThrows
    @Override
    public PagedResult<AppointDto> CommentList(AppointPagedInput input) {
        //构建where条件+排序
        LambdaQueryWrapper<Appoint> queryWrapper = BuilderQuery(input);
        // 动态排序处理
        if (input.getSortItem() != null) {
            // 根据字段名动态排序
            queryWrapper.last("ORDER BY " + input.getSortItem().getFieldName()
                    + (input.getSortItem().getIsAsc() ? " ASC" : " DESC"));
        } else {
            // 默认按创建时间从大到小排序
            queryWrapper = queryWrapper.orderByDesc(Appoint::getCreationTime);
        }

        //构建一个分页查询的model
        Page<Appoint> page = new Page<>(input.getPage(), input.getLimit());
        //从数据库进行分页查询获取预约记录数据
        IPage<Appoint> pageRecords= AppointMapper.selectPage(page, queryWrapper);
        //获取所有满足条件的数据行数
        Long totalCount= AppointMapper.selectCount(queryWrapper);
        //把Appoint实体转换成Appoint传输模型
        List<AppointDto> items= Extension.copyBeanList(pageRecords.getRecords(),AppointDto.class);

        for (AppointDto item : items) {
            //查询出关联的AppUser表信息
            AppUser  ToUserEntity= AppUserMapper.selectById(item.getToUserId());
            item.setToUserDto(ToUserEntity!=null?ToUserEntity.MapToDto():new AppUserDto());


            //查询出关联的Room表信息
            Room  RoomEntity= RoomMapper.selectById(item.getRoomId());
            item.setRoomDto(RoomEntity!=null?RoomEntity.MapToDto():new RoomDto());


            //查询出关联的Hotel表信息
            Hotel  HotelEntity= HotelMapper.selectById(item.getHotelId());
            item.setHotelDto(HotelEntity!=null?HotelEntity.MapToDto():new HotelDto());
        }

        //返回一个分页结构给前端
        return PagedResult.GetInstance(items,totalCount);

    }

}
