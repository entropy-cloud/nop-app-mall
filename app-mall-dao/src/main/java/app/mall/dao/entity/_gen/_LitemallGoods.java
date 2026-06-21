package app.mall.dao.entity._gen;

import io.nop.orm.model.IEntityModel;
import io.nop.orm.support.DynamicOrmEntity;
import io.nop.orm.support.OrmEntitySet; //NOPMD - suppressed UnusedImports - Auto Gen Code
import io.nop.orm.IOrmEntitySet; //NOPMD - suppressed UnusedImports - Auto Gen Code
import io.nop.api.core.convert.ConvertHelper;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;

import app.mall.dao.entity.LitemallGoods;

// tell cpd to start ignoring code - CPD-OFF
/**
 *  商品基本信息: litemall_goods
 */
@SuppressWarnings({"PMD.UselessOverridingMethod","PMD.UnusedLocalVariable","java:S3008","java:S1602","java:S1128","java:S1161",
        "PMD.UnnecessaryFullyQualifiedName","PMD.EmptyControlStatement","java:S116","java:S115","java:S101","java:S3776"})
public class _LitemallGoods extends DynamicOrmEntity{
    
    /* Id: ID INTEGER */
    public static final String PROP_NAME_id = "id";
    public static final int PROP_ID_id = 1;
    
    /* 商品编号: GOODS_SN VARCHAR */
    public static final String PROP_NAME_goodsSn = "goodsSn";
    public static final int PROP_ID_goodsSn = 2;
    
    /* 商品名称: NAME VARCHAR */
    public static final String PROP_NAME_name = "name";
    public static final int PROP_ID_name = 3;
    
    /* 商品所属类目ID: CATEGORY_ID INTEGER */
    public static final String PROP_NAME_categoryId = "categoryId";
    public static final int PROP_ID_categoryId = 4;
    
    /* 品牌ID: BRAND_ID INTEGER */
    public static final String PROP_NAME_brandId = "brandId";
    public static final int PROP_ID_brandId = 5;
    
    /* 商品宣传图片列表: GALLERY VARCHAR */
    public static final String PROP_NAME_gallery = "gallery";
    public static final int PROP_ID_gallery = 6;
    
    /* 商品关键字: KEYWORDS VARCHAR */
    public static final String PROP_NAME_keywords = "keywords";
    public static final int PROP_ID_keywords = 7;
    
    /* 商品简介: BRIEF VARCHAR */
    public static final String PROP_NAME_brief = "brief";
    public static final int PROP_ID_brief = 8;
    
    /* 是否在售: IS_ON_SALE BOOLEAN */
    public static final String PROP_NAME_isOnSale = "isOnSale";
    public static final int PROP_ID_isOnSale = 9;
    
    /* 排序顺序: SORT_ORDER INTEGER */
    public static final String PROP_NAME_sortOrder = "sortOrder";
    public static final int PROP_ID_sortOrder = 10;
    
    /* 商品图片: PIC_URL VARCHAR */
    public static final String PROP_NAME_picUrl = "picUrl";
    public static final int PROP_ID_picUrl = 11;
    
    /* 商品分享海报: SHARE_URL VARCHAR */
    public static final String PROP_NAME_shareUrl = "shareUrl";
    public static final int PROP_ID_shareUrl = 12;
    
    /* 是否新品: IS_NEW BOOLEAN */
    public static final String PROP_NAME_isNew = "isNew";
    public static final int PROP_ID_isNew = 13;
    
    /* 是否热品: IS_HOT BOOLEAN */
    public static final String PROP_NAME_isHot = "isHot";
    public static final int PROP_ID_isHot = 14;
    
    /* 商品单位: UNIT VARCHAR */
    public static final String PROP_NAME_unit = "unit";
    public static final int PROP_ID_unit = 15;
    
    /* 市场售价: COUNTER_PRICE DECIMAL */
    public static final String PROP_NAME_counterPrice = "counterPrice";
    public static final int PROP_ID_counterPrice = 16;
    
    /* 当前价格: RETAIL_PRICE DECIMAL */
    public static final String PROP_NAME_retailPrice = "retailPrice";
    public static final int PROP_ID_retailPrice = 17;
    
    /* 详情: DETAIL VARCHAR */
    public static final String PROP_NAME_detail = "detail";
    public static final int PROP_ID_detail = 18;
    
    /* 创建时间: ADD_TIME DATETIME */
    public static final String PROP_NAME_addTime = "addTime";
    public static final int PROP_ID_addTime = 19;
    
    /* 更新时间: UPDATE_TIME DATETIME */
    public static final String PROP_NAME_updateTime = "updateTime";
    public static final int PROP_ID_updateTime = 20;
    
    /* 逻辑删除: DELETED BOOLEAN */
    public static final String PROP_NAME_deleted = "deleted";
    public static final int PROP_ID_deleted = 21;
    
    /* 是否推荐: IS_RECOMMEND BOOLEAN */
    public static final String PROP_NAME_isRecommend = "isRecommend";
    public static final int PROP_ID_isRecommend = 22;
    
    /* 商品视频链接: VIDEO_URL VARCHAR */
    public static final String PROP_NAME_videoUrl = "videoUrl";
    public static final int PROP_ID_videoUrl = 23;
    

    private static int _PROP_ID_BOUND = 24;

    
    /* relation: 商品类目 */
    public static final String PROP_NAME_category = "category";
    
    /* relation: 商品品牌 */
    public static final String PROP_NAME_brand = "brand";
    
    /* relation: 商品参数 */
    public static final String PROP_NAME_attributes = "attributes";
    
    /* relation: 包含产品 */
    public static final String PROP_NAME_products = "products";
    
    /* relation: 商品规格 */
    public static final String PROP_NAME_specifications = "specifications";
    
    /* relation: 订单商品 */
    public static final String PROP_NAME_orderGoods = "orderGoods";
    
    /* component:  */
    public static final String PROP_NAME_galleryComponent = "galleryComponent";
    
    /* component:  */
    public static final String PROP_NAME_picUrlComponent = "picUrlComponent";
    
    /* component:  */
    public static final String PROP_NAME_shareUrlComponent = "shareUrlComponent";
    
    /* component:  */
    public static final String PROP_NAME_videoUrlComponent = "videoUrlComponent";
    

    protected static final List<String> PK_PROP_NAMES = Arrays.asList(PROP_NAME_id);
    protected static final int[] PK_PROP_IDS = new int[]{PROP_ID_id};

    private static final String[] PROP_ID_TO_NAME = new String[24];
    private static final Map<String,Integer> PROP_NAME_TO_ID = new HashMap<>();
    static{
      
          PROP_ID_TO_NAME[PROP_ID_id] = PROP_NAME_id;
          PROP_NAME_TO_ID.put(PROP_NAME_id, PROP_ID_id);
      
          PROP_ID_TO_NAME[PROP_ID_goodsSn] = PROP_NAME_goodsSn;
          PROP_NAME_TO_ID.put(PROP_NAME_goodsSn, PROP_ID_goodsSn);
      
          PROP_ID_TO_NAME[PROP_ID_name] = PROP_NAME_name;
          PROP_NAME_TO_ID.put(PROP_NAME_name, PROP_ID_name);
      
          PROP_ID_TO_NAME[PROP_ID_categoryId] = PROP_NAME_categoryId;
          PROP_NAME_TO_ID.put(PROP_NAME_categoryId, PROP_ID_categoryId);
      
          PROP_ID_TO_NAME[PROP_ID_brandId] = PROP_NAME_brandId;
          PROP_NAME_TO_ID.put(PROP_NAME_brandId, PROP_ID_brandId);
      
          PROP_ID_TO_NAME[PROP_ID_gallery] = PROP_NAME_gallery;
          PROP_NAME_TO_ID.put(PROP_NAME_gallery, PROP_ID_gallery);
      
          PROP_ID_TO_NAME[PROP_ID_keywords] = PROP_NAME_keywords;
          PROP_NAME_TO_ID.put(PROP_NAME_keywords, PROP_ID_keywords);
      
          PROP_ID_TO_NAME[PROP_ID_brief] = PROP_NAME_brief;
          PROP_NAME_TO_ID.put(PROP_NAME_brief, PROP_ID_brief);
      
          PROP_ID_TO_NAME[PROP_ID_isOnSale] = PROP_NAME_isOnSale;
          PROP_NAME_TO_ID.put(PROP_NAME_isOnSale, PROP_ID_isOnSale);
      
          PROP_ID_TO_NAME[PROP_ID_sortOrder] = PROP_NAME_sortOrder;
          PROP_NAME_TO_ID.put(PROP_NAME_sortOrder, PROP_ID_sortOrder);
      
          PROP_ID_TO_NAME[PROP_ID_picUrl] = PROP_NAME_picUrl;
          PROP_NAME_TO_ID.put(PROP_NAME_picUrl, PROP_ID_picUrl);
      
          PROP_ID_TO_NAME[PROP_ID_shareUrl] = PROP_NAME_shareUrl;
          PROP_NAME_TO_ID.put(PROP_NAME_shareUrl, PROP_ID_shareUrl);
      
          PROP_ID_TO_NAME[PROP_ID_isNew] = PROP_NAME_isNew;
          PROP_NAME_TO_ID.put(PROP_NAME_isNew, PROP_ID_isNew);
      
          PROP_ID_TO_NAME[PROP_ID_isHot] = PROP_NAME_isHot;
          PROP_NAME_TO_ID.put(PROP_NAME_isHot, PROP_ID_isHot);
      
          PROP_ID_TO_NAME[PROP_ID_unit] = PROP_NAME_unit;
          PROP_NAME_TO_ID.put(PROP_NAME_unit, PROP_ID_unit);
      
          PROP_ID_TO_NAME[PROP_ID_counterPrice] = PROP_NAME_counterPrice;
          PROP_NAME_TO_ID.put(PROP_NAME_counterPrice, PROP_ID_counterPrice);
      
          PROP_ID_TO_NAME[PROP_ID_retailPrice] = PROP_NAME_retailPrice;
          PROP_NAME_TO_ID.put(PROP_NAME_retailPrice, PROP_ID_retailPrice);
      
          PROP_ID_TO_NAME[PROP_ID_detail] = PROP_NAME_detail;
          PROP_NAME_TO_ID.put(PROP_NAME_detail, PROP_ID_detail);
      
          PROP_ID_TO_NAME[PROP_ID_addTime] = PROP_NAME_addTime;
          PROP_NAME_TO_ID.put(PROP_NAME_addTime, PROP_ID_addTime);
      
          PROP_ID_TO_NAME[PROP_ID_updateTime] = PROP_NAME_updateTime;
          PROP_NAME_TO_ID.put(PROP_NAME_updateTime, PROP_ID_updateTime);
      
          PROP_ID_TO_NAME[PROP_ID_deleted] = PROP_NAME_deleted;
          PROP_NAME_TO_ID.put(PROP_NAME_deleted, PROP_ID_deleted);
      
          PROP_ID_TO_NAME[PROP_ID_isRecommend] = PROP_NAME_isRecommend;
          PROP_NAME_TO_ID.put(PROP_NAME_isRecommend, PROP_ID_isRecommend);
      
          PROP_ID_TO_NAME[PROP_ID_videoUrl] = PROP_NAME_videoUrl;
          PROP_NAME_TO_ID.put(PROP_NAME_videoUrl, PROP_ID_videoUrl);
      
    }

    
    /* Id: ID */
    private java.lang.String _id;
    
    /* 商品编号: GOODS_SN */
    private java.lang.String _goodsSn;
    
    /* 商品名称: NAME */
    private java.lang.String _name;
    
    /* 商品所属类目ID: CATEGORY_ID */
    private java.lang.String _categoryId;
    
    /* 品牌ID: BRAND_ID */
    private java.lang.String _brandId;
    
    /* 商品宣传图片列表: GALLERY */
    private java.lang.String _gallery;
    
    /* 商品关键字: KEYWORDS */
    private java.lang.String _keywords;
    
    /* 商品简介: BRIEF */
    private java.lang.String _brief;
    
    /* 是否在售: IS_ON_SALE */
    private java.lang.Boolean _isOnSale;
    
    /* 排序顺序: SORT_ORDER */
    private java.lang.Integer _sortOrder;
    
    /* 商品图片: PIC_URL */
    private java.lang.String _picUrl;
    
    /* 商品分享海报: SHARE_URL */
    private java.lang.String _shareUrl;
    
    /* 是否新品: IS_NEW */
    private java.lang.Boolean _isNew;
    
    /* 是否热品: IS_HOT */
    private java.lang.Boolean _isHot;
    
    /* 商品单位: UNIT */
    private java.lang.String _unit;
    
    /* 市场售价: COUNTER_PRICE */
    private java.math.BigDecimal _counterPrice;
    
    /* 当前价格: RETAIL_PRICE */
    private java.math.BigDecimal _retailPrice;
    
    /* 详情: DETAIL */
    private java.lang.String _detail;
    
    /* 创建时间: ADD_TIME */
    private java.time.LocalDateTime _addTime;
    
    /* 更新时间: UPDATE_TIME */
    private java.time.LocalDateTime _updateTime;
    
    /* 逻辑删除: DELETED */
    private java.lang.Boolean _deleted;
    
    /* 是否推荐: IS_RECOMMEND */
    private java.lang.Boolean _isRecommend;
    
    /* 商品视频链接: VIDEO_URL */
    private java.lang.String _videoUrl;
    

    public _LitemallGoods(){
        // for debug
    }

    protected LitemallGoods newInstance(){
        LitemallGoods entity = new LitemallGoods();
        entity.orm_attach(orm_enhancer());
        entity.orm_entityModel(orm_entityModel());
        return entity;
    }

    @Override
    public LitemallGoods cloneInstance() {
        LitemallGoods entity = newInstance();
        orm_forEachInitedProp((value, propId) -> {
            entity.orm_propValue(propId,value);
        });
        return entity;
    }

    @Override
    public String orm_entityName() {
      // 如果存在实体模型对象，则以模型对象上的设置为准
      IEntityModel entityModel = orm_entityModel();
      if(entityModel != null)
          return entityModel.getName();
      return "app.mall.dao.entity.LitemallGoods";
    }

    @Override
    public int orm_propIdBound(){
      IEntityModel entityModel = orm_entityModel();
      if(entityModel != null)
          return entityModel.getPropIdBound();
      return _PROP_ID_BOUND;
    }

    @Override
    public Object orm_id() {
    
        return buildSimpleId(PROP_ID_id);
     
    }

    @Override
    public boolean orm_isPrimary(int propId) {
        
            return propId == PROP_ID_id;
          
    }

    @Override
    public String orm_propName(int propId) {
        if(propId >= PROP_ID_TO_NAME.length)
            return super.orm_propName(propId);
        String propName = PROP_ID_TO_NAME[propId];
        if(propName == null)
           return super.orm_propName(propId);
        return propName;
    }

    @Override
    public int orm_propId(String propName) {
        Integer propId = PROP_NAME_TO_ID.get(propName);
        if(propId == null)
            return super.orm_propId(propName);
        return propId;
    }

    @Override
    public Object orm_propValue(int propId) {
        switch(propId){
        
            case PROP_ID_id:
               return getId();
        
            case PROP_ID_goodsSn:
               return getGoodsSn();
        
            case PROP_ID_name:
               return getName();
        
            case PROP_ID_categoryId:
               return getCategoryId();
        
            case PROP_ID_brandId:
               return getBrandId();
        
            case PROP_ID_gallery:
               return getGallery();
        
            case PROP_ID_keywords:
               return getKeywords();
        
            case PROP_ID_brief:
               return getBrief();
        
            case PROP_ID_isOnSale:
               return getIsOnSale();
        
            case PROP_ID_sortOrder:
               return getSortOrder();
        
            case PROP_ID_picUrl:
               return getPicUrl();
        
            case PROP_ID_shareUrl:
               return getShareUrl();
        
            case PROP_ID_isNew:
               return getIsNew();
        
            case PROP_ID_isHot:
               return getIsHot();
        
            case PROP_ID_unit:
               return getUnit();
        
            case PROP_ID_counterPrice:
               return getCounterPrice();
        
            case PROP_ID_retailPrice:
               return getRetailPrice();
        
            case PROP_ID_detail:
               return getDetail();
        
            case PROP_ID_addTime:
               return getAddTime();
        
            case PROP_ID_updateTime:
               return getUpdateTime();
        
            case PROP_ID_deleted:
               return getDeleted();
        
            case PROP_ID_isRecommend:
               return getIsRecommend();
        
            case PROP_ID_videoUrl:
               return getVideoUrl();
        
           default:
              return super.orm_propValue(propId);
        }
    }

    

    @Override
    public void orm_propValue(int propId, Object value){
        switch(propId){
        
            case PROP_ID_id:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_id));
               }
               setId(typedValue);
               break;
            }
        
            case PROP_ID_goodsSn:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_goodsSn));
               }
               setGoodsSn(typedValue);
               break;
            }
        
            case PROP_ID_name:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_name));
               }
               setName(typedValue);
               break;
            }
        
            case PROP_ID_categoryId:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_categoryId));
               }
               setCategoryId(typedValue);
               break;
            }
        
            case PROP_ID_brandId:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_brandId));
               }
               setBrandId(typedValue);
               break;
            }
        
            case PROP_ID_gallery:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_gallery));
               }
               setGallery(typedValue);
               break;
            }
        
            case PROP_ID_keywords:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_keywords));
               }
               setKeywords(typedValue);
               break;
            }
        
            case PROP_ID_brief:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_brief));
               }
               setBrief(typedValue);
               break;
            }
        
            case PROP_ID_isOnSale:{
               java.lang.Boolean typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toBoolean(value,
                       err-> newTypeConversionError(PROP_NAME_isOnSale));
               }
               setIsOnSale(typedValue);
               break;
            }
        
            case PROP_ID_sortOrder:{
               java.lang.Integer typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toInteger(value,
                       err-> newTypeConversionError(PROP_NAME_sortOrder));
               }
               setSortOrder(typedValue);
               break;
            }
        
            case PROP_ID_picUrl:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_picUrl));
               }
               setPicUrl(typedValue);
               break;
            }
        
            case PROP_ID_shareUrl:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_shareUrl));
               }
               setShareUrl(typedValue);
               break;
            }
        
            case PROP_ID_isNew:{
               java.lang.Boolean typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toBoolean(value,
                       err-> newTypeConversionError(PROP_NAME_isNew));
               }
               setIsNew(typedValue);
               break;
            }
        
            case PROP_ID_isHot:{
               java.lang.Boolean typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toBoolean(value,
                       err-> newTypeConversionError(PROP_NAME_isHot));
               }
               setIsHot(typedValue);
               break;
            }
        
            case PROP_ID_unit:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_unit));
               }
               setUnit(typedValue);
               break;
            }
        
            case PROP_ID_counterPrice:{
               java.math.BigDecimal typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toBigDecimal(value,
                       err-> newTypeConversionError(PROP_NAME_counterPrice));
               }
               setCounterPrice(typedValue);
               break;
            }
        
            case PROP_ID_retailPrice:{
               java.math.BigDecimal typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toBigDecimal(value,
                       err-> newTypeConversionError(PROP_NAME_retailPrice));
               }
               setRetailPrice(typedValue);
               break;
            }
        
            case PROP_ID_detail:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_detail));
               }
               setDetail(typedValue);
               break;
            }
        
            case PROP_ID_addTime:{
               java.time.LocalDateTime typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toLocalDateTime(value,
                       err-> newTypeConversionError(PROP_NAME_addTime));
               }
               setAddTime(typedValue);
               break;
            }
        
            case PROP_ID_updateTime:{
               java.time.LocalDateTime typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toLocalDateTime(value,
                       err-> newTypeConversionError(PROP_NAME_updateTime));
               }
               setUpdateTime(typedValue);
               break;
            }
        
            case PROP_ID_deleted:{
               java.lang.Boolean typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toBoolean(value,
                       err-> newTypeConversionError(PROP_NAME_deleted));
               }
               setDeleted(typedValue);
               break;
            }
        
            case PROP_ID_isRecommend:{
               java.lang.Boolean typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toBoolean(value,
                       err-> newTypeConversionError(PROP_NAME_isRecommend));
               }
               setIsRecommend(typedValue);
               break;
            }
        
            case PROP_ID_videoUrl:{
               java.lang.String typedValue = null;
               if(value != null){
                   typedValue = ConvertHelper.toString(value,
                       err-> newTypeConversionError(PROP_NAME_videoUrl));
               }
               setVideoUrl(typedValue);
               break;
            }
        
           default:
              super.orm_propValue(propId,value);
        }
    }

    @Override
    public void orm_internalSet(int propId, Object value) {
        switch(propId){
        
            case PROP_ID_id:{
               onInitProp(propId);
               this._id = (java.lang.String)value;
               orm_id(); // 如果是设置主键字段，则触发watcher
               break;
            }
        
            case PROP_ID_goodsSn:{
               onInitProp(propId);
               this._goodsSn = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_name:{
               onInitProp(propId);
               this._name = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_categoryId:{
               onInitProp(propId);
               this._categoryId = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_brandId:{
               onInitProp(propId);
               this._brandId = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_gallery:{
               onInitProp(propId);
               this._gallery = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_keywords:{
               onInitProp(propId);
               this._keywords = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_brief:{
               onInitProp(propId);
               this._brief = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_isOnSale:{
               onInitProp(propId);
               this._isOnSale = (java.lang.Boolean)value;
               
               break;
            }
        
            case PROP_ID_sortOrder:{
               onInitProp(propId);
               this._sortOrder = (java.lang.Integer)value;
               
               break;
            }
        
            case PROP_ID_picUrl:{
               onInitProp(propId);
               this._picUrl = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_shareUrl:{
               onInitProp(propId);
               this._shareUrl = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_isNew:{
               onInitProp(propId);
               this._isNew = (java.lang.Boolean)value;
               
               break;
            }
        
            case PROP_ID_isHot:{
               onInitProp(propId);
               this._isHot = (java.lang.Boolean)value;
               
               break;
            }
        
            case PROP_ID_unit:{
               onInitProp(propId);
               this._unit = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_counterPrice:{
               onInitProp(propId);
               this._counterPrice = (java.math.BigDecimal)value;
               
               break;
            }
        
            case PROP_ID_retailPrice:{
               onInitProp(propId);
               this._retailPrice = (java.math.BigDecimal)value;
               
               break;
            }
        
            case PROP_ID_detail:{
               onInitProp(propId);
               this._detail = (java.lang.String)value;
               
               break;
            }
        
            case PROP_ID_addTime:{
               onInitProp(propId);
               this._addTime = (java.time.LocalDateTime)value;
               
               break;
            }
        
            case PROP_ID_updateTime:{
               onInitProp(propId);
               this._updateTime = (java.time.LocalDateTime)value;
               
               break;
            }
        
            case PROP_ID_deleted:{
               onInitProp(propId);
               this._deleted = (java.lang.Boolean)value;
               
               break;
            }
        
            case PROP_ID_isRecommend:{
               onInitProp(propId);
               this._isRecommend = (java.lang.Boolean)value;
               
               break;
            }
        
            case PROP_ID_videoUrl:{
               onInitProp(propId);
               this._videoUrl = (java.lang.String)value;
               
               break;
            }
        
           default:
              super.orm_internalSet(propId,value);
        }
    }

    
    /**
     * Id: ID
     */
    public final java.lang.String getId(){
         onPropGet(PROP_ID_id);
         return _id;
    }

    /**
     * Id: ID
     */
    public final void setId(java.lang.String value){
        if(onPropSet(PROP_ID_id,value)){
            this._id = value;
            internalClearRefs(PROP_ID_id);
            orm_id();
        }
    }
    
    /**
     * 商品编号: GOODS_SN
     */
    public final java.lang.String getGoodsSn(){
         onPropGet(PROP_ID_goodsSn);
         return _goodsSn;
    }

    /**
     * 商品编号: GOODS_SN
     */
    public final void setGoodsSn(java.lang.String value){
        if(onPropSet(PROP_ID_goodsSn,value)){
            this._goodsSn = value;
            internalClearRefs(PROP_ID_goodsSn);
            
        }
    }
    
    /**
     * 商品名称: NAME
     */
    public final java.lang.String getName(){
         onPropGet(PROP_ID_name);
         return _name;
    }

    /**
     * 商品名称: NAME
     */
    public final void setName(java.lang.String value){
        if(onPropSet(PROP_ID_name,value)){
            this._name = value;
            internalClearRefs(PROP_ID_name);
            
        }
    }
    
    /**
     * 商品所属类目ID: CATEGORY_ID
     */
    public final java.lang.String getCategoryId(){
         onPropGet(PROP_ID_categoryId);
         return _categoryId;
    }

    /**
     * 商品所属类目ID: CATEGORY_ID
     */
    public final void setCategoryId(java.lang.String value){
        if(onPropSet(PROP_ID_categoryId,value)){
            this._categoryId = value;
            internalClearRefs(PROP_ID_categoryId);
            
        }
    }
    
    /**
     * 品牌ID: BRAND_ID
     */
    public final java.lang.String getBrandId(){
         onPropGet(PROP_ID_brandId);
         return _brandId;
    }

    /**
     * 品牌ID: BRAND_ID
     */
    public final void setBrandId(java.lang.String value){
        if(onPropSet(PROP_ID_brandId,value)){
            this._brandId = value;
            internalClearRefs(PROP_ID_brandId);
            
        }
    }
    
    /**
     * 商品宣传图片列表: GALLERY
     */
    public final java.lang.String getGallery(){
         onPropGet(PROP_ID_gallery);
         return _gallery;
    }

    /**
     * 商品宣传图片列表: GALLERY
     */
    public final void setGallery(java.lang.String value){
        if(onPropSet(PROP_ID_gallery,value)){
            this._gallery = value;
            internalClearRefs(PROP_ID_gallery);
            
        }
    }
    
    /**
     * 商品关键字: KEYWORDS
     */
    public final java.lang.String getKeywords(){
         onPropGet(PROP_ID_keywords);
         return _keywords;
    }

    /**
     * 商品关键字: KEYWORDS
     */
    public final void setKeywords(java.lang.String value){
        if(onPropSet(PROP_ID_keywords,value)){
            this._keywords = value;
            internalClearRefs(PROP_ID_keywords);
            
        }
    }
    
    /**
     * 商品简介: BRIEF
     */
    public final java.lang.String getBrief(){
         onPropGet(PROP_ID_brief);
         return _brief;
    }

    /**
     * 商品简介: BRIEF
     */
    public final void setBrief(java.lang.String value){
        if(onPropSet(PROP_ID_brief,value)){
            this._brief = value;
            internalClearRefs(PROP_ID_brief);
            
        }
    }
    
    /**
     * 是否在售: IS_ON_SALE
     */
    public final java.lang.Boolean getIsOnSale(){
         onPropGet(PROP_ID_isOnSale);
         return _isOnSale;
    }

    /**
     * 是否在售: IS_ON_SALE
     */
    public final void setIsOnSale(java.lang.Boolean value){
        if(onPropSet(PROP_ID_isOnSale,value)){
            this._isOnSale = value;
            internalClearRefs(PROP_ID_isOnSale);
            
        }
    }
    
    /**
     * 排序顺序: SORT_ORDER
     */
    public final java.lang.Integer getSortOrder(){
         onPropGet(PROP_ID_sortOrder);
         return _sortOrder;
    }

    /**
     * 排序顺序: SORT_ORDER
     */
    public final void setSortOrder(java.lang.Integer value){
        if(onPropSet(PROP_ID_sortOrder,value)){
            this._sortOrder = value;
            internalClearRefs(PROP_ID_sortOrder);
            
        }
    }
    
    /**
     * 商品图片: PIC_URL
     */
    public final java.lang.String getPicUrl(){
         onPropGet(PROP_ID_picUrl);
         return _picUrl;
    }

    /**
     * 商品图片: PIC_URL
     */
    public final void setPicUrl(java.lang.String value){
        if(onPropSet(PROP_ID_picUrl,value)){
            this._picUrl = value;
            internalClearRefs(PROP_ID_picUrl);
            
        }
    }
    
    /**
     * 商品分享海报: SHARE_URL
     */
    public final java.lang.String getShareUrl(){
         onPropGet(PROP_ID_shareUrl);
         return _shareUrl;
    }

    /**
     * 商品分享海报: SHARE_URL
     */
    public final void setShareUrl(java.lang.String value){
        if(onPropSet(PROP_ID_shareUrl,value)){
            this._shareUrl = value;
            internalClearRefs(PROP_ID_shareUrl);
            
        }
    }
    
    /**
     * 是否新品: IS_NEW
     */
    public final java.lang.Boolean getIsNew(){
         onPropGet(PROP_ID_isNew);
         return _isNew;
    }

    /**
     * 是否新品: IS_NEW
     */
    public final void setIsNew(java.lang.Boolean value){
        if(onPropSet(PROP_ID_isNew,value)){
            this._isNew = value;
            internalClearRefs(PROP_ID_isNew);
            
        }
    }
    
    /**
     * 是否热品: IS_HOT
     */
    public final java.lang.Boolean getIsHot(){
         onPropGet(PROP_ID_isHot);
         return _isHot;
    }

    /**
     * 是否热品: IS_HOT
     */
    public final void setIsHot(java.lang.Boolean value){
        if(onPropSet(PROP_ID_isHot,value)){
            this._isHot = value;
            internalClearRefs(PROP_ID_isHot);
            
        }
    }
    
    /**
     * 商品单位: UNIT
     */
    public final java.lang.String getUnit(){
         onPropGet(PROP_ID_unit);
         return _unit;
    }

    /**
     * 商品单位: UNIT
     */
    public final void setUnit(java.lang.String value){
        if(onPropSet(PROP_ID_unit,value)){
            this._unit = value;
            internalClearRefs(PROP_ID_unit);
            
        }
    }
    
    /**
     * 市场售价: COUNTER_PRICE
     */
    public final java.math.BigDecimal getCounterPrice(){
         onPropGet(PROP_ID_counterPrice);
         return _counterPrice;
    }

    /**
     * 市场售价: COUNTER_PRICE
     */
    public final void setCounterPrice(java.math.BigDecimal value){
        if(onPropSet(PROP_ID_counterPrice,value)){
            this._counterPrice = value;
            internalClearRefs(PROP_ID_counterPrice);
            
        }
    }
    
    /**
     * 当前价格: RETAIL_PRICE
     */
    public final java.math.BigDecimal getRetailPrice(){
         onPropGet(PROP_ID_retailPrice);
         return _retailPrice;
    }

    /**
     * 当前价格: RETAIL_PRICE
     */
    public final void setRetailPrice(java.math.BigDecimal value){
        if(onPropSet(PROP_ID_retailPrice,value)){
            this._retailPrice = value;
            internalClearRefs(PROP_ID_retailPrice);
            
        }
    }
    
    /**
     * 详情: DETAIL
     */
    public final java.lang.String getDetail(){
         onPropGet(PROP_ID_detail);
         return _detail;
    }

    /**
     * 详情: DETAIL
     */
    public final void setDetail(java.lang.String value){
        if(onPropSet(PROP_ID_detail,value)){
            this._detail = value;
            internalClearRefs(PROP_ID_detail);
            
        }
    }
    
    /**
     * 创建时间: ADD_TIME
     */
    public final java.time.LocalDateTime getAddTime(){
         onPropGet(PROP_ID_addTime);
         return _addTime;
    }

    /**
     * 创建时间: ADD_TIME
     */
    public final void setAddTime(java.time.LocalDateTime value){
        if(onPropSet(PROP_ID_addTime,value)){
            this._addTime = value;
            internalClearRefs(PROP_ID_addTime);
            
        }
    }
    
    /**
     * 更新时间: UPDATE_TIME
     */
    public final java.time.LocalDateTime getUpdateTime(){
         onPropGet(PROP_ID_updateTime);
         return _updateTime;
    }

    /**
     * 更新时间: UPDATE_TIME
     */
    public final void setUpdateTime(java.time.LocalDateTime value){
        if(onPropSet(PROP_ID_updateTime,value)){
            this._updateTime = value;
            internalClearRefs(PROP_ID_updateTime);
            
        }
    }
    
    /**
     * 逻辑删除: DELETED
     */
    public final java.lang.Boolean getDeleted(){
         onPropGet(PROP_ID_deleted);
         return _deleted;
    }

    /**
     * 逻辑删除: DELETED
     */
    public final void setDeleted(java.lang.Boolean value){
        if(onPropSet(PROP_ID_deleted,value)){
            this._deleted = value;
            internalClearRefs(PROP_ID_deleted);
            
        }
    }
    
    /**
     * 是否推荐: IS_RECOMMEND
     */
    public final java.lang.Boolean getIsRecommend(){
         onPropGet(PROP_ID_isRecommend);
         return _isRecommend;
    }

    /**
     * 是否推荐: IS_RECOMMEND
     */
    public final void setIsRecommend(java.lang.Boolean value){
        if(onPropSet(PROP_ID_isRecommend,value)){
            this._isRecommend = value;
            internalClearRefs(PROP_ID_isRecommend);
            
        }
    }
    
    /**
     * 商品视频链接: VIDEO_URL
     */
    public final java.lang.String getVideoUrl(){
         onPropGet(PROP_ID_videoUrl);
         return _videoUrl;
    }

    /**
     * 商品视频链接: VIDEO_URL
     */
    public final void setVideoUrl(java.lang.String value){
        if(onPropSet(PROP_ID_videoUrl,value)){
            this._videoUrl = value;
            internalClearRefs(PROP_ID_videoUrl);
            
        }
    }
    
    /**
     * 商品类目
     */
    public final app.mall.dao.entity.LitemallCategory getCategory(){
       return (app.mall.dao.entity.LitemallCategory)internalGetRefEntity(PROP_NAME_category);
    }

    public final void setCategory(app.mall.dao.entity.LitemallCategory refEntity){
   
           if(refEntity == null){
           
                   this.setCategoryId(null);
               
           }else{
           internalSetRefEntity(PROP_NAME_category, refEntity,()->{
           
                           this.setCategoryId(refEntity.getId());
                       
           });
           }
       
    }
       
    /**
     * 商品品牌
     */
    public final app.mall.dao.entity.LitemallBrand getBrand(){
       return (app.mall.dao.entity.LitemallBrand)internalGetRefEntity(PROP_NAME_brand);
    }

    public final void setBrand(app.mall.dao.entity.LitemallBrand refEntity){
   
           if(refEntity == null){
           
                   this.setBrandId(null);
               
           }else{
           internalSetRefEntity(PROP_NAME_brand, refEntity,()->{
           
                           this.setBrandId(refEntity.getId());
                       
           });
           }
       
    }
       
    private final OrmEntitySet<app.mall.dao.entity.LitemallGoodsAttribute> _attributes = new OrmEntitySet<>(this, PROP_NAME_attributes,
        app.mall.dao.entity.LitemallGoodsAttribute.PROP_NAME_goods, null,app.mall.dao.entity.LitemallGoodsAttribute.class);

    /**
     * 商品参数。 refPropName: goods, keyProp: {rel.keyProp}
     */
    public final IOrmEntitySet<app.mall.dao.entity.LitemallGoodsAttribute> getAttributes(){
       return _attributes;
    }
       
    private final OrmEntitySet<app.mall.dao.entity.LitemallGoodsProduct> _products = new OrmEntitySet<>(this, PROP_NAME_products,
        app.mall.dao.entity.LitemallGoodsProduct.PROP_NAME_goods, null,app.mall.dao.entity.LitemallGoodsProduct.class);

    /**
     * 包含产品。 refPropName: goods, keyProp: {rel.keyProp}
     */
    public final IOrmEntitySet<app.mall.dao.entity.LitemallGoodsProduct> getProducts(){
       return _products;
    }
       
    private final OrmEntitySet<app.mall.dao.entity.LitemallGoodsSpecification> _specifications = new OrmEntitySet<>(this, PROP_NAME_specifications,
        app.mall.dao.entity.LitemallGoodsSpecification.PROP_NAME_goods, null,app.mall.dao.entity.LitemallGoodsSpecification.class);

    /**
     * 商品规格。 refPropName: goods, keyProp: {rel.keyProp}
     */
    public final IOrmEntitySet<app.mall.dao.entity.LitemallGoodsSpecification> getSpecifications(){
       return _specifications;
    }
       
    private final OrmEntitySet<app.mall.dao.entity.LitemallOrderGoods> _orderGoods = new OrmEntitySet<>(this, PROP_NAME_orderGoods,
        app.mall.dao.entity.LitemallOrderGoods.PROP_NAME_goods, null,app.mall.dao.entity.LitemallOrderGoods.class);

    /**
     * 订单商品。 refPropName: goods, keyProp: {rel.keyProp}
     */
    public final IOrmEntitySet<app.mall.dao.entity.LitemallOrderGoods> getOrderGoods(){
       return _orderGoods;
    }
       
   private io.nop.orm.component.OrmFileListComponent _galleryComponent;

   private static Map<String,Integer> COMPONENT_PROP_ID_MAP_galleryComponent = new HashMap<>();
   static{
      
         COMPONENT_PROP_ID_MAP_galleryComponent.put(io.nop.orm.component.OrmFileListComponent.PROP_NAME_filePath,PROP_ID_gallery);
      
   }

   public final io.nop.orm.component.OrmFileListComponent getGalleryComponent(){
      if(_galleryComponent == null){
          _galleryComponent = new io.nop.orm.component.OrmFileListComponent();
          _galleryComponent.bindToEntity(this, COMPONENT_PROP_ID_MAP_galleryComponent);
      }
      return _galleryComponent;
   }

   private io.nop.orm.component.OrmFileComponent _picUrlComponent;

   private static Map<String,Integer> COMPONENT_PROP_ID_MAP_picUrlComponent = new HashMap<>();
   static{
      
         COMPONENT_PROP_ID_MAP_picUrlComponent.put(io.nop.orm.component.OrmFileComponent.PROP_NAME_filePath,PROP_ID_picUrl);
      
   }

   public final io.nop.orm.component.OrmFileComponent getPicUrlComponent(){
      if(_picUrlComponent == null){
          _picUrlComponent = new io.nop.orm.component.OrmFileComponent();
          _picUrlComponent.bindToEntity(this, COMPONENT_PROP_ID_MAP_picUrlComponent);
      }
      return _picUrlComponent;
   }

   private io.nop.orm.component.OrmFileComponent _shareUrlComponent;

   private static Map<String,Integer> COMPONENT_PROP_ID_MAP_shareUrlComponent = new HashMap<>();
   static{
      
         COMPONENT_PROP_ID_MAP_shareUrlComponent.put(io.nop.orm.component.OrmFileComponent.PROP_NAME_filePath,PROP_ID_shareUrl);
      
   }

   public final io.nop.orm.component.OrmFileComponent getShareUrlComponent(){
      if(_shareUrlComponent == null){
          _shareUrlComponent = new io.nop.orm.component.OrmFileComponent();
          _shareUrlComponent.bindToEntity(this, COMPONENT_PROP_ID_MAP_shareUrlComponent);
      }
      return _shareUrlComponent;
   }

   private io.nop.orm.component.OrmFileComponent _videoUrlComponent;

   private static Map<String,Integer> COMPONENT_PROP_ID_MAP_videoUrlComponent = new HashMap<>();
   static{
      
         COMPONENT_PROP_ID_MAP_videoUrlComponent.put(io.nop.orm.component.OrmFileComponent.PROP_NAME_filePath,PROP_ID_videoUrl);
      
   }

   public final io.nop.orm.component.OrmFileComponent getVideoUrlComponent(){
      if(_videoUrlComponent == null){
          _videoUrlComponent = new io.nop.orm.component.OrmFileComponent();
          _videoUrlComponent.bindToEntity(this, COMPONENT_PROP_ID_MAP_videoUrlComponent);
      }
      return _videoUrlComponent;
   }

        public final List<app.mall.dao.entity.LitemallGoodsProduct> getRelatedProductList(){
            return (List<app.mall.dao.entity.LitemallGoodsProduct>)io.nop.orm.support.OrmEntityHelper.getRefProps(getOrderGoods(),"product");
        }

        public final List<String> getRelatedProductList_ids(){
            return io.nop.orm.support.OrmEntityHelper.getRefIds(getOrderGoods(),"product");
        }

        public void setRelatedProductList_ids(List<String> value){
            io.nop.orm.support.OrmEntityHelper.setRefIds(getOrderGoods(),"product",value);
        }
    

    public final String getRelatedProductList_label(){
        return io.nop.orm.support.OrmEntityHelper.getLabelForRefProps(getOrderGoods(),"product");
    }


}
// resume CPD analysis - CPD-ON
