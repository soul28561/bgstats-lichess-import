import java.io.FileWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chariot.Client;
import chariot.model.Enums;
import chariot.model.Game;
import chariot.model.Player;
import org.json.JSONArray;
import org.json.JSONObject;

public class LichessImport {
  private static final DateTimeFormatter BG_STATS_FORMAT =
          DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private static final String USER_ID = "soul2197";
  private static final LocalDate FETCH_THROUGH = LocalDate.of(2026, 1, 12);
  private static final String OUTPUT_FILE = String.format("lichess-%s.json", FETCH_THROUGH);

  public static void main(String[] args) throws Exception {
    final Client client = Client.basic();
    final List<Game> lcGames =
      client.games().byUserId(USER_ID)
              .stream()
              .takeWhile(game -> !game.createdAt().toLocalDate().isBefore(FETCH_THROUGH))
              .toList();

    final Map<String, Integer> userIdMap = new HashMap<>();
    int nextPlayerId = 1;
    userIdMap.put(USER_ID, nextPlayerId++);

    final JSONObject exportData = new JSONObject();
    exportData.put("challenges", new JSONArray());

    final JSONObject userInfo = new JSONObject();
    userInfo.put("meRefId", userIdMap.get(USER_ID));
    exportData.put("userInfo", userInfo);

    final JSONArray games = new JSONArray();
    final JSONObject chess = new JSONObject();
    chess.put("id", 1);
    chess.put("uuid", "BG-STATS-LICHESS-IMPORT-GAME-1");
    chess.put("noPoints", false);
    chess.put("highestWins", true);
    chess.put("cooperative", false);
    chess.put("bggId", 171);
    chess.put("bggName", "Chess");
    chess.put("bggYear", 1475);
    chess.put("name", "Chess");
    games.put(chess);
    exportData.put("games", games);

    final JSONArray plays = new JSONArray();
    for (Game lcGame : lcGames) {
      final Player white = lcGame.players().white();
      Integer whiteId = userIdMap.get(white.name());
      if (whiteId == null) {
        whiteId = nextPlayerId++;
        userIdMap.put(white.name(), whiteId);
      }

      final Player black = lcGame.players().black();
      Integer blackId = userIdMap.get(black.name());
      if (blackId == null) {
        blackId = nextPlayerId++;
        userIdMap.put(black.name(), blackId);
      }

      final String playTime = lcGame.createdAt().format(BG_STATS_FORMAT);

      final JSONObject play = new JSONObject();
      play.put("uuid", String.format("LICHESS-%S", lcGame.id()));
      play.put("ignored", false);
      play.put("rating", 0);
      play.put("scoringSetting", 0);
      play.put("playDate", playTime);
      play.put("entryDate", playTime);
      play.put("modificationDate", playTime);
      play.put("bggLastSync", playTime);
      play.put("manualWinner", false);
      play.put("locationRefId", 1);
      play.put("rounds", 0);
      play.put("usesTeams", false);
      play.put("bggId", 0);
      play.put("playImages", "[]");
      play.put("durationMin", Math.round(lcGame.clock().totalTime() / 30.0));
      play.put("nemestatsId", 0);
      play.put("gameRefId", 1);

      final JSONArray playerScores = new JSONArray();

      final JSONObject p1 = new JSONObject();
      p1.put("winner", lcGame.winner() == Enums.Color.white);
      p1.put("seatOrder", 0);
      p1.put("score", lcGame.winner() == Enums.Color.white ? "1" : "0");
      p1.put("startPlayer", true);
      p1.put("playerRefId", whiteId);
      p1.put("rank", 0);
      p1.put("newPlayer", true);
      playerScores.put(p1);

      final JSONObject p2 = new JSONObject();
      p2.put("winner", lcGame.winner() == Enums.Color.black);
      p2.put("seatOrder", 0);
      p2.put("score", lcGame.winner() == Enums.Color.black ? "1" : "0");
      p2.put("startPlayer", false);
      p2.put("playerRefId", blackId);
      p2.put("rank", 0);
      p2.put("newPlayer", true);
      playerScores.put(p2);

      play.put("playerScores", playerScores);
      plays.put(play);
    }
    exportData.put("plays", plays);

    final JSONArray locations = new JSONArray();
    final JSONObject location = new JSONObject();
    location.put("id", 1);
    location.put("modificationDate", "2025-01-01 00:00:00");
    location.put("name", "Lichess");
    location.put("uuid", "BG-STATS-LICHESS-IMPORT-LOCATION-1");
    locations.put(location);
    exportData.put("locations", locations);

    final JSONArray players = new JSONArray();
    for (String name : userIdMap.keySet()) {
      final JSONObject player = new JSONObject();
      player.put("id", userIdMap.get(name));
      player.put("modificationDate", "2025-01-01 00:00:00");
      player.put("name", name);
      player.put("uuid", String.format("BG-STATS-LICHESS-IMPORT-PLAYER-%s", name));
      players.put(player);
    }
    exportData.put("players", players);

    final FileWriter writer = new FileWriter(OUTPUT_FILE);
    writer.write(exportData.toString());
    writer.close();
  }
}
