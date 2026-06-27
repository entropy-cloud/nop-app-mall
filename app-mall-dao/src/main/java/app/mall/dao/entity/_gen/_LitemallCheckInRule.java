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

import app.mall.dao.entity.LitemallCheckInRule;

// tell cpd to start ignoring code - CPD-OFF
/**
 *  签到规则表: litemall_check_in_rule
 */
@SuppressWarnings({"PMD.UselessOverridingMethod","PMD.UnusedLocalVariable","java:S3008","java:S1602","java:S1128","java:S1161",
        "PMD.UnnecessaryFullyQualifiedName","PMD.EmptyControlStatement","java:S116","java:S115","java:S101","java:S3776"})
public class _LitemallCheckInRule extends DynamicOrmEntity{
    
    /* Id: ID INTEGER */
    public static final String PROP_NAME_id = "id";
    public static final int PROP_ID_id = 1;
    
    /* 连续第N天: DAY_SEQ INTEGER */
    public static final String PROP_NAME_daySeq = "daySeq";
    public static final int PROP_ID_daySeq = 2;
    
    /* 奖励积分数: POINT_REWARD INTEGER */
    public static final String PROP_NAME_pointReward = "pointReward";
    public static final int PROP_ID_pointReward = 3;
    
    /* 重置周期（天数，0不重置）: RESET_CYCLE INTEGER */
    public static final String PROP_NAME_resetCycle = "resetCycle";
    public static final int PROP_ID_resetCycle = 4;
    
    /* 创建时间: ADD_TIME DATETIME */
    public static final String PROP_NAME_addTime = "addTime";
    public static final int PROP_ID_addTime = 5;
    
    /* 更新时间: UPDATE_TIME DATETIME */
    public static final String PROP_NAME_updateTime = "updateTime";
    public static final int PROP_ID_updateTime = 6;
    

    private static int _PROP_ID_BOUND = 7;

    

    protected static final List<String> PK_PROP_NAMES = Arrays.asList(PROP_NAME_id);
    protected static final int[] PK_PROP_IDS = new int[]{PROP_ID_id};

    private static final String[] PROP_ID_TO_NAME = new String[7];
    private static final Map<String,Integer> PROP_NAME_TO_ID = new HashMap<>();
    static{
      
          PROP_ID_TO_NAME[PROP_ID_id] = PROP_NAME_id;
          PROP_NAME_TO_ID.put(PROP_NAME_id, PROP_ID_id);
      
          PROP_ID_TO_NAME[PROP_ID_daySeq] = PROP_NAME_daySeq;
          PROP_NAME_TO_ID.put(PROP_NAME_daySeq, PROP_ID_daySeq);
      
          PROP_ID_TO_NAME[PROP_ID_pointReward] = PROP_NAME_pointReward;
          PROP_NAME_TO_ID.put(PROP_NAME_pointReward, PROP_ID_pointReward);
      
          PROP_ID_TO_NAME[PROP_ID_resetCycle] = PROP_NAME_resetCycle;
          PROP_NAME_TO_ID.put(PROP_NAME_resetCycle, PROP_ID_resetCycle);
      
          PROP_ID_TO_NAME[PROP_ID_addTime] = PROP_NAME_addTime;
          PROP_NAME_TO_ID.put(PROP_NAME_addTime, PROP_ID_addTime);
      
          PROP_ID_TO_NAME[PROP_ID_updateTime] = PROP_NAME_updateTime;
          PROP_NAME_TO_ID.put(PROP_NAME_updateTime, PROP_ID_updateTime);
      
    }

    
    /* Id: ID */
    private java.lang.String _id;
    
    /* 连续第N天: DAY_SEQ */
    private java.lang.Integer _daySeq;
    
    /* 奖励积分数: POINT_REWARD */
    private java.lang.Integer _pointReward;
    
    /* 重置周期（天数，0不重置）: RESET_CYCLE */
    private java.lang.Integer _resetCycle;
    
    /* 创建时间: ADD_TIME */
    private java.time.LocalDateTime _addTime;
    
    /* 更新时间: UPDATE_TIME */
    private java.time.LocalDateTime _updateTime;
    

    public _LitemallCheckInRule(){
        // for debug
    }

    protected LitemallCheckInRule newInstance(){
        LitemallCheckInRule entity = new LitemallCheckInRule();
        entity.orm_attach(orm_enhancer());
        entity.orm_entityModel(orm_entityModel());
        return entity;
    }

    @Override
    public LitemallCheckInRule cloneInstance() {
        LitemallCheckInRule entity = newInstance();
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
      return "app.mall.dao.entity.LitemallCheckInRule";
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
        
            case PROP_ID_daySeq:
               return getDaySeq();
        
            case PROP_ID_pointReward:
               return getPointReward();
        
            case PROP_ID_resetCycle:
               return getResetCycle();
        
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
        
            case PROP_ID_daySeq:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_daySeq));
               }
               setDaySeq(typedValue);
               break;
            }
        
            case PROP_ID_pointReward:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_pointReward));
               }
               setPointReward(typedValue);
               break;
            }
        
            case PROP_ID_resetCycle:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_resetCycle));
               }
               setResetCycle(typedValue);
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
        
            case PROP_ID_daySeq:{
               onInitProp(propId);
               this._daySeq = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_pointReward:{
               onInitProp(propId);
               this._pointReward = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_resetCycle:{
               onInitProp(propId);
               this._resetCycle = (java.lang.Integer)value;
               
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
     * 连续第N天: DAY_SEQ
     */
    public final java.lang.Integer getDaySeq(){
         onPropGet(PROP_ID_daySeq);
         return _daySeq;
    }

    /**
     * 连续第N天: DAY_SEQ
     */
    public final void setDaySeq(java.lang.Integer value){
        if(onPropSet(PROP_ID_daySeq,value)){
            this._daySeq = value;
            internalClearRefs(PROP_ID_daySeq);
            
        }
    }
    
    /**
     * 奖励积分数: POINT_REWARD
     */
    public final java.lang.Integer getPointReward(){
         onPropGet(PROP_ID_pointReward);
         return _pointReward;
    }

    /**
     * 奖励积分数: POINT_REWARD
     */
    public final void setPointReward(java.lang.Integer value){
        if(onPropSet(PROP_ID_pointReward,value)){
            this._pointReward = value;
            internalClearRefs(PROP_ID_pointReward);
            
        }
    }
    
    /**
     * 重置周期（天数，0不重置）: RESET_CYCLE
     */
    public final java.lang.Integer getResetCycle(){
         onPropGet(PROP_ID_resetCycle);
         return _resetCycle;
    }

    /**
     * 重置周期（天数，0不重置）: RESET_CYCLE
     */
    public final void setResetCycle(java.lang.Integer value){
        if(onPropSet(PROP_ID_resetCycle,value)){
            this._resetCycle = value;
            internalClearRefs(PROP_ID_resetCycle);
            
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
    
}
// resume CPD analysis - CPD-ON
