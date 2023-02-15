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

import app.mall.dao.entity.LitemallGroupon;

// tell cpd to start ignoring code - CPD-OFF
/**
 *  团购活动表: litemall_groupon
 */
@SuppressWarnings({"PMD.UselessOverridingMethod","PMD.UnusedLocalVariable",
        "PMD.UnnecessaryFullyQualifiedName","PMD.EmptyControlStatement"})
public class _LitemallGroupon extends DynamicOrmEntity{
    
    /* Id: ID INTEGER */
    public static final String PROP_NAME_id = "id";
    public static final int PROP_ID_id = 1;
    
    /* 关联的订单ID: ORDER_ID INTEGER */
    public static final String PROP_NAME_orderId = "orderId";
    public static final int PROP_ID_orderId = 2;
    
    /* 如果是开团用户，则groupon_id是0；如果是参团用户，则groupon_id是团购活动ID: GROUPON_ID INTEGER */
    public static final String PROP_NAME_grouponId = "grouponId";
    public static final int PROP_ID_grouponId = 3;
    
    /* 团购规则ID，关联litemall_groupon_rules表ID字段: RULES_ID INTEGER */
    public static final String PROP_NAME_rulesId = "rulesId";
    public static final int PROP_ID_rulesId = 4;
    
    /* 用户ID: USER_ID INTEGER */
    public static final String PROP_NAME_userId = "userId";
    public static final int PROP_ID_userId = 5;
    
    /* 团购分享图片地址: SHARE_URL VARCHAR */
    public static final String PROP_NAME_shareUrl = "shareUrl";
    public static final int PROP_ID_shareUrl = 6;
    
    /* 开团用户ID: CREATOR_USER_ID INTEGER */
    public static final String PROP_NAME_creatorUserId = "creatorUserId";
    public static final int PROP_ID_creatorUserId = 7;
    
    /* 开团时间: CREATOR_USER_TIME DATETIME */
    public static final String PROP_NAME_creatorUserTime = "creatorUserTime";
    public static final int PROP_ID_creatorUserTime = 8;
    
    /* 团购活动状态，开团未支付则0，开团中则1，开团失败则2: STATUS SMALLINT */
    public static final String PROP_NAME_status = "status";
    public static final int PROP_ID_status = 9;
    
    /* 创建时间: ADD_TIME DATETIME */
    public static final String PROP_NAME_addTime = "addTime";
    public static final int PROP_ID_addTime = 10;
    
    /* 更新时间: UPDATE_TIME DATETIME */
    public static final String PROP_NAME_updateTime = "updateTime";
    public static final int PROP_ID_updateTime = 11;
    
    /* 逻辑删除: DELETED BOOLEAN */
    public static final String PROP_NAME_deleted = "deleted";
    public static final int PROP_ID_deleted = 12;
    

    private static int _PROP_ID_BOUND = 13;

    

    public static final List<String> PK_PROP_NAMES = Arrays.asList(PROP_NAME_id);
    public static final int[] PK_PROP_IDS = new int[]{PROP_ID_id};

    private static final String[] PROP_ID_TO_NAME = new String[13];
    private static final Map<String,Integer> PROP_NAME_TO_ID = new HashMap<>();
    static{
      
          PROP_ID_TO_NAME[PROP_ID_id] = PROP_NAME_id;
          PROP_NAME_TO_ID.put(PROP_NAME_id, PROP_ID_id);
      
          PROP_ID_TO_NAME[PROP_ID_orderId] = PROP_NAME_orderId;
          PROP_NAME_TO_ID.put(PROP_NAME_orderId, PROP_ID_orderId);
      
          PROP_ID_TO_NAME[PROP_ID_grouponId] = PROP_NAME_grouponId;
          PROP_NAME_TO_ID.put(PROP_NAME_grouponId, PROP_ID_grouponId);
      
          PROP_ID_TO_NAME[PROP_ID_rulesId] = PROP_NAME_rulesId;
          PROP_NAME_TO_ID.put(PROP_NAME_rulesId, PROP_ID_rulesId);
      
          PROP_ID_TO_NAME[PROP_ID_userId] = PROP_NAME_userId;
          PROP_NAME_TO_ID.put(PROP_NAME_userId, PROP_ID_userId);
      
          PROP_ID_TO_NAME[PROP_ID_shareUrl] = PROP_NAME_shareUrl;
          PROP_NAME_TO_ID.put(PROP_NAME_shareUrl, PROP_ID_shareUrl);
      
          PROP_ID_TO_NAME[PROP_ID_creatorUserId] = PROP_NAME_creatorUserId;
          PROP_NAME_TO_ID.put(PROP_NAME_creatorUserId, PROP_ID_creatorUserId);
      
          PROP_ID_TO_NAME[PROP_ID_creatorUserTime] = PROP_NAME_creatorUserTime;
          PROP_NAME_TO_ID.put(PROP_NAME_creatorUserTime, PROP_ID_creatorUserTime);
      
          PROP_ID_TO_NAME[PROP_ID_status] = PROP_NAME_status;
          PROP_NAME_TO_ID.put(PROP_NAME_status, PROP_ID_status);
      
          PROP_ID_TO_NAME[PROP_ID_addTime] = PROP_NAME_addTime;
          PROP_NAME_TO_ID.put(PROP_NAME_addTime, PROP_ID_addTime);
      
          PROP_ID_TO_NAME[PROP_ID_updateTime] = PROP_NAME_updateTime;
          PROP_NAME_TO_ID.put(PROP_NAME_updateTime, PROP_ID_updateTime);
      
          PROP_ID_TO_NAME[PROP_ID_deleted] = PROP_NAME_deleted;
          PROP_NAME_TO_ID.put(PROP_NAME_deleted, PROP_ID_deleted);
      
    }

    
    /* Id: ID */
    private java.lang.Integer _id;
    
    /* 关联的订单ID: ORDER_ID */
    private java.lang.Integer _orderId;
    
    /* 如果是开团用户，则groupon_id是0；如果是参团用户，则groupon_id是团购活动ID: GROUPON_ID */
    private java.lang.Integer _grouponId;
    
    /* 团购规则ID，关联litemall_groupon_rules表ID字段: RULES_ID */
    private java.lang.Integer _rulesId;
    
    /* 用户ID: USER_ID */
    private java.lang.Integer _userId;
    
    /* 团购分享图片地址: SHARE_URL */
    private java.lang.String _shareUrl;
    
    /* 开团用户ID: CREATOR_USER_ID */
    private java.lang.Integer _creatorUserId;
    
    /* 开团时间: CREATOR_USER_TIME */
    private java.time.LocalDateTime _creatorUserTime;
    
    /* 团购活动状态，开团未支付则0，开团中则1，开团失败则2: STATUS */
    private java.lang.Short _status;
    
    /* 创建时间: ADD_TIME */
    private java.time.LocalDateTime _addTime;
    
    /* 更新时间: UPDATE_TIME */
    private java.time.LocalDateTime _updateTime;
    
    /* 逻辑删除: DELETED */
    private java.lang.Boolean _deleted;
    

    public _LitemallGroupon(){
    }

    protected LitemallGroupon newInstance(){
       return new LitemallGroupon();
    }

    @Override
    public LitemallGroupon cloneInstance() {
        LitemallGroupon entity = newInstance();
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
      return "app.mall.dao.entity.LitemallGroupon";
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
        
            case PROP_ID_orderId:
               return getOrderId();
        
            case PROP_ID_grouponId:
               return getGrouponId();
        
            case PROP_ID_rulesId:
               return getRulesId();
        
            case PROP_ID_userId:
               return getUserId();
        
            case PROP_ID_shareUrl:
               return getShareUrl();
        
            case PROP_ID_creatorUserId:
               return getCreatorUserId();
        
            case PROP_ID_creatorUserTime:
               return getCreatorUserTime();
        
            case PROP_ID_status:
               return getStatus();
        
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
        
            case PROP_ID_orderId:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_orderId));
               }
               setOrderId(typedValue);
               break;
            }
        
            case PROP_ID_grouponId:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_grouponId));
               }
               setGrouponId(typedValue);
               break;
            }
        
            case PROP_ID_rulesId:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_rulesId));
               }
               setRulesId(typedValue);
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
        
            case PROP_ID_shareUrl:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_shareUrl));
               }
               setShareUrl(typedValue);
               break;
            }
        
            case PROP_ID_creatorUserId:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_creatorUserId));
               }
               setCreatorUserId(typedValue);
               break;
            }
        
            case PROP_ID_creatorUserTime:{
               java.time.LocalDateTime typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toLocalDateTime(value,
                       err-> newTypeConversionError(PROP_NAME_creatorUserTime));
               }
               setCreatorUserTime(typedValue);
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
        
            case PROP_ID_orderId:{
               onInitProp(propId);
               this._orderId = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_grouponId:{
               onInitProp(propId);
               this._grouponId = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_rulesId:{
               onInitProp(propId);
               this._rulesId = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_userId:{
               onInitProp(propId);
               this._userId = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_shareUrl:{
               onInitProp(propId);
               this._shareUrl = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_creatorUserId:{
               onInitProp(propId);
               this._creatorUserId = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_creatorUserTime:{
               onInitProp(propId);
               this._creatorUserTime = (java.time.LocalDateTime)value;
               
               break;
            }
        
            case PROP_ID_status:{
               onInitProp(propId);
               this._status = (java.lang.Short)value;
               
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
     * 关联的订单ID: ORDER_ID
     */
    public java.lang.Integer getOrderId(){
         onPropGet(PROP_ID_orderId);
         return _orderId;
    }

    /**
     * 关联的订单ID: ORDER_ID
     */
    public void setOrderId(java.lang.Integer value){
        if(onPropSet(PROP_ID_orderId,value)){
            this._orderId = value;
            internalClearRefs(PROP_ID_orderId);
            
        }
    }
    
    /**
     * 如果是开团用户，则groupon_id是0；如果是参团用户，则groupon_id是团购活动ID: GROUPON_ID
     */
    public java.lang.Integer getGrouponId(){
         onPropGet(PROP_ID_grouponId);
         return _grouponId;
    }

    /**
     * 如果是开团用户，则groupon_id是0；如果是参团用户，则groupon_id是团购活动ID: GROUPON_ID
     */
    public void setGrouponId(java.lang.Integer value){
        if(onPropSet(PROP_ID_grouponId,value)){
            this._grouponId = value;
            internalClearRefs(PROP_ID_grouponId);
            
        }
    }
    
    /**
     * 团购规则ID，关联litemall_groupon_rules表ID字段: RULES_ID
     */
    public java.lang.Integer getRulesId(){
         onPropGet(PROP_ID_rulesId);
         return _rulesId;
    }

    /**
     * 团购规则ID，关联litemall_groupon_rules表ID字段: RULES_ID
     */
    public void setRulesId(java.lang.Integer value){
        if(onPropSet(PROP_ID_rulesId,value)){
            this._rulesId = value;
            internalClearRefs(PROP_ID_rulesId);
            
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
     * 团购分享图片地址: SHARE_URL
     */
    public java.lang.String getShareUrl(){
         onPropGet(PROP_ID_shareUrl);
         return _shareUrl;
    }

    /**
     * 团购分享图片地址: SHARE_URL
     */
    public void setShareUrl(java.lang.String value){
        if(onPropSet(PROP_ID_shareUrl,value)){
            this._shareUrl = value;
            internalClearRefs(PROP_ID_shareUrl);
            
        }
    }
    
    /**
     * 开团用户ID: CREATOR_USER_ID
     */
    public java.lang.Integer getCreatorUserId(){
         onPropGet(PROP_ID_creatorUserId);
         return _creatorUserId;
    }

    /**
     * 开团用户ID: CREATOR_USER_ID
     */
    public void setCreatorUserId(java.lang.Integer value){
        if(onPropSet(PROP_ID_creatorUserId,value)){
            this._creatorUserId = value;
            internalClearRefs(PROP_ID_creatorUserId);
            
        }
    }
    
    /**
     * 开团时间: CREATOR_USER_TIME
     */
    public java.time.LocalDateTime getCreatorUserTime(){
         onPropGet(PROP_ID_creatorUserTime);
         return _creatorUserTime;
    }

    /**
     * 开团时间: CREATOR_USER_TIME
     */
    public void setCreatorUserTime(java.time.LocalDateTime value){
        if(onPropSet(PROP_ID_creatorUserTime,value)){
            this._creatorUserTime = value;
            internalClearRefs(PROP_ID_creatorUserTime);
            
        }
    }
    
    /**
     * 团购活动状态，开团未支付则0，开团中则1，开团失败则2: STATUS
     */
    public java.lang.Short getStatus(){
         onPropGet(PROP_ID_status);
         return _status;
    }

    /**
     * 团购活动状态，开团未支付则0，开团中则1，开团失败则2: STATUS
     */
    public void setStatus(java.lang.Short value){
        if(onPropSet(PROP_ID_status,value)){
            this._status = value;
            internalClearRefs(PROP_ID_status);
            
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
