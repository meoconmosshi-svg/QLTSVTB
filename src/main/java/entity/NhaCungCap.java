package com.qlts.entity;

public record NhaCungCap(int maNcc, String tenNcc) {
    @Override public String toString() { return tenNcc; }
}
