package org.trc.enums;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

/**
 * 
* @ClassName: OutboundOrderStatusEnum
* @Description: 发货通知单状态枚举
* @author sone
*
 */
public enum OutboundOrderStatusEnum {

	RECEIVE_FAIL("1", "仓库接收失败"),
	WAITING("2","等待仓库发货"),
//	ON_WAREHOUSE_NOTICE("3","仓库告知的过程中状态"),
	ALL_GOODS("4","全部发货"),
	PART_OF_SHIPMENT("5","部分发货"),
	CANCELED("6","已取消"),
	ON_CANCELED("7", "取消中");

	private String code;
	private String name;

	OutboundOrderStatusEnum(String code, String name){
		this.code = code;
		this.name = name;
	}
	
	/**
	 * 
	* @Title: getValidEnumByName 
	* @Description: 根据枚举名称获取枚举
	* @param @param name
	* @param @return    
	* @return ValidEnum
	* @throws
	 */
	public static OutboundOrderStatusEnum getOutboundOrderStatusEnumByName(String name){
		for(OutboundOrderStatusEnum validEnum : OutboundOrderStatusEnum.values()){
			if(StringUtils.equals(name, validEnum.getName())){
				return validEnum;
			}
		}
		return null;
	}
	
	/**
	 * 
	* @Title: getValidEnumByCode 
	* @Description: 根据枚举编码获取枚举
	* @param @param name
	* @param @return    
	* @return ValidEnum
	* @throws
	 */
	public static OutboundOrderStatusEnum getOutboundOrderStatusEnumByCode(String code){
		for(OutboundOrderStatusEnum validEnum : OutboundOrderStatusEnum.values()){
			if(StringUtils.equals(validEnum.getCode(), code)){
				return validEnum;
			}
		}
		return null;
	}

	/**
	 *
	 * @Title: toJSONArray
	 * @Description: 转换成json数组
	 * @param @return
	 * @return JSONArray
	 * @throws
	 */
	public static JSONArray toJSONArray(){
		JSONArray array = new JSONArray();
		for(OutboundOrderStatusEnum sexEnum : OutboundOrderStatusEnum.values()){
			JSONObject obj = new JSONObject();
			obj.put("code", sexEnum.getCode());
			obj.put("name", sexEnum.getName());
			array.add(obj);
		}
		return array;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
}
