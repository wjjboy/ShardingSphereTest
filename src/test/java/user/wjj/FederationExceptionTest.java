package user.wjj;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author WuJJ
 * @description
 * @date 2024/10/24 14:04
 **/
@Slf4j
@SpringBootTest(classes = Application.class)
public class FederationExceptionTest {

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
        // 定义线程数量
        final int threadCount = 3;
        // 创建线程池
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        // 创建 CountDownLatch，计数器初始化为线程数量
        CountDownLatch latch = new CountDownLatch(threadCount);
        // 提交任务到线程池，并获取 Future 对象
        Future<String> future1 = executorService.submit(new CallableTask(latch, "Task 1", shardingRepository, sql));
        Future<String> future2 = executorService.submit(new CallableTask(latch, "Task 2", shardingRepository, sql));
        Future<String> future3 = executorService.submit(new CallableTask(latch, "Task 3", shardingRepository, sql));
        // 启动所有线程
        latch.await();
        // 获取每个任务的结果
        String result1 = future1.get();
        String result2 = future2.get();
        String result3 = future3.get();
        // 打印结果
        log.info("Result 1: {}", result1);
        log.info("Result 2: {}", result2);
        log.info("Result 3: {}", result3);
        // 关闭线程池
        executorService.shutdown();
    }

    // 定义一个 Callable 任务，实现 Callable 接口
    static class CallableTask implements Callable<String> {
        private final CountDownLatch latch;
        private final String taskName;
        private final String sql;
        private final ShardingRepository shardingRepository;

        public CallableTask(CountDownLatch latch,
                            String taskName,
                            ShardingRepository shardingRepository,
                            String sql) {
            this.latch = latch;
            this.taskName = taskName;
            this.shardingRepository = shardingRepository;
            this.sql = sql;
        }

        @Override
        public String call() throws Exception {
            // 计数器减 1
            latch.countDown();
            // 等待直到 CountDownLatch 计数器为 0
            latch.await();
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

}
