package com.wajiha.sentiment.service;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.regex.*;

/**
 * Advanced Rule-Based Sentiment Analysis Engine
 *
 * Features over the basic version:
 *  - 120+ lexicon entries vs 18
 *  - Negation handling ("not good", "never happy", "don't like")
 *  - Intensity amplifiers ("very", "extremely", "absolutely") and diminishers ("slightly", "barely")
 *  - Punctuation signals (!!!, ???, ALL CAPS)
 *  - Emoji sentiment detection
 *  - Contextual phrase detection ("could be better", "not bad")
 *  - Confidence calibration based on signal strength and text length
 *  - Explainable reasoning field
 */
@Service
public class SentimentService {

    // ── Positive lexicon (word → base score 1–3) ────────────────────────────
    private static final Map<String, Integer> POSITIVE = new LinkedHashMap<>();
    static {
        // Strong positive (score 3)
        POSITIVE.put("outstanding",  3); POSITIVE.put("exceptional",   3);
        POSITIVE.put("excellent",    3); POSITIVE.put("phenomenal",    3);
        POSITIVE.put("extraordinary",3); POSITIVE.put("breathtaking",  3);
        POSITIVE.put("superb",       3); POSITIVE.put("magnificent",   3);
        POSITIVE.put("brilliant",    3); POSITIVE.put("flawless",      3);
        POSITIVE.put("perfect",      3); POSITIVE.put("incredible",    3);
        POSITIVE.put("amazing",      3); POSITIVE.put("fantastic",     3);
        POSITIVE.put("wonderful",    3); POSITIVE.put("delightful",    3);
        // Moderate positive (score 2)
        POSITIVE.put("great",        2); POSITIVE.put("good",          2);
        POSITIVE.put("love",         2); POSITIVE.put("happy",         2);
        POSITIVE.put("enjoy",        2); POSITIVE.put("enjoyed",       2);
        POSITIVE.put("positive",     2); POSITIVE.put("pleased",       2);
        POSITIVE.put("impressive",   2); POSITIVE.put("recommend",     2);
        POSITIVE.put("recommended",  2); POSITIVE.put("beautiful",     2);
        POSITIVE.put("helpful",      2); POSITIVE.put("effective",     2);
        POSITIVE.put("efficient",    2); POSITIVE.put("reliable",      2);
        POSITIVE.put("solid",        2); POSITIVE.put("smooth",        2);
        POSITIVE.put("clean",        2); POSITIVE.put("fast",          2);
        POSITIVE.put("easy",         2); POSITIVE.put("intuitive",     2);
        POSITIVE.put("powerful",     2); POSITIVE.put("robust",        2);
        POSITIVE.put("professional", 2); POSITIVE.put("elegant",       2);
        POSITIVE.put("excited",      2); POSITIVE.put("thrilled",      2);
        POSITIVE.put("grateful",     2); POSITIVE.put("thankful",      2);
        POSITIVE.put("proud",        2); POSITIVE.put("confident",     2);
        POSITIVE.put("motivated",    2); POSITIVE.put("inspired",      2);
        // Mild positive (score 1)
        POSITIVE.put("nice",         1); POSITIVE.put("fine",          1);
        POSITIVE.put("decent",       1); POSITIVE.put("okay",          1);
        POSITIVE.put("ok",           1); POSITIVE.put("acceptable",    1);
        POSITIVE.put("adequate",     1); POSITIVE.put("fair",          1);
        POSITIVE.put("better",       1); POSITIVE.put("improved",      1);
        POSITIVE.put("useful",       1); POSITIVE.put("works",         1);
        POSITIVE.put("like",         1); POSITIVE.put("liked",         1);
        POSITIVE.put("fun",          1); POSITIVE.put("interesting",   1);
        POSITIVE.put("worth",        1); POSITIVE.put("simple",        1);
    }

    // ── Negative lexicon (word → base score 1–3) ────────────────────────────
    private static final Map<String, Integer> NEGATIVE = new LinkedHashMap<>();
    static {
        // Strong negative (score 3)
        NEGATIVE.put("terrible",     3); NEGATIVE.put("horrific",      3);
        NEGATIVE.put("atrocious",    3); NEGATIVE.put("appalling",     3);
        NEGATIVE.put("dreadful",     3); NEGATIVE.put("catastrophic",  3);
        NEGATIVE.put("abysmal",      3); NEGATIVE.put("disgusting",    3);
        NEGATIVE.put("revolting",    3); NEGATIVE.put("horrendous",    3);
        NEGATIVE.put("pathetic",     3); NEGATIVE.put("infuriating",   3);
        NEGATIVE.put("outrageous",   3); NEGATIVE.put("unbearable",    3);
        NEGATIVE.put("unacceptable", 3); NEGATIVE.put("deplorable",    3);
        // Moderate negative (score 2)
        NEGATIVE.put("bad",          2); NEGATIVE.put("awful",         2);
        NEGATIVE.put("hate",         2); NEGATIVE.put("horrible",      2);
        NEGATIVE.put("poor",         2); NEGATIVE.put("worst",         2);
        NEGATIVE.put("broken",       2); NEGATIVE.put("failed",        2);
        NEGATIVE.put("failure",      2); NEGATIVE.put("useless",       2);
        NEGATIVE.put("disappointed", 2); NEGATIVE.put("disappointing", 2);
        NEGATIVE.put("frustrating",  2); NEGATIVE.put("frustrated",    2);
        NEGATIVE.put("annoying",     2); NEGATIVE.put("annoyed",       2);
        NEGATIVE.put("boring",       2); NEGATIVE.put("slow",          2);
        NEGATIVE.put("difficult",    2); NEGATIVE.put("complicated",   2);
        NEGATIVE.put("confusing",    2); NEGATIVE.put("unreliable",    2);
        NEGATIVE.put("buggy",        2); NEGATIVE.put("crashe",        2);
        NEGATIVE.put("error",        2); NEGATIVE.put("problem",       2);
        NEGATIVE.put("issue",        2); NEGATIVE.put("wrong",         2);
        NEGATIVE.put("waste",        2); NEGATIVE.put("regret",        2);
        NEGATIVE.put("angry",        2); NEGATIVE.put("upset",         2);
        NEGATIVE.put("sad",          2); NEGATIVE.put("unhappy",       2);
        // Mild negative (score 1)
        NEGATIVE.put("mediocre",     1); NEGATIVE.put("average",       1);
        NEGATIVE.put("lacking",      1); NEGATIVE.put("limited",       1);
        NEGATIVE.put("missing",      1); NEGATIVE.put("unclear",       1);
        NEGATIVE.put("confusing",    1); NEGATIVE.put("clunky",        1);
        NEGATIVE.put("outdated",     1); NEGATIVE.put("overpriced",    1);
        NEGATIVE.put("expensive",    1); NEGATIVE.put("messy",         1);
    }

    // ── Negation window words ────────────────────────────────────────────────
    private static final Set<String> NEGATIONS = new HashSet<>(Arrays.asList(
        "not", "no", "never", "neither", "nobody", "nothing",
        "nowhere", "nor", "cannot", "can't", "won't", "don't",
        "doesn't", "didn't", "wasn't", "weren't", "isn't", "aren't",
        "hardly", "barely", "scarcely"
    ));

    // ── Intensity amplifiers (multiply score) ────────────────────────────────
    private static final Map<String, Double> AMPLIFIERS = new LinkedHashMap<>();
    static {
        AMPLIFIERS.put("extremely",   2.0); AMPLIFIERS.put("incredibly",  2.0);
        AMPLIFIERS.put("absolutely",  1.9); AMPLIFIERS.put("completely",  1.8);
        AMPLIFIERS.put("utterly",     1.8); AMPLIFIERS.put("deeply",      1.7);
        AMPLIFIERS.put("highly",      1.6); AMPLIFIERS.put("really",      1.5);
        AMPLIFIERS.put("very",        1.5); AMPLIFIERS.put("so",          1.4);
        AMPLIFIERS.put("quite",       1.3); AMPLIFIERS.put("pretty",      1.2);
        AMPLIFIERS.put("somewhat",    0.7); AMPLIFIERS.put("slightly",    0.6);
        AMPLIFIERS.put("barely",      0.4); AMPLIFIERS.put("hardly",      0.3);
        AMPLIFIERS.put("little",      0.5); AMPLIFIERS.put("bit",         0.6);
    }

    // ── Positive / Negative emoji sets ──────────────────────────────────────
    private static final String POSITIVE_EMOJI = "😊😃😄😁🥰😍🤩👍🎉✅💯🌟⭐❤️🔥💪👏🙌😀😆";
    private static final String NEGATIVE_EMOJI = "😞😢😭😡🤬😤👎💔😔😩😫🚫❌💀☹️😠😒";

    // ── Contextual phrases ───────────────────────────────────────────────────
    private static final Map<String, Integer> PHRASES = new LinkedHashMap<>();
    static {
        // Positive phrases (score added directly)
        PHRASES.put("not bad",          2); PHRASES.put("pretty good",     2);
        PHRASES.put("really good",      3); PHRASES.put("very good",       3);
        PHRASES.put("worked well",      2); PHRASES.put("works well",      2);
        PHRASES.put("well done",        2); PHRASES.put("good job",        2);
        PHRASES.put("well built",       2); PHRASES.put("highly recommend", 3);
        PHRASES.put("love it",          3); PHRASES.put("loved it",        3);
        PHRASES.put("great job",        2); PHRASES.put("amazing work",    3);
        // Negative phrases (score subtracted)
        PHRASES.put("could be better", -2); PHRASES.put("needs improvement", -2);
        PHRASES.put("not good",        -3); PHRASES.put("not great",       -2);
        PHRASES.put("not worth",       -2); PHRASES.put("waste of time",   -3);
        PHRASES.put("waste of money",  -3); PHRASES.put("not recommended", -3);
        PHRASES.put("stay away",       -3); PHRASES.put("does not work",   -3);
        PHRASES.put("doesn't work",    -3); PHRASES.put("didn't work",     -3);
        PHRASES.put("not helpful",     -2); PHRASES.put("not useful",      -2);
        PHRASES.put("hard to use",     -2); PHRASES.put("very bad",        -3);
    }

    // ── Main analysis method ─────────────────────────────────────────────────
    public SentimentResult analyze(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new SentimentResult("Neutral", 0.5, "😐", "None", 0, 0,
                "Empty input — no sentiment signals detected.");
        }

        String original = text.trim();
        String lower    = original.toLowerCase();

        // 1. Tokenise
        String[] tokens = lower.split("\\s+|(?=[!?.,:;])|(?<=[!?.,:;])");

        // 2. Phrase matching (before word-level to catch multi-word signals)
        double phraseScore = 0;
        List<String> phraseMatches = new ArrayList<>();
        for (Map.Entry<String, Integer> e : PHRASES.entrySet()) {
            if (lower.contains(e.getKey())) {
                phraseScore += e.getValue();
                phraseMatches.add(e.getKey());
            }
        }

        // 3. Word-level scoring with negation and amplifier windows
        double wordScore  = 0;
        int    posSignals = 0;
        int    negSignals = 0;
        List<String> posMatched = new ArrayList<>();
        List<String> negMatched = new ArrayList<>();

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i].replaceAll("[^a-z']", "");
            if (token.isEmpty()) continue;

            // Check negation in a 3-word window before this token
            boolean negated = false;
            for (int j = Math.max(0, i - 3); j < i; j++) {
                String prev = tokens[j].replaceAll("[^a-z']", "");
                if (NEGATIONS.contains(prev)) { negated = true; break; }
            }

            // Check amplifier in 2-word window before this token
            double amplifier = 1.0;
            for (int j = Math.max(0, i - 2); j < i; j++) {
                String prev = tokens[j].replaceAll("[^a-z']", "");
                if (AMPLIFIERS.containsKey(prev)) {
                    amplifier = AMPLIFIERS.get(prev);
                    break;
                }
            }

            if (POSITIVE.containsKey(token)) {
                double contribution = POSITIVE.get(token) * amplifier;
                if (negated) { wordScore -= contribution; negSignals++; negMatched.add("not " + token); }
                else         { wordScore += contribution; posSignals++; posMatched.add(token); }
            } else if (NEGATIVE.containsKey(token)) {
                double contribution = NEGATIVE.get(token) * amplifier;
                if (negated) { wordScore += contribution * 0.5; posSignals++; posMatched.add("not " + token); }
                else         { wordScore -= contribution; negSignals++; negMatched.add(token); }
            }
        }

        // 4. Punctuation signals
        double punctScore = 0;
        int exclamations = countOccurrences(original, "!");
        int capsRatio    = countCapsWords(original);
        if (exclamations >= 2) punctScore += 1.0;
        if (capsRatio > 2)     punctScore += 0.5;

        // 5. Emoji signals
        double emojiScore = 0;
        for (char c : original.toCharArray()) {
            String s = String.valueOf(c);
            if (POSITIVE_EMOJI.contains(s)) emojiScore += 1.5;
            if (NEGATIVE_EMOJI.contains(s)) emojiScore -= 1.5;
        }

        // 6. Aggregate
        double totalScore = wordScore + phraseScore + emojiScore + punctScore;

        // 7. Map to sentiment + confidence
        String sentiment;
        String emoji;
        String intensity;

        double absScore = Math.abs(totalScore);
        if (totalScore > 0) {
            sentiment = "Positive";
            emoji     = totalScore >= 6 ? "🤩" : totalScore >= 3 ? "😊" : "🙂";
            intensity = absScore >= 6 ? "Strong" : absScore >= 3 ? "Moderate" : "Mild";
        } else if (totalScore < 0) {
            sentiment = "Negative";
            emoji     = totalScore <= -6 ? "😡" : totalScore <= -3 ? "😞" : "😕";
            intensity = absScore >= 6 ? "Strong" : absScore >= 3 ? "Moderate" : "Mild";
        } else {
            sentiment = "Neutral";
            emoji     = "😐";
            intensity = "None";
        }

        // 8. Confidence calibration
        int totalSignals = posSignals + negSignals + (int) Math.abs(phraseScore);
        double baseConfidence = totalSignals == 0 ? 0.5
            : Math.min(0.50 + (absScore * 0.06) + (totalSignals * 0.04), 0.97);
        // Reduce confidence if signals are mixed
        if (posSignals > 0 && negSignals > 0) {
            baseConfidence *= 0.85;
        }

        // 9. Build reasoning
        StringBuilder reason = new StringBuilder();
        reason.append(String.format("Score: %.1f | ", totalScore));
        if (!posMatched.isEmpty())  reason.append("Positive signals: ").append(String.join(", ", posMatched)).append(". ");
        if (!negMatched.isEmpty())  reason.append("Negative signals: ").append(String.join(", ", negMatched)).append(". ");
        if (!phraseMatches.isEmpty()) reason.append("Phrases detected: ").append(String.join(", ", phraseMatches)).append(". ");
        if (exclamations >= 2)     reason.append("Emphasis detected (!!!). ");
        if (emojiScore != 0)       reason.append(String.format("Emoji contribution: %.1f. ", emojiScore));
        if (posSignals > 0 && negSignals > 0) reason.append("Mixed signals detected — confidence reduced.");

        return new SentimentResult(
            sentiment,
            Math.round(baseConfidence * 100.0) / 100.0,
            emoji,
            intensity,
            posSignals,
            negSignals,
            reason.toString().trim()
        );
    }

    private int countOccurrences(String text, String target) {
        int count = 0, idx = 0;
        while ((idx = text.indexOf(target, idx)) != -1) { count++; idx++; }
        return count;
    }

    private int countCapsWords(String text) {
        int count = 0;
        for (String w : text.split("\\s+")) {
            if (w.length() > 2 && w.equals(w.toUpperCase()) && w.matches("[A-Z]+")) count++;
        }
        return count;
    }
}
