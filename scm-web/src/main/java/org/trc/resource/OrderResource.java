package org.trc.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.trc.biz.order.IScmOrderBiz;
import org.trc.constants.SupplyConstants;
import org.trc.domain.dict.DictType;
import org.trc.domain.order.ShopOrder;
import org.trc.domain.order.WarehouseOrder;
import org.trc.form.order.PlatformOrderForm;
import org.trc.form.order.ShopOrderForm;
import org.trc.form.order.WarehouseOrderForm;
import org.trc.util.AppResult;
import org.trc.util.Pagenation;
import org.trc.util.ResultUtil;

import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by hzwdx on 2017/6/26.
 */
@Component
@Path(SupplyConstants.Order.ROOT)
public class OrderResource {

    @Autowired
    private IScmOrderBiz scmOrderBiz;

    @GET
    @Path(SupplyConstants.Order.SHOP_ORDER_PAGE)
    @Produces(MediaType.APPLICATION_JSON)
    public Pagenation<ShopOrder> shopOrderPage(@BeanParam ShopOrderForm form, @BeanParam Pagenation<ShopOrder> page){
        return scmOrderBiz.shopOrderPage(form, page);
    }

    @GET
    @Path(SupplyConstants.Order.WAREHOUSE_ORDER_PAGE)
    @Produces(MediaType.APPLICATION_JSON)
    public Pagenation<WarehouseOrder> warehouseOrderPage(@BeanParam WarehouseOrderForm form, @BeanParam Pagenation<WarehouseOrder> page){
        return scmOrderBiz.warehouseOrderPage(form, page);
    }

    @GET
    @Path(SupplyConstants.Order.SHOP_ORDER_LIST)
    @Produces(MediaType.APPLICATION_JSON)
    public AppResult<List<ShopOrder>> queryShopOrders(@BeanParam ShopOrderForm form){
        return ResultUtil.createSucssAppResult("根据条件查询店铺订单成功", scmOrderBiz.queryShopOrders(form));
    }

    @GET
    @Path(SupplyConstants.Order.WAREHOUSE_ORDER_DETAIL+"/{warehouseOrderCode}")
    @Produces(MediaType.APPLICATION_JSON)
    public AppResult<List<ShopOrder>> queryWarehouseOrdersDetail(@PathParam("warehouseOrderCode") String warehouseOrderCode){
        return ResultUtil.createSucssAppResult("根据仓库订单编码查询仓库订单成功", scmOrderBiz.queryWarehouseOrdersDetail(warehouseOrderCode));
    }

    @GET
    @Path(SupplyConstants.Order.PLATFORM_ORDER_LIST)
    @Produces(MediaType.APPLICATION_JSON)
    public AppResult<List<ShopOrder>> queryPlatformOrders(@BeanParam PlatformOrderForm form){
        return ResultUtil.createSucssAppResult("根据条件查询平台订单成功", scmOrderBiz.queryPlatformOrders(form));
    }

    @POST
    @Path(SupplyConstants.Order.JING_DONG_ORDER)
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes("application/x-www-form-urlencoded")
    public AppResult submitJingDongOrder(@FormParam("warehouseOrderCode") String warehouseOrderCode,
            @FormParam("jdAddress") String jdAddress, @Context ContainerRequestContext requestContext) throws Exception {

        return ResultUtil.createSucssAppResult("订单提交京东成功", "");
    }




}



