package com.ispas.dao;

import com.ispas.db.Database;
import com.ispas.model.Plan;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlanDao {
    public Plan create(Plan p) throws SQLException {
        String sql = "INSERT INTO plans(name, monthly_fee, rate_per_mb) VALUES(?,?,?)";
        Connection c = Database.getConnection();
        PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        try {
            ps.setString(1, p.getName());
            ps.setDouble(2, p.getMonthlyFee());
            ps.setDouble(3, p.getRatePerMb());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) p.setId(rs.getInt(1));
            }
        } finally {
            ps.close();
        }
        return p;
    }

    public List<Plan> listAll() throws SQLException {
        List<Plan> out = new ArrayList<>();
        String sql = "SELECT id, name, monthly_fee, rate_per_mb FROM plans";
        Connection c = Database.getConnection();
        PreparedStatement ps = c.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        try {
            while (rs.next()) {
                Plan p = new Plan();
                p.setId(rs.getInt("id"));
                p.setName(rs.getString("name"));
                p.setMonthlyFee(rs.getDouble("monthly_fee"));
                p.setRatePerMb(rs.getDouble("rate_per_mb"));
                out.add(p);
            }
        } finally {
            rs.close();
            ps.close();
        }
        return out;
    }

    public Plan findById(int id) throws SQLException {
        String sql = "SELECT id, name, monthly_fee, rate_per_mb FROM plans WHERE id = ?";
        Connection c = Database.getConnection();
        PreparedStatement ps = c.prepareStatement(sql);
        try {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            try {
                if (rs.next()) {
                    Plan p = new Plan();
                    p.setId(rs.getInt("id"));
                    p.setName(rs.getString("name"));
                    p.setMonthlyFee(rs.getDouble("monthly_fee"));
                    p.setRatePerMb(rs.getDouble("rate_per_mb"));
                    return p;
                }
            } finally {
                rs.close();
            }
        } finally {
            ps.close();
        }
        return null;
    }
}
