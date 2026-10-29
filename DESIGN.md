# Coroutine Lab: screen design

A workbench for predicting execution and inspecting actual traces. Its composition follows a question, strategy, code, action and event sequence. The tool uses an open guide, wrapping controls, a code plate and flat event rows, rather than a repeated panel template. Platform monospace is reserved for source and exact event messages.

## Visual system

Light: paper #FAF8F3, ink #202421, muted #5E625C, copper #9B432F, soft #F2E3DB, outline #85877E. Dark: paper #191D1A, ink #F1EEE7, muted #C7C9C2, copper #EDB7A4, soft #4B3026, outline #96998F. A single copper accent adapts to system dark mode; no dynamic palette overrides it. Body contrast is 14.81:1 light/14.71:1 dark, secondary text 5.86:1/10.20:1 and primary button 6.49:1/9.30:1. State is identified by words as well as color.

Local Outfit handles titles, labels and reading. Controls have explicit 12 dp corners, minimum 48 dp actions and native touch feedback. Gutters 20 dp; section spacing 20–32 dp. Open sections and rules replace repeated cards. Text wraps and scrolls with available space; large text is not shrunk to fit. No remote font provider, analytics or decorative animation is added. ES resources have an English fallback. Full font attribution is in THIRD_PARTY.md and the packaged OFL notice.

## Interaction and ownership

The ViewModel owns the running scenario; navigation between sections and activity recreation do not replace that owner. A toolbar Cancel remains reachable from Guide. Strategy selection freezes during a run, and Clear appears only once work stops. Events keep their real sequence/source/message; no timestamps or performance figures are fabricated. Original scenario titles and raw Spanish trace messages remain unchanged and are identified on screen.

Headings expose native semantics. Trace rows merge their related text, and the Running/Ready label is a polite live region. Standard tab selection/ripple is the only added motion. Code wraps instead of horizontally overflowing. Error snippets show cancellation rethrowing before handling business failures; the executable domain now follows that rule in both previously misclassified cases.

## Checked scope

10 JVM tests; four real MainActivity journeys at API 37, also at 150% text/360×800 dp; selected Flow journey in EN/ES portrait, ES dark and EN landscape. Actual screenshots are emulator output. Lint has no errors and one target warning. CI runs JVM/lint/debug build; native checks are local. API 26 is the declared minimum, not a newly tested lower-platform result. No TalkBack, physical-device, optimized-release or store claim is made.
