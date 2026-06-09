//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    
    import java.util.Map;

    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallCartOutputBean {

    
        private String _id;

    
        @PropMeta(propId=1)
    
        public String getId(){
            return _id;
        }

        public void setId(String value){
            this._id = value;
        }


        private String _userId;

    
        @PropMeta(propId=2)
    
        public String getUserId(){
            return _userId;
        }

        public void setUserId(String value){
            this._userId = value;
        }


        private String _goodsId;

    
        @PropMeta(propId=3)
    
        public String getGoodsId(){
            return _goodsId;
        }

        public void setGoodsId(String value){
            this._goodsId = value;
        }


        private String _goodsSn;

    
        @PropMeta(propId=4)
    
        public String getGoodsSn(){
            return _goodsSn;
        }

        public void setGoodsSn(String value){
            this._goodsSn = value;
        }


        private String _goodsName;

    
        @PropMeta(propId=5)
    
        public String getGoodsName(){
            return _goodsName;
        }

        public void setGoodsName(String value){
            this._goodsName = value;
        }


        private String _productId;

    
        @PropMeta(propId=6)
    
        public String getProductId(){
            return _productId;
        }

        public void setProductId(String value){
            this._productId = value;
        }


        private java.math.BigDecimal _price;

    
        @PropMeta(propId=7)
    
        public java.math.BigDecimal getPrice(){
            return _price;
        }

        public void setPrice(java.math.BigDecimal value){
            this._price = value;
        }


        private Short _number;

    
        @PropMeta(propId=8)
    
        public Short getNumber(){
            return _number;
        }

        public void setNumber(Short value){
            this._number = value;
        }


        private String _specifications;

    
        @PropMeta(propId=9)
    
        public String getSpecifications(){
            return _specifications;
        }

        public void setSpecifications(String value){
            this._specifications = value;
        }


        private Boolean _checked;

    
        @PropMeta(propId=10)
    
        public Boolean getChecked(){
            return _checked;
        }

        public void setChecked(Boolean value){
            this._checked = value;
        }


        private String _picUrl;

    
        @PropMeta(propId=11)
    
        public String getPicUrl(){
            return _picUrl;
        }

        public void setPicUrl(String value){
            this._picUrl = value;
        }


        private java.time.LocalDateTime _addTime;

    
        @PropMeta(propId=12)
    
        public java.time.LocalDateTime getAddTime(){
            return _addTime;
        }

        public void setAddTime(java.time.LocalDateTime value){
            this._addTime = value;
        }


        private java.time.LocalDateTime _updateTime;

    
        @PropMeta(propId=13)
    
        public java.time.LocalDateTime getUpdateTime(){
            return _updateTime;
        }

        public void setUpdateTime(java.time.LocalDateTime value){
            this._updateTime = value;
        }


        private Boolean _deleted;

    
        @PropMeta(propId=14)
    
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


        private Map<String,Object> _user;

        public Map<String,Object> getUser(){
            return _user;
        }

        public void setUser(Map<String,Object> value){
            this._user = value;
        }


    }
