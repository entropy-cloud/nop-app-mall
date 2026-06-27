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

import app.mall.dao.entity.LitemallMemberLevel;

// tell cpd to start ignoring code - CPD-OFF
/**
 *  会员等级规则表: litemall_member_level
 */
@SuppressWarnings({"PMD.UselessOverridingMethod","PMD.UnusedLocalVariable","java:S3008","java:S1602","java:S1128","java:S1161",
        "PMD.UnnecessaryFullyQualifiedName","PMD.EmptyControlStatement","java:S116","java:S115","java:S101","java:S3776"})
public class _LitemallMemberLevel extends DynamicOrmEntity{
    
    /* Id: ID INTEGER */
    public static final String PROP_NAME_id = "id";
    public static final int PROP_ID_id = 1;
    
    /* 等级: LEVEL INTEGER */
    public static final String PROP_NAME_level = "level";
    public static final int PROP_ID_level = 2;
    
    /* 等级名称: NAME VARCHAR */
    public static final String PROP_NAME_name = "name";
    public static final int PROP_ID_name = 3;
    
    /* 升级阈值(累计消费): UPGRADE_THRESHOLD DECIMAL */
    public static final String PROP_NAME_upgradeThreshold = "upgradeThreshold";
    public static final int PROP_ID_upgradeThreshold = 4;
    
    /* 保级阈值(周期内累计消费): DOWNGRADE_THRESHOLD DECIMAL */
    public static final String PROP_NAME_downgradeThreshold = "downgradeThreshold";
    public static final int PROP_ID_downgradeThreshold = 5;
    
    /* 权益配置(JSON): BENEFITS VARCHAR */
    public static final String PROP_NAME_benefits = "benefits";
    public static final int PROP_ID_benefits = 6;
    
    /* 排序: SORT_ORDER INTEGER */
    public static final String PROP_NAME_sortOrder = "sortOrder";
    public static final int PROP_ID_sortOrder = 7;
    
    /* 备注: REMARK VARCHAR */
    public static final String PROP_NAME_remark = "remark";
    public static final int PROP_ID_remark = 8;
    
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

    
    /* component:  */
    public static final String PROP_NAME_benefitsComponent = "benefitsComponent";
    

    protected static final List<String> PK_PROP_NAMES = Arrays.asList(PROP_NAME_id);
    protected static final int[] PK_PROP_IDS = new int[]{PROP_ID_id};

    private static final String[] PROP_ID_TO_NAME = new String[12];
    private static final Map<String,Integer> PROP_NAME_TO_ID = new HashMap<>();
    static{
      
          PROP_ID_TO_NAME[PROP_ID_id] = PROP_NAME_id;
          PROP_NAME_TO_ID.put(PROP_NAME_id, PROP_ID_id);
      
          PROP_ID_TO_NAME[PROP_ID_level] = PROP_NAME_level;
          PROP_NAME_TO_ID.put(PROP_NAME_level, PROP_ID_level);
      
          PROP_ID_TO_NAME[PROP_ID_name] = PROP_NAME_name;
          PROP_NAME_TO_ID.put(PROP_NAME_name, PROP_ID_name);
      
          PROP_ID_TO_NAME[PROP_ID_upgradeThreshold] = PROP_NAME_upgradeThreshold;
          PROP_NAME_TO_ID.put(PROP_NAME_upgradeThreshold, PROP_ID_upgradeThreshold);
      
          PROP_ID_TO_NAME[PROP_ID_downgradeThreshold] = PROP_NAME_downgradeThreshold;
          PROP_NAME_TO_ID.put(PROP_NAME_downgradeThreshold, PROP_ID_downgradeThreshold);
      
          PROP_ID_TO_NAME[PROP_ID_benefits] = PROP_NAME_benefits;
          PROP_NAME_TO_ID.put(PROP_NAME_benefits, PROP_ID_benefits);
      
          PROP_ID_TO_NAME[PROP_ID_sortOrder] = PROP_NAME_sortOrder;
          PROP_NAME_TO_ID.put(PROP_NAME_sortOrder, PROP_ID_sortOrder);
      
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
    
    /* 等级: LEVEL */
    private java.lang.Integer _level;
    
    /* 等级名称: NAME */
    private java.lang.String _name;
    
    /* 升级阈值(累计消费): UPGRADE_THRESHOLD */
    private java.math.BigDecimal _upgradeThreshold;
    
    /* 保级阈值(周期内累计消费): DOWNGRADE_THRESHOLD */
    private java.math.BigDecimal _downgradeThreshold;
    
    /* 权益配置(JSON): BENEFITS */
    private java.lang.String _benefits;
    
    /* 排序: SORT_ORDER */
    private java.lang.Integer _sortOrder;
    
    /* 备注: REMARK */
    private java.lang.String _remark;
    
    /* 创建时间: ADD_TIME */
    private java.time.LocalDateTime _addTime;
    
    /* 更新时间: UPDATE_TIME */
    private java.time.LocalDateTime _updateTime;
    
    /* 逻辑删除: DELETED */
    private java.lang.Boolean _deleted;
    

    public _LitemallMemberLevel(){
        // for debug
    }

    protected LitemallMemberLevel newInstance(){
        LitemallMemberLevel entity = new LitemallMemberLevel();
        entity.orm_attach(orm_enhancer());
        entity.orm_entityModel(orm_entityModel());
        return entity;
    }

    @Override
    public LitemallMemberLevel cloneInstance() {
        LitemallMemberLevel entity = newInstance();
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
      return "app.mall.dao.entity.LitemallMemberLevel";
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
        
            case PROP_ID_level:
               return getLevel();
        
            case PROP_ID_name:
               return getName();
        
            case PROP_ID_upgradeThreshold:
               return getUpgradeThreshold();
        
            case PROP_ID_downgradeThreshold:
               return getDowngradeThreshold();
        
            case PROP_ID_benefits:
               return getBenefits();
        
            case PROP_ID_sortOrder:
               return getSortOrder();
        
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
        
            case PROP_ID_level:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_level));
               }
               setLevel(typedValue);
               break;
            }
        
            case PROP_ID_name:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_name));
               }
               setName(typedValue);
               break;
            }
        
            case PROP_ID_upgradeThreshold:{
               java.math.BigDecimal typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toBigDecimal(value,
                       err-> newTypeConversionError(PROP_NAME_upgradeThreshold));
               }
               setUpgradeThreshold(typedValue);
               break;
            }
        
            case PROP_ID_downgradeThreshold:{
               java.math.BigDecimal typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toBigDecimal(value,
                       err-> newTypeConversionError(PROP_NAME_downgradeThreshold));
               }
               setDowngradeThreshold(typedValue);
               break;
            }
        
            case PROP_ID_benefits:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_benefits));
               }
               setBenefits(typedValue);
               break;
            }
        
            case PROP_ID_sortOrder:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_sortOrder));
               }
               setSortOrder(typedValue);
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
        
            case PROP_ID_level:{
               onInitProp(propId);
               this._level = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_name:{
               onInitProp(propId);
               this._name = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_upgradeThreshold:{
               onInitProp(propId);
               this._upgradeThreshold = (java.math.BigDecimal)value;
               
               break;
            }
        
            case PROP_ID_downgradeThreshold:{
               onInitProp(propId);
               this._downgradeThreshold = (java.math.BigDecimal)value;
               
               break;
            }
        
            case PROP_ID_benefits:{
               onInitProp(propId);
               this._benefits = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_sortOrder:{
               onInitProp(propId);
               this._sortOrder = (java.lang.Integer)value;
               
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
     * 等级: LEVEL
     */
    public final java.lang.Integer getLevel(){
         onPropGet(PROP_ID_level);
         return _level;
    }

    /**
     * 等级: LEVEL
     */
    public final void setLevel(java.lang.Integer value){
        if(onPropSet(PROP_ID_level,value)){
            this._level = value;
            internalClearRefs(PROP_ID_level);
            
        }
    }
    
    /**
     * 等级名称: NAME
     */
    public final java.lang.String getName(){
         onPropGet(PROP_ID_name);
         return _name;
    }

    /**
     * 等级名称: NAME
     */
    public final void setName(java.lang.String value){
        if(onPropSet(PROP_ID_name,value)){
            this._name = value;
            internalClearRefs(PROP_ID_name);
            
        }
    }
    
    /**
     * 升级阈值(累计消费): UPGRADE_THRESHOLD
     */
    public final java.math.BigDecimal getUpgradeThreshold(){
         onPropGet(PROP_ID_upgradeThreshold);
         return _upgradeThreshold;
    }

    /**
     * 升级阈值(累计消费): UPGRADE_THRESHOLD
     */
    public final void setUpgradeThreshold(java.math.BigDecimal value){
        if(onPropSet(PROP_ID_upgradeThreshold,value)){
            this._upgradeThreshold = value;
            internalClearRefs(PROP_ID_upgradeThreshold);
            
        }
    }
    
    /**
     * 保级阈值(周期内累计消费): DOWNGRADE_THRESHOLD
     */
    public final java.math.BigDecimal getDowngradeThreshold(){
         onPropGet(PROP_ID_downgradeThreshold);
         return _downgradeThreshold;
    }

    /**
     * 保级阈值(周期内累计消费): DOWNGRADE_THRESHOLD
     */
    public final void setDowngradeThreshold(java.math.BigDecimal value){
        if(onPropSet(PROP_ID_downgradeThreshold,value)){
            this._downgradeThreshold = value;
            internalClearRefs(PROP_ID_downgradeThreshold);
            
        }
    }
    
    /**
     * 权益配置(JSON): BENEFITS
     */
    public final java.lang.String getBenefits(){
         onPropGet(PROP_ID_benefits);
         return _benefits;
    }

    /**
     * 权益配置(JSON): BENEFITS
     */
    public final void setBenefits(java.lang.String value){
        if(onPropSet(PROP_ID_benefits,value)){
            this._benefits = value;
            internalClearRefs(PROP_ID_benefits);
            
        }
    }
    
    /**
     * 排序: SORT_ORDER
     */
    public final java.lang.Integer getSortOrder(){
         onPropGet(PROP_ID_sortOrder);
         return _sortOrder;
    }

    /**
     * 排序: SORT_ORDER
     */
    public final void setSortOrder(java.lang.Integer value){
        if(onPropSet(PROP_ID_sortOrder,value)){
            this._sortOrder = value;
            internalClearRefs(PROP_ID_sortOrder);
            
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
    
   private io.nop.orm.component.JsonOrmComponent _benefitsComponent;

   private static Map<String,Integer> COMPONENT_PROP_ID_MAP_benefitsComponent = new HashMap<>();
   static{
      
         COMPONENT_PROP_ID_MAP_benefitsComponent.put(io.nop.orm.component.JsonOrmComponent.PROP_NAME__jsonText,PROP_ID_benefits);
      
   }

   public final io.nop.orm.component.JsonOrmComponent getBenefitsComponent(){
      if(_benefitsComponent == null){
          _benefitsComponent = new io.nop.orm.component.JsonOrmComponent();
          _benefitsComponent.bindToEntity(this, COMPONENT_PROP_ID_MAP_benefitsComponent);
      }
      return _benefitsComponent;
   }

}
// resume CPD analysis - CPD-ON
