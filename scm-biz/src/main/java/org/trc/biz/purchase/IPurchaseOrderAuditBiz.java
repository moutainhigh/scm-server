package org.trc.biz.purchase;

import org.trc.domain.impower.AclUserAccreditInfo;
import org.trc.domain.purchase.PurchaseOrderAddAudit;
import org.trc.domain.purchase.PurchaseOrderAudit;
import org.trc.form.purchase.PurchaseOrderAuditForm;
import org.trc.util.Pagenation;

/**
 * Created by sone on 2017/6/20.
 */
public interface IPurchaseOrderAuditBiz {

    Pagenation<PurchaseOrderAddAudit> purchaseOrderAuditPage(PurchaseOrderAuditForm form, Pagenation<PurchaseOrderAddAudit> page, AclUserAccreditInfo aclUserAccreditInfo) throws  Exception;

    /**
     * 审核采购单
     * @param purchaseOrderAudit
     * @throws Exception
     */
    void auditPurchaseOrder(PurchaseOrderAudit purchaseOrderAudit, AclUserAccreditInfo aclUserAccreditInfo) throws Exception;

}
