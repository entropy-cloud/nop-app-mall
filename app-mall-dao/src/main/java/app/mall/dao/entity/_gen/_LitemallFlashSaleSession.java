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

import app.mall.dao.entity.LitemallFlashSaleSession;

// tell cpd to start ignoring code - CPD-OFF
/**
 *  秒杀场次表: litemall_flash_sale_session
 */
@SuppressWarnings({"PMD.UselessOverridingMethod","PMD.UnusedLocalVariable","java:S3008","java:S1602","java:S1128","java:S1161",
        "PMD.UnnecessaryFullyQualifiedName","PMD.EmptyControlStatement","java:S116","java:S115","java:S101","java:S3776"})
public class _LitemallFlashSaleSession extends DynamicOrmEntity{
    
    /* Id: ID INTEGER */
    public static final String PROP_NAME_id = "id";
    public static final int PROP_ID_id = 1;
    
    /* 秒杀活动ID: FLASH_SALE_ID INTEGER */
    public static final String PROP_NAME_flashSaleId = "flashSaleId";
    public static final int PROP_ID_flashSaleId = 2;
    
    /* 场次开始时间: SESSION_START DATETIME */
    public static final String PROP_NAME_sessionStart = "sessionStart";
    public static final int PROP_ID_sessionStart = 3;
    
    /* 场次结束时间: SESSION_END DATETIME */
    public static final String PROP_NAME_sessionEnd = "sessionEnd";
    public static final int PROP_ID_sessionEnd = 4;
    
    /* 场次库存: SESSION_STOCK INTEGER */
    public static final String PROP_NAME_sessionStock = "sessionStock";
    public static final int PROP_ID_sessionStock = 5;
    
    /* 场次状态（0未开始/1进行中/2已结束）: SESSION_STATUS INTEGER */
    public static final String PROP_NAME_sessionStatus = "sessionStatus";
    public static final int PROP_ID_sessionStatus = 6;
    
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

    
    /* relation: 秒杀活动 */
    public static final String PROP_NAME_flashSale = "flashSale";
    

    protected static final List<String> PK_PROP_NAMES = Arrays.asList(PROP_NAME_id);
    protected static final int[] PK_PROP_IDS = new int[]{PROP_ID_id};

    private static final String[] PROP_ID_TO_NAME = new String[10];
    private static final Map<String,Integer> PROP_NAME_TO_ID = new HashMap<>();
    static{
      
          PROP_ID_TO_NAME[PROP_ID_id] = PROP_NAME_id;
          PROP_NAME_TO_ID.put(PROP_NAME_id, PROP_ID_id);
      
          PROP_ID_TO_NAME[PROP_ID_flashSaleId] = PROP_NAME_flashSaleId;
          PROP_NAME_TO_ID.put(PROP_NAME_flashSaleId, PROP_ID_flashSaleId);
      
          PROP_ID_TO_NAME[PROP_ID_sessionStart] = PROP_NAME_sessionStart;
          PROP_NAME_TO_ID.put(PROP_NAME_sessionStart, PROP_ID_sessionStart);
      
          PROP_ID_TO_NAME[PROP_ID_sessionEnd] = PROP_NAME_sessionEnd;
          PROP_NAME_TO_ID.put(PROP_NAME_sessionEnd, PROP_ID_sessionEnd);
      
          PROP_ID_TO_NAME[PROP_ID_sessionStock] = PROP_NAME_sessionStock;
          PROP_NAME_TO_ID.put(PROP_NAME_sessionStock, PROP_ID_sessionStock);
      
          PROP_ID_TO_NAME[PROP_ID_sessionStatus] = PROP_NAME_sessionStatus;
          PROP_NAME_TO_ID.put(PROP_NAME_sessionStatus, PROP_ID_sessionStatus);
      
          PROP_ID_TO_NAME[PROP_ID_addTime] = PROP_NAME_addTime;
          PROP_NAME_TO_ID.put(PROP_NAME_addTime, PROP_ID_addTime);
      
          PROP_ID_TO_NAME[PROP_ID_updateTime] = PROP_NAME_updateTime;
          PROP_NAME_TO_ID.put(PROP_NAME_updateTime, PROP_ID_updateTime);
      
          PROP_ID_TO_NAME[PROP_ID_deleted] = PROP_NAME_deleted;
          PROP_NAME_TO_ID.put(PROP_NAME_deleted, PROP_ID_deleted);
      
    }

    
    /* Id: ID */
    private java.lang.String _id;
    
    /* 秒杀活动ID: FLASH_SALE_ID */
    private java.lang.String _flashSaleId;
    
    /* 场次开始时间: SESSION_START */
    private java.time.LocalDateTime _sessionStart;
    
    /* 场次结束时间: SESSION_END */
    private java.time.LocalDateTime _sessionEnd;
    
    /* 场次库存: SESSION_STOCK */
    private java.lang.Integer _sessionStock;
    
    /* 场次状态（0未开始/1进行中/2已结束）: SESSION_STATUS */
    private java.lang.Integer _sessionStatus;
    
    /* 创建时间: ADD_TIME */
    private java.time.LocalDateTime _addTime;
    
    /* 更新时间: UPDATE_TIME */
    private java.time.LocalDateTime _updateTime;
    
    /* 逻辑删除: DELETED */
    private java.lang.Boolean _deleted;
    

    public _LitemallFlashSaleSession(){
        // for debug
    }

    protected LitemallFlashSaleSession newInstance(){
        LitemallFlashSaleSession entity = new LitemallFlashSaleSession();
        entity.orm_attach(orm_enhancer());
        entity.orm_entityModel(orm_entityModel());
        return entity;
    }

    @Override
    public LitemallFlashSaleSession cloneInstance() {
        LitemallFlashSaleSession entity = newInstance();
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
      return "app.mall.dao.entity.LitemallFlashSaleSession";
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
        
            case PROP_ID_flashSaleId:
               return getFlashSaleId();
        
            case PROP_ID_sessionStart:
               return getSessionStart();
        
            case PROP_ID_sessionEnd:
               return getSessionEnd();
        
            case PROP_ID_sessionStock:
               return getSessionStock();
        
            case PROP_ID_sessionStatus:
               return getSessionStatus();
        
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
        
            case PROP_ID_flashSaleId:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_flashSaleId));
               }
               setFlashSaleId(typedValue);
               break;
            }
        
            case PROP_ID_sessionStart:{
               java.time.LocalDateTime typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toLocalDateTime(value,
                       err-> newTypeConversionError(PROP_NAME_sessionStart));
               }
               setSessionStart(typedValue);
               break;
            }
        
            case PROP_ID_sessionEnd:{
               java.time.LocalDateTime typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toLocalDateTime(value,
                       err-> newTypeConversionError(PROP_NAME_sessionEnd));
               }
               setSessionEnd(typedValue);
               break;
            }
        
            case PROP_ID_sessionStock:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_sessionStock));
               }
               setSessionStock(typedValue);
               break;
            }
        
            case PROP_ID_sessionStatus:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_sessionStatus));
               }
               setSessionStatus(typedValue);
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
        
            case PROP_ID_flashSaleId:{
               onInitProp(propId);
               this._flashSaleId = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_sessionStart:{
               onInitProp(propId);
               this._sessionStart = (java.time.LocalDateTime)value;
               
               break;
            }
        
            case PROP_ID_sessionEnd:{
               onInitProp(propId);
               this._sessionEnd = (java.time.LocalDateTime)value;
               
               break;
            }
        
            case PROP_ID_sessionStock:{
               onInitProp(propId);
               this._sessionStock = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_sessionStatus:{
               onInitProp(propId);
               this._sessionStatus = (java.lang.Integer)value;
               
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
     * 秒杀活动ID: FLASH_SALE_ID
     */
    public final java.lang.String getFlashSaleId(){
         onPropGet(PROP_ID_flashSaleId);
         return _flashSaleId;
    }

    /**
     * 秒杀活动ID: FLASH_SALE_ID
     */
    public final void setFlashSaleId(java.lang.String value){
        if(onPropSet(PROP_ID_flashSaleId,value)){
            this._flashSaleId = value;
            internalClearRefs(PROP_ID_flashSaleId);
            
        }
    }
    
    /**
     * 场次开始时间: SESSION_START
     */
    public final java.time.LocalDateTime getSessionStart(){
         onPropGet(PROP_ID_sessionStart);
         return _sessionStart;
    }

    /**
     * 场次开始时间: SESSION_START
     */
    public final void setSessionStart(java.time.LocalDateTime value){
        if(onPropSet(PROP_ID_sessionStart,value)){
            this._sessionStart = value;
            internalClearRefs(PROP_ID_sessionStart);
            
        }
    }
    
    /**
     * 场次结束时间: SESSION_END
     */
    public final java.time.LocalDateTime getSessionEnd(){
         onPropGet(PROP_ID_sessionEnd);
         return _sessionEnd;
    }

    /**
     * 场次结束时间: SESSION_END
     */
    public final void setSessionEnd(java.time.LocalDateTime value){
        if(onPropSet(PROP_ID_sessionEnd,value)){
            this._sessionEnd = value;
            internalClearRefs(PROP_ID_sessionEnd);
            
        }
    }
    
    /**
     * 场次库存: SESSION_STOCK
     */
    public final java.lang.Integer getSessionStock(){
         onPropGet(PROP_ID_sessionStock);
         return _sessionStock;
    }

    /**
     * 场次库存: SESSION_STOCK
     */
    public final void setSessionStock(java.lang.Integer value){
        if(onPropSet(PROP_ID_sessionStock,value)){
            this._sessionStock = value;
            internalClearRefs(PROP_ID_sessionStock);
            
        }
    }
    
    /**
     * 场次状态（0未开始/1进行中/2已结束）: SESSION_STATUS
     */
    public final java.lang.Integer getSessionStatus(){
         onPropGet(PROP_ID_sessionStatus);
         return _sessionStatus;
    }

    /**
     * 场次状态（0未开始/1进行中/2已结束）: SESSION_STATUS
     */
    public final void setSessionStatus(java.lang.Integer value){
        if(onPropSet(PROP_ID_sessionStatus,value)){
            this._sessionStatus = value;
            internalClearRefs(PROP_ID_sessionStatus);
            
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
     * 秒杀活动
     */
    public final app.mall.dao.entity.LitemallFlashSale getFlashSale(){
       return (app.mall.dao.entity.LitemallFlashSale)internalGetRefEntity(PROP_NAME_flashSale);
    }

    public final void setFlashSale(app.mall.dao.entity.LitemallFlashSale refEntity){
   
           if(refEntity == null){
           
                   this.setFlashSaleId(null);
               
           }else{
           internalSetRefEntity(PROP_NAME_flashSale, refEntity,()->{
           
                           this.setFlashSaleId(refEntity.getId());
                       
           });
           }
       
    }
       
}
// resume CPD analysis - CPD-ON
