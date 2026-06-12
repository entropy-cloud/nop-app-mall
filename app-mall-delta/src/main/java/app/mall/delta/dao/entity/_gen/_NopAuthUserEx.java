package app.mall.delta.dao.entity._gen;

import io.nop.orm.model.IEntityModel;
import io.nop.orm.support.DynamicOrmEntity;
import io.nop.orm.support.OrmEntitySet; //NOPMD - suppressed UnusedImports - Auto Gen Code
import io.nop.orm.IOrmEntitySet; //NOPMD - suppressed UnusedImports - Auto Gen Code
import io.nop.api.core.convert.ConvertHelper;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;

import app.mall.delta.dao.entity.NopAuthUserEx;

// tell cpd to start ignoring code - CPD-OFF
/**
 *  用户: nop_auth_user
 */
@SuppressWarnings({"PMD.UselessOverridingMethod","PMD.UnusedLocalVariable","java:S3008","java:S1602","java:S1128","java:S1161",
        "PMD.UnnecessaryFullyQualifiedName","PMD.EmptyControlStatement","java:S116","java:S115","java:S101","java:S3776"})
public class _NopAuthUserEx extends io.nop.auth.dao.entity.NopAuthUser{
    
    /* 测试图片: PIC_URL VARCHAR */
    public static final String PROP_NAME_picUrl = "picUrl";
    public static final int PROP_ID_picUrl = 101;
    
    /* 最近登录时间: LAST_LOGIN_TIME DATETIME */
    public static final String PROP_NAME_lastLoginTime = "lastLoginTime";
    public static final int PROP_ID_lastLoginTime = 102;
    
    /* 最近登录IP: LAST_LOGIN_IP VARCHAR */
    public static final String PROP_NAME_lastLoginIp = "lastLoginIp";
    public static final int PROP_ID_lastLoginIp = 103;
    
    /* 用户等级: USER_LEVEL INTEGER */
    public static final String PROP_NAME_userLevel = "userLevel";
    public static final int PROP_ID_userLevel = 104;
    
    /* 微信会话KEY: SESSION_KEY VARCHAR */
    public static final String PROP_NAME_sessionKey = "sessionKey";
    public static final int PROP_ID_sessionKey = 105;
    

    private static int _PROP_ID_BOUND = 106;

    

    protected static final List<String> PK_PROP_NAMES = Arrays.asList(PROP_NAME_userId);
    protected static final int[] PK_PROP_IDS = new int[]{PROP_ID_userId};

    private static final String[] PROP_ID_TO_NAME = new String[106];
    private static final Map<String,Integer> PROP_NAME_TO_ID = new HashMap<>();
    static{
      
          PROP_ID_TO_NAME[PROP_ID_picUrl] = PROP_NAME_picUrl;
          PROP_NAME_TO_ID.put(PROP_NAME_picUrl, PROP_ID_picUrl);
      
          PROP_ID_TO_NAME[PROP_ID_lastLoginTime] = PROP_NAME_lastLoginTime;
          PROP_NAME_TO_ID.put(PROP_NAME_lastLoginTime, PROP_ID_lastLoginTime);
      
          PROP_ID_TO_NAME[PROP_ID_lastLoginIp] = PROP_NAME_lastLoginIp;
          PROP_NAME_TO_ID.put(PROP_NAME_lastLoginIp, PROP_ID_lastLoginIp);
      
          PROP_ID_TO_NAME[PROP_ID_userLevel] = PROP_NAME_userLevel;
          PROP_NAME_TO_ID.put(PROP_NAME_userLevel, PROP_ID_userLevel);
      
          PROP_ID_TO_NAME[PROP_ID_sessionKey] = PROP_NAME_sessionKey;
          PROP_NAME_TO_ID.put(PROP_NAME_sessionKey, PROP_ID_sessionKey);
      
    }

    
    /* 测试图片: PIC_URL */
    private java.lang.String _picUrl;
    
    /* 最近登录时间: LAST_LOGIN_TIME */
    private java.time.LocalDateTime _lastLoginTime;
    
    /* 最近登录IP: LAST_LOGIN_IP */
    private java.lang.String _lastLoginIp;
    
    /* 用户等级: USER_LEVEL */
    private java.lang.Integer _userLevel;
    
    /* 微信会话KEY: SESSION_KEY */
    private java.lang.String _sessionKey;
    

    public _NopAuthUserEx(){
        // for debug
    }

    protected NopAuthUserEx newInstance(){
        NopAuthUserEx entity = new NopAuthUserEx();
        entity.orm_attach(orm_enhancer());
        entity.orm_entityModel(orm_entityModel());
        return entity;
    }

    @Override
    public NopAuthUserEx cloneInstance() {
        NopAuthUserEx entity = newInstance();
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
      return "io.nop.auth.dao.entity.NopAuthUser";
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
    
        return buildSimpleId(PROP_ID_userId);
     
    }

    @Override
    public boolean orm_isPrimary(int propId) {
        
            return propId == PROP_ID_userId;
          
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
        
            case PROP_ID_picUrl:
               return getPicUrl();
        
            case PROP_ID_lastLoginTime:
               return getLastLoginTime();
        
            case PROP_ID_lastLoginIp:
               return getLastLoginIp();
        
            case PROP_ID_userLevel:
               return getUserLevel();
        
            case PROP_ID_sessionKey:
               return getSessionKey();
        
           default:
              return super.orm_propValue(propId);
        }
    }

    

    @Override
    public void orm_propValue(int propId, Object value){
        switch(propId){
        
            case PROP_ID_picUrl:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_picUrl));
               }
               setPicUrl(typedValue);
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
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_userLevel));
               }
               setUserLevel(typedValue);
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
        
           default:
              super.orm_propValue(propId,value);
        }
    }

    @Override
    public void orm_internalSet(int propId, Object value) {
        switch(propId){
        
            case PROP_ID_picUrl:{
               onInitProp(propId);
               this._picUrl = (java.lang.String)value;
               
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
               this._userLevel = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_sessionKey:{
               onInitProp(propId);
               this._sessionKey = (java.lang.String)value;
               
               break;
            }
        
           default:
              super.orm_internalSet(propId,value);
        }
    }

    
    /**
     * 测试图片: PIC_URL
     */
    public final java.lang.String getPicUrl(){
         onPropGet(PROP_ID_picUrl);
         return _picUrl;
    }

    /**
     * 测试图片: PIC_URL
     */
    public final void setPicUrl(java.lang.String value){
        if(onPropSet(PROP_ID_picUrl,value)){
            this._picUrl = value;
            internalClearRefs(PROP_ID_picUrl);
            
        }
    }
    
    /**
     * 最近登录时间: LAST_LOGIN_TIME
     */
    public final java.time.LocalDateTime getLastLoginTime(){
         onPropGet(PROP_ID_lastLoginTime);
         return _lastLoginTime;
    }

    /**
     * 最近登录时间: LAST_LOGIN_TIME
     */
    public final void setLastLoginTime(java.time.LocalDateTime value){
        if(onPropSet(PROP_ID_lastLoginTime,value)){
            this._lastLoginTime = value;
            internalClearRefs(PROP_ID_lastLoginTime);
            
        }
    }
    
    /**
     * 最近登录IP: LAST_LOGIN_IP
     */
    public final java.lang.String getLastLoginIp(){
         onPropGet(PROP_ID_lastLoginIp);
         return _lastLoginIp;
    }

    /**
     * 最近登录IP: LAST_LOGIN_IP
     */
    public final void setLastLoginIp(java.lang.String value){
        if(onPropSet(PROP_ID_lastLoginIp,value)){
            this._lastLoginIp = value;
            internalClearRefs(PROP_ID_lastLoginIp);
            
        }
    }
    
    /**
     * 用户等级: USER_LEVEL
     */
    public final java.lang.Integer getUserLevel(){
         onPropGet(PROP_ID_userLevel);
         return _userLevel;
    }

    /**
     * 用户等级: USER_LEVEL
     */
    public final void setUserLevel(java.lang.Integer value){
        if(onPropSet(PROP_ID_userLevel,value)){
            this._userLevel = value;
            internalClearRefs(PROP_ID_userLevel);
            
        }
    }
    
    /**
     * 微信会话KEY: SESSION_KEY
     */
    public final java.lang.String getSessionKey(){
         onPropGet(PROP_ID_sessionKey);
         return _sessionKey;
    }

    /**
     * 微信会话KEY: SESSION_KEY
     */
    public final void setSessionKey(java.lang.String value){
        if(onPropSet(PROP_ID_sessionKey,value)){
            this._sessionKey = value;
            internalClearRefs(PROP_ID_sessionKey);
            
        }
    }
    
}
// resume CPD analysis - CPD-ON
