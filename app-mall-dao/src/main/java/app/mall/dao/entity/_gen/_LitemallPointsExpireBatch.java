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

import app.mall.dao.entity.LitemallPointsExpireBatch;

// tell cpd to start ignoring code - CPD-OFF
/**
 *  积分有效期批次表: litemall_points_expire_batch
 */
@SuppressWarnings({"PMD.UselessOverridingMethod","PMD.UnusedLocalVariable","java:S3008","java:S1602","java:S1128","java:S1161",
        "PMD.UnnecessaryFullyQualifiedName","PMD.EmptyControlStatement","java:S116","java:S115","java:S101","java:S3776"})
public class _LitemallPointsExpireBatch extends DynamicOrmEntity{
    
    /* Id: ID INTEGER */
    public static final String PROP_NAME_id = "id";
    public static final int PROP_ID_id = 1;
    
    /* 积分账户ID: ACCOUNT_ID INTEGER */
    public static final String PROP_NAME_accountId = "accountId";
    public static final int PROP_ID_accountId = 2;
    
    /* 用户ID: USER_ID INTEGER */
    public static final String PROP_NAME_userId = "userId";
    public static final int PROP_ID_userId = 3;
    
    /* 原始获取量: TOTAL_POINTS INTEGER */
    public static final String PROP_NAME_totalPoints = "totalPoints";
    public static final int PROP_ID_totalPoints = 4;
    
    /* 未消耗量: REMAINING_POINTS INTEGER */
    public static final String PROP_NAME_remainingPoints = "remainingPoints";
    public static final int PROP_ID_remainingPoints = 5;
    
    /* 过期时间: EXPIRE_TIME DATETIME */
    public static final String PROP_NAME_expireTime = "expireTime";
    public static final int PROP_ID_expireTime = 6;
    
    /* 来源类型: SOURCE_TYPE VARCHAR */
    public static final String PROP_NAME_sourceType = "sourceType";
    public static final int PROP_ID_sourceType = 7;
    
    /* 来源业务ID: SOURCE_ID VARCHAR */
    public static final String PROP_NAME_sourceId = "sourceId";
    public static final int PROP_ID_sourceId = 8;
    
    /* 数据版本: VERSION INTEGER */
    public static final String PROP_NAME_version = "version";
    public static final int PROP_ID_version = 9;
    
    /* 创建时间: ADD_TIME DATETIME */
    public static final String PROP_NAME_addTime = "addTime";
    public static final int PROP_ID_addTime = 10;
    
    /* 更新时间: UPDATE_TIME DATETIME */
    public static final String PROP_NAME_updateTime = "updateTime";
    public static final int PROP_ID_updateTime = 11;
    

    private static int _PROP_ID_BOUND = 12;

    
    /* relation: 积分账户 */
    public static final String PROP_NAME_account = "account";
    

    protected static final List<String> PK_PROP_NAMES = Arrays.asList(PROP_NAME_id);
    protected static final int[] PK_PROP_IDS = new int[]{PROP_ID_id};

    private static final String[] PROP_ID_TO_NAME = new String[12];
    private static final Map<String,Integer> PROP_NAME_TO_ID = new HashMap<>();
    static{
      
          PROP_ID_TO_NAME[PROP_ID_id] = PROP_NAME_id;
          PROP_NAME_TO_ID.put(PROP_NAME_id, PROP_ID_id);
      
          PROP_ID_TO_NAME[PROP_ID_accountId] = PROP_NAME_accountId;
          PROP_NAME_TO_ID.put(PROP_NAME_accountId, PROP_ID_accountId);
      
          PROP_ID_TO_NAME[PROP_ID_userId] = PROP_NAME_userId;
          PROP_NAME_TO_ID.put(PROP_NAME_userId, PROP_ID_userId);
      
          PROP_ID_TO_NAME[PROP_ID_totalPoints] = PROP_NAME_totalPoints;
          PROP_NAME_TO_ID.put(PROP_NAME_totalPoints, PROP_ID_totalPoints);
      
          PROP_ID_TO_NAME[PROP_ID_remainingPoints] = PROP_NAME_remainingPoints;
          PROP_NAME_TO_ID.put(PROP_NAME_remainingPoints, PROP_ID_remainingPoints);
      
          PROP_ID_TO_NAME[PROP_ID_expireTime] = PROP_NAME_expireTime;
          PROP_NAME_TO_ID.put(PROP_NAME_expireTime, PROP_ID_expireTime);
      
          PROP_ID_TO_NAME[PROP_ID_sourceType] = PROP_NAME_sourceType;
          PROP_NAME_TO_ID.put(PROP_NAME_sourceType, PROP_ID_sourceType);
      
          PROP_ID_TO_NAME[PROP_ID_sourceId] = PROP_NAME_sourceId;
          PROP_NAME_TO_ID.put(PROP_NAME_sourceId, PROP_ID_sourceId);
      
          PROP_ID_TO_NAME[PROP_ID_version] = PROP_NAME_version;
          PROP_NAME_TO_ID.put(PROP_NAME_version, PROP_ID_version);
      
          PROP_ID_TO_NAME[PROP_ID_addTime] = PROP_NAME_addTime;
          PROP_NAME_TO_ID.put(PROP_NAME_addTime, PROP_ID_addTime);
      
          PROP_ID_TO_NAME[PROP_ID_updateTime] = PROP_NAME_updateTime;
          PROP_NAME_TO_ID.put(PROP_NAME_updateTime, PROP_ID_updateTime);
      
    }

    
    /* Id: ID */
    private java.lang.String _id;
    
    /* 积分账户ID: ACCOUNT_ID */
    private java.lang.String _accountId;
    
    /* 用户ID: USER_ID */
    private java.lang.String _userId;
    
    /* 原始获取量: TOTAL_POINTS */
    private java.lang.Integer _totalPoints;
    
    /* 未消耗量: REMAINING_POINTS */
    private java.lang.Integer _remainingPoints;
    
    /* 过期时间: EXPIRE_TIME */
    private java.time.LocalDateTime _expireTime;
    
    /* 来源类型: SOURCE_TYPE */
    private java.lang.String _sourceType;
    
    /* 来源业务ID: SOURCE_ID */
    private java.lang.String _sourceId;
    
    /* 数据版本: VERSION */
    private java.lang.Integer _version;
    
    /* 创建时间: ADD_TIME */
    private java.time.LocalDateTime _addTime;
    
    /* 更新时间: UPDATE_TIME */
    private java.time.LocalDateTime _updateTime;
    

    public _LitemallPointsExpireBatch(){
        // for debug
    }

    protected LitemallPointsExpireBatch newInstance(){
        LitemallPointsExpireBatch entity = new LitemallPointsExpireBatch();
        entity.orm_attach(orm_enhancer());
        entity.orm_entityModel(orm_entityModel());
        return entity;
    }

    @Override
    public LitemallPointsExpireBatch cloneInstance() {
        LitemallPointsExpireBatch entity = newInstance();
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
      return "app.mall.dao.entity.LitemallPointsExpireBatch";
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
        
            case PROP_ID_accountId:
               return getAccountId();
        
            case PROP_ID_userId:
               return getUserId();
        
            case PROP_ID_totalPoints:
               return getTotalPoints();
        
            case PROP_ID_remainingPoints:
               return getRemainingPoints();
        
            case PROP_ID_expireTime:
               return getExpireTime();
        
            case PROP_ID_sourceType:
               return getSourceType();
        
            case PROP_ID_sourceId:
               return getSourceId();
        
            case PROP_ID_version:
               return getVersion();
        
            case PROP_ID_addTime:
               return getAddTime();
        
            case PROP_ID_updateTime:
               return getUpdateTime();
        
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
        
            case PROP_ID_accountId:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_accountId));
               }
               setAccountId(typedValue);
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
        
            case PROP_ID_totalPoints:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_totalPoints));
               }
               setTotalPoints(typedValue);
               break;
            }
        
            case PROP_ID_remainingPoints:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_remainingPoints));
               }
               setRemainingPoints(typedValue);
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
        
            case PROP_ID_sourceType:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_sourceType));
               }
               setSourceType(typedValue);
               break;
            }
        
            case PROP_ID_sourceId:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_sourceId));
               }
               setSourceId(typedValue);
               break;
            }
        
            case PROP_ID_version:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_version));
               }
               setVersion(typedValue);
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
        
            case PROP_ID_accountId:{
               onInitProp(propId);
               this._accountId = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_userId:{
               onInitProp(propId);
               this._userId = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_totalPoints:{
               onInitProp(propId);
               this._totalPoints = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_remainingPoints:{
               onInitProp(propId);
               this._remainingPoints = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_expireTime:{
               onInitProp(propId);
               this._expireTime = (java.time.LocalDateTime)value;
               
               break;
            }
        
            case PROP_ID_sourceType:{
               onInitProp(propId);
               this._sourceType = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_sourceId:{
               onInitProp(propId);
               this._sourceId = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_version:{
               onInitProp(propId);
               this._version = (java.lang.Integer)value;
               
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
     * 积分账户ID: ACCOUNT_ID
     */
    public final java.lang.String getAccountId(){
         onPropGet(PROP_ID_accountId);
         return _accountId;
    }

    /**
     * 积分账户ID: ACCOUNT_ID
     */
    public final void setAccountId(java.lang.String value){
        if(onPropSet(PROP_ID_accountId,value)){
            this._accountId = value;
            internalClearRefs(PROP_ID_accountId);
            
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
     * 原始获取量: TOTAL_POINTS
     */
    public final java.lang.Integer getTotalPoints(){
         onPropGet(PROP_ID_totalPoints);
         return _totalPoints;
    }

    /**
     * 原始获取量: TOTAL_POINTS
     */
    public final void setTotalPoints(java.lang.Integer value){
        if(onPropSet(PROP_ID_totalPoints,value)){
            this._totalPoints = value;
            internalClearRefs(PROP_ID_totalPoints);
            
        }
    }
    
    /**
     * 未消耗量: REMAINING_POINTS
     */
    public final java.lang.Integer getRemainingPoints(){
         onPropGet(PROP_ID_remainingPoints);
         return _remainingPoints;
    }

    /**
     * 未消耗量: REMAINING_POINTS
     */
    public final void setRemainingPoints(java.lang.Integer value){
        if(onPropSet(PROP_ID_remainingPoints,value)){
            this._remainingPoints = value;
            internalClearRefs(PROP_ID_remainingPoints);
            
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
     * 来源类型: SOURCE_TYPE
     */
    public final java.lang.String getSourceType(){
         onPropGet(PROP_ID_sourceType);
         return _sourceType;
    }

    /**
     * 来源类型: SOURCE_TYPE
     */
    public final void setSourceType(java.lang.String value){
        if(onPropSet(PROP_ID_sourceType,value)){
            this._sourceType = value;
            internalClearRefs(PROP_ID_sourceType);
            
        }
    }
    
    /**
     * 来源业务ID: SOURCE_ID
     */
    public final java.lang.String getSourceId(){
         onPropGet(PROP_ID_sourceId);
         return _sourceId;
    }

    /**
     * 来源业务ID: SOURCE_ID
     */
    public final void setSourceId(java.lang.String value){
        if(onPropSet(PROP_ID_sourceId,value)){
            this._sourceId = value;
            internalClearRefs(PROP_ID_sourceId);
            
        }
    }
    
    /**
     * 数据版本: VERSION
     */
    public final java.lang.Integer getVersion(){
         onPropGet(PROP_ID_version);
         return _version;
    }

    /**
     * 数据版本: VERSION
     */
    public final void setVersion(java.lang.Integer value){
        if(onPropSet(PROP_ID_version,value)){
            this._version = value;
            internalClearRefs(PROP_ID_version);
            
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
     * 积分账户
     */
    public final app.mall.dao.entity.LitemallPointsAccount getAccount(){
       return (app.mall.dao.entity.LitemallPointsAccount)internalGetRefEntity(PROP_NAME_account);
    }

    public final void setAccount(app.mall.dao.entity.LitemallPointsAccount refEntity){
   
           if(refEntity == null){
           
                   this.setAccountId(null);
               
           }else{
           internalSetRefEntity(PROP_NAME_account, refEntity,()->{
           
                           this.setAccountId(refEntity.getId());
                       
           });
           }
       
    }
       
}
// resume CPD analysis - CPD-ON
