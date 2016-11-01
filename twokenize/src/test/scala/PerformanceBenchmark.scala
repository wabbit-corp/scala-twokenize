import org.scalameter.api._
import osu.nlp.twokenize

class PerformanceBenchmark extends Bench.LocalTime {
  val step = 500
  val maxSize = 3000
  val tweets = Tweet.tweets.slice(0, maxSize).map(_.value).toIterable
  val sizes = Gen.range("size")(step, maxSize, step)
  val inputs = sizes.map(size => tweets.take(size))

  performance of "Twokenize" in {
    measure method "simpleTwokenize" in {
      using(inputs) in { input =>
        input.toIterator.map(twokenize.simpleTokenize).map(_.length).sum
      }
    }
  }
}
