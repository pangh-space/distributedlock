package com.tuling.distributedlock;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;


/***
 * @Author 郭嘉   QQ:2790284115
 * @Slogan 致敬大师，致敬未来的你
 */
@RestController
@Slf4j
public class TestController {


    @Autowired
    private OrderService orderService;

    @Value("${server.port}")
    private String port;


    @Autowired
    CuratorFramework curatorFramework;

    @GetMapping("/stock/deduct/{id}")
    public Object reduceStock(@PathVariable("id") Integer id) throws Exception {

        log.info("线程：" + Thread.currentThread().getName() + "，进入请求...");

        InterProcessMutex interProcessMutex = new InterProcessMutex(curatorFramework, "/product_" + id);
        log.info("线程：" + Thread.currentThread().getName() + "，等待获取锁...");
        try {
            // ...
            interProcessMutex.acquire();
            log.info("线程：" + Thread.currentThread().getName() + "，获取到锁...");
            orderService.reduceStock(id);
            log.info("线程：" + Thread.currentThread().getName() + "，买到产品...");
        } catch (Exception e) {
            log.info("线程：" + Thread.currentThread().getName() + "，晚到，没有产品可售...");
            if (e instanceof RuntimeException) {
                throw e;
            }
        } finally {
            interProcessMutex.release();
        }
        return "ok:" + port;
    }


}
