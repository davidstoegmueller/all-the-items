package com.daveestar.alltheitems.utils;

import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

import com.daveestar.alltheitems.Main;

public final class CompletionFireworkShow {
  private static final int _TOTAL_CYCLES = 8;
  private static final int _WAVES_PER_CYCLE = 8;
  private static final long _WAVE_SPACING_TICKS = 24L;
  private static final long _CYCLE_GAP_TICKS = 6L;
  private static final long _SHOW_LENGTH_MULTIPLIER = 1L;
  private static final long _INTER_BEAT_BRIDGE_OFFSET_TICKS = 12L;
  private static final long _FINALE_VOLLEY_SPACING_TICKS = 14L;
  private static final int _OMEGA_PHASE_COUNT = 5;
  private static final long _OMEGA_PHASE_SPACING_TICKS = 18L;
  private static final long _OMEGA_AFTERSHOCK_DELAY_TICKS = 108L;
  private static final int _MAX_SAFE_PATTERN_AMOUNT = 30;
  private static final int _MAX_SAFE_HEAVY_PATTERN_AMOUNT = 24;
  private static final double _FINAL_SEQUENCE_INTENSITY = 0.82;
  private static final double _SHOW_FORWARD_DISTANCE = 8.5;
  private static final double _SHOW_BASE_HEIGHT = 3.2;
  private static final double _SPREAD_MULTIPLIER = 1.6;

  private CompletionFireworkShow() {
  }

  public static void play(Main plugin, Player player) {
    _setWorldNight(player);

    long startDelayTicks = _scaledTicks(22L);
    long cycleDurationTicks = _scaledTicks(_WAVES_PER_CYCLE * _WAVE_SPACING_TICKS + _CYCLE_GAP_TICKS);
    long waveSpacingTicks = _scaledTicks(_WAVE_SPACING_TICKS);

    for (int cycle = 0; cycle < _TOTAL_CYCLES; cycle++) {
      final int cycleIndex = cycle;

      for (int wave = 0; wave < _WAVES_PER_CYCLE; wave++) {
        final int waveIndex = wave;
        long delay = startDelayTicks + (cycle * cycleDurationTicks) + (wave * waveSpacingTicks);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
          if (!player.isOnline()) {
            return;
          }

          _launchCycleSequenceBeat(player, cycleIndex, _getSequenceBeat(waveIndex));
        }, delay);

        if (waveIndex < _WAVES_PER_CYCLE - 1) {
          Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) {
              return;
            }

            _launchInterBeatBridge(player, cycleIndex, _getSequenceBeat(waveIndex));
          }, delay + _scaledTicks(_INTER_BEAT_BRIDGE_OFFSET_TICKS));
        }
      }
    }

    long finaleDelay = startDelayTicks + (_TOTAL_CYCLES * cycleDurationTicks);
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      if (!player.isOnline()) {
        return;
      }

      _launchTitanFinaleSequence(plugin, player);
    }, finaleDelay);

    long finaleAndOmegaTailTicks = _scaledTicks(92L + _OMEGA_AFTERSHOCK_DELAY_TICKS) + 12L;
    long dayResetDelayTicks = finaleDelay + finaleAndOmegaTailTicks;

    Bukkit.getScheduler().runTaskLater(plugin, () -> _setWorldDay(player), dayResetDelayTicks);
  }

  private static SequenceBeat _getSequenceBeat(int waveIndex) {
    SequenceBeat[] beats = SequenceBeat.values();

    if (waveIndex < 0 || waveIndex >= beats.length) {
      return SequenceBeat.ASCENSION_STORM;
    }

    return beats[waveIndex];
  }

  private static void _launchCycleSequenceBeat(Player player, int cycle, SequenceBeat beat) {
    double cycleBoost = cycle * 0.75;
    int intensityBoost = 1 + (cycle * 3);
    boolean oddCycle = cycle % 2 == 1;

    switch (beat) {
      case HORIZON_FANFARE:
        if (oddCycle) {
          _launchFanFireworks(player, 12 + intensityBoost, 5.8 + cycleBoost, 3.0 + (cycle * 0.3), true);
          _launchArcSweepFireworks(player, 10 + cycle, 5.2 + cycleBoost, 3.4 + (cycle * 0.25), false);
          _launchCometLanes(player, 8 + cycle, 5.5 + cycleBoost, 3.3 + (cycle * 0.2), false);
        } else {
          _launchCircularFireworks(player, 10 + intensityBoost, 4.8 + cycleBoost, 2.8 + (cycle * 0.3), false);
          _launchStarFireworks(player, 8 + (cycle * 2), 4.5 + cycleBoost, 3.1 + (cycle * 0.22), false);
          _launchCometLanes(player, 7 + cycle, 5.0 + cycleBoost, 3.1 + (cycle * 0.2), false);
        }
        player.playSound(player, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.85F, 1.03F + (cycle * 0.04F));
        break;
      case SKY_CRUCIBLE:
        if (oddCycle) {
          _launchArcSweepFireworks(player, 12 + intensityBoost, 5.4 + cycleBoost, 3.1 + (cycle * 0.25), false);
          _launchCrossFireworks(player, 9 + cycle, 4.7 + cycleBoost, 3.3 + (cycle * 0.22), false);
          _launchCanopyFireworks(player, 7 + cycle, 5.4 + cycleBoost, 4.3 + (cycle * 0.25), false);
        } else {
          _launchCrossFireworks(player, 11 + intensityBoost, 4.6 + cycleBoost, 3.0 + (cycle * 0.25), false);
          _launchFanFireworks(player, 8 + cycle, 4.9 + cycleBoost, 3.0 + (cycle * 0.2), false);
          _launchCanopyFireworks(player, 6 + cycle, 5.0 + cycleBoost, 4.0 + (cycle * 0.25), false);
        }
        player.playSound(player, Sound.ENTITY_FIREWORK_ROCKET_BLAST_FAR, 0.78F, 1.02F);
        break;
      case HELIX_ASCENT:
        if (oddCycle) {
          _launchHelixFireworks(player, 12 + intensityBoost, 5.0 + cycleBoost, 3.4 + (cycle * 0.25), false);
          _launchSpiralFireworks(player, 8 + cycle, 4.6 + cycleBoost, 3.8 + (cycle * 0.22), false);
          _launchCanopyFireworks(player, 5 + cycle, 4.6 + cycleBoost, 4.8 + (cycle * 0.2), false);
        } else {
          _launchSpiralFireworks(player, 11 + intensityBoost, 5.2 + cycleBoost, 3.3 + (cycle * 0.25), false);
          _launchHelixFireworks(player, 8 + cycle, 4.6 + cycleBoost, 3.7 + (cycle * 0.22), false);
          _launchCanopyFireworks(player, 5 + cycle, 4.8 + cycleBoost, 4.8 + (cycle * 0.2), false);
        }
        player.playSound(player, Sound.ENTITY_FIREWORK_ROCKET_BLAST_FAR, 0.88F, 0.93F);
        break;
      case EMBER_SWELL:
        _launchBurstFireworks(player, 10 + intensityBoost, 3.8 + cycleBoost, 3.2 + (cycle * 0.35), false);
        _launchFanFireworks(player, 6 + cycle, 3.6 + cycleBoost, 2.7 + (cycle * 0.2), false);
        break;
      case TWIN_HALO:
        _launchDualRingFireworks(player, 6 + cycle, 2.6 + (cycle * 0.35), 4.8 + cycleBoost, 2.8 + (cycle * 0.3));
        player.playSound(player, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 0.8F, 1.1F);
        break;
      case SKYFALL_CASCADE:
        _launchRainFireworks(player, 14 + (intensityBoost * 2), 4.8 + cycleBoost, 4.2 + (cycle * 0.4), true);
        break;
      case STARFURY_PULSE:
        _launchStarFireworks(player, 12 + intensityBoost, 4.5 + cycleBoost, 3.0 + (cycle * 0.25), true);
        _launchBurstFireworks(player, 6 + intensityBoost, 3.6 + cycleBoost, 3.5 + (cycle * 0.3), true);
        player.playSound(player, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.9F, 0.9F + (cycle * 0.04F));
        break;
      case ASCENSION_STORM:
      default:
        _launchBurstFireworks(player, 16 + (intensityBoost * 2), 5.2 + cycleBoost, 3.5 + (cycle * 0.35), true);
        _launchRainFireworks(player, 10 + intensityBoost, 5.0 + cycleBoost, 4.8 + (cycle * 0.4), true);
        _launchCanopyFireworks(player, 8 + cycle, 5.4 + cycleBoost, 5.2 + (cycle * 0.3), true);
        player.spawnParticle(Particle.FIREWORK, player.getLocation().add(0, 1.1, 0), 28 + (cycle * 6),
            1.5 + (cycle * 0.25), 1.5, 1.5 + (cycle * 0.25), 0.08);
        player.playSound(player, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE_FAR, 0.9F, 1.18F);
        break;
    }
  }

  private static void _launchInterBeatBridge(Player player, int cycle, SequenceBeat beat) {
    double cycleBoost = cycle * 0.6;

    switch (beat) {
      case HORIZON_FANFARE:
      case SKY_CRUCIBLE:
        _launchCometLanes(player, 5 + cycle, 4.2 + cycleBoost, 3.0 + (cycle * 0.15), false);
        break;
      case HELIX_ASCENT:
      case EMBER_SWELL:
        _launchBurstFireworks(player, 8 + (cycle * 2), 4.6 + cycleBoost, 3.5 + (cycle * 0.2), false);
        break;
      case TWIN_HALO:
      case SKYFALL_CASCADE:
        _launchCanopyFireworks(player, 4 + cycle, 4.6 + cycleBoost, 4.7 + (cycle * 0.2), true);
        break;
      case STARFURY_PULSE:
      case ASCENSION_STORM:
      default:
        _launchBurstFireworks(player, 10 + (cycle * 2), 5.0 + cycleBoost, 3.9 + (cycle * 0.2), true);
        _launchCometLanes(player, 4 + cycle, 4.8 + cycleBoost, 3.2 + (cycle * 0.15), true);
        break;
    }

    player.playSound(player, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE_FAR, 0.55F, 1.08F + (cycle * 0.02F));
  }

  private static void _launchTitanFinaleSequence(Main plugin, Player player) {
    player.playSound(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F);

    for (int volley = 0; volley < 6; volley++) {
      final int volleyIndex = volley;
      Bukkit.getScheduler().runTaskLater(plugin, () -> {
        if (!player.isOnline()) {
          return;
        }

        _launchDualRingFireworks(player, 9 + (volleyIndex * 2), 3.2 + (volleyIndex * 0.32),
            5.2 + (volleyIndex * 0.34), 3.1 + (volleyIndex * 0.22));

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
          if (!player.isOnline()) {
            return;
          }

          _launchStarFireworks(player, _finaleAmount(10 + (volleyIndex * 2)), 5.0 + (volleyIndex * 0.2),
              3.7 + (volleyIndex * 0.16), true);
        }, 3L);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
          if (!player.isOnline()) {
            return;
          }

          _launchBurstFireworks(player, _finaleAmount(10 + (volleyIndex * 4)), 5.6 + (volleyIndex * 0.26),
              3.9 + (volleyIndex * 0.3), true);
        }, 6L);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
          if (!player.isOnline()) {
            return;
          }

          _launchRainFireworks(player, _finaleAmount(10 + (volleyIndex * 2)), 5.3 + (volleyIndex * 0.22),
              4.6 + (volleyIndex * 0.26), true);
        }, 9L);

        player.playSound(player, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0F, 0.9F + (volleyIndex * 0.07F));
        player.playSound(player, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 0.95F, 1.18F);
        player.spawnParticle(Particle.FIREWORK, player.getLocation().add(0, 1.1, 0), 36 + (volleyIndex * 10),
            1.9 + (volleyIndex * 0.18), 1.8, 1.9 + (volleyIndex * 0.18), 0.11);
      }, _scaledTicks(volley * _FINALE_VOLLEY_SPACING_TICKS));
    }

    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      if (!player.isOnline()) {
        return;
      }

      _launchBurstFireworks(player, _finaleAmount(24), 6.6, 4.9, true);
      _launchRainFireworks(player, _finaleAmount(20), 5.9, 5.4, true);
      player.playSound(player, Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 1.0F, 0.95F);
      player.playSound(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 0.8F);
      player.spawnParticle(Particle.FIREWORK, player.getLocation().add(0, 1.3, 0), 58, 2.4, 1.8, 2.4, 0.12);
    }, _scaledTicks(68L));

    _launchOverdriveOmegaSequence(plugin, player, _scaledTicks(92L));
  }

  private static void _launchOverdriveOmegaSequence(Main plugin, Player player, long startDelay) {
    for (int phase = 0; phase < _OMEGA_PHASE_COUNT; phase++) {
      final int phaseIndex = phase;
      Bukkit.getScheduler().runTaskLater(plugin, () -> {
        if (!player.isOnline()) {
          return;
        }

        double phaseBoost = phaseIndex * 0.35;

        _launchDualRingFireworks(player, 9 + (phaseIndex * 2), 3.4 + phaseBoost, 6.1 + phaseBoost, 3.4 + phaseBoost);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
          if (!player.isOnline()) {
            return;
          }

          _launchHelixFireworks(player, _finaleAmount(7 + (phaseIndex * 2)), 4.9 + phaseBoost, 3.6 + phaseBoost,
              true);
          _launchFanFireworks(player, _finaleAmount(7 + phaseIndex), 5.7 + phaseBoost, 3.3 + phaseBoost, true);
        }, 3L);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
          if (!player.isOnline()) {
            return;
          }

          _launchStarFireworks(player, _finaleAmount(10 + (phaseIndex * 2)), 5.4 + phaseBoost, 3.9 + phaseBoost,
              true);
          _launchBurstFireworks(player, _finaleAmount(11 + (phaseIndex * 3)), 6.4 + phaseBoost, 4.2 + phaseBoost,
              true);
        }, 7L);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
          if (!player.isOnline()) {
            return;
          }

          _launchRainFireworks(player, _finaleAmount(11 + (phaseIndex * 2)), 6.1 + phaseBoost, 5.5 + phaseBoost,
              true);
        }, 11L);

        player.playSound(player, Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 1.0F, 0.85F + (phaseIndex * 0.05F));
        player.playSound(player, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE_FAR, 1.0F, 1.22F);
        player.spawnParticle(Particle.FIREWORK, player.getLocation().add(0, 1.5, 0), 48 + (phaseIndex * 8),
            3.2 + (phaseIndex * 0.25), 2.1, 3.2 + (phaseIndex * 0.25), 0.2);
      }, startDelay + _scaledTicks(phase * _OMEGA_PHASE_SPACING_TICKS));
    }

    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      if (!player.isOnline()) {
        return;
      }

      _launchBurstFireworks(player, _finaleAmount(12), 7.0, 4.8, true);
      _launchRainFireworks(player, _finaleAmount(10), 6.5, 5.8, true);
      _launchStarFireworks(player, _finaleAmount(8), 6.1, 4.3, true);
      _launchHelixFireworks(player, _finaleAmount(5), 5.6, 3.9, true);

      Bukkit.getScheduler().runTaskLater(plugin, () -> {
        if (!player.isOnline()) {
          return;
        }

        _launchBurstFireworks(player, _finaleAmount(10), 7.2, 5.0, true);
        _launchRainFireworks(player, _finaleAmount(8), 6.7, 5.9, true);
      }, 6L);

      player.playSound(player, Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 1.0F, 0.75F);
      player.playSound(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 0.65F);
      player.spawnParticle(Particle.FIREWORK, player.getLocation().add(0, 1.5, 0), 42, 2.6, 1.8, 2.6, 0.12);
    }, startDelay + _scaledTicks(_OMEGA_AFTERSHOCK_DELAY_TICKS));
  }

  private static void _launchCircularFireworks(Player player, int amount, double radius, double height,
      boolean intense) {
    double spreadRadius = _spread(radius);

    for (int i = 0; i < amount; i++) {
      double angle = ((Math.PI * 2) / amount) * i;
      double offsetX = Math.cos(angle) * spreadRadius;
      double offsetZ = Math.sin(angle) * spreadRadius;

      _spawnDecoratedFirework(player, offsetX, height, offsetZ, intense);
    }
  }

  private static void _launchFanFireworks(Player player, int amount, double width, double height,
      boolean intense) {
    double spreadWidth = _spread(width);

    if (amount <= 1) {
      _spawnDecoratedFirework(player, 0, height, 2.0, intense);
      return;
    }

    for (int i = 0; i < amount; i++) {
      double progress = i / (double) (amount - 1);
      double offsetX = -spreadWidth + (progress * spreadWidth * 2.0);
      double offsetZ = 1.2 + Math.abs(offsetX) * 0.35;

      _spawnDecoratedFirework(player, offsetX, height + (progress * 0.5), offsetZ, intense);
    }
  }

  private static void _launchCrossFireworks(Player player, int amount, double radius, double height,
      boolean intense) {
    double spreadRadius = _spread(radius);

    for (int i = 0; i < amount; i++) {
      double axis = (i % 2 == 0) ? spreadRadius : -spreadRadius;
      double offsetX = (i < amount / 2) ? axis : 0;
      double offsetZ = (i < amount / 2) ? 0 : axis;

      _spawnDecoratedFirework(player, offsetX, height, offsetZ, intense);
    }
  }

  private static void _launchSpiralFireworks(Player player, int amount, double radius, double startHeight,
      boolean intense) {
    double spreadRadius = _spread(radius);

    for (int i = 0; i < amount; i++) {
      double progress = i / (double) amount;
      double angle = (Math.PI * 2.4) * progress;
      double currentRadius = spreadRadius * (0.55 + (0.45 * progress));
      double offsetX = Math.cos(angle) * currentRadius;
      double offsetZ = Math.sin(angle) * currentRadius;
      double height = startHeight + (1.1 * progress);

      _spawnDecoratedFirework(player, offsetX, height, offsetZ, intense);
    }
  }

  private static void _launchHelixFireworks(Player player, int amount, double radius, double startHeight,
      boolean intense) {
    double spreadRadius = _spread(radius);

    for (int i = 0; i < amount; i++) {
      double progress = i / (double) amount;
      double angle = (Math.PI * 4.0) * progress;
      double offsetX = Math.cos(angle) * spreadRadius;
      double offsetZ = (Math.sin(angle) * spreadRadius * 0.45) + (progress * _spread(2.4));
      double height = startHeight + (1.5 * progress);

      _spawnDecoratedFirework(player, offsetX, height, offsetZ, intense);
      _spawnDecoratedFirework(player, -offsetX * 0.7, height + 0.45, offsetZ + 0.6, intense);
    }
  }

  private static void _launchArcSweepFireworks(Player player, int amount, double radius, double height,
      boolean intense) {
    double spreadRadius = _spread(radius);

    for (int i = 0; i < amount; i++) {
      double progress = i / (double) (amount - 1);
      double angle = (-Math.PI * 0.65) + (progress * Math.PI * 1.3);
      double offsetX = Math.sin(angle) * spreadRadius;
      double offsetZ = Math.cos(angle) * (spreadRadius * 0.65);

      _spawnDecoratedFirework(player, offsetX, height + (Math.abs(offsetX) * 0.08), offsetZ + 1.2, intense);
    }
  }

  private static void _launchCometLanes(Player player, int amount, double width, double height,
      boolean intense) {
    if (amount <= 1) {
      _spawnDecoratedFirework(player, 0, height, 4.0, intense);
      return;
    }

    double spreadWidth = _spread(width);

    for (int i = 0; i < amount; i++) {
      double progress = i / (double) (amount - 1);
      double offsetX = -spreadWidth + (progress * spreadWidth * 2.0);
      double offsetZ = 3.8 + (Math.abs(offsetX) * 0.2);

      _spawnDecoratedFirework(player, offsetX, height + (progress * 0.35), offsetZ, intense);
    }
  }

  private static void _launchCanopyFireworks(Player player, int amount, double radius, double height,
      boolean intense) {
    double spreadRadius = _spread(radius);

    for (int i = 0; i < amount; i++) {
      double angle = ((Math.PI * 2) / amount) * i;
      double offsetX = Math.cos(angle) * spreadRadius;
      double offsetZ = Math.sin(angle) * spreadRadius;
      double curveHeight = height + (Math.cos(angle * 2) * 0.45);

      _spawnDecoratedFirework(player, offsetX, curveHeight, offsetZ + 1.8, intense);
    }
  }

  private static void _launchDualRingFireworks(Player player, int ringAmount, double innerRadius,
      double outerRadius, double height) {
    _launchCircularFireworks(player, ringAmount, innerRadius, height, false);
    _launchCircularFireworks(player, ringAmount + 2, outerRadius, height + 0.5, true);
  }

  private static void _launchRainFireworks(Player player, int amount, double radius, double maxHeight,
      boolean intense) {
    amount = _normalizePatternAmount(amount, intense);
    double spreadRadius = _spread(radius);

    for (int i = 0; i < amount; i++) {
      double offsetX = _randomSignedWithMinAbs(spreadRadius, spreadRadius * 0.2);
      double offsetZ = _randomSignedWithMinAbs(spreadRadius, spreadRadius * 0.2);
      double offsetY = 2.4 + ThreadLocalRandom.current().nextDouble(0.8, maxHeight);

      _spawnDecoratedFirework(player, offsetX, offsetY, offsetZ, intense);
    }
  }

  private static void _launchStarFireworks(Player player, int amount, double radius, double height,
      boolean intense) {
    amount = _normalizePatternAmount(amount, intense);
    for (int i = 0; i < amount; i++) {
      double angle = ((Math.PI * 2) / amount) * i;
      double starRadius = (i % 2 == 0) ? radius : radius * 0.45;
      double offsetX = Math.cos(angle) * starRadius;
      double offsetZ = Math.sin(angle) * starRadius;

      _spawnDecoratedFirework(player, offsetX, height, offsetZ, intense);
    }
  }

  private static void _launchBurstFireworks(Player player, int amount, double radius, double baseHeight,
      boolean intense) {
    amount = _normalizePatternAmount(amount, intense);
    double spreadRadius = _spread(radius);

    for (int i = 0; i < amount; i++) {
      double offsetX = _randomSignedWithMinAbs(spreadRadius, spreadRadius * 0.18);
      double offsetZ = _randomSignedWithMinAbs(spreadRadius, spreadRadius * 0.18);
      double offsetY = baseHeight + ThreadLocalRandom.current().nextDouble(0.0, 1.4);

      _spawnDecoratedFirework(player, offsetX, offsetY, offsetZ, intense);
    }
  }

  private static void _spawnDecoratedFirework(Player player, double offsetX, double offsetY, double offsetZ,
      boolean intense) {
    Location spawnLocation = _toShowLocation(player, offsetX, offsetY, offsetZ);
    Firework firework = player.getWorld().spawn(spawnLocation, Firework.class);
    FireworkMeta fireworkMeta = firework.getFireworkMeta();

    FireworkEffect.Type[] normalTypes = {
        FireworkEffect.Type.BALL,
        FireworkEffect.Type.BALL_LARGE,
        FireworkEffect.Type.BURST,
        FireworkEffect.Type.STAR
    };

    FireworkEffect.Type[] intenseTypes = {
        FireworkEffect.Type.BALL_LARGE,
        FireworkEffect.Type.BURST,
        FireworkEffect.Type.STAR,
        FireworkEffect.Type.CREEPER
    };

    FireworkEffect.Type[] types = intense ? intenseTypes : normalTypes;

    FireworkEffect effect = FireworkEffect.builder()
        .with(types[ThreadLocalRandom.current().nextInt(types.length)])
        .withColor(_pickRandomFireworkColor(), _pickRandomFireworkColor())
        .withFade(_pickRandomFireworkColor())
        .trail(true)
        .flicker(ThreadLocalRandom.current().nextBoolean())
        .build();

    fireworkMeta.addEffect(effect);

    if (intense) {
      FireworkEffect extraEffect = FireworkEffect.builder()
          .with(types[ThreadLocalRandom.current().nextInt(types.length)])
          .withColor(_pickRandomFireworkColor(), _pickRandomFireworkColor(), _pickRandomFireworkColor())
          .withFade(_pickRandomFireworkColor())
          .trail(true)
          .flicker(true)
          .build();
      fireworkMeta.addEffect(extraEffect);
    }

    fireworkMeta.setPower(0);
    firework.setFireworkMeta(fireworkMeta);

    long detonationDelay = intense ? (3L + ThreadLocalRandom.current().nextLong(3L))
        : (4L + ThreadLocalRandom.current().nextLong(4L));

    Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
      if (firework.isValid()) {
        firework.detonate();
      }
    }, detonationDelay);
  }

  private static Location _toShowLocation(Player player, double localX, double localY, double localZ) {
    Location base = _getShowAnchor(player);
    Vector forward = _getHorizontalForward(player);
    Vector right = new Vector(-forward.getZ(), 0, forward.getX());

    return base.clone()
        .add(right.multiply(localX))
        .add(0, localY, 0)
        .add(forward.multiply(localZ));
  }

  private static Location _getShowAnchor(Player player) {
    Vector forward = _getHorizontalForward(player);

    return player.getEyeLocation().clone()
        .add(forward.multiply(_SHOW_FORWARD_DISTANCE))
        .add(0, _SHOW_BASE_HEIGHT, 0);
  }

  private static Vector _getHorizontalForward(Player player) {
    Vector forward = player.getLocation().getDirection().clone().setY(0);

    if (forward.lengthSquared() < 1.0E-6) {
      return new Vector(0, 0, 1);
    }

    return forward.normalize();
  }

  private static double _spread(double value) {
    return value * _SPREAD_MULTIPLIER;
  }

  private static long _scaledTicks(long baseTicks) {
    return baseTicks * _SHOW_LENGTH_MULTIPLIER;
  }

  private static double _randomSignedWithMinAbs(double maxAbs, double minAbs) {
    double abs = ThreadLocalRandom.current().nextDouble(minAbs, maxAbs);
    return ThreadLocalRandom.current().nextBoolean() ? abs : -abs;
  }

  private static int _normalizePatternAmount(int amount, boolean intense) {
    int capped = Math.max(1, amount);
    int max = intense ? _MAX_SAFE_HEAVY_PATTERN_AMOUNT : _MAX_SAFE_PATTERN_AMOUNT;

    if (capped > max) {
      return max;
    }

    return capped;
  }

  private static int _finaleAmount(int baseAmount) {
    return Math.max(1, (int) Math.round(baseAmount * _FINAL_SEQUENCE_INTENSITY));
  }

  private static void _setWorldNight(Player player) {
    if (player == null || player.getWorld() == null) {
      return;
    }

    player.getWorld().setTime(13000L);
  }

  private static void _setWorldDay(Player player) {
    if (player == null || player.getWorld() == null) {
      return;
    }

    player.getWorld().setTime(1000L);
  }

  private enum SequenceBeat {
    HORIZON_FANFARE,
    SKY_CRUCIBLE,
    HELIX_ASCENT,
    EMBER_SWELL,
    TWIN_HALO,
    SKYFALL_CASCADE,
    STARFURY_PULSE,
    ASCENSION_STORM
  }

  private static Color _pickRandomFireworkColor() {
    Color[] palette = {
        Color.RED,
        Color.ORANGE,
        Color.YELLOW,
        Color.LIME,
        Color.AQUA,
        Color.BLUE,
        Color.PURPLE,
        Color.FUCHSIA,
        Color.WHITE
    };

    return palette[ThreadLocalRandom.current().nextInt(palette.length)];
  }
}
