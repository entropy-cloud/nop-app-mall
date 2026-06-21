//__XGEN_FORCE_OVERRIDE__
    package app.mall.api.beans;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import io.nop.api.core.annotations.data.DataBean;
    import io.nop.api.core.annotations.meta.PropMeta;
    import io.nop.api.core.api.CrudInputBase;
    
    @DataBean
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings({"PMD","java:S116","java:S115"})
    public class LitemallCommentInputBean extends CrudInputBase {

    
        private String _valueId;

    
        @PropMeta(propId=2)
    
        public String getValueId(){
            return _valueId;
        }

        public void setValueId(String value){
            this._valueId = value;
        }


        private Integer _type;

    
        @PropMeta(propId=3)
    
        public Integer getType(){
            return _type;
        }

        public void setType(Integer value){
            this._type = value;
        }


        private String _content;

    
        @PropMeta(propId=4)
    
        public String getContent(){
            return _content;
        }

        public void setContent(String value){
            this._content = value;
        }


        private String _adminContent;

    
        @PropMeta(propId=5)
    
        public String getAdminContent(){
            return _adminContent;
        }

        public void setAdminContent(String value){
            this._adminContent = value;
        }


        private String _userId;

    
        @PropMeta(propId=6)
    
        public String getUserId(){
            return _userId;
        }

        public void setUserId(String value){
            this._userId = value;
        }


        private Boolean _hasPicture;

    
        @PropMeta(propId=7)
    
        public Boolean getHasPicture(){
            return _hasPicture;
        }

        public void setHasPicture(Boolean value){
            this._hasPicture = value;
        }


        private java.util.List<java.lang.String> _picUrls;

    
        @PropMeta(propId=8)
    
        public java.util.List<java.lang.String> getPicUrls(){
            return _picUrls;
        }

        public void setPicUrls(java.util.List<java.lang.String> value){
            this._picUrls = value;
        }


        private Integer _star;

    
        @PropMeta(propId=9)
    
        public Integer getStar(){
            return _star;
        }

        public void setStar(Integer value){
            this._star = value;
        }


        private Boolean _deleted;

    
        @PropMeta(propId=12)
    
        public Boolean getDeleted(){
            return _deleted;
        }

        public void setDeleted(Boolean value){
            this._deleted = value;
        }


        private String _pros;

    
        @PropMeta(propId=13)
    
        public String getPros(){
            return _pros;
        }

        public void setPros(String value){
            this._pros = value;
        }


        private String _cons;

    
        @PropMeta(propId=14)
    
        public String getCons(){
            return _cons;
        }

        public void setCons(String value){
            this._cons = value;
        }


        private Integer _semanticRating;

    
        @PropMeta(propId=15)
    
        public Integer getSemanticRating(){
            return _semanticRating;
        }

        public void setSemanticRating(Integer value){
            this._semanticRating = value;
        }


    }
