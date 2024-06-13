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

import app.mall.dao.entity.LitemallGoodsProduct;

// tell cpd to start ignoring code - CPD-OFF
/**
 *  商品货品表: litemall_goods_product
 */
@SuppressWarnings({"PMD.UselessOverridingMethod","PMD.UnusedLocalVariable","java:S3008","java:S1602","java:S1128","java:S1161",
        "PMD.UnnecessaryFullyQualifiedName","PMD.EmptyControlStatement","java:S116","java:S115","java:S101","java:S3776"})
public class _LitemallGoodsProduct extends DynamicOrmEntity{
    
    /* Id: ID INTEGER */
    public static final String PROP_NAME_id = "id";
    public static final int PROP_ID_id = 1;
    
    /* 商品ID: GOODS_ID INTEGER */
    public static final String PROP_NAME_goodsId = "goodsId";
    public static final int PROP_ID_goodsId = 2;
    
    /* 商品规格值: SPECIFICATIONS VARCHAR */
    public static final String PROP_NAME_specifications = "specifications";
    public static final int PROP_ID_specifications = 3;
    
    /* 商品货品价格: PRICE DECIMAL */
    public static final String PROP_NAME_price = "price";
    public static final int PROP_ID_price = 4;
    
    /* 商品货品数量: NUMBER INTEGER */
    public static final String PROP_NAME_number = "number";
    public static final int PROP_ID_number = 5;
    
    /* 商品货品图片: URL VARCHAR */
    public static final String PROP_NAME_url = "url";
    public static final int PROP_ID_url = 6;
    
    /* 创建时间: ADD_TIME DATETIME */
    public static final String PROP_NAME_addTime = "addTime";
    public static final int PROP_ID_addTime = 7;
    
    /* 更新时间: UPDATE_TIME DATETIME */
    public static final String PROP_NAME_updateTime = "updateTime";
    public static final int PROP_ID_updateTime = 8;
    
    /* 逻辑删除: DELETED BOOLEAN */
    public static final String PROP_NAME_deleted = "deleted";
    public static final int PROP_ID_deleted = 9;
    

    private static int _PROP_ID_BOUND = 10;

    
    /* relation: 所属商品 */
    public static final String PROP_NAME_goods = "goods";
    
    /* component:  */
    public static final String PROP_NAME_specificationsComponent = "specificationsComponent";
    
    /* component:  */
    public static final String PROP_NAME_urlComponent = "urlComponent";
    

    protected static final List<String> PK_PROP_NAMES = Arrays.asList(PROP_NAME_id);
    protected static final int[] PK_PROP_IDS = new int[]{PROP_ID_id};

    private static final String[] PROP_ID_TO_NAME = new String[10];
    private static final Map<String,Integer> PROP_NAME_TO_ID = new HashMap<>();
    static{
      
          PROP_ID_TO_NAME[PROP_ID_id] = PROP_NAME_id;
          PROP_NAME_TO_ID.put(PROP_NAME_id, PROP_ID_id);
      
          PROP_ID_TO_NAME[PROP_ID_goodsId] = PROP_NAME_goodsId;
          PROP_NAME_TO_ID.put(PROP_NAME_goodsId, PROP_ID_goodsId);
      
          PROP_ID_TO_NAME[PROP_ID_specifications] = PROP_NAME_specifications;
          PROP_NAME_TO_ID.put(PROP_NAME_specifications, PROP_ID_specifications);
      
          PROP_ID_TO_NAME[PROP_ID_price] = PROP_NAME_price;
          PROP_NAME_TO_ID.put(PROP_NAME_price, PROP_ID_price);
      
          PROP_ID_TO_NAME[PROP_ID_number] = PROP_NAME_number;
          PROP_NAME_TO_ID.put(PROP_NAME_number, PROP_ID_number);
      
          PROP_ID_TO_NAME[PROP_ID_url] = PROP_NAME_url;
          PROP_NAME_TO_ID.put(PROP_NAME_url, PROP_ID_url);
      
          PROP_ID_TO_NAME[PROP_ID_addTime] = PROP_NAME_addTime;
          PROP_NAME_TO_ID.put(PROP_NAME_addTime, PROP_ID_addTime);
      
          PROP_ID_TO_NAME[PROP_ID_updateTime] = PROP_NAME_updateTime;
          PROP_NAME_TO_ID.put(PROP_NAME_updateTime, PROP_ID_updateTime);
      
          PROP_ID_TO_NAME[PROP_ID_deleted] = PROP_NAME_deleted;
          PROP_NAME_TO_ID.put(PROP_NAME_deleted, PROP_ID_deleted);
      
    }

    
    /* Id: ID */
    private java.lang.Integer _id;
    
    /* 商品ID: GOODS_ID */
    private java.lang.Integer _goodsId;
    
    /* 商品规格值: SPECIFICATIONS */
    private java.lang.String _specifications;
    
    /* 商品货品价格: PRICE */
    private java.math.BigDecimal _price;
    
    /* 商品货品数量: NUMBER */
    private java.lang.Integer _number;
    
    /* 商品货品图片: URL */
    private java.lang.String _url;
    
    /* 创建时间: ADD_TIME */
    private java.time.LocalDateTime _addTime;
    
    /* 更新时间: UPDATE_TIME */
    private java.time.LocalDateTime _updateTime;
    
    /* 逻辑删除: DELETED */
    private java.lang.Boolean _deleted;
    

    public _LitemallGoodsProduct(){
        // for debug
    }

    protected LitemallGoodsProduct newInstance(){
        LitemallGoodsProduct entity = new LitemallGoodsProduct();
        entity.orm_attach(orm_enhancer());
        entity.orm_entityModel(orm_entityModel());
        return entity;
    }

    @Override
    public LitemallGoodsProduct cloneInstance() {
        LitemallGoodsProduct entity = newInstance();
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
      return "app.mall.dao.entity.LitemallGoodsProduct";
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
        
            case PROP_ID_goodsId:
               return getGoodsId();
        
            case PROP_ID_specifications:
               return getSpecifications();
        
            case PROP_ID_price:
               return getPrice();
        
            case PROP_ID_number:
               return getNumber();
        
            case PROP_ID_url:
               return getUrl();
        
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
        
            case PROP_ID_goodsId:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_goodsId));
               }
               setGoodsId(typedValue);
               break;
            }
        
            case PROP_ID_specifications:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_specifications));
               }
               setSpecifications(typedValue);
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
        
            case PROP_ID_number:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_number));
               }
               setNumber(typedValue);
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
        
            case PROP_ID_goodsId:{
               onInitProp(propId);
               this._goodsId = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_specifications:{
               onInitProp(propId);
               this._specifications = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_price:{
               onInitProp(propId);
               this._price = (java.math.BigDecimal)value;
               
               break;
            }
        
            case PROP_ID_number:{
               onInitProp(propId);
               this._number = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_url:{
               onInitProp(propId);
               this._url = (java.lang.String)value;
               
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
     * 商品ID: GOODS_ID
     */
    public java.lang.Integer getGoodsId(){
         onPropGet(PROP_ID_goodsId);
         return _goodsId;
    }

    /**
     * 商品ID: GOODS_ID
     */
    public void setGoodsId(java.lang.Integer value){
        if(onPropSet(PROP_ID_goodsId,value)){
            this._goodsId = value;
            internalClearRefs(PROP_ID_goodsId);
            
        }
    }
    
    /**
     * 商品规格值: SPECIFICATIONS
     */
    public java.lang.String getSpecifications(){
         onPropGet(PROP_ID_specifications);
         return _specifications;
    }

    /**
     * 商品规格值: SPECIFICATIONS
     */
    public void setSpecifications(java.lang.String value){
        if(onPropSet(PROP_ID_specifications,value)){
            this._specifications = value;
            internalClearRefs(PROP_ID_specifications);
            
        }
    }
    
    /**
     * 商品货品价格: PRICE
     */
    public java.math.BigDecimal getPrice(){
         onPropGet(PROP_ID_price);
         return _price;
    }

    /**
     * 商品货品价格: PRICE
     */
    public void setPrice(java.math.BigDecimal value){
        if(onPropSet(PROP_ID_price,value)){
            this._price = value;
            internalClearRefs(PROP_ID_price);
            
        }
    }
    
    /**
     * 商品货品数量: NUMBER
     */
    public java.lang.Integer getNumber(){
         onPropGet(PROP_ID_number);
         return _number;
    }

    /**
     * 商品货品数量: NUMBER
     */
    public void setNumber(java.lang.Integer value){
        if(onPropSet(PROP_ID_number,value)){
            this._number = value;
            internalClearRefs(PROP_ID_number);
            
        }
    }
    
    /**
     * 商品货品图片: URL
     */
    public java.lang.String getUrl(){
         onPropGet(PROP_ID_url);
         return _url;
    }

    /**
     * 商品货品图片: URL
     */
    public void setUrl(java.lang.String value){
        if(onPropSet(PROP_ID_url,value)){
            this._url = value;
            internalClearRefs(PROP_ID_url);
            
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
     * 所属商品
     */
    public app.mall.dao.entity.LitemallGoods getGoods(){
       return (app.mall.dao.entity.LitemallGoods)internalGetRefEntity(PROP_NAME_goods);
    }

    public void setGoods(app.mall.dao.entity.LitemallGoods refEntity){
   
           if(refEntity == null){
           
                   this.setGoodsId(null);
               
           }else{
           internalSetRefEntity(PROP_NAME_goods, refEntity,()->{
           
                           this.setGoodsId(refEntity.getId());
                       
           });
           }
       
    }
       
   private io.nop.orm.component.JsonOrmComponent _specificationsComponent;

   private static Map<String,Integer> COMPONENT_PROP_ID_MAP_specificationsComponent = new HashMap<>();
   static{
      
         COMPONENT_PROP_ID_MAP_specificationsComponent.put(io.nop.orm.component.JsonOrmComponent.PROP_NAME__jsonText,PROP_ID_specifications);
      
   }

   public io.nop.orm.component.JsonOrmComponent getSpecificationsComponent(){
      if(_specificationsComponent == null){
          _specificationsComponent = new io.nop.orm.component.JsonOrmComponent();
          _specificationsComponent.bindToEntity(this, COMPONENT_PROP_ID_MAP_specificationsComponent);
      }
      return _specificationsComponent;
   }

   private io.nop.orm.component.OrmFileComponent _urlComponent;

   private static Map<String,Integer> COMPONENT_PROP_ID_MAP_urlComponent = new HashMap<>();
   static{
      
         COMPONENT_PROP_ID_MAP_urlComponent.put(io.nop.orm.component.OrmFileComponent.PROP_NAME_filePath,PROP_ID_url);
      
   }

   public io.nop.orm.component.OrmFileComponent getUrlComponent(){
      if(_urlComponent == null){
          _urlComponent = new io.nop.orm.component.OrmFileComponent();
          _urlComponent.bindToEntity(this, COMPONENT_PROP_ID_MAP_urlComponent);
      }
      return _urlComponent;
   }

}
// resume CPD analysis - CPD-ON
