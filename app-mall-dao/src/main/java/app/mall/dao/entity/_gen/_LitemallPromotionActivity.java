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

import app.mall.dao.entity.LitemallPromotionActivity;

// tell cpd to start ignoring code - CPD-OFF
/**
 *  促销活动表（满减送）: litemall_promotion_activity
 */
@SuppressWarnings({"PMD.UselessOverridingMethod","PMD.UnusedLocalVariable","java:S3008","java:S1602","java:S1128","java:S1161",
        "PMD.UnnecessaryFullyQualifiedName","PMD.EmptyControlStatement","java:S116","java:S115","java:S101","java:S3776"})
public class _LitemallPromotionActivity extends DynamicOrmEntity{
    
    /* Id: ID INTEGER */
    public static final String PROP_NAME_id = "id";
    public static final int PROP_ID_id = 1;
    
    /* 活动名称: NAME VARCHAR */
    public static final String PROP_NAME_name = "name";
    public static final int PROP_ID_name = 2;
    
    /* 折扣类型: DISCOUNT_TYPE INTEGER */
    public static final String PROP_NAME_discountType = "discountType";
    public static final int PROP_ID_discountType = 3;
    
    /* 开始时间: START_TIME DATETIME */
    public static final String PROP_NAME_startTime = "startTime";
    public static final int PROP_ID_startTime = 4;
    
    /* 结束时间: END_TIME DATETIME */
    public static final String PROP_NAME_endTime = "endTime";
    public static final int PROP_ID_endTime = 5;
    
    /* 活动状态: STATUS INTEGER */
    public static final String PROP_NAME_status = "status";
    public static final int PROP_ID_status = 6;
    
    /* 商品范围类型: GOODS_SCOPE INTEGER */
    public static final String PROP_NAME_goodsScope = "goodsScope";
    public static final int PROP_ID_goodsScope = 7;
    
    /* 商品范围值(JSON): GOODS_SCOPE_VALUE VARCHAR */
    public static final String PROP_NAME_goodsScopeValue = "goodsScopeValue";
    public static final int PROP_ID_goodsScopeValue = 8;
    
    /* 优先级: PRIORITY INTEGER */
    public static final String PROP_NAME_priority = "priority";
    public static final int PROP_ID_priority = 9;
    
    /* 每人限参与次数(0不限): MAX_PER_USER INTEGER */
    public static final String PROP_NAME_maxPerUser = "maxPerUser";
    public static final int PROP_ID_maxPerUser = 10;
    
    /* 备注: REMARK VARCHAR */
    public static final String PROP_NAME_remark = "remark";
    public static final int PROP_ID_remark = 11;
    
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

    
    /* relation: 满减档位 */
    public static final String PROP_NAME_tiers = "tiers";
    
    /* relation: 满减参与记录 */
    public static final String PROP_NAME_promotionUsages = "promotionUsages";
    

    protected static final List<String> PK_PROP_NAMES = Arrays.asList(PROP_NAME_id);
    protected static final int[] PK_PROP_IDS = new int[]{PROP_ID_id};

    private static final String[] PROP_ID_TO_NAME = new String[15];
    private static final Map<String,Integer> PROP_NAME_TO_ID = new HashMap<>();
    static{
      
          PROP_ID_TO_NAME[PROP_ID_id] = PROP_NAME_id;
          PROP_NAME_TO_ID.put(PROP_NAME_id, PROP_ID_id);
      
          PROP_ID_TO_NAME[PROP_ID_name] = PROP_NAME_name;
          PROP_NAME_TO_ID.put(PROP_NAME_name, PROP_ID_name);
      
          PROP_ID_TO_NAME[PROP_ID_discountType] = PROP_NAME_discountType;
          PROP_NAME_TO_ID.put(PROP_NAME_discountType, PROP_ID_discountType);
      
          PROP_ID_TO_NAME[PROP_ID_startTime] = PROP_NAME_startTime;
          PROP_NAME_TO_ID.put(PROP_NAME_startTime, PROP_ID_startTime);
      
          PROP_ID_TO_NAME[PROP_ID_endTime] = PROP_NAME_endTime;
          PROP_NAME_TO_ID.put(PROP_NAME_endTime, PROP_ID_endTime);
      
          PROP_ID_TO_NAME[PROP_ID_status] = PROP_NAME_status;
          PROP_NAME_TO_ID.put(PROP_NAME_status, PROP_ID_status);
      
          PROP_ID_TO_NAME[PROP_ID_goodsScope] = PROP_NAME_goodsScope;
          PROP_NAME_TO_ID.put(PROP_NAME_goodsScope, PROP_ID_goodsScope);
      
          PROP_ID_TO_NAME[PROP_ID_goodsScopeValue] = PROP_NAME_goodsScopeValue;
          PROP_NAME_TO_ID.put(PROP_NAME_goodsScopeValue, PROP_ID_goodsScopeValue);
      
          PROP_ID_TO_NAME[PROP_ID_priority] = PROP_NAME_priority;
          PROP_NAME_TO_ID.put(PROP_NAME_priority, PROP_ID_priority);
      
          PROP_ID_TO_NAME[PROP_ID_maxPerUser] = PROP_NAME_maxPerUser;
          PROP_NAME_TO_ID.put(PROP_NAME_maxPerUser, PROP_ID_maxPerUser);
      
          PROP_ID_TO_NAME[PROP_ID_remark] = PROP_NAME_remark;
          PROP_NAME_TO_ID.put(PROP_NAME_remark, PROP_ID_remark);
      
          PROP_ID_TO_NAME[PROP_ID_addTime] = PROP_NAME_addTime;
          PROP_NAME_TO_ID.put(PROP_NAME_addTime, PROP_ID_addTime);
      
          PROP_ID_TO_NAME[PROP_ID_updateTime] = PROP_NAME_updateTime;
          PROP_NAME_TO_ID.put(PROP_NAME_updateTime, PROP_ID_updateTime);
      
          PROP_ID_TO_NAME[PROP_ID_deleted] = PROP_NAME_deleted;
          PROP_NAME_TO_ID.put(PROP_NAME_deleted, PROP_ID_deleted);
      
    }

    
    /* Id: ID */
    private java.lang.String _id;
    
    /* 活动名称: NAME */
    private java.lang.String _name;
    
    /* 折扣类型: DISCOUNT_TYPE */
    private java.lang.Integer _discountType;
    
    /* 开始时间: START_TIME */
    private java.time.LocalDateTime _startTime;
    
    /* 结束时间: END_TIME */
    private java.time.LocalDateTime _endTime;
    
    /* 活动状态: STATUS */
    private java.lang.Integer _status;
    
    /* 商品范围类型: GOODS_SCOPE */
    private java.lang.Integer _goodsScope;
    
    /* 商品范围值(JSON): GOODS_SCOPE_VALUE */
    private java.lang.String _goodsScopeValue;
    
    /* 优先级: PRIORITY */
    private java.lang.Integer _priority;
    
    /* 每人限参与次数(0不限): MAX_PER_USER */
    private java.lang.Integer _maxPerUser;
    
    /* 备注: REMARK */
    private java.lang.String _remark;
    
    /* 创建时间: ADD_TIME */
    private java.time.LocalDateTime _addTime;
    
    /* 更新时间: UPDATE_TIME */
    private java.time.LocalDateTime _updateTime;
    
    /* 逻辑删除: DELETED */
    private java.lang.Boolean _deleted;
    

    public _LitemallPromotionActivity(){
        // for debug
    }

    protected LitemallPromotionActivity newInstance(){
        LitemallPromotionActivity entity = new LitemallPromotionActivity();
        entity.orm_attach(orm_enhancer());
        entity.orm_entityModel(orm_entityModel());
        return entity;
    }

    @Override
    public LitemallPromotionActivity cloneInstance() {
        LitemallPromotionActivity entity = newInstance();
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
      return "app.mall.dao.entity.LitemallPromotionActivity";
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
        
            case PROP_ID_discountType:
               return getDiscountType();
        
            case PROP_ID_startTime:
               return getStartTime();
        
            case PROP_ID_endTime:
               return getEndTime();
        
            case PROP_ID_status:
               return getStatus();
        
            case PROP_ID_goodsScope:
               return getGoodsScope();
        
            case PROP_ID_goodsScopeValue:
               return getGoodsScopeValue();
        
            case PROP_ID_priority:
               return getPriority();
        
            case PROP_ID_maxPerUser:
               return getMaxPerUser();
        
            case PROP_ID_remark:
               return getRemark();
        
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
        
            case PROP_ID_discountType:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_discountType));
               }
               setDiscountType(typedValue);
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
        
            case PROP_ID_status:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_status));
               }
               setStatus(typedValue);
               break;
            }
        
            case PROP_ID_goodsScope:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_goodsScope));
               }
               setGoodsScope(typedValue);
               break;
            }
        
            case PROP_ID_goodsScopeValue:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_goodsScopeValue));
               }
               setGoodsScopeValue(typedValue);
               break;
            }
        
            case PROP_ID_priority:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_priority));
               }
               setPriority(typedValue);
               break;
            }
        
            case PROP_ID_maxPerUser:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_maxPerUser));
               }
               setMaxPerUser(typedValue);
               break;
            }
        
            case PROP_ID_remark:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_remark));
               }
               setRemark(typedValue);
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
               this._id = (java.lang.String)value;
               orm_id(); // 如果是设置主键字段，则触发watcher
               break;
            }
        
            case PROP_ID_name:{
               onInitProp(propId);
               this._name = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_discountType:{
               onInitProp(propId);
               this._discountType = (java.lang.Integer)value;
               
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
        
            case PROP_ID_status:{
               onInitProp(propId);
               this._status = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_goodsScope:{
               onInitProp(propId);
               this._goodsScope = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_goodsScopeValue:{
               onInitProp(propId);
               this._goodsScopeValue = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_priority:{
               onInitProp(propId);
               this._priority = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_maxPerUser:{
               onInitProp(propId);
               this._maxPerUser = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_remark:{
               onInitProp(propId);
               this._remark = (java.lang.String)value;
               
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
     * 活动名称: NAME
     */
    public final java.lang.String getName(){
         onPropGet(PROP_ID_name);
         return _name;
    }

    /**
     * 活动名称: NAME
     */
    public final void setName(java.lang.String value){
        if(onPropSet(PROP_ID_name,value)){
            this._name = value;
            internalClearRefs(PROP_ID_name);
            
        }
    }
    
    /**
     * 折扣类型: DISCOUNT_TYPE
     */
    public final java.lang.Integer getDiscountType(){
         onPropGet(PROP_ID_discountType);
         return _discountType;
    }

    /**
     * 折扣类型: DISCOUNT_TYPE
     */
    public final void setDiscountType(java.lang.Integer value){
        if(onPropSet(PROP_ID_discountType,value)){
            this._discountType = value;
            internalClearRefs(PROP_ID_discountType);
            
        }
    }
    
    /**
     * 开始时间: START_TIME
     */
    public final java.time.LocalDateTime getStartTime(){
         onPropGet(PROP_ID_startTime);
         return _startTime;
    }

    /**
     * 开始时间: START_TIME
     */
    public final void setStartTime(java.time.LocalDateTime value){
        if(onPropSet(PROP_ID_startTime,value)){
            this._startTime = value;
            internalClearRefs(PROP_ID_startTime);
            
        }
    }
    
    /**
     * 结束时间: END_TIME
     */
    public final java.time.LocalDateTime getEndTime(){
         onPropGet(PROP_ID_endTime);
         return _endTime;
    }

    /**
     * 结束时间: END_TIME
     */
    public final void setEndTime(java.time.LocalDateTime value){
        if(onPropSet(PROP_ID_endTime,value)){
            this._endTime = value;
            internalClearRefs(PROP_ID_endTime);
            
        }
    }
    
    /**
     * 活动状态: STATUS
     */
    public final java.lang.Integer getStatus(){
         onPropGet(PROP_ID_status);
         return _status;
    }

    /**
     * 活动状态: STATUS
     */
    public final void setStatus(java.lang.Integer value){
        if(onPropSet(PROP_ID_status,value)){
            this._status = value;
            internalClearRefs(PROP_ID_status);
            
        }
    }
    
    /**
     * 商品范围类型: GOODS_SCOPE
     */
    public final java.lang.Integer getGoodsScope(){
         onPropGet(PROP_ID_goodsScope);
         return _goodsScope;
    }

    /**
     * 商品范围类型: GOODS_SCOPE
     */
    public final void setGoodsScope(java.lang.Integer value){
        if(onPropSet(PROP_ID_goodsScope,value)){
            this._goodsScope = value;
            internalClearRefs(PROP_ID_goodsScope);
            
        }
    }
    
    /**
     * 商品范围值(JSON): GOODS_SCOPE_VALUE
     */
    public final java.lang.String getGoodsScopeValue(){
         onPropGet(PROP_ID_goodsScopeValue);
         return _goodsScopeValue;
    }

    /**
     * 商品范围值(JSON): GOODS_SCOPE_VALUE
     */
    public final void setGoodsScopeValue(java.lang.String value){
        if(onPropSet(PROP_ID_goodsScopeValue,value)){
            this._goodsScopeValue = value;
            internalClearRefs(PROP_ID_goodsScopeValue);
            
        }
    }
    
    /**
     * 优先级: PRIORITY
     */
    public final java.lang.Integer getPriority(){
         onPropGet(PROP_ID_priority);
         return _priority;
    }

    /**
     * 优先级: PRIORITY
     */
    public final void setPriority(java.lang.Integer value){
        if(onPropSet(PROP_ID_priority,value)){
            this._priority = value;
            internalClearRefs(PROP_ID_priority);
            
        }
    }
    
    /**
     * 每人限参与次数(0不限): MAX_PER_USER
     */
    public final java.lang.Integer getMaxPerUser(){
         onPropGet(PROP_ID_maxPerUser);
         return _maxPerUser;
    }

    /**
     * 每人限参与次数(0不限): MAX_PER_USER
     */
    public final void setMaxPerUser(java.lang.Integer value){
        if(onPropSet(PROP_ID_maxPerUser,value)){
            this._maxPerUser = value;
            internalClearRefs(PROP_ID_maxPerUser);
            
        }
    }
    
    /**
     * 备注: REMARK
     */
    public final java.lang.String getRemark(){
         onPropGet(PROP_ID_remark);
         return _remark;
    }

    /**
     * 备注: REMARK
     */
    public final void setRemark(java.lang.String value){
        if(onPropSet(PROP_ID_remark,value)){
            this._remark = value;
            internalClearRefs(PROP_ID_remark);
            
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
    
    private final OrmEntitySet<app.mall.dao.entity.LitemallPromotionTier> _tiers = new OrmEntitySet<>(this, PROP_NAME_tiers,
        app.mall.dao.entity.LitemallPromotionTier.PROP_NAME_activity, null,app.mall.dao.entity.LitemallPromotionTier.class);

    /**
     * 满减档位。 refPropName: activity, keyProp: {rel.keyProp}
     */
    public final IOrmEntitySet<app.mall.dao.entity.LitemallPromotionTier> getTiers(){
       return _tiers;
    }
       
    private final OrmEntitySet<app.mall.dao.entity.LitemallPromotionUsage> _promotionUsages = new OrmEntitySet<>(this, PROP_NAME_promotionUsages,
        app.mall.dao.entity.LitemallPromotionUsage.PROP_NAME_activity, null,app.mall.dao.entity.LitemallPromotionUsage.class);

    /**
     * 满减参与记录。 refPropName: activity, keyProp: {rel.keyProp}
     */
    public final IOrmEntitySet<app.mall.dao.entity.LitemallPromotionUsage> getPromotionUsages(){
       return _promotionUsages;
    }
       
}
// resume CPD analysis - CPD-ON
