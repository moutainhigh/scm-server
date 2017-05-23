package org.trc.biz.purchase;

import org.trc.domain.purchase.PurchaseGroup;
import org.trc.form.purchase.PurchaseGroupForm;
import org.trc.util.Pagenation;

/**
 * Created by sone on 2017/5/19.
 */
public interface IPurchaseGroupBiz {

    Pagenation<PurchaseGroup> purchaseGroupPage(PurchaseGroupForm form , Pagenation<PurchaseGroup> page)throws Exception;

    void updatePurchaseStatus(PurchaseGroup purchaseGroup) throws Exception;

    void  savePurchaseGroup(PurchaseGroup purchaseGroup) throws Exception;

    PurchaseGroup findPurchaseByName(String name) throws Exception;

    PurchaseGroup findPurchaseById(Long id) throws Exception;

    PurchaseGroup findPurchaseGroupByCode(String code) throws Exception;

}
