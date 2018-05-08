package org.trc.service.impl.allocateOrder;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.trc.domain.allocateOrder.AllocateInOrder;
import org.trc.domain.allocateOrder.AllocateOrderBase;
import org.trc.domain.allocateOrder.AllocateOutInOrderBase;
import org.trc.domain.allocateOrder.AllocateOutOrder;
import org.trc.domain.impower.AclUserAccreditInfo;
import org.trc.domain.warehouseInfo.WarehouseInfo;
import org.trc.enums.AllocateOrderEnum;
import org.trc.enums.ZeroToNineEnum;
import org.trc.service.allocateOrder.IAllocateInOrderService;
import org.trc.service.allocateOrder.IAllocateOrderExtService;
import org.trc.service.allocateOrder.IAllocateOutOrderService;
import org.trc.service.impower.IAclUserAccreditInfoService;
import org.trc.service.warehouseInfo.IWarehouseInfoService;
import org.trc.util.AssertUtil;
import org.trc.util.DateCheckUtil;
import org.trc.util.Pagenation;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

@Service("allocateOrderExtService")
public class AllocateOrderExtService implements IAllocateOrderExtService {

    @Autowired
    private IAclUserAccreditInfoService aclUserAccreditInfoService;
    @Autowired
    private IWarehouseInfoService warehouseInfoService;
    @Autowired
    private IAllocateOutOrderService allocateOutOrderService;
    @Autowired
    private IAllocateInOrderService allocateInOrderService;

    @Override
    public void setCreateOperator(String createOpertorName, Example.Criteria criteria) {
        Example example = new Example(AclUserAccreditInfo.class);
        Example.Criteria criteria2 = example.createCriteria();
        criteria2.andLike("name", "%" + createOpertorName + "%");
        List<AclUserAccreditInfo> aclUserAccreditInfoList = aclUserAccreditInfoService.selectByExample(example);
        if(!CollectionUtils.isEmpty(aclUserAccreditInfoList)){
            List<String> userIds = new ArrayList<>();
            for(AclUserAccreditInfo userAccreditInfo: aclUserAccreditInfoList){
                userIds.add(userAccreditInfo.getUserId());
            }
            criteria.andIn("createOperator", userIds);
        }
    }

    @Override
    public void setAllocateOrderOtherNames(Pagenation pagenation) {
        if(null == pagenation){
            return;
        }
        if(CollectionUtils.isEmpty(pagenation.getResult())){
            return;
        }
        List<AllocateOrderBase> allocateOrderBaseList = (List<AllocateOrderBase>)pagenation.getResult();
        Set<String> warehouseCodes = new HashSet<>();
        Set<String> operatorIds = new HashSet<>();
        for (AllocateOrderBase base : allocateOrderBaseList) {
            warehouseCodes.add(base.getInWarehouseCode());
            warehouseCodes.add(base.getOutWarehouseCode());
            operatorIds.add(base.getCreateOperator());
        }
        List<WarehouseInfo> warehouseInfoList = null;
        List<AclUserAccreditInfo> aclUserAccreditInfoList = null;
        if(warehouseCodes.size() > 0){
            Example example = new Example(WarehouseInfo.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andIn("code", warehouseCodes);
            warehouseInfoList = warehouseInfoService.selectByExample(example);
        }
        if(operatorIds.size() > 0){
            Example example = new Example(AclUserAccreditInfo.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andIn("userId", operatorIds);
            aclUserAccreditInfoList = aclUserAccreditInfoService.selectByExample(example);
        }
        for (AllocateOrderBase base : allocateOrderBaseList) {
            if(CollectionUtils.isNotEmpty(warehouseInfoList)){
                for(WarehouseInfo warehouseInfo: warehouseInfoList){
                    if(StringUtils.equals(base.getInWarehouseCode(), warehouseInfo.getCode())){
                        base.setInWarehouseName(warehouseInfo.getWarehouseName());
                        break;
                    }
                }
                for(WarehouseInfo warehouseInfo: warehouseInfoList){
                    if(StringUtils.equals(base.getOutWarehouseCode(), warehouseInfo.getCode())){
                        base.setOutWarehouseName(warehouseInfo.getWarehouseName());
                        break;
                    }
                }
            }
            if(CollectionUtils.isNotEmpty(aclUserAccreditInfoList)){
                for(AclUserAccreditInfo userAccreditInfo: aclUserAccreditInfoList){
                    if(StringUtils.equals(base.getCreateOperator(), userAccreditInfo.getUserId())){
                        base.setCreateOperatorName(userAccreditInfo.getName());
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void setIsTimeOut(Pagenation pagenation) {
        if(null == pagenation){
            return;
        }
        if(CollectionUtils.isEmpty(pagenation.getResult())){
            return;
        }
        List<AllocateOutInOrderBase> allocateOutInOrderBaseList = pagenation.getResult();

        for(AllocateOutInOrderBase allocateOutInOrderBase : allocateOutInOrderBaseList){
            if((StringUtils.equals(allocateOutInOrderBase.getIsCancel(), ZeroToNineEnum.ONE.getCode())
                    || StringUtils.equals(allocateOutInOrderBase.getIsClose(), ZeroToNineEnum.ONE.getCode())) &&
                    DateCheckUtil.checkDate(allocateOutInOrderBase.getUpdateTime())){
                allocateOutInOrderBase.setIsTimeOut(ZeroToNineEnum.ONE.getCode());
            }else if(StringUtils.equals(allocateOutInOrderBase.getStatus(), AllocateOrderEnum.AllocateOutOrderStatusEnum.CANCEL.getCode()) &&
                    StringUtils.equals(allocateOutInOrderBase.getIsCancel(), ZeroToNineEnum.ZERO.getCode()) &&
                    StringUtils.equals(allocateOutInOrderBase.getIsClose(), ZeroToNineEnum.ZERO.getCode())){
                allocateOutInOrderBase.setIsTimeOut(ZeroToNineEnum.ONE.getCode());
            }else{
                allocateOutInOrderBase.setIsTimeOut(ZeroToNineEnum.ZERO.getCode());
            }
        }
    }

    @Override
    public void updateOrderCancelInfo(AllocateOutInOrderBase allocateOutInOrderBase, String remark, boolean isClose, String status) {
        allocateOutInOrderBase.setOldtatus(allocateOutInOrderBase.getStatus());
        allocateOutInOrderBase.setStatus(status);
        if(isClose){
            allocateOutInOrderBase.setIsClose(ZeroToNineEnum.ONE.getCode());
        }else {
            allocateOutInOrderBase.setIsCancel(ZeroToNineEnum.ONE.getCode());
        }
        allocateOutInOrderBase.setUpdateTime(Calendar.getInstance().getTime());
        allocateOutInOrderBase.setMemo(remark);
        if(allocateOutInOrderBase instanceof AllocateOutOrder){
            allocateOutOrderService.updateByPrimaryKey((AllocateOutOrder)allocateOutInOrderBase);
        }else if(allocateOutInOrderBase instanceof AllocateInOrder){
            allocateInOrderService.updateByPrimaryKey((AllocateInOrder)allocateOutInOrderBase);
        }
    }

    @Override
    public void updateOrderCancelInfoExt(AllocateOutInOrderBase allocateOutInOrderBase, boolean isClose) {
        allocateOutInOrderBase.setStatus(allocateOutInOrderBase.getOldtatus());
        allocateOutInOrderBase.setOldtatus("");
        if(isClose){
            allocateOutInOrderBase.setIsClose(ZeroToNineEnum.ZERO.getCode());
        }else{
            allocateOutInOrderBase.setIsCancel(ZeroToNineEnum.ZERO.getCode());
        }
        allocateOutInOrderBase.setUpdateTime(Calendar.getInstance().getTime());
        allocateOutInOrderBase.setMemo("");
        if(allocateOutInOrderBase instanceof AllocateOutOrder){
            allocateOutOrderService.updateByPrimaryKey((AllocateOutOrder)allocateOutInOrderBase);
        }else if(allocateOutInOrderBase instanceof AllocateInOrder){
            allocateInOrderService.updateByPrimaryKey((AllocateInOrder)allocateOutInOrderBase);
        }
    }

}
