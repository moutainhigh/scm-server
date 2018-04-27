package org.trc.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.trc.biz.category.IBrandBiz;
import org.trc.constants.SupplyConstants;
import org.trc.domain.category.Brand;
import org.trc.domain.impower.AclUserAccreditInfo;
import org.trc.enums.ValidEnum;
import org.trc.exception.ParamValidException;
import org.trc.form.category.BrandForm;
import org.trc.util.AssertUtil;
import org.trc.util.Pagenation;
import org.trc.util.ResultUtil;

import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by hzqph on 2017/5/2.
 */
@Component
@Path(SupplyConstants.Category.ROOT)
public class BrandResource {

    @Autowired
    private IBrandBiz brandBiz;

    @GET
    @Path(SupplyConstants.Category.Brand.BRAND_PAGE)
    @Produces(MediaType.APPLICATION_JSON)
    public Response brandPage(@BeanParam BrandForm form,@BeanParam Pagenation<Brand> page) throws Exception {
        return ResultUtil.createSuccessPageResult(brandBiz.brandPage(form,page));
    }

    @GET
    @Path(SupplyConstants.SelectList.VALID_LIST)
    @Produces(MediaType.APPLICATION_JSON)
    public Response queryValidList(){
        return ResultUtil.createSuccessResult("成功", ValidEnum.toJSONArray());
    }

    @GET
    @Path(SupplyConstants.Category.Brand.ASSOCIATION_SEARCH)
    @Produces(MediaType.APPLICATION_JSON)
    public Response associationSearch(@QueryParam("queryString") String queryString) throws Exception{
        return ResultUtil.createSuccessResult("成功", brandBiz.associationSearch(queryString));
    }

    /**
     *
     * @param brand
     * @return
     * @throws Exception
     */
    @POST
    @Path(SupplyConstants.Category.Brand.BRAND)
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveBrand(@BeanParam Brand brand , @Context ContainerRequestContext requestContext) throws Exception{
        brandBiz.saveBrand(brand,(AclUserAccreditInfo) requestContext.getProperty(SupplyConstants.Authorization.ACL_USER_ACCREDIT_INFO));
        return ResultUtil.createSuccessResult("保存品牌成功", "");
    }

    @GET
    @Path(SupplyConstants.Category.Brand.BRAND +"/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findBrandById(@PathParam("id") Long id) throws Exception{
        return ResultUtil.createSuccessResult("查询品牌成功", brandBiz.findBrandById(id));
    }

    @GET
    @Path(SupplyConstants.Category.Brand.BRAND_LIST)
    @Produces(MediaType.APPLICATION_JSON)
    public Response queryBrands(@BeanParam BrandForm form) throws Exception{
        return ResultUtil.createSuccessResult("查询品牌列表成功", brandBiz.queryBrands(form));
    }

    @PUT
    @Path(SupplyConstants.Category.Brand.BRAND +"/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateBrand(@BeanParam Brand brand, @Context ContainerRequestContext requestContext) throws Exception{
        brandBiz.updateBrand(brand, (AclUserAccreditInfo) requestContext.getProperty(SupplyConstants.Authorization.ACL_USER_ACCREDIT_INFO));
        return ResultUtil.createSuccessResult("更新品牌成功", "");
    }

    @PUT
    @Path(SupplyConstants.Category.Brand.BRAND_STATE+"/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateBrandStatus(@FormParam("isValid")String isValid,@PathParam("id") Long id, @Context ContainerRequestContext requestContext)throws Exception{
        AssertUtil.notNull(id, "需要更新品牌状态时，品牌主键ID不能为空");
        AssertUtil.notBlank(isValid, "需要更新品牌状态时，品牌当状态不能为空");
        Brand brand = new Brand();
        brand.setId(id);
        brand.setIsValid(isValid);
        brandBiz.updateBrandStatus(brand,(AclUserAccreditInfo) requestContext.getProperty(SupplyConstants.Authorization.ACL_USER_ACCREDIT_INFO));
        ValidEnum validEnum=ValidEnum.VALID;
        if (brand.getIsValid().equals(ValidEnum.VALID.getCode())) {
            validEnum=ValidEnum.NOVALID;
        }
        String msg=validEnum.getName()+"成功!";
        return ResultUtil.createSuccessResult(msg, "");
    }

    @POST
    @Path(SupplyConstants.Category.Brand.CHECK_NAME)
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkBrandName(@FormParam("id")Long id,@FormParam("name")String name){
        try {
            brandBiz.checkBrandName(id,name);
        }catch (ParamValidException e){
            return ResultUtil.createSuccessResult("品牌查询成功", "1");
        }
        return ResultUtil.createSuccessResult("品牌查询成功", null);
    }

}
