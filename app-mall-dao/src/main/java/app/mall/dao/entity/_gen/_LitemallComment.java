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

import app.mall.dao.entity.LitemallComment;

// tell cpd to start ignoring code - CPD-OFF
/**
 *  评论表: litemall_comment
 */
@SuppressWarnings({"PMD.UselessOverridingMethod","PMD.UnusedLocalVariable","java:S3008","java:S1602","java:S1128","java:S1161",
        "PMD.UnnecessaryFullyQualifiedName","PMD.EmptyControlStatement","java:S116","java:S115","java:S101","java:S3776"})
public class _LitemallComment extends DynamicOrmEntity{
    
    /* Id: ID INTEGER */
    public static final String PROP_NAME_id = "id";
    public static final int PROP_ID_id = 1;
    
    /* 如果type=0，则是商品评论；如果是type=1，则是专题评论。: VALUE_ID INTEGER */
    public static final String PROP_NAME_valueId = "valueId";
    public static final int PROP_ID_valueId = 2;
    
    /* 评论类型: TYPE TINYINT */
    public static final String PROP_NAME_type = "type";
    public static final int PROP_ID_type = 3;
    
    /* 评论内容: CONTENT VARCHAR */
    public static final String PROP_NAME_content = "content";
    public static final int PROP_ID_content = 4;
    
    /* 管理员回复内容: ADMIN_CONTENT VARCHAR */
    public static final String PROP_NAME_adminContent = "adminContent";
    public static final int PROP_ID_adminContent = 5;
    
    /* 用户ID: USER_ID INTEGER */
    public static final String PROP_NAME_userId = "userId";
    public static final int PROP_ID_userId = 6;
    
    /* 是否含有图片: HAS_PICTURE BOOLEAN */
    public static final String PROP_NAME_hasPicture = "hasPicture";
    public static final int PROP_ID_hasPicture = 7;
    
    /* 图片地址列表，采用JSON数组格式: PIC_URLS VARCHAR */
    public static final String PROP_NAME_picUrls = "picUrls";
    public static final int PROP_ID_picUrls = 8;
    
    /* 评分， 1-5: STAR SMALLINT */
    public static final String PROP_NAME_star = "star";
    public static final int PROP_ID_star = 9;
    
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
      
          PROP_ID_TO_NAME[PROP_ID_valueId] = PROP_NAME_valueId;
          PROP_NAME_TO_ID.put(PROP_NAME_valueId, PROP_ID_valueId);
      
          PROP_ID_TO_NAME[PROP_ID_type] = PROP_NAME_type;
          PROP_NAME_TO_ID.put(PROP_NAME_type, PROP_ID_type);
      
          PROP_ID_TO_NAME[PROP_ID_content] = PROP_NAME_content;
          PROP_NAME_TO_ID.put(PROP_NAME_content, PROP_ID_content);
      
          PROP_ID_TO_NAME[PROP_ID_adminContent] = PROP_NAME_adminContent;
          PROP_NAME_TO_ID.put(PROP_NAME_adminContent, PROP_ID_adminContent);
      
          PROP_ID_TO_NAME[PROP_ID_userId] = PROP_NAME_userId;
          PROP_NAME_TO_ID.put(PROP_NAME_userId, PROP_ID_userId);
      
          PROP_ID_TO_NAME[PROP_ID_hasPicture] = PROP_NAME_hasPicture;
          PROP_NAME_TO_ID.put(PROP_NAME_hasPicture, PROP_ID_hasPicture);
      
          PROP_ID_TO_NAME[PROP_ID_picUrls] = PROP_NAME_picUrls;
          PROP_NAME_TO_ID.put(PROP_NAME_picUrls, PROP_ID_picUrls);
      
          PROP_ID_TO_NAME[PROP_ID_star] = PROP_NAME_star;
          PROP_NAME_TO_ID.put(PROP_NAME_star, PROP_ID_star);
      
          PROP_ID_TO_NAME[PROP_ID_addTime] = PROP_NAME_addTime;
          PROP_NAME_TO_ID.put(PROP_NAME_addTime, PROP_ID_addTime);
      
          PROP_ID_TO_NAME[PROP_ID_updateTime] = PROP_NAME_updateTime;
          PROP_NAME_TO_ID.put(PROP_NAME_updateTime, PROP_ID_updateTime);
      
          PROP_ID_TO_NAME[PROP_ID_deleted] = PROP_NAME_deleted;
          PROP_NAME_TO_ID.put(PROP_NAME_deleted, PROP_ID_deleted);
      
    }

    
    /* Id: ID */
    private java.lang.Integer _id;
    
    /* 如果type=0，则是商品评论；如果是type=1，则是专题评论。: VALUE_ID */
    private java.lang.Integer _valueId;
    
    /* 评论类型: TYPE */
    private java.lang.Byte _type;
    
    /* 评论内容: CONTENT */
    private java.lang.String _content;
    
    /* 管理员回复内容: ADMIN_CONTENT */
    private java.lang.String _adminContent;
    
    /* 用户ID: USER_ID */
    private java.lang.Integer _userId;
    
    /* 是否含有图片: HAS_PICTURE */
    private java.lang.Boolean _hasPicture;
    
    /* 图片地址列表，采用JSON数组格式: PIC_URLS */
    private java.lang.String _picUrls;
    
    /* 评分， 1-5: STAR */
    private java.lang.Short _star;
    
    /* 创建时间: ADD_TIME */
    private java.time.LocalDateTime _addTime;
    
    /* 更新时间: UPDATE_TIME */
    private java.time.LocalDateTime _updateTime;
    
    /* 逻辑删除: DELETED */
    private java.lang.Boolean _deleted;
    

    public _LitemallComment(){
        // for debug
    }

    protected LitemallComment newInstance(){
        LitemallComment entity = new LitemallComment();
        entity.orm_attach(orm_enhancer());
        entity.orm_entityModel(orm_entityModel());
        return entity;
    }

    @Override
    public LitemallComment cloneInstance() {
        LitemallComment entity = newInstance();
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
      return "app.mall.dao.entity.LitemallComment";
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
        
            case PROP_ID_valueId:
               return getValueId();
        
            case PROP_ID_type:
               return getType();
        
            case PROP_ID_content:
               return getContent();
        
            case PROP_ID_adminContent:
               return getAdminContent();
        
            case PROP_ID_userId:
               return getUserId();
        
            case PROP_ID_hasPicture:
               return getHasPicture();
        
            case PROP_ID_picUrls:
               return getPicUrls();
        
            case PROP_ID_star:
               return getStar();
        
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
        
            case PROP_ID_valueId:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_valueId));
               }
               setValueId(typedValue);
               break;
            }
        
            case PROP_ID_type:{
               java.lang.Byte typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toByte(value,
                       err-> newTypeConversionError(PROP_NAME_type));
               }
               setType(typedValue);
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
        
            case PROP_ID_adminContent:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_adminContent));
               }
               setAdminContent(typedValue);
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
        
            case PROP_ID_star:{
               java.lang.Short typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toShort(value,
                       err-> newTypeConversionError(PROP_NAME_star));
               }
               setStar(typedValue);
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
        
            case PROP_ID_valueId:{
               onInitProp(propId);
               this._valueId = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_type:{
               onInitProp(propId);
               this._type = (java.lang.Byte)value;
               
               break;
            }
        
            case PROP_ID_content:{
               onInitProp(propId);
               this._content = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_adminContent:{
               onInitProp(propId);
               this._adminContent = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_userId:{
               onInitProp(propId);
               this._userId = (java.lang.Integer)value;
               
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
        
            case PROP_ID_star:{
               onInitProp(propId);
               this._star = (java.lang.Short)value;
               
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
     * 如果type=0，则是商品评论；如果是type=1，则是专题评论。: VALUE_ID
     */
    public java.lang.Integer getValueId(){
         onPropGet(PROP_ID_valueId);
         return _valueId;
    }

    /**
     * 如果type=0，则是商品评论；如果是type=1，则是专题评论。: VALUE_ID
     */
    public void setValueId(java.lang.Integer value){
        if(onPropSet(PROP_ID_valueId,value)){
            this._valueId = value;
            internalClearRefs(PROP_ID_valueId);
            
        }
    }
    
    /**
     * 评论类型: TYPE
     */
    public java.lang.Byte getType(){
         onPropGet(PROP_ID_type);
         return _type;
    }

    /**
     * 评论类型: TYPE
     */
    public void setType(java.lang.Byte value){
        if(onPropSet(PROP_ID_type,value)){
            this._type = value;
            internalClearRefs(PROP_ID_type);
            
        }
    }
    
    /**
     * 评论内容: CONTENT
     */
    public java.lang.String getContent(){
         onPropGet(PROP_ID_content);
         return _content;
    }

    /**
     * 评论内容: CONTENT
     */
    public void setContent(java.lang.String value){
        if(onPropSet(PROP_ID_content,value)){
            this._content = value;
            internalClearRefs(PROP_ID_content);
            
        }
    }
    
    /**
     * 管理员回复内容: ADMIN_CONTENT
     */
    public java.lang.String getAdminContent(){
         onPropGet(PROP_ID_adminContent);
         return _adminContent;
    }

    /**
     * 管理员回复内容: ADMIN_CONTENT
     */
    public void setAdminContent(java.lang.String value){
        if(onPropSet(PROP_ID_adminContent,value)){
            this._adminContent = value;
            internalClearRefs(PROP_ID_adminContent);
            
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
     * 评分， 1-5: STAR
     */
    public java.lang.Short getStar(){
         onPropGet(PROP_ID_star);
         return _star;
    }

    /**
     * 评分， 1-5: STAR
     */
    public void setStar(java.lang.Short value){
        if(onPropSet(PROP_ID_star,value)){
            this._star = value;
            internalClearRefs(PROP_ID_star);
            
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
