//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    import io.nop.api.core.api.CrudInputBase;
    
    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallPickupStoreInputBean extends CrudInputBase {

    
        private String _name;

    
        @PropMeta(propId=2)
    
        public String getName(){
            return _name;
        }

        public void setName(String value){
            this._name = value;
        }


        private String _address;

    
        @PropMeta(propId=3)
    
        public String getAddress(){
            return _address;
        }

        public void setAddress(String value){
            this._address = value;
        }


        private String _contact;

    
        @PropMeta(propId=4)
    
        public String getContact(){
            return _contact;
        }

        public void setContact(String value){
            this._contact = value;
        }


        private String _phone;

    
        @PropMeta(propId=5)
    
        public String getPhone(){
            return _phone;
        }

        public void setPhone(String value){
            this._phone = value;
        }


        private java.math.BigDecimal _latitude;

    
        @PropMeta(propId=6)
    
        public java.math.BigDecimal getLatitude(){
            return _latitude;
        }

        public void setLatitude(java.math.BigDecimal value){
            this._latitude = value;
        }


        private java.math.BigDecimal _longitude;

    
        @PropMeta(propId=7)
    
        public java.math.BigDecimal getLongitude(){
            return _longitude;
        }

        public void setLongitude(java.math.BigDecimal value){
            this._longitude = value;
        }


        private String _openingHours;

    
        @PropMeta(propId=8)
    
        public String getOpeningHours(){
            return _openingHours;
        }

        public void setOpeningHours(String value){
            this._openingHours = value;
        }


        private Integer _status;

    
        @PropMeta(propId=9)
    
        public Integer getStatus(){
            return _status;
        }

        public void setStatus(Integer value){
            this._status = value;
        }


        private String _remark;

    
        @PropMeta(propId=10)
    
        public String getRemark(){
            return _remark;
        }

        public void setRemark(String value){
            this._remark = value;
        }


        private Boolean _deleted;

    
        @PropMeta(propId=13)
    
        public Boolean getDeleted(){
            return _deleted;
        }

        public void setDeleted(Boolean value){
            this._deleted = value;
        }


    }
