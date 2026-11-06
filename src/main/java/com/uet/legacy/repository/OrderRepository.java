package com.uet.legacy.repository;

public interface OrderRepository {
    void save(String email, double payableAmount, String status);
}