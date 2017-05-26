package db

import org.sedis._
import redis.clients.jedis._
object DB {
  val pool = new Pool(new JedisPool(new JedisPoolConfig(), "localhost", 6379, 2000, "PASSWORD"))
}
