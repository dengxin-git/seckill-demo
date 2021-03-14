# seckill-demo2
这是一个简易的秒杀系统，使用了一些简单的秒杀思想

第一次秒杀:
线程数500，Ramp-Up时间1秒，总库存15
商品超卖140
吞吐量187.7/sec

超卖原因：多线并发情况下，仅仅在业务层中对库存与已售进行判断会出现线程安全问题，比如在一个service层的秒杀事务中查到了已售数量小于秒杀商品库存量，于是开始下单，但是在事务提交之前别的线程已经将商品秒杀完毕，这时再提交事务就会生成超卖的订单，导致订单数大于原有库存数


=================================


第一次优化思路：使用synchronized同步秒杀kill方法
，即使用悲观锁

优化后进行秒杀的结果：
商品无超卖
吞吐量92.0/sec

第一次优化后总结：
优点：解决了超卖
缺点1：使用synchronized同步秒杀方法后对系统造成阻塞，吞吐量变低，效率变低
缺点2：如果在@Transcational里面使用同步导致线程的执行比事务的范围小，会出现这个同步方法执行完毕后事务还未提交，
但是别的线程读到了这个事务还未提交的数据，最后导致超卖，并有可能产生脏读。



=================================



第二次优化思路：把超卖问题交给数据库，使用乐观锁，给库存表增加一个version字段，每更新一次version字段就自增，
然后在下一次更新前将version查出来，然后在更新时用where比对查出来的version与数据库中的version是否一样，
若一样则说明没有人进行更新，则执行此次操作，否则更新失败。

优化后进行秒杀的结果：
商品无超卖
吞吐量488.3/sec

第二次优化后总结：
解决了超卖
系统吞吐量明显提升，效率明显提高，速度变快


===================================

第三次优化思路：
前提：我在jmeter把线程数调到了20000结果电脑变得奇卡无比,jmeter也卡死了不得已强制重启了电脑。
大量请求查询到有库存便进调用下单接口，这使得服务器和数据库压力剧增。
虽然解决了超卖的问题，但是在极端情况下突然的流量暴增可能会让系统效率变得非常缓慢，甚至宕机

解决方案：接口限流（令牌桶算法），使用google开源工具RateLimter
令牌桶算法：自行固定的以恒定的速率生产令牌，如果令牌没有被消耗就会慢慢被填满，溢出出的令牌就抛弃，
如果有请求拿到了令牌就进行业务处理(下单)，没有拿到的话就等待(有两种方案:一是一直等待拿到令牌，二是等待一定时间如果没拿到令牌就拒绝此次请求(此种方案有可能出现库存未被秒杀完全))
所以在等待拿令牌的过程中就解决了接口限流

将线程数改为15000，令牌桶最大容量为10，设置2秒后未获取到令牌就请求失败
优化后进行秒杀结果结果：
商品无超卖

第三次优化后总结：
系统没有像第一次一样变得卡顿
实现了接口限流

=====================
第四次优化思路
前提：不可能什么时间都可以访问秒杀接口，另外还要防止有人获取秒杀接口用脚本抢购，所以得隐藏秒杀接口和加密。
同一用户多次点击抢购或者使用脚本一直抢购加大服务器压力，也需对其进行一定时间内的访问次数限制。

解决方案：在秒杀开始时再redis中加入商品过期时间，对还未开始的商品请求和过期的商品秒杀请求进行拒绝(启动项目时将商品放入redis，加EX商品过期时间，秒杀时先看redis的该商品是否过期)
用用户id+商品id+盐生成MD5加密的签名放入redis，必须有此有此签名才能进行秒杀
对单个用户限制访问频率(使用redis计数,设置过期时间作为限制时间)

第四次优化后总结:
秒杀之前未获取MD5加密的请求都被拒绝
未到秒杀开始时间或者时间已过的请求也被拒绝
访问时同一用户6分钟内求情秒杀接口次数超过10次(设置的过期时间和最大次数分别为360秒和10次)的请求也失败
