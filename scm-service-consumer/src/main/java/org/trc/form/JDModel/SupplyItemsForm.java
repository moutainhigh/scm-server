package org.trc.form.JDModel;

import io.swagger.annotations.ApiParam;
import org.hibernate.validator.constraints.Length;
import org.trc.util.QueryModel;

import javax.ws.rs.QueryParam;

/**
 * Created by hzwdx on 2017/6/20.
 */
public class SupplyItemsForm extends QueryModel{

    @ApiParam(value = "供应商编号")
    @QueryParam("supplierCode")
    private String supplierCode; //供应商编号
    @ApiParam(value = "商品SKU编号")
    @QueryParam("supplySku")
    @Length(max = 64, message = "商品SKU编号长度不能超过64个")
    private String supplySku;//供应商商品Sku
    @ApiParam(value = "商品名称")
    @QueryParam("skuName")
    @Length(max = 255, message = "商品名称长度不能超过255个")
    private String skuName;//商品名称
    @ApiParam(value = "品牌")
    @QueryParam("brand")
    private String brand;
    public String getSupplierCode() {
        return supplierCode;
    }

    public void setSupplierCode(String supplierCode) {
        this.supplierCode = supplierCode;
    }

    public String getSupplySku() {
        return supplySku;
    }

    public void setSupplySku(String supplySku) {
        this.supplySku = supplySku;
    }

    public String getSkuName() {
        return skuName;
    }

    public void setSkuName(String skuName) {
        this.skuName = skuName;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }
}
