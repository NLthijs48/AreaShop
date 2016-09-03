package me.wiefferink.areashop.messages;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * FancyMessageFormat converter, a library that enables to convert
 * messages in the FancyMessageFormat to Minecraft's bulky tellraw
 * format.
 * @author NLThijs48 | http://wiefferink.me
 * @author Tobias aka Phoenix | http://www.phoenix-iv.de
 */
public class FancyMessageFormat {

	private static final char TAG_BEFORE = '[';
	private static final char TAG_AFTER = ']';
	private static final char END_TAG_INDICATOR = '/';

	/**
	 * The special character that prefixes all basic chat formatting codes.
	 */
	private static final char SIMPLE_FORMAT_PREFIX_CHAR = '§';

	/**
	 * Resets all previous chat colors or formats.
	 */
	private static final char SIMPLE_FORMAT_RESET_CHAR = 'r';

	/**
	 * Lookup table for all continuous tags (marked by [])
	 */
	private static final HashMap<String, Tag> BRACKET_TAG_LIST = new HashMap<>();

	/**
	 * Lookup table for all interactive tags
	 */
	private static final HashMap<String, Tag> INTERACTIVE_TAG_LIST = new HashMap<>();

	static {
		// Enlist all possible tags
		// (They go into a HashMap for lookup purposes)
		cacheTags(BRACKET_TAG_LIST, Color.class);
		cacheTags(BRACKET_TAG_LIST, FormatType.class);
		cacheTags(BRACKET_TAG_LIST, FormatCloseTag.class);
		cacheTags(BRACKET_TAG_LIST, ControlTag.class);
		// Interactive tags
		cacheTags(INTERACTIVE_TAG_LIST, ClickType.class);
		cacheTags(INTERACTIVE_TAG_LIST, HoverType.class);
	}


	/**
	 * Puts all constants in the given Tag class into the given lookup table.
	 */
	private static <T extends Tag> void cacheTags(HashMap<String, Tag> tagList, Class<T> tags) {
		for(Tag tag : tags.getEnumConstants()) {
			for(String key : tag.getTags()) {
				tagList.put(key, tag);
			}
		}
	}


	// ------------------------------------------------------------------------------------------
	// -------------------------------     Public / Interface     -------------------------------
	// ------------------------------------------------------------------------------------------


	/**
	 * Parses the given FancyMessageFormat message to a JSON array that can be
	 * used with the tellraw command and the like.
	 * @param message The mesage to convert to JSON
	 * @return JSON strings that can be send to a player (multiple means line breaks have been used)
	 */
	public static List<String> convertToJSON(final String message) {
		return convertToJSON(Collections.singleton(message));
	}

	/**
	 * Parses the given FancyMessageFormat message to a JSON array that can be
	 * used with the tellraw command and the like.
	 * @param inputLines Input message split at line breaks.
	 * @return JSON string that can be send to a player (multiple means line breaks have been used)
	 */
	public static List<String> convertToJSON(final Iterable<String> inputLines) {
		ArrayList<String> lines = cleanInputString(inputLines);
		LinkedList<InteractiveMessagePart> message = parse(lines);

		List<String> result = new ArrayList<>();
		List<InteractiveMessagePart> combine = new ArrayList<>(); // Part that are combined to a line
		while(!message.isEmpty()) {
			InteractiveMessagePart part = message.removeFirst();
			combine.add(part);
			if(part.newline || message.isEmpty()) {
				if(combine.size() == 1) {
					StringBuilder nextLine = new StringBuilder("[");
					combine.get(0).toJSON(nextLine);
					nextLine.append("]");
					result.add(nextLine.toString());
				} else {
					StringBuilder nextLine = new StringBuilder("[{\"text\":\"\",\"extra\":[");
					for(int i = 0; i < combine.size(); i++) {
						if(i != 0) {
							nextLine.append(",");
						}
						combine.get(i).toJSON(nextLine);
					}
					nextLine.append("]}]");
					result.add(nextLine.toString());
				}
				combine.clear();
			}
		}
		return result;
	}


	/**
	 * Parses the given FancyMessageFormat message to a String containing control characters
	 * for formatting that can be used for console outputs, but also for normal player
	 * messages.
	 * <p>
	 * The returned message will only contain colors, bold, italic, underlining and 'magic'
	 * characters. Hovers and other advanced tellraw tags will be skipped.
	 * @param message Input message split at line breaks.
	 * @return Plain message that can be send
	 */
	public static String convertToConsole(final String message) {
		return convertToConsole(Collections.singleton(message));
	}

	/**
	 * Parses the given FancyMessageFormat message to a String containing control characters
	 * for formatting that can be used for console outputs, but also for normal player
	 * messages.
	 * <p>
	 * The returned message will only contain colors, bold, italic, underlining and 'magic'
	 * characters. Hovers and other advanced tellraw tags will be skipped.
	 * @param inputLines The raw message lines to process
	 * @return Plain message that can be send
	 */
	public static String convertToConsole(final Iterable<String> inputLines) {
		if(inputLines == null) {
			return null;
		}
		LinkedList<InteractiveMessagePart> parts = parse(inputLines, false);
		StringBuilder result = new StringBuilder();
		for(InteractiveMessagePart part : parts) {
			part.toSimpleString(result);
		}
		return result.toString();
	}


	/**
	 * Insert a message at the specified position
	 * @param message The current message
	 * @param insert  The message to insert
	 * @param line    The line number to insert at
	 * @param start   The start of the variable to replace
	 * @param end     The end of the variable to replace
	 */
	public static void insertMessage(List<String> message, List<String> insert, int line, int start, int end) {
		if(insert == null || line < 0 || line >= message.size() || start < 0 || end < 0) {
			return;
		}
		String lineContent = message.remove(line);
		if(start > lineContent.length() || end > lineContent.length()) {
			return;
		}
		if(isTaggedInteractive(lineContent)) {
			lineContent = lineContent.replace("", "");
			message.add(line, lineContent.substring(0, start)+convertToConsole(insert)+lineContent.substring(end));
			return;
		}
		// Find interactive lines meant for this message
		List<String> interactives = new ArrayList<>();
		while(line < message.size() && isTaggedInteractive(message.get(line))) {
			interactives.add(message.remove(line));
		}
		// Split the line and add the parts
		int at = line;
		if(start > 0) {
			message.add(line, lineContent.substring(0, start));
			at++;
			message.addAll(at, interactives);
			at += interactives.size();
		}
		message.addAll(at, insert);
		at += insert.size();
		message.addAll(at, interactives);
		at += interactives.size();
		if(end < lineContent.length()) {
			message.add(at, lineContent.substring(end));
			at++;
			message.addAll(at, interactives);
		}
	}


	// ------------------------------------------------------------------------------------------
	// -------------------------------     Private functions      -------------------------------
	// ------------------------------------------------------------------------------------------


	/**
	 * <ul>
	 * <li>Splits lines at line breaks (creating a new line in the Array).
	 * <li>Removes empty lines at the beginning.
	 * <li>Removes lines with properties in front of the first text-line.
	 * </ul>
	 */
	private static ArrayList<String> cleanInputString(Iterable<String> inputLines) {
		// Split lines at line breaks
		// In the end we will have a list with one line per element
		ArrayList<String> lines = new ArrayList<>();
		for(String line : inputLines) {
			lines.addAll(Arrays.asList(line.split("\\r?\\n")));
		}

		// Remove any special lines at the start (a real text line should be first)
		while(!lines.isEmpty() && isTaggedInteractive(lines.get(0))) {
			lines.remove(0);
		}

		return lines;
	}


	private static LinkedList<InteractiveMessagePart> parse(Iterable<String> inputLines) {
		return parse(inputLines, true);
	}

	private static LinkedList<InteractiveMessagePart> parse(final Iterable<String> inputLines, boolean doInteractives) {
		LinkedList<InteractiveMessagePart> message = new LinkedList<>();

		Color currentColor = null;
		Set<FormatType> currentFormatting = new HashSet<>();

		lineLoop:
		for(String line : inputLines) {
			InteractiveMessagePart messagePart;
			TaggedContent interactiveTag = getInteractiveTag(line);
			boolean isTextLine = interactiveTag == null;
			if(!doInteractives && !isTextLine) {
				continue;
			}
			boolean isHoverLine = false;

			if(isTextLine) {
				messagePart = new InteractiveMessagePart();
				message.add(messagePart);
			} else /* if Interactive formatting */ {
				if(message.isEmpty()) {
					continue;
				}
				messagePart = message.getLast();
				Tag tag = interactiveTag.tag;
				if(tag instanceof ClickType) {
					messagePart.clickType = (ClickType)interactiveTag.tag;
					messagePart.clickContent = interactiveTag.subsequentContent;
				} else if(tag instanceof HoverType) {
					line = interactiveTag.subsequentContent;
					isHoverLine = true;
					if(messagePart.hoverType != tag) {
						// Hover type changed
						messagePart.hoverContent = new LinkedList<>();
						messagePart.hoverType = (HoverType)tag;
					}
					// Add hover content below
				}
			}

			if(isTextLine || isHoverLine) {
				// Parse inline tags
				Color currentLineColor = currentColor;
				Set<FormatType> currentLineFormatting = currentFormatting;
				LinkedList<TextMessagePart> targetList = messagePart.content;
				boolean parseBreak = true;
				if(isHoverLine) {
					// Reset - use own
					currentLineColor = null;
					currentLineFormatting = new HashSet<>();
					targetList = messagePart.hoverContent;
					parseBreak = false;

					// Add line break after previous hover line
					if(!targetList.isEmpty()) {
						targetList.getLast().text += '\n';
					}
				}

				// Split into pieces at places where formatting changes
				while(!line.isEmpty()) {
					String textToAdd;
					TaggedContent nextTag = getNextTag(line, parseBreak);
					boolean tagged = nextTag != null;

					if(!tagged) {
						textToAdd = line;
						line = "";
					} else {
						textToAdd = nextTag.precedingContent;
						line = nextTag.subsequentContent;
					}

					// Add a text part with the correct formatting
					if((tagged && nextTag.tag == ControlTag.BREAK) || !textToAdd.isEmpty()) {
						TextMessagePart part = new TextMessagePart();
						part.text = textToAdd;
						part.formatTypes = new HashSet<>(currentLineFormatting);
						part.color = currentLineColor;
						targetList.add(part);
					}

					// Handle the change in formatting if a Tag has been detected (this needs to be after creating the InteractiveMessagePart)
					if(tagged) {
						// Handle the formatting tag
						Tag tag = nextTag.tag;
						if(tag instanceof Color) {
							currentLineColor = (Color)tag;
						} else if(tag instanceof FormatType) {
							currentLineFormatting.add((FormatType)tag);
						} else if(tag instanceof FormatCloseTag) {
							currentLineFormatting.remove(((FormatCloseTag)tag).closes);
						} else if(tag == ControlTag.BREAK) {
							messagePart.newline = true;
							currentLineFormatting.clear();
							continue lineLoop;
						} else if(tag == ControlTag.RESET) {
							currentLineFormatting.clear();
							currentLineColor = Color.WHITE;
						}
					}
				}

				if(!isHoverLine) {
					// Adapt global attributes
					currentColor = currentLineColor;
					if(messagePart.content.size() == 0) { // Prevent interactive parts without content
						message.removeLast();
					}
				}
			}
		}
		return message;
	}


	/**
	 * Searches and returns the first continuous tag found in the given String.
	 * @return The tag (plus its preceding and subsequent content) if found.
	 * Null if nothing is found.
	 */
	private static TaggedContent getNextTag(String line, boolean parseBreak) {
		Pattern pattern = Pattern.compile("\\[[/a-zA-Z1-9]+\\]|[&"+SIMPLE_FORMAT_PREFIX_CHAR+"][1-9abcdeflonskr]");
		Matcher matcher = pattern.matcher(line);
		// TODO Fix for escape things, and something with parseBreak?
		while(matcher.find()) {
			Tag tag = null;
			if(matcher.group().startsWith("&") || matcher.group().startsWith(SIMPLE_FORMAT_PREFIX_CHAR+"")) {
				for(Color color : Color.class.getEnumConstants()) {
					if(color.getNativeFormattingCode() == matcher.group().charAt(1)) {
						tag = color;
					}
				}
				for(FormatType format : FormatType.class.getEnumConstants()) {
					if(format.getNativeFormattingCode() == matcher.group().charAt(1)) {
						tag = format;
					}
				}
				if(matcher.group().charAt(1) == SIMPLE_FORMAT_RESET_CHAR) {
					tag = ControlTag.RESET;
				}
			} else {
				tag = BRACKET_TAG_LIST.get(matcher.group().substring(1, matcher.group().length()-1).toLowerCase());
			}
			// Continue search if we found something like [abc] that is not a tag
			if(tag != null) {
				return new TaggedContent(line.substring(0, matcher.start()), tag, line.substring(matcher.end()));
			}
		}
		return null;

		/*
		for(int startIndex = 0; startIndex < line.length(); startIndex++) {
			int start = line.indexOf(TAG_BEFORE, startIndex);
			if(start != -1) {
				int end = line.indexOf(TAG_AFTER, start);
				if(end != -1) {
					String inBetween = line.substring(start+1, end).toLowerCase();
					if(BRACKET_TAG_LIST.containsKey(inBetween)) {
						Tag tag = BRACKET_TAG_LIST.get(inBetween);
						if(tag == ControlTag.ESCAPE) {
							// Ignore next char
							line = line.substring(0, start)+line.substring(end+1);
							startIndex = start;
						} else if(!parseBreak && tag == ControlTag.ESCAPE) {
							// Ignore break
							startIndex = end+1;
						} else {
							String previousContent = line.substring(0, start);
							String subsequentContent = line.substring(end+1);
							return new TaggedContent(previousContent, tag, subsequentContent);
						}
					} else {
						startIndex = start;
					}
				} else {
					return null;
				}
			} else {
				return null;
			}
		}
		return null;
		*/
	}


	/**
	 * If the given line defines an interactive property (e.g. "hover: myText")
	 * the tag / property will be returned. Otherwise null is returned.
	 */
	private static TaggedContent getInteractiveTag(String line) {
		for(int index = 0; index < line.length(); index++) {
			char c = line.charAt(index);
			if(c == ' ' || c == '\t') {
				// Ignore (Skip spacing)
			} else {
				int end = line.indexOf(": ", index);
				if(end != -1) {
					String inBetween = line.substring(index, end).toLowerCase();
					if(INTERACTIVE_TAG_LIST.containsKey(inBetween)) {
						Tag tag = INTERACTIVE_TAG_LIST.get(inBetween);
						String subsequentContent = line.substring(end+2);
						return new TaggedContent(null, tag, subsequentContent);
					}
				}
				return null;
			}
		}
		return null;
	}


	/**
	 * Check if a line is an advanced declaration like hover or command
	 * @param line The line to check
	 * @return true if the line is interactive, false when it is a text line
	 */
	public static boolean isTaggedInteractive(String line) {
		return getInteractiveTag(line) != null;
	}


	/**
	 * Produce a string in double quotes with backslash sequences in all the
	 * right places.
	 * @param string A String
	 * @param sb The StringBuilder to add the quoted string to
	 * @return A String correctly formatted for insertion in a JSON text.
	 */
	/*
	 * Copyright (c) 2002 JSON.org
	 * Licensed under the Apache License, Version 2.0
	 */
	private static StringBuilder quoteStringJson(String string, StringBuilder sb) {
		if(string == null || string.length() == 0) {
			return new StringBuilder("\"\"");
		}

		char c;
		int i;
		int len = string.length();
		String t;

		sb.append('"');
		for(i = 0; i < len; i += 1) {
			c = string.charAt(i);
			switch(c) {
				case '\\':
				case '"':
					sb.append('\\');
					sb.append(c);
					break;
				case '/':
					sb.append('\\');
					sb.append(c);
					break;
				case '\b':
					sb.append("\\b");
					break;
				case '\t':
					sb.append("\\t");
					break;
				case '\n':
					sb.append("\\n");
					break;
				case '\f':
					sb.append("\\f");
					break;
				case '\r':
					sb.append("\\r");
					break;
				default:
					if(c < ' ') {
						t = "000"+Integer.toHexString(c);
						sb.append("\\u");
						sb.append(t.substring(t.length()-4));
					} else {
						sb.append(c);
					}
			}
		}
		sb.append('"');
		return sb;
	}


	// ------------------------------------------------------------------------------------------
	// -------------------------------       Helper classes       -------------------------------
	// ------------------------------------------------------------------------------------------


	private static class TaggedContent {
		final String precedingContent;
		final Tag tag;
		final String subsequentContent;

		public TaggedContent(String pre, Tag tag, String sub) {
			this.precedingContent = pre;
			this.tag = tag;
			this.subsequentContent = sub;
		}
	}


	/**
	 * Holds a string with basic (non-interactive) formatting.
	 */
	private static class TextMessagePart {
		String text = "";
		Color color = null;
		Set<FormatType> formatTypes = new HashSet<>();

		/**
		 * Get a simple colored/formatted string
		 * @param sb The StringBuilder to append the result to
		 * @return StringBuilder with the message appended
		 */
		StringBuilder toSimpleString(StringBuilder sb) {
			// Color
			if(color != null) {
				sb.append(SIMPLE_FORMAT_PREFIX_CHAR);
				sb.append(color.getNativeFormattingCode());
			}
			// Formatting
			for(FormatType format : formatTypes) {
				sb.append(SIMPLE_FORMAT_PREFIX_CHAR);
				sb.append(format.getNativeFormattingCode());
			}
			// Text
			sb.append(text);
			return sb;
		}

		/**
		 * Get a JSON component for this message part
		 * @param sb The StringBuilder to append the result to
		 * @return This part formatted in JSON
		 */
		StringBuilder toJSON(StringBuilder sb) {
			sb.append('{');
			sb.append("\"text\":");
			quoteStringJson(text, sb);
			if(color != null && color != Color.WHITE) {
				sb.append(",\"color\":\"").append(color.jsonValue).append("\"");
			}
			for(FormatType formatting : formatTypes) {
				sb.append(",\"");
				sb.append(formatting.jsonKey).append("\":");
				sb.append("true");
			}
			sb.append('}');
			return sb;
		}

		boolean hasFormatting() {
			return !(color == Color.WHITE && formatTypes.isEmpty());
		}

		@Override
		public String toString() {
			return "TextMessagePart(text:"+text+", color:"+color+", formatting:"+formatTypes.toString()+")";
		}
	}


	/**
	 * Holds a string with interactive formatting.
	 */
	private static class InteractiveMessagePart {

		LinkedList<TextMessagePart> content = new LinkedList<>();
		boolean newline = false;

		// Click
		ClickType clickType = null;
		String clickContent = "";

		// Hover
		HoverType hoverType = null;
		LinkedList<TextMessagePart> hoverContent = null;

		/**
		 * Append the message content to the StringBuilder
		 * @param sb The StringBuilder to append the message to
		 */
		StringBuilder toSimpleString(StringBuilder sb) {
			for(TextMessagePart part : content) {
				part.toSimpleString(sb);
			}
			return sb;
		}

		/**
		 * Get a JSON component for this message part
		 * @param sb The StringBuilder to append the result to
		 * @return This part formatted in JSON
		 */
		StringBuilder toJSON(StringBuilder sb) {
			if(content.size() == 1) {
				// Add attributes to TextMessagePart object
				content.getFirst().toJSON(sb);
				sb.deleteCharAt(sb.length()-1);
			} else {
				sb.append('{');
				sb.append("\"text\":\"\",\"extra\":[");
				for(TextMessagePart textPart : content) {
					textPart.toJSON(sb);
					sb.append(',');
				}
				sb.deleteCharAt(sb.length()-1);
				sb.append(']');
			}
			if(clickType != null) {
				sb.append(',');
				sb.append("\"clickEvent\":{");
				sb.append("\"action\":\"").append(clickType.getJsonKey()).append("\",");
				sb.append("\"value\":");
				quoteStringJson(clickContent, sb);
				sb.append('}');
			}
			if(hoverType != null) {
				sb.append(',');
				sb.append("\"hoverEvent\":{");
				sb.append("\"action\":\"").append(hoverType.getJsonKey()).append("\",");
				sb.append("\"value\":");
				if(hoverContent.size() == 1) {
					TextMessagePart hoverPart = hoverContent.getFirst();
					if(hoverPart.hasFormatting()) {
						hoverPart.toJSON(sb);
					} else {
						quoteStringJson(hoverPart.text, sb);
					}
				} else {
					sb.append('[');
					for(TextMessagePart hoverPart : hoverContent) {
						hoverPart.toJSON(sb);
						sb.append(',');
					}
					sb.deleteCharAt(sb.length()-1);
					sb.append(']');
				}
				sb.append('}');
			}
			sb.append('}');
			return sb;
		}

		@Override
		public String toString() {
			return "InteractiveMessagePart(textMessageParts:"+content+", clickType:"+clickType+", clickContent:"+clickContent+", hoverType:"+hoverType+", hoverContent:"+hoverContent+", newline:"+newline+")";
		}
	}


	// --------------------------------------- Tags ---------------------------------------


	interface Tag {
		/**
		 * Tag text(s) used in the FancyMessageFormat (The text between '[' and ']')
		 */
		String[] getTags();
	}


	/**
	 * Indicates formatting that is applied until explicitly stopped.
	 * Can also be used in simple Minecraft messages (Non-JSON).
	 */
	interface ContinuousTag extends Tag {
		/**
		 * The character that defines upcoming formatting in a native (non-JSON) Minecraft message.
		 */
		char getNativeFormattingCode();
	}


	/**
	 * Indicates formatting that allows cursor interaction. Requires the
	 * Minecraft JSON / tellraw format.
	 */
	interface InteractiveMessageTag extends Tag {
		String getJsonKey();
	}


	enum Color implements ContinuousTag {
		WHITE('f'),
		BLACK('0'),
		BLUE('9'),
		DARK_BLUE('1', "darkblue"),
		GREEN('a'),
		DARK_GREEN('2', "darkgreen"),
		AQUA('b'),
		DARK_AQUA('3', "darkaqua"),
		RED('c'),
		DARK_RED('4', "darkred"),
		LIGHT_PURPLE('d', "lightpurple"),
		DARK_PURPLE('5', "darkpurple"),
		YELLOW('e'),
		GOLD('6'),
		GRAY('7', "grey"),
		DARK_GRAY('8', "dark_grey", "darkgray", "darkgrey");

		final char bytecode;
		final String jsonValue;
		final String[] tags;

		Color(char bytecode, String... extraTags) {
			this.bytecode = bytecode;
			this.jsonValue = this.name().toLowerCase();
			this.tags = new String[extraTags.length+1];
			this.tags[0] = this.name().toLowerCase();
			System.arraycopy(extraTags, 0, this.tags, 1, extraTags.length);
		}

		@Override
		public String[] getTags() {
			return tags;
		}

		@Override
		public char getNativeFormattingCode() {
			return bytecode;
		}
	}


	enum FormatType implements ContinuousTag {
		BOLD('l', "bold", "b", "bold"),
		ITALIC('o', "italic", "i", "italic"),
		UNDERLINE('n', "underlined", "u", "underline"),
		STRIKETHROUGH('s', "strikethrough", "s", "strike", "strikethrough"),
		OBFUSCATE('k', "obfuscated", "obfuscate");

		final char bytecode;
		final String jsonKey;
		final String[] tags;

		FormatType(char bytecode, String jsonKey, String... tags) {
			this.bytecode = bytecode;
			this.jsonKey = jsonKey;
			this.tags = tags;
		}

		@Override
		public String[] getTags() {
			return tags;
		}

		@Override
		public char getNativeFormattingCode() {
			return bytecode;
		}

	}


	enum FormatCloseTag implements Tag {
		BOLD_END(FormatType.BOLD),
		ITALIC_END(FormatType.ITALIC),
		UNDERLINE_END(FormatType.UNDERLINE),
		STRIKETHROUGH_END(FormatType.STRIKETHROUGH),
		OBFUSCATE_END(FormatType.OBFUSCATE);

		/**
		 * Formatting that is stopped at this point
		 */
		final FormatType closes;
		private final String[] tags;

		FormatCloseTag(FormatType openingTag) {
			this.closes = openingTag;

			// Auto-generate close tags
			tags = new String[closes.tags.length];
			for(int i = 0; i < tags.length; i++) {
				tags[i] = END_TAG_INDICATOR+closes.tags[i];
			}
		}

		@Override
		public String[] getTags() {
			return tags;
		}

	}


	enum ControlTag implements Tag {
		BREAK("break"),
		ESCAPE("esc"),
		RESET("reset");

		private final String[] tags;

		ControlTag(String... tags) {
			this.tags = tags;
		}

		@Override
		public String[] getTags() {
			return tags;
		}

	}


	/**
	 * Types of clicking
	 */
	enum ClickType implements InteractiveMessageTag {
		LINK("open_url", "link"),
		COMMAND("run_command", "command"),
		SUGGEST("suggest_command", "suggest");

		private final String jsonKey;
		private final String[] tags;

		ClickType(String jsonKey, String... tags) {
			this.jsonKey = jsonKey;
			this.tags = tags;
		}

		@Override
		public String[] getTags() {
			return tags;
		}

		@Override
		public String getJsonKey() {
			return jsonKey;
		}
	}


	enum HoverType implements InteractiveMessageTag {
		HOVER;

		@Override
		public String[] getTags() {
			return new String[]{"hover"};
		}

		@Override
		public String getJsonKey() {
			return "show_text";
		}
	}

}