import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Twokenize -- a tokenizer designed for Twitter text in English and some other European languages.
 * This is the Java version. If you want the old Python version, see: http://github.com/brendano/tweetmotif
 *
 * This tokenizer code has gone through a long history:
 *
 * (1) Brendan O'Connor wrote original version in Python, http://github.com/brendano/tweetmotif
 *        TweetMotif: Exploratory Search and Topic Summarization for Twitter.
 *        Brendan O'Connor, Michel Krieger, and David Ahn.
 *        ICWSM-2010 (demo track), http://brenocon.com/oconnor_krieger_ahn.icwsm2010.tweetmotif.pdf
 * (2a) Kevin Gimpel and Daniel Mills modified it for POS tagging for the CMU ARK Twitter POS Tagger
 * (2b) Jason Baldridge and David Snyder ported it to Scala
 * (3) Brendan bugfixed the Scala port and merged with POS-specific changes
 *     for the CMU ARK Twitter POS Tagger
 * (4) Tobi Owoputi ported it back to Java and added many improvements (2012-06)
 *
 * Current home is http://github.com/brendano/ark-tweet-nlp and http://www.ark.cs.cmu.edu/TweetNLP
 *
 * There have been at least 2 other Java ports, but they are not in the lineage for the code here.
 */
public class OldTwokenizer {
    public static Pattern Contractions = Pattern.compile("(?i)(\\w+)(n['’′]t|['’′]ve|['’′]ll|['’′]d|['’′]re|['’′]s|['’′]m)$");
    public static Pattern Whitespace = Pattern.compile("[\\s\\p{Zs}]+");

    public static String punctChars = "['\"“”‘’.?!…,:;]";
    //static String punctSeq   = punctChars+"+";	//'anthem'. => ' anthem '.
    public static String punctSeq   = "['\"“”‘’]+|[.?!,…]+|[:;]+";	//'anthem'. => ' anthem ' .
    public static String entity     = "&(?:amp|lt|gt|quot);";
    //  URLs

    // BTO 2012-06: everyone thinks the daringfireball regex should be better, but they're wrong.
    // If you actually empirically test it the results are bad.
    // Please see https://github.com/brendano/ark-tweet-nlp/pull/9

    public static String urlStart1  = "(?:https?://|\\bwww\\.)";
    public static String commonTLDs = "(?:com|org|edu|gov|net|mil|aero|asia|biz|cat|coop|info|int|jobs|mobi|museum|name|pro|tel|travel|xxx)";
    public static String ccTLDs	 = "(?:ac|ad|ae|af|ag|ai|al|am|an|ao|aq|ar|as|at|au|aw|ax|az|ba|bb|bd|be|bf|bg|bh|bi|bj|bm|bn|bo|br|bs|bt|" +
                               "bv|bw|by|bz|ca|cc|cd|cf|cg|ch|ci|ck|cl|cm|cn|co|cr|cs|cu|cv|cx|cy|cz|dd|de|dj|dk|dm|do|dz|ec|ee|eg|eh|" +
                               "er|es|et|eu|fi|fj|fk|fm|fo|fr|ga|gb|gd|ge|gf|gg|gh|gi|gl|gm|gn|gp|gq|gr|gs|gt|gu|gw|gy|hk|hm|hn|hr|ht|" +
                               "hu|id|ie|il|im|in|io|iq|ir|is|it|je|jm|jo|jp|ke|kg|kh|ki|km|kn|kp|kr|kw|ky|kz|la|lb|lc|li|lk|lr|ls|lt|" +
                               "lu|lv|ly|ma|mc|md|me|mg|mh|mk|ml|mm|mn|mo|mp|mq|mr|ms|mt|mu|mv|mw|mx|my|mz|na|nc|ne|nf|ng|ni|nl|no|np|" +
                               "nr|nu|nz|om|pa|pe|pf|pg|ph|pk|pl|pm|pn|pr|ps|pt|pw|py|qa|re|ro|rs|ru|rw|sa|sb|sc|sd|se|sg|sh|si|sj|sk|" +
                               "sl|sm|sn|so|sr|ss|st|su|sv|sy|sz|tc|td|tf|tg|th|tj|tk|tl|tm|tn|to|tp|tr|tt|tv|tw|tz|ua|ug|uk|us|uy|uz|" +
                               "va|vc|ve|vg|vi|vn|vu|wf|ws|ye|yt|za|zm|zw)";	//TODO: remove obscure country domains?
    public static String urlStart2  = "\\b(?:[A-Za-z\\d-])+(?:\\.[A-Za-z0-9]+){0,3}\\." + "(?:"+commonTLDs+"|"+ccTLDs+")"+"(?:\\."+ccTLDs+")?(?=\\W|$)";
    public static String urlBody    = "(?:[^\\.\\s<>][^\\s<>]*?)?";
    public static String urlExtraCrapBeforeEnd = "(?:"+punctChars+"|"+entity+")+?";
    public static String urlEnd     = "(?:\\.\\.+|[<>]|\\s|$)";
    public static String url        = "(?:"+urlStart1+"|"+urlStart2+")"+urlBody+"(?=(?:"+urlExtraCrapBeforeEnd+")?"+urlEnd+")";


    // Numeric
    public static String timeLike   = "\\d+(?::\\d+){1,2}";
    //static String numNum     = "\\d+\\.\\d+";
    public static String numberWithCommas = "(?:(?<!\\d)\\d{1,3},)+?\\d{3}" + "(?=(?:[^,\\d]|$))";
    public static String numComb	 = "\\p{Sc}?\\d+(?:\\.\\d+)+%?";

    // Abbreviations
    public static String boundaryNotDot = "(?:$|\\s|[“\\u0022?!,:;]|" + entity + ")";
    public static String aa1  = "(?:[A-Za-z]\\.){2,}(?=" + boundaryNotDot + ")";
    public static String aa2  = "[^A-Za-z](?:[A-Za-z]\\.){1,}[A-Za-z](?=" + boundaryNotDot + ")";
    public static String standardAbbreviations = "\\b(?:[Mm]r|[Mm]rs|[Mm]s|[Dd]r|[Ss]r|[Jj]r|[Rr]ep|[Ss]en|[Ss]t)\\.";
    public static String arbitraryAbbrev = "(?:" + aa1 +"|"+ aa2 + "|" + standardAbbreviations + ")";
    public static String separators  = "(?:--+|―|—|~|–|=)";
    public static String decorations = "(?:[♫♪]+|[★☆]+|[♥❤♡]+|[\\u2639-\\u263b]+|[\\ue001-\\uebbb]+)";
    public static String thingsThatSplitWords = "[^\\s\\.,?\"]";
    public static String embeddedApostrophe = thingsThatSplitWords+"+['’′]" + thingsThatSplitWords + "*";

    public static String OR(String... parts) {
        String prefix="(?:";
        StringBuilder sb = new StringBuilder();
        for (String s:parts){
            sb.append(prefix);
            prefix="|";
            sb.append(s);
        }
        sb.append(")");
        return sb.toString();
    }

    //  Emoticons
    public static String normalEyes = "(?iu)[:=]"; // 8 and x are eyes but cause problems
    public static String wink = "[;]";
    public static String noseArea = "(?:|-|[^a-zA-Z0-9 ])"; // doesn't get :'-(
    public static String happyMouths = "[D\\)\\]\\}]+";
    public static String sadMouths = "[\\(\\[\\{]+";
    public static String tongue = "[pPd3]+";
    public static String otherMouths = "(?:[oO]+|[/\\\\]+|[vV]+|[Ss]+|[|]+)"; // remove forward slash if http://'s aren't cleaned

    // mouth repetition examples:
    // @aliciakeys Put it in a love song :-))
    // @hellocalyclops =))=))=)) Oh well

    public static String bfLeft = "(♥|0|o|°|v|\\$|t|x|;|\\u0CA0|@|ʘ|•|・|◕|\\^|¬|\\*)";
    public static String bfCenter = "(?:[\\.]|[_-]+)";
    public static String bfRight = "\\2";
    public static String s3 = "(?:--['\"])";
    public static String s4 = "(?:<|&lt;|>|&gt;)[\\._-]+(?:<|&lt;|>|&gt;)";
    public static String s5 = "(?:[.][_]+[.])";
    public static String basicface = "(?:(?i)" +bfLeft+bfCenter+bfRight+ ")|" +s3+ "|" +s4+ "|" + s5;

    public static String eeLeft = "[＼\\\\ƪԄ\\(（<>;ヽ\\-=~\\*]+";
    public static String eeRight= "[\\-=\\);'\\u0022<>ʃ）/／ノﾉ丿╯σっµ~\\*]+";
    public static String eeSymbol = "[^A-Za-z0-9\\s\\(\\)\\*:=-]";
    public static String eastEmote = eeLeft + "(?:"+basicface+"|" +eeSymbol+")+" + eeRight;


    public static String emoticon = OR(
            // Standard version  :) :( :] :D :P
            "(?:>|&gt;)?" + OR(normalEyes, wink) + OR(noseArea,"[Oo]") +
            OR(tongue+"(?=\\W|$|RT|rt|Rt)", otherMouths+"(?=\\W|$|RT|rt|Rt)", sadMouths, happyMouths),

            // reversed version (: D:  use positive lookbehind to remove "(word):"
            // because eyes on the right side is more ambiguous with the standard usage of : ;
            "(?<=(?: |^))" + OR(sadMouths,happyMouths,otherMouths) + noseArea + OR(normalEyes, wink) + "(?:<|&lt;)?",

            //inspired by http://en.wikipedia.org/wiki/User:Scapler/emoticons#East_Asian_style
            eastEmote.replaceFirst("2", "1"), basicface
            // iOS 'emoji' characters (some smileys, some symbols) [\ue001-\uebbb]
            // TODO should try a big precompiled lexicon from Wikipedia, Dan Ramage told me (BTO) he does this
    );

    public static String Hearts = "(?:<+/?3+)+"; //the other hearts are in decorations

    public static String Arrows = "(?:<*[-―—=]*>+|<+[-―—=]*>*)|\\p{InArrows}+";

    // BTO 2011-06: restored Hashtag, AtMention protection (dropped in original scala port) because it fixes
    // "hello (#hashtag)" ==> "hello (#hashtag )"  WRONG
    // "hello (#hashtag)" ==> "hello ( #hashtag )"  RIGHT
    // "hello (@person)" ==> "hello (@person )"  WRONG
    // "hello (@person)" ==> "hello ( @person )"  RIGHT
    // ... Some sort of weird interaction with edgepunct I guess, because edgepunct
    // has poor content-symbol detection.

    // This also gets #1 #40 which probably aren't hashtags .. but good as tokens.
    // If you want good hashtag identification, use a different regex.
    public static String Hashtag = "#[a-zA-Z0-9_]+";  //optional: lookbehind for \b
    //optional: lookbehind for \b, max length 15
    public static String AtMention = "[@＠][a-zA-Z0-9_]+";

    // I was worried this would conflict with at-mentions
    // but seems ok in sample of 5800: 7 changes all email fixes
    // http://www.regular-expressions.info/email.html
    public static String Bound = "(?:\\W|^|$)";
    public static String Email = "(?<=" +Bound+ ")[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}(?=" +Bound+")";

    // We will be tokenizing using these regexps as delimiters
    // Additionally, these things are "protected", meaning they shouldn't be further split themselves.
    public static Pattern Protected  = Pattern.compile(
            OR(
                    Hearts,
                    url,
                    Email,
                    timeLike,
                    //numNum,
                    numberWithCommas,
                    numComb,
                    emoticon,
                    Arrows,
                    entity,
                    punctSeq,
                    arbitraryAbbrev,
                    separators,
                    decorations,
                    embeddedApostrophe,
                    Hashtag,
                    AtMention
            ));

    // Edge punctuation
    // Want: 'foo' => ' foo '
    // While also:   don't => don't
    // the first is considered "edge punctuation".
    // the second is word-internal punctuation -- don't want to mess with it.
    // BTO (2011-06): the edgepunct system seems to be the #1 source of problems these days.
    // I remember it causing lots of trouble in the past as well.  Would be good to revisit or eliminate.

    // Note the 'smart quotes' (http://en.wikipedia.org/wiki/Smart_quotes)
    public static String edgePunctChars    = "'\"“”‘’«»{}\\(\\)\\[\\]\\*&"; //add \\p{So}? (symbols)
    public static String edgePunct    = "[" + edgePunctChars + "]";
    public static String notEdgePunct = "[a-zA-Z0-9]"; // content characters
    public static String offEdge = "(^|$|:|;|\\s|\\.|,)";  // colon here gets "(hello):" ==> "( hello ):"
    public static Pattern EdgePunctLeft  = Pattern.compile(offEdge + "("+edgePunct+"+)("+notEdgePunct+")");
    public static Pattern EdgePunctRight = Pattern.compile("("+notEdgePunct+")("+edgePunct+"+)" + offEdge);

    public static String splitEdgePunct (String input) {
        Matcher m1 = EdgePunctLeft.matcher(input);
        input = m1.replaceAll("$1$2 $3");
        m1 = EdgePunctRight.matcher(input);
        input = m1.replaceAll("$1 $2$3");
        return input;
    }

    private static class Pair<T1, T2> {
        public T1 first;
        public T2 second;
        public Pair(T1 x, T2 y) { first=x; second=y; }
    }

    // The main work of tokenizing a tweet.
    public static List<String> simpleTokenize (String text) {

        // Do the no-brainers first
        String splitPunctText = splitEdgePunct(text);

        int textLength = splitPunctText.length();

        // BTO: the logic here got quite convoluted via the Scala porting detour
        // It would be good to switch back to a nice simple procedural style like in the Python version
        // ... Scala is such a pain.  Never again.

        // Find the matches for subsequences that should be protected,
        // e.g. URLs, 1.0, U.N.K.L.E., 12:53
        Matcher matches = Protected.matcher(splitPunctText);
        //Storing as List[List[String]] to make zip easier later on
        List<List<String>> bads = new ArrayList<>();	//linked list?
        List<Pair<Integer,Integer>> badSpans = new ArrayList<>();
        while(matches.find()){
            // The spans of the "bads" should not be split.
            if (matches.start() != matches.end()){ //unnecessary?
                List<String> bad = new ArrayList<>(1);
                bad.add(splitPunctText.substring(matches.start(),matches.end()));
                bads.add(bad);
                badSpans.add(new Pair<>(matches.start(), matches.end()));
            }
        }

        // Create a list of indices to create the "goods", which can be
        // split. We are taking "bad" spans like
        //     List((2,5), (8,10))
        // to create
        ///    List(0, 2, 5, 8, 10, 12)
        // where, e.g., "12" here would be the textLength
        // has an even length and no indices are the same
        List<Integer> indices = new ArrayList<>(2 + 2 * badSpans.size());
        indices.add(0);
        for(Pair<Integer,Integer> p:badSpans){
            indices.add(p.first);
            indices.add(p.second);
        }
        indices.add(textLength);

        // Group the indices and map them to their respective portion of the string
        List<List<String>> splitGoods = new ArrayList<>(indices.size() / 2);
        for (int i=0; i<indices.size(); i+=2) {
            String goodstr = splitPunctText.substring(indices.get(i),indices.get(i+1));
            List<String> splitstr = Arrays.asList(goodstr.trim().split(" "));
            splitGoods.add(splitstr);
        }

        //  Reinterpolate the 'good' and 'bad' Lists, ensuring that
        //  additonal tokens from last good item get included
        List<String> zippedStr= new ArrayList<>();
        int i;
        for(i=0; i < bads.size(); i++) {
            zippedStr = addAllnonempty(zippedStr,splitGoods.get(i));
            zippedStr = addAllnonempty(zippedStr,bads.get(i));
        }
        zippedStr = addAllnonempty(zippedStr,splitGoods.get(i));

        // BTO: our POS tagger wants "ur" and "you're" to both be one token.
        // Uncomment to get "you 're"
        /*ArrayList<String> splitStr = new ArrayList<String>(zippedStr.size());
        for(String tok:zippedStr)
        	splitStr.addAll(splitToken(tok));
        zippedStr=splitStr;*/

        return zippedStr;
    }

    private static List<String> addAllnonempty(List<String> master, List<String> smaller){
        for (String s : smaller){
            String strim = s.trim();
            if (strim.length() > 0)
                master.add(strim);
        }
        return master;
    }
    /** "foo   bar " => "foo bar" */
    public static String squeezeWhitespace (String input){
        return Whitespace.matcher(input).replaceAll(" ").trim();
    }

    // Final pass tokenization based on special patterns
    private static List<String> splitToken (String token) {
        Matcher m = Contractions.matcher(token);
        if (m.find()){
            String[] contract = {m.group(1), m.group(2)};
            return Arrays.asList(contract);
        }
        String[] contract = {token};
        return Arrays.asList(contract);
    }

    /** Assume 'text' has no HTML escaping. **/
    public static List<String> tokenize(String text){
        return simpleTokenize(squeezeWhitespace(text));
    }


    /**
     * Twitter text comes HTML-escaped, so unescape it.
     * We also first unescape &amp;'s, in case the text has been buggily double-escaped.
     */
    public static String normalizeTextForTagger(String text) {
        text = text.replaceAll("&amp;", "&");
        text = StringEscapeUtils.unescapeHtml4(text);
        return text;
    }

    /**
     * This is intended for raw tweet text -- we do some HTML entity unescaping before running the tagger.
     *
     * This function normalizes the input text BEFORE calling the tokenizer.
     * So the tokens you get back may not exactly correspond to
     * substrings of the original text.
     */
    public static List<String> tokenizeRawTweetText(String text) {
        return tokenize(normalizeTextForTagger(text));
    }

//    /** Tokenizes tweet texts on standard input, tokenizations on standard output.  Input and output UTF-8. */
//    public static void main(String[] args) throws IOException {
//        BufferedReader input = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
//        PrintStream output = new PrintStream(System.out, true, "UTF-8");
//        String line;
//        while ( (line = input.readLine()) != null) {
//            List<String> toks = tokenizeRawTweetText(line);
//            for (int i=0; i<toks.size(); i++) {
//                output.print(toks.get(i));
//                if (i < toks.size()-1) {
//                    output.print(" ");
//                }
//            }
//            output.print("\n");
//        }
//    }

}