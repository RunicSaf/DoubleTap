# Doubletap v1.0

Doubletap is a lightweight Android utility for binding hardware key gestures to apps.

## Core features

- Multiple app bindings
- App icons in picker and binding cards
- Double and triple press gestures
- Two-key and three-key gesture sequences
- Optional keep-active mode with persistent notification
- Local-only settings

## Supported trigger examples

- ↑↑ Double press volume up
- ↓↓↓ Triple press volume down
- ↑↓ Press both volume keys together
- ⏻↑ Press power + volume up together
- ⏻→↑ Power then volume up
- ↓→↑→⏻ Down, up, power
https://github.com/RunicSaf/DoubleTap/blob/main/README.md

## Privacy

Doubletap is local-only.

Doubletap stores your shortcut bindings on your device using Android local storage. It does not collect, transmit, sell, or share personal data.

Doubletap does not use analytics, ads, tracking, accounts, cloud sync, or network access.

The app uses Android Accessibility only to detect the hardware key gestures you configure and open the apps you choose. It does not read screen content, type for you, inspect other apps, or capture personal information.

## Permissions

Doubletap uses Android Accessibility so it can detect supported hardware key gestures while other apps are open.

Optional Keep Active mode uses a foreground service with a persistent notification to help keep shortcuts available in the background.

Hardware key support depends on the device. Some phones may block certain keys or combinations.

## Notes

Hardware key support depends on which key events your device exposes to Accessibility.
