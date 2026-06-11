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
    public class LitemallRoleInputBean extends CrudInputBase {

    
        private String _id;

    
        @PropMeta(propId=1)
    
        public String getId(){
            return _id;
        }

        public void setId(String value){
            this._id = value;
        }


        private String _name;

    
        @PropMeta(propId=2)
    
        public String getName(){
            return _name;
        }

        public void setName(String value){
            this._name = value;
        }


        private String _desc;

    
        @PropMeta(propId=3)
    
        public String getDesc(){
            return _desc;
        }

        public void setDesc(String value){
            this._desc = value;
        }


        private Boolean _enabled;

    
        @PropMeta(propId=4)
    
        public Boolean getEnabled(){
            return _enabled;
        }

        public void setEnabled(Boolean value){
            this._enabled = value;
        }


        private java.time.LocalDateTime _addTime;

    
        @PropMeta(propId=5)
    
        public java.time.LocalDateTime getAddTime(){
            return _addTime;
        }

        public void setAddTime(java.time.LocalDateTime value){
            this._addTime = value;
        }


        private java.time.LocalDateTime _updateTime;

    
        @PropMeta(propId=6)
    
        public java.time.LocalDateTime getUpdateTime(){
            return _updateTime;
        }

        public void setUpdateTime(java.time.LocalDateTime value){
            this._updateTime = value;
        }


        private Boolean _deleted;

    
        @PropMeta(propId=7)
    
        public Boolean getDeleted(){
            return _deleted;
        }

        public void setDeleted(Boolean value){
            this._deleted = value;
        }


        private java.util.List<java.lang.String> _relatedUserList_ids;

    
        public java.util.List<java.lang.String> getRelatedUserList_ids(){
            return _relatedUserList_ids;
        }

        public void setRelatedUserList_ids(java.util.List<java.lang.String> value){
            this._relatedUserList_ids = value;
        }


        private List<LitemallUserRoleInputBean> _userMappings;

        public List<LitemallUserRoleInputBean> getUserMappings(){
            return _userMappings;
        }

        public void setUserMappings(List<LitemallUserRoleInputBean> value){
            this._userMappings = value;
        }


        private List<LitemallUserInputBean> _relatedUserList;

        public List<LitemallUserInputBean> getRelatedUserList(){
            return _relatedUserList;
        }

        public void setRelatedUserList(List<LitemallUserInputBean> value){
            this._relatedUserList = value;
        }


    }
