package org.trc.enums;

import org.apache.commons.lang3.StringUtils;

/** 
 * @ClassName: ResultEnum 
 * @Description: TODO
 * @author 吴东雄
 * @date 2016年1月15日 下午3:23:10 
 *  
 */
public enum ResultEnum {

	SUCCESS("1","成功"),
	FAILURE("0","失败");
	
	private String code; 
	private String name; 
	
	ResultEnum(String code, String name){
		this.code = code;
		this.name = name;
	}
	
	/**
	 * 
	* @Title: getResultEnumByName 
	* @Description: 根据枚举名称获取枚举
	* @param @param name
	* @param @return    
	* @return ResultEnum
	* @throws
	 */
	public static ResultEnum getResultEnumByName(String name){
		for(ResultEnum resultEnum : ResultEnum.values()){
			if(StringUtils.equals(name, resultEnum.getName())){
				return resultEnum;
			}
		}
		return null;
	}
	
	/**
	 * 
	* @Title: getResultEnumByCode 
	* @Description: 根据枚举编码获取枚举
	* @param @param name
	* @param @return    
	* @return ResultEnum
	* @throws
	 */
	public static ResultEnum getResultEnumByCode(String code){
		for(ResultEnum resultEnum : ResultEnum.values()){
			if(StringUtils.equals(resultEnum.getCode(), code)){
				return resultEnum;
			}
		}
		return null;
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
