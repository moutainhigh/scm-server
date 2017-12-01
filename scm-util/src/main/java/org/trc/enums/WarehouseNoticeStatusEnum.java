package org.trc.enums;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

/**
 * 
* @ClassName: ClearanceEnum
* @Description: 入库通知单状态枚举
* @author A18ccms a18ccms_gmail_com 
* @date 2017年4月6日 上午9:16:13 
*
 */
public enum WarehouseNoticeStatusEnum {

	WAREHOUSE_NOTICE_RECEIVE("0","待通知收货"),
	WAREHOUSE_RECEIVE_FAILED("1","仓库接收失败"),
	ON_WAREHOUSE_TICKLING("2","待仓库反馈"),
	ALL_GOODS("3","全部收货"),
	RECEIVE_GOODS_EXCEPTION("4","收货异常"),
	RECEIVE_PARTIAL_GOODS("5","部分收货"),
	DROPPED("6","作废"),
	CANCELLATION("7","已取消");

	private String code;
	private String name;

	WarehouseNoticeStatusEnum(String code, String name){
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
	public static WarehouseNoticeStatusEnum getClearanceEnumByName(String name){
		for(WarehouseNoticeStatusEnum validEnum : WarehouseNoticeStatusEnum.values()){
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
	public static WarehouseNoticeStatusEnum getClearanceEnumByCode(String code){
		for(WarehouseNoticeStatusEnum validEnum : WarehouseNoticeStatusEnum.values()){
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
		for(WarehouseNoticeStatusEnum sexEnum : WarehouseNoticeStatusEnum.values()){
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
