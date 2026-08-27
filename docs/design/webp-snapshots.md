# Snapshots as lossless WebP

Notes on why snapshots are stored as WebP, why the encoder is configured the way it is, and what
we got wrong on the way there. Written for whoever touches `WebpCodec` next.

## What changed

Three things, in this order:

1. **Removed animated snapshots.** `Paparazzi#gif`, `PaparazziSdk#gif`, the `snapshots/videos`
   directory, and the whole `internal/apng` package (an APNG reader, writer, and a frame-aligning
   verifier — around 700 lines) serving six checked-in files. `SnapshotHandler#newFrameHandler`
   lost its `frameCount` and `fps` parameters. Breaking change.
2. **Switched stills from PNG to lossless WebP.** The old encoder was hand-rolled: raw scanlines,
   filter `None`, `Deflater.BEST_COMPRESSION`. Because it never applied row filtering it produced
   files larger than a normal PNG encoder would.
3. **Migrated everything else that could be WebP** — drawables, differ fixtures, delta comparison
   fixtures, the delta label resources. Deleted six orphaned PNGs that nothing referenced.

Removing video first is what made the format switch small: no animation means no container to
build, just single-image encode and decode.

## Why this library

`javax.imageio` has no WebP support on any JDK, so a library was needed. Two other options were
looked at and dropped:

- **Hand-rolling VP8L.** The lossless bitstream is LZ77 plus canonical Huffman plus colour
  transforms — nothing from the old PNG writer carries over. Thousands of lines.
- **`Bitmap.compress(WEBP_LOSSLESS)` through layoutlib.** libwebp is statically linked into
  layoutlib's `libandroid_runtime`, so this needs no new dependency. But it only works inside a
  live `Bridge.init` session, which rules out the `paparazzi` module's own unit tests and the
  Gradle plugin's `ImageSubject`.

We use `com.github.usefulness:webp-imageio`. It registers an ImageIO SPI, so existing
`ImageIO.read` call sites kept working untouched.

`dev.matrixlab.webp4j` was the other serious candidate and is a decent library — actively
maintained, no transitive dependencies, wider platform coverage. It lost on one specific point,
below.

## The `exact` flag matters more than anything else here

libwebp has a config flag `exact`: *"preserve the exact RGB values under transparent area.
Otherwise, discard this invisible RGB information for better compression."* **It defaults to off.**

Every `Differ` compares with `BufferedImage.getRGB`, which returns full ARGB including the RGB
channels of fully transparent pixels. Compose and Android screenshots are full of transparent
regions. With `exact` off, a golden and a fresh render of the same view can differ on invisible
pixels — `PixelPerfect` fails outright, and `OffByTwo`'s tolerance won't absorb an arbitrary
rewrite either.

webp-imageio exposes the flag. webp4j never references it — checked across its JNI C and its Java —
and offers no way to set it. That decided the library choice.

Measured, on a fully transparent test image: `exact = true` gives 0 differing pixels, `exact = false`
corrupts all 4096. `WebpCodecTest.preservesRgbUnderFullyTransparentPixels` guards this. Don't remove
it.

## Encoder settings, and the mistake we made

Current settings are `CompressionType.Lossless`, `method = 4`, `compressionQuality = 0.5f`,
`exact = true`.

We originally shipped `method = 6` with `compressionQuality = 1f`. The magnitude was never checked,
and it's absurd.

Benchmarked over the 145 goldens in this repo, sweeping method 0–6 against quality 0–1: every one of
the fifteen combinations encodes the corpus in 2–7 seconds, except `m6 q1.0`, which takes **81.6
seconds**. That single combination trips libwebp's exhaustive backward-reference search. It costs
24× the time of `m6 q0.75` to save 2.7% of bytes. Total size varies under 5% across the entire grid.

Under `Lossless`, `method` and `compressionQuality` are effort knobs only — how hard the encoder
hunts for a shorter encoding of the same pixels. They never affect fidelity. Verified: all 145
goldens round-trip with zero differing pixels at every setting tested.
`WebpCodecTest.effortSettingsDoNotAffectFidelity` pins that down, and fails if anyone switches the
codec to lossy.

So tuning these is safe. Maxing them is not worth it.

## Numbers

Golden corpus, 145 files:

| | Size | Encode | Decode |
|---|---:|---:|---:|
| PNG (old hand-rolled writer) | 2509 KB | 21.9 ms/img | 2.50 ms/img |
| WebP `m6 q1.0` (first attempt) | 997 KB | 566.0 ms/img | 0.43 ms/img |
| **WebP `m4 q0.5` (current)** | **1042 KB** | **~14 ms/img** | **0.43 ms/img** |

Net against the old PNG encoder: **58% smaller, encode faster, decode 5.8× faster.**

End to end, same tests and same renders, only the setting changed:
`:sample:recordPaparazziDebug` went from 34.0s to 8.9s.

Checked-in PNGs went from 27 to 3.

## What went wrong along the way

Worth recording so nobody repeats them.

- **A bogus 2× speedup.** The first end-to-end comparison showed `:sample:verifyPaparazziDebug` at
  15.7s on master versus 8.0s on the branch. That has nothing to do with WebP: master's sample still
  had `LottieTest`, two 60fps × 5s gifs costing 7.3s on their own. The measurement was of deleting
  video, not of the codec. Real decode saving on a 44-golden verify is about 90ms — invisible.
- **Removing `androidx.appcompat` from the sample.** A comment implied it was a Lottie workaround,
  so it went out with Lottie. It isn't — it drives view inflation, and dropping it shifted text
  rendering about 1% in three tests. Caught by running master in a scratch worktree instead of
  assuming the failures were pre-existing drift.
- **Re-recording goldens instead of re-encoding them.** An actual `recordPaparazziDebug` rewrites
  goldens with *this machine's* renders, which drift ~0.16% from the committed ones (font
  rendering, under the 1% threshold, so verify still passes). That would have quietly replaced the
  project's goldens with one developer's. Re-encoding instead — decode the existing golden, encode
  the same pixels — makes drift structurally impossible.
- **`git add -A` after test runs.** Swept up files that self-polluting tests had mutated, including
  a fixture whose golden must stay a specific colour. See below.
- **The size figure moved.** An early "46% smaller" came from comparing `du` disk blocks, which
  rounds 145 small files up to 4 KB each. Git object size is the right metric for repo cost.
