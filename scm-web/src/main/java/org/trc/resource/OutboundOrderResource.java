package org.trc.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.trc.biz.outbuond.IOutBoundOrderBiz;
import org.trc.biz.system.IWarehouseBiz;
import org.trc.constants.SupplyConstants;
import org.trc.domain.System.Warehouse;
import org.trc.domain.impower.AclUserAccreditInfo;
import org.trc.domain.order.OutboundOrder;
import org.trc.form.outbound.OutBoundOrderForm;
import org.trc.util.AppResult;
import org.trc.util.Pagenation;
import org.trc.util.ResultUtil;

import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by sone on 2017/8/10.
 */
@Component
@Path(SupplyConstants.OutboundOrder.ROOT)
public class OutboundOrderResource {

    @Autowired
    private IOutBoundOrderBiz outBoundOrderBiz;
    @Autowired
    private IWarehouseBiz warehouseBiz;

    //出库通知单分页查询
    @GET
    @Path(SupplyConstants.OutboundOrder.OUTBOUND_ORDER_PAGE)
    @Produces(MediaType.APPLICATION_JSON)
    public  Pagenation<OutboundOrder> outboundOrderPage(@BeanParam OutBoundOrderForm form, @BeanParam Pagenation<OutboundOrder> page, @Context ContainerRequestContext requestContext) throws Exception {
        return  outBoundOrderBiz.outboundOrderPage(form, page, (AclUserAccreditInfo) requestContext.getProperty(SupplyConstants.Authorization.ACL_USER_ACCREDIT_INFO));
    }

    @GET
    @Path(SupplyConstants.OutboundOrder.WAREHOUSE_LIST)
    @Produces(MediaType.APPLICATION_JSON)
    public AppResult<Warehouse> findValidWarehouseList() {
        return ResultUtil.createSucssAppResult("查询有效的仓库成功!", warehouseBiz.findWarehouseValid());
    }

    //发货通知单创建
    @POST
    @Path(SupplyConstants.OutboundOrder.DELIVERY_ORDER_CREATE)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createOutbound(@FormParam("id") String id) throws Exception {
        return ResultUtil.createSuccessResult("重新发送成功",outBoundOrderBiz.createOutbound(id));
    }

    /**
     * 取消发货
     */
    @PUT
    @Path(SupplyConstants.OutboundOrder.ORDER_CANCEL + "/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response orderCancel(@PathParam("id") Long id, @FormParam("remark") String remark){
        return outBoundOrderBiz.orderCancel(id, remark);
    }

    /**
     * 关闭
     */
    @PUT
    @Path(SupplyConstants.OutboundOrder.CLOSE + "/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response close(@PathParam("id") Long id, @FormParam("remark") String remark){
        return outBoundOrderBiz.close(id, remark);
    }

    /**
     * 取消关闭
     */
    @PUT
    @Path(SupplyConstants.OutboundOrder.CANCEL_CLOSE + "/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response cancelClose(@PathParam("id") Long id){
        return outBoundOrderBiz.cancelClose(id);
    }

    /**
     * 获取发货单详情
     */
    @GET
    @Path(SupplyConstants.OutboundOrder.OUTBOUND_ORDER_DETAIL + "/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOutboundOrderDetail(@PathParam("id") Long id){
        return outBoundOrderBiz.getOutboundOrderDetail(id);
    }
}
