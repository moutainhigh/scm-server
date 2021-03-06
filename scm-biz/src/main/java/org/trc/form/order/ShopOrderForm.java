package org.trc.form.order;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.validator.constraints.Length;
import org.trc.util.QueryModel;

import javax.ws.rs.QueryParam;

/**
 * Created by hzwdx on 2017/6/26.
 */
public class ShopOrderForm extends QueryModel{

    // 店铺订单编码
    @QueryParam("shopOrderCode")
    @Length(max = 32, message = "店铺订单编码长度不能超过32个")
    private String shopOrderCode;

    @QueryParam("scmShopOrderCode")
    @Length(max = 32, message = "系统订单号长度不能超过32个")
    private String scmShopOrderCode;

    @QueryParam("sellCode")
    @Length(max = 32, message = "销售渠道编码长度不能超过32个")
    private String sellCode;

    // 平台订单编码
    @QueryParam("platformOrderCode")
    @Length(max = 32, message = "平台订单编码长度不能超过32个")
    private String platformOrderCode;

    // 订单状态
    @QueryParam("status")
    @Length(max = 32, message = "订单状态长度不能超过32个")
    private String status;

    // 收货人姓名
    @QueryParam("receiverName")
    @Length(max = 128, message = "收货人姓名长度不能超过128个")
    private String receiverName;

    // 店铺名称
    @QueryParam("shopName")
    @Length(max = 255, message = "店铺名称长度不能超过255个")
    private String shopName;

    // 订单类型：0-普通订单 1-零元购 2-分期购 3-拼团
    @QueryParam("type")
    private String type;

    //供应商订单状态：1-待发货,2-部分发货,3-全部发货,4-已取消
    @QueryParam("supplierOrderStatus")
    private String supplierOrderStatus;

    // 0-接口,1-导入
    @QueryParam("reciverType")
    private String reciverType;


    public String getScmShopOrderCode() {
        return scmShopOrderCode;
    }

    public void setScmShopOrderCode(String scmShopOrderCode) {
        this.scmShopOrderCode = scmShopOrderCode;
    }

    public String getSellCode() {
        return sellCode;
    }

    public void setSellCode(String sellCode) {
        this.sellCode = sellCode;
    }

    public String getShopOrderCode() {
        return shopOrderCode;
    }

    public void setShopOrderCode(String shopOrderCode) {
        this.shopOrderCode = shopOrderCode;
    }

    public String getPlatformOrderCode() {
        return platformOrderCode;
    }

    public void setPlatformOrderCode(String platformOrderCode) {
        this.platformOrderCode = platformOrderCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSupplierOrderStatus() {
        return supplierOrderStatus;
    }

    public void setSupplierOrderStatus(String supplierOrderStatus) {
        this.supplierOrderStatus = supplierOrderStatus;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getReciverType() {
        return reciverType;
    }

    public void setReciverType(String reciverType) {
        this.reciverType = reciverType;
    }

    @Override
    public String toString(){
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
