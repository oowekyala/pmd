/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.internal.dataflow;

/**
 * A value model models the statically known state of a value in the
 * program. It must support a lattice structure, ie, two models of the
 * same type can always be joined into a new model that represents the
 * union of the state represented by both models.
 *
 * @param <T> Self type
 */
public interface ValueModel<T extends ValueModel<T>> {

    /**
     * Produce a model that represents the union of both sets of facts.
     *
     * @param other Another model
     */
    T join(T other);

    /**
     * Model for any primitive integer value (excluding boolean).
     */
    class IntegerModel implements ValueModel<IntegerModel> {
        private final long minInclusive;
        private final long maxInclusive;
        private final BitWidth bitwidth;

        private IntegerModel(long minInclusive, long maxInclusive, BitWidth bitwidth) {
            this.minInclusive = minInclusive;
            this.maxInclusive = maxInclusive;
            this.bitwidth = bitwidth;
        }

        @Override
        public IntegerModel join(IntegerModel other) {
            // todo implicit widening is not expected, there might be truncation happening
            BitWidth widest = bitwidth.compareTo(other.bitwidth) < 0 ? other.bitwidth : bitwidth;
            long min = Math.min(this.minInclusive, other.minInclusive);
            long max = Math.max(this.maxInclusive, other.maxInclusive);
            return new IntegerModel(min, max, widest);
        }

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


            BitWidth(int width, long minInclusive, long maxInclusive) {
                this.width = width;
                this.minInclusive = minInclusive;
                this.maxInclusive = maxInclusive;
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

}
