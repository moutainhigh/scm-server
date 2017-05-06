package org.trc.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.trc.biz.supplier.ISupplierBiz;
import org.trc.constants.SupplyConstants;
import org.trc.domain.dict.Dict;
import org.trc.domain.supplier.Supplier;
import org.trc.form.supplier.SupplierForm;
import org.trc.util.AppResult;
import org.trc.util.Pagenation;
import org.trc.util.ResultUtil;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by hzwdx on 2017/5/5.
 */
@Component
@Path(SupplyConstants.Supply.ROOT)
public class SupplierResource {

    @Autowired
    private ISupplierBiz supplierBiz;

    @GET
    @Path(SupplyConstants.Supply.Supplier.SUPPLIER_PAGE)
    @Produces(MediaType.APPLICATION_JSON)
    public Pagenation<Supplier> supplierPage(@BeanParam SupplierForm form, @BeanParam Pagenation<Supplier> page) throws Exception{
        return supplierBiz.SupplierPage(form, page);
    }

    @GET
    @Path(SupplyConstants.Supply.Supplier.SUPPLIER_LIST)
    @Produces(MediaType.APPLICATION_JSON)
    public AppResult<List<Supplier>> querySuppliers(@BeanParam SupplierForm form) throws Exception{
        return ResultUtil.createSucssAppResult("查询供应商列表成功", supplierBiz.querySuppliers(form));
    }

    @POST
    @Path(SupplyConstants.Supply.Supplier.SUPPLIER)
    @Produces(MediaType.APPLICATION_JSON)
    public AppResult saveSupplier(@BeanParam Supplier supplier) throws Exception{
        return ResultUtil.createSucssAppResult("保存字典成功", supplierBiz.saveSupplier(supplier));
    }

    @PUT
    @Path(SupplyConstants.Supply.Supplier.SUPPLIER+"/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public AppResult updateSupplier(@BeanParam Supplier supplier,@PathParam("id") Long id) throws Exception{
        return ResultUtil.createSucssAppResult("修改字典成功", supplierBiz.updateSupplier(supplier,id));
    }

    @GET
    @Path(SupplyConstants.Supply.Supplier.SUPPLIER+"/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public AppResult<Dict> findSupplierById(@PathParam("id") Long id) throws Exception{
        return ResultUtil.createSucssAppResult("查询字典成功", supplierBiz.findSupplierById(id));
    }





}
