package com.example.web.dto;

import com.example.web.entity.RoomDet;
import com.example.web.enums.RoleTypeEnum;
import com.example.web.enums.RoomStatusEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 房间明细视图
 */
@Data
public class RoomDetViewDto {

    @JsonProperty("RoomDetId")
    private Integer RoomDetId;

    /**
     * 酒店
     */
    @JsonProperty("HotelId")
    private Integer HotelId;


    /**
     * 楼层
     */
    @JsonProperty("Floor")
    private String Floor;


    /**
     * 房号
     */
    @JsonProperty("No")
    private String No;

    @JsonProperty("RoomStatus")
    private Integer RoomStatus;

    @JsonProperty("RoomStatusFormat")
    public String RoomStatusFormat() {
        return RoomStatusEnum.GetEnum(RoomStatus).toString();
    };

    /**
     * 对应预约的编号
     */
    @JsonProperty("AppointId")
    private String AppointId;

    /**
     * 房间
     */
    @JsonProperty("RoomId")
    private Integer RoomId;

    @JsonProperty("HotelDto")
    private HotelDto HotelDto;

    @JsonProperty("RoomDto")
    private RoomDto RoomDto;

    /**
     * 把房间明细传输模型转换成房间明细实体
     */
    public RoomDet MapToEntity() throws InvocationTargetException, IllegalAccessException {
        RoomDet RoomDet= new RoomDet();

        BeanUtils.copyProperties(RoomDet,this);

        return RoomDet;
    }

}
