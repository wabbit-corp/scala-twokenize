import org.scalatest.{Matchers, FunSuite}
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import osu.nlp.twokenizer
import scala.collection.JavaConverters._

final class BackwardCompatibility extends FunSuite with GeneratorDrivenPropertyChecks with Matchers {
  def old(s: String): List[String] = OldTwokenizer.tokenizeRawTweetText(s).asScala.toList

  test("tokenizeRawTweetText on real tweets") {
    forAll (minSuccessful(10000)) { (s: Tweet) => twokenizer.tokenizeRawTweetText(s.value) should equal (old(s.value)) }
  }

  test("tokenizeRawTweetText on random inputs") {
    forAll { (s: String) => twokenizer.tokenizeRawTweetText(s) should equal (old(s)) }
  }

  test("tokenize on random inputs") {
    forAll { (s: String) => twokenizer.tokenize(s) should equal (old(s)) }
  }

  test("regression #1") {
    val s = "ummmm.....@kerilynnroche...looking pretty hacked right now"
    twokenizer.tokenizeRawTweetText(s) should equal (old(s))
  }
}
