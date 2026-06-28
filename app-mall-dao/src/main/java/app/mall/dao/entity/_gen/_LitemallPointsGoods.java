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

import app.mall.dao.entity.LitemallPointsGoods;

// tell cpd to start ignoring code - CPD-OFF
/**
 *  积分商品表: litemall_points_goods
 */
@SuppressWarnings({"PMD.UselessOverridingMethod","PMD.UnusedLocalVariable","java:S3008","java:S1602","java:S1128","java:S1161",
        "PMD.UnnecessaryFullyQualifiedName","PMD.EmptyControlStatement","java:S116","java:S115","java:S101","java:S3776"})
public class _LitemallPointsGoods extends DynamicOrmEntity{
    
    /* Id: ID INTEGER */
    public static final String PROP_NAME_id = "id";
    public static final int PROP_ID_id = 1;
    
    /* 商品ID: GOODS_ID INTEGER */
    public static final String PROP_NAME_goodsId = "goodsId";
    public static final int PROP_ID_goodsId = 2;
    
    /* SKU ID（null表示全部SKU）: PRODUCT_ID INTEGER */
    public static final String PROP_NAME_productId = "productId";
    public static final int PROP_ID_productId = 3;
    
    /* 积分单价（单件所需积分）: POINTS_PRICE INTEGER */
    public static final String PROP_NAME_pointsPrice = "pointsPrice";
    public static final int PROP_ID_pointsPrice = 4;
    
    /* 兑换活动库存: EXCHANGE_STOCK INTEGER */
    public static final String PROP_NAME_exchangeStock = "exchangeStock";
    public static final int PROP_ID_exchangeStock = 5;
    
    /* 已兑换数量: EXCHANGED_COUNT INTEGER */
    public static final String PROP_NAME_exchangedCount = "exchangedCount";
    public static final int PROP_ID_exchangedCount = 6;
    
    /* 每人限兑次数（0=不限）: MAX_PER_USER INTEGER */
    public static final String PROP_NAME_maxPerUser = "maxPerUser";
    public static final int PROP_ID_maxPerUser = 7;
    
    /* 兑换开始时间: START_TIME DATETIME */
    public static final String PROP_NAME_startTime = "startTime";
    public static final int PROP_ID_startTime = 8;
    
    /* 兑换结束时间: END_TIME DATETIME */
    public static final String PROP_NAME_endTime = "endTime";
    public static final int PROP_ID_endTime = 9;
    
    /* 状态: STATUS INTEGER */
    public static final String PROP_NAME_status = "status";
    public static final int PROP_ID_status = 10;
    
    /* 备注: REMARK VARCHAR */
    public static final String PROP_NAME_remark = "remark";
    public static final int PROP_ID_remark = 11;
    
    /* 创建时间: ADD_TIME DATETIME */
    public static final String PROP_NAME_addTime = "addTime";
    public static final int PROP_ID_addTime = 12;
    
    /* 更新时间: UPDATE_TIME DATETIME */
    public static final String PROP_NAME_updateTime = "updateTime";
    public static final int PROP_ID_updateTime = 13;
    
    /* 逻辑删除: DELETED BOOLEAN */
    public static final String PROP_NAME_deleted = "deleted";
    public static final int PROP_ID_deleted = 14;
    

    private static int _PROP_ID_BOUND = 15;

    
    /* relation: 商品 */
    public static final String PROP_NAME_goods = "goods";
    

    protected static final List<String> PK_PROP_NAMES = Arrays.asList(PROP_NAME_id);
    protected static final int[] PK_PROP_IDS = new int[]{PROP_ID_id};

    private static final String[] PROP_ID_TO_NAME = new String[15];
    private static final Map<String,Integer> PROP_NAME_TO_ID = new HashMap<>();
    static{
      
          PROP_ID_TO_NAME[PROP_ID_id] = PROP_NAME_id;
          PROP_NAME_TO_ID.put(PROP_NAME_id, PROP_ID_id);
      
          PROP_ID_TO_NAME[PROP_ID_goodsId] = PROP_NAME_goodsId;
          PROP_NAME_TO_ID.put(PROP_NAME_goodsId, PROP_ID_goodsId);
      
          PROP_ID_TO_NAME[PROP_ID_productId] = PROP_NAME_productId;
          PROP_NAME_TO_ID.put(PROP_NAME_productId, PROP_ID_productId);
      
          PROP_ID_TO_NAME[PROP_ID_pointsPrice] = PROP_NAME_pointsPrice;
          PROP_NAME_TO_ID.put(PROP_NAME_pointsPrice, PROP_ID_pointsPrice);
      
          PROP_ID_TO_NAME[PROP_ID_exchangeStock] = PROP_NAME_exchangeStock;
          PROP_NAME_TO_ID.put(PROP_NAME_exchangeStock, PROP_ID_exchangeStock);
      
          PROP_ID_TO_NAME[PROP_ID_exchangedCount] = PROP_NAME_exchangedCount;
          PROP_NAME_TO_ID.put(PROP_NAME_exchangedCount, PROP_ID_exchangedCount);
      
          PROP_ID_TO_NAME[PROP_ID_maxPerUser] = PROP_NAME_maxPerUser;
          PROP_NAME_TO_ID.put(PROP_NAME_maxPerUser, PROP_ID_maxPerUser);
      
          PROP_ID_TO_NAME[PROP_ID_startTime] = PROP_NAME_startTime;
          PROP_NAME_TO_ID.put(PROP_NAME_startTime, PROP_ID_startTime);
      
          PROP_ID_TO_NAME[PROP_ID_endTime] = PROP_NAME_endTime;
          PROP_NAME_TO_ID.put(PROP_NAME_endTime, PROP_ID_endTime);
      
          PROP_ID_TO_NAME[PROP_ID_status] = PROP_NAME_status;
          PROP_NAME_TO_ID.put(PROP_NAME_status, PROP_ID_status);
      
          PROP_ID_TO_NAME[PROP_ID_remark] = PROP_NAME_remark;
          PROP_NAME_TO_ID.put(PROP_NAME_remark, PROP_ID_remark);
      
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
    
    /* 积分单价（单件所需积分）: POINTS_PRICE */
    private java.lang.Integer _pointsPrice;
    
    /* 兑换活动库存: EXCHANGE_STOCK */
    private java.lang.Integer _exchangeStock;
    
    /* 已兑换数量: EXCHANGED_COUNT */
    private java.lang.Integer _exchangedCount;
    
    /* 每人限兑次数（0=不限）: MAX_PER_USER */
    private java.lang.Integer _maxPerUser;
    
    /* 兑换开始时间: START_TIME */
    private java.time.LocalDateTime _startTime;
    
    /* 兑换结束时间: END_TIME */
    private java.time.LocalDateTime _endTime;
    
    /* 状态: STATUS */
    private java.lang.Integer _status;
    
    /* 备注: REMARK */
    private java.lang.String _remark;
    
    /* 创建时间: ADD_TIME */
    private java.time.LocalDateTime _addTime;
    
    /* 更新时间: UPDATE_TIME */
    private java.time.LocalDateTime _updateTime;
    
    /* 逻辑删除: DELETED */
    private java.lang.Boolean _deleted;
    

    public _LitemallPointsGoods(){
        // for debug
    }

    protected LitemallPointsGoods newInstance(){
        LitemallPointsGoods entity = new LitemallPointsGoods();
        entity.orm_attach(orm_enhancer());
        entity.orm_entityModel(orm_entityModel());
        return entity;
    }

    @Override
    public LitemallPointsGoods cloneInstance() {
        LitemallPointsGoods entity = newInstance();
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
      return "app.mall.dao.entity.LitemallPointsGoods";
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
        
            case PROP_ID_pointsPrice:
               return getPointsPrice();
        
            case PROP_ID_exchangeStock:
               return getExchangeStock();
        
            case PROP_ID_exchangedCount:
               return getExchangedCount();
        
            case PROP_ID_maxPerUser:
               return getMaxPerUser();
        
            case PROP_ID_startTime:
               return getStartTime();
        
            case PROP_ID_endTime:
               return getEndTime();
        
            case PROP_ID_status:
               return getStatus();
        
            case PROP_ID_remark:
               return getRemark();
        
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
        
            case PROP_ID_pointsPrice:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_pointsPrice));
               }
               setPointsPrice(typedValue);
               break;
            }
        
            case PROP_ID_exchangeStock:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_exchangeStock));
               }
               setExchangeStock(typedValue);
               break;
            }
        
            case PROP_ID_exchangedCount:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_exchangedCount));
               }
               setExchangedCount(typedValue);
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
        
            case PROP_ID_status:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_status));
               }
               setStatus(typedValue);
               break;
            }
        
            case PROP_ID_remark:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_remark));
               }
               setRemark(typedValue);
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
        
            case PROP_ID_pointsPrice:{
               onInitProp(propId);
               this._pointsPrice = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_exchangeStock:{
               onInitProp(propId);
               this._exchangeStock = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_exchangedCount:{
               onInitProp(propId);
               this._exchangedCount = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_maxPerUser:{
               onInitProp(propId);
               this._maxPerUser = (java.lang.Integer)value;
               
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
        
            case PROP_ID_status:{
               onInitProp(propId);
               this._status = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_remark:{
               onInitProp(propId);
               this._remark = (java.lang.String)value;
               
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
     * 积分单价（单件所需积分）: POINTS_PRICE
     */
    public final java.lang.Integer getPointsPrice(){
         onPropGet(PROP_ID_pointsPrice);
         return _pointsPrice;
    }

    /**
     * 积分单价（单件所需积分）: POINTS_PRICE
     */
    public final void setPointsPrice(java.lang.Integer value){
        if(onPropSet(PROP_ID_pointsPrice,value)){
            this._pointsPrice = value;
            internalClearRefs(PROP_ID_pointsPrice);
            
        }
    }
    
    /**
     * 兑换活动库存: EXCHANGE_STOCK
     */
    public final java.lang.Integer getExchangeStock(){
         onPropGet(PROP_ID_exchangeStock);
         return _exchangeStock;
    }

    /**
     * 兑换活动库存: EXCHANGE_STOCK
     */
    public final void setExchangeStock(java.lang.Integer value){
        if(onPropSet(PROP_ID_exchangeStock,value)){
            this._exchangeStock = value;
            internalClearRefs(PROP_ID_exchangeStock);
            
        }
    }
    
    /**
     * 已兑换数量: EXCHANGED_COUNT
     */
    public final java.lang.Integer getExchangedCount(){
         onPropGet(PROP_ID_exchangedCount);
         return _exchangedCount;
    }

    /**
     * 已兑换数量: EXCHANGED_COUNT
     */
    public final void setExchangedCount(java.lang.Integer value){
        if(onPropSet(PROP_ID_exchangedCount,value)){
            this._exchangedCount = value;
            internalClearRefs(PROP_ID_exchangedCount);
            
        }
    }
    
    /**
     * 每人限兑次数（0=不限）: MAX_PER_USER
     */
    public final java.lang.Integer getMaxPerUser(){
         onPropGet(PROP_ID_maxPerUser);
         return _maxPerUser;
    }

    /**
     * 每人限兑次数（0=不限）: MAX_PER_USER
     */
    public final void setMaxPerUser(java.lang.Integer value){
        if(onPropSet(PROP_ID_maxPerUser,value)){
            this._maxPerUser = value;
            internalClearRefs(PROP_ID_maxPerUser);
            
        }
    }
    
    /**
     * 兑换开始时间: START_TIME
     */
    public final java.time.LocalDateTime getStartTime(){
         onPropGet(PROP_ID_startTime);
         return _startTime;
    }

    /**
     * 兑换开始时间: START_TIME
     */
    public final void setStartTime(java.time.LocalDateTime value){
        if(onPropSet(PROP_ID_startTime,value)){
            this._startTime = value;
            internalClearRefs(PROP_ID_startTime);
            
        }
    }
    
    /**
     * 兑换结束时间: END_TIME
     */
    public final java.time.LocalDateTime getEndTime(){
         onPropGet(PROP_ID_endTime);
         return _endTime;
    }

    /**
     * 兑换结束时间: END_TIME
     */
    public final void setEndTime(java.time.LocalDateTime value){
        if(onPropSet(PROP_ID_endTime,value)){
            this._endTime = value;
            internalClearRefs(PROP_ID_endTime);
            
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
     * 备注: REMARK
     */
    public final java.lang.String getRemark(){
         onPropGet(PROP_ID_remark);
         return _remark;
    }

    /**
     * 备注: REMARK
     */
    public final void setRemark(java.lang.String value){
        if(onPropSet(PROP_ID_remark,value)){
            this._remark = value;
            internalClearRefs(PROP_ID_remark);
            
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
       
}
// resume CPD analysis - CPD-ON
