package net.sourceforge.pmd.lang.javadoc.ast;

/*
    Adapted from the lexer used by IntelliJ Community.
    This lexer simplifies the treatment of tag values (we lex them as comment data),
    whereas the original one has special tokens for eg "#", "(", ")", ",", etc., and
    treats @param, {@code}, {@literal}  specially.
    Conversely, it has more sophisticated treatment of line breaks and HTML data.
 */

%%


%class JavadocFlexer
%unicode
%function advance
%type JavadocTokenType

%{


  public boolean lookahead(char c) {
    return lookahead(c, 0);
  }

  // depth of paired braces (used in inline tags)
  private int braces;
  // saved state after a line break
  private int savedState;

  public boolean lookahead(char c, int distance) {
    if (zzMarkedPos + distance >= zzBuffer.length) return false;
    return zzBuffer[zzMarkedPos + distance] == c;
  }

  public char fetchChar(int distance) {
    if (zzMarkedPos + distance >= zzBuffer.length) return '\0';
    return zzBuffer[zzMarkedPos + distance];
  }

  public void goTo(int offset) {
    zzCurrentPos = zzMarkedPos = zzStartRead = offset;
    zzAtEOF = false;
  }
%}

%state COMMENT_DATA_START
%state COMMENT_DATA

%state INLINE_TAG_NAME

%state BLOCK_TAG_SPACE
%state INLINE_TAG_SPACE

%state INSIDE_INLINE_TAG

%state LINE_HEAD

%state IN_HTML
%state IN_HTML_COMMENT

%state HTML_ATTRS
%state HTML_ATTR_VAL
%state HTML_ATTR_VAL_DQ
%state HTML_ATTR_VAL_SQ

WHITESPACE_CHAR=        [\ \t\f]
DIGIT=                  [0-9]
HEX_DIGIT=              [0-9a-fA-F]
ALPHA=                  [:jletter:]
BLOCK_TAG_ID=           "@"[^\ \t\f\n\r]+
INLINE_TAG_ID=          "@"[^\ \t\f\n\r\{\}]+
// Uses the same convention as java.util.regex.Pattern#line-terminators
LINE_TERM=              (\r\n | [\r\n\u0085\u2028\u2029])

ENTITY =                "&" {ALPHA}+ ";"
                      | "&#" {DIGIT}+ ";"
                      | "&#" [xX] {HEX_DIGIT}+ ";"

HTML_TAG_NAME=          ({ALPHA}|"_"|":")({ALPHA}|{DIGIT}|"_"|":"|"."|"-")*
HTML_ATTR_NAME=         ([^ \n\r\t\f\"\'<>/=])+

%%

<YYINITIAL>             "/**"                  { yybegin(COMMENT_DATA_START); return JavadocTokenType.COMMENT_START; }

<COMMENT_DATA,
 COMMENT_DATA_START>    "</" / {HTML_TAG_NAME} { yybegin(IN_HTML);           return JavadocTokenType.HTML_LCLOSE; }

<COMMENT_DATA,
 COMMENT_DATA_START>    "<!--"                 { yybegin(IN_HTML_COMMENT);   return JavadocTokenType.HTML_COMMENT_START; }
<IN_HTML_COMMENT>       "-->"                  { yybegin(COMMENT_DATA);      return JavadocTokenType.HTML_COMMENT_END; }
<IN_HTML_COMMENT>       "--"                   { yybegin(COMMENT_DATA);      return JavadocTokenType.BAD_CHAR; }
<IN_HTML_COMMENT>       .                      {                             return JavadocTokenType.HTML_COMMENT_CONTENT; }


<COMMENT_DATA,
 COMMENT_DATA_START>    [<] / {HTML_TAG_NAME}  { yybegin(IN_HTML);           return JavadocTokenType.HTML_LT; }
<IN_HTML>               {HTML_TAG_NAME}        { yybegin(HTML_ATTRS);        return JavadocTokenType.HTML_IDENT; }
<IN_HTML,
 HTML_ATTRS>            [\>]                   { yybegin(COMMENT_DATA);      return JavadocTokenType.HTML_GT; }
<IN_HTML,
 HTML_ATTRS>            "/>"                   { yybegin(COMMENT_DATA);      return JavadocTokenType.HTML_RCLOSE; }

<HTML_ATTRS>            {HTML_ATTR_NAME}       {                             return JavadocTokenType.HTML_IDENT; }
<HTML_ATTRS>            [=]                    { yybegin(HTML_ATTR_VAL);     return JavadocTokenType.HTML_EQ; }
<HTML_ATTR_VAL>         [\"]                   { yybegin(HTML_ATTR_VAL_DQ);  return JavadocTokenType.HTML_DQUOTE; }
<HTML_ATTR_VAL>         [\']                   { yybegin(HTML_ATTR_VAL_SQ);  return JavadocTokenType.HTML_SQUOTE; }
// unquoted
<HTML_ATTR_VAL>         {HTML_ATTR_NAME}       { yybegin(HTML_ATTRS);        return JavadocTokenType.HTML_ATTR_VAL; }

<HTML_ATTR_VAL_DQ>      [\"]                   { yybegin(HTML_ATTRS);        return JavadocTokenType.HTML_DQUOTE; }
<HTML_ATTR_VAL_SQ>      [\']                   { yybegin(HTML_ATTRS);        return JavadocTokenType.HTML_SQUOTE; }
<HTML_ATTR_VAL_DQ>      [^\"&]+                {                             return JavadocTokenType.HTML_ATTR_VAL; }
<HTML_ATTR_VAL_SQ>      [^\'&]+                {                             return JavadocTokenType.HTML_ATTR_VAL; }

<COMMENT_DATA_START,
 COMMENT_DATA>          "{" / {INLINE_TAG_ID}  { yybegin(INLINE_TAG_NAME);   return JavadocTokenType.INLINE_TAG_START; }

// brace balancing inside inline tags

<INSIDE_INLINE_TAG>     "{"                    {                             braces++; return JavadocTokenType.COMMENT_DATA; }
<INLINE_TAG_SPACE>      "{"                    { yybegin(INSIDE_INLINE_TAG); braces++; return JavadocTokenType.COMMENT_DATA; }
<INSIDE_INLINE_TAG,
 INLINE_TAG_SPACE>      "}"
    {
      if (--braces >= 0) return JavadocTokenType.COMMENT_DATA;
      else { yybegin(COMMENT_DATA); return JavadocTokenType.INLINE_TAG_END; }
    }

<INLINE_TAG_NAME>       {INLINE_TAG_ID}        { yybegin(INLINE_TAG_SPACE); braces = 0; return JavadocTokenType.TAG_NAME; }
<COMMENT_DATA_START>    {BLOCK_TAG_ID}         { yybegin(BLOCK_TAG_SPACE);              return JavadocTokenType.TAG_NAME; }


// whitespace

<BLOCK_TAG_SPACE>       {WHITESPACE_CHAR}+     { yybegin(COMMENT_DATA);      return JavadocTokenType.WHITESPACE;   }
<INLINE_TAG_SPACE>      {WHITESPACE_CHAR}+     { yybegin(INSIDE_INLINE_TAG); return JavadocTokenType.WHITESPACE;   }

<INLINE_TAG_SPACE>      {WHITESPACE_CHAR}+     { yybegin(INSIDE_CODE_TAG);   return JavadocTokenType.WHITESPACE;   }
<COMMENT_DATA_START,
 HTML_ATTRS,
 HTML_ATTR_VAL>         {WHITESPACE_CHAR}+     {                             return JavadocTokenType.WHITESPACE;   }
<COMMENT_DATA>          {WHITESPACE_CHAR}+     {                             return JavadocTokenType.COMMENT_DATA; }

<COMMENT_DATA_START>    {ENTITY}               { yybegin(COMMENT_DATA);      return JavadocTokenType.HTML_ENTITY;  }
<COMMENT_DATA,
 HTML_ATTR_VAL_DQ,
 HTML_ATTR_VAL_SQ>      {ENTITY}               {                             return JavadocTokenType.HTML_ENTITY;  }
<COMMENT_DATA_START>    [^&<>]                 { yybegin(COMMENT_DATA);      return JavadocTokenType.COMMENT_DATA; }
<COMMENT_DATA>          [^&<>]                 {                             return JavadocTokenType.COMMENT_DATA; }
<INSIDE_INLINE_TAG>     .                      {                             return JavadocTokenType.COMMENT_DATA; }

// ALL STATES

// Trailing spaces before the end marker, or a line break
// If the comment is one one line, eg /** Foo */, that space shouldn't be lexed as COMMENT_DATA

                        {WHITESPACE_CHAR}+ / ({LINE_TERM} | "*/")      { return JavadocTokenType.WHITESPACE; }

// Line termination

// start of a block tag
                        {LINE_TERM} ({WHITESPACE_CHAR}* "*")?
                            / {WHITESPACE_CHAR}* {BLOCK_TAG_ID}        { yybegin(COMMENT_DATA_START); return JavadocTokenType.LINE_BREAK; }

// the part / [^/]  avoids matching "*/"
                        {LINE_TERM} {WHITESPACE_CHAR}* "*"
                            / [^/]                                     { if (yystate() == INLINE_TAG_SPACE) yybegin(INSIDE_INLINE_TAG); return JavadocTokenType.LINE_BREAK; }
                        {LINE_TERM}                                    { if (yystate() == INLINE_TAG_SPACE) yybegin(INSIDE_INLINE_TAG); return JavadocTokenType.LINE_BREAK; }

                        "*/"                                           { return JavadocTokenType.COMMENT_END; }
                        [^]                                            { return JavadocTokenType.BAD_CHAR; }
