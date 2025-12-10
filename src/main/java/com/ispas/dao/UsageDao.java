package com.ispas.dao;

import com.ispas.db.Database;
import com.ispas.model.UsageRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsageDao {
    public UsageRecord create(UsageRecord u) throws SQLException {
        String sql = "INSERT INTO usage_records(customer_id, device_name, date_time, mb_used) VALUES(?,?,?,?)";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, u.getCustomerId());
            ps.setString(2, u.getDeviceName());
            ps.setString(3, u.getDateTime());
            ps.setDouble(4, u.getMbUsed());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) u.setId(rs.getInt(1)); }
        }
        return u;
    }

    public List<UsageRecord> listByCustomer(int customerId) throws SQLException {
        String sql = "SELECT id, customer_id, device_name, date_time, mb_used FROM usage_records WHERE customer_id = ?";
        List<UsageRecord> out = new ArrayList<>();
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UsageRecord u = new UsageRecord();
                    u.setId(rs.getInt("id"));
                    u.setCustomerId(rs.getInt("customer_id"));
                    u.setDeviceName(rs.getString("device_name"));
                    u.setDateTime(rs.getString("date_time"));
                    u.setMbUsed(rs.getDouble("mb_used"));
                    out.add(u);
                }
            }
        }
        return out;
    }
}
