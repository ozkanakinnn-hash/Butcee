package com.blokbahcesi.app;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.google.android.gms.games.AnnotatedData;
import com.google.android.gms.games.GamesSignInClient;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.PlayGames;
import com.google.android.gms.games.PlayGamesSdk;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardScoreBuffer;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;

/**
 * Google Play Games Services v2 köprüsü: platform kimliğiyle giriş + "Bahçe Ligi" sıralaması.
 * Kimlikler derleme sırasında PGS_APP_ID / PGS_LEADERBOARD_ID ortam değişkenlerinden gelir;
 * boşsa eklenti "yapılandırılmamış" döner ve hiçbir Play Games çağrısı yapılmaz.
 */
@CapacitorPlugin(name = "PlayGames")
public class PlayGamesPlugin extends Plugin {

    private boolean initialized = false;

    private String res(String name) {
        int id = getContext().getResources().getIdentifier(name, "string", getContext().getPackageName());
        return id == 0 ? "" : getContext().getString(id);
    }

    private boolean configured() {
        return res("game_services_project_id").matches("\\d{6,}");
    }

    private String leaderboardId() {
        return res("leaderboard_garden");
    }

    private boolean ensureInit() {
        if (!configured()) return false;
        if (!initialized) {
            try {
                PlayGamesSdk.initialize(getContext());
                initialized = true;
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    @PluginMethod
    public void status(PluginCall call) {
        JSObject r = new JSObject();
        r.put("configured", configured());
        r.put("leaderboard", !leaderboardId().isEmpty());
        call.resolve(r);
    }

    @PluginMethod
    public void signIn(PluginCall call) {
        if (!ensureInit()) { call.reject("not_configured"); return; }
        GamesSignInClient client = PlayGames.getGamesSignInClient(getActivity());
        client.isAuthenticated().addOnCompleteListener(t -> {
            boolean ok = t.isSuccessful() && t.getResult() != null && t.getResult().isAuthenticated();
            if (ok) { fetchPlayer(call); return; }
            client.signIn().addOnCompleteListener(t2 -> {
                if (t2.isSuccessful() && t2.getResult() != null && t2.getResult().isAuthenticated()) fetchPlayer(call);
                else call.reject("sign_in_failed");
            });
        });
    }

    private void fetchPlayer(PluginCall call) {
        PlayGames.getPlayersClient(getActivity()).getCurrentPlayer()
            .addOnSuccessListener(p -> {
                JSObject o = new JSObject();
                o.put("id", p.getPlayerId());
                o.put("name", p.getDisplayName());
                call.resolve(o);
            })
            .addOnFailureListener(e -> call.reject("player_failed", e));
    }

    @PluginMethod
    public void submitScore(PluginCall call) {
        if (!ensureInit() || leaderboardId().isEmpty()) { call.reject("not_configured"); return; }
        Long score = call.getLong("score", 0L);
        String tag = call.getString("tag", "");
        PlayGames.getLeaderboardsClient(getActivity())
            .submitScoreImmediate(leaderboardId(), score == null ? 0L : score, tag)
            .addOnSuccessListener(d -> call.resolve())
            .addOnFailureListener(e -> call.reject("submit_failed", e));
    }

    @PluginMethod
    public void loadTop(PluginCall call) {
        if (!ensureInit() || leaderboardId().isEmpty()) { call.reject("not_configured"); return; }
        int max = Math.max(1, Math.min(25, call.getInt("max", 25)));
        PlayGames.getLeaderboardsClient(getActivity())
            .loadTopScores(leaderboardId(), LeaderboardVariant.TIME_SPAN_ALL_TIME, LeaderboardVariant.COLLECTION_PUBLIC, max)
            .addOnSuccessListener((AnnotatedData<LeaderboardsClient.LeaderboardScores> data) -> {
                JSArray arr = new JSArray();
                LeaderboardsClient.LeaderboardScores scores = data.get();
                if (scores != null) {
                    LeaderboardScoreBuffer buf = scores.getScores();
                    try {
                        for (LeaderboardScore s : buf) {
                            JSObject o = new JSObject();
                            o.put("rank", s.getRank());
                            o.put("name", s.getScoreHolderDisplayName());
                            o.put("score", s.getRawScore());
                            o.put("tag", s.getScoreTag() == null ? "" : s.getScoreTag());
                            o.put("playerId", s.getScoreHolder() == null ? "" : s.getScoreHolder().getPlayerId());
                            arr.put(o);
                        }
                    } finally {
                        buf.release();
                        scores.release();
                    }
                }
                JSObject r = new JSObject();
                r.put("entries", arr);
                call.resolve(r);
            })
            .addOnFailureListener(e -> call.reject("load_failed", e));
    }

    @PluginMethod
    public void loadMine(PluginCall call) {
        if (!ensureInit() || leaderboardId().isEmpty()) { call.reject("not_configured"); return; }
        PlayGames.getLeaderboardsClient(getActivity())
            .loadCurrentPlayerLeaderboardScore(leaderboardId(), LeaderboardVariant.TIME_SPAN_ALL_TIME, LeaderboardVariant.COLLECTION_PUBLIC)
            .addOnSuccessListener((AnnotatedData<LeaderboardScore> data) -> {
                JSObject r = new JSObject();
                LeaderboardScore s = data.get();
                r.put("rank", s == null ? -1 : s.getRank());
                r.put("score", s == null ? 0 : s.getRawScore());
                call.resolve(r);
            })
            .addOnFailureListener(e -> call.reject("load_failed", e));
    }
}
