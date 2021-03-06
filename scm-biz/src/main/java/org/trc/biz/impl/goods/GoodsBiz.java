package org.trc.biz.impl.goods;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.trc.biz.category.ICategoryBiz;
import org.trc.biz.goods.IGoodsBiz;
import org.trc.biz.impl.config.LogInfoBiz;
import org.trc.biz.qinniu.IQinniuBiz;
import org.trc.biz.trc.ITrcBiz;
import org.trc.constants.SupplyConstants;
import org.trc.domain.category.*;
import org.trc.domain.dict.Dict;
import org.trc.domain.goods.*;
import org.trc.domain.impower.AclUserAccreditInfo;
import org.trc.domain.purchase.PurchaseDetail;
import org.trc.domain.supplier.Supplier;
import org.trc.domain.supplier.SupplierApply;
import org.trc.domain.warehouseInfo.WarehouseInfo;
import org.trc.domain.warehouseInfo.WarehouseItemInfo;
import org.trc.domain.wms.WmsItemInfo;
import org.trc.enums.*;
import org.trc.exception.GoodsException;
import org.trc.exception.ParamValidException;
import org.trc.exception.UserAccreditInfoException;
import org.trc.form.JDModel.ExternalSupplierConfig;
import org.trc.form.JDModel.ReturnTypeDO;
import org.trc.form.JDModel.SkuDO;
import org.trc.form.JDModel.SupplyItemsForm;
import org.trc.form.SupplyItemsExt;
import org.trc.form.goods.ExternalItemSkuForm;
import org.trc.form.goods.ItemsExt;
import org.trc.form.goods.ItemsForm;
import org.trc.form.goods.SkusForm;
import org.trc.form.supplier.SupplierForm;
import org.trc.form.warehouse.ScmInventoryQueryResponse;
import org.trc.form.warehouse.ScmItemSyncRequest;
import org.trc.form.warehouse.ScmItemSyncResponse;
import org.trc.form.warehouse.ScmWarehouseItem;
import org.trc.model.ToGlyResultDO;
import org.trc.service.IJDService;
import org.trc.service.category.*;
import org.trc.service.config.IDictService;
import org.trc.service.config.ILogInfoService;
import org.trc.service.goods.*;
import org.trc.service.impl.goods.ItemNatureProperyService;
import org.trc.service.impl.goods.ItemSalesProperyService;
import org.trc.service.impl.impower.AclUserAccreditInfoService;
import org.trc.service.purchase.IPurchaseDetailService;
import org.trc.service.supplier.ISupplierApplyService;
import org.trc.service.supplier.ISupplierService;
import org.trc.service.util.ISerialUtilService;
import org.trc.service.warehouse.IWarehouseApiService;
import org.trc.service.warehouse.IWarehouseExtService;
import org.trc.service.warehouseInfo.IWarehouseInfoService;
import org.trc.service.warehouseInfo.IWarehouseItemInfoService;
import org.trc.service.wms.IWmsItemInfoService;
import org.trc.util.*;
import org.trc.util.cache.GoodsCacheEvict;
import org.trc.util.cache.OutGoodsCacheEvict;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.util.StringUtil;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by hzwdx on 2017/5/24.
 */
@Service("goodsBiz")
public class GoodsBiz implements IGoodsBiz {

    private Logger log = LoggerFactory.getLogger(GoodsBiz.class);

    //线程池线程数量
    private final static int EXECUTOR_SIZE = 10;

    //分类ID全路径分割符号
    public static final String CATEGORY_ID_SPLIT_SYMBOL = "|";
    //分类名称全路径分割符号
    public static final String CATEGORY_NAME_SPLIT_SYMBOL = "-";
    //SKU的属性值ID分割符号
    public static final String SKU_PROPERTY_VALUE_ID_SPLIT_SYMBOL = ",";
    //SKU的属性组合名称分割符号
    public static final String SKU_PROPERTY_COMBINE_NAME_SPLIT_SYMBOL = ":";
    //换行符号
    public static final String SWITCH_LINE = "<br>";
    //SKU的属性组合名称空格
    public static final String SKU_PROPERTY_COMBINE_NAME_EMPTY = "&nbsp&nbsp&nbsp";
    //自然属性
    public static final String NATURE_PROPERTY = "natureProperty";
    //采购属性
    public static final String PURCHASE_PROPERTY = "purchaseProperty";
    //自然属性名称
    private static final String NATURE_PROPERTY_NAME = "自然属性";
    //采购属性名称
    private static final String PURCHASE_PROPERTY_NAME = "采购属性";
    //供应商京东编码
    public static final String JD_SUPPLIER_CODE = "JD";
    //供应商粮油编码
    public static final String LY_SUPPLIER_CODE = "LY";
    //代发商品名称中替换&符号的字符串
    private static final String AND_QUOT_REPLACE = "__111__222__";
    //代发商品图片七牛路径
    public static final String EXTERNAL_QINNIU_PATH = "external/";
    //京东原图路径
    public static final String JING_DONG_PIC_N_12 = "n12/";
    //逗号分隔的正则
    private static final String COMMA_SPLIT = "\\,{2,}";
    //条码正则
    private static final String CODE_CHECK = "^[\\da-zA-Z!#-]*$";
    //-
    private final static String BAR = "-";
    //EXCEL
    private final static String EXCEL = ".xls";


    @Autowired
    private IItemsService itemsService;
    @Autowired
    private IBrandService brandService;
    @Autowired
    private ICategoryService categoryService;
    @Autowired
    private ISerialUtilService serialUtilService;
    @Autowired
    private ISkusService skusService;
    @Autowired
    private ItemNatureProperyService itemNatureProperyService;
    @Autowired
    private ItemSalesProperyService itemSalesProperyService;
    @Autowired
    private ICategoryBiz categoryBiz;
    @Autowired
    private IPropertyService propertyService;
    @Autowired
    private ISkuStockService skuStockService;
    @Autowired
    private IPurchaseDetailService iPurchaseDetailService;
    @Autowired
    private IPropertyValueService propertyValueService;
    @Autowired
    private ICategoryPropertyService categoryPropertyService;
    @Autowired
    private ICategoryBrandService categoryBrandService;
    @Autowired
    private IExternalItemSkuService externalItemSkuService;
    @Autowired
    private IJDService jdService;
    @Autowired
    private ExternalSupplierConfig externalSupplierConfig;
    @Autowired
    private ITrcBiz trcBiz;
    @Autowired
    private ILogInfoService logInfoService;
    @Autowired
    private ISupplierService supplierService;
    @Autowired
    private ISupplierApplyService supplierApplyService;
    @Autowired
    private IWarehouseItemInfoService warehouseItemInfoService;
    @Autowired
    private IQinniuBiz qinniuBiz;
    @Autowired
    private IExternalPictureService externalPictureService;
    @Autowired
    private IWarehouseApiService warehouseApiService;
    @Autowired
    private IWarehouseInfoService warehouseInfoService;
    @Autowired
    private IWmsItemInfoService wmsItemInfoService;
    @Autowired
    private IWarehouseExtService warehouseExtService;
    @Autowired
    private IBusiItemsService busiItemsService;
    @Autowired
    private IBusiSkusService busiSkusService;

    @Autowired
    private IItemGroupUserService itemGroupUserService;
    @Autowired
    private AclUserAccreditInfoService aclUserAccreditInfoService;
    @Autowired
    private IDictService dictService;

    //EXCEL
    private final static String TRADE_TYPE = "tradeType";


    @Override
    /*@Cacheable(value = SupplyConstants.Cache.GOODS)*/
    public Pagenation<Items> itemsPage(ItemsForm queryModel, Pagenation<Items> page, AclUserAccreditInfo aclUserAccreditInfo) throws Exception {
        Example example = new Example(Items.class);
        Example.Criteria criteria = example.createCriteria();
        if (StringUtil.isNotEmpty(queryModel.getName())) {//商品名称
            criteria.andLike("name", "%" + queryModel.getName() + "%");
        }
        Map<String, Object> map = handlerSkuCondition(queryModel.getSkuCode(), queryModel.getSpuCode(), queryModel.getBarCode());
        Object spuCodes = map.get("spuCodes");
        if (null != spuCodes) {//根据SPU或者SKU来查询
            List<String> _spuCodes = (List<String>) spuCodes;
            if (_spuCodes.size() == 0) {
                return page;
            } else {
                criteria.andIn("spuCode", _spuCodes);
            }
        }
        if (null != queryModel.getCategoryId()) {
            categoryFrom(criteria, queryModel.getCategoryLevel(), queryModel.getCategoryId());
        }
        if (null != queryModel.getBrandId()) {//商品所属品牌ID
            criteria.andEqualTo("brandId", queryModel.getBrandId());
        }
        if (StringUtil.isNotEmpty(queryModel.getIsValid())) {
            criteria.andEqualTo("isValid", queryModel.getIsValid());
        }
        example.orderBy("updateTime").desc();
        page = itemsService.pagination(example, page, queryModel);
        List<Items> result = page.getResult();
        for (Items items : result) {
            String flag = selectDataAcl(items.getId(), aclUserAccreditInfo, true);
            items.setUpdateAuth(flag);//V3.1数据权限0无，1有
        }
        List<Skus> skusList = null;
        if (null != map.get("skuList")) {
            skusList = (List<Skus>) map.get("skuList");
        }
        handerPage(page, skusList);
        //分页查询
        return page;
    }

    private void categoryFrom(Example.Criteria criteria, String categoryLevel, Long categoryId) {
        AssertUtil.notBlank(categoryLevel, "根据分类条件查询时,当前选择的分类等级不能为空!");
        switch (categoryLevel) {
            case "1":
                Category categoryLevel2 = new Category();
                categoryLevel2.setParentId(categoryId);
                List<Category> categoryListLevel2 = categoryService.select(categoryLevel2);
                List<Long> categoryLevel2Ids = new ArrayList<>();
                List<Long> categoryLevel3Ids = new ArrayList<>();
                if (!AssertUtil.collectionIsEmpty(categoryListLevel2)) {
                    for (Category c : categoryListLevel2) {
                        categoryLevel2Ids.add(c.getId());
                    }
                    Example exampleLevel = new Example(Category.class);
                    Example.Criteria criteriaLevel = exampleLevel.createCriteria();
                    criteriaLevel.andIn("parentId", categoryLevel2Ids);
                    List<Category> categoryLevel3List = categoryService.selectByExample(exampleLevel);
                    if (!AssertUtil.collectionIsEmpty(categoryLevel3List)) {
                        for (Category c : categoryLevel3List) {
                            categoryLevel3Ids.add(c.getId());
                        }
                        criteria.andIn("categoryId", categoryLevel3Ids);
                    } else {
                        criteria.andEqualTo("categoryId", categoryId);
                    }
                } else {
                    criteria.andEqualTo("categoryId", categoryId);
                }
                break;
            case "2":
                Category category = new Category();
                category.setParentId(categoryId);
                List<Category> categoryList = categoryService.select(category);
                List<Long> categoryIds = new ArrayList<>();
                if (!AssertUtil.collectionIsEmpty(categoryList)) {
                    for (Category c : categoryList) {
                        categoryIds.add(c.getId());
                    }
                    criteria.andIn("categoryId", categoryIds);
                } else {
                    criteria.andEqualTo("categoryId", categoryId);
                }
                break;
            case "3":
                criteria.andEqualTo("categoryId", categoryId);
                break;
        }
    }

    @Override
//    @Cacheable(value = SupplyConstants.Cache.GOODS_QUERY)
    public Pagenation<Skus> itemsSkusPage(SkusForm queryModel, Pagenation<Skus> page, AclUserAccreditInfo aclUserAccreditInfo) throws Exception {
        AssertUtil.notNull(aclUserAccreditInfo, "用户授权信息为空");
        Example example = new Example(Skus.class);
        Example.Criteria criteria = example.createCriteria();
        if (StringUtil.isNotEmpty(queryModel.getSpuCode())) {//spuCode
            criteria.andLike("spuCode", "%" + queryModel.getSpuCode() + "%");
        }
        if (StringUtil.isNotEmpty(queryModel.getSkuCode())) {//skuCode
            criteria.andLike("skuCode", "%" + queryModel.getSkuCode() + "%");
        }
        if (StringUtil.isNotEmpty(queryModel.getIsValid())) {
            criteria.andEqualTo("isValid", queryModel.getIsValid());
        }

        if (StringUtil.isNotEmpty(queryModel.getSkuName())) {//skuName
            criteria.andLike("skuName", "%" + queryModel.getSkuName() + "%");
        }

        if (StringUtils.isNotBlank(queryModel.getBarCode())) {
            List<String> barCodeList = Arrays.asList(StringUtils.split(queryModel.getBarCode(), SupplyConstants.Symbol.COMMA));
            Set<String> barCodeSet = skusService.selectSkuListByBarCode(barCodeList);
            if (!AssertUtil.collectionIsEmpty(barCodeSet)) {
                criteria.andIn("barCode", barCodeSet);
            } else {
                criteria.andEqualTo("barCode", StringUtils.EMPTY);
            }
        }
        Set<String> spus = getSkusQueryConditonRelateSpus(queryModel);
        if (null != spus) {
            if (spus.size() > 0) {
                criteria.andIn("spuCode", spus);
            } else {
                return page;
            }
        }
        example.orderBy("updateTime").desc();
        page = skusService.pagination(example, page, queryModel);
        if (page.getResult().size() > 0) {
            handerSkusPage(page, aclUserAccreditInfo.getChannelCode());
        }
        //分页查询
        return page;
    }

    /**
     *
     * @param page
     * @param channelCode 渠道编码
     * @throws Exception
     */
    private void handerSkusPage(Pagenation<Skus> page, String channelCode) throws Exception {
        Set<String> spuCodeList = new HashSet<String>();
        List<String> skuCodeList = new ArrayList<String>();
        for (Skus skus : page.getResult()) {
            spuCodeList.add(skus.getSpuCode());
            skuCodeList.add(skus.getSkuCode());
        }
        //查询相关商品
        Example example = new Example(Items.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("spuCode", spuCodeList);
        List<Items> itemsList = itemsService.selectByExample(example);
        AssertUtil.notEmpty(itemsList, String.format("根据多个SPU编码[%s]查询商品信息为空",
                CommonUtil.converCollectionToString(Arrays.asList(spuCodeList))));
        Set<Long> categoryIdList = new HashSet<Long>();
        Set<Long> brandIdList = new HashSet<Long>();
        for (Items items : itemsList) {
            categoryIdList.add(items.getCategoryId());
            brandIdList.add(items.getBrandId());
        }
        //spu和分类名称map
        Map<String, String> spuCategoryMap = new HashMap<String, String>();
        for (Items items : itemsList) {
            List<String> namePathList = null;
            try {
                namePathList = categoryBiz.queryCategoryNamePath(items.getCategoryId());
                AssertUtil.notEmpty(namePathList, String.format("根据分类ID[%s]查询分类名称信息为空", items.getCategoryId()));
            } catch (Exception e) {
                log.error("查询分类名称异常", e);
            }
            if (null != namePathList) {
                StringBuilder sb = new StringBuilder();
                for (int i = namePathList.size(); i > 0; i--) {
                    int j = i - 1;
                    if (j == 0) {
                        sb.append(namePathList.get(j));
                    } else {
                        sb.append(namePathList.get(j)).append("-");
                    }
                }
                spuCategoryMap.put(items.getSpuCode(), sb.toString());
            }
        }
        //查询相关品牌
        Example example3 = new Example(Brand.class);
        Example.Criteria criteria3 = example3.createCriteria();
        criteria3.andIn("id", brandIdList);
        List<Brand> brandList = brandService.selectByExample(example3);
        AssertUtil.notEmpty(brandList, String.format("根据多个品牌ID[%s]查询品牌信息为空",
                CommonUtil.converCollectionToString(Arrays.asList(brandIdList))));
        //spu和商品名称map
        Map<String, String> spuBrandMap = new HashMap<String, String>();
        for (Items items : itemsList) {
            for (Brand brand : brandList) {
                if (items.getBrandId().longValue() == brand.getId().longValue()) {
                    spuBrandMap.put(items.getSpuCode(), brand.getName());
                    break;
                }
            }
        }
        //查询SKU相关库存信息,直接调用京东接口查库存
        //通知成功的仓库
        List<ScmInventoryQueryResponse> inventoryQueryResponseList = new ArrayList<>();
        WarehouseInfo warehouseInfo = new WarehouseInfo();
        warehouseInfo.setOwnerWarehouseState(OwnerWarehouseStateEnum.NOTICE_SUCCESS.getCode());//通知成功
        warehouseInfo.setIsValid(ZeroToNineEnum.ONE.getCode());//启用
        List<WarehouseInfo> warehouseInfoList = warehouseInfoService.select(warehouseInfo);
        if (CollectionUtils.isEmpty(warehouseInfoList)) {
            log.warn("自采SKU没有查询到可用仓库");

        }
        List<String> skuCodes = new ArrayList<>();
        for (Skus skus : page.getResult()) {
            skuCodes.add(skus.getSkuCode());
        }
        List<SkuStock> skuStockList = new ArrayList<>();
        inventoryQueryResponseList = warehouseExtService.getWarehouseInventory(skuCodes, null);
        if (!AssertUtil.collectionIsEmpty(inventoryQueryResponseList)) {
            //sku计算库存总和
            for (String skuCode : skuCodes) {
                //先获取到该sku关联的仓库
                WarehouseItemInfo warehouseItemInfo = new WarehouseItemInfo();
                warehouseItemInfo.setSkuCode(skuCode);
                //通知成功的状态
                warehouseItemInfo.setNoticeStatus(4);
                List<WarehouseItemInfo> warehouseItemInfoList = warehouseItemInfoService.select(warehouseItemInfo);
                if (AssertUtil.collectionIsEmpty(warehouseItemInfoList)) {
                    log.warn("自采SKU" + skuCode + "没有查询到仓库关联信息");
                    continue;
                }
                List<String> itemIds = new ArrayList<>();
                for (WarehouseItemInfo warehouseItem : warehouseItemInfoList) {
                    itemIds.add(warehouseItem.getWarehouseItemId());
                }
                for (ScmInventoryQueryResponse inventoryQueryResponse : inventoryQueryResponseList) {
                    SkuStock skuStock = new SkuStock();
                    skuStock.setSkuCode(skuCode);
                    boolean flag = false;
                    for (String itemId : itemIds) {
                        if (StringUtils.equals(inventoryQueryResponse.getItemId(), itemId)) {
                            flag = true;
                        }
                    }
                    if (flag) {
                        //判断库存类型,可销售
                        if (StringUtils.equals(inventoryQueryResponse.getInventoryType(), JingdongInventoryTypeEnum.SALE.getCode()) && StringUtils.equals(inventoryQueryResponse.getInventoryStatus(), JingdongInventoryStateEnum.GOOD.getCode())) {
                            skuStock.setAvailableInventory((inventoryQueryResponse.getQuantity() == null ? 0 : inventoryQueryResponse.getQuantity()) + (skuStock.getAvailableInventory() == null ? 0 : skuStock.getAvailableInventory()));
                        }
//                        //判断库存类型,仓库锁定
//                        if (StringUtils.equals(inventoryQueryResponse.getInventoryType(),JingdongInventoryTypeEnum.WAREHOUSE_LOCK.getCode())){
//                            skuStock.setLockInventory((inventoryQueryResponse.getTotalNum()==null?0:inventoryQueryResponse.getTotalNum())+(skuStock.getLockInventory()==null?0:skuStock.getLockInventory()));
//                        }
                    }
                    skuStockList.add(skuStock);
                }
            }
        }
        //设置分类名称、品牌名称、库存信息
        for (Skus skus : page.getResult()) {
            skus.setCategoryName(spuCategoryMap.get(skus.getSpuCode()));
            skus.setBrandName(spuBrandMap.get(skus.getSpuCode()));
            for (Items items : itemsList) {
                if (StringUtils.equals(skus.getSpuCode(), items.getSpuCode())) {
                    skus.setItemsName(items.getName());
                }
            }
            for (SkuStock skuStock : skuStockList) {
                if (StringUtils.equals(skus.getSkuCode(), skuStock.getSkuCode())) {
                    skus.setAvailableInventory((skuStock.getAvailableInventory() == null ? 0 : skuStock.getAvailableInventory()) + (skus.getAvailableInventory() == null ? 0 : skus.getAvailableInventory()));
                    skus.setRealInventory((skuStock.getLockInventory() == null ? 0 : skuStock.getLockInventory()) + (skus.getRealInventory() == null ? 0 : skus.getRealInventory()));
                    if (null == skus.getAvailableInventory()) {
                        skus.setAvailableInventory(null);
                    }
                    if (null == skus.getRealInventory()) {
                        skus.setRealInventory(null);
                    }
                }
            }
        }
    }

    /**
     * 获取SKU查询条件相关的SPU
     *
     * @param queryModel
     * @return
     */
    private Set<String> getSkusQueryConditonRelateSpus(SkusForm queryModel) {
        if (StringUtil.isNotEmpty(queryModel.getItemName()) || null != queryModel.getCategoryId() || null != queryModel.getBrandId()) {
            Example example = new Example(Items.class);
            Example.Criteria criteria = example.createCriteria();
            if (StringUtil.isNotEmpty(queryModel.getItemName())) {//商品名称
                criteria.andLike("name", "%" + queryModel.getItemName() + "%");
            }
            if (null != queryModel.getCategoryId()) {
                categoryFrom(criteria, queryModel.getCategoryLevel(), queryModel.getCategoryId());
            }
            if (null != queryModel.getBrandId()) {
                criteria.andEqualTo("brandId", queryModel.getBrandId());
            }
            List<Items> items = itemsService.selectByExample(example);
            Set<String> spus = new HashSet<String>();
            for (Items item : items) {
                spus.add(item.getSpuCode());
            }
            return spus;
        } else {
            return null;
        }
    }


    private Map<String, Object> handlerSkuCondition(String skuCode, String spuCode, String barCode) {
        Map<String, Object> map = new HashMap<>();
        List<String> spuCodes = null;
        List<Skus> skuses = new ArrayList<>();
        if (StringUtils.isNotBlank(spuCode)) {
            spuCodes = new ArrayList<String>();
            Example example = new Example(Items.class);
            Example.Criteria criteria2 = example.createCriteria();
            criteria2.andLike("spuCode", "%" + spuCode + "%");
            List<Items> itemsList = itemsService.selectByExample(example);
            for (Items items : itemsList) {
                spuCodes.add(items.getSpuCode());
            }
        }
        if (StringUtils.isNotBlank(skuCode)) {
            spuCodes = new ArrayList<String>();
            Example example = new Example(Skus.class);
            Example.Criteria criteria2 = example.createCriteria();
            criteria2.andLike("skuCode", "%" + skuCode + "%");
            List<Skus> skusList = skusService.selectByExample(example);
            if (skusList.size() > 0) {
                for (Skus s : skusList) {
                    spuCodes.add(s.getSpuCode());
                }
                skuses.addAll(skusList);
            }
        }


        if (StringUtils.isNotBlank(barCode)) {
            spuCodes = new ArrayList<String>();
            Set<String> barCodeSet = skusService.selectSkuListByBarCode(Arrays.asList(StringUtils.split(barCode, SupplyConstants.Symbol.COMMA)));
            Example example = new Example(Skus.class);
            Example.Criteria criteria2 = example.createCriteria();
            if (!AssertUtil.collectionIsEmpty(barCodeSet)) {
                criteria2.andIn("barCode", barCodeSet);
            } else {
                criteria2.andEqualTo("barCode", StringUtils.EMPTY);
            }
            List<Skus> skusList = skusService.selectByExample(example);
            if (skusList.size() > 0) {
                for (Skus s : skusList) {
                    spuCodes.add(s.getSpuCode());
                }
            }
            skuses.addAll(skusList);
        }
        if (!AssertUtil.collectionIsEmpty(skuses)) {
            map.put("skuList", skuses);
        }
        map.put("spuCodes", spuCodes);
        return map;
    }

    private void handerPage(Pagenation<Items> page, List<Skus> skusList) {
        List<Long> categoryIds = new ArrayList<Long>();
        List<Long> brandIds = new ArrayList<Long>();
        for (Items item : page.getResult()) {
            categoryIds.add(item.getCategoryId());
            brandIds.add(item.getBrandId());
        }
        if (categoryIds.size() > 0) {
            setCategoryName(page.getResult(), categoryIds);
        }
        if (brandIds.size() > 0) {
            setBrandName(page.getResult(), brandIds);
        }
        //设置商品对应的Sku信息
        setSkus(page.getResult(), skusList);
    }

    /**
     * 设置商品对应的sku
     *
     * @param items
     */
    private void setSkus(List<Items> items, List<Skus> skusList) {
        if (items.size() > 0) {
            List<Skus> _skusList = new ArrayList<>();
            if (null != skusList) {
                _skusList = skusList;
            } else {
                List<String> spuCodes = new ArrayList<String>();
                for (Items item : items) {
                    spuCodes.add(item.getSpuCode());
                }
                //查询商品对应的SKU
                Example example = new Example(Skus.class);
                Example.Criteria criteria = example.createCriteria();
                criteria.andIn("spuCode", spuCodes);
                example.orderBy("isValid").desc();
                example.orderBy("skuCode").asc();
                _skusList = skusService.selectByExample(example);
                AssertUtil.notEmpty(_skusList, String.format("批量查询商品SPU编码为[%s]的商品对应的SKU信息为空", CommonUtil.converCollectionToString(spuCodes)));
            }
            List<String> skuCodes = new ArrayList<String>();
            for (Skus skus : _skusList) {
                skuCodes.add(skus.getSkuCode());
            }
            //查询所有sku对应的采购属性
            Example example2 = new Example(ItemSalesPropery.class);
            Example.Criteria criteria2 = example2.createCriteria();
            criteria2.andIn("skuCode", skuCodes);
            List<ItemSalesPropery> itemSalesProperies = itemSalesProperyService.selectByExample(example2);
            AssertUtil.notEmpty(itemSalesProperies, String.format("批量查询商品SKU编码为[%s]的SKU对应的采购属性信息为空", CommonUtil.converCollectionToString(skuCodes)));
            //查询所有采购属性详细信息
            List<Long> propertyIds = new ArrayList<Long>();
            for (ItemSalesPropery itemSalesPropery : itemSalesProperies) {
                propertyIds.add(itemSalesPropery.getPropertyId());
            }
            Example example3 = new Example(Property.class);
            Example.Criteria criteria3 = example3.createCriteria();
            criteria3.andIn("id", propertyIds);
            List<Property> propertyList = propertyService.selectByExample(example3);
            AssertUtil.notEmpty(propertyList, String.format("批量查询属性ID为[%s]的属性对应的信息为空", CommonUtil.converCollectionToString(propertyIds)));
            //设置SKU的采购属性组合名称
            for (Skus skus : _skusList) {
                skus.setPropertyCombineName(getPropertyCombineName(skus, itemSalesProperies, propertyList));
            }
            //设置商品SKU
            for (Items item : items) {
                List<Skus> _tmpSkus = new ArrayList<Skus>();
                for (Skus skus : _skusList) {
                    if (StringUtils.equals(item.getSpuCode(), skus.getSpuCode())) {
                        _tmpSkus.add(skus);
                    }
                }
                item.setRecords(_tmpSkus);
            }
        }
    }

    /**
     * 获取SKU属性组合名称
     *
     * @param skus
     * @param itemSalesProperies
     * @param properties
     * @return
     */
    private String getPropertyCombineName(Skus skus, List<ItemSalesPropery> itemSalesProperies, List<Property> properties) {
        String[] propertyValueIdArray = skus.getPropertyValueId().split(SKU_PROPERTY_VALUE_ID_SPLIT_SYMBOL);
        String[] propertyValueArray = skus.getPropertyValue().split(SKU_PROPERTY_VALUE_ID_SPLIT_SYMBOL);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < propertyValueIdArray.length; i++) {
            Long propertyValueId = Long.parseLong(propertyValueIdArray[i]);
            for (ItemSalesPropery itemSalesPropery : itemSalesProperies) {
                if (propertyValueId.longValue() == itemSalesPropery.getPropertyValueId().longValue()) {
                    for (Property property : properties) {
                        if (itemSalesPropery.getPropertyId().equals(property.getId())) {
                            sb.append(SKU_PROPERTY_COMBINE_NAME_EMPTY).append(property.getName()).append(SKU_PROPERTY_COMBINE_NAME_SPLIT_SYMBOL).append(propertyValueArray[i]);
                            break;
                        }
                    }
                    break;
                }
            }
        }
        return sb.toString();
    }

    /**
     * 设置分类名称
     *
     * @param items
     * @param categoryIds
     */
    private void setCategoryName(List<Items> items, List<Long> categoryIds) {
        Example example = new Example(Category.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", categoryIds);
        criteria.andEqualTo("isDeleted", ZeroToNineEnum.ZERO.getCode());
        List<Category> thridCategories = categoryService.selectByExample(example);
        AssertUtil.notEmpty(thridCategories, String.format("查询商品所属分类ID为[%s]的分类信息为空", CommonUtil.converCollectionToString(categoryIds)));
        /**
         * 将分类的全路径ID(full_path_id)取出来，然后从中取到从第一级到第三季的所有分类ID
         * 放到分类ID列表categoryIds中
         */
        for (Category c : thridCategories) {
            String[] tmps = c.getFullPathId().split("\\" + CATEGORY_ID_SPLIT_SYMBOL);
            for (String s : tmps) {
                categoryIds.add(Long.parseLong(s));
            }
        }
        List<Category> categories = categoryService.selectByExample(example);
        //获取三级分类对应的全路径名称
        Map<Long, String> map = getThirdCategoryFullPathName(thridCategories, categories);
        for (Items items2 : items) {
            items2.setCategoryName(map.get(items2.getCategoryId()));
        }
    }

    /**
     * 获取第三级分类全路径名称
     *
     * @param thirdCategories 第三级分类列表
     * @param categories      当前相关所有分类列表
     * @return
     */
    private Map<Long, String> getThirdCategoryFullPathName(List<Category> thirdCategories, List<Category> categories) {
        Map<Long, String> map = new HashMap<Long, String>();
        for (Category c : thirdCategories) {
            String[] tmps = c.getFullPathId().split("\\" + CATEGORY_ID_SPLIT_SYMBOL);
            if (tmps.length == 2) {
                StringBuilder sb = new StringBuilder();
                //第一级分类名称
                for (Category c2 : categories) {
                    if (Long.parseLong(tmps[0]) == c2.getId()) {
                        sb.append(c2.getName());
                        break;
                    }
                }
                //第二级分类名称
                for (Category c2 : categories) {
                    if (Long.parseLong(tmps[1]) == c2.getId()) {
                        sb.append(CATEGORY_NAME_SPLIT_SYMBOL).append(c2.getName());
                        break;
                    }
                }
                //第三级分类名称
                for (Category c2 : categories) {
                    if (c.getId().longValue() == c2.getId().longValue()) {
                        sb.append(CATEGORY_NAME_SPLIT_SYMBOL).append(c2.getName());
                        break;
                    }
                }
                map.put(c.getId(), sb.toString());
            } else if (tmps.length < 2) {
                log.error(String.format("三级分类[%s]的上级分类信息为空", c.getName()));
            }
        }
        return map;
    }

    /**
     * 设置品牌名称
     *
     * @param items
     * @param brandIds
     */
    private void setBrandName(List<Items> items, List<Long> brandIds) {
        Example example = new Example(Brand.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", brandIds);
        criteria.andEqualTo("isDeleted", ZeroToNineEnum.ZERO.getCode());
        List<Brand> brands = brandService.selectByExample(example);
        AssertUtil.notEmpty(brands, String.format("查询商品品牌ID为[%s]的品牌信息为空", CommonUtil.converCollectionToString(brandIds)));
        for (Items items2 : items) {
            for (Brand c : brands) {
                if (items2.getBrandId().longValue() == c.getId().longValue()) {
                    items2.setBrandName(c.getName());
                    break;
                }
            }
        }
    }

    @Override
    public List<Items> queryItems(ItemsForm itemsForm) throws Exception {
        Items items = new Items();
        BeanUtils.copyProperties(itemsForm, items);
        if (StringUtils.isEmpty(itemsForm.getIsValid())) {
            items.setIsValid(ZeroToNineEnum.ONE.getCode());
        }
        items.setIsDeleted(ZeroToNineEnum.ZERO.getCode());
        return itemsService.select(items);
    }

    @Override
    @GoodsCacheEvict
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void saveItems(Items items, Skus skus, ItemNaturePropery itemNaturePropery, ItemSalesPropery itemSalesPropery) throws Exception {
        AssertUtil.notBlank(itemSalesPropery.getSalesPropertys(), "提交商品信息采购属性不能为空");
        AssertUtil.notBlank(skus.getSkusInfo(), "提交商品信息SKU信息不能为空");
        checkSkuInfo(skus);//检查sku参数
        //生成序列号
        String code = serialUtilService.generateCode(SupplyConstants.Serial.SPU_LENGTH, SupplyConstants.Serial.SPU_NAME, DateUtils.dateToCompactString(Calendar.getInstance().getTime()));
        items.setSpuCode(code);
        ParamsUtil.setBaseDO(items);
        //保存商品基础信息
        saveItemsBase(items);
        //保存sku信息
        skus.setItemId(items.getId());
        skus.setSpuCode(items.getSpuCode());
        List<Skus> skuss = saveSkus(skus);
        //保存自然属性信息
        if (StringUtils.isNotBlank(itemNaturePropery.getNaturePropertys())) {
            itemNaturePropery.setItemId(items.getId());
            itemNaturePropery.setSpuCode(items.getSpuCode());
            saveItemNatureProperty(itemNaturePropery, items.getCategoryId());
        }
        //保存采购属性信息
        saveItemSalesPropery(itemSalesPropery, skuss, items.getCategoryId());
        //记录操作日志
        logInfoService.recordLog(items, items.getId().toString(), items.getCreateOperator(), LogOperationEnum.ADD.getMessage(), null, null);
        //保存商品同步到企业购
        saveItemsNotifyToBusinessPurchase(items, skuss);
    }

    /**
     * 保存商品同步到企业购
     *
     * @param items
     * @param skusList
     */
    private void saveItemsNotifyToBusinessPurchase(Items items, List<Skus> skusList) {
        BusiItems busiItems = new BusiItems();
        BeanUtils.copyProperties(items, busiItems, "id");
        busiItems.setScmIsValid(items.getIsValid());
        busiItems.setIsValid(ValidEnum.NOVALID.getCode());
        busiItems.setId(GuidUtil.getNextUid("ITEM-"));
        List<BusiSkus> busiSkusList = new ArrayList<>();
        for (Skus skus : skusList) {
            BusiSkus busiSkus = new BusiSkus();
            BeanUtils.copyProperties(skus, busiSkus, "id", "marketPrice");
            if (null != skus.getMarketPrice()) {
                busiSkus.setMarketPrice(new BigDecimal(CommonUtil.getMoneyYuan(skus.getMarketPrice())));
            }
            busiSkus.setScmIsValid(skus.getIsValid());
            busiSkus.setIsValid(ValidEnum.NOVALID.getCode());
            busiSkus.setId(GuidUtil.getNextUid("SKU"));
            busiSkusList.add(busiSkus);
        }
        //设置企业购商品分类
        setBusinessPurchaseItemCategory(items.getCategoryId(), busiItems);
        busiItemsService.insert(busiItems);
        for (BusiSkus busiSkus : busiSkusList) {
            busiSkusService.insert(busiSkus);
        }
    }

    /**
     * 设置企业购商品分类
     *
     * @param categoryId
     * @param busiItems
     */
    private void setBusinessPurchaseItemCategory(Long categoryId, BusiItems busiItems) {
        Category thirdCategory = categoryService.selectByPrimaryKey(categoryId);
        AssertUtil.notNull(thirdCategory, String.format("根据第三级分类ID[%s]查询分类为空", categoryId));
        Category secondCategory = categoryService.selectByPrimaryKey(thirdCategory.getParentId());
        AssertUtil.notNull(secondCategory, String.format("根据第二级分类ID[%s]查询分类为空", thirdCategory.getParentId()));
        Category firstCategory = categoryService.selectByPrimaryKey(secondCategory.getParentId());
        AssertUtil.notNull(firstCategory, String.format("根据第一级分类ID[%s]查询分类为空", secondCategory.getParentId()));
        busiItems.setFirstCategoryId(firstCategory.getId());
        busiItems.setSecondCategoryId(secondCategory.getId());
        busiItems.setThirdCategoryId(thirdCategory.getId());
    }

    /**
     * 商品更新通知渠道
     *
     * @param items
     */
    private void itemsUpdateNoticeChannel(Items items, List<Skus> updateSkus, TrcActionTypeEnum trcActionTypeEnum) {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ItemNaturePropery itemNaturePropery = new ItemNaturePropery();
                            itemNaturePropery.setSpuCode(items.getSpuCode());
                            List<ItemNaturePropery> itemNatureProperies = itemNatureProperyService.select(itemNaturePropery);
                            //AssertUtil.notEmpty(itemNatureProperies, String.format("根据商品SPU编码%s查询相关自然属性为空", items.getSpuCode()));
                            ItemSalesPropery itemSalesPropery = new ItemSalesPropery();
                            itemSalesPropery.setSpuCode(items.getSpuCode());
                            List<ItemSalesPropery> itemSalesProperies = itemSalesProperyService.select(itemSalesPropery);
                            AssertUtil.notEmpty(itemSalesProperies, String.format("根据商品SPU编码%s查询相关采购属性为空", items.getSpuCode()));
                        /*Skus skus = new Skus();
                        skus.setSpuCode(items.getSpuCode());
                        List<Skus> skusList = skusService.select(skus);
                        AssertUtil.notEmpty(itemSalesProperies, String.format("根据商品SPU编码%s查询相关SKU信息为空", items.getSpuCode()));*/
                            ToGlyResultDO toGlyResultDO = trcBiz.sendItem(trcActionTypeEnum, items, itemNatureProperies, itemSalesProperies, updateSkus, System.currentTimeMillis());
                            if (StringUtils.equals(SuccessFailureEnum.SUCCESS.getCode(), toGlyResultDO.getStatus())) {
                                if (log.isInfoEnabled())
                                    log.info(String.format("更新商品%s通知渠道成功", JSON.toJSONString(items)));
                            } else {
                                log.error(String.format("更新商品%s通知渠道失败", JSON.toJSONString(items)));
                            }
                        } catch (Exception e) {
                            String msg = String.format("更新商品%s通知渠道异常,异常信息:%s", JSON.toJSONString(items), e.getMessage());
                            log.error(msg, e);
                        }
                    }
                }
        ).start();
    }

    @Override
    @GoodsCacheEvict
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void updateItems(Items items, Skus skus, ItemNaturePropery itemNaturePropery, ItemSalesPropery itemSalesPropery, AclUserAccreditInfo aclUserAccreditInfo) throws Exception {

        selectDataAcl(items.getId(), aclUserAccreditInfo, false);
        //记录操作日志
        String logMsg = "";
       try {
           Items orginItems = itemsService.selectByPrimaryKey(items.getId());
           if (StringUtils.equals(orginItems.getName(), items.getName()) && StringUtils.equals(orginItems.getBrandId().toString(), items.getBrandId().toString())
                   && StringUtils.equals(orginItems.getTradeType(), items.getTradeType()) && StringUtils.equals(orginItems.getItemNo(), items.getItemNo())
                   && StringUtils.equals(orginItems.getProducer(), items.getProducer())   && StringUtils.equals(orginItems.getRemark().trim(), items.getRemark().trim())
                   && StringUtils.equals(orginItems.getIsQuality(), items.getIsQuality()) && StringUtils.equals(orginItems.getQualityDay().toString(),items.getQualityDay()==null?"0":items.getQualityDay().toString())
                   && StringUtils.equals(orginItems.getItemType(),items.getItemType())) {
               logMsg = "";
           } else {
               logMsg = logMsg + "SPU信息：";
               if (!StringUtils.equals(orginItems.getName(), items.getName())) {
                   logMsg = logMsg + "商品名称由\"" + orginItems.getName() + "\"改为\"" + items.getName() + "\";";
               }
               if (!StringUtils.equals(orginItems.getBrandId().toString(), items.getBrandId().toString())) {
                   Brand orginBrand = brandService.selectOneById(orginItems.getBrandId());
                   Brand brand = brandService.selectOneById(items.getBrandId());
                   logMsg = logMsg + "所属品牌由\"" + orginBrand.getName() + "\"改为\"" + brand.getName() + "\";";
               }
               if (!StringUtils.equals(orginItems.getTradeType(), items.getTradeType())) {
                   Dict temp = new Dict();
                   temp.setTypeCode(TRADE_TYPE);
                   List<Dict> list = dictService.select(temp);
                   String orginTradeType="";
                   String nowTradeType="";
                   for (Dict dict : list) {
                       if (StringUtils.equals( dict.getValue(),orginItems.getTradeType())){
                           orginTradeType=dict.getName();
                       }
                       if (StringUtils.equals( dict.getValue(),items.getTradeType())){
                           nowTradeType=dict.getName();
                       }
                   }
                   logMsg = logMsg + "贸易类型由\"" + orginTradeType+ "\"改为\"" + nowTradeType+ "\";";
               }
               if (!StringUtils.equals(orginItems.getItemNo(), items.getItemNo())) {
                   logMsg = logMsg + "商品货号由\"" + orginItems.getItemNo() + "\"改为\"" + items.getItemNo() + "\";";
               }
               if (!StringUtils.equals(orginItems.getProducer(), items.getProducer())) {
                   logMsg = logMsg + "生厂商由\"" + orginItems.getProducer() + "\"改为\"" + items.getProducer() + "\";";
               }
               if (!StringUtils.equals(orginItems.getItemType(), items.getItemType())) {
                   if (StringUtils.equals(orginItems.getItemType(),ZeroToNineEnum.ZERO.getCode())){
                       logMsg = logMsg + "商品类别由\"" + ItemTypeEnum.XIAOTAI.getName() + "\"改为\"" + ItemTypeEnum.NON_XIAOTAI.getName() + "\";";
                   }else {
                       logMsg = logMsg + "商品类别由\"" + ItemTypeEnum.NON_XIAOTAI + "\"改为\"" + ItemTypeEnum.XIAOTAI.getName() + "\";";
                   }
               }
               if (StringUtils.equals(orginItems.getIsQuality(), ZeroToNineEnum.ONE.getCode()) || StringUtils.equals(items.getIsQuality(), ZeroToNineEnum.ONE.getCode())){//排除都为否的情形
                   if (StringUtils.equals(orginItems.getIsQuality(), ZeroToNineEnum.ONE.getCode()) && StringUtils.equals(items.getIsQuality(), ZeroToNineEnum.ONE.getCode())) {
                       if (!StringUtils.equals(orginItems.getQualityDay().toString(), items.getQualityDay().toString())){
                           logMsg = logMsg + "保质期天数由\"" + orginItems.getQualityDay() + "天\"改为\"" + items.getQualityDay() + "天\";";
                       }
                   } else if (StringUtils.equals(orginItems.getIsQuality(), ZeroToNineEnum.ZERO.getCode()) && StringUtils.equals(items.getIsQuality(), ZeroToNineEnum.ONE.getCode())) {
                       logMsg = logMsg + "是否具有保质期由\"无" + "\"改为\"" + items.getQualityDay() + "天\";";
                   } else if (StringUtils.equals(orginItems.getIsQuality(), ZeroToNineEnum.ONE.getCode()) && StringUtils.equals(items.getIsQuality(), ZeroToNineEnum.ZERO.getCode())){
                       logMsg = logMsg + "是否具有保质期由\"" + orginItems.getQualityDay() + "天\"" + "改为\"" + "无\";";
                   }
               }
               if (!StringUtils.equals(orginItems.getRemark().trim(), items.getRemark().trim())) {
                   logMsg = logMsg + "商品备注由\"" + orginItems.getRemark().trim() + "\"改为\"" + items.getRemark().trim() + "\";";
               }
               logMsg = logMsg.substring(0, logMsg.lastIndexOf(";")) + "。<br/>";
           }

           StringBuilder logMsg2 = new StringBuilder("");
           JSONArray categoryArray = JSONArray.parseArray(itemNaturePropery.getNaturePropertys());
           if (categoryArray != null && categoryArray.size() > 0) {
               ItemNaturePropery temp = new ItemNaturePropery();
               temp.setSpuCode(items.getSpuCode());
               temp.setIsDeleted(ZeroToNineEnum.ZERO.getCode());
               List<ItemNaturePropery> itemNatureProperyList = itemNatureProperyService.select(temp);
               for (Object obj : categoryArray) {
                   JSONObject jbo = (JSONObject) obj;
                   String  propertyId = jbo.getString("propertyId");
                   String propertyValueId = jbo.getString("propertyValueId");
                   String propertyValue = jbo.getString("propertyValue");
                   for (ItemNaturePropery naturePropery : itemNatureProperyList) {
                       if (StringUtils.equals(naturePropery.getPropertyId().toString(),propertyId)){
                           if (!StringUtils.equals(naturePropery.getPropertyValueId().toString(), propertyValueId)) {
                               Property property = propertyService.selectOneById(Long.parseLong(propertyId));
                               PropertyValue propertyValueTemp = propertyValueService.selectByPrimaryKey(naturePropery.getPropertyValueId());
                               //logMsg2 = logMsg2 + property.getName() + "由\"" + propertyValueTemp.getValue() + "\"改为\"" + propertyValue + "\";";
                               logMsg2.append(property.getName() + "由\"" + propertyValueTemp.getValue() + "\"改为\"" + propertyValue + "\";");
                           }
                       }
                   }
               }
           }

           JSONArray skuArray = JSONArray.parseArray(skus.getSkusInfo());
           if (skuArray != null && skuArray.size() > 0) {
               for (Object obj : skuArray) {
                   JSONObject jbo = (JSONObject) obj;
                   Skus temp = new Skus();
                   String skuCode = jbo.getString("skuCode");
                   if (StringUtils.equals(skuCode, "")) {
                       //logMsg2 = logMsg2 + "新增了名称为\"" + jbo.getString("skuName") + "\"的sku;";
                       logMsg2.append("新增了名称为\"" + jbo.getString("skuName") + "\"的sku;");
                   } else {
                       temp.setSkuCode(skuCode);
                       Skus orginSkus = skusService.selectOne(temp);

                       String orginMarketPrice= orginSkus.getMarketPrice() == null ? "" :String.valueOf(orginSkus.getMarketPrice());
                       String orginWeight = orginSkus.getWeight() == null ? "" : String.valueOf(orginSkus.getWeight());
                  /*  String s = String.valueOf(orginSkus.getMarketPrice()/100L);
                    String s2=String.valueOf(orginSkus.getMarketPrice()%100L/10L*0.1d);
                    String s3=String.valueOf(orginSkus.getMarketPrice()%100L/10L/10L*0.01d);
                    String s4 = String.valueOf(Double.parseDouble(jbo.getString("marketPrice2"))*100/100L);
                    String s5 = String.valueOf(Double.parseDouble(jbo.getString("marketPrice2")) *100%100L/10L*0.1d);
                    String s6 = String.valueOf(Double.parseDouble(jbo.getString("marketPrice2"))*100%100L/10L/10L*0.01d);
                    String s7 = String.valueOf(Double.parseDouble(jbo.getString("weight2"))*100/100L);
                    String s8 = String.valueOf(Double.parseDouble(jbo.getString("weight2")) *100%100L/10L*0.1d);
                    String s9 = String.valueOf(Double.parseDouble(jbo.getString("weight2"))*100%100L/10L/10L*0.01d);*/

                       String newMarketPrice=jbo.getString("marketPrice2")==null?"":(getLongValue(jbo.getString("marketPrice2")).toString());
                       String newWeight=jbo.getString("weight2")==null?"":(CommonUtil.getWeightLong(jbo.getString("weight2")).toString());

                       if (StringUtils.equals(orginSkus.getSkuName(), jbo.getString("skuName")) && StringUtils.equals(orginSkus.getBarCode(), jbo.getString("barCode"))
                               && StringUtils.equals(orginSkus.getIsValid(), jbo.getString("isValid")) &&StringUtils.equals(orginMarketPrice,newMarketPrice)
                               && StringUtils.equals(orginWeight, newWeight)) {
                           logMsg2.append("");
                       } else {
                           logMsg2.append(skuCode + ":") ;
                           if (!StringUtils.equals(orginSkus.getSkuName(), jbo.getString("skuName"))) {
                               logMsg2.append("SKU名称由\"" + orginSkus.getSkuName() + "\"改为\"" + jbo.getString("skuName") + "\";") ;
                           }
                           if (!StringUtils.equals(orginSkus.getBarCode(), jbo.getString("barCode"))) {
                               logMsg2.append("条形码由\"" + orginSkus.getBarCode() + "\"改为\"" + jbo.getString("barCode") + "\";");
                           }
                           if (!StringUtils.equals(orginSkus.getIsValid(), jbo.getString("isValid"))) {
                               logMsg2.append("sku状态由\"" + ValidEnum.getValidEnumByCode(orginSkus.getIsValid()).getName() + "\"改为\"" + ValidEnum.getValidEnumByCode(jbo.getString("isValid")).getName()
                                       + "\";") ;
                           }
                           if (!StringUtils.equals(orginMarketPrice,newMarketPrice)) {
                               logMsg2.append("参考市场价由\"" +orginMarketPrice==""?"":Double.parseDouble(CommonUtil.fenToYuan(Long.parseLong(orginMarketPrice)).toString()) + "\"改为\"" + jbo.getString("marketPrice2") + "\";");
                           }
                           if (!StringUtils.equals(orginWeight, newWeight)) {
                               logMsg2.append("重量由\"" + orginSkus.getMarketPrice()==""?"":Double.parseDouble(CommonUtil.getWeight(Long.parseLong(orginWeight)).toString()) + "\"改为\"" + jbo.getString("weight2") + "\";");
                           }
                           logMsg2.append("<br/>") ;
                       }
                   }
               }
           }

           if (!StringUtils.isEmpty(logMsg2)) {
               logMsg2 = new StringBuilder("商品信息：" ).append(logMsg2.substring(0, logMsg2.lastIndexOf(";")) + "。<br/>");
           }

           //记录日志
           if (StringUtils.equals(logMsg2, "商品信息：")) {
               logMsg2 = new StringBuilder("");
           }
           logMsg = logMsg2.toString() + logMsg;
           if (!StringUtils.equals(logMsg, "")) {
               logInfoService.recordLog(items, items.getId().toString(), aclUserAccreditInfo.getUserId(), LogOperationEnum.UPDATE.getMessage(), logMsg, null);
           }
       }catch (Exception e){
           log.error("日志异常",e);
           e.printStackTrace();
       }finally {
           AssertUtil.notBlank(items.getSpuCode(), "提交商品信息自然属性不能为空");
           AssertUtil.notBlank(itemSalesPropery.getSalesPropertys(), "提交商品信息采购属性不能为空");
           AssertUtil.notBlank(skus.getSkusInfo(), "提交商品信息SKU信息不能为空");
           //用于更新仓库商品信息
           Map<String, Object> warehouseItemInfoMap = new HashMap<>();

           String userId = aclUserAccreditInfo.getUserId();
           checkSkuInfo(skus);//检查sku参数
           //保存sku信息
           skus.setItemId(items.getId());
           skus.setSpuCode(items.getSpuCode());
           List<Skus> updateSkus = updateSkus(skus, userId, warehouseItemInfoMap);
           //根据sku启停用状态设置商品启停用状态
           //boolean isValidUpdate = setItemsIsValidBySkuStatus(items);
           //保存商品基础信息
           updateItemsBase(items, warehouseItemInfoMap);
           //保存自然属性信息
           itemNaturePropery.setItemId(items.getId());
           itemNaturePropery.setSpuCode(items.getSpuCode());
           updateItemNatureProperty(itemNaturePropery, items.getCategoryId());
           //保存采购属性信息
           updateItemSalesPropery(itemSalesPropery, items.getCategoryId());
           //商品编辑通知渠道
           itemsUpdateNoticeChannel(items, updateSkus, TrcActionTypeEnum.EDIT_ITEMS);
           //更新仓库商品信息和同步仓库
           this.updateWarehouseItemInfo(warehouseItemInfoMap, items.getSpuCode());
           //更新仓库商品启停用状态
           for (Skus s : updateSkus) {
               itemsUpdateNoticeWarehouseItemInfo(s, s.getIsValid());
           }

           //更新商品同步到企业购
           updateItemsNotifyToBusinessPurchase(items, updateSkus);
       }

    }


    /**
     * 更新商品同步到企业购
     *
     * @param items
     * @param skusList
     */
    private void updateItemsNotifyToBusinessPurchase(Items items, List<Skus> skusList) {
        BusiItems busiItems = new BusiItems();
        busiItems.setSpuCode(items.getSpuCode());
        busiItems = busiItemsService.selectOne(busiItems);
        if (null == busiItems) {
            return;
        }
        String isValid = busiItems.getIsValid();
        BeanUtils.copyProperties(items, busiItems, "id");
        busiItems.setScmIsValid(items.getIsValid());
        busiItems.setIsValid(isValid);
        //设置企业购商品分类
        setBusinessPurchaseItemCategory(items.getCategoryId(), busiItems);
        busiItemsService.updateByPrimaryKeySelective(busiItems);
        if (CollectionUtils.isEmpty(skusList)) {
            return;
        }
        List<String> skuCodes = new ArrayList<>();
        for (Skus skus : skusList) {
            skuCodes.add(skus.getSkuCode());
        }
        Example example = new Example(BusiSkus.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("skuCode", skuCodes);
        List<BusiSkus> busiSkusList = busiSkusService.selectByExample(example);
        List<BusiSkus> addBusiSkusList = new ArrayList<>();
        List<BusiSkus> updateBusiSkusList = new ArrayList<>();
        for (BusiSkus busiSkus : busiSkusList) {
            String isValid2 = busiSkus.getIsValid();
            for (Skus skus : skusList) {
                if (StringUtils.equals(busiSkus.getSkuCode(), skus.getSkuCode())) {
                    BeanUtils.copyProperties(skus, busiSkus, "id", "marketPrice");
                    if (null != skus.getMarketPrice()) {
                        busiSkus.setMarketPrice(new BigDecimal(CommonUtil.getMoneyYuan(skus.getMarketPrice())));
                    }
                    busiSkus.setScmIsValid(skus.getIsValid());
                    busiSkus.setIsValid(isValid2);
                    updateBusiSkusList.add(busiSkus);
                    break;
                }
            }
        }
        for (Skus skus : skusList) {
            boolean flag = false;
            for (BusiSkus busiSkus : busiSkusList) {
                if (StringUtils.equals(busiSkus.getSkuCode(), skus.getSkuCode())) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                BusiSkus busiSkus = new BusiSkus();
                BeanUtils.copyProperties(skus, busiSkus, "id", "marketPrice");
                if (null != skus.getMarketPrice()) {
                    busiSkus.setMarketPrice(new BigDecimal(CommonUtil.getMoneyYuan(skus.getMarketPrice())));
                }
                busiSkus.setScmIsValid(skus.getIsValid());
                busiSkus.setIsValid(ValidEnum.NOVALID.getCode());
                busiSkus.setId(GuidUtil.getNextUid("SKU"));
                addBusiSkusList.add(busiSkus);
            }
        }
        if (addBusiSkusList.size() > 0) {
            for (BusiSkus skus : addBusiSkusList) {
                busiSkusService.insert(skus);
            }
        }
        if (updateBusiSkusList.size() > 0) {
            for (BusiSkus busiSkus : updateBusiSkusList) {
                busiSkusService.updateByPrimaryKeySelective(busiSkus);
            }
        }
    }

    //更新仓库商品信息和同步仓库
    private void updateWarehouseItemInfo(Map<String, Object> warehouseItemInfoMap, String spuCode) throws Exception {
        //获取所有仓库商品信息
        WarehouseItemInfo warehouseItemInfo = new WarehouseItemInfo();
        warehouseItemInfo.setSpuCode(spuCode);
        warehouseItemInfo.setIsDelete(Integer.parseInt(ZeroToNineEnum.ZERO.getCode()));
        List<WarehouseItemInfo> warehouseItemInfoList = warehouseItemInfoService.select(warehouseItemInfo);

        //更新所有子仓库商品信息
        WmsItemInfo wmsItemInfo = new WmsItemInfo();
        wmsItemInfo.setSpuCode(spuCode);
        List<WmsItemInfo> wmsItemInfoList = wmsItemInfoService.select(wmsItemInfo);

        //如果仓库不存在该spu信息则不更新
        if (warehouseItemInfoList == null || warehouseItemInfoList.size() < 1) {
            return;
        }

        boolean flag = false;
        if (wmsItemInfoList != null && wmsItemInfoList.size() > 0) {
            flag = true;
        }

        //获取需要修改的信息
        Map<Long, WarehouseItemInfo> warehouseItemInfoMap1 = new HashMap<>();
        //子仓库需要修改的信息
        Map<Long, WmsItemInfo> wmsItemInfoMapNeedChange = new HashMap<>();

        List<Skus> updatelist = new ArrayList<>();
        if (warehouseItemInfoMap.containsKey("updateSkus")) {
            updatelist = (List<Skus>) warehouseItemInfoMap.get("updateSkus");
        }
        if (warehouseItemInfoMap.containsKey("itemNo")) {
            String itemNo = (String) warehouseItemInfoMap.get("itemNo");
            for (WarehouseItemInfo warehouseItemInfo2 : warehouseItemInfoList) {
                warehouseItemInfo2.setItemNo(itemNo);
                warehouseItemInfoMap1.put(warehouseItemInfo2.getId(), warehouseItemInfo2);
            }
        }
        for (WarehouseItemInfo warehouseItemInfo1 : warehouseItemInfoList) {
            for (Skus s : updatelist) {
                if (StringUtils.equals(s.getSkuCode(), warehouseItemInfo1.getSkuCode())) {
                    if (warehouseItemInfoMap1.containsKey(warehouseItemInfo1.getId())) {
                        warehouseItemInfo1 = warehouseItemInfoMap1.get(warehouseItemInfo1.getId());
                    }
                    warehouseItemInfo1.setItemName(s.getSkuName());
                    warehouseItemInfo1.setBarCode(s.getBarCode());
                    warehouseItemInfo1.setSpecNatureInfo(s.getSpecInfo());
                    warehouseItemInfoMap1.put(warehouseItemInfo1.getId(), warehouseItemInfo1);
                }
            }
        }

        //更新子仓库商品信息
        if (flag) {
            if (warehouseItemInfoMap.containsKey("brandId")) {
                Long brandId = (Long) warehouseItemInfoMap.get("brandId");
                for (WmsItemInfo wmsItemInfoNeedChange : wmsItemInfoList) {
                    wmsItemInfoNeedChange.setBrandId(brandId);
                    wmsItemInfoMapNeedChange.put(wmsItemInfoNeedChange.getId(), wmsItemInfoNeedChange);
                }
            }

            for (WmsItemInfo wmsItemInfoNeedChange : wmsItemInfoList) {
                for (Skus s : updatelist) {
                    if (StringUtils.equals(s.getSkuCode(), wmsItemInfoNeedChange.getSkuCode())) {
                        if (wmsItemInfoMapNeedChange.containsKey(wmsItemInfoNeedChange.getId())) {
                            wmsItemInfoNeedChange = wmsItemInfoMapNeedChange.get(wmsItemInfoNeedChange.getId());
                        }
                        wmsItemInfoNeedChange.setSkuName(s.getSkuName());
                        wmsItemInfoNeedChange.setBarCode(s.getBarCode());
                        wmsItemInfoNeedChange.setSpecNatureInfo(s.getSpecInfo());
                        wmsItemInfoMapNeedChange.put(wmsItemInfoNeedChange.getId(), wmsItemInfoNeedChange);
                    }
                }
            }

            for (Map.Entry<Long, WmsItemInfo> entry : wmsItemInfoMapNeedChange.entrySet()) {
                WmsItemInfo wmsInfo = entry.getValue();
                wmsItemInfoService.updateByPrimaryKey(wmsInfo);
            }
        }


        //更新仓库商品信息
        Map<Long, List<WarehouseItemInfo>> map = new HashMap<>();
        for (Map.Entry<Long, WarehouseItemInfo> entry : warehouseItemInfoMap1.entrySet()) {
            WarehouseItemInfo info = entry.getValue();
            warehouseItemInfoService.updateByPrimaryKey(info);
            Long key = info.getWarehouseInfoId();
            List<WarehouseItemInfo> list = new ArrayList<>();
            if (map.containsKey(key)) {
                list = map.get(key);
            }
            list.add(info);
            map.put(key, list);
        }

        //调用奇门接口同步商品
        String msg = itemsSync(map);
        if (StringUtils.isNotEmpty(msg)) {
            throw new Exception(msg);
        }
    }

    //调用奇门接口
    private String itemsSync(Map<Long, List<WarehouseItemInfo>> map) {
        try {
            for (Map.Entry<Long, List<WarehouseItemInfo>> entry : map.entrySet()) {
                ScmItemSyncRequest request = this.setItemsSynchronizeRequest(entry.getValue());
                if (request != null) {
                    AppResult<List<ScmItemSyncResponse>> result = warehouseApiService.itemSync(request);
                    if (StringUtils.equals("200", result.getAppcode())) {
                        List<ScmItemSyncResponse> list = (List<ScmItemSyncResponse>) result.getResult();
                        for (ScmItemSyncResponse r : list) {
                            if (!StringUtils.equals("200", r.getCode())) {
                                return r.getMessage();
                            }
                        }
                    } else {
                        return "开放平台接口调用错误";
                    }
                }
            }
        } catch (Exception e) {
            log.error("商品同步失败", e);
        }
        return "";
    }

    //组装信息
    private ScmItemSyncRequest setItemsSynchronizeRequest(List<WarehouseItemInfo> list) {

        WarehouseInfo warehouseInfo = warehouseInfoService.selectByPrimaryKey(list.get(0).getWarehouseInfoId());
        if (StringUtils.equals(warehouseInfo.getOperationalNature(), OperationalNatureEnum.SELF_SUPPORT.getCode())) {
            return null;
        }
        if (StringUtils.equals(warehouseInfo.getIsNoticeWarehouseItems(), ZeroToNineEnum.ZERO.getCode())) {
            return null;
        }

        List<ScmWarehouseItem> list1 = new ArrayList<>();
        ScmWarehouseItem item = null;
        for (WarehouseItemInfo info : list) {
            if (StringUtils.isEmpty(info.getWarehouseItemId())) {
                continue;
            }
            item = new ScmWarehouseItem();
            item.setItemCode(info.getSkuCode());
            item.setGoodsCode(info.getItemNo());
            item.setItemName(info.getItemName());
            item.setBarCode(info.getBarCode());
            item.setSkuProperty(info.getSpecNatureInfo());
            item.setItemType(info.getItemType());
            item.setItemId(info.getWarehouseItemId());
            list1.add(item);
        }

        if (list1.size() < 1) {
            return null;
        }

        //组装奇门接口
        ScmItemSyncRequest request = new ScmItemSyncRequest();
        request.setWarehouseItemList(list1);
        request.setOwnerCode(warehouseInfo.getWarehouseOwnerId());
        request.setActionType("update");

        return request;
    }

    /**
     * 根据sku启停用状态设置商品启停用状态
     *
     * @param items
     */
    private boolean setItemsIsValidBySkuStatus(Items items) {
        String _isValid = items.getIsValid();
        Skus skus = new Skus();
        skus.setSpuCode(items.getSpuCode());
        List<Skus> skusList = skusService.select(skus);
        AssertUtil.notEmpty(skusList, String.format("根据spu编码查询sku信息为空", items.getSpuCode()));
        boolean flag = false;//商品启用
        for (Skus skus2 : skusList) {
            if (StringUtils.equals(ValidEnum.VALID.getCode(), skus2.getIsValid())) {
                flag = true;
                break;
            }
        }
        if (flag) {
            items.setIsValid(ValidEnum.VALID.getCode());
        } else {
            items.setIsValid(ValidEnum.NOVALID.getCode());
        }
        if (StringUtils.equals(_isValid, items.getIsValid()))
            return false;
        else
            return true;
    }

    /**
     *
     * @param skus
     */
    private void checkSkuInfo(Skus skus) {
        JSONArray skuArray = JSONArray.parseArray(skus.getSkusInfo());
        if (skuArray.size() == 0) {
            throw new ParamValidException(CommonExceptionEnum.PARAM_CHECK_EXCEPTION, "提交商品信息SKU信息不能为空");
        }
        //当前需要报存sku重复校验
        List<SkuGridInfo> skuGridInfoList = JSON.parseArray(skus.getSkusInfo(), SkuGridInfo.class);
        //啟用的sku信息
        List<SkuGridInfo> skuValidInfoList = new ArrayList<>();
        //页面停用的skuCode
        List<String> skuNoValidInfoList = new ArrayList<>();

        for (SkuGridInfo s : skuGridInfoList) {
            if (s.getBarCode().indexOf(SupplyConstants.Symbol.COMMA) != -1) {
                //逗号分隔的正则
                Pattern p = Pattern.compile(COMMA_SPLIT);
                Matcher m = p.matcher(s.getBarCode());
                if (m.find()) {
                    String msg = "条形码格式异常";
                    log.error(msg);
                    throw new GoodsException(ExceptionEnum.GOODS_UPDATE_EXCEPTION, msg);
                }
            }
            if (s.getIsValid().equals(ValidEnum.VALID.getCode())) {
                skuValidInfoList.add(s);
            }
            if (s.getIsValid().equals(ValidEnum.NOVALID.getCode())) {
                if (StringUtils.isNotBlank(s.getSkuCode())) {
                    skuNoValidInfoList.add(s.getSkuCode());
                }
            }
        }
        if (!AssertUtil.collectionIsEmpty(skuValidInfoList)) {
            checkRepeatBarCode(skuValidInfoList);
        }
        for (Object obj : skuArray) {
            JSONObject jbo = (JSONObject) obj;
            AssertUtil.notBlank(jbo.getString("barCode"), "SKU条形码不能为空");
            AssertUtil.notBlank(jbo.getString("isValid"), "启停用状态不能为空");
            if (StringUtils.equals(ZeroToNineEnum.ZERO.getCode(), jbo.getString("source"))) {
                AssertUtil.notBlank(jbo.getString("skuCode"), "SKU编码不能为空");
                //查询当前sku的数据
                Skus sku = new Skus();
                sku.setSkuCode(jbo.getString("skuCode"));
                sku = skusService.selectOne(sku);
                //判断sku当前的状态
                if (null != sku) {
                    if (StringUtils.equals(sku.getIsValid(), ValidEnum.NOVALID.getCode())) {
                        //条形码校验
                        if (StringUtils.equals(jbo.getString("isValid"), ValidEnum.VALID.getCode())) {
                            if (!AssertUtil.collectionIsEmpty(skuNoValidInfoList)) {
                                checkBarcodeOnly(jbo.getString("barCode"), "", StringUtils.join(skuNoValidInfoList, SupplyConstants.Symbol.COMMA));
                            } else {
                                checkBarcodeOnly(jbo.getString("barCode"), "", "");
                            }
                        }
                    }
                }
                continue;
            }
            //条形码校验
            if (StringUtils.equals(jbo.getString("isValid"), ValidEnum.VALID.getCode())) {
                if (!AssertUtil.collectionIsEmpty(skuNoValidInfoList)) {
                    checkBarcodeOnly(jbo.getString("barCode"), "", StringUtils.join(skuNoValidInfoList, SupplyConstants.Symbol.COMMA));
                } else {
                    checkBarcodeOnly(jbo.getString("barCode"), "", "");
                }
            }
        }
    }

    /**
     * 校验编辑页面是否有重复条形码
     *
     * @param skuValidInfoList
     */
    private void checkRepeatBarCode(List<SkuGridInfo> skuValidInfoList) {
        for (int i = 0; i < skuValidInfoList.size(); i++) {
            SkuGridInfo o1 = skuValidInfoList.get(i);
            for (int j = 0; j < skuValidInfoList.size(); j++) {
                SkuGridInfo o2 = skuValidInfoList.get(j);
                if (i != j) {
                    String barCodeO1[] = StringUtils.split(o1.getBarCode(), SupplyConstants.Symbol.COMMA);
                    String barCodeO2[] = StringUtils.split(o2.getBarCode(), SupplyConstants.Symbol.COMMA);
                    boolean isFlag = false;
                    for (String barO1 : barCodeO1) {
                        for (String barO2 : barCodeO2) {
                            if (barO1.equals(barO2)) {
                                isFlag = true;
                            }
                        }
                    }
                    if (isFlag) {
                        String msg = "存在相同的条形码,请检查条形码!";
                        log.error(msg);
                        throw new GoodsException(ExceptionEnum.GOODS_UPDATE_EXCEPTION, msg);
                    }
                }
            }
        }
    }

    /**
     * 保存商品基础信息
     *
     * @param items
     * @throws Exception
     */
    private void saveItemsBase(Items items) throws Exception {
        ParamsUtil.setBaseDO(items);
        checkCategoryBrandValidStatus(items.getCategoryId(), items.getBrandId());
        checkIsQuality(items);
        AssertUtil.notNull(items.getItemType(),"商品类别不能为空");
        int count = itemsService.insert(items);
        if (count == 0) {
            String msg = String.format("保商品基础信息%s到数据库失败", JSON.toJSONString(items));
            log.error(msg);
            throw new GoodsException(ExceptionEnum.GOODS_SAVE_EXCEPTION, msg);
        }
    }

    /**
     * 保存SKU信息
     *
     * @param skus
     * @throws Exception
     */
    private List<Skus> saveSkus(Skus skus) throws Exception {
        JSONArray skuArray = JSONArray.parseArray(skus.getSkusInfo());
        List<Skus> list = new ArrayList<Skus>();
        Date sysTime = Calendar.getInstance().getTime();
        for (Object obj : skuArray) {
            JSONObject jbo = (JSONObject) obj;
            String code = serialUtilService.generateCode(SupplyConstants.Serial.SKU_LENGTH, SupplyConstants.Serial.SKU_NAME,
                    SupplyConstants.Serial.SKU_INNER, DateUtils.dateToCompactString(Calendar.getInstance().getTime()));
            Skus skus2 = new Skus();
            skus2.setSkuCode(code);
            skus2.setItemId(skus.getItemId());
            skus2.setSpuCode(skus.getSpuCode());
            skus2.setPropertyValueId(jbo.getString("propertyValueId"));
            skus2.setPropertyValue(jbo.getString("propertyValue"));
            skus2.setBarCode(jbo.getString("barCode"));
            if (StringUtils.isNotBlank(jbo.getString("weight2"))) {
                skus2.setWeight(CommonUtil.getWeightLong(jbo.getString("weight2")));
            }
            skus2.setMarketPrice(getLongValue(jbo.getString("marketPrice2")));
            skus2.setPicture(jbo.getString("picture"));
            skus2.setIsValid(jbo.getString("isValid"));
            skus2.setSkuName(jbo.getString("skuName")); // sku名称
            skus2.setSpecInfo(jbo.getString("normName")); // 规格信息保存  20171117
            skus2.setIsDeleted(ZeroToNineEnum.ZERO.getCode());
            skus2.setCreateTime(sysTime);
            skus2.setUpdateTime(sysTime);
            list.add(skus2);
        }
        int count = skusService.insertList(list);
        if (count == 0) {
            String msg = String.format("保存商品SKU信息%s到数据库失败", JSON.toJSONString(list));
            log.error(msg);
            throw new GoodsException(ExceptionEnum.GOODS_SAVE_EXCEPTION, msg);
        }
        return list;
    }

    /**
     * 保存商品自然属性
     *
     * @param itemNaturePropery
     * @param categoryId        分类ID
     */
    private void saveItemNatureProperty(ItemNaturePropery itemNaturePropery, Long categoryId) throws Exception {
        JSONArray categoryArray = JSONArray.parseArray(itemNaturePropery.getNaturePropertys());
        checkItemNatureProperty(categoryArray);
        if (categoryArray.size() > 0) {
            List<ItemNaturePropery> itemNatureProperies = new ArrayList<ItemNaturePropery>();
            Date sysTime = Calendar.getInstance().getTime();
            for (Object obj : categoryArray) {
                JSONObject jbo = (JSONObject) obj;
                ItemNaturePropery _itemNaturePropery = new ItemNaturePropery();
                _itemNaturePropery.setItemId(itemNaturePropery.getItemId());
                _itemNaturePropery.setSpuCode(itemNaturePropery.getSpuCode());
                Long propertyId = jbo.getLong("propertyId");
                _itemNaturePropery.setPropertyId(propertyId);
                Long propertyValueId = jbo.getLong("propertyValueId");
                _itemNaturePropery.setPropertyValueId(propertyValueId);
                _itemNaturePropery.setIsValid(ZeroToNineEnum.ONE.getCode());
                _itemNaturePropery.setIsDeleted(ZeroToNineEnum.ZERO.getCode());
                _itemNaturePropery.setCreateTime(sysTime);
                _itemNaturePropery.setUpdateTime(sysTime);
                //判断属性是否停用
                checkCategoryPropertyValidStatus(categoryId, propertyId, propertyValueId);
                itemNatureProperies.add(_itemNaturePropery);
            }
            int count = itemNatureProperyService.insertList(itemNatureProperies);
            if (count == 0) {
                String msg = String.format("保存商品自然属性信息%s到数据库失败", JSON.toJSONString(itemNatureProperies));
                log.error(msg);
                throw new GoodsException(ExceptionEnum.GOODS_SAVE_EXCEPTION, msg);
            }
        }
    }

    /**
     * 检查分类品牌启停用状态
     *
     * @param categoryId
     * @param brandId
     * @throws Exception
     */
    private void checkCategoryBrandValidStatus(Long categoryId, Long brandId) throws Exception {
        Category category = categoryService.selectByPrimaryKey(categoryId);
        AssertUtil.notNull(category, String.format("根据主键ID[%s]查询分类信息为空", categoryId));
        if (StringUtils.equals(ZeroToNineEnum.ZERO.getCode(), category.getIsValid())) {
            throw new GoodsException(ExceptionEnum.GOODS_DEPEND_DATA_INVALID, String.format("分类[%s]已被禁用,请选择其他分类!", category.getName()));
        }
        Brand brand = brandService.selectByPrimaryKey(brandId);
        AssertUtil.notNull(brand, String.format("根据主键ID[%s]查询品牌信息为空", brandId));
        if (StringUtils.equals(ZeroToNineEnum.ZERO.getCode(), brand.getIsValid())) {
            throw new GoodsException(ExceptionEnum.GOODS_DEPEND_DATA_INVALID, String.format("品牌[%s]已被禁用,请选择其他品牌!", brand.getName()));
        }
        CategoryBrand categoryBrand = new CategoryBrand();
        categoryBrand.setCategoryId(categoryId);
        categoryBrand.setBrandId(brandId);
        categoryBrand = categoryBrandService.selectOne(categoryBrand);
        AssertUtil.notNull(categoryBrand, String.format("分类[%s]和品牌[%s]关联关系已解除,请选择其他品牌!", category.getName(), brand.getName()));
        if (StringUtils.equals(ZeroToNineEnum.ZERO.getCode(), categoryBrand.getIsValid())) {
            throw new GoodsException(ExceptionEnum.GOODS_DEPEND_DATA_INVALID, String.format("分类[%s]关联品牌[%s]已被禁用,请选择其他品牌!", category.getName(), brand.getName()));
        }
    }


    /**
     * 校验是否质保
     *
     * @param items
     */
    private void checkIsQuality(Items items) {
        if (StringUtils.equals(items.getIsQuality(), ZeroToNineEnum.ONE.getCode())) {
            AssertUtil.notNull(items.getQualityDay(), "商品具有质保日期管理时，质保天数不能为空！");
            AssertUtil.isTrue(items.getQualityDay() >= 0, "天数不能小于0");
        } else {
            AssertUtil.isNull(items.getQualityDay(), "商品不具有质保日期管理时，质保天数必须为空！");
            items.setQualityDay(0L);
        }
    }

    /**
     * @param categoryId
     * @param propertyId
     * @param propertyValueId
     * @throws Exception
     */
    private void checkCategoryPropertyValidStatus(Long categoryId, Long propertyId, Long propertyValueId) throws Exception {
        CategoryProperty categoryProperty = getCategoryProperty(categoryId, propertyId);
        if (null == categoryProperty || StringUtils.equals(ZeroToNineEnum.ZERO.getCode(), categoryProperty.getIsValid())) {
            PropertyValue propertyValue = propertyValueService.selectByPrimaryKey(propertyValueId);
            AssertUtil.notNull(propertyValue, String.format("根据属性值ID[%s]查询属性值信息为空", propertyValueId));
            throw new GoodsException(ExceptionEnum.GOODS_DEPEND_DATA_INVALID, String.format("属性[%s]已被禁用", propertyValue.getValue()));
        }
    }

    /**
     * 根据分类ID和属性ID查询分类属性
     *
     * @param categoryId
     * @param propertyId
     * @return
     * @throws Exception
     */
    private CategoryProperty getCategoryProperty(Long categoryId, Long propertyId) throws Exception {
        CategoryProperty categoryProperty = new CategoryProperty();
        categoryProperty.setCategoryId(categoryId);
        categoryProperty.setPropertyId(propertyId);
        return categoryPropertyService.selectOne(categoryProperty);
    }

    /**
     * 自然属性校验
     *
     * @param itemNaturePropertyArray
     */
    private void checkItemNatureProperty(JSONArray itemNaturePropertyArray) {
        for (Object obj : itemNaturePropertyArray) {
            JSONObject jbo = (JSONObject) obj;
            AssertUtil.notNull(jbo.getLong("propertyId"), "商品自然属性ID不能为空");
            AssertUtil.notNull(jbo.getLong("propertyValueId"), "商品自然属性值ID不能为空");
        }
    }

    /**
     * 保存商品采购属性
     *
     * @param skuses
     */
    private void saveItemSalesPropery(ItemSalesPropery itemSalesPropery, List<Skus> skuses, Long categoryId) throws Exception {
        JSONArray itemSalesArray = JSONArray.parseArray(itemSalesPropery.getSalesPropertys());
        AssertUtil.notEmpty(itemSalesArray, "保存商品采购属性不能为空");
        List<ItemSalesPropery> itemSalesProperys = new ArrayList<ItemSalesPropery>();
        Date sysTime = Calendar.getInstance().getTime();
        for (Skus skus : skuses) {
            String[] propertyValueIdsArray = skus.getPropertyValueId().split(SKU_PROPERTY_VALUE_ID_SPLIT_SYMBOL);
            String[] propertyValuesArray = skus.getPropertyValue().split(SKU_PROPERTY_VALUE_ID_SPLIT_SYMBOL);
            for (int i = 0; i < propertyValueIdsArray.length; i++) {
                Long propertyValueId = Long.parseLong(propertyValueIdsArray[i]);
                ItemSalesPropery _itemSalesPropery = new ItemSalesPropery();
                _itemSalesPropery.setItemId(skus.getItemId());
                _itemSalesPropery.setSpuCode(skus.getSpuCode());
                _itemSalesPropery.setSkuCode(skus.getSkuCode());
                String[] _tmp = getPropertyIdAndPicture(itemSalesArray, propertyValueId);
                Long propertyId = Long.parseLong(_tmp[0]);
                _itemSalesPropery.setPropertyId(propertyId);
                _itemSalesPropery.setPropertyValueId(propertyValueId);
                _itemSalesPropery.setPropertyActualValue(propertyValuesArray[i]);
                _itemSalesPropery.setPicture(_tmp[1]);
                _itemSalesPropery.setIsValid(ZeroToNineEnum.ONE.getCode());
                _itemSalesPropery.setIsDeleted(ZeroToNineEnum.ZERO.getCode());
                _itemSalesPropery.setCreateTime(sysTime);
                _itemSalesPropery.setUpdateTime(sysTime);
                //判断属性是否停用
                checkCategoryPropertyValidStatus(categoryId, propertyId, propertyValueId);
                itemSalesProperys.add(_itemSalesPropery);
            }
        }
        int count = itemSalesProperyService.insertList(itemSalesProperys);
        if (count == 0) {
            String msg = String.format("保存商品采购属性信息%s到数据库失败", JSON.toJSONString(itemSalesProperys));
            log.error(msg);
            throw new GoodsException(ExceptionEnum.GOODS_SAVE_EXCEPTION, msg);
        }
    }

    /**
     * 获取属性值ID对应的属性ID和属性值对应的图片路径
     *
     * @param itemSalesArray
     * @param propertyValueId
     * @return
     */
    private String[] getPropertyIdAndPicture(JSONArray itemSalesArray, Long propertyValueId) {
        String[] _result = null;
        for (Object obj : itemSalesArray) {
            JSONObject jbo = (JSONObject) obj;
            if (propertyValueId.longValue() == jbo.getLong("propertyValueId").longValue()) {
                _result = new String[3];
                _result[0] = jbo.getString("propertyId");
                _result[1] = jbo.getString("picture");
                _result[2] = jbo.getString("isValid");
                break;
            }
        }
        return _result;
    }

    /**
     * 修改商品基础信息
     *
     * @param items
     * @throws Exception
     */
    private void updateItemsBase(Items items, Map<String, Object> map) throws Exception {
        AssertUtil.notNull(items.getId(), "商品ID不能为空");
        items.setUpdateTime(Calendar.getInstance().getTime());
        checkCategoryBrandValidStatus(items.getCategoryId(), items.getBrandId());
        checkIsQuality(items);
        Items items1 = itemsService.selectByPrimaryKey(items.getId());
        if (!StringUtils.equals(items1.getItemNo(), items.getItemNo())) {
            map.put("itemNo", items.getItemNo());
        }
        if (items1.getBrandId() != items.getBrandId()) {
            map.put("brandId", items.getBrandId());
        }
        AssertUtil.notNull(items.getItemType(),"商品类别不能为空");
        int count = itemsService.updateByPrimaryKeySelective(items);
        if (count == 0) {
            String msg = CommonUtil.joinStr("修改商品基础信息", JSON.toJSONString(items), "数据库操作失败").toString();
            log.error(msg);
            throw new GoodsException(ExceptionEnum.GOODS_UPDATE_EXCEPTION, msg);
        }
    }

    private Long getLongValue(String val) {
        if (StringUtils.isNotBlank(val)) {
            BigDecimal d = new BigDecimal(val);
            return CommonUtil.getMoneyLong(d);
        }
        return null;
    }

    private List<Skus> updateSkus(Skus skus, String userId, Map<String, Object> map) throws Exception {
        AssertUtil.notBlank(skus.getSpuCode(), "更新SKU信息商品SPU编码不能为空");
        JSONArray skuArray = JSONArray.parseArray(skus.getSkusInfo());
        List<Skus> addlist = new ArrayList<Skus>();
        List<Skus> updatelist = new ArrayList<Skus>();
        List<Skus> updatelist2 = new ArrayList<>();
        Date sysTime = Calendar.getInstance().getTime();
        for (Object obj : skuArray) {
            boolean flag = false;
            JSONObject jbo = (JSONObject) obj;
            Skus skus2 = new Skus();
            skus2.setItemId(skus.getItemId());
            skus2.setSpuCode(skus.getSpuCode());
            skus2.setSkuCode(jbo.getString("skuCode"));

            if (StringUtils.isNotEmpty(jbo.getString("skuCode"))) {
                Skus skus1 = new Skus();
                skus1.setSkuCode(jbo.getString("skuCode"));
                skus1 = skusService.selectOne(skus1);
                if (!StringUtils.equals(skus1.getBarCode(), jbo.getString("barCode")) ||
                        !StringUtils.equals(skus1.getSkuName(), jbo.getString("skuName"))) {
                    flag = true;
                }
            }

            skus2.setPropertyValueId(jbo.getString("propertyValueId"));
            skus2.setPropertyValue(jbo.getString("propertyValue"));
            skus2.setBarCode(jbo.getString("barCode"));
            if (StringUtils.isNotBlank(jbo.getString("weight2"))) {
                skus2.setWeight(CommonUtil.getWeightLong(jbo.getString("weight2")));
            }
            skus2.setMarketPrice(getLongValue(jbo.getString("marketPrice2")));
            skus2.setPicture(jbo.getString("picture"));
            skus2.setSpecInfo(jbo.getString("normName")); // 规格信息保存  20171117
            skus2.setIsValid(jbo.getString("isValid"));
            skus2.setUpdateTime(sysTime);
            skus2.setIsDeleted(ZeroToNineEnum.ZERO.getCode());
            skus2.setSkuName(jbo.getString("skuName")); // sku名称
            if (StringUtils.equals(ZeroToNineEnum.ONE.getCode(), jbo.getString("source"))) {//新增的数据
                String code = serialUtilService.generateCode(SupplyConstants.Serial.SKU_LENGTH, SupplyConstants.Serial.SKU_NAME,
                        SupplyConstants.Serial.SKU_INNER, DateUtils.dateToCompactString(sysTime));
                skus2.setSkuCode(code);
                addlist.add(skus2);
            } else {
                if (StringUtils.equals(ZeroToNineEnum.TWO.getCode(), jbo.getString("status"))) {//已修改
                    updatelist.add(skus2);
                    if (flag) {
                        updatelist2.add(skus2);
                    }
                }
            }
        }
        int count = 0;
        if (updatelist.size() > 0) {
            //记录sku启停用状态更新日志
            updateSkusValidStatusLog(updatelist, userId);
            count = skusService.updateSkus(updatelist);
            if (count == 0) {
                String msg = CommonUtil.joinStr("更新商品SKU", JSON.toJSONString(updatelist), "到数据库失败").toString();
                log.error(msg);
                throw new GoodsException(ExceptionEnum.GOODS_UPDATE_EXCEPTION, msg);
            }
            map.put("updateSkus", updatelist2);
        }

        if (addlist.size() > 0) {
            count = skusService.insertList(addlist);
            if (count == 0) {
                String msg = CommonUtil.joinStr("保存商品SKU", JSON.toJSONString(addlist), "到数据库失败").toString();
                log.error(msg);
                throw new GoodsException(ExceptionEnum.GOODS_SAVE_EXCEPTION, msg);
            }
        }
        /*Skus _tmp = new Skus();
        _tmp.setSpuCode(skus.getSpuCode());
        _tmp.setIsDeleted(ZeroToNineEnum.ZERO.getCode());
        List<Skus> _skus = skusService.select(_tmp);
        AssertUtil.notEmpty(_skus, String.format("根据商品SPU编码[%s]查询相关SKU为空", skus.getSpuCode()));
        return _skus;*/
        updatelist.addAll(addlist);
        return updatelist;
    }

    /**
     * 记录sku启停用状态更新日志
     *
     * @param skusList
     */
    private void updateSkusValidStatusLog(List<Skus> skusList, String userId) {
        List<String> skuCodes = new ArrayList<String>();
        for (Skus skus : skusList) {
            skuCodes.add(skus.getSkuCode());
        }
        Example example = new Example(Skus.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("skuCode", skuCodes);
        List<Skus> skusList2 = skusService.selectByExample(example);
        AssertUtil.notEmpty(skusList2, String.format("根据多个sku编码查询sku信息为空", CommonUtil.converCollectionToString(skuCodes)));
        List<Skus> tmpList = new ArrayList<Skus>();
        for (Skus skus : skusList2) {
            for (Skus skus2 : skusList) {
                if (StringUtils.equals(skus.getSkuCode(), skus2.getSkuCode())) {
                    skus2.setId(skus.getId());
                    if (!StringUtils.equals(skus.getIsValid(), skus2.getIsValid()))
                        tmpList.add(skus2);
                }
            }
        }
        if (tmpList.size() > 0) {
            for (Skus skus : tmpList) {
                //记录操作日志
                logInfoService.recordLog(skus, skus.getId().toString(), userId,
                        LogOperationEnum.UPDATE.getMessage(), String.format("SKU[%s]状态更新为%s", skus.getSkuCode(), ValidEnum.getValidEnumByCode(skus.getIsValid()).getName()), null);
            }
        }

    }

    /**
     * 更新商品自然属性
     *
     * @param itemNaturePropery
     */
    private void updateItemNatureProperty(ItemNaturePropery itemNaturePropery, Long categoryId) throws Exception {
        JSONArray categoryArray = JSONArray.parseArray(itemNaturePropery.getNaturePropertys());
        if (null != categoryArray && categoryArray.size() > 0) {
            checkItemNatureProperty(categoryArray);
            List<ItemNaturePropery> addList = new ArrayList<ItemNaturePropery>();
            List<ItemNaturePropery> updateList = new ArrayList<ItemNaturePropery>();
            List<Long> delList = new ArrayList<Long>();
            ItemNaturePropery tmp = new ItemNaturePropery();
            tmp.setSpuCode(itemNaturePropery.getSpuCode());
            tmp.setIsDeleted(ZeroToNineEnum.ZERO.getCode());
            List<ItemNaturePropery> itemNatureProperyList = itemNatureProperyService.select(tmp);
            Date sysTime = Calendar.getInstance().getTime();
            List<ItemNaturePropery> list = new ArrayList<ItemNaturePropery>();
            for (Object obj : categoryArray) {
                JSONObject jbo = (JSONObject) obj;
                ItemNaturePropery _itemNaturePropery = new ItemNaturePropery();
                _itemNaturePropery.setItemId(itemNaturePropery.getItemId());
                _itemNaturePropery.setSpuCode(itemNaturePropery.getSpuCode());
                Long propertyId = jbo.getLong("propertyId");
                _itemNaturePropery.setPropertyId(propertyId);
                Long propertyValueId = jbo.getLong("propertyValueId");
                _itemNaturePropery.setPropertyValueId(propertyValueId);
                _itemNaturePropery.setUpdateTime(sysTime);
                _itemNaturePropery.setIsValid(jbo.getString("isValid"));
                _itemNaturePropery.setIsDeleted(ZeroToNineEnum.ZERO.getCode());
                if (StringUtils.equals(ZeroToNineEnum.ZERO.getCode(), _itemNaturePropery.getIsValid())) {
                    delList.add(_itemNaturePropery.getPropertyValueId());
                } else {
                    //判断属性是否停用
                    checkCategoryPropertyValidStatus(categoryId, propertyId, propertyValueId);
                }
                list.add(_itemNaturePropery);
                Boolean flag = false;
                for (ItemNaturePropery it : itemNatureProperyList) {
                    if (_itemNaturePropery.getPropertyId().longValue() == it.getPropertyId().longValue()) {
                        updateList.add(_itemNaturePropery);
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    _itemNaturePropery.setIsDeleted(ZeroToNineEnum.ZERO.getCode());
                    _itemNaturePropery.setCreateTime(sysTime);
                    addList.add(_itemNaturePropery);
                }
            }
            for (ItemNaturePropery it : itemNatureProperyList) {
                Boolean flag = false;
                for (ItemNaturePropery it2 : list) {
                    if (it.getPropertyId().longValue() == it2.getPropertyId().longValue()) {
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    delList.add(it.getPropertyValueId());
                }
            }
            int count = 0;
            if (addList.size() > 0) {
                count = itemNatureProperyService.insertList(addList);
                if (count == 0) {
                    String msg = String.format("保存商品自然属性信息%s到数据库失败", JSON.toJSONString(addList));
                    log.error(msg);
                    throw new GoodsException(ExceptionEnum.GOODS_SAVE_EXCEPTION, msg);
                }
            }
            if (updateList.size() > 0) {
                count = itemNatureProperyService.updateItemNaturePropery(updateList);
                if (count == 0) {
                    String msg = String.format("更细商品自然属性信息%s到数据库失败", JSON.toJSONString(updateList));
                    log.error(msg);
                    throw new GoodsException(ExceptionEnum.GOODS_UPDATE_EXCEPTION, msg);
                }
            }
            if (delList.size() > 0) {
                Example example = new Example(ItemNaturePropery.class);
                Example.Criteria criteria = example.createCriteria();
                criteria.andEqualTo("spuCode", itemNaturePropery.getSpuCode());
                criteria.andIn("propertyValueId", delList);
                count = itemNatureProperyService.deleteByExample(example);
                if (count == 0) {
                    String msg = String.format("根据商品SPU编码[%s]和属性值ID[%s]删除自然属性信息", itemNaturePropery.getSpuCode(), JSON.toJSONString(delList));
                    log.error(msg);
                    throw new GoodsException(ExceptionEnum.GOODS_UPDATE_EXCEPTION, msg);
                }
            }
        } else {
            //删除所有自然属性
            Example example = new Example(ItemNaturePropery.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("spuCode", itemNaturePropery.getSpuCode());
            itemNatureProperyService.deleteByExample(example);
        }
    }

    /**
     * 更新商品采购属性
     *
     * @param itemSalesPropery
     * @param categoryId
     * @throws Exception
     */
    private void updateItemSalesPropery(ItemSalesPropery itemSalesPropery, Long categoryId) throws Exception {
        JSONArray itemSalesArray = JSONArray.parseArray(itemSalesPropery.getSalesPropertys());
        AssertUtil.notEmpty(itemSalesArray, "保存商品采购属性不能为空");
        List<ItemSalesPropery> addList = new ArrayList<ItemSalesPropery>();
        List<ItemSalesPropery> updateList = new ArrayList<ItemSalesPropery>();
        List<Long> delList = new ArrayList<Long>();
        Set<String> stopSkusList = new HashSet<String>();
        Date sysTime = Calendar.getInstance().getTime();
        List<ItemSalesPropery> list = new ArrayList<ItemSalesPropery>();
        Skus _tmp = new Skus();
        _tmp.setSpuCode(itemSalesPropery.getSpuCode());
        _tmp.setIsDeleted(ZeroToNineEnum.ZERO.getCode());
        List<Skus> skuses = skusService.select(_tmp);
        AssertUtil.notEmpty(skuses, String.format("根据商品SPU编码[%s]查询相关SKU为空", itemSalesPropery.getSpuCode()));
        for (Skus skus : skuses) {
            String[] propertyValueIdsArray = skus.getPropertyValueId().split(SKU_PROPERTY_VALUE_ID_SPLIT_SYMBOL);
            String[] propertyValuesArray = skus.getPropertyValue().split(SKU_PROPERTY_VALUE_ID_SPLIT_SYMBOL);
            List<ItemSalesPropery> _currentList = querySkuItemSalesProperys(skus.getSpuCode(), skus.getSkuCode(), null);
            for (int i = 0; i < propertyValueIdsArray.length; i++) {
                Long propertyValueId = Long.parseLong(propertyValueIdsArray[i]);
                ItemSalesPropery _itemSalesPropery = getItemSalesPropery(skus, itemSalesArray, propertyValueId, propertyValuesArray[i], sysTime);
                if (null != _itemSalesPropery) {
                    if (StringUtils.equals(ZeroToNineEnum.ZERO.getCode(), _itemSalesPropery.getIsValid())) {
                        delList.add(_itemSalesPropery.getPropertyValueId());
                        stopSkusList.add(_itemSalesPropery.getSkuCode());
                    } else {
                        //判断属性是否停用
                        checkCategoryPropertyValidStatus(categoryId, _itemSalesPropery.getPropertyId(), propertyValueId);
                    }
                    list.add(_itemSalesPropery);
                    Boolean flag = false;
                    for (ItemSalesPropery it : _currentList) {
                        if (_itemSalesPropery.getPropertyId().longValue() == it.getPropertyId().longValue() &&
                                _itemSalesPropery.getPropertyValueId().longValue() == it.getPropertyValueId().longValue()) {
                            updateList.add(_itemSalesPropery);
                            flag = true;
                            break;
                        }
                    }
                    if (!flag) {
                        _itemSalesPropery.setIsDeleted(ZeroToNineEnum.ZERO.getCode());
                        _itemSalesPropery.setCreateTime(sysTime);
                        addList.add(_itemSalesPropery);
                    }
                }
            }
            for (ItemSalesPropery it : _currentList) {
                Boolean flag = false;
                for (ItemSalesPropery it2 : list) {
                    if (it.getPropertyId().longValue() == it2.getPropertyId().longValue() &&
                            it.getPropertyValueId().longValue() == it2.getPropertyValueId().longValue()) {
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    it.setUpdateTime(sysTime);
                    it.setIsDeleted(ZeroToNineEnum.ONE.getCode());
                    updateList.add(it);
                }
            }
        }
        int count = 0;
        if (updateList.size() > 0) {
            count = itemSalesProperyService.updateItemSalesPropery(updateList);
            if (count == 0) {
                String msg = String.format("更细商品采购属性信息%s到数据库失败", JSON.toJSONString(updateList));
                log.error(msg);
                throw new GoodsException(ExceptionEnum.GOODS_UPDATE_EXCEPTION, msg);
            }
        }
        if (addList.size() > 0) {
            count = itemSalesProperyService.insertList(addList);
            if (count == 0) {
                String msg = String.format("保存商品采购属性信息%s到数据库失败", JSON.toJSONString(addList));
                log.error(msg);
                throw new GoodsException(ExceptionEnum.GOODS_SAVE_EXCEPTION, msg);
            }
        }
        if (delList.size() > 0) {
            Example example = new Example(ItemSalesPropery.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("spuCode", itemSalesPropery.getSpuCode());
            criteria.andIn("propertyValueId", delList);
            count = itemSalesProperyService.deleteByExample(example);
            if (count == 0) {
                String msg = String.format("根据商品SPU编码[%s]和属性值ID[%s]删除采购属性信息", itemSalesPropery.getSpuCode(), JSON.toJSONString(delList));
                log.error(msg);
                throw new GoodsException(ExceptionEnum.GOODS_UPDATE_EXCEPTION, msg);
            }
        }
        if (stopSkusList.size() > 0) {
            //停用SKU
            stopSku(stopSkusList);
            //停用SKU库存
            stopSkuStock(stopSkusList);
        }
    }

    private ItemSalesPropery getItemSalesPropery(Skus skus, JSONArray itemSalesArray, Long propertyValueId, String propertyValue, Date sysTime) {
        ItemSalesPropery itemSalesPropery = new ItemSalesPropery();
        itemSalesPropery.setItemId(skus.getItemId());
        itemSalesPropery.setSpuCode(skus.getSpuCode());
        itemSalesPropery.setSkuCode(skus.getSkuCode());
        String[] _tmp = getPropertyIdAndPicture(itemSalesArray, propertyValueId);
        if (null != _tmp) {
            itemSalesPropery.setPropertyId(Long.parseLong(_tmp[0]));
            itemSalesPropery.setPropertyValueId(propertyValueId);
            itemSalesPropery.setPropertyActualValue(propertyValue);
            itemSalesPropery.setPicture(_tmp[1]);
            itemSalesPropery.setUpdateTime(sysTime);
            String isValid = _tmp[2];
            itemSalesPropery.setIsValid(isValid);
            itemSalesPropery.setIsDeleted(ZeroToNineEnum.ZERO.getCode());
        } else {
            itemSalesPropery = null;
        }
        return itemSalesPropery;
    }

    /**
     * 停用SKU
     *
     * @param stopSkusList
     * @throws Exception
     */
    private void stopSku(Set<String> stopSkusList) throws Exception {
        Example example = new Example(Skus.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("skuCode", stopSkusList);
        Skus skus = new Skus();
        skus.setIsValid(ZeroToNineEnum.ZERO.getCode());
        skusService.updateByExampleSelective(skus, example);
    }

    /**
     * 停用SKU库存
     *
     * @param stopSkusList
     * @throws Exception
     */
    private void stopSkuStock(Set<String> stopSkusList) throws Exception {
        Example example = new Example(SkuStock.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("skuCode", stopSkusList);
        SkuStock skuStock = new SkuStock();
//        skuStock.setIsValid(ZeroToNineEnum.ZERO.getCode());
        skuStockService.updateByExampleSelective(skuStock, example);
    }

    /**
     * 查询sku对应的采购属性
     *
     * @param spuCode
     * @param skuCode
     * @param isDelete
     * @return
     * @throws Exception
     */
    private List<ItemSalesPropery> querySkuItemSalesProperys(String spuCode, String skuCode, String isDelete) throws Exception {
        ItemSalesPropery itemSalesPropery = new ItemSalesPropery();
        itemSalesPropery.setSpuCode(spuCode);
        itemSalesPropery.setSkuCode(skuCode);
        if (StringUtils.isNotBlank(isDelete)) {
            itemSalesPropery.setIsDeleted(isDelete);
        }
        return itemSalesProperyService.select(itemSalesPropery);
    }


    @Override
    @GoodsCacheEvict
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public AppResult updateValid(Long id, String isValid, AclUserAccreditInfo aclUserAccreditInfo) throws Exception {
        //查询数据操作权限
        selectDataAcl(id, aclUserAccreditInfo, false);

        AssertUtil.notNull(id, "商品启用/停用操作参数id不能为空");
        AssertUtil.notBlank(isValid, "商品启用/停用操作参数isValid不能为空");
        Items items2 = new Items();
        items2.setId(id);
        items2 = itemsService.selectOne(items2);

        AssertUtil.notNull(items2, String.format("根据主键ID[%s]查询商品基础信息为空", id.toString()));
        Items items = new Items();
        items.setId(id);
        items.setIsDeleted(ZeroToNineEnum.ZERO.getCode());
        String _isValid = ZeroToNineEnum.ZERO.getCode();
        if (StringUtils.equals(ZeroToNineEnum.ZERO.getCode(), isValid)) {
            _isValid = ZeroToNineEnum.ONE.getCode();
        } else {
            if (stopItemsSkusCheck(items2.getSpuCode())) {
                throw new GoodsException(ExceptionEnum.GOODS_SKU_VALID_CON_NOT_STOP, "当前SPU下还存在启用的商品,无法停用");
            }
        }
        items.setIsValid(_isValid);
        items2.setIsValid(_isValid);
        int count = itemsService.updateByPrimaryKeySelective(items);
        if (count == 0) {
            String msg = "商品启用/停用操作更新数据库失败";
            log.error(msg);
            throw new GoodsException(ExceptionEnum.GOODS_UPDATE_EXCEPTION, msg);
        }
        //更新商品相关SKU启用/停用状态
        //updateGoodsSkusValid(items2.getSpuCode(),_isValid, CommonUtil.getUserId(requestContext));
        //更新SKU库存启停用状态
        //updateSkuStockIsValid(items2.getSpuCode(), null, _isValid);
        //更新采购单明细启停用状态
        //updatePurchaseDetailIsValid(items2.getSpuCode(), null, _isValid);
        Skus _tmp = new Skus();
        _tmp.setSpuCode(items2.getSpuCode());
        _tmp.setIsDeleted(ZeroToNineEnum.ZERO.getCode());
        List<Skus> updateSkus = skusService.select(_tmp);
        AssertUtil.notEmpty(updateSkus, String.format("根据商品SPU编码[%s]查询相关SKU为空", items2.getSpuCode()));
        //商品启停用通知渠道
        itemsUpdateNoticeChannel(items2, updateSkus, TrcActionTypeEnum.ITEMS_IS_VALID);
        //记录操作日志
        String logMsg = String.format("SPU信息：SPU状态由[%s]改为[%s]", ValidEnum.getValidEnumByCode(_isValid).getName(), ValidEnum.getValidEnumByCode(isValid).getName());
        logInfoService.recordLog(items2, items.getId().toString(), aclUserAccreditInfo.getUserId(),
                LogOperationEnum.UPDATE.getMessage(), logMsg, null);
        //更新商品同步到企业购
        updateItemsNotifyToBusinessPurchase(items2, updateSkus);
        return ResultUtil.createSucssAppResult(String.format("%s商品SPU成功", ValidEnum.getValidEnumByCode(_isValid).getName()), "");
    }


    /*
         方法返回值 ：   用于商品组分页方法
    *    pageFlag  ：   商品组分页方法调用标志
    * */
    //V3.1数据权限查询,通用方法，调入分页方法分页返回给前端的值   返回值：有权限1，无权限0
    private String selectDataAcl(Long id, AclUserAccreditInfo aclUserAccreditInfo, Boolean pageFlag) {
        //用户数据权限查询
        //登录用户
        String loginPhone = aclUserAccreditInfo.getPhone();
        //数据归属创建人
        Items itemsCurrent = itemsService.selectByPrimaryKey(id);
        String userId = itemsCurrent.getCreateOperator();//归属创建人
        AclUserAccreditInfo tempAcl = new AclUserAccreditInfo();
        tempAcl.setUserId(userId);
        tempAcl = aclUserAccreditInfoService.selectOne(tempAcl);


        Example example = new Example(ItemGroupUser.class);
        example.createCriteria().andEqualTo("phoneNumber", tempAcl.getPhone());
        List<ItemGroupUser> list = itemGroupUserService.selectByExample(example);
        if (!loginPhone.equals(tempAcl.getPhone())) {
            if (list.size() == 0) {
                if (pageFlag) {
                    return ZeroToNineEnum.ZERO.getCode();
                } else {
                    String msg = "该条数据不归属任何商品组，当前用户不是创建者本人，无此操作权限";
                    throw new UserAccreditInfoException(ExceptionEnum.SYSTEM_ACCREDIT_QUERY_EXCEPTION, msg);
                }
            } else {//该条数据归属某个或多个商品组
                for (ItemGroupUser itemGroupUser : list) {
                    String itemGroupCode = itemGroupUser.getItemGroupCode();
                    Example exampleTemp = new Example(ItemGroupUser.class);
                    exampleTemp.createCriteria().andEqualTo("itemGroupCode", itemGroupCode);
                    List<ItemGroupUser> list1 = itemGroupUserService.selectByExample(exampleTemp);
                    List<String> phoneNumberList = list1.stream().map(e -> e.getPhoneNumber()).collect(Collectors.toList());
                    if (!phoneNumberList.contains(loginPhone)) {
                        continue;
                    } else {
                        for (ItemGroupUser groupUser : list1) {
                            if (StringUtils.equals(groupUser.getPhoneNumber(), loginPhone)) {
                                String isLeader = groupUser.getIsLeader();
                                String isValid = groupUser.getIsValid();
                                if (StringUtils.equals(isLeader, ZeroToNineEnum.ONE.getCode()) && StringUtils.equals(isValid, ZeroToNineEnum.ONE.getCode())) {
                                    return ZeroToNineEnum.ONE.getCode();
                                } else {
                                    continue;
                                }
                            }
                        }

                    }

                }
                if (pageFlag) {
                    return ZeroToNineEnum.ZERO.getCode();
                } else {
                    String msg = "当前数据不归属任何商品组，当前用户不是创建者本人，无此操作权限";
                    throw new UserAccreditInfoException(ExceptionEnum.SYSTEM_ACCREDIT_QUERY_EXCEPTION, msg);
                }
            }

        } else {
            return ZeroToNineEnum.ONE.getCode();
        }
    }

    /**
     * 停用自采商品检查是否存在启用的SKU
     *
     * @param spuCode
     * @return
     */
    private boolean stopItemsSkusCheck(String spuCode) {
        boolean flag = false;
        Skus skus = new Skus();
        skus.setSpuCode(spuCode);
        skus.setIsValid(ValidEnum.VALID.getCode());
        List<Skus> skusList = skusService.select(skus);
        if (skusList.size() > 0)
            flag = true;
        return flag;
    }

    private void updateGoodsSkusValid(String spuCode, String isValid, String userId) throws Exception {
        Skus skus = new Skus();
        skus.setSpuCode(spuCode);
        String _isValid = ZeroToNineEnum.ZERO.getCode();
        if (StringUtils.equals(ZeroToNineEnum.ZERO.getCode(), isValid)) {
            _isValid = ZeroToNineEnum.ONE.getCode();
        }
        skus.setIsValid(_isValid);
        List<Skus> skusList = skusService.select(skus);
        //AssertUtil.notEmpty(skusList, String.format("根据商品SPU编码[%s]查询相关SKU信息为空", spuCode));
        if (skusList.size() > 0) {
            Date sysTime = Calendar.getInstance().getTime();
            for (Skus sku : skusList) {
                sku.setIsValid(isValid);
                sku.setUpdateTime(sysTime);
            }
            int count = skusService.updateSkus(skusList);
            if (count == 0) {
                String msg = "商品SKU启用/停用操作更新数据库失败";
                log.error(msg);
                throw new GoodsException(ExceptionEnum.GOODS_UPDATE_EXCEPTION, msg);
            }
            for (Skus skus2 : skusList) {
                //记录操作日志
                logInfoService.recordLog(skus2, skus2.getId().toString(), userId,
                        LogOperationEnum.UPDATE.getMessage(), String.format("SKU[%s]状态更新为%s", skus2.getSkuCode(), ValidEnum.getValidEnumByCode(isValid).getName()), null);
            }
        }
    }

    @Override
    @GoodsCacheEvict
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void updateSkusValid(Long id, String spuCode, String isValid, AclUserAccreditInfo aclUserAccreditInfo) throws Exception {
        //数据权限查询
        Items tempItems = new Items();
        tempItems.setSpuCode(spuCode);
        tempItems = itemsService.selectOne(tempItems);
        selectDataAcl(tempItems.getId(), aclUserAccreditInfo, false);


        AssertUtil.notNull(id, "SKU启用/停用操作参数ID不能为空");
        AssertUtil.notBlank(spuCode, "SKU启用/停用操作参数spuCode不能为空");
        AssertUtil.notBlank(isValid, "SKU启用/停用操作参数isValid不能为空");
        Skus skus = new Skus();
        skus.setId(id);
        String _isValid = ZeroToNineEnum.ZERO.getCode();
        if (StringUtils.equals(ZeroToNineEnum.ZERO.getCode(), isValid)) {
            _isValid = ZeroToNineEnum.ONE.getCode();
        }
        skus.setIsValid(_isValid);
        if (StringUtils.equals(ZeroToNineEnum.ONE.getCode(), _isValid)) {
            //如果状态要改为启用 就要校验条形码
            Skus barSku = new Skus();
            barSku.setId(id);
            barSku = skusService.selectOne(barSku);
            checkBarcodeOnly(barSku.getBarCode(), "", "");
            //如果spu停用，则开启spu
            if (tempItems.getIsValid().equals(ZeroToNineEnum.ZERO.getCode())) {
                tempItems.setIsValid(ZeroToNineEnum.ONE.getCode());
                itemsService.updateByPrimaryKey(tempItems);
            }
        }
        int count = skusService.updateByPrimaryKeySelective(skus);
        if (count == 0) {
            String msg = "商品SKU启用/停用操作更新数据库失败";
            log.error(msg);
            throw new GoodsException(ExceptionEnum.GOODS_UPDATE_EXCEPTION, msg);
        }
        //更新商品启停用状态
        //updateItemsValid(spuCode, _isValid);
        Skus skus2 = new Skus();
        skus2.setId(id);
        skus2 = skusService.selectOne(skus2);
        AssertUtil.notNull(skus2, String.format("根据商品sku的ID[%s]查询SKU信息为空", id));
        //更新SKU库存启停用状态
        //updateSkuStockIsValid(spuCode, skus2.getSkuCode(), _isValid);
        //更新采购单明细启停用状态
        updatePurchaseDetailIsValid(spuCode, skus2.getSkuCode(), _isValid);
        Items items = new Items();
        items.setSpuCode(spuCode);
        items = itemsService.selectOne(items);
        AssertUtil.notNull(items, String.format("根据商品spuCode编码[%s]查询商品信息为空", spuCode));
        List<Skus> updateSkus = skusService.select(skus2);
        AssertUtil.notNull(skus2, String.format("根据商品sku的ID[%s]查询SKU信息为空", id));
        updateSkus.add(skus2);
        //商品SKU启停用通知仓库商品信息
        itemsUpdateNoticeWarehouseItemInfo(skus2, _isValid);
        //商品SKU启停用通知渠道
        itemsUpdateNoticeChannel(items, updateSkus, TrcActionTypeEnum.ITEMS_SKU_IS_VALID);
        //更新商品同步到企业购
        updateItemsNotifyToBusinessPurchase(items, updateSkus);

        //记录操作日志
        String logMsg = "";
        logMsg = String.format("商品信息：①%s:SKU状态由\"%s\"改为\"%s\"", spuCode, ValidEnum.getValidEnumByCode(_isValid).getName(), ValidEnum.getValidEnumByCode(isValid).getName());
        logInfoService.recordLog(items, items.getId().toString(), aclUserAccreditInfo.getUserId(),
                LogOperationEnum.UPDATE.getMessage(), logMsg, null);


    }

    private void itemsUpdateNoticeWarehouseItemInfo(Skus skus, String _isValid) {
        WarehouseItemInfo warehouseItemInfo = new WarehouseItemInfo();
        warehouseItemInfo.setSkuCode(skus.getSkuCode());
        warehouseItemInfo.setIsDelete(Integer.parseInt(ZeroToNineEnum.ZERO.getCode()));
        List<WarehouseItemInfo> warehouseItemInfoList = warehouseItemInfoService.select(warehouseItemInfo);
        String isValid = _isValid;
        for (WarehouseItemInfo warehouseItemInfo1 : warehouseItemInfoList) {
            int status = warehouseItemInfo1.getNoticeStatus();
            if (status == 2 && StringUtils.equals("0", isValid)) {
                return;
            }
            warehouseItemInfo1.setIsValid(Integer.parseInt(isValid));
            if (ZeroToNineEnum.ZERO.getCode().equals(isValid)) {
                int oldNoticeStatus = warehouseItemInfo1.getNoticeStatus();
                warehouseItemInfo1.setOldNoticeStatus(oldNoticeStatus);
                if (oldNoticeStatus == Integer.parseInt(ZeroToNineEnum.ZERO.getCode()) ||
                        oldNoticeStatus == Integer.parseInt(ZeroToNineEnum.ONE.getCode())) {
                    warehouseItemInfo1.setNoticeStatus(Integer.parseInt(ZeroToNineEnum.TWO.getCode()));
                    logInfoService.recordLog(warehouseItemInfo1, warehouseItemInfo1.getId().toString(), "admin",
                            LogOperationEnum.CANCEL_NOTICE.getMessage(), "商品被停用", null);
                }
            } else {
                Integer oldNoticeStatus = warehouseItemInfo1.getOldNoticeStatus();
                if (oldNoticeStatus == null) {
                    continue;
                }
                if (oldNoticeStatus == Integer.parseInt(ZeroToNineEnum.ZERO.getCode()) ||
                        oldNoticeStatus == Integer.parseInt(ZeroToNineEnum.ONE.getCode())) {
                    if (warehouseItemInfo1.getOldNoticeStatus() != null) {
                        warehouseItemInfo1.setNoticeStatus(warehouseItemInfo1.getOldNoticeStatus());
                        logInfoService.recordLog(warehouseItemInfo1, warehouseItemInfo1.getId().toString(), "admin",
                                LogOperationEnum.RECOVER_NOTICE.getMessage(), "商品重新启用", null);
                    }
                    warehouseItemInfo1.setOldNoticeStatus(null);
                }
            }
            warehouseItemInfoService.updateByPrimaryKey(warehouseItemInfo1);
        }
    }

    /**
     * 更新商品启用/停用状态
     *
     * @param spuCode
     * @param isValid
     * @throws Exception
     */
    private void updateItemsValid(String spuCode, String isValid) throws Exception {
        Items items = new Items();
        items.setSpuCode(spuCode);
        items = itemsService.selectOne(items);
        AssertUtil.notNull(items, String.format("根据商品SPU编码[%s]查询商品基础信息为空", spuCode));
        Boolean flag = false;
        if (StringUtils.equals(ZeroToNineEnum.ONE.getCode(), isValid)) {//启用SKU
            if (!StringUtils.equals(isValid, items.getIsValid())) {
                items.setIsValid(isValid);
                flag = true;
            }
        } else {//停用SKU
            //查询商品spu对应的所有启用的sku
            Skus skus = new Skus();
            skus.setSpuCode(spuCode);
            skus.setIsValid(ZeroToNineEnum.ONE.getCode());
            List<Skus> list = skusService.select(skus);
            if (list.size() == 0) {
                items.setIsValid(ZeroToNineEnum.ZERO.getCode());
                flag = true;
            }
        }
        if (flag) {
            items.setUpdateTime(Calendar.getInstance().getTime());
            int count = itemsService.updateByPrimaryKeySelective(items);
            if (count == 0) {
                String msg = String.format("更新商品基础信息%s到数据库失败", JSON.toJSONString(items));
                log.error(msg);
                throw new GoodsException(ExceptionEnum.GOODS_UPDATE_EXCEPTION, msg);
            }
        }
    }

    /**
     * 跟新SKU库存启停用状态
     *
     * @param spuCode
     * @param skuCode
     * @param isValid
     * @throws Exception
     */
    private void updateSkuStockIsValid(String spuCode, String skuCode, String isValid) throws Exception {
        Example example = new Example(SkuStock.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("spuCode", spuCode);
        if (StringUtils.isNotBlank(skuCode)) {//商品SKU启停用
            criteria.andEqualTo("skuCode", skuCode);
        }
        SkuStock skuStock = new SkuStock();
//        skuStock.setIsValid(isValid);
        skuStockService.updateByExampleSelective(skuStock, example);
    }

    /**
     * 跟新采购单明细启停用状态
     *
     * @param spuCode
     * @param skuCode
     * @param isValid
     * @throws Exception
     */
    private void updatePurchaseDetailIsValid(String spuCode, String skuCode, String isValid) throws Exception {
        Example example = new Example(PurchaseDetail.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("spuCode", spuCode);
        if (StringUtils.isNotBlank(skuCode)) {//商品SKU启停用
            criteria.andEqualTo("skuCode", skuCode);
        }
        PurchaseDetail purchaseDetail = new PurchaseDetail();
        purchaseDetail.setIsValid(isValid);
        iPurchaseDetailService.updateByExampleSelective(purchaseDetail, example);
    }


    @Override
//    @Cacheable(value = SupplyConstants.Cache.GOODS)
    public ItemsExt queryItemsInfo(String spuCode, String skuCode, AclUserAccreditInfo aclUserAccreditInfo) throws Exception {
        AssertUtil.notBlank(spuCode, "查询商品详情参数商品SPU编码supCode不能为空");
        AssertUtil.notNull(aclUserAccreditInfo, "用户授权信息为空");
        //查询商品基础信息
        Items items = new Items();
        items.setSpuCode(spuCode);
        items.setIsDeleted(ZeroToNineEnum.ZERO.getCode());
        items = itemsService.selectOne(items);
        AssertUtil.notNull(items, String.format("根据商品SPU编码[%s]查询商品基础信息为空", spuCode));
        String categoryName = categoryBiz.getCategoryName(items.getCategoryId());
        items.setCategoryName(categoryName);
        if (StringUtils.equals(items.getIsQuality(), ZeroToNineEnum.ZERO.getCode())) {
            items.setQualityDay(null);
        }
        //查询商品SKU信息
        Skus skus = new Skus();
        skus.setSpuCode(spuCode);
        if (StringUtils.isNotBlank(skuCode)) {
            skus.setSkuCode(skuCode);
        }
        skus.setIsDeleted(ZeroToNineEnum.ZERO.getCode());
        List<Skus> skuses = skusService.select(skus);
        AssertUtil.notEmpty(skuses, String.format("根据商品SPU编码[%s]查询商品SKU信息为空", spuCode));
        //设置商品重量和市场价返回值
        for (Skus s : skuses) {
            if (null != s.getWeight() && s.getWeight() >= 0) {
                s.setWeight2(CommonUtil.getWeight(s.getWeight()));
            }
            if (null != s.getMarketPrice() && s.getMarketPrice() >= 0) {
                s.setMarketPrice2(CommonUtil.fenToYuan(s.getMarketPrice()));
            }
            //调用接口查询库存
            List<ScmInventoryQueryResponse> inventoryQueryResponseList = new ArrayList<>();
            List<WarehouseInfo> warehouseInfoList = getNoticeWarehouse(s);
            if (CollectionUtils.isEmpty(warehouseInfoList)) {
                log.warn("自采SKU没有查询到可用仓库");
                continue;
            }
            List<String> skuCodes = new ArrayList<>();
            skuCodes.add(s.getSkuCode());
            List<RequestSkuStock> skuStockList = new ArrayList<>();
            //所有库存类型
            inventoryQueryResponseList = warehouseExtService.getWarehouseInventory(skuCodes, null);
            for (WarehouseInfo warehouse : warehouseInfoList) {
                RequestSkuStock skuStock = new RequestSkuStock();
                skuStock.setWarehouseName(warehouse.getWarehouseName());
                if (!AssertUtil.collectionIsEmpty(inventoryQueryResponseList)) {
                    for (ScmInventoryQueryResponse inventoryQueryResponse : inventoryQueryResponseList) {
                        if (StringUtils.equals(warehouse.getWmsWarehouseCode(), inventoryQueryResponse.getWarehouseCode())) {
                            //判断库存类型,可销售
                            if (StringUtils.equals(inventoryQueryResponse.getInventoryType(), JingdongInventoryTypeEnum.SALE.getCode())
                                    && StringUtils.equals(inventoryQueryResponse.getInventoryStatus(), JingdongInventoryStateEnum.GOOD.getCode())) {
                                skuStock.setAvailableInventory((inventoryQueryResponse.getQuantity() == null ? 0 : inventoryQueryResponse.getQuantity()) + (skuStock.getAvailableInventory() == null ? 0 : skuStock.getAvailableInventory()));
                                if (null == inventoryQueryResponse.getQuantity() && null == skuStock.getAvailableInventory()) {
                                    skuStock.setAvailableInventory(null);
                                }
                            }
//                                //判断库存类型,仓库锁定
//                                if (StringUtils.equals(inventoryQueryResponse.getInventoryType(), JingdongInventoryTypeEnum.WAREHOUSE_LOCK.getCode())) {
//                                    skuStock.setWarehouseLockInventory((inventoryQueryResponse.getTotalNum() == null ? 0 : inventoryQueryResponse.getTotalNum()) + (skuStock.getWarehouseLockInventory() == null ? 0 : skuStock.getWarehouseLockInventory()));
//                                }
//                                //判断库存类型,临期锁定
//                                if (StringUtils.equals(inventoryQueryResponse.getInventoryType(), JingdongInventoryTypeEnum.ADVENT_LOCK.getCode())) {
//                                    skuStock.setAdventLockInventory((inventoryQueryResponse.getTotalNum() == null ? 0 : inventoryQueryResponse.getTotalNum()) + (skuStock.getAdventLockInventory() == null ? 0 : skuStock.getAdventLockInventory()));
//                                }
//                                //判断库存类型,盘点锁定
//                                if (StringUtils.equals(inventoryQueryResponse.getInventoryType(), JingdongInventoryTypeEnum.INVENTORY_LOCK.getCode())) {
//                                    skuStock.setCheckLockInventory((inventoryQueryResponse.getTotalNum() == null ? 0 : inventoryQueryResponse.getTotalNum()) + (skuStock.getCheckLockInventory() == null ? 0 : skuStock.getCheckLockInventory()));
//                                }
                            //残品库存
                            if (StringUtils.equals(inventoryQueryResponse.getInventoryStatus(), JingdongInventoryStateEnum.Quality.getCode())) {
                                skuStock.setDefectiveInventory((inventoryQueryResponse.getTotalNum() == null ? 0 : inventoryQueryResponse.getTotalNum()) + (skuStock.getDefectiveInventory() == null ? 0 : skuStock.getDefectiveInventory()));
                                if (null == inventoryQueryResponse.getTotalNum() && null == skuStock.getDefectiveInventory()) {
                                    skuStock.setDefectiveInventory(null);
                                }
                            }
                        }
                    }
                }
                skuStockList.add(skuStock);
            }
            if (!AssertUtil.collectionIsEmpty(skuStockList)) {
                s.setRequestSkuStockList(skuStockList);
            }
        }
        //获取自然属性和采购属性
        Object[] objs = getItemsPropertys(spuCode);
        List<ItemNaturePropery> itemNatureProperies = (List<ItemNaturePropery>) objs[0];
        List<ItemSalesPropery> itemSalesProperies = (List<ItemSalesPropery>) objs[1];
        //返回数据组装
        ItemsExt itemsExt = new ItemsExt();
        itemsExt.setItems(items);
        itemsExt.setSkus(skuses);
        itemsExt.setItemNatureProperys(itemNatureProperies);
        itemsExt.setItemSalesProperies(itemSalesProperies);
        return itemsExt;
    }

    /**
     * sku关联的信息
     *
     * @param s
     * @return
     */
    private List<WarehouseInfo> getNoticeWarehouse(Skus s) {
        List<WarehouseInfo> warehouseInfoList = new ArrayList<>();
        //先获取到该sku关联的仓库
        WarehouseItemInfo warehouseItemInfo = new WarehouseItemInfo();
        warehouseItemInfo.setSkuCode(s.getSkuCode());
        //通知成功的状态
        warehouseItemInfo.setNoticeStatus(4);
        List<WarehouseItemInfo> warehouseItemInfoList = warehouseItemInfoService.select(warehouseItemInfo);
        if (AssertUtil.collectionIsEmpty(warehouseItemInfoList)) {
            log.warn("自采SKU" + s.getSkuCode() + "没有查询到仓库关联信息");
            return warehouseInfoList;
        }
        Set<Long> warehouseInfoIds = new HashSet<>();
        for (WarehouseItemInfo warehouseItem : warehouseItemInfoList) {
            warehouseInfoIds.add(warehouseItem.getWarehouseInfoId());
        }

        Example example = new Example(WarehouseInfo.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", warehouseInfoIds);
        criteria.andEqualTo("ownerWarehouseState", OwnerWarehouseStateEnum.NOTICE_SUCCESS.getCode());

        warehouseInfoList = warehouseInfoService.selectByExample(example);
        return warehouseInfoList;
    }

    private Object[] getItemsPropertys(String spuCode) throws Exception {
        //查询商品自然属性信息
        ItemNaturePropery itemNaturePropery = new ItemNaturePropery();
        itemNaturePropery.setSpuCode(spuCode);
        //itemNaturePropery.setIsDeleted(ZeroToNineEnum.ZERO.getCode());
        List<ItemNaturePropery> itemNatureProperies = itemNatureProperyService.select(itemNaturePropery);
        //查询商品采购属性信息
        ItemSalesPropery itemSalesPropery = new ItemSalesPropery();
        itemSalesPropery.setSpuCode(spuCode);
        //itemSalesPropery.setIsDeleted(ZeroToNineEnum.ZERO.getCode());
        List<ItemSalesPropery> itemSalesProperies = itemSalesProperyService.select(itemSalesPropery);
        AssertUtil.notEmpty(itemSalesProperies, String.format("根据商品SPU编码[%s]查询商品采购属性信息为空", spuCode));
        //设置自然属性和采购属性中文名词
        setItemNatureProperyValue(itemNatureProperies, itemSalesProperies);
        Object[] objs = new Object[2];
        objs[0] = itemNatureProperies;
        objs[1] = itemSalesProperies;
        return objs;
    }

    @Override
    @Cacheable(value = SupplyConstants.Cache.GOODS)
    public List<CategoryProperty> queryItemsCategoryProperty(String spuCode, Long categoryId) throws Exception {
        AssertUtil.notBlank(spuCode, "查询商品分类属性spuCode为空");
        AssertUtil.notNull(categoryId, "查询商品分类属性categoryId为空");
        Example example = new Example(CategoryProperty.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("categoryId", categoryId);
        //criteria.andEqualTo("isValid", ValidEnum.VALID.getCode());
        example.orderBy("propertySort").asc();
        List<CategoryProperty> categoryProperties = categoryPropertyService.selectByExample(example);
        AssertUtil.notEmpty(categoryProperties, String.format("根据分类ID[%s]查询分类属性为空", categoryId));
        //获取自然属性和采购属性
        Object[] objs = getItemsPropertys(spuCode);
        List<ItemNaturePropery> itemNatureProperies = (List<ItemNaturePropery>) objs[0];
        List<ItemSalesPropery> itemSalesProperies = (List<ItemSalesPropery>) objs[1];
        //将已经禁用的属性加入到返回的分类属性列表里面
        handlerCategoryPropertys(itemNatureProperies, itemSalesProperies, categoryProperties, categoryId);
        //设置分类属性名称
        setCategoryPropertyName(categoryProperties);
        return categoryProperties;
    }

    @Override
    @Cacheable(value = SupplyConstants.Cache.OUT_GOODS_QUERY)
    public Pagenation<ExternalItemSku> externalGoodsPage(ExternalItemSkuForm queryModel, Pagenation<ExternalItemSku> page, AclUserAccreditInfo aclUserAccreditInfo) throws Exception {
        Example example = new Example(ExternalItemSku.class);
        Example.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(queryModel.getSupplierCode())) {//供应商编号
            criteria.andEqualTo("supplierCode", queryModel.getSupplierCode());
        } else if (StringUtils.equals(queryModel.getQuerySource(), ZeroToNineEnum.ZERO.getCode())) {
            //查询到当前渠道下审核通过的一件代发供应商
            Example example2 = new Example(SupplierApply.class);
            Example.Criteria criteria2 = example2.createCriteria();
            criteria2.andEqualTo("status", ZeroToNineEnum.TWO.getCode());
            if (StringUtils.equals(queryModel.getQuerySource(), ZeroToNineEnum.ZERO.getCode())) {
                criteria2.andEqualTo("channelCode", aclUserAccreditInfo.getChannelCode());
            }
            List<SupplierApply> supplierApplyList = supplierApplyService.selectByExample(example2);
            List<String> supplierInterfaceIdList = new ArrayList<>();
            for (SupplierApply supplierApply : supplierApplyList) {
                Supplier supplier = new Supplier();
                supplier.setSupplierCode(supplierApply.getSupplierCode());
                supplier.setSupplierKindCode(SupplyConstants.Supply.Supplier.SUPPLIER_ONE_AGENT_SELLING);
                supplier = supplierService.selectOne(supplier);
                if (null != supplier) {
                    supplierInterfaceIdList.add(supplier.getSupplierInterfaceId());
                }
            }
            if (!AssertUtil.collectionIsEmpty(supplierInterfaceIdList)) {
                criteria.andIn("supplierCode", supplierInterfaceIdList);
            } else {
                return page;
            }
        }
        if (StringUtils.isNotBlank(queryModel.getSkuCode())) {//商品SKU编号
            criteria.andLike("skuCode", "%" + queryModel.getSkuCode() + "%");
        }
        if (StringUtils.isNotBlank(queryModel.getItemName())) {//商品名称
            criteria.andLike("itemName", "%" + queryModel.getItemName() + "%");
        }
       /* if (StringUtils.isNotBlank(queryModel.getWarehouse())) {//仓库名称
            criteria.andLike("warehouse", "%" + queryModel.getWarehouse() + "%");
        }*/
        if (StringUtils.isNotBlank(queryModel.getSupplierSkuCode())) {//供应商sku编号 2.0新增
            criteria.andLike("supplierSkuCode", "%" + queryModel.getSupplierSkuCode() + "%");
        }
        if (StringUtils.isNotBlank(queryModel.getBrand())) {//品牌
            criteria.andLike("brand", "%" + queryModel.getBrand() + "%");
        }
        if (StringUtils.isNotBlank(queryModel.getBarCode())) {//条形码
            criteria.andLike("barCode", "%" + queryModel.getBarCode() + "%");
        }
        //2.0新增条件最近更新时间
        if (!StringUtils.isBlank(queryModel.getStartDate())) {
            criteria.andGreaterThan("updateTime", queryModel.getStartDate());
        }
        if (!StringUtils.isBlank(queryModel.getEndDate())) {
            criteria.andLessThan("updateTime", DateUtils.formatDateTime(DateUtils.addDays(queryModel.getEndDate(), DateUtils.NORMAL_DATE_FORMAT, 1)));
        }
        //2.0新增供应商商品状态
        if (StringUtils.isNotBlank(queryModel.getState())) {//条形码
            criteria.andEqualTo("state", queryModel.getState());
        }
        example.orderBy("updateTime").desc();
        page = externalItemSkuService.pagination(example, page, queryModel);
        //setSupplierName(page.getResult());
        for (ExternalItemSku externalItemSku : page.getResult()) {
            externalItemSku.setBarCode2(externalItemSku.getBarCode());
        }
        return page;
    }

    @Override
    @Cacheable(value = SupplyConstants.Cache.OUT_GOODS)
    public List<ExternalItemSku> queryExternalItems(ExternalItemSkuForm form) {
        AssertUtil.notNull(form, "查询代发商品参数不能为空");
        ExternalItemSku externalItemSku = new ExternalItemSku();
        BeanUtils.copyProperties(form, externalItemSku);
        List<ExternalItemSku> externalItemSkuList = externalItemSkuService.select(externalItemSku);
        Set<String> supplierCodes = new HashSet<String>();
        for (ExternalItemSku externalItems : externalItemSkuList) {
            supplierCodes.add(externalItems.getSupplierCode());
            if (StringUtils.equals(externalItems.getSupplierCode(), JD_SUPPLIER_CODE)) {
                externalItems.setJdPictureUrl(externalSupplierConfig.getJdPictureUrl());
                externalItems.setCategory(getExternalItemCategory(externalItems.getCategory()));
            }
        }
        if (supplierCodes.size() > 0) {
            setSupplierInfo(supplierCodes, externalItemSkuList);
        }
        return externalItemSkuList;
    }

    /**
     * 设置代付供应商信息
     *
     * @param supplierCodes
     * @param externalItemSkuList
     */
    private void setSupplierInfo(Set<String> supplierCodes, List<ExternalItemSku> externalItemSkuList) {
        Example example = new Example(Supplier.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("supplierInterfaceId", supplierCodes);
        criteria.andEqualTo("supplierKindCode", SupplyConstants.Supply.Supplier.SUPPLIER_ONE_AGENT_SELLING);//一件代付供应商
        List<Supplier> supplierList = supplierService.selectByExample(example);
        for (ExternalItemSku externalItemSku : externalItemSkuList) {
            for (Supplier supplier : supplierList) {
                if (StringUtils.equals(externalItemSku.getSupplierCode(), supplier.getSupplierInterfaceId())) {
                    externalItemSku.setSupplierCode2(externalItemSku.getSupplierCode());
                    externalItemSku.setSupplierCode(supplier.getSupplierCode());
                    externalItemSku.setSupplierName(supplier.getSupplierName());
                    break;
                }
            }
        }
    }

    /**
     * 获取代发商品分类名称
     *
     * @param category
     * @return
     */
    private String getExternalItemCategory(String category) {
        if (StringUtils.isNotBlank(category)) {
            String[] _categorys = category.split(SupplyConstants.Symbol.SEMICOLON);
            StringBuilder sb = new StringBuilder();
            for (String _cate : _categorys) {
                sb.append(_cate).append(SupplyConstants.Symbol.XIE_GANG);
            }
            String categoryName = "";
            if (sb.length() > 0) {
                categoryName = sb.substring(0, sb.length() - 1);
            }
            return categoryName;
        }
        return "";
    }

    @Override
    public Pagenation<SupplyItemsExt> externalGoodsPage2(SupplyItemsForm queryModel, Pagenation<SupplyItemsExt> page, AclUserAccreditInfo aclUserAccreditInfo) throws Exception {
        AssertUtil.notNull(page.getPageNo(), "分页查询参数pageNo不能为空");
        AssertUtil.notNull(page.getPageSize(), "分页查询参数pageSize不能为空");
        AssertUtil.notNull(page.getStart(), "分页查询参数start不能为空");
        SupplyItemsExt supplyItems2 = new SupplyItemsExt();
        BeanUtils.copyProperties(queryModel, supplyItems2);
        Supplier supplier = new Supplier();
        //supplier.setIsValid(ValidEnum.NOVALID.getCode());//停用
        supplier.setSupplierKindCode(SupplyConstants.Supply.Supplier.SUPPLIER_ONE_AGENT_SELLING);//一件代发
        List<Supplier> supplierList = supplierService.select(supplier);
        if (!CollectionUtils.isEmpty(supplierList)) {
            List<Supplier> usedSupplierList = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            for (Supplier supplier2 : supplierList) {
                if (StringUtils.equals(ValidEnum.VALID.getCode(), supplier2.getIsValid())) {
                    usedSupplierList.add(supplier2);
                } else {
                    sb.append(supplier2.getSupplierInterfaceId()).append(SupplyConstants.Symbol.COMMA);
                    if (StringUtils.isNotBlank(queryModel.getSupplierCode())) {
                        if (StringUtils.equals(queryModel.getSupplierCode(), supplier2.getSupplierInterfaceId())) {
                            return page;
                        }
                    }
                }
            }
            if (usedSupplierList.size() == 0)
                return page;
            if (sb.length() > 0)
                supplyItems2.setStopedSupplierCode(sb.substring(0, sb.length() - 1));
        } else {
            return page;
        }
        ReturnTypeDO<Pagenation<SupplyItemsExt>> returnTypeDO = jdService.skuPage(supplyItems2, page);
        if (!returnTypeDO.getSuccess()) {
            log.error(returnTypeDO.getResultMessage());
            throw new GoodsException(ExceptionEnum.EXTERNAL_GOODS_QUERY_EXCEPTION, returnTypeDO.getResultMessage());
        }
        page = returnTypeDO.getResult();
        if (page.getResult().size() > 0) {
            setOutSupplierName(page.getResult());
        }
        return page;
    }

    /**
     * 设置一件代发供应商名称
     *
     * @param supplyItemsList
     * @throws Exception
     */
    private void setOutSupplierName(List<SupplyItemsExt> supplyItemsList) throws Exception {
        Set<String> supplierInterfaceIds = new HashSet<>();
        for (SupplyItemsExt items : supplyItemsList) {
            supplierInterfaceIds.add(items.getSupplierCode());
        }
        Example example = new Example(Supplier.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("supplierKindCode", SupplyConstants.Supply.Supplier.SUPPLIER_ONE_AGENT_SELLING);//一件代发
        criteria.andIn("supplierInterfaceId", supplierInterfaceIds);
        List<Supplier> supplierList = supplierService.selectByExample(example);
        for (String supplierInterfaceId : supplierInterfaceIds) {
            boolean bool = false;
            for (Supplier supplier : supplierList) {
                if (StringUtils.equals(supplier.getSupplierInterfaceId(), supplierInterfaceId)) {
                    bool = true;
                    break;
                }
            }
            if (!bool)
                AssertUtil.notEmpty(supplierList, String.format("查询不到接口ID为[%s]的一件代发供应商信息", supplierInterfaceId));
        }
        for (SupplyItemsExt items : supplyItemsList) {
            for (Supplier supplier : supplierList) {
                if (StringUtils.equals(items.getSupplierCode(), supplier.getSupplierInterfaceId())) {
                    items.setSupplyName(supplier.getSupplierName());
                    break;
                }
            }
        }
    }


    @Override
    @OutGoodsCacheEvict
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void saveExternalItems(String supplySkus, AclUserAccreditInfo aclUserAccreditInfo) {
        AssertUtil.notBlank(supplySkus, "新增代发商品不能为空");
        JSONArray skuArray = null;
        try {
            skuArray = JSONArray.parseArray(supplySkus);
        } catch (Exception e) {
            String msg = String.format("新增代发商品参数不是JSON格式,错误信息:%s", e.getMessage());
            log.error(msg);
            throw new GoodsException(ExceptionEnum.GOODS_SAVE_EXCEPTION, msg);
        }
        AssertUtil.notEmpty(skuArray, "新增代发商品不能为空");
        List<SupplyItems> supplyItems = new ArrayList<SupplyItems>();
        for (Object jbo : skuArray) {
            JSONObject obj = (JSONObject) jbo;
            SupplyItems items = JSON.parseObject(JSON.toJSONString(obj), SupplyItems.class);
            supplyItems.add(items);
        }
        List<ExternalItemSku> externalItemSkuList = getExternalItemSkus(supplyItems, ZeroToNineEnum.ZERO.getCode());
        List<ExternalPicture> externalPictureList = setExternalPictureQinniuPath(externalItemSkuList);
        externalItemSkuService.insertList(externalItemSkuList);
        externalPictureService.insertList(externalPictureList);
        updateSupplyItemsUsedStatus(externalItemSkuList);
        //上传代发商品图片到七牛
        uploadExternalPictureToQinniu(externalPictureList);
        //记录操作日志
        List<String> newIds = new ArrayList<String>();
        for (ExternalItemSku externalItemSku : externalItemSkuList) {
            newIds.add(externalItemSku.getId().toString());
        }
        logInfoService.recordLogs(new ExternalItemSku(), aclUserAccreditInfo.getUserId(),
                LogOperationEnum.ADD.getMessage(), null, null, newIds);
    }

    /**
     * 设置代发商品图片的七牛存储路径
     *
     * @param externalItemSkuList
     */
    private List<ExternalPicture> setExternalPictureQinniuPath(List<ExternalItemSku> externalItemSkuList) {
        List<ExternalPicture> externalPictureList = new ArrayList<>();
        for (ExternalItemSku externalItemSku : externalItemSkuList) {
            if (StringUtils.isNotBlank(externalItemSku.getMainPictrue())) {
                //设置商品主图的七牛路径
                StringBuilder sb = new StringBuilder();
                String[] mainPics = externalItemSku.getMainPictrue().split(SupplyConstants.Symbol.COMMA);
                for (String mainPicUrl : mainPics) {
                    //文件类型
                    String suffix = mainPicUrl.substring(mainPicUrl.lastIndexOf(SupplyConstants.Symbol.FILE_NAME_SPLIT) + 1);
                    String fileName = String.format("%s%s%s%s", EXTERNAL_QINNIU_PATH, GuidUtil.getNextUid(String.valueOf(System.nanoTime())), SupplyConstants.Symbol.FILE_NAME_SPLIT, suffix);
                    sb.append(fileName).append(SupplyConstants.Symbol.COMMA);
                    externalPictureList.add(getExternalPicture(externalItemSku, fileName, mainPicUrl));
                }
                if (sb.length() > 0) {
                    externalItemSku.setMainPictrue2(sb.substring(0, sb.length() - 1));
                }
                //设置商品详情图的七牛路径
                StringBuilder sb2 = new StringBuilder();
                String[] detailPics = externalItemSku.getDetailPictrues().split(SupplyConstants.Symbol.COMMA);
                for (String detailPicUrl : detailPics) {
                    //文件类型
                    String suffix = detailPicUrl.substring(detailPicUrl.lastIndexOf(SupplyConstants.Symbol.FILE_NAME_SPLIT) + 1);
                    String fileName = String.format("%s%s%s%s", EXTERNAL_QINNIU_PATH, GuidUtil.getNextUid(String.valueOf(System.nanoTime())), SupplyConstants.Symbol.FILE_NAME_SPLIT, suffix);
                    sb2.append(fileName).append(SupplyConstants.Symbol.COMMA);
                    externalPictureList.add(getExternalPicture(externalItemSku, fileName, detailPicUrl));
                }
                if (sb2.length() > 0) {
                    externalItemSku.setDetailPictrues2(sb2.substring(0, sb2.length() - 1));
                }
            }
        }
        return externalPictureList;
    }


    private ExternalPicture getExternalPicture(ExternalItemSku externalItemSku, String fileName, String url) {
        ExternalPicture externalPicture = new ExternalPicture();
        externalPicture.setSupplierCode(externalItemSku.getSupplierCode());
        externalPicture.setSkuCode(externalItemSku.getSkuCode());
        externalPicture.setSupplierSkuCode(externalItemSku.getSupplierSkuCode());
        externalPicture.setStatus(Integer.parseInt(ZeroToNineEnum.ZERO.getCode()));
        if (StringUtils.equals(SupplyConstants.Order.SUPPLIER_LY_CODE, externalItemSku.getSupplierCode())) {//粮油代发商品
            externalPicture.setUrl(url);
        } else if (StringUtils.equals(SupplyConstants.Order.SUPPLIER_JD_CODE, externalItemSku.getSupplierCode())) {//京东代发商品
            externalPicture.setUrl(String.format("%s%s%s", externalSupplierConfig.getJdPictureUrl(), JING_DONG_PIC_N_12, url));
        }
        externalPicture.setFilePath(fileName);
        Date currentDate = Calendar.getInstance().getTime();
        externalPicture.setCreateTime(currentDate);
        externalPicture.setUpdateTime(currentDate);
        return externalPicture;
    }

    /**
     * 上传代发商品图片到七牛
     *
     * @param externalPictureList
     */
    private void uploadExternalPictureToQinniu(List<ExternalPicture> externalPictureList) {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(EXECUTOR_SIZE);
                        for (ExternalPicture externalPicture : externalPictureList) {
                            Future future = fixedThreadPool.submit(new Callable<Object>() {
                                @Override
                                public Object call() throws Exception {
                                    String key = qinniuBiz.fetch(externalPicture.getUrl(), externalPicture.getFilePath());
                                    if (StringUtils.isNotBlank(key)) {
                                        externalPicture.setStatus(Integer.parseInt(ZeroToNineEnum.ONE.getCode()));
                                        externalPicture.setUpdateTime(Calendar.getInstance().getTime());
                                        externalPictureService.updateByPrimaryKeySelective(externalPicture);
                                    }
                                    return null;
                                }
                            });
                            try {
                                future.get();
                            } catch (InterruptedException e) {
                                log.error("代发商品图片上传七牛线程中断异常", e);
                            } catch (ExecutionException e) {
                                log.error("代发商品图片上传七牛线程执行异常", e);
                            } catch (Exception e) {
                                log.error("代发商品图片上传七牛线程任务异常", e);
                            }
                        }
                        fixedThreadPool.shutdown();
                        fixedThreadPool = null;
                    }
                }
        ).start();
    }

    /**
     * 代发商品更新通知渠道
     *
     * @param oldExternalItemSkuList
     * @param externalItemSkuList
     * @param trcActionTypeEnum
     */
    private void externalItemsUpdateNoticeChannel(List<ExternalItemSku> oldExternalItemSkuList, List<ExternalItemSku> externalItemSkuList, TrcActionTypeEnum trcActionTypeEnum) {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ToGlyResultDO toGlyResultDO = trcBiz.sendExternalItemSkuUpdation(trcActionTypeEnum, oldExternalItemSkuList, externalItemSkuList, System.currentTimeMillis());
                            if (StringUtils.equals(SuccessFailureEnum.SUCCESS.getCode(), toGlyResultDO.getStatus())) {
                                if (log.isInfoEnabled())
                                    log.info("更新代发商品通知渠道成功");
                            } else {
                                log.error(String.format("更新代发商品通知渠道失败,错误信息:%s", toGlyResultDO.getMsg()));
                            }
                        } catch (Exception e) {
                            String msg = String.format("更新代发商品通知渠道异常,异常信息:%s", e.getMessage());
                            log.error(msg, e);
                        }
                    }
                }
        ).start();
    }

    @Override
    @OutGoodsCacheEvict
    public void updateExternalItemsValid(Long id, String isValid, AclUserAccreditInfo aclUserAccreditInfo) throws Exception {
        AssertUtil.notNull(id, "代发商品启用/停用操作参数id不能为空");
        AssertUtil.notBlank(isValid, "代发商品启用/停用操作参数isValid不能为空");
        ExternalItemSku externalItemSku = externalItemSkuService.selectByPrimaryKey(id);
        AssertUtil.notNull(externalItemSku, String.format("根据主键ID[%s]查询代发商品为空", id));
        String _isValid = ZeroToNineEnum.ZERO.getCode();
        if (StringUtils.equals(ZeroToNineEnum.ZERO.getCode(), isValid)) {
            _isValid = ZeroToNineEnum.ONE.getCode();
        }
        ExternalItemSku externalItemSku2 = new ExternalItemSku();
        externalItemSku2.setId(id);
        externalItemSku2.setIsValid(_isValid);
        int count = externalItemSkuService.updateByPrimaryKeySelective(externalItemSku2);
        if (count == 0) {
            String msg = "代发商品启用/停用操作更新数据库失败";
            log.error(msg);
            throw new GoodsException(ExceptionEnum.GOODS_UPDATE_EXCEPTION, msg);
        }
        ExternalItemSku externalItemSku3 = externalItemSkuService.selectByPrimaryKey(id);
        AssertUtil.notNull(externalItemSku3, String.format("根据主键ID[%s]查询代发商品为空", id));
        List<ExternalItemSku> oldExternalItemSkuList = new ArrayList<ExternalItemSku>();
        List<ExternalItemSku> externalItemSkuList = new ArrayList<ExternalItemSku>();
        oldExternalItemSkuList.add(externalItemSku);
        externalItemSkuList.add(externalItemSku3);
        //代发商品启停用通知渠道
        externalItemsUpdateNoticeChannel(oldExternalItemSkuList, externalItemSkuList, TrcActionTypeEnum.EXTERNAL_ITEMS_IS_VALID);
        //记录操作日志
        logInfoService.recordLog(externalItemSku3, id.toString(), aclUserAccreditInfo.getUserId(), LogOperationEnum.UPDATE.getMessage(),
                String.format("SKU[%s]状态更新为%s", externalItemSku3.getSkuCode(), ValidEnum.getValidEnumByCode(_isValid).getName()), null);
    }

    @Override
    @OutGoodsCacheEvict
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void updateExternalItems(ExternalItemSku externalItemSku, AclUserAccreditInfo aclUserAccreditInfo) {
        AssertUtil.notNull(externalItemSku, "更新代发商品不能为空");
        AssertUtil.notNull(externalItemSku.getId(), "更新代发商品ID不能为空");
        ExternalItemSku externalItemSku2 = externalItemSkuService.selectByPrimaryKey(externalItemSku.getId());
        AssertUtil.notNull(externalItemSku2, String.format("根据主键ID[%s]查询代发商品为空", externalItemSku.getId()));
        int count = externalItemSkuService.updateByPrimaryKeySelective(externalItemSku);
        if (count == 0) {
            String msg = String.format("根据主键ID[%s]更新代发商品%s失败", externalItemSku.getId(), JSONObject.toJSON(externalItemSku));
            log.error(msg);
            throw new GoodsException(ExceptionEnum.EXTERNAL_GOODS_UPDATE_EXCEPTION, msg);
        }
        ExternalItemSku externalItemSku3 = externalItemSkuService.selectByPrimaryKey(externalItemSku.getId());
        AssertUtil.notNull(externalItemSku3, String.format("根据主键ID[%s]查询代发商品为空", externalItemSku.getId()));
        List<ExternalItemSku> oldExternalItemSkuList = new ArrayList<ExternalItemSku>();
        List<ExternalItemSku> externalItemSkuList = new ArrayList<ExternalItemSku>();
        oldExternalItemSkuList.add(externalItemSku2);
        externalItemSkuList.add(externalItemSku3);
        //代发商品编辑通知渠道
        externalItemsUpdateNoticeChannel(oldExternalItemSkuList, externalItemSkuList, TrcActionTypeEnum.EDIT_EXTERNAL_ITEMS);
        //记录操作日志
        logInfoService.recordLog(externalItemSku3, externalItemSku3.getId().toString(), aclUserAccreditInfo.getUserId(), LogOperationEnum.UPDATE.getMessage(), null, null);
    }

    @Override
    @OutGoodsCacheEvict
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void supplierSkuUpdateNotice(String updateSupplierSkus) {
        AssertUtil.notBlank(updateSupplierSkus, "根据供应商sku更新通知更新一件代发商品供应商更新的sku参数updateSupplierSkus不能为空");
        JSONArray skusArray = null;
        try {
            skusArray = JSONArray.parseArray(updateSupplierSkus);
        } catch (JSONException e) {
            String msg = String.format("根据供应商sku更新通知更新一件代发商品供应商更新的sku参数updateSupplierSkus不是json数组格式,错误信息:%s", e.getMessage());
            log.error(msg);
            throw new GoodsException(ExceptionEnum.EXTERNAL_GOODS_UPDATE_EXCEPTION, msg);
        }
        AssertUtil.notEmpty(skusArray, "根据供应商sku更新通知更新一件代发商品供应商更新的sku参数updateSupplierSkus不能为空");
        List<String> supplySkuList = new ArrayList<>();
        List<SupplyItems> supplyItems = new ArrayList<SupplyItems>();
        for (Object obj : skusArray) {
            JSONObject jbo = (JSONObject) obj;
            SupplyItems supplyItems2 = jbo.toJavaObject(SupplyItems.class);
            supplyItems2.setSkuName(jbo.getString("name"));
            supplyItems2.setBrand(jbo.getString("brandName"));
            supplyItems.add(supplyItems2);
            supplySkuList.add(supplyItems2.getSupplySku());
        }
        Example example2 = new Example(ExternalItemSku.class);
        Example.Criteria criteria2 = example2.createCriteria();
        criteria2.andIn("supplierSkuCode", supplySkuList);
        List<ExternalItemSku> oldExternalItemSkuList = externalItemSkuService.selectByExample(example2);
        if (CollectionUtils.isEmpty(oldExternalItemSkuList)) {
            return;
        }
        List<ExternalItemSku> externalItemSkuList = getExternalItemSkus(supplyItems, ZeroToNineEnum.ONE.getCode());
        List<ExternalPicture> updatePicList = new ArrayList<>();
        for (ExternalItemSku externalItemSku : externalItemSkuList) {
            for (ExternalItemSku externalItemSku2 : oldExternalItemSkuList) {
                if (StringUtils.equals(externalItemSku.getSupplierCode(), externalItemSku2.getSupplierCode()) &&
                        StringUtils.equals(externalItemSku.getSupplierSkuCode(), externalItemSku2.getSupplierSkuCode())) {
                    externalItemSku.setSkuCode(externalItemSku2.getSkuCode());
                    externalItemSku.setMainPictrue2(externalItemSku2.getMainPictrue2());
                    externalItemSku.setDetailPictrues2(externalItemSku2.getDetailPictrues2());
                    //更新代发商品图片
                    List<ExternalPicture> tmpList = updateExternalPicture(externalItemSku, externalItemSku2);
                    if (!CollectionUtils.isEmpty(tmpList)) {
                        updatePicList.addAll(tmpList);
                    }
                }
            }
            ExternalItemSku oldItemSku = new ExternalItemSku();
            oldItemSku.setSupplierSkuCode(externalItemSku.getSupplierSkuCode());
            oldItemSku = externalItemSkuService.selectOne(oldItemSku);

            Example example = new Example(ExternalItemSku.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("supplierSkuCode", externalItemSku.getSupplierSkuCode());
            int count = externalItemSkuService.updateByExampleSelective(externalItemSku, example);
            if (count == 0) {
                String msg = String.format("根据供应商SKU编号[%s]更新代发商品%s失败", externalItemSku.getSupplierSkuCode(), JSONObject.toJSON(externalItemSku));
                log.error(msg);
                throw new GoodsException(ExceptionEnum.EXTERNAL_GOODS_UPDATE_EXCEPTION, msg);
            } else {
                try {
                    ExternalItemSku newItemSku = new ExternalItemSku();
                    newItemSku.setSupplierSkuCode(externalItemSku.getSupplierSkuCode());
                    newItemSku = externalItemSkuService.selectOne(newItemSku);

                    //记录同步日志
                    List<String> ids = new ArrayList<>();
                    ids.add(String.valueOf(newItemSku.getId()));
                    logInfoService.recordLogs(new ExternalItemSku(), LogInfoBiz.ADMIN_SIGN,
                            LogOperationEnum.SYNCHRONIZE.getMessage(), "", null, ids);
                    boolean isLog = false;
                    //记录改动日志
                    if (!StringUtils.equals(oldItemSku.getState(), newItemSku.getState())) {
                        isLog = true;
                        logInfoService.recordLogs(new ExternalItemSku(), newItemSku.getSupplierName(),
                                LogOperationEnum.RENEWAL.getMessage(), "商品状态由" + StateEnum.getStateEnumByCode(oldItemSku.getState()).getName() + "修改为" + StateEnum.getStateEnumByCode(newItemSku.getState()).getName(), null, ids);

                    }
                    if (oldItemSku.getSupplyPrice().longValue() != newItemSku.getSupplyPrice().longValue()) {
                        isLog = true;
                        logInfoService.recordLogs(new ExternalItemSku(), newItemSku.getSupplierName(),
                                LogOperationEnum.RENEWAL.getMessage(), "供货价由" + CommonUtil.fenToYuan(oldItemSku.getSupplyPrice()) + "修改为" + CommonUtil.fenToYuan(newItemSku.getSupplyPrice()), null, ids);
                    }
                    if (StringUtils.equals(externalItemSku.getUpdateFlag(), ZeroToNineEnum.ONE.getCode())) {
                        if (!isLog) {
                            logInfoService.recordLogs(new ExternalItemSku(), externalItemSku.getSupplierName(),
                                    LogOperationEnum.RENEWAL.getMessage(), "", null, ids);
                        }
                    }
                } catch (Exception e) {
                    log.error("日志记录失败", e);
                }

            }
        }
        //上传代发商品图片到七牛
        if (updatePicList.size() > 0) {
            uploadExternalPictureToQinniu(updatePicList);
        }

        /*List<ExternalItemSku> oldExternalItemSkuList2 = externalItemSkuService.selectByExample(example2);
        AssertUtil.notEmpty(oldExternalItemSkuList2, String.format("根据多个供应商skuCode[%s]查询代发商品为空", CommonUtil.converCollectionToString(supplySkuList)));*/
        //代发商品更新通知渠道
        externalItemsUpdateNoticeChannel(oldExternalItemSkuList, externalItemSkuList, TrcActionTypeEnum.DAILY_EXTERNAL_ITEMS_UPDATE);
    }

    /**
     * 更新代发商品图片
     *
     * @param externalItemSku    新代发商品对象
     * @param oldExternalItemSku 就代发商品对象
     */
    private List<ExternalPicture> updateExternalPicture(ExternalItemSku externalItemSku, ExternalItemSku oldExternalItemSku) {
        //获取主图需要更新的图片
        List<ExternalPicture> mainPicList = new ArrayList<>();
        //获取主图需要更新的图片
        List<ExternalPicture> detailPicList = new ArrayList<>();
        if (StringUtils.isBlank(oldExternalItemSku.getMainPictrue2()) && StringUtils.isBlank(oldExternalItemSku.getDetailPictrues2())) {//渠道已经添加但未上传过七牛
            mainPicList = getUpdateExternalPic(externalItemSku.getMainPictrue(), externalItemSku.getMainPictrue2(), externalItemSku);
            detailPicList = getUpdateExternalPic(externalItemSku.getDetailPictrues(), externalItemSku.getDetailPictrues2(), externalItemSku);
        } else {//渠道已经添加且上传过七牛
            mainPicList = getUpdateExternalPic(externalItemSku.getMainPictrue(), oldExternalItemSku.getMainPictrue(), externalItemSku);
            detailPicList = getUpdateExternalPic(externalItemSku.getDetailPictrues(), oldExternalItemSku.getDetailPictrues(), externalItemSku);
        }
        //保存需要上传七牛的图片信息
        List<ExternalPicture> updatePicList = new ArrayList<>(mainPicList);
        updatePicList.addAll(detailPicList);
        if (CollectionUtils.isEmpty(updatePicList)) {
            return null;
        }
        externalPictureService.insertList(updatePicList);
        //重新设置主图和详情图信息
        Set<String> urls = new HashSet<>();
        for (ExternalPicture externalPicture : updatePicList) {
            urls.add(externalPicture.getUrl());
        }
        Example example = new Example(ExternalPicture.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("url", urls);
        List<ExternalPicture> externalPictureList = externalPictureService.selectByExample(example);
        StringBuilder sbMainPic = new StringBuilder();
        for (ExternalPicture externalPicture : mainPicList) {
            for (ExternalPicture externalPicture2 : externalPictureList) {
                if (StringUtils.equals(externalPicture.getUrl(), externalPicture2.getUrl()) && StringUtils.equals(externalPicture.getSupplierCode(), externalPicture2.getSupplierCode())) {
                    sbMainPic.append(externalPicture.getFilePath()).append(SupplyConstants.Symbol.COMMA);
                }
            }
        }
        if (sbMainPic.length() > 0) {
            externalItemSku.setMainPictrue2(sbMainPic.substring(0, sbMainPic.length() - 1));
        }
        StringBuilder sbDetailPic = new StringBuilder();
        for (ExternalPicture externalPicture : detailPicList) {
            for (ExternalPicture externalPicture2 : externalPictureList) {
                if (StringUtils.equals(externalPicture.getUrl(), externalPicture2.getUrl()) && StringUtils.equals(externalPicture.getSupplierCode(), externalPicture2.getSupplierCode())) {
                    sbDetailPic.append(externalPicture.getFilePath()).append(SupplyConstants.Symbol.COMMA);
                }
            }
        }
        if (sbDetailPic.length() > 0) {
            externalItemSku.setDetailPictrues2(sbDetailPic.substring(0, sbDetailPic.length() - 1));
        }
        return updatePicList;
    }

    /**
     * 获取更新的图片
     *
     * @param picture
     * @param oldPicture
     * @return
     */
    private List<ExternalPicture> getUpdateExternalPic(String picture, String oldPicture, ExternalItemSku externalItemSku) {
        List<ExternalPicture> externalPictureList = new ArrayList<>();
        if (!StringUtils.equals(picture, oldPicture)) {
            String[] mainPics = new String[]{};
            if (StringUtils.isNotBlank(picture)) {
                mainPics = picture.split(SupplyConstants.Symbol.COMMA);
            }
            String[] oldMainPics = new String[]{};
            if (StringUtils.isNotBlank(oldPicture)) {
                oldMainPics = oldPicture.split(SupplyConstants.Symbol.COMMA);
            }
            for (String mainPic : mainPics) {
                boolean flag = false;
                for (String oldMainPic : oldMainPics) {
                    if (StringUtils.equals(mainPic, oldMainPic)) {
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    //文件类型
                    String suffix = mainPic.substring(mainPic.lastIndexOf(SupplyConstants.Symbol.FILE_NAME_SPLIT) + 1);
                    String fileName = String.format("%s%s%s%s", EXTERNAL_QINNIU_PATH, GuidUtil.getNextUid(String.valueOf(System.nanoTime())), SupplyConstants.Symbol.FILE_NAME_SPLIT, suffix);
                    externalPictureList.add(getExternalPicture(externalItemSku, fileName, mainPic));
                }
            }
        }
        return externalPictureList;
    }

    @Override
    public void checkPropetyStatus(String propertyInfo) {
        AssertUtil.notBlank(propertyInfo, "属性信息不能为空");
        JSONObject propertyObj = JSONObject.parseObject(propertyInfo);
        JSONArray naturePropertys = propertyObj.getJSONArray("naturePropertys");
        JSONArray purchasPropertys = propertyObj.getJSONArray("purchasPropertys");
        //检查自然属性
        if (naturePropertys.size() > 0)
            checkPropetyStatus(naturePropertys, ZeroToNineEnum.ZERO.getCode());
        //检查采购属性
        if (purchasPropertys.size() > 0)
            checkPropetyStatus(purchasPropertys, ZeroToNineEnum.ONE.getCode());
    }

    @Override
    @Cacheable(value = SupplyConstants.Cache.OUT_GOODS)
    public List<Supplier> querySuppliers(SupplierForm supplierForm, AclUserAccreditInfo aclUserAccreditInfo) throws Exception {
        Supplier supplier = new Supplier();
        BeanUtils.copyProperties(supplierForm, supplier);
        if (StringUtils.isNotBlank(supplierForm.getStatus())) {
            if (StringUtils.isNotBlank(supplierForm.getIsValid())) {
                supplier.setIsValid(ZeroToNineEnum.ONE.getCode());
            }
            supplier.setIsDeleted(ZeroToNineEnum.ZERO.getCode());
            List<Supplier> supplierList = supplierService.select(supplier);

            Example example2 = new Example(SupplierApply.class);
            Example.Criteria criteria2 = example2.createCriteria();
            criteria2.andEqualTo("status", ZeroToNineEnum.TWO.getCode());
            criteria2.andEqualTo("channelCode", aclUserAccreditInfo.getChannelCode());
            List<SupplierApply> supplierApplyList = supplierApplyService.selectByExample(example2);
            List<Supplier> supplierResultList = new ArrayList<>();
            if (!AssertUtil.collectionIsEmpty(supplierList)) {
                for (Supplier supplierResult : supplierList) {
                    boolean isAudit = false;
                    for (SupplierApply supplierApply : supplierApplyList) {
                        if (StringUtils.equals(supplierResult.getSupplierCode(), supplierApply.getSupplierCode())) {
                            isAudit = true;
                        }
                    }
                    if (isAudit) {
                        supplierResultList.add(supplierResult);
                    }
                }
            }
            return supplierResultList;
        } else {
            if (StringUtils.isNotBlank(supplierForm.getIsValid())) {
                supplier.setIsValid(ZeroToNineEnum.ONE.getCode());
            }
            supplier.setIsDeleted(ZeroToNineEnum.ZERO.getCode());
            return supplierService.select(supplier);
        }
    }

    @Override
    public void checkBarcodeOnly(String barcode, String skuCode, String notIn) {
        AssertUtil.notBlank(barcode, "条形码不能为空");
        if (barcode.indexOf(SupplyConstants.Symbol.COMMA) != -1) {
            Pattern p = Pattern.compile(COMMA_SPLIT);
            Matcher m = p.matcher(barcode);
            if (m.find()) {
                String msg = "条形码检验格式异常";
                log.error(msg);
                throw new GoodsException(ExceptionEnum.GOODS_UPDATE_EXCEPTION, msg);
            }
        }
        String barArray[] = StringUtils.split(barcode, SupplyConstants.Symbol.COMMA);
        AssertUtil.notEmpty(Arrays.asList(barArray), "未接收到条形码");
        List<String> errorBarCode = new ArrayList<>();
        for (String barCodeString : Arrays.asList(barArray)) {
            //判断条形码的形式是否符合正则
            Pattern p = Pattern.compile(CODE_CHECK);
            Matcher m = p.matcher(barCodeString);
            if (!m.find()) {
                errorBarCode.add(barCodeString);
            }
        }
        if (!AssertUtil.collectionIsEmpty(errorBarCode)) {
            String msg = "条形码:" + StringUtils.join(errorBarCode, SupplyConstants.Symbol.COMMA) + "格式异常";
            log.error(msg);
            throw new ParamValidException(CommonExceptionEnum.PARAM_CHECK_EXCEPTION, msg);
        }
        List<String> noValidBarCodeList = new ArrayList<>();
        if (StringUtils.isNotBlank(notIn)) {
            String noValidBarCodeArray[] = StringUtils.split(notIn, SupplyConstants.Symbol.COMMA);
            noValidBarCodeList = Arrays.asList(noValidBarCodeArray);
        }
        List<String> existedCode = new ArrayList<>();
        List<String> barCodeList = new ArrayList<>();
        List<String> nowBarCode = new ArrayList<>();
        List<String> allBarCode = skusService.selectAllBarCode(noValidBarCodeList);
        String allBarCodeString = StringUtils.join(allBarCode, SupplyConstants.Symbol.COMMA);
        String allBarCodeArray[] = StringUtils.split(allBarCodeString, SupplyConstants.Symbol.COMMA);
        allBarCode = Arrays.asList(allBarCodeArray);
        if (AssertUtil.collectionIsEmpty(allBarCode)) {
            //如果数据库中sku为空
            return;
        }
        List<String> realBarCode = new ArrayList<>();
        if (!AssertUtil.collectionIsEmpty(noValidBarCodeList)) {
            //过滤停用
            //条码过滤,把页面的条码从所有启用条码中去掉
            if (!AssertUtil.collectionIsEmpty(noValidBarCodeList)) {
                for (String code : allBarCode) {
                    boolean flag = false;
                    for (String noValidCode : noValidBarCodeList) {
                        if (StringUtils.equals(code, noValidCode)) {
                            flag = true;
                            break;
                        }
                    }
                    if (!flag) {
                        realBarCode.add(code);
                    }
                }
            } else {
                realBarCode = allBarCode;
            }
        } else {
            realBarCode = allBarCode;
        }

        //新增时校验条形码
        if (StringUtils.isBlank(skuCode)) {
            for (String barCode : Arrays.asList(barArray)) {
                boolean isFlag = false;
                for (String validBar : realBarCode) {
                    if (StringUtils.equals(barCode, validBar)) {
                        isFlag = true;
                    }
                }
                if (isFlag) {
                    existedCode.add(barCode);
                }
            }
            if (!AssertUtil.collectionIsEmpty(existedCode)) {
                throw new ParamValidException(CommonExceptionEnum.PARAM_CHECK_EXCEPTION, "条形码" + StringUtils.join(existedCode, SupplyConstants.Symbol.COMMA) + "已经存在!");
            }
        } else {
            //编辑时校验方式
            //编辑时获取到新增的条形码
            getNewBarCode(skuCode, barArray, barCodeList, nowBarCode);
            if (AssertUtil.collectionIsEmpty(barCodeList) && StringUtils.isBlank(skuCode)) {
                barCodeList = Arrays.asList(barArray);
            }
            for (String barCode : barCodeList) {
                boolean isFlag = false;
                for (String validBar : realBarCode) {
                    if (StringUtils.equals(barCode, validBar)) {
                        isFlag = true;
                    }
                }
                if (isFlag) {
                    existedCode.add(barCode);
                }
            }
            if (!AssertUtil.collectionIsEmpty(existedCode)) {
                throw new ParamValidException(CommonExceptionEnum.PARAM_CHECK_EXCEPTION, "条形码" + StringUtils.join(existedCode, SupplyConstants.Symbol.COMMA) + "已经存在!");
            }
        }
    }

    private void getNewBarCode(String skuCode, String[] barArray, List<String> barCodeList, List<String> nowBarCode) {
        if (StringUtils.isNotBlank(skuCode)) {
            Skus sku = new Skus();
            sku.setSkuCode(skuCode);
            sku.setIsValid(ValidEnum.VALID.getCode());
            sku = skusService.selectOne(sku);
            if (null != sku) {
                nowBarCode = Arrays.asList(StringUtils.split(sku.getBarCode(), SupplyConstants.Symbol.COMMA));
            }
            for (int i = 0; i < barArray.length; i++) {
                boolean isFlag = false;
                for (String bar : nowBarCode) {
                    if (StringUtils.equals(barArray[i], bar)) {
                        isFlag = true;
                    }
                }
                if (!isFlag) {
                    barCodeList.add(barArray[i]);
                }
            }
        }
    }

    /**
     * @param propertyArrays
     * @param flag:0-自然属性,1-采购属性
     */
    private void checkPropetyStatus(JSONArray propertyArrays, String flag) {
        String propertyType = "";
        if (StringUtils.equals(ZeroToNineEnum.ZERO.getCode(), flag)) {
            propertyType = NATURE_PROPERTY_NAME;
        } else {
            propertyType = PURCHASE_PROPERTY_NAME;
        }
        JSONObject object = propertyArrays.getJSONObject(0);
        Long categoryId = object.getLong("categoryId");
        List<Long> propertyIds = new ArrayList<Long>();
        for (Object obj : propertyArrays) {
            JSONObject job = (JSONObject) obj;
            propertyIds.add(job.getLong("propertyId"));
        }
        Example example = new Example(CategoryProperty.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("categoryId", categoryId);
        criteria.andIn("propertyId", propertyIds);
        List<CategoryProperty> categoryPropertyList = categoryPropertyService.selectByExample(example);
        for (CategoryProperty categoryProperty : categoryPropertyList) {
            if (StringUtils.equals(ValidEnum.NOVALID.getCode(), categoryProperty.getIsValid())) {
                String propertyName = "";
                for (Object obj : propertyArrays) {
                    JSONObject job = (JSONObject) obj;
                    if (categoryProperty.getPropertyId().longValue() == job.getLong("propertyId")) {
                        propertyName = job.getString("name");
                    }
                }
                throw new GoodsException(ExceptionEnum.GOODS_DEPEND_DATA_INVALID, String.format("%s[%s]已被停用！如需继续编辑请点击\"确认\"按钮重新加载页面。", propertyType, propertyName));
            }
        }
        for (Object obj : propertyArrays) {
            JSONObject job = (JSONObject) obj;
            propertyIds.add(job.getLong("propertyId"));
            boolean bool = false;
            String propertyName = job.getString("name");
            for (CategoryProperty categoryProperty : categoryPropertyList) {
                if (categoryProperty.getCategoryId().longValue() == job.getLong("categoryId") &&
                        categoryProperty.getPropertyId().longValue() == job.getLong("propertyId")) {
                    bool = true;
                    break;
                }
            }
            if (!bool) {
                throw new GoodsException(ExceptionEnum.GOODS_DEPEND_DATA_INVALID, String.format("%s[%s]已被停用！如需继续编辑请点击\"确认\"按钮重新加载页面。", propertyType, propertyName));
            }
        }
    }


    /**
     * @param supplyItems
     * @param flag        0-新增代发商品,1-根据供应商sku更新通知更新一件代发商品
     * @return
     */
    private List<ExternalItemSku> getExternalItemSkus(List<SupplyItems> supplyItems, String flag) {
        List<ExternalItemSku> externalItemSkus = new ArrayList<ExternalItemSku>();
        Date sysDate = Calendar.getInstance().getTime();
        String sysDateStr = DateUtils.dateToCompactString(sysDate);
        Map<String, String> supplierMap = new HashMap<>();
        Set<String> supplierInterfaceIds = new HashSet<>();
        for (SupplyItems items : supplyItems) {
            supplierInterfaceIds.add(items.getSupplierCode());
            supplierMap.put(items.getSupplierCode(), items.getSupplyName());
        }
        Example example = new Example(Supplier.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("supplierKindCode", SupplyConstants.Supply.Supplier.SUPPLIER_ONE_AGENT_SELLING);//一件代发
        criteria.andIn("supplierInterfaceId", supplierInterfaceIds);
        List<Supplier> supplierList = supplierService.selectByExample(example);
        for (String supplierInterfaceId : supplierInterfaceIds) {
            boolean bool = false;
            for (Supplier supplier : supplierList) {
                if (StringUtils.equals(supplier.getSupplierInterfaceId(), supplierInterfaceId)) {
                    bool = true;
                    break;
                }
            }
            if (!bool)
                AssertUtil.notEmpty(supplierList, String.format("请先在供应商管理里面新增接口ID为[%s]的一件代发供应商[%s]", supplierInterfaceId, supplierMap.get(supplierInterfaceId)));
        }
        for (SupplyItems items : supplyItems) {
            ExternalItemSku externalItemSku = new ExternalItemSku();
            if (StringUtils.equals(flag, ZeroToNineEnum.ZERO.getCode())) {//新增代发商品
                String code = serialUtilService.generateCode(SupplyConstants.Serial.SKU_LENGTH, SupplyConstants.Serial.SKU_NAME,
                        SupplyConstants.Serial.SKU_OUTERER, sysDateStr);
                externalItemSku.setSkuCode(code);
                externalItemSku.setIsValid(ZeroToNineEnum.ONE.getCode());
                externalItemSku.setIsDeleted(ZeroToNineEnum.ZERO.getCode());
                externalItemSku.setCreateTime(sysDate);
                if (StringUtils.equals(externalItemSku.getSupplierCode(), JD_SUPPLIER_CODE)) {
                    externalItemSku.setWarehouse(externalSupplierConfig.getJdWarehouse());//京东仓库
                } else if (StringUtils.equals(externalItemSku.getSupplierCode(), LY_SUPPLIER_CODE)) {
                    externalItemSku.setWarehouse(externalSupplierConfig.getLyWarehouse());//粮油仓库
                }
                externalItemSku.setSupplyPrice(CommonUtil.getMoneyLong(items.getSupplyPrice()));
                externalItemSku.setSupplierPrice(CommonUtil.getMoneyLong(items.getSupplierPrice()));
                externalItemSku.setMarketReferencePrice(CommonUtil.getMoneyLong(items.getMarketPrice()));
            } else {
                if (null != items.getSupplyPrice())
                    externalItemSku.setSupplyPrice(items.getSupplyPrice().longValue());
                if (null != items.getSupplierPrice())
                    externalItemSku.setSupplierPrice(items.getSupplierPrice().longValue());
                if (null != items.getMarketPrice())
                    externalItemSku.setMarketReferencePrice(items.getMarketPrice().longValue());
            }
            externalItemSku.setSupplierCode(items.getSupplierCode());
            for (Supplier supplier : supplierList) {
                if (StringUtils.equals(items.getSupplierCode(), supplier.getSupplierInterfaceId())) {
                    externalItemSku.setSupplierName(supplier.getSupplierName());
                    break;
                }
            }
            externalItemSku.setSupplierSkuCode(items.getSupplySku());
            String skuName = items.getSkuName().replaceAll(AND_QUOT_REPLACE, SupplyConstants.Symbol.AND);
            externalItemSku.setItemName(skuName);
            externalItemSku.setCategory(items.getCategory());
            externalItemSku.setCategoryCode(items.getCategoryCode());
            externalItemSku.setBarCode(items.getUpc());
            //externalItemSku.setSubtitle();//商品副标题 TODO
            externalItemSku.setBrand(items.getBrand());
            externalItemSku.setCategory(items.getCategory());
            externalItemSku.setWeight(CommonUtil.getWeightLong(items.getWeight()));
            externalItemSku.setProducingArea(items.getProductArea());
            //externalItemSku.setPlaceOfDelivery();//发货地址 TODO
            externalItemSku.setItemType(items.getSkuType());
            //externalItemSku.setTariff(); //税率 TODO
            externalItemSku.setMainPictrue(items.getImagePath());//主图
            externalItemSku.setDetailPictrues(items.getDetailImagePath());//详图
            externalItemSku.setDetail(items.getIntroduction());
            //externalItemSku.setProperties();// 属性 TODO
            externalItemSku.setState(items.getState());//上下架状态
            externalItemSku.setStock(items.getStock());//库存
            externalItemSku.setUpdateFlag(items.getUpdateFlag());
            if (StringUtils.equals(flag, ZeroToNineEnum.ONE.getCode())) {
                if (items.getUpdateFlag().equals(ZeroToNineEnum.ZERO.getCode())) {
                    externalItemSku.setNotifyTime(items.getNotifyTime());
                    externalItemSku.setUpdateTime(items.getUpdateTime());
                } else {
                    externalItemSku.setNotifyTime(items.getUpdateTime());
                    externalItemSku.setUpdateTime(items.getUpdateTime());
                }
            } else {
                externalItemSku.setUpdateTime(sysDate);
            }
            externalItemSku.setMinBuyCount(items.getMinBuyCount());
            externalItemSku.setExternalId(items.getId());
            externalItemSkus.add(externalItemSku);
        }
        return externalItemSkus;
    }

    /**
     * 更新京东商品是否使用状态
     *
     * @param externalItemSkuList
     */
    private void updateSupplyItemsUsedStatus(List<ExternalItemSku> externalItemSkuList) {
        List<SkuDO> skuDOList = new ArrayList<SkuDO>();
        for (ExternalItemSku externalItemSku : externalItemSkuList) {
            SkuDO skuDO = new SkuDO();
            skuDO.setSupplySku(externalItemSku.getSupplierSkuCode());
            skuDO.setId(externalItemSku.getExternalId());
            skuDOList.add(skuDO);
        }
        ReturnTypeDO returnTypeDO = jdService.noticeUpdateSkuUsedStatus(skuDOList);
        if (!returnTypeDO.getSuccess()) {
            log.error(returnTypeDO.getResultMessage());
            throw new GoodsException(ExceptionEnum.EXTERNAL_GOODS_UPDATE_NOTICE_CHANNEL_EXCEPTION, returnTypeDO.getResultMessage());
        }
    }


    private void setCategoryPropertyName(List<CategoryProperty> categoryProperties) throws Exception {
        List<Long> propertyIds = new ArrayList<>();
        for (CategoryProperty categoryProperty : categoryProperties) {
            propertyIds.add(categoryProperty.getPropertyId());
        }
        List<Property> propertyList = new ArrayList<Property>();
        if (propertyIds.size() > 0) {
            propertyList = propertyService.queryPropertyList(propertyIds);
        }
        if (propertyList.size() > 0) {
            for (CategoryProperty c : categoryProperties) {
                for (Property p : propertyList) {
                    if (StringUtils.equals(c.getPropertyId().toString(), p.getId().toString())) {
                        c.setName(p.getName());
                        c.setTypeCode(p.getTypeCode());
                        c.setValueType(p.getValueType());
                    }
                }
            }
        }
    }


    /**
     * 将已经禁用的属性加入到返回的分类属性列表里面
     *
     * @param categoryProperties
     */
    private void handlerCategoryPropertys(List<ItemNaturePropery> itemNatures, List<ItemSalesPropery>
            itemSales, List<CategoryProperty> categoryProperties, Long categoryId) {
        for (ItemNaturePropery itemNaturePropery : itemNatures) {
            Boolean flag = false;
            for (CategoryProperty categoryProperty : categoryProperties) {
                if (itemNaturePropery.getPropertyId().longValue() == categoryProperty.getPropertyId().longValue()) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                CategoryProperty categoryProperty = new CategoryProperty();
                categoryProperty.setCategoryId(categoryId);
                categoryProperty.setPropertyId(itemNaturePropery.getPropertyId());
                categoryProperty.setName(itemNaturePropery.getPropertyName());
                categoryProperty.setTypeCode(NATURE_PROPERTY);//自然属性
                categoryProperty.setValueType(ZeroToNineEnum.ZERO.getCode());//文字类型
                categoryProperty.setIsValid(ZeroToNineEnum.ZERO.getCode());//禁用
                if (!hasCategoryProperty(categoryProperties, categoryProperty)) {
                    categoryProperties.add(categoryProperty);
                }
            }
        }
        for (ItemSalesPropery itemSalesPropery : itemSales) {
            Boolean flag = false;
            for (CategoryProperty categoryProperty : categoryProperties) {
                if (itemSalesPropery.getPropertyId().longValue() == categoryProperty.getPropertyId().longValue()) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                CategoryProperty categoryProperty = new CategoryProperty();
                categoryProperty.setCategoryId(categoryId);
                categoryProperty.setPropertyId(itemSalesPropery.getPropertyId());
                categoryProperty.setName(itemSalesPropery.getPropertyName());
                categoryProperty.setTypeCode(PURCHASE_PROPERTY);//采购属性
                categoryProperty.setValueType(ZeroToNineEnum.ZERO.getCode());//文字类型
                if (StringUtils.isNotBlank(itemSalesPropery.getPicture())) {
                    categoryProperty.setValueType(ZeroToNineEnum.ONE.getCode());//图片类型
                }
                categoryProperty.setIsValid(ZeroToNineEnum.ZERO.getCode());//禁用
                if (!hasCategoryProperty(categoryProperties, categoryProperty)) {
                    categoryProperties.add(categoryProperty);
                }
            }
        }
    }

    private Boolean hasCategoryProperty(List<CategoryProperty> categoryProperties, CategoryProperty categoryProperty) {
        for (CategoryProperty categoryProperty2 : categoryProperties) {
            if (categoryProperty2.getCategoryId().longValue() == categoryProperty.getCategoryId().longValue() &&
                    categoryProperty2.getPropertyId().longValue() == categoryProperty.getPropertyId().longValue()) {
                return true;
            }
        }
        return false;
    }


    /**
     * 设置自然属性值
     *
     * @param itemNatureProperies
     * @throws Exception
     */
    private void setItemNatureProperyValue(List<ItemNaturePropery> itemNatureProperies, List<ItemSalesPropery> itemSalesProperies) throws Exception {
        Set<Long> propertyIds = new HashSet<Long>();
        Set<Long> propertyValueIds = new HashSet<Long>();
        for (ItemNaturePropery itemNaturePropery : itemNatureProperies) {
            propertyIds.add(itemNaturePropery.getPropertyId());
            propertyValueIds.add(itemNaturePropery.getPropertyValueId());
        }
        for (ItemSalesPropery itemSalesPropery : itemSalesProperies) {
            propertyIds.add(itemSalesPropery.getPropertyId());
            propertyValueIds.add(itemSalesPropery.getPropertyValueId());
        }
        Example example = new Example(Property.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", propertyIds);
        List<Property> propertyList = propertyService.selectByExample(example);
        AssertUtil.notEmpty(propertyList, String.format("根据属性ID[%s]查询属性信息为空",
                CommonUtil.converCollectionToString(Arrays.asList(propertyIds.toArray())).toString()));
        Example example2 = new Example(PropertyValue.class);
        Example.Criteria criteria2 = example2.createCriteria();
        criteria2.andIn("id", propertyValueIds);
        List<PropertyValue> propertyValueList = propertyValueService.selectByExample(example2);
        AssertUtil.notEmpty(propertyValueList, String.format("根据属性值ID[%s]查询属性值信息为空",
                CommonUtil.converCollectionToString(Arrays.asList(propertyValueIds.toArray()))).toString());
        for (ItemNaturePropery itemNaturePropery : itemNatureProperies) {
            for (Property property : propertyList) {
                if (itemNaturePropery.getPropertyId().longValue() == property.getId().longValue()) {
                    itemNaturePropery.setPropertyName(property.getName());
                }
            }
            for (PropertyValue propertyValue : propertyValueList) {
                if (itemNaturePropery.getPropertyValueId().longValue() == propertyValue.getId().longValue()) {
                    itemNaturePropery.setPropertyValue(propertyValue.getValue());
                }
            }
        }
        for (ItemSalesPropery itemSalesPropery : itemSalesProperies) {
            for (Property property : propertyList) {
                if (itemSalesPropery.getPropertyId().longValue() == property.getId().longValue()) {
                    itemSalesPropery.setPropertyName(property.getName());
                }
            }
        }
    }


    /**
     * 条形码校验终极修改版
     *
     * @param skuInfo
     */
    @Override
    public void skuInfoBarCode(String skuInfo) {
        JSONArray skuArray = JSONArray.parseArray(skuInfo);
        if (skuArray.size() == 0) {
            throw new ParamValidException(CommonExceptionEnum.PARAM_CHECK_EXCEPTION, "提交商品信息SKU信息不能为空");
        }
        List<SkuGridInfo> skuGridInfoList = JSON.parseArray(skuInfo, SkuGridInfo.class);
        Map<String, List<SkuGridInfo>> barCodeMap = new HashMap<>();
        List<SkuGridInfo> validSkuGridInfoList = new ArrayList<>();
        List<SkuGridInfo> noValidSkuGridInfoList = new ArrayList<>();
        for (SkuGridInfo s : skuGridInfoList) {
            //先校验正则
            if (s.getBarCode().indexOf(SupplyConstants.Symbol.COMMA) != -1) {
                Pattern p = Pattern.compile(COMMA_SPLIT);
                Matcher m = p.matcher(s.getBarCode());
                if (m.find()) {
                    String msg = "条形码格式异常";
                    log.error(msg);
                    throw new GoodsException(ExceptionEnum.GOODS_UPDATE_EXCEPTION, msg);
                }
            }
            if (StringUtils.equals(s.getIsValid(), ValidEnum.VALID.getCode())) {
                validSkuGridInfoList.add(s);
            }
            if (StringUtils.equals(s.getIsValid(), ValidEnum.NOVALID.getCode())) {
                noValidSkuGridInfoList.add(s);
            }
        }
        //页面上停用的条码
        List<String> noValidBarCode = new ArrayList<>();
        //页面上启用的条码
        List<String> validBarCode = new ArrayList<>();
        //所有的条形码
        List<String> barCodeList = new ArrayList<>();
        if (!AssertUtil.collectionIsEmpty(noValidSkuGridInfoList)) {
            for (SkuGridInfo skuGridInfo : noValidSkuGridInfoList) {
                noValidBarCode.add(skuGridInfo.getBarCode());
                barCodeList.add(skuGridInfo.getBarCode());
            }
        }
        if (!AssertUtil.collectionIsEmpty(validSkuGridInfoList)) {
            for (SkuGridInfo skuGridInfo : validSkuGridInfoList) {
                validBarCode.add(skuGridInfo.getBarCode());
                barCodeList.add(skuGridInfo.getBarCode());
            }
        }
        AssertUtil.notEmpty(barCodeList, "未接收到条形码");
        //正则校验
        List<String> errorBarCode = new ArrayList<>();
        for (String barCodeString : StringUtils.split(StringUtils.join(barCodeList, SupplyConstants.Symbol.COMMA), SupplyConstants.Symbol.COMMA)) {
            //判断条形码的形式是否符合正则
            Pattern p = Pattern.compile(CODE_CHECK);
            Matcher m = p.matcher(barCodeString);
            if (!m.find()) {
                errorBarCode.add(barCodeString);
            }
        }
        if (!AssertUtil.collectionIsEmpty(errorBarCode)) {
            String msg = "条形码:" + StringUtils.join(errorBarCode, SupplyConstants.Symbol.COMMA) + "格式异常";
            log.error(msg);
            throw new ParamValidException(CommonExceptionEnum.PARAM_CHECK_EXCEPTION, msg);
        }
        //开始校验条码
        List<String> allBarCode = skusService.selectAllBarCode(new ArrayList<>());
        String allBarCodeString = StringUtils.join(allBarCode, SupplyConstants.Symbol.COMMA);
        String allBarCodeArray[] = StringUtils.split(allBarCodeString, SupplyConstants.Symbol.COMMA);
        allBarCode = Arrays.asList(allBarCodeArray);
        if (!AssertUtil.collectionIsEmpty(allBarCode)) {
            List<String> realBarCode = new ArrayList<>();
            //条码过滤,把页面的条码从所有启用条码中去掉
            if (!AssertUtil.collectionIsEmpty(noValidBarCode)) {
                for (String code : allBarCode) {
                    boolean flag = false;
                    for (String noValidCode : noValidBarCode) {
                        if (StringUtils.equals(code, noValidCode)) {
                            flag = true;
                            break;
                        }
                    }
                    if (!flag) {
                        realBarCode.add(code);
                    }
                }
            } else {
                realBarCode = allBarCode;
            }
            //条码对比校验
            List<String> existedCode = new ArrayList<>();
            for (String barCode : validBarCode) {
                boolean isFlag = false;
                for (String bar : realBarCode) {
                    if (StringUtils.equals(barCode, bar)) {
                        isFlag = true;
                    }
                }
                if (isFlag) {
                    existedCode.add(barCode);
                }
            }
            if (!AssertUtil.collectionIsEmpty(existedCode)) {
                throw new ParamValidException(CommonExceptionEnum.PARAM_CHECK_EXCEPTION, "条形码" + StringUtils.join(existedCode, SupplyConstants.Symbol.COMMA) + "已经存在!");
            }
        }
    }

    @Override
    public Response exportExternalGoods(ExternalItemSkuForm queryModel, AclUserAccreditInfo aclUserAccreditInfo) {
        try {
            Pagenation<ExternalItemSku> pageExport = new Pagenation<>();
            pageExport.setPageSize(Integer.MAX_VALUE);
            pageExport = externalGoodsPage(queryModel, pageExport, aclUserAccreditInfo);
            List<ExternalItemSku> externalItemSkuList = new ArrayList<>();
            externalItemSkuList = pageExport.getResult();
            CellDefinition skuCode = new CellDefinition("skuCode", "商品SKU编号", CellDefinition.TEXT, null, 6000);
            CellDefinition supplierName = new CellDefinition("supplierName", "供应商名称", CellDefinition.TEXT, null, 6000);
            CellDefinition supplierSkuCode = new CellDefinition("supplierSkuCode", "供应商商品SKU编号", CellDefinition.TEXT, null, 6000);
            CellDefinition itemName = new CellDefinition("itemName", "商品名称", CellDefinition.TEXT, null, 6000);
            CellDefinition barCode = new CellDefinition("barCode", "条形码", CellDefinition.TEXT, null, 6000);
            CellDefinition supplyPrice = new CellDefinition("supplyPriceYuan", "供货价(元)", CellDefinition.NUM_0_00, null, 6000);
            CellDefinition marketReferencePrice = new CellDefinition("marketReferencePriceYuan", "市场参考价(元)", CellDefinition.NUM_0_00, null, 8000);
            CellDefinition warehouse = new CellDefinition("warehouse", "仓库名称", CellDefinition.TEXT, null, 3000);
            CellDefinition stock = new CellDefinition("stock", "库存", CellDefinition.TEXT, null, 2000);
            CellDefinition state = new CellDefinition("state", "供应商商品状态", CellDefinition.TEXT, null, 2000);
            CellDefinition notifyTime = new CellDefinition("notifyTime", "最近同步时间", CellDefinition.DATE_TIME, null, 6000);
            CellDefinition updateTime = new CellDefinition("updateTime", "最近更新时间", CellDefinition.DATE_TIME, null, 6000);

            List<CellDefinition> cellDefinitionList = new ArrayList<>();
            cellDefinitionList.add(skuCode);
            cellDefinitionList.add(supplierName);
            cellDefinitionList.add(supplierSkuCode);
            cellDefinitionList.add(itemName);
            cellDefinitionList.add(barCode);
            cellDefinitionList.add(supplyPrice);
            cellDefinitionList.add(marketReferencePrice);
            cellDefinitionList.add(warehouse);
            cellDefinitionList.add(stock);
            cellDefinitionList.add(state);
            cellDefinitionList.add(notifyTime);
            cellDefinitionList.add(updateTime);

            String sheetName = "代发商品表";
            String fileName = "代发商品表" + EXCEL;
            if (!AssertUtil.collectionIsEmpty(externalItemSkuList)) {
                for (ExternalItemSku itemSku : externalItemSkuList) {
                    itemSku.setSupplyPriceYuan(CommonUtil.fenToYuan(itemSku.getSupplyPrice()));
                    itemSku.setMarketReferencePriceYuan(CommonUtil.fenToYuan(itemSku.getMarketReferencePrice()));
                    itemSku.setState(StateEnum.getStateEnumByCode(itemSku.getState()).getName());
                }
            }
            try {
                fileName = URLEncoder.encode(fileName, "UTF-8");
            } catch (UnsupportedEncodingException e1) {
                log.error("文件导出错误", e1);
            }
            HSSFWorkbook hssfWorkbook = ExportExcel.generateExcel(externalItemSkuList, cellDefinitionList, sheetName);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            hssfWorkbook.write(stream);
            return javax.ws.rs.core.Response.ok(stream.toByteArray()).header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename*=utf-8'zh_cn'" + fileName).type(MediaType.APPLICATION_OCTET_STREAM)
                    .header("Cache-Control", "no-cache").build();

        } catch (Exception e) {
            log.error("一件代发商品导出异常" + e.getMessage(), e);
            return ResultUtil.createfailureResult(Integer.parseInt(ExceptionEnum.EXTERNAL_GOODS_EXPORT_EXCEPTION.getCode()), ExceptionEnum.EXTERNAL_GOODS_EXPORT_EXCEPTION.getMessage());

        }

    }

    @Override
    public Response exportItemGoods(SkusForm queryModel, AclUserAccreditInfo aclUserAccreditInfo) {
        try {
            Pagenation<Skus> pageExport = new Pagenation<>();
            pageExport.setPageSize(Integer.MAX_VALUE);
            pageExport = itemsSkusPage(queryModel, pageExport, aclUserAccreditInfo);
            List<Skus> skusList = pageExport.getResult();
            CellDefinition skuCode = new CellDefinition("skuCode", "商品SKU编号", CellDefinition.TEXT, null, 6000);
            CellDefinition skuName = new CellDefinition("skuName", "SKU名称", CellDefinition.TEXT, null, 6000);
            CellDefinition spuCode = new CellDefinition("spuCode", "SPU编号", CellDefinition.TEXT, null, 6000);
            CellDefinition propertyValue = new CellDefinition("propertyValue", "规格", CellDefinition.TEXT, null, 6000);
            CellDefinition barCode = new CellDefinition("barCode", "条形码", CellDefinition.TEXT, null, 6000);
            CellDefinition categoryName = new CellDefinition("categoryName", "类目", CellDefinition.TEXT, null, 8000);
            CellDefinition brandName = new CellDefinition("brandName", "品牌", CellDefinition.TEXT, null, 3000);
//            CellDefinition availableInventory = new CellDefinition("availableInventory", "可用正品总库存", CellDefinition.TEXT, null, 2000);
            CellDefinition isValid = new CellDefinition("isValid", "商品状态", CellDefinition.TEXT, null, 6000);
            CellDefinition updateTime = new CellDefinition("updateTime", "最近更新时间", CellDefinition.DATE_TIME, null, 6000);

            List<CellDefinition> cellDefinitionList = new ArrayList<>();
            cellDefinitionList.add(skuCode);
            cellDefinitionList.add(skuName);
            cellDefinitionList.add(spuCode);
            cellDefinitionList.add(propertyValue);
            cellDefinitionList.add(barCode);
            cellDefinitionList.add(categoryName);
            cellDefinitionList.add(brandName);
//            cellDefinitionList.add(availableInventory);
            cellDefinitionList.add(isValid);
            cellDefinitionList.add(updateTime);

            String sheetName = "自采商品表";
            String fileName = "自采商品表" + EXCEL;
            if (!AssertUtil.collectionIsEmpty(skusList)) {
                for (Skus itemSku : skusList) {
                    itemSku.setIsValid(ValidEnum.getValidEnumByCode(itemSku.getIsValid()).getName());
                }
            }
            try {
                fileName = URLEncoder.encode(fileName, "UTF-8");
            } catch (UnsupportedEncodingException e1) {
                log.error("文件导出错误", e1);
            }
            HSSFWorkbook hssfWorkbook = ExportExcel.generateExcel(skusList, cellDefinitionList, sheetName);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            hssfWorkbook.write(stream);
            return javax.ws.rs.core.Response.ok(stream.toByteArray()).header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename*=utf-8'zh_cn'" + fileName).type(MediaType.APPLICATION_OCTET_STREAM)
                    .header("Cache-Control", "no-cache").build();

        } catch (Exception e) {
            log.error("自采商品导出异常" + e.getMessage(), e);
            return ResultUtil.createfailureResult(Integer.parseInt(ExceptionEnum.GOODS_EXPORT_EXCEPTION.getCode()), ExceptionEnum.GOODS_EXPORT_EXCEPTION.getMessage());

        }
    }

}

