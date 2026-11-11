package com.carloship.craft.sdk.server.leveling;

public enum XpCurvePrestige {

    LINEAR {
        @Override
        public double xpForLevel(int level, int prestige) {
            return 100 + (level * 50);
        }
    },

    EXPONENTIAL {
        @Override
        public double xpForLevel(int level, int prestige) {
            return 100 * Math.pow(1.25, level);
        }
    },

    MMO {
        @Override
        public double xpForLevel(int level, int prestige) {
            double base = 150 * Math.pow(level, 1.45);
            return base * (1 + prestige * 0.25);
        }
    };

    public abstract double xpForLevel(int level, int prestige);
}
