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
     * 售后状态: 用户已退货待收货 
     */
    int AFTERSALE_STATUS_RETURNED = 6;
                    
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
     * 售后原因: 不想要了 
     */
    String AFTERSALE_REASON_UNWANTED = "不想要了";
                    
    /**
     * 售后原因: 质量问题 
     */
    String AFTERSALE_REASON_QUALITY = "质量问题";
                    
    /**
     * 售后原因: 少发漏发 
     */
    String AFTERSALE_REASON_MISSING = "少发漏发";
                    
    /**
     * 售后原因: 商品损坏 
     */
    String AFTERSALE_REASON_DAMAGED = "商品损坏";
                    
    /**
     * 售后原因: 与描述不符 
     */
    String AFTERSALE_REASON_NOT_AS_DESCRIBED = "与描述不符";
                    
    /**
     * 售后原因: 七天无理由 
     */
    String AFTERSALE_REASON_SEVEN_DAY = "七天无理由";
                    
    /**
     * 优惠券赠送类型: 通用券，用户领取 
     */
    int COUPON_TYPE_COMMON = 0;
                    
    /**
     * 优惠券赠送类型: 注册赠券 
     */
    int COUPON_TYPE_REGISTER = 1;
                    
    /**
     * 优惠券赠送类型: 优惠券码兑换 
     */
    int COUPON_TYPE_EXCHANGE = 2;
                    
    /**
     * 优惠券状态: 正常可用 
     */
    int COUPON_STATUS_NORMAL = 0;
                    
    /**
     * 优惠券状态: 过期 
     */
    int COUPON_STATUS_EXPIRED = 1;
                    
    /**
     * 优惠券状态: 下架 
     */
    int COUPON_STATUS_OFFLINE = 2;
                    
    /**
     * 优惠券商品限制类型: 全商品 
     */
    int COUPON_GOODS_TYPE_ALL = 0;
                    
    /**
     * 优惠券商品限制类型: 类目限制 
     */
    int COUPON_GOODS_TYPE_CATEGORY = 1;
                    
    /**
     * 优惠券商品限制类型: 商品限制 
     */
    int COUPON_GOODS_TYPE_GOODS = 2;
                    
    /**
     * 优惠券时间限制类型: 基于领取时间的天数 
     */
    int COUPON_TIME_TYPE_DAYS = 0;
                    
    /**
     * 优惠券时间限制类型: 使用startTime和endTime限制 
     */
    int COUPON_TIME_TYPE_RANGE = 1;
                    
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
    int CATEGORY_LEVEL_L1 = 1;
                    
    /**
     * 商品类目级别: 二级类目 
     */
    int CATEGORY_LEVEL_L2 = 2;
                    
    /**
     * 行政区划类型: 省 
     */
    int REGION_TYPE_PROVINCE = 1;
                    
    /**
     * 行政区划类型: 市 
     */
    int REGION_TYPE_CITY = 2;
                    
    /**
     * 行政区划类型: 区县 
     */
    int REGION_TYPE_DISTRICT = 3;
                    
    /**
     * 性别: 未知 
     */
    int GENDER_UNKNOWN = 0;
                    
    /**
     * 性别: 男 
     */
    int GENDER_MALE = 1;
                    
    /**
     * 性别: 女 
     */
    int GENDER_FEMALE = 2;
                    
    /**
     * 用户状态: 可用 
     */
    int USER_STATUS_NORMAL = 0;
                    
    /**
     * 用户状态: 禁用 
     */
    int USER_STATUS_DISABLED = 1;
                    
    /**
     * 用户状态: 注销 
     */
    int USER_STATUS_CANCELLED = 2;
                    
    /**
     * 用户等级: 普通用户 
     */
    int USER_LEVEL_NORMAL = 0;
                    
    /**
     * 用户等级: VIP用户 
     */
    int USER_LEVEL_VIP = 1;
                    
    /**
     * 用户等级: 高级VIP用户 
     */
    int USER_LEVEL_VIP_SENIOR = 2;
                    
    /**
     * 优惠券使用状态: 未使用 
     */
    int COUPON_USE_STATUS_UNUSED = 0;
                    
    /**
     * 优惠券使用状态: 已使用 
     */
    int COUPON_USE_STATUS_USED = 1;
                    
    /**
     * 优惠券使用状态: 已过期 
     */
    int COUPON_USE_STATUS_EXPIRED = 2;
                    
    /**
     * 优惠券使用状态: 已下架 
     */
    int COUPON_USE_STATUS_OFFLINE = 3;
                    
    /**
     * 促销活动状态: 草稿 
     */
    int PROMOTION_STATUS_DRAFT = 0;
                    
    /**
     * 促销活动状态: 进行中 
     */
    int PROMOTION_STATUS_ACTIVE = 10;
                    
    /**
     * 促销活动状态: 已结束 
     */
    int PROMOTION_STATUS_FINISHED = 20;
                    
    /**
     * 促销活动状态: 已关闭 
     */
    int PROMOTION_STATUS_CLOSED = 30;
                    
    /**
     * 折扣类型: 减金额 
     */
    int DISCOUNT_TYPE_AMOUNT = 0;
                    
    /**
     * 折扣类型: 打折 
     */
    int DISCOUNT_TYPE_PERCENT = 10;
                    
    /**
     * 商品范围类型: 全商品 
     */
    int GOODS_SCOPE_ALL = 0;
                    
    /**
     * 商品范围类型: 指定分类 
     */
    int GOODS_SCOPE_CATEGORY = 10;
                    
    /**
     * 商品范围类型: 指定商品 
     */
    int GOODS_SCOPE_GOODS = 20;
                    
    /**
     * 拼团状态: 进行中 
     */
    int PIN_TUAN_GROUP_STATUS_ACTIVE = 0;
                    
    /**
     * 拼团状态: 拼团成功 
     */
    int PIN_TUAN_GROUP_STATUS_SUCCESS = 10;
                    
    /**
     * 拼团状态: 拼团失败 
     */
    int PIN_TUAN_GROUP_STATUS_FAILED = 20;
                    
    /**
     * 积分变动类型: 获取 
     */
    int POINTS_CHANGE_TYPE_EARN = 0;
                    
    /**
     * 积分变动类型: 消耗 
     */
    int POINTS_CHANGE_TYPE_SPEND = 10;
                    
    /**
     * 积分变动类型: 过期 
     */
    int POINTS_CHANGE_TYPE_EXPIRE = 20;
                    
    /**
     * 钱包变动类型: 充值 
     */
    int WALLET_CHANGE_TYPE_RECHARGE = 0;
                    
    /**
     * 钱包变动类型: 支付 
     */
    int WALLET_CHANGE_TYPE_PAY = 10;
                    
    /**
     * 钱包变动类型: 退款 
     */
    int WALLET_CHANGE_TYPE_REFUND = 20;
                    
    /**
     * 钱包变动类型: 提现 
     */
    int WALLET_CHANGE_TYPE_WITHDRAW = 30;
                    
    /**
     * 消息类型: 订单消息 
     */
    int MSG_TYPE_ORDER = 0;
                    
    /**
     * 消息类型: 营销消息 
     */
    int MSG_TYPE_MARKETING = 10;
                    
    /**
     * 消息类型: 系统消息 
     */
    int MSG_TYPE_SYSTEM = 20;
                    
    /**
     * 配送方式: 快递 
     */
    int DELIVERY_TYPE_EXPRESS = 0;
                    
    /**
     * 配送方式: 门店自提 
     */
    int DELIVERY_TYPE_PICKUP = 10;
                    
    /**
     * 支付通道: 微信支付 
     */
    int PAY_CHANNEL_WECHAT = 0;
                    
    /**
     * 支付通道: 支付宝 
     */
    int PAY_CHANNEL_ALIPAY = 10;
                    
    /**
     * 支付通道: 余额支付 
     */
    int PAY_CHANNEL_BALANCE = 20;
                    
    /**
     * 支付状态: 未支付 
     */
    int PAY_STATUS_UNPAID = 0;
                    
    /**
     * 支付状态: 已支付 
     */
    int PAY_STATUS_PAID = 10;
                    
    /**
     * 支付状态: 支付失败 
     */
    int PAY_STATUS_FAILED = 20;
                    
    /**
     * 支付状态: 已退款 
     */
    int PAY_STATUS_REFUNDED = 30;
                    
    /**
     * 团购活动状态: 开团未支付 
     */
    int GROUPON_STATUS_UNPAID = 0;
                    
    /**
     * 团购活动状态: 开团中 
     */
    int GROUPON_STATUS_OPEN = 1;
                    
    /**
     * 团购活动状态: 开团失败 
     */
    int GROUPON_STATUS_FAILED = 2;
                    
    /**
     * 团购活动状态: 开团成功 
     */
    int GROUPON_STATUS_SUCCESS = 3;
                    
    /**
     * 评论审核状态: 待审核 
     */
    int COMMENT_AUDIT_STATUS_PENDING = 0;
                    
    /**
     * 评论审核状态: 已通过 
     */
    int COMMENT_AUDIT_STATUS_APPROVED = 1;
                    
    /**
     * 评论审核状态: 已拒绝 
     */
    int COMMENT_AUDIT_STATUS_REJECTED = 2;
                    
}
