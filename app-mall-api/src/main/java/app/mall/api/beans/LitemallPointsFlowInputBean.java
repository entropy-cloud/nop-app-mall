//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    import io.nop.api.core.api.CrudInputBase;
    
    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallPointsFlowInputBean extends CrudInputBase {

    
        private String _accountId;

    
        @PropMeta(propId=2)
    
        public String getAccountId(){
            return _accountId;
        }

        public void setAccountId(String value){
            this._accountId = value;
        }


        private String _userId;

    
        @PropMeta(propId=3)
    
        public String getUserId(){
            return _userId;
        }

        public void setUserId(String value){
            this._userId = value;
        }


        private Integer _changeType;

    
        @PropMeta(propId=4)
    
        public Integer getChangeType(){
            return _changeType;
        }

        public void setChangeType(Integer value){
            this._changeType = value;
        }


        private Integer _changeAmount;

    
        @PropMeta(propId=5)
    
        public Integer getChangeAmount(){
            return _changeAmount;
        }

        public void setChangeAmount(Integer value){
            this._changeAmount = value;
        }


        private Integer _balanceAfter;

    
        @PropMeta(propId=6)
    
        public Integer getBalanceAfter(){
            return _balanceAfter;
        }

        public void setBalanceAfter(Integer value){
            this._balanceAfter = value;
        }


        private String _sourceType;

    
        @PropMeta(propId=7)
    
        public String getSourceType(){
            return _sourceType;
        }

        public void setSourceType(String value){
            this._sourceType = value;
        }


        private String _sourceId;

    
        @PropMeta(propId=8)
    
        public String getSourceId(){
            return _sourceId;
        }

        public void setSourceId(String value){
            this._sourceId = value;
        }


        private String _remark;

    
        @PropMeta(propId=9)
    
        public String getRemark(){
            return _remark;
        }

        public void setRemark(String value){
            this._remark = value;
        }


    }
