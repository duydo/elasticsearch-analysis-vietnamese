package com.coccoc;

import java.util.ArrayList;
import java.util.List;

public final class Token implements Cloneable {
	public static Token FULL_STOP = new Token(".", Type.PUNCT, SegType.END_SEG_TYPE, -1, -1);
	public static Token COMMA = new Token(",", Type.PUNCT, SegType.END_SEG_TYPE, -1, -1);
	public static Token SPACE = new Token(" ", Type.SPACE, -1, -1);

	public enum Type {
		WORD,
		NUMBER,
		SPACE,
		PUNCT,
		WHOLE_URL,
		SITE_URL;

		private static Type[] values = null;
		public static Type fromInt(int i) {
			if (Type.values == null) {
				Type.values = Type.values();
			}
			return Type.values[i];
		}
	}

	public enum SegType {
		OTHER_SEG_TYPE,
		SKIP_SEG_TYPE,
		URL_SEG_TYPE,
		END_URL_TYPE,
		END_SEG_TYPE;

		private static SegType[] values = null;
		public static SegType fromInt(int i) {
			if (SegType.values == null) {
				SegType.values = SegType.values();
			}
			return SegType.values[i];
		}
	}

	private final String text;
	private final Type type;
	private SegType segType; // Nullable
	private boolean splittedByDot;
	private final int startPos;
	private final int endPos;

	public Token(String text, int start, int end) {
		this(text, Type.WORD, start, end);
	}

	public Token(String text, Type type, int start, int end) {
		this(text, type, null, start, end);
	}

	public Token cloneWithNewText(String newText, int newEnd) {
		return new Token(newText, type, segType, splittedByDot, startPos, endPos);
	}

	public Token(String text, Type type, SegType segType, int start, int end) {
		this(text, type, segType, false, start, end);
	}

	public Token(String text, Type type, SegType segType, boolean splittedByDot, int start, int end) {
		this.text = text;
		this.type = type;
		this.segType = segType;
		this.splittedByDot = splittedByDot;
		this.startPos = start;
		this.endPos = end > 0 ? end : start + text.length();
	}

	public String getText() {
		return text;
	}

	public Type getType() {
		return type;
	}

	public int getPos() {
		return startPos;
	}

	public int getEndPos() {
		return endPos;
	}

	public SegType getSegType() {
		return segType;
	}

	public static ArrayList<String> toStringList(List<Token> tokenList) {
		ArrayList<String> temp = new ArrayList<>();
		for (Token token : tokenList) {
			temp.add(token.getText());
		}
		return temp;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(type).append(" `").append(text).append('`');
		if (segType == SegType.END_SEG_TYPE) {
			sb.append(" END");
		} else if (segType == SegType.URL_SEG_TYPE) {
			sb.append(" URL");
		} else if (segType == SegType.SKIP_SEG_TYPE) {
			sb.append(" SKIP");
		} else if (segType == SegType.END_URL_TYPE) {
			sb.append(" END_URL");
		} else {
			sb.append(" OTHER");
		}
		sb.append(' ').append(startPos).append('-').append(endPos);
		return sb.toString();
	}

	@Override
	public Token clone() {
		return new Token(text, type, segType, startPos, endPos);
	}

	@Override
	public int hashCode() {
		return text.hashCode() ^ type.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Token that = (Token) obj;

		if (!this.text.equals(that.text)) {
			return false;
		}

		if (this.type != that.type) {
			return false;
		}

		return true;
	}

	public boolean isWord() {
		return type == Type.WORD;
	}

	public boolean isPunct() {
		return type == Type.PUNCT;
	}

	public boolean isNumber() {
		return type == Type.NUMBER;
	}

	public boolean isWholeUrl() {
		return type == Type.WHOLE_URL;
	}

	public boolean isSiteUrl() {
		return type == Type.SITE_URL;
	}

	public boolean isSpace() {
		return type == Type.SPACE;
	}

	public boolean isEndSeg() {
		return segType == SegType.END_SEG_TYPE;
	}

	public boolean isSplittedByDot() {
		return splittedByDot;
	}

	public void setEndSeg() {
		segType = SegType.END_SEG_TYPE;
	}

	public void setOtherSeg() {
		segType = SegType.OTHER_SEG_TYPE;
	}

	public void setEndUrlSeg() {
		segType = SegType.END_URL_TYPE;
	}

	public void setUrlSeg() {
		segType = SegType.URL_SEG_TYPE;
	}

	public void setSkipSeg() {
		segType = SegType.SKIP_SEG_TYPE;
	}

	public boolean isUrlSeg() {
		return segType == SegType.URL_SEG_TYPE;
	}

	public boolean isEndUrlSeg() {
		return segType == SegType.END_URL_TYPE;
	}

	public boolean isSkipSeg() {
		return segType == SegType.SKIP_SEG_TYPE;
	}

	public boolean isOtherSeg() {
		return segType == SegType.OTHER_SEG_TYPE;
	}

	public boolean isWordOrNumber() {
		return isWord() || isNumber() || isSiteUrl();
	}
}
