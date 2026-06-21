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

import app.mall.dao.entity.LitemallPromotionTier;

// tell cpd to start ignoring code - CPD-OFF
/**
 *  满减档位表: litemall_promotion_tier
 */
@SuppressWarnings({"PMD.UselessOverridingMethod","PMD.UnusedLocalVariable","java:S3008","java:S1602","java:S1128","java:S1161",
        "PMD.UnnecessaryFullyQualifiedName","PMD.EmptyControlStatement","java:S116","java:S115","java:S101","java:S3776"})
public class _LitemallPromotionTier extends DynamicOrmEntity{
    
    /* Id: ID INTEGER */
    public static final String PROP_NAME_id = "id";
    public static final int PROP_ID_id = 1;
    
    /* 促销活动ID: ACTIVITY_ID INTEGER */
    public static final String PROP_NAME_activityId = "activityId";
    public static final int PROP_ID_activityId = 2;
    
    /* 满足金额: MEET_AMOUNT DECIMAL */
    public static final String PROP_NAME_meetAmount = "meetAmount";
    public static final int PROP_ID_meetAmount = 3;
    
    /* 减免值（金额或折扣率）: DISCOUNT_VALUE DECIMAL */
    public static final String PROP_NAME_discountValue = "discountValue";
    public static final int PROP_ID_discountValue = 4;
    
    /* 创建时间: ADD_TIME DATETIME */
    public static final String PROP_NAME_addTime = "addTime";
    public static final int PROP_ID_addTime = 5;
    
    /* 更新时间: UPDATE_TIME DATETIME */
    public static final String PROP_NAME_updateTime = "updateTime";
    public static final int PROP_ID_updateTime = 6;
    
    /* 逻辑删除: DELETED BOOLEAN */
    public static final String PROP_NAME_deleted = "deleted";
    public static final int PROP_ID_deleted = 7;
    

    private static int _PROP_ID_BOUND = 8;

    
    /* relation: 促销活动 */
    public static final String PROP_NAME_activity = "activity";
    

    protected static final List<String> PK_PROP_NAMES = Arrays.asList(PROP_NAME_id);
    protected static final int[] PK_PROP_IDS = new int[]{PROP_ID_id};

    private static final String[] PROP_ID_TO_NAME = new String[8];
    private static final Map<String,Integer> PROP_NAME_TO_ID = new HashMap<>();
    static{
      
          PROP_ID_TO_NAME[PROP_ID_id] = PROP_NAME_id;
          PROP_NAME_TO_ID.put(PROP_NAME_id, PROP_ID_id);
      
          PROP_ID_TO_NAME[PROP_ID_activityId] = PROP_NAME_activityId;
          PROP_NAME_TO_ID.put(PROP_NAME_activityId, PROP_ID_activityId);
      
          PROP_ID_TO_NAME[PROP_ID_meetAmount] = PROP_NAME_meetAmount;
          PROP_NAME_TO_ID.put(PROP_NAME_meetAmount, PROP_ID_meetAmount);
      
          PROP_ID_TO_NAME[PROP_ID_discountValue] = PROP_NAME_discountValue;
          PROP_NAME_TO_ID.put(PROP_NAME_discountValue, PROP_ID_discountValue);
      
          PROP_ID_TO_NAME[PROP_ID_addTime] = PROP_NAME_addTime;
          PROP_NAME_TO_ID.put(PROP_NAME_addTime, PROP_ID_addTime);
      
          PROP_ID_TO_NAME[PROP_ID_updateTime] = PROP_NAME_updateTime;
          PROP_NAME_TO_ID.put(PROP_NAME_updateTime, PROP_ID_updateTime);
      
          PROP_ID_TO_NAME[PROP_ID_deleted] = PROP_NAME_deleted;
          PROP_NAME_TO_ID.put(PROP_NAME_deleted, PROP_ID_deleted);
      
    }

    
    /* Id: ID */
    private java.lang.String _id;
    
    /* 促销活动ID: ACTIVITY_ID */
    private java.lang.String _activityId;
    
    /* 满足金额: MEET_AMOUNT */
    private java.math.BigDecimal _meetAmount;
    
    /* 减免值（金额或折扣率）: DISCOUNT_VALUE */
    private java.math.BigDecimal _discountValue;
    
    /* 创建时间: ADD_TIME */
    private java.time.LocalDateTime _addTime;
    
    /* 更新时间: UPDATE_TIME */
    private java.time.LocalDateTime _updateTime;
    
    /* 逻辑删除: DELETED */
    private java.lang.Boolean _deleted;
    

    public _LitemallPromotionTier(){
        // for debug
    }

    protected LitemallPromotionTier newInstance(){
        LitemallPromotionTier entity = new LitemallPromotionTier();
        entity.orm_attach(orm_enhancer());
        entity.orm_entityModel(orm_entityModel());
        return entity;
    }

    @Override
    public LitemallPromotionTier cloneInstance() {
        LitemallPromotionTier entity = newInstance();
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
      return "app.mall.dao.entity.LitemallPromotionTier";
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
        
            case PROP_ID_activityId:
               return getActivityId();
        
            case PROP_ID_meetAmount:
               return getMeetAmount();
        
            case PROP_ID_discountValue:
               return getDiscountValue();
        
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
        
            case PROP_ID_activityId:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_activityId));
               }
               setActivityId(typedValue);
               break;
            }
        
            case PROP_ID_meetAmount:{
               java.math.BigDecimal typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toBigDecimal(value,
                       err-> newTypeConversionError(PROP_NAME_meetAmount));
               }
               setMeetAmount(typedValue);
               break;
            }
        
            case PROP_ID_discountValue:{
               java.math.BigDecimal typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toBigDecimal(value,
                       err-> newTypeConversionError(PROP_NAME_discountValue));
               }
               setDiscountValue(typedValue);
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
        
            case PROP_ID_activityId:{
               onInitProp(propId);
               this._activityId = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_meetAmount:{
               onInitProp(propId);
               this._meetAmount = (java.math.BigDecimal)value;
               
               break;
            }
        
            case PROP_ID_discountValue:{
               onInitProp(propId);
               this._discountValue = (java.math.BigDecimal)value;
               
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
     * 促销活动ID: ACTIVITY_ID
     */
    public final java.lang.String getActivityId(){
         onPropGet(PROP_ID_activityId);
         return _activityId;
    }

    /**
     * 促销活动ID: ACTIVITY_ID
     */
    public final void setActivityId(java.lang.String value){
        if(onPropSet(PROP_ID_activityId,value)){
            this._activityId = value;
            internalClearRefs(PROP_ID_activityId);
            
        }
    }
    
    /**
     * 满足金额: MEET_AMOUNT
     */
    public final java.math.BigDecimal getMeetAmount(){
         onPropGet(PROP_ID_meetAmount);
         return _meetAmount;
    }

    /**
     * 满足金额: MEET_AMOUNT
     */
    public final void setMeetAmount(java.math.BigDecimal value){
        if(onPropSet(PROP_ID_meetAmount,value)){
            this._meetAmount = value;
            internalClearRefs(PROP_ID_meetAmount);
            
        }
    }
    
    /**
     * 减免值（金额或折扣率）: DISCOUNT_VALUE
     */
    public final java.math.BigDecimal getDiscountValue(){
         onPropGet(PROP_ID_discountValue);
         return _discountValue;
    }

    /**
     * 减免值（金额或折扣率）: DISCOUNT_VALUE
     */
    public final void setDiscountValue(java.math.BigDecimal value){
        if(onPropSet(PROP_ID_discountValue,value)){
            this._discountValue = value;
            internalClearRefs(PROP_ID_discountValue);
            
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
     * 促销活动
     */
    public final app.mall.dao.entity.LitemallPromotionActivity getActivity(){
       return (app.mall.dao.entity.LitemallPromotionActivity)internalGetRefEntity(PROP_NAME_activity);
    }

    public final void setActivity(app.mall.dao.entity.LitemallPromotionActivity refEntity){
   
           if(refEntity == null){
           
                   this.setActivityId(null);
               
           }else{
           internalSetRefEntity(PROP_NAME_activity, refEntity,()->{
           
                           this.setActivityId(refEntity.getId());
                       
           });
           }
       
    }
       
}
// resume CPD analysis - CPD-ON
