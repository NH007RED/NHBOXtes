package com.nhbox.intromaker;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main Studio Activity for NH BOX Intro Maker Pro v2.0 (24 Ultra HD Animated Text Intro Templates).
 * - 4 Original Cinema Intros (Netflix Spectrum + Letter Draw, Prime Video, Disney+ Hotstar, Marvel Studios)
 * - 20 NEW Next-Level Per-Character Animated Text Intros (Cyber Glitch, 24K Gold 3D, Fire Forge, Thunder Strike,
 *   Quantum Vortex, Liquid Chrome, Supernova, Matrix Code, Glass Prism, 80s Synthwave, Golden Pen, Sci-Fi HUD,
 *   Aurora Magic, Titanium Slam, Neon Pulse, Prism Silk, Katana Slash, Sub-Zero Ice, Bass Equalizer, Premiere Stage)
 * - Live Customizer for EVERY text element in the intro (Main Brand Title + Subtitle Tagline + TMDB API Key)
 * - Hardware YUV420 H.264 + AAC Audio MP4 Video Exporter + In-App Video Player
 */
public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private LinearLayout llTemplateChips;
    private Spinner spTemplates, spResolution, spFps;
    private EditText etText1, etText2, etText3, etText4, etText5;
    private EditText etBrandText, etTaglineText, etTmdbKey;
    private Button btnImportHtml, btnApplyBrand, btnExportMp4, btnPlayExported, btnShareVideo;
    private Button btnPreviewPlay, btnPreviewRestart;
    private ProgressBar progressBar;
    private TextView tvStatus, tvActiveTemplateTitle;

    private Button[] chipButtons;
    private int selectedTemplateIndex = 0;

    private String currentHtmlContent = "";
    private Mp4VideoEncoder activeEncoder = null;
    private File lastExportedMp4 = null;
    private byte[] pendingWavAudioBytes = null;
    private int currentFrameIdx = 0;
    private int exportFps = 30;
    private boolean isExporting = false;
    private long exportStartTimeMs = 0;

    private final ExecutorService encodeExecutor = Executors.newSingleThreadExecutor();

    private final String[] CHIP_LABELS = new String[]{
            "🔢 1. Code Grid (Video Exact)",
            "🟢 2. Emerald Cipher (5-Text)",
            "🔴 3. Red Ops Grid (5-Text)",
            "🟡 4. Gold Vault Grid (5-Text)",
            "🔵 5. Cyan Mainframe (5-Text)",
            "🎬 6. Netflix Letter Draw",
            "💠 7. Prime Video Smile",
            "✨ 8. Disney+ Hotstar",
            "🦸 9. Marvel Poster Flip",
            "⚡ 10. Cyber Glitch",
            "👑 11. 24K Gold 3D",
            "🔥 12. Fire Forge",
            "🌩️ 13. Thunder Strike",
            "🌪️ 14. Quantum Vortex",
            "💿 15. Liquid Chrome",
            "🌌 16. Supernova",
            "💻 17. Matrix Code",
            "💎 18. Glass Prism",
            "🌆 19. 80s Synthwave",
            "✒️ 20. Golden Pen",
            "🎯 21. Sci-Fi HUD",
            "🧪 22. Aurora Magic",
            "⚙️ 23. Titanium Slam",
            "💓 24. Neon Pulse",
            "🌈 25. Prism Silk",
            "⚔️ 26. Katana Slash",
            "❄️ 27. Sub-Zero Ice",
            "🎵 28. Bass Equalizer",
            "🎭 29. Premiere Stage"
    };

    private final String[] TEMPLATE_NAMES = new String[]{
            "1. Cinema Code-Grid Decoder (Exact Video • 5 Texts)",
            "2. Classified Emerald Terminal Cipher Grid (5 Texts)",
            "3. Tactical Crimson Black-Ops Data Matrix (5 Texts)",
            "4. Wall Street Golden Quantum Ledger Grid (5 Texts)",
            "5. Deep-Space Cyan Mainframe Crosshair Grid (5 Texts)",
            "6. Netflix Spectrum + Letter-by-Letter Draw",
            "7. Amazon Prime Video 3D + Smile Swoosh",
            "8. Disney+ Hotstar Shooting Star Arch",
            "9. Marvel Studios TMDB Poster Flipbook",
            "10. Cyberpunk Neon Laser & RGB Glitch Slice",
            "11. 24K Royal Gold 3D Extrusion & Diamond Dust",
            "12. Molten Magma Ember & Fire Shockwave Forge",
            "13. High-Voltage Plasma Lightning Arc Strike",
            "14. Quantum 1200-Particle Swarm Letter Assembly",
            "15. Liquid Mercury Chrome Ripple & Specular Morph",
            "16. Cosmic Nebula Supernova & Starlight Ribbon",
            "17. Matrix Cyber Rain & Hexadecimal Decoder Lock",
            "18. 3D Crystal Shard Implosion & Refraction",
            "19. 80s Outrun Neon Horizon Grid & Sunset Chrome",
            "20. Calligraphy Golden Fountain Pen Signature Draw",
            "21. Futuristic Iron-HUD Biometric Laser Scan",
            "22. Mystic Emerald Aurora & Floating Runes Write",
            "23. Heavy Industrial Titanium Piston 3D Drop Slam",
            "24. Electric ECG Laser Heartbeat Wave Morph",
            "25. 3D Iridescent Silk Ribbon Unfold & Shimmer",
            "26. High-Speed Katana Blade Flash & Split Reveal",
            "27. Sub-Zero Frost Crystal Growth & Ice Shatter",
            "28. 64-Band Neon Audio Spectrum Equalizer Rise",
            "29. Grand Cinema Searchlights & 3D Gold Monolith"
    };

    private final String[] TEMPLATE_FILES = new String[]{
            "intros/tpl25_john_woo_code_grid_classic.html",
            "intros/tpl26_cyber_emerald_cipher_5text.html",
            "intros/tpl27_crimson_red_ops_5text.html",
            "intros/tpl28_golden_vault_ledger_5text.html",
            "intros/tpl29_neon_cyan_mainframe_5text.html",
            "intros/netflix_nh_box_realistic_tmdb.html",
            "intros/amazon_prime_nh_box_realistic_tmdb.html",
            "intros/disney_plus_hotstar_nh_box_realistic_tmdb.html",
            "intros/marvel_nh_box_realistic_tmdb.html",
            "intros/tpl05_cyber_neon_glitch.html",
            "intros/tpl06_gold_luxury_3d.html",
            "intros/tpl07_fire_magma_forge.html",
            "intros/tpl08_electric_lightning.html",
            "intros/tpl09_quantum_particle_vortex.html",
            "intros/tpl10_liquid_chrome_metal.html",
            "intros/tpl11_galaxy_supernova.html",
            "intros/tpl12_matrix_digital_rain.html",
            "intros/tpl13_shatter_glass_prism.html",
            "intros/tpl14_synthwave_retro_grid.html",
            "intros/tpl15_golden_fountain_pen.html",
            "intros/tpl16_hologram_hud_scan.html",
            "intros/tpl17_emerald_aurora_magic.html",
            "intros/tpl18_titanium_mech_slam.html",
            "intros/tpl19_neon_heartbeat_pulse.html",
            "intros/tpl20_prism_rainbow_ribbon.html",
            "intros/tpl21_samurai_katana_slash.html",
            "intros/tpl22_ice_crystal_freeze.html",
            "intros/tpl23_soundwave_eq_drop.html",
            "intros/tpl24_royal_velvet_spotlight.html"
    };

    private final ActivityResultLauncher<String> htmlPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    loadExternalHtmlUri(uri);
                }
            });

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        llTemplateChips = findViewById(R.id.llTemplateChips);
        spTemplates = findViewById(R.id.spTemplates);
        spResolution = findViewById(R.id.spResolution);
        spFps = findViewById(R.id.spFps);
        etText1 = findViewById(R.id.etText1);
        etText2 = findViewById(R.id.etText2);
        etText3 = findViewById(R.id.etText3);
        etText4 = findViewById(R.id.etText4);
        etText5 = findViewById(R.id.etText5);
        etBrandText = findViewById(R.id.etBrandText);
        etTaglineText = findViewById(R.id.etTaglineText);
        etTmdbKey = findViewById(R.id.etTmdbKey);
        btnImportHtml = findViewById(R.id.btnImportHtml);
        btnApplyBrand = findViewById(R.id.btnApplyBrand);
        btnExportMp4 = findViewById(R.id.btnExportMp4);
        btnPlayExported = findViewById(R.id.btnPlayExported);
        btnShareVideo = findViewById(R.id.btnShareVideo);
        btnPreviewPlay = findViewById(R.id.btnPreviewPlay);
        btnPreviewRestart = findViewById(R.id.btnPreviewRestart);
        progressBar = findViewById(R.id.progressBar);
        tvStatus = findViewById(R.id.tvStatus);
        tvActiveTemplateTitle = findViewById(R.id.tvActiveTemplateTitle);

        buildDynamicTemplateChips();
        setupSpinners();
        setupWebView();
        setupButtons();

        Intent intent = getIntent();
        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null) {
            loadExternalHtmlUri(intent.getData());
        } else {
            selectTemplate(0, true);
        }
    }

    private void buildDynamicTemplateChips() {
        llTemplateChips.removeAllViews();
        chipButtons = new Button[CHIP_LABELS.length];
        int padH = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, getResources().getDisplayMetrics());
        int heightPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 38, getResources().getDisplayMetrics());
        int marginEnd = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());

        for (int i = 0; i < CHIP_LABELS.length; i++) {
            final int idx = i;
            Button b = new Button(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, heightPx);
            lp.setMarginEnd(marginEnd);
            b.setLayoutParams(lp);
            b.setPadding(padH, 0, padH, 0);
            b.setText(CHIP_LABELS[i]);
            b.setAllCaps(false);
            b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            b.setOnClickListener(v -> selectTemplate(idx, true));
            chipButtons[i] = b;
            llTemplateChips.addView(b);
        }
    }

    private void setupSpinners() {
        ArrayAdapter<String> tplAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, TEMPLATE_NAMES);
        spTemplates.setAdapter(tplAdapter);
        spTemplates.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isExporting && position != selectedTemplateIndex) {
                    selectTemplate(position, false);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        String[] resolutions = new String[]{
                "Full HD 1080p (1920x1080 • Fast & Crisp)",
                "HD 720p (1280x720 • Ultra-Fast Export)",
                "4K Ultra HD (3840x2160 • Master Quality)"
        };
        spResolution.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, resolutions));

        String[] fpsOptions = new String[]{"30 FPS (Fast)", "60 FPS (Smooth)"};
        spFps.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, fpsOptions));
    }

    private void selectTemplate(int index, boolean updateSpinner) {
        if (isExporting) return;
        selectedTemplateIndex = index;
        for (int i = 0; i < chipButtons.length; i++) {
            chipButtons[i].setBackgroundResource(i == index ? R.drawable.bg_chip_active : R.drawable.bg_chip_inactive);
            chipButtons[i].setTextColor(i == index ? 0xFFFFFFFF : 0xFFCBD5E1);
        }
        if (updateSpinner && spTemplates.getSelectedItemPosition() != index) {
            spTemplates.setSelection(index);
        }
        tvActiveTemplateTitle.setText("🔴 LIVE STAGE • " + TEMPLATE_NAMES[index]);
        loadAssetTemplate(index);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setMediaPlaybackRequiresUserGesture(false);
        ws.setAllowFileAccess(true);
        ws.setAllowContentAccess(true);
        ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String urlStr = request.getUrl().toString();
                if (urlStr.contains("image.tmdb.org/t/p/")) {
                    try {
                        URL url = new URL(urlStr);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setConnectTimeout(8000);
                        conn.setReadTimeout(8000);
                        conn.setRequestProperty("User-Agent", "NHBOX-Studio/2.0");
                        conn.connect();
                        String mime = conn.getContentType();
                        if (mime == null) mime = "image/jpeg";
                        InputStream is = conn.getInputStream();
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Access-Control-Allow-Origin", "*");
                        headers.put("Access-Control-Allow-Methods", "GET, OPTIONS");
                        headers.put("Cache-Control", "max-age=86400");
                        return new WebResourceResponse(mime, "binary", 200, "OK", headers, is);
                    } catch (Exception e) {
                        return super.shouldInterceptRequest(view, request);
                    }
                }
                return super.shouldInterceptRequest(view, request);
            }
        });
        webView.addJavascriptInterface(new AndroidBridge(), "Android");
    }

    private void setupButtons() {
        btnImportHtml.setOnClickListener(v -> {
            if (!isExporting) {
                htmlPickerLauncher.launch("text/html");
            }
        });

        btnPreviewPlay.setOnClickListener(v -> {
            if (!isExporting) {
                webView.evaluateJavascript("(function(){ var b=document.getElementById('pv'); if(b) b.click(); })();", null);
            }
        });

        btnPreviewRestart.setOnClickListener(v -> {
            if (!isExporting) {
                webView.evaluateJavascript("(function(){ if(window.stopPlay) window.stopPlay(); if(window.playAt) window.playAt(0); })();", null);
            }
        });

        btnApplyBrand.setOnClickListener(v -> {
            if (!isExporting && !currentHtmlContent.isEmpty()) {
                renderCustomizedHtmlIntoWebView();
                Toast.makeText(this, "All Intro Texts & TMDB Key Updated!", Toast.LENGTH_SHORT).show();
            }
        });

        btnExportMp4.setOnClickListener(v -> {
            if (!isExporting) {
                startUltraHdExport();
            }
        });

        btnPlayExported.setOnClickListener(v -> {
            if (lastExportedMp4 != null && lastExportedMp4.exists()) {
                showBuiltInVideoPlayer(lastExportedMp4);
            }
        });

        btnShareVideo.setOnClickListener(v -> {
            if (lastExportedMp4 != null && lastExportedMp4.exists()) {
                shareExportedVideo(lastExportedMp4);
            }
        });
    }

    private void loadAssetTemplate(int index) {
        try (InputStream is = getAssets().open(TEMPLATE_FILES[index])) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int r;
            while ((r = is.read(buf)) != -1) {
                baos.write(buf, 0, r);
            }
            currentHtmlContent = baos.toString(StandardCharsets.UTF_8.name());
            renderCustomizedHtmlIntoWebView();
            tvStatus.setText("Loaded: " + TEMPLATE_NAMES[index]);
        } catch (Exception e) {
            tvStatus.setText("Error loading asset: " + e.getMessage());
        }
    }

    private void loadExternalHtmlUri(Uri uri) {
        try (InputStream is = getContentResolver().openInputStream(uri)) {
            if (is == null) return;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int r;
            while ((r = is.read(buf)) != -1) {
                baos.write(buf, 0, r);
            }
            currentHtmlContent = baos.toString(StandardCharsets.UTF_8.name());
            renderCustomizedHtmlIntoWebView();
            String name = getFileName(uri);
            tvActiveTemplateTitle.setText("🔴 CUSTOM HTML • " + name);
            tvStatus.setText("Imported custom HTML: " + name);
        } catch (Exception e) {
            tvStatus.setText("Failed to read HTML file: " + e.getMessage());
        }
    }

    /**
     * Converts a plain string like "AN NHBOX ORIGINAL" into the wide-spaced cinema format
     * "A N   N H B O X   O R I G I N A L" used in the canvas subtitle lockups.
     */
    private static String toWideSpacedTagline(String input) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == ' ') {
                sb.append("   ");
            } else {
                sb.append(c);
                if (i < input.length() - 1 && input.charAt(i + 1) != ' ') {
                    sb.append(' ');
                }
            }
        }
        return sb.toString();
    }

    /**
     * Dynamically replaces EVERY text element inside the HTML intro:
     * - Main Brand Text (`"NH BOX"`, `"NHBOX"`, `BRAND_TEXT`)
     * - Subtitle / Tagline Text (`"A N   N H B O X   O R I G I N A L"`, `"ORIGINAL"`, `"HOTSTAR SPECIALS"`, `"STUDIOS"`, `TAGLINE_TEXT`)
     * - TMDB API Key
     */
    private void renderCustomizedHtmlIntoWebView() {
        String brand = etBrandText.getText().toString().trim();
        if (brand.isEmpty()) brand = "NH BOX";

        String rawTagline = etTaglineText.getText().toString().trim();
        if (rawTagline.isEmpty()) rawTagline = "AN " + brand + " ORIGINAL";
        String spacedTagline = toWideSpacedTagline(rawTagline);

        String tmdbKey = etTmdbKey.getText().toString().trim();
        if (tmdbKey.isEmpty()) tmdbKey = "2e211dfda888f7cc55ce433d743f9bc3";

        String t1 = etText1.getText().toString().trim();
        if (t1.isEmpty()) t1 = "A NH BOX ENTERTAINMENT COMPANY/";
        String t2 = etText2.getText().toString().trim();
        if (t2.isEmpty()) t2 = "LION ROCK";
        String t3 = etText3.getText().toString().trim();
        if (t3.isEmpty()) t3 = "PRODUCTION";
        String t4 = etText4.getText().toString().trim();
        if (t4.isEmpty()) t4 = "IN ASSOCIATION WITH";
        String t5 = etText5.getText().toString().trim();
        if (t5.isEmpty()) t5 = "A NH BOX FILM";

        String quote = String.valueOf((char) 34);
        String compactBrand = brand.replace(" ", "");

        String modified = currentHtmlContent
                .replace("2e211dfda888f7cc55ce433d743f9bc3", tmdbKey)
                .replace(quote + "A NH BOX ENTERTAINMENT COMPANY/" + quote, quote + t1 + quote)
                .replace(quote + "LION ROCK" + quote, quote + t2 + quote)
                .replace(quote + "PRODUCTION" + quote, quote + t3 + quote)
                .replace(quote + "IN ASSOCIATION WITH" + quote, quote + t4 + quote)
                .replace(quote + "A NH BOX FILM" + quote, quote + t5 + quote)
                .replace("A N   N H B O X   O R I G I N A L", spacedTagline)
                .replace("H O T S T A R   S P E C I A L S", spacedTagline)
                .replace(quote + "HOTSTAR SPECIALS" + quote, quote + rawTagline + quote)
                .replace(quote + "ORIGINAL" + quote, quote + rawTagline + quote)
                .replace(quote + "STUDIOS" + quote, quote + rawTagline + quote)
                .replace(quote + "NH BOX" + quote, quote + brand + quote)
                .replace(quote + "NHBOX" + quote, quote + compactBrand + quote)
                .replace("NH BOX Originals", brand + " Originals");

        String stageCss = "<style>"
                + "html,body{margin:0!important;padding:0!important;background:#000!important;overflow:hidden!important;height:100%!important;display:flex!important;align-items:center!important;justify-content:center!important;}"
                + ".header,.controls-bar,.status-bar,.ctl,h1,p,#st{display:none!important;}"
                + ".wrap,.w,.canvas-holder{width:100%!important;max-width:100%!important;margin:0!important;padding:0!important;border:none!important;border-radius:0!important;box-shadow:none!important;}"
                + "canvas#cv{width:100vw!important;height:100vh!important;object-fit:contain!important;border:none!important;border-radius:0!important;}"
                + "</style>";

        modified = modified.replace("</head>", stageCss + "</head>");

        webView.loadDataWithBaseURL("https://api.themoviedb.org/", modified, "text/html", "UTF-8", null);
    }

    private void startUltraHdExport() {
        int resPos = spResolution.getSelectedItemPosition();
        int outW = 1920, outH = 1080;
        if (resPos == 1) {
            outW = 1280;
            outH = 720;
        } else if (resPos == 2) {
            outW = 3840;
            outH = 2160;
        }

        exportFps = (spFps.getSelectedItemPosition() == 1) ? 60 : 30;

        try {
            File exportDir = new File(getExternalFilesDir(Environment.DIRECTORY_MOVIES), "NHBOX_Intros");
            if (!exportDir.exists()) exportDir.mkdirs();

            String fileName = "NHBOX_Intro_" + (selectedTemplateIndex + 1) + "_" + outW + "x" + outH + "_" + exportFps + "fps_" + System.currentTimeMillis() + ".mp4";
            lastExportedMp4 = new File(exportDir, fileName);

            activeEncoder = new Mp4VideoEncoder(lastExportedMp4, outW, outH, exportFps);
            currentFrameIdx = 0;
            pendingWavAudioBytes = null;
            isExporting = true;
            exportStartTimeMs = System.currentTimeMillis();

            btnExportMp4.setEnabled(false);
            btnPlayExported.setEnabled(false);
            btnShareVideo.setEnabled(false);
            progressBar.setProgress(0);
            tvStatus.setText("Synthesizing studio audio track & preparing frames...");

            String jsExportInit = "(function(){"
                    + "if(window.stopPlay) window.stopPlay();"
                    + "var dur = (typeof TOTAL !== 'undefined' && TOTAL > 0) ? TOTAL : 8.5;"
                    + "window.__expFps = " + exportFps + ";"
                    + "window.__expTotal = Math.ceil(dur * window.__expFps);"
                    + "window.__expFrame = 0;"
                    + "window.__exportStep = function(){"
                    + "  if(window.__expFrame >= window.__expTotal){ Android.done(); return; }"
                    + "  var t = window.__expFrame / window.__expFps;"
                    + "  frameAt(t);"
                    + "  var dataUrl = '';"
                    + "  try { dataUrl = cv.toDataURL('image/jpeg', 0.90); } catch(e) { dataUrl = ''; }"
                    + "  Android.onFrameCaptured(dataUrl, window.__expFrame, window.__expTotal);"
                    + "  window.__expFrame++;"
                    + "};"
                    + "if(window.renderSfxWav){"
                    + "  window.renderSfxWav(function(blob){"
                    + "    var r = new FileReader();"
                    + "    r.onloadend = function(){ Android.pushWavBase64(r.result); window.__exportStep(); };"
                    + "    r.readAsDataURL(blob);"
                    + "  });"
                    + "} else { window.__exportStep(); }"
                    + "})();";

            webView.evaluateJavascript(jsExportInit, null);

        } catch (Exception e) {
            isExporting = false;
            btnExportMp4.setEnabled(true);
            tvStatus.setText("Encoder init error: " + e.getMessage());
        }
    }

    private void showBuiltInVideoPlayer(File mp4File) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        VideoView videoView = new VideoView(this);
        videoView.setVideoPath(mp4File.getAbsolutePath());
        MediaController mc = new MediaController(this);
        mc.setAnchorView(videoView);
        videoView.setMediaController(mc);

        dialog.setContentView(videoView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        dialog.show();
        videoView.start();
    }

    private void publishToGallery(File mp4File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Video.Media.DISPLAY_NAME, mp4File.getName());
                values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
                values.put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/NHBOX_Intros");
                Uri uri = getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
                if (uri != null) {
                    try (OutputStream os = getContentResolver().openOutputStream(uri);
                         FileInputStream fis = new FileInputStream(mp4File)) {
                        byte[] buf = new byte[16384];
                        int r;
                        while ((r = fis.read(buf)) != -1) {
                            os.write(buf, 0, r);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void shareExportedVideo(File file) {
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("video/mp4");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Share or Play Exported Ultra HD Intro"));
    }

    private String getFileName(Uri uri) {
        String result = "custom_intro.html";
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) result = cursor.getString(idx);
            }
        } catch (Exception ignored) {}
        return result;
    }

    public class AndroidBridge {

        @JavascriptInterface
        public void pushWavBase64(String dataUrl) {
            if (dataUrl == null) return;
            try {
                int comma = dataUrl.indexOf(',');
                String b64 = (comma >= 0) ? dataUrl.substring(comma + 1) : dataUrl;
                pendingWavAudioBytes = Base64.decode(b64, Base64.DEFAULT);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @JavascriptInterface
        public void onFrameCaptured(String dataUrl, int frameIdx, int totalFrames) {
            if (!isExporting || activeEncoder == null) return;
            encodeExecutor.execute(() -> {
                try {
                    if (dataUrl != null && !dataUrl.isEmpty()) {
                        int commaIdx = dataUrl.indexOf(',');
                        String base64Part = (commaIdx >= 0) ? dataUrl.substring(commaIdx + 1) : dataUrl;
                        byte[] jpegBytes = Base64.decode(base64Part, Base64.DEFAULT);
                        Bitmap bmp = BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.length);
                        if (bmp != null) {
                            activeEncoder.encodeFrame(bmp, frameIdx);
                            bmp.recycle();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                int pct = (totalFrames > 0) ? (int) (((frameIdx + 1) * 100L) / totalFrames) : 0;
                runOnUiThread(() -> {
                    progressBar.setProgress(pct);
                    tvStatus.setText("Encoding MP4 Frame " + (frameIdx + 1) + " / " + totalFrames + " (" + pct + "%)");
                    if (isExporting) {
                        webView.evaluateJavascript("if(window.__exportStep) window.__exportStep();", null);
                    }
                });
            });
        }

        @JavascriptInterface
        public void pushFrame(String dataUrl) {
            if (!isExporting || activeEncoder == null || dataUrl == null) return;
            final int fIdx = currentFrameIdx++;
            encodeExecutor.execute(() -> {
                try {
                    int commaIdx = dataUrl.indexOf(',');
                    String base64Part = (commaIdx >= 0) ? dataUrl.substring(commaIdx + 1) : dataUrl;
                    byte[] jpegBytes = Base64.decode(base64Part, Base64.DEFAULT);
                    Bitmap bmp = BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.length);
                    if (bmp != null) {
                        activeEncoder.encodeFrame(bmp, fIdx);
                        bmp.recycle();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        @JavascriptInterface
        public void progress(int frame, int totalFrames) {
            int pct = (totalFrames > 0) ? (int) ((frame * 100L) / totalFrames) : 0;
            runOnUiThread(() -> {
                progressBar.setProgress(pct);
                tvStatus.setText("Encoding Frame " + frame + " / " + totalFrames + " (" + pct + "%)");
            });
        }

        @JavascriptInterface
        public void done() {
            if (!isExporting) return;
            isExporting = false;
            runOnUiThread(() -> tvStatus.setText("Muxing H.264 Video + AAC Audio into MP4..."));

            encodeExecutor.execute(() -> {
                if (activeEncoder != null) {
                    activeEncoder.finishAndMuxAudio(pendingWavAudioBytes);
                    activeEncoder = null;
                }
                if (lastExportedMp4 != null && lastExportedMp4.exists()) {
                    publishToGallery(lastExportedMp4);
                }
                long elapsedSec = Math.max(1, (System.currentTimeMillis() - exportStartTimeMs) / 1000);
                runOnUiThread(() -> {
                    progressBar.setProgress(100);
                    btnExportMp4.setEnabled(true);
                    btnPlayExported.setEnabled(true);
                    btnShareVideo.setEnabled(true);
                    tvStatus.setText("✅ Exported in " + elapsedSec + "s! Saved to Movies/NHBOX_Intros");
                    Toast.makeText(MainActivity.this, "MP4 Ready! Tap '▶ Watch' to play.", Toast.LENGTH_LONG).show();
                });
            });
        }
    }
}
