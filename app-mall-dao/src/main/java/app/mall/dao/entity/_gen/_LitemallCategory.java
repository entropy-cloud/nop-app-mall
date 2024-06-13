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

import app.mall.dao.entity.LitemallCategory;

// tell cpd to start ignoring code - CPD-OFF
/**
 *  类目表: litemall_category
 */
@SuppressWarnings({"PMD.UselessOverridingMethod","PMD.UnusedLocalVariable","java:S3008","java:S1602","java:S1128","java:S1161",
        "PMD.UnnecessaryFullyQualifiedName","PMD.EmptyControlStatement","java:S116","java:S115","java:S101","java:S3776"})
public class _LitemallCategory extends DynamicOrmEntity{
    
    /* Id: ID INTEGER */
    public static final String PROP_NAME_id = "id";
    public static final int PROP_ID_id = 1;
    
    /* 类目名称: NAME VARCHAR */
    public static final String PROP_NAME_name = "name";
    public static final int PROP_ID_name = 2;
    
    /* 类目图标: ICON_URL VARCHAR */
    public static final String PROP_NAME_iconUrl = "iconUrl";
    public static final int PROP_ID_iconUrl = 3;
    
    /* 类目图片: PIC_URL VARCHAR */
    public static final String PROP_NAME_picUrl = "picUrl";
    public static final int PROP_ID_picUrl = 4;
    
    /* 类目关键字: KEYWORDS VARCHAR */
    public static final String PROP_NAME_keywords = "keywords";
    public static final int PROP_ID_keywords = 5;
    
    /* 简介: DESC VARCHAR */
    public static final String PROP_NAME_desc = "desc";
    public static final int PROP_ID_desc = 6;
    
    /* 级别: LEVEL VARCHAR */
    public static final String PROP_NAME_level = "level";
    public static final int PROP_ID_level = 7;
    
    /* 父类目ID: PID INTEGER */
    public static final String PROP_NAME_pid = "pid";
    public static final int PROP_ID_pid = 8;
    
    /* 排序: SORT_ORDER TINYINT */
    public static final String PROP_NAME_sortOrder = "sortOrder";
    public static final int PROP_ID_sortOrder = 9;
    
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

    
    /* relation: 父类目 */
    public static final String PROP_NAME_parent = "parent";
    
    /* relation: 子类目 */
    public static final String PROP_NAME_children = "children";
    
    /* component:  */
    public static final String PROP_NAME_iconUrlComponent = "iconUrlComponent";
    
    /* component:  */
    public static final String PROP_NAME_picUrlComponent = "picUrlComponent";
    

    protected static final List<String> PK_PROP_NAMES = Arrays.asList(PROP_NAME_id);
    protected static final int[] PK_PROP_IDS = new int[]{PROP_ID_id};

    private static final String[] PROP_ID_TO_NAME = new String[13];
    private static final Map<String,Integer> PROP_NAME_TO_ID = new HashMap<>();
    static{
      
          PROP_ID_TO_NAME[PROP_ID_id] = PROP_NAME_id;
          PROP_NAME_TO_ID.put(PROP_NAME_id, PROP_ID_id);
      
          PROP_ID_TO_NAME[PROP_ID_name] = PROP_NAME_name;
          PROP_NAME_TO_ID.put(PROP_NAME_name, PROP_ID_name);
      
          PROP_ID_TO_NAME[PROP_ID_iconUrl] = PROP_NAME_iconUrl;
          PROP_NAME_TO_ID.put(PROP_NAME_iconUrl, PROP_ID_iconUrl);
      
          PROP_ID_TO_NAME[PROP_ID_picUrl] = PROP_NAME_picUrl;
          PROP_NAME_TO_ID.put(PROP_NAME_picUrl, PROP_ID_picUrl);
      
          PROP_ID_TO_NAME[PROP_ID_keywords] = PROP_NAME_keywords;
          PROP_NAME_TO_ID.put(PROP_NAME_keywords, PROP_ID_keywords);
      
          PROP_ID_TO_NAME[PROP_ID_desc] = PROP_NAME_desc;
          PROP_NAME_TO_ID.put(PROP_NAME_desc, PROP_ID_desc);
      
          PROP_ID_TO_NAME[PROP_ID_level] = PROP_NAME_level;
          PROP_NAME_TO_ID.put(PROP_NAME_level, PROP_ID_level);
      
          PROP_ID_TO_NAME[PROP_ID_pid] = PROP_NAME_pid;
          PROP_NAME_TO_ID.put(PROP_NAME_pid, PROP_ID_pid);
      
          PROP_ID_TO_NAME[PROP_ID_sortOrder] = PROP_NAME_sortOrder;
          PROP_NAME_TO_ID.put(PROP_NAME_sortOrder, PROP_ID_sortOrder);
      
          PROP_ID_TO_NAME[PROP_ID_addTime] = PROP_NAME_addTime;
          PROP_NAME_TO_ID.put(PROP_NAME_addTime, PROP_ID_addTime);
      
          PROP_ID_TO_NAME[PROP_ID_updateTime] = PROP_NAME_updateTime;
          PROP_NAME_TO_ID.put(PROP_NAME_updateTime, PROP_ID_updateTime);
      
          PROP_ID_TO_NAME[PROP_ID_deleted] = PROP_NAME_deleted;
          PROP_NAME_TO_ID.put(PROP_NAME_deleted, PROP_ID_deleted);
      
    }

    
    /* Id: ID */
    private java.lang.Integer _id;
    
    /* 类目名称: NAME */
    private java.lang.String _name;
    
    /* 类目图标: ICON_URL */
    private java.lang.String _iconUrl;
    
    /* 类目图片: PIC_URL */
    private java.lang.String _picUrl;
    
    /* 类目关键字: KEYWORDS */
    private java.lang.String _keywords;
    
    /* 简介: DESC */
    private java.lang.String _desc;
    
    /* 级别: LEVEL */
    private java.lang.String _level;
    
    /* 父类目ID: PID */
    private java.lang.Integer _pid;
    
    /* 排序: SORT_ORDER */
    private java.lang.Byte _sortOrder;
    
    /* 创建时间: ADD_TIME */
    private java.time.LocalDateTime _addTime;
    
    /* 更新时间: UPDATE_TIME */
    private java.time.LocalDateTime _updateTime;
    
    /* 逻辑删除: DELETED */
    private java.lang.Boolean _deleted;
    

    public _LitemallCategory(){
        // for debug
    }

    protected LitemallCategory newInstance(){
        LitemallCategory entity = new LitemallCategory();
        entity.orm_attach(orm_enhancer());
        entity.orm_entityModel(orm_entityModel());
        return entity;
    }

    @Override
    public LitemallCategory cloneInstance() {
        LitemallCategory entity = newInstance();
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
      return "app.mall.dao.entity.LitemallCategory";
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
        
            case PROP_ID_iconUrl:
               return getIconUrl();
        
            case PROP_ID_picUrl:
               return getPicUrl();
        
            case PROP_ID_keywords:
               return getKeywords();
        
            case PROP_ID_desc:
               return getDesc();
        
            case PROP_ID_level:
               return getLevel();
        
            case PROP_ID_pid:
               return getPid();
        
            case PROP_ID_sortOrder:
               return getSortOrder();
        
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
        
            case PROP_ID_name:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_name));
               }
               setName(typedValue);
               break;
            }
        
            case PROP_ID_iconUrl:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_iconUrl));
               }
               setIconUrl(typedValue);
               break;
            }
        
            case PROP_ID_picUrl:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_picUrl));
               }
               setPicUrl(typedValue);
               break;
            }
        
            case PROP_ID_keywords:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_keywords));
               }
               setKeywords(typedValue);
               break;
            }
        
            case PROP_ID_desc:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_desc));
               }
               setDesc(typedValue);
               break;
            }
        
            case PROP_ID_level:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_level));
               }
               setLevel(typedValue);
               break;
            }
        
            case PROP_ID_pid:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_pid));
               }
               setPid(typedValue);
               break;
            }
        
            case PROP_ID_sortOrder:{
               java.lang.Byte typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toByte(value,
                       err-> newTypeConversionError(PROP_NAME_sortOrder));
               }
               setSortOrder(typedValue);
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
        
            case PROP_ID_name:{
               onInitProp(propId);
               this._name = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_iconUrl:{
               onInitProp(propId);
               this._iconUrl = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_picUrl:{
               onInitProp(propId);
               this._picUrl = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_keywords:{
               onInitProp(propId);
               this._keywords = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_desc:{
               onInitProp(propId);
               this._desc = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_level:{
               onInitProp(propId);
               this._level = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_pid:{
               onInitProp(propId);
               this._pid = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_sortOrder:{
               onInitProp(propId);
               this._sortOrder = (java.lang.Byte)value;
               
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
     * 类目名称: NAME
     */
    public java.lang.String getName(){
         onPropGet(PROP_ID_name);
         return _name;
    }

    /**
     * 类目名称: NAME
     */
    public void setName(java.lang.String value){
        if(onPropSet(PROP_ID_name,value)){
            this._name = value;
            internalClearRefs(PROP_ID_name);
            
        }
    }
    
    /**
     * 类目图标: ICON_URL
     */
    public java.lang.String getIconUrl(){
         onPropGet(PROP_ID_iconUrl);
         return _iconUrl;
    }

    /**
     * 类目图标: ICON_URL
     */
    public void setIconUrl(java.lang.String value){
        if(onPropSet(PROP_ID_iconUrl,value)){
            this._iconUrl = value;
            internalClearRefs(PROP_ID_iconUrl);
            
        }
    }
    
    /**
     * 类目图片: PIC_URL
     */
    public java.lang.String getPicUrl(){
         onPropGet(PROP_ID_picUrl);
         return _picUrl;
    }

    /**
     * 类目图片: PIC_URL
     */
    public void setPicUrl(java.lang.String value){
        if(onPropSet(PROP_ID_picUrl,value)){
            this._picUrl = value;
            internalClearRefs(PROP_ID_picUrl);
            
        }
    }
    
    /**
     * 类目关键字: KEYWORDS
     */
    public java.lang.String getKeywords(){
         onPropGet(PROP_ID_keywords);
         return _keywords;
    }

    /**
     * 类目关键字: KEYWORDS
     */
    public void setKeywords(java.lang.String value){
        if(onPropSet(PROP_ID_keywords,value)){
            this._keywords = value;
            internalClearRefs(PROP_ID_keywords);
            
        }
    }
    
    /**
     * 简介: DESC
     */
    public java.lang.String getDesc(){
         onPropGet(PROP_ID_desc);
         return _desc;
    }

    /**
     * 简介: DESC
     */
    public void setDesc(java.lang.String value){
        if(onPropSet(PROP_ID_desc,value)){
            this._desc = value;
            internalClearRefs(PROP_ID_desc);
            
        }
    }
    
    /**
     * 级别: LEVEL
     */
    public java.lang.String getLevel(){
         onPropGet(PROP_ID_level);
         return _level;
    }

    /**
     * 级别: LEVEL
     */
    public void setLevel(java.lang.String value){
        if(onPropSet(PROP_ID_level,value)){
            this._level = value;
            internalClearRefs(PROP_ID_level);
            
        }
    }
    
    /**
     * 父类目ID: PID
     */
    public java.lang.Integer getPid(){
         onPropGet(PROP_ID_pid);
         return _pid;
    }

    /**
     * 父类目ID: PID
     */
    public void setPid(java.lang.Integer value){
        if(onPropSet(PROP_ID_pid,value)){
            this._pid = value;
            internalClearRefs(PROP_ID_pid);
            
        }
    }
    
    /**
     * 排序: SORT_ORDER
     */
    public java.lang.Byte getSortOrder(){
         onPropGet(PROP_ID_sortOrder);
         return _sortOrder;
    }

    /**
     * 排序: SORT_ORDER
     */
    public void setSortOrder(java.lang.Byte value){
        if(onPropSet(PROP_ID_sortOrder,value)){
            this._sortOrder = value;
            internalClearRefs(PROP_ID_sortOrder);
            
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
     * 父类目
     */
    public app.mall.dao.entity.LitemallCategory getParent(){
       return (app.mall.dao.entity.LitemallCategory)internalGetRefEntity(PROP_NAME_parent);
    }

    public void setParent(app.mall.dao.entity.LitemallCategory refEntity){
   
           if(refEntity == null){
           
                   this.setPid(null);
               
           }else{
           internalSetRefEntity(PROP_NAME_parent, refEntity,()->{
           
                           this.setPid(refEntity.getId());
                       
           });
           }
       
    }
       
    private final OrmEntitySet<app.mall.dao.entity.LitemallCategory> _children = new OrmEntitySet<>(this, PROP_NAME_children,
        app.mall.dao.entity.LitemallCategory.PROP_NAME_parent, null,app.mall.dao.entity.LitemallCategory.class);

    /**
     * 子类目。 refPropName: parent, keyProp: {rel.keyProp}
     */
    public IOrmEntitySet<app.mall.dao.entity.LitemallCategory> getChildren(){
       return _children;
    }
       
   private io.nop.orm.component.OrmFileComponent _iconUrlComponent;

   private static Map<String,Integer> COMPONENT_PROP_ID_MAP_iconUrlComponent = new HashMap<>();
   static{
      
         COMPONENT_PROP_ID_MAP_iconUrlComponent.put(io.nop.orm.component.OrmFileComponent.PROP_NAME_filePath,PROP_ID_iconUrl);
      
   }

   public io.nop.orm.component.OrmFileComponent getIconUrlComponent(){
      if(_iconUrlComponent == null){
          _iconUrlComponent = new io.nop.orm.component.OrmFileComponent();
          _iconUrlComponent.bindToEntity(this, COMPONENT_PROP_ID_MAP_iconUrlComponent);
      }
      return _iconUrlComponent;
   }

   private io.nop.orm.component.OrmFileComponent _picUrlComponent;

   private static Map<String,Integer> COMPONENT_PROP_ID_MAP_picUrlComponent = new HashMap<>();
   static{
      
         COMPONENT_PROP_ID_MAP_picUrlComponent.put(io.nop.orm.component.OrmFileComponent.PROP_NAME_filePath,PROP_ID_picUrl);
      
   }

   public io.nop.orm.component.OrmFileComponent getPicUrlComponent(){
      if(_picUrlComponent == null){
          _picUrlComponent = new io.nop.orm.component.OrmFileComponent();
          _picUrlComponent.bindToEntity(this, COMPONENT_PROP_ID_MAP_picUrlComponent);
      }
      return _picUrlComponent;
   }

}
// resume CPD analysis - CPD-ON
