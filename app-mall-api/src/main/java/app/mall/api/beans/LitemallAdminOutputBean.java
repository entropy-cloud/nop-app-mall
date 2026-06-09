//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    
    import java.util.Map;

    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallAdminOutputBean {

    
        private String _id;

    
        @PropMeta(propId=1)
    
        public String getId(){
            return _id;
        }

        public void setId(String value){
            this._id = value;
        }


        private String _username;

    
        @PropMeta(propId=2)
    
        public String getUsername(){
            return _username;
        }

        public void setUsername(String value){
            this._username = value;
        }


        private String _password;

    
        @PropMeta(propId=3)
    
        public String getPassword(){
            return _password;
        }

        public void setPassword(String value){
            this._password = value;
        }


        private String _lastLoginIp;

    
        @PropMeta(propId=4)
    
        public String getLastLoginIp(){
            return _lastLoginIp;
        }

        public void setLastLoginIp(String value){
            this._lastLoginIp = value;
        }


        private java.time.LocalDateTime _lastLoginTime;

    
        @PropMeta(propId=5)
    
        public java.time.LocalDateTime getLastLoginTime(){
            return _lastLoginTime;
        }

        public void setLastLoginTime(java.time.LocalDateTime value){
            this._lastLoginTime = value;
        }


        private String _avatar;

    
        @PropMeta(propId=6)
    
        public String getAvatar(){
            return _avatar;
        }

        public void setAvatar(String value){
            this._avatar = value;
        }


        private java.time.LocalDateTime _addTime;

    
        @PropMeta(propId=7)
    
        public java.time.LocalDateTime getAddTime(){
            return _addTime;
        }

        public void setAddTime(java.time.LocalDateTime value){
            this._addTime = value;
        }


        private java.time.LocalDateTime _updateTime;

    
        @PropMeta(propId=8)
    
        public java.time.LocalDateTime getUpdateTime(){
            return _updateTime;
        }

        public void setUpdateTime(java.time.LocalDateTime value){
            this._updateTime = value;
        }


        private Boolean _deleted;

    
        @PropMeta(propId=9)
    
        public Boolean getDeleted(){
            return _deleted;
        }

        public void setDeleted(Boolean value){
            this._deleted = value;
        }


        private String _roleIds;

    
        @PropMeta(propId=10)
    
        public String getRoleIds(){
            return _roleIds;
        }

        public void setRoleIds(String value){
            this._roleIds = value;
        }


    }
