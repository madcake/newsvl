package ru.vl.news.parser;

/**
 * @author andrey.pogrebnoy
 */
public interface Parser<T, J> {
	T parse(J data);
}
