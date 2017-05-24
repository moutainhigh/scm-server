package org.trc.resource;

import org.springframework.stereotype.Component;
import org.trc.constants.SupplyConstants;
import org.trc.domain.dict.DictType;
import org.trc.domain.goods.Items;
import org.trc.form.goods.ItemsForm;
import org.trc.util.AppResult;
import org.trc.util.Pagenation;
import org.trc.util.ResultUtil;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by hzwdx on 2017/5/24.
 */
@Component
@Path(SupplyConstants.Goods.ROOT)
public class GoodsResource {

    @GET
    @Path(SupplyConstants.Goods.GOODS_PAGE)
    @Produces(MediaType.APPLICATION_JSON)
    public Pagenation<DictType> goodsPage(@BeanParam ItemsForm form, @BeanParam Pagenation<Items> page) throws Exception {
        return null;
    }

    @GET
    @Path(SupplyConstants.Goods.GOODS_LIST)
    @Produces(MediaType.APPLICATION_JSON)
    public AppResult<List<DictType>> queryGoodsList(@BeanParam ItemsForm itemsForm) throws Exception {
        //return ResultUtil.createSucssAppResult("查询商品列表成功", configBiz.queryDictTypes(dictTypeForm));
        return null;
    }

    @POST
    @Path(SupplyConstants.Goods.GOODS)
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes("application/x-www-form-urlencoded")
    public AppResult saveGoods(@BeanParam Items items) throws Exception {
        //configBiz.saveDictType(dictType);
        return ResultUtil.createSucssAppResult("保存商品成功", "");
    }

    @PUT
    @Path(SupplyConstants.Goods.GOODS + "/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public AppResult updateGoods(@BeanParam Items items) throws Exception {
        //configBiz.updateDictType(dictType);
        return ResultUtil.createSucssAppResult("修改商品成功", "");
    }

    @POST
    @Path(SupplyConstants.Goods.IS_VALID + "/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public AppResult updateValid(@PathParam("id") Long id, @FormParam("isValid") String isValid) throws Exception {
        //supplierBiz.updateValid(id, isValid);
        return ResultUtil.createSucssAppResult("保存供应商成功", "");
    }


}
