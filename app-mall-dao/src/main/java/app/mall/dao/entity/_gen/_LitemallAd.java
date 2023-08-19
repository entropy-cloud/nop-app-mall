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

import app.mall.dao.entity.LitemallAd;

// tell cpd to start ignoring code - CPD-OFF
/**
 *  广告表: litemall_ad
 */
@SuppressWarnings({"PMD.UselessOverridingMethod","PMD.UnusedLocalVariable",
        "PMD.UnnecessaryFullyQualifiedName","PMD.EmptyControlStatement"})
public class _LitemallAd extends DynamicOrmEntity{
    
    /* Id: ID INTEGER */
    public static final String PROP_NAME_id = "id";
    public static final int PROP_ID_id = 1;
    
    /* 广告标题: NAME VARCHAR */
    public static final String PROP_NAME_name = "name";
    public static final int PROP_ID_name = 2;
    
    /* 所广告的商品页面或者活动页面链接地址: LINK VARCHAR */
    public static final String PROP_NAME_link = "link";
    public static final int PROP_ID_link = 3;
    
    /* 广告宣传图片: URL VARCHAR */
    public static final String PROP_NAME_url = "url";
    public static final int PROP_ID_url = 4;
    
    /* 广告位置：1则是首页: POSITION TINYINT */
    public static final String PROP_NAME_position = "position";
    public static final int PROP_ID_position = 5;
    
    /* 活动内容: CONTENT VARCHAR */
    public static final String PROP_NAME_content = "content";
    public static final int PROP_ID_content = 6;
    
    /* 广告开始时间: START_TIME DATETIME */
    public static final String PROP_NAME_startTime = "startTime";
    public static final int PROP_ID_startTime = 7;
    
    /* 广告结束时间: END_TIME DATETIME */
    public static final String PROP_NAME_endTime = "endTime";
    public static final int PROP_ID_endTime = 8;
    
    /* 是否启动: ENABLED BOOLEAN */
    public static final String PROP_NAME_enabled = "enabled";
    public static final int PROP_ID_enabled = 9;
    
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

    
    /* component:  */
    public static final String PROP_NAME_urlComponent = "urlComponent";
    

    public static final List<String> PK_PROP_NAMES = Arrays.asList(PROP_NAME_id);
    public static final int[] PK_PROP_IDS = new int[]{PROP_ID_id};

    private static final String[] PROP_ID_TO_NAME = new String[13];
    private static final Map<String,Integer> PROP_NAME_TO_ID = new HashMap<>();
    static{
      
          PROP_ID_TO_NAME[PROP_ID_id] = PROP_NAME_id;
          PROP_NAME_TO_ID.put(PROP_NAME_id, PROP_ID_id);
      
          PROP_ID_TO_NAME[PROP_ID_name] = PROP_NAME_name;
          PROP_NAME_TO_ID.put(PROP_NAME_name, PROP_ID_name);
      
          PROP_ID_TO_NAME[PROP_ID_link] = PROP_NAME_link;
          PROP_NAME_TO_ID.put(PROP_NAME_link, PROP_ID_link);
      
          PROP_ID_TO_NAME[PROP_ID_url] = PROP_NAME_url;
          PROP_NAME_TO_ID.put(PROP_NAME_url, PROP_ID_url);
      
          PROP_ID_TO_NAME[PROP_ID_position] = PROP_NAME_position;
          PROP_NAME_TO_ID.put(PROP_NAME_position, PROP_ID_position);
      
          PROP_ID_TO_NAME[PROP_ID_content] = PROP_NAME_content;
          PROP_NAME_TO_ID.put(PROP_NAME_content, PROP_ID_content);
      
          PROP_ID_TO_NAME[PROP_ID_startTime] = PROP_NAME_startTime;
          PROP_NAME_TO_ID.put(PROP_NAME_startTime, PROP_ID_startTime);
      
          PROP_ID_TO_NAME[PROP_ID_endTime] = PROP_NAME_endTime;
          PROP_NAME_TO_ID.put(PROP_NAME_endTime, PROP_ID_endTime);
      
          PROP_ID_TO_NAME[PROP_ID_enabled] = PROP_NAME_enabled;
          PROP_NAME_TO_ID.put(PROP_NAME_enabled, PROP_ID_enabled);
      
          PROP_ID_TO_NAME[PROP_ID_addTime] = PROP_NAME_addTime;
          PROP_NAME_TO_ID.put(PROP_NAME_addTime, PROP_ID_addTime);
      
          PROP_ID_TO_NAME[PROP_ID_updateTime] = PROP_NAME_updateTime;
          PROP_NAME_TO_ID.put(PROP_NAME_updateTime, PROP_ID_updateTime);
      
          PROP_ID_TO_NAME[PROP_ID_deleted] = PROP_NAME_deleted;
          PROP_NAME_TO_ID.put(PROP_NAME_deleted, PROP_ID_deleted);
      
    }

    
    /* Id: ID */
    private java.lang.Integer _id;
    
    /* 广告标题: NAME */
    private java.lang.String _name;
    
    /* 所广告的商品页面或者活动页面链接地址: LINK */
    private java.lang.String _link;
    
    /* 广告宣传图片: URL */
    private java.lang.String _url;
    
    /* 广告位置：1则是首页: POSITION */
    private java.lang.Byte _position;
    
    /* 活动内容: CONTENT */
    private java.lang.String _content;
    
    /* 广告开始时间: START_TIME */
    private java.time.LocalDateTime _startTime;
    
    /* 广告结束时间: END_TIME */
    private java.time.LocalDateTime _endTime;
    
    /* 是否启动: ENABLED */
    private java.lang.Boolean _enabled;
    
    /* 创建时间: ADD_TIME */
    private java.time.LocalDateTime _addTime;
    
    /* 更新时间: UPDATE_TIME */
    private java.time.LocalDateTime _updateTime;
    
    /* 逻辑删除: DELETED */
    private java.lang.Boolean _deleted;
    

    public _LitemallAd(){
    }

    protected LitemallAd newInstance(){
       return new LitemallAd();
    }

    @Override
    public LitemallAd cloneInstance() {
        LitemallAd entity = newInstance();
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
      return "app.mall.dao.entity.LitemallAd";
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
        
            case PROP_ID_link:
               return getLink();
        
            case PROP_ID_url:
               return getUrl();
        
            case PROP_ID_position:
               return getPosition();
        
            case PROP_ID_content:
               return getContent();
        
            case PROP_ID_startTime:
               return getStartTime();
        
            case PROP_ID_endTime:
               return getEndTime();
        
            case PROP_ID_enabled:
               return getEnabled();
        
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
        
            case PROP_ID_link:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_link));
               }
               setLink(typedValue);
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
        
            case PROP_ID_position:{
               java.lang.Byte typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toByte(value,
                       err-> newTypeConversionError(PROP_NAME_position));
               }
               setPosition(typedValue);
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
        
            case PROP_ID_startTime:{
               java.time.LocalDateTime typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toLocalDateTime(value,
                       err-> newTypeConversionError(PROP_NAME_startTime));
               }
               setStartTime(typedValue);
               break;
            }
        
            case PROP_ID_endTime:{
               java.time.LocalDateTime typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toLocalDateTime(value,
                       err-> newTypeConversionError(PROP_NAME_endTime));
               }
               setEndTime(typedValue);
               break;
            }
        
            case PROP_ID_enabled:{
               java.lang.Boolean typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toBoolean(value,
                       err-> newTypeConversionError(PROP_NAME_enabled));
               }
               setEnabled(typedValue);
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
        
            case PROP_ID_link:{
               onInitProp(propId);
               this._link = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_url:{
               onInitProp(propId);
               this._url = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_position:{
               onInitProp(propId);
               this._position = (java.lang.Byte)value;
               
               break;
            }
        
            case PROP_ID_content:{
               onInitProp(propId);
               this._content = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_startTime:{
               onInitProp(propId);
               this._startTime = (java.time.LocalDateTime)value;
               
               break;
            }
        
            case PROP_ID_endTime:{
               onInitProp(propId);
               this._endTime = (java.time.LocalDateTime)value;
               
               break;
            }
        
            case PROP_ID_enabled:{
               onInitProp(propId);
               this._enabled = (java.lang.Boolean)value;
               
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
     * 广告标题: NAME
     */
    public java.lang.String getName(){
         onPropGet(PROP_ID_name);
         return _name;
    }

    /**
     * 广告标题: NAME
     */
    public void setName(java.lang.String value){
        if(onPropSet(PROP_ID_name,value)){
            this._name = value;
            internalClearRefs(PROP_ID_name);
            
        }
    }
    
    /**
     * 所广告的商品页面或者活动页面链接地址: LINK
     */
    public java.lang.String getLink(){
         onPropGet(PROP_ID_link);
         return _link;
    }

    /**
     * 所广告的商品页面或者活动页面链接地址: LINK
     */
    public void setLink(java.lang.String value){
        if(onPropSet(PROP_ID_link,value)){
            this._link = value;
            internalClearRefs(PROP_ID_link);
            
        }
    }
    
    /**
     * 广告宣传图片: URL
     */
    public java.lang.String getUrl(){
         onPropGet(PROP_ID_url);
         return _url;
    }

    /**
     * 广告宣传图片: URL
     */
    public void setUrl(java.lang.String value){
        if(onPropSet(PROP_ID_url,value)){
            this._url = value;
            internalClearRefs(PROP_ID_url);
            
        }
    }
    
    /**
     * 广告位置：1则是首页: POSITION
     */
    public java.lang.Byte getPosition(){
         onPropGet(PROP_ID_position);
         return _position;
    }

    /**
     * 广告位置：1则是首页: POSITION
     */
    public void setPosition(java.lang.Byte value){
        if(onPropSet(PROP_ID_position,value)){
            this._position = value;
            internalClearRefs(PROP_ID_position);
            
        }
    }
    
    /**
     * 活动内容: CONTENT
     */
    public java.lang.String getContent(){
         onPropGet(PROP_ID_content);
         return _content;
    }

    /**
     * 活动内容: CONTENT
     */
    public void setContent(java.lang.String value){
        if(onPropSet(PROP_ID_content,value)){
            this._content = value;
            internalClearRefs(PROP_ID_content);
            
        }
    }
    
    /**
     * 广告开始时间: START_TIME
     */
    public java.time.LocalDateTime getStartTime(){
         onPropGet(PROP_ID_startTime);
         return _startTime;
    }

    /**
     * 广告开始时间: START_TIME
     */
    public void setStartTime(java.time.LocalDateTime value){
        if(onPropSet(PROP_ID_startTime,value)){
            this._startTime = value;
            internalClearRefs(PROP_ID_startTime);
            
        }
    }
    
    /**
     * 广告结束时间: END_TIME
     */
    public java.time.LocalDateTime getEndTime(){
         onPropGet(PROP_ID_endTime);
         return _endTime;
    }

    /**
     * 广告结束时间: END_TIME
     */
    public void setEndTime(java.time.LocalDateTime value){
        if(onPropSet(PROP_ID_endTime,value)){
            this._endTime = value;
            internalClearRefs(PROP_ID_endTime);
            
        }
    }
    
    /**
     * 是否启动: ENABLED
     */
    public java.lang.Boolean getEnabled(){
         onPropGet(PROP_ID_enabled);
         return _enabled;
    }

    /**
     * 是否启动: ENABLED
     */
    public void setEnabled(java.lang.Boolean value){
        if(onPropSet(PROP_ID_enabled,value)){
            this._enabled = value;
            internalClearRefs(PROP_ID_enabled);
            
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
    
   private io.nop.orm.support.OrmFileComponent _urlComponent;

   private static Map<String,Integer> COMPONENT_PROP_ID_MAP_urlComponent = new HashMap<>();
   static{
      
         COMPONENT_PROP_ID_MAP_urlComponent.put(io.nop.orm.support.OrmFileComponent.PROP_NAME_filePath,PROP_ID_url);
      
   }

   public io.nop.orm.support.OrmFileComponent getUrlComponent(){
      if(_urlComponent == null){
          _urlComponent = new io.nop.orm.support.OrmFileComponent();
          _urlComponent.bindToEntity(this, COMPONENT_PROP_ID_MAP_urlComponent);
      }
      return _urlComponent;
   }

}
// resume CPD analysis - CPD-ON
