package org.trc.domain.order;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.trc.custom.CustomDateSerializer;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.Date;
import java.util.List;

/**
 * Created by Ding on 2017/6/21.
 */
public class WarehouseOrder {
    // 主键
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 店铺订单编码
    private String warehouseOrderCode;

    // 订单所属的店铺id
    private Long shopId;

    // 店铺名称
    private String shopName;

    // 供应链编号
    private String supplierCode;

    // 供应商名称
    private String supplierName;

    // 店铺订单编码
    private String shopOrderCode;

    // 平台订单编码
    private String platformOrderCode;

    // 渠道编码
    private String channelCode;

    // 来源平台编码
    private String platformCode;

    // 订单来源平台 电脑-pc 手机网页-wap 移动端-app
    private String platformType;

    // 所在仓库id
    private Long warehouseId;

    // 所在仓库名称
    private String warehouseName;

    // 会员id
    private String userId;

    // 订单状态:1-待出库 2-部分出库 3-全部出库
    private String status;

    // 卖家手工调整金额,子订单调整金额之和,单位/分,单位/分
    private Long adjustFee;

    // 邮费分摊,单位/分
    private Long postageFee;

    // 促销优惠总金额,单位/分
    private Long discountPromotion;

    // 店铺优惠卷分摊总金额,单位/分
    private Long discountCouponShop;

    // 平台优惠卷分摊总金额,单位/分
    private Long discountCouponPlatform;

    // 促销优惠金额,单位/分
    private Long discountFee;

    // 各子订单中商品price * num的和，不包括任何优惠信息,单位/分
    private Long totalFee;

    // 实付金额,订单最终总额,单位/分
    private Long payment;

    // 是否删除:0-否,1-是
    private String isDeleted;

    // 创建时间,格式yyyy-mm-dd hh:mi:ss
    private Date createTime;

    // 发货时间
    private Date consignTime;

    // 更新时间
    private Date updateTime;

    //商品总数量
    private Integer itemsNum;

    //订单类型:0-自采订单,1-一件代发订单
    private String orderType;

    //付款时间
    @Transient
    @JsonSerialize(using = CustomDateSerializer.class)
    private Date payTime;

    //物流公司
    @Transient
    private String logisticsCorporation;
    //物流运单号
    @Transient
    private String waybillNumber;

    /**
     * 订单商品明细列表
     */
    @Transient
    private List<OrderItem> orderItemList;

    @Transient
    private PlatformOrder platformOrder;

    public List<OrderItem> getOrderItemList() {
        return orderItemList;
    }

    public void setOrderItemList(List<OrderItem> orderItemList) {
        this.orderItemList = orderItemList;
    }

    public PlatformOrder getPlatformOrder() {
        return platformOrder;
    }

    public void setPlatformOrder(PlatformOrder platformOrder) {
        this.platformOrder = platformOrder;
    }

    /**
     * 返回主键
     * @return 主键
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置主键
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 返回店铺订单编码
     * @return 店铺订单编码
     */
    public String getWarehouseOrderCode() {
        return warehouseOrderCode;
    }

    /**
     * 设置店铺订单编码
     */
    public void setWarehouseOrderCode(String warehouseOrderCode) {
        this.warehouseOrderCode = warehouseOrderCode == null ? null : warehouseOrderCode.trim();
    }

    /**
     * 返回订单所属的店铺id
     * @return 订单所属的店铺id
     */
    public Long getShopId() {
        return shopId;
    }

    /**
     * 设置订单所属的店铺id
     */
    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    /**
     * 返回店铺名称
     * @return 店铺名称
     */
    public String getShopName() {
        return shopName;
    }

    /**
     * 设置店铺名称
     */
    public void setShopName(String shopName) {
        this.shopName = shopName == null ? null : shopName.trim();
    }

    /**
     * 返回供应链编号
     * @return 供应链编号
     */
    public String getSupplierCode() {
        return supplierCode;
    }

    /**
     * 设置供应链编号
     */
    public void setSupplierCode(String supplierCode) {
        this.supplierCode = supplierCode == null ? null : supplierCode.trim();
    }

    /**
     * 返回供应商名称
     * @return 供应商名称
     */
    public String getSupplierName() {
        return supplierName;
    }

    /**
     * 设置供应商名称
     */
    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName == null ? null : supplierName.trim();
    }

    /**
     * 返回店铺订单编码
     * @return 店铺订单编码
     */
    public String getShopOrderCode() {
        return shopOrderCode;
    }

    /**
     * 设置店铺订单编码
     */
    public void setShopOrderCode(String shopOrderCode) {
        this.shopOrderCode = shopOrderCode == null ? null : shopOrderCode.trim();
    }

    /**
     * 返回平台订单编码
     * @return 平台订单编码
     */
    public String getPlatformOrderCode() {
        return platformOrderCode;
    }

    /**
     * 设置平台订单编码
     */
    public void setPlatformOrderCode(String platformOrderCode) {
        this.platformOrderCode = platformOrderCode == null ? null : platformOrderCode.trim();
    }

    /**
     * 返回渠道编码
     * @return 渠道编码
     */
    public String getChannelCode() {
        return channelCode;
    }

    /**
     * 设置渠道编码
     */
    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode == null ? null : channelCode.trim();
    }

    /**
     * 返回来源平台编码
     * @return 来源平台编码
     */
    public String getPlatformCode() {
        return platformCode;
    }

    /**
     * 设置来源平台编码
     */
    public void setPlatformCode(String platformCode) {
        this.platformCode = platformCode == null ? null : platformCode.trim();
    }

    /**
     * 返回订单来源平台 电脑-pc 手机网页-wap 移动端-app
     * @return 订单来源平台 电脑-pc 手机网页-wap 移动端-app
     */
    public String getPlatformType() {
        return platformType;
    }

    /**
     * 设置订单来源平台 电脑-pc 手机网页-wap 移动端-app
     */
    public void setPlatformType(String platformType) {
        this.platformType = platformType == null ? null : platformType.trim();
    }

    /**
     * 返回所在仓库id
     * @return 所在仓库id
     */
    public Long getWarehouseId() {
        return warehouseId;
    }

    /**
     * 设置所在仓库id
     */
    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    /**
     * 返回所在仓库名称
     * @return 所在仓库名称
     */
    public String getWarehouseName() {
        return warehouseName;
    }

    /**
     * 设置所在仓库名称
     */
    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName == null ? null : warehouseName.trim();
    }

    /**
     * 返回会员id
     * @return 会员id
     */
    public String getUserId() {
        return userId;
    }

    /**
     * 设置会员id
     */
    public void setUserId(String userId) {
        this.userId = userId == null ? null : userId.trim();
    }

    /**
     * 返回订单状态:1-待出库 2-部分出库 3-全部出库
     * @return 订单状态:1-待出库 2-部分出库 3-全部出库
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置订单状态:1-待出库 2-部分出库 3-全部出库
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 返回卖家手工调整金额,子订单调整金额之和,单位/分,单位/分
     * @return 卖家手工调整金额,子订单调整金额之和,单位/分,单位/分
     */
    public Long getAdjustFee() {
        return adjustFee;
    }

    /**
     * 设置卖家手工调整金额,子订单调整金额之和,单位/分,单位/分
     */
    public void setAdjustFee(Long adjustFee) {
        this.adjustFee = adjustFee;
    }

    /**
     * 返回邮费分摊,单位/分
     * @return 邮费分摊,单位/分
     */
    public Long getPostageFee() {
        return postageFee;
    }

    /**
     * 设置邮费分摊,单位/分
     */
    public void setPostageFee(Long postageFee) {
        this.postageFee = postageFee;
    }

    /**
     * 返回促销优惠总金额,单位/分
     * @return 促销优惠总金额,单位/分
     */
    public Long getDiscountPromotion() {
        return discountPromotion;
    }

    /**
     * 设置促销优惠总金额,单位/分
     */
    public void setDiscountPromotion(Long discountPromotion) {
        this.discountPromotion = discountPromotion;
    }

    /**
     * 返回店铺优惠卷分摊总金额,单位/分
     * @return 店铺优惠卷分摊总金额,单位/分
     */
    public Long getDiscountCouponShop() {
        return discountCouponShop;
    }

    /**
     * 设置店铺优惠卷分摊总金额,单位/分
     */
    public void setDiscountCouponShop(Long discountCouponShop) {
        this.discountCouponShop = discountCouponShop;
    }

    /**
     * 返回平台优惠卷分摊总金额,单位/分
     * @return 平台优惠卷分摊总金额,单位/分
     */
    public Long getDiscountCouponPlatform() {
        return discountCouponPlatform;
    }

    /**
     * 设置平台优惠卷分摊总金额,单位/分
     */
    public void setDiscountCouponPlatform(Long discountCouponPlatform) {
        this.discountCouponPlatform = discountCouponPlatform;
    }

    /**
     * 返回促销优惠金额,单位/分
     * @return 促销优惠金额,单位/分
     */
    public Long getDiscountFee() {
        return discountFee;
    }

    /**
     * 设置促销优惠金额,单位/分
     */
    public void setDiscountFee(Long discountFee) {
        this.discountFee = discountFee;
    }

    /**
     * 返回各子订单中商品price * num的和，不包括任何优惠信息,单位/分
     * @return 各子订单中商品price * num的和，不包括任何优惠信息,单位/分
     */
    public Long getTotalFee() {
        return totalFee;
    }

    /**
     * 设置各子订单中商品price * num的和，不包括任何优惠信息,单位/分
     */
    public void setTotalFee(Long totalFee) {
        this.totalFee = totalFee;
    }

    /**
     * 返回实付金额,订单最终总额,单位/分
     * @return 实付金额,订单最终总额,单位/分
     */
    public Long getPayment() {
        return payment;
    }

    /**
     * 设置实付金额,订单最终总额,单位/分
     */
    public void setPayment(Long payment) {
        this.payment = payment;
    }

    /**
     * 返回是否删除:0-否,1-是
     * @return 是否删除:0-否,1-是
     */
    public String getIsDeleted() {
        return isDeleted;
    }

    /**
     * 设置是否删除:0-否,1-是
     */
    public void setIsDeleted(String isDeleted) {
        this.isDeleted = isDeleted == null ? null : isDeleted.trim();
    }

    /**
     * 返回创建时间,格式yyyy-mm-dd hh:mi:ss
     * @return 创建时间,格式yyyy-mm-dd hh:mi:ss
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * 设置创建时间,格式yyyy-mm-dd hh:mi:ss
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * 返回发货时间
     * @return 发货时间
     */
    public Date getConsignTime() {
        return consignTime;
    }

    /**
     * 设置发货时间
     */
    public void setConsignTime(Date consignTime) {
        this.consignTime = consignTime;
    }

    /**
     * 返回更新时间
     * @return 更新时间
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * 设置更新时间
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getItemsNum() {
        return itemsNum;
    }

    public void setItemsNum(Integer itemsNum) {
        this.itemsNum = itemsNum;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public Date getPayTime() {
        return payTime;
    }

    public void setPayTime(Date payTime) {
        this.payTime = payTime;
    }

    public String getLogisticsCorporation() {
        return logisticsCorporation;
    }

    public void setLogisticsCorporation(String logisticsCorporation) {
        this.logisticsCorporation = logisticsCorporation;
    }

    public String getWaybillNumber() {
        return waybillNumber;
    }

    public void setWaybillNumber(String waybillNumber) {
        this.waybillNumber = waybillNumber;
    }
}