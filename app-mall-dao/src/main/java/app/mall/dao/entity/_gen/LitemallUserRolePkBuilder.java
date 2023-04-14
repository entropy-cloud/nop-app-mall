package app.mall.dao.entity._gen;

import io.nop.orm.support.OrmCompositePk;
import app.mall.dao.entity.LitemallUserRole;

/**
 * 用于生成复合主键的帮助类
 */
@SuppressWarnings({"PMD.UnnecessaryFullyQualifiedName"})
public class LitemallUserRolePkBuilder{
    private Object[] values = new Object[2];

   
    public LitemallUserRolePkBuilder setUserId(java.lang.String value){
        this.values[0] = value;
        return this;
    }
   
    public LitemallUserRolePkBuilder setRoleId(java.lang.String value){
        this.values[1] = value;
        return this;
    }
   

    public OrmCompositePk build(){
        return OrmCompositePk.buildNotNull(LitemallUserRole.PK_PROP_NAMES,values);
    }
}
