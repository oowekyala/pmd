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

  // depth of paired braces (used in inline tags)
  private int braces;

%}

%debug

%state COMMENT_DATA_START, COMMENT_DATA
%state INLINE_TAG_NAME
%state BLOCK_TAG_SPACE, INLINE_TAG_SPACE
%state IN_HTML, IN_HTML_COMMENT
%state HTML_ATTRS, HTML_ATTR_VAL
%state HTML_ATTR_VAL_DQ, HTML_ATTR_VAL_SQ, INSIDE_INLINE_TAG

HEX_DIGIT=              [0-9a-fA-F]
ALPHA=                  [:jletter:]
BLOCK_TAG_ID=           "@"[^\ \t\f\n\r]+
INLINE_TAG_ID=          "@"[^\ \t\f\n\r\{\}]+

// whitespace
WS_CHAR=                [ \t\f]
LEADER=                 {WS_CHAR}* "*"

TEXT_CHAR=              [^&<>{*\s\R]

ENTITY=                 "&" [:jletter:]+ ";"
                      | "&#" [:digit:]+ ";"
                      | "&#" [xX] {HEX_DIGIT}+ ";"

HTML_TAG_NAME=          [^\s\"\'<>/=]+
HTML_ATTR_NAME=         [^\s\"\'<>/=]+
UNQUOTED_ATTR_VALUE=    [^\s\"\'<>/=]+

%%

<YYINITIAL>             "/**"                  { yybegin(COMMENT_DATA_START); return JavadocTokenType.COMMENT_START; }

<COMMENT_DATA,
 COMMENT_DATA_START>    "</" / {HTML_TAG_NAME} { yybegin(IN_HTML);           return JavadocTokenType.HTML_LCLOSE; }

<COMMENT_DATA,
 COMMENT_DATA_START>    "<!--"                 { yybegin(IN_HTML_COMMENT);   return JavadocTokenType.HTML_COMMENT_START; }
<IN_HTML_COMMENT>       "-->"                  { yybegin(COMMENT_DATA);      return JavadocTokenType.HTML_COMMENT_END; }
<IN_HTML_COMMENT>       "<!--" | "--!>"        { yybegin(COMMENT_DATA);      return JavadocTokenType.BAD_CHAR; }
<IN_HTML_COMMENT>       .                      {                             return JavadocTokenType.HTML_COMMENT_CONTENT; }

<COMMENT_DATA,
 COMMENT_DATA_START>    "<" / {HTML_TAG_NAME}  { yybegin(IN_HTML);           return JavadocTokenType.HTML_LT; }
<IN_HTML>               {HTML_TAG_NAME}        { yybegin(HTML_ATTRS);        return JavadocTokenType.HTML_IDENT; }
<IN_HTML,
 HTML_ATTRS>            [\>]                   { yybegin(COMMENT_DATA);      return JavadocTokenType.HTML_GT; }
<IN_HTML,
 HTML_ATTRS>            "/>"                   { yybegin(COMMENT_DATA);      return JavadocTokenType.HTML_RCLOSE; }

<HTML_ATTRS>            {HTML_ATTR_NAME}       {                             return JavadocTokenType.HTML_IDENT; }
<HTML_ATTRS>            "="                    { yybegin(HTML_ATTR_VAL);     return JavadocTokenType.HTML_EQ; }
<HTML_ATTR_VAL>         [\"]                   { yybegin(HTML_ATTR_VAL_DQ);  return JavadocTokenType.HTML_DQUOTE; }
<HTML_ATTR_VAL>         [\']                   { yybegin(HTML_ATTR_VAL_SQ);  return JavadocTokenType.HTML_SQUOTE; }
<HTML_ATTR_VAL>         {UNQUOTED_ATTR_VALUE}  { yybegin(HTML_ATTRS);        return JavadocTokenType.HTML_ATTR_VAL; }

<HTML_ATTR_VAL_DQ>      [\"]                   { yybegin(HTML_ATTRS);        return JavadocTokenType.HTML_DQUOTE; }
<HTML_ATTR_VAL_SQ>      [\']                   { yybegin(HTML_ATTRS);        return JavadocTokenType.HTML_SQUOTE; }
<HTML_ATTR_VAL_DQ>      [^\"&\R]+              {                             return JavadocTokenType.HTML_ATTR_VAL; }
<HTML_ATTR_VAL_SQ>      [^\'&\R]+              {                             return JavadocTokenType.HTML_ATTR_VAL; }

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


// whitespace and regular text

                        ^{WS_CHAR}+            { return JavadocTokenType.LEADER; }


<BLOCK_TAG_SPACE,
 COMMENT_DATA_START>    {WS_CHAR}+             { yybegin(COMMENT_DATA);      return JavadocTokenType.WHITESPACE;   }
<INLINE_TAG_SPACE>      {WS_CHAR}+             { yybegin(INSIDE_INLINE_TAG); return JavadocTokenType.WHITESPACE;   }

<HTML_ATTRS,
 HTML_ATTR_VAL>         {WS_CHAR}+             {                             return JavadocTokenType.WHITESPACE;   }

<COMMENT_DATA>          {WS_CHAR}+             {                             return JavadocTokenType.COMMENT_DATA; }

<COMMENT_DATA_START>    {ENTITY}               { yybegin(COMMENT_DATA);      return JavadocTokenType.HTML_ENTITY;  }
<COMMENT_DATA,
 HTML_ATTR_VAL_DQ,
 HTML_ATTR_VAL_SQ>      {ENTITY}               {                             return JavadocTokenType.HTML_ENTITY;  }
<COMMENT_DATA_START>    {TEXT_CHAR}+           { yybegin(COMMENT_DATA);      return JavadocTokenType.COMMENT_DATA; }
<COMMENT_DATA>          {TEXT_CHAR}+           {                             return JavadocTokenType.COMMENT_DATA; }
<INSIDE_INLINE_TAG>     [^\R}{*]               {                             return JavadocTokenType.COMMENT_DATA; }



// ALL STATES

// Line termination

// those states preserve leading whitespace, after the leader,

<HTML_ATTR_VAL_DQ,
 HTML_ATTR_VAL_SQ,
 INSIDE_INLINE_TAG>     ^{LEADER}                      {                              return JavadocTokenType.LEADER; }

// start of a block tag, overrides any previous state

                        ^{LEADER} {WS_CHAR}*
                            / {BLOCK_TAG_ID}           { yybegin(COMMENT_DATA_START); return JavadocTokenType.LEADER; }
                        ^{LEADER} {WS_CHAR}*
                            / [^/]                     {                              return JavadocTokenType.LEADER; }
                        ^{WS_CHAR}+                    {                              return JavadocTokenType.LEADER; }


// Trailing spaces before the end marker, or a line break
// If the comment is one one line, eg /** Foo */, that space shouldn't be lexed as COMMENT_DATA

                        {WS_CHAR}+
                            / (\R | "*/")              { return JavadocTokenType.WHITESPACE; }

                        \R                             { return JavadocTokenType.LINE_BREAK; }

                        "*/"                           { return JavadocTokenType.COMMENT_END; }
                        [^]                            { return JavadocTokenType.BAD_CHAR; }
