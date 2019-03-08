package net.sourceforge.pmd.lang.xpath.annotation;

import java.lang.annotation.ElementType;
import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierDefault;

@Nonnull
@TypeQualifierDefault( {ElementType.METHOD, ElementType.PARAMETER})
public @interface NonnullApi {
}