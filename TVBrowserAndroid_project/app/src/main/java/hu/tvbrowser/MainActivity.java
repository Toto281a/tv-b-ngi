package hu.tvbrowser;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.view.View;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.webkit.*;
import android.widget.*;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.view.ViewGroup;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import java.util.*;
import java.net.URLEncoder;

public class MainActivity extends Activity {
    private LinearLayout root, tiles, toolbar;
    private WebView web;
    private EditText address;
    private TextView status;
    private View customView;
    private WebChromeClient.CustomViewCallback customCallback;
    private WebChromeClient activeChromeClient;
    private boolean adBlock = true, popups = true;
    private final ArrayList<String[]> bookmarks = new ArrayList<>();
    private final Set<String> blockedHosts = new HashSet<>(Arrays.asList(
        "doubleclick.net", "googlesyndication.com", "googleadservices.com",
        "adnxs.com", "adform.net", "taboola.com", "outbrain.com",
        "scorecardresearch.com", "zedo.com", "popads.net", "popcash.net",
        "adsterra.com", "exoclick.com", "propellerads.com"
    ));

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, 0);
        bookmarks.add(new String[]{"YouTube", "https://m.youtube.com"});
        bookmarks.add(new String[]{"Google", "https://www.google.com"});
        bookmarks.add(new String[]{"Index", "https://index.hu"});
        bookmarks.add(new String[]{"Telex", "https://telex.hu"});
        bookmarks.add(new String[]{"444", "https://444.hu"});
        bookmarks.add(new String[]{"RTL", "https://rtl.hu"});
        showHome();
    }

    private GradientDrawable bg(int color, int stroke) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color); d.setCornerRadius(24);
        d.setStroke(2, stroke);
        return d;
    }
    private TextView text(String s, int size, int color) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(size);
        t.setTextColor(color); t.setGravity(Gravity.CENTER_VERTICAL);
        return t;
    }
    private TextView button(String label, int color) {
        TextView t = text(label, 17, Color.WHITE);
        t.setGravity(Gravity.CENTER); t.setFocusable(true); t.setClickable(true);
        t.setPadding(18, 14, 18, 14); t.setBackground(bg(color, 0xFF3B82F6));
        return t;
    }
    private void initShell() {
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(22, 18, 22, 18); root.setBackgroundColor(0xFF071426);
        setContentView(root);
        toolbar = new LinearLayout(this); toolbar.setGravity(Gravity.CENTER_VERTICAL);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        TextView logo = text("🌐 TV Browser", 25, Color.WHITE);
        logo.setTypeface(null, Typeface.BOLD);
        toolbar.addView(logo, new LinearLayout.LayoutParams(0, 62, 1));
        address = new EditText(this); address.setSingleLine(true); address.setTextSize(17);
        address.setHint("Keresés vagy webcím…"); address.setHintTextColor(0xFFB7C8E0);
        address.setTextColor(Color.WHITE); address.setSelectAllOnFocus(true);
        address.setPadding(18, 4, 18, 4); address.setBackground(bg(0xFF1A2E48, 0xFF355579));
        toolbar.addView(address, new LinearLayout.LayoutParams(0, 56, 2));
        TextView go = button("Megnyitás", 0xFF1769E0);
        LinearLayout.LayoutParams gp = new LinearLayout.LayoutParams(-2, 56); gp.leftMargin = 10;
        toolbar.addView(go, gp); go.setOnClickListener(v -> navigate(address.getText().toString()));
        address.setOnEditorActionListener((v, action, event) -> { navigate(address.getText().toString()); return true; });
        TextView home = button("Kezdőlap", 0xFF253A56);
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(-2, 56); hp.leftMargin = 8;
        toolbar.addView(home, hp); home.setOnClickListener(v -> showHome());
        root.addView(toolbar);
    }

    private void showHome() {
        initShell();
        ScrollView scroll = new ScrollView(this);
        LinearLayout content = new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(0, 18, 0, 8); scroll.addView(content);
        TextView heading = text("Jó böngészést!  •  Gyors, színes és távirányítóbarát", 21, Color.WHITE);
        heading.setPadding(8, 8, 8, 18); content.addView(heading);
        LinearLayout features = new LinearLayout(this);
        addTile(features, "🛡  Reklámblokkoló", 0xFF5834D6, () -> toggleAds());
        addTile(features, "🚫  Felugró ablakok", 0xFF008F78, () -> togglePopups());
        addTile(features, "🎬  Videó teljes képernyő", 0xFF1169D8, () -> openVideoHelp());
        addTile(features, "🍃  Memóriakímélő", 0xFF126B68, () -> showInfo("Memóriakímélő mód", "A böngésző egy WebView-t használ, nem tart fenn háttérfolyamatokat. A videóminőséget a weboldal lejátszója határozza meg."));
        content.addView(features);
        TextView favHead = text("⭐  Kedvenc weboldalak", 23, Color.WHITE);
        favHead.setPadding(8, 22, 8, 12); content.addView(favHead);
        tiles = new LinearLayout(this); tiles.setOrientation(LinearLayout.VERTICAL);
        content.addView(tiles); renderBookmarks();
        TextView hint = text("Tipp: a csempékre a TV távirányítójával is rá tudsz lépni. A „+ Új könyvjelző” az aktuális címet menti el.", 15, 0xFFB7C8E0);
        hint.setPadding(8, 22, 8, 10); content.addView(hint);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        status = text("Reklámszűrés: " + (adBlock ? "BE" : "KI") + "    •    Felugró ablakok: " + (popups ? "TILTVA" : "ENGEDÉLYEZVE"), 14, 0xFF66E3C4);
        status.setPadding(8, 8, 8, 0); root.addView(status);
    }

    private void addTile(LinearLayout row, String label, int color, Runnable action) {
        TextView t = button(label, color);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, 74, 1);
        p.setMargins(5, 4, 5, 4); row.addView(t, p); t.setOnClickListener(v -> action.run());
    }
    private void renderBookmarks() {
        tiles.removeAllViews();
        LinearLayout row = null;
        for (int i=0; i<bookmarks.size(); i++) {
            if (i % 4 == 0) { row = new LinearLayout(this); tiles.addView(row); }
            final String[] b = bookmarks.get(i);
            TextView t = button("🌐\n" + b[0], new int[]{0xFF145ED4,0xFF7C32D6,0xFF087F8C,0xFFBD4A23}[i%4]);
            t.setGravity(Gravity.CENTER); t.setTextSize(18);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, 94, 1);
            p.setMargins(5, 5, 5, 5); row.addView(t, p);
            t.setOnClickListener(v -> navigate(b[1]));
            t.setOnLongClickListener(v -> { new android.app.AlertDialog.Builder(this).setTitle(b[0]).setItems(new String[]{"Megnyitás", "Eltávolítás"}, (d, which) -> {
                if (which == 0) navigate(b[1]); else { bookmarks.remove(b); renderBookmarks(); }
            }).show(); return true; });
        }
        if (row == null || bookmarks.size()%4 != 0) { /* next tile gets its own row for clarity */ }
        if (row == null || bookmarks.size()%4 != 0) row = new LinearLayout(this);
        TextView add = button("＋  Új könyvjelző", 0xFF253A56);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, 78, 1);
        p.setMargins(5,5,5,5); row.addView(add,p); tiles.addView(row);
        add.setOnClickListener(v -> {
            if (web == null) { showInfo("Új könyvjelző", "Először nyiss meg egy weboldalt."); return; }
            String url = web.getUrl();
            EditText name = new EditText(this); name.setHint("A könyvjelző neve");
            new android.app.AlertDialog.Builder(this).setTitle("Könyvjelző mentése").setView(name)
              .setPositiveButton("Mentés", (d,w) -> { String n=name.getText().toString().trim(); bookmarks.add(new String[]{n.isEmpty()?url:n,url}); showHome(); })
              .setNegativeButton("Mégse",null).show();
        });
    }

    private void toggleAds() { adBlock = !adBlock; showHome(); }
    private void togglePopups() { popups = !popups; showHome(); }
    private void openVideoHelp() {
        showInfo("Videók teljes képernyőn", "A támogatott HTML5-lejátszók teljes képernyős gombja működik. Ha egy oldal nem ajánlja fel, használd a lejátszó saját teljes képernyős ikonját. A maximális 720p/1080p minőséget a weboldal lejátszójában állítsd be.");
    }
    private void showInfo(String title, String message) {
        new android.app.AlertDialog.Builder(this).setTitle(title).setMessage(message).setPositiveButton("Rendben", null).show();
    }
    private String normalize(String raw) {
        String s = raw == null ? "" : raw.trim();
        if (s.isEmpty()) return "https://www.google.com";
        if (s.matches("(?i)^https?://.*")) return s;
        if (s.contains(" ") || !s.contains(".")) {
            try { return "https://www.google.com/search?q=" + URLEncoder.encode(s, "UTF-8"); }
            catch(Exception e) { return "https://www.google.com"; }
        }
        return "https://" + s;
    }
    private boolean blocked(String url) {
        if (!adBlock || url == null) return false;
        try {
            String host = Uri.parse(url).getHost();
            if (host == null) return false;
            host = host.toLowerCase(Locale.US);
            for (String b : blockedHosts) if (host.equals(b) || host.endsWith("." + b)) return true;
        } catch(Exception ignored) {}
        return false;
    }
    private void navigate(String raw) {
        String url = normalize(raw);
        if (web == null) {
            initShell();
            web = new WebView(this);
            web.setBackgroundColor(Color.WHITE);
            WebSettings s = web.getSettings();
            s.setJavaScriptEnabled(true);
            s.setDomStorageEnabled(true);
            s.setDatabaseEnabled(false);
            s.setLoadsImagesAutomatically(true);
            s.setBlockNetworkImage(false);
            s.setSupportMultipleWindows(false);
            s.setJavaScriptCanOpenWindowsAutomatically(false);
            s.setMediaPlaybackRequiresUserGesture(true);
            s.setCacheMode(WebSettings.LOAD_DEFAULT);
            s.setDefaultTextEncodingName("UTF-8");
            web.setOverScrollMode(View.OVER_SCROLL_NEVER);
            web.setWebViewClient(new WebViewClient() {
                @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
                    Uri u=req.getUrl();
                    if ("http".equals(u.getScheme()) || "https".equals(u.getScheme())) return false;
                    return true;
                }
                @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest req) {
                    if (blocked(req.getUrl().toString()))
                        return new WebResourceResponse("text/plain", "utf-8", new java.io.ByteArrayInputStream(new byte[0]));
                    return super.shouldInterceptRequest(view, req);
                }
                @Override public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    if (address != null) address.setText(url);
                }
                @Override public void onPageFinished(WebView view, String url) {
                    if (address != null) address.setText(url);
                    // Best-effort video prompt; sites control their own player and resolution options.
                    view.evaluateJavascript("(function(){if(window.__tvbVideoPrompt)return;window.__tvbVideoPrompt=1;var v=document.querySelector('video');if(v&&!document.getElementById('__tvb_full')){var b=document.createElement('button');b.id='__tvb_full';b.textContent='⛶ Teljes képernyős videó';b.style='position:fixed;z-index:2147483647;top:12px;right:12px;padding:12px 16px;background:#1269e8;color:white;border:0;border-radius:12px;font-size:16px;box-shadow:0 2px 10px #0008';b.onclick=function(){try{(v.requestFullscreen||v.webkitRequestFullscreen||v.webkitEnterFullscreen).call(v)}catch(e){};b.remove()};document.body.appendChild(b);setTimeout(function(){if(b.parentNode)b.remove()},12000)}})();", null);
                }
            });
            activeChromeClient = new WebChromeClient() {
                @Override public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, android.os.Message resultMsg) {
                    return popups ? false : super.onCreateWindow(view,isDialog,isUserGesture,resultMsg);
                }
                @Override public void onShowCustomView(View view, CustomViewCallback callback) {
                    if (customView != null) { callback.onCustomViewHidden(); return; }
                    customView=view; customCallback=callback;
                    root.addView(view, new LinearLayout.LayoutParams(-1,-1));
                    toolbar.setVisibility(View.GONE); web.setVisibility(View.GONE);
                    getWindow().getDecorView().setSystemUiVisibility(5894 | 1024 | 512);
                }
                @Override public void onHideCustomView() {
                    if (customView != null) {
                        root.removeView(customView); customView=null;
                        toolbar.setVisibility(View.VISIBLE); web.setVisibility(View.VISIBLE);
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                        if (customCallback != null) customCallback.onCustomViewHidden();
                    }
                }
            };
            web.setWebChromeClient(activeChromeClient);
            root.addView(web, new LinearLayout.LayoutParams(-1,0,1));
            status = text("Weboldal betöltése…", 13, 0xFF66E3C4); root.addView(status);
        }
        web.loadUrl(url);
    }
    @Override public void onBackPressed() {
        if (customView != null) { if (activeChromeClient != null) activeChromeClient.onHideCustomView(); return; }
        if (web != null && web.canGoBack()) web.goBack(); else showHome();
    }
    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) { showHome(); return true; }
        return super.onKeyDown(keyCode,event);
    }
}
