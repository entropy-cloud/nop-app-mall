package app.mall.dao.entity._gen;

import io.nop.orm.model.IEntityModel;
import io.nop.orm.support.DynamicOrmEntity;
import io.nop.orm.support.OrmEntitySet; //NOPMD - suppressed UnusedImports - Auto Gen Code
import io.nop.orm.IOrmEntitySet; //NOPMD - suppressed UnusedImports - Auto Gen Code
import io.nop.api.core.convert.ConvertHelper;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;

import app.mall.dao.entity.LitemallAddress;

// tell cpd to start ignoring code - CPD-OFF
/**
 *  收货地址表: litemall_address
 */
@SuppressWarnings({"PMD.UselessOverridingMethod","PMD.UnusedLocalVariable",
        "PMD.UnnecessaryFullyQualifiedName","PMD.EmptyControlStatement"})
public class _LitemallAddress extends DynamicOrmEntity{
    
    /* Id: ID INTEGER */
    public static final String PROP_NAME_id = "id";
    public static final int PROP_ID_id = 1;
    
    /* 收货人名称: NAME VARCHAR */
    public static final String PROP_NAME_name = "name";
    public static final int PROP_ID_name = 2;
    
    /* 用户表的用户ID: USER_ID INTEGER */
    public static final String PROP_NAME_userId = "userId";
    public static final int PROP_ID_userId = 3;
    
    /* 行政区域表的省ID: PROVINCE VARCHAR */
    public static final String PROP_NAME_province = "province";
    public static final int PROP_ID_province = 4;
    
    /* 行政区域表的市ID: CITY VARCHAR */
    public static final String PROP_NAME_city = "city";
    public static final int PROP_ID_city = 5;
    
    /* 行政区域表的区县ID: COUNTY VARCHAR */
    public static final String PROP_NAME_county = "county";
    public static final int PROP_ID_county = 6;
    
    /* 详细收货地址: ADDRESS_DETAIL VARCHAR */
    public static final String PROP_NAME_addressDetail = "addressDetail";
    public static final int PROP_ID_addressDetail = 7;
    
    /* 地区编码: AREA_CODE CHAR */
    public static final String PROP_NAME_areaCode = "areaCode";
    public static final int PROP_ID_areaCode = 8;
    
    /* 邮政编码: POSTAL_CODE CHAR */
    public static final String PROP_NAME_postalCode = "postalCode";
    public static final int PROP_ID_postalCode = 9;
    
    /* 手机号码: TEL VARCHAR */
    public static final String PROP_NAME_tel = "tel";
    public static final int PROP_ID_tel = 10;
    
    /* 是否默认地址: IS_DEFAULT BOOLEAN */
    public static final String PROP_NAME_isDefault = "isDefault";
    public static final int PROP_ID_isDefault = 11;
    
    /* 创建时间: ADD_TIME DATETIME */
    public static final String PROP_NAME_addTime = "addTime";
    public static final int PROP_ID_addTime = 12;
    
    /* 更新时间: UPDATE_TIME DATETIME */
    public static final String PROP_NAME_updateTime = "updateTime";
    public static final int PROP_ID_updateTime = 13;
    
    /* 逻辑删除: DELETED BOOLEAN */
    public static final String PROP_NAME_deleted = "deleted";
    public static final int PROP_ID_deleted = 14;
    

    private static int _PROP_ID_BOUND = 15;

    

    public static final List<String> PK_PROP_NAMES = Arrays.asList(PROP_NAME_id);
    public static final int[] PK_PROP_IDS = new int[]{PROP_ID_id};

    private static final String[] PROP_ID_TO_NAME = new String[15];
    private static final Map<String,Integer> PROP_NAME_TO_ID = new HashMap<>();
    static{
      
          PROP_ID_TO_NAME[PROP_ID_id] = PROP_NAME_id;
          PROP_NAME_TO_ID.put(PROP_NAME_id, PROP_ID_id);
      
          PROP_ID_TO_NAME[PROP_ID_name] = PROP_NAME_name;
          PROP_NAME_TO_ID.put(PROP_NAME_name, PROP_ID_name);
      
          PROP_ID_TO_NAME[PROP_ID_userId] = PROP_NAME_userId;
          PROP_NAME_TO_ID.put(PROP_NAME_userId, PROP_ID_userId);
      
          PROP_ID_TO_NAME[PROP_ID_province] = PROP_NAME_province;
          PROP_NAME_TO_ID.put(PROP_NAME_province, PROP_ID_province);
      
          PROP_ID_TO_NAME[PROP_ID_city] = PROP_NAME_city;
          PROP_NAME_TO_ID.put(PROP_NAME_city, PROP_ID_city);
      
          PROP_ID_TO_NAME[PROP_ID_county] = PROP_NAME_county;
          PROP_NAME_TO_ID.put(PROP_NAME_county, PROP_ID_county);
      
          PROP_ID_TO_NAME[PROP_ID_addressDetail] = PROP_NAME_addressDetail;
          PROP_NAME_TO_ID.put(PROP_NAME_addressDetail, PROP_ID_addressDetail);
      
          PROP_ID_TO_NAME[PROP_ID_areaCode] = PROP_NAME_areaCode;
          PROP_NAME_TO_ID.put(PROP_NAME_areaCode, PROP_ID_areaCode);
      
          PROP_ID_TO_NAME[PROP_ID_postalCode] = PROP_NAME_postalCode;
          PROP_NAME_TO_ID.put(PROP_NAME_postalCode, PROP_ID_postalCode);
      
          PROP_ID_TO_NAME[PROP_ID_tel] = PROP_NAME_tel;
          PROP_NAME_TO_ID.put(PROP_NAME_tel, PROP_ID_tel);
      
          PROP_ID_TO_NAME[PROP_ID_isDefault] = PROP_NAME_isDefault;
          PROP_NAME_TO_ID.put(PROP_NAME_isDefault, PROP_ID_isDefault);
      
          PROP_ID_TO_NAME[PROP_ID_addTime] = PROP_NAME_addTime;
          PROP_NAME_TO_ID.put(PROP_NAME_addTime, PROP_ID_addTime);
      
          PROP_ID_TO_NAME[PROP_ID_updateTime] = PROP_NAME_updateTime;
          PROP_NAME_TO_ID.put(PROP_NAME_updateTime, PROP_ID_updateTime);
      
          PROP_ID_TO_NAME[PROP_ID_deleted] = PROP_NAME_deleted;
          PROP_NAME_TO_ID.put(PROP_NAME_deleted, PROP_ID_deleted);
      
    }

    
    /* Id: ID */
    private java.lang.Integer _id;
    
    /* 收货人名称: NAME */
    private java.lang.String _name;
    
    /* 用户表的用户ID: USER_ID */
    private java.lang.Integer _userId;
    
    /* 行政区域表的省ID: PROVINCE */
    private java.lang.String _province;
    
    /* 行政区域表的市ID: CITY */
    private java.lang.String _city;
    
    /* 行政区域表的区县ID: COUNTY */
    private java.lang.String _county;
    
    /* 详细收货地址: ADDRESS_DETAIL */
    private java.lang.String _addressDetail;
    
    /* 地区编码: AREA_CODE */
    private java.lang.String _areaCode;
    
    /* 邮政编码: POSTAL_CODE */
    private java.lang.String _postalCode;
    
    /* 手机号码: TEL */
    private java.lang.String _tel;
    
    /* 是否默认地址: IS_DEFAULT */
    private java.lang.Boolean _isDefault;
    
    /* 创建时间: ADD_TIME */
    private java.time.LocalDateTime _addTime;
    
    /* 更新时间: UPDATE_TIME */
    private java.time.LocalDateTime _updateTime;
    
    /* 逻辑删除: DELETED */
    private java.lang.Boolean _deleted;
    

    public _LitemallAddress(){
    }

    protected LitemallAddress newInstance(){
       return new LitemallAddress();
    }

    @Override
    public LitemallAddress cloneInstance() {
        LitemallAddress entity = newInstance();
        orm_forEachInitedProp((value, propId) -> {
            entity.onInitProp(propId);
        });
        return entity;
    }

    @Override
    public String orm_entityName() {
      // 如果存在实体模型对象，则以模型对象上的设置为准
      IEntityModel entityModel = orm_entityModel();
      if(entityModel != null)
          return entityModel.getName();
      return "app.mall.dao.entity.LitemallAddress";
    }

    @Override
    public int orm_propIdBound(){
      IEntityModel entityModel = orm_entityModel();
      if(entityModel != null)
          return entityModel.getPropIdBound();
      return _PROP_ID_BOUND;
    }

    @Override
    public Object orm_id() {
    
        return buildSimpleId(PROP_ID_id);
     
    }

    @Override
    public boolean orm_isPrimary(int propId) {
        
            return propId == PROP_ID_id;
          
    }

    @Override
    public String orm_propName(int propId) {
        if(propId >= PROP_ID_TO_NAME.length)
            return super.orm_propName(propId);
        String propName = PROP_ID_TO_NAME[propId];
        if(propName == null)
           return super.orm_propName(propId);
        return propName;
    }

    @Override
    public int orm_propId(String propName) {
        Integer propId = PROP_NAME_TO_ID.get(propName);
        if(propId == null)
            return super.orm_propId(propName);
        return propId;
    }

    @Override
    public Object orm_propValue(int propId) {
        switch(propId){
        
            case PROP_ID_id:
               return getId();
        
            case PROP_ID_name:
               return getName();
        
            case PROP_ID_userId:
               return getUserId();
        
            case PROP_ID_province:
               return getProvince();
        
            case PROP_ID_city:
               return getCity();
        
            case PROP_ID_county:
               return getCounty();
        
            case PROP_ID_addressDetail:
               return getAddressDetail();
        
            case PROP_ID_areaCode:
               return getAreaCode();
        
            case PROP_ID_postalCode:
               return getPostalCode();
        
            case PROP_ID_tel:
               return getTel();
        
            case PROP_ID_isDefault:
               return getIsDefault();
        
            case PROP_ID_addTime:
               return getAddTime();
        
            case PROP_ID_updateTime:
               return getUpdateTime();
        
            case PROP_ID_deleted:
               return getDeleted();
        
           default:
              return super.orm_propValue(propId);
        }
    }

    

    @Override
    public void orm_propValue(int propId, Object value){
        switch(propId){
        
            case PROP_ID_id:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_id));
               }
               setId(typedValue);
               break;
            }
        
            case PROP_ID_name:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_name));
               }
               setName(typedValue);
               break;
            }
        
            case PROP_ID_userId:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_userId));
               }
               setUserId(typedValue);
               break;
            }
        
            case PROP_ID_province:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_province));
               }
               setProvince(typedValue);
               break;
            }
        
            case PROP_ID_city:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_city));
               }
               setCity(typedValue);
               break;
            }
        
            case PROP_ID_county:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_county));
               }
               setCounty(typedValue);
               break;
            }
        
            case PROP_ID_addressDetail:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_addressDetail));
               }
               setAddressDetail(typedValue);
               break;
            }
        
            case PROP_ID_areaCode:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_areaCode));
               }
               setAreaCode(typedValue);
               break;
            }
        
            case PROP_ID_postalCode:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_postalCode));
               }
               setPostalCode(typedValue);
               break;
            }
        
            case PROP_ID_tel:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_tel));
               }
               setTel(typedValue);
               break;
            }
        
            case PROP_ID_isDefault:{
               java.lang.Boolean typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toBoolean(value,
                       err-> newTypeConversionError(PROP_NAME_isDefault));
               }
               setIsDefault(typedValue);
               break;
            }
        
            case PROP_ID_addTime:{
               java.time.LocalDateTime typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toLocalDateTime(value,
                       err-> newTypeConversionError(PROP_NAME_addTime));
               }
               setAddTime(typedValue);
               break;
            }
        
            case PROP_ID_updateTime:{
               java.time.LocalDateTime typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toLocalDateTime(value,
                       err-> newTypeConversionError(PROP_NAME_updateTime));
               }
               setUpdateTime(typedValue);
               break;
            }
        
            case PROP_ID_deleted:{
               java.lang.Boolean typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toBoolean(value,
                       err-> newTypeConversionError(PROP_NAME_deleted));
               }
               setDeleted(typedValue);
               break;
            }
        
           default:
              super.orm_propValue(propId,value);
        }
    }

    @Override
    public void orm_internalSet(int propId, Object value) {
        switch(propId){
        
            case PROP_ID_id:{
               onInitProp(propId);
               this._id = (java.lang.Integer)value;
               orm_id(); // 如果是设置主键字段，则触发watcher
               break;
            }
        
            case PROP_ID_name:{
               onInitProp(propId);
               this._name = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_userId:{
               onInitProp(propId);
               this._userId = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_province:{
               onInitProp(propId);
               this._province = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_city:{
               onInitProp(propId);
               this._city = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_county:{
               onInitProp(propId);
               this._county = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_addressDetail:{
               onInitProp(propId);
               this._addressDetail = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_areaCode:{
               onInitProp(propId);
               this._areaCode = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_postalCode:{
               onInitProp(propId);
               this._postalCode = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_tel:{
               onInitProp(propId);
               this._tel = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_isDefault:{
               onInitProp(propId);
               this._isDefault = (java.lang.Boolean)value;
               
               break;
            }
        
            case PROP_ID_addTime:{
               onInitProp(propId);
               this._addTime = (java.time.LocalDateTime)value;
               
               break;
            }
        
            case PROP_ID_updateTime:{
               onInitProp(propId);
               this._updateTime = (java.time.LocalDateTime)value;
               
               break;
            }
        
            case PROP_ID_deleted:{
               onInitProp(propId);
               this._deleted = (java.lang.Boolean)value;
               
               break;
            }
        
           default:
              super.orm_internalSet(propId,value);
        }
    }

    
    /**
     * Id: ID
     */
    public java.lang.Integer getId(){
         onPropGet(PROP_ID_id);
         return _id;
    }

    /**
     * Id: ID
     */
    public void setId(java.lang.Integer value){
        if(onPropSet(PROP_ID_id,value)){
            this._id = value;
            internalClearRefs(PROP_ID_id);
            orm_id();
        }
    }
    
    /**
     * 收货人名称: NAME
     */
    public java.lang.String getName(){
         onPropGet(PROP_ID_name);
         return _name;
    }

    /**
     * 收货人名称: NAME
     */
    public void setName(java.lang.String value){
        if(onPropSet(PROP_ID_name,value)){
            this._name = value;
            internalClearRefs(PROP_ID_name);
            
        }
    }
    
    /**
     * 用户表的用户ID: USER_ID
     */
    public java.lang.Integer getUserId(){
         onPropGet(PROP_ID_userId);
         return _userId;
    }

    /**
     * 用户表的用户ID: USER_ID
     */
    public void setUserId(java.lang.Integer value){
        if(onPropSet(PROP_ID_userId,value)){
            this._userId = value;
            internalClearRefs(PROP_ID_userId);
            
        }
    }
    
    /**
     * 行政区域表的省ID: PROVINCE
     */
    public java.lang.String getProvince(){
         onPropGet(PROP_ID_province);
         return _province;
    }

    /**
     * 行政区域表的省ID: PROVINCE
     */
    public void setProvince(java.lang.String value){
        if(onPropSet(PROP_ID_province,value)){
            this._province = value;
            internalClearRefs(PROP_ID_province);
            
        }
    }
    
    /**
     * 行政区域表的市ID: CITY
     */
    public java.lang.String getCity(){
         onPropGet(PROP_ID_city);
         return _city;
    }

    /**
     * 行政区域表的市ID: CITY
     */
    public void setCity(java.lang.String value){
        if(onPropSet(PROP_ID_city,value)){
            this._city = value;
            internalClearRefs(PROP_ID_city);
            
        }
    }
    
    /**
     * 行政区域表的区县ID: COUNTY
     */
    public java.lang.String getCounty(){
         onPropGet(PROP_ID_county);
         return _county;
    }

    /**
     * 行政区域表的区县ID: COUNTY
     */
    public void setCounty(java.lang.String value){
        if(onPropSet(PROP_ID_county,value)){
            this._county = value;
            internalClearRefs(PROP_ID_county);
            
        }
    }
    
    /**
     * 详细收货地址: ADDRESS_DETAIL
     */
    public java.lang.String getAddressDetail(){
         onPropGet(PROP_ID_addressDetail);
         return _addressDetail;
    }

    /**
     * 详细收货地址: ADDRESS_DETAIL
     */
    public void setAddressDetail(java.lang.String value){
        if(onPropSet(PROP_ID_addressDetail,value)){
            this._addressDetail = value;
            internalClearRefs(PROP_ID_addressDetail);
            
        }
    }
    
    /**
     * 地区编码: AREA_CODE
     */
    public java.lang.String getAreaCode(){
         onPropGet(PROP_ID_areaCode);
         return _areaCode;
    }

    /**
     * 地区编码: AREA_CODE
     */
    public void setAreaCode(java.lang.String value){
        if(onPropSet(PROP_ID_areaCode,value)){
            this._areaCode = value;
            internalClearRefs(PROP_ID_areaCode);
            
        }
    }
    
    /**
     * 邮政编码: POSTAL_CODE
     */
    public java.lang.String getPostalCode(){
         onPropGet(PROP_ID_postalCode);
         return _postalCode;
    }

    /**
     * 邮政编码: POSTAL_CODE
     */
    public void setPostalCode(java.lang.String value){
        if(onPropSet(PROP_ID_postalCode,value)){
            this._postalCode = value;
            internalClearRefs(PROP_ID_postalCode);
            
        }
    }
    
    /**
     * 手机号码: TEL
     */
    public java.lang.String getTel(){
         onPropGet(PROP_ID_tel);
         return _tel;
    }

    /**
     * 手机号码: TEL
     */
    public void setTel(java.lang.String value){
        if(onPropSet(PROP_ID_tel,value)){
            this._tel = value;
            internalClearRefs(PROP_ID_tel);
            
        }
    }
    
    /**
     * 是否默认地址: IS_DEFAULT
     */
    public java.lang.Boolean getIsDefault(){
         onPropGet(PROP_ID_isDefault);
         return _isDefault;
    }

    /**
     * 是否默认地址: IS_DEFAULT
     */
    public void setIsDefault(java.lang.Boolean value){
        if(onPropSet(PROP_ID_isDefault,value)){
            this._isDefault = value;
            internalClearRefs(PROP_ID_isDefault);
            
        }
    }
    
    /**
     * 创建时间: ADD_TIME
     */
    public java.time.LocalDateTime getAddTime(){
         onPropGet(PROP_ID_addTime);
         return _addTime;
    }

    /**
     * 创建时间: ADD_TIME
     */
    public void setAddTime(java.time.LocalDateTime value){
        if(onPropSet(PROP_ID_addTime,value)){
            this._addTime = value;
            internalClearRefs(PROP_ID_addTime);
            
        }
    }
    
    /**
     * 更新时间: UPDATE_TIME
     */
    public java.time.LocalDateTime getUpdateTime(){
         onPropGet(PROP_ID_updateTime);
         return _updateTime;
    }

    /**
     * 更新时间: UPDATE_TIME
     */
    public void setUpdateTime(java.time.LocalDateTime value){
        if(onPropSet(PROP_ID_updateTime,value)){
            this._updateTime = value;
            internalClearRefs(PROP_ID_updateTime);
            
        }
    }
    
    /**
     * 逻辑删除: DELETED
     */
    public java.lang.Boolean getDeleted(){
         onPropGet(PROP_ID_deleted);
         return _deleted;
    }

    /**
     * 逻辑删除: DELETED
     */
    public void setDeleted(java.lang.Boolean value){
        if(onPropSet(PROP_ID_deleted,value)){
            this._deleted = value;
            internalClearRefs(PROP_ID_deleted);
            
        }
    }
    
}
// resume CPD analysis - CPD-ON
