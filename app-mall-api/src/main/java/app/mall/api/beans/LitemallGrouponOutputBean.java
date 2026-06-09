//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    
    import java.util.Map;

    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallGrouponOutputBean {

    
        private String _id;

    
        @PropMeta(propId=1)
    
        public String getId(){
            return _id;
        }

        public void setId(String value){
            this._id = value;
        }


        private String _orderId;

    
        @PropMeta(propId=2)
    
        public String getOrderId(){
            return _orderId;
        }

        public void setOrderId(String value){
            this._orderId = value;
        }


        private String _grouponId;

    
        @PropMeta(propId=3)
    
        public String getGrouponId(){
            return _grouponId;
        }

        public void setGrouponId(String value){
            this._grouponId = value;
        }


        private String _rulesId;

    
        @PropMeta(propId=4)
    
        public String getRulesId(){
            return _rulesId;
        }

        public void setRulesId(String value){
            this._rulesId = value;
        }


        private String _userId;

    
        @PropMeta(propId=5)
    
        public String getUserId(){
            return _userId;
        }

        public void setUserId(String value){
            this._userId = value;
        }


        private String _shareUrl;

    
        @PropMeta(propId=6)
    
        public String getShareUrl(){
            return _shareUrl;
        }

        public void setShareUrl(String value){
            this._shareUrl = value;
        }


        private String _creatorUserId;

    
        @PropMeta(propId=7)
    
        public String getCreatorUserId(){
            return _creatorUserId;
        }

        public void setCreatorUserId(String value){
            this._creatorUserId = value;
        }


        private java.time.LocalDateTime _creatorUserTime;

    
        @PropMeta(propId=8)
    
        public java.time.LocalDateTime getCreatorUserTime(){
            return _creatorUserTime;
        }

        public void setCreatorUserTime(java.time.LocalDateTime value){
            this._creatorUserTime = value;
        }


        private Short _status;

    
        @PropMeta(propId=9)
    
        public Short getStatus(){
            return _status;
        }

        public void setStatus(Short value){
            this._status = value;
        }


        private java.time.LocalDateTime _addTime;

    
        @PropMeta(propId=10)
    
        public java.time.LocalDateTime getAddTime(){
            return _addTime;
        }

        public void setAddTime(java.time.LocalDateTime value){
            this._addTime = value;
        }


        private java.time.LocalDateTime _updateTime;

    
        @PropMeta(propId=11)
    
        public java.time.LocalDateTime getUpdateTime(){
            return _updateTime;
        }

        public void setUpdateTime(java.time.LocalDateTime value){
            this._updateTime = value;
        }


        private Boolean _deleted;

    
        @PropMeta(propId=12)
    
        public Boolean getDeleted(){
            return _deleted;
        }

        public void setDeleted(Boolean value){
            this._deleted = value;
        }


        private io.nop.api.core.beans.file.FileStatusBean _shareUrlComponentFileStatus;

    
        public io.nop.api.core.beans.file.FileStatusBean getShareUrlComponentFileStatus(){
            return _shareUrlComponentFileStatus;
        }

        public void setShareUrlComponentFileStatus(io.nop.api.core.beans.file.FileStatusBean value){
            this._shareUrlComponentFileStatus = value;
        }


        private Map<String,Object> _order;

        public Map<String,Object> getOrder(){
            return _order;
        }

        public void setOrder(Map<String,Object> value){
            this._order = value;
        }


    }
