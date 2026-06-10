//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    import java.util.List;
    
    import java.util.Map;

    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallUserOutputBean {

    
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


        private Integer _gender;

    
        @PropMeta(propId=4)
    
        public Integer getGender(){
            return _gender;
        }

        public void setGender(Integer value){
            this._gender = value;
        }


        private String _gender_label;

    
        public String getGender_label(){
            return _gender_label;
        }

        public void setGender_label(String value){
            this._gender_label = value;
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


        private String _userLevel_label;

    
        public String getUserLevel_label(){
            return _userLevel_label;
        }

        public void setUserLevel_label(String value){
            this._userLevel_label = value;
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


        private String _status_label;

    
        public String getStatus_label(){
            return _status_label;
        }

        public void setStatus_label(String value){
            this._status_label = value;
        }


        private java.time.LocalDateTime _addTime;

    
        @PropMeta(propId=15)
    
        public java.time.LocalDateTime getAddTime(){
            return _addTime;
        }

        public void setAddTime(java.time.LocalDateTime value){
            this._addTime = value;
        }


        private java.time.LocalDateTime _updateTime;

    
        @PropMeta(propId=16)
    
        public java.time.LocalDateTime getUpdateTime(){
            return _updateTime;
        }

        public void setUpdateTime(java.time.LocalDateTime value){
            this._updateTime = value;
        }


        private Boolean _deleted;

    
        @PropMeta(propId=17)
    
        public Boolean getDeleted(){
            return _deleted;
        }

        public void setDeleted(Boolean value){
            this._deleted = value;
        }


        private io.nop.api.core.beans.file.FileStatusBean _avatarComponentFileStatus;

    
        public io.nop.api.core.beans.file.FileStatusBean getAvatarComponentFileStatus(){
            return _avatarComponentFileStatus;
        }

        public void setAvatarComponentFileStatus(io.nop.api.core.beans.file.FileStatusBean value){
            this._avatarComponentFileStatus = value;
        }


        private java.util.List<java.lang.String> _relatedRoleList_ids;

    
        public java.util.List<java.lang.String> getRelatedRoleList_ids(){
            return _relatedRoleList_ids;
        }

        public void setRelatedRoleList_ids(java.util.List<java.lang.String> value){
            this._relatedRoleList_ids = value;
        }


        private String _relatedRoleList_label;

    
        public String getRelatedRoleList_label(){
            return _relatedRoleList_label;
        }

        public void setRelatedRoleList_label(String value){
            this._relatedRoleList_label = value;
        }


        private List<Map<String,Object>> _roleMappings;

        public List<Map<String,Object>> getRoleMappings(){
            return _roleMappings;
        }

        public void setRoleMappings(List<Map<String,Object>> value){
            this._roleMappings = value;
        }


        private List<Map<String,Object>> _relatedRoleList;

        public List<Map<String,Object>> getRelatedRoleList(){
            return _relatedRoleList;
        }

        public void setRelatedRoleList(List<Map<String,Object>> value){
            this._relatedRoleList = value;
        }


    }
