package io.zershyan.sccore.datagen.init;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class SCCLang {
	private static final List<Entry> LangEntries = new ArrayList<>();
	protected final List<Entry> entries = new ArrayList<>();

	public static void stringEntry(String key, String enDesc, String zhDesc) {
		LangEntries.add(new FinalEntry<>(key, enDesc, zhDesc));
	}

	public static List<FinalEntry<?>> getAllLang() {
		List<FinalEntry<?>> entryList = new ArrayList<>();
		Stream.of( //添加lang类
			new SCCKeyLang()
		).map(SCCLang::initLang).map(
				list -> list.stream().map(Entry::toLang).toList()
		).forEach(entryList::addAll);
		entryList.addAll(LangEntries.stream().map(Entry::toLang).toList());
		return entryList;
	}

	protected void addEntry(Entry entry) {
		this.entries.add(entry);
	}

	protected <T> void addFinalEntry(T key, String enUs, String zhCn) {
		this.entries.add(new FinalEntry<>(key, enUs, zhCn));
	}

	private List<Entry> initLang() {
		init(); // 无返回初始化
		return init(entries); // 有返回初始化
	}

	protected List<Entry> init(List<Entry> entries) {
		return entries;
	}

	protected void init() {}

	public interface Entry {
		FinalEntry<?> toLang();
	}

	public record Lang(String enDesc, String zhDesc) {}

	public record FinalEntry<T>(T key, Lang lang) implements Entry {
		public FinalEntry(T key, String enDesc, String zhDesc) {
			this(key, new Lang(enDesc, zhDesc));
		}

		@Override
		public FinalEntry<?> toLang() {
			return this;
		}
	}
}
