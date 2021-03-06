package org.trc.form.trc;

import org.trc.util.QueryModel;

import javax.ws.rs.QueryParam;

/**
 * Created by hzwdx on 2017/5/24.
 */
public class ItemsForm2 extends QueryModel {
    //业务线编号
    @QueryParam("channelCode")
    private String channelCode;
    //商品SPU编号
    @QueryParam("spuCode")
    private String spuCode;
    //商品SPU编号
    @QueryParam("skuCode")
    private String skuCode;
    //商品名称
    @QueryParam("name")
    private String name;
    //商品所属分类ID
    @QueryParam("categoryId")
    private Long categoryId;
    //商品所属品牌ID
    @QueryParam("brandId")
    private Long brandId;
    //贸易类型
    @QueryParam("tradeType")
    private String tradeType;

    //sku关联状态: 0-未关联,1-已关联
    @QueryParam("skuRelationStatus")
    private String skuRelationStatus;

    public String getSpuCode() {
        return spuCode;
    }

    public void setSpuCode(String spuCode) {
        this.spuCode = spuCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getBrandId() {
        return brandId;
    }

    public void setBrandId(Long brandId) {
        this.brandId = brandId;
    }

    public String getSkuCode() {
        return skuCode;
    }

    public void setSkuCode(String skuCode) {
        this.skuCode = skuCode;
    }

    public String getTradeType() {
        return tradeType;
    }

    public void setTradeType(String tradeType) {
        this.tradeType = tradeType;
    }

    public String getSkuRelationStatus() {
        return skuRelationStatus;
    }

    public void setSkuRelationStatus(String skuRelationStatus) {
        this.skuRelationStatus = skuRelationStatus;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }
}
