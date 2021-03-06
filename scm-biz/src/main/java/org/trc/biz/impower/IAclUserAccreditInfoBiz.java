package org.trc.biz.impower;

import org.trc.domain.System.ChannelExt;
import org.trc.domain.impower.AclRole;
import org.trc.domain.impower.AclUserAccreditInfo;
import org.trc.domain.impower.AclUserAddPageDate;
import org.trc.form.impower.UserAccreditInfoForm;
import org.trc.util.Pagenation;

import java.util.List;

/**
 * @author sone
 * @date 2017/5/11
 */
public interface IAclUserAccreditInfoBiz {
    /**
     * 分页查询授权信息
     *
     * @param form 授权信息查询条件
     * @param page 分页信息
     * @return 分页信息及当前页面的数据
     */
    Pagenation<AclUserAddPageDate> userAccreditInfoPage(UserAccreditInfoForm form, Pagenation<AclUserAddPageDate> page);


    /**
     * 分页查询授权信息es搜索
     *
     * @param form 授权信息查询条件
     * @param page 分页信息
     * @return 分页信息及当前页面的数据
     */
//    Pagenation<AclUserAddPageDate> userAccreditInfoPageES(UserAccreditInfoForm form, Pagenation<AclUserAddPageDate> page);


    /**
     * 查询拥有采购员角色的用户
     *
     * @return 采购员列表
     * @throws Exception
     */
    List<AclUserAccreditInfo> findPurchase(AclUserAccreditInfo aclUserAccreditInfoContext);

    /**
     * 修改授权用户的状态
     *
     * @param
     * @throws Exception
     */
    void updateUserAccreditInfoStatus(AclUserAccreditInfo aclUserAccreditInfo, AclUserAccreditInfo aclUserAccreditInfoContext);

    /**
     * 根据名称查询用户授权信息
     *
     * @param name 用户姓名
     * @return 用户授权信息
     * @throws Exception
     */
    int checkUserByName(Long id, String name);

    /**
     * 处理用户显示页面的角色拼接和用户显示页面的对象的转换
     *
     * @param list
     * @return
     * @throws Exception
     */
    List<AclUserAddPageDate> handleRolesStr(List<AclUserAccreditInfo> list);

    /**
     * 查询已启用的渠道
     *
     * @return
     * @throws Exception
     */
    List<ChannelExt> findChannel();

    /**
     * 查询渠道角色或者全局角色
     *
     * @return
     * @throws Exception
     */
    List<AclRole> findChannelOrWholeJur(String roleType, AclUserAccreditInfo userAccreditInfo);

    /**
     * 新增授权
     */
    void saveUserAccreditInfo(AclUserAddPageDate userAddPageDate, AclUserAccreditInfo aclUserAccreditInfoContext) throws Exception;

    /**
     * 根据ID查询用户
     *
     * @param id
     * @return
     * @throws Exception
     */
    AclUserAccreditInfo findUserAccreditInfoById(Long id);

    /**
     * 修改授权
     *
     * @param userAddPageDate
     * @throws Exception
     */
    void updateUserAccredit(AclUserAddPageDate userAddPageDate, AclUserAccreditInfo aclUserAccreditInfoContext);

    /**
     * 手机号校验
     *
     * @param phone
     * @return
     * @throws Exception
     */
    String checkPhone(String phone);

    /**
     * 根据手机号查询用户名称
     *
     * @param phone
     * @return
     */
    String getNameByPhone(String phone);

    /**
     * 新增时用户姓名是否已经被使用
     */
//    int checkName(String name) throws Exception;

    /**
     * 采购组员校验
     *
     * @param id
     * @throws Exception
     */
    String[] purchaseRole(Long id);

    /**
     * 查询用户对应用户的起停用状态
     */
    String[] checkRoleValid(Long id);

    //    void queryChannelAndSellChannel() ;
    void logOut(String userId);
}
