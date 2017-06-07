package org.trc.biz.impl.impower;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.trc.biz.impower.IRoleBiz;
import org.trc.biz.impower.IRoleJurisdictionRelationBiz;
import org.trc.domain.impower.Role;
import org.trc.enums.ExceptionEnum;
import org.trc.enums.ValidEnum;
import org.trc.exception.ConfigException;
import org.trc.exception.RoleException;
import org.trc.form.impower.RoleForm;
import org.trc.service.impower.IRoleService;
import org.trc.service.impower.IUserAccreditInfoRoleRelationService;
import org.trc.util.AssertUtil;
import org.trc.util.CommonUtil;
import org.trc.util.Pagenation;
import org.trc.util.ParamsUtil;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.util.StringUtil;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sone on 2017/5/11.
 */
@Service("roleBiz")
public class RoleBiz implements IRoleBiz{

    private Logger  LOGGER = LoggerFactory.getLogger(RoleBiz.class);

    private final static Long SYS_ROLE_ID=1L; //系统角色的id wholeJurisdiction

    private final static String WHOLE_TYPE ="wholeJurisdiction";//全局角色
    @Resource
    private IRoleService roleService;
    @Resource
    private IRoleJurisdictionRelationBiz roleJurisdictionRelationBiz;
    @Resource
    private IUserAccreditInfoRoleRelationService userAccreditInfoRoleRelationService;


    @Override
    public Role findRoleById(Long roleId) throws Exception {
        /*
         根据id查询角色对象
         */
        AssertUtil.notNull(roleId,"根据角色id，查询角色，角色的id为空");
        Role role = new Role();
        role.setId(roleId);
        Role queryRole = roleService.selectOne(role);
        AssertUtil.notNull(queryRole,String.format("根据主键ID[id=%s]查询角色为空",roleId.toString()));
        return queryRole;

    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void updateRoleState(Role role) throws Exception {

        AssertUtil.notNull(role,"根据角色对象，修改角色的状态，角色对象为空");
        Role tmp = findRoleByName(role.getName());
        if(tmp!=null){
            if(!tmp.getId().equals(role.getId())){
                throw new RoleException(ExceptionEnum.SYSTEM_SYS_ROLE_STATE_UPDATE_EXCEPTION, "其它的角色已经使用该角色名称");
            }
        }
        Role updateRole = new Role();
        if(role.getId()==SYS_ROLE_ID){ //防止恶意修改系统角色的状态
            String tip="系统角色的状态不能被修改";
            LOGGER.error(tip);
            throw  new RoleException(ExceptionEnum.SYSTEM_SYS_ROLE_STATE_UPDATE_EXCEPTION,tip);
        }
        updateRole.setId(role.getId());

        if (role.getIsValid().equals(ValidEnum.VALID.getCode())) {
            updateRole.setIsValid(ValidEnum.NOVALID.getCode());
        } else {
            updateRole.setIsValid(ValidEnum.VALID.getCode());
        }
        updateRole.setUpdateTime(Calendar.getInstance().getTime());
        int count = roleService.updateByPrimaryKeySelective(updateRole);
        if (count == 0) {
            String msg = String.format("修改角色%s数据库操作失败",JSON.toJSONString(role));
            LOGGER.error(msg);
            throw new RoleException(ExceptionEnum.SYSTEM_ACCREDIT_UPDATE_EXCEPTION, msg);
        }
        //修改关联状态
        Map<String, Object> map=new HashMap<>();
        map.put("status",updateRole.getIsValid());
        map.put("roleId",updateRole.getId());
        userAccreditInfoRoleRelationService.updateStatusByRoleId(map);

    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public int findNumFromRoleAndAccreditInfoByRoleId(Long roleId) throws Exception {

        AssertUtil.notNull(roleId,"根据角色的id查询用户的数量，角色id为空");
        int num = roleService.findNumFromRoleAndAccreditInfoByRoleId(roleId);
        return num;

    }

    @Override
    public Pagenation<Role> rolePage(RoleForm form, Pagenation<Role> page) {

        Example example=new Example(Role.class);
        Example.Criteria criteria = example.createCriteria();
        if (StringUtil.isNotEmpty(form.getName())) {
            criteria.andLike("name", "%" + form.getName() + "%");
        }
        if (StringUtil.isNotEmpty(form.getIsValid())) {
            criteria.andEqualTo("isValid", form.getIsValid());
        }
        if(StringUtil.isNotEmpty(form.getRoleType())){
            criteria.andEqualTo("roleType",form.getRoleType());
        }
        example.orderBy("updateTime").desc();
        return roleService.pagination(example,page,form);

    }
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void updateRole(Role role, String roleJurisdiction) throws Exception{

        //判断是否是系统用户,系统用户只能修改，系统角色类型，对应的权限,和备注信息
        AssertUtil.notNull(role,"角色更新时,角色对象为空");
        if(role.getId()==SYS_ROLE_ID){//为渠道用户
            if(role.getRoleType()==WHOLE_TYPE){//渠道用户,反而传的是全局的类型
                String msg = CommonUtil.joinStr("修改渠道角色,角色类型不匹配").toString();
                LOGGER.error(msg);
                throw new ConfigException(ExceptionEnum.SYSTEM_ACCREDIT_UPDATE_EXCEPTION, msg);
            }//传的权限id不做校验
            role.setName("采购组员");
            role.setRoleType("channelJurisdiction");
            role.setIsValid(null);//为防止对不需要改变的值，做修改
        }

        role.setUpdateTime(Calendar.getInstance().getTime());
        int count = roleService.updateByPrimaryKeySelective(role);
        if (count == 0) {
            String msg = String.format("修改角色%s数据库操作失败",JSON.toJSONString(role));
            LOGGER.error(msg);
            throw new RoleException(ExceptionEnum.SYSTEM_ACCREDIT_UPDATE_EXCEPTION, msg);
        }
        roleJurisdictionRelationBiz.updateRoleJurisdictionRelations(roleJurisdiction,role.getId());
        Map<String, Object> map=new HashMap<>();
        map.put("status",role.getIsValid());
        map.put("roleId",role.getId());
        userAccreditInfoRoleRelationService.updateStatusByRoleId(map);

    }
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void saveRole(Role role,String roleJurisdiction) throws Exception {
        AssertUtil.notNull(role,"角色管理模块保存角色信息失败，角色信息为空");
        Role tmp = findRoleByName(role.getName());
        AssertUtil.isNull(tmp,String.format("角色名称[name=%s]的名称已存在,请使用其他名称",role.getName()));
        int count=0;
        ParamsUtil.setBaseDO(role);
        count=roleService.insert(role);
        if(count==0){
            String msg = String.format("保存角色%s数据库操作失败", JSON.toJSONString(role));
            LOGGER.error(msg);
            throw new ConfigException(ExceptionEnum.SYSTEM_ACCREDIT_SAVE_EXCEPTION, msg);
        }
        //用来保存角色和授权的关联信息
        roleJurisdictionRelationBiz.saveRoleJurisdictionRelations(roleJurisdiction,role.getId());

    }
    @Override
    public Role findRoleByName(String name) throws Exception {

        AssertUtil.notNull(name,"根据角色名称查询角色的参数name为空");
        Role role = new Role();
        role.setName(name);
        return roleService.selectOne(role);

    }
}
