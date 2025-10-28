package com.blockycraft.blockyclaim.database;

import java.sql.*;

public class DatabaseManager {
    private Connection connection;

    /**
     * Inicializa conexão com banco SQLite. Cria o arquivo e as tabelas se não existirem.
     * @param dbPath Caminho absoluto para o arquivo compras.db
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public DatabaseManager(String dbPath) throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        createTables();
    }

    /**
     * Cria a tabela de compras no banco caso não exista ainda.
     */
    private void createTables() {
        String sql = "CREATE TABLE IF NOT EXISTS compras (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "uuid TEXT NOT NULL," +
                "username TEXT NOT NULL," +
                "blocks_bought INTEGER NOT NULL," +
                "iron_bars_spent INTEGER NOT NULL," +
                "bought_at INTEGER NOT NULL" +
                ");";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Registra uma compra de blocos no banco.
     * @param uuid UUID do jogador
     * @param username Nome do jogador
     * @param blocksBought Qtde de blocos comprados
     * @param ironBarsSpent Qtde de barras de ferro utilizadas
     * @param boughtAt Timestamp Unix UTC em segundos
     */
    public void logCompra(String uuid, String username, int blocksBought, int ironBarsSpent, long boughtAt) {
        String sql = "INSERT INTO compras(uuid, username, blocks_bought, iron_bars_spent, bought_at) VALUES(?,?,?,?,?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            pstmt.setString(2, username);
            pstmt.setInt(3, blocksBought);
            pstmt.setInt(4, ironBarsSpent);
            pstmt.setLong(5, boughtAt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fecha a conexão com o banco. Deve ser chamado ao desabilitar o plugin.
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
