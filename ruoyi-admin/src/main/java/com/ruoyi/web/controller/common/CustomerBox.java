package com.ruoyi.web.controller.common;

import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.system.domain.Customer;
import com.ruoyi.system.domain.SysUserRole;
import com.ruoyi.system.mapper.SysUserRoleMapper;
import com.ruoyi.system.service.ICustomerService;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.system.websocket.handler.CustomerWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@EnableScheduling
public class CustomerBox {

    @Autowired
    private ICustomerService customerService;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private CustomerWebSocketHandler customerWebSocketHandler;

    // 用于记录当前轮询到的业务员索引
    private AtomicInteger currentIndex = new AtomicInteger(0);

    /**
     * 每5分钟执行一次客户分配
     */
    @Scheduled(fixedRate = 60000)  // 300000毫秒 = 5分钟
    public void assignCustomers() {
        // 1. 获取所有未分配的客户（userId为空的记录）
        Customer queryCustomer = new Customer();
        queryCustomer.setAssignType("0");
        List<Customer> unassignedCustomers = customerService.selectCustomerList(queryCustomer);
        
        if (unassignedCustomers.isEmpty()) {
            return;
        }

        // 2. 获取所有角色ID为101的业务员
        List<SysUserRole> sysUserRoles = sysUserRoleMapper.selectUserRoleByRoleId(101L);
        List<SysUser> salesReps = new ArrayList<>();
        for (SysUserRole sysUserRole : sysUserRoles) {
            salesReps.add(userService.selectUserById(sysUserRole.getUserId()));
        }

        if (salesReps.isEmpty()) {
            return;
        }

        // 3. 轮询分配客户给业务员
        for (Customer customer : unassignedCustomers) {
            // 获取当前应该分配的业务员索引
            int index = currentIndex.getAndUpdate(current -> (current + 1) % salesReps.size());
            
            // 分配客户给业务员
            SysUser salesRep = salesReps.get(index);
            customer.setUserId(salesRep.getUserId());
            customer.setStauts("0"); // 设置状态为未处理
            
            // 更新客户信息
            customerService.updateCustomer(customer);

            // 发送WebSocket通知给业务员
            JSONObject notification = new JSONObject();
            notification.put("type", "newCustomer");
            notification.put("message", "您有新的客户分配");
            notification.put("customerTel", customer.getCustomerTel());
            notification.put("salesRepId", salesRep.getUserId());
            
            // 只发送给被分配的业务员
            customerWebSocketHandler.sendMessageToUser(salesRep.getUserId().toString(), notification.toString());
        }
    }
}
