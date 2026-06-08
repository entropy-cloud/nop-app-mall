//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    
    import java.util.Map;

    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallPermissionOutputBean {

    
        private Integer _id;

    
        @PropMeta(propId=1)
    
        public Integer getId(){
            return _id;
        }

        public void setId(Integer value){
            this._id = value;
        }


        private Integer _roleId;

    
        @PropMeta(propId=2)
    
        public Integer getRoleId(){
            return _roleId;
        }

        public void setRoleId(Integer value){
            this._roleId = value;
        }


        private String _permission;

    
        @PropMeta(propId=3)
    
        public String getPermission(){
            return _permission;
        }

        public void setPermission(String value){
            this._permission = value;
        }


        private java.time.LocalDateTime _addTime;

    
        @PropMeta(propId=4)
    
        public java.time.LocalDateTime getAddTime(){
            return _addTime;
        }

        public void setAddTime(java.time.LocalDateTime value){
            this._addTime = value;
        }


        private java.time.LocalDateTime _updateTime;

    
        @PropMeta(propId=5)
    
        public java.time.LocalDateTime getUpdateTime(){
            return _updateTime;
        }

        public void setUpdateTime(java.time.LocalDateTime value){
            this._updateTime = value;
        }


        private Boolean _deleted;

    
        @PropMeta(propId=6)
    
        public Boolean getDeleted(){
            return _deleted;
        }

        public void setDeleted(Boolean value){
            this._deleted = value;
        }


    }
