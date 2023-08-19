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

import app.mall.dao.entity.LitemallUser;

// tell cpd to start ignoring code - CPD-OFF
/**
 *  用户表: litemall_user
 */
@SuppressWarnings({"PMD.UselessOverridingMethod","PMD.UnusedLocalVariable",
        "PMD.UnnecessaryFullyQualifiedName","PMD.EmptyControlStatement"})
public class _LitemallUser extends DynamicOrmEntity{
    
    /* Id: ID INTEGER */
    public static final String PROP_NAME_id = "id";
    public static final int PROP_ID_id = 1;
    
    /* 用户名称: USERNAME VARCHAR */
    public static final String PROP_NAME_username = "username";
    public static final int PROP_ID_username = 2;
    
    /* 用户密码: PASSWORD VARCHAR */
    public static final String PROP_NAME_password = "password";
    public static final int PROP_ID_password = 3;
    
    /* 性别: GENDER TINYINT */
    public static final String PROP_NAME_gender = "gender";
    public static final int PROP_ID_gender = 4;
    
    /* 生日: BIRTHDAY DATE */
    public static final String PROP_NAME_birthday = "birthday";
    public static final int PROP_ID_birthday = 5;
    
    /* 最近一次登录时间: LAST_LOGIN_TIME DATETIME */
    public static final String PROP_NAME_lastLoginTime = "lastLoginTime";
    public static final int PROP_ID_lastLoginTime = 6;
    
    /* 最近一次登录IP地址: LAST_LOGIN_IP VARCHAR */
    public static final String PROP_NAME_lastLoginIp = "lastLoginIp";
    public static final int PROP_ID_lastLoginIp = 7;
    
    /* 用户等级: USER_LEVEL TINYINT */
    public static final String PROP_NAME_userLevel = "userLevel";
    public static final int PROP_ID_userLevel = 8;
    
    /* 用户昵称或网络名称: NICKNAME VARCHAR */
    public static final String PROP_NAME_nickname = "nickname";
    public static final int PROP_ID_nickname = 9;
    
    /* 用户手机号码: MOBILE VARCHAR */
    public static final String PROP_NAME_mobile = "mobile";
    public static final int PROP_ID_mobile = 10;
    
    /* 用户头像图片: AVATAR VARCHAR */
    public static final String PROP_NAME_avatar = "avatar";
    public static final int PROP_ID_avatar = 11;
    
    /* 微信登录openid: WEIXIN_OPENID VARCHAR */
    public static final String PROP_NAME_weixinOpenid = "weixinOpenid";
    public static final int PROP_ID_weixinOpenid = 12;
    
    /* 微信登录会话KEY: SESSION_KEY VARCHAR */
    public static final String PROP_NAME_sessionKey = "sessionKey";
    public static final int PROP_ID_sessionKey = 13;
    
    /* 用户状态: STATUS TINYINT */
    public static final String PROP_NAME_status = "status";
    public static final int PROP_ID_status = 14;
    
    /* 创建时间: ADD_TIME DATETIME */
    public static final String PROP_NAME_addTime = "addTime";
    public static final int PROP_ID_addTime = 15;
    
    /* 更新时间: UPDATE_TIME DATETIME */
    public static final String PROP_NAME_updateTime = "updateTime";
    public static final int PROP_ID_updateTime = 16;
    
    /* 逻辑删除: DELETED BOOLEAN */
    public static final String PROP_NAME_deleted = "deleted";
    public static final int PROP_ID_deleted = 17;
    

    private static int _PROP_ID_BOUND = 18;

    
    /* relation: 角色映射 */
    public static final String PROP_NAME_roleMappings = "roleMappings";
    
    /* component:  */
    public static final String PROP_NAME_avatarComponent = "avatarComponent";
    

    public static final List<String> PK_PROP_NAMES = Arrays.asList(PROP_NAME_id);
    public static final int[] PK_PROP_IDS = new int[]{PROP_ID_id};

    private static final String[] PROP_ID_TO_NAME = new String[18];
    private static final Map<String,Integer> PROP_NAME_TO_ID = new HashMap<>();
    static{
      
          PROP_ID_TO_NAME[PROP_ID_id] = PROP_NAME_id;
          PROP_NAME_TO_ID.put(PROP_NAME_id, PROP_ID_id);
      
          PROP_ID_TO_NAME[PROP_ID_username] = PROP_NAME_username;
          PROP_NAME_TO_ID.put(PROP_NAME_username, PROP_ID_username);
      
          PROP_ID_TO_NAME[PROP_ID_password] = PROP_NAME_password;
          PROP_NAME_TO_ID.put(PROP_NAME_password, PROP_ID_password);
      
          PROP_ID_TO_NAME[PROP_ID_gender] = PROP_NAME_gender;
          PROP_NAME_TO_ID.put(PROP_NAME_gender, PROP_ID_gender);
      
          PROP_ID_TO_NAME[PROP_ID_birthday] = PROP_NAME_birthday;
          PROP_NAME_TO_ID.put(PROP_NAME_birthday, PROP_ID_birthday);
      
          PROP_ID_TO_NAME[PROP_ID_lastLoginTime] = PROP_NAME_lastLoginTime;
          PROP_NAME_TO_ID.put(PROP_NAME_lastLoginTime, PROP_ID_lastLoginTime);
      
          PROP_ID_TO_NAME[PROP_ID_lastLoginIp] = PROP_NAME_lastLoginIp;
          PROP_NAME_TO_ID.put(PROP_NAME_lastLoginIp, PROP_ID_lastLoginIp);
      
          PROP_ID_TO_NAME[PROP_ID_userLevel] = PROP_NAME_userLevel;
          PROP_NAME_TO_ID.put(PROP_NAME_userLevel, PROP_ID_userLevel);
      
          PROP_ID_TO_NAME[PROP_ID_nickname] = PROP_NAME_nickname;
          PROP_NAME_TO_ID.put(PROP_NAME_nickname, PROP_ID_nickname);
      
          PROP_ID_TO_NAME[PROP_ID_mobile] = PROP_NAME_mobile;
          PROP_NAME_TO_ID.put(PROP_NAME_mobile, PROP_ID_mobile);
      
          PROP_ID_TO_NAME[PROP_ID_avatar] = PROP_NAME_avatar;
          PROP_NAME_TO_ID.put(PROP_NAME_avatar, PROP_ID_avatar);
      
          PROP_ID_TO_NAME[PROP_ID_weixinOpenid] = PROP_NAME_weixinOpenid;
          PROP_NAME_TO_ID.put(PROP_NAME_weixinOpenid, PROP_ID_weixinOpenid);
      
          PROP_ID_TO_NAME[PROP_ID_sessionKey] = PROP_NAME_sessionKey;
          PROP_NAME_TO_ID.put(PROP_NAME_sessionKey, PROP_ID_sessionKey);
      
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
    
    /* 用户名称: USERNAME */
    private java.lang.String _username;
    
    /* 用户密码: PASSWORD */
    private java.lang.String _password;
    
    /* 性别: GENDER */
    private java.lang.Byte _gender;
    
    /* 生日: BIRTHDAY */
    private java.time.LocalDate _birthday;
    
    /* 最近一次登录时间: LAST_LOGIN_TIME */
    private java.time.LocalDateTime _lastLoginTime;
    
    /* 最近一次登录IP地址: LAST_LOGIN_IP */
    private java.lang.String _lastLoginIp;
    
    /* 用户等级: USER_LEVEL */
    private java.lang.Byte _userLevel;
    
    /* 用户昵称或网络名称: NICKNAME */
    private java.lang.String _nickname;
    
    /* 用户手机号码: MOBILE */
    private java.lang.String _mobile;
    
    /* 用户头像图片: AVATAR */
    private java.lang.String _avatar;
    
    /* 微信登录openid: WEIXIN_OPENID */
    private java.lang.String _weixinOpenid;
    
    /* 微信登录会话KEY: SESSION_KEY */
    private java.lang.String _sessionKey;
    
    /* 用户状态: STATUS */
    private java.lang.Byte _status;
    
    /* 创建时间: ADD_TIME */
    private java.time.LocalDateTime _addTime;
    
    /* 更新时间: UPDATE_TIME */
    private java.time.LocalDateTime _updateTime;
    
    /* 逻辑删除: DELETED */
    private java.lang.Boolean _deleted;
    

    public _LitemallUser(){
    }

    protected LitemallUser newInstance(){
       return new LitemallUser();
    }

    @Override
    public LitemallUser cloneInstance() {
        LitemallUser entity = newInstance();
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
      return "app.mall.dao.entity.LitemallUser";
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
        
            case PROP_ID_gender:
               return getGender();
        
            case PROP_ID_birthday:
               return getBirthday();
        
            case PROP_ID_lastLoginTime:
               return getLastLoginTime();
        
            case PROP_ID_lastLoginIp:
               return getLastLoginIp();
        
            case PROP_ID_userLevel:
               return getUserLevel();
        
            case PROP_ID_nickname:
               return getNickname();
        
            case PROP_ID_mobile:
               return getMobile();
        
            case PROP_ID_avatar:
               return getAvatar();
        
            case PROP_ID_weixinOpenid:
               return getWeixinOpenid();
        
            case PROP_ID_sessionKey:
               return getSessionKey();
        
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
        
            case PROP_ID_gender:{
               java.lang.Byte typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toByte(value,
                       err-> newTypeConversionError(PROP_NAME_gender));
               }
               setGender(typedValue);
               break;
            }
        
            case PROP_ID_birthday:{
               java.time.LocalDate typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toLocalDate(value,
                       err-> newTypeConversionError(PROP_NAME_birthday));
               }
               setBirthday(typedValue);
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
        
            case PROP_ID_lastLoginIp:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_lastLoginIp));
               }
               setLastLoginIp(typedValue);
               break;
            }
        
            case PROP_ID_userLevel:{
               java.lang.Byte typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toByte(value,
                       err-> newTypeConversionError(PROP_NAME_userLevel));
               }
               setUserLevel(typedValue);
               break;
            }
        
            case PROP_ID_nickname:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_nickname));
               }
               setNickname(typedValue);
               break;
            }
        
            case PROP_ID_mobile:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_mobile));
               }
               setMobile(typedValue);
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
        
            case PROP_ID_weixinOpenid:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_weixinOpenid));
               }
               setWeixinOpenid(typedValue);
               break;
            }
        
            case PROP_ID_sessionKey:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_sessionKey));
               }
               setSessionKey(typedValue);
               break;
            }
        
            case PROP_ID_status:{
               java.lang.Byte typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toByte(value,
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
        
            case PROP_ID_gender:{
               onInitProp(propId);
               this._gender = (java.lang.Byte)value;
               
               break;
            }
        
            case PROP_ID_birthday:{
               onInitProp(propId);
               this._birthday = (java.time.LocalDate)value;
               
               break;
            }
        
            case PROP_ID_lastLoginTime:{
               onInitProp(propId);
               this._lastLoginTime = (java.time.LocalDateTime)value;
               
               break;
            }
        
            case PROP_ID_lastLoginIp:{
               onInitProp(propId);
               this._lastLoginIp = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_userLevel:{
               onInitProp(propId);
               this._userLevel = (java.lang.Byte)value;
               
               break;
            }
        
            case PROP_ID_nickname:{
               onInitProp(propId);
               this._nickname = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_mobile:{
               onInitProp(propId);
               this._mobile = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_avatar:{
               onInitProp(propId);
               this._avatar = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_weixinOpenid:{
               onInitProp(propId);
               this._weixinOpenid = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_sessionKey:{
               onInitProp(propId);
               this._sessionKey = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_status:{
               onInitProp(propId);
               this._status = (java.lang.Byte)value;
               
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
     * 用户名称: USERNAME
     */
    public java.lang.String getUsername(){
         onPropGet(PROP_ID_username);
         return _username;
    }

    /**
     * 用户名称: USERNAME
     */
    public void setUsername(java.lang.String value){
        if(onPropSet(PROP_ID_username,value)){
            this._username = value;
            internalClearRefs(PROP_ID_username);
            
        }
    }
    
    /**
     * 用户密码: PASSWORD
     */
    public java.lang.String getPassword(){
         onPropGet(PROP_ID_password);
         return _password;
    }

    /**
     * 用户密码: PASSWORD
     */
    public void setPassword(java.lang.String value){
        if(onPropSet(PROP_ID_password,value)){
            this._password = value;
            internalClearRefs(PROP_ID_password);
            
        }
    }
    
    /**
     * 性别: GENDER
     */
    public java.lang.Byte getGender(){
         onPropGet(PROP_ID_gender);
         return _gender;
    }

    /**
     * 性别: GENDER
     */
    public void setGender(java.lang.Byte value){
        if(onPropSet(PROP_ID_gender,value)){
            this._gender = value;
            internalClearRefs(PROP_ID_gender);
            
        }
    }
    
    /**
     * 生日: BIRTHDAY
     */
    public java.time.LocalDate getBirthday(){
         onPropGet(PROP_ID_birthday);
         return _birthday;
    }

    /**
     * 生日: BIRTHDAY
     */
    public void setBirthday(java.time.LocalDate value){
        if(onPropSet(PROP_ID_birthday,value)){
            this._birthday = value;
            internalClearRefs(PROP_ID_birthday);
            
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
     * 用户等级: USER_LEVEL
     */
    public java.lang.Byte getUserLevel(){
         onPropGet(PROP_ID_userLevel);
         return _userLevel;
    }

    /**
     * 用户等级: USER_LEVEL
     */
    public void setUserLevel(java.lang.Byte value){
        if(onPropSet(PROP_ID_userLevel,value)){
            this._userLevel = value;
            internalClearRefs(PROP_ID_userLevel);
            
        }
    }
    
    /**
     * 用户昵称或网络名称: NICKNAME
     */
    public java.lang.String getNickname(){
         onPropGet(PROP_ID_nickname);
         return _nickname;
    }

    /**
     * 用户昵称或网络名称: NICKNAME
     */
    public void setNickname(java.lang.String value){
        if(onPropSet(PROP_ID_nickname,value)){
            this._nickname = value;
            internalClearRefs(PROP_ID_nickname);
            
        }
    }
    
    /**
     * 用户手机号码: MOBILE
     */
    public java.lang.String getMobile(){
         onPropGet(PROP_ID_mobile);
         return _mobile;
    }

    /**
     * 用户手机号码: MOBILE
     */
    public void setMobile(java.lang.String value){
        if(onPropSet(PROP_ID_mobile,value)){
            this._mobile = value;
            internalClearRefs(PROP_ID_mobile);
            
        }
    }
    
    /**
     * 用户头像图片: AVATAR
     */
    public java.lang.String getAvatar(){
         onPropGet(PROP_ID_avatar);
         return _avatar;
    }

    /**
     * 用户头像图片: AVATAR
     */
    public void setAvatar(java.lang.String value){
        if(onPropSet(PROP_ID_avatar,value)){
            this._avatar = value;
            internalClearRefs(PROP_ID_avatar);
            
        }
    }
    
    /**
     * 微信登录openid: WEIXIN_OPENID
     */
    public java.lang.String getWeixinOpenid(){
         onPropGet(PROP_ID_weixinOpenid);
         return _weixinOpenid;
    }

    /**
     * 微信登录openid: WEIXIN_OPENID
     */
    public void setWeixinOpenid(java.lang.String value){
        if(onPropSet(PROP_ID_weixinOpenid,value)){
            this._weixinOpenid = value;
            internalClearRefs(PROP_ID_weixinOpenid);
            
        }
    }
    
    /**
     * 微信登录会话KEY: SESSION_KEY
     */
    public java.lang.String getSessionKey(){
         onPropGet(PROP_ID_sessionKey);
         return _sessionKey;
    }

    /**
     * 微信登录会话KEY: SESSION_KEY
     */
    public void setSessionKey(java.lang.String value){
        if(onPropSet(PROP_ID_sessionKey,value)){
            this._sessionKey = value;
            internalClearRefs(PROP_ID_sessionKey);
            
        }
    }
    
    /**
     * 用户状态: STATUS
     */
    public java.lang.Byte getStatus(){
         onPropGet(PROP_ID_status);
         return _status;
    }

    /**
     * 用户状态: STATUS
     */
    public void setStatus(java.lang.Byte value){
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
    
    private final OrmEntitySet<app.mall.dao.entity.LitemallUserRole> _roleMappings = new OrmEntitySet<>(this, PROP_NAME_roleMappings,
        app.mall.dao.entity.LitemallUserRole.PROP_NAME_user, null,app.mall.dao.entity.LitemallUserRole.class);

    /**
     * 角色映射。 refPropName: user, keyProp: {rel.keyProp}
     */
    public IOrmEntitySet<app.mall.dao.entity.LitemallUserRole> getRoleMappings(){
       return _roleMappings;
    }
       
   private io.nop.orm.support.OrmFileComponent _avatarComponent;

   private static Map<String,Integer> COMPONENT_PROP_ID_MAP_avatarComponent = new HashMap<>();
   static{
      
         COMPONENT_PROP_ID_MAP_avatarComponent.put(io.nop.orm.support.OrmFileComponent.PROP_NAME_filePath,PROP_ID_avatar);
      
   }

   public io.nop.orm.support.OrmFileComponent getAvatarComponent(){
      if(_avatarComponent == null){
          _avatarComponent = new io.nop.orm.support.OrmFileComponent();
          _avatarComponent.bindToEntity(this, COMPONENT_PROP_ID_MAP_avatarComponent);
      }
      return _avatarComponent;
   }

        public List<app.mall.dao.entity.LitemallRole> getRelatedRoleList(){
            return (List<app.mall.dao.entity.LitemallRole>)io.nop.orm.support.OrmEntityHelper.getRefProps(getRoleMappings(),app.mall.dao.entity.LitemallUserRole.PROP_NAME_role);
        }
    
        public List<java.lang.String> getRelatedRoleIdList(){
        return (List<java.lang.String>)io.nop.orm.support.OrmEntityHelper.getRefProps(getRoleMappings(),app.mall.dao.entity.LitemallUserRole.PROP_NAME_roleId);
        }

        public void setRelatedRoleIdList(List<java.lang.String> value){
        io.nop.orm.support.OrmEntityHelper.setRefProps(getRoleMappings(),app.mall.dao.entity.LitemallUserRole.PROP_NAME_roleId,value);
        }
    
}
// resume CPD analysis - CPD-ON
