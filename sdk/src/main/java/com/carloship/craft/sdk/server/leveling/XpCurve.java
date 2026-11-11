package com.carloship.craft.sdk.server.leveling;

public enum XpCurve {

    LINEAR {
        @Override
        public double xpForLevel(int level) {
            return 100 + (level * 50);
        }
    },

    EXPONENTIAL {
        @Override
        public double xpForLevel(int level) {
            return 100 * Math.pow(1.25, level);
        }
    },

    MMO {
        @Override
        public double xpForLevel(int level) {
            return 150 * Math.pow(level, 1.45);
        }
    };

    public abstract double xpForLevel(int level);
}
