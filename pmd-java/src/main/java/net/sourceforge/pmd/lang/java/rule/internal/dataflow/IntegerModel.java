/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.internal.dataflow;

/**
 * Model for any primitive integer value (excluding boolean).
 */
public class IntegerModel implements ValueModel<IntegerModel> {
    private final long minInclusive;
    private final long maxInclusive;
    private final BitWidth bitwidth;
    private final boolean isBottom;

    /** Constructor for a regular range. */
    private IntegerModel(long minInclusive, long maxInclusive, BitWidth bitwidth) {
        this.minInclusive = minInclusive;
        this.maxInclusive = maxInclusive;
        this.bitwidth = bitwidth;
        this.isBottom = false;
    }

    /** Constructor for the bottom value. */
    private IntegerModel(BitWidth bitwidth) {
        this.minInclusive = 0;
        this.maxInclusive = 0;
        this.bitwidth = bitwidth;
        this.isBottom = true;
    }

    @Override
    public boolean isTop() {
        return bitwidth.minInclusive == minInclusive
               && bitwidth.maxInclusive == maxInclusive;
    }

    @Override
    public boolean isBottom() {
        return isBottom;
    }

    @Override
    public IntegerModel join(IntegerModel other) {
        if (this.isBottom) {
            return other;
        } else if (other.isBottom()) {
            return this;
        }
        // todo implicit widening is not expected, there might be truncation happening
        BitWidth widest = bitwidth.compareTo(other.bitwidth) < 0 ? other.bitwidth : bitwidth;
        long min = Math.min(this.minInclusive, other.minInclusive);
        long max = Math.max(this.maxInclusive, other.maxInclusive);
        return new IntegerModel(min, max, widest);
    }

    /**
     * Bitwidth enum to create integer range models with a specific
     * bitwidth.
     */
    public enum BitWidth {
        /** byte */
        INT8(8, Byte.MIN_VALUE, Byte.MAX_VALUE),
        /** char and short */
        INT16(16, Short.MIN_VALUE, Short.MAX_VALUE),
        /** int */
        INT32(32, Integer.MIN_VALUE, Integer.MAX_VALUE),
        /** long */
        INT64(64, Long.MIN_VALUE, Long.MAX_VALUE);

        private final int width;
        private final long minInclusive;
        private final long maxInclusive;
        private final IntegerModel bottom;


        BitWidth(int width, long minInclusive, long maxInclusive) {
            this.width = width;
            this.minInclusive = minInclusive;
            this.maxInclusive = maxInclusive;
            this.bottom = new IntegerModel(this);
        }

        public boolean contains(long value) {
            return minInclusive <= value && value <= maxInclusive;
        }

        public IntegerModel unknown() {
            return new IntegerModel(minInclusive, maxInclusive, this);
        }

        public IntegerModel point(long value) {
            assert contains(value);
            return new IntegerModel(value, value, this);
        }

        public IntegerModel range(long minInclusive, long maxInclusive) {
            assert contains(minInclusive) && contains(maxInclusive);
            assert minInclusive <= maxInclusive;
            return new IntegerModel(minInclusive, maxInclusive, this);
        }
    }
}
