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

import app.mall.dao.entity.LitemallPromotionUsage;

// tell cpd to start ignoring code - CPD-OFF
/**
 *  满减参与记录表: litemall_promotion_usage
 */
@SuppressWarnings({"PMD.UselessOverridingMethod","PMD.UnusedLocalVariable","java:S3008","java:S1602","java:S1128","java:S1161",
        "PMD.UnnecessaryFullyQualifiedName","PMD.EmptyControlStatement","java:S116","java:S115","java:S101","java:S3776"})
public class _LitemallPromotionUsage extends DynamicOrmEntity{
    
    /* Id: ID INTEGER */
    public static final String PROP_NAME_id = "id";
    public static final int PROP_ID_id = 1;
    
    /* 用户ID: USER_ID VARCHAR */
    public static final String PROP_NAME_userId = "userId";
    public static final int PROP_ID_userId = 2;
    
    /* 促销活动ID: PROMOTION_ACTIVITY_ID INTEGER */
    public static final String PROP_NAME_promotionActivityId = "promotionActivityId";
    public static final int PROP_ID_promotionActivityId = 3;
    
    /* 订单ID: ORDER_ID INTEGER */
    public static final String PROP_NAME_orderId = "orderId";
    public static final int PROP_ID_orderId = 4;
    
    /* 满减门槛命中金额: MEET_AMOUNT DECIMAL */
    public static final String PROP_NAME_meetAmount = "meetAmount";
    public static final int PROP_ID_meetAmount = 5;
    
    /* 实际优惠额: DISCOUNT_AMOUNT DECIMAL */
    public static final String PROP_NAME_discountAmount = "discountAmount";
    public static final int PROP_ID_discountAmount = 6;
    
    /* 创建时间: ADD_TIME DATETIME */
    public static final String PROP_NAME_addTime = "addTime";
    public static final int PROP_ID_addTime = 7;
    
    /* 更新时间: UPDATE_TIME DATETIME */
    public static final String PROP_NAME_updateTime = "updateTime";
    public static final int PROP_ID_updateTime = 8;
    
    /* 逻辑删除: DELETED BOOLEAN */
    public static final String PROP_NAME_deleted = "deleted";
    public static final int PROP_ID_deleted = 9;
    

    private static int _PROP_ID_BOUND = 10;

    
    /* relation: 促销活动 */
    public static final String PROP_NAME_activity = "activity";
    
    /* relation: 订单 */
    public static final String PROP_NAME_order = "order";
    

    protected static final List<String> PK_PROP_NAMES = Arrays.asList(PROP_NAME_id);
    protected static final int[] PK_PROP_IDS = new int[]{PROP_ID_id};

    private static final String[] PROP_ID_TO_NAME = new String[10];
    private static final Map<String,Integer> PROP_NAME_TO_ID = new HashMap<>();
    static{
      
          PROP_ID_TO_NAME[PROP_ID_id] = PROP_NAME_id;
          PROP_NAME_TO_ID.put(PROP_NAME_id, PROP_ID_id);
      
          PROP_ID_TO_NAME[PROP_ID_userId] = PROP_NAME_userId;
          PROP_NAME_TO_ID.put(PROP_NAME_userId, PROP_ID_userId);
      
          PROP_ID_TO_NAME[PROP_ID_promotionActivityId] = PROP_NAME_promotionActivityId;
          PROP_NAME_TO_ID.put(PROP_NAME_promotionActivityId, PROP_ID_promotionActivityId);
      
          PROP_ID_TO_NAME[PROP_ID_orderId] = PROP_NAME_orderId;
          PROP_NAME_TO_ID.put(PROP_NAME_orderId, PROP_ID_orderId);
      
          PROP_ID_TO_NAME[PROP_ID_meetAmount] = PROP_NAME_meetAmount;
          PROP_NAME_TO_ID.put(PROP_NAME_meetAmount, PROP_ID_meetAmount);
      
          PROP_ID_TO_NAME[PROP_ID_discountAmount] = PROP_NAME_discountAmount;
          PROP_NAME_TO_ID.put(PROP_NAME_discountAmount, PROP_ID_discountAmount);
      
          PROP_ID_TO_NAME[PROP_ID_addTime] = PROP_NAME_addTime;
          PROP_NAME_TO_ID.put(PROP_NAME_addTime, PROP_ID_addTime);
      
          PROP_ID_TO_NAME[PROP_ID_updateTime] = PROP_NAME_updateTime;
          PROP_NAME_TO_ID.put(PROP_NAME_updateTime, PROP_ID_updateTime);
      
          PROP_ID_TO_NAME[PROP_ID_deleted] = PROP_NAME_deleted;
          PROP_NAME_TO_ID.put(PROP_NAME_deleted, PROP_ID_deleted);
      
    }

    
    /* Id: ID */
    private java.lang.String _id;
    
    /* 用户ID: USER_ID */
    private java.lang.String _userId;
    
    /* 促销活动ID: PROMOTION_ACTIVITY_ID */
    private java.lang.String _promotionActivityId;
    
    /* 订单ID: ORDER_ID */
    private java.lang.String _orderId;
    
    /* 满减门槛命中金额: MEET_AMOUNT */
    private java.math.BigDecimal _meetAmount;
    
    /* 实际优惠额: DISCOUNT_AMOUNT */
    private java.math.BigDecimal _discountAmount;
    
    /* 创建时间: ADD_TIME */
    private java.time.LocalDateTime _addTime;
    
    /* 更新时间: UPDATE_TIME */
    private java.time.LocalDateTime _updateTime;
    
    /* 逻辑删除: DELETED */
    private java.lang.Boolean _deleted;
    

    public _LitemallPromotionUsage(){
        // for debug
    }

    protected LitemallPromotionUsage newInstance(){
        LitemallPromotionUsage entity = new LitemallPromotionUsage();
        entity.orm_attach(orm_enhancer());
        entity.orm_entityModel(orm_entityModel());
        return entity;
    }

    @Override
    public LitemallPromotionUsage cloneInstance() {
        LitemallPromotionUsage entity = newInstance();
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
      return "app.mall.dao.entity.LitemallPromotionUsage";
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
        
            case PROP_ID_userId:
               return getUserId();
        
            case PROP_ID_promotionActivityId:
               return getPromotionActivityId();
        
            case PROP_ID_orderId:
               return getOrderId();
        
            case PROP_ID_meetAmount:
               return getMeetAmount();
        
            case PROP_ID_discountAmount:
               return getDiscountAmount();
        
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
        
            case PROP_ID_userId:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_userId));
               }
               setUserId(typedValue);
               break;
            }
        
            case PROP_ID_promotionActivityId:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_promotionActivityId));
               }
               setPromotionActivityId(typedValue);
               break;
            }
        
            case PROP_ID_orderId:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_orderId));
               }
               setOrderId(typedValue);
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
        
            case PROP_ID_discountAmount:{
               java.math.BigDecimal typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toBigDecimal(value,
                       err-> newTypeConversionError(PROP_NAME_discountAmount));
               }
               setDiscountAmount(typedValue);
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
        
            case PROP_ID_userId:{
               onInitProp(propId);
               this._userId = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_promotionActivityId:{
               onInitProp(propId);
               this._promotionActivityId = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_orderId:{
               onInitProp(propId);
               this._orderId = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_meetAmount:{
               onInitProp(propId);
               this._meetAmount = (java.math.BigDecimal)value;
               
               break;
            }
        
            case PROP_ID_discountAmount:{
               onInitProp(propId);
               this._discountAmount = (java.math.BigDecimal)value;
               
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
     * 用户ID: USER_ID
     */
    public final java.lang.String getUserId(){
         onPropGet(PROP_ID_userId);
         return _userId;
    }

    /**
     * 用户ID: USER_ID
     */
    public final void setUserId(java.lang.String value){
        if(onPropSet(PROP_ID_userId,value)){
            this._userId = value;
            internalClearRefs(PROP_ID_userId);
            
        }
    }
    
    /**
     * 促销活动ID: PROMOTION_ACTIVITY_ID
     */
    public final java.lang.String getPromotionActivityId(){
         onPropGet(PROP_ID_promotionActivityId);
         return _promotionActivityId;
    }

    /**
     * 促销活动ID: PROMOTION_ACTIVITY_ID
     */
    public final void setPromotionActivityId(java.lang.String value){
        if(onPropSet(PROP_ID_promotionActivityId,value)){
            this._promotionActivityId = value;
            internalClearRefs(PROP_ID_promotionActivityId);
            
        }
    }
    
    /**
     * 订单ID: ORDER_ID
     */
    public final java.lang.String getOrderId(){
         onPropGet(PROP_ID_orderId);
         return _orderId;
    }

    /**
     * 订单ID: ORDER_ID
     */
    public final void setOrderId(java.lang.String value){
        if(onPropSet(PROP_ID_orderId,value)){
            this._orderId = value;
            internalClearRefs(PROP_ID_orderId);
            
        }
    }
    
    /**
     * 满减门槛命中金额: MEET_AMOUNT
     */
    public final java.math.BigDecimal getMeetAmount(){
         onPropGet(PROP_ID_meetAmount);
         return _meetAmount;
    }

    /**
     * 满减门槛命中金额: MEET_AMOUNT
     */
    public final void setMeetAmount(java.math.BigDecimal value){
        if(onPropSet(PROP_ID_meetAmount,value)){
            this._meetAmount = value;
            internalClearRefs(PROP_ID_meetAmount);
            
        }
    }
    
    /**
     * 实际优惠额: DISCOUNT_AMOUNT
     */
    public final java.math.BigDecimal getDiscountAmount(){
         onPropGet(PROP_ID_discountAmount);
         return _discountAmount;
    }

    /**
     * 实际优惠额: DISCOUNT_AMOUNT
     */
    public final void setDiscountAmount(java.math.BigDecimal value){
        if(onPropSet(PROP_ID_discountAmount,value)){
            this._discountAmount = value;
            internalClearRefs(PROP_ID_discountAmount);
            
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
           
                   this.setPromotionActivityId(null);
               
           }else{
           internalSetRefEntity(PROP_NAME_activity, refEntity,()->{
           
                           this.setPromotionActivityId(refEntity.getId());
                       
           });
           }
       
    }
       
    /**
     * 订单
     */
    public final app.mall.dao.entity.LitemallOrder getOrder(){
       return (app.mall.dao.entity.LitemallOrder)internalGetRefEntity(PROP_NAME_order);
    }

    public final void setOrder(app.mall.dao.entity.LitemallOrder refEntity){
   
           if(refEntity == null){
           
                   this.setOrderId(null);
               
           }else{
           internalSetRefEntity(PROP_NAME_order, refEntity,()->{
           
                           this.setOrderId(refEntity.getId());
                       
           });
           }
       
    }
       
}
// resume CPD analysis - CPD-ON
