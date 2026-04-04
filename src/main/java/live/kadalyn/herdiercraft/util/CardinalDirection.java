package live.kadalyn.herdiercraft.util;

public enum CardinalDirection {
    NW,
    N,
    NE,
    W,
    C,
    E,
    SW,
    S,
    SE;

    public static CardinalDirection fromUnitXZ(double x, double z) {
        if (z < 0.33) {
            if (x < 0.33) return NW;
            if (x < 0.67) return N;
            return NE;
        }
        else if (z < 0.66) {
            if (x < 0.33) return W;
            if (x < 0.67) return C;
            return E;
        }
        else {
            if (x < 0.33) return SW;
            if (x < 0.67) return S;
            return SE;
        }
    }
}
