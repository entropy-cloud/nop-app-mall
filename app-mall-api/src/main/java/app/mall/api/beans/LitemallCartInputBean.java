//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    import io.nop.api.core.api.CrudInputBase;
    
    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallCartInputBean extends CrudInputBase {

    
        private Integer _userId;

    
        @PropMeta(propId=2)
    
        public Integer getUserId(){
            return _userId;
        }

        public void setUserId(Integer value){
            this._userId = value;
        }


        private Integer _goodsId;

    
        @PropMeta(propId=3)
    
        public Integer getGoodsId(){
            return _goodsId;
        }

        public void setGoodsId(Integer value){
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


        private Integer _productId;

    
        @PropMeta(propId=6)
    
        public Integer getProductId(){
            return _productId;
        }

        public void setProductId(Integer value){
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


        private Boolean _deleted;

    
        @PropMeta(propId=14)
    
        public Boolean getDeleted(){
            return _deleted;
        }

        public void setDeleted(Boolean value){
            this._deleted = value;
        }


    }
