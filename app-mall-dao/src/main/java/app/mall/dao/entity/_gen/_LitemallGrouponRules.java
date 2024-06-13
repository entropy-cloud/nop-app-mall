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

import app.mall.dao.entity.LitemallGrouponRules;

// tell cpd to start ignoring code - CPD-OFF
/**
 *  团购规则表: litemall_groupon_rules
 */
@SuppressWarnings({"PMD.UselessOverridingMethod","PMD.UnusedLocalVariable","java:S3008","java:S1602","java:S1128","java:S1161",
        "PMD.UnnecessaryFullyQualifiedName","PMD.EmptyControlStatement","java:S116","java:S115","java:S101","java:S3776"})
public class _LitemallGrouponRules extends DynamicOrmEntity{
    
    /* Id: ID INTEGER */
    public static final String PROP_NAME_id = "id";
    public static final int PROP_ID_id = 1;
    
    /* 商品ID: GOODS_ID INTEGER */
    public static final String PROP_NAME_goodsId = "goodsId";
    public static final int PROP_ID_goodsId = 2;
    
    /* 商品名称: GOODS_NAME VARCHAR */
    public static final String PROP_NAME_goodsName = "goodsName";
    public static final int PROP_ID_goodsName = 3;
    
    /* 商品/货品图片: PIC_URL VARCHAR */
    public static final String PROP_NAME_picUrl = "picUrl";
    public static final int PROP_ID_picUrl = 4;
    
    /* 优惠金额: DISCOUNT DECIMAL */
    public static final String PROP_NAME_discount = "discount";
    public static final int PROP_ID_discount = 5;
    
    /* 达到优惠条件的人数: DISCOUNT_MEMBER INTEGER */
    public static final String PROP_NAME_discountMember = "discountMember";
    public static final int PROP_ID_discountMember = 6;
    
    /* 团购过期时间: EXPIRE_TIME DATETIME */
    public static final String PROP_NAME_expireTime = "expireTime";
    public static final int PROP_ID_expireTime = 7;
    
    /* 团购规则状态，正常上线则0，到期自动下线则1，管理手动下线则2: STATUS SMALLINT */
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

    
    /* relation: 订单 */
    public static final String PROP_NAME_goods = "goods";
    
    /* component:  */
    public static final String PROP_NAME_picUrlComponent = "picUrlComponent";
    

    protected static final List<String> PK_PROP_NAMES = Arrays.asList(PROP_NAME_id);
    protected static final int[] PK_PROP_IDS = new int[]{PROP_ID_id};

    private static final String[] PROP_ID_TO_NAME = new String[12];
    private static final Map<String,Integer> PROP_NAME_TO_ID = new HashMap<>();
    static{
      
          PROP_ID_TO_NAME[PROP_ID_id] = PROP_NAME_id;
          PROP_NAME_TO_ID.put(PROP_NAME_id, PROP_ID_id);
      
          PROP_ID_TO_NAME[PROP_ID_goodsId] = PROP_NAME_goodsId;
          PROP_NAME_TO_ID.put(PROP_NAME_goodsId, PROP_ID_goodsId);
      
          PROP_ID_TO_NAME[PROP_ID_goodsName] = PROP_NAME_goodsName;
          PROP_NAME_TO_ID.put(PROP_NAME_goodsName, PROP_ID_goodsName);
      
          PROP_ID_TO_NAME[PROP_ID_picUrl] = PROP_NAME_picUrl;
          PROP_NAME_TO_ID.put(PROP_NAME_picUrl, PROP_ID_picUrl);
      
          PROP_ID_TO_NAME[PROP_ID_discount] = PROP_NAME_discount;
          PROP_NAME_TO_ID.put(PROP_NAME_discount, PROP_ID_discount);
      
          PROP_ID_TO_NAME[PROP_ID_discountMember] = PROP_NAME_discountMember;
          PROP_NAME_TO_ID.put(PROP_NAME_discountMember, PROP_ID_discountMember);
      
          PROP_ID_TO_NAME[PROP_ID_expireTime] = PROP_NAME_expireTime;
          PROP_NAME_TO_ID.put(PROP_NAME_expireTime, PROP_ID_expireTime);
      
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
    private java.lang.Integer _id;
    
    /* 商品ID: GOODS_ID */
    private java.lang.Integer _goodsId;
    
    /* 商品名称: GOODS_NAME */
    private java.lang.String _goodsName;
    
    /* 商品/货品图片: PIC_URL */
    private java.lang.String _picUrl;
    
    /* 优惠金额: DISCOUNT */
    private java.math.BigDecimal _discount;
    
    /* 达到优惠条件的人数: DISCOUNT_MEMBER */
    private java.lang.Integer _discountMember;
    
    /* 团购过期时间: EXPIRE_TIME */
    private java.time.LocalDateTime _expireTime;
    
    /* 团购规则状态，正常上线则0，到期自动下线则1，管理手动下线则2: STATUS */
    private java.lang.Short _status;
    
    /* 创建时间: ADD_TIME */
    private java.time.LocalDateTime _addTime;
    
    /* 更新时间: UPDATE_TIME */
    private java.time.LocalDateTime _updateTime;
    
    /* 逻辑删除: DELETED */
    private java.lang.Boolean _deleted;
    

    public _LitemallGrouponRules(){
        // for debug
    }

    protected LitemallGrouponRules newInstance(){
        LitemallGrouponRules entity = new LitemallGrouponRules();
        entity.orm_attach(orm_enhancer());
        entity.orm_entityModel(orm_entityModel());
        return entity;
    }

    @Override
    public LitemallGrouponRules cloneInstance() {
        LitemallGrouponRules entity = newInstance();
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
      return "app.mall.dao.entity.LitemallGrouponRules";
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
        
            case PROP_ID_goodsName:
               return getGoodsName();
        
            case PROP_ID_picUrl:
               return getPicUrl();
        
            case PROP_ID_discount:
               return getDiscount();
        
            case PROP_ID_discountMember:
               return getDiscountMember();
        
            case PROP_ID_expireTime:
               return getExpireTime();
        
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
        
            case PROP_ID_goodsName:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_goodsName));
               }
               setGoodsName(typedValue);
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
        
            case PROP_ID_discount:{
               java.math.BigDecimal typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toBigDecimal(value,
                       err-> newTypeConversionError(PROP_NAME_discount));
               }
               setDiscount(typedValue);
               break;
            }
        
            case PROP_ID_discountMember:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_discountMember));
               }
               setDiscountMember(typedValue);
               break;
            }
        
            case PROP_ID_expireTime:{
               java.time.LocalDateTime typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toLocalDateTime(value,
                       err-> newTypeConversionError(PROP_NAME_expireTime));
               }
               setExpireTime(typedValue);
               break;
            }
        
            case PROP_ID_status:{
               java.lang.Short typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toShort(value,
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
               this._id = (java.lang.Integer)value;
               orm_id(); // 如果是设置主键字段，则触发watcher
               break;
            }
        
            case PROP_ID_goodsId:{
               onInitProp(propId);
               this._goodsId = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_goodsName:{
               onInitProp(propId);
               this._goodsName = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_picUrl:{
               onInitProp(propId);
               this._picUrl = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_discount:{
               onInitProp(propId);
               this._discount = (java.math.BigDecimal)value;
               
               break;
            }
        
            case PROP_ID_discountMember:{
               onInitProp(propId);
               this._discountMember = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_expireTime:{
               onInitProp(propId);
               this._expireTime = (java.time.LocalDateTime)value;
               
               break;
            }
        
            case PROP_ID_status:{
               onInitProp(propId);
               this._status = (java.lang.Short)value;
               
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
     * 商品名称: GOODS_NAME
     */
    public java.lang.String getGoodsName(){
         onPropGet(PROP_ID_goodsName);
         return _goodsName;
    }

    /**
     * 商品名称: GOODS_NAME
     */
    public void setGoodsName(java.lang.String value){
        if(onPropSet(PROP_ID_goodsName,value)){
            this._goodsName = value;
            internalClearRefs(PROP_ID_goodsName);
            
        }
    }
    
    /**
     * 商品/货品图片: PIC_URL
     */
    public java.lang.String getPicUrl(){
         onPropGet(PROP_ID_picUrl);
         return _picUrl;
    }

    /**
     * 商品/货品图片: PIC_URL
     */
    public void setPicUrl(java.lang.String value){
        if(onPropSet(PROP_ID_picUrl,value)){
            this._picUrl = value;
            internalClearRefs(PROP_ID_picUrl);
            
        }
    }
    
    /**
     * 优惠金额: DISCOUNT
     */
    public java.math.BigDecimal getDiscount(){
         onPropGet(PROP_ID_discount);
         return _discount;
    }

    /**
     * 优惠金额: DISCOUNT
     */
    public void setDiscount(java.math.BigDecimal value){
        if(onPropSet(PROP_ID_discount,value)){
            this._discount = value;
            internalClearRefs(PROP_ID_discount);
            
        }
    }
    
    /**
     * 达到优惠条件的人数: DISCOUNT_MEMBER
     */
    public java.lang.Integer getDiscountMember(){
         onPropGet(PROP_ID_discountMember);
         return _discountMember;
    }

    /**
     * 达到优惠条件的人数: DISCOUNT_MEMBER
     */
    public void setDiscountMember(java.lang.Integer value){
        if(onPropSet(PROP_ID_discountMember,value)){
            this._discountMember = value;
            internalClearRefs(PROP_ID_discountMember);
            
        }
    }
    
    /**
     * 团购过期时间: EXPIRE_TIME
     */
    public java.time.LocalDateTime getExpireTime(){
         onPropGet(PROP_ID_expireTime);
         return _expireTime;
    }

    /**
     * 团购过期时间: EXPIRE_TIME
     */
    public void setExpireTime(java.time.LocalDateTime value){
        if(onPropSet(PROP_ID_expireTime,value)){
            this._expireTime = value;
            internalClearRefs(PROP_ID_expireTime);
            
        }
    }
    
    /**
     * 团购规则状态，正常上线则0，到期自动下线则1，管理手动下线则2: STATUS
     */
    public java.lang.Short getStatus(){
         onPropGet(PROP_ID_status);
         return _status;
    }

    /**
     * 团购规则状态，正常上线则0，到期自动下线则1，管理手动下线则2: STATUS
     */
    public void setStatus(java.lang.Short value){
        if(onPropSet(PROP_ID_status,value)){
            this._status = value;
            internalClearRefs(PROP_ID_status);
            
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
     * 订单
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
