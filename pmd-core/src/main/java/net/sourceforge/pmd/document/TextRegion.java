/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.document;

/** A generic range of text in a document. */
public interface TextRegion {

    /**
     * Returns a view of this region as an (offset,length) 2-tuple.
     *
     * @param document Containing document
     */
    RegionByOffset toOffset(Document document);


    /**
     * Returns a view of this region as a (begin,end)x(line,column) 4-tuple.
     *
     * @param document Containing document
     */
    RegionByLine toLine(Document document);


    static RegionByLine newRegionByLine(final int beginLine, final int beginColumn, final int endLine, final int endColumn) {
        return new RegionByLineImp(beginLine, beginColumn, endLine, endColumn);
    }


    static RegionByOffset newRegionByOffset(final int offset, final int length) {
        return new RegionByOffsetImp(offset, length);
    }


    /**
     * Represents a region in a {@link Document} with the tuple (beginLine, endLine, beginColumn, endColumn).
     *
     * <p>Lines and columns in PMD are 1-based.
     */
    interface RegionByLine extends TextRegion {

        int getBeginLine();


        int getEndLine();


        int getBeginColumn();


        int getEndColumn();


        @Override
        default RegionByLine toLine(Document document) {
            return this;
        }


        @Override
        default RegionByOffset toOffset(Document document) {
            return document.mapToOffset(this);
        }
    }

    /**
     * Represents a region in a {@link Document} with the tuple (offset, length).
     */
    interface RegionByOffset extends TextRegion {

        int getOffset();


        int getLength();


        default int getOffsetAfterEnding() {
            return getOffset() + getLength();
        }

        @Override
        default RegionByLine toLine(Document document) {
            return document.mapToLine(this);
        }


        @Override
        default RegionByOffset toOffset(Document document) {
            return this;
        }
    }
}
