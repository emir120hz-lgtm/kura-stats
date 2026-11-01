package kuro;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

/**
 * FakeScoreboard: sunucunun gercek scoreboard'unun (sidebar) yazilarini yalnizca yerel olarak degistirir.
 * Kendi panelini cizmez; sunucudan gelen satirlar ve baslik cizilmeden once "bul -> yerine" kurallarindan
 * gecirilir. Sunucuya hicbir sey gonderilmez, baskalari gormez.
 *
 * Kural formati (Config.fsbRules): "bul→yerine¦bul2→yerine2"
 */
public final class ScoreboardEdit {
   public static final String RULE_SEP = "¦";
   public static final String PAIR_SEP = "→";

   /** Yalnizca render thread'inde, sidebar cizilirken true. */
   public static boolean inSidebar;

   private static String cacheSrc = null;
   private static String[][] cache = new String[0][];

   private ScoreboardEdit() {
   }

   public static boolean active() {
      return inSidebar && Kuro.on[Kuro.FAKESCOREBOARD];
   }

   public static String[][] rules() {
      String src = Config.fsbRules == null ? "" : Config.fsbRules;
      if (!src.equals(cacheSrc)) {
         List<String[]> out = new ArrayList<>();
         for (String r : src.split(RULE_SEP)) {
            int i = r.indexOf(PAIR_SEP);
            if (i <= 0) {
               continue;
            }

            out.add(new String[]{r.substring(0, i), r.substring(i + PAIR_SEP.length())});
         }

         cache = out.toArray(new String[0][]);
         cacheSrc = src;
      }

      return cache;
   }

   private static String applyRules(String s, String[][] rules) {
      for (String[] r : rules) {
         s = s.replace(r[0], r[1]);
      }

      return s;
   }

   /** Satiri kurallardan gecirir; stil (renk) mumkun oldugunca korunur. */
   public static MutableText apply(Text t) {
      MutableText orig = t.copy();
      String[][] rules = rules();
      if (rules.length == 0) {
         return orig;
      }

      String flat = t.getString();
      String want = applyRules(flat, rules);
      if (want.equals(flat)) {
         return orig;
      }

      // once parca parca dene (renkler korunur)
      MutableText rebuilt = Text.empty();
      StringBuilder got = new StringBuilder();
      Style[] first = {null};
      t.visit((style, s) -> {
         if (first[0] == null) {
            first[0] = style;
         }

         String r = applyRules(s, rules);
         got.append(r);
         if (!r.isEmpty()) {
            rebuilt.append(Text.literal(r).setStyle(style));
         }

         return java.util.Optional.empty();
      }, Style.EMPTY);

      if (got.toString().equals(want)) {
         return rebuilt;
      }

      // kural parcalar arasina denk geldiyse: duz metin, ilk parcanin stiliyle
      return Text.literal(want).setStyle(first[0] == null ? Style.EMPTY : first[0]);
   }
}
