package net.sourceforge.pmd.lang.javadoc.ast;


%%


%class JavadocLexer
%unicode
%function advance
%type JavadocTokenType

%{

  private int braces;

  public JavadocLexer() {}

  public boolean lookahead(char c) {
    if (zzMarkedPos >= zzBuffer.length) return false;
    return zzBuffer[zzMarkedPos] == c;
  }

  public void goTo(int offset) {
    zzCurrentPos = zzMarkedPos = zzStartRead = offset;
    zzAtEOF = false;
  }
%}

%state COMMENT_DATA_START
%state COMMENT_DATA
%state TAG_DOC_SPACE
%state PARAM_TAG_SPACE
%state DOC_TAG_VALUE
%state DOC_TAG_VALUE_IN_PAREN
%state DOC_TAG_VALUE_IN_LTGT
%state INLINE_TAG_NAME
%state INSIDE_CODE_TAG
%state CODE_TAG_SPACE

WHITE_DOC_SPACE_CHAR=[\ \t\f\n\r]
WHITE_DOC_SPACE_NO_LR=[\ \t\f]
DIGIT=[0-9]
ALPHA=[:jletter:]
IDENTIFIER={ALPHA}({ALPHA}|{DIGIT}|[":.-"])*
TAG_IDENTIFIER=[^\ \t\f\n\r]+
INLINE_TAG_IDENTIFIER=[^\ \t\f\n\r\}]+

%%
// TODO HTML comments

<YYINITIAL> "/**" { yybegin(COMMENT_DATA_START); return JavadocTokenType.COMMENT_START; }
<COMMENT_DATA_START> {WHITE_DOC_SPACE_CHAR}+ { return JavadocTokenType.WHITESPACE; }
<COMMENT_DATA> {WHITE_DOC_SPACE_NO_LR}+ { return JavadocTokenType.COMMENT_DATA; }
<COMMENT_DATA> [\n\r]+ {WHITE_DOC_SPACE_CHAR}* { return JavadocTokenType.WHITESPACE; }

<DOC_TAG_VALUE> {WHITE_DOC_SPACE_CHAR}+ { yybegin(COMMENT_DATA); return JavadocTokenType.WHITESPACE; }
<DOC_TAG_VALUE, DOC_TAG_VALUE_IN_PAREN> ({ALPHA}|[_0-9\."$"\[\]])+ { return JavadocTokenType.VAL_PART; }
<DOC_TAG_VALUE> [\(] { yybegin(DOC_TAG_VALUE_IN_PAREN); return JavadocTokenType.VAL_LPAREN; }
<DOC_TAG_VALUE_IN_PAREN> [\)] { yybegin(DOC_TAG_VALUE); return JavadocTokenType.VAL_RPAREN; }
<DOC_TAG_VALUE> [#] { return JavadocTokenType.VAL_HASH; }
<DOC_TAG_VALUE, DOC_TAG_VALUE_IN_PAREN> [,] { return JavadocTokenType.VAL_COMMA; }
<DOC_TAG_VALUE_IN_PAREN> {WHITE_DOC_SPACE_CHAR}+ { return JavadocTokenType.WHITESPACE; }

<INLINE_TAG_NAME, COMMENT_DATA_START> "@param" { yybegin(PARAM_TAG_SPACE); return JavadocTokenType.TAG_NAME; }
<PARAM_TAG_SPACE> {WHITE_DOC_SPACE_CHAR}+ {yybegin(DOC_TAG_VALUE); return JavadocTokenType.WHITESPACE; }
<DOC_TAG_VALUE, COMMENT_DATA, COMMENT_DATA_START> ("</") {
    yybegin(DOC_TAG_VALUE_IN_LTGT);
    return JavadocTokenType.HTML_LCLOSE;
}
<DOC_TAG_VALUE, COMMENT_DATA, COMMENT_DATA_START> [\<] {
    yybegin(DOC_TAG_VALUE_IN_LTGT);
    return JavadocTokenType.HTML_LT;
}
<DOC_TAG_VALUE_IN_LTGT> {IDENTIFIER} { return JavadocTokenType.HTML_IDENT; }
<DOC_TAG_VALUE_IN_LTGT> [\>] { yybegin(COMMENT_DATA); return JavadocTokenType.HTML_GT; }
<DOC_TAG_VALUE_IN_LTGT> "/>" { yybegin(COMMENT_DATA); return JavadocTokenType.HTML_RCLOSE; }

<COMMENT_DATA_START, COMMENT_DATA> "{"
{
  yybegin(lookahead('@') ? INLINE_TAG_NAME : COMMENT_DATA);
  return JavadocTokenType.INLINE_TAG_START;
}
// brace balancing inside code and literal envs
<INSIDE_CODE_TAG> "{" { braces++; return JavadocTokenType.COMMENT_DATA; }
<INSIDE_CODE_TAG> "}"
      {
          if (--braces >= 0) { return JavadocTokenType.COMMENT_DATA; }
          else { yybegin(COMMENT_DATA); braces = 0; return JavadocTokenType.INLINE_TAG_END; }
      }

<INLINE_TAG_NAME> ("@code" | "@literal") { yybegin(CODE_TAG_SPACE); return JavadocTokenType.TAG_NAME; }
<INLINE_TAG_NAME> "@"{INLINE_TAG_IDENTIFIER} { yybegin(TAG_DOC_SPACE); return JavadocTokenType.TAG_NAME; }
// closing }
<COMMENT_DATA_START, COMMENT_DATA, TAG_DOC_SPACE, DOC_TAG_VALUE, CODE_TAG_SPACE> "}"
        { yybegin(COMMENT_DATA); return JavadocTokenType.INLINE_TAG_END; }

<COMMENT_DATA_START, COMMENT_DATA, DOC_TAG_VALUE> "&" {IDENTIFIER} ";" {return JavadocTokenType.HTML_ENTITY;}
<COMMENT_DATA_START, COMMENT_DATA, DOC_TAG_VALUE> . { yybegin(COMMENT_DATA); return JavadocTokenType.COMMENT_DATA; }
<INSIDE_CODE_TAG, CODE_TAG_SPACE> . { yybegin(INSIDE_CODE_TAG); return JavadocTokenType.COMMENT_DATA; }
<COMMENT_DATA_START> "@"{TAG_IDENTIFIER} { yybegin(TAG_DOC_SPACE); return JavadocTokenType.TAG_NAME; }

<TAG_DOC_SPACE> {WHITE_DOC_SPACE_CHAR}+ {
  if (lookahead('<') || lookahead('\"')) yybegin(COMMENT_DATA);
  else if (lookahead('\u007b')) yybegin(COMMENT_DATA);
  else yybegin(DOC_TAG_VALUE);

  return JavadocTokenType.WHITESPACE;
}

<INSIDE_CODE_TAG, CODE_TAG_SPACE> {WHITE_DOC_SPACE_CHAR}+ { yybegin(INSIDE_CODE_TAG); return JavadocTokenType.WHITESPACE; }

"*""/" { return JavadocTokenType.COMMENT_END; }
[^] { return JavadocTokenType.BAD_CHAR; }
