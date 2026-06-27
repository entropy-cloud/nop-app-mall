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

import app.mall.dao.entity.LitemallWalletFlow;

// tell cpd to start ignoring code - CPD-OFF
/**
 *  钱包流水表: litemall_wallet_flow
 */
@SuppressWarnings({"PMD.UselessOverridingMethod","PMD.UnusedLocalVariable","java:S3008","java:S1602","java:S1128","java:S1161",
        "PMD.UnnecessaryFullyQualifiedName","PMD.EmptyControlStatement","java:S116","java:S115","java:S101","java:S3776"})
public class _LitemallWalletFlow extends DynamicOrmEntity{
    
    /* Id: ID INTEGER */
    public static final String PROP_NAME_id = "id";
    public static final int PROP_ID_id = 1;
    
    /* 钱包ID: WALLET_ID INTEGER */
    public static final String PROP_NAME_walletId = "walletId";
    public static final int PROP_ID_walletId = 2;
    
    /* 变动类型: CHANGE_TYPE INTEGER */
    public static final String PROP_NAME_changeType = "changeType";
    public static final int PROP_ID_changeType = 3;
    
    /* 变动金额: CHANGE_AMOUNT DECIMAL */
    public static final String PROP_NAME_changeAmount = "changeAmount";
    public static final int PROP_ID_changeAmount = 4;
    
    /* 变动后余额: BALANCE_AFTER DECIMAL */
    public static final String PROP_NAME_balanceAfter = "balanceAfter";
    public static final int PROP_ID_balanceAfter = 5;
    
    /* 来源类型: SOURCE_TYPE VARCHAR */
    public static final String PROP_NAME_sourceType = "sourceType";
    public static final int PROP_ID_sourceType = 6;
    
    /* 来源业务ID: SOURCE_ID VARCHAR */
    public static final String PROP_NAME_sourceId = "sourceId";
    public static final int PROP_ID_sourceId = 7;
    
    /* 备注: REMARK VARCHAR */
    public static final String PROP_NAME_remark = "remark";
    public static final int PROP_ID_remark = 8;
    
    /* 创建时间: ADD_TIME DATETIME */
    public static final String PROP_NAME_addTime = "addTime";
    public static final int PROP_ID_addTime = 9;
    
    /* 更新时间: UPDATE_TIME DATETIME */
    public static final String PROP_NAME_updateTime = "updateTime";
    public static final int PROP_ID_updateTime = 10;
    

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
      
          PROP_ID_TO_NAME[PROP_ID_walletId] = PROP_NAME_walletId;
          PROP_NAME_TO_ID.put(PROP_NAME_walletId, PROP_ID_walletId);
      
          PROP_ID_TO_NAME[PROP_ID_changeType] = PROP_NAME_changeType;
          PROP_NAME_TO_ID.put(PROP_NAME_changeType, PROP_ID_changeType);
      
          PROP_ID_TO_NAME[PROP_ID_changeAmount] = PROP_NAME_changeAmount;
          PROP_NAME_TO_ID.put(PROP_NAME_changeAmount, PROP_ID_changeAmount);
      
          PROP_ID_TO_NAME[PROP_ID_balanceAfter] = PROP_NAME_balanceAfter;
          PROP_NAME_TO_ID.put(PROP_NAME_balanceAfter, PROP_ID_balanceAfter);
      
          PROP_ID_TO_NAME[PROP_ID_sourceType] = PROP_NAME_sourceType;
          PROP_NAME_TO_ID.put(PROP_NAME_sourceType, PROP_ID_sourceType);
      
          PROP_ID_TO_NAME[PROP_ID_sourceId] = PROP_NAME_sourceId;
          PROP_NAME_TO_ID.put(PROP_NAME_sourceId, PROP_ID_sourceId);
      
          PROP_ID_TO_NAME[PROP_ID_remark] = PROP_NAME_remark;
          PROP_NAME_TO_ID.put(PROP_NAME_remark, PROP_ID_remark);
      
          PROP_ID_TO_NAME[PROP_ID_addTime] = PROP_NAME_addTime;
          PROP_NAME_TO_ID.put(PROP_NAME_addTime, PROP_ID_addTime);
      
          PROP_ID_TO_NAME[PROP_ID_updateTime] = PROP_NAME_updateTime;
          PROP_NAME_TO_ID.put(PROP_NAME_updateTime, PROP_ID_updateTime);
      
    }

    
    /* Id: ID */
    private java.lang.String _id;
    
    /* 钱包ID: WALLET_ID */
    private java.lang.String _walletId;
    
    /* 变动类型: CHANGE_TYPE */
    private java.lang.Integer _changeType;
    
    /* 变动金额: CHANGE_AMOUNT */
    private java.math.BigDecimal _changeAmount;
    
    /* 变动后余额: BALANCE_AFTER */
    private java.math.BigDecimal _balanceAfter;
    
    /* 来源类型: SOURCE_TYPE */
    private java.lang.String _sourceType;
    
    /* 来源业务ID: SOURCE_ID */
    private java.lang.String _sourceId;
    
    /* 备注: REMARK */
    private java.lang.String _remark;
    
    /* 创建时间: ADD_TIME */
    private java.time.LocalDateTime _addTime;
    
    /* 更新时间: UPDATE_TIME */
    private java.time.LocalDateTime _updateTime;
    

    public _LitemallWalletFlow(){
        // for debug
    }

    protected LitemallWalletFlow newInstance(){
        LitemallWalletFlow entity = new LitemallWalletFlow();
        entity.orm_attach(orm_enhancer());
        entity.orm_entityModel(orm_entityModel());
        return entity;
    }

    @Override
    public LitemallWalletFlow cloneInstance() {
        LitemallWalletFlow entity = newInstance();
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
      return "app.mall.dao.entity.LitemallWalletFlow";
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
        
            case PROP_ID_walletId:
               return getWalletId();
        
            case PROP_ID_changeType:
               return getChangeType();
        
            case PROP_ID_changeAmount:
               return getChangeAmount();
        
            case PROP_ID_balanceAfter:
               return getBalanceAfter();
        
            case PROP_ID_sourceType:
               return getSourceType();
        
            case PROP_ID_sourceId:
               return getSourceId();
        
            case PROP_ID_remark:
               return getRemark();
        
            case PROP_ID_addTime:
               return getAddTime();
        
            case PROP_ID_updateTime:
               return getUpdateTime();
        
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
        
            case PROP_ID_walletId:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_walletId));
               }
               setWalletId(typedValue);
               break;
            }
        
            case PROP_ID_changeType:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_changeType));
               }
               setChangeType(typedValue);
               break;
            }
        
            case PROP_ID_changeAmount:{
               java.math.BigDecimal typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toBigDecimal(value,
                       err-> newTypeConversionError(PROP_NAME_changeAmount));
               }
               setChangeAmount(typedValue);
               break;
            }
        
            case PROP_ID_balanceAfter:{
               java.math.BigDecimal typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toBigDecimal(value,
                       err-> newTypeConversionError(PROP_NAME_balanceAfter));
               }
               setBalanceAfter(typedValue);
               break;
            }
        
            case PROP_ID_sourceType:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_sourceType));
               }
               setSourceType(typedValue);
               break;
            }
        
            case PROP_ID_sourceId:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_sourceId));
               }
               setSourceId(typedValue);
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
        
            case PROP_ID_walletId:{
               onInitProp(propId);
               this._walletId = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_changeType:{
               onInitProp(propId);
               this._changeType = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_changeAmount:{
               onInitProp(propId);
               this._changeAmount = (java.math.BigDecimal)value;
               
               break;
            }
        
            case PROP_ID_balanceAfter:{
               onInitProp(propId);
               this._balanceAfter = (java.math.BigDecimal)value;
               
               break;
            }
        
            case PROP_ID_sourceType:{
               onInitProp(propId);
               this._sourceType = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_sourceId:{
               onInitProp(propId);
               this._sourceId = (java.lang.String)value;
               
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
     * 变动类型: CHANGE_TYPE
     */
    public final java.lang.Integer getChangeType(){
         onPropGet(PROP_ID_changeType);
         return _changeType;
    }

    /**
     * 变动类型: CHANGE_TYPE
     */
    public final void setChangeType(java.lang.Integer value){
        if(onPropSet(PROP_ID_changeType,value)){
            this._changeType = value;
            internalClearRefs(PROP_ID_changeType);
            
        }
    }
    
    /**
     * 变动金额: CHANGE_AMOUNT
     */
    public final java.math.BigDecimal getChangeAmount(){
         onPropGet(PROP_ID_changeAmount);
         return _changeAmount;
    }

    /**
     * 变动金额: CHANGE_AMOUNT
     */
    public final void setChangeAmount(java.math.BigDecimal value){
        if(onPropSet(PROP_ID_changeAmount,value)){
            this._changeAmount = value;
            internalClearRefs(PROP_ID_changeAmount);
            
        }
    }
    
    /**
     * 变动后余额: BALANCE_AFTER
     */
    public final java.math.BigDecimal getBalanceAfter(){
         onPropGet(PROP_ID_balanceAfter);
         return _balanceAfter;
    }

    /**
     * 变动后余额: BALANCE_AFTER
     */
    public final void setBalanceAfter(java.math.BigDecimal value){
        if(onPropSet(PROP_ID_balanceAfter,value)){
            this._balanceAfter = value;
            internalClearRefs(PROP_ID_balanceAfter);
            
        }
    }
    
    /**
     * 来源类型: SOURCE_TYPE
     */
    public final java.lang.String getSourceType(){
         onPropGet(PROP_ID_sourceType);
         return _sourceType;
    }

    /**
     * 来源类型: SOURCE_TYPE
     */
    public final void setSourceType(java.lang.String value){
        if(onPropSet(PROP_ID_sourceType,value)){
            this._sourceType = value;
            internalClearRefs(PROP_ID_sourceType);
            
        }
    }
    
    /**
     * 来源业务ID: SOURCE_ID
     */
    public final java.lang.String getSourceId(){
         onPropGet(PROP_ID_sourceId);
         return _sourceId;
    }

    /**
     * 来源业务ID: SOURCE_ID
     */
    public final void setSourceId(java.lang.String value){
        if(onPropSet(PROP_ID_sourceId,value)){
            this._sourceId = value;
            internalClearRefs(PROP_ID_sourceId);
            
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
