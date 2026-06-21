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

import app.mall.dao.entity.LitemallMaterial;

// tell cpd to start ignoring code - CPD-OFF
/**
 *  素材资源表: litemall_material
 */
@SuppressWarnings({"PMD.UselessOverridingMethod","PMD.UnusedLocalVariable","java:S3008","java:S1602","java:S1128","java:S1161",
        "PMD.UnnecessaryFullyQualifiedName","PMD.EmptyControlStatement","java:S116","java:S115","java:S101","java:S3776"})
public class _LitemallMaterial extends DynamicOrmEntity{
    
    /* Id: ID INTEGER */
    public static final String PROP_NAME_id = "id";
    public static final int PROP_ID_id = 1;
    
    /* 文件名: NAME VARCHAR */
    public static final String PROP_NAME_name = "name";
    public static final int PROP_ID_name = 2;
    
    /* 访问链接: URL VARCHAR */
    public static final String PROP_NAME_url = "url";
    public static final int PROP_ID_url = 3;
    
    /* 文件类型(image/video): FILE_TYPE VARCHAR */
    public static final String PROP_NAME_fileType = "fileType";
    public static final int PROP_ID_fileType = 4;
    
    /* 文件大小(bytes): FILE_SIZE INTEGER */
    public static final String PROP_NAME_fileSize = "fileSize";
    public static final int PROP_ID_fileSize = 5;
    
    /* 分类ID: CATEGORY_ID INTEGER */
    public static final String PROP_NAME_categoryId = "categoryId";
    public static final int PROP_ID_categoryId = 6;
    
    /* 标签: TAG VARCHAR */
    public static final String PROP_NAME_tag = "tag";
    public static final int PROP_ID_tag = 7;
    
    /* 创建时间: ADD_TIME DATETIME */
    public static final String PROP_NAME_addTime = "addTime";
    public static final int PROP_ID_addTime = 8;
    
    /* 更新时间: UPDATE_TIME DATETIME */
    public static final String PROP_NAME_updateTime = "updateTime";
    public static final int PROP_ID_updateTime = 9;
    
    /* 逻辑删除: DELETED BOOLEAN */
    public static final String PROP_NAME_deleted = "deleted";
    public static final int PROP_ID_deleted = 10;
    

    private static int _PROP_ID_BOUND = 11;

    
    /* relation: 分类 */
    public static final String PROP_NAME_category = "category";
    

    protected static final List<String> PK_PROP_NAMES = Arrays.asList(PROP_NAME_id);
    protected static final int[] PK_PROP_IDS = new int[]{PROP_ID_id};

    private static final String[] PROP_ID_TO_NAME = new String[11];
    private static final Map<String,Integer> PROP_NAME_TO_ID = new HashMap<>();
    static{
      
          PROP_ID_TO_NAME[PROP_ID_id] = PROP_NAME_id;
          PROP_NAME_TO_ID.put(PROP_NAME_id, PROP_ID_id);
      
          PROP_ID_TO_NAME[PROP_ID_name] = PROP_NAME_name;
          PROP_NAME_TO_ID.put(PROP_NAME_name, PROP_ID_name);
      
          PROP_ID_TO_NAME[PROP_ID_url] = PROP_NAME_url;
          PROP_NAME_TO_ID.put(PROP_NAME_url, PROP_ID_url);
      
          PROP_ID_TO_NAME[PROP_ID_fileType] = PROP_NAME_fileType;
          PROP_NAME_TO_ID.put(PROP_NAME_fileType, PROP_ID_fileType);
      
          PROP_ID_TO_NAME[PROP_ID_fileSize] = PROP_NAME_fileSize;
          PROP_NAME_TO_ID.put(PROP_NAME_fileSize, PROP_ID_fileSize);
      
          PROP_ID_TO_NAME[PROP_ID_categoryId] = PROP_NAME_categoryId;
          PROP_NAME_TO_ID.put(PROP_NAME_categoryId, PROP_ID_categoryId);
      
          PROP_ID_TO_NAME[PROP_ID_tag] = PROP_NAME_tag;
          PROP_NAME_TO_ID.put(PROP_NAME_tag, PROP_ID_tag);
      
          PROP_ID_TO_NAME[PROP_ID_addTime] = PROP_NAME_addTime;
          PROP_NAME_TO_ID.put(PROP_NAME_addTime, PROP_ID_addTime);
      
          PROP_ID_TO_NAME[PROP_ID_updateTime] = PROP_NAME_updateTime;
          PROP_NAME_TO_ID.put(PROP_NAME_updateTime, PROP_ID_updateTime);
      
          PROP_ID_TO_NAME[PROP_ID_deleted] = PROP_NAME_deleted;
          PROP_NAME_TO_ID.put(PROP_NAME_deleted, PROP_ID_deleted);
      
    }

    
    /* Id: ID */
    private java.lang.String _id;
    
    /* 文件名: NAME */
    private java.lang.String _name;
    
    /* 访问链接: URL */
    private java.lang.String _url;
    
    /* 文件类型(image/video): FILE_TYPE */
    private java.lang.String _fileType;
    
    /* 文件大小(bytes): FILE_SIZE */
    private java.lang.Integer _fileSize;
    
    /* 分类ID: CATEGORY_ID */
    private java.lang.String _categoryId;
    
    /* 标签: TAG */
    private java.lang.String _tag;
    
    /* 创建时间: ADD_TIME */
    private java.time.LocalDateTime _addTime;
    
    /* 更新时间: UPDATE_TIME */
    private java.time.LocalDateTime _updateTime;
    
    /* 逻辑删除: DELETED */
    private java.lang.Boolean _deleted;
    

    public _LitemallMaterial(){
        // for debug
    }

    protected LitemallMaterial newInstance(){
        LitemallMaterial entity = new LitemallMaterial();
        entity.orm_attach(orm_enhancer());
        entity.orm_entityModel(orm_entityModel());
        return entity;
    }

    @Override
    public LitemallMaterial cloneInstance() {
        LitemallMaterial entity = newInstance();
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
      return "app.mall.dao.entity.LitemallMaterial";
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
        
            case PROP_ID_name:
               return getName();
        
            case PROP_ID_url:
               return getUrl();
        
            case PROP_ID_fileType:
               return getFileType();
        
            case PROP_ID_fileSize:
               return getFileSize();
        
            case PROP_ID_categoryId:
               return getCategoryId();
        
            case PROP_ID_tag:
               return getTag();
        
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
        
            case PROP_ID_name:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_name));
               }
               setName(typedValue);
               break;
            }
        
            case PROP_ID_url:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_url));
               }
               setUrl(typedValue);
               break;
            }
        
            case PROP_ID_fileType:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_fileType));
               }
               setFileType(typedValue);
               break;
            }
        
            case PROP_ID_fileSize:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_fileSize));
               }
               setFileSize(typedValue);
               break;
            }
        
            case PROP_ID_categoryId:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_categoryId));
               }
               setCategoryId(typedValue);
               break;
            }
        
            case PROP_ID_tag:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_tag));
               }
               setTag(typedValue);
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
        
            case PROP_ID_name:{
               onInitProp(propId);
               this._name = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_url:{
               onInitProp(propId);
               this._url = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_fileType:{
               onInitProp(propId);
               this._fileType = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_fileSize:{
               onInitProp(propId);
               this._fileSize = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_categoryId:{
               onInitProp(propId);
               this._categoryId = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_tag:{
               onInitProp(propId);
               this._tag = (java.lang.String)value;
               
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
     * 文件名: NAME
     */
    public final java.lang.String getName(){
         onPropGet(PROP_ID_name);
         return _name;
    }

    /**
     * 文件名: NAME
     */
    public final void setName(java.lang.String value){
        if(onPropSet(PROP_ID_name,value)){
            this._name = value;
            internalClearRefs(PROP_ID_name);
            
        }
    }
    
    /**
     * 访问链接: URL
     */
    public final java.lang.String getUrl(){
         onPropGet(PROP_ID_url);
         return _url;
    }

    /**
     * 访问链接: URL
     */
    public final void setUrl(java.lang.String value){
        if(onPropSet(PROP_ID_url,value)){
            this._url = value;
            internalClearRefs(PROP_ID_url);
            
        }
    }
    
    /**
     * 文件类型(image/video): FILE_TYPE
     */
    public final java.lang.String getFileType(){
         onPropGet(PROP_ID_fileType);
         return _fileType;
    }

    /**
     * 文件类型(image/video): FILE_TYPE
     */
    public final void setFileType(java.lang.String value){
        if(onPropSet(PROP_ID_fileType,value)){
            this._fileType = value;
            internalClearRefs(PROP_ID_fileType);
            
        }
    }
    
    /**
     * 文件大小(bytes): FILE_SIZE
     */
    public final java.lang.Integer getFileSize(){
         onPropGet(PROP_ID_fileSize);
         return _fileSize;
    }

    /**
     * 文件大小(bytes): FILE_SIZE
     */
    public final void setFileSize(java.lang.Integer value){
        if(onPropSet(PROP_ID_fileSize,value)){
            this._fileSize = value;
            internalClearRefs(PROP_ID_fileSize);
            
        }
    }
    
    /**
     * 分类ID: CATEGORY_ID
     */
    public final java.lang.String getCategoryId(){
         onPropGet(PROP_ID_categoryId);
         return _categoryId;
    }

    /**
     * 分类ID: CATEGORY_ID
     */
    public final void setCategoryId(java.lang.String value){
        if(onPropSet(PROP_ID_categoryId,value)){
            this._categoryId = value;
            internalClearRefs(PROP_ID_categoryId);
            
        }
    }
    
    /**
     * 标签: TAG
     */
    public final java.lang.String getTag(){
         onPropGet(PROP_ID_tag);
         return _tag;
    }

    /**
     * 标签: TAG
     */
    public final void setTag(java.lang.String value){
        if(onPropSet(PROP_ID_tag,value)){
            this._tag = value;
            internalClearRefs(PROP_ID_tag);
            
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
     * 分类
     */
    public final app.mall.dao.entity.LitemallMaterialCategory getCategory(){
       return (app.mall.dao.entity.LitemallMaterialCategory)internalGetRefEntity(PROP_NAME_category);
    }

    public final void setCategory(app.mall.dao.entity.LitemallMaterialCategory refEntity){
   
           if(refEntity == null){
           
                   this.setCategoryId(null);
               
           }else{
           internalSetRefEntity(PROP_NAME_category, refEntity,()->{
           
                           this.setCategoryId(refEntity.getId());
                       
           });
           }
       
    }
       
}
// resume CPD analysis - CPD-ON
