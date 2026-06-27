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

import app.mall.dao.entity.LitemallPinTuanGroup;

// tell cpd to start ignoring code - CPD-OFF
/**
 *  拼团开团记录表: litemall_pin_tuan_group
 */
@SuppressWarnings({"PMD.UselessOverridingMethod","PMD.UnusedLocalVariable","java:S3008","java:S1602","java:S1128","java:S1161",
        "PMD.UnnecessaryFullyQualifiedName","PMD.EmptyControlStatement","java:S116","java:S115","java:S101","java:S3776"})
public class _LitemallPinTuanGroup extends DynamicOrmEntity{
    
    /* Id: ID INTEGER */
    public static final String PROP_NAME_id = "id";
    public static final int PROP_ID_id = 1;
    
    /* 拼团活动ID: ACTIVITY_ID INTEGER */
    public static final String PROP_NAME_activityId = "activityId";
    public static final int PROP_ID_activityId = 2;
    
    /* 团长用户ID: CREATOR_USER_ID INTEGER */
    public static final String PROP_NAME_creatorUserId = "creatorUserId";
    public static final int PROP_ID_creatorUserId = 3;
    
    /* 团长的订单ID: ORDER_ID INTEGER */
    public static final String PROP_NAME_orderId = "orderId";
    public static final int PROP_ID_orderId = 4;
    
    /* 拼团状态: STATUS INTEGER */
    public static final String PROP_NAME_status = "status";
    public static final int PROP_ID_status = 5;
    
    /* 过期时间: EXPIRE_TIME DATETIME */
    public static final String PROP_NAME_expireTime = "expireTime";
    public static final int PROP_ID_expireTime = 6;
    
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

    
    /* relation: 拼团活动 */
    public static final String PROP_NAME_activity = "activity";
    
    /* relation: 参团成员 */
    public static final String PROP_NAME_members = "members";
    

    protected static final List<String> PK_PROP_NAMES = Arrays.asList(PROP_NAME_id);
    protected static final int[] PK_PROP_IDS = new int[]{PROP_ID_id};

    private static final String[] PROP_ID_TO_NAME = new String[10];
    private static final Map<String,Integer> PROP_NAME_TO_ID = new HashMap<>();
    static{
      
          PROP_ID_TO_NAME[PROP_ID_id] = PROP_NAME_id;
          PROP_NAME_TO_ID.put(PROP_NAME_id, PROP_ID_id);
      
          PROP_ID_TO_NAME[PROP_ID_activityId] = PROP_NAME_activityId;
          PROP_NAME_TO_ID.put(PROP_NAME_activityId, PROP_ID_activityId);
      
          PROP_ID_TO_NAME[PROP_ID_creatorUserId] = PROP_NAME_creatorUserId;
          PROP_NAME_TO_ID.put(PROP_NAME_creatorUserId, PROP_ID_creatorUserId);
      
          PROP_ID_TO_NAME[PROP_ID_orderId] = PROP_NAME_orderId;
          PROP_NAME_TO_ID.put(PROP_NAME_orderId, PROP_ID_orderId);
      
          PROP_ID_TO_NAME[PROP_ID_status] = PROP_NAME_status;
          PROP_NAME_TO_ID.put(PROP_NAME_status, PROP_ID_status);
      
          PROP_ID_TO_NAME[PROP_ID_expireTime] = PROP_NAME_expireTime;
          PROP_NAME_TO_ID.put(PROP_NAME_expireTime, PROP_ID_expireTime);
      
          PROP_ID_TO_NAME[PROP_ID_addTime] = PROP_NAME_addTime;
          PROP_NAME_TO_ID.put(PROP_NAME_addTime, PROP_ID_addTime);
      
          PROP_ID_TO_NAME[PROP_ID_updateTime] = PROP_NAME_updateTime;
          PROP_NAME_TO_ID.put(PROP_NAME_updateTime, PROP_ID_updateTime);
      
          PROP_ID_TO_NAME[PROP_ID_deleted] = PROP_NAME_deleted;
          PROP_NAME_TO_ID.put(PROP_NAME_deleted, PROP_ID_deleted);
      
    }

    
    /* Id: ID */
    private java.lang.String _id;
    
    /* 拼团活动ID: ACTIVITY_ID */
    private java.lang.String _activityId;
    
    /* 团长用户ID: CREATOR_USER_ID */
    private java.lang.String _creatorUserId;
    
    /* 团长的订单ID: ORDER_ID */
    private java.lang.String _orderId;
    
    /* 拼团状态: STATUS */
    private java.lang.Integer _status;
    
    /* 过期时间: EXPIRE_TIME */
    private java.time.LocalDateTime _expireTime;
    
    /* 创建时间: ADD_TIME */
    private java.time.LocalDateTime _addTime;
    
    /* 更新时间: UPDATE_TIME */
    private java.time.LocalDateTime _updateTime;
    
    /* 逻辑删除: DELETED */
    private java.lang.Boolean _deleted;
    

    public _LitemallPinTuanGroup(){
        // for debug
    }

    protected LitemallPinTuanGroup newInstance(){
        LitemallPinTuanGroup entity = new LitemallPinTuanGroup();
        entity.orm_attach(orm_enhancer());
        entity.orm_entityModel(orm_entityModel());
        return entity;
    }

    @Override
    public LitemallPinTuanGroup cloneInstance() {
        LitemallPinTuanGroup entity = newInstance();
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
      return "app.mall.dao.entity.LitemallPinTuanGroup";
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
        
            case PROP_ID_creatorUserId:
               return getCreatorUserId();
        
            case PROP_ID_orderId:
               return getOrderId();
        
            case PROP_ID_status:
               return getStatus();
        
            case PROP_ID_expireTime:
               return getExpireTime();
        
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
        
            case PROP_ID_creatorUserId:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_creatorUserId));
               }
               setCreatorUserId(typedValue);
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
        
            case PROP_ID_status:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_status));
               }
               setStatus(typedValue);
               break;
            }
        
            case PROP_ID_expireTime:{
               java.time.LocalDateTime typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toLocalDateTime(value,
                       err-> newTypeConversionError(PROP_NAME_expireTime));
               }
               setExpireTime(typedValue);
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
        
            case PROP_ID_creatorUserId:{
               onInitProp(propId);
               this._creatorUserId = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_orderId:{
               onInitProp(propId);
               this._orderId = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_status:{
               onInitProp(propId);
               this._status = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_expireTime:{
               onInitProp(propId);
               this._expireTime = (java.time.LocalDateTime)value;
               
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
     * 拼团活动ID: ACTIVITY_ID
     */
    public final java.lang.String getActivityId(){
         onPropGet(PROP_ID_activityId);
         return _activityId;
    }

    /**
     * 拼团活动ID: ACTIVITY_ID
     */
    public final void setActivityId(java.lang.String value){
        if(onPropSet(PROP_ID_activityId,value)){
            this._activityId = value;
            internalClearRefs(PROP_ID_activityId);
            
        }
    }
    
    /**
     * 团长用户ID: CREATOR_USER_ID
     */
    public final java.lang.String getCreatorUserId(){
         onPropGet(PROP_ID_creatorUserId);
         return _creatorUserId;
    }

    /**
     * 团长用户ID: CREATOR_USER_ID
     */
    public final void setCreatorUserId(java.lang.String value){
        if(onPropSet(PROP_ID_creatorUserId,value)){
            this._creatorUserId = value;
            internalClearRefs(PROP_ID_creatorUserId);
            
        }
    }
    
    /**
     * 团长的订单ID: ORDER_ID
     */
    public final java.lang.String getOrderId(){
         onPropGet(PROP_ID_orderId);
         return _orderId;
    }

    /**
     * 团长的订单ID: ORDER_ID
     */
    public final void setOrderId(java.lang.String value){
        if(onPropSet(PROP_ID_orderId,value)){
            this._orderId = value;
            internalClearRefs(PROP_ID_orderId);
            
        }
    }
    
    /**
     * 拼团状态: STATUS
     */
    public final java.lang.Integer getStatus(){
         onPropGet(PROP_ID_status);
         return _status;
    }

    /**
     * 拼团状态: STATUS
     */
    public final void setStatus(java.lang.Integer value){
        if(onPropSet(PROP_ID_status,value)){
            this._status = value;
            internalClearRefs(PROP_ID_status);
            
        }
    }
    
    /**
     * 过期时间: EXPIRE_TIME
     */
    public final java.time.LocalDateTime getExpireTime(){
         onPropGet(PROP_ID_expireTime);
         return _expireTime;
    }

    /**
     * 过期时间: EXPIRE_TIME
     */
    public final void setExpireTime(java.time.LocalDateTime value){
        if(onPropSet(PROP_ID_expireTime,value)){
            this._expireTime = value;
            internalClearRefs(PROP_ID_expireTime);
            
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
     * 拼团活动
     */
    public final app.mall.dao.entity.LitemallPinTuanActivity getActivity(){
       return (app.mall.dao.entity.LitemallPinTuanActivity)internalGetRefEntity(PROP_NAME_activity);
    }

    public final void setActivity(app.mall.dao.entity.LitemallPinTuanActivity refEntity){
   
           if(refEntity == null){
           
                   this.setActivityId(null);
               
           }else{
           internalSetRefEntity(PROP_NAME_activity, refEntity,()->{
           
                           this.setActivityId(refEntity.getId());
                       
           });
           }
       
    }
       
    private final OrmEntitySet<app.mall.dao.entity.LitemallPinTuanMember> _members = new OrmEntitySet<>(this, PROP_NAME_members,
        app.mall.dao.entity.LitemallPinTuanMember.PROP_NAME_group, null,app.mall.dao.entity.LitemallPinTuanMember.class);

    /**
     * 参团成员。 refPropName: group, keyProp: {rel.keyProp}
     */
    public final IOrmEntitySet<app.mall.dao.entity.LitemallPinTuanMember> getMembers(){
       return _members;
    }
       
}
// resume CPD analysis - CPD-ON
