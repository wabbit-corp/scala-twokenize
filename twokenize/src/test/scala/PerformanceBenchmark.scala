import org.scalameter.api._
import osu.nlp.twokenizer

final class PerformanceBenchmark extends Bench.LocalTime {
  val step = 500
  val maxSize = 3000
  val tweets = Tweet.tweets.slice(0, maxSize).map(_.value).toIterable
  val sizes = Gen.range("size")(step, maxSize, step)
  val inputs = sizes.map(size => tweets.take(size))

  performance of "Twokenizer" in {
    measure method "tokenize" in {
      using(inputs) in { input =>
        input.toIterator.map(twokenizer.tokenize).map(_.length).sum
      }
    }
  }
}
