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

    void setLineBreak() {
        switch (yystate()) {
            case INLINE_TAG:
              yybegin(INLINE_TAG_START);
              break;
            case COMMENT_DATA:
              yybegin(COMMENT_DATA_START);
              break;

        }
    }
%}


%state COMMENT_DATA_START, COMMENT_DATA
%state INLINE_TAG_NAME
%state COMMENT_DATA_START, INLINE_TAG_START
%state IN_HTML, IN_HTML_COMMENT
%state HTML_ATTRS, HTML_ATTR_VAL
%state HTML_ATTR_VAL_DQ, HTML_ATTR_VAL_SQ, INLINE_TAG

HEX_DIGIT=              [0-9a-fA-F]
BLOCK_TAG_ID=           "@"[^\ \t\f\n\r]+
INLINE_TAG_ID=          "@"[^\ \t\f\n\r\{\}]+

// whitespace
WS_CHAR=                [ \t\f]
LEADER=                 {WS_CHAR}* "*"

ENTITY=                 "&" [:jletter:]+ ";"        // named
                      | "&#" [:digit:]+ ";"         // decimal
                      | "&#" [xX] {HEX_DIGIT}+ ";"  // hex

// Distinction is semantic
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
<COMMENT_DATA_START,
 COMMENT_DATA>          "{"                    {                             return JavadocTokenType.COMMENT_DATA; }

// brace balancing inside inline tags

<INLINE_TAG_START>      "{"                    { yybegin(INLINE_TAG); braces++; return JavadocTokenType.COMMENT_DATA; }
<INLINE_TAG>            "{"                    {                      braces++; return JavadocTokenType.COMMENT_DATA; }
<INLINE_TAG,
 INLINE_TAG_START>      "}"                    { if (--braces >= 0)             return JavadocTokenType.COMMENT_DATA;
                                                 else { yybegin(COMMENT_DATA);  return JavadocTokenType.INLINE_TAG_END; }
                                               }


<INLINE_TAG_NAME>       {INLINE_TAG_ID}        { yybegin(INLINE_TAG_START); braces = 0; return JavadocTokenType.TAG_NAME; }
<COMMENT_DATA_START,
 INLINE_TAG_START>      {BLOCK_TAG_ID}         { yybegin(COMMENT_DATA_START);           return JavadocTokenType.TAG_NAME; }


// this is for whitespace either trailing a line or the whole input

                        {WS_CHAR}+
                            / (\R | "*/")      {                             return JavadocTokenType.WHITESPACE;   }


<COMMENT_DATA_START,
 INLINE_TAG_START,
 HTML_ATTRS,
 HTML_ATTR_VAL>         {WS_CHAR}+             {                             return JavadocTokenType.WHITESPACE;   }

<COMMENT_DATA_START>    {ENTITY}               { yybegin(COMMENT_DATA);      return JavadocTokenType.CHARACTER_REFERENCE;  }
<COMMENT_DATA,
 HTML_ATTR_VAL_DQ,
 HTML_ATTR_VAL_SQ>      {ENTITY}               {                             return JavadocTokenType.CHARACTER_REFERENCE;  }

<COMMENT_DATA_START>    .                      { yybegin(COMMENT_DATA);      return JavadocTokenType.COMMENT_DATA; }
<INLINE_TAG_START>      .                      { yybegin(INLINE_TAG);        return JavadocTokenType.COMMENT_DATA; }
<COMMENT_DATA,
 INLINE_TAG>            .                      {                             return JavadocTokenType.COMMENT_DATA; }



// ALL STATES

// Line termination
// Trailing whitespace

// start of a block tag, overrides any previous state

                        \R {LEADER}?
                           / {WS_CHAR}*
                             {BLOCK_TAG_ID}    { yybegin(COMMENT_DATA_START); return JavadocTokenType.LINE_BREAK; }


                        \R {LEADER}
                           / [^/]              { setLineBreak();              return JavadocTokenType.LINE_BREAK; }

                        \R                     { setLineBreak();              return JavadocTokenType.LINE_BREAK; }


                        "*/"                   {                              return JavadocTokenType.COMMENT_END; }
                        [^]                    {                              return JavadocTokenType.BAD_CHAR;    }
