package com.ispas.dao;

import com.ispas.db.Database;
import com.ispas.model.Ticket;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TicketDao {
    public Ticket create(Ticket t) throws SQLException {
        String sql = "INSERT INTO tickets(customer_id, title, description, status, created_at) VALUES(?,?,?,?,?)";
        Connection c = Database.getConnection();
        PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        try {
            ps.setInt(1, t.getCustomerId());
            ps.setString(2, t.getTitle());
            ps.setString(3, t.getDescription());
            ps.setString(4, t.getStatus());
            ps.setString(5, t.getCreatedAt());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) t.setId(rs.getInt(1)); }
        } finally {
            ps.close();
        }
        return t;
    }

    public List<Ticket> listByCustomer(int customerId) throws SQLException {
        String sql = "SELECT id, customer_id, title, description, status, created_at FROM tickets WHERE customer_id = ?";
        List<Ticket> out = new ArrayList<>();
        Connection c = Database.getConnection();
        PreparedStatement ps = c.prepareStatement(sql);
        try {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            try {
                while (rs.next()) {
                    Ticket t = new Ticket();
                    t.setId(rs.getInt("id"));
                    t.setCustomerId(rs.getInt("customer_id"));
                    t.setTitle(rs.getString("title"));
                    t.setDescription(rs.getString("description"));
                    t.setStatus(rs.getString("status"));
                    t.setCreatedAt(rs.getString("created_at"));
                    out.add(t);
                }
            } finally {
                rs.close();
            }
        } finally {
            ps.close();
        }
        return out;
    }
}
