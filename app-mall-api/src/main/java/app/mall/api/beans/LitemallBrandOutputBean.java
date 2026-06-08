//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    
    import java.util.Map;

    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallBrandOutputBean {

    
        private Integer _id;

    
        @PropMeta(propId=1)
    
        public Integer getId(){
            return _id;
        }

        public void setId(Integer value){
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


        private String _picUrl;

    
        @PropMeta(propId=3)
    
        public String getPicUrl(){
            return _picUrl;
        }

        public void setPicUrl(String value){
            this._picUrl = value;
        }


        private String _desc;

    
        @PropMeta(propId=4)
    
        public String getDesc(){
            return _desc;
        }

        public void setDesc(String value){
            this._desc = value;
        }


        private Byte _sortOrder;

    
        @PropMeta(propId=5)
    
        public Byte getSortOrder(){
            return _sortOrder;
        }

        public void setSortOrder(Byte value){
            this._sortOrder = value;
        }


        private java.math.BigDecimal _floorPrice;

    
        @PropMeta(propId=6)
    
        public java.math.BigDecimal getFloorPrice(){
            return _floorPrice;
        }

        public void setFloorPrice(java.math.BigDecimal value){
            this._floorPrice = value;
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


        private io.nop.api.core.beans.file.FileStatusBean _picUrlComponentFileStatus;

    
        public io.nop.api.core.beans.file.FileStatusBean getPicUrlComponentFileStatus(){
            return _picUrlComponentFileStatus;
        }

        public void setPicUrlComponentFileStatus(io.nop.api.core.beans.file.FileStatusBean value){
            this._picUrlComponentFileStatus = value;
        }


    }
