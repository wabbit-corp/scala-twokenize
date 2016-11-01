package osu.nlp

import org.apache.commons.lang3.StringEscapeUtils

package object twokenizer {
  import detail._

  /** "foo   bar " => "foo bar" */
  def squeezeWhitespace(input: String): String =
    Whitespace.replaceAllIn(input, " ").trim()

  /**
    * Twitter text comes HTML-escaped, so unescape it.
    * We also first unescape &amp;'s, in case the text has been buggily double-escaped.
    */
  def normalizeTextForTagger(text: String): String =
    StringEscapeUtils.unescapeHtml4(text.replaceAll("&amp;", "&"))

  /** Assume 'text' has no HTML escaping. **/
  def tokenize(text: String): List[String] =
    simpleTokenize(squeezeWhitespace(text))

  /**
    * This is intended for raw tweet text -- we do some HTML entity unescaping before running the tagger.
    *
    * This function normalizes the input text BEFORE calling the tokenizer.
    * So the tokens you get back may not exactly correspond to
    * substrings of the original text.
    */
  def tokenizeRawTweetText(text: String): List[String] =
    tokenize(normalizeTextForTagger(text))
}
