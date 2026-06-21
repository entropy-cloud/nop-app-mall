//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    import io.nop.api.core.api.CrudInputBase;
    
    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallWalletFlowInputBean extends CrudInputBase {

    
        private String _walletId;

    
        @PropMeta(propId=2)
    
        public String getWalletId(){
            return _walletId;
        }

        public void setWalletId(String value){
            this._walletId = value;
        }


        private Integer _changeType;

    
        @PropMeta(propId=3)
    
        public Integer getChangeType(){
            return _changeType;
        }

        public void setChangeType(Integer value){
            this._changeType = value;
        }


        private java.math.BigDecimal _changeAmount;

    
        @PropMeta(propId=4)
    
        public java.math.BigDecimal getChangeAmount(){
            return _changeAmount;
        }

        public void setChangeAmount(java.math.BigDecimal value){
            this._changeAmount = value;
        }


        private java.math.BigDecimal _balanceAfter;

    
        @PropMeta(propId=5)
    
        public java.math.BigDecimal getBalanceAfter(){
            return _balanceAfter;
        }

        public void setBalanceAfter(java.math.BigDecimal value){
            this._balanceAfter = value;
        }


        private String _sourceType;

    
        @PropMeta(propId=6)
    
        public String getSourceType(){
            return _sourceType;
        }

        public void setSourceType(String value){
            this._sourceType = value;
        }


        private String _sourceId;

    
        @PropMeta(propId=7)
    
        public String getSourceId(){
            return _sourceId;
        }

        public void setSourceId(String value){
            this._sourceId = value;
        }


        private String _remark;

    
        @PropMeta(propId=8)
    
        public String getRemark(){
            return _remark;
        }

        public void setRemark(String value){
            this._remark = value;
        }


    }
