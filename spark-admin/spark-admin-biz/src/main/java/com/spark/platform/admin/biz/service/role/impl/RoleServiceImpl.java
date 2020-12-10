package com.spark.platform.admin.biz.service.role.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.Preconditions;
import com.spark.platform.admin.api.entity.role.Role;
import com.spark.platform.admin.api.entity.role.RoleMenu;
import com.spark.platform.admin.biz.dao.role.RoleDao;
import com.spark.platform.admin.biz.dao.role.RoleMenuDao;
import com.spark.platform.admin.biz.service.role.RoleService;
import com.spark.platform.common.base.constants.GlobalsConstants;
import com.spark.platform.common.base.constants.RedisConstants;
import com.spark.platform.common.base.exception.BusinessException;
import com.spark.platform.common.base.support.WrapperSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: wangdingfeng
 * @ProjectName: spark-platform
 * @Package: com.spark.platform.adminbiz.service.role.impl
 * @ClassName: RoleServiceImpl
 * @Date: 2019/11/5 09:28
 * @Description: 角色service
 * @Version: 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RoleServiceImpl extends ServiceImpl<RoleDao, Role> implements RoleService {

    private final RoleMenuDao roleMenuDao;

    @Override
    public List<Role> getRoleByUserId(Long userId) {
        return super.baseMapper.getRoleByUserId(userId);
    }

    @Override
    public IPage findPage(Role role, Page page) {
        QueryWrapper queryWrapper = new QueryWrapper<Role>();
        WrapperSupport.putParamsLike(queryWrapper, role, "roleName", "roleCode");
        WrapperSupport.putParamsEqual(queryWrapper, role, "deptId");
        return super.baseMapper.selectPage(page, queryWrapper);
    }

    @Override
    public Role saveOrUpdateRole(Role role) {
        validateRoleCode(role.getRoleCode(), role.getId());
        super.saveOrUpdate(role);
        return role;
    }

    @Override
    @CacheEvict(value = RedisConstants.USER_CACHE, allEntries = true)
    public void saveRoleAuth(Role role) {
        Preconditions.checkArgument(null != role && null != role.getId(), "角色id不能为空");
        //删除该角色下所有的权限
        int i = roleMenuDao.delteRoleAuth(role.getId());
        log.info("删除角色：{}权限:{}个", role.getId(), i);
        for (Long menuId : role.getMenuIds()) {
            RoleMenu roleMenu = new RoleMenu();
            roleMenu.setRoleId(role.getId());
            roleMenu.setMenuId(menuId);
            roleMenuDao.insert(roleMenu);
        }
    }

    @Override
    public List<Role> findAllRole() {
        return super.baseMapper.findAllRole();
    }

    @Override
    public void validateRoleCode(String roleCode, Long roleId) {
        roleCode = roleCode.toUpperCase();
        //判断是否包含
        if (!roleCode.startsWith(GlobalsConstants.ROLE_PREFIX)) {
            throw new BusinessException("角色编号应该包含ROLE_");
        }
        LambdaQueryWrapper<Role> queryWrapper = Wrappers.lambdaQuery();
        if (null != roleId) {
            queryWrapper.ne(Role::getId, roleId);
        }
        queryWrapper.eq(Role::getRoleCode, roleCode);
        if (0 != super.count(queryWrapper)) {
            throw new BusinessException("角色编号重复");
        }
    }

    @Override
    public boolean delete(Long roleId) {
        String roleCode = super.baseMapper.getRoleCode(roleId);
        if (GlobalsConstants.ROLE_ADMIN.equals(roleCode)) {
            throw new BusinessException("超级管理员角色，不允许删除");
        }
        return super.removeById(roleId);
    }


}
