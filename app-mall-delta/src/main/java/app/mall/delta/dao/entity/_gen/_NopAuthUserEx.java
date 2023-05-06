package app.mall.delta.dao.entity._gen;

import io.nop.orm.model.IEntityModel;
import io.nop.orm.support.DynamicOrmEntity;
import io.nop.orm.support.OrmEntitySet; //NOPMD - suppressed UnusedImports - Auto Gen Code
import io.nop.orm.IOrmEntitySet; //NOPMD - suppressed UnusedImports - Auto Gen Code
import io.nop.api.core.convert.ConvertHelper;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;

import app.mall.delta.dao.entity.NopAuthUserEx;

// tell cpd to start ignoring code - CPD-OFF
/**
 *  用户: nop_auth_user
 */
@SuppressWarnings({"PMD.UselessOverridingMethod","PMD.UnusedLocalVariable",
        "PMD.UnnecessaryFullyQualifiedName","PMD.EmptyControlStatement"})
public class _NopAuthUserEx extends io.nop.auth.dao.entity.NopAuthUser{
    
    /* 关联用户ID: MALL_USER_ID INTEGER */
    public static final String PROP_NAME_mallUserId = "mallUserId";
    public static final int PROP_ID_mallUserId = 100;
    

    private static int _PROP_ID_BOUND = 101;

    
    /* relation: 关联用户 */
    public static final String PROP_NAME_mallUser = "mallUser";
    

    public static final List<String> PK_PROP_NAMES = Arrays.asList(PROP_NAME_userId);
    public static final int[] PK_PROP_IDS = new int[]{PROP_ID_userId};

    private static final String[] PROP_ID_TO_NAME = new String[101];
    private static final Map<String,Integer> PROP_NAME_TO_ID = new HashMap<>();
    static{
      
          PROP_ID_TO_NAME[PROP_ID_mallUserId] = PROP_NAME_mallUserId;
          PROP_NAME_TO_ID.put(PROP_NAME_mallUserId, PROP_ID_mallUserId);
      
    }

    
    /* 关联用户ID: MALL_USER_ID */
    private java.lang.Integer _mallUserId;
    

    public _NopAuthUserEx(){
    }

    protected NopAuthUserEx newInstance(){
       return new NopAuthUserEx();
    }

    @Override
    public NopAuthUserEx cloneInstance() {
        NopAuthUserEx entity = newInstance();
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
      return "io.nop.auth.dao.entity.NopAuthUser";
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
    
        return buildSimpleId(PROP_ID_userId);
     
    }

    @Override
    public boolean orm_isPrimary(int propId) {
        
            return propId == PROP_ID_userId;
          
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
        
            case PROP_ID_mallUserId:
               return getMallUserId();
        
           default:
              return super.orm_propValue(propId);
        }
    }

    

    @Override
    public void orm_propValue(int propId, Object value){
        switch(propId){
        
            case PROP_ID_mallUserId:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_mallUserId));
               }
               setMallUserId(typedValue);
               break;
            }
        
           default:
              super.orm_propValue(propId,value);
        }
    }

    @Override
    public void orm_internalSet(int propId, Object value) {
        switch(propId){
        
            case PROP_ID_mallUserId:{
               onInitProp(propId);
               this._mallUserId = (java.lang.Integer)value;
               
               break;
            }
        
           default:
              super.orm_internalSet(propId,value);
        }
    }

    
    /**
     * 关联用户ID: MALL_USER_ID
     */
    public java.lang.Integer getMallUserId(){
         onPropGet(PROP_ID_mallUserId);
         return _mallUserId;
    }

    /**
     * 关联用户ID: MALL_USER_ID
     */
    public void setMallUserId(java.lang.Integer value){
        if(onPropSet(PROP_ID_mallUserId,value)){
            this._mallUserId = value;
            internalClearRefs(PROP_ID_mallUserId);
            
        }
    }
    
    /**
     * 关联用户
     */
    public app.mall.dao.entity.LitemallUser getMallUser(){
       return (app.mall.dao.entity.LitemallUser)internalGetRefEntity(PROP_NAME_mallUser);
    }

    public void setMallUser(app.mall.dao.entity.LitemallUser refEntity){
       if(refEntity == null){
         
         this.setMallUserId(null);
         
       }else{
          internalSetRefEntity(PROP_NAME_mallUser, refEntity,()->{
             
              this.orm_propValue(PROP_ID_mallUserId,
                refEntity.getId());
                
          });
       }
    }
       
}
// resume CPD analysis - CPD-ON
