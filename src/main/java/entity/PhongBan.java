package com.qlts.entity;

public record PhongBan(int maPhongBan, String tenPhongBan) {
    @Override public String toString() { return tenPhongBan; }
}
