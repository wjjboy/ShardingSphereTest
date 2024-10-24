package user.wjj;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

/**
 * @author WuJJ
 * @description
 * @date 2024/10/24 14:04
 **/
@Slf4j
@SpringBootTest(classes = Application.class)
public class FederationSuccessTest {

    private static final String sql = "" +
            "select " +
            "   t1.id, t2.name, t1.num " +
            "from ( " +
            "   select " +
            "       id, count(*) as num" +
            "   from t_b " +
            "   group by t_b.id" +
            ") t1 left join ( " +
            "   select " +
            "       id, name from t_a " +
            ") t2 on t1.id = t2.id";

    @Autowired
    ShardingRepository shardingRepository;

    @SneakyThrows
    @Test
    public void test1() {
        // 获取每个任务的结果
        String result1 = query("Task 1");
        String result2 = query("Task 2");
        String result3 = query("Task 3");
        // 打印结果
        log.info("Result 1: {}", result1);
        log.info("Result 2: {}", result2);
        log.info("Result 3: {}", result3);
    }

    public String query(String taskName) {
        log.info("[{}] query start", taskName);
        try {
            List<Map<String, Object>> result = shardingRepository.executeQuery(sql);
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            log.info("[{}] query exception", taskName);
            return "fail";
        } finally {
            log.info("[{}] query end", taskName);
        }
    }

}
