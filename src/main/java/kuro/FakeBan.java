package kuro;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * FakeBan: tamamen kozmetik ve yalnizca yerel (client-side) bir "sahte kopma / sahte ban" ekrani.
 *
 * Onemli: Bu modul sunucuya HICBIR paket/istek gondermez, oyuncuyu gercekten sunucudan atmaz
 * ve gercek bir baglanti kaybi olusturmaz. Vanilla "Baglanti Kesildi" ekranini (DisconnectedScreen)
 * local olarak gosterir; gercek bir ban da zaten oyuncunun ekraninda tam olarak bu ekranla gorunur,
 * aradaki tek fark sunucunun gonderdigi metindir -- burada o metni sen yaziyorsun.
 *
 * Parent olarak null verildigi icin, ekrandaki "Sunucu listesine don" butonuna basildiginda ya da
 * ESC ile kapatildiginda oyuncu normal sekilde oyuna / dunyaya geri doner; gercekte baglanti hic
 * kesilmemistir. Ekran sadece bunu calistiran kisinin kendi istemcisinde gorunur; baska hicbir
 * oyuncu bunu gormez ve bundan etkilenmez.
 */
public final class FakeBan {
   private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
   private static final String ID_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

   private FakeBan() {
   }

   public static void show() {
      MinecraftClient mc = Kuro.mc;
      if (mc.player == null || mc.world == null) {
         return;
      }

      Text title = Text.literal(blankOr(Config.fbanTitle, "Failed to connect to the server"));
      String reason = blankOr(Config.fbanReason, "You are banned.");
      String date = blankOr(Config.fbanDate, LocalDate.now().format(DATE_FMT));
      String banId = blankOr(Config.fbanId, "#" + randomId());
      String appeal = Config.fbanAppeal == null ? "" : Config.fbanAppeal.trim();

      MutableText body = Text.literal(reason).formatted(Formatting.RED);
      body = body.append(Text.literal("\n\nDate: ").formatted(Formatting.GRAY));
      body = body.append(Text.literal(date).formatted(Formatting.WHITE));
      body = body.append(Text.literal("\n\nBan ID: ").formatted(Formatting.GRAY));
      body = body.append(Text.literal(banId).formatted(Formatting.GRAY));
      if (!appeal.isEmpty()) {
         body = body.append(Text.literal("\n" + appeal).formatted(Formatting.GRAY));
      }

      // parent = null: ekran kapatilinca (buton / ESC) gercek baglanti etkilenmeden oyuna donulur
      mc.setScreen(new DisconnectedScreen(null, title, body));
   }

   private static String blankOr(String v, String fallback) {
      return v == null || v.isBlank() ? fallback : v;
   }

   private static String randomId() {
      StringBuilder sb = new StringBuilder(8);
      for (int i = 0; i < 8; i++) {
         sb.append(ID_CHARS.charAt(ThreadLocalRandom.current().nextInt(ID_CHARS.length())));
      }

      return sb.toString();
   }
}
