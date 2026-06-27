//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    import io.nop.api.core.api.CrudInputBase;
    
    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallMemberLevelInputBean extends CrudInputBase {

    
        private Integer _level;

    
        @PropMeta(propId=2)
    
        public Integer getLevel(){
            return _level;
        }

        public void setLevel(Integer value){
            this._level = value;
        }


        private String _name;

    
        @PropMeta(propId=3)
    
        public String getName(){
            return _name;
        }

        public void setName(String value){
            this._name = value;
        }


        private java.math.BigDecimal _upgradeThreshold;

    
        @PropMeta(propId=4)
    
        public java.math.BigDecimal getUpgradeThreshold(){
            return _upgradeThreshold;
        }

        public void setUpgradeThreshold(java.math.BigDecimal value){
            this._upgradeThreshold = value;
        }


        private java.math.BigDecimal _downgradeThreshold;

    
        @PropMeta(propId=5)
    
        public java.math.BigDecimal getDowngradeThreshold(){
            return _downgradeThreshold;
        }

        public void setDowngradeThreshold(java.math.BigDecimal value){
            this._downgradeThreshold = value;
        }


        private String _benefits;

    
        @PropMeta(propId=6)
    
        public String getBenefits(){
            return _benefits;
        }

        public void setBenefits(String value){
            this._benefits = value;
        }


        private Integer _sortOrder;

    
        @PropMeta(propId=7)
    
        public Integer getSortOrder(){
            return _sortOrder;
        }

        public void setSortOrder(Integer value){
            this._sortOrder = value;
        }


        private String _remark;

    
        @PropMeta(propId=8)
    
        public String getRemark(){
            return _remark;
        }

        public void setRemark(String value){
            this._remark = value;
        }


        private Boolean _deleted;

    
        @PropMeta(propId=11)
    
        public Boolean getDeleted(){
            return _deleted;
        }

        public void setDeleted(Boolean value){
            this._deleted = value;
        }


    }
