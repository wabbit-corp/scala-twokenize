package osu.nlp.twokenizer

import scala.util.matching.Regex

/**
  * Created by alex on 10/31/16.
  */
object detail {
  val Contractions = new Regex("(?i)(\\w+)(n['’′]t|['’′]ve|['’′]ll|['’′]d|['’′]re|['’′]s|['’′]m)$")
  val Whitespace = new Regex("[\\s\\p{Zs}]+")

  val punctChars = "['\"“”‘’.?!…,:;]"
  //val punctSeq   = punctChars+"+"	//'anthem'. => ' anthem '.
  val PunctSeq   = "['\"“”‘’]+|[.?!,…]+|[:;]+"	//'anthem'. => ' anthem ' .
  val Entity     = "&(?:amp|lt|gt|quot);"

  //  URLs
  val Url = {
    // BTO 2012-06: everyone thinks the daringfireball regex should be better, but they're wrong.
    // If you actually empirically test it the results are bad.
    // Please see https://github.com/brendano/ark-tweet-nlp/pull/9
    val urlStart1  = "(?:https?://|\\bwww\\.)"
    val commonTLDs = "(?:com|org|edu|gov|net|mil|aero|asia|biz|cat|coop|info|int|jobs|mobi|museum|name|pro|tel|travel|xxx)"
    val ccTLDs	 = "(?:ac|ad|ae|af|ag|ai|al|am|an|ao|aq|ar|as|at|au|aw|ax|az|ba|bb|bd|be|bf|bg|bh|bi|bj|bm|bn|bo|br|bs|bt|" +
      "bv|bw|by|bz|ca|cc|cd|cf|cg|ch|ci|ck|cl|cm|cn|co|cr|cs|cu|cv|cx|cy|cz|dd|de|dj|dk|dm|do|dz|ec|ee|eg|eh|" +
      "er|es|et|eu|fi|fj|fk|fm|fo|fr|ga|gb|gd|ge|gf|gg|gh|gi|gl|gm|gn|gp|gq|gr|gs|gt|gu|gw|gy|hk|hm|hn|hr|ht|" +
      "hu|id|ie|il|im|in|io|iq|ir|is|it|je|jm|jo|jp|ke|kg|kh|ki|km|kn|kp|kr|kw|ky|kz|la|lb|lc|li|lk|lr|ls|lt|" +
      "lu|lv|ly|ma|mc|md|me|mg|mh|mk|ml|mm|mn|mo|mp|mq|mr|ms|mt|mu|mv|mw|mx|my|mz|na|nc|ne|nf|ng|ni|nl|no|np|" +
      "nr|nu|nz|om|pa|pe|pf|pg|ph|pk|pl|pm|pn|pr|ps|pt|pw|py|qa|re|ro|rs|ru|rw|sa|sb|sc|sd|se|sg|sh|si|sj|sk|" +
      "sl|sm|sn|so|sr|ss|st|su|sv|sy|sz|tc|td|tf|tg|th|tj|tk|tl|tm|tn|to|tp|tr|tt|tv|tw|tz|ua|ug|uk|us|uy|uz|" +
      "va|vc|ve|vg|vi|vn|vu|wf|ws|ye|yt|za|zm|zw)";	//TODO: remove obscure country domains?
    val urlStart2  = "\\b(?:[A-Za-z\\d-])+(?:\\.[A-Za-z0-9]+){0,3}\\." + "(?:"+commonTLDs+"|"+ccTLDs+")"+"(?:\\."+ccTLDs+")?(?=\\W|$)"
    val urlBody    = "(?:[^\\.\\s<>][^\\s<>]*?)?"
    val urlExtraCrapBeforeEnd = "(?:"+punctChars+"|"+Entity+")+?"
    val urlEnd     = "(?:\\.\\.+|[<>]|\\s|$)"

    "(?:"+urlStart1+"|"+urlStart2+")"+urlBody+"(?=(?:"+urlExtraCrapBeforeEnd+")?"+urlEnd+")"
  }


  // Numeric
  val TimeLike   = "\\d+(?::\\d+){1,2}"
  //val numNum     = "\\d+\\.\\d+";
  val NumberWithCommas = "(?:(?<!\\d)\\d{1,3},)+?\\d{3}" + "(?=(?:[^,\\d]|$))"
  val NumComb	 = "\\p{Sc}?\\d+(?:\\.\\d+)+%?"

  // Abbreviations
  val ArbitraryAbbrev = {
    val boundaryNotDot = "(?:$|\\s|[“\\u0022?!,:;]|" + Entity + ")"
    val aa1  = "(?:[A-Za-z]\\.){2,}(?=" + boundaryNotDot + ")"
    val aa2  = "[^A-Za-z](?:[A-Za-z]\\.){1,}[A-Za-z](?=" + boundaryNotDot + ")"
    val standardAbbreviations = "\\b(?:[Mm]r|[Mm]rs|[Mm]s|[Dd]r|[Ss]r|[Jj]r|[Rr]ep|[Ss]en|[Ss]t)\\."
    "(?:" + aa1 +"|"+ aa2 + "|" + standardAbbreviations + ")"
  }

  val Separators  = "(?:--+|―|—|~|–|=)"
  val Decorations = "(?:[♫♪]+|[★☆]+|[♥❤♡]+|[\\u2639-\\u263b]+|[\\ue001-\\uebbb]+)"
  val thingsThatSplitWords = "[^\\s\\.,?\"]"
  val EmbeddedApostrophe = thingsThatSplitWords+"+['’′]" + thingsThatSplitWords + "*"

  def or(parts: String*): String = "(?:" + parts.mkString("|") + ")"

  //  Emoticons
  val Emoticon = {
    val normalEyes = "(?iu)[:=]" // 8 and x are eyes but cause problems
    val wink = "[;]"
    val noseArea = "(?:|-|[^a-zA-Z0-9 ])" // doesn't get :'-(
    val happyMouths = "[D\\)\\]\\}]+"
    val sadMouths = "[\\(\\[\\{]+"
    val tongue = "[pPd3]+"
    val otherMouths = "(?:[oO]+|[/\\\\]+|[vV]+|[Ss]+|[|]+)" // remove forward slash if http://'s aren't cleaned

    // mouth repetition examples:
    // @aliciakeys Put it in a love song :-))
    // @hellocalyclops =))=))=)) Oh well

    val bfLeft = "(♥|0|o|°|v|\\$|t|x|;|\\u0CA0|@|ʘ|•|・|◕|\\^|¬|\\*)"
    val bfCenter = "(?:[\\.]|[_-]+)"
    val bfRight = "\\2"
    val s3 = "(?:--['\"])"
    val s4 = "(?:<|&lt;|>|&gt;)[\\._-]+(?:<|&lt;|>|&gt;)"
    val s5 = "(?:[.][_]+[.])"
    val basicface = "(?:(?i)" +bfLeft+bfCenter+bfRight+ ")|" +s3+ "|" +s4+ "|" + s5

    val eeLeft = "[＼\\\\ƪԄ\\(（<>;ヽ\\-=~\\*]+"
    val eeRight= "[\\-=\\);'\\u0022<>ʃ）/／ノﾉ丿╯σっµ~\\*]+"
    val eeSymbol = "[^A-Za-z0-9\\s\\(\\)\\*:=-]"
    val eastEmote = eeLeft + "(?:"+basicface+"|" +eeSymbol+")+" + eeRight

    or(
      // Standard version  :) :( :] :D :P
      "(?:>|&gt;)?" + or(normalEyes, wink) + or(noseArea,"[Oo]") +
        or(tongue+"(?=\\W|$|RT|rt|Rt)", otherMouths+"(?=\\W|$|RT|rt|Rt)", sadMouths, happyMouths),

      // reversed version (: D:  use positive lookbehind to remove "(word):"
      // because eyes on the right side is more ambiguous with the standard usage of : ;
      "(?<=(?: |^))" + or(sadMouths,happyMouths,otherMouths) + noseArea + or(normalEyes, wink) + "(?:<|&lt;)?",

      //inspired by http://en.wikipedia.org/wiki/User:Scapler/emoticons#East_Asian_style
      eastEmote.replaceFirst("2", "1"), basicface
      // iOS 'emoji' characters (some smileys, some symbols) [\ue001-\uebbb]
      // TODO should try a big precompiled lexicon from Wikipedia, Dan Ramage told me (BTO) he does this
    )
  }

  val Hearts = "(?:<+/?3+)+" //the other hearts are in decorations

  val Arrows = "(?:<*[-―—=]*>+|<+[-―—=]*>*)|\\p{InArrows}+"

  // BTO 2011-06: restored Hashtag, AtMention protection (dropped in original scala port) because it fixes
  // "hello (#hashtag)" ==> "hello (#hashtag )"  WRONG
  // "hello (#hashtag)" ==> "hello ( #hashtag )"  RIGHT
  // "hello (@person)" ==> "hello (@person )"  WRONG
  // "hello (@person)" ==> "hello ( @person )"  RIGHT
  // ... Some sort of weird interaction with edgepunct I guess, because edgepunct
  // has poor content-symbol detection.

  // This also gets #1 #40 which probably aren't hashtags .. but good as tokens.
  // If you want good hashtag identification, use a different regex.
  val Hashtag = "#[a-zA-Z0-9_]+"  //optional: lookbehind for \b
  //optional: lookbehind for \b, max length 15
  val AtMention = "[@＠][a-zA-Z0-9_]+"

  // I was worried this would conflict with at-mentions
  // but seems ok in sample of 5800: 7 changes all email fixes
  // http://www.regular-expressions.info/email.html

  val Email = {
    val bound = "(?:\\W|^|$)"
    "(?<=" + bound + ")[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}(?=" + bound + ")"
  }

  // We will be tokenizing using these regexps as delimiters
  // Additionally, these things are "protected", meaning they shouldn't be further split themselves.
  val Protected = new Regex(or(
    Hearts,
    Url,
    Email,
    TimeLike,
    //numNum,
    NumberWithCommas,
    NumComb,
    Emoticon,
    Arrows,
    Entity,
    PunctSeq,
    ArbitraryAbbrev,
    Separators,
    Decorations,
    EmbeddedApostrophe,
    Hashtag,
    AtMention
  ))

  // Edge punctuation
  // Want: 'foo' => ' foo '
  // While also:   don't => don't
  // the first is considered "edge punctuation".
  // the second is word-internal punctuation -- don't want to mess with it.
  // BTO (2011-06): the edgepunct system seems to be the #1 source of problems these days.
  // I remember it causing lots of trouble in the past as well.  Would be good to revisit or eliminate.

  // Note the 'smart quotes' (http://en.wikipedia.org/wiki/Smart_quotes)
  val edgePunctChars    = "'\"“”‘’«»{}\\(\\)\\[\\]\\*&" //add \\p{So}? (symbols)
  val edgePunct    = "[" + edgePunctChars + "]"
  val notEdgePunct = "[a-zA-Z0-9]" // content characters
  val offEdge = "(^|$|:|;|\\s|\\.|,)"  // colon here gets "(hello):" ==> "( hello ):"
  val EdgePunctLeft  = new Regex(offEdge + "("+edgePunct+"+)("+notEdgePunct+")")
  val EdgePunctRight = new Regex("("+notEdgePunct+")("+edgePunct+"+)" + offEdge)

  def splitEdgePunct(input: String): String =
    EdgePunctRight.replaceAllIn(EdgePunctLeft.replaceAllIn(input, "$1$2 $3"), "$1 $2$3")

  implicit class RegexOps(val regex: Regex) extends AnyVal {
    def spans(s: String): Iterator[Either[(Int, Int), (Int, Int)]] = {
      val matchIndices = regex.findAllIn(s).matchData
        .flatMap(m => List(m.start, m.end))
      val indices = Iterator(0) ++ matchIndices ++ Iterator(s.length)
      indices.sliding(2).zipWithIndex.map {
        case (Seq(a, b), n) =>
          if (n % 2 == 0) Left((a, b))
          else Right((a, b))
      }
    }
  }

  // The main work of tokenizing a tweet.
  def simpleTokenize(text: String): List[String] = {
    // Do the no-brainers first
    val splitPunctText = splitEdgePunct(text)

    // Find the matches for subsequences that should be protected, e.g. URLs, "1.0", "U.N.K.L.E.", "12:53".
    Protected.spans(splitPunctText).flatMap {
      case Right((a, b)) => List(splitPunctText.slice(a, b))
      case Left((a, b)) => splitPunctText.slice(a, b).trim.split(" ")
    }.map(_.trim).filter(_.nonEmpty).toList

    // Split based on special patterns (like contractions) and check all tokens are non empty
    // .flatMap(splitToken)
  }

  // Final pass tokenization based on special patterns
  def splitToken(token: String): List[String] = token match {
    case Contractions(w, c) => List(w, c)
    case _ => List(token)
  }
}
