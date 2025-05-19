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
@SuppressWarnings({"PMD.UselessOverridingMethod","PMD.UnusedLocalVariable","java:S3008","java:S1602","java:S1128","java:S1161",
        "PMD.UnnecessaryFullyQualifiedName","PMD.EmptyControlStatement","java:S116","java:S115","java:S101","java:S3776"})
public class _NopAuthUserEx extends io.nop.auth.dao.entity.NopAuthUser{
    
    /* 关联用户ID: MALL_USER_ID INTEGER */
    public static final String PROP_NAME_mallUserId = "mallUserId";
    public static final int PROP_ID_mallUserId = 100;
    
    /* 测试图片: PIC_URL VARCHAR */
    public static final String PROP_NAME_picUrl = "picUrl";
    public static final int PROP_ID_picUrl = 101;
    

    private static int _PROP_ID_BOUND = 102;

    
    /* relation: 关联用户 */
    public static final String PROP_NAME_mallUser = "mallUser";
    

    protected static final List<String> PK_PROP_NAMES = Arrays.asList(PROP_NAME_userId);
    protected static final int[] PK_PROP_IDS = new int[]{PROP_ID_userId};

    private static final String[] PROP_ID_TO_NAME = new String[102];
    private static final Map<String,Integer> PROP_NAME_TO_ID = new HashMap<>();
    static{
      
          PROP_ID_TO_NAME[PROP_ID_mallUserId] = PROP_NAME_mallUserId;
          PROP_NAME_TO_ID.put(PROP_NAME_mallUserId, PROP_ID_mallUserId);
      
          PROP_ID_TO_NAME[PROP_ID_picUrl] = PROP_NAME_picUrl;
          PROP_NAME_TO_ID.put(PROP_NAME_picUrl, PROP_ID_picUrl);
      
    }

    
    /* 关联用户ID: MALL_USER_ID */
    private java.lang.Integer _mallUserId;
    
    /* 测试图片: PIC_URL */
    private java.lang.String _picUrl;
    

    public _NopAuthUserEx(){
        // for debug
    }

    protected NopAuthUserEx newInstance(){
        NopAuthUserEx entity = new NopAuthUserEx();
        entity.orm_attach(orm_enhancer());
        entity.orm_entityModel(orm_entityModel());
        return entity;
    }

    @Override
    public NopAuthUserEx cloneInstance() {
        NopAuthUserEx entity = newInstance();
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
        
            case PROP_ID_picUrl:
               return getPicUrl();
        
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
        
            case PROP_ID_picUrl:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_picUrl));
               }
               setPicUrl(typedValue);
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
        
            case PROP_ID_picUrl:{
               onInitProp(propId);
               this._picUrl = (java.lang.String)value;
               
               break;
            }
        
           default:
              super.orm_internalSet(propId,value);
        }
    }

    
    /**
     * 关联用户ID: MALL_USER_ID
     */
    public final java.lang.Integer getMallUserId(){
         onPropGet(PROP_ID_mallUserId);
         return _mallUserId;
    }

    /**
     * 关联用户ID: MALL_USER_ID
     */
    public final void setMallUserId(java.lang.Integer value){
        if(onPropSet(PROP_ID_mallUserId,value)){
            this._mallUserId = value;
            internalClearRefs(PROP_ID_mallUserId);
            
        }
    }
    
    /**
     * 测试图片: PIC_URL
     */
    public final java.lang.String getPicUrl(){
         onPropGet(PROP_ID_picUrl);
         return _picUrl;
    }

    /**
     * 测试图片: PIC_URL
     */
    public final void setPicUrl(java.lang.String value){
        if(onPropSet(PROP_ID_picUrl,value)){
            this._picUrl = value;
            internalClearRefs(PROP_ID_picUrl);
            
        }
    }
    
    /**
     * 关联用户
     */
    public final app.mall.dao.entity.LitemallUser getMallUser(){
       return (app.mall.dao.entity.LitemallUser)internalGetRefEntity(PROP_NAME_mallUser);
    }

    public final void setMallUser(app.mall.dao.entity.LitemallUser refEntity){
   
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
