package cn.foreveryang.my12306;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@SpringBootTest
public class MysqlTest {
    @Autowired
    private DataSource dataSource;

    @Test
    void tryConnect() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            assert conn != null;
            assert conn.isValid(3);
        }
        System.out.println("数据库连接成功");
    }
}
