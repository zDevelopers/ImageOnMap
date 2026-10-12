/*
 * Copyright or © or Copr. Moribus (2013)
 * Copyright or © or Copr. ProkopyL <prokopylmc@gmail.com> (2015)
 * Copyright or © or Copr. Amaury Carrade <amaury@carrade.eu> (2016 – 2021)
 * Copyright or © or Copr. Vlammar <valentin.jabre@gmail.com> (2019 – 2021)
 *
 * Parts of this file are adapted from QuartzLib (https://github.com/zDevelopers/QuartzLib),
 * Copyright or © or Copr. ZDevelopers (2015 – 2021), governed by the CeCILL-B license.
 *
 * This software is a computer program whose purpose is to allow insertion of
 * custom images in a Minecraft world.
 *
 * This software is governed by the CeCILL license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 */

package fr.moribus.imageonmap.i18n;

import fr.moribus.imageonmap.tools.PluginLogger;
import java.util.Locale;
import java.util.function.LongToIntFunction;

/**
 * The plural rules of a gettext translation file.
 *
 * <p>The usual gettext plural rules are recognized; for any other rule, English rules are used.</p>
 */
public final class PluralForms {
    private static final LongToIntFunction ENGLISH_RULES = n -> n != 1 ? 1 : 0;

    private final int formsCount;
    private final String formsScript;
    private final LongToIntFunction formsFunction;

    public PluralForms(int formsCount, String formsScript) {
        this.formsCount = formsCount;
        this.formsScript = formsScript == null ? "" : formsScript.trim();
        this.formsFunction = computeFormsFunction();
    }

    /**
     * Computes the index of the plural form to use for the given count.
     *
     * @param count The count.
     * @return The index of the plural form.
     */
    public int computePluralForm(long count) {
        final int index = formsFunction.applyAsInt(count);
        if (index < 0 || index > formsCount) {
            return 0;
        }
        return index;
    }

    private LongToIntFunction computeFormsFunction() {
        if (formsScript.isEmpty()) {
            return ENGLISH_RULES;
        }

        final LongToIntFunction function = formsFunctionFromKnownScripts();
        if (function != null) {
            return function;
        }

        PluginLogger.warning("Unknown plural rule “{0}”; English pluralization rules will be used.", formsScript);
        return ENGLISH_RULES;
    }

    private String normalizeFormsScript(final String script) {
        final String normalized = script.toLowerCase(Locale.ROOT).replace(" ", "").trim();

        // POEdit tends to add unnecessary parenthesis around the plural scripts.
        if (normalized.startsWith("(") && normalized.endsWith(")")) {
            return normalized.substring(1, normalized.length() - 1);
        }
        return normalized;
    }

    @SuppressWarnings("checkstyle:LineLength")
    private LongToIntFunction formsFunctionFromKnownScripts() {
        switch (normalizeFormsScript(formsScript)) {
            // Only one form (Japanese, Vietnamese, Korean, Thai, Chinese)
            case "0":
                return n -> 0;

            // Two forms, singular used for one only
            case "n!=1":
            case "n<1||n>1":
            case "n>1||n<1":
                return n -> n != 1 ? 1 : 0;
            case "n!=0":
                return n -> n != 0 ? 1 : 0;
            case "n==0||n==1":
            case "n==1||n==0":
                return n -> n == 0 || n == 1 ? 1 : 0;
            case "n>0":
                return n -> n > 0 ? 1 : 0;

            // Two forms, singular used for zero and one (Brazilian Portuguese, French)
            case "n>1":
                return n -> n > 1 ? 1 : 0;

            // Icelandic, Macedonian
            case "n%10==1&&n%100!=11":
                return n -> n % 10 == 1 && n % 100 != 11 ? 1 : 0;
            case "n<=1||(n>=11&&n<=99)":
                return n -> n <= 1 || (n >= 11 && n <= 99) ? 1 : 0;

            // Three forms, special case for zero (Latvian)
            case "n%10==1&&n%100!=11?0:n!=0?1:2":
                return n -> n % 10 == 1 && n % 100 != 11 ? 0 : (n != 0 ? 1 : 2);

            // Three forms, special cases for one and two (Irish, Cornish, Nama, Northern Sami)
            case "n==1?0:n==2?1:2":
                return n -> n == 1 ? 0 : (n == 2 ? 1 : 2);

            // Romanian
            case "n==1?0:(n==0||(n%100>0&&n%100<20))?1:2":
                return n -> n == 1 ? 0 : ((n == 0 || (n % 100 > 0 && n % 100 < 20)) ? 1 : 2);

            // Lithuanian
            case "n%10==1&&n%100!=11?0:n%10>=2&&(n%100<10||n%100>=20)?1:2":
                return n -> n % 10 == 1 && n % 100 != 11 ? 0 : (n % 10 >= 2 && (n % 100 < 10 || n % 100 >= 20) ? 1 : 2);

            // Belarusian, Bosnian, Croatian, Russian, Serbian, Ukrainian
            case "n%10==1&&n%100!=11?0:n%10>=2&&n%10<=4&&(n%100<10||n%100>=20)?1:2":
                return n -> n % 10 == 1 && n % 100 != 11 ? 0
                        : (n % 10 >= 2 && n % 10 <= 4 && (n % 100 < 10 || n % 100 >= 20) ? 1 : 2);
            case "n%10==1&&n%100!=11?0:n%10>=2&&n%10<=4&&(n%100<12||n%100>14)?1:2":
                return n -> n % 10 == 1 && n % 100 != 11 ? 0
                        : (n % 10 >= 2 && n % 10 <= 4 && (n % 100 < 12 || n % 100 >= 14) ? 1 : 2);

            // Czech, Slovak
            case "(n==1)?0:(n>=2&&n<=4)?1:2":
            case "n==1?0:(n>=2&&n<=4)?1:2":
            case "n==1?0:n>=2&&n<=4?1:2":
                return n -> n == 1 ? 0 : ((n >= 2 && n <= 4) ? 1 : 2);

            // Polish
            case "n==1?0:n%10>=2&&n%10<=4&&(n%100<10||n%100>=20)?1:2":
                return n -> n == 1 ? 0 : (n % 10 >= 2 && n % 10 <= 4 && (n % 100 < 10 || n % 100 >= 20) ? 1 : 2);
            case "n==1?0:n%10>=2&&n%10<=4&&(n%100<12||n%100>14)?1:2":
                return n -> n == 1 ? 0 : n % 10 >= 2 && n % 10 <= 4 && (n % 100 < 12 || n % 100 > 14) ? 1 : 2;

            // Slovenian
            case "n%100==1?0:n%100==2?1:n%100==3||n%100==4?2:3":
                return n -> n % 100 == 1 ? 0 : (n % 100 == 2 ? 1 : (n % 100 == 3 || n % 100 == 4 ? 2 : 3));

            // Arabic
            case "n==0?0:n==1?1:n==2?2:n%100>=3&&n%100<=10?3:n%100>=11?4:5":
                return n -> n == 0 ? 0
                        : (n == 1 ? 1 : (n == 2 ? 2 : (n % 100 >= 3 && n % 100 <= 10 ? 3 : (n % 100 >= 11 ? 4 : 5))));

            default:
                return null;
        }
    }
}
