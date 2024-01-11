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

import app.mall.dao.entity.LitemallTopic;

// tell cpd to start ignoring code - CPD-OFF
/**
 *  专题表: litemall_topic
 */
@SuppressWarnings({"PMD.UselessOverridingMethod","PMD.UnusedLocalVariable","java:S3008","java:S1602","java:S1128","java:S1161",
        "PMD.UnnecessaryFullyQualifiedName","PMD.EmptyControlStatement","java:S116","java:S115","java:S101","java:S3776"})
public class _LitemallTopic extends DynamicOrmEntity{
    
    /* Id: ID INTEGER */
    public static final String PROP_NAME_id = "id";
    public static final int PROP_ID_id = 1;
    
    /* 专题标题: TITLE VARCHAR */
    public static final String PROP_NAME_title = "title";
    public static final int PROP_ID_title = 2;
    
    /* 专题子标题: SUBTITLE VARCHAR */
    public static final String PROP_NAME_subtitle = "subtitle";
    public static final int PROP_ID_subtitle = 3;
    
    /* 专题内容: CONTENT VARCHAR */
    public static final String PROP_NAME_content = "content";
    public static final int PROP_ID_content = 4;
    
    /* 专题相关商品最低价: PRICE DECIMAL */
    public static final String PROP_NAME_price = "price";
    public static final int PROP_ID_price = 5;
    
    /* 专题阅读量: READ_COUNT VARCHAR */
    public static final String PROP_NAME_readCount = "readCount";
    public static final int PROP_ID_readCount = 6;
    
    /* 专题图片: PIC_URL VARCHAR */
    public static final String PROP_NAME_picUrl = "picUrl";
    public static final int PROP_ID_picUrl = 7;
    
    /* 排序: SORT_ORDER INTEGER */
    public static final String PROP_NAME_sortOrder = "sortOrder";
    public static final int PROP_ID_sortOrder = 8;
    
    /* 专题相关商品: GOODS VARCHAR */
    public static final String PROP_NAME_goods = "goods";
    public static final int PROP_ID_goods = 9;
    
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
    public static final String PROP_NAME_goodsComponent = "goodsComponent";
    

    protected static final List<String> PK_PROP_NAMES = Arrays.asList(PROP_NAME_id);
    protected static final int[] PK_PROP_IDS = new int[]{PROP_ID_id};

    private static final String[] PROP_ID_TO_NAME = new String[13];
    private static final Map<String,Integer> PROP_NAME_TO_ID = new HashMap<>();
    static{
      
          PROP_ID_TO_NAME[PROP_ID_id] = PROP_NAME_id;
          PROP_NAME_TO_ID.put(PROP_NAME_id, PROP_ID_id);
      
          PROP_ID_TO_NAME[PROP_ID_title] = PROP_NAME_title;
          PROP_NAME_TO_ID.put(PROP_NAME_title, PROP_ID_title);
      
          PROP_ID_TO_NAME[PROP_ID_subtitle] = PROP_NAME_subtitle;
          PROP_NAME_TO_ID.put(PROP_NAME_subtitle, PROP_ID_subtitle);
      
          PROP_ID_TO_NAME[PROP_ID_content] = PROP_NAME_content;
          PROP_NAME_TO_ID.put(PROP_NAME_content, PROP_ID_content);
      
          PROP_ID_TO_NAME[PROP_ID_price] = PROP_NAME_price;
          PROP_NAME_TO_ID.put(PROP_NAME_price, PROP_ID_price);
      
          PROP_ID_TO_NAME[PROP_ID_readCount] = PROP_NAME_readCount;
          PROP_NAME_TO_ID.put(PROP_NAME_readCount, PROP_ID_readCount);
      
          PROP_ID_TO_NAME[PROP_ID_picUrl] = PROP_NAME_picUrl;
          PROP_NAME_TO_ID.put(PROP_NAME_picUrl, PROP_ID_picUrl);
      
          PROP_ID_TO_NAME[PROP_ID_sortOrder] = PROP_NAME_sortOrder;
          PROP_NAME_TO_ID.put(PROP_NAME_sortOrder, PROP_ID_sortOrder);
      
          PROP_ID_TO_NAME[PROP_ID_goods] = PROP_NAME_goods;
          PROP_NAME_TO_ID.put(PROP_NAME_goods, PROP_ID_goods);
      
          PROP_ID_TO_NAME[PROP_ID_addTime] = PROP_NAME_addTime;
          PROP_NAME_TO_ID.put(PROP_NAME_addTime, PROP_ID_addTime);
      
          PROP_ID_TO_NAME[PROP_ID_updateTime] = PROP_NAME_updateTime;
          PROP_NAME_TO_ID.put(PROP_NAME_updateTime, PROP_ID_updateTime);
      
          PROP_ID_TO_NAME[PROP_ID_deleted] = PROP_NAME_deleted;
          PROP_NAME_TO_ID.put(PROP_NAME_deleted, PROP_ID_deleted);
      
    }

    
    /* Id: ID */
    private java.lang.Integer _id;
    
    /* 专题标题: TITLE */
    private java.lang.String _title;
    
    /* 专题子标题: SUBTITLE */
    private java.lang.String _subtitle;
    
    /* 专题内容: CONTENT */
    private java.lang.String _content;
    
    /* 专题相关商品最低价: PRICE */
    private java.math.BigDecimal _price;
    
    /* 专题阅读量: READ_COUNT */
    private java.lang.String _readCount;
    
    /* 专题图片: PIC_URL */
    private java.lang.String _picUrl;
    
    /* 排序: SORT_ORDER */
    private java.lang.Integer _sortOrder;
    
    /* 专题相关商品: GOODS */
    private java.lang.String _goods;
    
    /* 创建时间: ADD_TIME */
    private java.time.LocalDateTime _addTime;
    
    /* 更新时间: UPDATE_TIME */
    private java.time.LocalDateTime _updateTime;
    
    /* 逻辑删除: DELETED */
    private java.lang.Boolean _deleted;
    

    public _LitemallTopic(){
        // for debug
    }

    protected LitemallTopic newInstance(){
       return new LitemallTopic();
    }

    @Override
    public LitemallTopic cloneInstance() {
        LitemallTopic entity = newInstance();
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
      return "app.mall.dao.entity.LitemallTopic";
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
        
            case PROP_ID_title:
               return getTitle();
        
            case PROP_ID_subtitle:
               return getSubtitle();
        
            case PROP_ID_content:
               return getContent();
        
            case PROP_ID_price:
               return getPrice();
        
            case PROP_ID_readCount:
               return getReadCount();
        
            case PROP_ID_picUrl:
               return getPicUrl();
        
            case PROP_ID_sortOrder:
               return getSortOrder();
        
            case PROP_ID_goods:
               return getGoods();
        
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
        
            case PROP_ID_title:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_title));
               }
               setTitle(typedValue);
               break;
            }
        
            case PROP_ID_subtitle:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_subtitle));
               }
               setSubtitle(typedValue);
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
        
            case PROP_ID_price:{
               java.math.BigDecimal typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toBigDecimal(value,
                       err-> newTypeConversionError(PROP_NAME_price));
               }
               setPrice(typedValue);
               break;
            }
        
            case PROP_ID_readCount:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_readCount));
               }
               setReadCount(typedValue);
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
        
            case PROP_ID_sortOrder:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_sortOrder));
               }
               setSortOrder(typedValue);
               break;
            }
        
            case PROP_ID_goods:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_goods));
               }
               setGoods(typedValue);
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
        
            case PROP_ID_title:{
               onInitProp(propId);
               this._title = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_subtitle:{
               onInitProp(propId);
               this._subtitle = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_content:{
               onInitProp(propId);
               this._content = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_price:{
               onInitProp(propId);
               this._price = (java.math.BigDecimal)value;
               
               break;
            }
        
            case PROP_ID_readCount:{
               onInitProp(propId);
               this._readCount = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_picUrl:{
               onInitProp(propId);
               this._picUrl = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_sortOrder:{
               onInitProp(propId);
               this._sortOrder = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_goods:{
               onInitProp(propId);
               this._goods = (java.lang.String)value;
               
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
     * 专题标题: TITLE
     */
    public java.lang.String getTitle(){
         onPropGet(PROP_ID_title);
         return _title;
    }

    /**
     * 专题标题: TITLE
     */
    public void setTitle(java.lang.String value){
        if(onPropSet(PROP_ID_title,value)){
            this._title = value;
            internalClearRefs(PROP_ID_title);
            
        }
    }
    
    /**
     * 专题子标题: SUBTITLE
     */
    public java.lang.String getSubtitle(){
         onPropGet(PROP_ID_subtitle);
         return _subtitle;
    }

    /**
     * 专题子标题: SUBTITLE
     */
    public void setSubtitle(java.lang.String value){
        if(onPropSet(PROP_ID_subtitle,value)){
            this._subtitle = value;
            internalClearRefs(PROP_ID_subtitle);
            
        }
    }
    
    /**
     * 专题内容: CONTENT
     */
    public java.lang.String getContent(){
         onPropGet(PROP_ID_content);
         return _content;
    }

    /**
     * 专题内容: CONTENT
     */
    public void setContent(java.lang.String value){
        if(onPropSet(PROP_ID_content,value)){
            this._content = value;
            internalClearRefs(PROP_ID_content);
            
        }
    }
    
    /**
     * 专题相关商品最低价: PRICE
     */
    public java.math.BigDecimal getPrice(){
         onPropGet(PROP_ID_price);
         return _price;
    }

    /**
     * 专题相关商品最低价: PRICE
     */
    public void setPrice(java.math.BigDecimal value){
        if(onPropSet(PROP_ID_price,value)){
            this._price = value;
            internalClearRefs(PROP_ID_price);
            
        }
    }
    
    /**
     * 专题阅读量: READ_COUNT
     */
    public java.lang.String getReadCount(){
         onPropGet(PROP_ID_readCount);
         return _readCount;
    }

    /**
     * 专题阅读量: READ_COUNT
     */
    public void setReadCount(java.lang.String value){
        if(onPropSet(PROP_ID_readCount,value)){
            this._readCount = value;
            internalClearRefs(PROP_ID_readCount);
            
        }
    }
    
    /**
     * 专题图片: PIC_URL
     */
    public java.lang.String getPicUrl(){
         onPropGet(PROP_ID_picUrl);
         return _picUrl;
    }

    /**
     * 专题图片: PIC_URL
     */
    public void setPicUrl(java.lang.String value){
        if(onPropSet(PROP_ID_picUrl,value)){
            this._picUrl = value;
            internalClearRefs(PROP_ID_picUrl);
            
        }
    }
    
    /**
     * 排序: SORT_ORDER
     */
    public java.lang.Integer getSortOrder(){
         onPropGet(PROP_ID_sortOrder);
         return _sortOrder;
    }

    /**
     * 排序: SORT_ORDER
     */
    public void setSortOrder(java.lang.Integer value){
        if(onPropSet(PROP_ID_sortOrder,value)){
            this._sortOrder = value;
            internalClearRefs(PROP_ID_sortOrder);
            
        }
    }
    
    /**
     * 专题相关商品: GOODS
     */
    public java.lang.String getGoods(){
         onPropGet(PROP_ID_goods);
         return _goods;
    }

    /**
     * 专题相关商品: GOODS
     */
    public void setGoods(java.lang.String value){
        if(onPropSet(PROP_ID_goods,value)){
            this._goods = value;
            internalClearRefs(PROP_ID_goods);
            
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
    
   private io.nop.orm.component.JsonOrmComponent _goodsComponent;

   private static Map<String,Integer> COMPONENT_PROP_ID_MAP_goodsComponent = new HashMap<>();
   static{
      
         COMPONENT_PROP_ID_MAP_goodsComponent.put(io.nop.orm.component.JsonOrmComponent.PROP_NAME__jsonText,PROP_ID_goods);
      
   }

   public io.nop.orm.component.JsonOrmComponent getGoodsComponent(){
      if(_goodsComponent == null){
          _goodsComponent = new io.nop.orm.component.JsonOrmComponent();
          _goodsComponent.bindToEntity(this, COMPONENT_PROP_ID_MAP_goodsComponent);
      }
      return _goodsComponent;
   }

}
// resume CPD analysis - CPD-ON
