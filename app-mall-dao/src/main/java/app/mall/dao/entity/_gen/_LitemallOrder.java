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

import app.mall.dao.entity.LitemallOrder;

// tell cpd to start ignoring code - CPD-OFF
/**
 *  订单表: litemall_order
 */
@SuppressWarnings({"PMD.UselessOverridingMethod","PMD.UnusedLocalVariable",
        "PMD.UnnecessaryFullyQualifiedName","PMD.EmptyControlStatement"})
public class _LitemallOrder extends DynamicOrmEntity{
    
    /* Id: ID INTEGER */
    public static final String PROP_NAME_id = "id";
    public static final int PROP_ID_id = 1;
    
    /* 用户ID: USER_ID INTEGER */
    public static final String PROP_NAME_userId = "userId";
    public static final int PROP_ID_userId = 2;
    
    /* 订单编号: ORDER_SN VARCHAR */
    public static final String PROP_NAME_orderSn = "orderSn";
    public static final int PROP_ID_orderSn = 3;
    
    /* 订单状态: ORDER_STATUS SMALLINT */
    public static final String PROP_NAME_orderStatus = "orderStatus";
    public static final int PROP_ID_orderStatus = 4;
    
    /* 售后状态: AFTERSALE_STATUS SMALLINT */
    public static final String PROP_NAME_aftersaleStatus = "aftersaleStatus";
    public static final int PROP_ID_aftersaleStatus = 5;
    
    /* 收货人名称: CONSIGNEE VARCHAR */
    public static final String PROP_NAME_consignee = "consignee";
    public static final int PROP_ID_consignee = 6;
    
    /* 收货人手机号: MOBILE VARCHAR */
    public static final String PROP_NAME_mobile = "mobile";
    public static final int PROP_ID_mobile = 7;
    
    /* 收货具体地址: ADDRESS VARCHAR */
    public static final String PROP_NAME_address = "address";
    public static final int PROP_ID_address = 8;
    
    /* 用户订单留言: MESSAGE VARCHAR */
    public static final String PROP_NAME_message = "message";
    public static final int PROP_ID_message = 9;
    
    /* 商品总费用: GOODS_PRICE DECIMAL */
    public static final String PROP_NAME_goodsPrice = "goodsPrice";
    public static final int PROP_ID_goodsPrice = 10;
    
    /* 配送费用: FREIGHT_PRICE DECIMAL */
    public static final String PROP_NAME_freightPrice = "freightPrice";
    public static final int PROP_ID_freightPrice = 11;
    
    /* 优惠券减免: COUPON_PRICE DECIMAL */
    public static final String PROP_NAME_couponPrice = "couponPrice";
    public static final int PROP_ID_couponPrice = 12;
    
    /* 用户积分减免: INTEGRAL_PRICE DECIMAL */
    public static final String PROP_NAME_integralPrice = "integralPrice";
    public static final int PROP_ID_integralPrice = 13;
    
    /* 团购优惠价减免: GROUPON_PRICE DECIMAL */
    public static final String PROP_NAME_grouponPrice = "grouponPrice";
    public static final int PROP_ID_grouponPrice = 14;
    
    /* 订单费用: ORDER_PRICE DECIMAL */
    public static final String PROP_NAME_orderPrice = "orderPrice";
    public static final int PROP_ID_orderPrice = 15;
    
    /* 实付费用: ACTUAL_PRICE DECIMAL */
    public static final String PROP_NAME_actualPrice = "actualPrice";
    public static final int PROP_ID_actualPrice = 16;
    
    /* 微信付款编号: PAY_ID VARCHAR */
    public static final String PROP_NAME_payId = "payId";
    public static final int PROP_ID_payId = 17;
    
    /* 微信付款时间: PAY_TIME DATETIME */
    public static final String PROP_NAME_payTime = "payTime";
    public static final int PROP_ID_payTime = 18;
    
    /* 发货编号: SHIP_SN VARCHAR */
    public static final String PROP_NAME_shipSn = "shipSn";
    public static final int PROP_ID_shipSn = 19;
    
    /* 发货快递公司: SHIP_CHANNEL VARCHAR */
    public static final String PROP_NAME_shipChannel = "shipChannel";
    public static final int PROP_ID_shipChannel = 20;
    
    /* 发货开始时间: SHIP_TIME DATETIME */
    public static final String PROP_NAME_shipTime = "shipTime";
    public static final int PROP_ID_shipTime = 21;
    
    /* 实际退款金额: REFUND_AMOUNT DECIMAL */
    public static final String PROP_NAME_refundAmount = "refundAmount";
    public static final int PROP_ID_refundAmount = 22;
    
    /* 退款方式: REFUND_TYPE VARCHAR */
    public static final String PROP_NAME_refundType = "refundType";
    public static final int PROP_ID_refundType = 23;
    
    /* 退款备注: REFUND_CONTENT VARCHAR */
    public static final String PROP_NAME_refundContent = "refundContent";
    public static final int PROP_ID_refundContent = 24;
    
    /* 退款时间: REFUND_TIME DATETIME */
    public static final String PROP_NAME_refundTime = "refundTime";
    public static final int PROP_ID_refundTime = 25;
    
    /* 用户确认收货时间: CONFIRM_TIME DATETIME */
    public static final String PROP_NAME_confirmTime = "confirmTime";
    public static final int PROP_ID_confirmTime = 26;
    
    /* 待评价订单商品数量: COMMENTS SMALLINT */
    public static final String PROP_NAME_comments = "comments";
    public static final int PROP_ID_comments = 27;
    
    /* 订单关闭时间: END_TIME DATETIME */
    public static final String PROP_NAME_endTime = "endTime";
    public static final int PROP_ID_endTime = 28;
    
    /* 创建时间: ADD_TIME DATETIME */
    public static final String PROP_NAME_addTime = "addTime";
    public static final int PROP_ID_addTime = 29;
    
    /* 更新时间: UPDATE_TIME DATETIME */
    public static final String PROP_NAME_updateTime = "updateTime";
    public static final int PROP_ID_updateTime = 30;
    
    /* 逻辑删除: DELETED BOOLEAN */
    public static final String PROP_NAME_deleted = "deleted";
    public static final int PROP_ID_deleted = 31;
    

    private static int _PROP_ID_BOUND = 32;

    
    /* relation: 客户 */
    public static final String PROP_NAME_user = "user";
    

    public static final List<String> PK_PROP_NAMES = Arrays.asList(PROP_NAME_id);
    public static final int[] PK_PROP_IDS = new int[]{PROP_ID_id};

    private static final String[] PROP_ID_TO_NAME = new String[32];
    private static final Map<String,Integer> PROP_NAME_TO_ID = new HashMap<>();
    static{
      
          PROP_ID_TO_NAME[PROP_ID_id] = PROP_NAME_id;
          PROP_NAME_TO_ID.put(PROP_NAME_id, PROP_ID_id);
      
          PROP_ID_TO_NAME[PROP_ID_userId] = PROP_NAME_userId;
          PROP_NAME_TO_ID.put(PROP_NAME_userId, PROP_ID_userId);
      
          PROP_ID_TO_NAME[PROP_ID_orderSn] = PROP_NAME_orderSn;
          PROP_NAME_TO_ID.put(PROP_NAME_orderSn, PROP_ID_orderSn);
      
          PROP_ID_TO_NAME[PROP_ID_orderStatus] = PROP_NAME_orderStatus;
          PROP_NAME_TO_ID.put(PROP_NAME_orderStatus, PROP_ID_orderStatus);
      
          PROP_ID_TO_NAME[PROP_ID_aftersaleStatus] = PROP_NAME_aftersaleStatus;
          PROP_NAME_TO_ID.put(PROP_NAME_aftersaleStatus, PROP_ID_aftersaleStatus);
      
          PROP_ID_TO_NAME[PROP_ID_consignee] = PROP_NAME_consignee;
          PROP_NAME_TO_ID.put(PROP_NAME_consignee, PROP_ID_consignee);
      
          PROP_ID_TO_NAME[PROP_ID_mobile] = PROP_NAME_mobile;
          PROP_NAME_TO_ID.put(PROP_NAME_mobile, PROP_ID_mobile);
      
          PROP_ID_TO_NAME[PROP_ID_address] = PROP_NAME_address;
          PROP_NAME_TO_ID.put(PROP_NAME_address, PROP_ID_address);
      
          PROP_ID_TO_NAME[PROP_ID_message] = PROP_NAME_message;
          PROP_NAME_TO_ID.put(PROP_NAME_message, PROP_ID_message);
      
          PROP_ID_TO_NAME[PROP_ID_goodsPrice] = PROP_NAME_goodsPrice;
          PROP_NAME_TO_ID.put(PROP_NAME_goodsPrice, PROP_ID_goodsPrice);
      
          PROP_ID_TO_NAME[PROP_ID_freightPrice] = PROP_NAME_freightPrice;
          PROP_NAME_TO_ID.put(PROP_NAME_freightPrice, PROP_ID_freightPrice);
      
          PROP_ID_TO_NAME[PROP_ID_couponPrice] = PROP_NAME_couponPrice;
          PROP_NAME_TO_ID.put(PROP_NAME_couponPrice, PROP_ID_couponPrice);
      
          PROP_ID_TO_NAME[PROP_ID_integralPrice] = PROP_NAME_integralPrice;
          PROP_NAME_TO_ID.put(PROP_NAME_integralPrice, PROP_ID_integralPrice);
      
          PROP_ID_TO_NAME[PROP_ID_grouponPrice] = PROP_NAME_grouponPrice;
          PROP_NAME_TO_ID.put(PROP_NAME_grouponPrice, PROP_ID_grouponPrice);
      
          PROP_ID_TO_NAME[PROP_ID_orderPrice] = PROP_NAME_orderPrice;
          PROP_NAME_TO_ID.put(PROP_NAME_orderPrice, PROP_ID_orderPrice);
      
          PROP_ID_TO_NAME[PROP_ID_actualPrice] = PROP_NAME_actualPrice;
          PROP_NAME_TO_ID.put(PROP_NAME_actualPrice, PROP_ID_actualPrice);
      
          PROP_ID_TO_NAME[PROP_ID_payId] = PROP_NAME_payId;
          PROP_NAME_TO_ID.put(PROP_NAME_payId, PROP_ID_payId);
      
          PROP_ID_TO_NAME[PROP_ID_payTime] = PROP_NAME_payTime;
          PROP_NAME_TO_ID.put(PROP_NAME_payTime, PROP_ID_payTime);
      
          PROP_ID_TO_NAME[PROP_ID_shipSn] = PROP_NAME_shipSn;
          PROP_NAME_TO_ID.put(PROP_NAME_shipSn, PROP_ID_shipSn);
      
          PROP_ID_TO_NAME[PROP_ID_shipChannel] = PROP_NAME_shipChannel;
          PROP_NAME_TO_ID.put(PROP_NAME_shipChannel, PROP_ID_shipChannel);
      
          PROP_ID_TO_NAME[PROP_ID_shipTime] = PROP_NAME_shipTime;
          PROP_NAME_TO_ID.put(PROP_NAME_shipTime, PROP_ID_shipTime);
      
          PROP_ID_TO_NAME[PROP_ID_refundAmount] = PROP_NAME_refundAmount;
          PROP_NAME_TO_ID.put(PROP_NAME_refundAmount, PROP_ID_refundAmount);
      
          PROP_ID_TO_NAME[PROP_ID_refundType] = PROP_NAME_refundType;
          PROP_NAME_TO_ID.put(PROP_NAME_refundType, PROP_ID_refundType);
      
          PROP_ID_TO_NAME[PROP_ID_refundContent] = PROP_NAME_refundContent;
          PROP_NAME_TO_ID.put(PROP_NAME_refundContent, PROP_ID_refundContent);
      
          PROP_ID_TO_NAME[PROP_ID_refundTime] = PROP_NAME_refundTime;
          PROP_NAME_TO_ID.put(PROP_NAME_refundTime, PROP_ID_refundTime);
      
          PROP_ID_TO_NAME[PROP_ID_confirmTime] = PROP_NAME_confirmTime;
          PROP_NAME_TO_ID.put(PROP_NAME_confirmTime, PROP_ID_confirmTime);
      
          PROP_ID_TO_NAME[PROP_ID_comments] = PROP_NAME_comments;
          PROP_NAME_TO_ID.put(PROP_NAME_comments, PROP_ID_comments);
      
          PROP_ID_TO_NAME[PROP_ID_endTime] = PROP_NAME_endTime;
          PROP_NAME_TO_ID.put(PROP_NAME_endTime, PROP_ID_endTime);
      
          PROP_ID_TO_NAME[PROP_ID_addTime] = PROP_NAME_addTime;
          PROP_NAME_TO_ID.put(PROP_NAME_addTime, PROP_ID_addTime);
      
          PROP_ID_TO_NAME[PROP_ID_updateTime] = PROP_NAME_updateTime;
          PROP_NAME_TO_ID.put(PROP_NAME_updateTime, PROP_ID_updateTime);
      
          PROP_ID_TO_NAME[PROP_ID_deleted] = PROP_NAME_deleted;
          PROP_NAME_TO_ID.put(PROP_NAME_deleted, PROP_ID_deleted);
      
    }

    
    /* Id: ID */
    private java.lang.Integer _id;
    
    /* 用户ID: USER_ID */
    private java.lang.Integer _userId;
    
    /* 订单编号: ORDER_SN */
    private java.lang.String _orderSn;
    
    /* 订单状态: ORDER_STATUS */
    private java.lang.Short _orderStatus;
    
    /* 售后状态: AFTERSALE_STATUS */
    private java.lang.Short _aftersaleStatus;
    
    /* 收货人名称: CONSIGNEE */
    private java.lang.String _consignee;
    
    /* 收货人手机号: MOBILE */
    private java.lang.String _mobile;
    
    /* 收货具体地址: ADDRESS */
    private java.lang.String _address;
    
    /* 用户订单留言: MESSAGE */
    private java.lang.String _message;
    
    /* 商品总费用: GOODS_PRICE */
    private java.math.BigDecimal _goodsPrice;
    
    /* 配送费用: FREIGHT_PRICE */
    private java.math.BigDecimal _freightPrice;
    
    /* 优惠券减免: COUPON_PRICE */
    private java.math.BigDecimal _couponPrice;
    
    /* 用户积分减免: INTEGRAL_PRICE */
    private java.math.BigDecimal _integralPrice;
    
    /* 团购优惠价减免: GROUPON_PRICE */
    private java.math.BigDecimal _grouponPrice;
    
    /* 订单费用: ORDER_PRICE */
    private java.math.BigDecimal _orderPrice;
    
    /* 实付费用: ACTUAL_PRICE */
    private java.math.BigDecimal _actualPrice;
    
    /* 微信付款编号: PAY_ID */
    private java.lang.String _payId;
    
    /* 微信付款时间: PAY_TIME */
    private java.time.LocalDateTime _payTime;
    
    /* 发货编号: SHIP_SN */
    private java.lang.String _shipSn;
    
    /* 发货快递公司: SHIP_CHANNEL */
    private java.lang.String _shipChannel;
    
    /* 发货开始时间: SHIP_TIME */
    private java.time.LocalDateTime _shipTime;
    
    /* 实际退款金额: REFUND_AMOUNT */
    private java.math.BigDecimal _refundAmount;
    
    /* 退款方式: REFUND_TYPE */
    private java.lang.String _refundType;
    
    /* 退款备注: REFUND_CONTENT */
    private java.lang.String _refundContent;
    
    /* 退款时间: REFUND_TIME */
    private java.time.LocalDateTime _refundTime;
    
    /* 用户确认收货时间: CONFIRM_TIME */
    private java.time.LocalDateTime _confirmTime;
    
    /* 待评价订单商品数量: COMMENTS */
    private java.lang.Short _comments;
    
    /* 订单关闭时间: END_TIME */
    private java.time.LocalDateTime _endTime;
    
    /* 创建时间: ADD_TIME */
    private java.time.LocalDateTime _addTime;
    
    /* 更新时间: UPDATE_TIME */
    private java.time.LocalDateTime _updateTime;
    
    /* 逻辑删除: DELETED */
    private java.lang.Boolean _deleted;
    

    public _LitemallOrder(){
    }

    protected LitemallOrder newInstance(){
       return new LitemallOrder();
    }

    @Override
    public LitemallOrder cloneInstance() {
        LitemallOrder entity = newInstance();
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
      return "app.mall.dao.entity.LitemallOrder";
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
        
            case PROP_ID_orderSn:
               return getOrderSn();
        
            case PROP_ID_orderStatus:
               return getOrderStatus();
        
            case PROP_ID_aftersaleStatus:
               return getAftersaleStatus();
        
            case PROP_ID_consignee:
               return getConsignee();
        
            case PROP_ID_mobile:
               return getMobile();
        
            case PROP_ID_address:
               return getAddress();
        
            case PROP_ID_message:
               return getMessage();
        
            case PROP_ID_goodsPrice:
               return getGoodsPrice();
        
            case PROP_ID_freightPrice:
               return getFreightPrice();
        
            case PROP_ID_couponPrice:
               return getCouponPrice();
        
            case PROP_ID_integralPrice:
               return getIntegralPrice();
        
            case PROP_ID_grouponPrice:
               return getGrouponPrice();
        
            case PROP_ID_orderPrice:
               return getOrderPrice();
        
            case PROP_ID_actualPrice:
               return getActualPrice();
        
            case PROP_ID_payId:
               return getPayId();
        
            case PROP_ID_payTime:
               return getPayTime();
        
            case PROP_ID_shipSn:
               return getShipSn();
        
            case PROP_ID_shipChannel:
               return getShipChannel();
        
            case PROP_ID_shipTime:
               return getShipTime();
        
            case PROP_ID_refundAmount:
               return getRefundAmount();
        
            case PROP_ID_refundType:
               return getRefundType();
        
            case PROP_ID_refundContent:
               return getRefundContent();
        
            case PROP_ID_refundTime:
               return getRefundTime();
        
            case PROP_ID_confirmTime:
               return getConfirmTime();
        
            case PROP_ID_comments:
               return getComments();
        
            case PROP_ID_endTime:
               return getEndTime();
        
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
        
            case PROP_ID_userId:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_userId));
               }
               setUserId(typedValue);
               break;
            }
        
            case PROP_ID_orderSn:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_orderSn));
               }
               setOrderSn(typedValue);
               break;
            }
        
            case PROP_ID_orderStatus:{
               java.lang.Short typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toShort(value,
                       err-> newTypeConversionError(PROP_NAME_orderStatus));
               }
               setOrderStatus(typedValue);
               break;
            }
        
            case PROP_ID_aftersaleStatus:{
               java.lang.Short typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toShort(value,
                       err-> newTypeConversionError(PROP_NAME_aftersaleStatus));
               }
               setAftersaleStatus(typedValue);
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
        
            case PROP_ID_mobile:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_mobile));
               }
               setMobile(typedValue);
               break;
            }
        
            case PROP_ID_address:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_address));
               }
               setAddress(typedValue);
               break;
            }
        
            case PROP_ID_message:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_message));
               }
               setMessage(typedValue);
               break;
            }
        
            case PROP_ID_goodsPrice:{
               java.math.BigDecimal typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toBigDecimal(value,
                       err-> newTypeConversionError(PROP_NAME_goodsPrice));
               }
               setGoodsPrice(typedValue);
               break;
            }
        
            case PROP_ID_freightPrice:{
               java.math.BigDecimal typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toBigDecimal(value,
                       err-> newTypeConversionError(PROP_NAME_freightPrice));
               }
               setFreightPrice(typedValue);
               break;
            }
        
            case PROP_ID_couponPrice:{
               java.math.BigDecimal typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toBigDecimal(value,
                       err-> newTypeConversionError(PROP_NAME_couponPrice));
               }
               setCouponPrice(typedValue);
               break;
            }
        
            case PROP_ID_integralPrice:{
               java.math.BigDecimal typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toBigDecimal(value,
                       err-> newTypeConversionError(PROP_NAME_integralPrice));
               }
               setIntegralPrice(typedValue);
               break;
            }
        
            case PROP_ID_grouponPrice:{
               java.math.BigDecimal typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toBigDecimal(value,
                       err-> newTypeConversionError(PROP_NAME_grouponPrice));
               }
               setGrouponPrice(typedValue);
               break;
            }
        
            case PROP_ID_orderPrice:{
               java.math.BigDecimal typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toBigDecimal(value,
                       err-> newTypeConversionError(PROP_NAME_orderPrice));
               }
               setOrderPrice(typedValue);
               break;
            }
        
            case PROP_ID_actualPrice:{
               java.math.BigDecimal typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toBigDecimal(value,
                       err-> newTypeConversionError(PROP_NAME_actualPrice));
               }
               setActualPrice(typedValue);
               break;
            }
        
            case PROP_ID_payId:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_payId));
               }
               setPayId(typedValue);
               break;
            }
        
            case PROP_ID_payTime:{
               java.time.LocalDateTime typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toLocalDateTime(value,
                       err-> newTypeConversionError(PROP_NAME_payTime));
               }
               setPayTime(typedValue);
               break;
            }
        
            case PROP_ID_shipSn:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_shipSn));
               }
               setShipSn(typedValue);
               break;
            }
        
            case PROP_ID_shipChannel:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_shipChannel));
               }
               setShipChannel(typedValue);
               break;
            }
        
            case PROP_ID_shipTime:{
               java.time.LocalDateTime typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toLocalDateTime(value,
                       err-> newTypeConversionError(PROP_NAME_shipTime));
               }
               setShipTime(typedValue);
               break;
            }
        
            case PROP_ID_refundAmount:{
               java.math.BigDecimal typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toBigDecimal(value,
                       err-> newTypeConversionError(PROP_NAME_refundAmount));
               }
               setRefundAmount(typedValue);
               break;
            }
        
            case PROP_ID_refundType:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_refundType));
               }
               setRefundType(typedValue);
               break;
            }
        
            case PROP_ID_refundContent:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_refundContent));
               }
               setRefundContent(typedValue);
               break;
            }
        
            case PROP_ID_refundTime:{
               java.time.LocalDateTime typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toLocalDateTime(value,
                       err-> newTypeConversionError(PROP_NAME_refundTime));
               }
               setRefundTime(typedValue);
               break;
            }
        
            case PROP_ID_confirmTime:{
               java.time.LocalDateTime typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toLocalDateTime(value,
                       err-> newTypeConversionError(PROP_NAME_confirmTime));
               }
               setConfirmTime(typedValue);
               break;
            }
        
            case PROP_ID_comments:{
               java.lang.Short typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toShort(value,
                       err-> newTypeConversionError(PROP_NAME_comments));
               }
               setComments(typedValue);
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
        
            case PROP_ID_userId:{
               onInitProp(propId);
               this._userId = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_orderSn:{
               onInitProp(propId);
               this._orderSn = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_orderStatus:{
               onInitProp(propId);
               this._orderStatus = (java.lang.Short)value;
               
               break;
            }
        
            case PROP_ID_aftersaleStatus:{
               onInitProp(propId);
               this._aftersaleStatus = (java.lang.Short)value;
               
               break;
            }
        
            case PROP_ID_consignee:{
               onInitProp(propId);
               this._consignee = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_mobile:{
               onInitProp(propId);
               this._mobile = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_address:{
               onInitProp(propId);
               this._address = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_message:{
               onInitProp(propId);
               this._message = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_goodsPrice:{
               onInitProp(propId);
               this._goodsPrice = (java.math.BigDecimal)value;
               
               break;
            }
        
            case PROP_ID_freightPrice:{
               onInitProp(propId);
               this._freightPrice = (java.math.BigDecimal)value;
               
               break;
            }
        
            case PROP_ID_couponPrice:{
               onInitProp(propId);
               this._couponPrice = (java.math.BigDecimal)value;
               
               break;
            }
        
            case PROP_ID_integralPrice:{
               onInitProp(propId);
               this._integralPrice = (java.math.BigDecimal)value;
               
               break;
            }
        
            case PROP_ID_grouponPrice:{
               onInitProp(propId);
               this._grouponPrice = (java.math.BigDecimal)value;
               
               break;
            }
        
            case PROP_ID_orderPrice:{
               onInitProp(propId);
               this._orderPrice = (java.math.BigDecimal)value;
               
               break;
            }
        
            case PROP_ID_actualPrice:{
               onInitProp(propId);
               this._actualPrice = (java.math.BigDecimal)value;
               
               break;
            }
        
            case PROP_ID_payId:{
               onInitProp(propId);
               this._payId = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_payTime:{
               onInitProp(propId);
               this._payTime = (java.time.LocalDateTime)value;
               
               break;
            }
        
            case PROP_ID_shipSn:{
               onInitProp(propId);
               this._shipSn = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_shipChannel:{
               onInitProp(propId);
               this._shipChannel = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_shipTime:{
               onInitProp(propId);
               this._shipTime = (java.time.LocalDateTime)value;
               
               break;
            }
        
            case PROP_ID_refundAmount:{
               onInitProp(propId);
               this._refundAmount = (java.math.BigDecimal)value;
               
               break;
            }
        
            case PROP_ID_refundType:{
               onInitProp(propId);
               this._refundType = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_refundContent:{
               onInitProp(propId);
               this._refundContent = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_refundTime:{
               onInitProp(propId);
               this._refundTime = (java.time.LocalDateTime)value;
               
               break;
            }
        
            case PROP_ID_confirmTime:{
               onInitProp(propId);
               this._confirmTime = (java.time.LocalDateTime)value;
               
               break;
            }
        
            case PROP_ID_comments:{
               onInitProp(propId);
               this._comments = (java.lang.Short)value;
               
               break;
            }
        
            case PROP_ID_endTime:{
               onInitProp(propId);
               this._endTime = (java.time.LocalDateTime)value;
               
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
     * 用户ID: USER_ID
     */
    public java.lang.Integer getUserId(){
         onPropGet(PROP_ID_userId);
         return _userId;
    }

    /**
     * 用户ID: USER_ID
     */
    public void setUserId(java.lang.Integer value){
        if(onPropSet(PROP_ID_userId,value)){
            this._userId = value;
            internalClearRefs(PROP_ID_userId);
            
        }
    }
    
    /**
     * 订单编号: ORDER_SN
     */
    public java.lang.String getOrderSn(){
         onPropGet(PROP_ID_orderSn);
         return _orderSn;
    }

    /**
     * 订单编号: ORDER_SN
     */
    public void setOrderSn(java.lang.String value){
        if(onPropSet(PROP_ID_orderSn,value)){
            this._orderSn = value;
            internalClearRefs(PROP_ID_orderSn);
            
        }
    }
    
    /**
     * 订单状态: ORDER_STATUS
     */
    public java.lang.Short getOrderStatus(){
         onPropGet(PROP_ID_orderStatus);
         return _orderStatus;
    }

    /**
     * 订单状态: ORDER_STATUS
     */
    public void setOrderStatus(java.lang.Short value){
        if(onPropSet(PROP_ID_orderStatus,value)){
            this._orderStatus = value;
            internalClearRefs(PROP_ID_orderStatus);
            
        }
    }
    
    /**
     * 售后状态: AFTERSALE_STATUS
     */
    public java.lang.Short getAftersaleStatus(){
         onPropGet(PROP_ID_aftersaleStatus);
         return _aftersaleStatus;
    }

    /**
     * 售后状态: AFTERSALE_STATUS
     */
    public void setAftersaleStatus(java.lang.Short value){
        if(onPropSet(PROP_ID_aftersaleStatus,value)){
            this._aftersaleStatus = value;
            internalClearRefs(PROP_ID_aftersaleStatus);
            
        }
    }
    
    /**
     * 收货人名称: CONSIGNEE
     */
    public java.lang.String getConsignee(){
         onPropGet(PROP_ID_consignee);
         return _consignee;
    }

    /**
     * 收货人名称: CONSIGNEE
     */
    public void setConsignee(java.lang.String value){
        if(onPropSet(PROP_ID_consignee,value)){
            this._consignee = value;
            internalClearRefs(PROP_ID_consignee);
            
        }
    }
    
    /**
     * 收货人手机号: MOBILE
     */
    public java.lang.String getMobile(){
         onPropGet(PROP_ID_mobile);
         return _mobile;
    }

    /**
     * 收货人手机号: MOBILE
     */
    public void setMobile(java.lang.String value){
        if(onPropSet(PROP_ID_mobile,value)){
            this._mobile = value;
            internalClearRefs(PROP_ID_mobile);
            
        }
    }
    
    /**
     * 收货具体地址: ADDRESS
     */
    public java.lang.String getAddress(){
         onPropGet(PROP_ID_address);
         return _address;
    }

    /**
     * 收货具体地址: ADDRESS
     */
    public void setAddress(java.lang.String value){
        if(onPropSet(PROP_ID_address,value)){
            this._address = value;
            internalClearRefs(PROP_ID_address);
            
        }
    }
    
    /**
     * 用户订单留言: MESSAGE
     */
    public java.lang.String getMessage(){
         onPropGet(PROP_ID_message);
         return _message;
    }

    /**
     * 用户订单留言: MESSAGE
     */
    public void setMessage(java.lang.String value){
        if(onPropSet(PROP_ID_message,value)){
            this._message = value;
            internalClearRefs(PROP_ID_message);
            
        }
    }
    
    /**
     * 商品总费用: GOODS_PRICE
     */
    public java.math.BigDecimal getGoodsPrice(){
         onPropGet(PROP_ID_goodsPrice);
         return _goodsPrice;
    }

    /**
     * 商品总费用: GOODS_PRICE
     */
    public void setGoodsPrice(java.math.BigDecimal value){
        if(onPropSet(PROP_ID_goodsPrice,value)){
            this._goodsPrice = value;
            internalClearRefs(PROP_ID_goodsPrice);
            
        }
    }
    
    /**
     * 配送费用: FREIGHT_PRICE
     */
    public java.math.BigDecimal getFreightPrice(){
         onPropGet(PROP_ID_freightPrice);
         return _freightPrice;
    }

    /**
     * 配送费用: FREIGHT_PRICE
     */
    public void setFreightPrice(java.math.BigDecimal value){
        if(onPropSet(PROP_ID_freightPrice,value)){
            this._freightPrice = value;
            internalClearRefs(PROP_ID_freightPrice);
            
        }
    }
    
    /**
     * 优惠券减免: COUPON_PRICE
     */
    public java.math.BigDecimal getCouponPrice(){
         onPropGet(PROP_ID_couponPrice);
         return _couponPrice;
    }

    /**
     * 优惠券减免: COUPON_PRICE
     */
    public void setCouponPrice(java.math.BigDecimal value){
        if(onPropSet(PROP_ID_couponPrice,value)){
            this._couponPrice = value;
            internalClearRefs(PROP_ID_couponPrice);
            
        }
    }
    
    /**
     * 用户积分减免: INTEGRAL_PRICE
     */
    public java.math.BigDecimal getIntegralPrice(){
         onPropGet(PROP_ID_integralPrice);
         return _integralPrice;
    }

    /**
     * 用户积分减免: INTEGRAL_PRICE
     */
    public void setIntegralPrice(java.math.BigDecimal value){
        if(onPropSet(PROP_ID_integralPrice,value)){
            this._integralPrice = value;
            internalClearRefs(PROP_ID_integralPrice);
            
        }
    }
    
    /**
     * 团购优惠价减免: GROUPON_PRICE
     */
    public java.math.BigDecimal getGrouponPrice(){
         onPropGet(PROP_ID_grouponPrice);
         return _grouponPrice;
    }

    /**
     * 团购优惠价减免: GROUPON_PRICE
     */
    public void setGrouponPrice(java.math.BigDecimal value){
        if(onPropSet(PROP_ID_grouponPrice,value)){
            this._grouponPrice = value;
            internalClearRefs(PROP_ID_grouponPrice);
            
        }
    }
    
    /**
     * 订单费用: ORDER_PRICE
     */
    public java.math.BigDecimal getOrderPrice(){
         onPropGet(PROP_ID_orderPrice);
         return _orderPrice;
    }

    /**
     * 订单费用: ORDER_PRICE
     */
    public void setOrderPrice(java.math.BigDecimal value){
        if(onPropSet(PROP_ID_orderPrice,value)){
            this._orderPrice = value;
            internalClearRefs(PROP_ID_orderPrice);
            
        }
    }
    
    /**
     * 实付费用: ACTUAL_PRICE
     */
    public java.math.BigDecimal getActualPrice(){
         onPropGet(PROP_ID_actualPrice);
         return _actualPrice;
    }

    /**
     * 实付费用: ACTUAL_PRICE
     */
    public void setActualPrice(java.math.BigDecimal value){
        if(onPropSet(PROP_ID_actualPrice,value)){
            this._actualPrice = value;
            internalClearRefs(PROP_ID_actualPrice);
            
        }
    }
    
    /**
     * 微信付款编号: PAY_ID
     */
    public java.lang.String getPayId(){
         onPropGet(PROP_ID_payId);
         return _payId;
    }

    /**
     * 微信付款编号: PAY_ID
     */
    public void setPayId(java.lang.String value){
        if(onPropSet(PROP_ID_payId,value)){
            this._payId = value;
            internalClearRefs(PROP_ID_payId);
            
        }
    }
    
    /**
     * 微信付款时间: PAY_TIME
     */
    public java.time.LocalDateTime getPayTime(){
         onPropGet(PROP_ID_payTime);
         return _payTime;
    }

    /**
     * 微信付款时间: PAY_TIME
     */
    public void setPayTime(java.time.LocalDateTime value){
        if(onPropSet(PROP_ID_payTime,value)){
            this._payTime = value;
            internalClearRefs(PROP_ID_payTime);
            
        }
    }
    
    /**
     * 发货编号: SHIP_SN
     */
    public java.lang.String getShipSn(){
         onPropGet(PROP_ID_shipSn);
         return _shipSn;
    }

    /**
     * 发货编号: SHIP_SN
     */
    public void setShipSn(java.lang.String value){
        if(onPropSet(PROP_ID_shipSn,value)){
            this._shipSn = value;
            internalClearRefs(PROP_ID_shipSn);
            
        }
    }
    
    /**
     * 发货快递公司: SHIP_CHANNEL
     */
    public java.lang.String getShipChannel(){
         onPropGet(PROP_ID_shipChannel);
         return _shipChannel;
    }

    /**
     * 发货快递公司: SHIP_CHANNEL
     */
    public void setShipChannel(java.lang.String value){
        if(onPropSet(PROP_ID_shipChannel,value)){
            this._shipChannel = value;
            internalClearRefs(PROP_ID_shipChannel);
            
        }
    }
    
    /**
     * 发货开始时间: SHIP_TIME
     */
    public java.time.LocalDateTime getShipTime(){
         onPropGet(PROP_ID_shipTime);
         return _shipTime;
    }

    /**
     * 发货开始时间: SHIP_TIME
     */
    public void setShipTime(java.time.LocalDateTime value){
        if(onPropSet(PROP_ID_shipTime,value)){
            this._shipTime = value;
            internalClearRefs(PROP_ID_shipTime);
            
        }
    }
    
    /**
     * 实际退款金额: REFUND_AMOUNT
     */
    public java.math.BigDecimal getRefundAmount(){
         onPropGet(PROP_ID_refundAmount);
         return _refundAmount;
    }

    /**
     * 实际退款金额: REFUND_AMOUNT
     */
    public void setRefundAmount(java.math.BigDecimal value){
        if(onPropSet(PROP_ID_refundAmount,value)){
            this._refundAmount = value;
            internalClearRefs(PROP_ID_refundAmount);
            
        }
    }
    
    /**
     * 退款方式: REFUND_TYPE
     */
    public java.lang.String getRefundType(){
         onPropGet(PROP_ID_refundType);
         return _refundType;
    }

    /**
     * 退款方式: REFUND_TYPE
     */
    public void setRefundType(java.lang.String value){
        if(onPropSet(PROP_ID_refundType,value)){
            this._refundType = value;
            internalClearRefs(PROP_ID_refundType);
            
        }
    }
    
    /**
     * 退款备注: REFUND_CONTENT
     */
    public java.lang.String getRefundContent(){
         onPropGet(PROP_ID_refundContent);
         return _refundContent;
    }

    /**
     * 退款备注: REFUND_CONTENT
     */
    public void setRefundContent(java.lang.String value){
        if(onPropSet(PROP_ID_refundContent,value)){
            this._refundContent = value;
            internalClearRefs(PROP_ID_refundContent);
            
        }
    }
    
    /**
     * 退款时间: REFUND_TIME
     */
    public java.time.LocalDateTime getRefundTime(){
         onPropGet(PROP_ID_refundTime);
         return _refundTime;
    }

    /**
     * 退款时间: REFUND_TIME
     */
    public void setRefundTime(java.time.LocalDateTime value){
        if(onPropSet(PROP_ID_refundTime,value)){
            this._refundTime = value;
            internalClearRefs(PROP_ID_refundTime);
            
        }
    }
    
    /**
     * 用户确认收货时间: CONFIRM_TIME
     */
    public java.time.LocalDateTime getConfirmTime(){
         onPropGet(PROP_ID_confirmTime);
         return _confirmTime;
    }

    /**
     * 用户确认收货时间: CONFIRM_TIME
     */
    public void setConfirmTime(java.time.LocalDateTime value){
        if(onPropSet(PROP_ID_confirmTime,value)){
            this._confirmTime = value;
            internalClearRefs(PROP_ID_confirmTime);
            
        }
    }
    
    /**
     * 待评价订单商品数量: COMMENTS
     */
    public java.lang.Short getComments(){
         onPropGet(PROP_ID_comments);
         return _comments;
    }

    /**
     * 待评价订单商品数量: COMMENTS
     */
    public void setComments(java.lang.Short value){
        if(onPropSet(PROP_ID_comments,value)){
            this._comments = value;
            internalClearRefs(PROP_ID_comments);
            
        }
    }
    
    /**
     * 订单关闭时间: END_TIME
     */
    public java.time.LocalDateTime getEndTime(){
         onPropGet(PROP_ID_endTime);
         return _endTime;
    }

    /**
     * 订单关闭时间: END_TIME
     */
    public void setEndTime(java.time.LocalDateTime value){
        if(onPropSet(PROP_ID_endTime,value)){
            this._endTime = value;
            internalClearRefs(PROP_ID_endTime);
            
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
     * 客户
     */
    public app.mall.dao.entity.LitemallUser getUser(){
       return (app.mall.dao.entity.LitemallUser)internalGetRefEntity(PROP_NAME_user);
    }

    public void setUser(app.mall.dao.entity.LitemallUser refEntity){
       if(refEntity == null){
         
         this.setUserId(null);
         
       }else{
          internalSetRefEntity(PROP_NAME_user, refEntity,()->{
             
                    this.setUserId(refEntity.getId());
                 
          });
       }
    }
       
}
// resume CPD analysis - CPD-ON
