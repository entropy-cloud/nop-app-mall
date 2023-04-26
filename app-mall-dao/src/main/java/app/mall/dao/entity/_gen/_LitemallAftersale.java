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

import app.mall.dao.entity.LitemallAftersale;

// tell cpd to start ignoring code - CPD-OFF
/**
 *  售后表: litemall_aftersale
 */
@SuppressWarnings({"PMD.UselessOverridingMethod","PMD.UnusedLocalVariable",
        "PMD.UnnecessaryFullyQualifiedName","PMD.EmptyControlStatement"})
public class _LitemallAftersale extends DynamicOrmEntity{
    
    /* Id: ID INTEGER */
    public static final String PROP_NAME_id = "id";
    public static final int PROP_ID_id = 1;
    
    /* 售后编号: AFTERSALE_SN VARCHAR */
    public static final String PROP_NAME_aftersaleSn = "aftersaleSn";
    public static final int PROP_ID_aftersaleSn = 2;
    
    /* 订单ID: ORDER_ID INTEGER */
    public static final String PROP_NAME_orderId = "orderId";
    public static final int PROP_ID_orderId = 3;
    
    /* 用户ID: USER_ID INTEGER */
    public static final String PROP_NAME_userId = "userId";
    public static final int PROP_ID_userId = 4;
    
    /* 售后类型: TYPE SMALLINT */
    public static final String PROP_NAME_type = "type";
    public static final int PROP_ID_type = 5;
    
    /* 退款原因: REASON VARCHAR */
    public static final String PROP_NAME_reason = "reason";
    public static final int PROP_ID_reason = 6;
    
    /* 退款金额: AMOUNT DECIMAL */
    public static final String PROP_NAME_amount = "amount";
    public static final int PROP_ID_amount = 7;
    
    /* 退款凭证图片链接数组: PICTURES VARCHAR */
    public static final String PROP_NAME_pictures = "pictures";
    public static final int PROP_ID_pictures = 8;
    
    /* 退款说明: COMMENT VARCHAR */
    public static final String PROP_NAME_comment = "comment";
    public static final int PROP_ID_comment = 9;
    
    /* 售后状态: STATUS SMALLINT */
    public static final String PROP_NAME_status = "status";
    public static final int PROP_ID_status = 10;
    
    /* 管理员操作时间: HANDLE_TIME DATETIME */
    public static final String PROP_NAME_handleTime = "handleTime";
    public static final int PROP_ID_handleTime = 11;
    
    /* 添加时间: ADD_TIME DATETIME */
    public static final String PROP_NAME_addTime = "addTime";
    public static final int PROP_ID_addTime = 12;
    
    /* 更新时间: UPDATE_TIME DATETIME */
    public static final String PROP_NAME_updateTime = "updateTime";
    public static final int PROP_ID_updateTime = 13;
    
    /* 逻辑删除: DELETED BOOLEAN */
    public static final String PROP_NAME_deleted = "deleted";
    public static final int PROP_ID_deleted = 14;
    

    private static int _PROP_ID_BOUND = 15;

    
    /* relation: 订单 */
    public static final String PROP_NAME_order = "order";
    
    /* relation: 客户 */
    public static final String PROP_NAME_user = "user";
    
    /* component:  */
    public static final String PROP_NAME_picturesComponent = "picturesComponent";
    

    public static final List<String> PK_PROP_NAMES = Arrays.asList(PROP_NAME_id);
    public static final int[] PK_PROP_IDS = new int[]{PROP_ID_id};

    private static final String[] PROP_ID_TO_NAME = new String[15];
    private static final Map<String,Integer> PROP_NAME_TO_ID = new HashMap<>();
    static{
      
          PROP_ID_TO_NAME[PROP_ID_id] = PROP_NAME_id;
          PROP_NAME_TO_ID.put(PROP_NAME_id, PROP_ID_id);
      
          PROP_ID_TO_NAME[PROP_ID_aftersaleSn] = PROP_NAME_aftersaleSn;
          PROP_NAME_TO_ID.put(PROP_NAME_aftersaleSn, PROP_ID_aftersaleSn);
      
          PROP_ID_TO_NAME[PROP_ID_orderId] = PROP_NAME_orderId;
          PROP_NAME_TO_ID.put(PROP_NAME_orderId, PROP_ID_orderId);
      
          PROP_ID_TO_NAME[PROP_ID_userId] = PROP_NAME_userId;
          PROP_NAME_TO_ID.put(PROP_NAME_userId, PROP_ID_userId);
      
          PROP_ID_TO_NAME[PROP_ID_type] = PROP_NAME_type;
          PROP_NAME_TO_ID.put(PROP_NAME_type, PROP_ID_type);
      
          PROP_ID_TO_NAME[PROP_ID_reason] = PROP_NAME_reason;
          PROP_NAME_TO_ID.put(PROP_NAME_reason, PROP_ID_reason);
      
          PROP_ID_TO_NAME[PROP_ID_amount] = PROP_NAME_amount;
          PROP_NAME_TO_ID.put(PROP_NAME_amount, PROP_ID_amount);
      
          PROP_ID_TO_NAME[PROP_ID_pictures] = PROP_NAME_pictures;
          PROP_NAME_TO_ID.put(PROP_NAME_pictures, PROP_ID_pictures);
      
          PROP_ID_TO_NAME[PROP_ID_comment] = PROP_NAME_comment;
          PROP_NAME_TO_ID.put(PROP_NAME_comment, PROP_ID_comment);
      
          PROP_ID_TO_NAME[PROP_ID_status] = PROP_NAME_status;
          PROP_NAME_TO_ID.put(PROP_NAME_status, PROP_ID_status);
      
          PROP_ID_TO_NAME[PROP_ID_handleTime] = PROP_NAME_handleTime;
          PROP_NAME_TO_ID.put(PROP_NAME_handleTime, PROP_ID_handleTime);
      
          PROP_ID_TO_NAME[PROP_ID_addTime] = PROP_NAME_addTime;
          PROP_NAME_TO_ID.put(PROP_NAME_addTime, PROP_ID_addTime);
      
          PROP_ID_TO_NAME[PROP_ID_updateTime] = PROP_NAME_updateTime;
          PROP_NAME_TO_ID.put(PROP_NAME_updateTime, PROP_ID_updateTime);
      
          PROP_ID_TO_NAME[PROP_ID_deleted] = PROP_NAME_deleted;
          PROP_NAME_TO_ID.put(PROP_NAME_deleted, PROP_ID_deleted);
      
    }

    
    /* Id: ID */
    private java.lang.Integer _id;
    
    /* 售后编号: AFTERSALE_SN */
    private java.lang.String _aftersaleSn;
    
    /* 订单ID: ORDER_ID */
    private java.lang.Integer _orderId;
    
    /* 用户ID: USER_ID */
    private java.lang.Integer _userId;
    
    /* 售后类型: TYPE */
    private java.lang.Short _type;
    
    /* 退款原因: REASON */
    private java.lang.String _reason;
    
    /* 退款金额: AMOUNT */
    private java.math.BigDecimal _amount;
    
    /* 退款凭证图片链接数组: PICTURES */
    private java.lang.String _pictures;
    
    /* 退款说明: COMMENT */
    private java.lang.String _comment;
    
    /* 售后状态: STATUS */
    private java.lang.Short _status;
    
    /* 管理员操作时间: HANDLE_TIME */
    private java.time.LocalDateTime _handleTime;
    
    /* 添加时间: ADD_TIME */
    private java.time.LocalDateTime _addTime;
    
    /* 更新时间: UPDATE_TIME */
    private java.time.LocalDateTime _updateTime;
    
    /* 逻辑删除: DELETED */
    private java.lang.Boolean _deleted;
    

    public _LitemallAftersale(){
    }

    protected LitemallAftersale newInstance(){
       return new LitemallAftersale();
    }

    @Override
    public LitemallAftersale cloneInstance() {
        LitemallAftersale entity = newInstance();
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
      return "app.mall.dao.entity.LitemallAftersale";
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
        
            case PROP_ID_aftersaleSn:
               return getAftersaleSn();
        
            case PROP_ID_orderId:
               return getOrderId();
        
            case PROP_ID_userId:
               return getUserId();
        
            case PROP_ID_type:
               return getType();
        
            case PROP_ID_reason:
               return getReason();
        
            case PROP_ID_amount:
               return getAmount();
        
            case PROP_ID_pictures:
               return getPictures();
        
            case PROP_ID_comment:
               return getComment();
        
            case PROP_ID_status:
               return getStatus();
        
            case PROP_ID_handleTime:
               return getHandleTime();
        
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
        
            case PROP_ID_aftersaleSn:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_aftersaleSn));
               }
               setAftersaleSn(typedValue);
               break;
            }
        
            case PROP_ID_orderId:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_orderId));
               }
               setOrderId(typedValue);
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
        
            case PROP_ID_type:{
               java.lang.Short typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toShort(value,
                       err-> newTypeConversionError(PROP_NAME_type));
               }
               setType(typedValue);
               break;
            }
        
            case PROP_ID_reason:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_reason));
               }
               setReason(typedValue);
               break;
            }
        
            case PROP_ID_amount:{
               java.math.BigDecimal typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toBigDecimal(value,
                       err-> newTypeConversionError(PROP_NAME_amount));
               }
               setAmount(typedValue);
               break;
            }
        
            case PROP_ID_pictures:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_pictures));
               }
               setPictures(typedValue);
               break;
            }
        
            case PROP_ID_comment:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_comment));
               }
               setComment(typedValue);
               break;
            }
        
            case PROP_ID_status:{
               java.lang.Short typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toShort(value,
                       err-> newTypeConversionError(PROP_NAME_status));
               }
               setStatus(typedValue);
               break;
            }
        
            case PROP_ID_handleTime:{
               java.time.LocalDateTime typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toLocalDateTime(value,
                       err-> newTypeConversionError(PROP_NAME_handleTime));
               }
               setHandleTime(typedValue);
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
        
            case PROP_ID_aftersaleSn:{
               onInitProp(propId);
               this._aftersaleSn = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_orderId:{
               onInitProp(propId);
               this._orderId = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_userId:{
               onInitProp(propId);
               this._userId = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_type:{
               onInitProp(propId);
               this._type = (java.lang.Short)value;
               
               break;
            }
        
            case PROP_ID_reason:{
               onInitProp(propId);
               this._reason = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_amount:{
               onInitProp(propId);
               this._amount = (java.math.BigDecimal)value;
               
               break;
            }
        
            case PROP_ID_pictures:{
               onInitProp(propId);
               this._pictures = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_comment:{
               onInitProp(propId);
               this._comment = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_status:{
               onInitProp(propId);
               this._status = (java.lang.Short)value;
               
               break;
            }
        
            case PROP_ID_handleTime:{
               onInitProp(propId);
               this._handleTime = (java.time.LocalDateTime)value;
               
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
     * 售后编号: AFTERSALE_SN
     */
    public java.lang.String getAftersaleSn(){
         onPropGet(PROP_ID_aftersaleSn);
         return _aftersaleSn;
    }

    /**
     * 售后编号: AFTERSALE_SN
     */
    public void setAftersaleSn(java.lang.String value){
        if(onPropSet(PROP_ID_aftersaleSn,value)){
            this._aftersaleSn = value;
            internalClearRefs(PROP_ID_aftersaleSn);
            
        }
    }
    
    /**
     * 订单ID: ORDER_ID
     */
    public java.lang.Integer getOrderId(){
         onPropGet(PROP_ID_orderId);
         return _orderId;
    }

    /**
     * 订单ID: ORDER_ID
     */
    public void setOrderId(java.lang.Integer value){
        if(onPropSet(PROP_ID_orderId,value)){
            this._orderId = value;
            internalClearRefs(PROP_ID_orderId);
            
        }
    }
    
    /**
     * 用户ID: USER_ID
     */
    public java.lang.Integer getUserId(){
         onPropGet(PROP_ID_userId);
         return _userId;
    }

    /**
     * 用户ID: USER_ID
     */
    public void setUserId(java.lang.Integer value){
        if(onPropSet(PROP_ID_userId,value)){
            this._userId = value;
            internalClearRefs(PROP_ID_userId);
            
        }
    }
    
    /**
     * 售后类型: TYPE
     */
    public java.lang.Short getType(){
         onPropGet(PROP_ID_type);
         return _type;
    }

    /**
     * 售后类型: TYPE
     */
    public void setType(java.lang.Short value){
        if(onPropSet(PROP_ID_type,value)){
            this._type = value;
            internalClearRefs(PROP_ID_type);
            
        }
    }
    
    /**
     * 退款原因: REASON
     */
    public java.lang.String getReason(){
         onPropGet(PROP_ID_reason);
         return _reason;
    }

    /**
     * 退款原因: REASON
     */
    public void setReason(java.lang.String value){
        if(onPropSet(PROP_ID_reason,value)){
            this._reason = value;
            internalClearRefs(PROP_ID_reason);
            
        }
    }
    
    /**
     * 退款金额: AMOUNT
     */
    public java.math.BigDecimal getAmount(){
         onPropGet(PROP_ID_amount);
         return _amount;
    }

    /**
     * 退款金额: AMOUNT
     */
    public void setAmount(java.math.BigDecimal value){
        if(onPropSet(PROP_ID_amount,value)){
            this._amount = value;
            internalClearRefs(PROP_ID_amount);
            
        }
    }
    
    /**
     * 退款凭证图片链接数组: PICTURES
     */
    public java.lang.String getPictures(){
         onPropGet(PROP_ID_pictures);
         return _pictures;
    }

    /**
     * 退款凭证图片链接数组: PICTURES
     */
    public void setPictures(java.lang.String value){
        if(onPropSet(PROP_ID_pictures,value)){
            this._pictures = value;
            internalClearRefs(PROP_ID_pictures);
            
        }
    }
    
    /**
     * 退款说明: COMMENT
     */
    public java.lang.String getComment(){
         onPropGet(PROP_ID_comment);
         return _comment;
    }

    /**
     * 退款说明: COMMENT
     */
    public void setComment(java.lang.String value){
        if(onPropSet(PROP_ID_comment,value)){
            this._comment = value;
            internalClearRefs(PROP_ID_comment);
            
        }
    }
    
    /**
     * 售后状态: STATUS
     */
    public java.lang.Short getStatus(){
         onPropGet(PROP_ID_status);
         return _status;
    }

    /**
     * 售后状态: STATUS
     */
    public void setStatus(java.lang.Short value){
        if(onPropSet(PROP_ID_status,value)){
            this._status = value;
            internalClearRefs(PROP_ID_status);
            
        }
    }
    
    /**
     * 管理员操作时间: HANDLE_TIME
     */
    public java.time.LocalDateTime getHandleTime(){
         onPropGet(PROP_ID_handleTime);
         return _handleTime;
    }

    /**
     * 管理员操作时间: HANDLE_TIME
     */
    public void setHandleTime(java.time.LocalDateTime value){
        if(onPropSet(PROP_ID_handleTime,value)){
            this._handleTime = value;
            internalClearRefs(PROP_ID_handleTime);
            
        }
    }
    
    /**
     * 添加时间: ADD_TIME
     */
    public java.time.LocalDateTime getAddTime(){
         onPropGet(PROP_ID_addTime);
         return _addTime;
    }

    /**
     * 添加时间: ADD_TIME
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
    
    /**
     * 订单
     */
    public app.mall.dao.entity.LitemallOrder getOrder(){
       return (app.mall.dao.entity.LitemallOrder)internalGetRefEntity(PROP_NAME_order);
    }

    public void setOrder(app.mall.dao.entity.LitemallOrder refEntity){
       if(refEntity == null){
         
         this.setOrderId(null);
         
       }else{
          internalSetRefEntity(PROP_NAME_order, refEntity,()->{
             
                    this.setOrderId(refEntity.getId());
                 
          });
       }
    }
       
    /**
     * 客户
     */
    public app.mall.dao.entity.LitemallUser getUser(){
       return (app.mall.dao.entity.LitemallUser)internalGetRefEntity(PROP_NAME_user);
    }

    public void setUser(app.mall.dao.entity.LitemallUser refEntity){
       if(refEntity == null){
         
         this.setUserId(null);
         
       }else{
          internalSetRefEntity(PROP_NAME_user, refEntity,()->{
             
                    this.setUserId(refEntity.getId());
                 
          });
       }
    }
       
   private io.nop.orm.support.JsonOrmComponent _picturesComponent;

   private static Map<String,Integer> COMPONENT_PROP_ID_MAP_picturesComponent = new HashMap<>();
   static{
      
         COMPONENT_PROP_ID_MAP_picturesComponent.put(io.nop.orm.support.JsonOrmComponent.PROP_NAME__jsonText,PROP_ID_pictures);
      
   }

   public io.nop.orm.support.JsonOrmComponent getPicturesComponent(){
      if(_picturesComponent == null){
          _picturesComponent = new io.nop.orm.support.JsonOrmComponent();
          _picturesComponent.bindToEntity(this, COMPONENT_PROP_ID_MAP_picturesComponent);
      }
      return _picturesComponent;
   }

}
// resume CPD analysis - CPD-ON
