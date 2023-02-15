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

import app.mall.dao.entity.LitemallNoticeAdmin;

// tell cpd to start ignoring code - CPD-OFF
/**
 *  通知管理员表: litemall_notice_admin
 */
@SuppressWarnings({"PMD.UselessOverridingMethod","PMD.UnusedLocalVariable",
        "PMD.UnnecessaryFullyQualifiedName","PMD.EmptyControlStatement"})
public class _LitemallNoticeAdmin extends DynamicOrmEntity{
    
    /* Id: ID INTEGER */
    public static final String PROP_NAME_id = "id";
    public static final int PROP_ID_id = 1;
    
    /* 通知ID: NOTICE_ID INTEGER */
    public static final String PROP_NAME_noticeId = "noticeId";
    public static final int PROP_ID_noticeId = 2;
    
    /* 通知标题: NOTICE_TITLE VARCHAR */
    public static final String PROP_NAME_noticeTitle = "noticeTitle";
    public static final int PROP_ID_noticeTitle = 3;
    
    /* 接收通知的管理员ID: ADMIN_ID INTEGER */
    public static final String PROP_NAME_adminId = "adminId";
    public static final int PROP_ID_adminId = 4;
    
    /* 阅读时间，如果是NULL则是未读状态: READ_TIME DATETIME */
    public static final String PROP_NAME_readTime = "readTime";
    public static final int PROP_ID_readTime = 5;
    
    /* 创建时间: ADD_TIME DATETIME */
    public static final String PROP_NAME_addTime = "addTime";
    public static final int PROP_ID_addTime = 6;
    
    /* 更新时间: UPDATE_TIME DATETIME */
    public static final String PROP_NAME_updateTime = "updateTime";
    public static final int PROP_ID_updateTime = 7;
    
    /* 逻辑删除: DELETED BOOLEAN */
    public static final String PROP_NAME_deleted = "deleted";
    public static final int PROP_ID_deleted = 8;
    

    private static int _PROP_ID_BOUND = 9;

    

    public static final List<String> PK_PROP_NAMES = Arrays.asList(PROP_NAME_id);
    public static final int[] PK_PROP_IDS = new int[]{PROP_ID_id};

    private static final String[] PROP_ID_TO_NAME = new String[9];
    private static final Map<String,Integer> PROP_NAME_TO_ID = new HashMap<>();
    static{
      
          PROP_ID_TO_NAME[PROP_ID_id] = PROP_NAME_id;
          PROP_NAME_TO_ID.put(PROP_NAME_id, PROP_ID_id);
      
          PROP_ID_TO_NAME[PROP_ID_noticeId] = PROP_NAME_noticeId;
          PROP_NAME_TO_ID.put(PROP_NAME_noticeId, PROP_ID_noticeId);
      
          PROP_ID_TO_NAME[PROP_ID_noticeTitle] = PROP_NAME_noticeTitle;
          PROP_NAME_TO_ID.put(PROP_NAME_noticeTitle, PROP_ID_noticeTitle);
      
          PROP_ID_TO_NAME[PROP_ID_adminId] = PROP_NAME_adminId;
          PROP_NAME_TO_ID.put(PROP_NAME_adminId, PROP_ID_adminId);
      
          PROP_ID_TO_NAME[PROP_ID_readTime] = PROP_NAME_readTime;
          PROP_NAME_TO_ID.put(PROP_NAME_readTime, PROP_ID_readTime);
      
          PROP_ID_TO_NAME[PROP_ID_addTime] = PROP_NAME_addTime;
          PROP_NAME_TO_ID.put(PROP_NAME_addTime, PROP_ID_addTime);
      
          PROP_ID_TO_NAME[PROP_ID_updateTime] = PROP_NAME_updateTime;
          PROP_NAME_TO_ID.put(PROP_NAME_updateTime, PROP_ID_updateTime);
      
          PROP_ID_TO_NAME[PROP_ID_deleted] = PROP_NAME_deleted;
          PROP_NAME_TO_ID.put(PROP_NAME_deleted, PROP_ID_deleted);
      
    }

    
    /* Id: ID */
    private java.lang.Integer _id;
    
    /* 通知ID: NOTICE_ID */
    private java.lang.Integer _noticeId;
    
    /* 通知标题: NOTICE_TITLE */
    private java.lang.String _noticeTitle;
    
    /* 接收通知的管理员ID: ADMIN_ID */
    private java.lang.Integer _adminId;
    
    /* 阅读时间，如果是NULL则是未读状态: READ_TIME */
    private java.time.LocalDateTime _readTime;
    
    /* 创建时间: ADD_TIME */
    private java.time.LocalDateTime _addTime;
    
    /* 更新时间: UPDATE_TIME */
    private java.time.LocalDateTime _updateTime;
    
    /* 逻辑删除: DELETED */
    private java.lang.Boolean _deleted;
    

    public _LitemallNoticeAdmin(){
    }

    protected LitemallNoticeAdmin newInstance(){
       return new LitemallNoticeAdmin();
    }

    @Override
    public LitemallNoticeAdmin cloneInstance() {
        LitemallNoticeAdmin entity = newInstance();
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
      return "app.mall.dao.entity.LitemallNoticeAdmin";
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
        
            case PROP_ID_noticeId:
               return getNoticeId();
        
            case PROP_ID_noticeTitle:
               return getNoticeTitle();
        
            case PROP_ID_adminId:
               return getAdminId();
        
            case PROP_ID_readTime:
               return getReadTime();
        
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
        
            case PROP_ID_noticeId:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_noticeId));
               }
               setNoticeId(typedValue);
               break;
            }
        
            case PROP_ID_noticeTitle:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_noticeTitle));
               }
               setNoticeTitle(typedValue);
               break;
            }
        
            case PROP_ID_adminId:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_adminId));
               }
               setAdminId(typedValue);
               break;
            }
        
            case PROP_ID_readTime:{
               java.time.LocalDateTime typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toLocalDateTime(value,
                       err-> newTypeConversionError(PROP_NAME_readTime));
               }
               setReadTime(typedValue);
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
        
            case PROP_ID_noticeId:{
               onInitProp(propId);
               this._noticeId = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_noticeTitle:{
               onInitProp(propId);
               this._noticeTitle = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_adminId:{
               onInitProp(propId);
               this._adminId = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_readTime:{
               onInitProp(propId);
               this._readTime = (java.time.LocalDateTime)value;
               
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
     * 通知ID: NOTICE_ID
     */
    public java.lang.Integer getNoticeId(){
         onPropGet(PROP_ID_noticeId);
         return _noticeId;
    }

    /**
     * 通知ID: NOTICE_ID
     */
    public void setNoticeId(java.lang.Integer value){
        if(onPropSet(PROP_ID_noticeId,value)){
            this._noticeId = value;
            internalClearRefs(PROP_ID_noticeId);
            
        }
    }
    
    /**
     * 通知标题: NOTICE_TITLE
     */
    public java.lang.String getNoticeTitle(){
         onPropGet(PROP_ID_noticeTitle);
         return _noticeTitle;
    }

    /**
     * 通知标题: NOTICE_TITLE
     */
    public void setNoticeTitle(java.lang.String value){
        if(onPropSet(PROP_ID_noticeTitle,value)){
            this._noticeTitle = value;
            internalClearRefs(PROP_ID_noticeTitle);
            
        }
    }
    
    /**
     * 接收通知的管理员ID: ADMIN_ID
     */
    public java.lang.Integer getAdminId(){
         onPropGet(PROP_ID_adminId);
         return _adminId;
    }

    /**
     * 接收通知的管理员ID: ADMIN_ID
     */
    public void setAdminId(java.lang.Integer value){
        if(onPropSet(PROP_ID_adminId,value)){
            this._adminId = value;
            internalClearRefs(PROP_ID_adminId);
            
        }
    }
    
    /**
     * 阅读时间，如果是NULL则是未读状态: READ_TIME
     */
    public java.time.LocalDateTime getReadTime(){
         onPropGet(PROP_ID_readTime);
         return _readTime;
    }

    /**
     * 阅读时间，如果是NULL则是未读状态: READ_TIME
     */
    public void setReadTime(java.time.LocalDateTime value){
        if(onPropSet(PROP_ID_readTime,value)){
            this._readTime = value;
            internalClearRefs(PROP_ID_readTime);
            
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
