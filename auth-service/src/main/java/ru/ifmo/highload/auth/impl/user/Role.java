package ru.ifmo.highload.auth.impl.user;

/**
 * User roles: SUPERVISOR (full access, create users), LOGISTICIAN (stock management),
 * CASHIER (order status updates), USER (create/view own orders).
 */
public enum Role {
    SUPERVISOR,
    LOGISTICIAN,
    CASHIER,
    USER
}
