/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.servlet.mvc.condition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.lang.Nullable;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.UrlPathHelper;

/**
 * A logical disjunction (' || ') request condition that matches a request
 * against a set of URL path patterns.
 *
 * @author Rossen Stoyanchev
 * @since 3.1
 *
 *
 *
 *
 *
 * Spring MVC请求URL带后缀匹配的情况，如/hello.json也能匹配/hello
 *  * RequestMappingInfoHandlerMapping 在处理http请求的时候， 如果 请求url 有后缀，如果找不到精确匹配的那个@RequestMapping方法。
 *  *  那么，就把后缀去掉，然后.*去匹配，这样，一般都可以匹配，默认这个行为是被开启的。
 *  *
 *  * 比如有一个@RequestMapping("/rest"), 那么精确匹配的情况下， 只会匹配/rest请求。 但如果我前端发来一个 /rest.abcdef 这样的请求， 又没有配置 @RequestMapping("/rest.abcdef") 这样映射的情况下， 那么@RequestMapping("/rest") 就会生效。
 *  *
 *  *  这样会带来什么问题呢？绝大多数情况下是没有问题的，但是如果你是一个对权限要求非常严格的系统，强烈关闭此项功能，否则你会有意想不到的"收获"。
 */
public final class PatternsRequestCondition extends AbstractRequestCondition<PatternsRequestCondition> {

	private final Set<String> patterns;

	private final UrlPathHelper pathHelper;

	private final PathMatcher pathMatcher;

	private final boolean useSuffixPatternMatch;

	private final boolean useTrailingSlashMatch;

	private final List<String> fileExtensions = new ArrayList<>();


	/**
	 * Creates a new instance with the given URL patterns.
	 * Each pattern that is not empty and does not start with "/" is prepended with "/".
	 * @param patterns 0 or more URL patterns; if 0 the condition will match to every request.
	 */
	public PatternsRequestCondition(String... patterns) {
		this(Arrays.asList(patterns), null, null, true, true, null);
	}

	/**
	 * Additional constructor with flags for using suffix pattern (.*) and
	 * trailing slash matches.
	 * @param patterns the URL patterns to use; if 0, the condition will match to every request.
	 * @param urlPathHelper for determining the lookup path of a request
	 * @param pathMatcher for path matching with patterns
	 * @param useSuffixPatternMatch whether to enable matching by suffix (".*")
	 * @param useTrailingSlashMatch whether to match irrespective of a trailing slash
	 */
	public PatternsRequestCondition(String[] patterns, @Nullable UrlPathHelper urlPathHelper,
			@Nullable PathMatcher pathMatcher, boolean useSuffixPatternMatch, boolean useTrailingSlashMatch) {

		this(Arrays.asList(patterns), urlPathHelper, pathMatcher, useSuffixPatternMatch, useTrailingSlashMatch, null);
	}

	/**
	 * Creates a new instance with the given URL patterns.
	 * Each pattern that is not empty and does not start with "/" is pre-pended with "/".
	 * @param patterns the URL patterns to use; if 0, the condition will match to every request.
	 * @param urlPathHelper a {@link UrlPathHelper} for determining the lookup path for a request
	 * @param pathMatcher a {@link PathMatcher} for pattern path matching
	 * @param useSuffixPatternMatch whether to enable matching by suffix (".*")
	 * @param useTrailingSlashMatch whether to match irrespective of a trailing slash
	 * @param fileExtensions a list of file extensions to consider for path matching
	 */
	public PatternsRequestCondition(String[] patterns, @Nullable UrlPathHelper urlPathHelper,
			@Nullable PathMatcher pathMatcher, boolean useSuffixPatternMatch,
			boolean useTrailingSlashMatch, @Nullable List<String> fileExtensions) {

		this(Arrays.asList(patterns), urlPathHelper, pathMatcher, useSuffixPatternMatch,
				useTrailingSlashMatch, fileExtensions);
	}

	/**
	 * Private constructor accepting a collection of patterns.
	 */
	private PatternsRequestCondition(Collection<String> patterns, @Nullable UrlPathHelper urlPathHelper,
			@Nullable PathMatcher pathMatcher, boolean useSuffixPatternMatch,
			boolean useTrailingSlashMatch, @Nullable List<String> fileExtensions) {

		this.patterns = Collections.unmodifiableSet(prependLeadingSlash(patterns));
		this.pathHelper = urlPathHelper != null ? urlPathHelper : new UrlPathHelper();
		this.pathMatcher = pathMatcher != null ? pathMatcher : new AntPathMatcher();
		this.useSuffixPatternMatch = useSuffixPatternMatch;
		this.useTrailingSlashMatch = useTrailingSlashMatch;

		if (fileExtensions != null) {
			for (String fileExtension : fileExtensions) {
				if (fileExtension.charAt(0) != '.') {
					fileExtension = "." + fileExtension;
				}
				this.fileExtensions.add(fileExtension);
			}
		}
	}

	/**
	 * Private constructor for use when combining and matching.
	 */
	private PatternsRequestCondition(Set<String> patterns, PatternsRequestCondition other) {
		this.patterns = patterns;
		this.pathHelper = other.pathHelper;
		this.pathMatcher = other.pathMatcher;
		this.useSuffixPatternMatch = other.useSuffixPatternMatch;
		this.useTrailingSlashMatch = other.useTrailingSlashMatch;
		this.fileExtensions.addAll(other.fileExtensions);
	}


	private static Set<String> prependLeadingSlash(Collection<String> patterns) {
		if (patterns.isEmpty()) {
			return Collections.singleton("");
		}
		Set<String> result = new LinkedHashSet<>(patterns.size());
		for (String pattern : patterns) {
			if (StringUtils.hasLength(pattern) && !pattern.startsWith("/")) {
				pattern = "/" + pattern;
			}
			result.add(pattern);
		}
		return result;
	}

	public Set<String> getPatterns() {
		return this.patterns;
	}

	@Override
	protected Collection<String> getContent() {
		return this.patterns;
	}

	@Override
	protected String getToStringInfix() {
		return " || ";
	}

	/**
	 * Returns a new instance with URL patterns from the current instance ("this") and
	 * the "other" instance as follows:
	 * <ul>
	 * <li>If there are patterns in both instances, combine the patterns in "this" with
	 * the patterns in "other" using {@link PathMatcher#combine(String, String)}.
	 * <li>If only one instance has patterns, use them.
	 * <li>If neither instance has patterns, use an empty String (i.e. "").
	 * </ul>
	 */
	@Override
	public PatternsRequestCondition combine(PatternsRequestCondition other) {
		Set<String> result = new LinkedHashSet<>();
		if (!this.patterns.isEmpty() && !other.patterns.isEmpty()) {
			for (String pattern1 : this.patterns) {
				for (String pattern2 : other.patterns) {
					result.add(this.pathMatcher.combine(pattern1, pattern2));
				}
			}
		}
		else if (!this.patterns.isEmpty()) {
			result.addAll(this.patterns);
		}
		else if (!other.patterns.isEmpty()) {
			result.addAll(other.patterns);
		}
		else {
			result.add("");
		}
		return new PatternsRequestCondition(result, this);
	}

	/**
	 * Checks if any of the patterns match the given request and returns an instance
	 * that is guaranteed to contain matching patterns, sorted via
	 * {@link PathMatcher#getPatternComparator(String)}.
	 * <p>A matching pattern is obtained by making checks in the following order:
	 * <ul>
	 * <li>Direct match
	 * <li>Pattern match with ".*" appended if the pattern doesn't already contain a "."
	 * <li>Pattern match
	 * <li>Pattern match with "/" appended if the pattern doesn't already end in "/"
	 * </ul>
	 * @param request the current request
	 * @return the same instance if the condition contains no patterns;
	 * or a new condition with sorted matching patterns;
	 * or {@code null} if no patterns match.
	 */
	@Override
	@Nullable
	public PatternsRequestCondition getMatchingCondition(HttpServletRequest request) {
		// patterns表示此MappingInfo可以匹配的值们。一般对应@RequestMapping注解上的patters数组的值
		if (this.patterns.isEmpty()) {
			return this;
		}
		// 拿到待匹配的值，比如此处为"/hello.json"
		String lookupPath = this.pathHelper.getLookupPathForRequest(request, HandlerMapping.LOOKUP_PATH);
		// 最主要就是这个方法了，它拿着这个lookupPath匹配~~~~
		List<String> matches = getMatchingPatterns(lookupPath);
		// 此处如果为empty，就返回null了
		return !matches.isEmpty() ? new PatternsRequestCondition(new LinkedHashSet<>(matches), this) : null;
	}

	/**
	 * Find the patterns matching the given lookup path. Invoking this method should
	 * yield results equivalent to those of calling {@link #getMatchingCondition}.
	 * This method is provided as an alternative to be used if no request is available
	 * (e.g. introspection, tooling, etc).
	 * @param lookupPath the lookup path to match to existing patterns
	 * @return a collection of matching patterns sorted with the closest match at the top
	 */
	public List<String> getMatchingPatterns(String lookupPath) {
		List<String> matches = null;
		for (String pattern : this.patterns) {
			// 拿着lookupPath和pattern看它俩合拍不
			String match = getMatchingPattern(pattern, lookupPath);
			if (match != null) {
				matches = matches != null ? matches : new ArrayList<>();
				matches.add(match);
			}
		}
		if (matches == null) {
			return Collections.emptyList();
		}
		// 解释一下为何匹配的可能是多个。因为url匹配上了，但是还有可能@RequestMapping的其余属性匹配不上啊，所以此处需要注意  是可能匹配上多个的  最终是唯一匹配就成
		if (matches.size() > 1) {
			matches.sort(this.pathMatcher.getPatternComparator(lookupPath));
		}
		return matches;
	}

	// // ===============url的真正匹配规则  非常重要~~~===============
	// 注意这个方法的取名，上面是负数，这里是单数~~~~命名规范也是有艺术感的
	@Nullable
	private String getMatchingPattern(String pattern, String lookupPath) {
		// 完全相等，那就不继续聊了
		if (pattern.equals(lookupPath)) {
			return pattern;
		}

		// 注意了：useSuffixPatternMatch 这个属性就是我们最终要关闭后缀匹配的关键
		// 这个值默外部给传的true（其实内部默认值是boolean类型为false）
		if (this.useSuffixPatternMatch) {

			// 这个意思是若useSuffixPatternMatch=true我们支持后缀匹配。我们还可以配置fileExtensions让只支持我们自定义的指定的后缀匹配，而不是下面最终的.*全部支持
			if (!this.fileExtensions.isEmpty() && lookupPath.indexOf('.') != -1) {
				for (String extension : this.fileExtensions) {
					if (this.pathMatcher.match(pattern + extension, lookupPath)) {
						return pattern + extension;
					}
				}
			}
			// 若你没有配置指定后缀匹配，并且你的handler也没有.*这样匹配的，那就默认你的pattern就给你添加上后缀".*"，表示匹配所有请求的url的后缀
			else {
				boolean hasSuffix = pattern.indexOf('.') != -1;
				if (!hasSuffix && this.pathMatcher.match(pattern + ".*", lookupPath)) {
					return pattern + ".*";
				}
			}
		}
		// 若匹配上了 直接返回此patter
		if (this.pathMatcher.match(pattern, lookupPath)) {
			return pattern;
		}
		// 这又是它支持的匹配规则。默认useTrailingSlashMatch它也是true
		// 这就是为何我们的/hello/也能匹配上/hello的原因
		// 从这可以看出，Spring MVC的宽容度是很高的，容错处理做得是非常不错的
		if (this.useTrailingSlashMatch) {
			if (!pattern.endsWith("/") && this.pathMatcher.match(pattern + "/", lookupPath)) {
				return pattern + "/";
			}
		}
		/**
		 * 分析了URL的匹配原因，现在肯定知道为何默认情况下"/hello.aaaa"或者"/hello.aaaa/“或者”"/hello/""能匹配上我们/hello的原因了吧
		 * Spring和SpringBoot中如何关闭此项功能呢？
		 * 为何要关闭的理由，上面其实已经说了。当我们涉及到严格的权限校验（强权限控制）的时候。特备是一些银行系统、资产系统等等，关闭后缀匹配事非常有必要的。
		 *
		 * @Configuration
		 * @EnableWebMvc
		 * public class WebMvcConfig extends WebMvcConfigurerAdapter {
		 *
		 *     // 关闭后缀名匹配，关闭最后一个/匹配
		 *     @Override
		 *     public void configurePathMatch(PathMatchConfigurer configurer) {
		 *         configurer.setUseSuffixPatternMatch(false);
		 *         configurer.setUseTrailingSlashMatch(false);
		 *     }
		 * }
		 */
		return null;
	}

	/**
	 * Compare the two conditions based on the URL patterns they contain.
	 * Patterns are compared one at a time, from top to bottom via
	 * {@link PathMatcher#getPatternComparator(String)}. If all compared
	 * patterns match equally, but one instance has more patterns, it is
	 * considered a closer match.
	 * <p>It is assumed that both instances have been obtained via
	 * {@link #getMatchingCondition(HttpServletRequest)} to ensure they
	 * contain only patterns that match the request and are sorted with
	 * the best matches on top.
	 */
	@Override
	public int compareTo(PatternsRequestCondition other, HttpServletRequest request) {
		String lookupPath = this.pathHelper.getLookupPathForRequest(request, HandlerMapping.LOOKUP_PATH);
		Comparator<String> patternComparator = this.pathMatcher.getPatternComparator(lookupPath);
		Iterator<String> iterator = this.patterns.iterator();
		Iterator<String> iteratorOther = other.patterns.iterator();
		while (iterator.hasNext() && iteratorOther.hasNext()) {
			int result = patternComparator.compare(iterator.next(), iteratorOther.next());
			if (result != 0) {
				return result;
			}
		}
		if (iterator.hasNext()) {
			return -1;
		}
		else if (iteratorOther.hasNext()) {
			return 1;
		}
		else {
			return 0;
		}
	}

}
