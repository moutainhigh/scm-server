package org.trc.biz.impl.system;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.trc.biz.system.IWarehouseBiz;
import org.trc.constants.SupplyConstants;
import org.trc.domain.impower.AclUserAccreditInfo;
import org.trc.domain.warehouseInfo.WarehouseInfo;
import org.trc.domain.warehouseInfo.WarehouseItemInfo;
import org.trc.enums.*;
import org.trc.exception.WarehouseException;
import org.trc.form.system.WarehouseForm;
import org.trc.form.warehouseInfo.WarehouseInfoResult;
import org.trc.model.SearchResult;
import org.trc.service.IPageNationService;
import org.trc.service.config.ILogInfoService;
import org.trc.service.util.ILocationUtilService;
import org.trc.service.util.ISerialUtilService;
import org.trc.service.util.IUserNameUtilService;
import org.trc.service.warehouseInfo.IWarehouseInfoService;
import org.trc.service.warehouseInfo.IWarehouseItemInfoService;
import org.trc.util.*;
import org.trc.util.cache.WarehouseCacheEvict;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by sone on 2017/5/5.
 */
@Service("warehouseBiz")
public class WarehouseBiz implements IWarehouseBiz {

    private Logger logger = LoggerFactory.getLogger(ChannelBiz.class);

    private final static String SERIALNAME = "CK";

    private final static Integer LENGTH = 5;

    /**
     * 正则表达式：验证手机号
     */
    private final static String REGEX_MOBILE = "^((13[0-9])|(15[^4])|(18[0,2,3,5-9])|(17[0-9])|(147))\\\\d{8}$";

    @Autowired
    private IUserNameUtilService userNameUtilService;

    @Autowired
    private ISerialUtilService serialUtilService;

    @Autowired
    private IPageNationService pageNationService;

    @Autowired
    private ILogInfoService logInfoService;

    @Autowired
    private ILocationUtilService locationUtilService;

    @Autowired
    private IWarehouseInfoService warehouseInfoService;

    @Autowired
    private IWarehouseItemInfoService warehouseItemInfoService;


    @Override
    @Cacheable(value = SupplyConstants.Cache.WAREHOUSE)
    public Pagenation<WarehouseInfoResult> warehousePage(WarehouseForm form, Pagenation<WarehouseInfo> page) {
        AssertUtil.notNull(page.getPageNo(),"分页查询参数pageNo不能为空");
        AssertUtil.notNull(page.getPageSize(),"分页查询参数pageSize不能为空");
        AssertUtil.notNull(page.getStart(),"分页查询参数start不能为空");

        logger.info("开始查询符合条件的仓库信息===========》");

        Example example = new Example(WarehouseInfo.class);
        Example.Criteria criteria = example.createCriteria();
        if(!com.alibaba.dubbo.common.utils.StringUtils.isBlank(form.getName())){
            criteria.andLike("warehouseName","%"+form.getName()+"%");
        }
        if (!StringUtils.isBlank(form.getIsValid())) {
            criteria.andEqualTo("isValid", form.getIsValid());
        }
        if (!StringUtils.isBlank(form.getOperationalNature())) {
            criteria.andEqualTo("operationalNature", form.getOperationalNature());
        }
        if (!StringUtils.isBlank(form.getOperationalType())) {
            criteria.andEqualTo("operationalType", form.getOperationalType());
        }
        example.orderBy("updateTime").desc();
        Pagenation<WarehouseInfo> pagenation = warehouseInfoService.pagination(example,page,form);

        logger.info("《==========查询结束，开始组装返回结果");

        List<WarehouseInfo> list = pagenation.getResult();
        userNameUtilService.handleUserName(list);
        List<WarehouseInfoResult> newList = new ArrayList<>();
        for (WarehouseInfo warehouseInfo:list){
            WarehouseInfoResult result = new WarehouseInfoResult();
            result.setId(warehouseInfo.getId());
            result.setWarehouseName(warehouseInfo.getWarehouseName());
            result.setWarehouseTypeCode(warehouseInfo.getWarehouseTypeCode());
            result.setWmsWarehouseCode(warehouseInfo.getWmsWarehouseCode());
            result.setSkuNum(warehouseInfo.getSkuNum());
            String state = convertWarehouseState(warehouseInfo.getOwnerWarehouseState());
            result.setOwnerWarehouseState(state);
            Integer noticeSuccess = warehouseInfo.getIsNoticeSuccess();
            if (noticeSuccess == null){
                noticeSuccess=0;
            }
            result.setIsNoticeWarehouseItems(warehouseInfo.getIsNoticeWarehouseItems()!=null?warehouseInfo.getIsNoticeWarehouseItems():"");
            result.setIsNoticeSuccess(noticeSuccess);
            result.setIsSupportReturn(warehouseInfo.getIsSupportReturn());
            result.setCreateTime(DateUtils.formatDateTime(warehouseInfo.getCreateTime()));
            result.setUpdateTime(DateUtils.formatDateTime(warehouseInfo.getUpdateTime()));
            result.setIsDelete(convertDeleteState(warehouseInfo));
            result.setOwnerId(warehouseInfo.getChannelCode());
            result.setOwnerName(warehouseInfo.getOwnerName());
            result.setWarehouseOwnerId(warehouseInfo.getWarehouseOwnerId()==null?"":warehouseInfo.getWarehouseOwnerId());
            result.setCreateOperator(warehouseInfo.getCreateOperator());
            result.setCode(warehouseInfo.getCode());
            result.setIsValid(warehouseInfo.getIsValid());
            result.setRemark(warehouseInfo.getRemark()==null?"":warehouseInfo.getRemark());
            result.setOperationalNature(warehouseInfo.getOperationalNature());
            result.setOperationalType(warehouseInfo.getOperationalType());
            newList.add(result);
        }

        Pagenation<WarehouseInfoResult> resultPagenation = new Pagenation<>();
        resultPagenation.setResult(newList);
        resultPagenation.setPageNo(pagenation.getPageNo());
        resultPagenation.setPageSize(pagenation.getPageSize());
        resultPagenation.setTotalCount(pagenation.getTotalCount());
        resultPagenation.setStart(pagenation.getStart());

        logger.info("组装数据完成《=============");

        return resultPagenation;
    }

    private Integer convertDeleteState(WarehouseInfo warehouseInfo){
        Integer count = 0;
        if (com.alibaba.dubbo.common.utils.StringUtils.isEquals(warehouseInfo.getOwnerWarehouseState(), ZeroToNineEnum.ZERO.getCode()) ||
                com.alibaba.dubbo.common.utils.StringUtils.isEquals(warehouseInfo.getOwnerWarehouseState(), ZeroToNineEnum.TWO.getCode())){
            count = 1;
        }
        if (warehouseInfo.getSkuNum()==null || warehouseInfo.getSkuNum() == 0 ){
            count = 1;
        }
        return count;
    }

    private String convertWarehouseState(String ownerWarehouseState) {
        String state = null;
        if(com.alibaba.dubbo.common.utils.StringUtils.isBlank(ownerWarehouseState)){
            state = "通知仓库状态为空";
        } else if (com.alibaba.dubbo.common.utils.StringUtils.isEquals(ownerWarehouseState, ZeroToNineEnum.ZERO.getCode())){
            state = "待通知";
        }else if (com.alibaba.dubbo.common.utils.StringUtils.isEquals(ownerWarehouseState, ZeroToNineEnum.ONE.getCode())){
            state = "通知成功";
        }else if (com.alibaba.dubbo.common.utils.StringUtils.isEquals(ownerWarehouseState, ZeroToNineEnum.TWO.getCode())){
            state = "通知失败";
        }else if (com.alibaba.dubbo.common.utils.StringUtils.isEquals(ownerWarehouseState, ZeroToNineEnum.THREE.getCode())){
            state = "通知中";
        }
        return state;
    }


//    private void handleAreaName(List<WarehouseInfoResult> warehouses){
//        for (WarehouseInfoResult warehouse : warehouses) {
//            StringBuffer allAreaName = new StringBuffer();
//            Area privinceArea = new Area();
//            privinceArea.setCode(warehouse.getProvince());
//            privinceArea = locationUtilService.selectOne(privinceArea);
//            AssertUtil.notNull(privinceArea.getProvince(),"数据库查询失败!");
//            allAreaName.append(privinceArea.getProvince());
//            Area cityArea = new Area();
//            cityArea.setCode(warehouse.getCity());
//            cityArea = locationUtilService.selectOne(cityArea);
//            AssertUtil.notNull(cityArea.getCity(),"数据库查询失败!");
//            allAreaName.append("."+cityArea.getCity());
//            if(StringUtils.isNotBlank(cityArea.getDistrict())){
//                warehouse.setAllAreaName(allAreaName.toString());
//                continue;
//            }
//            Area districtArea = new Area();
//            districtArea.setCode(warehouse.getArea());
//            districtArea = locationUtilService.selectOne(districtArea);
//            AssertUtil.notNull(districtArea.getDistrict(),"数据库查询失败!");
//            allAreaName.append("."+districtArea.getDistrict());
//            warehouse.setAllAreaName(allAreaName.toString());
//        }
//
//    }


    @Override
    public Pagenation<WarehouseInfo> warehousePageEs(WarehouseForm queryModel, Pagenation<WarehouseInfo> page) {

        TransportClient clientUtil = TransportClientUtil.getTransportClient();
        HighlightBuilder hiBuilder = new HighlightBuilder();
        hiBuilder.preTags("<b style=\"color: red\">");
        hiBuilder.postTags("</b>");
        hiBuilder.field("name.pinyin");//http://172.30.250.164:9100/ 模糊字段可在这里找到
        SearchRequestBuilder srb = clientUtil.prepareSearch("warehouse")//es表名
                .highlighter(hiBuilder).addSort(SortBuilders.fieldSort("update_time").order(SortOrder.DESC))
                .setFrom(page.getStart())//第几个开始
                .setSize(page.getPageSize());//长度
        if (StringUtils.isNotBlank(queryModel.getName())) {
            QueryBuilder matchQuery = QueryBuilders.matchQuery("name.pinyin", queryModel.getName());
            srb.setQuery(matchQuery);
        }
        if (!StringUtils.isBlank(queryModel.getIsValid())) {
            QueryBuilder filterBuilder = QueryBuilders.termQuery("is_valid", queryModel.getIsValid());
            srb.setPostFilter(filterBuilder);
        }
        SearchResult searchResult;
        try {
            searchResult = pageNationService.resultES(srb, clientUtil);
        } catch (Exception e) {
            logger.error("es查询失败" + e.getMessage(), e);
            return page;
        }
        List<WarehouseInfo> warehouseList = new ArrayList<>();
        for (SearchHit searchHit : searchResult.getSearchHits()) {
            WarehouseInfo warehouse = JSON.parseObject(JSON.toJSONString(searchHit.getSource()), WarehouseInfo.class);
            if (StringUtils.isNotBlank(queryModel.getName())) {
                for (Text text : searchHit.getHighlightFields().get("name.pinyin").getFragments()) {
                    warehouse.setHighLightName(text.string());
                }
            }
            warehouseList.add(warehouse);
        }
        if (AssertUtil.collectionIsEmpty(warehouseList)) {
            return page;
        }
        page.setResult(warehouseList);
        userNameUtilService.handleUserName(page.getResult());
        page.setTotalCount(searchResult.getCount());
        return page;
    }

    @Override
    @WarehouseCacheEvict
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void updateWarehouseConfig(WarehouseInfo warehouse) {
        AssertUtil.notNull(warehouse, "修改仓库配置失败，仓库信息为空");
        AssertUtil.notNull(warehouse.getId(), "根据ID修改仓库参数ID为空");

        warehouse.setUpdateTime(Calendar.getInstance().getTime());
        WarehouseInfo _warehouse = warehouseInfoService.selectByPrimaryKey(warehouse.getId());
        AssertUtil.notNull(_warehouse, "根据id查询仓库为空");

        if(StringUtils.equals(ZeroToNineEnum.ONE.getCode(), _warehouse.getOperationalNature())){
            String msg = "仓库为自营仓不允许修改！" ;
            logger.error(msg);
            throw new WarehouseException(ExceptionEnum.SYSTEM_WAREHOUSE_SAVE_EXCEPTION, msg);
        }

        if(warehouse.getIsThroughWms() == Integer.parseInt(ZeroToNineEnum.ONE.getCode()) &&
                StringUtils.isEmpty(warehouse.getWmsWarehouseCode())){
            String msg = "奇门仓库编码不能为空！" ;
            logger.error(msg);
            throw new WarehouseException(ExceptionEnum.SYSTEM_WAREHOUSE_SAVE_EXCEPTION, msg);
        }

        if(warehouse.getIsThroughWms() == Integer.parseInt(ZeroToNineEnum.ONE.getCode()) &&
                StringUtils.isNoneEmpty(warehouse.getWmsWarehouseCode()) &&
                !this.checkQimenWarehouseCode(warehouse.getWmsWarehouseCode(), warehouse.getId())){
            String msg = "奇门仓库编码重复," + warehouse.getWmsWarehouseCode();
            logger.error(msg);
            throw new WarehouseException(ExceptionEnum.SYSTEM_WAREHOUSE_SAVE_EXCEPTION, msg);
        }

//        if(warehouse.getIsThroughWms() == Integer.parseInt(ZeroToNineEnum.ZERO.getCode())){
//            warehouse.setWmsWarehouseCode("");
////            warehouse.setIsNoticeSuccess(Integer.parseInt(ZeroToNineEnum.ZERO.getCode()));
////            warehouse.setIsNoticeWarehouseItems(ZeroToNineEnum.ZERO.getCode());
//        }

        int count = warehouseInfoService.updateByPrimaryKeySelective(warehouse);

        if (count == 0) {
            String msg = String.format("修改仓库%s数据库操作失败", JSON.toJSONString(warehouse));
            logger.error(msg);
            throw new WarehouseException(ExceptionEnum.SYSTEM_WAREHOUSE_UPDATE_EXCEPTION, msg);
        }

        //更新仓库商品信息
        WarehouseItemInfo warehouseItemInfoTemp = new WarehouseItemInfo();
        warehouseItemInfoTemp.setWmsWarehouseCode(warehouse.getWmsWarehouseCode());
        Example example = new Example(WarehouseItemInfo.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("warehouseInfoId", warehouse.getId());
        criteria.andEqualTo("isDelete", ZeroToNineEnum.ZERO.getCode());
        warehouseItemInfoService.updateByExampleSelective(warehouseItemInfoTemp, example);

    }

    @Override
    @Cacheable(value = SupplyConstants.Cache.WAREHOUSE)
    public List<WarehouseInfo> findWarehouseValid() {
        WarehouseInfo warehouse = new WarehouseInfo();
        warehouse.setIsValid(ValidEnum.VALID.getCode());
        List<WarehouseInfo> warehouseList = warehouseInfoService.select(warehouse);
        if (warehouseList == null) {
            warehouseList = new ArrayList<WarehouseInfo>();
        }
        return warehouseList;
    }

    @Override
    @Cacheable(value = SupplyConstants.Cache.WAREHOUSE)
    public List<WarehouseInfo> findWarehouse(boolean isValid) {
        WarehouseInfo warehouse = new WarehouseInfo();
        if(isValid){
            warehouse.setOperationalNature(ZeroToNineEnum.ZERO.getCode());
        }
        List<WarehouseInfo> warehouseList = warehouseInfoService.select(warehouse);
        if (warehouseList == null) {
            warehouseList = new ArrayList<>();
        }
        return warehouseList;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @WarehouseCacheEvict
    public void saveWarehouse(WarehouseInfo warehouse, AclUserAccreditInfo aclUserAccreditInfo) {
        AssertUtil.notNull(warehouse, "仓库管理模块保存仓库信息失败，仓库信息为空");
        if (StringUtils.equals(warehouse.getOperationalNature(),OperationalNatureEnum.SELF_SUPPORT.getCode())){
            AssertUtil.notNull(warehouse.getIsSupportReturn(),"仓库运营类型为自营时,是否支持退货字段不能为空");
        }
        WarehouseInfo tmp = findWarehouseByName(warehouse.getWarehouseName());
        AssertUtil.isNull(tmp, String.format("仓库名称[name=%s]的数据已存在,请使用其他名称", warehouse.getWarehouseName()));
        ParamsUtil.setBaseDO(warehouse);
        String warehouseCode = serialUtilService.generateCode(LENGTH, SERIALNAME);
        warehouse.setCode(warehouseCode);
        /*
        校验如果是保税仓，必须要是否支持清关的数据<否则，为不合理的，或者非法提交>
        如果是其它仓，不能接受是否支持清关的数据
         */
        String strs = warehouse.getWarehouseTypeCode();
        AssertUtil.notBlank(strs,"仓库类型为空");
        if(strs.equals("bondedWarehouse")){
            AssertUtil.notNull(warehouse.getIsCustomsClearance(),"仓库类型为保税仓,是否支持清关不能为空");
        }
        if (Pattern.matches(REGEX_MOBILE, warehouse.getWarehouseContactNumber())) {
            String msg = "仓库联系手机号格式错误," + warehouse.getWarehouseContactNumber();
            logger.error(msg);
            throw new WarehouseException(ExceptionEnum.SYSTEM_WAREHOUSE_SAVE_EXCEPTION, msg);
        }
        if (Pattern.matches(REGEX_MOBILE, warehouse.getSenderPhoneNumber())) {
            String msg = "运单发件人手机号格式错误," + warehouse.getSenderPhoneNumber();
            logger.error(msg);
            throw new WarehouseException(ExceptionEnum.SYSTEM_WAREHOUSE_SAVE_EXCEPTION, msg);
        }

        //校验运行性质字段是否符合要求
        String operationalNature = warehouse.getOperationalNature();
        String operationalType = warehouse.getOperationalType();
        String storeCorrespondChannel = warehouse.getStoreCorrespondChannel();
        if(StringUtils.isEmpty(operationalNature)){
            String msg = "运营性质不能为空";
            logger.error(msg);
            throw new WarehouseException(ExceptionEnum.SYSTEM_WAREHOUSE_SAVE_EXCEPTION, msg);
        }

        if(StringUtils.equals(OperationalNatureEnum.SELF_SUPPORT.getCode(), operationalNature)){
            if(StringUtils.isEmpty(operationalType)){
                String msg = "运营类型不能为空";
                logger.error(msg);
                throw new WarehouseException(ExceptionEnum.SYSTEM_WAREHOUSE_SAVE_EXCEPTION, msg);
            }
            warehouse.setWmsWarehouseCode(warehouseCode);
            if(!StringUtils.equals(OperationalTypeEnum.ONLY_WAREHOUSE.getCode(), operationalType)){
                if(StringUtils.isEmpty(storeCorrespondChannel)){
                    String msg = "门店仓对应销售渠道不能为空";
                    logger.error(msg);
                    throw new WarehouseException(ExceptionEnum.SYSTEM_WAREHOUSE_SAVE_EXCEPTION, msg);
                }

                if(StringUtils.equals(ZeroToNineEnum.ONE.getCode(), warehouse.getIsValid())){
                    WarehouseInfo warehouseInfoTemp = new WarehouseInfo();
                    warehouseInfoTemp.setStoreCorrespondChannel(storeCorrespondChannel);
                    warehouseInfoTemp.setIsValid(ZeroToNineEnum.ONE.getCode());
                    List<WarehouseInfo> warehouseInfoList =  warehouseInfoService.select(warehouseInfoTemp);
                    if(warehouseInfoList != null && warehouseInfoList.size() > 0){
                        String msg = "该销售渠道已对应相应的门店!";
                        logger.error(msg);
                        throw new WarehouseException(ExceptionEnum.SYSTEM_WAREHOUSE_SAVE_EXCEPTION, msg);
                    }
                }
            }
            warehouse.setOwnerWarehouseState(ZeroToNineEnum.ONE.getCode());
        }else{
            warehouse.setOwnerWarehouseState(ZeroToNineEnum.ZERO.getCode());
        }

        warehouse.setIsThroughWms(Integer.parseInt(ZeroToNineEnum.ZERO.getCode()));
        warehouse.setChannelCode(SupplyConstants.WarehouseConstant.CHANNEL_CODE);
        warehouse.setOwnerName(SupplyConstants.WarehouseConstant.OWNER_NAME);
        warehouse.setSkuNum(0);


        int count = warehouseInfoService.insert(warehouse);
        if (count == 0) {
            String msg = "仓库保存,数据库操作失败";
            logger.error(msg);
            throw new WarehouseException(ExceptionEnum.SYSTEM_WAREHOUSE_SAVE_EXCEPTION, msg);
        }
        String userId = aclUserAccreditInfo.getUserId();
        logInfoService.recordLog(warehouse, warehouse.getId().toString(), userId, LogOperationEnum.ADD.getMessage(), null, null);

    }



    private boolean checkQimenWarehouseCode(String qimenWarehouseCode, Long id){
        Example example = new Example(WarehouseInfo.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("wmsWarehouseCode",qimenWarehouseCode);
        List<WarehouseInfo> list = warehouseInfoService.selectByExample(example);
        if(list == null || list.size() < 1 ||
            (list.size() == 1 && list.get(0).getId() == id)){
            return true;
        }else{
            return false;
        }
    }

    @Override
    @Cacheable(value = SupplyConstants.Cache.WAREHOUSE)
    public WarehouseInfo findWarehouseByName(String name) {

        AssertUtil.notBlank(name, "根据渠道名称查询渠道的参数name为空");
        WarehouseInfo warehouse = new WarehouseInfo();
        warehouse.setWarehouseName(name);
        return warehouseInfoService.selectOne(warehouse);

    }

    @Override
    @WarehouseCacheEvict
    public void updateWarehouseState(WarehouseInfo warehouse, AclUserAccreditInfo aclUserAccreditInfo) {

        AssertUtil.notNull(warehouse, "仓库管理模块修改仓库信息失败，仓库信息为空");
        WarehouseInfo updateWarehouse = new WarehouseInfo();
        updateWarehouse.setId(warehouse.getId());
        String remark = null;

        WarehouseInfo _warehouseInfo = warehouseInfoService.selectByPrimaryKey(warehouse.getId());

        //校验运行性质字段是否符合要求
        String operationalType = _warehouseInfo.getOperationalType();
        String operationalNature = _warehouseInfo.getOperationalNature();
        String storeCorrespondChannel = _warehouseInfo.getStoreCorrespondChannel();
        if(!StringUtils.equals(OperationalTypeEnum.ONLY_WAREHOUSE.getCode(), operationalType) &&
                StringUtils.equals(operationalNature, OperationalNatureEnum.SELF_SUPPORT.getCode().toString())){
            if(StringUtils.equals(ValidEnum.NOVALID.getCode(), _warehouseInfo.getIsValid())){
                Example example = new Example(WarehouseInfo.class);
                Example.Criteria criteria = example.createCriteria();
                criteria.andEqualTo("storeCorrespondChannel", storeCorrespondChannel);
                criteria.andNotEqualTo("code", _warehouseInfo.getCode());
                criteria.andEqualTo("isValid", ZeroToNineEnum.ONE.getCode());
                List<WarehouseInfo> warehouseInfoList =  warehouseInfoService.selectByExample(example);

                if(warehouseInfoList != null && warehouseInfoList.size() > 0){
                    String msg = "该销售渠道已对应相应的门店!";
                    logger.error(msg);
                    throw new WarehouseException(ExceptionEnum.SYSTEM_WAREHOUSE_UPDATE_EXCEPTION, msg);
                }
            }
        }

        if (warehouse.getIsValid().equals(ValidEnum.VALID.getCode())) {
            updateWarehouse.setIsValid(ValidEnum.NOVALID.getCode());
            remark = remarkEnum.VALID_OFF.getMessage();
        } else {
            updateWarehouse.setIsValid(ValidEnum.VALID.getCode());
        }
        updateWarehouse.setUpdateTime(Calendar.getInstance().getTime());
        int count = warehouseInfoService.updateByPrimaryKeySelective(updateWarehouse);
        if (count == 0) {
            String msg = String.format("修改仓库%s数据库操作失败", JSON.toJSONString(warehouse));
            logger.error(msg);
            throw new WarehouseException(ExceptionEnum.SYSTEM_WAREHOUSE_UPDATE_EXCEPTION, msg);
        }
        String userId = aclUserAccreditInfo.getUserId();
        logInfoService.recordLog(warehouse, warehouse.getId().toString(), userId, LogOperationEnum.UPDATE.getMessage(), remark, null);

    }

    @Override
    @Cacheable(value = SupplyConstants.Cache.WAREHOUSE)
    public WarehouseInfo findWarehouseById(Long id) {

        AssertUtil.notNull(id, "根据ID查询仓库参数ID为空");
        WarehouseInfo warehouse = new WarehouseInfo();
        warehouse.setId(id);
        warehouse = warehouseInfoService.selectOne(warehouse);
        AssertUtil.notNull(warehouse, String.format("根据主键ID[id=%s]查询仓库为空", id.toString()));
        return warehouse;

    }

    @Override
    @WarehouseCacheEvict
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void updateWarehouse(WarehouseInfo warehouse, AclUserAccreditInfo aclUserAccreditInfo) {
        AssertUtil.notNull(warehouse.getId(), "根据ID修改仓库参数ID为空");
        if (StringUtils.equals(warehouse.getOperationalNature(),OperationalNatureEnum.SELF_SUPPORT.getCode())){
            AssertUtil.notNull(warehouse.getIsSupportReturn(),"仓库运营类型为自营时,是否支持退货字段不能为空");
        }
        WarehouseInfo tmp = findWarehouseByName(warehouse.getWarehouseName());
        if (tmp != null) {
            if (!tmp.getId().equals(warehouse.getId())) {
                throw new WarehouseException(ExceptionEnum.SYSTEM_WAREHOUSE_UPDATE_EXCEPTION, "其它的仓库已经使用该仓库名称");
            }
        }

        warehouse.setUpdateTime(Calendar.getInstance().getTime());
        WarehouseInfo _warehouse = warehouseInfoService.selectByPrimaryKey(warehouse.getId());
        String remark = null;
        AssertUtil.notNull(_warehouse, "根据id查询仓库为空");

        //校验运行性质字段是否符合要求
        String operationalType = warehouse.getOperationalType();
        String storeCorrespondChannel = warehouse.getStoreCorrespondChannel();
        if(!StringUtils.equals(OperationalTypeEnum.ONLY_WAREHOUSE.getCode(), operationalType)){
            if(StringUtils.isEmpty(storeCorrespondChannel)){
                String msg = "门店仓对应销售渠道不能为空";
                logger.error(msg);
                throw new WarehouseException(ExceptionEnum.SYSTEM_WAREHOUSE_SAVE_EXCEPTION, msg);
            }

            if(StringUtils.equals(ZeroToNineEnum.ONE.getCode(), warehouse.getIsValid())){
                Example example = new Example(WarehouseInfo.class);
                Example.Criteria criteria = example.createCriteria();
                criteria.andEqualTo("storeCorrespondChannel", storeCorrespondChannel);
                criteria.andNotEqualTo("code", warehouse.getCode());
                criteria.andEqualTo("isValid", ZeroToNineEnum.ONE.getCode());
                List<WarehouseInfo> warehouseInfoList =  warehouseInfoService.selectByExample(example);

                if(warehouseInfoList != null && warehouseInfoList.size() > 0){
                    String msg = "该销售渠道已对应相应的门店!";
                    logger.error(msg);
                    throw new WarehouseException(ExceptionEnum.SYSTEM_WAREHOUSE_SAVE_EXCEPTION, msg);
                }
            }
        }

        int count = warehouseInfoService.updateByPrimaryKeySelective(warehouse);
        if (count == 0) {
            String msg = String.format("修改仓库%s数据库操作失败", JSON.toJSONString(warehouse));
            logger.error(msg);
            throw new WarehouseException(ExceptionEnum.SYSTEM_WAREHOUSE_UPDATE_EXCEPTION, msg);
        }
        if (!_warehouse.getIsValid().equals(warehouse.getIsValid())) {
            if (warehouse.getIsValid().equals(ValidEnum.VALID.getCode())) {
                remark = remarkEnum.VALID_ON.getMessage();
            } else {
                remark = remarkEnum.VALID_OFF.getMessage();
            }
        }

        String userId = aclUserAccreditInfo.getUserId();
        logInfoService.recordLog(warehouse, warehouse.getId().toString(), userId, LogOperationEnum.UPDATE.getMessage(), remark, null);

    }

}
