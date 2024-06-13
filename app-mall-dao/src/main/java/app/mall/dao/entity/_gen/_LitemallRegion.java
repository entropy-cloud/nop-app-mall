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

import app.mall.dao.entity.LitemallRegion;

// tell cpd to start ignoring code - CPD-OFF
/**
 *  行政区域表: litemall_region
 */
@SuppressWarnings({"PMD.UselessOverridingMethod","PMD.UnusedLocalVariable","java:S3008","java:S1602","java:S1128","java:S1161",
        "PMD.UnnecessaryFullyQualifiedName","PMD.EmptyControlStatement","java:S116","java:S115","java:S101","java:S3776"})
public class _LitemallRegion extends DynamicOrmEntity{
    
    /* Id: ID INTEGER */
    public static final String PROP_NAME_id = "id";
    public static final int PROP_ID_id = 1;
    
    /* 行政区域父ID: PID INTEGER */
    public static final String PROP_NAME_pid = "pid";
    public static final int PROP_ID_pid = 2;
    
    /* 行政区域名称: NAME VARCHAR */
    public static final String PROP_NAME_name = "name";
    public static final int PROP_ID_name = 3;
    
    /* 行政区域类型: TYPE TINYINT */
    public static final String PROP_NAME_type = "type";
    public static final int PROP_ID_type = 4;
    
    /* 行政区域编码: CODE INTEGER */
    public static final String PROP_NAME_code = "code";
    public static final int PROP_ID_code = 5;
    

    private static int _PROP_ID_BOUND = 6;

    
    /* relation: 父区域 */
    public static final String PROP_NAME_parent = "parent";
    
    /* relation: 子区域 */
    public static final String PROP_NAME_children = "children";
    

    protected static final List<String> PK_PROP_NAMES = Arrays.asList(PROP_NAME_id);
    protected static final int[] PK_PROP_IDS = new int[]{PROP_ID_id};

    private static final String[] PROP_ID_TO_NAME = new String[6];
    private static final Map<String,Integer> PROP_NAME_TO_ID = new HashMap<>();
    static{
      
          PROP_ID_TO_NAME[PROP_ID_id] = PROP_NAME_id;
          PROP_NAME_TO_ID.put(PROP_NAME_id, PROP_ID_id);
      
          PROP_ID_TO_NAME[PROP_ID_pid] = PROP_NAME_pid;
          PROP_NAME_TO_ID.put(PROP_NAME_pid, PROP_ID_pid);
      
          PROP_ID_TO_NAME[PROP_ID_name] = PROP_NAME_name;
          PROP_NAME_TO_ID.put(PROP_NAME_name, PROP_ID_name);
      
          PROP_ID_TO_NAME[PROP_ID_type] = PROP_NAME_type;
          PROP_NAME_TO_ID.put(PROP_NAME_type, PROP_ID_type);
      
          PROP_ID_TO_NAME[PROP_ID_code] = PROP_NAME_code;
          PROP_NAME_TO_ID.put(PROP_NAME_code, PROP_ID_code);
      
    }

    
    /* Id: ID */
    private java.lang.Integer _id;
    
    /* 行政区域父ID: PID */
    private java.lang.Integer _pid;
    
    /* 行政区域名称: NAME */
    private java.lang.String _name;
    
    /* 行政区域类型: TYPE */
    private java.lang.Byte _type;
    
    /* 行政区域编码: CODE */
    private java.lang.Integer _code;
    

    public _LitemallRegion(){
        // for debug
    }

    protected LitemallRegion newInstance(){
        LitemallRegion entity = new LitemallRegion();
        entity.orm_attach(orm_enhancer());
        entity.orm_entityModel(orm_entityModel());
        return entity;
    }

    @Override
    public LitemallRegion cloneInstance() {
        LitemallRegion entity = newInstance();
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
      return "app.mall.dao.entity.LitemallRegion";
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
        
            case PROP_ID_pid:
               return getPid();
        
            case PROP_ID_name:
               return getName();
        
            case PROP_ID_type:
               return getType();
        
            case PROP_ID_code:
               return getCode();
        
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
        
            case PROP_ID_pid:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_pid));
               }
               setPid(typedValue);
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
        
            case PROP_ID_type:{
               java.lang.Byte typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toByte(value,
                       err-> newTypeConversionError(PROP_NAME_type));
               }
               setType(typedValue);
               break;
            }
        
            case PROP_ID_code:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_code));
               }
               setCode(typedValue);
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
        
            case PROP_ID_pid:{
               onInitProp(propId);
               this._pid = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_name:{
               onInitProp(propId);
               this._name = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_type:{
               onInitProp(propId);
               this._type = (java.lang.Byte)value;
               
               break;
            }
        
            case PROP_ID_code:{
               onInitProp(propId);
               this._code = (java.lang.Integer)value;
               
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
     * 行政区域父ID: PID
     */
    public java.lang.Integer getPid(){
         onPropGet(PROP_ID_pid);
         return _pid;
    }

    /**
     * 行政区域父ID: PID
     */
    public void setPid(java.lang.Integer value){
        if(onPropSet(PROP_ID_pid,value)){
            this._pid = value;
            internalClearRefs(PROP_ID_pid);
            
        }
    }
    
    /**
     * 行政区域名称: NAME
     */
    public java.lang.String getName(){
         onPropGet(PROP_ID_name);
         return _name;
    }

    /**
     * 行政区域名称: NAME
     */
    public void setName(java.lang.String value){
        if(onPropSet(PROP_ID_name,value)){
            this._name = value;
            internalClearRefs(PROP_ID_name);
            
        }
    }
    
    /**
     * 行政区域类型: TYPE
     */
    public java.lang.Byte getType(){
         onPropGet(PROP_ID_type);
         return _type;
    }

    /**
     * 行政区域类型: TYPE
     */
    public void setType(java.lang.Byte value){
        if(onPropSet(PROP_ID_type,value)){
            this._type = value;
            internalClearRefs(PROP_ID_type);
            
        }
    }
    
    /**
     * 行政区域编码: CODE
     */
    public java.lang.Integer getCode(){
         onPropGet(PROP_ID_code);
         return _code;
    }

    /**
     * 行政区域编码: CODE
     */
    public void setCode(java.lang.Integer value){
        if(onPropSet(PROP_ID_code,value)){
            this._code = value;
            internalClearRefs(PROP_ID_code);
            
        }
    }
    
    /**
     * 父区域
     */
    public app.mall.dao.entity.LitemallRegion getParent(){
       return (app.mall.dao.entity.LitemallRegion)internalGetRefEntity(PROP_NAME_parent);
    }

    public void setParent(app.mall.dao.entity.LitemallRegion refEntity){
   
           if(refEntity == null){
           
                   this.setPid(null);
               
           }else{
           internalSetRefEntity(PROP_NAME_parent, refEntity,()->{
           
                           this.setPid(refEntity.getId());
                       
           });
           }
       
    }
       
    private final OrmEntitySet<app.mall.dao.entity.LitemallRegion> _children = new OrmEntitySet<>(this, PROP_NAME_children,
        app.mall.dao.entity.LitemallRegion.PROP_NAME_parent, null,app.mall.dao.entity.LitemallRegion.class);

    /**
     * 子区域。 refPropName: parent, keyProp: {rel.keyProp}
     */
    public IOrmEntitySet<app.mall.dao.entity.LitemallRegion> getChildren(){
       return _children;
    }
       
}
// resume CPD analysis - CPD-ON
