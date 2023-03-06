package app.mall.dao;

public interface _AppMallDaoConstants {
    
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
