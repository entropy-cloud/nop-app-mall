//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    
    import java.util.Map;

    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallCheckInRuleOutputBean {

    
        private String _id;

    
        @PropMeta(propId=1)
    
        public String getId(){
            return _id;
        }

        public void setId(String value){
            this._id = value;
        }


        private Integer _daySeq;

    
        @PropMeta(propId=2)
    
        public Integer getDaySeq(){
            return _daySeq;
        }

        public void setDaySeq(Integer value){
            this._daySeq = value;
        }


        private Integer _pointReward;

    
        @PropMeta(propId=3)
    
        public Integer getPointReward(){
            return _pointReward;
        }

        public void setPointReward(Integer value){
            this._pointReward = value;
        }


        private Integer _resetCycle;

    
        @PropMeta(propId=4)
    
        public Integer getResetCycle(){
            return _resetCycle;
        }

        public void setResetCycle(Integer value){
            this._resetCycle = value;
        }


        private java.time.LocalDateTime _addTime;

    
        @PropMeta(propId=5)
    
        public java.time.LocalDateTime getAddTime(){
            return _addTime;
        }

        public void setAddTime(java.time.LocalDateTime value){
            this._addTime = value;
        }


        private java.time.LocalDateTime _updateTime;

    
        @PropMeta(propId=6)
    
        public java.time.LocalDateTime getUpdateTime(){
            return _updateTime;
        }

        public void setUpdateTime(java.time.LocalDateTime value){
            this._updateTime = value;
        }


    }
