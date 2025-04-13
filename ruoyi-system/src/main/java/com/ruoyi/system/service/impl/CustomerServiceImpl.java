package com.ruoyi.system.service.impl;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.uuid.UUID;
import com.ruoyi.system.websocket.handler.CustomerWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.CustomerMapper;
import com.ruoyi.system.domain.Customer;
import com.ruoyi.system.service.ICustomerService;
import com.ruoyi.common.core.text.Convert;

/**
 * 【请填写功能名称】Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-04-09
 */
@Service
public class CustomerServiceImpl implements ICustomerService 
{
    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private CustomerWebSocketHandler customerWebSocketHandler;

    /**
     * 查询【请填写功能名称】
     * 
     * @param customerId 【请填写功能名称】主键
     * @return 【请填写功能名称】
     */
    @Override
    public Customer selectCustomerByCustomerId(Long customerId)
    {
        return customerMapper.selectCustomerByCustomerId(customerId);
    }

    /**
     * 查询【请填写功能名称】列表
     * 
     * @param customer 【请填写功能名称】
     * @return 【请填写功能名称】
     */
    @Override
    public List<Customer> selectCustomerList(Customer customer)
    {
        return customerMapper.selectCustomerList(customer);
    }

    /**
     * 新增【请填写功能名称】
     * 
     * @param customer 【请填写功能名称】
     * @return 结果
     */
    @Override
    public int insertCustomer(Customer customer)
    {
        customer.setCreateTime(DateUtils.getNowDate());
        customer.setStauts("0");
        //如果没有选择指派人，则随机指派一个业务员或者轮询指派
        if(customer.getUserId() == null){
            customer.setAssignType("0");
        }else {
            // 发送WebSocket通知给业务员
            JSONObject notification = new JSONObject();
            notification.put("type", "newCustomer");
            notification.put("message", "您有新的客户分配");
            notification.put("customerTel", customer.getCustomerTel());
            notification.put("salesRepId", customer.getUserId());

            // 只发送给被分配的业务员
            customerWebSocketHandler.sendMessageToUser(customer.getUserId().toString(), notification.toString());
            customer.setAssignType("1");
            customer.setAssignTime(DateUtils.getNowDate());
        }
        return customerMapper.insertCustomer(customer);
    }

    /**
     * 修改【请填写功能名称】
     * 
     * @param customer 【请填写功能名称】
     * @return 结果
     */
    @Override
    public int updateCustomer(Customer customer)
    {
        if(customer.getUserId() == null){
            customer.setAssignType("0");
        }else {
            // 发送WebSocket通知给业务员
            JSONObject notification = new JSONObject();
            notification.put("type", "newCustomer");
            notification.put("message", "您有新的客户分配");
            notification.put("customerTel", customer.getCustomerTel());
            notification.put("salesRepId", customer.getUserId());

            // 只发送给被分配的业务员
            customerWebSocketHandler.sendMessageToUser(customer.getUserId().toString(), notification.toString());
            customer.setAssignType("1");
            customer.setAssignTime(DateUtils.getNowDate());
            customer.setCreateTime(DateUtils.getNowDate());
        }
        return customerMapper.updateCustomer(customer);
    }

    /**
     * 批量删除【请填写功能名称】
     * 
     * @param customerIds 需要删除的【请填写功能名称】主键
     * @return 结果
     */
    @Override
    public int deleteCustomerByCustomerIds(String customerIds)
    {
        return customerMapper.deleteCustomerByCustomerIds(Convert.toStrArray(customerIds));
    }

    /**
     * 删除【请填写功能名称】信息
     * 
     * @param customerId 【请填写功能名称】主键
     * @return 结果
     */
    @Override
    public int deleteCustomerByCustomerId(Long customerId)
    {
        return customerMapper.deleteCustomerByCustomerId(customerId);
    }
}
