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

import app.mall.dao.entity.LitemallFlashSale;

// tell cpd to start ignoring code - CPD-OFF
/**
 *  秒杀活动表: litemall_flash_sale
 */
@SuppressWarnings({"PMD.UselessOverridingMethod","PMD.UnusedLocalVariable","java:S3008","java:S1602","java:S1128","java:S1161",
        "PMD.UnnecessaryFullyQualifiedName","PMD.EmptyControlStatement","java:S116","java:S115","java:S101","java:S3776"})
public class _LitemallFlashSale extends DynamicOrmEntity{
    
    /* Id: ID INTEGER */
    public static final String PROP_NAME_id = "id";
    public static final int PROP_ID_id = 1;
    
    /* 商品ID: GOODS_ID INTEGER */
    public static final String PROP_NAME_goodsId = "goodsId";
    public static final int PROP_ID_goodsId = 2;
    
    /* SKU ID（null表示全部SKU）: PRODUCT_ID INTEGER */
    public static final String PROP_NAME_productId = "productId";
    public static final int PROP_ID_productId = 3;
    
    /* 秒杀价: FLASH_PRICE DECIMAL */
    public static final String PROP_NAME_flashPrice = "flashPrice";
    public static final int PROP_ID_flashPrice = 4;
    
    /* 活动总库存: TOTAL_STOCK INTEGER */
    public static final String PROP_NAME_totalStock = "totalStock";
    public static final int PROP_ID_totalStock = 5;
    
    /* 每人限购: MAX_PER_USER INTEGER */
    public static final String PROP_NAME_maxPerUser = "maxPerUser";
    public static final int PROP_ID_maxPerUser = 6;
    
    /* 每单限购: MAX_PER_ORDER INTEGER */
    public static final String PROP_NAME_maxPerOrder = "maxPerOrder";
    public static final int PROP_ID_maxPerOrder = 7;
    
    /* 状态: STATUS INTEGER */
    public static final String PROP_NAME_status = "status";
    public static final int PROP_ID_status = 8;
    
    /* 创建时间: ADD_TIME DATETIME */
    public static final String PROP_NAME_addTime = "addTime";
    public static final int PROP_ID_addTime = 9;
    
    /* 更新时间: UPDATE_TIME DATETIME */
    public static final String PROP_NAME_updateTime = "updateTime";
    public static final int PROP_ID_updateTime = 10;
    
    /* 逻辑删除: DELETED BOOLEAN */
    public static final String PROP_NAME_deleted = "deleted";
    public static final int PROP_ID_deleted = 11;
    

    private static int _PROP_ID_BOUND = 12;

    
    /* relation: 商品 */
    public static final String PROP_NAME_goods = "goods";
    
    /* relation: 秒杀场次 */
    public static final String PROP_NAME_sessions = "sessions";
    

    protected static final List<String> PK_PROP_NAMES = Arrays.asList(PROP_NAME_id);
    protected static final int[] PK_PROP_IDS = new int[]{PROP_ID_id};

    private static final String[] PROP_ID_TO_NAME = new String[12];
    private static final Map<String,Integer> PROP_NAME_TO_ID = new HashMap<>();
    static{
      
          PROP_ID_TO_NAME[PROP_ID_id] = PROP_NAME_id;
          PROP_NAME_TO_ID.put(PROP_NAME_id, PROP_ID_id);
      
          PROP_ID_TO_NAME[PROP_ID_goodsId] = PROP_NAME_goodsId;
          PROP_NAME_TO_ID.put(PROP_NAME_goodsId, PROP_ID_goodsId);
      
          PROP_ID_TO_NAME[PROP_ID_productId] = PROP_NAME_productId;
          PROP_NAME_TO_ID.put(PROP_NAME_productId, PROP_ID_productId);
      
          PROP_ID_TO_NAME[PROP_ID_flashPrice] = PROP_NAME_flashPrice;
          PROP_NAME_TO_ID.put(PROP_NAME_flashPrice, PROP_ID_flashPrice);
      
          PROP_ID_TO_NAME[PROP_ID_totalStock] = PROP_NAME_totalStock;
          PROP_NAME_TO_ID.put(PROP_NAME_totalStock, PROP_ID_totalStock);
      
          PROP_ID_TO_NAME[PROP_ID_maxPerUser] = PROP_NAME_maxPerUser;
          PROP_NAME_TO_ID.put(PROP_NAME_maxPerUser, PROP_ID_maxPerUser);
      
          PROP_ID_TO_NAME[PROP_ID_maxPerOrder] = PROP_NAME_maxPerOrder;
          PROP_NAME_TO_ID.put(PROP_NAME_maxPerOrder, PROP_ID_maxPerOrder);
      
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
    private java.lang.String _id;
    
    /* 商品ID: GOODS_ID */
    private java.lang.String _goodsId;
    
    /* SKU ID（null表示全部SKU）: PRODUCT_ID */
    private java.lang.String _productId;
    
    /* 秒杀价: FLASH_PRICE */
    private java.math.BigDecimal _flashPrice;
    
    /* 活动总库存: TOTAL_STOCK */
    private java.lang.Integer _totalStock;
    
    /* 每人限购: MAX_PER_USER */
    private java.lang.Integer _maxPerUser;
    
    /* 每单限购: MAX_PER_ORDER */
    private java.lang.Integer _maxPerOrder;
    
    /* 状态: STATUS */
    private java.lang.Integer _status;
    
    /* 创建时间: ADD_TIME */
    private java.time.LocalDateTime _addTime;
    
    /* 更新时间: UPDATE_TIME */
    private java.time.LocalDateTime _updateTime;
    
    /* 逻辑删除: DELETED */
    private java.lang.Boolean _deleted;
    

    public _LitemallFlashSale(){
        // for debug
    }

    protected LitemallFlashSale newInstance(){
        LitemallFlashSale entity = new LitemallFlashSale();
        entity.orm_attach(orm_enhancer());
        entity.orm_entityModel(orm_entityModel());
        return entity;
    }

    @Override
    public LitemallFlashSale cloneInstance() {
        LitemallFlashSale entity = newInstance();
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
      return "app.mall.dao.entity.LitemallFlashSale";
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
        
            case PROP_ID_productId:
               return getProductId();
        
            case PROP_ID_flashPrice:
               return getFlashPrice();
        
            case PROP_ID_totalStock:
               return getTotalStock();
        
            case PROP_ID_maxPerUser:
               return getMaxPerUser();
        
            case PROP_ID_maxPerOrder:
               return getMaxPerOrder();
        
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
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_id));
               }
               setId(typedValue);
               break;
            }
        
            case PROP_ID_goodsId:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_goodsId));
               }
               setGoodsId(typedValue);
               break;
            }
        
            case PROP_ID_productId:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_productId));
               }
               setProductId(typedValue);
               break;
            }
        
            case PROP_ID_flashPrice:{
               java.math.BigDecimal typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toBigDecimal(value,
                       err-> newTypeConversionError(PROP_NAME_flashPrice));
               }
               setFlashPrice(typedValue);
               break;
            }
        
            case PROP_ID_totalStock:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_totalStock));
               }
               setTotalStock(typedValue);
               break;
            }
        
            case PROP_ID_maxPerUser:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_maxPerUser));
               }
               setMaxPerUser(typedValue);
               break;
            }
        
            case PROP_ID_maxPerOrder:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_maxPerOrder));
               }
               setMaxPerOrder(typedValue);
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
        
            case PROP_ID_goodsId:{
               onInitProp(propId);
               this._goodsId = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_productId:{
               onInitProp(propId);
               this._productId = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_flashPrice:{
               onInitProp(propId);
               this._flashPrice = (java.math.BigDecimal)value;
               
               break;
            }
        
            case PROP_ID_totalStock:{
               onInitProp(propId);
               this._totalStock = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_maxPerUser:{
               onInitProp(propId);
               this._maxPerUser = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_maxPerOrder:{
               onInitProp(propId);
               this._maxPerOrder = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_status:{
               onInitProp(propId);
               this._status = (java.lang.Integer)value;
               
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
     * 商品ID: GOODS_ID
     */
    public final java.lang.String getGoodsId(){
         onPropGet(PROP_ID_goodsId);
         return _goodsId;
    }

    /**
     * 商品ID: GOODS_ID
     */
    public final void setGoodsId(java.lang.String value){
        if(onPropSet(PROP_ID_goodsId,value)){
            this._goodsId = value;
            internalClearRefs(PROP_ID_goodsId);
            
        }
    }
    
    /**
     * SKU ID（null表示全部SKU）: PRODUCT_ID
     */
    public final java.lang.String getProductId(){
         onPropGet(PROP_ID_productId);
         return _productId;
    }

    /**
     * SKU ID（null表示全部SKU）: PRODUCT_ID
     */
    public final void setProductId(java.lang.String value){
        if(onPropSet(PROP_ID_productId,value)){
            this._productId = value;
            internalClearRefs(PROP_ID_productId);
            
        }
    }
    
    /**
     * 秒杀价: FLASH_PRICE
     */
    public final java.math.BigDecimal getFlashPrice(){
         onPropGet(PROP_ID_flashPrice);
         return _flashPrice;
    }

    /**
     * 秒杀价: FLASH_PRICE
     */
    public final void setFlashPrice(java.math.BigDecimal value){
        if(onPropSet(PROP_ID_flashPrice,value)){
            this._flashPrice = value;
            internalClearRefs(PROP_ID_flashPrice);
            
        }
    }
    
    /**
     * 活动总库存: TOTAL_STOCK
     */
    public final java.lang.Integer getTotalStock(){
         onPropGet(PROP_ID_totalStock);
         return _totalStock;
    }

    /**
     * 活动总库存: TOTAL_STOCK
     */
    public final void setTotalStock(java.lang.Integer value){
        if(onPropSet(PROP_ID_totalStock,value)){
            this._totalStock = value;
            internalClearRefs(PROP_ID_totalStock);
            
        }
    }
    
    /**
     * 每人限购: MAX_PER_USER
     */
    public final java.lang.Integer getMaxPerUser(){
         onPropGet(PROP_ID_maxPerUser);
         return _maxPerUser;
    }

    /**
     * 每人限购: MAX_PER_USER
     */
    public final void setMaxPerUser(java.lang.Integer value){
        if(onPropSet(PROP_ID_maxPerUser,value)){
            this._maxPerUser = value;
            internalClearRefs(PROP_ID_maxPerUser);
            
        }
    }
    
    /**
     * 每单限购: MAX_PER_ORDER
     */
    public final java.lang.Integer getMaxPerOrder(){
         onPropGet(PROP_ID_maxPerOrder);
         return _maxPerOrder;
    }

    /**
     * 每单限购: MAX_PER_ORDER
     */
    public final void setMaxPerOrder(java.lang.Integer value){
        if(onPropSet(PROP_ID_maxPerOrder,value)){
            this._maxPerOrder = value;
            internalClearRefs(PROP_ID_maxPerOrder);
            
        }
    }
    
    /**
     * 状态: STATUS
     */
    public final java.lang.Integer getStatus(){
         onPropGet(PROP_ID_status);
         return _status;
    }

    /**
     * 状态: STATUS
     */
    public final void setStatus(java.lang.Integer value){
        if(onPropSet(PROP_ID_status,value)){
            this._status = value;
            internalClearRefs(PROP_ID_status);
            
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
     * 商品
     */
    public final app.mall.dao.entity.LitemallGoods getGoods(){
       return (app.mall.dao.entity.LitemallGoods)internalGetRefEntity(PROP_NAME_goods);
    }

    public final void setGoods(app.mall.dao.entity.LitemallGoods refEntity){
   
           if(refEntity == null){
           
                   this.setGoodsId(null);
               
           }else{
           internalSetRefEntity(PROP_NAME_goods, refEntity,()->{
           
                           this.setGoodsId(refEntity.getId());
                       
           });
           }
       
    }
       
    private final OrmEntitySet<app.mall.dao.entity.LitemallFlashSaleSession> _sessions = new OrmEntitySet<>(this, PROP_NAME_sessions,
        app.mall.dao.entity.LitemallFlashSaleSession.PROP_NAME_flashSale, null,app.mall.dao.entity.LitemallFlashSaleSession.class);

    /**
     * 秒杀场次。 refPropName: flashSale, keyProp: {rel.keyProp}
     */
    public final IOrmEntitySet<app.mall.dao.entity.LitemallFlashSaleSession> getSessions(){
       return _sessions;
    }
       
}
// resume CPD analysis - CPD-ON
