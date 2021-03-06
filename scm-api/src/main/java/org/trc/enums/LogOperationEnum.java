package org.trc.enums;

/**
 * Created by hzqph on 2017/6/26.
 */
public enum LogOperationEnum {
    ADD("新增"),UPDATE("修改"),DELETE("删除"), AUDIT_REJECT("审核驳回"),CANCEL("作废"),RENEWAL("更新"),CREATE("创建"),SUBMIT("提交审核"), AUDIT_PASS("审核通过"),
    FREEZE("冻结"),UN_FREEZE("解冻"),WAREHOUSE_NOTICE("入库通知"),OUTBOUND_NOTICE("出库通知"),NOTICE_RECEIVE("通知收货"),WMS_RECEIVE_FAILED("仓库接收失败"),
    SUBMIT_JINGDONG_ORDER("映射京东地址并发送"),SUBMIT_ORDER("发送"),SUBMIT_ORDER_FIALURE("下单失败"), DELIVER("已发货"),SYNCHRONIZE("同步"),

    WAIT_FOR_SUBMIT("待发送供应商"),ORDER_EXCEPTION("供应商下单异常"),WAIT_FOR_DELIVER("等待供应商发货"),ALL_DELIVER("全部发货"),
    ORDER_FAILURE("供应商下单失败"),PARTS_DELIVER("部分发货"),ORDER_CANCEL("已取消"),ORDER_CLOSE("关闭"),ORDER_REOPEN("取消关闭"),
    ORDER_SUCCESS("下单成功"),

    OUTBOUND_RECEIVE_FAIL("仓库接收失败"),OUTBOUND_WAITING("等待仓库发货"),OUTBOUND_ON_WAREHOUSE_NOTICE("仓库告知的过程中状态"),
    OUTBOUND_ALL_GOODS("全部发货"),OUTBOUND_PART_OF_SHIPMENT("部分发货"),OUTBOUND_CANCELED("已取消"),OUTBOUND_SEND("发送"),OUTBOUND_RECEIVE_SUCCESS("仓库接收成功"),
    SEND("发货"), UPDATE_RECEIVER_INFO("修改收货地址"), ADD_NEW_WAREHOUSE("新增发货仓库"),

    ALLOCATE_IN_WMS("入库"), DISCARDED("作废"),CANCEL_CLOSE("取消关闭"),HAND_CLOSE("手工关闭"),RECIVE_WMS_RECIVE_FAILURE("入库仓接收失败"),
    RECIVE_WMS_RECIVE_SUCCESS("入库仓接收成功"),RE_RECIVE_GOODS("重新收货"),CANCEL_RECIVE_GOODS("取消收货"),NOTICE_RECIVE_GOODS("通知收货"),
	NOTICE_WMS("通知仓库"),NOTICE_SEND_GOODS("通知出库"),
    PURCHASE_BOX_INFO("装箱信息维护"),NOTICE_SUCCESS("通知成功"),ADD_ITEM("添加"),NOTICE_WAREHOUSE("通知仓库"),
    NOTICE_FAIL("通知失败"),IMPORT_ITEM_ID("导入商品及其仓库ID"),CANCEL_NOTICE("取消通知"),RECOVER_NOTICE("恢复"),

    //调拨
    RECEIVE_ORDER("接收"), IMPORT_ORDER("导入"),RECIVE_GOODS_IN("收货入库"),

    ALLOCATE_ORDER_OUT_NOTICE("通知出库"),
    ALLOCATE_ORDER_IN_NOTICE("通知入库"),
	ALLOCATE_ORDER_OUT_NOTICE_SUCC("出库仓接收成功"),
	ALLOCATE_ORDER_OUT_NOTICE_FAIL("出库仓接收失败"),
	ALLOCATE_ORDER_IN_NOTICE_SUCC("入库仓接收成功"),
	ALLOCATE_ORDER_IN_NOTICE_FAIL("入库仓接收失败"),
    ALLOCATE_OUT("出库"),
    ALLOCATE_IN("入库"),
    UPDATE_LOGISTICS_NUM("填写物流单号"),
    //采购退货
    ENTRY_RETURN_NOTICE("通知出库"),
    ENTRY_RETURN_NOTICE_SUCC("仓库接收成功"),
    ENTRY_RETURN_NOTICE_FAIL("仓库接收失败"),
    ENTRY_RETURN_NOTICE_CANCEL("取消出库"),
	ENTRY_RETURN_NOTICE_FINISH("出库"),
	ENTRY_RETURN_NOTICE_SYNCHRONIZE("退货出库"),

    //售后
    AFTER_SALE_ORDER_IN("入库");

    private String message;

    LogOperationEnum(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
