package utils
import org.apache.commons.codec.binary.Base64
import java.security._
import java.security.spec.X509EncodedKeySpec

object PublicKeyUtils {
  val removeBeginAndEnd: (String) => String = (str) => str.split('\n').dropRight(1).tail.mkString("")
  val publicKeyFromString: (String) => PublicKey = (key) => {
    val publicBytes: Array[Byte] = Base64.decodeBase64(key)
    val keySpec: X509EncodedKeySpec = new X509EncodedKeySpec(publicBytes)
    val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")
    keyFactory.generatePublic(keySpec)
  }
}
