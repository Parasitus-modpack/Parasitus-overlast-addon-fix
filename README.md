# Parasites-Addon-OverLast-fix

Scape and Run: Parasites expansion mod fork with server/client fixes, broadcast formatting updates, and player-side radio mute controls.

## Current mod version

- Mod version: `0.2.9`
- Minecraft target: `1.12.2`
- Main mod id: `overlast`

## Build

This project is built with ForgeGradle 2.3 and Java 8.

Local build command:

```powershell
.\gradlew --no-daemon build
```

Built jar output:

```text
build/libs/Parasites-Addon-OverLast-0.2.9.jar
```

## Broadcast system

The broadcast/chat transmission system is server-side and is built from three main parts:

1. `java/com/overlast/util/Broadcasts.java`
2. `java/com/overlast/handlers/EventHandlerServer.java`
3. `java/com/overlast/command/CommandOverLast.java`

### How the transmission format works

All transmissions currently use this layout:

1. Header
2. Intro
3. Weather message
4. Main message
5. Outro

In chat it is formatted like this:

```text
========== [Incoming Transmission] ==========
> <intro>
> <weather>
> <main>
> <outro>
```

Formatting is applied in `java/com/overlast/util/Broadcasts.java`:

- Header text and separators are built in `createHeader()`
- Each content line is styled in `createLine(...)`
- Full transmissions are sent with:
  - `sendTransmission(...)`
  - `sendDailyTransmission(...)`
  - `sendInvasionTransmission(...)`

## Where the daily radio message comes from

Daily radio broadcasts are triggered in:

- `java/com/overlast/handlers/EventHandlerServer.java`

Relevant logic:

test commit

- `onWorldTick(...)`
  - checks the overworld once per day near the start of the day
- `broadcastDailyMessage(...)`
  - assembles the intro, weather, main message, and outro
- `getWeatherMessage(...)`
  - chooses which weather line to use

### Daily message source keys in `en_us.lang`

The daily broadcast currently reads from:

- `message.seasons.login`
  - file: `resources/assets/overlast/lang/en_us.lang`
  - line: `117`
  - used as the intro line

- `message.seasons.spring`
  - line: `112`
- `message.seasons.summer`
  - line: `113`
- `message.seasons.fall`
  - line: `114`
- `message.seasons.winter`
  - line: `115`
  - these are used as season names inserted into `message.seasons.login`

- `message.seasons.daily0` through `message.seasons.daily56`
  - lines: `123` through `179`
  - one of these is picked at random each day as the main line

- `broadcast.overlast.daily.weather.clear`
  - line: `463`
- `broadcast.overlast.daily.weather.rain`
  - line: `464`
- `broadcast.overlast.daily.weather.thunder`
  - line: `465`
  - one of these is used as the weather line based on current overworld weather

- `broadcast.overlast.daily.outro`
  - line: `466`
  - used as the outro line

### Daily transmission flow

The current daily transmission is assembled as:

1. Intro:
   `message.seasons.login`
2. Weather:
   `broadcast.overlast.daily.weather.clear` or `rain` or `thunder`
3. Main:
   one random `message.seasons.daily*`
4. Outro:
   `broadcast.overlast.daily.outro`

### Notes about the seasonal text blocks

The `.lang` file also already contains extra seasonal pools that are not yet wired into the broadcast scheduler:

- `message.seasons.dailySpring0` through `dailySpring8`
  - starts at line `181`
- `message.seasons.dailySummer0` through `dailySummer8`
  - starts at line `191`
- `message.seasons.dailyFall0` through `dailyFall8`
  - starts at line `201`
- `message.seasons.dailyWinter0` through `dailyWinter8`
  - starts at line `212`
- `message.seasons.dailyEnd0` through `dailyEnd19`
  - starts at line `222`

These keys are present in the language file but are not currently selected by the daily radio code.

## Test broadcast command

Manual test broadcasts are sent by:

- `/overlast broadcasttest`

Implementation:

- file: `java/com/overlast/command/CommandOverLast.java`

The test transmission reads from these keys in `resources/assets/overlast/lang/en_us.lang`:

- `broadcast.overlast.test.intro`
  - line: `455`
- `broadcast.overlast.test.weather`
  - line: `456`
- `broadcast.overlast.test.body`
  - line: `457`
- `broadcast.overlast.test.outro`
  - line: `458`

These are used only by the manual test command.

## Invasion test broadcast

There is also an invasion-style test transmission:

- `/overlast invasiontest`

Implementation:

- file: `java/com/overlast/command/CommandOverLast.java`

It currently reads from:

- `broadcast.overlast.invasion.intro`
  - line: `459`
- `broadcast.overlast.invasion.weather`
  - line: `460`
- `broadcast.overlast.invasion.body`
  - line: `461`
- `broadcast.overlast.invasion.outro`
  - line: `462`

At the moment this is a test/admin path. There is not yet a real gameplay invasion event wired into this addon.

## Player mute commands

Available player commands:

- `/overlast mute`
- `/overlast unmute`
- `/overlast mutestatus`
- `/overlast muteuntilnextinvasion`

Admin/testing commands:

- `/overlast broadcasttest`
- `/overlast invasiontest`

Implementation:

- file: `java/com/overlast/command/CommandOverLast.java`

### What each mute mode does

- `mute`
  - mutes all OverLast transmissions for that player

- `muteuntilnextinvasion`
  - hides only daily radio transmissions for that player
  - the temporary mute is cleared when an invasion transmission is sent

- `mutestatus`
  - shows a styled status line:
    - `Radio Status - Active`
    - `Radio Status - Muted`
    - `Radio Status - Muted Until Next Invasion`

### Where mute state is saved

Player mute state is stored in player persisted NBT on the server in:

- `java/com/overlast/util/Broadcasts.java`

NBT keys used:

- `overlastMutedBroadcasts`
- `overlastDailyMutedUntilInvasion`

These are stored under the player's `PlayerPersisted` NBT compound, so they survive relogs.

## Language file reference

Primary language file:

- `resources/assets/overlast/lang/en_us.lang`

Important current blocks:

- Lines `112-117`
  - season names and `message.seasons.login`

- Lines `123-179`
  - main daily random pool: `message.seasons.daily*`

- Lines `181-241`
  - extra seasonal/endgame pools not yet wired

- Lines `445-452`
  - OverLast command feedback strings

- Lines `455-466`
  - test broadcast, invasion test broadcast, and daily weather/outro keys

## Important maintenance note

The line numbers above are accurate for the current repository state when this README was updated. If the language file is edited later, those line numbers may shift, but the key names remain the stable source of truth.
