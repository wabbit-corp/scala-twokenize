import org.scalatest.{Matchers, FunSuite}
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import osu.nlp.twokenize
import scala.collection.JavaConverters._

class BackwardCompatibility extends FunSuite with GeneratorDrivenPropertyChecks with Matchers {
  def old(s: String): List[String] = OldTwokenizer.simpleTokenize(s).asScala.toList

  test("simpleTokenize on real tweets") {
    forAll (minSuccessful(10000)) { (s: Tweet) => twokenize.simpleTokenize(s.value) should equal (old(s.value)) }
  }

  test("simpleTokenize on random inputs") {
    forAll { (s: String) => twokenize.simpleTokenize(s) should equal (old(s)) }
  }

  test("regression #1") {
    val s = "ummmm.....@kerilynnroche...looking pretty hacked right now"
    twokenize.simpleTokenize(s) should equal (old(s))
  }
}
