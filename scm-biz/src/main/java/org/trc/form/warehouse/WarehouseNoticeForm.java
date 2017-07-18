package org.trc.form.warehouse;

import org.hibernate.validator.constraints.Length;
import org.trc.util.QueryModel;

import javax.ws.rs.QueryParam;

/**
 * Created by sone on 2017/5/2.
 */
public class WarehouseNoticeForm extends QueryModel{
    /**
     * 入库通知单编码
     */
    @QueryParam("warehouseNoticeCode")
    @Length(max = 64)
    private String warehouseNoticeCode;

    @QueryParam("purchaseOrderCode")
    @Length(max = 32)
    private String purchaseOrderCode;

    @QueryParam("purchaseType")
    @Length(max = 32)
    private String purchaseType;

    @QueryParam("state")
    @Length(max = 2)
    private String state;

    @QueryParam("supplierName")
    @Length(max = 64)
    private String supplierName;

    public String getWarehouseNoticeCode() {
        return warehouseNoticeCode;
    }

    public void setWarehouseNoticeCode(String warehouseNoticeCode) {
        this.warehouseNoticeCode = warehouseNoticeCode;
    }

    public String getPurchaseOrderCode() {
        return purchaseOrderCode;
    }

    public void setPurchaseOrderCode(String purchaseOrderCode) {
        this.purchaseOrderCode = purchaseOrderCode;
    }

    public String getPurchaseType() {
        return purchaseType;
    }

    public void setPurchaseType(String purchaseType) {
        this.purchaseType = purchaseType;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }
}