package org.trc.biz.impl.outbound;

import com.qimen.api.request.DeliveryorderConfirmRequest;
import com.qimen.api.request.DeliveryorderCreateRequest;
import com.qimen.api.request.OrderCancelRequest;
import com.qimen.api.response.OrderCancelResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.trc.biz.outbuond.IOutBoundOrderBiz;
import org.trc.cache.CacheEvit;
import org.trc.domain.System.Warehouse;
import org.trc.domain.impower.AclUserAccreditInfo;
import org.trc.domain.order.OutboundDetail;
import org.trc.domain.order.OutboundDetailLogistics;
import org.trc.domain.order.OutboundOrder;
import org.trc.enums.*;
import org.trc.exception.OutboundOrderException;
import org.trc.form.outbound.OutBoundOrderForm;
import org.trc.service.IQimenService;
import org.trc.service.System.IWarehouseService;
import org.trc.service.outbound.IOutBoundOrderService;
import org.trc.service.outbound.IOutboundDetailLogisticsService;
import org.trc.service.outbound.IOutboundDetailService;
import org.trc.util.*;
import tk.mybatis.mapper.entity.Example;

import javax.ws.rs.core.Response;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service("outBoundOrderBiz")
public class OutBoundOrderBiz implements IOutBoundOrderBiz {

    private Logger logger = LoggerFactory.getLogger(OutBoundOrderBiz.class);

    @Autowired
    private IOutBoundOrderService outBoundOrderService;
    @Autowired
    private IWarehouseService warehouseService;
    @Autowired
    private IOutboundDetailService outboundDetailService;
    @Autowired
    private IOutboundDetailLogisticsService outboundDetailLogisticsService;
    @Autowired
    private IQimenService qimenService;

    private static final String SUCCESS_CODE = "200";


    @Override
    public Pagenation<OutboundOrder> outboundOrderPage(OutBoundOrderForm form, Pagenation<OutboundOrder> page, AclUserAccreditInfo aclUserAccreditInfo) throws Exception {
        AssertUtil.notNull(aclUserAccreditInfo, "获取用户信息失败!");
        //获得业务线编码
        String channelCode = aclUserAccreditInfo.getChannelCode();
        AssertUtil.notBlank(channelCode, "业务线编码为空!");

        //创建查询条件
        Example example = new Example(OutboundOrder.class);
        this.setQueryParam(example, form);

        //查询数据
        Pagenation<OutboundOrder> pagenation = outBoundOrderService.pagination(example, page, form);

        return pagenation;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void updateOutboundDetail(String requestText) throws Exception {
        AssertUtil.notBlank(requestText, "获取奇门返回信息为空!");
        DeliveryorderConfirmRequest confirmRequest = (DeliveryorderConfirmRequest) XmlUtil.xmlStrToBean(requestText, DeliveryorderConfirmRequest.class);
        //包裹信息
        List<DeliveryorderConfirmRequest.Package> packageList = confirmRequest.getPackages();
        //发货单信息
        DeliveryorderConfirmRequest.DeliveryOrder deliveryOrder = confirmRequest.getDeliveryOrder();

        //获取发货单
        String outboundOrderCode = deliveryOrder.getDeliveryOrderCode();
        String status = deliveryOrder.getStatus();
        String operateTime = deliveryOrder.getOperateTime();
        AssertUtil.notBlank(outboundOrderCode, "发货单编号不能为空!");
        OutboundOrder outboundOrder = new OutboundOrder();
        outboundOrder.setOutboundOrderCode(outboundOrderCode);
        outboundOrder = outBoundOrderService.selectOne(outboundOrder);

        //更新发货单信息
        this.updateOutboundDetailAndLogistics(packageList, outboundOrderCode, status, operateTime);

        //更新发货单状态
        this.setOutboundOrderStatus(outboundOrderCode, outboundOrder);

    }

    //更新发货单状态
    private void setOutboundOrderStatus(String outboundOrderCode, OutboundOrder outboundOrder){
        List<OutboundDetail> outboundDetailList = null;
        OutboundDetail outboundDetail = new OutboundDetail();
        outboundDetail.setOutboundOrderCode(outboundOrderCode);
        outboundDetailList = outboundDetailService.select(outboundDetail);
        String outboundOrderStatus = this.getOutboundOrderStatusByDetail(outboundDetailList);
        outboundOrder.setStatus(outboundOrderStatus);
        outBoundOrderService.updateByPrimaryKey(outboundOrder);
    }

    //更新发货单
    private void updateOutboundDetailAndLogistics(List<DeliveryorderConfirmRequest.Package> packageList, String outboundOrderCode, String status, String operateTime) throws Exception{
        List<DeliveryorderConfirmRequest.Item> itemList = null;
        OutboundDetail outboundDetail = null;
        OutboundDetailLogistics outboundDetailLogistics = null;
        List<OutboundDetailLogistics> outboundDetailLogisticsList = null;
        //遍历包裹
        for(DeliveryorderConfirmRequest.Package packageD : packageList){
            itemList = packageD.getItems();
            if(itemList != null){
                //遍历包裹内商品，更新物流和发货单详情
                for(DeliveryorderConfirmRequest.Item item : itemList){
                    Long sentNum = item.getQuantity();
                    //获取发货详情
                    outboundDetail = new OutboundDetail();
                    outboundDetail.setOutboundOrderCode(outboundOrderCode);
                    outboundDetail.setSkuCode(item.getItemCode());
                    outboundDetail = outboundDetailService.selectOne(outboundDetail);

                    //获取当前商品物流
                    outboundDetailLogistics = new OutboundDetailLogistics();
                    outboundDetailLogistics.setOutboundDetailId(outboundDetail.getId());
                    outboundDetailLogistics.setWaybillNumber(packageD.getExpressCode());
                    outboundDetailLogisticsList = outboundDetailLogisticsService.select(outboundDetailLogistics);

                    //判断是否已存储物流信息，如果没有新增
                    if(outboundDetailLogisticsList != null && outboundDetailLogisticsList.size() > 0){
                        outboundDetailLogistics = outboundDetailLogisticsList.get(0);
                        outboundDetailLogistics.setUpdateTime(Calendar.getInstance().getTime());
                        //更新发货商品详情
                        this.updateOutboundDetail(status, outboundDetailLogistics, outboundDetail, operateTime, sentNum);
                        //保存信息
                        outboundDetailLogisticsService.updateByPrimaryKey(outboundDetailLogistics);
                        outboundDetailService.updateByPrimaryKey(outboundDetail);
                    }else{
                        outboundDetailLogistics = new OutboundDetailLogistics();
                        outboundDetailLogistics.setOutboundDetailId(outboundDetail.getId());
                        outboundDetailLogistics.setLogisticsCorporation(packageD.getLogisticsName());
                        outboundDetailLogistics.setLogisticsCode(packageD.getLogisticsCode());
                        outboundDetailLogistics.setItemNum(sentNum);
                        outboundDetailLogistics.setWaybillNumber(packageD.getExpressCode());
                        outboundDetailLogistics.setCreateTime(Calendar.getInstance().getTime());
                        outboundDetailLogistics.setUpdateTime(Calendar.getInstance().getTime());
                        //更新发货商品详情
                        this.updateOutboundDetail(status, outboundDetailLogistics, outboundDetail, operateTime, sentNum);
                        //保存信息
                        outboundDetailLogisticsService.insert(outboundDetailLogistics);
                        outboundDetailService.updateByPrimaryKey(outboundDetail);
                    }
                }
            }
        }
    }

    //更新发货商品明细
    private void updateOutboundDetail(String status, OutboundDetailLogistics outboundDetailLogistics,
                                      OutboundDetail outboundDetail, String operateTime, Long sentNum) throws Exception{
        if(StringUtils.equals(status, QimenDeliveryEnum.DELIVERED.getCode())){
            if(outboundDetailLogistics.getDeliverTime() == null ||
                    this.compareDeliverTime(outboundDetailLogistics.getDeliverTime(), operateTime)){
                outboundDetailLogistics.setDeliverTime(this.getTime(operateTime));
            }
            outboundDetail.setStatus(OutboundDetailStatusEnum.ALL_GOODS.getCode());
            outboundDetail.setUpdateTime(Calendar.getInstance().getTime());
            outboundDetail.setRealSentItemNum(sentNum);
        }
        if(StringUtils.equals(status, QimenDeliveryEnum.PARTDELIVERED.getCode())){
            outboundDetail.setStatus(OutboundDetailStatusEnum.PART_OF_SHIPMENT.getCode());
            outboundDetail.setUpdateTime(Calendar.getInstance().getTime());
            outboundDetail.setRealSentItemNum(sentNum);
        }
    }

    //获取时间
    private Date getTime(String operateTime) throws Exception{
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.parse(operateTime);
    }

    //比较时间
    private boolean compareDeliverTime(Date oldDate, String newTime) throws Exception{
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date newDate = sdf.parse(newTime);
        if(oldDate.getTime() > newDate.getTime()){
            return false;
        }
        return true;
    }

    @Override
    public void createOutbound(String outboundOrderId) throws Exception {
        AssertUtil.notBlank(outboundOrderId,"ID不能为空");
        //根据id获取到发货通知单
        OutboundOrder outboundOrder = outBoundOrderService.selectByPrimaryKey(Long.valueOf(outboundOrderId));
        AssertUtil.notNull(outboundOrder,"根据发货通知单id获取发货通知单记录为空");
        Long warehouseId = outboundOrder.getWarehouseId();
        Warehouse warehouse = warehouseService.selectByPrimaryKey(warehouseId);

        //参数校验
        verifyParam(warehouse,outboundOrder);
        DeliveryorderCreateRequest request = new DeliveryorderCreateRequest();
        DeliveryorderCreateRequest.DeliveryOrder deliveryOrder =  new DeliveryorderCreateRequest.DeliveryOrder();
        //request.setDeliveryOrder();
    }

    @Override
    @CacheEvit
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public Response orderCancel(Long id, String remark) {
        AssertUtil.notNull(id, "发货单主键不能为空");
        AssertUtil.notBlank(remark, "取消原因不能为空");

        //获取发货单信息
        OutboundOrder outboundOrder = outBoundOrderService.selectByPrimaryKey(id);

        if(!StringUtils.equals(outboundOrder.getStatus(), OutboundOrderStatusEnum.WAITING.getCode())){
            String msg = "发货通知单状态必须为等待仓库发货!";
            logger.error(msg);
            throw new OutboundOrderException(ExceptionEnum.OUTBOUND_ORDER_EXCEPTION, msg);
        }

        //获取仓库信息
        Warehouse warehouse =warehouseService.selectByPrimaryKey(outboundOrder.getWarehouseId());

        //组装请求
        OrderCancelRequest orderCancelRequest = new OrderCancelRequest();
        orderCancelRequest.setCancelReason(remark);
        orderCancelRequest.setOrderCode(outboundOrder.getOutboundOrderCode());
        orderCancelRequest.setWarehouseCode(warehouse.getQimenWarehouseCode());

        //调用奇门接口
        AppResult<OrderCancelResponse> appResult = qimenService.orderCancel(orderCancelRequest);

        //处理信息
        if (StringUtils.equals(appResult.getAppcode(), SUCCESS_CODE)) { // 成功
            this.updateDetailStatus(OutboundDetailStatusEnum.CANCELED.getCode(), outboundOrder.getOutboundOrderCode());

            return ResultUtil.createSuccessResult("发货通知单取消成功！", "");
        } else {
            return ResultUtil.createfailureResult(Response.Status.BAD_REQUEST.getStatusCode(), "发货通知单取消失败！", "");
        }
    }

    //修改详情状态
    private void updateDetailStatus(String code, String outboundOrderCode){
        OutboundDetail outboundDetail = new OutboundDetail();
        outboundDetail.setStatus(code);
        outboundDetail.setUpdateTime(Calendar.getInstance().getTime());
        Example exampleOrder = new Example(OutboundDetail.class);
        Example.Criteria criteriaOrder = exampleOrder.createCriteria();
        criteriaOrder.andEqualTo("outboundOrderCode", outboundOrderCode);
        outboundDetailService.updateByExampleSelective(outboundDetail, exampleOrder);
    }

    //修改取消发货单信息
    private void updateOrderCancelInfo(OutboundOrder outboundOrder){
        outboundOrder.setStatus(OutboundOrderStatusEnum.CANCELED.getCode());
        outboundOrder.setIsCancel(ZeroToNineEnum.ONE.getCode());
        outboundOrder.setUpdateTime(Calendar.getInstance().getTime());
        outBoundOrderService.updateByPrimaryKey(outboundOrder);
    }

    private void verifyParam(Warehouse warehouse,OutboundOrder outboundOrder){
        AssertUtil.notBlank(outboundOrder.getOutboundOrderCode(),"出库通知单编号不能为空");
        AssertUtil.notBlank(outboundOrder.getOrderType(),"出库单类型不能为空");
        AssertUtil.notBlank(warehouse.getCode(),"仓库编码不能为空");
        AssertUtil.notNull(outboundOrder.getCreateTime(),"发货单创建时间不能为空");
        AssertUtil.notNull(outboundOrder.getPayTime(),"付款时间不能为空");
        AssertUtil.notBlank(outboundOrder.getShopName(),"店铺名称不能为空");
        AssertUtil.notBlank(warehouse.getName(),"发货仓库名称不能为空");
        AssertUtil.notBlank(warehouse.getSenderPhoneNumber(),"运单发件人手机号不能为空");
        AssertUtil.notBlank(warehouse.getProvince(),"发货仓库省份不能为空");
        AssertUtil.notBlank(warehouse.getCity(),"发货仓库城市不能为空");
        AssertUtil.notBlank(warehouse.getAddress(),"发货仓库的详细地址不能为空");
        AssertUtil.notBlank(outboundOrder.getReceiverName(),"收件人姓名不能为空");
        AssertUtil.notBlank(outboundOrder.getReceiverPhone(),"收件人联系方式不能为空");
        AssertUtil.notBlank(outboundOrder.getReceiverProvince(),"收件人省份不能为空");
        AssertUtil.notBlank(outboundOrder.getReceiverCity(),"收件人城市不能为空");
        AssertUtil.notBlank(outboundOrder.getReceiverAddress(),"收件人详细地址不能为空");
        AssertUtil.notBlank(outboundOrder.getBuyerMessage(),"买家留言不能为空");
        AssertUtil.notBlank(outboundOrder.getSellerMessage(),"卖家留言不能为空");
        //AssertUtil.not
    }

    public void setQueryParam(Example example, OutBoundOrderForm form) {
        Example.Criteria criteria = example.createCriteria();
        //发货通知单编号
        if (!StringUtils.isBlank(form.getOutboundOrderCode())) {
            criteria.andLike("outboundOrderCode", "%" + form.getOutboundOrderCode() + "%");

        }
        //店铺订单编号
        if (!StringUtils.isBlank(form.getShopOrderCode())) {
            criteria.andLike("shopOrderCode", "%" + form.getShopOrderCode() + "%");

        }
        //发货仓库id
        if (!StringUtils.isBlank(form.getWarehouseId())) {
            criteria.andEqualTo("warehouseId", form.getWarehouseId());
        }
        //状态
        if (!StringUtils.isBlank(form.getStatus())) {
            criteria.andEqualTo("status", String.valueOf(form.getStatus()));
        }
        //收货人
        if (!StringUtils.isBlank(form.getReceiverName())) {
            criteria.andLike("receiverName", "%" + form.getReceiverName() + "%");

        }
        //付款时间
        if (!StringUtils.isBlank(form.getStartPayDate())) {
            criteria.andGreaterThan("payTime", form.getStartPayDate());
        }
        if (!StringUtils.isBlank(form.getEndPayDate())) {
            criteria.andLessThan("payTime", DateUtils.formatDateTime(DateUtils.addDays(form.getEndPayDate(), DateUtils.NORMAL_DATE_FORMAT, 1)));
        }
        //平台订单编号
        if (!StringUtils.isBlank(form.getPlatformOrderCode())) {
            criteria.andLike("platformOrderCode", "%" + form.getPlatformOrderCode() + "%");

        }
        //发货单创建日期
        if (!StringUtils.isBlank(form.getStartCreateDate())) {
            criteria.andGreaterThan("createTime", form.getStartCreateDate());
        }
        if (!StringUtils.isBlank(form.getEndCreateDate())) {
            criteria.andLessThan("createTime", DateUtils.formatDateTime(DateUtils.addDays(form.getEndCreateDate(), DateUtils.NORMAL_DATE_FORMAT, 1)));
        }
        example.orderBy("status").asc();
        example.orderBy("createTime").desc();
    }

    //获取状态
    private String getOutboundOrderStatusByDetail(List<OutboundDetail> outboundDetailList){
        int failureNum = 0;//仓库接收失败数
        int waitDeliverNum = 0;//等待发货数
        int allDeliverNum = 0;//全部发货数
        int partsDeliverNum = 0;//部分发货数
        int cancelNum = 0;//已取消数
        for(OutboundDetail detail : outboundDetailList){
            if(StringUtils.equals(OutboundDetailStatusEnum.RECEIVE_FAIL.getCode(), detail.getStatus()))
                failureNum++;
            else if(StringUtils.equals(OutboundDetailStatusEnum.WAITING.getCode(), detail.getStatus()))
                waitDeliverNum++;
            else if(StringUtils.equals(OutboundDetailStatusEnum.ALL_GOODS.getCode(), detail.getStatus()))
                allDeliverNum++;
            else if(StringUtils.equals(OutboundDetailStatusEnum.PART_OF_SHIPMENT.getCode(), detail.getStatus()))
                partsDeliverNum++;
            else if(StringUtils.equals(OutboundDetailStatusEnum.CANCELED.getCode(), detail.getStatus()))
                cancelNum++;
        }
        //已取消：所有商品的发货状态均更新为“已取消”时，发货单的状态就更新为“已取消”；
        if(cancelNum == outboundDetailList.size()){
            return OutboundOrderStatusEnum.CANCELED.getCode();
        }
        //仓库接收失败：所有商品的发货状态均为“仓库接收失败”时，发货单的状态就为“仓库接收失败”
        if(failureNum == outboundDetailList.size()){
            return OutboundOrderStatusEnum.RECEIVE_FAIL.getCode();
        }
        //全部发货：所有商品的发货状态均为“全部发货”时，发货单的状态就为“全部发货”
        if(allDeliverNum == outboundDetailList.size()){
            return OutboundOrderStatusEnum.ALL_GOODS.getCode();
        }
        //部分发货：存在发货状态为“部分发货”的商品或者同时存在待发货和已发货(部分发货或全部发货)的商品，发货单的状态就为“部分发货”
        if(partsDeliverNum > 0 || (waitDeliverNum > 0 && (partsDeliverNum > 0 || allDeliverNum > 0))){
            return OutboundOrderStatusEnum.PART_OF_SHIPMENT.getCode();
        }
        return OutboundOrderStatusEnum.WAITING.getCode();
    }


}
