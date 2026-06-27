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

import app.mall.dao.entity.LitemallRecharge;

// tell cpd to start ignoring code - CPD-OFF
/**
 *  充值记录表: litemall_recharge
 */
@SuppressWarnings({"PMD.UselessOverridingMethod","PMD.UnusedLocalVariable","java:S3008","java:S1602","java:S1128","java:S1161",
        "PMD.UnnecessaryFullyQualifiedName","PMD.EmptyControlStatement","java:S116","java:S115","java:S101","java:S3776"})
public class _LitemallRecharge extends DynamicOrmEntity{
    
    /* Id: ID INTEGER */
    public static final String PROP_NAME_id = "id";
    public static final int PROP_ID_id = 1;
    
    /* 用户ID: USER_ID INTEGER */
    public static final String PROP_NAME_userId = "userId";
    public static final int PROP_ID_userId = 2;
    
    /* 钱包ID: WALLET_ID INTEGER */
    public static final String PROP_NAME_walletId = "walletId";
    public static final int PROP_ID_walletId = 3;
    
    /* 充值金额: AMOUNT DECIMAL */
    public static final String PROP_NAME_amount = "amount";
    public static final int PROP_ID_amount = 4;
    
    /* 赠送金额: GIFT_AMOUNT DECIMAL */
    public static final String PROP_NAME_giftAmount = "giftAmount";
    public static final int PROP_ID_giftAmount = 5;
    
    /* 支付渠道: PAY_CHANNEL INTEGER */
    public static final String PROP_NAME_payChannel = "payChannel";
    public static final int PROP_ID_payChannel = 6;
    
    /* 支付状态: PAY_STATUS INTEGER */
    public static final String PROP_NAME_payStatus = "payStatus";
    public static final int PROP_ID_payStatus = 7;
    
    /* 创建时间: ADD_TIME DATETIME */
    public static final String PROP_NAME_addTime = "addTime";
    public static final int PROP_ID_addTime = 8;
    
    /* 更新时间: UPDATE_TIME DATETIME */
    public static final String PROP_NAME_updateTime = "updateTime";
    public static final int PROP_ID_updateTime = 9;
    
    /* 逻辑删除: DELETED BOOLEAN */
    public static final String PROP_NAME_deleted = "deleted";
    public static final int PROP_ID_deleted = 10;
    

    private static int _PROP_ID_BOUND = 11;

    
    /* relation: 钱包 */
    public static final String PROP_NAME_wallet = "wallet";
    

    protected static final List<String> PK_PROP_NAMES = Arrays.asList(PROP_NAME_id);
    protected static final int[] PK_PROP_IDS = new int[]{PROP_ID_id};

    private static final String[] PROP_ID_TO_NAME = new String[11];
    private static final Map<String,Integer> PROP_NAME_TO_ID = new HashMap<>();
    static{
      
          PROP_ID_TO_NAME[PROP_ID_id] = PROP_NAME_id;
          PROP_NAME_TO_ID.put(PROP_NAME_id, PROP_ID_id);
      
          PROP_ID_TO_NAME[PROP_ID_userId] = PROP_NAME_userId;
          PROP_NAME_TO_ID.put(PROP_NAME_userId, PROP_ID_userId);
      
          PROP_ID_TO_NAME[PROP_ID_walletId] = PROP_NAME_walletId;
          PROP_NAME_TO_ID.put(PROP_NAME_walletId, PROP_ID_walletId);
      
          PROP_ID_TO_NAME[PROP_ID_amount] = PROP_NAME_amount;
          PROP_NAME_TO_ID.put(PROP_NAME_amount, PROP_ID_amount);
      
          PROP_ID_TO_NAME[PROP_ID_giftAmount] = PROP_NAME_giftAmount;
          PROP_NAME_TO_ID.put(PROP_NAME_giftAmount, PROP_ID_giftAmount);
      
          PROP_ID_TO_NAME[PROP_ID_payChannel] = PROP_NAME_payChannel;
          PROP_NAME_TO_ID.put(PROP_NAME_payChannel, PROP_ID_payChannel);
      
          PROP_ID_TO_NAME[PROP_ID_payStatus] = PROP_NAME_payStatus;
          PROP_NAME_TO_ID.put(PROP_NAME_payStatus, PROP_ID_payStatus);
      
          PROP_ID_TO_NAME[PROP_ID_addTime] = PROP_NAME_addTime;
          PROP_NAME_TO_ID.put(PROP_NAME_addTime, PROP_ID_addTime);
      
          PROP_ID_TO_NAME[PROP_ID_updateTime] = PROP_NAME_updateTime;
          PROP_NAME_TO_ID.put(PROP_NAME_updateTime, PROP_ID_updateTime);
      
          PROP_ID_TO_NAME[PROP_ID_deleted] = PROP_NAME_deleted;
          PROP_NAME_TO_ID.put(PROP_NAME_deleted, PROP_ID_deleted);
      
    }

    
    /* Id: ID */
    private java.lang.String _id;
    
    /* 用户ID: USER_ID */
    private java.lang.String _userId;
    
    /* 钱包ID: WALLET_ID */
    private java.lang.String _walletId;
    
    /* 充值金额: AMOUNT */
    private java.math.BigDecimal _amount;
    
    /* 赠送金额: GIFT_AMOUNT */
    private java.math.BigDecimal _giftAmount;
    
    /* 支付渠道: PAY_CHANNEL */
    private java.lang.Integer _payChannel;
    
    /* 支付状态: PAY_STATUS */
    private java.lang.Integer _payStatus;
    
    /* 创建时间: ADD_TIME */
    private java.time.LocalDateTime _addTime;
    
    /* 更新时间: UPDATE_TIME */
    private java.time.LocalDateTime _updateTime;
    
    /* 逻辑删除: DELETED */
    private java.lang.Boolean _deleted;
    

    public _LitemallRecharge(){
        // for debug
    }

    protected LitemallRecharge newInstance(){
        LitemallRecharge entity = new LitemallRecharge();
        entity.orm_attach(orm_enhancer());
        entity.orm_entityModel(orm_entityModel());
        return entity;
    }

    @Override
    public LitemallRecharge cloneInstance() {
        LitemallRecharge entity = newInstance();
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
      return "app.mall.dao.entity.LitemallRecharge";
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
        
            case PROP_ID_walletId:
               return getWalletId();
        
            case PROP_ID_amount:
               return getAmount();
        
            case PROP_ID_giftAmount:
               return getGiftAmount();
        
            case PROP_ID_payChannel:
               return getPayChannel();
        
            case PROP_ID_payStatus:
               return getPayStatus();
        
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
        
            case PROP_ID_userId:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_userId));
               }
               setUserId(typedValue);
               break;
            }
        
            case PROP_ID_walletId:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_walletId));
               }
               setWalletId(typedValue);
               break;
            }
        
            case PROP_ID_amount:{
               java.math.BigDecimal typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toBigDecimal(value,
                       err-> newTypeConversionError(PROP_NAME_amount));
               }
               setAmount(typedValue);
               break;
            }
        
            case PROP_ID_giftAmount:{
               java.math.BigDecimal typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toBigDecimal(value,
                       err-> newTypeConversionError(PROP_NAME_giftAmount));
               }
               setGiftAmount(typedValue);
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
        
            case PROP_ID_payStatus:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_payStatus));
               }
               setPayStatus(typedValue);
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
        
            case PROP_ID_userId:{
               onInitProp(propId);
               this._userId = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_walletId:{
               onInitProp(propId);
               this._walletId = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_amount:{
               onInitProp(propId);
               this._amount = (java.math.BigDecimal)value;
               
               break;
            }
        
            case PROP_ID_giftAmount:{
               onInitProp(propId);
               this._giftAmount = (java.math.BigDecimal)value;
               
               break;
            }
        
            case PROP_ID_payChannel:{
               onInitProp(propId);
               this._payChannel = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_payStatus:{
               onInitProp(propId);
               this._payStatus = (java.lang.Integer)value;
               
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
     * 钱包ID: WALLET_ID
     */
    public final java.lang.String getWalletId(){
         onPropGet(PROP_ID_walletId);
         return _walletId;
    }

    /**
     * 钱包ID: WALLET_ID
     */
    public final void setWalletId(java.lang.String value){
        if(onPropSet(PROP_ID_walletId,value)){
            this._walletId = value;
            internalClearRefs(PROP_ID_walletId);
            
        }
    }
    
    /**
     * 充值金额: AMOUNT
     */
    public final java.math.BigDecimal getAmount(){
         onPropGet(PROP_ID_amount);
         return _amount;
    }

    /**
     * 充值金额: AMOUNT
     */
    public final void setAmount(java.math.BigDecimal value){
        if(onPropSet(PROP_ID_amount,value)){
            this._amount = value;
            internalClearRefs(PROP_ID_amount);
            
        }
    }
    
    /**
     * 赠送金额: GIFT_AMOUNT
     */
    public final java.math.BigDecimal getGiftAmount(){
         onPropGet(PROP_ID_giftAmount);
         return _giftAmount;
    }

    /**
     * 赠送金额: GIFT_AMOUNT
     */
    public final void setGiftAmount(java.math.BigDecimal value){
        if(onPropSet(PROP_ID_giftAmount,value)){
            this._giftAmount = value;
            internalClearRefs(PROP_ID_giftAmount);
            
        }
    }
    
    /**
     * 支付渠道: PAY_CHANNEL
     */
    public final java.lang.Integer getPayChannel(){
         onPropGet(PROP_ID_payChannel);
         return _payChannel;
    }

    /**
     * 支付渠道: PAY_CHANNEL
     */
    public final void setPayChannel(java.lang.Integer value){
        if(onPropSet(PROP_ID_payChannel,value)){
            this._payChannel = value;
            internalClearRefs(PROP_ID_payChannel);
            
        }
    }
    
    /**
     * 支付状态: PAY_STATUS
     */
    public final java.lang.Integer getPayStatus(){
         onPropGet(PROP_ID_payStatus);
         return _payStatus;
    }

    /**
     * 支付状态: PAY_STATUS
     */
    public final void setPayStatus(java.lang.Integer value){
        if(onPropSet(PROP_ID_payStatus,value)){
            this._payStatus = value;
            internalClearRefs(PROP_ID_payStatus);
            
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
     * 钱包
     */
    public final app.mall.dao.entity.LitemallWallet getWallet(){
       return (app.mall.dao.entity.LitemallWallet)internalGetRefEntity(PROP_NAME_wallet);
    }

    public final void setWallet(app.mall.dao.entity.LitemallWallet refEntity){
   
           if(refEntity == null){
           
                   this.setWalletId(null);
               
           }else{
           internalSetRefEntity(PROP_NAME_wallet, refEntity,()->{
           
                           this.setWalletId(refEntity.getId());
                       
           });
           }
       
    }
       
}
// resume CPD analysis - CPD-ON
