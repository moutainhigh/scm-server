package org.trc.form.trc;

import org.trc.util.QueryModel;

import javax.ws.rs.QueryParam;

/**
 * Created by hzwdx on 2017/5/24.
 */
public class ItemsForm2 extends QueryModel {
    //商品SPU编号
    @QueryParam("spuCode")
    private String spuCode;
    //商品名称
    @QueryParam("name")
    private String name;
    //商品所属分类ID
    @QueryParam("categoryId")
    private Long categoryId;
    //商品所属品牌ID
    @QueryParam("brandId")
    private Long brandId;

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
}