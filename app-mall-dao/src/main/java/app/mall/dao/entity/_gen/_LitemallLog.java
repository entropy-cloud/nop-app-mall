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

import app.mall.dao.entity.LitemallLog;

// tell cpd to start ignoring code - CPD-OFF
/**
 *  操作日志表: litemall_log
 */
@SuppressWarnings({"PMD.UselessOverridingMethod","PMD.UnusedLocalVariable","java:S3008","java:S1602","java:S1128","java:S1161",
        "PMD.UnnecessaryFullyQualifiedName","PMD.EmptyControlStatement","java:S116","java:S115","java:S101","java:S3776"})
public class _LitemallLog extends DynamicOrmEntity{
    
    /* Id: ID INTEGER */
    public static final String PROP_NAME_id = "id";
    public static final int PROP_ID_id = 1;
    
    /* 管理员: ADMIN VARCHAR */
    public static final String PROP_NAME_admin = "admin";
    public static final int PROP_ID_admin = 2;
    
    /* 管理员地址: IP VARCHAR */
    public static final String PROP_NAME_ip = "ip";
    public static final int PROP_ID_ip = 3;
    
    /* 操作分类: TYPE INTEGER */
    public static final String PROP_NAME_type = "type";
    public static final int PROP_ID_type = 4;
    
    /* 操作动作: ACTION VARCHAR */
    public static final String PROP_NAME_action = "action";
    public static final int PROP_ID_action = 5;
    
    /* 操作状态: STATUS BOOLEAN */
    public static final String PROP_NAME_status = "status";
    public static final int PROP_ID_status = 6;
    
    /* 操作结果/消息: RESULT VARCHAR */
    public static final String PROP_NAME_result = "result";
    public static final int PROP_ID_result = 7;
    
    /* 补充信息: COMMENT VARCHAR */
    public static final String PROP_NAME_comment = "comment";
    public static final int PROP_ID_comment = 8;
    
    /* 创建时间: ADD_TIME DATETIME */
    public static final String PROP_NAME_addTime = "addTime";
    public static final int PROP_ID_addTime = 9;
    
    /* 更新时间: UPDATE_TIME DATETIME */
    public static final String PROP_NAME_updateTime = "updateTime";
    public static final int PROP_ID_updateTime = 10;
    
    /* 逻辑删除: DELETED BOOLEAN */
    public static final String PROP_NAME_deleted = "deleted";
    public static final int PROP_ID_deleted = 11;
    

    private static int _PROP_ID_BOUND = 12;

    

    protected static final List<String> PK_PROP_NAMES = Arrays.asList(PROP_NAME_id);
    protected static final int[] PK_PROP_IDS = new int[]{PROP_ID_id};

    private static final String[] PROP_ID_TO_NAME = new String[12];
    private static final Map<String,Integer> PROP_NAME_TO_ID = new HashMap<>();
    static{
      
          PROP_ID_TO_NAME[PROP_ID_id] = PROP_NAME_id;
          PROP_NAME_TO_ID.put(PROP_NAME_id, PROP_ID_id);
      
          PROP_ID_TO_NAME[PROP_ID_admin] = PROP_NAME_admin;
          PROP_NAME_TO_ID.put(PROP_NAME_admin, PROP_ID_admin);
      
          PROP_ID_TO_NAME[PROP_ID_ip] = PROP_NAME_ip;
          PROP_NAME_TO_ID.put(PROP_NAME_ip, PROP_ID_ip);
      
          PROP_ID_TO_NAME[PROP_ID_type] = PROP_NAME_type;
          PROP_NAME_TO_ID.put(PROP_NAME_type, PROP_ID_type);
      
          PROP_ID_TO_NAME[PROP_ID_action] = PROP_NAME_action;
          PROP_NAME_TO_ID.put(PROP_NAME_action, PROP_ID_action);
      
          PROP_ID_TO_NAME[PROP_ID_status] = PROP_NAME_status;
          PROP_NAME_TO_ID.put(PROP_NAME_status, PROP_ID_status);
      
          PROP_ID_TO_NAME[PROP_ID_result] = PROP_NAME_result;
          PROP_NAME_TO_ID.put(PROP_NAME_result, PROP_ID_result);
      
          PROP_ID_TO_NAME[PROP_ID_comment] = PROP_NAME_comment;
          PROP_NAME_TO_ID.put(PROP_NAME_comment, PROP_ID_comment);
      
          PROP_ID_TO_NAME[PROP_ID_addTime] = PROP_NAME_addTime;
          PROP_NAME_TO_ID.put(PROP_NAME_addTime, PROP_ID_addTime);
      
          PROP_ID_TO_NAME[PROP_ID_updateTime] = PROP_NAME_updateTime;
          PROP_NAME_TO_ID.put(PROP_NAME_updateTime, PROP_ID_updateTime);
      
          PROP_ID_TO_NAME[PROP_ID_deleted] = PROP_NAME_deleted;
          PROP_NAME_TO_ID.put(PROP_NAME_deleted, PROP_ID_deleted);
      
    }

    
    /* Id: ID */
    private java.lang.Integer _id;
    
    /* 管理员: ADMIN */
    private java.lang.String _admin;
    
    /* 管理员地址: IP */
    private java.lang.String _ip;
    
    /* 操作分类: TYPE */
    private java.lang.Integer _type;
    
    /* 操作动作: ACTION */
    private java.lang.String _action;
    
    /* 操作状态: STATUS */
    private java.lang.Boolean _status;
    
    /* 操作结果/消息: RESULT */
    private java.lang.String _result;
    
    /* 补充信息: COMMENT */
    private java.lang.String _comment;
    
    /* 创建时间: ADD_TIME */
    private java.time.LocalDateTime _addTime;
    
    /* 更新时间: UPDATE_TIME */
    private java.time.LocalDateTime _updateTime;
    
    /* 逻辑删除: DELETED */
    private java.lang.Boolean _deleted;
    

    public _LitemallLog(){
        // for debug
    }

    protected LitemallLog newInstance(){
        LitemallLog entity = new LitemallLog();
        entity.orm_attach(orm_enhancer());
        entity.orm_entityModel(orm_entityModel());
        return entity;
    }

    @Override
    public LitemallLog cloneInstance() {
        LitemallLog entity = newInstance();
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
      return "app.mall.dao.entity.LitemallLog";
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
        
            case PROP_ID_admin:
               return getAdmin();
        
            case PROP_ID_ip:
               return getIp();
        
            case PROP_ID_type:
               return getType();
        
            case PROP_ID_action:
               return getAction();
        
            case PROP_ID_status:
               return getStatus();
        
            case PROP_ID_result:
               return getResult();
        
            case PROP_ID_comment:
               return getComment();
        
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
        
            case PROP_ID_admin:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_admin));
               }
               setAdmin(typedValue);
               break;
            }
        
            case PROP_ID_ip:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_ip));
               }
               setIp(typedValue);
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
        
            case PROP_ID_action:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_action));
               }
               setAction(typedValue);
               break;
            }
        
            case PROP_ID_status:{
               java.lang.Boolean typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toBoolean(value,
                       err-> newTypeConversionError(PROP_NAME_status));
               }
               setStatus(typedValue);
               break;
            }
        
            case PROP_ID_result:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_result));
               }
               setResult(typedValue);
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
        
            case PROP_ID_admin:{
               onInitProp(propId);
               this._admin = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_ip:{
               onInitProp(propId);
               this._ip = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_type:{
               onInitProp(propId);
               this._type = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_action:{
               onInitProp(propId);
               this._action = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_status:{
               onInitProp(propId);
               this._status = (java.lang.Boolean)value;
               
               break;
            }
        
            case PROP_ID_result:{
               onInitProp(propId);
               this._result = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_comment:{
               onInitProp(propId);
               this._comment = (java.lang.String)value;
               
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
     * 管理员: ADMIN
     */
    public java.lang.String getAdmin(){
         onPropGet(PROP_ID_admin);
         return _admin;
    }

    /**
     * 管理员: ADMIN
     */
    public void setAdmin(java.lang.String value){
        if(onPropSet(PROP_ID_admin,value)){
            this._admin = value;
            internalClearRefs(PROP_ID_admin);
            
        }
    }
    
    /**
     * 管理员地址: IP
     */
    public java.lang.String getIp(){
         onPropGet(PROP_ID_ip);
         return _ip;
    }

    /**
     * 管理员地址: IP
     */
    public void setIp(java.lang.String value){
        if(onPropSet(PROP_ID_ip,value)){
            this._ip = value;
            internalClearRefs(PROP_ID_ip);
            
        }
    }
    
    /**
     * 操作分类: TYPE
     */
    public java.lang.Integer getType(){
         onPropGet(PROP_ID_type);
         return _type;
    }

    /**
     * 操作分类: TYPE
     */
    public void setType(java.lang.Integer value){
        if(onPropSet(PROP_ID_type,value)){
            this._type = value;
            internalClearRefs(PROP_ID_type);
            
        }
    }
    
    /**
     * 操作动作: ACTION
     */
    public java.lang.String getAction(){
         onPropGet(PROP_ID_action);
         return _action;
    }

    /**
     * 操作动作: ACTION
     */
    public void setAction(java.lang.String value){
        if(onPropSet(PROP_ID_action,value)){
            this._action = value;
            internalClearRefs(PROP_ID_action);
            
        }
    }
    
    /**
     * 操作状态: STATUS
     */
    public java.lang.Boolean getStatus(){
         onPropGet(PROP_ID_status);
         return _status;
    }

    /**
     * 操作状态: STATUS
     */
    public void setStatus(java.lang.Boolean value){
        if(onPropSet(PROP_ID_status,value)){
            this._status = value;
            internalClearRefs(PROP_ID_status);
            
        }
    }
    
    /**
     * 操作结果/消息: RESULT
     */
    public java.lang.String getResult(){
         onPropGet(PROP_ID_result);
         return _result;
    }

    /**
     * 操作结果/消息: RESULT
     */
    public void setResult(java.lang.String value){
        if(onPropSet(PROP_ID_result,value)){
            this._result = value;
            internalClearRefs(PROP_ID_result);
            
        }
    }
    
    /**
     * 补充信息: COMMENT
     */
    public java.lang.String getComment(){
         onPropGet(PROP_ID_comment);
         return _comment;
    }

    /**
     * 补充信息: COMMENT
     */
    public void setComment(java.lang.String value){
        if(onPropSet(PROP_ID_comment,value)){
            this._comment = value;
            internalClearRefs(PROP_ID_comment);
            
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
