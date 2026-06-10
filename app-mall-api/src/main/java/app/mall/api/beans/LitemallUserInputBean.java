//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    import io.nop.api.core.api.CrudInputBase;
    import java.util.List;
    
    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallUserInputBean extends CrudInputBase {

    
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


        private Integer _gender;

    
        @PropMeta(propId=4)
    
        public Integer getGender(){
            return _gender;
        }

        public void setGender(Integer value){
            this._gender = value;
        }


        private java.time.LocalDate _birthday;

    
        @PropMeta(propId=5)
    
        public java.time.LocalDate getBirthday(){
            return _birthday;
        }

        public void setBirthday(java.time.LocalDate value){
            this._birthday = value;
        }


        private java.time.LocalDateTime _lastLoginTime;

    
        @PropMeta(propId=6)
    
        public java.time.LocalDateTime getLastLoginTime(){
            return _lastLoginTime;
        }

        public void setLastLoginTime(java.time.LocalDateTime value){
            this._lastLoginTime = value;
        }


        private String _lastLoginIp;

    
        @PropMeta(propId=7)
    
        public String getLastLoginIp(){
            return _lastLoginIp;
        }

        public void setLastLoginIp(String value){
            this._lastLoginIp = value;
        }


        private Integer _userLevel;

    
        @PropMeta(propId=8)
    
        public Integer getUserLevel(){
            return _userLevel;
        }

        public void setUserLevel(Integer value){
            this._userLevel = value;
        }


        private String _nickname;

    
        @PropMeta(propId=9)
    
        public String getNickname(){
            return _nickname;
        }

        public void setNickname(String value){
            this._nickname = value;
        }


        private String _mobile;

    
        @PropMeta(propId=10)
    
        public String getMobile(){
            return _mobile;
        }

        public void setMobile(String value){
            this._mobile = value;
        }


        private String _avatar;

    
        @PropMeta(propId=11)
    
        public String getAvatar(){
            return _avatar;
        }

        public void setAvatar(String value){
            this._avatar = value;
        }


        private String _weixinOpenid;

    
        @PropMeta(propId=12)
    
        public String getWeixinOpenid(){
            return _weixinOpenid;
        }

        public void setWeixinOpenid(String value){
            this._weixinOpenid = value;
        }


        private String _sessionKey;

    
        @PropMeta(propId=13)
    
        public String getSessionKey(){
            return _sessionKey;
        }

        public void setSessionKey(String value){
            this._sessionKey = value;
        }


        private Integer _status;

    
        @PropMeta(propId=14)
    
        public Integer getStatus(){
            return _status;
        }

        public void setStatus(Integer value){
            this._status = value;
        }


        private Boolean _deleted;

    
        @PropMeta(propId=17)
    
        public Boolean getDeleted(){
            return _deleted;
        }

        public void setDeleted(Boolean value){
            this._deleted = value;
        }


        private java.util.List<java.lang.String> _relatedRoleList_ids;

    
        public java.util.List<java.lang.String> getRelatedRoleList_ids(){
            return _relatedRoleList_ids;
        }

        public void setRelatedRoleList_ids(java.util.List<java.lang.String> value){
            this._relatedRoleList_ids = value;
        }


        private List<LitemallUserRoleInputBean> _roleMappings;

        public List<LitemallUserRoleInputBean> getRoleMappings(){
            return _roleMappings;
        }

        public void setRoleMappings(List<LitemallUserRoleInputBean> value){
            this._roleMappings = value;
        }


        private List<LitemallRoleInputBean> _relatedRoleList;

        public List<LitemallRoleInputBean> getRelatedRoleList(){
            return _relatedRoleList;
        }

        public void setRelatedRoleList(List<LitemallRoleInputBean> value){
            this._relatedRoleList = value;
        }


    }
