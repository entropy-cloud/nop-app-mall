//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    
    import java.util.Map;

    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallTopicOutputBean {

    
        private String _id;

    
        @PropMeta(propId=1)
    
        public String getId(){
            return _id;
        }

        public void setId(String value){
            this._id = value;
        }


        private String _title;

    
        @PropMeta(propId=2)
    
        public String getTitle(){
            return _title;
        }

        public void setTitle(String value){
            this._title = value;
        }


        private String _subtitle;

    
        @PropMeta(propId=3)
    
        public String getSubtitle(){
            return _subtitle;
        }

        public void setSubtitle(String value){
            this._subtitle = value;
        }


        private String _content;

    
        @PropMeta(propId=4)
    
        public String getContent(){
            return _content;
        }

        public void setContent(String value){
            this._content = value;
        }


        private java.math.BigDecimal _price;

    
        @PropMeta(propId=5)
    
        public java.math.BigDecimal getPrice(){
            return _price;
        }

        public void setPrice(java.math.BigDecimal value){
            this._price = value;
        }


        private String _readCount;

    
        @PropMeta(propId=6)
    
        public String getReadCount(){
            return _readCount;
        }

        public void setReadCount(String value){
            this._readCount = value;
        }


        private String _picUrl;

    
        @PropMeta(propId=7)
    
        public String getPicUrl(){
            return _picUrl;
        }

        public void setPicUrl(String value){
            this._picUrl = value;
        }


        private Integer _sortOrder;

    
        @PropMeta(propId=8)
    
        public Integer getSortOrder(){
            return _sortOrder;
        }

        public void setSortOrder(Integer value){
            this._sortOrder = value;
        }


        private String _goods;

    
        @PropMeta(propId=9)
    
        public String getGoods(){
            return _goods;
        }

        public void setGoods(String value){
            this._goods = value;
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


    }
