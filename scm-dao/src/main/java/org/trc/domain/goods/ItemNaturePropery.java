package org.trc.domain.goods;

import org.hibernate.validator.constraints.Length;
import org.trc.domain.util.ScmDO;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.ws.rs.FormParam;
import javax.ws.rs.PathParam;

public class ItemNaturePropery extends ScmDO{
    @PathParam("id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @FormParam("itemId")
    private Long itemId;
    @FormParam("spuCode")
    @Length(max = 32, message = "商品SPU编号长度不能超过32个")
    private String spuCode;
    @FormParam("propertyId")
    private Long propertyId;
    @Transient
    private String propertyName;
    @FormParam("propertyValueId")
    private Long propertyValueId;
    @FormParam("isValid")
    @Length(max = 2, message = "是否有编码字母和数字不能超过2个")
    private String isValid; //是否有效:0-否,1-是

    @Transient
    private String propertyValue;

    /**
     * 自然属性信息
     */
    @FormParam("naturePropertys")
    @Transient
    private String naturePropertys;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getSpuCode() {
        return spuCode;
    }

    public void setSpuCode(String spuCode) {
        this.spuCode = spuCode == null ? null : spuCode.trim();
    }

    public Long getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Long propertyId) {
        this.propertyId = propertyId;
    }

    public Long getPropertyValueId() {
        return propertyValueId;
    }

    public void setPropertyValueId(Long propertyValueId) {
        this.propertyValueId = propertyValueId;
    }

    public String getNaturePropertys() {
        return naturePropertys;
    }

    public void setNaturePropertys(String naturePropertys) {
        this.naturePropertys = naturePropertys;
    }

    public String getIsValid() {
        return isValid;
    }

    public void setIsValid(String isValid) {
        this.isValid = isValid;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }
}