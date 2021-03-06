package org.trc.service.impl.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.trc.domain.util.Area;
import org.trc.domain.util.AreaTreeNode;
import org.trc.mapper.util.ILocationMapper;
import org.trc.service.impl.BaseService;
import org.trc.service.util.ILocationUtilService;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sone on 2017/5/6.
 */
@Service("locationUtilService")
public class LocationUtilService extends BaseService<Area,Long> implements ILocationUtilService {


    @Autowired
    private ILocationMapper mapper;

    @Value("${areaId}")
    private Long areaId;

    @Override
    public Area selectOne(Area record) {
        return mapper.selectOne(record);
    }

    public List<AreaTreeNode> getTreeNodeFromLocation() throws Exception{
         //1.获得location
        Area area =new Area();
        area.setId(areaId);
        area = super.selectOne(area);
        //2.设置顶级父类的ID和TEXT
        AreaTreeNode node=new AreaTreeNode();
        node.setId(area.getCode());
        if(area.getProvince()!=null){
            node.setText(area.getProvince());
        }
        //3.设置省 (new Area(Area.getId()))
        List<Area> provinceAreaList = super.select(new Area(area.getId()));

        List<AreaTreeNode> areaTreeNodeProvinceList =new ArrayList<AreaTreeNode>();//用于存放子集的节点

        Map<String,AreaTreeNode> treeNodeProvincesMap=new HashMap<>();
        /**
         * 1.遍历provinceLocation
         * 2.创建treeNode对象加入到treeNodeProvinces中
         * 3.用map保存省节点--key==id==code(String)  value==AreaTreeNode
         */
        for (Area area1 : provinceAreaList) {
            AreaTreeNode areaTreeNode =new AreaTreeNode();
            areaTreeNode.setId(area1.getCode());
            if(area1.getProvince()!=null){
                areaTreeNode.setText(area1.getProvince());
            }
            areaTreeNodeProvinceList.add(areaTreeNode); //加入节点
            treeNodeProvincesMap.put(areaTreeNode.getId(), areaTreeNode);//map储存
        }
        node.setChildren(areaTreeNodeProvinceList);//设置子节点

        List<Area> allAreaCityList =new ArrayList<Area>();//用于存放城市（all）
        /**
         *为省节点，添加市节点
         */
        for (Area area1 : provinceAreaList) {
            List<Area> cityAreaList = super.select(new Area(area1.getId()));//某省下的所有的城市

            List<AreaTreeNode> areaTreeNodeCityList =new ArrayList<AreaTreeNode>();//用于存放子集的节点
            //省节点
            AreaTreeNode provinceAreaTreeNode = treeNodeProvincesMap.get(area1.getCode());

            provinceAreaTreeNode.setChildren(areaTreeNodeCityList);//设置子节点

            for (Area area2 : cityAreaList) {
                allAreaCityList.add(area2);

                AreaTreeNode areaTreeNode =new AreaTreeNode();
                areaTreeNode.setId(area2.getCode());
                if(area.getCity()!=null){
                    areaTreeNode.setText(area2.getCity());
                }
                areaTreeNodeCityList.add(areaTreeNode); //加入节点
                treeNodeProvincesMap.put(areaTreeNode.getId(), areaTreeNode);//map储存
            }
        }
        /**
         * 把所有的地区放到城市中
         */
        for (Area area1 : allAreaCityList) {
            //市节点
            AreaTreeNode cityAreaTreeNode = treeNodeProvincesMap.get(area1.getCode());
            /**
             * 直辖市ID
             * 1.北京  2
             * 2.天津  19
             * 3.上海市 857
             */
            Long parentId=area1.getParent();
            if(parentId==2L || parentId==19L || parentId==857L){
                List<AreaTreeNode> singleList=new ArrayList<AreaTreeNode>(9);//用于存放子集的节点
                AreaTreeNode areaTreeNode =new AreaTreeNode();
                areaTreeNode.setId(area1.getCode());
                areaTreeNode.setText(area1.getDistrict());
                areaTreeNode.setIsleaf(true);
                singleList.add(areaTreeNode);
                cityAreaTreeNode.setChildren(singleList);
                continue;
            }

            List<Area> districtAreaList = super.select(new Area(area1.getId()));//某市下的所有的地区

            List<AreaTreeNode> areaTreeNodeDistrictList =new ArrayList<AreaTreeNode>();//用于存放子集的节点
            /**
             *市添加地区节点
             */
            for (Area area2 : districtAreaList) {
                AreaTreeNode areaTreeNode =new AreaTreeNode();
                areaTreeNode.setId(area2.getCode());
                areaTreeNode.setIsleaf(true);
                if(area2.getDistrict()!=null){
                    areaTreeNode.setText(area2.getDistrict());
                }
                areaTreeNodeDistrictList.add(areaTreeNode); //加入节点
            }
            cityAreaTreeNode.setChildren(areaTreeNodeDistrictList);//设置子节点
        }
        List<AreaTreeNode> nodeList = node.getChildren();
        return  nodeList;
    }




}
