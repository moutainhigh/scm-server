package org.trc.biz.purchase;

import org.trc.domain.impower.AclUserAccreditInfo;
import org.trc.domain.purchase.PurchaseGroup;
import org.trc.form.purchase.PurchaseGroupForm;
import org.trc.util.Pagenation;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import java.util.List;

/**
 * Created by sone on 2017/5/19.
 */
public interface IPurchaseGroupBiz {

    Pagenation<PurchaseGroup> purchaseGroupPage(PurchaseGroupForm form , Pagenation<PurchaseGroup> page);

    void updatePurchaseStatus(PurchaseGroup purchaseGroup, ContainerRequestContext requestContext);

    void  savePurchaseGroup(PurchaseGroup purchaseGroup, ContainerRequestContext requestContext) ;

    PurchaseGroup findPurchaseByName(String name) ;

    PurchaseGroup findPurchaseById(Long id) ;

    PurchaseGroup findPurchaseGroupByCode(String code) ;

    void updatePurchaseGroup(PurchaseGroup purchaseGroup, ContainerRequestContext requestContext) ;
    /**
     * 查询该采购组，对应的无效状态的成员
     * @param id
     * @return
     * @
     */
    List<AclUserAccreditInfo> findPurchaseGroupMemberStateById(Long id) ;

    /**
     * 查询采购组列表
     * @return
     * @
     */
    List<PurchaseGroup> findPurchaseGroupList() ;

    /**
     * 根据采购组的code查询改组的采购人员
     * @param purchaseGroupCode
     * @return
     * @
     */
    List<AclUserAccreditInfo> findPurchaseGroupPersons(String purchaseGroupCode) ;
}
