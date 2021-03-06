package org.trc.enums;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by hzcyn on 2018/9/4.
 */
public enum QiNiuUploadTypeEnum {

    XLS("XLS","XLS"),
    XLSX("XLSX","XLSX"),
    DOC("DOC","DOC"),
    DOCX("DOCX","DOCX"),
    JPG("JPG","JPG"),
    JPEG("JPEG","JPEG"),
    PNG("PNG","PNG"),
    BMP("BMP","BMP"),
    RAR("RAR","RAR");

    private String code;
    private String name;

    QiNiuUploadTypeEnum(String code, String name){
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
    public static QiNiuUploadTypeEnum getValidEnumByName(String name){
        for(QiNiuUploadTypeEnum validEnum : QiNiuUploadTypeEnum.values()){
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
    public static QiNiuUploadTypeEnum getValidEnumByCode(String code){
        for(QiNiuUploadTypeEnum validEnum : QiNiuUploadTypeEnum.values()){
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
        for(QiNiuUploadTypeEnum sexEnum : QiNiuUploadTypeEnum.values()){
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
