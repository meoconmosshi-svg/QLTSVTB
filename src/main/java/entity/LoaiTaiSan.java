package com.qlts.entity;

public record LoaiTaiSan(int maLoai, String tenLoai) {
    @Override public String toString() { return tenLoai; }
}
