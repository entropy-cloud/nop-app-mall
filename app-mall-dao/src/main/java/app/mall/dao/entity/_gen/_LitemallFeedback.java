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

import app.mall.dao.entity.LitemallFeedback;

// tell cpd to start ignoring code - CPD-OFF
/**
 *  意见反馈表: litemall_feedback
 */
@SuppressWarnings({"PMD.UselessOverridingMethod","PMD.UnusedLocalVariable","java:S3008","java:S1602","java:S1128","java:S1161",
        "PMD.UnnecessaryFullyQualifiedName","PMD.EmptyControlStatement","java:S116","java:S115","java:S101","java:S3776"})
public class _LitemallFeedback extends DynamicOrmEntity{
    
    /* Id: ID INTEGER */
    public static final String PROP_NAME_id = "id";
    public static final int PROP_ID_id = 1;
    
    /* 用户ID: USER_ID INTEGER */
    public static final String PROP_NAME_userId = "userId";
    public static final int PROP_ID_userId = 2;
    
    /* 用户名称: USERNAME VARCHAR */
    public static final String PROP_NAME_username = "username";
    public static final int PROP_ID_username = 3;
    
    /* 手机号: MOBILE VARCHAR */
    public static final String PROP_NAME_mobile = "mobile";
    public static final int PROP_ID_mobile = 4;
    
    /* 反馈类型: FEED_TYPE VARCHAR */
    public static final String PROP_NAME_feedType = "feedType";
    public static final int PROP_ID_feedType = 5;
    
    /* 反馈内容: CONTENT VARCHAR */
    public static final String PROP_NAME_content = "content";
    public static final int PROP_ID_content = 6;
    
    /* 状态: STATUS INTEGER */
    public static final String PROP_NAME_status = "status";
    public static final int PROP_ID_status = 7;
    
    /* 是否含有图片: HAS_PICTURE BOOLEAN */
    public static final String PROP_NAME_hasPicture = "hasPicture";
    public static final int PROP_ID_hasPicture = 8;
    
    /* 图片地址列表，采用JSON数组格式: PIC_URLS VARCHAR */
    public static final String PROP_NAME_picUrls = "picUrls";
    public static final int PROP_ID_picUrls = 9;
    
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

    
    /* relation: 客户 */
    public static final String PROP_NAME_user = "user";
    
    /* component:  */
    public static final String PROP_NAME_picUrlsComponent = "picUrlsComponent";
    

    protected static final List<String> PK_PROP_NAMES = Arrays.asList(PROP_NAME_id);
    protected static final int[] PK_PROP_IDS = new int[]{PROP_ID_id};

    private static final String[] PROP_ID_TO_NAME = new String[13];
    private static final Map<String,Integer> PROP_NAME_TO_ID = new HashMap<>();
    static{
      
          PROP_ID_TO_NAME[PROP_ID_id] = PROP_NAME_id;
          PROP_NAME_TO_ID.put(PROP_NAME_id, PROP_ID_id);
      
          PROP_ID_TO_NAME[PROP_ID_userId] = PROP_NAME_userId;
          PROP_NAME_TO_ID.put(PROP_NAME_userId, PROP_ID_userId);
      
          PROP_ID_TO_NAME[PROP_ID_username] = PROP_NAME_username;
          PROP_NAME_TO_ID.put(PROP_NAME_username, PROP_ID_username);
      
          PROP_ID_TO_NAME[PROP_ID_mobile] = PROP_NAME_mobile;
          PROP_NAME_TO_ID.put(PROP_NAME_mobile, PROP_ID_mobile);
      
          PROP_ID_TO_NAME[PROP_ID_feedType] = PROP_NAME_feedType;
          PROP_NAME_TO_ID.put(PROP_NAME_feedType, PROP_ID_feedType);
      
          PROP_ID_TO_NAME[PROP_ID_content] = PROP_NAME_content;
          PROP_NAME_TO_ID.put(PROP_NAME_content, PROP_ID_content);
      
          PROP_ID_TO_NAME[PROP_ID_status] = PROP_NAME_status;
          PROP_NAME_TO_ID.put(PROP_NAME_status, PROP_ID_status);
      
          PROP_ID_TO_NAME[PROP_ID_hasPicture] = PROP_NAME_hasPicture;
          PROP_NAME_TO_ID.put(PROP_NAME_hasPicture, PROP_ID_hasPicture);
      
          PROP_ID_TO_NAME[PROP_ID_picUrls] = PROP_NAME_picUrls;
          PROP_NAME_TO_ID.put(PROP_NAME_picUrls, PROP_ID_picUrls);
      
          PROP_ID_TO_NAME[PROP_ID_addTime] = PROP_NAME_addTime;
          PROP_NAME_TO_ID.put(PROP_NAME_addTime, PROP_ID_addTime);
      
          PROP_ID_TO_NAME[PROP_ID_updateTime] = PROP_NAME_updateTime;
          PROP_NAME_TO_ID.put(PROP_NAME_updateTime, PROP_ID_updateTime);
      
          PROP_ID_TO_NAME[PROP_ID_deleted] = PROP_NAME_deleted;
          PROP_NAME_TO_ID.put(PROP_NAME_deleted, PROP_ID_deleted);
      
    }

    
    /* Id: ID */
    private java.lang.Integer _id;
    
    /* 用户ID: USER_ID */
    private java.lang.Integer _userId;
    
    /* 用户名称: USERNAME */
    private java.lang.String _username;
    
    /* 手机号: MOBILE */
    private java.lang.String _mobile;
    
    /* 反馈类型: FEED_TYPE */
    private java.lang.String _feedType;
    
    /* 反馈内容: CONTENT */
    private java.lang.String _content;
    
    /* 状态: STATUS */
    private java.lang.Integer _status;
    
    /* 是否含有图片: HAS_PICTURE */
    private java.lang.Boolean _hasPicture;
    
    /* 图片地址列表，采用JSON数组格式: PIC_URLS */
    private java.lang.String _picUrls;
    
    /* 创建时间: ADD_TIME */
    private java.time.LocalDateTime _addTime;
    
    /* 更新时间: UPDATE_TIME */
    private java.time.LocalDateTime _updateTime;
    
    /* 逻辑删除: DELETED */
    private java.lang.Boolean _deleted;
    

    public _LitemallFeedback(){
        // for debug
    }

    protected LitemallFeedback newInstance(){
        LitemallFeedback entity = new LitemallFeedback();
        entity.orm_attach(orm_enhancer());
        entity.orm_entityModel(orm_entityModel());
        return entity;
    }

    @Override
    public LitemallFeedback cloneInstance() {
        LitemallFeedback entity = newInstance();
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
      return "app.mall.dao.entity.LitemallFeedback";
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
        
            case PROP_ID_username:
               return getUsername();
        
            case PROP_ID_mobile:
               return getMobile();
        
            case PROP_ID_feedType:
               return getFeedType();
        
            case PROP_ID_content:
               return getContent();
        
            case PROP_ID_status:
               return getStatus();
        
            case PROP_ID_hasPicture:
               return getHasPicture();
        
            case PROP_ID_picUrls:
               return getPicUrls();
        
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
        
            case PROP_ID_userId:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_userId));
               }
               setUserId(typedValue);
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
        
            case PROP_ID_mobile:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_mobile));
               }
               setMobile(typedValue);
               break;
            }
        
            case PROP_ID_feedType:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_feedType));
               }
               setFeedType(typedValue);
               break;
            }
        
            case PROP_ID_content:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_content));
               }
               setContent(typedValue);
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
        
            case PROP_ID_hasPicture:{
               java.lang.Boolean typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toBoolean(value,
                       err-> newTypeConversionError(PROP_NAME_hasPicture));
               }
               setHasPicture(typedValue);
               break;
            }
        
            case PROP_ID_picUrls:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_picUrls));
               }
               setPicUrls(typedValue);
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
        
            case PROP_ID_userId:{
               onInitProp(propId);
               this._userId = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_username:{
               onInitProp(propId);
               this._username = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_mobile:{
               onInitProp(propId);
               this._mobile = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_feedType:{
               onInitProp(propId);
               this._feedType = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_content:{
               onInitProp(propId);
               this._content = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_status:{
               onInitProp(propId);
               this._status = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_hasPicture:{
               onInitProp(propId);
               this._hasPicture = (java.lang.Boolean)value;
               
               break;
            }
        
            case PROP_ID_picUrls:{
               onInitProp(propId);
               this._picUrls = (java.lang.String)value;
               
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
     * 手机号: MOBILE
     */
    public java.lang.String getMobile(){
         onPropGet(PROP_ID_mobile);
         return _mobile;
    }

    /**
     * 手机号: MOBILE
     */
    public void setMobile(java.lang.String value){
        if(onPropSet(PROP_ID_mobile,value)){
            this._mobile = value;
            internalClearRefs(PROP_ID_mobile);
            
        }
    }
    
    /**
     * 反馈类型: FEED_TYPE
     */
    public java.lang.String getFeedType(){
         onPropGet(PROP_ID_feedType);
         return _feedType;
    }

    /**
     * 反馈类型: FEED_TYPE
     */
    public void setFeedType(java.lang.String value){
        if(onPropSet(PROP_ID_feedType,value)){
            this._feedType = value;
            internalClearRefs(PROP_ID_feedType);
            
        }
    }
    
    /**
     * 反馈内容: CONTENT
     */
    public java.lang.String getContent(){
         onPropGet(PROP_ID_content);
         return _content;
    }

    /**
     * 反馈内容: CONTENT
     */
    public void setContent(java.lang.String value){
        if(onPropSet(PROP_ID_content,value)){
            this._content = value;
            internalClearRefs(PROP_ID_content);
            
        }
    }
    
    /**
     * 状态: STATUS
     */
    public java.lang.Integer getStatus(){
         onPropGet(PROP_ID_status);
         return _status;
    }

    /**
     * 状态: STATUS
     */
    public void setStatus(java.lang.Integer value){
        if(onPropSet(PROP_ID_status,value)){
            this._status = value;
            internalClearRefs(PROP_ID_status);
            
        }
    }
    
    /**
     * 是否含有图片: HAS_PICTURE
     */
    public java.lang.Boolean getHasPicture(){
         onPropGet(PROP_ID_hasPicture);
         return _hasPicture;
    }

    /**
     * 是否含有图片: HAS_PICTURE
     */
    public void setHasPicture(java.lang.Boolean value){
        if(onPropSet(PROP_ID_hasPicture,value)){
            this._hasPicture = value;
            internalClearRefs(PROP_ID_hasPicture);
            
        }
    }
    
    /**
     * 图片地址列表，采用JSON数组格式: PIC_URLS
     */
    public java.lang.String getPicUrls(){
         onPropGet(PROP_ID_picUrls);
         return _picUrls;
    }

    /**
     * 图片地址列表，采用JSON数组格式: PIC_URLS
     */
    public void setPicUrls(java.lang.String value){
        if(onPropSet(PROP_ID_picUrls,value)){
            this._picUrls = value;
            internalClearRefs(PROP_ID_picUrls);
            
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
       
   private io.nop.orm.component.OrmFileListComponent _picUrlsComponent;

   private static Map<String,Integer> COMPONENT_PROP_ID_MAP_picUrlsComponent = new HashMap<>();
   static{
      
         COMPONENT_PROP_ID_MAP_picUrlsComponent.put(io.nop.orm.component.OrmFileListComponent.PROP_NAME_filePath,PROP_ID_picUrls);
      
   }

   public io.nop.orm.component.OrmFileListComponent getPicUrlsComponent(){
      if(_picUrlsComponent == null){
          _picUrlsComponent = new io.nop.orm.component.OrmFileListComponent();
          _picUrlsComponent.bindToEntity(this, COMPONENT_PROP_ID_MAP_picUrlsComponent);
      }
      return _picUrlsComponent;
   }

}
// resume CPD analysis - CPD-ON
