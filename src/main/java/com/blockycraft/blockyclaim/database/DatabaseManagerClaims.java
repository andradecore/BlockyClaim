package com.blockycraft.blockyclaim.database;

import java.sql.*;

public class DatabaseManagerClaims {
    private Connection connection;

    public DatabaseManagerClaims(String dbPath) throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        createTable();
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS claims (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "uuid TEXT NOT NULL," +
                "username TEXT NOT NULL," +
                "claimed_at INTEGER NOT NULL," +
                "x1 INTEGER NOT NULL," +
                "z1 INTEGER NOT NULL," +
                "x2 INTEGER NOT NULL," +
                "z2 INTEGER NOT NULL" +
                ");";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Insere nova claim. Sempre normaliza os valores de coordenadas. */
    public int insertClaim(String uuid, String username, long claimedAt, int x1, int z1, int x2, int z2) {
        int minX = Math.min(x1, x2);
        int minZ = Math.min(z1, z2);
        int maxX = Math.max(x1, x2);
        int maxZ = Math.max(z1, z2);
        String sql = "INSERT INTO claims(uuid, username, claimed_at, x1, z1, x2, z2) VALUES(?,?,?,?,?,?,?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, uuid);
            pstmt.setString(2, username);
            pstmt.setLong(3, claimedAt);
            pstmt.setInt(4, minX);
            pstmt.setInt(5, minZ);
            pstmt.setInt(6, maxX);
            pstmt.setInt(7, maxZ);
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /** Atualiza dono e data numa claim, identificada pelas coordenadas normalizadas. */
    public void updateClaimOwnerByCoords(int x1, int z1, int x2, int z2, String uuid, String username, long claimedAt) {
        int minX = Math.min(x1, x2);
        int minZ = Math.min(z1, z2);
        int maxX = Math.max(x1, x2);
        int maxZ = Math.max(z1, z2);
        String sql = "UPDATE claims SET uuid = ?, username = ?, claimed_at = ? WHERE x1 = ? AND z1 = ? AND x2 = ? AND z2 = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            pstmt.setString(2, username);
            pstmt.setLong(3, claimedAt);
            pstmt.setInt(4, minX);
            pstmt.setInt(5, minZ);
            pstmt.setInt(6, maxX);
            pstmt.setInt(7, maxZ);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Busca id da claim por coordenadas, sempre normalizando. */
    public int getClaimIdByCoords(int x1, int z1, int x2, int z2) {
        int minX = Math.min(x1, x2);
        int minZ = Math.min(z1, z2);
        int maxX = Math.max(x1, x2);
        int maxZ = Math.max(z1, z2);
        String sql = "SELECT id FROM claims WHERE x1 = ? AND z1 = ? AND x2 = ? AND z2 = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, minX);
            pstmt.setInt(2, minZ);
            pstmt.setInt(3, maxX);
            pstmt.setInt(4, maxZ);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static class ClaimRecord {
        public final int id;
        public final String uuid;
        public final String username;
        public final long claimedAt;
        public final int x1, z1, x2, z2;

        public ClaimRecord(int id, String uuid, String username, long claimedAt, int x1, int z1, int x2, int z2) {
            this.id = id;
            this.uuid = uuid;
            this.username = username;
            this.claimedAt = claimedAt;
            this.x1 = x1;
            this.z1 = z1;
            this.x2 = x2;
            this.z2 = z2;
        }
    }
}
