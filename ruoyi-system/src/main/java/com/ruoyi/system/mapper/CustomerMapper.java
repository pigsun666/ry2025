package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.Customer;

/**
 * 【请填写功能名称】Mapper接口
 * 
 * @author ruoyi
 * @date 2025-04-09
 */
public interface CustomerMapper 
{
    /**
     * 查询【请填写功能名称】
     * 
     * @param customerId 【请填写功能名称】主键
     * @return 【请填写功能名称】
     */
    public Customer selectCustomerByCustomerId(Long customerId);

    /**
     * 查询【请填写功能名称】列表
     * 
     * @param customer 【请填写功能名称】
     * @return 【请填写功能名称】集合
     */
    public List<Customer> selectCustomerList(Customer customer);

    /**
     * 新增【请填写功能名称】
     * 
     * @param customer 【请填写功能名称】
     * @return 结果
     */
    public int insertCustomer(Customer customer);

    /**
     * 修改【请填写功能名称】
     * 
     * @param customer 【请填写功能名称】
     * @return 结果
     */
    public int updateCustomer(Customer customer);

    /**
     * 删除【请填写功能名称】
     * 
     * @param customerId 【请填写功能名称】主键
     * @return 结果
     */
    public int deleteCustomerByCustomerId(Long customerId);

    /**
     * 批量删除【请填写功能名称】
     * 
     * @param customerIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCustomerByCustomerIds(String[] customerIds);
}
