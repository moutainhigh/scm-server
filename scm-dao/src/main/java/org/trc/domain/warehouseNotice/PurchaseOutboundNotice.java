package org.trc.domain.warehouseNotice;

import io.swagger.annotations.ApiModelProperty;
import org.trc.domain.BaseDO;
import org.trc.domain.purchase.PurchaseOutboundDetail;

import javax.persistence.*;
import javax.ws.rs.FormParam;
import javax.ws.rs.PathParam;
import java.util.List;

@Table(name = "purchase_outbound_notice")
public class PurchaseOutboundNotice extends BaseDO {

    private static final long serialVersionUID = -4114185533338509893L;

    @Id
    @PathParam("id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 退货出库通知单编号
     */
    @ApiModelProperty("退货出库通知单编号")
    @Column(name = "outbound_notice_code")
    @FormParam("outboundNoticeCode")
    private String outboundNoticeCode;

    /**
     * 采购退货单编号
     */
    @ApiModelProperty("采购退货单编号")
    @Column(name = "purchase_outbound_order_code")
    @FormParam("purchaseOutboundOrderCode")
    private String purchaseOutboundOrderCode;

    /**
     * 仓储系统出库单编码
     */
    @ApiModelProperty("仓储系统出库单编码")
    @Column(name = "entry_order_id")
    @FormParam("entryOrderId")
    private String entryOrderId;

    /**
     * warehouse_info_id
     */
    @ApiModelProperty("warehouseInfoId")
    @Column(name = "warehouse_info_id")
    @FormParam("warehouseInfoId")
    private Long warehouseInfoId;

    /**
     * 仓库编号
     */
    @ApiModelProperty("仓库编号")
    @Column(name = "warehouse_code")
    @FormParam("warehouseCode")
    private String warehouseCode;

    /**
     * 货主ID
     */
    @ApiModelProperty("货主ID")
    @Column(name = "channel_code")
    @FormParam("channelCode")
    private String channelCode;

    /**
     * 完成状态：0-未完成，1-已完成
     */
    @ApiModelProperty("完成状态：0-未完成，1-已完成")
    @Column(name = "finish_status")
    @FormParam("finishStatus")
    private String finishStatus;

    /**
     * 状态:0-待通知出库,1-出库仓接收成功,2-出库仓接收失败,3-出库完成,4-出库异常,5-已取消
     */
    @ApiModelProperty("状态:0-待通知出库,1-出库仓接收成功,2-出库仓接收失败,3-出库完成,4-出库异常,5-已取消")
    @FormParam("status")
    private String status;

    /**
     * 供应商编号
     */
    @ApiModelProperty("供应商编号")
    @Column(name = "supplier_code")
    @FormParam("supplierCode")
    private String supplierCode;

    /**
     * 退货收货人
     */
    @ApiModelProperty("退货收货人")
    @FormParam("receiver")
    private String receiver;

    /**
     * 退货人手机号
     */
    @ApiModelProperty("退货人手机号")
    @Column(name = "receiver_number")
    @FormParam("receiverNumber")
    private String receiverNumber;

    /**
     * 退货省份
     */
    @ApiModelProperty("退货省份")
    @Column(name = "receiver_province")
    @FormParam("receiverProvince")
    private String receiverProvince;

    /**
     * 退货城市
     */
    @ApiModelProperty("退货城市")
    @Column(name = "receiver_city")
    @FormParam("receiverCity")
    private String receiverCity;

    /**
     * 退货地区
     */
    @ApiModelProperty("退货地区")
    @Column(name = "receiver_area")
    @FormParam("receiverArea")
    private String receiverArea;

    /**
     * 退货详细地址
     */
    @ApiModelProperty("退货详细地址")
    @Column(name = "receiver_address")
    @FormParam("receiverAddress")
    private String receiverAddress;

    @ApiModelProperty("备注")
    @FormParam("remark")
    private String remark;

	/**
     * 仓库接收出库通知失败原因
     */
    @ApiModelProperty("仓库接收出库通知失败原因")
    @Column(name = "failure_cause")
    @FormParam("failureCause")
    private String failureCause;

    /**
     * 出库通知异常原因
     */
    @ApiModelProperty("出库通知异常原因")
    @Column(name = "exception_cause")
    @FormParam("exceptionCause")
    private String exceptionCause;
    
    /**
     * 提货方式1-到仓自提，2-京东配送，3-其他物流
     */
    @ApiModelProperty("提货方式1-到仓自提，2-京东配送，3-其他物流")
    @Column(name = "pick_type")
    @FormParam("pickType")
    private String pickType;

    /**
     * 退货类型1-正品，2-残品
     */
    @ApiModelProperty("退货类型1-正品，2-残品")
    @Column(name = "return_order_type")
    @FormParam("returnOrderType")
    private String returnOrderType;
    
    /**
     * 退货说明
     */
    @ApiModelProperty("退货说明")
    @Column(name = "return_policy")
    @FormParam("returnPolicy")
    private String returnPolicy;
    
    /**
     * 供应商名称
     */
    @Transient
    @ApiModelProperty("供应商名称")
    private String supplierName;
    
    /**
     * 退货仓库名称
     */
    @Transient
    @ApiModelProperty("退货仓库名称")
    private String warehouseName;
    
    /**
     * 出库单创建人名称
     */
    @Transient
    @ApiModelProperty("出库单创建人名称")
    private String creatorName;

    /**
     * 退货出库通知单商品明细列表
     */
    @Transient
    @ApiModelProperty("退货出库通知单商品明细列表")
    private List<PurchaseOutboundDetail> skuList;

    public String getReturnPolicy() {
		return returnPolicy;
	}

	public void setReturnPolicy(String returnPolicy) {
		this.returnPolicy = returnPolicy;
	}

	public String getReceiverArea() {
		return receiverArea;
	}

	public void setReceiverArea(String receiverArea) {
		this.receiverArea = receiverArea;
	}

	public String getReturnOrderType() {
		return returnOrderType;
	}

	public void setReturnOrderType(String returnOrderType) {
		this.returnOrderType = returnOrderType;
	}

    public List<PurchaseOutboundDetail> getSkuList() {
		return skuList;
	}

	public void setSkuList(List<PurchaseOutboundDetail> skuList) {
		this.skuList = skuList;
	}

	/**
     * @return id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取退货出库通知单编号
     *
     * @return outbound_notice_code - 退货出库通知单编号
     */
    public String getOutboundNoticeCode() {
        return outboundNoticeCode;
    }

    /**
     * 设置退货出库通知单编号
     *
     * @param outboundNoticeCode 退货出库通知单编号
     */
    public void setOutboundNoticeCode(String outboundNoticeCode) {
        this.outboundNoticeCode = outboundNoticeCode;
    }

    /**
     * 获取采购退货单编号
     *
     * @return purchase_outbound_order_code - 采购退货单编号
     */
    public String getPurchaseOutboundOrderCode() {
        return purchaseOutboundOrderCode;
    }

    /**
     * 设置采购退货单编号
     *
     * @param purchaseOutboundOrderCode 采购退货单编号
     */
    public void setPurchaseOutboundOrderCode(String purchaseOutboundOrderCode) {
        this.purchaseOutboundOrderCode = purchaseOutboundOrderCode;
    }

    /**
     * 获取仓储系统出库单编码
     *
     * @return entry_order_id - 仓储系统出库单编码
     */
    public String getEntryOrderId() {
        return entryOrderId;
    }

    /**
     * 设置仓储系统出库单编码
     *
     * @param entryOrderId 仓储系统出库单编码
     */
    public void setEntryOrderId(String entryOrderId) {
        this.entryOrderId = entryOrderId;
    }

    /**
     * 获取warehouse_info_id
     *
     * @return warehouse_info_id - warehouse_info_id
     */
    public Long getWarehouseInfoId() {
        return warehouseInfoId;
    }

    /**
     * 设置warehouse_info_id
     *
     * @param warehouseInfoId warehouse_info_id
     */
    public void setWarehouseInfoId(Long warehouseInfoId) {
        this.warehouseInfoId = warehouseInfoId;
    }

    /**
     * 获取仓库编号
     *
     * @return warehouse_code - 仓库编号
     */
    public String getWarehouseCode() {
        return warehouseCode;
    }

    /**
     * 设置仓库编号
     *
     * @param warehouseCode 仓库编号
     */
    public void setWarehouseCode(String warehouseCode) {
        this.warehouseCode = warehouseCode;
    }

    /**
     * 获取货主ID
     *
     * @return channel_code - 货主ID
     */
    public String getChannelCode() {
        return channelCode;
    }

    /**
     * 设置货主ID
     *
     * @param channelCode 货主ID
     */
    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    /**
     * 获取完成状态：0-未完成，1-已完成
     *
     * @return finish_status - 完成状态：0-未完成，1-已完成
     */
    public String getFinishStatus() {
        return finishStatus;
    }

    /**
     * 设置完成状态：0-未完成，1-已完成
     *
     * @param finishStatus 完成状态：0-未完成，1-已完成
     */
    public void setFinishStatus(String finishStatus) {
        this.finishStatus = finishStatus;
    }

    /**
     * 获取状态:0-待通知出库,1-出库仓接收成功,2-出库仓接收失败,3-出库完成,4-出库异常,5-已取消
     *
     * @return status - 状态:0-待通知出库,1-出库仓接收成功,2-出库仓接收失败,3-出库完成,4-出库异常,5-已取消
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置状态:0-待通知出库,1-出库仓接收成功,2-出库仓接收失败,3-出库完成,4-出库异常,5-已取消
     *
     * @param status 状态:0-待通知出库,1-出库仓接收成功,2-出库仓接收失败,3-出库完成,4-出库异常,5-已取消
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 获取供应商编号
     *
     * @return supplier_code - 供应商编号
     */
    public String getSupplierCode() {
        return supplierCode;
    }

    /**
     * 设置供应商编号
     *
     * @param supplierCode 供应商编号
     */
    public void setSupplierCode(String supplierCode) {
        this.supplierCode = supplierCode;
    }

    /**
     * 获取退货收货人
     *
     * @return receiver - 退货收货人
     */
    public String getReceiver() {
        return receiver;
    }

    /**
     * 设置退货收货人
     *
     * @param receiver 退货收货人
     */
    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    /**
     * 获取退货人手机号
     *
     * @return receiver_number - 退货人手机号
     */
    public String getReceiverNumber() {
        return receiverNumber;
    }

    /**
     * 设置退货人手机号
     *
     * @param receiverNumber 退货人手机号
     */
    public void setReceiverNumber(String receiverNumber) {
        this.receiverNumber = receiverNumber;
    }

    /**
     * 获取退货省份
     *
     * @return receiver_province - 退货省份
     */
    public String getReceiverProvince() {
        return receiverProvince;
    }

    /**
     * 设置退货省份
     *
     * @param receiverProvince 退货省份
     */
    public void setReceiverProvince(String receiverProvince) {
        this.receiverProvince = receiverProvince;
    }

    /**
     * 获取退货城市
     *
     * @return receiver_city - 退货城市
     */
    public String getReceiverCity() {
        return receiverCity;
    }

    /**
     * 设置退货城市
     *
     * @param receiverCity 退货城市
     */
    public void setReceiverCity(String receiverCity) {
        this.receiverCity = receiverCity;
    }

    /**
     * 获取退货详细地址
     *
     * @return receiver_address - 退货详细地址
     */
    public String getReceiverAddress() {
        return receiverAddress;
    }

    /**
     * 设置退货详细地址
     *
     * @param receiverAddress 退货详细地址
     */
    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    /**
     * @return remark
     */
    public String getRemark() {
        return remark;
    }

    /**
     * @param remark
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getPickType() {
		return pickType;
	}

	public void setPickType(String pickType) {
		this.pickType = pickType;
	}

	/**
     * 获取仓库接收出库通知失败原因
     *
     * @return failure_cause - 仓库接收出库通知失败原因
     */
    public String getFailureCause() {
        return failureCause;
    }

    /**
     * 设置仓库接收出库通知失败原因
     *
     * @param failureCause 仓库接收出库通知失败原因
     */
    public void setFailureCause(String failureCause) {
        this.failureCause = failureCause;
    }

    /**
     * 获取出库通知异常原因
     *
     * @return exception_cause - 出库通知异常原因
     */
    public String getExceptionCause() {
        return exceptionCause;
    }

    /**
     * 设置出库通知异常原因
     *
     * @param exceptionCause 出库通知异常原因
     */
    public void setExceptionCause(String exceptionCause) {
        this.exceptionCause = exceptionCause;
    }

	public String getSupplierName() {
		return supplierName;
	}

	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}

	public String getWarehouseName() {
		return warehouseName;
	}

	public void setWarehouseName(String warehouseName) {
		this.warehouseName = warehouseName;
	}

	public String getCreatorName() {
		return creatorName;
	}

	public void setCreatorName(String creatorName) {
		this.creatorName = creatorName;
	}
    
}