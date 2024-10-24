package user.wjj;

import com.google.common.collect.Lists;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sqlfederation.config.SQLFederationRuleConfiguration;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

/**
 * 分库分表数据源
 * @author WuJJ
 * @description
 * @date 2024/9/20 13:21
 **/
@Slf4j
@Service
public class ShardingRepository {

    private static final String JDBC_DRIVER_NAME = "com.mysql.jdbc.Driver";
    private static final String JDBC_URL = "jdbc:mysql://10.175.132.25:6306/%s?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false";
    private static final String JDBC_USERNAME = "root";
    private static final String JDBC_PASSWORD = "139ZXCV@qaz";

    /**
     * 库名
     */
    private static final String DATABASE_NAME = "db";
    /**
     * 单表表名
     */
    private static final List<String> SINGLE_TABLES = Lists.newArrayList(
            "t_a"
    );
    /**
     * 分表表名
     * TODO: 动态申明表
     */
    private static final List<String> SHARDING_TABLES = Lists.newArrayList(
            "t_b"
    );

    private DataSource shardingDataSource;


    /**
     * 初始化构建DataSource
     */
    @PostConstruct
    public void init() throws SQLException {
        // 构建数据源
        this.shardingDataSource = createShardingDataSource();
    }

    private DataSource createDataSource(String dbName) {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(JDBC_DRIVER_NAME);
        config.setJdbcUrl(String.format(JDBC_URL, dbName));
        config.setUsername(JDBC_USERNAME);
        config.setPassword(JDBC_PASSWORD);
        config.setMinimumIdle(1);
        config.setMaximumPoolSize(10);
        return new HikariDataSource(config);
    }

    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> map = new HashMap<>(4);
        DataSource ds1 = createDataSource("db1");
        map.put("dn1", ds1);
        DataSource ds2 = createDataSource("db2");
        map.put("dn2", ds2);
        DataSource ds3 = createDataSource("db3");
        map.put("dn3", ds3);
        return map;
    }

    private DataSource createShardingDataSource() throws SQLException {
        // 创建数据源
        Map<String, DataSource> dataSourceMap = createDataSourceMap();
        // 创建逻辑表
        ShardingTableRuleConfiguration tableA = new ShardingTableRuleConfiguration("t_a", "dn1.t_a");
        ShardingTableRuleConfiguration tableB = new ShardingTableRuleConfiguration("t_b", "dn${2..3}.t_b");
        tableB.setDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("id", "inline-algorithm"));
        List<ShardingTableRuleConfiguration> tables = Lists.newArrayList(tableA, tableB);
        // 分库规则
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().addAll(tables);
        // 分库算法
        Properties shardingProperties = new Properties();
        shardingProperties.put("algorithm-expression", "dn${id % 2 + 2}");
        shardingRuleConfig.getShardingAlgorithms().put("inline-algorithm", new AlgorithmConfiguration("INLINE", shardingProperties));
        // federation
        CacheOption executionPlanCache = new CacheOption(1024, 65535L);
        SQLFederationRuleConfiguration federationRuleConfiguration = new SQLFederationRuleConfiguration(true, false, executionPlanCache);
        // config
        Collection<RuleConfiguration> ruleConfigs = Lists.newArrayList();
        ruleConfigs.add(shardingRuleConfig);
        ruleConfigs.add(federationRuleConfiguration);
        // 构建属性配置
        Properties properties = new Properties();
        properties.setProperty("sql-show", "true");
        properties.setProperty("max-connections-size-per-query", "1");
        return ShardingSphereDataSourceFactory.createDataSource(DATABASE_NAME, null, dataSourceMap, ruleConfigs, properties);
    }

    public List<Map<String, Object>> executeQuery(String sql) {
        try(Connection connection = this.shardingDataSource.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            // 执行sql
            List<Map<String, Object>> result = new ArrayList<>();
            try(ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                int columnCount = md.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> rowData = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = md.getColumnName(i).toLowerCase();
                        Object value = rs.getObject(columnName);
                        rowData.put(columnName, value);
                    }
                    result.add(rowData);
                }
                return result;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
