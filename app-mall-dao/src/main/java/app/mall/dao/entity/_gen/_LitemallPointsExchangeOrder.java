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

import app.mall.dao.entity.LitemallPointsExchangeOrder;

// tell cpd to start ignoring code - CPD-OFF
/**
 *  积分兑换订单表: litemall_points_exchange_order
 */
@SuppressWarnings({"PMD.UselessOverridingMethod","PMD.UnusedLocalVariable","java:S3008","java:S1602","java:S1128","java:S1161",
        "PMD.UnnecessaryFullyQualifiedName","PMD.EmptyControlStatement","java:S116","java:S115","java:S101","java:S3776"})
public class _LitemallPointsExchangeOrder extends DynamicOrmEntity{
    
    /* Id: ID INTEGER */
    public static final String PROP_NAME_id = "id";
    public static final int PROP_ID_id = 1;
    
    /* 用户ID: USER_ID VARCHAR */
    public static final String PROP_NAME_userId = "userId";
    public static final int PROP_ID_userId = 2;
    
    /* 积分商品ID: POINTS_GOODS_ID INTEGER */
    public static final String PROP_NAME_pointsGoodsId = "pointsGoodsId";
    public static final int PROP_ID_pointsGoodsId = 3;
    
    /* 商品ID: GOODS_ID INTEGER */
    public static final String PROP_NAME_goodsId = "goodsId";
    public static final int PROP_ID_goodsId = 4;
    
    /* SKU ID: PRODUCT_ID INTEGER */
    public static final String PROP_NAME_productId = "productId";
    public static final int PROP_ID_productId = 5;
    
    /* 商品名称: GOODS_NAME VARCHAR */
    public static final String PROP_NAME_goodsName = "goodsName";
    public static final int PROP_ID_goodsName = 6;
    
    /* 商品图片: PIC_URL VARCHAR */
    public static final String PROP_NAME_picUrl = "picUrl";
    public static final int PROP_ID_picUrl = 7;
    
    /* 积分单价: POINTS_PRICE INTEGER */
    public static final String PROP_NAME_pointsPrice = "pointsPrice";
    public static final int PROP_ID_pointsPrice = 8;
    
    /* 兑换数量: QUANTITY INTEGER */
    public static final String PROP_NAME_quantity = "quantity";
    public static final int PROP_ID_quantity = 9;
    
    /* 消耗总积分: TOTAL_POINTS INTEGER */
    public static final String PROP_NAME_totalPoints = "totalPoints";
    public static final int PROP_ID_totalPoints = 10;
    
    /* 收货地址ID: ADDRESS_ID INTEGER */
    public static final String PROP_NAME_addressId = "addressId";
    public static final int PROP_ID_addressId = 11;
    
    /* 收货人: CONSIGNEE VARCHAR */
    public static final String PROP_NAME_consignee = "consignee";
    public static final int PROP_ID_consignee = 12;
    
    /* 联系电话: PHONE VARCHAR */
    public static final String PROP_NAME_phone = "phone";
    public static final int PROP_ID_phone = 13;
    
    /* 完整收货地址: FULL_ADDRESS VARCHAR */
    public static final String PROP_NAME_fullAddress = "fullAddress";
    public static final int PROP_ID_fullAddress = 14;
    
    /* 兑换状态: EXCHANGE_STATUS INTEGER */
    public static final String PROP_NAME_exchangeStatus = "exchangeStatus";
    public static final int PROP_ID_exchangeStatus = 15;
    
    /* 物流单号: SHIP_CODE VARCHAR */
    public static final String PROP_NAME_shipCode = "shipCode";
    public static final int PROP_ID_shipCode = 16;
    
    /* 备注: REMARK VARCHAR */
    public static final String PROP_NAME_remark = "remark";
    public static final int PROP_ID_remark = 17;
    
    /* 创建时间: ADD_TIME DATETIME */
    public static final String PROP_NAME_addTime = "addTime";
    public static final int PROP_ID_addTime = 18;
    
    /* 更新时间: UPDATE_TIME DATETIME */
    public static final String PROP_NAME_updateTime = "updateTime";
    public static final int PROP_ID_updateTime = 19;
    
    /* 逻辑删除: DELETED BOOLEAN */
    public static final String PROP_NAME_deleted = "deleted";
    public static final int PROP_ID_deleted = 20;
    
    /* 支付状态（组合兑换现金部分）: PAY_STATUS INTEGER */
    public static final String PROP_NAME_payStatus = "payStatus";
    public static final int PROP_ID_payStatus = 21;
    
    /* 支付通道（组合兑换现金部分）: PAY_CHANNEL INTEGER */
    public static final String PROP_NAME_payChannel = "payChannel";
    public static final int PROP_ID_payChannel = 22;
    
    /* 现金单价快照（组合兑换）: CASH_PRICE DECIMAL */
    public static final String PROP_NAME_cashPrice = "cashPrice";
    public static final int PROP_ID_cashPrice = 23;
    
    /* 余额支付金额（组合兑换现金部分）: WALLET_PAY_AMOUNT DECIMAL */
    public static final String PROP_NAME_walletPayAmount = "walletPayAmount";
    public static final int PROP_ID_walletPayAmount = 24;
    

    private static int _PROP_ID_BOUND = 25;

    
    /* relation: 积分商品 */
    public static final String PROP_NAME_pointsGoods = "pointsGoods";
    

    protected static final List<String> PK_PROP_NAMES = Arrays.asList(PROP_NAME_id);
    protected static final int[] PK_PROP_IDS = new int[]{PROP_ID_id};

    private static final String[] PROP_ID_TO_NAME = new String[25];
    private static final Map<String,Integer> PROP_NAME_TO_ID = new HashMap<>();
    static{
      
          PROP_ID_TO_NAME[PROP_ID_id] = PROP_NAME_id;
          PROP_NAME_TO_ID.put(PROP_NAME_id, PROP_ID_id);
      
          PROP_ID_TO_NAME[PROP_ID_userId] = PROP_NAME_userId;
          PROP_NAME_TO_ID.put(PROP_NAME_userId, PROP_ID_userId);
      
          PROP_ID_TO_NAME[PROP_ID_pointsGoodsId] = PROP_NAME_pointsGoodsId;
          PROP_NAME_TO_ID.put(PROP_NAME_pointsGoodsId, PROP_ID_pointsGoodsId);
      
          PROP_ID_TO_NAME[PROP_ID_goodsId] = PROP_NAME_goodsId;
          PROP_NAME_TO_ID.put(PROP_NAME_goodsId, PROP_ID_goodsId);
      
          PROP_ID_TO_NAME[PROP_ID_productId] = PROP_NAME_productId;
          PROP_NAME_TO_ID.put(PROP_NAME_productId, PROP_ID_productId);
      
          PROP_ID_TO_NAME[PROP_ID_goodsName] = PROP_NAME_goodsName;
          PROP_NAME_TO_ID.put(PROP_NAME_goodsName, PROP_ID_goodsName);
      
          PROP_ID_TO_NAME[PROP_ID_picUrl] = PROP_NAME_picUrl;
          PROP_NAME_TO_ID.put(PROP_NAME_picUrl, PROP_ID_picUrl);
      
          PROP_ID_TO_NAME[PROP_ID_pointsPrice] = PROP_NAME_pointsPrice;
          PROP_NAME_TO_ID.put(PROP_NAME_pointsPrice, PROP_ID_pointsPrice);
      
          PROP_ID_TO_NAME[PROP_ID_quantity] = PROP_NAME_quantity;
          PROP_NAME_TO_ID.put(PROP_NAME_quantity, PROP_ID_quantity);
      
          PROP_ID_TO_NAME[PROP_ID_totalPoints] = PROP_NAME_totalPoints;
          PROP_NAME_TO_ID.put(PROP_NAME_totalPoints, PROP_ID_totalPoints);
      
          PROP_ID_TO_NAME[PROP_ID_addressId] = PROP_NAME_addressId;
          PROP_NAME_TO_ID.put(PROP_NAME_addressId, PROP_ID_addressId);
      
          PROP_ID_TO_NAME[PROP_ID_consignee] = PROP_NAME_consignee;
          PROP_NAME_TO_ID.put(PROP_NAME_consignee, PROP_ID_consignee);
      
          PROP_ID_TO_NAME[PROP_ID_phone] = PROP_NAME_phone;
          PROP_NAME_TO_ID.put(PROP_NAME_phone, PROP_ID_phone);
      
          PROP_ID_TO_NAME[PROP_ID_fullAddress] = PROP_NAME_fullAddress;
          PROP_NAME_TO_ID.put(PROP_NAME_fullAddress, PROP_ID_fullAddress);
      
          PROP_ID_TO_NAME[PROP_ID_exchangeStatus] = PROP_NAME_exchangeStatus;
          PROP_NAME_TO_ID.put(PROP_NAME_exchangeStatus, PROP_ID_exchangeStatus);
      
          PROP_ID_TO_NAME[PROP_ID_shipCode] = PROP_NAME_shipCode;
          PROP_NAME_TO_ID.put(PROP_NAME_shipCode, PROP_ID_shipCode);
      
          PROP_ID_TO_NAME[PROP_ID_remark] = PROP_NAME_remark;
          PROP_NAME_TO_ID.put(PROP_NAME_remark, PROP_ID_remark);
      
          PROP_ID_TO_NAME[PROP_ID_addTime] = PROP_NAME_addTime;
          PROP_NAME_TO_ID.put(PROP_NAME_addTime, PROP_ID_addTime);
      
          PROP_ID_TO_NAME[PROP_ID_updateTime] = PROP_NAME_updateTime;
          PROP_NAME_TO_ID.put(PROP_NAME_updateTime, PROP_ID_updateTime);
      
          PROP_ID_TO_NAME[PROP_ID_deleted] = PROP_NAME_deleted;
          PROP_NAME_TO_ID.put(PROP_NAME_deleted, PROP_ID_deleted);
      
          PROP_ID_TO_NAME[PROP_ID_payStatus] = PROP_NAME_payStatus;
          PROP_NAME_TO_ID.put(PROP_NAME_payStatus, PROP_ID_payStatus);
      
          PROP_ID_TO_NAME[PROP_ID_payChannel] = PROP_NAME_payChannel;
          PROP_NAME_TO_ID.put(PROP_NAME_payChannel, PROP_ID_payChannel);
      
          PROP_ID_TO_NAME[PROP_ID_cashPrice] = PROP_NAME_cashPrice;
          PROP_NAME_TO_ID.put(PROP_NAME_cashPrice, PROP_ID_cashPrice);
      
          PROP_ID_TO_NAME[PROP_ID_walletPayAmount] = PROP_NAME_walletPayAmount;
          PROP_NAME_TO_ID.put(PROP_NAME_walletPayAmount, PROP_ID_walletPayAmount);
      
    }

    
    /* Id: ID */
    private java.lang.String _id;
    
    /* 用户ID: USER_ID */
    private java.lang.String _userId;
    
    /* 积分商品ID: POINTS_GOODS_ID */
    private java.lang.String _pointsGoodsId;
    
    /* 商品ID: GOODS_ID */
    private java.lang.String _goodsId;
    
    /* SKU ID: PRODUCT_ID */
    private java.lang.String _productId;
    
    /* 商品名称: GOODS_NAME */
    private java.lang.String _goodsName;
    
    /* 商品图片: PIC_URL */
    private java.lang.String _picUrl;
    
    /* 积分单价: POINTS_PRICE */
    private java.lang.Integer _pointsPrice;
    
    /* 兑换数量: QUANTITY */
    private java.lang.Integer _quantity;
    
    /* 消耗总积分: TOTAL_POINTS */
    private java.lang.Integer _totalPoints;
    
    /* 收货地址ID: ADDRESS_ID */
    private java.lang.String _addressId;
    
    /* 收货人: CONSIGNEE */
    private java.lang.String _consignee;
    
    /* 联系电话: PHONE */
    private java.lang.String _phone;
    
    /* 完整收货地址: FULL_ADDRESS */
    private java.lang.String _fullAddress;
    
    /* 兑换状态: EXCHANGE_STATUS */
    private java.lang.Integer _exchangeStatus;
    
    /* 物流单号: SHIP_CODE */
    private java.lang.String _shipCode;
    
    /* 备注: REMARK */
    private java.lang.String _remark;
    
    /* 创建时间: ADD_TIME */
    private java.time.LocalDateTime _addTime;
    
    /* 更新时间: UPDATE_TIME */
    private java.time.LocalDateTime _updateTime;
    
    /* 逻辑删除: DELETED */
    private java.lang.Boolean _deleted;
    
    /* 支付状态（组合兑换现金部分）: PAY_STATUS */
    private java.lang.Integer _payStatus;
    
    /* 支付通道（组合兑换现金部分）: PAY_CHANNEL */
    private java.lang.Integer _payChannel;
    
    /* 现金单价快照（组合兑换）: CASH_PRICE */
    private java.math.BigDecimal _cashPrice;
    
    /* 余额支付金额（组合兑换现金部分）: WALLET_PAY_AMOUNT */
    private java.math.BigDecimal _walletPayAmount;
    

    public _LitemallPointsExchangeOrder(){
        // for debug
    }

    protected LitemallPointsExchangeOrder newInstance(){
        LitemallPointsExchangeOrder entity = new LitemallPointsExchangeOrder();
        entity.orm_attach(orm_enhancer());
        entity.orm_entityModel(orm_entityModel());
        return entity;
    }

    @Override
    public LitemallPointsExchangeOrder cloneInstance() {
        LitemallPointsExchangeOrder entity = newInstance();
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
      return "app.mall.dao.entity.LitemallPointsExchangeOrder";
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
        
            case PROP_ID_userId:
               return getUserId();
        
            case PROP_ID_pointsGoodsId:
               return getPointsGoodsId();
        
            case PROP_ID_goodsId:
               return getGoodsId();
        
            case PROP_ID_productId:
               return getProductId();
        
            case PROP_ID_goodsName:
               return getGoodsName();
        
            case PROP_ID_picUrl:
               return getPicUrl();
        
            case PROP_ID_pointsPrice:
               return getPointsPrice();
        
            case PROP_ID_quantity:
               return getQuantity();
        
            case PROP_ID_totalPoints:
               return getTotalPoints();
        
            case PROP_ID_addressId:
               return getAddressId();
        
            case PROP_ID_consignee:
               return getConsignee();
        
            case PROP_ID_phone:
               return getPhone();
        
            case PROP_ID_fullAddress:
               return getFullAddress();
        
            case PROP_ID_exchangeStatus:
               return getExchangeStatus();
        
            case PROP_ID_shipCode:
               return getShipCode();
        
            case PROP_ID_remark:
               return getRemark();
        
            case PROP_ID_addTime:
               return getAddTime();
        
            case PROP_ID_updateTime:
               return getUpdateTime();
        
            case PROP_ID_deleted:
               return getDeleted();
        
            case PROP_ID_payStatus:
               return getPayStatus();
        
            case PROP_ID_payChannel:
               return getPayChannel();
        
            case PROP_ID_cashPrice:
               return getCashPrice();
        
            case PROP_ID_walletPayAmount:
               return getWalletPayAmount();
        
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
        
            case PROP_ID_userId:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_userId));
               }
               setUserId(typedValue);
               break;
            }
        
            case PROP_ID_pointsGoodsId:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_pointsGoodsId));
               }
               setPointsGoodsId(typedValue);
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
        
            case PROP_ID_pointsPrice:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_pointsPrice));
               }
               setPointsPrice(typedValue);
               break;
            }
        
            case PROP_ID_quantity:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_quantity));
               }
               setQuantity(typedValue);
               break;
            }
        
            case PROP_ID_totalPoints:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_totalPoints));
               }
               setTotalPoints(typedValue);
               break;
            }
        
            case PROP_ID_addressId:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_addressId));
               }
               setAddressId(typedValue);
               break;
            }
        
            case PROP_ID_consignee:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_consignee));
               }
               setConsignee(typedValue);
               break;
            }
        
            case PROP_ID_phone:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_phone));
               }
               setPhone(typedValue);
               break;
            }
        
            case PROP_ID_fullAddress:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_fullAddress));
               }
               setFullAddress(typedValue);
               break;
            }
        
            case PROP_ID_exchangeStatus:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_exchangeStatus));
               }
               setExchangeStatus(typedValue);
               break;
            }
        
            case PROP_ID_shipCode:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_shipCode));
               }
               setShipCode(typedValue);
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
        
            case PROP_ID_payStatus:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_payStatus));
               }
               setPayStatus(typedValue);
               break;
            }
        
            case PROP_ID_payChannel:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_payChannel));
               }
               setPayChannel(typedValue);
               break;
            }
        
            case PROP_ID_cashPrice:{
               java.math.BigDecimal typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toBigDecimal(value,
                       err-> newTypeConversionError(PROP_NAME_cashPrice));
               }
               setCashPrice(typedValue);
               break;
            }
        
            case PROP_ID_walletPayAmount:{
               java.math.BigDecimal typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toBigDecimal(value,
                       err-> newTypeConversionError(PROP_NAME_walletPayAmount));
               }
               setWalletPayAmount(typedValue);
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
        
            case PROP_ID_userId:{
               onInitProp(propId);
               this._userId = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_pointsGoodsId:{
               onInitProp(propId);
               this._pointsGoodsId = (java.lang.String)value;
               
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
        
            case PROP_ID_pointsPrice:{
               onInitProp(propId);
               this._pointsPrice = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_quantity:{
               onInitProp(propId);
               this._quantity = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_totalPoints:{
               onInitProp(propId);
               this._totalPoints = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_addressId:{
               onInitProp(propId);
               this._addressId = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_consignee:{
               onInitProp(propId);
               this._consignee = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_phone:{
               onInitProp(propId);
               this._phone = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_fullAddress:{
               onInitProp(propId);
               this._fullAddress = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_exchangeStatus:{
               onInitProp(propId);
               this._exchangeStatus = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_shipCode:{
               onInitProp(propId);
               this._shipCode = (java.lang.String)value;
               
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
        
            case PROP_ID_payStatus:{
               onInitProp(propId);
               this._payStatus = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_payChannel:{
               onInitProp(propId);
               this._payChannel = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_cashPrice:{
               onInitProp(propId);
               this._cashPrice = (java.math.BigDecimal)value;
               
               break;
            }
        
            case PROP_ID_walletPayAmount:{
               onInitProp(propId);
               this._walletPayAmount = (java.math.BigDecimal)value;
               
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
     * 用户ID: USER_ID
     */
    public final java.lang.String getUserId(){
         onPropGet(PROP_ID_userId);
         return _userId;
    }

    /**
     * 用户ID: USER_ID
     */
    public final void setUserId(java.lang.String value){
        if(onPropSet(PROP_ID_userId,value)){
            this._userId = value;
            internalClearRefs(PROP_ID_userId);
            
        }
    }
    
    /**
     * 积分商品ID: POINTS_GOODS_ID
     */
    public final java.lang.String getPointsGoodsId(){
         onPropGet(PROP_ID_pointsGoodsId);
         return _pointsGoodsId;
    }

    /**
     * 积分商品ID: POINTS_GOODS_ID
     */
    public final void setPointsGoodsId(java.lang.String value){
        if(onPropSet(PROP_ID_pointsGoodsId,value)){
            this._pointsGoodsId = value;
            internalClearRefs(PROP_ID_pointsGoodsId);
            
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
     * SKU ID: PRODUCT_ID
     */
    public final java.lang.String getProductId(){
         onPropGet(PROP_ID_productId);
         return _productId;
    }

    /**
     * SKU ID: PRODUCT_ID
     */
    public final void setProductId(java.lang.String value){
        if(onPropSet(PROP_ID_productId,value)){
            this._productId = value;
            internalClearRefs(PROP_ID_productId);
            
        }
    }
    
    /**
     * 商品名称: GOODS_NAME
     */
    public final java.lang.String getGoodsName(){
         onPropGet(PROP_ID_goodsName);
         return _goodsName;
    }

    /**
     * 商品名称: GOODS_NAME
     */
    public final void setGoodsName(java.lang.String value){
        if(onPropSet(PROP_ID_goodsName,value)){
            this._goodsName = value;
            internalClearRefs(PROP_ID_goodsName);
            
        }
    }
    
    /**
     * 商品图片: PIC_URL
     */
    public final java.lang.String getPicUrl(){
         onPropGet(PROP_ID_picUrl);
         return _picUrl;
    }

    /**
     * 商品图片: PIC_URL
     */
    public final void setPicUrl(java.lang.String value){
        if(onPropSet(PROP_ID_picUrl,value)){
            this._picUrl = value;
            internalClearRefs(PROP_ID_picUrl);
            
        }
    }
    
    /**
     * 积分单价: POINTS_PRICE
     */
    public final java.lang.Integer getPointsPrice(){
         onPropGet(PROP_ID_pointsPrice);
         return _pointsPrice;
    }

    /**
     * 积分单价: POINTS_PRICE
     */
    public final void setPointsPrice(java.lang.Integer value){
        if(onPropSet(PROP_ID_pointsPrice,value)){
            this._pointsPrice = value;
            internalClearRefs(PROP_ID_pointsPrice);
            
        }
    }
    
    /**
     * 兑换数量: QUANTITY
     */
    public final java.lang.Integer getQuantity(){
         onPropGet(PROP_ID_quantity);
         return _quantity;
    }

    /**
     * 兑换数量: QUANTITY
     */
    public final void setQuantity(java.lang.Integer value){
        if(onPropSet(PROP_ID_quantity,value)){
            this._quantity = value;
            internalClearRefs(PROP_ID_quantity);
            
        }
    }
    
    /**
     * 消耗总积分: TOTAL_POINTS
     */
    public final java.lang.Integer getTotalPoints(){
         onPropGet(PROP_ID_totalPoints);
         return _totalPoints;
    }

    /**
     * 消耗总积分: TOTAL_POINTS
     */
    public final void setTotalPoints(java.lang.Integer value){
        if(onPropSet(PROP_ID_totalPoints,value)){
            this._totalPoints = value;
            internalClearRefs(PROP_ID_totalPoints);
            
        }
    }
    
    /**
     * 收货地址ID: ADDRESS_ID
     */
    public final java.lang.String getAddressId(){
         onPropGet(PROP_ID_addressId);
         return _addressId;
    }

    /**
     * 收货地址ID: ADDRESS_ID
     */
    public final void setAddressId(java.lang.String value){
        if(onPropSet(PROP_ID_addressId,value)){
            this._addressId = value;
            internalClearRefs(PROP_ID_addressId);
            
        }
    }
    
    /**
     * 收货人: CONSIGNEE
     */
    public final java.lang.String getConsignee(){
         onPropGet(PROP_ID_consignee);
         return _consignee;
    }

    /**
     * 收货人: CONSIGNEE
     */
    public final void setConsignee(java.lang.String value){
        if(onPropSet(PROP_ID_consignee,value)){
            this._consignee = value;
            internalClearRefs(PROP_ID_consignee);
            
        }
    }
    
    /**
     * 联系电话: PHONE
     */
    public final java.lang.String getPhone(){
         onPropGet(PROP_ID_phone);
         return _phone;
    }

    /**
     * 联系电话: PHONE
     */
    public final void setPhone(java.lang.String value){
        if(onPropSet(PROP_ID_phone,value)){
            this._phone = value;
            internalClearRefs(PROP_ID_phone);
            
        }
    }
    
    /**
     * 完整收货地址: FULL_ADDRESS
     */
    public final java.lang.String getFullAddress(){
         onPropGet(PROP_ID_fullAddress);
         return _fullAddress;
    }

    /**
     * 完整收货地址: FULL_ADDRESS
     */
    public final void setFullAddress(java.lang.String value){
        if(onPropSet(PROP_ID_fullAddress,value)){
            this._fullAddress = value;
            internalClearRefs(PROP_ID_fullAddress);
            
        }
    }
    
    /**
     * 兑换状态: EXCHANGE_STATUS
     */
    public final java.lang.Integer getExchangeStatus(){
         onPropGet(PROP_ID_exchangeStatus);
         return _exchangeStatus;
    }

    /**
     * 兑换状态: EXCHANGE_STATUS
     */
    public final void setExchangeStatus(java.lang.Integer value){
        if(onPropSet(PROP_ID_exchangeStatus,value)){
            this._exchangeStatus = value;
            internalClearRefs(PROP_ID_exchangeStatus);
            
        }
    }
    
    /**
     * 物流单号: SHIP_CODE
     */
    public final java.lang.String getShipCode(){
         onPropGet(PROP_ID_shipCode);
         return _shipCode;
    }

    /**
     * 物流单号: SHIP_CODE
     */
    public final void setShipCode(java.lang.String value){
        if(onPropSet(PROP_ID_shipCode,value)){
            this._shipCode = value;
            internalClearRefs(PROP_ID_shipCode);
            
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
     * 支付状态（组合兑换现金部分）: PAY_STATUS
     */
    public final java.lang.Integer getPayStatus(){
         onPropGet(PROP_ID_payStatus);
         return _payStatus;
    }

    /**
     * 支付状态（组合兑换现金部分）: PAY_STATUS
     */
    public final void setPayStatus(java.lang.Integer value){
        if(onPropSet(PROP_ID_payStatus,value)){
            this._payStatus = value;
            internalClearRefs(PROP_ID_payStatus);
            
        }
    }
    
    /**
     * 支付通道（组合兑换现金部分）: PAY_CHANNEL
     */
    public final java.lang.Integer getPayChannel(){
         onPropGet(PROP_ID_payChannel);
         return _payChannel;
    }

    /**
     * 支付通道（组合兑换现金部分）: PAY_CHANNEL
     */
    public final void setPayChannel(java.lang.Integer value){
        if(onPropSet(PROP_ID_payChannel,value)){
            this._payChannel = value;
            internalClearRefs(PROP_ID_payChannel);
            
        }
    }
    
    /**
     * 现金单价快照（组合兑换）: CASH_PRICE
     */
    public final java.math.BigDecimal getCashPrice(){
         onPropGet(PROP_ID_cashPrice);
         return _cashPrice;
    }

    /**
     * 现金单价快照（组合兑换）: CASH_PRICE
     */
    public final void setCashPrice(java.math.BigDecimal value){
        if(onPropSet(PROP_ID_cashPrice,value)){
            this._cashPrice = value;
            internalClearRefs(PROP_ID_cashPrice);
            
        }
    }
    
    /**
     * 余额支付金额（组合兑换现金部分）: WALLET_PAY_AMOUNT
     */
    public final java.math.BigDecimal getWalletPayAmount(){
         onPropGet(PROP_ID_walletPayAmount);
         return _walletPayAmount;
    }

    /**
     * 余额支付金额（组合兑换现金部分）: WALLET_PAY_AMOUNT
     */
    public final void setWalletPayAmount(java.math.BigDecimal value){
        if(onPropSet(PROP_ID_walletPayAmount,value)){
            this._walletPayAmount = value;
            internalClearRefs(PROP_ID_walletPayAmount);
            
        }
    }
    
    /**
     * 积分商品
     */
    public final app.mall.dao.entity.LitemallPointsGoods getPointsGoods(){
       return (app.mall.dao.entity.LitemallPointsGoods)internalGetRefEntity(PROP_NAME_pointsGoods);
    }

    public final void setPointsGoods(app.mall.dao.entity.LitemallPointsGoods refEntity){
   
           if(refEntity == null){
           
                   this.setPointsGoodsId(null);
               
           }else{
           internalSetRefEntity(PROP_NAME_pointsGoods, refEntity,()->{
           
                           this.setPointsGoodsId(refEntity.getId());
                       
           });
           }
       
    }
       
}
// resume CPD analysis - CPD-ON
