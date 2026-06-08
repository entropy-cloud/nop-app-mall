//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    import io.nop.api.core.api.CrudInputBase;
    
    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallIssueInputBean extends CrudInputBase {

    
        private String _question;

    
        @PropMeta(propId=2)
    
        public String getQuestion(){
            return _question;
        }

        public void setQuestion(String value){
            this._question = value;
        }


        private String _answer;

    
        @PropMeta(propId=3)
    
        public String getAnswer(){
            return _answer;
        }

        public void setAnswer(String value){
            this._answer = value;
        }


        private Boolean _deleted;

    
        @PropMeta(propId=6)
    
        public Boolean getDeleted(){
            return _deleted;
        }

        public void setDeleted(Boolean value){
            this._deleted = value;
        }


    }
