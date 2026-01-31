package ru.ifmo.highload.auth.impl.user;

/**
 * Роли пользователей системы (табачный магазин).
 * SUPERVISOR — создание пользователей, полный доступ.
 * LOGISTICIAN — загрузка товаров на склад (product-service), не может принимать заказы у клиентов.
 * CASHIER — кассир: работа с заказами (статусы), не может загружать товары на склад.
 * USER — клиент: создание заказов, просмотр своих заказов.
 */
public enum Role {
    SUPERVISOR,
    LOGISTICIAN,
    CASHIER,
    USER
}
