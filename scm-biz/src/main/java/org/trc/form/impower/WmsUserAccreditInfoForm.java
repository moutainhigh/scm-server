package org.trc.form.impower;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.validator.constraints.Length;
import org.trc.util.QueryModel;

import javax.ws.rs.QueryParam;

/**
 * Created by szy on 2017/5/11.
 */
public class WmsUserAccreditInfoForm extends QueryModel {
    /**
     * 用户姓名
     */
    @QueryParam("name")
    @Length(max = 64)
    private String name;
    @QueryParam("phone")
    @Length(max = 16)
    private String phone;
    @QueryParam("warehouseName")
    @Length(max = 16)
    private String warehouseName;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
