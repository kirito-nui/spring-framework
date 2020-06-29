/*
 * Copyright 2002-2020 the original author or authors.
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

package org.springframework.web.servlet.handler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import kotlin.reflect.KFunction;
import kotlin.reflect.jvm.ReflectJvmMapping;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.KotlinDetector;
import org.springframework.core.MethodIntrospector;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

/**
 * Abstract base class for {@link HandlerMapping} implementations that define
 * a mapping between a request and a {@link HandlerMethod}.
 *
 * <p>For each registered handler method, a unique mapping is maintained with
 * subclasses defining the details of the mapping type {@code <T>}.
 *
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 3.1
 * @param <T> the mapping for a {@link HandlerMethod} containing the conditions
 * needed to match the handler method to an incoming request.
 *
 *
 *           以 Method 作为 Handler 的 HandlerMapping 抽象类，提供 Mapping 的初始化、注册等通用的骨架方法。这就是我们常说的“模板方法模式” 。
 *
 *
 */
public abstract class AbstractHandlerMethodMapping<T> extends AbstractHandlerMapping implements InitializingBean {

	/**
	 * Bean name prefix for target beans behind scoped proxies. Used to exclude those
	 * targets from handler method detection, in favor of the corresponding proxies.
	 * <p>We're not checking the autowire-candidate status here, which is how the
	 * proxy target filtering problem is being handled at the autowiring level,
	 * since autowire-candidate may have been turned to {@code false} for other
	 * reasons, while still expecting the bean to be eligible for handler methods.
	 * <p>Originally defined in {@link org.springframework.aop.scope.ScopedProxyUtils}
	 * but duplicated here to avoid a hard dependency on the spring-aop module.
	 */
	private static final String SCOPED_TARGET_NAME_PREFIX = "scopedTarget.";

	private static final HandlerMethod PREFLIGHT_AMBIGUOUS_MATCH =
			new HandlerMethod(new EmptyHandler(), ClassUtils.getMethod(EmptyHandler.class, "handle"));

	// 跨域相关
	private static final CorsConfiguration ALLOW_CORS_CONFIG = new CorsConfiguration();

	static {
		ALLOW_CORS_CONFIG.addAllowedOrigin("*");
		ALLOW_CORS_CONFIG.addAllowedMethod("*");
		ALLOW_CORS_CONFIG.addAllowedHeader("*");
		ALLOW_CORS_CONFIG.setAllowCredentials(true);
	}


	// 默认不会去祖先容器里面找Handlers
	private boolean detectHandlerMethodsInAncestorContexts = false;

	/**
	 * Mapping 命名策略
	 * 为处HandlerMethod的映射分配名称的策略接口   只有一个方法getName()
	 * 唯一实现为：RequestMappingInfoHandlerMethodMappingNamingStrategy
	 * 策略为：@RequestMapping指定了name属性，那就以指定的为准  否则策略为：取出Controller所有的`大写字母` + # + method.getName()
	 * 如：AppoloController#match方法  最终的name为：AC#match
	 * 当然这个你也可以自己实现这个接口，然后set进来即可（只是一般没啥必要这么去干~~）
	 */
	@Nullable
	private HandlerMethodMappingNamingStrategy<T> namingStrategy;

	/**
	 * Mapping 注册表
	 *
	 *
	 * 路由表,其包含着Spring MVC所管理的所有映射关系,并且运用读写锁提供并发访问能力
	 */
	private final MappingRegistry mappingRegistry = new MappingRegistry();


	/**
	 * Whether to detect handler methods in beans in ancestor ApplicationContexts.
	 * <p>Default is "false": Only beans in the current ApplicationContext are
	 * considered, i.e. only in the context that this HandlerMapping itself
	 * is defined in (typically the current DispatcherServlet's context).
	 * <p>Switch this flag on to detect handler beans in ancestor contexts
	 * (typically the Spring root WebApplicationContext) as well.
	 * @see #getCandidateBeanNames()
	 */
	public void setDetectHandlerMethodsInAncestorContexts(boolean detectHandlerMethodsInAncestorContexts) {
		this.detectHandlerMethodsInAncestorContexts = detectHandlerMethodsInAncestorContexts;
	}

	/**
	 * Configure the naming strategy to use for assigning a default name to every
	 * mapped handler method.
	 * <p>The default naming strategy is based on the capital letters of the
	 * class name followed by "#" and then the method name, e.g. "TC#getFoo"
	 * for a class named TestController with method getFoo.
	 */
	public void setHandlerMethodMappingNamingStrategy(HandlerMethodMappingNamingStrategy<T> namingStrategy) {
		this.namingStrategy = namingStrategy;
	}

	/**
	 * Return the configured naming strategy or {@code null}.
	 */
	@Nullable
	public HandlerMethodMappingNamingStrategy<T> getNamingStrategy() {
		return this.namingStrategy;
	}

	/**
	 * Return a (read-only) map with all mappings and HandlerMethod's.
	 *
	 * 此处细节：使用的是读写锁  比如此处使用的是读锁   获得所有的注册进去的Handler的Map
	 */
	public Map<T, HandlerMethod> getHandlerMethods() {
		this.mappingRegistry.acquireReadLock();
		try {
			return Collections.unmodifiableMap(this.mappingRegistry.getMappings());
		}
		finally {
			this.mappingRegistry.releaseReadLock();
		}
	}

	/**
	 * Return the handler methods for the given mapping name.
	 * @param mappingName the mapping name
	 * @return a list of matching HandlerMethod's or {@code null}; the returned
	 * list will never be modified and is safe to iterate.
	 * @see #setHandlerMethodMappingNamingStrategy
	 *
	 *
	 *
	 * 此处是根据mappingName来获取一个Handler
	 */
	@Nullable
	public List<HandlerMethod> getHandlerMethodsForMappingName(String mappingName) {
		return this.mappingRegistry.getHandlerMethodsByMappingName(mappingName);
	}

	/**
	 * Return the internal mapping registry. Provided for testing purposes.
	 */
	MappingRegistry getMappingRegistry() {
		return this.mappingRegistry;
	}

	/**
	 * Register the given mapping.
	 * <p>This method may be invoked at runtime after initialization has completed.
	 * @param mapping the mapping for the handler method
	 * @param handler the handler
	 * @param method the method
	 *
	 *
	 *
	 *              最终都是委托给mappingRegistry去做了注册的工作   此处日志级别为trace级别
	 */
	public void registerMapping(T mapping, Object handler, Method method) {
		if (logger.isTraceEnabled()) {
			logger.trace("Register \"" + mapping + "\" to " + method.toGenericString());
		}
		this.mappingRegistry.register(mapping, handler, method);
	}

	/**
	 * Un-register the given mapping.
	 * <p>This method may be invoked at runtime after initialization has completed.
	 * @param mapping the mapping to unregister
	 */
	public void unregisterMapping(T mapping) {
		if (logger.isTraceEnabled()) {
			logger.trace("Unregister mapping \"" + mapping + "\"");
		}
		this.mappingRegistry.unregister(mapping);
	}


	// Handler method detection

	/**
	 * Detects handler methods at initialization.
	 * @see #initHandlerMethods
	 */
	@Override
	public void afterPropertiesSet() {
		initHandlerMethods();
	}

	/**
	 * Scan beans in the ApplicationContext, detect and register handler methods.
	 * @see #getCandidateBeanNames()
	 * @see #processCandidateBean
	 * @see #handlerMethodsInitialized
	 */
	protected void initHandlerMethods() {
		// <1.1> 遍历 Bean ，逐个处理
		// getCandidateBeanNames：Object.class相当于拿到当前容器（一般都是当前容器） 内所有的Bean定义信息
		// 如果阁下容器隔离到的话，这里一般只会拿到@Controller标注的web组件  以及其它相关web组件的  不会非常的多的~~~~
		for (String beanName : getCandidateBeanNames()) {
			if (!beanName.startsWith(SCOPED_TARGET_NAME_PREFIX)) {
				// <1.2> 处理 Bean
				// 会在每个Bean里面找处理方法，HandlerMethod，然后注册进去
				processCandidateBean(beanName);
			}
		}
		// <2> 初始化处理器的方法们。目前是空方法，暂无具体的实现
		handlerMethodsInitialized(getHandlerMethods());
	}

	/**
	 * Determine the names of candidate beans in the application context.
	 * @since 5.1
	 * @see #setDetectHandlerMethodsInAncestorContexts
	 * @see BeanFactoryUtils#beanNamesForTypeIncludingAncestors
	 */
	protected String[] getCandidateBeanNames() {
		return (this.detectHandlerMethodsInAncestorContexts ?
				BeanFactoryUtils.beanNamesForTypeIncludingAncestors(obtainApplicationContext(), Object.class) :
				obtainApplicationContext().getBeanNamesForType(Object.class));
	}

	/**
	 * Determine the type of the specified candidate bean and call
	 * {@link #detectHandlerMethods} if identified as a handler type.
	 * <p>This implementation avoids bean creation through checking
	 * {@link org.springframework.beans.factory.BeanFactory#getType}
	 * and calling {@link #detectHandlerMethods} with the bean name.
	 * @param beanName the name of the candidate bean
	 * @since 5.1
	 * @see #isHandler
	 * @see #detectHandlerMethods
	 *
	 *
	 * 确定指定的候选bean的类型，如果标识为Handler类型，则调用DetectHandlerMethods
	 * isHandler(beanType):判断这个type是否为Handler类型   它是个抽象方法，由子类去决定到底啥才叫Handler~~~~
	 * `RequestMappingHandlerMapping`的判断依据为：该类上标注了@Controller注解或者@RequestMapping注解  就算作是一个Handler
	 * 所以此处：@Controller起到了一个特殊的作用，不能等价于@Component的哟~~~~
	 */
	protected void processCandidateBean(String beanName) {
		Class<?> beanType = null;
		try {
			beanType = obtainApplicationContext().getType(beanName);
		}
		catch (Throwable ex) {
			// An unresolvable bean type, probably from a lazy bean - let's ignore it.
			if (logger.isTraceEnabled()) {
				logger.trace("Could not resolve type for bean '" + beanName + "'", ex);
			}
		}
		// 判断 Bean 是否为处理器，如果是，则扫描处理器方法
		if (beanType != null && isHandler(beanType)) {
			//都属于detect探测系列
			detectHandlerMethods(beanName);
		}
	}

	/**
	 * Look for handler methods in the specified handler bean.
	 * @param handler either a bean name or an actual handler instance
	 * @see #getMappingForMethod
	 *
	 *
	 *
	 * 在指定的Handler的bean中查找处理程序方法Methods  找打就注册进去：mappingRegistry
	 */
	protected void detectHandlerMethods(Object handler) {
		// <1> 获得处理器类型
		Class<?> handlerType = (handler instanceof String ?
				obtainApplicationContext().getType((String) handler) : handler.getClass());

		if (handlerType != null) {
			// <2> 获得真实的类。因为，handlerType 可能是代理类
			Class<?> userType = ClassUtils.getUserClass(handlerType);
			// <3> 获得匹配的方法的集合
			// getMappingForMethod属于一个抽象方法，由子类去决定它的寻找规则~~~~  什么才算作一个处理器方法
			// @RequestMapping 解析成的 RequestMappingInfo 对象
			Map<Method, T> methods = MethodIntrospector.selectMethods(userType,
					(MethodIntrospector.MetadataLookup<T>) method -> {
						try {
							return getMappingForMethod(method, userType);
						}
						catch (Throwable ex) {
							throw new IllegalStateException("Invalid mapping on handler class [" +
									userType.getName() + "]: " + method, ex);
						}
					});
			if (logger.isTraceEnabled()) {
				logger.trace(formatMappings(userType, methods));
			}
			// <4> 遍历方法，逐个注册 HandlerMethod
			// 把找到的Method  一个个遍历，注册进去~~~~
			methods.forEach((method, mapping) -> {
				Method invocableMethod = AopUtils.selectInvocableMethod(method, userType);
				registerHandlerMethod(handler, invocableMethod, mapping);
			});
		}
	}

	private String formatMappings(Class<?> userType, Map<Method, T> methods) {
		String formattedType = Arrays.stream(ClassUtils.getPackageName(userType).split("\\."))
				.map(p -> p.substring(0, 1))
				.collect(Collectors.joining(".", "", "." + userType.getSimpleName()));
		Function<Method, String> methodFormatter = method -> Arrays.stream(method.getParameterTypes())
				.map(Class::getSimpleName)
				.collect(Collectors.joining(",", "(", ")"));
		return methods.entrySet().stream()
				.map(e -> {
					Method method = e.getKey();
					return e.getValue() + ": " + method.getName() + methodFormatter.apply(method);
				})
				.collect(Collectors.joining("\n\t", "\n\t" + formattedType + ":" + "\n\t", ""));
	}

	/**
	 * Register a handler method and its unique mapping. Invoked at startup for
	 * each detected handler method.
	 * @param handler the bean name of the handler or the handler instance
	 * @param method the method to register
	 * @param mapping the mapping conditions associated with the handler method
	 * @throws IllegalStateException if another method was already registered
	 * under the same mapping
	 */
	protected void registerHandlerMethod(Object handler, Method method, T mapping) {
		this.mappingRegistry.register(mapping, handler, method);
	}

	/**
	 * Create the HandlerMethod instance.
	 * @param handler either a bean name or an actual handler instance
	 * @param method the target method
	 * @return the created HandlerMethod
	 */
	protected HandlerMethod createHandlerMethod(Object handler, Method method) {
		if (handler instanceof String) {
			return new HandlerMethod((String) handler,
					obtainApplicationContext().getAutowireCapableBeanFactory(), method);
		}
		return new HandlerMethod(handler, method);
	}

	/**
	 * Extract and return the CORS configuration for the mapping.
	 */
	@Nullable
	protected CorsConfiguration initCorsConfiguration(Object handler, Method method, T mapping) {
		return null;
	}

	/**
	 * Invoked after all handler methods have been detected.
	 * @param handlerMethods a read-only map with handler methods and mappings.
	 */
	protected void handlerMethodsInitialized(Map<T, HandlerMethod> handlerMethods) {
		// Total includes detected mappings + explicit registrations via registerMapping
		int total = handlerMethods.size();
		if ((logger.isTraceEnabled() && total == 0) || (logger.isDebugEnabled() && total > 0) ) {
			logger.debug(total + " mappings in " + formatMappingName());
		}
	}


	// Handler method lookup

	/**
	 * Look up a handler method for the given request.
	 */
	@Override
	protected HandlerMethod getHandlerInternal(HttpServletRequest request) throws Exception {
		// 取出具体的url信息
		String lookupPath = getUrlPathHelper().getLookupPathForRequest(request);
		request.setAttribute(LOOKUP_PATH, lookupPath);
		this.mappingRegistry.acquireReadLock();
		try {
			// 找到对应的方法
			// 委托给方法lookupHandlerMethod() 去找到一个HandlerMethod去最终处理~
			HandlerMethod handlerMethod = lookupHandlerMethod(lookupPath, request);
			// 返回对应的处理方法
			return (handlerMethod != null ? handlerMethod.createWithResolvedBean() : null);
		}
		finally {
			this.mappingRegistry.releaseReadLock();
		}
	}

	/**
	 * Look up the best-matching handler method for the current request.
	 * If multiple matches are found, the best match is selected.
	 * @param lookupPath mapping lookup path within the current servlet mapping
	 * @param request the current request
	 * @return the best-matching handler method, or {@code null} if no match
	 * @see #handleMatch(Object, String, HttpServletRequest)
	 * @see #handleNoMatch(Set, String, HttpServletRequest)
	 */
	@Nullable
	protected HandlerMethod lookupHandlerMethod(String lookupPath, HttpServletRequest request) throws Exception {
		// Match是一个private class，内部就两个属性：T mapping和HandlerMethod handlerMethod
		List<Match> matches = new ArrayList<>();
		// 从urlLookup中取出匹配结果,这里是直接匹配
		// 根据lookupPath去注册中心里查找mappingInfos，因为一个具体的url可能匹配上多个MappingInfo的
		// 至于为何是多值？有这么一种情况  URL都是/api/v1/hello  但是有的是get post delete等方法  当然还有可能是headers/consumes等等不一样，都算多个的  所以有可能是会匹配到多个MappingInfo的
		// 所有这个里可以匹配出多个出来。比如/hello 匹配出GET、POST、PUT都成，所以size可以为3
		List<T> directPathMatches = this.mappingRegistry.getMappingsByUrl(lookupPath);
		if (directPathMatches != null) {

			// 依赖于子类实现的抽象方法：getMatchingMapping()  看看到底匹不匹配，而不仅仅是URL匹配就行
			// 比如还有method、headers、consumes等等这些不同都代表着不同的MappingInfo的
			// 最终匹配上的，会new Match()放进matches里面去
			addMatchingMappings(directPathMatches, matches, request);
		}

		// 当还没有匹配上的时候，别无选择，只能浏览所有映射
		// 上述直接匹配不到则全部匹配(这里是坑)
		if (matches.isEmpty()) {
			// No choice but to go through all mappings...
			addMatchingMappings(this.mappingRegistry.getMappings().keySet(), matches, request);
		}

		// 匹配后进行筛选
		if (!matches.isEmpty()) {
			// getMappingComparator这个方法也是抽象方法由子类去实现的。
			// 比如：`RequestMappingInfoHandlerMapping`的实现为先比较Method，patterns、params
			Comparator<Match> comparator = new MatchComparator(getMappingComparator(request));
			matches.sort(comparator);
			// 默认排序后第一个为最佳匹配
			Match bestMatch = matches.get(0);
			if (matches.size() > 1) {
				if (logger.isTraceEnabled()) {
					logger.trace(matches.size() + " matching mappings: " + matches);
				}
				if (CorsUtils.isPreFlightRequest(request)) {
					return PREFLIGHT_AMBIGUOUS_MATCH;
				}
				// 不允许有两个一样的处理器

				Match secondBestMatch = matches.get(1);
				// 如果发现次最佳匹配和最佳匹配  比较是相等的  那就报错吧~~~~
				// Ambiguous handler methods mapped for~~~
				// 注意：这个是运行时的检查，在启动的时候是检查不出来的~~~  所以运行期的这个检查也是很有必要的~~~   否则就会出现意想不到的效果
				if (comparator.compare(bestMatch, secondBestMatch) == 0) {
					Method m1 = bestMatch.handlerMethod.getMethod();
					Method m2 = secondBestMatch.handlerMethod.getMethod();
					String uri = request.getRequestURI();
					throw new IllegalStateException(
							"Ambiguous handler methods mapped for '" + uri + "': {" + m1 + ", " + m2 + "}");
				}
			}
			// 把最最佳匹配的方法  放进request的属性里面~~~
			request.setAttribute(BEST_MATCHING_HANDLER_ATTRIBUTE, bestMatch.handlerMethod);
			// 它也是做了一件事：request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, lookupPath)
			handleMatch(bestMatch.mapping, lookupPath, request);
			// 最终返回的是HandlerMethod
			return bestMatch.handlerMethod;
		}
		else {
			// 匹配不到的处理一般是找出原因,抛出相应的异常
			return handleNoMatch(this.mappingRegistry.getMappings().keySet(), lookupPath, request);
		}
	}

	// 因为上面说了mappings可能会有多个，比如get post put的都算~~~这里就是要进行筛选出所有match上的
	private void addMatchingMappings(Collection<T> mappings, List<Match> matches, HttpServletRequest request) {
		for (T mapping : mappings) {
			// 只有RequestMappingInfoHandlerMapping 实现了一句话：return info.getMatchingCondition(request);
			// 因此RequestMappingInfo#getMatchingCondition() 方法里大有文章可为~~~
			// 它会对所有的methods、params、headers... 都进行匹配  但凡匹配不上的就返回null
			T match = getMatchingMapping(mapping, request);
			if (match != null) {
				matches.add(new Match(match, this.mappingRegistry.getMappings().get(mapping)));
			}
		}
	}

	/**
	 * Invoked when a matching mapping is found.
	 * @param mapping the matching mapping
	 * @param lookupPath mapping lookup path within the current servlet mapping
	 * @param request the current request
	 */
	protected void handleMatch(T mapping, String lookupPath, HttpServletRequest request) {
		request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, lookupPath);
	}

	/**
	 * Invoked when no matching mapping is not found.
	 * @param mappings all registered mappings
	 * @param lookupPath mapping lookup path within the current servlet mapping
	 * @param request the current request
	 * @throws ServletException in case of errors
	 */
	@Nullable
	protected HandlerMethod handleNoMatch(Set<T> mappings, String lookupPath, HttpServletRequest request)
			throws Exception {

		return null;
	}

	@Override
	protected boolean hasCorsConfigurationSource(Object handler) {
		return super.hasCorsConfigurationSource(handler) ||
				(handler instanceof HandlerMethod && this.mappingRegistry.getCorsConfiguration((HandlerMethod) handler) != null);
	}

	@Override
	protected CorsConfiguration getCorsConfiguration(Object handler, HttpServletRequest request) {
		CorsConfiguration corsConfig = super.getCorsConfiguration(handler, request);
		if (handler instanceof HandlerMethod) {
			HandlerMethod handlerMethod = (HandlerMethod) handler;
			if (handlerMethod.equals(PREFLIGHT_AMBIGUOUS_MATCH)) {
				return AbstractHandlerMethodMapping.ALLOW_CORS_CONFIG;
			}
			else {
				CorsConfiguration corsConfigFromMethod = this.mappingRegistry.getCorsConfiguration(handlerMethod);
				corsConfig = (corsConfig != null ? corsConfig.combine(corsConfigFromMethod) : corsConfigFromMethod);
			}
		}
		return corsConfig;
	}


	// Abstract template methods

	/**
	 * Whether the given type is a handler with handler methods.
	 * @param beanType the type of the bean being checked
	 * @return "true" if this a handler type, "false" otherwise.
	 */
	protected abstract boolean isHandler(Class<?> beanType);

	/**
	 * Provide the mapping for a handler method. A method for which no
	 * mapping can be provided is not a handler method.
	 * @param method the method to provide a mapping for
	 * @param handlerType the handler type, possibly a sub-type of the method's
	 * declaring class
	 * @return the mapping, or {@code null} if the method is not mapped
	 */
	@Nullable
	protected abstract T getMappingForMethod(Method method, Class<?> handlerType);

	/**
	 * Extract and return the URL paths contained in the supplied mapping.
	 */
	protected abstract Set<String> getMappingPathPatterns(T mapping);

	/**
	 * Check if a mapping matches the current request and return a (potentially
	 * new) mapping with conditions relevant to the current request.
	 * @param mapping the mapping to get a match for
	 * @param request the current HTTP servlet request
	 * @return the match, or {@code null} if the mapping doesn't match
	 */
	@Nullable
	protected abstract T getMatchingMapping(T mapping, HttpServletRequest request);

	/**
	 * Return a comparator for sorting matching mappings.
	 * The returned comparator should sort 'better' matches higher.
	 * @param request the current request
	 * @return the comparator (never {@code null})
	 */
	protected abstract Comparator<T> getMappingComparator(HttpServletRequest request);


	/**
	 * A registry that maintains all mappings to handler methods, exposing methods
	 * to perform lookups and providing concurrent access.
	 * <p>Package-private for testing purposes.
	 */
	class MappingRegistry {

		// mapping对应的其MappingRegistration对象~~~
		private final Map<T, MappingRegistration<T>> registry = new HashMap<>();

		// 保存着mapping和HandlerMethod的对应关系~
		private final Map<T, HandlerMethod> mappingLookup = new LinkedHashMap<>();

		// 保存着URL与匹配条件（mapping）的对应关系  当然这里的URL是pattern式的，可以使用通配符
		// 这里的Map不是普通的Map，而是MultiValueMap，它是个多值Map。其实它的value是一个list类型的值
		// 至于为何是多值？有这么一种情况  URL都是/api/v1/hello  但是有的是get post delete等方法   所以有可能是会匹配到多个MappingInfo的
		private final MultiValueMap<String, T> urlLookup = new LinkedMultiValueMap<>();

		// 这个Map是Spring MVC4.1新增的（毕竟这个策略接口HandlerMethodMappingNamingStrategy在Spring4.1后才有,这里的name是它生成出来的）
		// 保存着name和HandlerMethod的对应关系（一个name可以有多个HandlerMethod）
		private final Map<String, List<HandlerMethod>> nameLookup = new ConcurrentHashMap<>();

		private final Map<HandlerMethod, CorsConfiguration> corsLookup = new ConcurrentHashMap<>();

		private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

		/**
		 * Return all mappings and handler methods. Not thread-safe.
		 * @see #acquireReadLock()
		 */
		public Map<T, HandlerMethod> getMappings() {
			return this.mappingLookup;
		}

		/**
		 * Return matches for the given URL path. Not thread-safe.
		 * @see #acquireReadLock()
		 */
		@Nullable
		public List<T> getMappingsByUrl(String urlPath) {
			return this.urlLookup.get(urlPath);
		}

		/**
		 * Return handler methods by mapping name. Thread-safe for concurrent use.
		 */
		public List<HandlerMethod> getHandlerMethodsByMappingName(String mappingName) {
			return this.nameLookup.get(mappingName);
		}

		/**
		 * Return CORS configuration. Thread-safe for concurrent use.
		 */
		@Nullable
		public CorsConfiguration getCorsConfiguration(HandlerMethod handlerMethod) {
			HandlerMethod original = handlerMethod.getResolvedFromHandlerMethod();
			return this.corsLookup.get(original != null ? original : handlerMethod);
		}

		// 读锁提供给外部访问，写锁自己放在内部即可~~~
		/**
		 * Acquire the read lock when using getMappings and getMappingsByUrl.
		 */
		public void acquireReadLock() {
			this.readWriteLock.readLock().lock();
		}

		/**
		 * Release the read lock after using getMappings and getMappingsByUrl.
		 */
		public void releaseReadLock() {
			this.readWriteLock.readLock().unlock();
		}

		// 注册Mapping和handler 以及Method    此处上写锁保证线程安全~
		public void register(T mapping, Object handler, Method method) {
			// Assert that the handler method is not a suspending one.
			if (KotlinDetector.isKotlinType(method.getDeclaringClass()) && KotlinDelegate.isSuspend(method)) {
				throw new IllegalStateException("Unsupported suspending handler method detected: " + method);
			}
			// <1> 获得写锁
			this.readWriteLock.writeLock().lock();
			try {
				// 此处注意：都是new HandlerMethod()了一个新的出来~~~~
				HandlerMethod handlerMethod = createHandlerMethod(handler, method);
				// 同样的：一个URL Mapping只能对应一个Handler
				// 这里可能会出现常见的一个异常信息：Ambiguous mapping. Cannot map XXX
				validateMethodMapping(handlerMethod, mapping);

				// 缓存Mapping和handlerMethod的关系
				this.mappingLookup.put(mapping, handlerMethod);

				// 保存url和RequestMappingInfo（mapping）对应关系
				// 这里注意：多个url可能对应着同一个mappingInfo呢~  毕竟@RequestMapping的url是可以写多个的
				// @RequestMapping({"/params", "/"})
				List<String> directUrls = getDirectUrls(mapping);
				for (String url : directUrls) {
					this.urlLookup.add(url, mapping);
				}

				// 保存name和handlerMethod的关系  同样也是一对多
				String name = null;
				if (getNamingStrategy() != null) {
					name = getNamingStrategy().getName(handlerMethod, mapping);
					addMappingName(name, handlerMethod);
				}

				CorsConfiguration corsConfig = initCorsConfiguration(handler, method, mapping);
				if (corsConfig != null) {
					this.corsLookup.put(handlerMethod, corsConfig);
				}

				// 注册mapping和MappingRegistration的关系
				this.registry.put(mapping, new MappingRegistration<>(mapping, handlerMethod, directUrls, name));
			}
			finally {
				this.readWriteLock.writeLock().unlock();
			}
		}

		private void validateMethodMapping(HandlerMethod handlerMethod, T mapping) {
			// Assert that the supplied mapping is unique.
			HandlerMethod existingHandlerMethod = this.mappingLookup.get(mapping);
			if (existingHandlerMethod != null && !existingHandlerMethod.equals(handlerMethod)) {
				throw new IllegalStateException(
						"Ambiguous mapping. Cannot map '" + handlerMethod.getBean() + "' method \n" +
						handlerMethod + "\nto " + mapping + ": There is already '" +
						existingHandlerMethod.getBean() + "' bean method\n" + existingHandlerMethod + " mapped.");
			}
		}

		private List<String> getDirectUrls(T mapping) {
			List<String> urls = new ArrayList<>(1);
			for (String path : getMappingPathPatterns(mapping)) {
				if (!getPathMatcher().isPattern(path)) {
					urls.add(path);
				}
			}
			return urls;
		}

		private void addMappingName(String name, HandlerMethod handlerMethod) {
			List<HandlerMethod> oldList = this.nameLookup.get(name);
			if (oldList == null) {
				oldList = Collections.emptyList();
			}

			for (HandlerMethod current : oldList) {
				if (handlerMethod.equals(current)) {
					return;
				}
			}

			List<HandlerMethod> newList = new ArrayList<>(oldList.size() + 1);
			newList.addAll(oldList);
			newList.add(handlerMethod);
			this.nameLookup.put(name, newList);
		}

		public void unregister(T mapping) {
			this.readWriteLock.writeLock().lock();
			try {
				MappingRegistration<T> definition = this.registry.remove(mapping);
				if (definition == null) {
					return;
				}

				this.mappingLookup.remove(definition.getMapping());

				for (String url : definition.getDirectUrls()) {
					List<T> list = this.urlLookup.get(url);
					if (list != null) {
						list.remove(definition.getMapping());
						if (list.isEmpty()) {
							this.urlLookup.remove(url);
						}
					}
				}

				removeMappingName(definition);

				this.corsLookup.remove(definition.getHandlerMethod());
			}
			finally {
				this.readWriteLock.writeLock().unlock();
			}
		}

		private void removeMappingName(MappingRegistration<T> definition) {
			String name = definition.getMappingName();
			if (name == null) {
				return;
			}
			HandlerMethod handlerMethod = definition.getHandlerMethod();
			List<HandlerMethod> oldList = this.nameLookup.get(name);
			if (oldList == null) {
				return;
			}
			if (oldList.size() <= 1) {
				this.nameLookup.remove(name);
				return;
			}
			List<HandlerMethod> newList = new ArrayList<>(oldList.size() - 1);
			for (HandlerMethod current : oldList) {
				if (!current.equals(handlerMethod)) {
					newList.add(current);
				}
			}
			this.nameLookup.put(name, newList);
		}
	}


	private static class MappingRegistration<T> {

		private final T mapping;

		private final HandlerMethod handlerMethod;

		/**
		 * 直接 URL 数组
		 */
		private final List<String> directUrls;

		/**
		 * {@link #mapping} 的名字
		 */
		@Nullable
		private final String mappingName;

		public MappingRegistration(T mapping, HandlerMethod handlerMethod,
				@Nullable List<String> directUrls, @Nullable String mappingName) {

			Assert.notNull(mapping, "Mapping must not be null");
			Assert.notNull(handlerMethod, "HandlerMethod must not be null");
			this.mapping = mapping;
			this.handlerMethod = handlerMethod;
			this.directUrls = (directUrls != null ? directUrls : Collections.emptyList());
			this.mappingName = mappingName;
		}

		public T getMapping() {
			return this.mapping;
		}

		public HandlerMethod getHandlerMethod() {
			return this.handlerMethod;
		}

		public List<String> getDirectUrls() {
			return this.directUrls;
		}

		@Nullable
		public String getMappingName() {
			return this.mappingName;
		}
	}


	/**
	 * A thin wrapper around a matched HandlerMethod and its mapping, for the purpose of
	 * comparing the best match with a comparator in the context of the current request.
	 */
	private class Match {

		private final T mapping;

		private final HandlerMethod handlerMethod;

		public Match(T mapping, HandlerMethod handlerMethod) {
			this.mapping = mapping;
			this.handlerMethod = handlerMethod;
		}

		@Override
		public String toString() {
			return this.mapping.toString();
		}
	}


	private class MatchComparator implements Comparator<Match> {

		private final Comparator<T> comparator;

		public MatchComparator(Comparator<T> comparator) {
			this.comparator = comparator;
		}

		@Override
		public int compare(Match match1, Match match2) {
			return this.comparator.compare(match1.mapping, match2.mapping);
		}
	}


	private static class EmptyHandler {

		@SuppressWarnings("unused")
		public void handle() {
			throw new UnsupportedOperationException("Not implemented");
		}
	}

	/**
	 * Inner class to avoid a hard dependency on Kotlin at runtime.
	 */
	private static class KotlinDelegate {

		static private boolean isSuspend(Method method) {
			KFunction<?> function = ReflectJvmMapping.getKotlinFunction(method);
			return function != null && function.isSuspend();
		}
	}

}
