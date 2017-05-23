package org.trc.resource;

import org.springframework.stereotype.Component;
import org.trc.biz.purchase.IPurchaseGroupBiz;
import org.trc.constants.SupplyConstants;
import org.trc.domain.purchase.PurchaseGroup;
import org.trc.form.purchase.PurchaseGroupForm;
import org.trc.util.AppResult;
import org.trc.util.Pagenation;
import org.trc.util.ResultUtil;

import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Created by sone on 2017/5/19.
 */
@Component
@Path(SupplyConstants.PurchaseGroup.ROOT)
public class PurchaseGroupResource {

    @Resource
    private IPurchaseGroupBiz purchaseGroupBiz;

    @GET
    @Path(SupplyConstants.PurchaseGroup.PURCHASE_GROUP_PAGE)
    @Produces(MediaType.APPLICATION_JSON)
    public Pagenation<PurchaseGroup> purchaseGroupPage(@BeanParam PurchaseGroupForm form, @BeanParam Pagenation<PurchaseGroup> page) throws Exception{
        return purchaseGroupBiz.purchaseGroupPage(form , page);
    }
    @POST
    @Path(SupplyConstants.PurchaseGroup.UPDATE_STATE+"/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public AppResult updatePurchaseStatus(@BeanParam PurchaseGroup purchaseGroup) throws Exception {
        purchaseGroupBiz.updatePurchaseStatus(purchaseGroup);
        return ResultUtil.createSucssAppResult("修改采购组状态成功","");
    }

}
