import org.scalacheck.{Gen, Arbitrary}

final case class Tweet(value: String)
object Tweet {
  lazy val tweets = io.Source.fromURL(classOf[Tweet].getResource("tweets100k.txt"))
    .getLines().map(_.trim).map(Tweet.apply).toArray
  implicit val arbitrary: Arbitrary[Tweet] = Arbitrary(Gen.oneOf(tweets))
}