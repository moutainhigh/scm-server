package org.trc.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by hzwdx on 2017/4/22.
 */
public enum ExceptionEnum{
    /**
     * 异常编码按模块划分：
     * 系统管理:000开头
     * 配置管理:100开头
     * 类目管理:200开头
     * 供应商管理:300开头
     * 商品管理:400开头
     * 采购管理:500开头
     * 订单管理:600开头
     * 库存管理:700开头
     * 审批管理:800开头
     * 权限管理:900开头
     * 外部调用:1000开头
     * 数据库:3000开头
     * 系统异常:4000开头
     */
    CONFIG_DICT_QUERY_EXCEPTION("100101","数据字典查询异常"),
    CONFIG_DICT_SAVE_EXCEPTION("100102","数据字典保存异常"),
    CONFIG_DICT_UPDATE_EXCEPTION("100103","数据字典更新异常"),
    SERIAL_MODULE_NOT_EXIST("100104","生成序列号的模块不存在"),

    SUPPLIER_QUERY_EXCEPTION("300100","供应商查询异常"),
    SUPPLIER_SAVE_EXCEPTION("300101","供应商保存异常"),
    SUPPLIER_UPDATE_EXCEPTION("300102","供应商更新异常"),
    SUPPLIER_DEPEND_DATA_INVALID("300103","供应商依赖数据被禁用"),

    GOODS_QUERY_EXCEPTION("400100","商品查询异常"),
    GOODS_SAVE_EXCEPTION("400101","商品保存异常"),
    GOODS_UPDATE_EXCEPTION("400102","商品更新异常"),
    GOODS_DEPEND_DATA_INVALID("400103","商品依赖属性被禁用"),
    GOODS_SKU_VALID_CON_NOT_STOP("400104","当前SPU下还存在启用的商品,无法停用"),

    EXTERNAL_GOODS_QUERY_EXCEPTION("400104","代发商品查询异常"),
    EXTERNAL_GOODS_UPDATE_EXCEPTION("400105","代发商品查询异常"),
    EXTERNAL_GOODS_UPDATE_NOTICE_CHANNEL_EXCEPTION("400107","代发商品更新通知渠道异常"),

    FILE_UPLOAD_EXCEPTION("1000100","文件上传异常"),
    FILE_DOWNLOAD_EXCEPTION("1000101","文件下载异常"),
    FILE_SHOW_EXCEPTION("1000102","文件显示异常"),

    CATEGORY_BRAND_QUERY_EXCEPTION("200101","品牌查询异常"),
    CATEGORY_BRAND_SAVE_EXCEPTION("200102","品牌保存异常"),
    CATEGORY_BRAND_UPDATE_EXCEPTION("200103","品牌更新异常"),
    CATEGORY_PROPERTY_SAVE_EXCEPTION("200104","属性保存异常"),
    CATEGORY_PROPERTY_UPDATE_EXCEPTION("200105","属性更新异常"),
    CATEGORY_PROPERTY_VALUE_SAVE_EXCEPTION("200106","属性值类型保存异常"),
    CATEGORY_PROPERTY_VALUE_UPDATE_EXCEPTION("200107","属性值类型保存异常"),
    CATEGORY_PROPERTY_VALUE_QUERY_EXCEPTION("200108","属性值类型查询异常"),
    CATEGORY_PROPERTY_QUERY_EXCEPTION("200109","属性查询异常"),
    CATEGORY_CATEGORY_UPDATE_EXCEPTION("200110","分类更新异常"),
    CATEGORY_CATEGORY_SAVE_EXCEPTION("200111","分类保存异常"),
    CATEGORY_PROPERTY_DELETE_EXCEPTION("200112","分类关联属性删除异常"),
    CATEGORY_LINK_LEVEL_EXCEPTION("200113","分类关联异常"),



    SYSTEM_CHANNEL_QUERY_EXCEPTION("000101","渠道查询异常"),
    SYSTEM_CHANNEL_SAVE_EXCEPTION("000102","渠道保存异常"),
    SYSTEM_CHANNEL_UPDATE_EXCEPTION("000103","渠道更新异常"),
    SYSTEM_WAREHOUSE_QUERY_EXCEPTION("000201","仓库查询异常"),
    SYSTEM_WAREHOUSE_SAVE_EXCEPTION("000202","仓库保存异常"),
    SYSTEM_WAREHOUSE_UPDATE_EXCEPTION("000203","仓库更新异常"),
    SYSTEM_ACCREDIT_QUERY_EXCEPTION("000301","授权相关查询异常"),
    SYSTEM_ACCREDIT_SAVE_EXCEPTION("000302","授权相关保存异常"),
    SYSTEM_ACCREDIT_UPDATE_EXCEPTION("000303","授权相关更新异常"),
    SYSTEM_SYS_ROLE_STATE_UPDATE_EXCEPTION("000304","系统状态不能修改异常"),

    SUPPLIER_APPLY_AUDIT_QUERY_EXCEPTION("300101","供应商申请审核信息查询异常"),
    SUPPLIER_APPLY_AUDIT_UPDATE_EXCEPTION("300102","供应商申请审核信息更新异常"),
    SUPPLIER_APPLY_AUDIT_LOG_INSERT_EXCEPTION("300103","供应商申请审核日志信息保存异常"),
    SUPPLIER_APPLY_SAVE_EXCEPTION("300104","供应商申请信息保存异常"),
    SUPPLIER_APPLY_DELETE_EXCEPTION("300105","供应商申请信息删除异常"),
    SUPPLIER_APPLY_UPDATE_EXCEPTION("300106","供应商申请信息更新异常"),
    SUPPLIER_APPLY_QUERY_EXCEPTION("300107","供应商申请信息查询异常"),


    DATABASE_DUPLICATE_KEY_EXCEPTION("3000100","数据库主键重复异常"),
    DATABASE_PERMISSION_DENIED_EXCEPTION("3000101","数据库数据访问权限异常"),
    DATABASE_QUERY_TIME_OUT_EXCEPTION("3000102","数据库查询超时异常"),
    DATABASE_DEADLOCK_DATA_ACESS_EXCEPTION("3000103","数据库死锁访问数据异常"),

    DATABASE_DATA_VERSION_EXCEPTION("3000104","数据库的流水记录正在使用"),
    DATABASE_SAVE_SERIAL_EXCEPTION("3000105","保存流水号异常"),

    PURCHASE_PURCHASEGROUP_QUERY_EXCEPTION("500101","采购组查询异常"),
    PURCHASE_PURCHASEGROUP_SAVE_EXCEPTION("500102","采购组保存异常"),
    PURCHASE_PURCHASEGROUP_UPDATE_EXCEPTION("500103","采购组更新异常"),

    PURCHASE_PURCHASE_ORDER_QUERY_EXCEPTION("500201","采购单查询异常"),
    PURCHASE_PURCHASE_ORDER_SAVE_EXCEPTION("500202","采购单保存异常"),
    PURCHASE_PURCHASE_ORDER_UPDATE_EXCEPTION("500203","采购单更新异常"),

    PURCHASE_PURCHASE_ORDER_AUDIT_QUERY_EXCEPTION("500301","采购单审核查询异常"),
    PURCHASE_PURCHASE_ORDER_AUDIT_SAVE_EXCEPTION("500302","采购单审核保存异常"),
    PURCHASE_PURCHASE_ORDER_AUDIT_UPDATE_EXCEPTION("500303","采购单审核更新异常"),

    PURCHASE_PURCHASE_ORDER_DETAIL_QUERY_EXCEPTION("500401","采购单商品查询异常"),

    WAREHOUSE_NOTICE_QUERY_EXCEPTION("500401","入库通知单查询异常"),
    WAREHOUSE_NOTICE_UPDATE_EXCEPTION("500403","入库通知单更新异常"),

    USER_CENTER_QUERY_EXCEPTION("600101","用户中心查询异常"),

    ORDER_IDEMPOTENT_SAVE_EXCEPTION("600102","订单保存幂等信息异常"),
    SUBMIT_JING_DONG_ORDER("600103","调用京东下单服务异常"),
    JING_DONG_ORDER_QUERY_EXCEPTION("600104","调用京东订单反查服务异常"),
    JING_DONG_LOGISTICS_QUERY_EXCEPTION("600105","调用京东订单配送服务异常"),
    SUPPLIER_ORDER_INFO_UPDATE_EXCEPTION("600107","供应商订单信息更新异常"),
    CHANNEL_ORDER_DATA_NOT_JSON_EXCEPTION("600108","渠道发送的订单不是JSON格式参数异常"),
    SUPPLIER_LOGISTICS_QUERY_EXCEPTION("600109","供应商物流信息查询异常"),
    SUPPLIER_LOGISTICS_UPDATE_EXCEPTION("600110","供应商物流信息更新异常"),
    SUPPLIER_ORDER_NOTIFY_EXCEPTION("600111","供应商订单下单结果通知信息错误"),
    ORDER_STATUS_UPDATE_EXCEPTION("600112","更新订单状态异常"),
    ORDER_QUERY_EXCEPTION("600113","订单查询异常"),
    ORDER_PARAM_DATA_ERROR("600114","订单参数数据错误"),
    INVOKE_JD_QUERY_INTERFACE_FAIL("600115","调用京东查询接口失败"),
    ORDER_NOTIFY_TIME_OUT("600116","订单同步超时"),
    ORDER_ITEMS_ERROR("600117","订单商品错误"),
    SUBMIT_LY_ORDER("600118","调用京东下单服务异常"),
    ORDER_IS_CANCEL("600119","订单已经是取消状态，不能进行取消操作"),
    ORDER_IS_CLOSE_CANCEL("600120","订单不是取消状态，不能进行关闭取消操作"),

    USER_BE_FORBIDDEN("900001","用户授权信息不存在或用户已经被禁用!"),
    USER_NOT_LOGIN("900002","用户未登录"),
    USER_NOT_HAVE_PERMISSION("900003","用户无此权限"),

    TRC_BRAND_EXCEPTION("600101","通知品牌更改异常"),
    TRC_PROPERTY_EXCEPTION("600102","通知属性更改异常"),
    TRC_CATEGORY_EXCEPTION("600103","通知分类更改异常"),
    TRC_CATEGORY_BRAND_EXCEPTION("600104","通知分类品牌更改异常"),
    TRC_CATEGORY_PROPERTY_EXCEPTION("600105","通知分类更改异常"),
    TRC_ITEMS_EXCEPTION("600106","通知商品更改异常"),
    TRC_ORDER_PUSH_EXCEPTION("600107","推送订单模块异常"),
    TRC_EXTERNALITEMSKU_UPDATE_EXCEPTION("600108","一件代发sku更新通知异常"),
    TRC_PARAM_EXCEPTION("600109","参数校验异常"),
    TRC_LOGISTIC_EXCEPTION("600110","物流信息请求异常"),

    JING_DONG_USE_EXCEPTION("1000101","京东地址更新调用异常"),
    JING_DONG_ORDER_UPDATE_OPERATE_STATE_EXCEPTION("1000102","京东订单明细操作更新异常"),
    JING_DONG_ORDER_GET_OPERATE_STATE_EXCEPTION("1000103","京东获取订单明细操作状态异常"),
    JING_DONG_ORDER_EXPORT_EXCEPTION("1000104","订单明细导出异常"),
    JING_DONG_BALANCE_EXPORT_EXCEPTION("1000105","余额明细导出异常"),
    JING_DONG_BALANCE_INFO_EXCEPTION("1000106","京东账户余额信息查询异常"),

    SYSTEM_EXCEPTION("4000100","系统异常"),
    SYSTEM_BUSY("4000101","系统繁忙"),
    REMOTE_INVOKE_TIMEOUT_EXCEPTION("4000102","外部接口调用超时"),
    SIGN_ERROR("4000103","签名错误"),
    NOVALID("0","停用");

    private String code;
    private String message;

    ExceptionEnum(String code, String message){
        this.code = code;
        this.message = message;
    }


    /**
     *
     * @Title: getExceptionEnumByCode
     * @Description: 根据枚举编码获取枚举
     * @param @param name
     * @param @return
     * @return CommonExceptionEnum
     * @throws
     */
    public static CommonExceptionEnum getExceptionEnumByCode(String code){
        for(CommonExceptionEnum exceptionEnum : CommonExceptionEnum.values()){
            if(StringUtils.equals(exceptionEnum.getCode(), code)){
                return exceptionEnum;
            }
        }
        return null;
    }

    public String getMessage() {
        return message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }


}
