package app.mall.dao;

@SuppressWarnings({"PMD","java:S116"})
public interface _AppMallDaoConstants {
    
    /**
     * 售后状态: 可申请 
     */
    int AFTERSALE_STATUS_INIT = 0;
                    
    /**
     * 售后状态: 用户已申请 
     */
    int AFTERSALE_STATUS_REQUEST = 1;
                    
    /**
     * 售后状态: 管理员审核通过 
     */
    int AFTERSALE_STATUS_APPROVED = 2;
                    
    /**
     * 售后状态: 管理员退款成功 
     */
    int AFTERSALE_STATUS_REFUND = 3;
                    
    /**
     * 售后状态: 管理员审核拒绝 
     */
    int AFTERSALE_STATUS_REJECT = 4;
                    
    /**
     * 售后状态: 用户已取消 
     */
    int AFTERSALE_STATUS_CANCELLED = 5;
                    
    /**
     * 售后类型: 未收货退款 
     */
    int AFTERSALE_TYPE_GOODS_MISS = 0;
                    
    /**
     * 售后类型: 已收货（无需退货）退款 
     */
    int AFTERSALE_TYPE_GOODS_NEEDLESS = 1;
                    
    /**
     * 售后类型: 用户退货退款 
     */
    int AFTERSALE_TYPE_GOODS_REQUIRED = 2;
                    
    /**
     * 订单状态: 未付款 
     */
    int ORDER_STATUS_CREATED = 101;
                    
    /**
     * 订单状态: 已取消 
     */
    int ORDER_STATUS_CANCEL = 102;
                    
    /**
     * 订单状态: 已取消（系统） 
     */
    int ORDER_STATUS_AUTO_CANCEL = 103;
                    
    /**
     * 订单状态: 已付款 
     */
    int ORDER_STATUS_PAY = 201;
                    
    /**
     * 订单状态: 订单取消，退款中 
     */
    int ORDER_STATUS_REFUND = 202;
                    
    /**
     * 订单状态: 已退款 
     */
    int ORDER_STATUS_REFUND_CONFIRM = 203;
                    
    /**
     * 订单状态: 已超时团购 
     */
    int ORDER_STATUS_GROUPON_EXPIRED = 204;
                    
    /**
     * 订单状态: 已发货 
     */
    int ORDER_STATUS_SHIP = 301;
                    
    /**
     * 订单状态: 已收货 
     */
    int ORDER_STATUS_CONFIRM = 401;
                    
    /**
     * 订单状态: 已收货(系统) 
     */
    int ORDER_STATUS_AUTO_CONFIRM = 402;
                    
    /**
     * 商品类目级别: 一级类目 
     */
    String CATEGORY_LEVEL_L1 = "L1";
                    
    /**
     * 商品类目级别: 二级类目 
     */
    String CATEGORY_LEVEL_L2 = "L2";
                    
}
