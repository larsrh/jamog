package core.build.checking;

import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author lars
 */
public final class Trie<T> {

	private final Map<Character,Trie<T>> children;
	private T value;

	public Trie(T value) {
		this.children = new TreeMap<Character, Trie<T>>();
		this.value = value;
	}

	public int size() {
		int sum = (value != null) ? 1 : 0;
		for (Trie<T> child : children.values())
			sum += child.size();
		return sum;
	}

	public T getValue() {
		return value;
	}

	public int getChildrenCount() {
		return children.size();
	}

	public T put(String key,T value) {
		return traverse(0, key.toCharArray(), true).value = value;
	}

	public Trie<T> get(String key) {
		return traverse(0, key.toCharArray(), false);
	}

	public Trie<T> get(char c) {
		if (children.containsKey(c))
			return children.get(c);
		else
			return null;
	}

	private Trie<T> traverse(int pos,char key[],boolean createNew) {
		if (pos >= key.length)
			return this;

		char first = key[pos];
		if (children.containsKey(first)) {
			return children.get(first).traverse(pos+1, key, createNew);
		}
		else if (createNew) {
			Trie<T> newTrie = new Trie<T>(null);
			children.put(first, newTrie);
			return newTrie.traverse(pos+1, key, true /*createNew=true*/);
		}
		else {
			return null;
		}
	}

	public boolean isEmpty() {
		return children.isEmpty();
	}

	@Override
	public String toString() {
		return appendToStringBuilder(new StringBuilder()).toString();
	}

	private StringBuilder appendToStringBuilder(StringBuilder sb) {
		if (value != null)
			sb.append("->").append(value);
		if (!children.isEmpty()) {
			sb.append(":[");
			for (Map.Entry<Character,Trie<T>> entry : children.entrySet()) {
				sb.append(entry.getKey());
				entry.getValue().appendToStringBuilder(sb);
				sb.append(',');
			}
			sb.replace(sb.length()-1, sb.length(), "]");
		}
		return sb;
	}

}
