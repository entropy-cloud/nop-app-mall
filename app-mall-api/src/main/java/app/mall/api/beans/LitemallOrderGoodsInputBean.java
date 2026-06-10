//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    import io.nop.api.core.api.CrudInputBase;
    
    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallOrderGoodsInputBean extends CrudInputBase {

    
        private String _orderId;

    
        @PropMeta(propId=2)
    
        public String getOrderId(){
            return _orderId;
        }

        public void setOrderId(String value){
            this._orderId = value;
        }


        private String _goodsId;

    
        @PropMeta(propId=3)
    
        public String getGoodsId(){
            return _goodsId;
        }

        public void setGoodsId(String value){
            this._goodsId = value;
        }


        private String _goodsName;

    
        @PropMeta(propId=4)
    
        public String getGoodsName(){
            return _goodsName;
        }

        public void setGoodsName(String value){
            this._goodsName = value;
        }


        private String _goodsSn;

    
        @PropMeta(propId=5)
    
        public String getGoodsSn(){
            return _goodsSn;
        }

        public void setGoodsSn(String value){
            this._goodsSn = value;
        }


        private String _productId;

    
        @PropMeta(propId=6)
    
        public String getProductId(){
            return _productId;
        }

        public void setProductId(String value){
            this._productId = value;
        }


        private Integer _number;

    
        @PropMeta(propId=7)
    
        public Integer getNumber(){
            return _number;
        }

        public void setNumber(Integer value){
            this._number = value;
        }


        private java.math.BigDecimal _price;

    
        @PropMeta(propId=8)
    
        public java.math.BigDecimal getPrice(){
            return _price;
        }

        public void setPrice(java.math.BigDecimal value){
            this._price = value;
        }


        private String _specifications;

    
        @PropMeta(propId=9)
    
        public String getSpecifications(){
            return _specifications;
        }

        public void setSpecifications(String value){
            this._specifications = value;
        }


        private String _picUrl;

    
        @PropMeta(propId=10)
    
        public String getPicUrl(){
            return _picUrl;
        }

        public void setPicUrl(String value){
            this._picUrl = value;
        }


        private Integer _comment;

    
        @PropMeta(propId=11)
    
        public Integer getComment(){
            return _comment;
        }

        public void setComment(Integer value){
            this._comment = value;
        }


        private Boolean _deleted;

    
        @PropMeta(propId=14)
    
        public Boolean getDeleted(){
            return _deleted;
        }

        public void setDeleted(Boolean value){
            this._deleted = value;
        }


    }
