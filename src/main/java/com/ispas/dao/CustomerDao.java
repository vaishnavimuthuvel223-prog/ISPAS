package com.ispas.dao;

import com.ispas.db.Database;
import com.ispas.model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDao {
    public Customer create(Customer cst) throws SQLException {
        String sql = "INSERT INTO customers(name,email,phone,plan_id) VALUES(?,?,?,?)";
        Connection c = Database.getConnection();
        PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        try {
            ps.setString(1, cst.getName());
            ps.setString(2, cst.getEmail());
            ps.setString(3, cst.getPhone());
            if (cst.getPlanId() == null) ps.setNull(4, Types.INTEGER); else ps.setInt(4, cst.getPlanId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) cst.setId(rs.getInt(1));
            }
        } finally {
            ps.close();
        }
        return cst;
    }

    public Customer findById(int id) throws SQLException {
        String sql = "SELECT id,name,email,phone,plan_id FROM customers WHERE id = ?";
        Connection c = Database.getConnection();
        PreparedStatement ps = c.prepareStatement(sql);
        try {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            try {
                if (rs.next()) {
                    Customer cst = new Customer();
                    cst.setId(rs.getInt("id"));
                    cst.setName(rs.getString("name"));
                    cst.setEmail(rs.getString("email"));
                    cst.setPhone(rs.getString("phone"));
                    int pid = rs.getInt("plan_id");
                    if (!rs.wasNull()) cst.setPlanId(pid);
                    return cst;
                }
            } finally {
                rs.close();
            }
        } finally {
            ps.close();
        }
        return null;
    }

    public List<Customer> listAll() throws SQLException {
        List<Customer> out = new ArrayList<>();
        String sql = "SELECT id,name,email,phone,plan_id FROM customers";
        Connection c = Database.getConnection();
        PreparedStatement ps = c.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        try {
            while (rs.next()) {
                Customer cst = new Customer();
                cst.setId(rs.getInt("id"));
                cst.setName(rs.getString("name"));
                cst.setEmail(rs.getString("email"));
                cst.setPhone(rs.getString("phone"));
                int pid = rs.getInt("plan_id");
                if (!rs.wasNull()) cst.setPlanId(pid);
                out.add(cst);
            }
        } finally {
            rs.close();
            ps.close();
        }
        return out;
    }

    public void assignPlan(int customerId, int planId) throws SQLException {
        String sql = "UPDATE customers SET plan_id = ? WHERE id = ?";
        Connection c = Database.getConnection();
        PreparedStatement ps = c.prepareStatement(sql);
        try {
            ps.setInt(1, planId);
            ps.setInt(2, customerId);
            ps.executeUpdate();
        } finally {
            ps.close();
        }
    }
}
