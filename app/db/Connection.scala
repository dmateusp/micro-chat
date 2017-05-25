package db

import org.sedis._
import redis.clients.jedis._
object DB {
  val pool = new Pool(new JedisPool(new JedisPoolConfig(), "localhost", 6379, 2000))
}


/* USAGE EXAMPLE FROM https://github.com/pk11/sedis
import Dress._
pool.withClient { client =>
 client.get("single").isDefined.must(be(true))
 client.get("single").get.must(be("foo"))
 client.lindex("test",0).must(be("bar"))
 val r: List[String] = client.lrange("test",0,2)
 r.size.must(be(2))
 r.toString.must(be("List(bar, foo)"))
 val s: List[String] = client.sort("test")
 s.size.must(be(2))
 s.toString.must(be("List(bar, foo)"))
}
*/
