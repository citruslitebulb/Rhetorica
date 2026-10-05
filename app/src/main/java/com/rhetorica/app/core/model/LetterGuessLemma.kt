package com.rhetorica.app.core.model

/**
 * Letter-guess answers should be citation forms: "advertise", never "advertising"
 * or "advertised".
 *
 * Speech-lifted headwords are often inflected. This decides from the headword and
 * its part of speech, without rewriting the seed. A form is rejected when a
 * suffix rule can map it to a base ("advertising" → "advertise") or, for
 * superlatives such as "chiefest", when that base is already a library headword.
 * [BASE_FORMS] keeps roots that only look inflected ("bring", "need", "bless",
 * "kindly").
 *
 * The website uses the same rules in `web/src/lib/letterGuessLemma.ts`.
 */
object LetterGuessLemma {
    fun normalize(word: String): String = buildString(word.length) {
        for (char in word) {
            val lower = char.lowercaseChar()
            if (lower in 'a'..'z') append(lower)
        }
    }

    /**
     * [lexicon] is the library's normalized headwords. It is only needed for
     * superlatives, whose base has to actually be in the library ("chiefest"
     * when "chief" is present). Pass an empty set to skip that check.
     * Keys must already be [normalize]d.
     */
    fun isBaseForm(
        word: String,
        partOfSpeech: String,
        lexicon: Set<String> = emptySet(),
    ): Boolean {
        val normalized = normalize(word)
        if (normalized.isEmpty()) return false
        if (normalized in BASE_FORMS) return true
        val kind = posKind(partOfSpeech)
        if (isDerivedAdverb(normalized, kind)) return false
        if (isIngForm(normalized, kind)) return false
        if (isEdForm(normalized, kind)) return false
        if (isPluralOrThirdPerson(normalized, kind)) return false
        if (isSuperlative(normalized, kind, lexicon)) return false
        return true
    }

    /**
     * Letter-guess eligibility: difficulty length, the complexity multi-select,
     * and [isBaseForm]. Multiple choice does not use this.
     */
    fun isCandidate(
        word: String,
        partOfSpeech: String,
        complexity: String,
        minLetters: Int,
        maxLetters: Int,
        allowedComplexity: WordComplexity,
        lexicon: Set<String> = emptySet(),
    ): Boolean {
        if (!allowedComplexity.matches(complexity)) return false
        val letters = word.count { it.isLetter() }
        if (letters == 0 || letters !in minLetters..maxLetters) return false
        return isBaseForm(word, partOfSpeech, lexicon)
    }

    private enum class PosKind {
        Adverb,
        Adjective,
        Verb,
        Noun,
        Other,
    }

    private fun posKind(partOfSpeech: String): PosKind {
        val pos = partOfSpeech.lowercase()
        return when {
            "adverb" in pos -> PosKind.Adverb
            "adjective" in pos -> PosKind.Adjective
            "verb" in pos -> PosKind.Verb
            "noun" in pos -> PosKind.Noun
            else -> PosKind.Other
        }
    }

    private fun hasVowel(value: String): Boolean = value.any { it in VOWELS }

    private fun isDoubledConsonant(stem: String): Boolean {
        if (stem.length < 3) return false
        val last = stem.last()
        return last == stem[stem.length - 2] && last !in VOWELS
    }

    /** True when the stem ends in a vowel plus one consonant, as in "care" → "car". */
    private fun endsWithSingleConsonant(stem: String): Boolean {
        if (stem.length < 2 || stem.endsWith("ss")) return false
        val previous = stem[stem.length - 2]
        val last = stem.last()
        return previous in VOWELS && last !in VOWELS
    }

    private fun isDerivedAdverb(word: String, kind: PosKind): Boolean {
        return kind == PosKind.Adverb && word.endsWith("ly") && word.length >= 5
    }

    private fun isIngForm(word: String, kind: PosKind): Boolean {
        if (!word.endsWith("ing") || word.length < 5) return false
        val stem = word.dropLast(3)
        if (!hasVowel(stem)) return false
        return when (kind) {
            PosKind.Verb -> true
            PosKind.Adjective -> stem.length >= 2
            PosKind.Noun, PosKind.Other -> isGerundStem(stem)
            PosKind.Adverb -> false
        }
    }

    private fun isGerundStem(stem: String): Boolean {
        if (isDoubledConsonant(stem)) return true
        return stem.length >= 3 && endsWithSingleConsonant(stem)
    }

    private fun isEdForm(word: String, kind: PosKind): Boolean {
        if (!word.endsWith("ed")) return false
        val stem = word.dropLast(2)
        if (stem.length < 2 || !hasVowel(stem)) return false
        val plusE = stem + "e"
        if (word.endsWith("ied") || isDoubledConsonant(stem) || isVerbishBase(plusE)) return true
        if (isMonosyllabicEDrop(plusE)) return true
        if (kind == PosKind.Verb && stem.length >= 3) return true
        if (kind == PosKind.Adjective && stem.length >= 4) return true
        return false
    }

    private fun isVerbishBase(base: String): Boolean {
        return base.endsWith("ise") ||
            base.endsWith("ize") ||
            base.endsWith("yze") ||
            base.endsWith("yse") ||
            base.endsWith("ate") ||
            base.endsWith("ify") ||
            base.endsWith("ite") ||
            base.endsWith("ute")
    }

    /** "used" → "use", "tired" → "tire". The silent e is not a second vowel. */
    private fun isMonosyllabicEDrop(plusE: String): Boolean {
        if (plusE.length < 3 || !plusE.endsWith("e")) return false
        val stem = plusE.dropLast(1)
        if (stem.count { it in VOWELS } != 1) return false
        val vowel = plusE[plusE.length - 3]
        val consonant = plusE[plusE.length - 2]
        return vowel in VOWELS && consonant !in VOWELS
    }

    private fun isPluralOrThirdPerson(word: String, kind: PosKind): Boolean {
        if (kind != PosKind.Noun && kind != PosKind.Verb) return false
        if (word.endsWith("ies") && word !in IRREGULAR_IES && word.length >= 5) return true
        if (word.length >= 5 && (word.endsWith("xes") || word.endsWith("zes") || word.endsWith("ches") || word.endsWith("shes"))) {
            return true
        }
        if (word.length >= 5 && word.endsWith("ses") && !word.endsWith("sses")) return true
        if (!word.endsWith("s") || isUninflectedSEnding(word)) return false
        val singular = word.dropLast(1)
        val minSingular = if (kind == PosKind.Verb) 3 else 4
        if (singular.length < minSingular || !hasVowel(singular)) return false
        val singularLooksReal = singular.last() !in VOWELS || singular.endsWith("e")
        if (!singularLooksReal) return false
        return kind == PosKind.Noun || kind == PosKind.Verb
    }

    /** Latin and Greek citation endings, plus -ness, that are not English plurals. */
    private fun isUninflectedSEnding(word: String): Boolean {
        return word.endsWith("ss") ||
            word.endsWith("ous") ||
            word.endsWith("us") ||
            word.endsWith("is") ||
            word.endsWith("os") ||
            word.endsWith("as") ||
            word.endsWith("ics") ||
            word.endsWith("ness")
    }

    private fun isSuperlative(word: String, kind: PosKind, lexicon: Set<String>): Boolean {
        if (kind != PosKind.Adjective || lexicon.isEmpty()) return false
        if (!word.endsWith("est") || word.length < 6) return false
        val stem = word.dropLast(3)
        val candidates = ArrayList<String>(4)
        candidates += stem
        candidates += stem + "e"
        if (isDoubledConsonant(stem)) candidates += stem.dropLast(1)
        if (word.endsWith("iest") && word.length > 4) candidates += word.dropLast(4) + "y"
        return candidates.any { it.length >= 4 && it != word && it in lexicon }
    }

    private const val VOWELS = "aeiou"

    private val IRREGULAR_IES = setOf("series", "species")

    /**
     * Citation forms a suffix chop would mistake for an inflection.
     * Included even when they are not in the seed yet, so a later headword is safe.
     */
    private val BASE_FORMS = setOf(
        "bring",
        "king",
        "morning",
        "evening",
        "something",
        "nothing",
        "anything",
        "everything",
        "during",
        "ceiling",
        "offspring",
        "shortcoming",
        "need",
        "feed",
        "seed",
        "weed",
        "bleed",
        "speed",
        "greed",
        "creed",
        "breed",
        "exceed",
        "proceed",
        "succeed",
        "bless",
        "pass",
        "class",
        "glass",
        "canvas",
        "means",
        "series",
        "species",
        "kindly",
        "only",
        "early",
        "holy",
        "ugly",
        "jolly",
        "silly",
        "worldly",
        "sacred",
        "naked",
        "wicked",
        "rugged",
        "hundred",
    )
}
