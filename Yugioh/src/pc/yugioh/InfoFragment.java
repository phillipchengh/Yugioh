package pc.yugioh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class InfoFragment extends Fragment {

	public static final int MAX_CHAR_PER_LINE = 22;
	public static final int MAX_CHAR_PER_LORE_LINE = 41;
	private Activity activity;
	private HashMap<String, LinkedList<String>> map;
	private LinearLayout table;
	private static String[] monsterInfo = {
			"property:ATK",
			"property:Actions",
			"property:Archseries",
			"property:Archseries_related",
			"property:Attack",
			"property:Attribute_Text",
			"property:Card_Number",
			"property:Card_type",
			"property:DEF",
			"property:Effect_types",
			"property:English_name",
			"property:Fusion_Material_for",
			"property:Level",
			"property:Lore",
			"property:Materials",
			"property:Rank",
			"property:Summoning",
			"property:Synchro_Material_for",
			"property:TCG_Advanced_Format_Status",
			"property:TCG_Traditional_Format_Status",
			"property:Translated_name",
			"property:Type_Text",
			"property:Types"
	};
	
	private static String[] spellTrapInfo = {
		"property:Actions",
		"property:Archseries",
		"property:Archseries_related",
		"property:Attribute_Text",
		"property:Card_Number",
		"property:Effect_types",
		"property:English_name",
		"property:Lore",
		"property:TCG_Advanced_Format_Status",
		"property:TCG_Traditional_Format_Status",
		"property:Translated_name",
		"property:Types"
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.info_fragment, container, false);
		activity = getActivity();
		table = (LinearLayout) view.findViewById(R.id.infoLayout);
		String rdf = null;
		rdf = getArguments().getString("rdf");
		String gallery_name = getArguments().getString("gallery_name");
		int card_type = getArguments().getInt("card_type");
		loadMap(rdf, card_type);
		if (card_type == 0) {
			loadMonster();
		} else {
			loadSpellTrap();
		}
		return view;
	}
	
	private String getProperty(String line) {
		int end;
		if ((end = line.indexOf(" rdf:")) == -1 || !line.contains("property:")) {
			return null;
		}
		return line.substring(3, end);
	}
	
	//returns true if property1 is after property2 in line
	private boolean isAfterProperty(String property1, String property2) {
		return (property1.compareTo(property2) > 0);
	}
	
	private void loadMap(String result, int card_type) {
		BufferedReader rdf = new BufferedReader(new StringReader(result));
		map = new HashMap<String, LinkedList<String>>();
		String line, property;
		String[] properties;
		LinkedList<String> list;
		int i = 0, size = 0;
		if (card_type == 0) {
			properties = monsterInfo;
			size = monsterInfo.length;
		} else {
			properties = spellTrapInfo;
			size = spellTrapInfo.length;
		}
		try {
			line = rdf.readLine();
			while (line != null && i < size) {
				if ((property = getProperty(line)) == null) {
					line = rdf.readLine();
					continue;
				}
				while (isAfterProperty(property, properties[i])) {
					i++;
				}
				if (line.contains(properties[i])) {
					list = new LinkedList<String>();
					list.add(line);
					while ((line = rdf.readLine()) != null 
							&& !(line.contains("property:") && !properties[i].equals(getProperty(line)))
							) {
						list.add(line);
					}
					map.put(properties[i], list);
					i++;
				} else {
					line = rdf.readLine();
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e("InfoFragment", "Load map error", e);
		}
	}

	private void loadTableRow(String name, String value, int lines) {
		LinearLayout layout = new LinearLayout(activity);
		layout.setOrientation(LinearLayout.HORIZONTAL);
		layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		TextView tv_name = new TextView(activity);
		tv_name.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.5f));
		tv_name.setText(name);
		tv_name.setTextSize(18);
		tv_name.setPadding(10, 10, 10, 10);
		tv_name.setSingleLine(false);
		tv_name.setHorizontallyScrolling(false);
		tv_name.setLines(lines);
		tv_name.setGravity(Gravity.LEFT);
		layout.addView(tv_name);
		TextView tv_value = new TextView(activity);
		tv_value.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.5f));
		tv_value.setText(value);
		tv_value.setTextSize(18);
		tv_value.setPadding(10, 10, 10, 10);
		tv_value.setSingleLine(false);
		tv_value.setHorizontallyScrolling(false);
		tv_value.setLines(lines);
		tv_value.setGravity(Gravity.RIGHT);
		layout.addView(tv_value);
		table.addView(layout);
	}
	
	private void loadLoreTableRow(String name, String value, int lines) {
		LinearLayout layout = new LinearLayout(activity);
		layout.setOrientation(LinearLayout.HORIZONTAL);
		layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		TextView tv_name = new TextView(activity);
		tv_name.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));
		tv_name.setText(name);
		tv_name.setTextSize(18);
		tv_name.setPadding(10, 10, 10, 10);
		tv_name.setSingleLine(false);
		tv_name.setHorizontallyScrolling(false);
		tv_name.setLines(1);
		tv_name.setGravity(Gravity.LEFT);
		layout.addView(tv_name);
		table.addView(layout);
		layout = new LinearLayout(activity);
		layout.setOrientation(LinearLayout.HORIZONTAL);
		layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		TextView tv_value = new TextView(activity);
		tv_value.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));
		tv_value.setText(value);
		tv_value.setTextSize(18);
		tv_value.setPadding(10, 10, 10, 10);
		tv_value.setSingleLine(false);
		tv_value.setHorizontallyScrolling(false);
		tv_value.setLines(lines);
		tv_value.setGravity(Gravity.LEFT);
		layout.addView(tv_value);
		table.addView(layout);
	}
	
	private String removeBrackets(String value) {
		String temp;
		Pattern pattern = Pattern.compile("\\[\\[[^\\[]*\\|[^\\]]*\\]\\]");
		Matcher matcher = pattern.matcher(value);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			temp = value.substring(matcher.start(), matcher.end());
			matcher.appendReplacement(sb, temp.substring(temp.indexOf('|') + 1, temp.indexOf(']')));
		}
		matcher.appendTail(sb);
		value = sb.toString();
		pattern = Pattern.compile("\\[\\[[^\\[^\\]]*\\]\\]");
		matcher = pattern.matcher(value);
		sb = new StringBuffer();
		while (matcher.find()) {
			temp = value.substring(matcher.start(), matcher.end());
			matcher.appendReplacement(sb, temp.substring(temp.indexOf("[[") + 2, temp.indexOf("]]")));
		}
		matcher.appendTail(sb);
		return sb.toString();
	}
	
	private String adjustLength(String line) {
		line.replaceAll("&lt;br /&gt;", "\n");
		if (line.length() <= MAX_CHAR_PER_LINE) {
			return line;
		}
		int curr = 0, next, size = line.length();
		String temp = line;
		while ((size - curr) > MAX_CHAR_PER_LINE) {
			next = temp.indexOf('\n', curr);
			if (next == -1 || next > MAX_CHAR_PER_LINE) {
				curr += (line.substring(curr, curr + MAX_CHAR_PER_LINE)).lastIndexOf(' ');
				temp = line.substring(curr+1);
				line = line.substring(0, curr) + '\n' + temp;
			}
		}
		return line;
	}
	
	private String adjustLoreLength(String line) {
		int i;
		while ((i = line.indexOf("&lt;br /&gt;")) != -1) {
			line = line.substring(0, i) + '\n' + line.substring(i + "&lt;br /&gt;".length());
		}
		if (line.length() <= MAX_CHAR_PER_LORE_LINE) {
			return line;
		}
		int curr = 0, next, size = line.length();
		while ((size - curr) > MAX_CHAR_PER_LORE_LINE) {
			next = line.indexOf('\n', curr+1);
			if (next == -1 || (next - curr) > MAX_CHAR_PER_LORE_LINE) {
				curr += (line.substring(curr, curr + MAX_CHAR_PER_LORE_LINE)).lastIndexOf(' ');
				line = line.substring(0, curr) + '\n' + line.substring(curr+1);;
			} else {
				curr = next;
			}
		}
		return line;
	}
	
	private int countChar(String s, char c) {
		int counter = 0, size = s.length();
		for (int i = 0; i < size; i++) {
			if (s.charAt(i) == c) {
				counter++;
			}
		}
		return counter;
	}
	
	private String getTagText(String line) {
		int start = line.indexOf("\">") + "\">".length();
		int end = line.indexOf("</property:");
		if (start == -1 || end == -1) {
			return null;
		}
		return line.substring(start, end);
	}

	private String getLore(String line) {
		int start = line.indexOf("#string\">") + "#string\">".length();
		int end = line.indexOf("</property:Lore");
		if (start == -1 || end == -1) {
			return null;
		}
		return line.substring(start, end);
	}
	
	private String getWikiEntry(String line) {
		int start = line.indexOf("&wiki;") + "&wiki;".length();
		int end = line.indexOf("\"/>");
		if (start == -1 || end == -1) {
			return null;
		}
		return (line.substring(start, end)).replaceAll("_", " ").replaceAll("2D", "");
	}
	
	private static String[] monsterInfoOrder = {
		"property:English_name",
		"property:Attribute_Text",
		"property:Type_Text",
		"property:Types",
		"property:Level",
		"property:Rank",
		"property:ATK",
		"property:DEF",
		"property:Card_Number",
		"property:Effect_types",
		"property:Lore",
		"property:Actions",
		"property:Archseries",
		"property:Archseries_related",
		"property:Attack",
		"property:Card_type",
		"property:Fusion_Material_for",
		"property:Materials",
		"property:Summoning",
		"property:Synchro_Material_for",
		"property:TCG_Advanced_Format_Status",
		"property:TCG_Traditional_Format_Status",
		"property:Translated_name"
	};
	
	private void loadMonster() {
		LinkedList<String> list, list2;
		int size, i, y;
		String value, temp;
		if ((list = map.get("property:English_name")) != null) {
			value = getTagText(list.get(0));
			value = adjustLength(value);
			loadTableRow("English Name", value, countChar(value, '\n')+2);
		} else if ((list = map.get("property:Translated_name")) != null) {
			value = getTagText(list.get(0));
			value = adjustLength(value);
			loadTableRow("Translated Name", value, countChar(value, '\n')+2);
		} 
		if ((list = map.get("property:Attribute_Text")) != null) {
			value = getTagText(list.get(0));
			loadTableRow("Attribute", value, 2);
		}
		if ((list = map.get("property:Type_Text")) != null) {
			value = getTagText(list.get(0));
			loadTableRow("Type", value, 2);
		}
		if ((list = map.get("property:Types")) != null) {
			size = list.size();
			value = "";
			for (i = 0, y = 0; i < size; i++) {
				if ((temp = getTagText(list.get(i))) != null) {
					value += temp + '\n';
					y++;
				}
			}
			loadTableRow("Types", value, y+1);
		}
		if ((list = map.get("property:Level")) != null) {
			value = getTagText(list.get(0));
			loadTableRow("Level", value, 2);
		}
		if ((list = map.get("property:Rank")) != null) {
			value = getTagText(list.get(0));
			loadTableRow("Rank", value, 2);
		}
		if ((list = map.get("property:ATK")) != null && (list2 = map.get("property:DEF")) != null) {
			value = getTagText(list.get(0)) + '/' + getTagText(list2.get(0));
			loadTableRow("ATK/DEF", value, 2);
		}
		if ((list = map.get("property:Card_Number")) != null) {
			value = getTagText(list.get(0));
			loadTableRow("Card Number", value, 2);
		}
		if ((list = map.get("property:Effect_types")) != null) {
			size = list.size();
			value = "";
			for (i = 0, y = 0; i < size; i++) {
				if ((temp = getTagText(list.get(i))) != null) {
					value += temp + '\n';
					y++;
				}
			}
			value = removeBrackets(value);
			loadTableRow("Effect Types", value, y+1);
		}
		if ((list = map.get("property:Lore")) != null) {
			value = getLore(list.get(0));
			value = removeBrackets(value);
			value = adjustLoreLength(value);
			loadLoreTableRow("Lore", value, countChar(value, '\n')+2);
		}
		if ((list = map.get("property:Actions")) != null) {
			value = getWikiEntry(list.get(0));
			loadTableRow("Actions", value, countChar(value, '\n')+2);
		}
		if ((list = map.get("property:Archseries")) != null) {
			size = list.size();
			value = "";
			for (i = 0, y = 0; i < size; i++) {
				if ((temp = getWikiEntry(list.get(i))) != null) {
					value += temp + '\n';
					y++;
				}
			}
			loadTableRow("Archseries", value, y+1);
		}
		if ((list = map.get("property:Archseries_related")) != null) {
			size = list.size();
			value = "";
			for (i = 0, y = 0; i < size; i++) {
				if ((temp = getWikiEntry(list.get(i))) != null) {
					value += temp + '\n';
					y++;
				}
			}
			loadTableRow("Archseries Related", value, y+1);
		}
		if ((list = map.get("property:Attack")) != null) {
			value = getWikiEntry(list.get(0));
			loadTableRow("Attack", value, 2);
		}
		if ((list = map.get("property:Card_type")) != null) {
			value = getWikiEntry(list.get(0));
			loadTableRow("Card Type", value, 2);
		}
		if ((list = map.get("property:Fusion_Material_for")) != null) {
			value = getWikiEntry(list.get(0));
			loadTableRow("Fusion Material for", value, 2);
		}
		if ((list = map.get("property:Materials")) != null) {
			value = getTagText(list.get(0));
			loadTableRow("Materials", value, countChar(value, '\n')+2);
		}
		if ((list = map.get("property:Summoning")) != null) {
			value = getWikiEntry(list.get(0));
			loadTableRow("Summoning", value, countChar(value, '\n')+2);
		}
		if ((list = map.get("property:Synchro_Material_for")) != null) {
			value = getWikiEntry(list.get(0));
			loadTableRow("Synchro Material for", value, 2);
		}
		if ((list = map.get("property:TCG_Advanced_Format_Status")) != null) {
			value = getWikiEntry(list.get(0));
			loadTableRow("TCG Advanced Status", value, 2);
		}
		if ((list = map.get("property:TCG_Traditional_Format_Status")) != null) {
			value = getWikiEntry(list.get(0));
			loadTableRow("TCG Traditional Status", value, 2);
		}
	}
	
	private static String[] spellTrapInfoOrder = {
		"property:English_name",
		"property:Attribute_Text",
		"property:Types",
		"property:Card_Number",
		"property:Effect_types",
		"property:Lore",
		"property:Actions",
		"property:Archseries",
		"property:Archseries_related",
		"property:TCG_Advanced_Format_Status",
		"property:TCG_Traditional_Format_Status",
	};
	
	private void loadSpellTrap() {
		LinkedList<String> list, list2;
		int size, i, y;
		String value, temp;
		if ((list = map.get("property:English_name")) != null) {
			value = getTagText(list.get(0));
			value = adjustLength(value);
			loadTableRow("English Name", value, countChar(value, '\n')+2);
		} else if ((list = map.get("property:Translated_name")) != null) {
			value = getTagText(list.get(0));
			value = adjustLength(value);
			loadTableRow("Translated Name", value, countChar(value, '\n')+2);
		} 
		if ((list = map.get("property:Attribute_Text")) != null) {
			value = getTagText(list.get(0));
			loadTableRow("Attribute", value, 2);
		}
		if ((list = map.get("property:Types")) != null) {
			size = list.size();
			value = "";
			for (i = 0, y = 0; i < size; i++) {
				if ((temp = getTagText(list.get(i))) != null) {
					value += temp + '\n';
					y++;
				}
			}
			loadTableRow("Types", value, y+1);
		}
		if ((list = map.get("property:Card_Number")) != null) {
			value = getTagText(list.get(0));
			loadTableRow("Card Number", value, 2);
		}
		if ((list = map.get("property:Effect_types")) != null) {
			size = list.size();
			value = "";
			for (i = 0, y = 0; i < size; i++) {
				if ((temp = getTagText(list.get(i))) != null) {
					value += temp + '\n';
					y++;
				}
			}
			value = removeBrackets(value);
			loadTableRow("Effect Types", value, y+1);
		}
		if ((list = map.get("property:Lore")) != null) {
			value = getLore(list.get(0));
			value = removeBrackets(value);
			value = adjustLoreLength(value);
			loadLoreTableRow("Lore", value, countChar(value, '\n')+2);
		}
		if ((list = map.get("property:Actions")) != null) {
			value = getWikiEntry(list.get(0));
			loadTableRow("Actions", value, countChar(value, '\n')+2);
		}
		if ((list = map.get("property:Archseries")) != null) {
			size = list.size();
			value = "";
			for (i = 0, y = 0; i < size; i++) {
				if ((temp = getWikiEntry(list.get(i))) != null) {
					value += temp + '\n';
					y++;
				}
			}
			loadTableRow("Archseries", value, y+1);
		}
		if ((list = map.get("property:Archseries_related")) != null) {
			size = list.size();
			value = "";
			for (i = 0, y = 0; i < size; i++) {
				if ((temp = getWikiEntry(list.get(i))) != null) {
					value += temp + '\n';
					y++;
				}
			}
			loadTableRow("Archseries Related", value, y+1);
		}
		if ((list = map.get("property:TCG_Advanced_Format_Status")) != null) {
			value = getWikiEntry(list.get(0));
			loadTableRow("TCG Advanced Status", value, 2);
		}
		if ((list = map.get("property:TCG_Traditional_Format_Status")) != null) {
			value = getWikiEntry(list.get(0));
			loadTableRow("TCG Traditional Status", value, 2);
		}
	}
	
}
