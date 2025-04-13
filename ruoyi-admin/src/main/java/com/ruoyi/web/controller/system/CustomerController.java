package com.ruoyi.web.controller.system;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.system.domain.SysUserRole;
import com.ruoyi.system.mapper.SysUserRoleMapper;
import com.ruoyi.system.service.ISysUserService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.Customer;
import com.ruoyi.system.service.ICustomerService;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.common.config.ServerConfig;

/**
 * 客户管理Controller
 * 
 */
@Controller
@RequestMapping("/system/customer")
public class CustomerController extends BaseController
{
    private String prefix = "system/customer";

    @Autowired
    private ICustomerService customerService;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private ServerConfig serverConfig;

    @Autowired
    private SysUserRoleMapper  sysUserRoleMapper;

    @RequiresPermissions("system:customer:view")
    @GetMapping()
    public String customer()
    {
        return prefix + "/customer";
    }

    /**
     * 查询【请填写功能名称】列表
     */
    @RequiresPermissions("system:customer:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(Customer customer)
    {
        startPage();
        SysUser user = getSysUser();
        List<SysRole> roles = user.getRoles();
        if(roles != null && roles.size() == 1 && roles.get(0).getRoleId() == 101L){
            customer.setUserId(user.getUserId());
        } else if (roles != null && roles.size() == 1 && roles.get(0).getRoleId() == 100L) {
            customer.setSaleId(user.getUserId());
        }
        List<Customer> list = customerService.selectCustomerList(customer);
        return getDataTable(list);
    }

    /**
     * 导出【请填写功能名称】列表
     */
    @RequiresPermissions("system:customer:export")
    @Log(title = "【请填写功能名称】", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(Customer customer)
    {
        List<Customer> list = customerService.selectCustomerList(customer);
        ExcelUtil<Customer> util = new ExcelUtil<Customer>(Customer.class);
        return util.exportExcel(list, "【请填写功能名称】数据");
    }

    /**
     * 新增【请填写功能名称】
     */
    @RequiresPermissions("system:customer:add")
    @GetMapping("/add")
    public String add(ModelMap mmap)
    {
        List<SysUserRole> sysUserRoles = sysUserRoleMapper.selectUserRoleByRoleId(101L);
        List<SysUser> sysUsers = new ArrayList<>();
        for (SysUserRole sysUserRole : sysUserRoles) {
            sysUsers.add(userService.selectUserById(sysUserRole.getUserId()));
        }
        mmap.put("sysUsers", sysUsers);
        return prefix + "/add";
    }

    /**
     * 新增保存【请填写功能名称】
     */
    @RequiresPermissions("system:customer:add")
    @Log(title = "【请填写功能名称】", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(Customer customer, @RequestParam("file") MultipartFile file)
    {
        String tel = customer.getCustomerTel();
        Customer queryC = new Customer();
        queryC.setCustomerTel(tel);
        List<Customer> list = customerService.selectCustomerList(queryC);
        if(list != null && !list.isEmpty()){
            return AjaxResult.error("客户信息已存在，请确认");
        }

        if(!file.isEmpty()){
            // 上传并返回新文件名称
            try {
                // 上传文件路径
                String filePath = RuoYiConfig.getUploadPath();
                String fileName = FileUploadUtils.upload(filePath, file);
                String url = serverConfig.getUrl() + fileName;

                customer.setAttachment(url);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        //填充销售信息
        SysUser user = getSysUser();
        customer.setSaleId(user.getUserId());
        customer.setSaleName(user.getUserName());

        return toAjax(customerService.insertCustomer(customer));
    }

    /**
     * 修改【请填写功能名称】
     */
    @RequiresPermissions("system:customer:edit")
    @GetMapping("/edit/{customerId}")
    public String edit(@PathVariable("customerId") Long customerId, ModelMap mmap)
    {
        List<SysUserRole> sysUserRoles = sysUserRoleMapper.selectUserRoleByRoleId(101L);
        List<SysUser> sysUsers = new ArrayList<>();
        for (SysUserRole sysUserRole : sysUserRoles) {
            sysUsers.add(userService.selectUserById(sysUserRole.getUserId()));
        }
        mmap.put("sysUsers", sysUsers);
        Customer customer = customerService.selectCustomerByCustomerId(customerId);
        mmap.put("customer", customer);
        return prefix + "/edit";
    }

    /**
     * 修改保存【请填写功能名称】
     */
    @RequiresPermissions("system:customer:edit")
    @Log(title = "【请填写功能名称】", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(Customer customer,@RequestParam("file") MultipartFile file)
    {
        String tel = customer.getCustomerTel();
        Customer queryC = new Customer();
        queryC.setCustomerTel(tel);
        List<Customer> list = customerService.selectCustomerList(queryC);
        if(list != null && !list.isEmpty()){
            return AjaxResult.error("客户信息已存在，请确认");
        }

        if(!file.isEmpty()){
            // 上传并返回新文件名称
            try {
                // 上传文件路径
                String filePath = RuoYiConfig.getUploadPath();
                String fileName = FileUploadUtils.upload(filePath, file);
                String url = serverConfig.getUrl() + fileName;

                customer.setAttachment(url);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return toAjax(customerService.updateCustomer(customer));
    }

    @RequiresPermissions("system:customer:list")
    @Log(title = "完成客户", businessType = BusinessType.UPDATE)
    @PostMapping("/handleCustomer/{customerId}")
    @ResponseBody
    public AjaxResult handleCustomer(@PathVariable("customerId") Long customerId,@RequestParam("stauts") String stauts) {
        Customer customer = new Customer();
        customer.setCustomerId(customerId);
        customer.setStauts(stauts);
        return toAjax(customerService.updateCustomer(customer));
    }

    @RequiresPermissions("system:customer:list")
    @GetMapping("/view/{customerId}")
    public String view(@PathVariable("customerId") Long customerId, ModelMap mmap){
        Customer customer = customerService.selectCustomerByCustomerId(customerId);
        mmap.put("customer", customer);
        return prefix + "/view";
    }

    /**
     * 删除【请填写功能名称】
     */
    @RequiresPermissions("system:customer:remove")
    @Log(title = "【请填写功能名称】", businessType = BusinessType.DELETE)
    @PostMapping( "/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return toAjax(customerService.deleteCustomerByCustomerIds(ids));
    }
}
