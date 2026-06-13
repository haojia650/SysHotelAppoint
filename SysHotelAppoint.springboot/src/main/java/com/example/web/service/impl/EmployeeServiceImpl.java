package com.example.web.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.web.SysConst;
import com.example.web.dto.*;
import com.example.web.dto.query.*;
import com.example.web.entity.*;
import com.example.web.mapper.*;
import com.example.web.enums.*;
import com.example.web.service.*;
import com.example.web.tools.dto.*;
import com.example.web.tools.exception.CustomException;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.lang.reflect.InvocationTargetException;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import lombok.SneakyThrows;
import java.io.IOException;
import com.example.web.tools.*;
import java.text.DecimalFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.springframework.web.bind.annotation.RequestParam;
/**
 * 员工功能实现类
 */
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

	 /**
     * 操作数据库AppUser表mapper对象
     */
    @Autowired
    private AppUserMapper AppUserMapper;
    /**
     * 操作数据库的Employee表mapper对象
     */
    @Autowired
    private EmployeeMapper EmployeeMapper;
    @Autowired
    private HotelMapper  HotelMapper;                        

  
   /**
     * 构建表查询sql
     */
    private LambdaQueryWrapper<Employee> BuilderQuery(EmployeePagedInput input) {
       //声明一个支持员工查询的(拉姆达)表达式
        LambdaQueryWrapper<Employee> queryWrapper = Wrappers.<Employee>lambdaQuery()
                .eq(input.getId() != null && input.getId() != 0, Employee::getId, input.getId());
   //如果前端搜索传入查询条件则拼接查询条件
        if (Extension.isNotNullOrEmpty(input.getAddress())) {
             queryWrapper = queryWrapper.like(Employee::getAddress, input.getAddress());
       	 }
        if (Extension.isNotNullOrEmpty(input.getName())) {
             queryWrapper = queryWrapper.like(Employee::getName, input.getName());
       	 }
        if (Extension.isNotNullOrEmpty(input.getSalary())) {
             queryWrapper = queryWrapper.like(Employee::getSalary, input.getSalary());
       	 }
        if (Extension.isNotNullOrEmpty(input.getNo())) {
             queryWrapper = queryWrapper.like(Employee::getNo, input.getNo());
       	 }
        if (Extension.isNotNullOrEmpty(input.getSex())) {
             queryWrapper = queryWrapper.like(Employee::getSex, input.getSex());
       	 }
        if (Extension.isNotNullOrEmpty(input.getIdCard())) {
             queryWrapper = queryWrapper.like(Employee::getIdCard, input.getIdCard());
       	 }
        if (Extension.isNotNullOrEmpty(input.getPhone())) {
             queryWrapper = queryWrapper.like(Employee::getPhone, input.getPhone());
       	 }

        if (input.getHotelId() != null) {
            queryWrapper = queryWrapper.eq(Employee::getHotelId, input.getHotelId());
       	 }

        if (input.getUserId() != null) {
            queryWrapper = queryWrapper.eq(Employee::getUserId, input.getUserId());
       	 }
      

 
 
     if(Extension.isNotNullOrEmpty(input.getKeyWord()))
        {
			queryWrapper=queryWrapper.and(i->i
          	   .like(Employee::getAddress,input.getKeyWord()).or()   	 
          	   .like(Employee::getName,input.getKeyWord()).or()   	 
          	   .like(Employee::getSalary,input.getKeyWord()).or()   	 
          	   .like(Employee::getNo,input.getKeyWord()).or()   	 
          	   .like(Employee::getSex,input.getKeyWord()).or()   	 
          	   .like(Employee::getIdCard,input.getKeyWord()).or()   	 
          	   .like(Employee::getPhone,input.getKeyWord()).or()   	 
        );
                                       
 		   }
    
      return queryWrapper;
    }
  
    /**
     * 处理员工对于的外键数据
     */
   private List<EmployeeDto> DispatchItem(List<EmployeeDto> items) throws InvocationTargetException, IllegalAccessException {
          
       for (EmployeeDto item : items) {           
          	            
           //查询出关联的AppUser表信息           
            AppUser  UserEntity= AppUserMapper.selectById(item.getUserId());
            item.setUserDto(UserEntity!=null?UserEntity.MapToDto():new AppUserDto());              
           
          	            
           //查询出关联的Hotel表信息           
            Hotel  HotelEntity= HotelMapper.selectById(item.getHotelId());
            item.setHotelDto(HotelEntity!=null?HotelEntity.MapToDto():new HotelDto());              
       }
       
     return items; 
   }
  
    /**
     * 员工分页查询
     */
    @SneakyThrows
    @Override
    public PagedResult<EmployeeDto> List(EmployeePagedInput input) {
			//构建where条件+排序
        LambdaQueryWrapper<Employee> queryWrapper = BuilderQuery(input);
        // 动态排序处理
        if (input.getSortItem() != null) {
            // 根据字段名动态排序
            queryWrapper.last("ORDER BY " + input.getSortItem().getFieldName()
                    + (input.getSortItem().getIsAsc() ? " ASC" : " DESC"));
        } else {
            // 默认按创建时间从大到小排序
            queryWrapper = queryWrapper.orderByDesc(Employee::getCreationTime);
        }

        //构建一个分页查询的model
        Page<Employee> page = new Page<>(input.getPage(), input.getLimit());
         //从数据库进行分页查询获取员工数据
        IPage<Employee> pageRecords= EmployeeMapper.selectPage(page, queryWrapper);
        //获取所有满足条件的数据行数
        Long totalCount= EmployeeMapper.selectCount(queryWrapper);
        //把Employee实体转换成Employee传输模型
        List<EmployeeDto> items= Extension.copyBeanList(pageRecords.getRecords(),EmployeeDto.class);

		   DispatchItem(items);
        //返回一个分页结构给前端
        return PagedResult.GetInstance(items,totalCount);

    }
  
    /**
     * 单个员工查询
     */
    @SneakyThrows
    @Override
    public EmployeeDto Get(EmployeePagedInput input) {
       if(input.getId()==null)
        {
         return new EmployeeDto();
        }
      
       PagedResult<EmployeeDto> pagedResult = List(input);
        return pagedResult.getItems().stream().findFirst().orElse(new EmployeeDto()); 
    }

    /**
     * 生成员工编号：EMP+酒店ID+4位递增序号（0001起步）
     * 修复：精准查询 + 强制递增
     */
    private String GetEmployeeNo(Integer hotelId) {
        // 1. 查询当前酒店下所有非空工号，按工号倒序取最大的一条（兼容新旧所有格式）
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Employee::getHotelId, hotelId)
                .isNotNull(Employee::getNo)
                .orderByDesc(Employee::getNo)
                .last("LIMIT 1");

        Employee maxEmployee = this.getOne(wrapper, false);
        int sequence = 1; // 默认初始序号

        if (maxEmployee != null && !Extension.isNullOrEmpty(maxEmployee.getNo())) {
            String maxNo = maxEmployee.getNo();
            try {
                // 核心修复1：提取工号中的所有纯数字，彻底过滤字母前缀
                String allNumber = maxNo.replaceAll("[^0-9]", "");
                if (!Extension.isNullOrEmpty(allNumber)) {
                    // 核心修复2：取最后4位作为序号（严格遵循4位递增规则）
                    String sequenceStr = allNumber.length() >= 4
                            ? allNumber.substring(allNumber.length() - 4)
                            : allNumber;
                    // 序号+1
                    sequence = Integer.parseInt(sequenceStr) + 1;
                }
            } catch (Exception e) {
                // 异常兜底：解析失败（如格式错误），默认从1开始，绝不报错
                sequence = 1;
            }
        }

        // 生成标准格式：EMP + 酒店ID + 4位补零序号（如酒店ID=1 → EMP10001、EMP10002）
        return String.format("EMP%d%04d", hotelId, sequence);
    }

    /**
     *员工创建或者修改
     */
    @SneakyThrows
    @Override
    public EmployeeDto CreateOrEdit(EmployeeDto input) {
        // 仅新增员工时自动生成工号，编辑时保留原工号
        if (input.getId() == null) {
            if (input.getHotelId() == null) {
                throw new RuntimeException("酒店ID不能为空，无法生成工号");
            }
            String employeeNo = GetEmployeeNo(input.getHotelId());
            input.setNo(employeeNo);
        }

        Employee employee = input.MapToEntity();
        this.saveOrUpdate(employee);
        return employee.MapToDto();

    }
    /**
     * 员工删除
     */
    @Override
    public void Delete(IdInput input) {
        Employee entity = EmployeeMapper.selectById(input.getId());
        EmployeeMapper.deleteById(entity);
    }

    /**
     * 员工批量删除
     */
    @Override
    public void BatchDelete(IdsInput input) {
        for (Integer id : input.getIds()) {
            IdInput idInput = new IdInput();
            idInput.setId(id);
            Delete(idInput);
        }
    }
}
