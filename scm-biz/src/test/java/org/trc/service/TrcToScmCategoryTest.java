package org.trc.service;

import com.alibaba.fastjson.JSON;
import com.qiniu.util.Json;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.trc.domain.category.*;
import org.trc.domain.trcDomain.*;
import org.trc.enums.SourceEnum;
import org.trc.enums.ValidEnum;
import org.trc.enums.ZeroToNineEnum;
import org.trc.service.category.ICategoryBrandService;
import org.trc.service.category.ICategoryPropertyService;
import org.trc.service.category.ICategoryService;
import org.trc.service.impl.category.BrandService;
import org.trc.service.impl.category.PropertyService;
import org.trc.service.impl.category.PropertyValueService;
import org.trc.service.impl.trcCategory.TrcBrandsService;
import org.trc.service.impl.util.SerialUtilService;
import org.trc.service.trcCategory.*;
import org.trc.util.AssertUtil;
import org.trc.util.DateUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)  //标记测试运行的环境
@ContextConfiguration(locations = {"classpath:config/resource-context.xml"}) //配合spring测试  可以引入多个配置文件
public class TrcToScmCategoryTest {
    @Autowired
    private TrcBrandsService trcBrandsService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private SerialUtilService serialUtilService;
    @Autowired
    private PropertyService propertyService;
    @Autowired
    private ITrcPropertiesService trcPropertiesService;

    @Autowired
    private ITrcPropertiesValuesService trcPropertiesValuesService;
    @Autowired
    private PropertyValueService propertyValueService;

    @Autowired
    private ITrcCategoriesService trcCategoriesService;
    @Autowired
    private ICategoryService categoryService;

    @Autowired
    private ITrcCategoriesBrandsService trcCategoriesBrandsService;
    @Autowired
    private ICategoryBrandService categoryBrandService;

    @Autowired
    private ITrcCategoriesPropertiesService trcCategoriesPropertiesService;
    @Autowired
    private ICategoryPropertyService categoryPropertyService;

    private final static String BRAND_CODE_EX_NAME = "PP";
    private final static int BRAND_CODE_LENGTH = 5;
    private final static String CHANNEL_TRC = "QD001";

    private final static String FL_SERIALNAME = "FL";
    private final static Integer FL_LENGTH = 3;

    @Test
    public void brandTest() {
        Example example = new Example(Brands.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("disabled", "0");

        List<Brands> brandsList = trcBrandsService.selectByExample(example);
        List<Brand> brandListScm = new ArrayList<>();
        if (!AssertUtil.collectionIsEmpty(brandsList)) {
            for (Brands brandTrc : brandsList) {
                Brand brandScm = new Brand();
                brandScm.setId(Long.valueOf(brandTrc.getBrandId()));
                brandScm.setAlise(brandTrc.getAlias());
                brandScm.setLogo(brandTrc.getLogo());
                brandScm.setName(brandTrc.getName());
                brandScm.setIsValid(ZeroToNineEnum.ONE.getCode());
                brandScm.setSource(SourceEnum.TRC.getCode());
                brandScm.setSort(brandTrc.getSortOrder());
                brandScm.setIsDeleted(ZeroToNineEnum.ZERO.getCode());
                brandScm.setCreateOperator("admin");
                brandScm.setLastEditOperator("admin");
                brandScm.setWebUrl("");
                brandScm.setCreateTime(Calendar.getInstance().getTime());
                brandScm.setUpdateTime(brandScm.getCreateTime());
                brandScm.setBrandCode(serialUtilService.generateCode(BRAND_CODE_LENGTH, BRAND_CODE_EX_NAME, DateUtils.dateToCompactString(brandScm.getCreateTime())));

                brandListScm.add(brandScm);
            }
            brandService.insertList(brandListScm);
        }

    }
    @Test
    public void propertyTest() {
        Example example = new Example(Properties.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("disabled", "0");
        List<Properties> propertiesList = trcPropertiesService.selectByExample(example);
        List<Property> propertyList = new ArrayList<>();
        if (!AssertUtil.collectionIsEmpty(propertiesList)) {
            for (Properties properties:  propertiesList) {
                Property property = new Property();
                property.setId(Long.valueOf(properties.getPropertyId()));
                property.setDescription(properties.getDescription());
                property.setLastEditOperator("admin");
                property.setName(properties.getName());
                if (properties.getType().equals("Natural")){
                    property.setTypeCode("natureProperty");
                }else {
                    property.setTypeCode("purchaseProperty");
                }
                if (properties.getShowType().equals("Text")){
                    property.setValueType(ZeroToNineEnum.ZERO.getCode());
                }else {
                    property.setValueType(ZeroToNineEnum.ONE.getCode());
                }
                property.setIsValid(ZeroToNineEnum.ONE.getCode());
                property.setSort(properties.getSortOrder());
                property.setCreateOperator("admin");
                property.setLastEditOperator("admin");
                property.setCreateTime(Calendar.getInstance().getTime());
                property.setUpdateTime(property.getCreateTime());
                property.setIsDeleted(ZeroToNineEnum.ZERO.getCode());
                propertyList.add(property);
            }
            propertyService.insertList(propertyList);
        }
    }


    @Test
    public void propertyValueTest() {
        Example example = new Example(PropertyValues.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIsNotNull("propertyValueId");
        List<PropertyValues> propertyValuesList = trcPropertiesValuesService.selectByExample(example);
        List<PropertyValue> propertyValueList = new ArrayList<>();
        if (!AssertUtil.collectionIsEmpty(propertyValuesList)) {
            for (PropertyValues propertyValues:  propertyValuesList) {
                PropertyValue propertyValueScm = new PropertyValue();
                propertyValueScm.setId(Long.valueOf(propertyValues.getPropertyValueId()));
                propertyValueScm.setPropertyId(Long.valueOf(propertyValues.getPropertyId()));
                propertyValueScm.setPicture(propertyValues.getImage());
                propertyValueScm.setValue(propertyValues.getText());
                propertyValueScm.setCreateTime(Calendar.getInstance().getTime());
                propertyValueScm.setUpdateTime(propertyValueScm.getCreateTime());
                propertyValueScm.setIsDeleted(ZeroToNineEnum.ZERO.getCode());
                propertyValueScm.setIsValid(ZeroToNineEnum.ONE.getCode());
                propertyValueScm.setCreateOperator("admin");
                propertyValueScm.setSort(propertyValues.getSortOrder());
                propertyValueList.add(propertyValueScm);

            }
            propertyValueService.insertList(propertyValueList);
        }
    }

    @Test
    public void categoryTest() {
        Example example = new Example(Categories.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("disabled", "0");

        List<Categories> categoriesList = trcCategoriesService.selectByExample(example);
        List<Category> categoryScm = new ArrayList<>();
        if (!AssertUtil.collectionIsEmpty(categoriesList)) {
            for (Categories categories : categoriesList) {
                Category category = new Category();
                category.setId(Long.valueOf(categories.getCategoryId()));
                category.setName(categories.getName());
                category.setParentId(Long.valueOf(categories.getParentId()));
                category.setLevel(categories.getLevel());
                category.setSort(categories.getSortOrder());
                category.setClassifyDescribe(categories.getDescription());
                int disabled = categories.getDisabled();
                if(disabled == Integer.parseInt(ZeroToNineEnum.ZERO.getCode()))//正常
                    category.setIsValid(ValidEnum.VALID.getCode());
                else
                    category.setIsValid(ValidEnum.NOVALID.getCode());
                category.setSource(CHANNEL_TRC);
                category.setIsDeleted(ZeroToNineEnum.ZERO.getCode());
                Date currentDate = Calendar.getInstance().getTime();
                category.setCreateTime(currentDate);
                category.setUpdateTime(currentDate);

                StringBuilder sb = new StringBuilder();
                if(categories.getPrimaryId() > 0)
                    sb.append(categories.getPrimaryId());
                if(categories.getSecondaryId() > 0)
                    sb.append("|").append(categories.getSecondaryId());
                category.setCategoryCode(serialUtilService.generateCode(FL_LENGTH, FL_SERIALNAME));

                categoryScm.add(category);
            }
            categoryService.insertList(categoryScm);
        }
    }

    @Test
    public void categoryBrandTest() {
        CategoryBrandRels categoryBrandRels = new CategoryBrandRels();
        List<CategoryBrandRels> categoryBrandRelsList = trcCategoriesBrandsService.select(categoryBrandRels);
        List<CategoryBrand> categoryBrandScm = new ArrayList<>();
        if (!AssertUtil.collectionIsEmpty(categoryBrandRelsList)) {
            for (CategoryBrandRels categoryBrandRels2 : categoryBrandRelsList) {
                CategoryBrand categoryBrand = new CategoryBrand();
                categoryBrand.setCategoryId(Long.valueOf(categoryBrandRels2.getCategoryId()));
                categoryBrand.setBrandId(Long.valueOf(categoryBrandRels2.getBrandId()));
                Category category = categoryService.selectByPrimaryKey(Long.valueOf(categoryBrandRels2.getCategoryId()));
                AssertUtil.notNull(category, String.format("根据主键ID[%s]查询分类信息为空",categoryBrandRels2.getCategoryId()));
                categoryBrand.setCategoryCode(category.getCategoryCode());
                Brand brand = brandService.selectByPrimaryKey(Long.valueOf(categoryBrandRels2.getBrandId()));
                AssertUtil.notNull(brand, String.format("根据主键ID[%s]查询品牌信息为空",categoryBrandRels2.getBrandId()));
                categoryBrand.setBrandCode(brand.getBrandCode());
                categoryBrand.setIsValid(ValidEnum.VALID.getCode());
                categoryBrand.setIsDeleted(ZeroToNineEnum.ZERO.getCode());
                Date currentDate = Calendar.getInstance().getTime();
                categoryBrand.setCreateTime(currentDate);
                categoryBrand.setUpdateTime(currentDate);

                categoryBrandScm.add(categoryBrand);
            }
            categoryBrandService.insertList(categoryBrandScm);
        }
    }

    @Test
    public void categoryPropertyTest() {
        CategoryPropertyRels categoryBrandRels = new CategoryPropertyRels();
        List<CategoryPropertyRels> categoryPropertyRelsList = trcCategoriesPropertiesService.select(categoryBrandRels);
        List<CategoryProperty> categoryPropertiesScm = new ArrayList<>();
        if (!AssertUtil.collectionIsEmpty(categoryPropertyRelsList)) {
            for (CategoryPropertyRels categoryPropertyRels : categoryPropertyRelsList) {
                CategoryProperty categoryProperty = new CategoryProperty();
                categoryProperty.setCategoryId(Long.valueOf(categoryPropertyRels.getCategoryId()));
                categoryProperty.setPropertyId(Long.valueOf(categoryPropertyRels.getPropertyId()));
                categoryProperty.setIsValid(ValidEnum.VALID.getCode());
                categoryProperty.setIsDeleted(ZeroToNineEnum.ZERO.getCode());
                Date currentDate = Calendar.getInstance().getTime();
                categoryProperty.setCreateTime(currentDate);

                categoryPropertiesScm.add(categoryProperty);
            }
            categoryPropertyService.insertList(categoryPropertiesScm);
        }
    }

}
