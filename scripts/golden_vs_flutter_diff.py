#!/usr/bin/env python3
# Cross-source golden diff: Kuikly Android actual/ vs Flutter reference baseline/.
# Equivalent to scripts/golden-vs-flutter-diff.sh (mask top/bottom, resize, RMSE rel).
# Requires: Pillow, numpy  (pip install Pillow numpy)
import os, sys, math
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
KUIKLY = os.environ.get("KUIKLY_ACTUAL", os.path.join(ROOT, "goldens/android/actual"))
REF    = os.environ.get("FLUTTER_REF",   os.path.join(ROOT, "goldens/flutter-ref/baseline"))
DIFF   = os.path.join(ROOT, "goldens/flutter-ref/diff")
RMSE_MAX = float(os.environ.get("GOLDEN_RMSE_MAX", "0.22"))
# 遮罩按**屏高比例**施加，而非固定像素：KMP 截图 1080x2340、Flutter 参考 1440x3120，
# 若两边都遮 80px，KMP 实际盖住 3.42% 屏高、Flutter 只盖 2.56%，resize 对齐后会拿
# KMP 的导航栏去比 Flutter 的状态栏（残留 27 行「黑 vs 状态栏」）。基准取参考图尺寸。
MASK_TOP_FRAC = float(os.environ.get("MASK_TOP_FRAC", str(80 / 3120)))
MASK_BOT_FRAC = float(os.environ.get("MASK_BOT_FRAC", str(40 / 3120)))

# (kmp_stem, flutter_stem) — mirrors golden-vs-flutter-diff.sh MAP
MAP = [
    ("02-main-home","02-flutter-main-home"),
    ("02b-main-home-logged-in","02b-flutter-main-home-logged-in"),
    ("06-main-me-guest","03-flutter-main-me-guest"),
    ("07-main-me-logged-in","08-flutter-main-me-logged-in"),
    ("03-login","07-flutter-login"),
    ("10-search","10-flutter-search"),
    ("04-usedcar-list","04-flutter-usedcar-list"),
    ("11-main-chat","11-flutter-main-chat"),
    ("13-main-community","13-flutter-main-community"),
    ("12-chat-detail","12-flutter-chat-detail"),
    ("05-usedcar-detail","05-flutter-usedcar-detail"),
    ("08-settings","08-flutter-settings"),
    ("09-login-password","09-flutter-login-password"),
    ("15-music-list","15-flutter-music-list"),
    ("19-friend-list","19-flutter-friend-list"),
    ("25-pay-list","25-flutter-pay-list"),
    ("17-video-list","17-flutter-video-list"),
    ("18-video-detail","18-flutter-video-detail"),
    ("21-classroom-list","21-flutter-classroom-list"),
    ("23-live-list","23-flutter-live-list"),
    ("24-live-detail","24-flutter-live-detail"),
    ("27-all-services","27-flutter-all-services"),
    ("28-register","28-flutter-register"),
    ("29-music-now-playing","29-flutter-music-now-playing"),
    ("30-strategy","30-flutter-strategy"),
    ("31-hot-rank-detail","31-flutter-hot-rank-detail"),
    ("32-personalized-settings","32-flutter-personalized-settings"),
    ("33-learning-report","33-flutter-learning-report"),
    ("34-check-in-mall","34-flutter-check-in-mall"),
    ("35-deal-invoice-upload","35-flutter-deal-invoice-upload"),
    ("36-classroom-gift-claim","36-flutter-classroom-gift-claim"),
    ("37-classroom-video-detail","37-flutter-classroom-video-detail"),
    ("38-community-publish","38-flutter-community-publish"),
    ("39-dubbing-home","39-flutter-dubbing-home"),
    ("40-dubbing-work-list","40-flutter-dubbing-work-list"),
    ("41-dubbing-work-detail","41-flutter-dubbing-work-detail"),
]

def load_masked(path):
    im = Image.open(path).convert("RGB")
    w, h = im.size
    px = im.load()
    top = min(int(round(MASK_TOP_FRAC * h)), h)
    bot = min(int(round(MASK_BOT_FRAC * h)), h - top)
    for y in range(0, top):
        for x in range(w):
            px[x, y] = (0, 0, 0)
    y0 = h - bot
    for y in range(y0, h):
        for x in range(w):
            px[x, y] = (0, 0, 0)
    return im

def rmse_rel(a, b):
    # a, b are equal-size RGB images
    pa = a.load(); pb = b.load()
    w, h = a.size
    s = 0.0
    n = w * h * 3
    for y in range(h):
        for x in range(w):
            ra, ga, ba = pa[x, y]
            rb, gb, bb = pb[x, y]
            s += (ra - rb) ** 2 + (ga - gb) ** 2 + (ba - bb) ** 2
    mse = s / n
    return math.sqrt(mse) / 255.0

def main():
    os.makedirs(DIFF, exist_ok=True)
    fail = 0
    for k, f in MAP:
        ka = os.path.join(KUIKLY, k + ".png")
        fr = os.path.join(REF, f + ".png")
        if not os.path.isfile(ka):
            print(f"!! missing Kuikly actual {k}"); fail = 1; continue
        if not os.path.isfile(fr):
            print(f"!! missing Flutter ref {f}"); fail = 1; continue
        kim = load_masked(ka)
        fim = load_masked(fr)
        # resize kmp -> flutter size (||!)
        kim = kim.resize(fim.size, Image.LANCZOS)
        rel = rmse_rel(kim, fim)
        if rel > RMSE_MAX:
            print(f"!! DIFF {k} vs {f} RMSE_rel={rel:.4f} (max {RMSE_MAX})")
            fail = 1
        else:
            print(f"==> OK  {k} vs {f} RMSE_rel={rel:.4f} (max {RMSE_MAX})")
    if fail:
        print("==> some pairs exceeded RMSE gate")
        sys.exit(1)
    print("==> all mapped Cross-Source diffs passed")

if __name__ == "__main__":
    main()
