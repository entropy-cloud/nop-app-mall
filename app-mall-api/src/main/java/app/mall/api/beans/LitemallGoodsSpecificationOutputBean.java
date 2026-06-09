//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    
    import java.util.Map;

    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallGoodsSpecificationOutputBean {

    
        private String _id;

    
        @PropMeta(propId=1)
    
        public String getId(){
            return _id;
        }

        public void setId(String value){
            this._id = value;
        }


        private String _goodsId;

    
        @PropMeta(propId=2)
    
        public String getGoodsId(){
            return _goodsId;
        }

        public void setGoodsId(String value){
            this._goodsId = value;
        }


        private String _specification;

    
        @PropMeta(propId=3)
    
        public String getSpecification(){
            return _specification;
        }

        public void setSpecification(String value){
            this._specification = value;
        }


        private String _value;

    
        @PropMeta(propId=4)
    
        public String getValue(){
            return _value;
        }

        public void setValue(String value){
            this._value = value;
        }


        private String _picUrl;

    
        @PropMeta(propId=5)
    
        public String getPicUrl(){
            return _picUrl;
        }

        public void setPicUrl(String value){
            this._picUrl = value;
        }


        private java.time.LocalDateTime _addTime;

    
        @PropMeta(propId=6)
    
        public java.time.LocalDateTime getAddTime(){
            return _addTime;
        }

        public void setAddTime(java.time.LocalDateTime value){
            this._addTime = value;
        }


        private java.time.LocalDateTime _updateTime;

    
        @PropMeta(propId=7)
    
        public java.time.LocalDateTime getUpdateTime(){
            return _updateTime;
        }

        public void setUpdateTime(java.time.LocalDateTime value){
            this._updateTime = value;
        }


        private Boolean _deleted;

    
        @PropMeta(propId=8)
    
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


        private Map<String,Object> _goods;

        public Map<String,Object> getGoods(){
            return _goods;
        }

        public void setGoods(Map<String,Object> value){
            this._goods = value;
        }


    }
