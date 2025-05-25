package net.sourceforge.pmd.lang.javadoc.ast;

/*
    Adapted from the lexer used by IntelliJ Community.
    This lexer simplifies the treatment of tag values (we lex them as comment data),
    whereas the original one has special tokens for eg "#", "(", ")", ",", etc., and
    treated the @param tag specially.
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

  // when true we're sensitive to "}" as they would close the tag
  private boolean inInlineTag;
  // depth of paired braces (used only in {@code} and {@literal})
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

  private void enterLineHead() {
     if (lookahead(' ') || lookahead('\f') || lookahead('\t')) {
         int curstate = yystate();
        savedState = curstate == CODE_TAG_SPACE ? INSIDE_CODE_TAG : curstate;
        yybegin(LINE_HEAD);
     } else if (lookahead('@')) {
         yybegin(COMMENT_DATA_START);
     }
  }

  private void exitLineHead() {
      if (lookahead('@') && Character.isAlphabetic(fetchChar(1))) { // start of a block tag
         yybegin(COMMENT_DATA_START);
      } else {
         yybegin(savedState);
      }
  }

  private void setInline() {
      inInlineTag = true;
  }

  private boolean isInline() {
      boolean isInline = inInlineTag;
      inInlineTag = false;
      return isInline;
  }

  public void goTo(int offset) {
    zzCurrentPos = zzMarkedPos = zzStartRead = offset;
    zzAtEOF = false;
  }
%}

%state COMMENT_DATA_START
%state COMMENT_DATA
%state BLOCK_TAG_DOC_SPACE
%state INLINE_TAG_DOC_SPACE
%state INLINE_TAG_NAME
%state INSIDE_CODE_TAG
%state CODE_TAG_SPACE

%state LINE_HEAD

%state IN_HTML
%state IN_HTML_COMMENT

%state HTML_ATTRS
%state HTML_ATTR_VAL_DQ
%state HTML_ATTR_VAL_SQ

WHITE_DOC_SPACE_CHAR=[\ \t\f]
DIGIT=[0-9]
ALPHA=[:jletter:]
IDENTIFIER={ALPHA}({ALPHA}|{DIGIT}|[":.-"])*
TAG_IDENTIFIER=[^\ \t\f\n\r]+
INLINE_TAG_IDENTIFIER=[^\ \t\f\n\r\}]+
LINE_TERM=(\n | \r\n)

HTML_TAG_NAME=({ALPHA}|"_"|":")({ALPHA}|{DIGIT}|"_"|":"|"."|"-")*
HTML_ATTR_NAME=([^ \n\r\t\f\"\'<>/=])+

%%
// TODO HTML comments

<YYINITIAL> "/**" { yybegin(COMMENT_DATA_START); return JavadocTokenType.COMMENT_START; }

<COMMENT_DATA, COMMENT_DATA_START> ("</") {
    yybegin(IN_HTML);
    return JavadocTokenType.HTML_LCLOSE;
}
<COMMENT_DATA, COMMENT_DATA_START> "<!--" { yybegin(IN_HTML_COMMENT); return JavadocTokenType.HTML_COMMENT_START; }
<IN_HTML_COMMENT> "-->" { yybegin(COMMENT_DATA); return JavadocTokenType.HTML_COMMENT_END; }
<IN_HTML_COMMENT> "--" { yybegin(COMMENT_DATA); return JavadocTokenType.BAD_CHAR; }
<IN_HTML_COMMENT> . { return JavadocTokenType.HTML_COMMENT_CONTENT; }


<COMMENT_DATA, COMMENT_DATA_START> [\<] {
    yybegin(IN_HTML);
    return JavadocTokenType.HTML_LT;
}
<IN_HTML> {HTML_TAG_NAME} { yybegin(HTML_ATTRS); return JavadocTokenType.HTML_IDENT; }
<IN_HTML, HTML_ATTRS> [\>] { yybegin(COMMENT_DATA); return JavadocTokenType.HTML_GT; }
<IN_HTML, HTML_ATTRS> "/>" { yybegin(COMMENT_DATA); return JavadocTokenType.HTML_RCLOSE; }

<HTML_ATTRS> {HTML_ATTR_NAME} { return JavadocTokenType.HTML_IDENT; }
<HTML_ATTRS> [=]  { return JavadocTokenType.HTML_EQ; }
<HTML_ATTRS> [\"] { yybegin(HTML_ATTR_VAL_DQ); return JavadocTokenType.HTML_DQUOTE; }
<HTML_ATTRS> [\'] { yybegin(HTML_ATTR_VAL_SQ); return JavadocTokenType.HTML_SQUOTE; }
<HTML_ATTRS> {WHITE_DOC_SPACE_CHAR}+ { return JavadocTokenType.WHITESPACE; }

<HTML_ATTR_VAL_DQ> [\"] { yybegin(HTML_ATTRS); return JavadocTokenType.HTML_DQUOTE; }
<HTML_ATTR_VAL_SQ> [\'] { yybegin(HTML_ATTRS); return JavadocTokenType.HTML_SQUOTE; }
<HTML_ATTR_VAL_DQ, HTML_ATTR_VAL_SQ> . { return JavadocTokenType.HTML_ATTR_VAL; }

<COMMENT_DATA_START, COMMENT_DATA> "{"
{
  boolean isTagStart = lookahead('@');
  yybegin(isTagStart ? INLINE_TAG_NAME : COMMENT_DATA);
  return isTagStart ? JavadocTokenType.INLINE_TAG_START : JavadocTokenType.COMMENT_DATA;
}
// brace balancing inside code and literal envs
<INSIDE_CODE_TAG> "{" { braces++; return JavadocTokenType.COMMENT_DATA; }
<INSIDE_CODE_TAG> "}"
      {
          if (--braces >= 0) { return JavadocTokenType.COMMENT_DATA; }
          else { yybegin(COMMENT_DATA); braces = 0; return JavadocTokenType.INLINE_TAG_END; }
      }

<INLINE_TAG_NAME> ("@code" | "@literal") { yybegin(CODE_TAG_SPACE); return JavadocTokenType.TAG_NAME; }
<INLINE_TAG_NAME> "@"{INLINE_TAG_IDENTIFIER} { setInline(); yybegin(INLINE_TAG_DOC_SPACE); return JavadocTokenType.TAG_NAME; }
<COMMENT_DATA_START, LINE_HEAD> "@"{TAG_IDENTIFIER} { yybegin(BLOCK_TAG_DOC_SPACE); return JavadocTokenType.TAG_NAME; }

// closing }
<COMMENT_DATA_START, COMMENT_DATA, INLINE_TAG_DOC_SPACE, CODE_TAG_SPACE>
        "}" {
          if (isInline()) {
            yybegin(COMMENT_DATA);
            return JavadocTokenType.INLINE_TAG_END;
          } else {
            return JavadocTokenType.COMMENT_DATA;
          }
      }

// {@literal &identifier; } or {@literal &#digits; } or {@literal &#xhex-digits; }
<COMMENT_DATA> "&" {IDENTIFIER} ";" {return JavadocTokenType.HTML_ENTITY;}

// whitespace

<INLINE_TAG_DOC_SPACE, BLOCK_TAG_DOC_SPACE> {WHITE_DOC_SPACE_CHAR}+ { yybegin(COMMENT_DATA); return JavadocTokenType.WHITESPACE; }

<CODE_TAG_SPACE> {WHITE_DOC_SPACE_CHAR}+ { yybegin(INSIDE_CODE_TAG); return JavadocTokenType.WHITESPACE; }

<COMMENT_DATA_START> {WHITE_DOC_SPACE_CHAR}+ { return JavadocTokenType.WHITESPACE; }

<COMMENT_DATA> {WHITE_DOC_SPACE_CHAR}+ { return JavadocTokenType.COMMENT_DATA; }

<COMMENT_DATA_START, COMMENT_DATA> .   { yybegin(COMMENT_DATA); return JavadocTokenType.COMMENT_DATA; }
<INSIDE_CODE_TAG> .                    { return JavadocTokenType.COMMENT_DATA; }

<LINE_HEAD> {WHITE_DOC_SPACE_CHAR}+    { exitLineHead(); return JavadocTokenType.WHITESPACE; }

// line termination (all states)
// the part / [^/]  avoids matching "*/"
{LINE_TERM} {WHITE_DOC_SPACE_CHAR}* "*" / [^/] { enterLineHead(); return JavadocTokenType.LINE_BREAK; }
{LINE_TERM}                                    { enterLineHead(); return JavadocTokenType.LINE_BREAK; }

"*/" { return JavadocTokenType.COMMENT_END; }
[^]  { return JavadocTokenType.BAD_CHAR; }
