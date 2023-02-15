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

import app.mall.dao.entity.LitemallAdmin;

// tell cpd to start ignoring code - CPD-OFF
/**
 *  管理员表: litemall_admin
 */
@SuppressWarnings({"PMD.UselessOverridingMethod","PMD.UnusedLocalVariable",
        "PMD.UnnecessaryFullyQualifiedName","PMD.EmptyControlStatement"})
public class _LitemallAdmin extends DynamicOrmEntity{
    
    /* Id: ID INTEGER */
    public static final String PROP_NAME_id = "id";
    public static final int PROP_ID_id = 1;
    
    /* 管理员名称: USERNAME VARCHAR */
    public static final String PROP_NAME_username = "username";
    public static final int PROP_ID_username = 2;
    
    /* 管理员密码: PASSWORD VARCHAR */
    public static final String PROP_NAME_password = "password";
    public static final int PROP_ID_password = 3;
    
    /* 最近一次登录IP地址: LAST_LOGIN_IP VARCHAR */
    public static final String PROP_NAME_lastLoginIp = "lastLoginIp";
    public static final int PROP_ID_lastLoginIp = 4;
    
    /* 最近一次登录时间: LAST_LOGIN_TIME DATETIME */
    public static final String PROP_NAME_lastLoginTime = "lastLoginTime";
    public static final int PROP_ID_lastLoginTime = 5;
    
    /* 头像图片: AVATAR VARCHAR */
    public static final String PROP_NAME_avatar = "avatar";
    public static final int PROP_ID_avatar = 6;
    
    /* 创建时间: ADD_TIME DATETIME */
    public static final String PROP_NAME_addTime = "addTime";
    public static final int PROP_ID_addTime = 7;
    
    /* 更新时间: UPDATE_TIME DATETIME */
    public static final String PROP_NAME_updateTime = "updateTime";
    public static final int PROP_ID_updateTime = 8;
    
    /* 逻辑删除: DELETED BOOLEAN */
    public static final String PROP_NAME_deleted = "deleted";
    public static final int PROP_ID_deleted = 9;
    
    /* 角色列表: ROLE_IDS VARCHAR */
    public static final String PROP_NAME_roleIds = "roleIds";
    public static final int PROP_ID_roleIds = 10;
    

    private static int _PROP_ID_BOUND = 11;

    

    public static final List<String> PK_PROP_NAMES = Arrays.asList(PROP_NAME_id);
    public static final int[] PK_PROP_IDS = new int[]{PROP_ID_id};

    private static final String[] PROP_ID_TO_NAME = new String[11];
    private static final Map<String,Integer> PROP_NAME_TO_ID = new HashMap<>();
    static{
      
          PROP_ID_TO_NAME[PROP_ID_id] = PROP_NAME_id;
          PROP_NAME_TO_ID.put(PROP_NAME_id, PROP_ID_id);
      
          PROP_ID_TO_NAME[PROP_ID_username] = PROP_NAME_username;
          PROP_NAME_TO_ID.put(PROP_NAME_username, PROP_ID_username);
      
          PROP_ID_TO_NAME[PROP_ID_password] = PROP_NAME_password;
          PROP_NAME_TO_ID.put(PROP_NAME_password, PROP_ID_password);
      
          PROP_ID_TO_NAME[PROP_ID_lastLoginIp] = PROP_NAME_lastLoginIp;
          PROP_NAME_TO_ID.put(PROP_NAME_lastLoginIp, PROP_ID_lastLoginIp);
      
          PROP_ID_TO_NAME[PROP_ID_lastLoginTime] = PROP_NAME_lastLoginTime;
          PROP_NAME_TO_ID.put(PROP_NAME_lastLoginTime, PROP_ID_lastLoginTime);
      
          PROP_ID_TO_NAME[PROP_ID_avatar] = PROP_NAME_avatar;
          PROP_NAME_TO_ID.put(PROP_NAME_avatar, PROP_ID_avatar);
      
          PROP_ID_TO_NAME[PROP_ID_addTime] = PROP_NAME_addTime;
          PROP_NAME_TO_ID.put(PROP_NAME_addTime, PROP_ID_addTime);
      
          PROP_ID_TO_NAME[PROP_ID_updateTime] = PROP_NAME_updateTime;
          PROP_NAME_TO_ID.put(PROP_NAME_updateTime, PROP_ID_updateTime);
      
          PROP_ID_TO_NAME[PROP_ID_deleted] = PROP_NAME_deleted;
          PROP_NAME_TO_ID.put(PROP_NAME_deleted, PROP_ID_deleted);
      
          PROP_ID_TO_NAME[PROP_ID_roleIds] = PROP_NAME_roleIds;
          PROP_NAME_TO_ID.put(PROP_NAME_roleIds, PROP_ID_roleIds);
      
    }

    
    /* Id: ID */
    private java.lang.Integer _id;
    
    /* 管理员名称: USERNAME */
    private java.lang.String _username;
    
    /* 管理员密码: PASSWORD */
    private java.lang.String _password;
    
    /* 最近一次登录IP地址: LAST_LOGIN_IP */
    private java.lang.String _lastLoginIp;
    
    /* 最近一次登录时间: LAST_LOGIN_TIME */
    private java.time.LocalDateTime _lastLoginTime;
    
    /* 头像图片: AVATAR */
    private java.lang.String _avatar;
    
    /* 创建时间: ADD_TIME */
    private java.time.LocalDateTime _addTime;
    
    /* 更新时间: UPDATE_TIME */
    private java.time.LocalDateTime _updateTime;
    
    /* 逻辑删除: DELETED */
    private java.lang.Boolean _deleted;
    
    /* 角色列表: ROLE_IDS */
    private java.lang.String _roleIds;
    

    public _LitemallAdmin(){
    }

    protected LitemallAdmin newInstance(){
       return new LitemallAdmin();
    }

    @Override
    public LitemallAdmin cloneInstance() {
        LitemallAdmin entity = newInstance();
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
      return "app.mall.dao.entity.LitemallAdmin";
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
        
            case PROP_ID_username:
               return getUsername();
        
            case PROP_ID_password:
               return getPassword();
        
            case PROP_ID_lastLoginIp:
               return getLastLoginIp();
        
            case PROP_ID_lastLoginTime:
               return getLastLoginTime();
        
            case PROP_ID_avatar:
               return getAvatar();
        
            case PROP_ID_addTime:
               return getAddTime();
        
            case PROP_ID_updateTime:
               return getUpdateTime();
        
            case PROP_ID_deleted:
               return getDeleted();
        
            case PROP_ID_roleIds:
               return getRoleIds();
        
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
        
            case PROP_ID_username:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_username));
               }
               setUsername(typedValue);
               break;
            }
        
            case PROP_ID_password:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_password));
               }
               setPassword(typedValue);
               break;
            }
        
            case PROP_ID_lastLoginIp:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_lastLoginIp));
               }
               setLastLoginIp(typedValue);
               break;
            }
        
            case PROP_ID_lastLoginTime:{
               java.time.LocalDateTime typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toLocalDateTime(value,
                       err-> newTypeConversionError(PROP_NAME_lastLoginTime));
               }
               setLastLoginTime(typedValue);
               break;
            }
        
            case PROP_ID_avatar:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_avatar));
               }
               setAvatar(typedValue);
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
        
            case PROP_ID_roleIds:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_roleIds));
               }
               setRoleIds(typedValue);
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
        
            case PROP_ID_username:{
               onInitProp(propId);
               this._username = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_password:{
               onInitProp(propId);
               this._password = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_lastLoginIp:{
               onInitProp(propId);
               this._lastLoginIp = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_lastLoginTime:{
               onInitProp(propId);
               this._lastLoginTime = (java.time.LocalDateTime)value;
               
               break;
            }
        
            case PROP_ID_avatar:{
               onInitProp(propId);
               this._avatar = (java.lang.String)value;
               
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
        
            case PROP_ID_roleIds:{
               onInitProp(propId);
               this._roleIds = (java.lang.String)value;
               
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
     * 管理员名称: USERNAME
     */
    public java.lang.String getUsername(){
         onPropGet(PROP_ID_username);
         return _username;
    }

    /**
     * 管理员名称: USERNAME
     */
    public void setUsername(java.lang.String value){
        if(onPropSet(PROP_ID_username,value)){
            this._username = value;
            internalClearRefs(PROP_ID_username);
            
        }
    }
    
    /**
     * 管理员密码: PASSWORD
     */
    public java.lang.String getPassword(){
         onPropGet(PROP_ID_password);
         return _password;
    }

    /**
     * 管理员密码: PASSWORD
     */
    public void setPassword(java.lang.String value){
        if(onPropSet(PROP_ID_password,value)){
            this._password = value;
            internalClearRefs(PROP_ID_password);
            
        }
    }
    
    /**
     * 最近一次登录IP地址: LAST_LOGIN_IP
     */
    public java.lang.String getLastLoginIp(){
         onPropGet(PROP_ID_lastLoginIp);
         return _lastLoginIp;
    }

    /**
     * 最近一次登录IP地址: LAST_LOGIN_IP
     */
    public void setLastLoginIp(java.lang.String value){
        if(onPropSet(PROP_ID_lastLoginIp,value)){
            this._lastLoginIp = value;
            internalClearRefs(PROP_ID_lastLoginIp);
            
        }
    }
    
    /**
     * 最近一次登录时间: LAST_LOGIN_TIME
     */
    public java.time.LocalDateTime getLastLoginTime(){
         onPropGet(PROP_ID_lastLoginTime);
         return _lastLoginTime;
    }

    /**
     * 最近一次登录时间: LAST_LOGIN_TIME
     */
    public void setLastLoginTime(java.time.LocalDateTime value){
        if(onPropSet(PROP_ID_lastLoginTime,value)){
            this._lastLoginTime = value;
            internalClearRefs(PROP_ID_lastLoginTime);
            
        }
    }
    
    /**
     * 头像图片: AVATAR
     */
    public java.lang.String getAvatar(){
         onPropGet(PROP_ID_avatar);
         return _avatar;
    }

    /**
     * 头像图片: AVATAR
     */
    public void setAvatar(java.lang.String value){
        if(onPropSet(PROP_ID_avatar,value)){
            this._avatar = value;
            internalClearRefs(PROP_ID_avatar);
            
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
    
    /**
     * 角色列表: ROLE_IDS
     */
    public java.lang.String getRoleIds(){
         onPropGet(PROP_ID_roleIds);
         return _roleIds;
    }

    /**
     * 角色列表: ROLE_IDS
     */
    public void setRoleIds(java.lang.String value){
        if(onPropSet(PROP_ID_roleIds,value)){
            this._roleIds = value;
            internalClearRefs(PROP_ID_roleIds);
            
        }
    }
    
}
// resume CPD analysis - CPD-ON
