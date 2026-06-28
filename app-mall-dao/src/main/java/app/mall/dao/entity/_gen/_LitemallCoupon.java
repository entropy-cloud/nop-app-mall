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

import app.mall.dao.entity.LitemallCoupon;

// tell cpd to start ignoring code - CPD-OFF
/**
 *  优惠券信息及规则表: litemall_coupon
 */
@SuppressWarnings({"PMD.UselessOverridingMethod","PMD.UnusedLocalVariable","java:S3008","java:S1602","java:S1128","java:S1161",
        "PMD.UnnecessaryFullyQualifiedName","PMD.EmptyControlStatement","java:S116","java:S115","java:S101","java:S3776"})
public class _LitemallCoupon extends DynamicOrmEntity{
    
    /* Id: ID INTEGER */
    public static final String PROP_NAME_id = "id";
    public static final int PROP_ID_id = 1;
    
    /* 优惠券名称: NAME VARCHAR */
    public static final String PROP_NAME_name = "name";
    public static final int PROP_ID_name = 2;
    
    /* 优惠券介绍: DESC VARCHAR */
    public static final String PROP_NAME_desc = "desc";
    public static final int PROP_ID_desc = 3;
    
    /* 优惠券标签: TAG VARCHAR */
    public static final String PROP_NAME_tag = "tag";
    public static final int PROP_ID_tag = 4;
    
    /* 优惠券数量: TOTAL INTEGER */
    public static final String PROP_NAME_total = "total";
    public static final int PROP_ID_total = 5;
    
    /* 优惠金额，: DISCOUNT DECIMAL */
    public static final String PROP_NAME_discount = "discount";
    public static final int PROP_ID_discount = 6;
    
    /* 最少消费金额: MIN DECIMAL */
    public static final String PROP_NAME_min = "min";
    public static final int PROP_ID_min = 7;
    
    /* 用户领券限制数量: LIMIT INTEGER */
    public static final String PROP_NAME_limit = "limit";
    public static final int PROP_ID_limit = 8;
    
    /* 优惠券赠送类型: TYPE INTEGER */
    public static final String PROP_NAME_type = "type";
    public static final int PROP_ID_type = 9;
    
    /* 优惠券状态: STATUS INTEGER */
    public static final String PROP_NAME_status = "status";
    public static final int PROP_ID_status = 10;
    
    /* 商品限制类型: GOODS_TYPE INTEGER */
    public static final String PROP_NAME_goodsType = "goodsType";
    public static final int PROP_ID_goodsType = 11;
    
    /* 商品限制值: GOODS_VALUE VARCHAR */
    public static final String PROP_NAME_goodsValue = "goodsValue";
    public static final int PROP_ID_goodsValue = 12;
    
    /* 优惠券兑换码: CODE VARCHAR */
    public static final String PROP_NAME_code = "code";
    public static final int PROP_ID_code = 13;
    
    /* 有效时间限制: TIME_TYPE INTEGER */
    public static final String PROP_NAME_timeType = "timeType";
    public static final int PROP_ID_timeType = 14;
    
    /* 基于领取时间的有效天数days。: DAYS INTEGER */
    public static final String PROP_NAME_days = "days";
    public static final int PROP_ID_days = 15;
    
    /* 使用券开始时间: START_TIME DATETIME */
    public static final String PROP_NAME_startTime = "startTime";
    public static final int PROP_ID_startTime = 16;
    
    /* 使用券截至时间: END_TIME DATETIME */
    public static final String PROP_NAME_endTime = "endTime";
    public static final int PROP_ID_endTime = 17;
    
    /* 创建时间: ADD_TIME DATETIME */
    public static final String PROP_NAME_addTime = "addTime";
    public static final int PROP_ID_addTime = 18;
    
    /* 更新时间: UPDATE_TIME DATETIME */
    public static final String PROP_NAME_updateTime = "updateTime";
    public static final int PROP_ID_updateTime = 19;
    
    /* 逻辑删除: DELETED BOOLEAN */
    public static final String PROP_NAME_deleted = "deleted";
    public static final int PROP_ID_deleted = 20;
    
    /* 最低会员等级: MIN_MEMBER_LEVEL INTEGER */
    public static final String PROP_NAME_minMemberLevel = "minMemberLevel";
    public static final int PROP_ID_minMemberLevel = 21;
    

    private static int _PROP_ID_BOUND = 22;

    

    protected static final List<String> PK_PROP_NAMES = Arrays.asList(PROP_NAME_id);
    protected static final int[] PK_PROP_IDS = new int[]{PROP_ID_id};

    private static final String[] PROP_ID_TO_NAME = new String[22];
    private static final Map<String,Integer> PROP_NAME_TO_ID = new HashMap<>();
    static{
      
          PROP_ID_TO_NAME[PROP_ID_id] = PROP_NAME_id;
          PROP_NAME_TO_ID.put(PROP_NAME_id, PROP_ID_id);
      
          PROP_ID_TO_NAME[PROP_ID_name] = PROP_NAME_name;
          PROP_NAME_TO_ID.put(PROP_NAME_name, PROP_ID_name);
      
          PROP_ID_TO_NAME[PROP_ID_desc] = PROP_NAME_desc;
          PROP_NAME_TO_ID.put(PROP_NAME_desc, PROP_ID_desc);
      
          PROP_ID_TO_NAME[PROP_ID_tag] = PROP_NAME_tag;
          PROP_NAME_TO_ID.put(PROP_NAME_tag, PROP_ID_tag);
      
          PROP_ID_TO_NAME[PROP_ID_total] = PROP_NAME_total;
          PROP_NAME_TO_ID.put(PROP_NAME_total, PROP_ID_total);
      
          PROP_ID_TO_NAME[PROP_ID_discount] = PROP_NAME_discount;
          PROP_NAME_TO_ID.put(PROP_NAME_discount, PROP_ID_discount);
      
          PROP_ID_TO_NAME[PROP_ID_min] = PROP_NAME_min;
          PROP_NAME_TO_ID.put(PROP_NAME_min, PROP_ID_min);
      
          PROP_ID_TO_NAME[PROP_ID_limit] = PROP_NAME_limit;
          PROP_NAME_TO_ID.put(PROP_NAME_limit, PROP_ID_limit);
      
          PROP_ID_TO_NAME[PROP_ID_type] = PROP_NAME_type;
          PROP_NAME_TO_ID.put(PROP_NAME_type, PROP_ID_type);
      
          PROP_ID_TO_NAME[PROP_ID_status] = PROP_NAME_status;
          PROP_NAME_TO_ID.put(PROP_NAME_status, PROP_ID_status);
      
          PROP_ID_TO_NAME[PROP_ID_goodsType] = PROP_NAME_goodsType;
          PROP_NAME_TO_ID.put(PROP_NAME_goodsType, PROP_ID_goodsType);
      
          PROP_ID_TO_NAME[PROP_ID_goodsValue] = PROP_NAME_goodsValue;
          PROP_NAME_TO_ID.put(PROP_NAME_goodsValue, PROP_ID_goodsValue);
      
          PROP_ID_TO_NAME[PROP_ID_code] = PROP_NAME_code;
          PROP_NAME_TO_ID.put(PROP_NAME_code, PROP_ID_code);
      
          PROP_ID_TO_NAME[PROP_ID_timeType] = PROP_NAME_timeType;
          PROP_NAME_TO_ID.put(PROP_NAME_timeType, PROP_ID_timeType);
      
          PROP_ID_TO_NAME[PROP_ID_days] = PROP_NAME_days;
          PROP_NAME_TO_ID.put(PROP_NAME_days, PROP_ID_days);
      
          PROP_ID_TO_NAME[PROP_ID_startTime] = PROP_NAME_startTime;
          PROP_NAME_TO_ID.put(PROP_NAME_startTime, PROP_ID_startTime);
      
          PROP_ID_TO_NAME[PROP_ID_endTime] = PROP_NAME_endTime;
          PROP_NAME_TO_ID.put(PROP_NAME_endTime, PROP_ID_endTime);
      
          PROP_ID_TO_NAME[PROP_ID_addTime] = PROP_NAME_addTime;
          PROP_NAME_TO_ID.put(PROP_NAME_addTime, PROP_ID_addTime);
      
          PROP_ID_TO_NAME[PROP_ID_updateTime] = PROP_NAME_updateTime;
          PROP_NAME_TO_ID.put(PROP_NAME_updateTime, PROP_ID_updateTime);
      
          PROP_ID_TO_NAME[PROP_ID_deleted] = PROP_NAME_deleted;
          PROP_NAME_TO_ID.put(PROP_NAME_deleted, PROP_ID_deleted);
      
          PROP_ID_TO_NAME[PROP_ID_minMemberLevel] = PROP_NAME_minMemberLevel;
          PROP_NAME_TO_ID.put(PROP_NAME_minMemberLevel, PROP_ID_minMemberLevel);
      
    }

    
    /* Id: ID */
    private java.lang.String _id;
    
    /* 优惠券名称: NAME */
    private java.lang.String _name;
    
    /* 优惠券介绍: DESC */
    private java.lang.String _desc;
    
    /* 优惠券标签: TAG */
    private java.lang.String _tag;
    
    /* 优惠券数量: TOTAL */
    private java.lang.Integer _total;
    
    /* 优惠金额，: DISCOUNT */
    private java.math.BigDecimal _discount;
    
    /* 最少消费金额: MIN */
    private java.math.BigDecimal _min;
    
    /* 用户领券限制数量: LIMIT */
    private java.lang.Integer _limit;
    
    /* 优惠券赠送类型: TYPE */
    private java.lang.Integer _type;
    
    /* 优惠券状态: STATUS */
    private java.lang.Integer _status;
    
    /* 商品限制类型: GOODS_TYPE */
    private java.lang.Integer _goodsType;
    
    /* 商品限制值: GOODS_VALUE */
    private java.lang.String _goodsValue;
    
    /* 优惠券兑换码: CODE */
    private java.lang.String _code;
    
    /* 有效时间限制: TIME_TYPE */
    private java.lang.Integer _timeType;
    
    /* 基于领取时间的有效天数days。: DAYS */
    private java.lang.Integer _days;
    
    /* 使用券开始时间: START_TIME */
    private java.time.LocalDateTime _startTime;
    
    /* 使用券截至时间: END_TIME */
    private java.time.LocalDateTime _endTime;
    
    /* 创建时间: ADD_TIME */
    private java.time.LocalDateTime _addTime;
    
    /* 更新时间: UPDATE_TIME */
    private java.time.LocalDateTime _updateTime;
    
    /* 逻辑删除: DELETED */
    private java.lang.Boolean _deleted;
    
    /* 最低会员等级: MIN_MEMBER_LEVEL */
    private java.lang.Integer _minMemberLevel;
    

    public _LitemallCoupon(){
        // for debug
    }

    protected LitemallCoupon newInstance(){
        LitemallCoupon entity = new LitemallCoupon();
        entity.orm_attach(orm_enhancer());
        entity.orm_entityModel(orm_entityModel());
        return entity;
    }

    @Override
    public LitemallCoupon cloneInstance() {
        LitemallCoupon entity = newInstance();
        orm_forEachInitedProp((value, propId) -> {
            entity.orm_propValue(propId,value);
        });
        return entity;
    }

    @Override
    public String orm_entityName() {
      // 如果存在实体模型对象，则以模型对象上的设置为准
      IEntityModel entityModel = orm_entityModel();
      if(entityModel != null)
          return entityModel.getName();
      return "app.mall.dao.entity.LitemallCoupon";
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
        
            case PROP_ID_desc:
               return getDesc();
        
            case PROP_ID_tag:
               return getTag();
        
            case PROP_ID_total:
               return getTotal();
        
            case PROP_ID_discount:
               return getDiscount();
        
            case PROP_ID_min:
               return getMin();
        
            case PROP_ID_limit:
               return getLimit();
        
            case PROP_ID_type:
               return getType();
        
            case PROP_ID_status:
               return getStatus();
        
            case PROP_ID_goodsType:
               return getGoodsType();
        
            case PROP_ID_goodsValue:
               return getGoodsValue();
        
            case PROP_ID_code:
               return getCode();
        
            case PROP_ID_timeType:
               return getTimeType();
        
            case PROP_ID_days:
               return getDays();
        
            case PROP_ID_startTime:
               return getStartTime();
        
            case PROP_ID_endTime:
               return getEndTime();
        
            case PROP_ID_addTime:
               return getAddTime();
        
            case PROP_ID_updateTime:
               return getUpdateTime();
        
            case PROP_ID_deleted:
               return getDeleted();
        
            case PROP_ID_minMemberLevel:
               return getMinMemberLevel();
        
           default:
              return super.orm_propValue(propId);
        }
    }

    

    @Override
    public void orm_propValue(int propId, Object value){
        switch(propId){
        
            case PROP_ID_id:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
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
        
            case PROP_ID_desc:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_desc));
               }
               setDesc(typedValue);
               break;
            }
        
            case PROP_ID_tag:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_tag));
               }
               setTag(typedValue);
               break;
            }
        
            case PROP_ID_total:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_total));
               }
               setTotal(typedValue);
               break;
            }
        
            case PROP_ID_discount:{
               java.math.BigDecimal typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toBigDecimal(value,
                       err-> newTypeConversionError(PROP_NAME_discount));
               }
               setDiscount(typedValue);
               break;
            }
        
            case PROP_ID_min:{
               java.math.BigDecimal typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toBigDecimal(value,
                       err-> newTypeConversionError(PROP_NAME_min));
               }
               setMin(typedValue);
               break;
            }
        
            case PROP_ID_limit:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_limit));
               }
               setLimit(typedValue);
               break;
            }
        
            case PROP_ID_type:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_type));
               }
               setType(typedValue);
               break;
            }
        
            case PROP_ID_status:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_status));
               }
               setStatus(typedValue);
               break;
            }
        
            case PROP_ID_goodsType:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_goodsType));
               }
               setGoodsType(typedValue);
               break;
            }
        
            case PROP_ID_goodsValue:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_goodsValue));
               }
               setGoodsValue(typedValue);
               break;
            }
        
            case PROP_ID_code:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_code));
               }
               setCode(typedValue);
               break;
            }
        
            case PROP_ID_timeType:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_timeType));
               }
               setTimeType(typedValue);
               break;
            }
        
            case PROP_ID_days:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_days));
               }
               setDays(typedValue);
               break;
            }
        
            case PROP_ID_startTime:{
               java.time.LocalDateTime typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toLocalDateTime(value,
                       err-> newTypeConversionError(PROP_NAME_startTime));
               }
               setStartTime(typedValue);
               break;
            }
        
            case PROP_ID_endTime:{
               java.time.LocalDateTime typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toLocalDateTime(value,
                       err-> newTypeConversionError(PROP_NAME_endTime));
               }
               setEndTime(typedValue);
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
        
            case PROP_ID_minMemberLevel:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_minMemberLevel));
               }
               setMinMemberLevel(typedValue);
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
               this._id = (java.lang.String)value;
               orm_id(); // 如果是设置主键字段，则触发watcher
               break;
            }
        
            case PROP_ID_name:{
               onInitProp(propId);
               this._name = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_desc:{
               onInitProp(propId);
               this._desc = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_tag:{
               onInitProp(propId);
               this._tag = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_total:{
               onInitProp(propId);
               this._total = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_discount:{
               onInitProp(propId);
               this._discount = (java.math.BigDecimal)value;
               
               break;
            }
        
            case PROP_ID_min:{
               onInitProp(propId);
               this._min = (java.math.BigDecimal)value;
               
               break;
            }
        
            case PROP_ID_limit:{
               onInitProp(propId);
               this._limit = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_type:{
               onInitProp(propId);
               this._type = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_status:{
               onInitProp(propId);
               this._status = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_goodsType:{
               onInitProp(propId);
               this._goodsType = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_goodsValue:{
               onInitProp(propId);
               this._goodsValue = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_code:{
               onInitProp(propId);
               this._code = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_timeType:{
               onInitProp(propId);
               this._timeType = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_days:{
               onInitProp(propId);
               this._days = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_startTime:{
               onInitProp(propId);
               this._startTime = (java.time.LocalDateTime)value;
               
               break;
            }
        
            case PROP_ID_endTime:{
               onInitProp(propId);
               this._endTime = (java.time.LocalDateTime)value;
               
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
        
            case PROP_ID_minMemberLevel:{
               onInitProp(propId);
               this._minMemberLevel = (java.lang.Integer)value;
               
               break;
            }
        
           default:
              super.orm_internalSet(propId,value);
        }
    }

    
    /**
     * Id: ID
     */
    public final java.lang.String getId(){
         onPropGet(PROP_ID_id);
         return _id;
    }

    /**
     * Id: ID
     */
    public final void setId(java.lang.String value){
        if(onPropSet(PROP_ID_id,value)){
            this._id = value;
            internalClearRefs(PROP_ID_id);
            orm_id();
        }
    }
    
    /**
     * 优惠券名称: NAME
     */
    public final java.lang.String getName(){
         onPropGet(PROP_ID_name);
         return _name;
    }

    /**
     * 优惠券名称: NAME
     */
    public final void setName(java.lang.String value){
        if(onPropSet(PROP_ID_name,value)){
            this._name = value;
            internalClearRefs(PROP_ID_name);
            
        }
    }
    
    /**
     * 优惠券介绍: DESC
     */
    public final java.lang.String getDesc(){
         onPropGet(PROP_ID_desc);
         return _desc;
    }

    /**
     * 优惠券介绍: DESC
     */
    public final void setDesc(java.lang.String value){
        if(onPropSet(PROP_ID_desc,value)){
            this._desc = value;
            internalClearRefs(PROP_ID_desc);
            
        }
    }
    
    /**
     * 优惠券标签: TAG
     */
    public final java.lang.String getTag(){
         onPropGet(PROP_ID_tag);
         return _tag;
    }

    /**
     * 优惠券标签: TAG
     */
    public final void setTag(java.lang.String value){
        if(onPropSet(PROP_ID_tag,value)){
            this._tag = value;
            internalClearRefs(PROP_ID_tag);
            
        }
    }
    
    /**
     * 优惠券数量: TOTAL
     */
    public final java.lang.Integer getTotal(){
         onPropGet(PROP_ID_total);
         return _total;
    }

    /**
     * 优惠券数量: TOTAL
     */
    public final void setTotal(java.lang.Integer value){
        if(onPropSet(PROP_ID_total,value)){
            this._total = value;
            internalClearRefs(PROP_ID_total);
            
        }
    }
    
    /**
     * 优惠金额，: DISCOUNT
     */
    public final java.math.BigDecimal getDiscount(){
         onPropGet(PROP_ID_discount);
         return _discount;
    }

    /**
     * 优惠金额，: DISCOUNT
     */
    public final void setDiscount(java.math.BigDecimal value){
        if(onPropSet(PROP_ID_discount,value)){
            this._discount = value;
            internalClearRefs(PROP_ID_discount);
            
        }
    }
    
    /**
     * 最少消费金额: MIN
     */
    public final java.math.BigDecimal getMin(){
         onPropGet(PROP_ID_min);
         return _min;
    }

    /**
     * 最少消费金额: MIN
     */
    public final void setMin(java.math.BigDecimal value){
        if(onPropSet(PROP_ID_min,value)){
            this._min = value;
            internalClearRefs(PROP_ID_min);
            
        }
    }
    
    /**
     * 用户领券限制数量: LIMIT
     */
    public final java.lang.Integer getLimit(){
         onPropGet(PROP_ID_limit);
         return _limit;
    }

    /**
     * 用户领券限制数量: LIMIT
     */
    public final void setLimit(java.lang.Integer value){
        if(onPropSet(PROP_ID_limit,value)){
            this._limit = value;
            internalClearRefs(PROP_ID_limit);
            
        }
    }
    
    /**
     * 优惠券赠送类型: TYPE
     */
    public final java.lang.Integer getType(){
         onPropGet(PROP_ID_type);
         return _type;
    }

    /**
     * 优惠券赠送类型: TYPE
     */
    public final void setType(java.lang.Integer value){
        if(onPropSet(PROP_ID_type,value)){
            this._type = value;
            internalClearRefs(PROP_ID_type);
            
        }
    }
    
    /**
     * 优惠券状态: STATUS
     */
    public final java.lang.Integer getStatus(){
         onPropGet(PROP_ID_status);
         return _status;
    }

    /**
     * 优惠券状态: STATUS
     */
    public final void setStatus(java.lang.Integer value){
        if(onPropSet(PROP_ID_status,value)){
            this._status = value;
            internalClearRefs(PROP_ID_status);
            
        }
    }
    
    /**
     * 商品限制类型: GOODS_TYPE
     */
    public final java.lang.Integer getGoodsType(){
         onPropGet(PROP_ID_goodsType);
         return _goodsType;
    }

    /**
     * 商品限制类型: GOODS_TYPE
     */
    public final void setGoodsType(java.lang.Integer value){
        if(onPropSet(PROP_ID_goodsType,value)){
            this._goodsType = value;
            internalClearRefs(PROP_ID_goodsType);
            
        }
    }
    
    /**
     * 商品限制值: GOODS_VALUE
     */
    public final java.lang.String getGoodsValue(){
         onPropGet(PROP_ID_goodsValue);
         return _goodsValue;
    }

    /**
     * 商品限制值: GOODS_VALUE
     */
    public final void setGoodsValue(java.lang.String value){
        if(onPropSet(PROP_ID_goodsValue,value)){
            this._goodsValue = value;
            internalClearRefs(PROP_ID_goodsValue);
            
        }
    }
    
    /**
     * 优惠券兑换码: CODE
     */
    public final java.lang.String getCode(){
         onPropGet(PROP_ID_code);
         return _code;
    }

    /**
     * 优惠券兑换码: CODE
     */
    public final void setCode(java.lang.String value){
        if(onPropSet(PROP_ID_code,value)){
            this._code = value;
            internalClearRefs(PROP_ID_code);
            
        }
    }
    
    /**
     * 有效时间限制: TIME_TYPE
     */
    public final java.lang.Integer getTimeType(){
         onPropGet(PROP_ID_timeType);
         return _timeType;
    }

    /**
     * 有效时间限制: TIME_TYPE
     */
    public final void setTimeType(java.lang.Integer value){
        if(onPropSet(PROP_ID_timeType,value)){
            this._timeType = value;
            internalClearRefs(PROP_ID_timeType);
            
        }
    }
    
    /**
     * 基于领取时间的有效天数days。: DAYS
     */
    public final java.lang.Integer getDays(){
         onPropGet(PROP_ID_days);
         return _days;
    }

    /**
     * 基于领取时间的有效天数days。: DAYS
     */
    public final void setDays(java.lang.Integer value){
        if(onPropSet(PROP_ID_days,value)){
            this._days = value;
            internalClearRefs(PROP_ID_days);
            
        }
    }
    
    /**
     * 使用券开始时间: START_TIME
     */
    public final java.time.LocalDateTime getStartTime(){
         onPropGet(PROP_ID_startTime);
         return _startTime;
    }

    /**
     * 使用券开始时间: START_TIME
     */
    public final void setStartTime(java.time.LocalDateTime value){
        if(onPropSet(PROP_ID_startTime,value)){
            this._startTime = value;
            internalClearRefs(PROP_ID_startTime);
            
        }
    }
    
    /**
     * 使用券截至时间: END_TIME
     */
    public final java.time.LocalDateTime getEndTime(){
         onPropGet(PROP_ID_endTime);
         return _endTime;
    }

    /**
     * 使用券截至时间: END_TIME
     */
    public final void setEndTime(java.time.LocalDateTime value){
        if(onPropSet(PROP_ID_endTime,value)){
            this._endTime = value;
            internalClearRefs(PROP_ID_endTime);
            
        }
    }
    
    /**
     * 创建时间: ADD_TIME
     */
    public final java.time.LocalDateTime getAddTime(){
         onPropGet(PROP_ID_addTime);
         return _addTime;
    }

    /**
     * 创建时间: ADD_TIME
     */
    public final void setAddTime(java.time.LocalDateTime value){
        if(onPropSet(PROP_ID_addTime,value)){
            this._addTime = value;
            internalClearRefs(PROP_ID_addTime);
            
        }
    }
    
    /**
     * 更新时间: UPDATE_TIME
     */
    public final java.time.LocalDateTime getUpdateTime(){
         onPropGet(PROP_ID_updateTime);
         return _updateTime;
    }

    /**
     * 更新时间: UPDATE_TIME
     */
    public final void setUpdateTime(java.time.LocalDateTime value){
        if(onPropSet(PROP_ID_updateTime,value)){
            this._updateTime = value;
            internalClearRefs(PROP_ID_updateTime);
            
        }
    }
    
    /**
     * 逻辑删除: DELETED
     */
    public final java.lang.Boolean getDeleted(){
         onPropGet(PROP_ID_deleted);
         return _deleted;
    }

    /**
     * 逻辑删除: DELETED
     */
    public final void setDeleted(java.lang.Boolean value){
        if(onPropSet(PROP_ID_deleted,value)){
            this._deleted = value;
            internalClearRefs(PROP_ID_deleted);
            
        }
    }
    
    /**
     * 最低会员等级: MIN_MEMBER_LEVEL
     */
    public final java.lang.Integer getMinMemberLevel(){
         onPropGet(PROP_ID_minMemberLevel);
         return _minMemberLevel;
    }

    /**
     * 最低会员等级: MIN_MEMBER_LEVEL
     */
    public final void setMinMemberLevel(java.lang.Integer value){
        if(onPropSet(PROP_ID_minMemberLevel,value)){
            this._minMemberLevel = value;
            internalClearRefs(PROP_ID_minMemberLevel);
            
        }
    }
    
}
// resume CPD analysis - CPD-ON
