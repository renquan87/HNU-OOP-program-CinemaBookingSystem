package com.cinema.exception;

/**
 * 支付失败异常
 * 当支付处理失败时抛出
 */
public class PaymentFailedException extends Exception {
    private final String orderId;
    private final double amount;
    private final String paymentMethod;

    public PaymentFailedException(String orderId, double amount) {
        super("支付失败 - 订单号: " + orderId + ", 金额: ¥" + amount);
        this.orderId = orderId;
        this.amount = amount;
        this.paymentMethod = "未知";
    }

    public PaymentFailedException(String orderId, double amount, String paymentMethod) {
        super("支付失败 - 订单号: " + orderId + ", 金额: ¥" + amount + ", 支付方式: " + paymentMethod);
        this.orderId = orderId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
    }

    public PaymentFailedException(String orderId, double amount, String paymentMethod, String reason) {
        super("支付失败 - " + reason + " (订单号: " + orderId + ", 金额: ¥" + amount + ", 支付方式: " + paymentMethod + ")");
        this.orderId = orderId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
    }

    public PaymentFailedException(String orderId, double amount, String paymentMethod, String reason, Throwable cause) {
        super("支付失败 - " + reason + " (订单号: " + orderId + ", 金额: ¥" + amount + ", 支付方式: " + paymentMethod + ")", cause);
        this.orderId = orderId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
    }

    public String getOrderId() {
        return orderId;
    }

    public double getAmount() {
        return amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }
}