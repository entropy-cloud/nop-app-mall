//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    import io.nop.api.core.api.CrudInputBase;
    
    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallPointsAccountInputBean extends CrudInputBase {

    
        private String _userId;

    
        @PropMeta(propId=2)
    
        public String getUserId(){
            return _userId;
        }

        public void setUserId(String value){
            this._userId = value;
        }


        private Integer _balance;

    
        @PropMeta(propId=3)
    
        public Integer getBalance(){
            return _balance;
        }

        public void setBalance(Integer value){
            this._balance = value;
        }


        private Integer _totalEarned;

    
        @PropMeta(propId=4)
    
        public Integer getTotalEarned(){
            return _totalEarned;
        }

        public void setTotalEarned(Integer value){
            this._totalEarned = value;
        }


        private Integer _totalSpent;

    
        @PropMeta(propId=5)
    
        public Integer getTotalSpent(){
            return _totalSpent;
        }

        public void setTotalSpent(Integer value){
            this._totalSpent = value;
        }


    }
