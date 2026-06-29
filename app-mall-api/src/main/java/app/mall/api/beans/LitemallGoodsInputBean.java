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
    public class LitemallGoodsInputBean extends CrudInputBase {

    
        private java.util.List<java.lang.String> _relatedProductList_ids;

    
        public java.util.List<java.lang.String> getRelatedProductList_ids(){
            return _relatedProductList_ids;
        }

        public void setRelatedProductList_ids(java.util.List<java.lang.String> value){
            this._relatedProductList_ids = value;
        }


        private String _name;

    
        @PropMeta(propId=3)
    
        public String getName(){
            return _name;
        }

        public void setName(String value){
            this._name = value;
        }


        private String _categoryId;

    
        @PropMeta(propId=4)
    
        public String getCategoryId(){
            return _categoryId;
        }

        public void setCategoryId(String value){
            this._categoryId = value;
        }


        private String _brandId;

    
        @PropMeta(propId=5)
    
        public String getBrandId(){
            return _brandId;
        }

        public void setBrandId(String value){
            this._brandId = value;
        }


        private java.util.List<java.lang.String> _gallery;

    
        @PropMeta(propId=6)
    
        public java.util.List<java.lang.String> getGallery(){
            return _gallery;
        }

        public void setGallery(java.util.List<java.lang.String> value){
            this._gallery = value;
        }


        private String _keywords;

    
        @PropMeta(propId=7)
    
        public String getKeywords(){
            return _keywords;
        }

        public void setKeywords(String value){
            this._keywords = value;
        }


        private String _brief;

    
        @PropMeta(propId=8)
    
        public String getBrief(){
            return _brief;
        }

        public void setBrief(String value){
            this._brief = value;
        }


        private Boolean _isOnSale;

    
        @PropMeta(propId=9)
    
        public Boolean getIsOnSale(){
            return _isOnSale;
        }

        public void setIsOnSale(Boolean value){
            this._isOnSale = value;
        }


        private Integer _sortOrder;

    
        @PropMeta(propId=10)
    
        public Integer getSortOrder(){
            return _sortOrder;
        }

        public void setSortOrder(Integer value){
            this._sortOrder = value;
        }


        private String _picUrl;

    
        @PropMeta(propId=11)
    
        public String getPicUrl(){
            return _picUrl;
        }

        public void setPicUrl(String value){
            this._picUrl = value;
        }


        private String _shareUrl;

    
        @PropMeta(propId=12)
    
        public String getShareUrl(){
            return _shareUrl;
        }

        public void setShareUrl(String value){
            this._shareUrl = value;
        }


        private Boolean _isNew;

    
        @PropMeta(propId=13)
    
        public Boolean getIsNew(){
            return _isNew;
        }

        public void setIsNew(Boolean value){
            this._isNew = value;
        }


        private Boolean _isHot;

    
        @PropMeta(propId=14)
    
        public Boolean getIsHot(){
            return _isHot;
        }

        public void setIsHot(Boolean value){
            this._isHot = value;
        }


        private String _unit;

    
        @PropMeta(propId=15)
    
        public String getUnit(){
            return _unit;
        }

        public void setUnit(String value){
            this._unit = value;
        }


        private java.math.BigDecimal _counterPrice;

    
        @PropMeta(propId=16)
    
        public java.math.BigDecimal getCounterPrice(){
            return _counterPrice;
        }

        public void setCounterPrice(java.math.BigDecimal value){
            this._counterPrice = value;
        }


        private java.math.BigDecimal _retailPrice;

    
        @PropMeta(propId=17)
    
        public java.math.BigDecimal getRetailPrice(){
            return _retailPrice;
        }

        public void setRetailPrice(java.math.BigDecimal value){
            this._retailPrice = value;
        }


        private String _detail;

    
        @PropMeta(propId=18)
    
        public String getDetail(){
            return _detail;
        }

        public void setDetail(String value){
            this._detail = value;
        }


        private Boolean _deleted;

    
        @PropMeta(propId=21)
    
        public Boolean getDeleted(){
            return _deleted;
        }

        public void setDeleted(Boolean value){
            this._deleted = value;
        }


        private Boolean _isRecommend;

    
        @PropMeta(propId=22)
    
        public Boolean getIsRecommend(){
            return _isRecommend;
        }

        public void setIsRecommend(Boolean value){
            this._isRecommend = value;
        }


        private String _videoUrl;

    
        @PropMeta(propId=23)
    
        public String getVideoUrl(){
            return _videoUrl;
        }

        public void setVideoUrl(String value){
            this._videoUrl = value;
        }


        private Integer _safetyStock;

    
        @PropMeta(propId=24)
    
        public Integer getSafetyStock(){
            return _safetyStock;
        }

        public void setSafetyStock(Integer value){
            this._safetyStock = value;
        }


        private java.math.BigDecimal _costPrice;

    
        @PropMeta(propId=25)
    
        public java.math.BigDecimal getCostPrice(){
            return _costPrice;
        }

        public void setCostPrice(java.math.BigDecimal value){
            this._costPrice = value;
        }


        private String _goodsSn;

    
        @PropMeta(propId=2)
    
        public String getGoodsSn(){
            return _goodsSn;
        }

        public void setGoodsSn(String value){
            this._goodsSn = value;
        }


        private List<LitemallGoodsAttributeInputBean> _attributes;

        public List<LitemallGoodsAttributeInputBean> getAttributes(){
            return _attributes;
        }

        public void setAttributes(List<LitemallGoodsAttributeInputBean> value){
            this._attributes = value;
        }


        private List<LitemallGoodsProductInputBean> _products;

        public List<LitemallGoodsProductInputBean> getProducts(){
            return _products;
        }

        public void setProducts(List<LitemallGoodsProductInputBean> value){
            this._products = value;
        }


        private List<LitemallGoodsSpecificationInputBean> _specifications;

        public List<LitemallGoodsSpecificationInputBean> getSpecifications(){
            return _specifications;
        }

        public void setSpecifications(List<LitemallGoodsSpecificationInputBean> value){
            this._specifications = value;
        }


        private List<LitemallOrderGoodsInputBean> _orderGoods;

        public List<LitemallOrderGoodsInputBean> getOrderGoods(){
            return _orderGoods;
        }

        public void setOrderGoods(List<LitemallOrderGoodsInputBean> value){
            this._orderGoods = value;
        }


        private List<LitemallGoodsProductInputBean> _relatedProductList;

        public List<LitemallGoodsProductInputBean> getRelatedProductList(){
            return _relatedProductList;
        }

        public void setRelatedProductList(List<LitemallGoodsProductInputBean> value){
            this._relatedProductList = value;
        }


    }
