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

import app.mall.dao.entity.LitemallPinTuanActivity;

// tell cpd to start ignoring code - CPD-OFF
/**
 *  拼团活动表: litemall_pin_tuan_activity
 */
@SuppressWarnings({"PMD.UselessOverridingMethod","PMD.UnusedLocalVariable","java:S3008","java:S1602","java:S1128","java:S1161",
        "PMD.UnnecessaryFullyQualifiedName","PMD.EmptyControlStatement","java:S116","java:S115","java:S101","java:S3776"})
public class _LitemallPinTuanActivity extends DynamicOrmEntity{
    
    /* Id: ID INTEGER */
    public static final String PROP_NAME_id = "id";
    public static final int PROP_ID_id = 1;
    
    /* 商品ID: GOODS_ID INTEGER */
    public static final String PROP_NAME_goodsId = "goodsId";
    public static final int PROP_ID_goodsId = 2;
    
    /* SKU ID（null表示全部SKU）: PRODUCT_ID INTEGER */
    public static final String PROP_NAME_productId = "productId";
    public static final int PROP_ID_productId = 3;
    
    /* 拼团价: PIN_TUAN_PRICE DECIMAL */
    public static final String PROP_NAME_pinTuanPrice = "pinTuanPrice";
    public static final int PROP_ID_pinTuanPrice = 4;
    
    /* 成团人数门槛: MIN_USER_COUNT INTEGER */
    public static final String PROP_NAME_minUserCount = "minUserCount";
    public static final int PROP_ID_minUserCount = 5;
    
    /* 最多人数: MAX_USER_COUNT INTEGER */
    public static final String PROP_NAME_maxUserCount = "maxUserCount";
    public static final int PROP_ID_maxUserCount = 6;
    
    /* 有效时间（小时）: EXPIRE_HOURS INTEGER */
    public static final String PROP_NAME_expireHours = "expireHours";
    public static final int PROP_ID_expireHours = 7;
    
    /* 状态: STATUS INTEGER */
    public static final String PROP_NAME_status = "status";
    public static final int PROP_ID_status = 8;
    
    /* 备注: REMARK VARCHAR */
    public static final String PROP_NAME_remark = "remark";
    public static final int PROP_ID_remark = 9;
    
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

    
    /* relation: 商品 */
    public static final String PROP_NAME_goods = "goods";
    

    protected static final List<String> PK_PROP_NAMES = Arrays.asList(PROP_NAME_id);
    protected static final int[] PK_PROP_IDS = new int[]{PROP_ID_id};

    private static final String[] PROP_ID_TO_NAME = new String[13];
    private static final Map<String,Integer> PROP_NAME_TO_ID = new HashMap<>();
    static{
      
          PROP_ID_TO_NAME[PROP_ID_id] = PROP_NAME_id;
          PROP_NAME_TO_ID.put(PROP_NAME_id, PROP_ID_id);
      
          PROP_ID_TO_NAME[PROP_ID_goodsId] = PROP_NAME_goodsId;
          PROP_NAME_TO_ID.put(PROP_NAME_goodsId, PROP_ID_goodsId);
      
          PROP_ID_TO_NAME[PROP_ID_productId] = PROP_NAME_productId;
          PROP_NAME_TO_ID.put(PROP_NAME_productId, PROP_ID_productId);
      
          PROP_ID_TO_NAME[PROP_ID_pinTuanPrice] = PROP_NAME_pinTuanPrice;
          PROP_NAME_TO_ID.put(PROP_NAME_pinTuanPrice, PROP_ID_pinTuanPrice);
      
          PROP_ID_TO_NAME[PROP_ID_minUserCount] = PROP_NAME_minUserCount;
          PROP_NAME_TO_ID.put(PROP_NAME_minUserCount, PROP_ID_minUserCount);
      
          PROP_ID_TO_NAME[PROP_ID_maxUserCount] = PROP_NAME_maxUserCount;
          PROP_NAME_TO_ID.put(PROP_NAME_maxUserCount, PROP_ID_maxUserCount);
      
          PROP_ID_TO_NAME[PROP_ID_expireHours] = PROP_NAME_expireHours;
          PROP_NAME_TO_ID.put(PROP_NAME_expireHours, PROP_ID_expireHours);
      
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
    
    /* 拼团价: PIN_TUAN_PRICE */
    private java.math.BigDecimal _pinTuanPrice;
    
    /* 成团人数门槛: MIN_USER_COUNT */
    private java.lang.Integer _minUserCount;
    
    /* 最多人数: MAX_USER_COUNT */
    private java.lang.Integer _maxUserCount;
    
    /* 有效时间（小时）: EXPIRE_HOURS */
    private java.lang.Integer _expireHours;
    
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
    

    public _LitemallPinTuanActivity(){
        // for debug
    }

    protected LitemallPinTuanActivity newInstance(){
        LitemallPinTuanActivity entity = new LitemallPinTuanActivity();
        entity.orm_attach(orm_enhancer());
        entity.orm_entityModel(orm_entityModel());
        return entity;
    }

    @Override
    public LitemallPinTuanActivity cloneInstance() {
        LitemallPinTuanActivity entity = newInstance();
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
      return "app.mall.dao.entity.LitemallPinTuanActivity";
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
        
            case PROP_ID_pinTuanPrice:
               return getPinTuanPrice();
        
            case PROP_ID_minUserCount:
               return getMinUserCount();
        
            case PROP_ID_maxUserCount:
               return getMaxUserCount();
        
            case PROP_ID_expireHours:
               return getExpireHours();
        
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
        
            case PROP_ID_pinTuanPrice:{
               java.math.BigDecimal typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toBigDecimal(value,
                       err-> newTypeConversionError(PROP_NAME_pinTuanPrice));
               }
               setPinTuanPrice(typedValue);
               break;
            }
        
            case PROP_ID_minUserCount:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_minUserCount));
               }
               setMinUserCount(typedValue);
               break;
            }
        
            case PROP_ID_maxUserCount:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_maxUserCount));
               }
               setMaxUserCount(typedValue);
               break;
            }
        
            case PROP_ID_expireHours:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_expireHours));
               }
               setExpireHours(typedValue);
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
        
            case PROP_ID_pinTuanPrice:{
               onInitProp(propId);
               this._pinTuanPrice = (java.math.BigDecimal)value;
               
               break;
            }
        
            case PROP_ID_minUserCount:{
               onInitProp(propId);
               this._minUserCount = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_maxUserCount:{
               onInitProp(propId);
               this._maxUserCount = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_expireHours:{
               onInitProp(propId);
               this._expireHours = (java.lang.Integer)value;
               
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
     * 拼团价: PIN_TUAN_PRICE
     */
    public final java.math.BigDecimal getPinTuanPrice(){
         onPropGet(PROP_ID_pinTuanPrice);
         return _pinTuanPrice;
    }

    /**
     * 拼团价: PIN_TUAN_PRICE
     */
    public final void setPinTuanPrice(java.math.BigDecimal value){
        if(onPropSet(PROP_ID_pinTuanPrice,value)){
            this._pinTuanPrice = value;
            internalClearRefs(PROP_ID_pinTuanPrice);
            
        }
    }
    
    /**
     * 成团人数门槛: MIN_USER_COUNT
     */
    public final java.lang.Integer getMinUserCount(){
         onPropGet(PROP_ID_minUserCount);
         return _minUserCount;
    }

    /**
     * 成团人数门槛: MIN_USER_COUNT
     */
    public final void setMinUserCount(java.lang.Integer value){
        if(onPropSet(PROP_ID_minUserCount,value)){
            this._minUserCount = value;
            internalClearRefs(PROP_ID_minUserCount);
            
        }
    }
    
    /**
     * 最多人数: MAX_USER_COUNT
     */
    public final java.lang.Integer getMaxUserCount(){
         onPropGet(PROP_ID_maxUserCount);
         return _maxUserCount;
    }

    /**
     * 最多人数: MAX_USER_COUNT
     */
    public final void setMaxUserCount(java.lang.Integer value){
        if(onPropSet(PROP_ID_maxUserCount,value)){
            this._maxUserCount = value;
            internalClearRefs(PROP_ID_maxUserCount);
            
        }
    }
    
    /**
     * 有效时间（小时）: EXPIRE_HOURS
     */
    public final java.lang.Integer getExpireHours(){
         onPropGet(PROP_ID_expireHours);
         return _expireHours;
    }

    /**
     * 有效时间（小时）: EXPIRE_HOURS
     */
    public final void setExpireHours(java.lang.Integer value){
        if(onPropSet(PROP_ID_expireHours,value)){
            this._expireHours = value;
            internalClearRefs(PROP_ID_expireHours);
            
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
