//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    import io.nop.api.core.api.CrudInputBase;
    
    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallAddressInputBean extends CrudInputBase {

    
        private String _name;

    
        @PropMeta(propId=2)
    
        public String getName(){
            return _name;
        }

        public void setName(String value){
            this._name = value;
        }


        private Integer _userId;

    
        @PropMeta(propId=3)
    
        public Integer getUserId(){
            return _userId;
        }

        public void setUserId(Integer value){
            this._userId = value;
        }


        private String _province;

    
        @PropMeta(propId=4)
    
        public String getProvince(){
            return _province;
        }

        public void setProvince(String value){
            this._province = value;
        }


        private String _city;

    
        @PropMeta(propId=5)
    
        public String getCity(){
            return _city;
        }

        public void setCity(String value){
            this._city = value;
        }


        private String _county;

    
        @PropMeta(propId=6)
    
        public String getCounty(){
            return _county;
        }

        public void setCounty(String value){
            this._county = value;
        }


        private String _addressDetail;

    
        @PropMeta(propId=7)
    
        public String getAddressDetail(){
            return _addressDetail;
        }

        public void setAddressDetail(String value){
            this._addressDetail = value;
        }


        private String _areaCode;

    
        @PropMeta(propId=8)
    
        public String getAreaCode(){
            return _areaCode;
        }

        public void setAreaCode(String value){
            this._areaCode = value;
        }


        private String _postalCode;

    
        @PropMeta(propId=9)
    
        public String getPostalCode(){
            return _postalCode;
        }

        public void setPostalCode(String value){
            this._postalCode = value;
        }


        private String _tel;

    
        @PropMeta(propId=10)
    
        public String getTel(){
            return _tel;
        }

        public void setTel(String value){
            this._tel = value;
        }


        private Boolean _isDefault;

    
        @PropMeta(propId=11)
    
        public Boolean getIsDefault(){
            return _isDefault;
        }

        public void setIsDefault(Boolean value){
            this._isDefault = value;
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
