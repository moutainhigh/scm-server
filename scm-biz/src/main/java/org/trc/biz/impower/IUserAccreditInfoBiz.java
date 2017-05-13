package org.trc.biz.impower;

import org.trc.domain.impower.UserAccreditInfo;
import org.trc.form.impower.UserAccreditInfoForm;
import org.trc.util.Pagenation;

/**
 * Created by sone on 2017/5/11.
 */
public interface IUserAccreditInfoBiz {
    /**
     * 分页查询授权信息
     * @param form  授权信息查询条件
     * @param page  分页信息
     * @return  分页信息及当前页面的数据
     */
    Pagenation<UserAccreditInfo> UserAccreditInfoPage(UserAccreditInfoForm form, Pagenation<UserAccreditInfo> page) throws Exception;

    /**
     *根据名称查询用户授权信息
     * @param name 用户姓名
     * @return 用户授权信息
     * @throws Exception
     */
    UserAccreditInfo findUserAccreditInfoByName(String name) throws Exception;

}
